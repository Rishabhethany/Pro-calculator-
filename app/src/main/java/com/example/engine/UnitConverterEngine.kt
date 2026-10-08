package com.example.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

enum class UnitCategory(val displayName: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    AREA("Area"),
    VOLUME("Volume"),
    TEMPERATURE("Temperature"),
    SPEED("Speed"),
    TIME("Time"),
    DATA("Data")
}

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    // conversion factor to standard base unit in that category
    val factorToBase: Double = 1.0,
    val isCustomFormula: Boolean = false
)

object UnitConverterEngine {

    val categories = UnitCategory.values().toList()

    val lengthUnits = listOf(
        ConversionUnit("mm", "Millimeter", "mm", 0.001),
        ConversionUnit("cm", "Centimeter", "cm", 0.01),
        ConversionUnit("m", "Meter", "m", 1.0),
        ConversionUnit("km", "Kilometer", "km", 1000.0),
        ConversionUnit("in", "Inch", "in", 0.0254),
        ConversionUnit("ft", "Foot", "ft", 0.3048),
        ConversionUnit("yd", "Yard", "yd", 0.9144),
        ConversionUnit("mi", "Mile", "mi", 1609.344)
    )

    val weightUnits = listOf(
        ConversionUnit("mg", "Milligram", "mg", 0.000001),
        ConversionUnit("g", "Gram", "g", 0.001),
        ConversionUnit("kg", "Kilogram", "kg", 1.0),
        ConversionUnit("q", "Quintal", "q", 100.0),
        ConversionUnit("t", "Ton (Metric)", "t", 1000.0),
        ConversionUnit("oz", "Ounce", "oz", 0.028349523125),
        ConversionUnit("lb", "Pound", "lb", 0.45359237)
    )

    val areaUnits = listOf(
        ConversionUnit("sqm", "Square meter", "m²", 1.0),
        ConversionUnit("sqkm", "Square kilometer", "km²", 1_000_000.0),
        ConversionUnit("sqft", "Square foot", "ft²", 0.09290304),
        ConversionUnit("sqyd", "Square yard", "yd²", 0.83612736),
        ConversionUnit("acre", "Acre", "ac", 4046.8564224),
        ConversionUnit("ha", "Hectare", "ha", 10000.0)
    )

    val volumeUnits = listOf(
        ConversionUnit("ml", "Milliliter", "mL", 0.001),
        ConversionUnit("l", "Liter", "L", 1.0),
        ConversionUnit("cbm", "Cubic meter", "m³", 1000.0),
        ConversionUnit("gal", "Gallon (US)", "gal", 3.785411784),
        ConversionUnit("qt", "Quart (US)", "qt", 0.946352946),
        ConversionUnit("pt", "Pint (US)", "pt", 0.473176473),
        ConversionUnit("cup", "Cup (US)", "cup", 0.2365882365)
    )

    val temperatureUnits = listOf(
        ConversionUnit("c", "Celsius", "°C", isCustomFormula = true),
        ConversionUnit("f", "Fahrenheit", "°F", isCustomFormula = true),
        ConversionUnit("k", "Kelvin", "K", isCustomFormula = true)
    )

    val speedUnits = listOf(
        ConversionUnit("mps", "m/s", "m/s", 1.0),
        ConversionUnit("kmh", "km/h", "km/h", 1.0 / 3.6),
        ConversionUnit("mph", "mph", "mph", 0.44704),
        ConversionUnit("knot", "Knot", "kn", 0.514444444444)
    )

    val timeUnits = listOf(
        ConversionUnit("ms", "Millisecond", "ms", 0.001),
        ConversionUnit("s", "Second", "s", 1.0),
        ConversionUnit("min", "Minute", "min", 60.0),
        ConversionUnit("hr", "Hour", "hr", 3600.0),
        ConversionUnit("day", "Day", "d", 86400.0),
        ConversionUnit("wk", "Week", "wk", 604800.0),
        ConversionUnit("mo", "Month (Avg)", "mo", 2629746.0),
        ConversionUnit("yr", "Year (365d)", "yr", 31536000.0)
    )

    val dataUnits = listOf(
        ConversionUnit("bit", "Bit", "b", 0.125),
        ConversionUnit("byte", "Byte", "B", 1.0),
        ConversionUnit("kb", "Kilobyte (KB)", "KB", 1024.0),
        ConversionUnit("mb", "Megabyte (MB)", "MB", 1024.0 * 1024.0),
        ConversionUnit("gb", "Gigabyte (GB)", "GB", 1024.0 * 1024.0 * 1024.0),
        ConversionUnit("tb", "Terabyte (TB)", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0)
    )

    fun getUnitsForCategory(category: UnitCategory): List<ConversionUnit> {
        return when (category) {
            UnitCategory.LENGTH -> lengthUnits
            UnitCategory.WEIGHT -> weightUnits
            UnitCategory.AREA -> areaUnits
            UnitCategory.VOLUME -> volumeUnits
            UnitCategory.TEMPERATURE -> temperatureUnits
            UnitCategory.SPEED -> speedUnits
            UnitCategory.TIME -> timeUnits
            UnitCategory.DATA -> dataUnits
        }
    }

    fun convert(
        value: Double,
        fromUnit: ConversionUnit,
        toUnit: ConversionUnit,
        category: UnitCategory
    ): Double {
        if (fromUnit.id == toUnit.id) return value

        if (category == UnitCategory.TEMPERATURE) {
            // Convert from fromUnit to Celsius
            val celsius = when (fromUnit.id) {
                "c" -> value
                "f" -> (value - 32.0) * (5.0 / 9.0)
                "k" -> value - 273.15
                else -> value
            }
            // Convert from Celsius to toUnit
            return when (toUnit.id) {
                "c" -> celsius
                "f" -> (celsius * (9.0 / 5.0)) + 32.0
                "k" -> celsius + 273.15
                else -> celsius
            }
        }

        // Standard linear conversion
        val inBase = value * fromUnit.factorToBase
        return inBase / toUnit.factorToBase
    }

    fun formatUnitValue(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val bd = BigDecimal(value, MathContext(10, RoundingMode.HALF_UP))
            .stripTrailingZeros()
        return if (bd.scale() < 0) bd.setScale(0).toPlainString() else bd.toPlainString()
    }
}
