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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
fun GstCalculatorScreen(
    onBack: () -> Unit
) {
    var amountInput by remember { mutableStateOf("1000") }
    var selectedRate by remember { mutableStateOf("18") }
    var isAddGst by remember { mutableStateOf(true) }

    val ratePresets = listOf("0", "5", "12", "18", "28")

    val amount = amountInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val rate = selectedRate.toBigDecimalOrNull() ?: BigDecimal.ZERO

    val result = remember(amount, rate, isAddGst) {
        FinanceEngine.calculateGst(amount, rate, isAddGst)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "GST Calculator", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Mode Toggle (Add GST vs Remove GST)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { isAddGst = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAddGst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isAddGst) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_gst_tab")
                ) {
                    Text("+ Add GST", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { isAddGst = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isAddGst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!isAddGst) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("remove_gst_tab")
                ) {
                    Text("− Remove GST", fontWeight = FontWeight.Bold)
                }
            }

            // Input Amount
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text(if (isAddGst) "Net Amount (₹)" else "Gross Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gst_amount_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Presets row
            Text(
                text = "GST Rate (%)",
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ratePresets.forEach { preset ->
                    FilterChip(
                        selected = selectedRate == preset,
                        onClick = { selectedRate = preset },
                        label = { Text("$preset%") },
                        modifier = Modifier.testTag("gst_preset_$preset")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Total Card
            ResultCard(
                title = if (isAddGst) "Total Amount (Incl. GST)" else "Net Amount (Excl. GST)",
                primaryValue = "₹${if (isAddGst) result.totalAmount else result.netAmount}",
                subtitle = "GST (${selectedRate}%): ₹${result.gstAmount}",
                shareSummary = "Amount: ₹${amountInput}\nGST: ${selectedRate}% (₹${result.gstAmount})\nCGST: ₹${result.cgst} | SGST: ₹${result.sgst}\nTotal: ₹${result.totalAmount}"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed Breakdown Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tax Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    BreakdownRow("Net Amount", "₹${result.netAmount}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("CGST (${rate.divide(BigDecimal(2))}%)", "₹${result.cgst}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("SGST (${rate.divide(BigDecimal(2))}%)", "₹${result.sgst}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Total GST (${rate}%)", "₹${result.gstAmount}", isHighlight = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Final Total", "₹${result.totalAmount}", isHighlight = true)
                }
            }
        }
    }
}

@Composable
fun BreakdownRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
