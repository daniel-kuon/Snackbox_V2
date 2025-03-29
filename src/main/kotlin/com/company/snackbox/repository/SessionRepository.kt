package com.company.snackbox.repository

import com.company.snackbox.database.BarcodeScansTable
import com.company.snackbox.database.SessionsTable
import com.company.snackbox.model.Barcode
import com.company.snackbox.model.BarcodeScanned
import com.company.snackbox.model.Session
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Repository for Session operations
 */
class SessionRepository {

    /**
     * Creates a new session in the database
     *
     * @param session Session to create
     * @return Created session with ID
     */
    fun create(session: Session): Session {
        logger.info { "Creating session for user: ${session.userId}" }

        val id = transaction {
            // Insert session
            val sessionId = SessionsTable.insert {
                it[SessionsTable.id] = session.id
                it[SessionsTable.userId] = session.userId
                it[SessionsTable.startTime] = session.startTime
                it[SessionsTable.endTime] = session.endTime
                it[SessionsTable.totalAmount] = session.totalAmount
            } get SessionsTable.id

            // Insert barcode scans
            session.scans.forEach { scan ->
                BarcodeScansTable.insert {
                    it[BarcodeScansTable.id] = scan.id
                    it[BarcodeScansTable.sessionId] = session.id
                    it[BarcodeScansTable.barcodeId] = scan.barcodeId
                    it[BarcodeScansTable.scanTime] = scan.scanTime
                    it[BarcodeScansTable.amount] = scan.amount
                }
            }

            sessionId
        }

        return session.copy(id = id.value)
    }

    /**
     * Updates an existing session in the database
     *
     * @param session Session to update
     * @return Updated session
     */
    fun update(session: Session): Session {
        logger.info { "Updating session: ${session.id}" }

        transaction {
            // Update session
            SessionsTable.update({ SessionsTable.id eq session.id }) {
                it[SessionsTable.endTime] = session.endTime
                it[SessionsTable.totalAmount] = session.totalAmount
            }

            // Get existing scans
            val existingScans = BarcodeScansTable
                .select { BarcodeScansTable.sessionId eq session.id }
                .map { it[BarcodeScansTable.id].value }
                .toSet()

            // Insert new scans
            session.scans
                .filter { !existingScans.contains(it.id) }
                .forEach { scan ->
                    BarcodeScansTable.insert {
                        it[BarcodeScansTable.id] = scan.id
                        it[BarcodeScansTable.sessionId] = session.id
                        it[BarcodeScansTable.barcodeId] = scan.barcodeId
                        it[BarcodeScansTable.scanTime] = scan.scanTime
                        it[BarcodeScansTable.amount] = scan.amount
                    }
                }
        }

        return session
    }

    /**
     * Ends a session
     *
     * @param id ID of the session to end
     * @param endTime When the session ended
     * @return true if the session was ended, false otherwise
     */
    fun endSession(id: UUID, endTime: LocalDateTime = LocalDateTime.now()): Boolean {
        logger.info { "Ending session: $id" }

        val updatedCount = transaction {
            SessionsTable.update({ 
                (SessionsTable.id eq id) and 
                SessionsTable.endTime.isNull()
            }) {
                it[SessionsTable.endTime] = endTime
            }
        }

        return updatedCount > 0
    }

    /**
     * Deletes a session from the database
     *
     * @param id ID of the session to delete
     * @return true if the session was deleted, false otherwise
     */
    fun delete(id: UUID): Boolean {
        logger.info { "Deleting session: $id" }

        val deletedCount = transaction {
            // Delete barcode scans first
            BarcodeScansTable.deleteWhere { BarcodeScansTable.sessionId eq id }

            // Then delete session
            SessionsTable.deleteWhere { SessionsTable.id eq id }
        }

        return deletedCount > 0
    }

    /**
     * Finds a session by ID
     *
     * @param id ID of the session to find
     * @return Session if found, null otherwise
     */
    fun findById(id: UUID): Session? {
        logger.debug { "Finding session by ID: $id" }

        return transaction {
            val session = SessionsTable.select { SessionsTable.id eq id }
                .mapNotNull { it.toSession() }
                .singleOrNull() ?: return@transaction null

            val scans = BarcodeScansTable
                .select { BarcodeScansTable.sessionId eq id }
                .mapNotNull { it.toBarcodeScan() }

            session.copy(scans = scans)
        }
    }

    /**
     * Finds the active session for a user
     *
     * @param userId ID of the user
     * @return Active session if found, null otherwise
     */
    fun findActiveSessionForUser(userId: UUID): Session? {
        logger.debug { "Finding active session for user: $userId" }

        return transaction {
            val session = SessionsTable
                .select { 
                    (SessionsTable.userId eq userId) and 
                    SessionsTable.endTime.isNull() 
                }
                .mapNotNull { it.toSession() }
                .singleOrNull() ?: return@transaction null

            val scans = BarcodeScansTable
                .select { BarcodeScansTable.sessionId eq session.id }
                .mapNotNull { it.toBarcodeScan() }

            session.copy(scans = scans)
        }
    }

    /**
     * Lists all sessions for a user
     *
     * @param userId ID of the user
     * @param limit Maximum number of sessions to return (0 for all)
     * @return List of sessions for the user
     */
    fun findByUserId(userId: UUID, limit: Int = 0): List<Session> {
        logger.debug { "Finding sessions for user: $userId, limit: $limit" }

        return transaction {
            // Get sessions
            val query = SessionsTable
                .select { SessionsTable.userId eq userId }
                .orderBy(SessionsTable.startTime, SortOrder.DESC)

            val limitedQuery = if (limit > 0) query.limit(limit) else query

            val sessions = limitedQuery.mapNotNull { it.toSession() }

            // Get scans for each session
            sessions.map { session ->
                val scans = BarcodeScansTable
                    .select { BarcodeScansTable.sessionId eq session.id }
                    .mapNotNull { it.toBarcodeScan() }

                session.copy(scans = scans)
            }
        }
    }

    /**
     * Adds a barcode scan to a session
     *
     * @param sessionId ID of the session
     * @param barcode Barcode that was scanned
     * @param scanTime When the barcode was scanned
     * @return Updated session
     */
    fun addScan(sessionId: UUID, barcode: Barcode, scanTime: LocalDateTime = LocalDateTime.now()): Session? {
        logger.info { "Adding scan to session: $sessionId, barcode: ${barcode.code}" }

        return transaction {
            // Get session
            val session = SessionsTable
                .select { SessionsTable.id eq sessionId }
                .mapNotNull { it.toSession() }
                .singleOrNull() ?: return@transaction null

            // Check if session is active
            if (session.endTime != null) {
                logger.warn { "Cannot add scan to inactive session: $sessionId" }
                return@transaction null
            }

            // Create scan
            val scan = BarcodeScanned(
                barcodeId = barcode.id,
                scanTime = scanTime,
                amount = barcode.amount
            )

            // Insert scan
            BarcodeScansTable.insert {
                it[BarcodeScansTable.id] = scan.id
                it[BarcodeScansTable.sessionId] = sessionId
                it[BarcodeScansTable.barcodeId] = scan.barcodeId
                it[BarcodeScansTable.scanTime] = scan.scanTime
                it[BarcodeScansTable.amount] = scan.amount
            }

            // Update session total amount if payment barcode
            if (barcode.type == com.company.snackbox.model.BarcodeType.PAYMENT && barcode.amount != null) {
                SessionsTable.update({ SessionsTable.id eq sessionId }) {
                    it[SessionsTable.totalAmount] = session.totalAmount + barcode.amount
                }
            }

            // Get updated session with scans
            val updatedSession = SessionsTable
                .select { SessionsTable.id eq sessionId }
                .mapNotNull { it.toSession() }
                .single()

            val scans = BarcodeScansTable
                .select { BarcodeScansTable.sessionId eq sessionId }
                .mapNotNull { it.toBarcodeScan() }

            updatedSession.copy(scans = scans)
        }
    }

    /**
     * Converts a ResultRow to a Session
     */
    private fun ResultRow.toSession(): Session {
        return Session(
            id = this[SessionsTable.id].value,
            userId = this[SessionsTable.userId].value,
            startTime = this[SessionsTable.startTime],
            endTime = this[SessionsTable.endTime],
            totalAmount = this[SessionsTable.totalAmount],
            scans = emptyList() // Scans are loaded separately
        )
    }

    /**
     * Converts a ResultRow to a BarcodeScanned
     */
    private fun ResultRow.toBarcodeScan(): BarcodeScanned {
        return BarcodeScanned(
            id = this[BarcodeScansTable.id].value,
            barcodeId = this[BarcodeScansTable.barcodeId].value,
            scanTime = this[BarcodeScansTable.scanTime],
            amount = this[BarcodeScansTable.amount]
        )
    }
}
