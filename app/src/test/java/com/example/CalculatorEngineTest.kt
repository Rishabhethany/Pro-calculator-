package com.example

import com.example.engine.AngleMode
import com.example.engine.CalculatorEngine
import com.example.engine.DateEngine
import com.example.engine.FinanceEngine
import com.example.engine.UnitCategory
import com.example.engine.UnitConverterEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class CalculatorEngineTest {

    @Test
    fun testBasicArithmeticPrecedence() {
        val result = CalculatorEngine.evaluate("25 + 15 * 2")
        assertFalse(result.isError)
        assertEquals("55", result.resultString)
    }

    @Test
    fun testParenthesesAndDecimals() {
        val result = CalculatorEngine.evaluate("(10.5 + 4.5) * 2")
        assertFalse(result.isError)
        assertEquals("30", result.resultString)
    }

    @Test
    fun testDivisionByZero() {
        val result = CalculatorEngine.evaluate("10 / 0")
        assertTrue(result.isError)
        assertEquals("Cannot divide by zero", result.errorMessage)
    }

    @Test
    fun testScientificTrigonometry() {
        // sin(90) in DEG is 1
        val degResult = CalculatorEngine.evaluate("sin(90)", AngleMode.DEG)
        assertEquals("1", degResult.resultString)

        // cos(0) in RAD is 1
        val radResult = CalculatorEngine.evaluate("cos(0)", AngleMode.RAD)
        assertEquals("1", radResult.resultString)
    }

    @Test
    fun testPowerAndFactorial() {
        val powRes = CalculatorEngine.evaluate("2^5")
        assertEquals("32", powRes.resultString)

        val factRes = CalculatorEngine.evaluate("5!")
        assertEquals("120", factRes.resultString)
    }

    @Test
    fun testGcdAndLcm() {
        assertEquals(6L, CalculatorEngine.gcd(48, 18))
        assertEquals(144L, CalculatorEngine.lcm(48, 18))
        assertTrue(CalculatorEngine.isPrime(97))
        assertFalse(CalculatorEngine.isPrime(100))
    }

    @Test
    fun testFinanceGst() {
        val addGst = FinanceEngine.calculateGst(BigDecimal("1000"), BigDecimal("18"), isAddGst = true)
        assertEquals(BigDecimal("180.00"), addGst.gstAmount)
        assertEquals(BigDecimal("1180.00"), addGst.totalAmount)
        assertEquals(BigDecimal("90.00"), addGst.cgst)
        assertEquals(BigDecimal("90.00"), addGst.sgst)
    }

    @Test
    fun testFinanceEmi() {
        val emi = FinanceEngine.calculateEmi(BigDecimal("100000"), BigDecimal("10"), 12)
        assertTrue(emi.monthlyEmi > BigDecimal.ZERO)
        assertTrue(emi.totalInterest > BigDecimal.ZERO)
        assertTrue(emi.totalPayable > emi.principal)
    }

    @Test
    fun testUnitConverterLength() {
        val m = UnitConverterEngine.lengthUnits.find { it.id == "m" }!!
        val cm = UnitConverterEngine.lengthUnits.find { it.id == "cm" }!!
        val converted = UnitConverterEngine.convert(1.0, m, cm, UnitCategory.LENGTH)
        assertEquals(100.0, converted, 0.001)
    }

    @Test
    fun testDateAgeCalculation() {
        val dob = LocalDate.of(2000, 1, 1)
        val target = LocalDate.of(2025, 1, 1)
        val age = DateEngine.calculateAge(dob, target)
        assertEquals(25, age.years)
        assertEquals(0, age.months)
        assertEquals(0, age.days)
    }
}
