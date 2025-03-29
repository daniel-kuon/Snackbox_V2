package com.company.snackbox.repository

import com.company.snackbox.database.BarcodeTypeDB
import com.company.snackbox.database.BarcodesTable
import com.company.snackbox.model.Barcode
import com.company.snackbox.model.BarcodeType
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Repository for Barcode operations
 */
class BarcodeRepository {

    /**
     * Creates a new barcode in the database
     *
     * @param barcode Barcode to create
     * @return Created barcode with ID
     */
    fun create(barcode: Barcode): Barcode {
        logger.info { "Creating barcode: ${barcode.code}" }

        val id = transaction {
            BarcodesTable.insert {
                it[id] = barcode.id
                it[code] = barcode.code
                it[userId] = barcode.userId
                it[type] = mapToDbType(barcode.type)
                it[amount] = barcode.amount
                it[createdAt] = barcode.createdAt
                it[updatedAt] = barcode.updatedAt
            } get BarcodesTable.id
        }

        return barcode.copy(id = id.value)
    }

    /**
     * Updates an existing barcode in the database
     *
     * @param barcode Barcode to update
     * @return Updated barcode
     */
    fun update(barcode: Barcode): Barcode {
        logger.info { "Updating barcode: ${barcode.id}" }

        transaction {
            BarcodesTable.update({ BarcodesTable.id eq barcode.id }) {
                it[code] = barcode.code
                it[type] = mapToDbType(barcode.type)
                it[amount] = barcode.amount
                it[updatedAt] = LocalDateTime.now()
            }
        }

        return barcode.copy(updatedAt = LocalDateTime.now())
    }

    /**
     * Deletes a barcode from the database
     *
     * @param id ID of the barcode to delete
     * @return true if the barcode was deleted, false otherwise
     */
    fun delete(id: UUID): Boolean {
        logger.info { "Deleting barcode: $id" }

        val deletedCount = transaction {
            BarcodesTable.deleteWhere { BarcodesTable.id eq id }
        }

        return deletedCount > 0
    }

    /**
     * Finds a barcode by ID
     *
     * @param id ID of the barcode to find
     * @return Barcode if found, null otherwise
     */
    fun findById(id: UUID): Barcode? {
        logger.debug { "Finding barcode by ID: $id" }

        return transaction {
            BarcodesTable.select { BarcodesTable.id eq id }
                .mapNotNull { it.toBarcode() }
                .singleOrNull()
        }
    }

    /**
     * Finds a barcode by code
     *
     * @param code Code of the barcode to find
     * @return Barcode if found, null otherwise
     */
    fun findByCode(code: String): Barcode? {
        logger.debug { "Finding barcode by code: $code" }

        return transaction {
            BarcodesTable.select { BarcodesTable.code eq code }
                .mapNotNull { it.toBarcode() }
                .singleOrNull()
        }
    }

    /**
     * Lists all barcodes for a user
     *
     * @param userId ID of the user
     * @return List of barcodes for the user
     */
    fun findByUserId(userId: UUID): List<Barcode> {
        logger.debug { "Finding barcodes for user: $userId" }

        return transaction {
            BarcodesTable.select { BarcodesTable.userId eq userId }
                .mapNotNull { it.toBarcode() }
        }
    }

    /**
     * Lists all payment barcodes for a user
     *
     * @param userId ID of the user
     * @return List of payment barcodes for the user
     */
    fun findPaymentBarcodesByUserId(userId: UUID): List<Barcode> {
        logger.debug { "Finding payment barcodes for user: $userId" }

        return transaction {
            BarcodesTable.select {
                (BarcodesTable.userId eq userId) and
                (BarcodesTable.type eq BarcodeTypeDB.PAYMENT)
            }
                .mapNotNull { it.toBarcode() }
        }
    }

    /**
     * Lists all admin barcodes for a user
     *
     * @param userId ID of the user
     * @return List of admin barcodes for the user
     */
    fun findAdminBarcodesByUserId(userId: UUID): List<Barcode> {
        logger.debug { "Finding admin barcodes for user: $userId" }

        return transaction {
            BarcodesTable.select {
                (BarcodesTable.userId eq userId) and
                (BarcodesTable.type eq BarcodeTypeDB.ADMIN)
            }
                .mapNotNull { it.toBarcode() }
        }
    }

    /**
     * Converts a ResultRow to a Barcode
     */
    private fun ResultRow.toBarcode(): Barcode {
        return Barcode(
            id = this[BarcodesTable.id].value,
            code = this[BarcodesTable.code],
            userId = this[BarcodesTable.userId].value,
            type = mapFromDbType(this[BarcodesTable.type]),
            amount = this[BarcodesTable.amount],
            createdAt = this[BarcodesTable.createdAt],
            updatedAt = this[BarcodesTable.updatedAt]
        )
    }

    /**
     * Maps BarcodeType to BarcodeTypeDB
     */
    private fun mapToDbType(type: BarcodeType): BarcodeTypeDB {
        return when (type) {
            BarcodeType.PAYMENT -> BarcodeTypeDB.PAYMENT
            BarcodeType.ADMIN -> BarcodeTypeDB.ADMIN
        }
    }

    /**
     * Maps BarcodeTypeDB to BarcodeType
     */
    private fun mapFromDbType(type: BarcodeTypeDB): BarcodeType {
        return when (type) {
            BarcodeTypeDB.PAYMENT -> BarcodeType.PAYMENT
            BarcodeTypeDB.ADMIN -> BarcodeType.ADMIN
        }
    }
}
