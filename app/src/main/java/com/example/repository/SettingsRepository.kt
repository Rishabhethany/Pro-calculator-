package com.example.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    LIGHT, DARK, AMOLED, SYSTEM
}

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("smart_calc_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name)
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(prefs.getBoolean("haptic_enabled", true))
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", false))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _decimalPlaces = MutableStateFlow(prefs.getInt("decimal_places", 4))
    val decimalPlaces: StateFlow<Int> = _decimalPlaces.asStateFlow()

    private val _favorites = MutableStateFlow(
        prefs.getStringSet("favorites", setOf("SCIENTIFIC", "UNIT_CONVERTER", "CURRENCY", "GST", "EMI", "PERCENTAGE")) ?: emptySet()
    )
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setHapticEnabled(enabled: Boolean) {
        _hapticEnabled.value = enabled
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun setDecimalPlaces(places: Int) {
        _decimalPlaces.value = places
        prefs.edit().putInt("decimal_places", places).apply()
    }

    fun toggleFavorite(toolKey: String) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(toolKey)) {
            current.remove(toolKey)
        } else {
            current.add(toolKey)
        }
        _favorites.value = current
        prefs.edit().putStringSet("favorites", current).apply()
    }
}
