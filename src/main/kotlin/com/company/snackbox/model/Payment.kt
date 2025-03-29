package com.company.snackbox.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents a payment method
 */
enum class PaymentMethod {
    /**
     * Payment via PayPal
     */
    PAYPAL,
    
    /**
     * Payment via cash
     */
    CASH,
    
    /**
     * Payment via bank transfer
     */
    BANK_TRANSFER,
    
    /**
     * Other payment method
     */
    OTHER
}

/**
 * Represents a payment in the system
 *
 * @property id Unique identifier for the payment
 * @property userId ID of the user who made the payment
 * @property amount Amount paid
 * @property method Payment method used
 * @property reference Reference or transaction ID (optional)
 * @property notes Additional notes about the payment (optional)
 * @property timestamp When the payment was made
 * @property recordedBy ID of the admin user who recorded the payment (null if self-recorded)
 */
data class Payment(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val amount: BigDecimal,
    val method: PaymentMethod,
    val reference: String? = null,
    val notes: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val recordedBy: UUID? = null
) {
    /**
     * Validates that the payment data is correct
     *
     * @return true if the payment data is valid, false otherwise
     */
    fun isValid(): Boolean {
        return amount > BigDecimal.ZERO
    }
    
    companion object {
        /**
         * Creates a payment recorded by an admin
         *
         * @param userId ID of the user who made the payment
         * @param amount Amount paid
         * @param method Payment method used
         * @param adminId ID of the admin user recording the payment
         * @param reference Reference or transaction ID (optional)
         * @param notes Additional notes about the payment (optional)
         * @return Payment recorded by admin
         */
        fun createAdminRecordedPayment(
            userId: UUID,
            amount: BigDecimal,
            method: PaymentMethod,
            adminId: UUID,
            reference: String? = null,
            notes: String? = null
        ): Payment {
            return Payment(
                userId = userId,
                amount = amount,
                method = method,
                reference = reference,
                notes = notes,
                recordedBy = adminId
            )
        }
        
        /**
         * Creates a self-recorded payment (e.g., via PayPal)
         *
         * @param userId ID of the user who made the payment
         * @param amount Amount paid
         * @param method Payment method used
         * @param reference Reference or transaction ID (optional)
         * @return Self-recorded payment
         */
        fun createSelfRecordedPayment(
            userId: UUID,
            amount: BigDecimal,
            method: PaymentMethod,
            reference: String? = null
        ): Payment {
            return Payment(
                userId = userId,
                amount = amount,
                method = method,
                reference = reference,
                recordedBy = null
            )
        }
    }
}
