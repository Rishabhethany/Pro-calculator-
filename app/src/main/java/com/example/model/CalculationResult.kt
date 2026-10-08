package com.example.model

data class CalculationResult(
    val resultString: String,
    val numericValue: Double? = null,
    val isError: Boolean = false,
    val errorMessage: String? = null
)
