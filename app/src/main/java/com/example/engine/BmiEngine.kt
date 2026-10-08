package com.example.engine

data class BmiResult(
    val bmiValue: Double,
    val category: String,
    val healthyWeightRangeKg: Pair<Double, Double>,
    val riskDescription: String
)

object BmiEngine {

    fun calculate(
        weightKg: Double,
        heightCm: Double
    ): BmiResult {
        if (heightCm <= 0 || weightKg <= 0) {
            return BmiResult(0.0, "Invalid Input", Pair(0.0, 0.0), "")
        }

        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)

        val (category, risk) = when {
            bmi < 16.0 -> "Severely Underweight" to "High risk of nutritional deficiency"
            bmi < 18.5 -> "Underweight" to "Moderate risk; consider balanced nutrition"
            bmi < 24.9 -> "Normal weight" to "Lowest health risk category for general adult population"
            bmi < 29.9 -> "Overweight" to "Increased risk of cardiovascular issues"
            bmi < 34.9 -> "Obesity (Class I)" to "High health risk"
            else -> "Obesity (Class II/III)" to "Very high health risk"
        }

        // Healthy range for height: BMI 18.5 to 24.9
        val minHealthyKg = 18.5 * (heightM * heightM)
        val maxHealthyKg = 24.9 * (heightM * heightM)

        return BmiResult(
            bmiValue = Math.round(bmi * 10.0) / 10.0,
            category = category,
            healthyWeightRangeKg = Pair(
                Math.round(minHealthyKg * 10.0) / 10.0,
                Math.round(maxHealthyKg * 10.0) / 10.0
            ),
            riskDescription = risk
        )
    }
}
