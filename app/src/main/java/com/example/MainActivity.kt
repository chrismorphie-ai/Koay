package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.PaymentEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AddClientDialog
import com.example.ui.components.AddPropertyDialog
import com.example.ui.components.DigitalReceiptDialog
import com.example.ui.components.EstateTopAppBar
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ClientLedgerScreen
import com.example.ui.screens.ClientPortalScreen
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.InstallmentPlanScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PaymentTrackingScreen
import com.example.ui.screens.PropertiesScreen
import com.example.ui.screens.ReceiptsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EstatePayApp(viewModel)
            }
        }
    }
}

@Composable
fun EstatePayApp(viewModel: MainViewModel) {
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
    val adminTab by viewModel.selectedAdminTab.collectAsStateWithLifecycle()
    val clientTab by viewModel.selectedClientTab.collectAsStateWithLifecycle()

    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val properties by viewModel.properties.collectAsStateWithLifecycle()
    val installments by viewModel.installments.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val ledgerEntries by viewModel.ledgerEntries.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val adminKpi by viewModel.adminKpiSummary.collectAsStateWithLifecycle()
    val clientSummary by viewModel.clientFinancialSummary.collectAsStateWithLifecycle()

    val unreadNotifCount = notifications.count { !it.isRead }

    // Dialog & overlay states
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var paymentDialogClientId by remember { mutableStateOf<Long?>(null) }
    var paymentDialogPropertyId by remember { mutableStateOf<Long?>(null) }
    var paymentDialogInstallmentId by remember { mutableStateOf<Long?>(null) }

    var showAddClientDialog by remember { mutableStateOf(false) }
    var showAddPropertyDialog by remember { mutableStateOf(false) }
    var viewingReceiptPayment by remember { mutableStateOf<PaymentEntity?>(null) }
    var showNotificationsOverlay by remember { mutableStateOf(false) }

    // Navigation back press handler
    BackHandler(enabled = (isAdminMode && adminTab != 0) || (!isAdminMode && clientTab != 0) || showNotificationsOverlay) {
        if (showNotificationsOverlay) {
            showNotificationsOverlay = false
        } else if (isAdminMode) {
            viewModel.setSelectedAdminTab(0)
        } else {
            viewModel.setSelectedClientTab(0)
        }
    }

    Scaffold(
        topBar = {
            EstateTopAppBar(
                title = if (showNotificationsOverlay) {
                    "Alerts & Updates"
                } else if (isAdminMode) {
                    when (adminTab) {
                        0 -> "Admin Overview"
                        1 -> "Client Ledger"
                        2 -> "Clients Directory"
                        3 -> "Plot & Plan Manager"
                        4 -> "Collections & Vouchers"
                        5 -> "Reports & Analytics"
                        else -> "Overview"
                    }
                } else {
                    when (clientTab) {
                        0 -> "My Property Dashboard"
                        1 -> "Payment Schedule"
                        2 -> "My Financial Ledger"
                        3 -> "Digital Receipts"
                        4 -> "Alerts & Notifications"
                        else -> "My Dashboard"
                    }
                },
                isAdminMode = isAdminMode,
                unreadNotificationCount = unreadNotifCount,
                onToggleMode = { viewModel.toggleAppMode() },
                onNotificationClick = { showNotificationsOverlay = !showNotificationsOverlay }
            )
        },
        bottomBar = {
            if (!showNotificationsOverlay) {
                if (isAdminMode) {
                    NavigationBar(modifier = Modifier.testTag("admin_bottom_nav")) {
                        NavigationBarItem(
                            selected = adminTab == 0,
                            onClick = { viewModel.setSelectedAdminTab(0) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") },
                            modifier = Modifier.testTag("admin_nav_dashboard")
                        )
                        NavigationBarItem(
                            selected = adminTab == 1,
                            onClick = { viewModel.setSelectedAdminTab(1) },
                            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Ledger") },
                            label = { Text("Ledger") },
                            modifier = Modifier.testTag("admin_nav_ledger")
                        )
                        NavigationBarItem(
                            selected = adminTab == 2,
                            onClick = { viewModel.setSelectedAdminTab(2) },
                            icon = { Icon(Icons.Default.People, contentDescription = "Clients") },
                            label = { Text("Clients") },
                            modifier = Modifier.testTag("admin_nav_clients")
                        )
                        NavigationBarItem(
                            selected = adminTab == 3,
                            onClick = { viewModel.setSelectedAdminTab(3) },
                            icon = { Icon(Icons.Default.Domain, contentDescription = "Plots") },
                            label = { Text("Plots") },
                            modifier = Modifier.testTag("admin_nav_plots")
                        )
                        NavigationBarItem(
                            selected = adminTab == 4,
                            onClick = { viewModel.setSelectedAdminTab(4) },
                            icon = { Icon(Icons.Default.Receipt, contentDescription = "Payments") },
                            label = { Text("Payments") },
                            modifier = Modifier.testTag("admin_nav_payments")
                        )
                        NavigationBarItem(
                            selected = adminTab == 5,
                            onClick = { viewModel.setSelectedAdminTab(5) },
                            icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                            label = { Text("Reports") },
                            modifier = Modifier.testTag("admin_nav_reports")
                        )
                    }
                } else {
                    NavigationBar(modifier = Modifier.testTag("client_bottom_nav")) {
                        NavigationBarItem(
                            selected = clientTab == 0,
                            onClick = { viewModel.setSelectedClientTab(0) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            modifier = Modifier.testTag("client_nav_home")
                        )
                        NavigationBarItem(
                            selected = clientTab == 1,
                            onClick = { viewModel.setSelectedClientTab(1) },
                            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Schedule") },
                            label = { Text("Schedule") },
                            modifier = Modifier.testTag("client_nav_schedule")
                        )
                        NavigationBarItem(
                            selected = clientTab == 2,
                            onClick = { viewModel.setSelectedClientTab(2) },
                            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Ledger") },
                            label = { Text("Ledger") },
                            modifier = Modifier.testTag("client_nav_ledger")
                        )
                        NavigationBarItem(
                            selected = clientTab == 3,
                            onClick = { viewModel.setSelectedClientTab(3) },
                            icon = { Icon(Icons.Default.Receipt, contentDescription = "Receipts") },
                            label = { Text("Receipts") },
                            modifier = Modifier.testTag("client_nav_receipts")
                        )
                        NavigationBarItem(
                            selected = clientTab == 4,
                            onClick = { viewModel.setSelectedClientTab(4) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotifCount > 0) {
                                            Badge { Text("$unreadNotifCount") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                                }
                            },
                            label = { Text("Alerts") },
                            modifier = Modifier.testTag("client_nav_alerts")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showNotificationsOverlay) {
                NotificationsScreen(
                    notifications = notifications,
                    onMarkAsRead = { viewModel.markNotificationAsRead(it) },
                    onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() }
                )
            } else if (isAdminMode) {
                when (adminTab) {
                    0 -> AdminDashboardScreen(
                        kpi = adminKpi,
                        recentPayments = payments,
                        clients = clients,
                        properties = properties,
                        onRecordPaymentClick = {
                            paymentDialogClientId = null
                            paymentDialogPropertyId = null
                            paymentDialogInstallmentId = null
                            showRecordPaymentDialog = true
                        },
                        onAddClientClick = { showAddClientDialog = true },
                        onAddPlotClick = { showAddPropertyDialog = true },
                        onViewLedgerClick = { viewModel.setSelectedAdminTab(1) },
                        onPaymentClick = { viewingReceiptPayment = it },
                        onNavigateToTab = { viewModel.setSelectedAdminTab(it) }
                    )
                    1 -> ClientLedgerScreen(
                        clients = clients,
                        properties = properties,
                        ledgerEntries = ledgerEntries,
                        onAddDebitCharge = { cId, pId, desc, amt, date ->
                            viewModel.addDebitCharge(cId, pId, desc, amt, date)
                        },
                        onRecordPaymentClick = { cId, pId ->
                            paymentDialogClientId = cId
                            paymentDialogPropertyId = pId
                            paymentDialogInstallmentId = null
                            showRecordPaymentDialog = true
                        }
                    )
                    2 -> ClientsScreen(
                        clients = clients,
                        properties = properties,
                        onAddClientClick = { showAddClientDialog = true },
                        onViewClientLedger = { cId ->
                            viewModel.selectClient(cId)
                            viewModel.setSelectedAdminTab(1)
                        },
                        onRecordClientPayment = { cId ->
                            paymentDialogClientId = cId
                            paymentDialogPropertyId = properties.firstOrNull { it.clientId == cId }?.id
                            paymentDialogInstallmentId = null
                            showRecordPaymentDialog = true
                        },
                        onClientSelect = { cId ->
                            viewModel.selectClient(cId)
                        }
                    )
                    3 -> PropertiesScreen(
                        properties = properties,
                        clients = clients,
                        installments = installments,
                        payments = payments,
                        onAddPlotClick = { showAddPropertyDialog = true },
                        onViewPlan = { propId ->
                            viewModel.selectProperty(propId)
                            viewModel.setSelectedAdminTab(4)
                        },
                        onViewLedger = { cId ->
                            viewModel.selectClient(cId)
                            viewModel.setSelectedAdminTab(1)
                        },
                        onRecordPayment = { pId, cId ->
                            paymentDialogPropertyId = pId
                            paymentDialogClientId = cId
                            paymentDialogInstallmentId = null
                            showRecordPaymentDialog = true
                        }
                    )
                    4 -> PaymentTrackingScreen(
                        payments = payments,
                        clients = clients,
                        properties = properties,
                        onRecordPaymentClick = {
                            paymentDialogClientId = null
                            paymentDialogPropertyId = null
                            paymentDialogInstallmentId = null
                            showRecordPaymentDialog = true
                        },
                        onPaymentClick = { viewingReceiptPayment = it }
                    )
                    5 -> ReportsScreen(
                        payments = payments,
                        clients = clients,
                        properties = properties,
                        installments = installments
                    )
                }
            } else {
                // Client Mode Screens
                when (clientTab) {
                    0 -> ClientPortalScreen(
                        summary = clientSummary,
                        clients = clients,
                        installments = installments,
                        payments = payments,
                        ledgerEntries = ledgerEntries,
                        onSelectClient = { viewModel.selectClient(it) },
                        onPayInstallment = { pId, cId, instId ->
                            paymentDialogPropertyId = pId
                            paymentDialogClientId = cId
                            paymentDialogInstallmentId = instId
                            showRecordPaymentDialog = true
                        },
                        onPaymentClick = { viewingReceiptPayment = it }
                    )
                    1 -> InstallmentPlanScreen(
                        properties = clientSummary.properties.ifEmpty { properties },
                        clients = clients,
                        installments = installments,
                        onPayInstallment = { pId, cId, instId ->
                            paymentDialogPropertyId = pId
                            paymentDialogClientId = cId
                            paymentDialogInstallmentId = instId
                            showRecordPaymentDialog = true
                        }
                    )
                    2 -> ClientLedgerScreen(
                        clients = clients,
                        properties = properties,
                        ledgerEntries = ledgerEntries,
                        initialClientId = clientSummary.client?.id,
                        onAddDebitCharge = { cId, pId, desc, amt, date ->
                            viewModel.addDebitCharge(cId, pId, desc, amt, date)
                        },
                        onRecordPaymentClick = { cId, pId ->
                            paymentDialogClientId = cId ?: clientSummary.client?.id
                            paymentDialogPropertyId = pId ?: clientSummary.properties.firstOrNull()?.id
                            paymentDialogInstallmentId = null
                            showRecordPaymentDialog = true
                        }
                    )
                    3 -> ReceiptsScreen(
                        payments = payments.filter { it.clientId == clientSummary.client?.id },
                        clients = clients,
                        properties = clientSummary.properties,
                        onPaymentClick = { viewingReceiptPayment = it }
                    )
                    4 -> NotificationsScreen(
                        notifications = notifications.filter { it.clientId == null || it.clientId == clientSummary.client?.id },
                        onMarkAsRead = { viewModel.markNotificationAsRead(it) },
                        onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() }
                    )
                }
            }
        }
    }

    // Record Payment Dialog
    if (showRecordPaymentDialog) {
        RecordPaymentDialog(
            clients = clients,
            properties = properties,
            installments = installments,
            initialClientId = paymentDialogClientId,
            initialPropertyId = paymentDialogPropertyId,
            initialInstallmentId = paymentDialogInstallmentId,
            onDismiss = { showRecordPaymentDialog = false },
            onSubmit = { pId, cId, instId, amt, date, method, ref, notes, manualReceiptNo ->
                viewModel.recordPayment(
                    propertyId = pId,
                    clientId = cId,
                    installmentId = instId,
                    amount = amt,
                    paymentDate = date,
                    paymentMethod = method,
                    referenceNumber = ref,
                    notes = notes,
                    manualReceiptNumber = manualReceiptNo
                ) { rcpNum ->
                    // Payment recorded successfully
                }
            }
        )
    }

    // Add Client Dialog
    if (showAddClientDialog) {
        AddClientDialog(
            onDismiss = { showAddClientDialog = false },
            onSubmit = { name, cnic, phone, email, address, nomineeName, nomineeRelation, nomineeCnic, nomineePhone, notes ->
                viewModel.addClient(name, cnic, phone, email, address, nomineeName, nomineeRelation, nomineeCnic, nomineePhone, notes)
            }
        )
    }

    // Add Property Dialog
    if (showAddPropertyDialog) {
        AddPropertyDialog(
            clients = clients,
            onDismiss = { showAddPropertyDialog = false },
            onSubmit = { cId, proj, block, plot, size, type, price, booking, dev, other, possession, date ->
                viewModel.addProperty(cId, proj, block, plot, size, type, price, booking, dev, other, possession, date)
            }
        )
    }

    // Digital Receipt Voucher Dialog
    viewingReceiptPayment?.let { pay ->
        val client = clients.find { it.id == pay.clientId }
        val prop = properties.find { it.id == pay.propertyId }
        DigitalReceiptDialog(
            payment = pay,
            client = client,
            property = prop,
            onDismiss = { viewingReceiptPayment = null }
        )
    }
}
