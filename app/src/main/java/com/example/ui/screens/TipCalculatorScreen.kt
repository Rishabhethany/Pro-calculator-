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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
fun TipCalculatorScreen(
    onBack: () -> Unit
) {
    var billInput by remember { mutableStateOf("1200") }
    var tipPercentInput by remember { mutableStateOf("10") }
    var peopleInput by remember { mutableStateOf("2") }

    val quickTipPresets = listOf("5", "10", "15", "20")

    val bill = billInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val tipPercent = tipPercentInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val people = peopleInput.toIntOrNull() ?: 1

    val result = remember(bill, tipPercent, people) {
        FinanceEngine.calculateTip(bill, tipPercent, people)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "Tip & Split Bill", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = billInput,
                onValueChange = { billInput = it },
                label = { Text("Bill Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tip_bill_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Tip Percentage (%)", style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickTipPresets.forEach { preset ->
                    FilterChip(
                        selected = tipPercentInput == preset,
                        onClick = { tipPercentInput = preset },
                        label = { Text("$preset%") },
                        modifier = Modifier.testTag("tip_chip_$preset")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tipPercentInput,
                onValueChange = { tipPercentInput = it },
                label = { Text("Custom Tip (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = peopleInput,
                onValueChange = { peopleInput = it },
                label = { Text("Number of People") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tip_people_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            ResultCard(
                title = "Amount Per Person",
                primaryValue = "₹${result.perPersonAmount}",
                subtitle = "Total Bill: ₹${result.totalBill} (${result.numPeople} people)",
                shareSummary = "Bill: ₹${result.billAmount}\nTip: ${result.tipPercent}% (₹${result.tipAmount})\nTotal: ₹${result.totalBill}\nPer Person (${result.numPeople}): ₹${result.perPersonAmount}"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bill Split Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))
                    BreakdownRow("Original Bill", "₹${result.billAmount}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Total Tip Amount", "₹${result.tipAmount}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Total Bill + Tip", "₹${result.totalBill}", isHighlight = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Tip Per Person", "₹${result.perPersonTip}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Each Person Pays", "₹${result.perPersonAmount}", isHighlight = true)
                }
            }
        }
    }
}
