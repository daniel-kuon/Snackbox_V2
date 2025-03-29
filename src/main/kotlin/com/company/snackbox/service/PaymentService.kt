package com.company.snackbox.service

import com.company.snackbox.config.AppConfig
import com.company.snackbox.model.Payment
import com.company.snackbox.model.PaymentMethod
import com.company.snackbox.repository.PaymentRepository
import com.company.snackbox.repository.SessionRepository
import com.company.snackbox.repository.UserRepository
import mu.KotlinLogging
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Service for payment operations
 */
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val config: AppConfig
) {
    
    /**
     * Records a payment made by a user
     *
     * @param userId ID of the user who made the payment
     * @param amount Amount paid
     * @param method Payment method used
     * @param reference Reference or transaction ID (optional)
     * @param notes Additional notes about the payment (optional)
     * @param recordedBy ID of the admin user who recorded the payment (null if self-recorded)
     * @return Created payment
     */
    fun recordPayment(
        userId: UUID,
        amount: BigDecimal,
        method: PaymentMethod,
        reference: String? = null,
        notes: String? = null,
        recordedBy: UUID? = null
    ): Payment {
        logger.info { "Recording payment for user: $userId, amount: $amount, method: $method" }
        
        // Check if user exists
        val user = userRepository.findById(userId) ?: throw IllegalArgumentException("User not found: $userId")
        
        // Check if admin exists (if provided)
        if (recordedBy != null) {
            val admin = userRepository.findById(recordedBy) ?: throw IllegalArgumentException("Admin user not found: $recordedBy")
            if (!admin.isAdmin) {
                throw IllegalArgumentException("User is not an admin: $recordedBy")
            }
        }
        
        // Validate amount
        if (amount <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Payment amount must be positive")
        }
        
        // Create payment
        val payment = if (recordedBy != null) {
            Payment.createAdminRecordedPayment(
                userId = userId,
                amount = amount,
                method = method,
                adminId = recordedBy,
                reference = reference,
                notes = notes
            )
        } else {
            Payment.createSelfRecordedPayment(
                userId = userId,
                amount = amount,
                method = method,
                reference = reference
            )
        }
        
        // Save to database
        return paymentRepository.create(payment)
    }
    
    /**
     * Deletes a payment
     *
     * @param id ID of the payment to delete
     * @return true if the payment was deleted, false otherwise
     */
    fun deletePayment(id: UUID): Boolean {
        logger.info { "Deleting payment: $id" }
        
        return paymentRepository.delete(id)
    }
    
    /**
     * Gets a payment by ID
     *
     * @param id ID of the payment to get
     * @return Payment if found, null otherwise
     */
    fun getPaymentById(id: UUID): Payment? {
        logger.debug { "Getting payment by ID: $id" }
        
        return paymentRepository.findById(id)
    }
    
    /**
     * Lists recent payments for a user
     *
     * @param userId ID of the user
     * @param limit Maximum number of payments to return
     * @return List of recent payments for the user
     */
    fun getRecentPaymentsForUser(userId: UUID, limit: Int = 3): List<Payment> {
        logger.debug { "Getting recent payments for user: $userId, limit: $limit" }
        
        return paymentRepository.findByUserId(userId, limit)
    }
    
    /**
     * Calculates the total amount spent by a user (sum of all sessions)
     *
     * @param userId ID of the user
     * @return Total amount spent
     */
    fun getTotalSpentByUser(userId: UUID): BigDecimal {
        logger.debug { "Calculating total spent by user: $userId" }
        
        // Get all sessions for the user
        val sessions = sessionRepository.findByUserId(userId)
        
        // Sum up the total amounts
        return sessions.fold(BigDecimal.ZERO) { acc, session ->
            acc + session.totalAmount
        }
    }
    
    /**
     * Calculates the total amount paid by a user
     *
     * @param userId ID of the user
     * @return Total amount paid
     */
    fun getTotalPaidByUser(userId: UUID): BigDecimal {
        logger.debug { "Calculating total paid by user: $userId" }
        
        return paymentRepository.getTotalPaidByUser(userId)
    }
    
    /**
     * Calculates the current balance for a user (total paid - total spent)
     *
     * @param userId ID of the user
     * @return Current balance
     */
    fun getUserBalance(userId: UUID): BigDecimal {
        logger.debug { "Calculating balance for user: $userId" }
        
        val totalPaid = getTotalPaidByUser(userId)
        val totalSpent = getTotalSpentByUser(userId)
        
        return totalPaid - totalSpent
    }
    
    /**
     * Generates a PayPal.me link for a user to pay their balance
     *
     * @param userId ID of the user
     * @return PayPal.me link
     */
    fun generatePayPalMeLink(userId: UUID): String {
        logger.debug { "Generating PayPal.me link for user: $userId" }
        
        val balance = getUserBalance(userId).abs()
        val paypalAddress = config.application.paypalMeAddress
        
        return "$paypalAddress/$balance"
    }
}
