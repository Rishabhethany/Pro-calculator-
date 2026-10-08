package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode

object FinanceEngine {

    private val MC_FINANCE = java.math.MathContext(16, RoundingMode.HALF_UP)

    data class GstResult(
        val netAmount: BigDecimal,
        val gstRate: BigDecimal,
        val gstAmount: BigDecimal,
        val cgst: BigDecimal,
        val sgst: BigDecimal,
        val totalAmount: BigDecimal
    )

    fun calculateGst(amount: BigDecimal, rate: BigDecimal, isAddGst: Boolean): GstResult {
        val hundred = BigDecimal(100)
        val two = BigDecimal(2)

        return if (isAddGst) {
            // Adding GST: total = amount + amount * (rate / 100)
            val gstAmount = amount.multiply(rate).divide(hundred, 2, RoundingMode.HALF_UP)
            val cgst = gstAmount.divide(two, 2, RoundingMode.HALF_UP)
            val sgst = gstAmount.subtract(cgst)
            val total = amount.add(gstAmount)
            GstResult(
                netAmount = amount.setScale(2, RoundingMode.HALF_UP),
                gstRate = rate,
                gstAmount = gstAmount,
                cgst = cgst,
                sgst = sgst,
                totalAmount = total.setScale(2, RoundingMode.HALF_UP)
            )
        } else {
            // Removing GST (Amount is already Inclusive of GST)
            // netAmount = amount / (1 + rate/100)
            val divisor = BigDecimal.ONE.add(rate.divide(hundred, 6, RoundingMode.HALF_UP))
            val net = amount.divide(divisor, 2, RoundingMode.HALF_UP)
            val gstAmount = amount.subtract(net)
            val cgst = gstAmount.divide(two, 2, RoundingMode.HALF_UP)
            val sgst = gstAmount.subtract(cgst)
            GstResult(
                netAmount = net,
                gstRate = rate,
                gstAmount = gstAmount,
                cgst = cgst,
                sgst = sgst,
                totalAmount = amount.setScale(2, RoundingMode.HALF_UP)
            )
        }
    }

    data class EmiResult(
        val monthlyEmi: BigDecimal,
        val principal: BigDecimal,
        val totalInterest: BigDecimal,
        val totalPayable: BigDecimal
    )

    fun calculateEmi(principal: BigDecimal, annualRate: BigDecimal, tenureMonths: Int): EmiResult {
        if (principal <= BigDecimal.ZERO || tenureMonths <= 0) {
            return EmiResult(BigDecimal.ZERO, principal, BigDecimal.ZERO, principal)
        }
        if (annualRate <= BigDecimal.ZERO) {
            val emi = principal.divide(BigDecimal(tenureMonths), 2, RoundingMode.HALF_UP)
            return EmiResult(emi, principal, BigDecimal.ZERO, principal)
        }

        val monthlyRateDouble = annualRate.toDouble() / (12.0 * 100.0)
        val p = principal.toDouble()
        val n = tenureMonths.toDouble()

        // EMI = [P * r * (1 + r)^n] / [(1 + r)^n - 1]
        val factor = Math.pow(1.0 + monthlyRateDouble, n)
        val emiDouble = (p * monthlyRateDouble * factor) / (factor - 1.0)

        val emi = BigDecimal(emiDouble).setScale(2, RoundingMode.HALF_UP)
        val totalPayable = emi.multiply(BigDecimal(tenureMonths)).setScale(2, RoundingMode.HALF_UP)
        val totalInterest = totalPayable.subtract(principal).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

        return EmiResult(
            monthlyEmi = emi,
            principal = principal.setScale(2, RoundingMode.HALF_UP),
            totalInterest = totalInterest,
            totalPayable = totalPayable
        )
    }

    data class SimpleInterestResult(
        val principal: BigDecimal,
        val rate: BigDecimal,
        val timeYears: BigDecimal,
        val interest: BigDecimal,
        val finalAmount: BigDecimal
    )

    fun calculateSimpleInterest(principal: BigDecimal, annualRate: BigDecimal, timeYears: BigDecimal): SimpleInterestResult {
        val hundred = BigDecimal(100)
        val interest = principal.multiply(annualRate).multiply(timeYears).divide(hundred, 2, RoundingMode.HALF_UP)
        val finalAmount = principal.add(interest).setScale(2, RoundingMode.HALF_UP)
        return SimpleInterestResult(
            principal = principal.setScale(2, RoundingMode.HALF_UP),
            rate = annualRate,
            timeYears = timeYears,
            interest = interest,
            finalAmount = finalAmount
        )
    }

    enum class CompoundingFrequency(val n: Int, val label: String) {
        YEARLY(1, "Yearly"),
        HALF_YEARLY(2, "Half-Yearly"),
        QUARTERLY(4, "Quarterly"),
        MONTHLY(12, "Monthly")
    }

    data class CompoundInterestResult(
        val principal: BigDecimal,
        val interest: BigDecimal,
        val finalAmount: BigDecimal
    )

    fun calculateCompoundInterest(
        principal: BigDecimal,
        annualRate: BigDecimal,
        timeYears: BigDecimal,
        frequency: CompoundingFrequency
    ): CompoundInterestResult {
        val p = principal.toDouble()
        val r = annualRate.toDouble() / 100.0
        val t = timeYears.toDouble()
        val n = frequency.n.toDouble()

        // A = P * (1 + r/n)^(n*t)
        val amountDouble = p * Math.pow(1.0 + (r / n), n * t)
        val finalAmount = BigDecimal(amountDouble).setScale(2, RoundingMode.HALF_UP)
        val interest = finalAmount.subtract(principal).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

        return CompoundInterestResult(
            principal = principal.setScale(2, RoundingMode.HALF_UP),
            interest = interest,
            finalAmount = finalAmount
        )
    }

    data class TipResult(
        val billAmount: BigDecimal,
        val tipPercent: BigDecimal,
        val tipAmount: BigDecimal,
        val totalBill: BigDecimal,
        val numPeople: Int,
        val perPersonAmount: BigDecimal,
        val perPersonTip: BigDecimal
    )

    fun calculateTip(bill: BigDecimal, tipPercent: BigDecimal, people: Int): TipResult {
        val safePeople = if (people <= 0) 1 else people
        val hundred = BigDecimal(100)
        val tipAmount = bill.multiply(tipPercent).divide(hundred, 2, RoundingMode.HALF_UP)
        val total = bill.add(tipAmount).setScale(2, RoundingMode.HALF_UP)
        val perPerson = total.divide(BigDecimal(safePeople), 2, RoundingMode.HALF_UP)
        val perPersonTip = tipAmount.divide(BigDecimal(safePeople), 2, RoundingMode.HALF_UP)

        return TipResult(
            billAmount = bill.setScale(2, RoundingMode.HALF_UP),
            tipPercent = tipPercent,
            tipAmount = tipAmount,
            totalBill = total,
            numPeople = safePeople,
            perPersonAmount = perPerson,
            perPersonTip = perPersonTip
        )
    }

    data class DiscountResult(
        val originalPrice: BigDecimal,
        val discountPercent: BigDecimal,
        val discountAmount: BigDecimal,
        val priceAfterDiscount: BigDecimal,
        val additionalDiscountPercent: BigDecimal,
        val additionalDiscountAmount: BigDecimal,
        val gstPercent: BigDecimal,
        val gstAmount: BigDecimal,
        val finalPayableAmount: BigDecimal,
        val totalSaved: BigDecimal
    )

    fun calculateDiscount(
        originalPrice: BigDecimal,
        discountPercent: BigDecimal,
        additionalDiscountPercent: BigDecimal = BigDecimal.ZERO,
        gstPercent: BigDecimal = BigDecimal.ZERO
    ): DiscountResult {
        val hundred = BigDecimal(100)
        val d1Amount = originalPrice.multiply(discountPercent).divide(hundred, 2, RoundingMode.HALF_UP)
        val afterD1 = originalPrice.subtract(d1Amount).max(BigDecimal.ZERO)

        val d2Amount = afterD1.multiply(additionalDiscountPercent).divide(hundred, 2, RoundingMode.HALF_UP)
        val afterD2 = afterD1.subtract(d2Amount).max(BigDecimal.ZERO)

        val gstAmount = afterD2.multiply(gstPercent).divide(hundred, 2, RoundingMode.HALF_UP)
        val finalAmount = afterD2.add(gstAmount).setScale(2, RoundingMode.HALF_UP)
        val totalSaved = originalPrice.subtract(afterD2).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

        return DiscountResult(
            originalPrice = originalPrice.setScale(2, RoundingMode.HALF_UP),
            discountPercent = discountPercent,
            discountAmount = d1Amount,
            priceAfterDiscount = afterD1.setScale(2, RoundingMode.HALF_UP),
            additionalDiscountPercent = additionalDiscountPercent,
            additionalDiscountAmount = d2Amount,
            gstPercent = gstPercent,
            gstAmount = gstAmount,
            finalPayableAmount = finalAmount,
            totalSaved = totalSaved
        )
    }

    data class PercentageResult(
        val label: String,
        val resultValue: BigDecimal,
        val explanation: String
    )

    fun percentOfNumber(pct: BigDecimal, total: BigDecimal): PercentageResult {
        val hundred = BigDecimal(100)
        val res = total.multiply(pct).divide(hundred, 4, RoundingMode.HALF_UP).stripTrailingZeros()
        return PercentageResult(
            label = "Percentage Value",
            resultValue = res,
            explanation = "$pct% of $total = $res"
        )
    }

    fun percentChange(original: BigDecimal, newVal: BigDecimal): PercentageResult {
        if (original.compareTo(BigDecimal.ZERO) == 0) {
            return PercentageResult("Error", BigDecimal.ZERO, "Original value cannot be zero")
        }
        val diff = newVal.subtract(original)
        val hundred = BigDecimal(100)
        val pctChange = diff.multiply(hundred).divide(original.abs(), 2, RoundingMode.HALF_UP)
        val isIncrease = diff >= BigDecimal.ZERO
        val verb = if (isIncrease) "increase" else "decrease"
        return PercentageResult(
            label = if (isIncrease) "Percentage Increase" else "Percentage Decrease",
            resultValue = pctChange.abs(),
            explanation = "$original → $newVal = ${pctChange.abs()}% $verb"
        )
    }

    fun findOriginalFromPercent(part: BigDecimal, pct: BigDecimal): PercentageResult {
        if (pct.compareTo(BigDecimal.ZERO) == 0) {
            return PercentageResult("Error", BigDecimal.ZERO, "Percentage cannot be zero")
        }
        val hundred = BigDecimal(100)
        val original = part.multiply(hundred).divide(pct, 2, RoundingMode.HALF_UP)
        return PercentageResult(
            label = "Original Value",
            resultValue = original,
            explanation = "If $pct% is $part, then 100% is $original"
        )
    }

    fun markup(cost: BigDecimal, markupPct: BigDecimal): PercentageResult {
        val hundred = BigDecimal(100)
        val profit = cost.multiply(markupPct).divide(hundred, 2, RoundingMode.HALF_UP)
        val selling = cost.add(profit).setScale(2, RoundingMode.HALF_UP)
        return PercentageResult(
            label = "Selling Price",
            resultValue = selling,
            explanation = "Cost: $cost + Markup ($markupPct% = $profit) → Selling Price: $selling"
        )
    }
}
