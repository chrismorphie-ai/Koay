package com.example.data

import com.example.data.dao.AppDao
import com.example.data.models.ClientEntity
import com.example.data.models.InstallmentEntity
import com.example.data.models.LedgerEntity
import com.example.data.models.NotificationEntity
import com.example.data.models.PaymentEntity
import com.example.data.models.PropertyEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PropertyRepository(private val dao: AppDao) {

    val allClients: Flow<List<ClientEntity>> = dao.getAllClients()
    val allProperties: Flow<List<PropertyEntity>> = dao.getAllProperties()
    val allInstallments: Flow<List<InstallmentEntity>> = dao.getAllInstallments()
    val allPayments: Flow<List<PaymentEntity>> = dao.getAllPayments()
    val allLedgerEntries: Flow<List<LedgerEntity>> = dao.getAllLedgerEntries()
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()

    fun getPropertiesForClient(clientId: Long): Flow<List<PropertyEntity>> =
        dao.getPropertiesByClientId(clientId)

    fun getInstallmentsForProperty(propertyId: Long): Flow<List<InstallmentEntity>> =
        dao.getInstallmentsByPropertyId(propertyId)

    fun getInstallmentsForClient(clientId: Long): Flow<List<InstallmentEntity>> =
        dao.getInstallmentsByClientId(clientId)

    fun getLedgerForClient(clientId: Long): Flow<List<LedgerEntity>> =
        dao.getLedgerByClientId(clientId)

    fun getLedgerForProperty(propertyId: Long): Flow<List<LedgerEntity>> =
        dao.getLedgerByPropertyId(propertyId)

    fun getPaymentsForProperty(propertyId: Long): Flow<List<PaymentEntity>> =
        dao.getPaymentsByPropertyId(propertyId)

    fun getNotificationsForClient(clientId: Long): Flow<List<NotificationEntity>> =
        dao.getNotificationsForClient(clientId)

    suspend fun addClient(client: ClientEntity): Long = dao.insertClient(client)

    suspend fun updateClient(client: ClientEntity) = dao.updateClient(client)

    suspend fun deleteClient(id: Long) = dao.deleteClient(id)

    suspend fun addProperty(property: PropertyEntity): Long {
        val propId = dao.insertProperty(property)

        // Automatically create initial Ledger Entry for property booking
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        val totalPayable = property.totalPrice + property.developmentCharges + property.otherCharges

        dao.insertLedgerEntry(
            LedgerEntity(
                clientId = property.clientId,
                propertyId = propId,
                date = property.bookingDate.ifEmpty { todayStr },
                description = "Property Booking - ${property.projectName} (${property.plotNumber})",
                debit = totalPayable,
                credit = 0.0,
                runningBalance = totalPayable
            )
        )

        // If booking down payment is paid upfront, record it
        if (property.bookingAmount > 0) {
            val rcpNum = "RCP-${System.currentTimeMillis() % 100000}"
            dao.insertPayment(
                PaymentEntity(
                    propertyId = propId,
                    clientId = property.clientId,
                    receiptNumber = rcpNum,
                    amount = property.bookingAmount,
                    paymentDate = property.bookingDate.ifEmpty { todayStr },
                    paymentMethod = "Bank Transfer",
                    referenceNumber = "INITIAL-BOOKING",
                    notes = "Booking / Down payment recorded on creation"
                )
            )

            dao.insertLedgerEntry(
                LedgerEntity(
                    clientId = property.clientId,
                    propertyId = propId,
                    date = property.bookingDate.ifEmpty { todayStr },
                    description = "Down Payment Received ($rcpNum)",
                    debit = 0.0,
                    credit = property.bookingAmount,
                    runningBalance = totalPayable - property.bookingAmount
                )
            )
        }

        return propId
    }

    suspend fun generateInstallmentSchedule(
        propertyId: Long,
        clientId: Long,
        totalPrice: Double,
        downPayment: Double,
        possessionPayment: Double,
        numberOfInstallments: Int,
        frequencyMonths: Int = 1, // 1 for monthly, 3 for quarterly, 6 for bi-annual
        startDateStr: String = ""
    ) {
        val remainingForInstallments = (totalPrice - downPayment - possessionPayment).coerceAtLeast(0.0)
        val perInstallment = if (numberOfInstallments > 0) remainingForInstallments / numberOfInstallments else 0.0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        if (startDateStr.isNotEmpty()) {
            try {
                dateFormat.parse(startDateStr)?.let { calendar.time = it }
            } catch (_: Exception) {}
        }

        val list = mutableListOf<InstallmentEntity>()

        // Down payment entry (installment 0)
        list.add(
            InstallmentEntity(
                propertyId = propertyId,
                clientId = clientId,
                installmentNumber = 0,
                title = "Down Payment / Booking",
                dueDate = dateFormat.format(calendar.time),
                amountDue = downPayment,
                amountPaid = downPayment, // assuming down payment paid at booking
                status = "Paid",
                lastPaymentDate = dateFormat.format(calendar.time)
            )
        )

        // Installments
        for (i in 1..numberOfInstallments) {
            calendar.add(Calendar.MONTH, frequencyMonths)
            val due = dateFormat.format(calendar.time)
            list.add(
                InstallmentEntity(
                    propertyId = propertyId,
                    clientId = clientId,
                    installmentNumber = i,
                    title = "Installment #$i",
                    dueDate = due,
                    amountDue = perInstallment,
                    amountPaid = 0.0,
                    status = "Upcoming"
                )
            )
        }

        // Possession payment (installment 999)
        if (possessionPayment > 0) {
            calendar.add(Calendar.MONTH, frequencyMonths)
            list.add(
                InstallmentEntity(
                    propertyId = propertyId,
                    clientId = clientId,
                    installmentNumber = 999,
                    title = "Possession / Final Settlement",
                    dueDate = dateFormat.format(calendar.time),
                    amountDue = possessionPayment,
                    amountPaid = 0.0,
                    status = "Upcoming"
                )
            )
        }

        dao.insertInstallments(list)
    }

    suspend fun recordPayment(
        propertyId: Long,
        clientId: Long,
        installmentId: Long?,
        amount: Double,
        paymentDate: String,
        paymentMethod: String,
        referenceNumber: String,
        notes: String,
        manualReceiptNumber: String? = null
    ): String {
        val rcpNum = if (!manualReceiptNumber.isNullOrBlank()) {
            manualReceiptNumber.trim()
        } else {
            "RCP-${SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())}-${(1000..9999).random()}"
        }

        // 1. Insert payment record
        dao.insertPayment(
            PaymentEntity(
                propertyId = propertyId,
                clientId = clientId,
                installmentId = installmentId,
                receiptNumber = rcpNum,
                amount = amount,
                paymentDate = paymentDate,
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber,
                notes = notes
            )
        )

        // 2. Update installment status if linked
        if (installmentId != null) {
            val inst = dao.getInstallmentById(installmentId)
            if (inst != null) {
                val newPaid = inst.amountPaid + amount
                val newStatus = when {
                    newPaid >= inst.amountDue - 1.0 -> "Paid"
                    newPaid > 0 -> "Partially Paid"
                    else -> inst.status
                }
                dao.updateInstallment(
                    inst.copy(
                        amountPaid = newPaid,
                        status = newStatus,
                        lastPaymentDate = paymentDate
                    )
                )
            }
        }

        // 3. Update ledger (Credit)
        val lastEntry = dao.getLastLedgerEntryForProperty(propertyId)
        val currentBalance = lastEntry?.runningBalance ?: 0.0
        val newBalance = (currentBalance - amount).coerceAtLeast(0.0)

        val desc = if (notes.isNotEmpty()) {
            "Payment Received ($paymentMethod - $rcpNum): $notes"
        } else {
            "Payment Received ($paymentMethod - $rcpNum)"
        }

        dao.insertLedgerEntry(
            LedgerEntity(
                clientId = clientId,
                propertyId = propertyId,
                date = paymentDate,
                description = desc,
                debit = 0.0,
                credit = amount,
                runningBalance = newBalance
            )
        )

        // 4. Create Notification
        dao.insertNotification(
            NotificationEntity(
                clientId = clientId,
                title = "Payment Received: $rcpNum",
                message = "Payment of Rs. ${String.format(Locale.US, "%,.0f", amount)} received via $paymentMethod. Ref: $referenceNumber",
                type = "Payment Receipt",
                date = paymentDate,
                isRead = false
            )
        )

        // 5. Update client status if all installments are clear
        val client = dao.getClientByIdOnce(clientId)
        if (client != null) {
            if (newBalance <= 0) {
                dao.updateClient(client.copy(status = "Completed"))
            } else if (client.status == "Overdue") {
                // If it was overdue, check if any overdue installment remains
                // Keep active if payment brought overdue down
                dao.updateClient(client.copy(status = "Active"))
            }
        }

        return rcpNum
    }

    suspend fun addDebitCharge(
        clientId: Long,
        propertyId: Long,
        description: String,
        amount: Double,
        date: String
    ) {
        val lastEntry = dao.getLastLedgerEntryForProperty(propertyId)
        val currentBalance = lastEntry?.runningBalance ?: 0.0
        val newBalance = currentBalance + amount

        dao.insertLedgerEntry(
            LedgerEntity(
                clientId = clientId,
                propertyId = propertyId,
                date = date,
                description = description,
                debit = amount,
                credit = 0.0,
                runningBalance = newBalance
            )
        )
    }

    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)
    suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()
}
