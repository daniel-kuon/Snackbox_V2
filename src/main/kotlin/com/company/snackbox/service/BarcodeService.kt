package com.company.snackbox.service

import com.company.snackbox.config.AppConfig
import com.company.snackbox.model.Barcode
import com.company.snackbox.model.BarcodeType
import com.company.snackbox.model.User
import com.company.snackbox.repository.BarcodeRepository
import com.company.snackbox.repository.UserRepository
import mu.KotlinLogging
import java.math.BigDecimal
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Service for barcode operations
 */
class BarcodeService(
    private val barcodeRepository: BarcodeRepository,
    private val userRepository: UserRepository,
    private val config: AppConfig
) {
    
    /**
     * Scans a barcode and returns the barcode object if found
     *
     * @param code Barcode value
     * @return Barcode if found, null otherwise
     */
    fun scanBarcode(code: String): Barcode? {
        logger.info { "Scanning barcode: $code" }
        
        return barcodeRepository.findByCode(code)
    }
    
    /**
     * Creates a new payment barcode for a user
     *
     * @param userId ID of the user
     * @param code Barcode value
     * @param amount Amount in euros
     * @return Created barcode
     */
    fun createPaymentBarcode(userId: UUID, code: String, amount: BigDecimal): Barcode {
        logger.info { "Creating payment barcode for user: $userId, amount: $amount" }
        
        // Check if user exists
        val user = userRepository.findById(userId) ?: throw IllegalArgumentException("User not found: $userId")
        
        // Check if code is already used
        if (barcodeRepository.findByCode(code) != null) {
            throw IllegalArgumentException("Barcode code already exists: $code")
        }
        
        // Create barcode
        val barcode = Barcode.createPaymentBarcode(code, userId, amount)
        
        // Save to database
        return barcodeRepository.create(barcode)
    }
    
    /**
     * Creates a new admin barcode for a user
     *
     * @param userId ID of the user
     * @param code Barcode value
     * @return Created barcode
     */
    fun createAdminBarcode(userId: UUID, code: String): Barcode {
        logger.info { "Creating admin barcode for user: $userId" }
        
        // Check if user exists
        val user = userRepository.findById(userId) ?: throw IllegalArgumentException("User not found: $userId")
        
        // Check if user is admin
        if (!user.isAdmin) {
            throw IllegalArgumentException("User is not an admin: $userId")
        }
        
        // Check if code is already used
        if (barcodeRepository.findByCode(code) != null) {
            throw IllegalArgumentException("Barcode code already exists: $code")
        }
        
        // Create barcode
        val barcode = Barcode.createAdminBarcode(code, userId)
        
        // Save to database
        return barcodeRepository.create(barcode)
    }
    
    /**
     * Deletes a barcode
     *
     * @param id ID of the barcode to delete
     * @return true if the barcode was deleted, false otherwise
     */
    fun deleteBarcode(id: UUID): Boolean {
        logger.info { "Deleting barcode: $id" }
        
        return barcodeRepository.delete(id)
    }
    
    /**
     * Lists all barcodes for a user
     *
     * @param userId ID of the user
     * @return List of barcodes for the user
     */
    fun getBarcodesByUser(userId: UUID): List<Barcode> {
        logger.debug { "Getting barcodes for user: $userId" }
        
        return barcodeRepository.findByUserId(userId)
    }
    
    /**
     * Lists all payment barcodes for a user
     *
     * @param userId ID of the user
     * @return List of payment barcodes for the user
     */
    fun getPaymentBarcodesByUser(userId: UUID): List<Barcode> {
        logger.debug { "Getting payment barcodes for user: $userId" }
        
        return barcodeRepository.findPaymentBarcodesByUserId(userId)
    }
    
    /**
     * Lists all admin barcodes for a user
     *
     * @param userId ID of the user
     * @return List of admin barcodes for the user
     */
    fun getAdminBarcodesByUser(userId: UUID): List<Barcode> {
        logger.debug { "Getting admin barcodes for user: $userId" }
        
        return barcodeRepository.findAdminBarcodesByUserId(userId)
    }
    
    /**
     * Checks if a user has admin privileges
     *
     * @param userId ID of the user
     * @return true if the user has admin privileges, false otherwise
     */
    fun isUserAdmin(userId: UUID): Boolean {
        logger.debug { "Checking if user is admin: $userId" }
        
        val user = userRepository.findById(userId) ?: return false
        return user.isAdmin
    }
}
