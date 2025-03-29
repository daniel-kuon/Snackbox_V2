package com.company.snackbox.repository

import com.company.snackbox.database.PaymentMethodDB
import com.company.snackbox.database.PaymentsTable
import com.company.snackbox.model.Payment
import com.company.snackbox.model.PaymentMethod
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Repository for Payment operations
 */
class PaymentRepository {

    /**
     * Creates a new payment in the database
     *
     * @param payment Payment to create
     * @return Created payment with ID
     */
    fun create(payment: Payment): Payment {
        logger.info { "Creating payment for user: ${payment.userId}, amount: ${payment.amount}" }

        val id = transaction {
            PaymentsTable.insert {
                it[id] = payment.id
                it[userId] = payment.userId
                it[amount] = payment.amount
                it[method] = mapToDbMethod(payment.method)
                it[reference] = payment.reference
                it[notes] = payment.notes
                it[timestamp] = payment.timestamp
                it[recordedBy] = payment.recordedBy
            } get PaymentsTable.id
        }

        return payment.copy(id = id.value)
    }

    /**
     * Deletes a payment from the database
     *
     * @param id ID of the payment to delete
     * @return true if the payment was deleted, false otherwise
     */
    fun delete(id: UUID): Boolean {
        logger.info { "Deleting payment: $id" }

        val deletedCount = transaction {
            PaymentsTable.deleteWhere { PaymentsTable.id eq id }
        }

        return deletedCount > 0
    }

    /**
     * Finds a payment by ID
     *
     * @param id ID of the payment to find
     * @return Payment if found, null otherwise
     */
    fun findById(id: UUID): Payment? {
        logger.debug { "Finding payment by ID: $id" }

        return transaction {
            PaymentsTable.select { PaymentsTable.id eq id }
                .mapNotNull { it.toPayment() }
                .singleOrNull()
        }
    }

    /**
     * Lists all payments for a user
     *
     * @param userId ID of the user
     * @param limit Maximum number of payments to return (0 for all)
     * @return List of payments for the user
     */
    fun findByUserId(userId: UUID, limit: Int = 0): List<Payment> {
        logger.debug { "Finding payments for user: $userId, limit: $limit" }

        return transaction {
            val query = PaymentsTable
                .select { PaymentsTable.userId eq userId }
                .orderBy(PaymentsTable.timestamp, SortOrder.DESC)

            val limitedQuery = if (limit > 0) query.limit(limit) else query

            limitedQuery.mapNotNull { it.toPayment() }
        }
    }

    /**
     * Lists all payments recorded by an admin
     *
     * @param adminId ID of the admin
     * @return List of payments recorded by the admin
     */
    fun findByRecordedBy(adminId: UUID): List<Payment> {
        logger.debug { "Finding payments recorded by admin: $adminId" }

        return transaction {
            PaymentsTable
                .select { PaymentsTable.recordedBy eq adminId }
                .orderBy(PaymentsTable.timestamp, SortOrder.DESC)
                .mapNotNull { it.toPayment() }
        }
    }

    /**
     * Calculates the total amount paid by a user
     *
     * @param userId ID of the user
     * @return Total amount paid
     */
    fun getTotalPaidByUser(userId: UUID): java.math.BigDecimal {
        logger.debug { "Calculating total paid by user: $userId" }

        return transaction {
            PaymentsTable
                .select { PaymentsTable.userId eq userId }
                .sumOf { it[PaymentsTable.amount] }
        }
    }

    /**
     * Converts a ResultRow to a Payment
     */
    private fun ResultRow.toPayment(): Payment {
        return Payment(
            id = this[PaymentsTable.id].value,
            userId = this[PaymentsTable.userId].value,
            amount = this[PaymentsTable.amount],
            method = mapFromDbMethod(this[PaymentsTable.method]),
            reference = this[PaymentsTable.reference],
            notes = this[PaymentsTable.notes],
            timestamp = this[PaymentsTable.timestamp],
            recordedBy = this[PaymentsTable.recordedBy]?.value
        )
    }

    /**
     * Maps PaymentMethod to PaymentMethodDB
     */
    private fun mapToDbMethod(method: PaymentMethod): PaymentMethodDB {
        return when (method) {
            PaymentMethod.PAYPAL -> PaymentMethodDB.PAYPAL
            PaymentMethod.CASH -> PaymentMethodDB.CASH
            PaymentMethod.BANK_TRANSFER -> PaymentMethodDB.BANK_TRANSFER
            PaymentMethod.OTHER -> PaymentMethodDB.OTHER
        }
    }

    /**
     * Maps PaymentMethodDB to PaymentMethod
     */
    private fun mapFromDbMethod(method: PaymentMethodDB): PaymentMethod {
        return when (method) {
            PaymentMethodDB.PAYPAL -> PaymentMethod.PAYPAL
            PaymentMethodDB.CASH -> PaymentMethod.CASH
            PaymentMethodDB.BANK_TRANSFER -> PaymentMethod.BANK_TRANSFER
            PaymentMethodDB.OTHER -> PaymentMethod.OTHER
        }
    }
}
