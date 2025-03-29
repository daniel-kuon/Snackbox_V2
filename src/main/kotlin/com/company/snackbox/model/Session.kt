package com.company.snackbox.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents a session in the system
 *
 * A session is created when a user scans a barcode and tracks all code scans.
 * It ends when no new code is scanned for a configured timeout period or when another user scans their card.
 *
 * @property id Unique identifier for the session
 * @property userId ID of the user this session belongs to
 * @property startTime When the session started
 * @property endTime When the session ended (null if still active)
 * @property totalAmount Total amount added to the user's account in this session
 * @property scans List of barcode scans in this session
 */
data class Session(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val startTime: LocalDateTime = LocalDateTime.now(),
    val endTime: LocalDateTime? = null,
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val scans: List<BarcodeScanned> = emptyList()
) {
    /**
     * Checks if the session is active
     *
     * @return true if the session is active, false otherwise
     */
    fun isActive(): Boolean = endTime == null
    
    /**
     * Adds a barcode scan to the session
     *
     * @param barcode The barcode that was scanned
     * @return Updated session with the new scan
     */
    fun addScan(barcode: Barcode): Session {
        if (!isActive()) {
            throw IllegalStateException("Cannot add scan to inactive session")
        }
        
        if (barcode.userId != userId) {
            throw IllegalArgumentException("Barcode belongs to a different user")
        }
        
        val scan = BarcodeScanned(
            barcodeId = barcode.id,
            scanTime = LocalDateTime.now(),
            amount = barcode.amount
        )
        
        val newScans = scans + scan
        val newTotalAmount = if (barcode.type == BarcodeType.PAYMENT && barcode.amount != null) {
            totalAmount + barcode.amount
        } else {
            totalAmount
        }
        
        return copy(
            scans = newScans,
            totalAmount = newTotalAmount
        )
    }
    
    /**
     * Ends the session
     *
     * @param endTime When the session ended (defaults to now)
     * @return Updated session with end time set
     */
    fun end(endTime: LocalDateTime = LocalDateTime.now()): Session {
        if (!isActive()) {
            return this
        }
        
        return copy(endTime = endTime)
    }
}

/**
 * Represents a barcode scan in a session
 *
 * @property id Unique identifier for the scan
 * @property barcodeId ID of the barcode that was scanned
 * @property scanTime When the barcode was scanned
 * @property amount Amount added to the user's account (null for admin barcodes)
 */
data class BarcodeScanned(
    val id: UUID = UUID.randomUUID(),
    val barcodeId: UUID,
    val scanTime: LocalDateTime,
    val amount: BigDecimal? = null
)
