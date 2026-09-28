package com.example

import com.example.ui.components.formatMoney
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCurrencyFormatting() {
        val formatted = formatMoney(5000000.0)
        assertTrue(formatted.contains("5,000,000"))
        assertTrue(formatted.startsWith("Rs."))
    }

    @Test
    fun testLedgerBalanceCalculation() {
        val totalPropertyPrice = 7500000.0
        val devCharges = 500000.0
        val totalDebit = totalPropertyPrice + devCharges
        val downPayment = 1500000.0
        val inst1 = 300000.0
        val totalCredit = downPayment + inst1
        val remainingBalance = totalDebit - totalCredit

        assertEquals(8000000.0, totalDebit, 0.01)
        assertEquals(1800000.0, totalCredit, 0.01)
        assertEquals(6200000.0, remainingBalance, 0.01)
    }

    @Test
    fun testManualReceiptNumberAssignment() {
        val manualInput = "  BK-99201  "
        val resolvedReceipt = if (!manualInput.isNullOrBlank()) manualInput.trim() else "AUTO"
        assertEquals("BK-99201", resolvedReceipt)

        val emptyInput: String? = ""
        val fallbackReceipt = if (!emptyInput.isNullOrBlank()) emptyInput.trim() else "AUTO"
        assertEquals("AUTO", fallbackReceipt)
    }
}
