package com.company.snackbox.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.company.snackbox.config.AppConfig
import com.company.snackbox.model.Barcode
import com.company.snackbox.model.BarcodeType
import com.company.snackbox.model.User
import com.company.snackbox.service.BarcodeService
import com.company.snackbox.service.PaymentService
import com.company.snackbox.service.SessionService
import com.company.snackbox.service.UserService
import com.company.snackbox.ui.localization.LocalizationManager
import com.company.snackbox.ui.screen.AdminScreen
import com.company.snackbox.ui.screen.UserSessionScreen
import com.company.snackbox.ui.theme.SnackboxTheme
import mu.KotlinLogging
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JFrame
import javax.swing.SwingUtilities

private val logger = KotlinLogging.logger {}

/**
 * Main Compose application component
 */
@Composable
fun SnackboxApp(
    userService: UserService,
    barcodeService: BarcodeService,
    sessionService: SessionService,
    paymentService: PaymentService,
    config: AppConfig
) {
    // Initialize localization
    val localizationManager = remember { LocalizationManager.getInstance(config) }
    val currentLocale = remember { mutableStateOf(localizationManager.getCurrentLocale()) }
    
    // Application state
    val currentUser = remember { mutableStateOf<User?>(null) }
    val isAdminMode = remember { mutableStateOf(false) }
    val barcodeBuffer = remember { mutableStateOf("") }
    
    // Set up barcode scanner listener
    DisposableEffect(Unit) {
        val keyListener = createBarcodeKeyListener(barcodeBuffer) { code ->
            logger.info { "Barcode scanned: $code" }
            
            // Process barcode
            val barcode = barcodeService.scanBarcode(code)
            if (barcode != null) {
                processBarcode(
                    barcode = barcode,
                    currentUser = currentUser,
                    isAdminMode = isAdminMode,
                    userService = userService,
                    sessionService = sessionService
                )
            } else {
                logger.warn { "Unknown barcode: $code" }
            }
        }
        
        // Add key listener to the window
        SwingUtilities.invokeLater {
            val window = SwingUtilities.getWindowAncestor(null) as? JFrame
            window?.addKeyListener(keyListener)
        }
        
        onDispose {
            // Clean up
            SwingUtilities.invokeLater {
                val window = SwingUtilities.getWindowAncestor(null) as? JFrame
                window?.removeKeyListener(keyListener)
            }
            
            // Shutdown session service
            sessionService.shutdown()
        }
    }
    
    // Main UI
    SnackboxTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isAdminMode.value) {
                // Admin screen
                AdminScreen(
                    userService = userService,
                    barcodeService = barcodeService,
                    sessionService = sessionService,
                    paymentService = paymentService,
                    localizationManager = localizationManager,
                    onExitAdminMode = { isAdminMode.value = false }
                )
            } else {
                // User session screen
                UserSessionScreen(
                    currentUser = currentUser.value,
                    sessionService = sessionService,
                    paymentService = paymentService,
                    localizationManager = localizationManager
                )
            }
        }
    }
}

/**
 * Creates a key listener for barcode scanning
 *
 * @param barcodeBuffer Buffer to store barcode characters
 * @param onBarcodeScanned Callback when a barcode is scanned
 * @return KeyListener for barcode scanning
 */
private fun createBarcodeKeyListener(
    barcodeBuffer: MutableState<String>,
    onBarcodeScanned: (String) -> Unit
): KeyListener {
    return object : KeyListener {
        override fun keyTyped(e: KeyEvent) {
            // Not used
        }
        
        override fun keyPressed(e: KeyEvent) {
            if (e.keyCode == KeyEvent.VK_ENTER) {
                // Enter key pressed, process the barcode
                val code = barcodeBuffer.value.trim()
                if (code.isNotEmpty()) {
                    onBarcodeScanned(code)
                    barcodeBuffer.value = ""
                }
            } else if (e.keyChar.isLetterOrDigit() || e.keyChar == '-' || e.keyChar == '_') {
                // Add character to buffer
                barcodeBuffer.value += e.keyChar
            }
        }
        
        override fun keyReleased(e: KeyEvent) {
            // Not used
        }
    }
}

/**
 * Processes a scanned barcode
 *
 * @param barcode Scanned barcode
 * @param currentUser Current user state
 * @param isAdminMode Admin mode state
 * @param userService User service
 * @param sessionService Session service
 */
private fun processBarcode(
    barcode: Barcode,
    currentUser: MutableState<User?>,
    isAdminMode: MutableState<Boolean>,
    userService: UserService,
    sessionService: SessionService
) {
    // Get user for the barcode
    val user = userService.getUserById(barcode.userId)
    if (user == null) {
        logger.warn { "User not found for barcode: ${barcode.code}" }
        return
    }
    
    // Update current user
    currentUser.value = user
    
    // Check barcode type
    if (barcode.type == BarcodeType.ADMIN && user.isAdmin) {
        // Admin barcode
        isAdminMode.value = true
        logger.info { "Admin mode activated for user: ${user.name}" }
    } else {
        // Payment barcode
        val session = sessionService.processBarcodeScan(barcode)
        if (session == null) {
            logger.warn { "Failed to process barcode scan: ${barcode.code}" }
        } else {
            logger.info { "Barcode scan processed: ${barcode.code}, session: ${session.id}" }
        }
    }
}
