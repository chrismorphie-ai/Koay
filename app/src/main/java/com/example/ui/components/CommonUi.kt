package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.ClientEntity
import com.example.data.models.InstallmentEntity
import com.example.data.models.PaymentEntity
import com.example.data.models.PropertyEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPartial
import com.example.ui.theme.StatusPartialBg
import com.example.ui.theme.StatusUpcoming
import com.example.ui.theme.StatusUpcomingBg
import com.example.ui.theme.StatusWaived
import com.example.ui.theme.StatusWaivedBg
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Helper to format currency nicely
fun formatMoney(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 0
    return "Rs. " + formatter.format(amount)
}

fun shareText(context: Context, title: String, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, title)
    context.startActivity(shareIntent)
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status.lowercase(Locale.ROOT)) {
        "paid", "completed" -> Triple(StatusPaidBg, StatusPaid, Icons.Default.CheckCircle)
        "overdue" -> Triple(StatusOverdueBg, StatusOverdue, Icons.Default.Error)
        "partially paid", "partial" -> Triple(StatusPartialBg, StatusPartial, Icons.Default.Warning)
        "waived", "cancelled" -> Triple(StatusWaivedBg, StatusWaived, Icons.Default.Close)
        else -> Triple(StatusUpcomingBg, StatusUpcoming, Icons.Default.Info)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = status,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstateTopAppBar(
    title: String,
    isAdminMode: Boolean,
    unreadNotificationCount: Int,
    onToggleMode: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "EstatePay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (isAdminMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isAdminMode) "ADMIN / STAFF" else "CLIENT VIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAdminMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // Mode toggle button
            TextButton(
                onClick = onToggleMode,
                modifier = Modifier.testTag("mode_toggle_button")
            ) {
                Icon(
                    imageVector = if (isAdminMode) Icons.Default.Person else Icons.Default.AccountBalance,
                    contentDescription = "Switch Mode",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isAdminMode) "Switch to Client" else "Switch to Staff",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Notification Bell with badge
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.testTag("notification_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge {
                                Text("$unreadNotificationCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Dialog to Record Payment
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    clients: List<ClientEntity>,
    properties: List<PropertyEntity>,
    installments: List<InstallmentEntity>,
    initialClientId: Long? = null,
    initialPropertyId: Long? = null,
    initialInstallmentId: Long? = null,
    onDismiss: () -> Unit,
    onSubmit: (
        propertyId: Long,
        clientId: Long,
        installmentId: Long?,
        amount: Double,
        date: String,
        method: String,
        ref: String,
        notes: String,
        manualReceiptNumber: String?
    ) -> Unit
) {
    var selectedClientId by remember { mutableStateOf(initialClientId ?: clients.firstOrNull()?.id ?: 0L) }
    var selectedPropertyId by remember {
        val initialProp = properties.firstOrNull { it.clientId == selectedClientId }?.id ?: properties.firstOrNull()?.id ?: 0L
        mutableStateOf(initialPropertyId ?: initialProp)
    }
    var selectedInstallmentId by remember { mutableStateOf<Long?>(initialInstallmentId) }

    val clientProperties = properties.filter { it.clientId == selectedClientId }
    val propInstallments = installments.filter { it.propertyId == selectedPropertyId && it.status != "Paid" }

    var amountText by remember {
        val targetInst = installments.find { it.id == initialInstallmentId }
        val defaultAmt = if (targetInst != null) (targetInst.amountDue - targetInst.amountPaid).toString() else ""
        mutableStateOf(defaultAmt)
    }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var dateText by remember { mutableStateOf(todayStr) }
    var selectedMethod by remember { mutableStateOf("Bank Transfer") }
    var referenceText by remember { mutableStateOf("") }
    var manualReceiptNo by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    val methods = listOf("Bank Transfer", "Cash", "Online / Card", "Cheque")

    var clientExpanded by remember { mutableStateOf(false) }
    var propertyExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }
    var instExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Record Payment & Generate Receipt", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Client Dropdown
                ExposedDropdownMenuBox(
                    expanded = clientExpanded,
                    onExpandedChange = { clientExpanded = !clientExpanded }
                ) {
                    val clientName = clients.find { it.id == selectedClientId }?.name ?: "Select Client"
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Client") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
                                    selectedPropertyId = properties.firstOrNull { it.clientId == c.id }?.id ?: 0L
                                    selectedInstallmentId = null
                                    clientExpanded = false
                                }
                            )
                        }
                    }
                }

                // Property Dropdown
                ExposedDropdownMenuBox(
                    expanded = propertyExpanded,
                    onExpandedChange = { propertyExpanded = !propertyExpanded }
                ) {
                    val propName = clientProperties.find { it.id == selectedPropertyId }?.let {
                        "${it.projectName} - ${it.plotNumber}"
                    } ?: "Select Plot/Unit"
                    OutlinedTextField(
                        value = propName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Plot / Property") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = propertyExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = propertyExpanded,
                        onDismissRequest = { propertyExpanded = false }
                    ) {
                        clientProperties.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.projectName} • ${p.blockName} • ${p.plotNumber}") },
                                onClick = {
                                    selectedPropertyId = p.id
                                    selectedInstallmentId = null
                                    propertyExpanded = false
                                }
                            )
                        }
                    }
                }

                // Installment Link Dropdown
                ExposedDropdownMenuBox(
                    expanded = instExpanded,
                    onExpandedChange = { instExpanded = !instExpanded }
                ) {
                    val instName = installments.find { it.id == selectedInstallmentId }?.let {
                        "${it.title} (Due: ${formatMoney(it.amountDue - it.amountPaid)})"
                    } ?: "Lump Sum / Advance Payment"
                    OutlinedTextField(
                        value = instName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Against Installment") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = instExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = instExpanded,
                        onDismissRequest = { instExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Lump Sum / Advance (Auto-adjust)") },
                            onClick = {
                                selectedInstallmentId = null
                                instExpanded = false
                            }
                        )
                        propInstallments.forEach { inst ->
                            val pending = inst.amountDue - inst.amountPaid
                            DropdownMenuItem(
                                text = { Text("${inst.title} - ${formatMoney(pending)} (${inst.status})") },
                                onClick = {
                                    selectedInstallmentId = inst.id
                                    amountText = pending.toInt().toString()
                                    instExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Paid (Rs.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                // Date
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Payment Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Payment Method
                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = !methodExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false }
                    ) {
                        methods.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    selectedMethod = m
                                    methodExpanded = false
                                }
                            )
                        }
                    }
                }

                // Manual Receipt Number
                OutlinedTextField(
                    value = manualReceiptNo,
                    onValueChange = { manualReceiptNo = it },
                    label = { Text("Receipt / Slip Number (Manual / Custom)") },
                    placeholder = { Text("Leave blank to auto-generate (e.g. RCP-2026-XXXX)") },
                    leadingIcon = {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (manualReceiptNo.isNotEmpty()) {
                            IconButton(onClick = { manualReceiptNo = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    supportingText = {
                        Text(
                            text = if (manualReceiptNo.isBlank()) "Auto-generates if empty. Or type your physical slip/booklet # (e.g. BK-1042, RCP-77)" else "Custom receipt # active for this voucher & ledger entry",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (manualReceiptNo.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("manual_receipt_no_input")
                )

                // Reference / Cheque #
                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { referenceText = it },
                    label = { Text("Reference / Cheque / Txn Number") },
                    placeholder = { Text("e.g. FT-99823 or Chq #4012") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Remarks / Notes") },
                    placeholder = { Text("e.g. Received at site office") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && selectedPropertyId > 0 && selectedClientId > 0) {
                        onSubmit(
                            selectedPropertyId,
                            selectedClientId,
                            selectedInstallmentId,
                            amt,
                            dateText,
                            selectedMethod,
                            referenceText,
                            notesText,
                            manualReceiptNo.trim().takeIf { it.isNotEmpty() }
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_payment_button")
            ) {
                Text("Confirm & Issue Receipt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog to Add Client
@Composable
fun AddClientDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        name: String, cnic: String, phone: String, email: String, address: String,
        nomineeName: String, nomineeRelation: String, nomineeCnic: String, nomineePhone: String, notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cnic by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var nomineeName by remember { mutableStateOf("") }
    var nomineeRelation by remember { mutableStateOf("") }
    var nomineeCnic by remember { mutableStateOf("") }
    var nomineePhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add New Client & Nominee", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Client Details", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Client Full Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("client_name_input")
                )
                OutlinedTextField(
                    value = cnic,
                    onValueChange = { cnic = it },
                    label = { Text("CNIC / Passport Number *") },
                    placeholder = { Text("e.g. 35201-1234567-1") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Residential Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("Family / Nominee Information", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = nomineeName,
                    onValueChange = { nomineeName = it },
                    label = { Text("Nominee Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nomineeRelation,
                    onValueChange = { nomineeRelation = it },
                    label = { Text("Relation with Client (e.g. Wife, Son)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nomineeCnic,
                    onValueChange = { nomineeCnic = it },
                    label = { Text("Nominee CNIC") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nomineePhone,
                    onValueChange = { nomineePhone = it },
                    label = { Text("Nominee Contact") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Agreements & Document Notes") },
                    placeholder = { Text("e.g. CNIC Scanned, Original Deed Signed") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && cnic.isNotBlank() && phone.isNotBlank()) {
                        onSubmit(name, cnic, phone, email, address, nomineeName, nomineeRelation, nomineeCnic, nomineePhone, notes)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_client_button")
            ) {
                Text("Save Client")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog to Add Property/Plot
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyDialog(
    clients: List<ClientEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        clientId: Long, projectName: String, blockName: String, plotNumber: String,
        size: String, type: String, totalPrice: Double, bookingAmount: Double,
        developmentCharges: Double, otherCharges: Double, possessionAmount: Double, bookingDate: String
    ) -> Unit
) {
    var selectedClientId by remember { mutableStateOf(clients.firstOrNull()?.id ?: 0L) }
    var projectName by remember { mutableStateOf("Emerald Heights Residency") }
    var blockName by remember { mutableStateOf("Executive Block A") }
    var plotNumber by remember { mutableStateOf("Plot #") }
    var size by remember { mutableStateOf("10 Marla (250 Sq Yds)") }
    var type by remember { mutableStateOf("Residential Plot") }
    var totalPriceText by remember { mutableStateOf("8000000") }
    var bookingAmountText by remember { mutableStateOf("1600000") }
    var devChargesText by remember { mutableStateOf("500000") }
    var otherChargesText by remember { mutableStateOf("0") }
    var possessionAmountText by remember { mutableStateOf("1000000") }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var bookingDate by remember { mutableStateOf(todayStr) }

    var clientExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Plot / Property Unit", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Client selector
                ExposedDropdownMenuBox(
                    expanded = clientExpanded,
                    onExpandedChange = { clientExpanded = !clientExpanded }
                ) {
                    val clientName = clients.find { it.id == selectedClientId }?.name ?: "Select Client"
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign to Client *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = clientExpanded,
                        onDismissRequest = { clientExpanded = false }
                    ) {
                        clients.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedClientId = c.id
                                    clientExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Project Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = blockName,
                        onValueChange = { blockName = it },
                        label = { Text("Block / Sector *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = plotNumber,
                        onValueChange = { plotNumber = it },
                        label = { Text("Plot/Unit # *") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Size") },
                        placeholder = { Text("e.g. 5 Marla") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = type,
                        onValueChange = { type = it },
                        label = { Text("Type") },
                        placeholder = { Text("Residential / Comm") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = totalPriceText,
                    onValueChange = { totalPriceText = it },
                    label = { Text("Total Property Price (Rs.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bookingAmountText,
                    onValueChange = { bookingAmountText = it },
                    label = { Text("Booking / Down Payment (Rs.) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = devChargesText,
                        onValueChange = { devChargesText = it },
                        label = { Text("Dev Charges") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = otherChargesText,
                        onValueChange = { otherChargesText = it },
                        label = { Text("Other Charges") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = possessionAmountText,
                    onValueChange = { possessionAmountText = it },
                    label = { Text("Possession Payment (Rs.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bookingDate,
                    onValueChange = { bookingDate = it },
                    label = { Text("Booking Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = totalPriceText.toDoubleOrNull() ?: 0.0
                    val booking = bookingAmountText.toDoubleOrNull() ?: 0.0
                    val dev = devChargesText.toDoubleOrNull() ?: 0.0
                    val other = otherChargesText.toDoubleOrNull() ?: 0.0
                    val possession = possessionAmountText.toDoubleOrNull() ?: 0.0
                    if (selectedClientId > 0 && projectName.isNotBlank() && plotNumber.isNotBlank() && price > 0) {
                        onSubmit(selectedClientId, projectName, blockName, plotNumber, size, type, price, booking, dev, other, possession, bookingDate)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_property_button")
            ) {
                Text("Create Plot & Plan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Digital Printable Receipt Voucher Dialog
@Composable
fun DigitalReceiptDialog(
    payment: PaymentEntity,
    client: ClientEntity?,
    property: PropertyEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val receiptText = remember(payment, client, property) {
        """
        ==================================================
                    ESTATEPAY PROPERTIES LTD.
                 OFFICIAL PAYMENT VOUCHER / RECEIPT
        ==================================================
        Receipt No:   ${payment.receiptNumber}
        Date:         ${payment.paymentDate}
        Payment Mode: ${payment.paymentMethod}
        Ref / Chq #:  ${if (payment.referenceNumber.isNotEmpty()) payment.referenceNumber else "N/A"}
        --------------------------------------------------
        CLIENT DETAILS:
        Name:         ${client?.name ?: "N/A"}
        CNIC/Pass:    ${client?.cnicOrPassport ?: "N/A"}
        Contact:      ${client?.phone ?: "N/A"}
        
        PROPERTY ALLOTMENT:
        Project:      ${property?.projectName ?: "N/A"}
        Plot/Unit:    ${property?.plotNumber ?: "N/A"} (${property?.blockName ?: ""})
        Type & Size:  ${property?.type ?: ""} - ${property?.size ?: ""}
        --------------------------------------------------
        AMOUNT RECEIVED:
        ${formatMoney(payment.amount)}
        Remarks:      ${if (payment.notes.isNotEmpty()) payment.notes else "Installment Payment Cleared"}
        --------------------------------------------------
        Status:       CONFIRMED & RECONCILED
        Issued By:    Finance & Accounts Department
        Verified:     Digital Stamp (Auto-generated)
        ==================================================
        """.trimIndent()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "ESTATEPAY DEVELOPERS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Official Payment Voucher",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Voucher Card Look
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Receipt No.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(payment.receiptNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Payment Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(payment.paymentDate, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Amount Banner
                        Surface(
                            color = EmeraldPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Column {
                                    Text("Amount Paid", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                                    Text(
                                        formatMoney(payment.amount),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldPrimary
                                    )
                                }
                                StatusBadge(status = "Paid")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Key detail rows
                        ReceiptRow("Received From", client?.name ?: "N/A")
                        ReceiptRow("CNIC / Passport", client?.cnicOrPassport ?: "N/A")
                        ReceiptRow("Project & Plot", "${property?.projectName ?: ""} • ${property?.plotNumber ?: ""}")
                        ReceiptRow("Block / Sector", property?.blockName ?: "N/A")
                        ReceiptRow("Payment Method", payment.paymentMethod)
                        if (payment.referenceNumber.isNotEmpty()) {
                            ReceiptRow("Ref / Cheque #", payment.referenceNumber)
                        }
                        if (payment.notes.isNotEmpty()) {
                            ReceiptRow("Remarks", payment.notes)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Share & Copy
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            shareText(context, "Receipt ${payment.receiptNumber}", receiptText)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Receipt")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}
