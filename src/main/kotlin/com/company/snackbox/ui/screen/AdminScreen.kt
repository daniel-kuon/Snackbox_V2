package com.company.snackbox.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.snackbox.service.BarcodeService
import com.company.snackbox.service.PaymentService
import com.company.snackbox.service.SessionService
import com.company.snackbox.service.UserService
import com.company.snackbox.ui.localization.LocalizationManager
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Screen for admin functionality
 */
@Composable
fun AdminScreen(
    userService: UserService,
    barcodeService: BarcodeService,
    sessionService: SessionService,
    paymentService: PaymentService,
    localizationManager: LocalizationManager,
    onExitAdminMode: () -> Unit
) {
    // State
    var selectedTab by remember { mutableStateOf(AdminTab.USERS) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizationManager.getString("admin.title"),
                fontSize = 28.sp
            )
            
            Button(
                onClick = onExitAdminMode
            ) {
                Text(localizationManager.getString("app.back"))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab.ordinal
        ) {
            AdminTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(localizationManager.getString(tab.titleKey))
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            when (selectedTab) {
                AdminTab.USERS -> UsersTab(userService, localizationManager)
                AdminTab.BARCODES -> BarcodesTab(barcodeService, userService, localizationManager)
                AdminTab.PAYMENTS -> PaymentsTab(paymentService, userService, localizationManager)
                AdminTab.SESSIONS -> SessionsTab(sessionService, userService, localizationManager)
            }
        }
    }
}

/**
 * Admin tabs
 */
enum class AdminTab(val titleKey: String) {
    USERS("admin.users"),
    BARCODES("admin.barcodes"),
    PAYMENTS("admin.payments"),
    SESSIONS("admin.sessions")
}

/**
 * Tab for managing users
 */
@Composable
private fun UsersTab(
    userService: UserService,
    localizationManager: LocalizationManager
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = localizationManager.getString("user.management"),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // In a real implementation, this would show a list of users and controls to add/edit/delete them
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = localizationManager.getString("user.list"),
                    fontSize = 20.sp
                )
            }
        }
    }
}

/**
 * Tab for managing barcodes
 */
@Composable
private fun BarcodesTab(
    barcodeService: BarcodeService,
    userService: UserService,
    localizationManager: LocalizationManager
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = localizationManager.getString("barcode.management"),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // In a real implementation, this would show a list of barcodes and controls to add/edit/delete them
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = localizationManager.getString("barcode.list"),
                    fontSize = 20.sp
                )
            }
        }
    }
}

/**
 * Tab for managing payments
 */
@Composable
private fun PaymentsTab(
    paymentService: PaymentService,
    userService: UserService,
    localizationManager: LocalizationManager
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = localizationManager.getString("payment.management"),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // In a real implementation, this would show a list of payments and controls to add/delete them
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = localizationManager.getString("payment.list"),
                    fontSize = 20.sp
                )
            }
        }
    }
}

/**
 * Tab for managing sessions
 */
@Composable
private fun SessionsTab(
    sessionService: SessionService,
    userService: UserService,
    localizationManager: LocalizationManager
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = localizationManager.getString("session.management"),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // In a real implementation, this would show a list of sessions and controls to view/end them
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = localizationManager.getString("session.list"),
                    fontSize = 20.sp
                )
            }
        }
    }
}
