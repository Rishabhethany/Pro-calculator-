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
import com.example.engine.CalculatorEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.SmartTopAppBar

@Composable
fun AdvancedMathScreen(
    onBack: () -> Unit
) {
    var gcdA by remember { mutableStateOf("48") }
    var gcdB by remember { mutableStateOf("18") }
    val aLong = gcdA.toLongOrNull() ?: 0L
    val bLong = gcdB.toLongOrNull() ?: 0L

    val gcdResult = remember(aLong, bLong) { CalculatorEngine.gcd(aLong, bLong) }
    val lcmResult = remember(aLong, bLong) { CalculatorEngine.lcm(aLong, bLong) }

    var primeInput by remember { mutableStateOf("97") }
    val primeLong = primeInput.toLongOrNull() ?: 0L
    val isPrimeResult = remember(primeLong) { CalculatorEngine.isPrime(primeLong) }

    var permN by remember { mutableStateOf("5") }
    var permR by remember { mutableStateOf("2") }
    val nLong = permN.toLongOrNull() ?: 0L
    val rLong = permR.toLongOrNull() ?: 0L

    val nPr = remember(nLong, rLong) {
        try {
            CalculatorEngine.permutations(nLong, rLong).toLong().toString()
        } catch (e: Exception) {
            "Error"
        }
    }
    val nCr = remember(nLong, rLong) {
        try {
            CalculatorEngine.combinations(nLong, rLong).toLong().toString()
        } catch (e: Exception) {
            "Error"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "Advanced Math & Combinatorics", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GCD & LCM Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("GCD & LCM", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = gcdA,
                            onValueChange = { gcdA = it },
                            label = { Text("Number A") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = gcdB,
                            onValueChange = { gcdB = it },
                            label = { Text("Number B") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    BreakdownRow("Greatest Common Divisor (GCD)", "$gcdResult", isHighlight = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Least Common Multiple (LCM)", "$lcmResult", isHighlight = true)
                }
            }

            // Prime Checker Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Prime Number Check", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = primeInput,
                        onValueChange = { primeInput = it },
                        label = { Text("Enter Integer") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BreakdownRow(
                        "Result",
                        if (isPrimeResult) "$primeLong is a PRIME number" else "$primeLong is NOT a prime number",
                        isHighlight = true
                    )
                }
            }

            // Permutations and Combinations Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Permutations & Combinations (nPr / nCr)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = permN,
                            onValueChange = { permN = it },
                            label = { Text("Total items (n)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = permR,
                            onValueChange = { permR = it },
                            label = { Text("Sample size (r)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    BreakdownRow("Permutations (nPr = n! / (n-r)!)", nPr, isHighlight = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Combinations (nCr = n! / (r!(n-r)!))", nCr, isHighlight = true)
                }
            }
        }
    }
}
