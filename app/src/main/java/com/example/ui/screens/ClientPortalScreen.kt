package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.ClientEntity
import com.example.data.models.InstallmentEntity
import com.example.data.models.LedgerEntity
import com.example.data.models.PaymentEntity
import com.example.data.models.PropertyEntity
import com.example.ui.ClientFinancialSummary
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatMoney
import com.example.ui.components.shareText
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUpcoming

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientPortalScreen(
    summary: ClientFinancialSummary,
    clients: List<ClientEntity>,
    installments: List<InstallmentEntity>,
    payments: List<PaymentEntity>,
    ledgerEntries: List<LedgerEntity>,
    onSelectClient: (Long) -> Unit,
    onPayInstallment: (propertyId: Long, clientId: Long, installmentId: Long?) -> Unit,
    onPaymentClick: (PaymentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var clientExpanded by remember { mutableStateOf(false) }
    var selectedPortalTab by remember { mutableStateOf(0) } // 0: Schedule, 1: Ledger, 2: Receipts, 3: Property Details

    val client = summary.client
    val clientProps = summary.properties
    val clientInsts = installments.filter { it.clientId == client?.id }
    val clientPays = payments.filter { it.clientId == client?.id }
    val clientLedger = ledgerEntries.filter { it.clientId == client?.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Client Switcher
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Client Self-Service Portal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Switch viewing persona or client account", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = clientExpanded,
                        onExpandedChange = { clientExpanded = !clientExpanded }
                    ) {
                        OutlinedTextField(
                            value = client?.let { "${it.name} (${it.cnicOrPassport})" } ?: "Select Client",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Logged-in Client") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("client_portal_switcher")
                        )
                        ExposedDropdownMenu(
                            expanded = clientExpanded,
                            onDismissRequest = { clientExpanded = false }
                        ) {
                            clients.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.name} (${c.cnicOrPassport})") },
                                    onClick = {
                                        onSelectClient(c.id)
                                        clientExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hero Financial Overview Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = client?.name ?: "My Portfolio",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val propCount = clientProps.size
                            Text(
                                text = "$propCount Property Unit(s) Registered",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        StatusBadge(status = client?.status ?: "Active")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 KPI numbers
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Total Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(formatMoney(summary.totalPropertyPrice), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Paid", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                            Text(formatMoney(summary.totalPaid), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(formatMoney(summary.remainingBalance), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { summary.paymentProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(summary.paymentProgress * 100).toInt()}% of total property investment cleared",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Overdue Alert Banner (if client has overdue amount)
        if (summary.overdueAmount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusOverdueBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusOverdue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Payment Overdue Notice", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StatusOverdue)
                                Text("Overdue amount: ${formatMoney(summary.overdueAmount)}. Please pay now to avoid late fee.", style = MaterialTheme.typography.bodySmall, color = StatusOverdue)
                            }
                        }
                    }
                }
            }
        }

        // Next Installment Due Card
        summary.nextInstallment?.let { nextInst ->
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = StatusUpcoming)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Next Installment Due", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            StatusBadge(status = nextInst.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(nextInst.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Due Date: ${nextInst.dueDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                formatMoney(nextInst.amountDue - nextInst.amountPaid),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (client != null) {
                                    onPayInstallment(nextInst.propertyId, client.id, nextInst.id)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("client_pay_installment_btn")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pay Installment Now")
                        }
                    }
                }
            }
        }

        // Sub-tabs: Schedule, Ledger, Receipts, Property
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedPortalTab,
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 0.dp
            ) {
                Tab(selected = selectedPortalTab == 0, onClick = { selectedPortalTab = 0 }, text = { Text("Schedule (${clientInsts.size})") })
                Tab(selected = selectedPortalTab == 1, onClick = { selectedPortalTab = 1 }, text = { Text("My Ledger (${clientLedger.size})") })
                Tab(selected = selectedPortalTab == 2, onClick = { selectedPortalTab = 2 }, text = { Text("Receipts (${clientPays.size})") })
                Tab(selected = selectedPortalTab == 3, onClick = { selectedPortalTab = 3 }, text = { Text("Property Info") })
            }
        }

        // Tab Content
        when (selectedPortalTab) {
            0 -> {
                // Schedule
                if (clientInsts.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No installments found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(clientInsts) { inst ->
                        InstallmentCardItem(
                            installment = inst,
                            onPay = {
                                if (client != null) {
                                    onPayInstallment(inst.propertyId, client.id, inst.id)
                                }
                            }
                        )
                    }
                }
            }
            1 -> {
                // Personal Ledger
                if (clientLedger.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No ledger entries found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(clientLedger) { entry ->
                        LedgerEntryRow(entry = entry)
                    }
                }
            }
            2 -> {
                // Receipts
                if (clientPays.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No payments recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(clientPays) { pay ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onPaymentClick(pay) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(pay.receiptNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${pay.paymentDate} • ${pay.paymentMethod}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(formatMoney(pay.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                    }
                }
            }
            3 -> {
                // Property info
                items(clientProps) { p ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${p.projectName} • ${p.plotNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Block: ${p.blockName}", style = MaterialTheme.typography.bodySmall)
                            Text("Category: ${p.type} (${p.size})", style = MaterialTheme.typography.bodySmall)
                            Text("Booking Date: ${p.bookingDate}", style = MaterialTheme.typography.bodySmall)
                            Text("Total Property Value: ${formatMoney(p.totalPrice)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
