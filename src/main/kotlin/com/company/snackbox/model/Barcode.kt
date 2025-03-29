package com.company.snackbox.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents the type of a barcode
 */
enum class BarcodeType {
    /**
     * Payment barcode that adds money to the user's account
     */
    PAYMENT,
    
    /**
     * Admin barcode that activates admin mode
     */
    ADMIN
}

/**
 * Represents a barcode in the system
 *
 * @property id Unique identifier for the barcode
 * @property code The actual barcode value
 * @property userId ID of the user this barcode belongs to
 * @property type Type of the barcode (payment or admin)
 * @property amount Amount in euros for payment barcodes (null for admin barcodes)
 * @property createdAt When the barcode was created
 * @property updatedAt When the barcode was last updated
 */
data class Barcode(
    val id: UUID = UUID.randomUUID(),
    val code: String,
    val userId: UUID,
    val type: BarcodeType,
    val amount: BigDecimal? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Validates that the barcode data is correct
     *
     * @return true if the barcode data is valid, false otherwise
     */
    fun isValid(): Boolean {
        return code.isNotBlank() && 
               when (type) {
                   BarcodeType.PAYMENT -> amount != null && amount > BigDecimal.ZERO
                   BarcodeType.ADMIN -> true
               }
    }
    
    /**
     * Creates a copy of this barcode with updated timestamp
     *
     * @param code New code (optional)
     * @param type New type (optional)
     * @param amount New amount (optional)
     * @return Updated barcode
     */
    fun update(
        code: String = this.code,
        type: BarcodeType = this.type,
        amount: BigDecimal? = this.amount
    ): Barcode {
        return copy(
            code = code,
            type = type,
            amount = amount,
            updatedAt = LocalDateTime.now()
        )
    }
    
    companion object {
        /**
         * Creates a payment barcode
         *
         * @param code Barcode value
         * @param userId User ID
         * @param amount Amount in euros
         * @return Payment barcode
         */
        fun createPaymentBarcode(code: String, userId: UUID, amount: BigDecimal): Barcode {
            return Barcode(
                code = code,
                userId = userId,
                type = BarcodeType.PAYMENT,
                amount = amount
            )
        }
        
        /**
         * Creates an admin barcode
         *
         * @param code Barcode value
         * @param userId User ID
         * @return Admin barcode
         */
        fun createAdminBarcode(code: String, userId: UUID): Barcode {
            return Barcode(
                code = code,
                userId = userId,
                type = BarcodeType.ADMIN,
                amount = null
            )
        }
    }
}
