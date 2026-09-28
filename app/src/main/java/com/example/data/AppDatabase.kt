package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppDao
import com.example.data.models.ClientEntity
import com.example.data.models.InstallmentEntity
import com.example.data.models.LedgerEntity
import com.example.data.models.NotificationEntity
import com.example.data.models.PaymentEntity
import com.example.data.models.PropertyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ClientEntity::class,
        PropertyEntity::class,
        InstallmentEntity::class,
        PaymentEntity::class,
        LedgerEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "estatepay_database.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.appDao())
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    if (database.appDao().getClientsOnce().isEmpty()) {
                        populateInitialData(database.appDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: AppDao) {
            // Seed Clients
            val c1Id = dao.insertClient(
                ClientEntity(
                    name = "Tariq Mahmood",
                    cnicOrPassport = "35201-1234567-1",
                    phone = "+92 300 1234567",
                    email = "tariq.mahmood@example.com",
                    address = "House 42, Street 7, Phase 5, DHA",
                    nomineeName = "Ayesha Tariq",
                    nomineeRelation = "Wife",
                    nomineeCnic = "35201-7654321-2",
                    nomineePhone = "+92 300 7654321",
                    status = "Overdue",
                    documentsInfo = "CNIC Scanned, Allotment Agreement Signed, 2 Photos"
                )
            )

            val c2Id = dao.insertClient(
                ClientEntity(
                    name = "Sarah Jenkins",
                    cnicOrPassport = "US-P-98421045",
                    phone = "+1 (415) 555-2671",
                    email = "sarah.jenkins@example.com",
                    address = "450 California St, San Francisco, CA",
                    nomineeName = "Mark Jenkins",
                    nomineeRelation = "Brother",
                    nomineeCnic = "US-P-98421099",
                    nomineePhone = "+1 (415) 555-9988",
                    status = "Active",
                    documentsInfo = "Passport Copy, Overseas Buyer Agreement, Power of Attorney"
                )
            )

            val c3Id = dao.insertClient(
                ClientEntity(
                    name = "Usman Farooq",
                    cnicOrPassport = "42101-9988776-5",
                    phone = "+92 321 8899001",
                    email = "usman.farooq@example.com",
                    address = "Plot 15, Sector 4, Clifton, Karachi",
                    nomineeName = "Bilal Farooq",
                    nomineeRelation = "Son",
                    nomineeCnic = "42101-1122334-3",
                    nomineePhone = "+92 321 1122334",
                    status = "Active",
                    documentsInfo = "CNIC Verified, Commercial Booking Deed"
                )
            )

            // Seed Properties
            val p1Id = dao.insertProperty(
                PropertyEntity(
                    clientId = c1Id,
                    projectName = "Emerald Heights Residency",
                    blockName = "Executive Block B",
                    plotNumber = "Plot #142",
                    size = "10 Marla (250 Sq Yds)",
                    type = "Residential Plot",
                    totalPrice = 7500000.0,
                    bookingAmount = 1500000.0,
                    developmentCharges = 500000.0,
                    otherCharges = 150000.0,
                    possessionAmount = 1000000.0,
                    status = "Installments Active",
                    bookingDate = "2026-01-15"
                )
            )

            val p2Id = dao.insertProperty(
                PropertyEntity(
                    clientId = c2Id,
                    projectName = "Skyline Valley Towers",
                    blockName = "Tower A",
                    plotNumber = "Apartment #402",
                    size = "1,450 Sq Ft",
                    type = "Luxury Apartment",
                    totalPrice = 12000000.0,
                    bookingAmount = 2500000.0,
                    developmentCharges = 600000.0,
                    otherCharges = 200000.0,
                    possessionAmount = 1500000.0,
                    status = "Installments Active",
                    bookingDate = "2026-02-01"
                )
            )

            val p3Id = dao.insertProperty(
                PropertyEntity(
                    clientId = c3Id,
                    projectName = "Palm Commercial Arena",
                    blockName = "Plaza 3",
                    plotNumber = "Shop #12",
                    size = "650 Sq Ft",
                    type = "Commercial Shop",
                    totalPrice = 9000000.0,
                    bookingAmount = 2000000.0,
                    developmentCharges = 400000.0,
                    otherCharges = 100000.0,
                    possessionAmount = 1000000.0,
                    status = "Installments Active",
                    bookingDate = "2026-03-01"
                )
            )

            // Seed Installments for Property 1
            val p1Insts = listOf(
                InstallmentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    installmentNumber = 0,
                    title = "Down Payment / Booking",
                    dueDate = "2026-01-15",
                    amountDue = 1500000.0,
                    amountPaid = 1500000.0,
                    status = "Paid",
                    lastPaymentDate = "2026-01-15"
                ),
                InstallmentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    installmentNumber = 1,
                    title = "Monthly Installment #1",
                    dueDate = "2026-02-15",
                    amountDue = 300000.0,
                    amountPaid = 300000.0,
                    status = "Paid",
                    lastPaymentDate = "2026-02-14"
                ),
                InstallmentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    installmentNumber = 2,
                    title = "Monthly Installment #2",
                    dueDate = "2026-03-15",
                    amountDue = 300000.0,
                    amountPaid = 300000.0,
                    status = "Paid",
                    lastPaymentDate = "2026-03-15"
                ),
                InstallmentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    installmentNumber = 3,
                    title = "Monthly Installment #3",
                    dueDate = "2026-04-15",
                    amountDue = 300000.0,
                    amountPaid = 300000.0,
                    status = "Paid",
                    lastPaymentDate = "2026-04-12"
                ),
                InstallmentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    installmentNumber = 4,
                    title = "Monthly Installment #4",
                    dueDate = "2026-05-15",
                    amountDue = 300000.0,
                    amountPaid = 150000.0,
                    status = "Partially Paid",
                    lastPaymentDate = "2026-05-20"
                ),
                InstallmentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    installmentNumber = 5,
                    title = "Monthly Installment #5",
                    dueDate = "2026-06-15",
                    amountDue = 300000.0,
                    amountPaid = 0.0,
                    status = "Overdue",
                    lastPaymentDate = null
                ),
                InstallmentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    installmentNumber = 6,
                    title = "Monthly Installment #6",
                    dueDate = "2026-10-15",
                    amountDue = 300000.0,
                    amountPaid = 0.0,
                    status = "Upcoming",
                    lastPaymentDate = null
                ),
                InstallmentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    installmentNumber = 7,
                    title = "Monthly Installment #7",
                    dueDate = "2026-11-15",
                    amountDue = 300000.0,
                    amountPaid = 0.0,
                    status = "Upcoming",
                    lastPaymentDate = null
                ),
                InstallmentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    installmentNumber = 999,
                    title = "Possession / Final Settlement",
                    dueDate = "2027-12-15",
                    amountDue = 1000000.0,
                    amountPaid = 0.0,
                    status = "Upcoming",
                    lastPaymentDate = null
                )
            )
            dao.insertInstallments(p1Insts)

            // Seed Payments for Property 1
            dao.insertPayment(
                PaymentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    receiptNumber = "RCP-2026-0001",
                    amount = 1500000.0,
                    paymentDate = "2026-01-15",
                    paymentMethod = "Bank Transfer",
                    referenceNumber = "FT-HBL-992140",
                    notes = "Down payment for Plot 142 Block B"
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    receiptNumber = "RCP-2026-0042",
                    amount = 300000.0,
                    paymentDate = "2026-02-14",
                    paymentMethod = "Online / Card",
                    referenceNumber = "TXN-CC-847291",
                    notes = "Installment #1 clearing"
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    receiptNumber = "RCP-2026-0089",
                    amount = 300000.0,
                    paymentDate = "2026-03-15",
                    paymentMethod = "Cheque",
                    referenceNumber = "CHQ-MCB-774102",
                    notes = "Installment #2 clearing"
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    receiptNumber = "RCP-2026-0114",
                    amount = 300000.0,
                    paymentDate = "2026-04-12",
                    paymentMethod = "Bank Transfer",
                    referenceNumber = "FT-MZN-334189",
                    notes = "Installment #3"
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    propertyId = p1Id,
                    clientId = c1Id,
                    receiptNumber = "RCP-2026-0150",
                    amount = 150000.0,
                    paymentDate = "2026-05-20",
                    paymentMethod = "Cash",
                    referenceNumber = "CSH-OFFICE-012",
                    notes = "Partial payment for Installment #4"
                )
            )

            // Seed Ledger for Property 1 (The central feature!)
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c1Id,
                    propertyId = p1Id,
                    date = "2026-01-15",
                    description = "Property Booking - Plot #142",
                    debit = 7500000.0,
                    credit = 0.0,
                    runningBalance = 7500000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c1Id,
                    propertyId = p1Id,
                    date = "2026-01-15",
                    description = "Down Payment Received (RCP-2026-0001)",
                    debit = 0.0,
                    credit = 1500000.0,
                    runningBalance = 6000000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c1Id,
                    propertyId = p1Id,
                    date = "2026-02-01",
                    description = "Development Charges Billed",
                    debit = 500000.0,
                    credit = 0.0,
                    runningBalance = 6500000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c1Id,
                    propertyId = p1Id,
                    date = "2026-02-14",
                    description = "Installment #1 Payment (RCP-2026-0042)",
                    debit = 0.0,
                    credit = 300000.0,
                    runningBalance = 6200000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c1Id,
                    propertyId = p1Id,
                    date = "2026-03-15",
                    description = "Installment #2 Payment (RCP-2026-0089)",
                    debit = 0.0,
                    credit = 300000.0,
                    runningBalance = 5900000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c1Id,
                    propertyId = p1Id,
                    date = "2026-04-12",
                    description = "Installment #3 Payment (RCP-2026-0114)",
                    debit = 0.0,
                    credit = 300000.0,
                    runningBalance = 5600000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c1Id,
                    propertyId = p1Id,
                    date = "2026-05-20",
                    description = "Installment #4 Partial Payment (RCP-2026-0150)",
                    debit = 0.0,
                    credit = 150000.0,
                    runningBalance = 5450000.0
                )
            )

            // Seed Property 2 (Sarah Jenkins)
            dao.insertInstallments(
                listOf(
                    InstallmentEntity(
                        propertyId = p2Id,
                        clientId = c2Id,
                        installmentNumber = 0,
                        title = "Down Payment",
                        dueDate = "2026-02-01",
                        amountDue = 2500000.0,
                        amountPaid = 2500000.0,
                        status = "Paid",
                        lastPaymentDate = "2026-02-01"
                    ),
                    InstallmentEntity(
                        propertyId = p2Id,
                        clientId = c2Id,
                        installmentNumber = 1,
                        title = "Quarterly Installment #1",
                        dueDate = "2026-05-01",
                        amountDue = 800000.0,
                        amountPaid = 800000.0,
                        status = "Paid",
                        lastPaymentDate = "2026-04-28"
                    ),
                    InstallmentEntity(
                        propertyId = p2Id,
                        clientId = c2Id,
                        installmentNumber = 2,
                        title = "Quarterly Installment #2",
                        dueDate = "2026-08-01",
                        amountDue = 800000.0,
                        amountPaid = 800000.0,
                        status = "Paid",
                        lastPaymentDate = "2026-07-30"
                    ),
                    InstallmentEntity(
                        propertyId = p2Id,
                        clientId = c2Id,
                        installmentNumber = 3,
                        title = "Quarterly Installment #3",
                        dueDate = "2026-11-01",
                        amountDue = 800000.0,
                        amountPaid = 0.0,
                        status = "Upcoming"
                    )
                )
            )

            dao.insertPayment(
                PaymentEntity(
                    propertyId = p2Id,
                    clientId = c2Id,
                    receiptNumber = "RCP-2026-0022",
                    amount = 2500000.0,
                    paymentDate = "2026-02-01",
                    paymentMethod = "Bank Transfer",
                    referenceNumber = "SWIFT-CHASE-0021",
                    notes = "Down payment for Apt 402"
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    propertyId = p2Id,
                    clientId = c2Id,
                    receiptNumber = "RCP-2026-0130",
                    amount = 800000.0,
                    paymentDate = "2026-04-28",
                    paymentMethod = "Online / Card",
                    referenceNumber = "TXN-STRIPE-44210",
                    notes = "Quarterly installment #1"
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    propertyId = p2Id,
                    clientId = c2Id,
                    receiptNumber = "RCP-2026-0210",
                    amount = 800000.0,
                    paymentDate = "2026-07-30",
                    paymentMethod = "Online / Card",
                    referenceNumber = "TXN-STRIPE-78912",
                    notes = "Quarterly installment #2"
                )
            )

            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c2Id,
                    propertyId = p2Id,
                    date = "2026-02-01",
                    description = "Apartment Booking - Apt #402 Tower A",
                    debit = 12000000.0,
                    credit = 0.0,
                    runningBalance = 12000000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c2Id,
                    propertyId = p2Id,
                    date = "2026-02-01",
                    description = "Down Payment Received (RCP-2026-0022)",
                    debit = 0.0,
                    credit = 2500000.0,
                    runningBalance = 9500000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c2Id,
                    propertyId = p2Id,
                    date = "2026-04-28",
                    description = "Quarterly Installment #1 (RCP-2026-0130)",
                    debit = 0.0,
                    credit = 800000.0,
                    runningBalance = 8700000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c2Id,
                    propertyId = p2Id,
                    date = "2026-07-30",
                    description = "Quarterly Installment #2 (RCP-2026-0210)",
                    debit = 0.0,
                    credit = 800000.0,
                    runningBalance = 7900000.0
                )
            )

            // Seed Property 3 (Usman Farooq)
            dao.insertInstallments(
                listOf(
                    InstallmentEntity(
                        propertyId = p3Id,
                        clientId = c3Id,
                        installmentNumber = 0,
                        title = "Booking / Down Payment",
                        dueDate = "2026-03-01",
                        amountDue = 2000000.0,
                        amountPaid = 2000000.0,
                        status = "Paid",
                        lastPaymentDate = "2026-03-01"
                    ),
                    InstallmentEntity(
                        propertyId = p3Id,
                        clientId = c3Id,
                        installmentNumber = 1,
                        title = "Bi-Monthly Installment #1",
                        dueDate = "2026-05-01",
                        amountDue = 700000.0,
                        amountPaid = 700000.0,
                        status = "Paid",
                        lastPaymentDate = "2026-04-29"
                    ),
                    InstallmentEntity(
                        propertyId = p3Id,
                        clientId = c3Id,
                        installmentNumber = 2,
                        title = "Bi-Monthly Installment #2",
                        dueDate = "2026-07-01",
                        amountDue = 700000.0,
                        amountPaid = 700000.0,
                        status = "Paid",
                        lastPaymentDate = "2026-06-30"
                    ),
                    InstallmentEntity(
                        propertyId = p3Id,
                        clientId = c3Id,
                        installmentNumber = 3,
                        title = "Bi-Monthly Installment #3",
                        dueDate = "2026-10-01",
                        amountDue = 700000.0,
                        amountPaid = 0.0,
                        status = "Upcoming"
                    )
                )
            )

            dao.insertPayment(
                PaymentEntity(
                    propertyId = p3Id,
                    clientId = c3Id,
                    receiptNumber = "RCP-2026-0060",
                    amount = 2000000.0,
                    paymentDate = "2026-03-01",
                    paymentMethod = "Cheque",
                    referenceNumber = "CHQ-UBL-90112",
                    notes = "Down payment for Shop 12"
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    propertyId = p3Id,
                    clientId = c3Id,
                    receiptNumber = "RCP-2026-0145",
                    amount = 700000.0,
                    paymentDate = "2026-04-29",
                    paymentMethod = "Bank Transfer",
                    referenceNumber = "FT-UBL-331002",
                    notes = "Bi-monthly installment #1"
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    propertyId = p3Id,
                    clientId = c3Id,
                    receiptNumber = "RCP-2026-0195",
                    amount = 700000.0,
                    paymentDate = "2026-06-30",
                    paymentMethod = "Cash",
                    referenceNumber = "CSH-OFFICE-088",
                    notes = "Bi-monthly installment #2"
                )
            )

            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c3Id,
                    propertyId = p3Id,
                    date = "2026-03-01",
                    description = "Commercial Booking - Shop #12 Plaza 3",
                    debit = 9000000.0,
                    credit = 0.0,
                    runningBalance = 9000000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c3Id,
                    propertyId = p3Id,
                    date = "2026-03-01",
                    description = "Down Payment Received (RCP-2026-0060)",
                    debit = 0.0,
                    credit = 2000000.0,
                    runningBalance = 7000000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c3Id,
                    propertyId = p3Id,
                    date = "2026-04-29",
                    description = "Bi-Monthly Installment #1 (RCP-2026-0145)",
                    debit = 0.0,
                    credit = 700000.0,
                    runningBalance = 6300000.0
                )
            )
            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = c3Id,
                    propertyId = p3Id,
                    date = "2026-06-30",
                    description = "Bi-Monthly Installment #2 (RCP-2026-0195)",
                    debit = 0.0,
                    credit = 700000.0,
                    runningBalance = 5600000.0
                )
            )

            // Seed Notifications
            dao.insertNotification(
                NotificationEntity(
                    clientId = c1Id,
                    title = "Overdue Alert: Installment #5",
                    message = "Installment #5 of Rs. 300,000 for Plot #142 was due on June 15, 2026. Please clear to avoid late surcharge.",
                    type = "Overdue Alert",
                    date = "2026-06-18",
                    isRead = false
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    clientId = c1Id,
                    title = "Upcoming Reminder: Installment #6",
                    message = "Installment #6 of Rs. 300,000 is due on October 15, 2026.",
                    type = "Upcoming Reminder",
                    date = "2026-09-25",
                    isRead = false
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    clientId = c2Id,
                    title = "Payment Confirmation: RCP-2026-0210",
                    message = "Payment of Rs. 800,000 for Apt #402 received via Online Card. Digital voucher generated.",
                    type = "Payment Receipt",
                    date = "2026-07-30",
                    isRead = true
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    clientId = null,
                    title = "Project Announcement: Fast-Track Road Asphalt",
                    message = "Development Update: Main Boulevard 150ft carpet road in Block B & Plaza area is now complete. Possession handover on schedule.",
                    type = "Project Announcement",
                    date = "2026-09-20",
                    isRead = false
                )
            )
        }
    }
}
