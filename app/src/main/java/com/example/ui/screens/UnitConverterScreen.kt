package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ConversionUnit
import com.example.engine.UnitCategory
import com.example.engine.UnitConverterEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.SmartTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    val units = remember(selectedCategory) { UnitConverterEngine.getUnitsForCategory(selectedCategory) }

    var fromUnit by remember(selectedCategory) { mutableStateOf(units[1]) } // e.g. cm
    var toUnit by remember(selectedCategory) { mutableStateOf(units[2]) }   // e.g. m
    var inputValue by remember { mutableStateOf("100") }

    val numericInput = inputValue.toDoubleOrNull() ?: 0.0
    val convertedValue = remember(numericInput, fromUnit, toUnit, selectedCategory) {
        UnitConverterEngine.convert(numericInput, fromUnit, toUnit, selectedCategory)
    }
    val formattedResult = UnitConverterEngine.formatUnitValue(convertedValue)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "Unit Converter", onBack = onBack)

        // Category Tabs
        val categories = UnitConverterEngine.categories
        val selectedIndex = categories.indexOf(selectedCategory)

        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            categories.forEachIndexed { index, cat ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = {
                        selectedCategory = cat
                        val catUnits = UnitConverterEngine.getUnitsForCategory(cat)
                        fromUnit = catUnits.first()
                        toUnit = if (catUnits.size > 1) catUnits[1] else catUnits.first()
                    },
                    text = {
                        Text(
                            text = cat.displayName,
                            fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("unit_tab_${cat.name.lowercase()}")
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Input card
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

                    UnitSelector(
                        label = "Source Unit",
                        selectedUnit = fromUnit,
                        units = units,
                        onUnitSelected = { fromUnit = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text("Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unit_input_field")
                    )
                }
            }

            // Swap Units Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val temp = fromUnit
                        fromUnit = toUnit
                        toUnit = temp
                    },
                    modifier = Modifier.testTag("swap_units_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap units",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // To Unit Card
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

                    UnitSelector(
                        label = "Target Unit",
                        selectedUnit = toUnit,
                        units = units,
                        onUnitSelected = { toUnit = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Result Display
            ResultCard(
                title = "Converted Result",
                primaryValue = "$formattedResult ${toUnit.symbol}",
                subtitle = "$inputValue ${fromUnit.symbol} = $formattedResult ${toUnit.symbol}",
                shareSummary = "$inputValue ${fromUnit.name} = $formattedResult ${toUnit.name}"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelector(
    label: String,
    selectedUnit: ConversionUnit,
    units: List<ConversionUnit>,
    onUnitSelected: (ConversionUnit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = "${selectedUnit.name} (${selectedUnit.symbol})",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text("${unit.name} (${unit.symbol})") },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}
