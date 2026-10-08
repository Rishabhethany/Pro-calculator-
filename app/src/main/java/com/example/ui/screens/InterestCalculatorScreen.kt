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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestCalculatorScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Simple, 1: Compound

    var principalInput by remember { mutableStateOf("100000") }
    var rateInput by remember { mutableStateOf("7.5") }
    var timeYearsInput by remember { mutableStateOf("3") }
    var compoundFreq by remember { mutableStateOf(FinanceEngine.CompoundingFrequency.YEARLY) }

    val principal = principalInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val rate = rateInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val timeYears = timeYearsInput.toBigDecimalOrNull() ?: BigDecimal.ZERO

    val simpleResult = remember(principal, rate, timeYears) {
        FinanceEngine.calculateSimpleInterest(principal, rate, timeYears)
    }

    val compoundResult = remember(principal, rate, timeYears, compoundFreq) {
        FinanceEngine.calculateCompoundInterest(principal, rate, timeYears, compoundFreq)
    }

    val df = remember { DecimalFormat("#,##0.00") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "Interest Calculator", onBack = onBack)

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Simple Interest", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_simple_interest")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Compound Interest", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_compound_interest")
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = principalInput,
                onValueChange = { principalInput = it },
                label = { Text("Principal Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interest_principal_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = rateInput,
                onValueChange = { rateInput = it },
                label = { Text("Annual Interest Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interest_rate_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = timeYearsInput,
                onValueChange = { timeYearsInput = it },
                label = { Text("Time Period (Years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interest_time_input")
            )

            if (selectedTab == 1) {
                Spacer(modifier = Modifier.height(12.dp))
                var freqExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = freqExpanded,
                    onExpandedChange = { freqExpanded = !freqExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = compoundFreq.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Compounding Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = freqExpanded,
                        onDismissRequest = { freqExpanded = false }
                    ) {
                        FinanceEngine.CompoundingFrequency.values().forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.label) },
                                onClick = {
                                    compoundFreq = freq
                                    freqExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == 0) {
                ResultCard(
                    title = "Total Interest Earned",
                    primaryValue = "₹${df.format(simpleResult.interest)}",
                    subtitle = "Maturity Amount: ₹${df.format(simpleResult.finalAmount)}",
                    shareSummary = "Simple Interest Calculation:\nPrincipal: ₹$principal\nRate: $rate%\nTime: $timeYears Years\nFormula: SI = P × R × T / 100\nInterest: ₹${df.format(simpleResult.interest)}\nTotal Amount: ₹${df.format(simpleResult.finalAmount)}"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Calculation Breakdown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))
                        BreakdownRow("Formula", "SI = P × R × T / 100")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        BreakdownRow("Principal (P)", "₹${df.format(principal)}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        BreakdownRow("Interest (I)", "₹${df.format(simpleResult.interest)}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        BreakdownRow("Total Maturity Value", "₹${df.format(simpleResult.finalAmount)}", isHighlight = true)
                    }
                }
            } else {
                ResultCard(
                    title = "Compound Interest Earned",
                    primaryValue = "₹${df.format(compoundResult.interest)}",
                    subtitle = "Maturity Amount: ₹${df.format(compoundResult.finalAmount)}",
                    shareSummary = "Compound Interest (${compoundFreq.label}):\nPrincipal: ₹$principal\nRate: $rate%\nTime: $timeYears Years\nCompound Interest: ₹${df.format(compoundResult.interest)}\nTotal Amount: ₹${df.format(compoundResult.finalAmount)}"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Calculation Breakdown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))
                        BreakdownRow("Compounding", compoundFreq.label)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        BreakdownRow("Principal (P)", "₹${df.format(principal)}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        BreakdownRow("Compound Interest", "₹${df.format(compoundResult.interest)}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        BreakdownRow("Total Maturity Value", "₹${df.format(compoundResult.finalAmount)}", isHighlight = true)
                    }
                }
            }
        }
    }
}
