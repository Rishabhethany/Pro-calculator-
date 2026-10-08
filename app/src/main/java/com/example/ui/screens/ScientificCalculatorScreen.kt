package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AngleMode
import com.example.ui.components.SmartTopAppBar
import com.example.ui.theme.AccentRose
import com.example.util.AppUtils
import com.example.viewmodel.MainViewModel

@Composable
fun ScientificCalculatorScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val calcState by viewModel.calcState.collectAsState()
    val hapticEnabled by viewModel.settingsRepo.hapticEnabled.collectAsState()
    val context = LocalContext.current
    var isShiftMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(
            title = "Scientific Calculator",
            onBack = onBack,
            actions = {
                // Angle Mode Indicator & Switcher
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clickable {
                            AppUtils.performHaptic(context, hapticEnabled)
                            viewModel.toggleAngleMode()
                        }
                        .padding(end = 8.dp)
                        .testTag("toggle_angle_mode_btn")
                ) {
                    Text(
                        text = calcState.angleMode.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Display LCD
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val scroll = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scroll, reverseScrolling = true),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = if (calcState.expression.isEmpty()) "0" else calcState.expression,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.testTag("sci_calc_expression_display")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (calcState.errorMessage != null) {
                        Text(
                            text = calcState.errorMessage!!,
                            color = AccentRose,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (calcState.previewResult.isNotEmpty()) {
                        Text(
                            text = "= ${calcState.previewResult}",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Scientific Keypad Matrix (5 columns x 7 rows)
            val sciRows = if (!isShiftMode) {
                listOf(
                    listOf("2nd", "sin", "cos", "tan", "AC"),
                    listOf("ln", "log", "log₂", "√", "⌫"),
                    listOf("x²", "xʸ", "1/x", "x!", "÷"),
                    listOf("π", "e", "(", ")", "×"),
                    listOf("7", "8", "9", "%", "−"),
                    listOf("4", "5", "6", "|x|", "+"),
                    listOf("1", "2", "3", "0", "="),
                    listOf("+/−", ".", "rand", "mod", "floor")
                )
            } else {
                listOf(
                    listOf("2nd", "sin⁻¹", "cos⁻¹", "tan⁻¹", "AC"),
                    listOf("sinh", "cosh", "tanh", "∛", "⌫"),
                    listOf("x³", "xʸ", "1/x", "x!", "÷"),
                    listOf("π", "e", "(", ")", "×"),
                    listOf("7", "8", "9", "%", "−"),
                    listOf("4", "5", "6", "ceil", "+"),
                    listOf("1", "2", "3", "0", "="),
                    listOf("+/−", ".", "rand", "mod", "floor")
                )
            }

            for (row in sciRows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (btn in row) {
                        Box(modifier = Modifier.weight(1f)) {
                            SciKey(
                                label = btn,
                                isShiftActive = isShiftMode && btn == "2nd",
                                onClick = {
                                    AppUtils.performHaptic(context, hapticEnabled)
                                    if (btn == "2nd") {
                                        isShiftMode = !isShiftMode
                                    } else {
                                        viewModel.onCalcInput(btn)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SciKey(
    label: String,
    isShiftActive: Boolean = false,
    onClick: () -> Unit
) {
    val isAction = label in listOf("÷", "×", "−", "+")
    val isEquals = label == "="
    val isClear = label in listOf("AC", "⌫")
    val isFn = label in listOf(
        "sin", "cos", "tan", "sin⁻¹", "cos⁻¹", "tan⁻¹", "sinh", "cosh", "tanh",
        "ln", "log", "log₂", "√", "∛", "x²", "x³", "xʸ", "1/x", "x!", "π", "e",
        "|x|", "rand", "mod", "floor", "ceil", "2nd"
    )

    val bg = when {
        isShiftActive -> MaterialTheme.colorScheme.tertiary
        isEquals -> AccentRose
        isAction -> MaterialTheme.colorScheme.primary
        isClear -> MaterialTheme.colorScheme.surfaceVariant
        isFn -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isShiftActive -> MaterialTheme.colorScheme.onTertiary
        isEquals -> Color.White
        isAction -> Color.White
        isClear -> AccentRose
        isFn -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.35f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("sci_key_$label")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                fontSize = if (label.length > 3) 12.sp else if (label.length > 2) 13.sp else 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}
