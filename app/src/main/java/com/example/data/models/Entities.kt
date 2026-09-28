package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cnicOrPassport: String,
    val phone: String,
    val email: String,
    val address: String,
    val nomineeName: String,
    val nomineeRelation: String,
    val nomineeCnic: String,
    val nomineePhone: String,
    val status: String = "Active", // "Active", "Completed", "Overdue", "Cancelled"
    val documentsInfo: String = "CNIC Scanned, Allotment Agreement Signed",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val projectName: String,
    val blockName: String,
    val plotNumber: String,
    val size: String, // e.g. "5 Marla", "10 Marla", "1 Kanal", "120 Sq Yds"
    val type: String, // "Residential Plot", "Commercial Shop", "Luxury Villa", "Apartment"
    val totalPrice: Double,
    val bookingAmount: Double,
    val developmentCharges: Double = 0.0,
    val otherCharges: Double = 0.0, // Corner, park-facing, processing
    val possessionAmount: Double = 0.0,
    val status: String = "Installments Active", // "Booked", "Installments Active", "Possession Handed", "Completed", "Cancelled"
    val bookingDate: String = "2026-01-15",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "installments")
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propertyId: Long,
    val clientId: Long,
    val installmentNumber: Int, // 0 for Down payment, 1..N for regular, 999 for Possession
    val title: String, // "Down Payment", "Installment #1", "Possession / Final"
    val dueDate: String, // YYYY-MM-DD
    val amountDue: Double,
    val amountPaid: Double = 0.0,
    val status: String = "Upcoming", // "Paid", "Partially Paid", "Overdue", "Upcoming", "Waived"
    val lastPaymentDate: String? = null
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propertyId: Long,
    val clientId: Long,
    val installmentId: Long? = null,
    val receiptNumber: String,
    val amount: Double,
    val paymentDate: String, // YYYY-MM-DD
    val paymentMethod: String, // "Bank Transfer", "Cash", "Online / Card", "Cheque"
    val referenceNumber: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ledger_entries")
data class LedgerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val propertyId: Long,
    val date: String,
    val description: String,
    val debit: Double = 0.0, // Increases client liability/balance
    val credit: Double = 0.0, // Client payment, reduces balance
    val runningBalance: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long? = null, // null for broadcast announcements
    val title: String,
    val message: String,
    val type: String, // "Upcoming Reminder", "Due Date", "Overdue Alert", "Payment Receipt", "Project Announcement"
    val date: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
