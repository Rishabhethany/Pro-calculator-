package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrencyRate
import com.example.ui.components.ResultCard
import com.example.ui.components.SmartTopAppBar
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val currencyState by viewModel.currencyRepo.currencyState.collectAsState()
    val scope = rememberCoroutineScope()

    val availableCurrencies = remember(currencyState.rates) {
        currencyState.rates.values.toList()
    }

    var fromCode by remember { mutableStateOf("USD") }
    var toCode by remember { mutableStateOf("INR") }
    var amountInput by remember { mutableStateOf("100") }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val convertedAmount = remember(amount, fromCode, toCode, currencyState.rates) {
        viewModel.currencyRepo.convert(amount, fromCode, toCode)
    }

    val df = remember { DecimalFormat("#,##0.00") }
    val formattedResult = df.format(convertedAmount)

    val fromRate = currencyState.rates[fromCode]
    val toRate = currencyState.rates[toCode]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(
            title = "Currency Converter",
            onBack = onBack,
            actions = {
                if (currencyState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 12.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = { scope.launch { viewModel.currencyRepo.fetchLiveRates() } },
                        modifier = Modifier.testTag("refresh_rates_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh rates",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Status bar showing Live vs Cached status with timestamp
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currencyState.isLive) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currencyState.isLive) "● LIVE RATES" else "○ CACHED RATES",
                        color = if (currencyState.isLive) AccentGreen else AccentOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = currencyState.lastUpdated,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            // From Currency Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "From",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    CurrencyDropdown(
                        selectedCode = fromCode,
                        currencies = availableCurrencies,
                        onCurrencySelected = { fromCode = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("currency_amount_input")
                    )
                }
            }

            // Swap Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val temp = fromCode
                        fromCode = toCode
                        toCode = temp
                    },
                    modifier = Modifier.testTag("swap_currency_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap currencies",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // To Currency Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "To",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.secondary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    CurrencyDropdown(
                        selectedCode = toCode,
                        currencies = availableCurrencies,
                        onCurrencySelected = { toCode = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Result
            val toSymbol = toRate?.symbol ?: toCode
            val fromSymbol = fromRate?.symbol ?: fromCode

            ResultCard(
                title = "Exchange Result",
                primaryValue = "$toSymbol $formattedResult $toCode",
                subtitle = "$fromSymbol $amountInput $fromCode = $toSymbol $formattedResult $toCode",
                shareSummary = "$amountInput $fromCode = $formattedResult $toCode (Rate: ${currencyState.lastUpdated})"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdown(
    selectedCode: String,
    currencies: List<CurrencyRate>,
    onCurrencySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = currencies.find { it.code == selectedCode }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = "${current?.code ?: selectedCode} - ${current?.name ?: ""}",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { curr ->
                DropdownMenuItem(
                    text = { Text("${curr.code} (${curr.symbol}) — ${curr.name}") },
                    onClick = {
                        onCurrencySelected(curr.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
