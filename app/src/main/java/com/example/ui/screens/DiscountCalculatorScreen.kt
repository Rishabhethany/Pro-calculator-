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
fun DiscountCalculatorScreen(
    onBack: () -> Unit
) {
    var priceInput by remember { mutableStateOf("1500") }
    var discountInput by remember { mutableStateOf("20") }
    var extraDiscountInput by remember { mutableStateOf("5") }
    var gstInput by remember { mutableStateOf("18") }

    val originalPrice = priceInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val discountPercent = discountInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val extraPercent = extraDiscountInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val gstPercent = gstInput.toBigDecimalOrNull() ?: BigDecimal.ZERO

    val result = remember(originalPrice, discountPercent, extraPercent, gstPercent) {
        FinanceEngine.calculateDiscount(originalPrice, discountPercent, extraPercent, gstPercent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "Discount Calculator", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = priceInput,
                onValueChange = { priceInput = it },
                label = { Text("Original Price (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("discount_price_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = discountInput,
                    onValueChange = { discountInput = it },
                    label = { Text("Discount (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = extraDiscountInput,
                    onValueChange = { extraDiscountInput = it },
                    label = { Text("Extra Off (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = gstInput,
                onValueChange = { gstInput = it },
                label = { Text("GST/Tax (% optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            ResultCard(
                title = "Final Payable Price",
                primaryValue = "₹${result.finalPayableAmount}",
                subtitle = "Total Savings: ₹${result.totalSaved}",
                shareSummary = "Original Price: ₹${result.originalPrice}\nDiscount: ${result.discountPercent}% + ${result.additionalDiscountPercent}%\nTotal Savings: ₹${result.totalSaved}\nFinal Price: ₹${result.finalPayableAmount}"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Breakdown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))
                    BreakdownRow("Original Price", "₹${result.originalPrice}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Primary Discount (${result.discountPercent}%)", "−₹${result.discountAmount}")
                    if (result.additionalDiscountPercent > BigDecimal.ZERO) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        BreakdownRow("Extra Discount (${result.additionalDiscountPercent}%)", "−₹${result.additionalDiscountAmount}")
                    }
                    if (result.gstPercent > BigDecimal.ZERO) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        BreakdownRow("GST (${result.gstPercent}%)", "+₹${result.gstAmount}")
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("You Save", "₹${result.totalSaved}", isHighlight = true)
                }
            }
        }
    }
}
