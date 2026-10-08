package com.example.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolScreen(
    val routeKey: String,
    val title: String,
    val description: String,
    val category: String
) {
    CALCULATOR("CALCULATOR", "Standard Calculator", "Everyday basic arithmetic with memory & brackets", "Core"),
    SCIENTIFIC("SCIENTIFIC", "Scientific Calculator", "Trigonometric, powers, logs & constants", "Core"),
    UNIT_CONVERTER("UNIT_CONVERTER", "Unit Converter", "Length, weight, area, volume, temp & more", "Convert"),
    CURRENCY("CURRENCY", "Currency Converter", "Live & cached real-time exchange rates", "Convert"),
    PERCENTAGE("PERCENTAGE", "Percentage Calculator", "% of number, increase, decrease & markup", "Finance"),
    GST("GST", "GST Calculator", "India CGST/SGST add & remove with 5%, 12%, 18%, 28%", "Finance"),
    EMI("EMI", "EMI Calculator", "Monthly loan payment, total interest & breakdown", "Finance"),
    DISCOUNT("DISCOUNT", "Discount Calculator", "Original price, % off, extra off & GST savings", "Finance"),
    INTEREST("INTEREST", "Interest Calculator", "Simple & Compound interest with compounding modes", "Finance"),
    TIP("TIP", "Tip Calculator", "Bill split, custom tip % & per-person sharing", "Finance"),
    SPLIT_BILL("SPLIT_BILL", "Split Bill", "Share dining bills evenly with optional tip", "Finance"),
    AGE("AGE", "Age Calculator", "Exact age in years/months/days & next birthday", "Date & Time"),
    DATE("DATE", "Date Calculator", "Date difference, working days, add/subtract days", "Date & Time"),
    BMI("BMI", "BMI Calculator", "Body Mass Index & healthy weight ranges", "Health"),
    HISTORY("HISTORY", "Calculation History", "Review, copy and reuse saved calculations", "Tools"),
    SETTINGS("SETTINGS", "Settings", "App theme, haptics, decimal format & info", "Tools")
}
