package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.ClientEntity
import com.example.data.models.InstallmentEntity
import com.example.data.models.LedgerEntity
import com.example.data.models.NotificationEntity
import com.example.data.models.PaymentEntity
import com.example.data.models.PropertyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Clients
    @Query("SELECT * FROM clients ORDER BY createdAt DESC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients")
    suspend fun getClientsOnce(): List<ClientEntity>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    fun getClientById(id: Long): Flow<ClientEntity?>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientByIdOnce(id: Long): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteClient(id: Long)

    // Properties
    @Query("SELECT * FROM properties ORDER BY createdAt DESC")
    fun getAllProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getPropertiesByClientId(clientId: Long): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE id = :id LIMIT 1")
    fun getPropertyById(id: Long): Flow<PropertyEntity?>

    @Query("SELECT * FROM properties WHERE id = :id LIMIT 1")
    suspend fun getPropertyByIdOnce(id: Long): PropertyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity): Long

    @Update
    suspend fun updateProperty(property: PropertyEntity)

    @Query("DELETE FROM properties WHERE id = :id")
    suspend fun deleteProperty(id: Long)

    // Installments
    @Query("SELECT * FROM installments ORDER BY installmentNumber ASC, dueDate ASC")
    fun getAllInstallments(): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments WHERE propertyId = :propertyId ORDER BY installmentNumber ASC, dueDate ASC")
    fun getInstallmentsByPropertyId(propertyId: Long): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments WHERE clientId = :clientId ORDER BY dueDate ASC")
    fun getInstallmentsByClientId(clientId: Long): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments WHERE id = :id LIMIT 1")
    suspend fun getInstallmentById(id: Long): InstallmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallments(installments: List<InstallmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallment(installment: InstallmentEntity): Long

    @Update
    suspend fun updateInstallment(installment: InstallmentEntity)

    // Payments
    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE propertyId = :propertyId ORDER BY timestamp DESC")
    fun getPaymentsByPropertyId(propertyId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getPaymentsByClientId(clientId: Long): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    // Ledger
    @Query("SELECT * FROM ledger_entries ORDER BY timestamp ASC")
    fun getAllLedgerEntries(): Flow<List<LedgerEntity>>

    @Query("SELECT * FROM ledger_entries WHERE clientId = :clientId ORDER BY timestamp ASC")
    fun getLedgerByClientId(clientId: Long): Flow<List<LedgerEntity>>

    @Query("SELECT * FROM ledger_entries WHERE propertyId = :propertyId ORDER BY timestamp ASC")
    fun getLedgerByPropertyId(propertyId: Long): Flow<List<LedgerEntity>>

    @Query("SELECT * FROM ledger_entries WHERE propertyId = :propertyId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLedgerEntryForProperty(propertyId: Long): LedgerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: LedgerEntity): Long

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE clientId IS NULL OR clientId = :clientId ORDER BY timestamp DESC")
    fun getNotificationsForClient(clientId: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()
}
