package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.model.ToolScreen
import com.example.ui.screens.AgeCalculatorScreen
import com.example.ui.screens.BmiCalculatorScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.CurrencyConverterScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DateCalculatorScreen
import com.example.ui.screens.DiscountCalculatorScreen
import com.example.ui.screens.EmiCalculatorScreen
import com.example.ui.screens.GstCalculatorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.InterestCalculatorScreen
import com.example.ui.screens.PercentageCalculatorScreen
import com.example.ui.screens.ScientificCalculatorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TipCalculatorScreen
import com.example.ui.screens.UnitConverterScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

enum class MainNavTab {
    CALCULATOR,
    TOOLS,
    HISTORY,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.settingsRepo.themeMode.collectAsState()

            MyApplicationTheme(themeMode = themeMode) {
                SmartCalcApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SmartCalcApp(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(MainNavTab.CALCULATOR) }
    var activeSubTool by remember { mutableStateOf<ToolScreen?>(null) }

    // Predictive / Custom back handling
    BackHandler(enabled = activeSubTool != null || currentTab != MainNavTab.CALCULATOR) {
        if (activeSubTool != null) {
            activeSubTool = null
        } else if (currentTab != MainNavTab.CALCULATOR) {
            currentTab = MainNavTab.CALCULATOR
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        bottomBar = {
            if (activeSubTool == null) {
                NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
                    NavigationBarItem(
                        selected = currentTab == MainNavTab.CALCULATOR,
                        onClick = { currentTab = MainNavTab.CALCULATOR },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") },
                        label = { Text("Calc") },
                        modifier = Modifier.testTag("tab_calculator")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainNavTab.TOOLS,
                        onClick = { currentTab = MainNavTab.TOOLS },
                        icon = { Icon(Icons.Default.GridView, contentDescription = "Tools") },
                        label = { Text("Tools") },
                        modifier = Modifier.testTag("tab_tools")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainNavTab.HISTORY,
                        onClick = { currentTab = MainNavTab.HISTORY },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        modifier = Modifier.testTag("tab_history")
                    )
                    NavigationBarItem(
                        selected = currentTab == MainNavTab.SETTINGS,
                        onClick = { currentTab = MainNavTab.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
            if (activeSubTool != null) {
                when (activeSubTool) {
                    ToolScreen.SCIENTIFIC -> ScientificCalculatorScreen(
                        viewModel = viewModel,
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.UNIT_CONVERTER -> UnitConverterScreen(
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.CURRENCY -> CurrencyConverterScreen(
                        viewModel = viewModel,
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.GST -> GstCalculatorScreen(
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.EMI -> EmiCalculatorScreen(
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.PERCENTAGE -> PercentageCalculatorScreen(
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.DISCOUNT -> DiscountCalculatorScreen(
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.INTEREST -> InterestCalculatorScreen(
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.TIP, ToolScreen.SPLIT_BILL -> TipCalculatorScreen(
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.AGE -> AgeCalculatorScreen(
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.DATE -> DateCalculatorScreen(
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.BMI -> BmiCalculatorScreen(
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.HISTORY -> HistoryScreen(
                        viewModel = viewModel,
                        onBack = { activeSubTool = null },
                        onSelectExpression = { expr ->
                            viewModel.setExpression(expr)
                            activeSubTool = null
                            currentTab = MainNavTab.CALCULATOR
                        }
                    )
                    ToolScreen.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onBack = { activeSubTool = null }
                    )
                    ToolScreen.CALCULATOR, null -> {
                        CalculatorScreen(
                            viewModel = viewModel,
                            onNavigateToScientific = { activeSubTool = ToolScreen.SCIENTIFIC },
                            onNavigateToHistory = { activeSubTool = ToolScreen.HISTORY }
                        )
                    }
                }
            } else {
                when (currentTab) {
                    MainNavTab.CALCULATOR -> CalculatorScreen(
                        viewModel = viewModel,
                        onNavigateToScientific = { activeSubTool = ToolScreen.SCIENTIFIC },
                        onNavigateToHistory = { activeSubTool = ToolScreen.HISTORY }
                    )
                    MainNavTab.TOOLS -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { tool ->
                            if (tool == ToolScreen.CALCULATOR) {
                                currentTab = MainNavTab.CALCULATOR
                            } else {
                                activeSubTool = tool
                            }
                        }
                    )
                    MainNavTab.HISTORY -> HistoryScreen(
                        viewModel = viewModel,
                        onBack = { currentTab = MainNavTab.CALCULATOR },
                        onSelectExpression = { expr ->
                            viewModel.setExpression(expr)
                            currentTab = MainNavTab.CALCULATOR
                        }
                    )
                    MainNavTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onBack = { currentTab = MainNavTab.CALCULATOR }
                    )
                }
            }
        }
    }
}
