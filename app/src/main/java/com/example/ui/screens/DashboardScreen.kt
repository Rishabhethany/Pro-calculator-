package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ToolScreen
import com.example.ui.components.SmartTopAppBar
import com.example.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (ToolScreen) -> Unit
) {
    val favorites by viewModel.settingsRepo.favorites.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val allTools = listOf(
        ToolScreen.CALCULATOR,
        ToolScreen.SCIENTIFIC,
        ToolScreen.UNIT_CONVERTER,
        ToolScreen.CURRENCY,
        ToolScreen.GST,
        ToolScreen.EMI,
        ToolScreen.PERCENTAGE,
        ToolScreen.DISCOUNT,
        ToolScreen.INTEREST,
        ToolScreen.TIP,
        ToolScreen.AGE,
        ToolScreen.DATE,
        ToolScreen.BMI
    )

    val filteredTools = if (searchQuery.isBlank()) {
        allTools
    } else {
        allTools.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val favoriteTools = allTools.filter { favorites.contains(it.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartTopAppBar(
            title = "SmartCalc Pro",
            actions = {
                IconButton(
                    onClick = { onNavigate(ToolScreen.HISTORY) },
                    modifier = Modifier.testTag("nav_history_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { onNavigate(ToolScreen.SETTINGS) },
                    modifier = Modifier.testTag("nav_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search calculator or converter...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("home_search_bar")
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Favorites Section (if not searching)
            if (searchQuery.isBlank() && favoriteTools.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorites",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = " FAVORITES",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                items(favoriteTools, key = { "fav_${it.name}" }) { tool ->
                    ToolCard(
                        tool = tool,
                        isFavorite = true,
                        onToggleFavorite = { viewModel.settingsRepo.toggleFavorite(tool.name) },
                        onClick = { onNavigate(tool) }
                    )
                }
            }

            // All Tools Section Header
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = if (searchQuery.isBlank()) "ALL CALCULATORS & TOOLS" else "SEARCH RESULTS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(filteredTools, key = { it.name }) { tool ->
                ToolCard(
                    tool = tool,
                    isFavorite = favorites.contains(tool.name),
                    onToggleFavorite = { viewModel.settingsRepo.toggleFavorite(tool.name) },
                    onClick = { onNavigate(tool) }
                )
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ToolCard(
    tool: ToolScreen,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val icon = getToolIcon(tool)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tool_card_${tool.routeKey.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tool.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = tool.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                ),
                maxLines = 2
            )
        }
    }
}

fun getToolIcon(tool: ToolScreen): ImageVector {
    return when (tool) {
        ToolScreen.CALCULATOR -> Icons.Default.Calculate
        ToolScreen.SCIENTIFIC -> Icons.Default.Science
        ToolScreen.UNIT_CONVERTER -> Icons.Default.Scale
        ToolScreen.CURRENCY -> Icons.Default.CurrencyExchange
        ToolScreen.PERCENTAGE -> Icons.Default.Percent
        ToolScreen.GST -> Icons.Default.ReceiptLong
        ToolScreen.EMI -> Icons.Default.RequestQuote
        ToolScreen.DISCOUNT -> Icons.Default.Discount
        ToolScreen.INTEREST -> Icons.Default.Savings
        ToolScreen.TIP -> Icons.Default.ReceiptLong
        ToolScreen.SPLIT_BILL -> Icons.Default.ReceiptLong
        ToolScreen.AGE -> Icons.Default.Today
        ToolScreen.DATE -> Icons.Default.CalendarMonth
        ToolScreen.BMI -> Icons.Default.FitnessCenter
        ToolScreen.HISTORY -> Icons.Default.History
        ToolScreen.SETTINGS -> Icons.Default.Settings
    }
}
