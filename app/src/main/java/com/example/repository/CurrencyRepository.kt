package com.example.repository

import android.content.Context
import com.example.model.CurrencyRate
import com.example.model.CurrencyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CurrencyRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)

    // Accurate realistic baseline fallback rates (Base: USD = 1.0)
    private val defaultRates = listOf(
        CurrencyRate("USD", "US Dollar", "$", 1.0),
        CurrencyRate("INR", "Indian Rupee", "₹", 86.85),
        CurrencyRate("EUR", "Euro", "€", 0.95),
        CurrencyRate("GBP", "British Pound", "£", 0.79),
        CurrencyRate("AED", "UAE Dirham", "د.إ", 3.67),
        CurrencyRate("CAD", "Canadian Dollar", "CA$", 1.43),
        CurrencyRate("AUD", "Australian Dollar", "AU$", 1.58),
        CurrencyRate("JPY", "Japanese Yen", "¥", 152.40),
        CurrencyRate("CNY", "Chinese Yuan", "¥", 7.28),
        CurrencyRate("SAR", "Saudi Riyal", "﷼", 3.75),
        CurrencyRate("RUB", "Russian Ruble", "₽", 96.50),
        CurrencyRate("CHF", "Swiss Franc", "CHF", 0.90),
        CurrencyRate("SGD", "Singapore Dollar", "S$", 1.34)
    ).associateBy { it.code }

    private val _currencyState = MutableStateFlow(
        CurrencyState(
            rates = defaultRates,
            lastUpdated = "Cached baseline rates",
            isLive = false
        )
    )
    val currencyState: StateFlow<CurrencyState> = _currencyState.asStateFlow()

    init {
        loadCachedRates()
    }

    private fun loadCachedRates() {
        val cachedJson = prefs.getString("cached_rates_json", null)
        val lastUpdated = prefs.getString("last_updated_time", "Cached baseline rates") ?: "Cached baseline rates"
        val isCachedLive = prefs.getBoolean("is_cached_live", false)

        if (cachedJson != null) {
            try {
                val json = JSONObject(cachedJson)
                val newRates = defaultRates.toMutableMap()
                val keys = json.keys()
                while (keys.hasNext()) {
                    val code = keys.next()
                    val rate = json.getDouble(code)
                    val existing = newRates[code]
                    if (existing != null) {
                        newRates[code] = existing.copy(ratePerUsd = rate)
                    } else {
                        newRates[code] = CurrencyRate(code, code, code, rate)
                    }
                }
                _currencyState.value = CurrencyState(
                    rates = newRates,
                    lastUpdated = lastUpdated,
                    isLive = isCachedLive,
                    isLoading = false
                )
                return
            } catch (e: Exception) {
                // fall through to default
            }
        }

        _currencyState.value = CurrencyState(
            rates = defaultRates,
            lastUpdated = "Offline baseline rates",
            isLive = false,
            isLoading = false
        )
    }

    suspend fun fetchLiveRates() {
        _currencyState.value = _currencyState.value.copy(isLoading = true, error = null)
        withContext(Dispatchers.IO) {
            try {
                // Open public free exchange rate endpoint (open.er-api.com)
                val url = URL("https://open.er-api.com/v6/latest/USD")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.requestMethod = "GET"

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val responseStr = reader.use { it.readText() }
                    val json = JSONObject(responseStr)
                    val ratesObj = json.getJSONObject("rates")

                    val newRates = defaultRates.toMutableMap()
                    val toStore = JSONObject()

                    for (code in defaultRates.keys) {
                        if (ratesObj.has(code)) {
                            val rate = ratesObj.getDouble(code)
                            newRates[code] = newRates[code]!!.copy(ratePerUsd = rate)
                            toStore.put(code, rate)
                        }
                    }

                    val now = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
                    val updateLabel = "Live: $now"

                    prefs.edit()
                        .putString("cached_rates_json", toStore.toString())
                        .putString("last_updated_time", "Cached: $now")
                        .putBoolean("is_cached_live", true)
                        .apply()

                    _currencyState.value = CurrencyState(
                        rates = newRates,
                        lastUpdated = updateLabel,
                        isLive = true,
                        isLoading = false,
                        error = null
                    )
                } else {
                    handleFetchFailure("Server returned code ${conn.responseCode}")
                }
            } catch (e: Exception) {
                handleFetchFailure(e.localizedMessage ?: "Network connection unavailable")
            }
        }
    }

    private fun handleFetchFailure(msg: String) {
        val current = _currencyState.value
        val label = if (current.lastUpdated.startsWith("Live")) {
            current.lastUpdated.replace("Live:", "Cached:")
        } else current.lastUpdated

        _currencyState.value = current.copy(
            lastUpdated = label,
            isLive = false,
            isLoading = false,
            error = "Could not fetch live rates ($msg). Showing cached rates."
        )
    }

    fun convert(amount: Double, fromCode: String, toCode: String): Double {
        val rates = _currencyState.value.rates
        val fromRate = rates[fromCode]?.ratePerUsd ?: return 0.0
        val toRate = rates[toCode]?.ratePerUsd ?: return 0.0

        // Amount in USD = amount / fromRate
        // Amount in toCode = (amount in USD) * toRate
        if (fromRate == 0.0) return 0.0
        val inUsd = amount / fromRate
        return inUsd * toRate
    }
}
