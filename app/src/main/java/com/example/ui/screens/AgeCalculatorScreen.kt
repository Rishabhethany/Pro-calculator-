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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.engine.DateEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.SmartTopAppBar
import java.time.LocalDate

@Composable
fun AgeCalculatorScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var dob by remember { mutableStateOf(LocalDate.of(2000, 1, 1)) }
    var targetDate by remember { mutableStateOf(LocalDate.now()) }

    val ageResult = remember(dob, targetDate) {
        DateEngine.calculateAge(dob, targetDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "Age Calculator", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Date of Birth Selector
            OutlinedTextField(
                value = dob.format(DateEngine.formatter),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date of Birth") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Select DOB",
                        modifier = Modifier.clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    dob = LocalDate.of(year, month + 1, dayOfMonth)
                                },
                                dob.year,
                                dob.monthValue - 1,
                                dob.dayOfMonth
                            ).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                dob = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            dob.year,
                            dob.monthValue - 1,
                            dob.dayOfMonth
                        ).show()
                    }
                    .testTag("dob_picker_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Target Date Selector
            OutlinedTextField(
                value = targetDate.format(DateEngine.formatter),
                onValueChange = {},
                readOnly = true,
                label = { Text("As of Date") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Select target date",
                        modifier = Modifier.clickable {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    targetDate = LocalDate.of(year, month + 1, dayOfMonth)
                                },
                                targetDate.year,
                                targetDate.monthValue - 1,
                                targetDate.dayOfMonth
                            ).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                targetDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            targetDate.year,
                            targetDate.monthValue - 1,
                            targetDate.dayOfMonth
                        ).show()
                    }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Result
            ResultCard(
                title = "Exact Age",
                primaryValue = "${ageResult.years} Years, ${ageResult.months} Months, ${ageResult.days} Days",
                subtitle = "Next Birthday in ${ageResult.daysToNextBirthday} days (${ageResult.nextBirthdayDayOfWeek})",
                shareSummary = "Age: ${ageResult.years} Years, ${ageResult.months} Months, ${ageResult.days} Days\nTotal Days lived: ${ageResult.totalDays}\nNext Birthday in ${ageResult.daysToNextBirthday} days"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lifetime Stats Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Time Summary", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))
                    BreakdownRow("Total Months", "${ageResult.totalMonths} months")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Total Weeks", "${ageResult.totalWeeks} weeks")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Total Days", "${ageResult.totalDays} days")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Total Hours", "${ageResult.totalHours} hours")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    BreakdownRow("Next Birthday", "${ageResult.daysToNextBirthday} days (${ageResult.nextBirthdayDayOfWeek})", isHighlight = true)
                }
            }
        }
    }
}
