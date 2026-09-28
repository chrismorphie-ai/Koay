package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.ClientEntity
import com.example.data.models.InstallmentEntity
import com.example.data.models.PaymentEntity
import com.example.data.models.PropertyEntity
import com.example.ui.components.formatMoney
import com.example.ui.components.shareText
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusUpcoming
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    payments: List<PaymentEntity>,
    clients: List<ClientEntity>,
    properties: List<PropertyEntity>,
    installments: List<InstallmentEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val todayStr = dateFormat.format(Date())
    val monthStr = monthFormat.format(Date())

    val totalCollected = payments.sumOf { it.amount }
    val todayCollected = payments.filter { it.paymentDate == todayStr }.sumOf { it.amount }
    val monthCollected = payments.filter { it.paymentDate.startsWith(monthStr) }.sumOf { it.amount }

    val overdueInsts = installments.filter { it.status == "Overdue" }
    val overdueTotal = overdueInsts.sumOf { (it.amountDue - it.amountPaid).coerceAtLeast(0.0) }
    val overdueClientIds = overdueInsts.map { it.clientId }.toSet()

    // Payment methods aggregation
    val bankTotal = payments.filter { it.paymentMethod.contains("Bank", ignoreCase = true) }.sumOf { it.amount }
    val cashTotal = payments.filter { it.paymentMethod.contains("Cash", ignoreCase = true) }.sumOf { it.amount }
    val onlineTotal = payments.filter { it.paymentMethod.contains("Online", ignoreCase = true) || it.paymentMethod.contains("Card", ignoreCase = true) }.sumOf { it.amount }
    val chequeTotal = payments.filter { it.paymentMethod.contains("Cheque", ignoreCase = true) }.sumOf { it.amount }

    // Project aggregation
    val projectMap = properties.groupBy { it.projectName }

    val reportText = """
    ==================================================
              ESTATEPAY FINANCIAL REPORT SUMMARY
    ==================================================
    Generated:    ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}
    Total Units:  ${properties.size} Properties Sold
    Total Clients:${clients.size} Registered Clients
    --------------------------------------------------
    COLLECTIONS OVERVIEW:
    Total Lifetime Collection: ${formatMoney(totalCollected)}
    Collections This Month:    ${formatMoney(monthCollected)}
    Collections Today:         ${formatMoney(todayCollected)}
    --------------------------------------------------
    RECEIVABLES & OVERDUE:
    Overdue Clients:           ${overdueClientIds.size}
    Overdue Amount:            ${formatMoney(overdueTotal)}
    --------------------------------------------------
    PAYMENT METHODS BREAKDOWN:
    Bank Transfer:             ${formatMoney(bankTotal)}
    Online / Card:             ${formatMoney(onlineTotal)}
    Cheque Clearance:          ${formatMoney(chequeTotal)}
    Cash Collection:           ${formatMoney(cashTotal)}
    ==================================================
    """.trimIndent()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column {
                        Text("Executive Reports & Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Daily / Monthly Collections & Overdue Audit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = { shareText(context, "Executive Collection Report", reportText) },
                        modifier = Modifier.testTag("export_report_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export Report", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Daily / Monthly KPI
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Collection Performance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Today's Collection", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatMoney(todayCollected), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Current Month", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatMoney(monthCollected), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Total Lifetime Inflow", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatMoney(totalCollected), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Overdue Pending", style = MaterialTheme.typography.labelSmall, color = StatusOverdue)
                            Text(formatMoney(overdueTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = StatusOverdue)
                        }
                    }
                }
            }
        }

        // Overdue Clients Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Overdue Installments Audit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("${overdueInsts.size} Overdue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    if (overdueInsts.isEmpty()) {
                        Text("No overdue installments. All collections are up to date!", style = MaterialTheme.typography.bodySmall, color = EmeraldPrimary)
                    } else {
                        overdueInsts.forEach { inst ->
                            val client = clients.find { it.id == inst.clientId }
                            val prop = properties.find { it.id == inst.propertyId }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column {
                                    Text(client?.name ?: "Client", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text("${prop?.plotNumber ?: "Plot"} • Due: ${inst.dueDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    formatMoney(inst.amountDue - inst.amountPaid),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusOverdue
                                )
                            }
                        }
                    }
                }
            }
        }

        // Payment Method Breakdown
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Payment Methods Distribution", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    MethodProgressRow(label = "Bank Transfer", amount = bankTotal, total = totalCollected)
                    MethodProgressRow(label = "Online / Card", amount = onlineTotal, total = totalCollected)
                    MethodProgressRow(label = "Cheque", amount = chequeTotal, total = totalCollected)
                    MethodProgressRow(label = "Cash at Office", amount = cashTotal, total = totalCollected)
                }
            }
        }

        // Project-Wise Collections
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Project-Wise Property Sales", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    projectMap.forEach { (projectName, propList) ->
                        val propIds = propList.map { it.id }.toSet()
                        val projectCollected = payments.filter { it.propertyId in propIds }.sumOf { it.amount }
                        val projectTotalValue = propList.sumOf { it.totalPrice }

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(projectName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text("${propList.size} Units", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Collected: ${formatMoney(projectCollected)}", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                                Text("Value: ${formatMoney(projectTotalValue)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MethodProgressRow(label: String, amount: Double, total: Double) {
    val fraction = if (total > 0) (amount / total).toFloat().coerceIn(0f, 1f) else 0f
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("${formatMoney(amount)} (${(fraction * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
