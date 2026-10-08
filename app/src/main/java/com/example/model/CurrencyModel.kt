package com.example.model

data class CurrencyRate(
    val code: String,
    val name: String,
    val symbol: String,
    // Rate relative to 1 USD
    val ratePerUsd: Double
)

data class CurrencyState(
    val rates: Map<String, CurrencyRate> = emptyMap(),
    val lastUpdated: String = "Offline baseline rates",
    val isLive: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
