package com.company.snackbox.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.snackbox.model.User
import com.company.snackbox.service.PaymentService
import com.company.snackbox.service.SessionService
import com.company.snackbox.ui.localization.LocalizationManager
import kotlinx.coroutines.delay
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Screen for displaying user session information
 */
@Composable
fun UserSessionScreen(
    currentUser: User?,
    sessionService: SessionService,
    paymentService: PaymentService,
    localizationManager: LocalizationManager
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        if (currentUser == null) {
            // No user logged in
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = localizationManager.getString("app.loading"),
                    fontSize = 24.sp
                )
            }
        } else {
            // User logged in
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Text(
                    text = localizationManager.getFormattedString("session.title", currentUser.name),
                    fontSize = 28.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Main content
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left column - Session info
                    Column(
                        modifier = Modifier.weight(0.6f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current session
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = localizationManager.getString("session.current"),
                                    fontSize = 20.sp
                                )
                            }
                        }
                        
                        // Recent sessions
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = localizationManager.getString("session.recent"),
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                    
                    // Right column - Payment info
                    Column(
                        modifier = Modifier.weight(0.4f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Recent payments
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = localizationManager.getString("session.payments"),
                                    fontSize = 20.sp
                                )
                            }
                        }
                        
                        // PayPal QR code
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = localizationManager.getString("session.paypal"),
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
