package com.company.snackbox

import androidx.compose.ui.window.application
import com.company.snackbox.config.AppConfig
import com.company.snackbox.database.DatabaseManager
import com.company.snackbox.repository.BarcodeRepository
import com.company.snackbox.repository.PaymentRepository
import com.company.snackbox.repository.SessionRepository
import com.company.snackbox.repository.UserRepository
import com.company.snackbox.service.BarcodeService
import com.company.snackbox.service.PaymentService
import com.company.snackbox.service.SessionService
import com.company.snackbox.service.UserService
import com.company.snackbox.ui.SnackboxApp
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Main entry point for the Snackbox application
 */
fun main() {
    logger.info { "Starting Snackbox application" }
    
    try {
        // Load configuration
        val config = AppConfig.load()
        logger.info { "Configuration loaded" }
        
        // Initialize database
        val databaseManager = DatabaseManager(config)
        databaseManager.init()
        logger.info { "Database initialized" }
        
        // Create repositories
        val userRepository = UserRepository()
        val barcodeRepository = BarcodeRepository()
        val sessionRepository = SessionRepository()
        val paymentRepository = PaymentRepository()
        logger.info { "Repositories created" }
        
        // Create services
        val userService = UserService(userRepository)
        val barcodeService = BarcodeService(barcodeRepository, userRepository, config)
        val sessionService = SessionService(sessionRepository, barcodeRepository, userRepository, config)
        val paymentService = PaymentService(paymentRepository, sessionRepository, userRepository, config)
        logger.info { "Services created" }
        
        // Start UI
        application {
            SnackboxApp(
                userService = userService,
                barcodeService = barcodeService,
                sessionService = sessionService,
                paymentService = paymentService,
                config = config
            )
        }
    } catch (e: Exception) {
        logger.error(e) { "Error starting application" }
    }
}
