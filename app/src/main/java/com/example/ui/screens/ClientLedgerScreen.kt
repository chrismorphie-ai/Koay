package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ClientEntity
import com.example.data.models.LedgerEntity
import com.example.data.models.PropertyEntity
import com.example.ui.components.formatMoney
import com.example.ui.components.shareText
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPaid
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientLedgerScreen(
    clients: List<ClientEntity>,
    properties: List<PropertyEntity>,
    ledgerEntries: List<LedgerEntity>,
    initialClientId: Long? = null,
    onAddDebitCharge: (clientId: Long, propertyId: Long, description: String, amount: Double, date: String) -> Unit,
    onRecordPaymentClick: ((clientId: Long?, propertyId: Long?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedClientId by remember { mutableStateOf<Long?>(initialClientId ?: clients.firstOrNull()?.id) }
    var selectedPropertyId by remember { mutableStateOf<Long?>(null) }
    var clientExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddChargeDialog by remember { mutableStateOf(false) }

    val activeClient = clients.find { it.id == selectedClientId }
    val clientProperties = properties.filter { it.clientId == selectedClientId }
    val activeProperty = properties.find { it.id == selectedPropertyId }

    // Filtered ledger entries
    val filteredEntries = ledgerEntries.filter { entry ->
        val matchesClient = selectedClientId == null || entry.clientId == selectedClientId
        val matchesProp = selectedPropertyId == null || entry.propertyId == selectedPropertyId
        val matchesSearch = searchQuery.isBlank() || entry.description.contains(searchQuery, ignoreCase = true) || entry.date.contains(searchQuery)
        matchesClient && matchesProp && matchesSearch
    }

    val totalDebit = filteredEntries.sumOf { it.debit }
    val totalCredit = filteredEntries.sumOf { it.credit }
    val currentBalance = filteredEntries.lastOrNull()?.runningBalance ?: (totalDebit - totalCredit)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title & Client Selector Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
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
                        Column {
                            Text(
                                text = "Client Financial Ledger",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Real-time debit, credit & running balance ledger",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                val statement = buildLedgerExportText(
                                    client = activeClient,
                                    property = activeProperty,
                                    entries = filteredEntries,
                                    totalDebit = totalDebit,
                                    totalCredit = totalCredit,
                                    balance = currentBalance
                                )
                                shareText(context, "Ledger Statement - ${activeClient?.name ?: "Client"}", statement)
                            },
                            modifier = Modifier.testTag("share_ledger_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Statement",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Client Selection Dropdown
                    ExposedDropdownMenuBox(
                        expanded = clientExpanded,
                        onExpandedChange = { clientExpanded = !clientExpanded }
                    ) {
                        OutlinedTextField(
                            value = activeClient?.let { "${it.name} (${it.cnicOrPassport})" } ?: "Select Client",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filter by Client") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("ledger_client_selector")
                        )
                        ExposedDropdownMenu(
                            expanded = clientExpanded,
                            onDismissRequest = { clientExpanded = false }
                        ) {
                            clients.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.name} (${c.cnicOrPassport})") },
                                    onClick = {
                                        selectedClientId = c.id
                                        selectedPropertyId = null
                                        clientExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (clientProperties.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Property Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedPropertyId == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedPropertyId = null }
                            ) {
                                Text(
                                    text = "All Properties (${clientProperties.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedPropertyId == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            clientProperties.forEach { p ->
                                val isSelected = selectedPropertyId == p.id
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { selectedPropertyId = p.id }
                                ) {
                                    Text(
                                        text = "${p.projectName} • ${p.plotNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ledger Balance Summary Strip (Debit vs Credit vs Balance)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Debits", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = formatMoney(totalDebit),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Credits", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                        Text(
                            text = formatMoney(totalCredit),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Net Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                        Text(
                            text = formatMoney(currentBalance),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        // Actions & Search Filter bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search receipt #, description, date...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = { showAddChargeDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_debit_charge_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Charge", maxLines = 1)
                    }
                }

                if (onRecordPaymentClick != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = { onRecordPaymentClick(selectedClientId, selectedPropertyId) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("ledger_record_payment_button")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post Payment & Receipt", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Ledger Table Header
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date & Description",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.3f)
                    )
                    Text(
                        text = "Debit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.9f)
                    )
                    Text(
                        text = "Credit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.9f)
                    )
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Ledger Items
        if (filteredEntries.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No ledger entries found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredEntries) { entry ->
                LedgerEntryRow(entry = entry)
            }
        }
    }

    // Add Charge Dialog
    if (showAddChargeDialog) {
        AddChargeDialog(
            clients = clients,
            properties = properties,
            selectedClientId = selectedClientId ?: clients.firstOrNull()?.id ?: 0L,
            onDismiss = { showAddChargeDialog = false },
            onSubmit = { cId, pId, desc, amt, date ->
                onAddDebitCharge(cId, pId, desc, amt, date)
                showAddChargeDialog = false
            }
        )
    }
}

@Composable
fun LedgerEntryRow(entry: LedgerEntity) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Debit Column
            Text(
                text = if (entry.debit > 0) formatMoney(entry.debit) else "—",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (entry.debit > 0) FontWeight.SemiBold else FontWeight.Normal,
                color = if (entry.debit > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.9f)
            )

            // Credit Column
            Text(
                text = if (entry.credit > 0) formatMoney(entry.credit) else "—",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (entry.credit > 0) FontWeight.Bold else FontWeight.Normal,
                color = if (entry.credit > 0) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.9f)
            )

            // Running Balance Column
            Text(
                text = formatMoney(entry.runningBalance),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Dialog to add debit charge (e.g. Surcharge, Dev charge, transfer fee)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChargeDialog(
    clients: List<ClientEntity>,
    properties: List<PropertyEntity>,
    selectedClientId: Long,
    onDismiss: () -> Unit,
    onSubmit: (clientId: Long, propertyId: Long, description: String, amount: Double, date: String) -> Unit
) {
    var cId by remember { mutableStateOf(selectedClientId) }
    val clientProps = properties.filter { it.clientId == cId }
    var pId by remember { mutableStateOf(clientProps.firstOrNull()?.id ?: 0L) }
    var desc by remember { mutableStateOf("Development Charges") }
    var amountText by remember { mutableStateOf("100000") }
    var voucherNo by remember { mutableStateOf("") }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var dateText by remember { mutableStateOf(today) }

    val chargeOptions = listOf(
        "Development Charges",
        "Late Payment Surcharge",
        "Corner / Park Facing Premium",
        "Utility Connection Charges",
        "Transfer / Verification Fee",
        "Possession Fee"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Debit Charge to Ledger", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Charge Type:", style = MaterialTheme.typography.labelSmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    chargeOptions.forEach { opt ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (desc == opt) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { desc = opt }
                        ) {
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (desc == opt) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Manual Voucher / Slip #
                OutlinedTextField(
                    value = voucherNo,
                    onValueChange = { voucherNo = it },
                    label = { Text("Manual Voucher / Bill / Slip # (Optional)") },
                    placeholder = { Text("e.g. VR-9021 or CHG-401") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("charge_voucher_no_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (Rs.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && cId > 0 && pId > 0) {
                        val finalDesc = if (voucherNo.isNotBlank()) "$desc [Ref #${voucherNo.trim()}]" else desc
                        onSubmit(cId, pId, finalDesc, amt, dateText)
                    }
                }
            ) {
                Text("Post Charge to Ledger")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun buildLedgerExportText(
    client: ClientEntity?,
    property: PropertyEntity?,
    entries: List<LedgerEntity>,
    totalDebit: Double,
    totalCredit: Double,
    balance: Double
): String {
    val sb = StringBuilder()
    sb.appendLine("==================================================")
    sb.appendLine("        ESTATEPAY PROPERTIES - CLIENT LEDGER")
    sb.appendLine("==================================================")
    sb.appendLine("Client:       ${client?.name ?: "N/A"}")
    sb.appendLine("CNIC/Pass:    ${client?.cnicOrPassport ?: "N/A"}")
    sb.appendLine("Contact:      ${client?.phone ?: "N/A"}")
    if (property != null) {
        sb.appendLine("Property:     ${property.projectName} - ${property.plotNumber} (${property.blockName})")
        sb.appendLine("Size & Type:  ${property.size} - ${property.type}")
    }
    sb.appendLine("Generated On: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
    sb.appendLine("--------------------------------------------------")
    sb.appendLine(String.format(Locale.US, "%-11s | %-20s | %-10s | %-10s | %-10s", "Date", "Description", "Debit", "Credit", "Balance"))
    sb.appendLine("--------------------------------------------------")
    for (e in entries) {
        val dStr = if (e.debit > 0) formatMoney(e.debit) else "—"
        val cStr = if (e.credit > 0) formatMoney(e.credit) else "—"
        val bStr = formatMoney(e.runningBalance)
        sb.appendLine(String.format(Locale.US, "%-11s | %-20s | %-10s | %-10s | %-10s", e.date, e.description.take(20), dStr, cStr, bStr))
    }
    sb.appendLine("==================================================")
    sb.appendLine("TOTAL DEBITS (Billed):    ${formatMoney(totalDebit)}")
    sb.appendLine("TOTAL CREDITS (Received): ${formatMoney(totalCredit)}")
    sb.appendLine("NET OUTSTANDING BALANCE:  ${formatMoney(balance)}")
    sb.appendLine("==================================================")
    sb.appendLine("Note: Computer generated financial ledger statement.")
    return sb.toString()
}
