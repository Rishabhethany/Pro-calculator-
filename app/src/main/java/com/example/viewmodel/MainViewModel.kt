package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.db.AppDatabase
import com.example.engine.AngleMode
import com.example.engine.CalculatorEngine
import com.example.model.CalculationResult
import com.example.model.HistoryItem
import com.example.repository.CurrencyRepository
import com.example.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val previewResult: String = "",
    val lastResult: String = "",
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false,
    val angleMode: AngleMode = AngleMode.DEG,
    val isRadMode: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val historyDao = db.historyDao()
    val currencyRepo = CurrencyRepository(application)
    val settingsRepo = SettingsRepository(application)

    // Calculator State
    private val _calcState = MutableStateFlow(CalculatorUiState())
    val calcState: StateFlow<CalculatorUiState> = _calcState.asStateFlow()

    // History Flow with search support
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    val historyList: StateFlow<List<HistoryItem>> = _historySearchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            historyDao.getAllHistory()
        } else {
            historyDao.searchHistory(query.trim())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Fetch fresh currency rates in background
        viewModelScope.launch {
            currencyRepo.fetchLiveRates()
        }
    }

    fun onCalcInput(input: String) {
        val curr = _calcState.value.expression
        val updated = when (input) {
            "AC" -> ""
            "⌫" -> if (curr.isNotEmpty()) curr.dropLast(1) else ""
            "+/−" -> toggleSign(curr)
            "=" -> {
                evaluateAndSave(curr)
                return
            }
            "π" -> curr + "pi"
            "e" -> curr + "e"
            "sin⁻¹" -> curr + "asin("
            "cos⁻¹" -> curr + "acos("
            "tan⁻¹" -> curr + "atan("
            "sin" -> curr + "sin("
            "cos" -> curr + "cos("
            "tan" -> curr + "tan("
            "sinh" -> curr + "sinh("
            "cosh" -> curr + "cosh("
            "tanh" -> curr + "tanh("
            "ln" -> curr + "ln("
            "log" -> curr + "log("
            "log₂" -> curr + "log2("
            "√" -> curr + "sqrt("
            "∛" -> curr + "cbrt("
            "1/x" -> curr + "inv("
            "|x|" -> curr + "abs("
            "floor" -> curr + "floor("
            "ceil" -> curr + "ceil("
            "rand" -> {
                val rand = Math.round(Math.random() * 1000.0) / 1000.0
                curr + rand.toString()
            }
            "x²" -> curr + "^2"
            "x³" -> curr + "^3"
            "xʸ" -> curr + "^"
            "x!" -> curr + "!"
            "mod" -> curr + " mod "
            else -> curr + input
        }

        // Real-time live result evaluation if possible
        val livePreview = if (updated.isNotEmpty()) {
            val eval = CalculatorEngine.evaluate(updated, _calcState.value.angleMode)
            if (!eval.isError) eval.resultString else ""
        } else ""

        _calcState.value = _calcState.value.copy(
            expression = updated,
            previewResult = livePreview,
            errorMessage = null
        )
    }

    private fun toggleSign(expr: String): String {
        if (expr.isEmpty()) return "-"
        return if (expr.startsWith("-")) expr.substring(1) else "-$expr"
    }

    private fun evaluateAndSave(expr: String) {
        if (expr.isBlank()) return
        val res = CalculatorEngine.evaluate(expr, _calcState.value.angleMode)
        if (res.isError) {
            _calcState.value = _calcState.value.copy(
                errorMessage = res.errorMessage ?: "Invalid expression"
            )
        } else {
            val resultStr = res.resultString
            _calcState.value = _calcState.value.copy(
                lastResult = resultStr,
                expression = resultStr,
                previewResult = "",
                errorMessage = null
            )
            // Persist to Room history
            saveHistory(expr, resultStr, "Calculator")
        }
    }

    fun saveHistory(expression: String, result: String, category: String = "Calculator") {
        viewModelScope.launch {
            historyDao.insertHistory(
                HistoryItem(
                    expression = expression,
                    result = result,
                    category = category
                )
            )
        }
    }

    fun setExpression(expr: String) {
        val eval = CalculatorEngine.evaluate(expr, _calcState.value.angleMode)
        _calcState.value = _calcState.value.copy(
            expression = expr,
            previewResult = if (!eval.isError) eval.resultString else "",
            errorMessage = null
        )
    }

    // Memory operations
    fun memoryClear() {
        _calcState.value = _calcState.value.copy(memoryValue = 0.0, hasMemory = false)
    }

    fun memoryRecall() {
        val memStr = CalculatorEngine.formatResult(_calcState.value.memoryValue)
        onCalcInput(memStr)
    }

    fun memoryAdd() {
        val curr = _calcState.value.expression
        val eval = CalculatorEngine.evaluate(curr, _calcState.value.angleMode)
        if (!eval.isError && eval.numericValue != null) {
            val newMem = _calcState.value.memoryValue + eval.numericValue
            _calcState.value = _calcState.value.copy(memoryValue = newMem, hasMemory = true)
        }
    }

    fun memorySubtract() {
        val curr = _calcState.value.expression
        val eval = CalculatorEngine.evaluate(curr, _calcState.value.angleMode)
        if (!eval.isError && eval.numericValue != null) {
            val newMem = _calcState.value.memoryValue - eval.numericValue
            _calcState.value = _calcState.value.copy(memoryValue = newMem, hasMemory = true)
        }
    }

    fun toggleAngleMode() {
        val nextMode = when (_calcState.value.angleMode) {
            AngleMode.DEG -> AngleMode.RAD
            AngleMode.RAD -> AngleMode.GRAD
            AngleMode.GRAD -> AngleMode.DEG
        }
        _calcState.value = _calcState.value.copy(
            angleMode = nextMode,
            isRadMode = nextMode == AngleMode.RAD
        )
        // Re-evaluate live preview with new angle mode
        val curr = _calcState.value.expression
        if (curr.isNotEmpty()) {
            val eval = CalculatorEngine.evaluate(curr, nextMode)
            if (!eval.isError) {
                _calcState.value = _calcState.value.copy(previewResult = eval.resultString)
            }
        }
    }

    // History methods
    fun setSearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyDao.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyDao.clearAll()
        }
    }

    // Voice calculation parser
    fun handleVoiceInput(speech: String): String {
        var clean = speech.lowercase().trim()
        clean = clean.replace("plus", "+")
            .replace("add", "+")
            .replace("minus", "-")
            .replace("subtract", "-")
            .replace("times", "*")
            .replace("multiplied by", "*")
            .replace("into", "*")
            .replace("x", "*")
            .replace("divided by", "/")
            .replace("divide by", "/")
            .replace("divide", "/")
            .replace("over", "/")
            .replace("percent of", "% *")
            .replace("percent", "%")
            .replace("ka", "*") // Hindi "500 ka 18 percent" -> 500 * 18 %

        // Clean out words that aren't math
        val sb = StringBuilder()
        for (token in clean.split(" ")) {
            val t = token.trim()
            if (t.matches(Regex("^[0-9.]+$")) || t in listOf("+", "-", "*", "/", "%", "(", ")")) {
                sb.append(t)
            }
        }
        val parsedExpr = sb.toString()
        if (parsedExpr.isNotEmpty()) {
            setExpression(parsedExpr)
        }
        return parsedExpr
    }
}
