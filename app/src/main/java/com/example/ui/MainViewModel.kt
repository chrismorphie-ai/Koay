package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PropertyRepository
import com.example.data.models.ClientEntity
import com.example.data.models.InstallmentEntity
import com.example.data.models.LedgerEntity
import com.example.data.models.NotificationEntity
import com.example.data.models.PaymentEntity
import com.example.data.models.PropertyEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AdminKpiSummary(
    val totalClients: Int = 0,
    val totalPropertiesSold: Int = 0,
    val totalExpectedReceivables: Double = 0.0,
    val totalCollected: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val overdueAmount: Double = 0.0,
    val overdueClientsCount: Int = 0,
    val paymentsToday: Double = 0.0,
    val paymentsThisMonth: Double = 0.0,
    val upcomingThisMonth: Double = 0.0
)

data class ClientFinancialSummary(
    val client: ClientEntity? = null,
    val properties: List<PropertyEntity> = emptyList(),
    val totalPropertyPrice: Double = 0.0,
    val totalPaid: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val overdueAmount: Double = 0.0,
    val nextInstallment: InstallmentEntity? = null,
    val paymentProgress: Float = 0f
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PropertyRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = PropertyRepository(database.appDao())
    }

    val clients: StateFlow<List<ClientEntity>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val properties: StateFlow<List<PropertyEntity>> = repository.allProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val installments: StateFlow<List<InstallmentEntity>> = repository.allInstallments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ledgerEntries: StateFlow<List<LedgerEntity>> = repository.allLedgerEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAdminMode = MutableStateFlow(true)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _selectedClientId = MutableStateFlow<Long?>(null)
    val selectedClientId: StateFlow<Long?> = _selectedClientId.asStateFlow()

    private val _selectedPropertyId = MutableStateFlow<Long?>(null)
    val selectedPropertyId: StateFlow<Long?> = _selectedPropertyId.asStateFlow()

    private val _selectedAdminTab = MutableStateFlow(0) // 0: Dashboard, 1: Ledger, 2: Clients/Plots, 3: Payments/Receipts, 4: Reports/Notices
    val selectedAdminTab: StateFlow<Int> = _selectedAdminTab.asStateFlow()

    private val _selectedClientTab = MutableStateFlow(0) // 0: Dashboard, 1: Schedule, 2: Ledger, 3: Receipts/Docs, 4: Notifications
    val selectedClientTab: StateFlow<Int> = _selectedClientTab.asStateFlow()

    // Search and filters
    val searchQuery = MutableStateFlow("")
    val clientFilterStatus = MutableStateFlow("All")
    val selectedLedgerClientId = MutableStateFlow<Long?>(null)

    // Admin KPIs
    val adminKpiSummary: StateFlow<AdminKpiSummary> = combine(
        clients, properties, installments, payments
    ) { clientList, propertyList, instList, payList ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        val currentMonthStr = monthFormat.format(Date())

        val totalExpected = propertyList.sumOf { it.totalPrice + it.developmentCharges + it.otherCharges }
        val totalCollected = payList.sumOf { it.amount }
        val outstanding = (totalExpected - totalCollected).coerceAtLeast(0.0)

        val overdueInsts = instList.filter { it.status == "Overdue" }
        val overdueAmount = overdueInsts.sumOf { (it.amountDue - it.amountPaid).coerceAtLeast(0.0) }
        val overdueClientIds = overdueInsts.map { it.clientId }.toSet()

        val paymentsToday = payList.filter { it.paymentDate == todayStr }.sumOf { it.amount }
        val paymentsThisMonth = payList.filter { it.paymentDate.startsWith(currentMonthStr) }.sumOf { it.amount }

        val upcomingThisMonth = instList.filter {
            it.status == "Upcoming" && it.dueDate.startsWith(currentMonthStr)
        }.sumOf { it.amountDue }

        AdminKpiSummary(
            totalClients = clientList.size,
            totalPropertiesSold = propertyList.size,
            totalExpectedReceivables = totalExpected,
            totalCollected = totalCollected,
            outstandingBalance = outstanding,
            overdueAmount = overdueAmount,
            overdueClientsCount = overdueClientIds.size,
            paymentsToday = paymentsToday,
            paymentsThisMonth = paymentsThisMonth,
            upcomingThisMonth = upcomingThisMonth
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminKpiSummary())

    // Active Client Summary for Client Portal Mode
    val clientFinancialSummary: StateFlow<ClientFinancialSummary> = combine(
        selectedClientId, clients, properties, installments, payments
    ) { selectedId, clientList, propertyList, instList, payList ->
        val effectiveClientId = selectedId ?: clientList.firstOrNull()?.id
        val client = clientList.find { it.id == effectiveClientId }
        val clientProperties = propertyList.filter { it.clientId == effectiveClientId }
        val clientInsts = instList.filter { it.clientId == effectiveClientId }
        val clientPays = payList.filter { it.clientId == effectiveClientId }

        val totalPrice = clientProperties.sumOf { it.totalPrice + it.developmentCharges + it.otherCharges }
        val totalPaid = clientPays.sumOf { it.amount }
        val remaining = (totalPrice - totalPaid).coerceAtLeast(0.0)

        val overdueInsts = clientInsts.filter { it.status == "Overdue" }
        val overdueAmount = overdueInsts.sumOf { (it.amountDue - it.amountPaid).coerceAtLeast(0.0) }

        val nextInst = clientInsts
            .filter { it.status == "Upcoming" || it.status == "Partially Paid" || it.status == "Overdue" }
            .minByOrNull { it.dueDate }

        val progress = if (totalPrice > 0) (totalPaid / totalPrice).toFloat().coerceIn(0f, 1f) else 0f

        ClientFinancialSummary(
            client = client,
            properties = clientProperties,
            totalPropertyPrice = totalPrice,
            totalPaid = totalPaid,
            remainingBalance = remaining,
            overdueAmount = overdueAmount,
            nextInstallment = nextInst,
            paymentProgress = progress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClientFinancialSummary())

    fun toggleAppMode() {
        _isAdminMode.value = !_isAdminMode.value
    }

    fun setAdminMode(isAdmin: Boolean) {
        _isAdminMode.value = isAdmin
    }

    fun setSelectedAdminTab(tab: Int) {
        _selectedAdminTab.value = tab
    }

    fun setSelectedClientTab(tab: Int) {
        _selectedClientTab.value = tab
    }

    fun selectClient(clientId: Long?) {
        _selectedClientId.value = clientId
    }

    fun selectProperty(propertyId: Long?) {
        _selectedPropertyId.value = propertyId
    }

    fun addClient(
        name: String,
        cnic: String,
        phone: String,
        email: String,
        address: String,
        nomineeName: String,
        nomineeRelation: String,
        nomineeCnic: String,
        nomineePhone: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addClient(
                ClientEntity(
                    name = name,
                    cnicOrPassport = cnic,
                    phone = phone,
                    email = email,
                    address = address,
                    nomineeName = nomineeName,
                    nomineeRelation = nomineeRelation,
                    nomineeCnic = nomineeCnic,
                    nomineePhone = nomineePhone,
                    status = "Active",
                    documentsInfo = notes.ifEmpty { "Agreement Pending Verification" }
                )
            )
        }
    }

    fun addProperty(
        clientId: Long,
        projectName: String,
        blockName: String,
        plotNumber: String,
        size: String,
        type: String,
        totalPrice: Double,
        bookingAmount: Double,
        developmentCharges: Double,
        otherCharges: Double,
        possessionAmount: Double,
        bookingDate: String
    ) {
        viewModelScope.launch {
            val propId = repository.addProperty(
                PropertyEntity(
                    clientId = clientId,
                    projectName = projectName,
                    blockName = blockName,
                    plotNumber = plotNumber,
                    size = size,
                    type = type,
                    totalPrice = totalPrice,
                    bookingAmount = bookingAmount,
                    developmentCharges = developmentCharges,
                    otherCharges = otherCharges,
                    possessionAmount = possessionAmount,
                    status = "Installments Active",
                    bookingDate = bookingDate
                )
            )

            // Auto-generate a default 12-month installment schedule
            repository.generateInstallmentSchedule(
                propertyId = propId,
                clientId = clientId,
                totalPrice = totalPrice + developmentCharges + otherCharges,
                downPayment = bookingAmount,
                possessionPayment = possessionAmount,
                numberOfInstallments = 12,
                frequencyMonths = 1,
                startDateStr = bookingDate
            )
        }
    }

    fun recordPayment(
        propertyId: Long,
        clientId: Long,
        installmentId: Long?,
        amount: Double,
        paymentDate: String,
        paymentMethod: String,
        referenceNumber: String,
        notes: String,
        manualReceiptNumber: String? = null,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val rcp = repository.recordPayment(
                propertyId = propertyId,
                clientId = clientId,
                installmentId = installmentId,
                amount = amount,
                paymentDate = paymentDate,
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber,
                notes = notes,
                manualReceiptNumber = manualReceiptNumber
            )
            onSuccess(rcp)
        }
    }

    fun addDebitCharge(
        clientId: Long,
        propertyId: Long,
        description: String,
        amount: Double,
        date: String
    ) {
        viewModelScope.launch {
            repository.addDebitCharge(clientId, propertyId, description, amount, date)
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }
}
