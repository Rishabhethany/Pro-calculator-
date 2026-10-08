package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.engine.FinanceEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.SmartTopAppBar
import java.math.BigDecimal

@Composable
fun PercentageCalculatorScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    // 0: % of number
    // 1: % increase / decrease
    // 2: Find original value
    // 3: Markup

    val tabs = listOf("% of Number", "% Change", "Find Original", "Cost Markup")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "Percentage Calculator", onBack = onBack)

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("pct_tab_$index")
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> PercentOfNumberSection()
                1 -> PercentChangeSection()
                2 -> FindOriginalSection()
                3 -> MarkupSection()
            }
        }
    }
}

@Composable
fun PercentOfNumberSection() {
    var pctInput by remember { mutableStateOf("20") }
    var numInput by remember { mutableStateOf("500") }

    val pct = pctInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val num = numInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val result = remember(pct, num) { FinanceEngine.percentOfNumber(pct, num) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = pctInput,
                onValueChange = { pctInput = it },
                label = { Text("What is (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pct_rate_input")
            )
            OutlinedTextField(
                value = numInput,
                onValueChange = { numInput = it },
                label = { Text("Of number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pct_total_input")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        ResultCard(
            title = "Percentage Value",
            primaryValue = result.resultValue.toPlainString(),
            subtitle = result.explanation,
            shareSummary = result.explanation
        )
    }
}

@Composable
fun PercentChangeSection() {
    var initialInput by remember { mutableStateOf("500") }
    var finalInput by remember { mutableStateOf("600") }

    val initial = initialInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val finalVal = finalInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val result = remember(initial, finalVal) { FinanceEngine.percentChange(initial, finalVal) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = initialInput,
                onValueChange = { initialInput = it },
                label = { Text("Initial Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = finalInput,
                onValueChange = { finalInput = it },
                label = { Text("Final Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        ResultCard(
            title = result.label,
            primaryValue = "${result.resultValue}%",
            subtitle = result.explanation,
            shareSummary = result.explanation
        )
    }
}

@Composable
fun FindOriginalSection() {
    var partInput by remember { mutableStateOf("100") }
    var pctInput by remember { mutableStateOf("20") }

    val part = partInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val pct = pctInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val result = remember(part, pct) { FinanceEngine.findOriginalFromPercent(part, pct) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = partInput,
                onValueChange = { partInput = it },
                label = { Text("If value is") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = pctInput,
                onValueChange = { pctInput = it },
                label = { Text("Is what (%) of total") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        ResultCard(
            title = result.label,
            primaryValue = result.resultValue.toPlainString(),
            subtitle = result.explanation,
            shareSummary = result.explanation
        )
    }
}

@Composable
fun MarkupSection() {
    var costInput by remember { mutableStateOf("100") }
    var markupInput by remember { mutableStateOf("25") }

    val cost = costInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val markup = markupInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val result = remember(cost, markup) { FinanceEngine.markup(cost, markup) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = costInput,
                onValueChange = { costInput = it },
                label = { Text("Cost Price (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = markupInput,
                onValueChange = { markupInput = it },
                label = { Text("Markup (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        ResultCard(
            title = result.label,
            primaryValue = "₹${result.resultValue}",
            subtitle = result.explanation,
            shareSummary = result.explanation
        )
    }
}
