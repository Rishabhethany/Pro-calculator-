package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.engine.DateEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.SmartTopAppBar
import java.time.LocalDate

@Composable
fun DateCalculatorScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Date Difference, 1: Add/Subtract Days
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "Date Calculator", onBack = onBack)

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Difference", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("date_tab_diff")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Add / Subtract Days", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("date_tab_add_sub")
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (selectedTab == 0) {
                DateDifferenceSection()
            } else {
                AddSubtractDateSection()
            }
        }
    }
}

@Composable
fun DateDifferenceSection() {
    val context = LocalContext.current
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusMonths(3)) }

    val diffResult = remember(startDate, endDate) {
        DateEngine.calculateDifference(startDate, endDate)
    }

    Column {
        OutlinedTextField(
            value = startDate.format(DateEngine.formatter),
            onValueChange = {},
            readOnly = true,
            label = { Text("Start Date") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select start date",
                    modifier = Modifier.clickable {
                        DatePickerDialog(context, { _, y, m, d -> startDate = LocalDate.of(y, m + 1, d) }, startDate.year, startDate.monthValue - 1, startDate.dayOfMonth).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth().clickable {
                DatePickerDialog(context, { _, y, m, d -> startDate = LocalDate.of(y, m + 1, d) }, startDate.year, startDate.monthValue - 1, startDate.dayOfMonth).show()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = endDate.format(DateEngine.formatter),
            onValueChange = {},
            readOnly = true,
            label = { Text("End Date") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select end date",
                    modifier = Modifier.clickable {
                        DatePickerDialog(context, { _, y, m, d -> endDate = LocalDate.of(y, m + 1, d) }, endDate.year, endDate.monthValue - 1, endDate.dayOfMonth).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth().clickable {
                DatePickerDialog(context, { _, y, m, d -> endDate = LocalDate.of(y, m + 1, d) }, endDate.year, endDate.monthValue - 1, endDate.dayOfMonth).show()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        ResultCard(
            title = "Difference Between Dates",
            primaryValue = "${diffResult.totalDays} Total Days",
            subtitle = "${diffResult.years} Years, ${diffResult.months} Months, ${diffResult.days} Days",
            shareSummary = "Difference from ${startDate.format(DateEngine.formatter)} to ${endDate.format(DateEngine.formatter)}:\n${diffResult.totalDays} Total Days (${diffResult.workingDays} Working Days)"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Calendar Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                BreakdownRow("Total Calendar Days", "${diffResult.totalDays} days")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                BreakdownRow("Working Days (Mon–Fri)", "${diffResult.workingDays} days", isHighlight = true)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                BreakdownRow("Weekend Days", "${diffResult.totalDays - diffResult.workingDays} days")
            }
        }
    }
}

@Composable
fun AddSubtractDateSection() {
    val context = LocalContext.current
    var baseDate by remember { mutableStateOf(LocalDate.now()) }
    var daysCountInput by remember { mutableStateOf("30") }
    var isAdd by remember { mutableStateOf(true) }

    val daysCount = daysCountInput.toLongOrNull() ?: 0L
    val finalDate = remember(baseDate, daysCount, isAdd) {
        if (isAdd) DateEngine.addDays(baseDate, daysCount)
        else DateEngine.subtractDays(baseDate, daysCount)
    }

    Column {
        OutlinedTextField(
            value = baseDate.format(DateEngine.formatter),
            onValueChange = {},
            readOnly = true,
            label = { Text("Base Date") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select base date",
                    modifier = Modifier.clickable {
                        DatePickerDialog(context, { _, y, m, d -> baseDate = LocalDate.of(y, m + 1, d) }, baseDate.year, baseDate.monthValue - 1, baseDate.dayOfMonth).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth().clickable {
                DatePickerDialog(context, { _, y, m, d -> baseDate = LocalDate.of(y, m + 1, d) }, baseDate.year, baseDate.monthValue - 1, baseDate.dayOfMonth).show()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = isAdd,
                onClick = { isAdd = true },
                label = { Text("+ Add Days") }
            )
            FilterChip(
                selected = !isAdd,
                onClick = { isAdd = false },
                label = { Text("− Subtract Days") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = daysCountInput,
            onValueChange = { daysCountInput = it },
            label = { Text("Number of Days") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        val dayOfWeek = finalDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

        ResultCard(
            title = "Target Date",
            primaryValue = finalDate.format(DateEngine.formatter),
            subtitle = "Day of week: $dayOfWeek",
            shareSummary = "Base: ${baseDate.format(DateEngine.formatter)} ${if (isAdd) "+" else "−"} $daysCount days = ${finalDate.format(DateEngine.formatter)} ($dayOfWeek)"
        )
    }
}
