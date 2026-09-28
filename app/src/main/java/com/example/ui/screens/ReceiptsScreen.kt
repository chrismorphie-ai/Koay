package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.ClientEntity
import com.example.data.models.PaymentEntity
import com.example.data.models.PropertyEntity
import com.example.ui.components.formatMoney
import com.example.ui.components.shareText
import com.example.ui.theme.EmeraldPrimary

@Composable
fun ReceiptsScreen(
    payments: List<PaymentEntity>,
    clients: List<ClientEntity>,
    properties: List<PropertyEntity>,
    onPaymentClick: (PaymentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Payment Receipts, 1: Booking Forms & Agreements
    var searchQuery by remember { mutableStateOf("") }

    val filteredPayments = payments.filter { pay ->
        val client = clients.find { it.id == pay.clientId }
        val prop = properties.find { it.id == pay.propertyId }
        searchQuery.isBlank() ||
                pay.receiptNumber.contains(searchQuery, ignoreCase = true) ||
                pay.referenceNumber.contains(searchQuery, ignoreCase = true) ||
                (client?.name?.contains(searchQuery, ignoreCase = true) == true) ||
                (prop?.plotNumber?.contains(searchQuery, ignoreCase = true) == true) ||
                pay.paymentDate.contains(searchQuery)
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Payment Vouchers (${filteredPayments.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Agreements (${properties.size})") }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTab == 0) {
                // Search bar for receipts
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search receipt / slip #, client, plot...") },
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
                        modifier = Modifier.fillMaxWidth().testTag("search_receipts_input")
                    )
                }

                // Payment Vouchers List
                if (filteredPayments.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    if (searchQuery.isNotBlank()) "No receipts matching '$searchQuery'" else "No payment receipts generated yet.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredPayments) { pay ->
                        val client = clients.find { it.id == pay.clientId }
                        val prop = properties.find { it.id == pay.propertyId }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPaymentClick(pay) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(pay.receiptNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${client?.name ?: "Client"} • ${prop?.plotNumber ?: "Plot"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${pay.paymentDate} • ${pay.paymentMethod}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatMoney(pay.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text("View Voucher", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Booking Deeds & Agreements
                items(properties) { prop ->
                    val client = clients.find { it.id == prop.clientId }
                    val agreementText = """
                    ESTATEPAY PROPERTIES - ALLOTMENT AGREEMENT
                    Deed Ref: AGR-${prop.id}-${prop.bookingDate.take(4)}
                    Client: ${client?.name} (CNIC: ${client?.cnicOrPassport})
                    Nominee: ${client?.nomineeName} (${client?.nomineeRelation})
                    Property: ${prop.projectName}, ${prop.blockName}, ${prop.plotNumber}
                    Category: ${prop.type} (${prop.size})
                    Total Consideration: ${formatMoney(prop.totalPrice)}
                    Booking Amount Paid: ${formatMoney(prop.bookingAmount)}
                    Allotment Date: ${prop.bookingDate}
                    Status: Allotment deed executed & registered.
                    """.trimIndent()

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
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Booking & Allotment Deed", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${prop.projectName} • ${prop.plotNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Official Document", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Buyer: ${client?.name ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                            Text("CNIC/Passport: ${client?.cnicOrPassport ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Booking Consideration: ${formatMoney(prop.bookingAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        shareText(context, "Allotment Agreement - ${prop.plotNumber}", agreementText)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share Agreement")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
