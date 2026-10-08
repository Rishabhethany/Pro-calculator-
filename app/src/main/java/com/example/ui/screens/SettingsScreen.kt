package com.example.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repository.AppThemeMode
import com.example.ui.components.SmartTopAppBar
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.settingsRepo.themeMode.collectAsState()
    val hapticEnabled by viewModel.settingsRepo.hapticEnabled.collectAsState()
    val soundEnabled by viewModel.settingsRepo.soundEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(title = "Settings", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Theme",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ThemeOption("Dark Mode (Obsidian Indigo)", AppThemeMode.DARK, themeMode) {
                        viewModel.settingsRepo.setThemeMode(AppThemeMode.DARK)
                    }
                    ThemeOption("AMOLED Pure Black", AppThemeMode.AMOLED, themeMode) {
                        viewModel.settingsRepo.setThemeMode(AppThemeMode.AMOLED)
                    }
                    ThemeOption("Light Mode", AppThemeMode.LIGHT, themeMode) {
                        viewModel.settingsRepo.setThemeMode(AppThemeMode.LIGHT)
                    }
                    ThemeOption("System Default", AppThemeMode.SYSTEM, themeMode) {
                        viewModel.settingsRepo.setThemeMode(AppThemeMode.SYSTEM)
                    }
                }
            }

            // Haptics & Feedback
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Feedback & Interaction",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Haptic Feedback", fontWeight = FontWeight.SemiBold)
                            Text("Vibrate on button press", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                        }
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { viewModel.settingsRepo.setHapticEnabled(it) },
                            modifier = Modifier.testTag("switch_haptic")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Audio Feedback", fontWeight = FontWeight.SemiBold)
                            Text("Click sound on key press", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { viewModel.settingsRepo.setSoundEnabled(it) }
                        )
                    }
                }
            }

            // About SmartCalc Pro
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About SmartCalc Pro", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "SmartCalc Pro is an advanced, high-precision mathematical suite engineered with decimal-safe arithmetic, Room database local history, full offline capability, and Material 3 design.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Version 1.0.0 Pro • 100% Privacy Friendly", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    mode: AppThemeMode,
    currentMode: AppThemeMode,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = currentMode == mode,
            onClick = onSelect,
            modifier = Modifier.testTag("radio_theme_${mode.name.lowercase()}")
        )
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
    }
}
