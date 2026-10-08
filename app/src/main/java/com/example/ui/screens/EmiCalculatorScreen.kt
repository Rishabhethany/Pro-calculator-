package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.FinanceEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.SmartTopAppBar
import com.example.ui.theme.AccentOrange
import java.math.BigDecimal
import java.text.DecimalFormat

@Composable
fun EmiCalculatorScreen(
    onBack: () -> Unit
) {
    var loanAmountInput by remember { mutableStateOf("500000") }
    var interestRateInput by remember { mutableStateOf("9.5") }
    var tenureInput by remember { mutableStateOf("5") }
    var isYearlyTenure by remember { mutableStateOf(true) }

    val principal = loanAmountInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val annualRate = interestRateInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val tenureNum = tenureInput.toIntOrNull() ?: 1
    val tenureMonths = if (isYearlyTenure) tenureNum * 12 else tenureNum

    val emiResult = remember(principal, annualRate, tenureMonths) {
        FinanceEngine.calculateEmi(principal, annualRate, tenureMonths)
    }

    val df = remember { DecimalFormat("#,##0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "Loan / EMI Calculator", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Inputs
            OutlinedTextField(
                value = loanAmountInput,
                onValueChange = { loanAmountInput = it },
                label = { Text("Loan Principal Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_amount_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = interestRateInput,
                onValueChange = { interestRateInput = it },
                label = { Text("Annual Interest Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_rate_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = tenureInput,
                    onValueChange = { tenureInput = it },
                    label = { Text(if (isYearlyTenure) "Tenure (Years)" else "Tenure (Months)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("loan_tenure_input")
                )

                Button(
                    onClick = { isYearlyTenure = !isYearlyTenure },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("tenure_unit_toggle_btn")
                ) {
                    Text(if (isYearlyTenure) "Yr" else "Mo", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Monthly EMI Result
            ResultCard(
                title = "Monthly EMI",
                primaryValue = "₹${df.format(emiResult.monthlyEmi)}",
                subtitle = "Total Payable: ₹${df.format(emiResult.totalPayable)}",
                shareSummary = "Monthly EMI: ₹${df.format(emiResult.monthlyEmi)}\nLoan Amount: ₹${df.format(principal)}\nInterest Rate: $annualRate%\nTenure: $tenureNum ${if (isYearlyTenure) "Years" else "Months"}\nTotal Interest: ₹${df.format(emiResult.totalInterest)}\nTotal Payable: ₹${df.format(emiResult.totalPayable)}"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Chart & Breakdown Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payment Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val totalPayableDouble = emiResult.totalPayable.toDouble()
                    val principalDouble = emiResult.principal.toDouble()
                    val interestDouble = emiResult.totalInterest.toDouble()

                    val principalRatio = if (totalPayableDouble > 0) (principalDouble / totalPayableDouble).toFloat() else 0.5f
                    val interestRatio = 1f - principalRatio

                    // Donut Ring Chart
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val interestColor = AccentOrange

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(110.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 22f
                                val diameter = size.minDimension - strokeWidth
                                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                                val arcSize = Size(diameter, diameter)

                                val principalSweep = principalRatio * 360f
                                val interestSweep = interestRatio * 360f

                                // Principal Arc
                                drawArc(
                                    color = primaryColor,
                                    startAngle = -90f,
                                    sweepAngle = principalSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // Interest Arc
                                drawArc(
                                    color = interestColor,
                                    startAngle = -90f + principalSweep,
                                    sweepAngle = interestSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = "${Math.round(principalRatio * 100)}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LegendItem("Principal Amount", "₹${df.format(principal)}", primaryColor)
                            LegendItem("Total Interest", "₹${df.format(emiResult.totalInterest)}", interestColor)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    BreakdownRow("Total Amount Payable", "₹${df.format(emiResult.totalPayable)}", isHighlight = true)
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, value: String, dotColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(dotColor, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}
