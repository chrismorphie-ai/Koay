package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.models.ClientEntity
import com.example.data.models.InstallmentEntity
import com.example.data.models.PropertyEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatMoney
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUpcoming
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallmentPlanScreen(
    properties: List<PropertyEntity>,
    clients: List<ClientEntity>,
    installments: List<InstallmentEntity>,
    initialPropertyId: Long? = null,
    onPayInstallment: (propertyId: Long, clientId: Long, installmentId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPropId by remember {
        mutableStateOf(initialPropertyId ?: properties.firstOrNull()?.id ?: 0L)
    }
    var propertyExpanded by remember { mutableStateOf(false) }

    val activeProperty = properties.find { it.id == selectedPropId }
    val activeClient = clients.find { it.id == activeProperty?.clientId }
    val propInstallments = installments.filter { it.propertyId == selectedPropId }

    val totalDue = propInstallments.sumOf { it.amountDue }
    val totalPaid = propInstallments.sumOf { it.amountPaid }
    val remaining = (totalDue - totalPaid).coerceAtLeast(0.0)
    val progress = if (totalDue > 0) (totalPaid / totalDue).toFloat().coerceIn(0f, 1f) else 0f

    val paidCount = propInstallments.count { it.status == "Paid" }
    val overdueCount = propInstallments.count { it.status == "Overdue" }
    val upcomingCount = propInstallments.count { it.status == "Upcoming" || it.status == "Partially Paid" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Property Selector Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Installment Payment Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track custom payment schedules, dues, and statuses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Property dropdown
                    ExposedDropdownMenuBox(
                        expanded = propertyExpanded,
                        onExpandedChange = { propertyExpanded = !propertyExpanded }
                    ) {
                        val propLabel = activeProperty?.let {
                            "${it.projectName} • ${it.blockName} • ${it.plotNumber}"
                        } ?: "Select Property"

                        OutlinedTextField(
                            value = propLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Property / Plot") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = propertyExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("plan_property_selector")
                        )
                        ExposedDropdownMenu(
                            expanded = propertyExpanded,
                            onDismissRequest = { propertyExpanded = false }
                        ) {
                            properties.forEach { p ->
                                val clientName = clients.find { it.id == p.clientId }?.name ?: ""
                                DropdownMenuItem(
                                    text = { Text("${p.projectName} • ${p.plotNumber} ($clientName)") },
                                    onClick = {
                                        selectedPropId = p.id
                                        propertyExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (activeProperty != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Client: ${activeClient?.name ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Total: ${formatMoney(activeProperty.totalPrice)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Summary Bar & Progress
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Total Paid", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                            Text(formatMoney(totalPaid), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatMoney(remaining), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surface,
                        strokeCap = StrokeCap.Round
                    )

                    // Badges strip
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${propInstallments.size}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Paid", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                                Text("$paidCount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Overdue", style = MaterialTheme.typography.labelSmall, color = StatusOverdue)
                                Text("$overdueCount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StatusOverdue)
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Upcoming", style = MaterialTheme.typography.labelSmall, color = StatusUpcoming)
                                Text("$upcomingCount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StatusUpcoming)
                            }
                        }
                    }
                }
            }
        }

        // Installment List Header
        item {
            Text(
                text = "Payment Schedule (${propInstallments.size} Installments)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (propInstallments.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No installment schedule generated for this property.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(propInstallments) { inst ->
                InstallmentCardItem(
                    installment = inst,
                    onPay = {
                        if (activeProperty != null && activeClient != null) {
                            onPayInstallment(activeProperty.id, activeClient.id, inst.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InstallmentCardItem(
    installment: InstallmentEntity,
    onPay: () -> Unit
) {
    val pendingAmount = (installment.amountDue - installment.amountPaid).coerceAtLeast(0.0)

    Card(
        shape = RoundedCornerShape(14.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                when (installment.status) {
                                    "Paid" -> EmeraldPrimary.copy(alpha = 0.15f)
                                    "Overdue" -> StatusOverdue.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (installment.installmentNumber == 0) "DP" else if (installment.installmentNumber == 999) "POS" else "#${installment.installmentNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (installment.status) {
                                "Paid" -> EmeraldPrimary
                                "Overdue" -> StatusOverdue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = installment.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Due: ${installment.dueDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                StatusBadge(status = installment.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Amounts strip
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Total Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatMoney(installment.amountDue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Amount Paid", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                    Text(formatMoney(installment.amountPaid), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Pending", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatMoney(pendingAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (pendingAmount > 0) MaterialTheme.colorScheme.onSurface else EmeraldPrimary
                    )
                }
            }

            // Quick Pay button if not paid
            if (installment.status != "Paid") {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onPay,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("pay_installment_btn_${installment.id}")
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay ${formatMoney(pendingAmount)}")
                }
            } else if (installment.lastPaymentDate != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "✓ Cleared on ${installment.lastPaymentDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
