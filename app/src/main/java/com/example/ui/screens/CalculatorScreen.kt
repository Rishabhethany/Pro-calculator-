package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AngleMode
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentRose
import com.example.util.AppUtils
import com.example.viewmodel.MainViewModel

@Composable
fun CalculatorScreen(
    viewModel: MainViewModel,
    onNavigateToScientific: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val calcState by viewModel.calcState.collectAsState()
    val hapticEnabled by viewModel.settingsRepo.hapticEnabled.collectAsState()
    val context = LocalContext.current

    // Voice recognition launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.handleVoiceInput(spokenText)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Top Toolbar inside screen: Memory, Voice, Scientific, History
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Memory Buttons Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MemoryButton("MC") {
                    AppUtils.performHaptic(context, hapticEnabled)
                    viewModel.memoryClear()
                }
                MemoryButton("MR", enabled = calcState.hasMemory) {
                    AppUtils.performHaptic(context, hapticEnabled)
                    viewModel.memoryRecall()
                }
                MemoryButton("M+") {
                    AppUtils.performHaptic(context, hapticEnabled)
                    viewModel.memoryAdd()
                }
                MemoryButton("M−") {
                    AppUtils.performHaptic(context, hapticEnabled)
                    viewModel.memorySubtract()
                }

                if (calcState.hasMemory) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "M",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Quick Tool Shortcuts
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say math expression (e.g. 50 plus 25)")
                            }
                            voiceLauncher.launch(intent)
                        } catch (e: Exception) {
                            AppUtils.copyToClipboard(context, "Voice input not supported on this device")
                        }
                    },
                    modifier = Modifier.testTag("voice_calc_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onNavigateToScientific,
                    modifier = Modifier.testTag("open_scientific_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Scientific Mode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onNavigateToHistory,
                    modifier = Modifier.testTag("open_history_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Calculation History",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Calculation Display LCD Area
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Expression Display (Scrollable)
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState, reverseScrolling = true),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = if (calcState.expression.isEmpty()) "0" else calcState.expression,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.testTag("calc_expression_display")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Real-time Preview Result or Error
                if (calcState.errorMessage != null) {
                    Text(
                        text = calcState.errorMessage!!,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = AccentRose,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.testTag("calc_error_display")
                    )
                } else if (calcState.previewResult.isNotEmpty()) {
                    Text(
                        text = "= ${calcState.previewResult}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.testTag("calc_preview_display")
                    )
                }
            }
        }

        // Standard Keypad Grid
        val buttonRows = listOf(
            listOf("AC", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("+/−", "0", ".", "=")
        )

        for (row in buttonRows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (btn in row) {
                    Box(modifier = Modifier.weight(1f)) {
                        CalcKey(
                            label = btn,
                            onClick = {
                                AppUtils.performHaptic(context, hapticEnabled)
                                viewModel.onCalcInput(btn)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("mem_btn_$text")
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun CalcKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAction = label in listOf("÷", "×", "−", "+")
    val isEquals = label == "="
    val isClear = label in listOf("AC", "⌫")

    val bg = when {
        isEquals -> AccentRose
        isAction -> MaterialTheme.colorScheme.primary
        isClear -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isEquals -> Color.White
        isAction -> Color.White
        isClear -> AccentRose
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = bg,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.25f)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("key_$label")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                fontSize = if (label.length > 2) 18.sp else 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}
