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
import com.example.engine.BmiEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.SmartTopAppBar

@Composable
fun BmiCalculatorScreen(
    onBack: () -> Unit
) {
    var isMetric by remember { mutableStateOf(true) } // true: cm / kg, false: ft / lb

    var weightInput by remember { mutableStateOf("70") }
    var heightCmInput by remember { mutableStateOf("175") }
    var heightFtInput by remember { mutableStateOf("5") }
    var heightInInput by remember { mutableStateOf("9") }

    // Convert everything to kg & cm
    val weightKg = if (isMetric) {
        weightInput.toDoubleOrNull() ?: 0.0
    } else {
        (weightInput.toDoubleOrNull() ?: 0.0) * 0.45359237
    }

    val heightCm = if (isMetric) {
        heightCmInput.toDoubleOrNull() ?: 0.0
    } else {
        val ft = heightFtInput.toDoubleOrNull() ?: 0.0
        val inches = heightInInput.toDoubleOrNull() ?: 0.0
        ((ft * 12.0) + inches) * 2.54
    }

    val bmiResult = remember(weightKg, heightCm) {
        BmiEngine.calculate(weightKg, heightCm)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "BMI Calculator", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Unit system switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { isMetric = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMetric) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isMetric) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Metric (kg, cm)", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { isMetric = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isMetric) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (!isMetric) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("US Standard (lb, ft)", fontWeight = FontWeight.Bold)
                }
            }

            // Weight input
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text(if (isMetric) "Weight (kg)" else "Weight (lb)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bmi_weight_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Height input
            if (isMetric) {
                OutlinedTextField(
                    value = heightCmInput,
                    onValueChange = { heightCmInput = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bmi_height_input")
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = heightFtInput,
                        onValueChange = { heightFtInput = it },
                        label = { Text("Feet (ft)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightInInput,
                        onValueChange = { heightInInput = it },
                        label = { Text("Inches (in)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ResultCard(
                title = "Body Mass Index (BMI)",
                primaryValue = "${bmiResult.bmiValue} kg/m²",
                subtitle = "Category: ${bmiResult.category}",
                shareSummary = "BMI: ${bmiResult.bmiValue} (${bmiResult.category})\nHealthy weight for this height: ${bmiResult.healthyWeightRangeKg.first} - ${bmiResult.healthyWeightRangeKg.second} kg"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Educational Standard Table Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Adult BMI Reference Ranges", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Note: BMI is a general screening indicator and does not account for muscle mass or body composition.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BreakdownRow("Underweight", "< 18.5")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Normal weight", "18.5 – 24.9", isHighlight = bmiResult.category == "Normal weight")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Overweight", "25.0 – 29.9", isHighlight = bmiResult.category == "Overweight")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Obesity", "≥ 30.0", isHighlight = bmiResult.category.startsWith("Obesity"))
                }
            }
        }
    }
}
