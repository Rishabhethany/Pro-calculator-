package com.example.engine

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val totalMonths: Long,
    val totalWeeks: Long,
    val totalHours: Long,
    val daysToNextBirthday: Long,
    val nextBirthdayDayOfWeek: String
)

data class DateDifferenceResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val workingDays: Long
)

object DateEngine {

    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    fun calculateAge(dob: LocalDate, targetDate: LocalDate): AgeResult {
        if (targetDate.isBefore(dob)) {
            // Target is before birth
            return AgeResult(0, 0, 0, 0, 0, 0, 0, 0, "")
        }

        val period = Period.between(dob, targetDate)
        val totalDays = ChronoUnit.DAYS.between(dob, targetDate)
        val totalMonths = ChronoUnit.MONTHS.between(dob, targetDate)
        val totalWeeks = totalDays / 7
        val totalHours = totalDays * 24

        // Next birthday calculation
        var nextBday = dob.withYear(targetDate.year)
        if (nextBday.isBefore(targetDate) || nextBday.isEqual(targetDate)) {
            nextBday = nextBday.plusYears(1)
        }
        val daysToNext = ChronoUnit.DAYS.between(targetDate, nextBday)
        val nextDayOfWeek = nextBday.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

        return AgeResult(
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            totalMonths = totalMonths,
            totalWeeks = totalWeeks,
            totalHours = totalHours,
            daysToNextBirthday = daysToNext,
            nextBirthdayDayOfWeek = nextDayOfWeek
        )
    }

    fun calculateDifference(start: LocalDate, end: LocalDate): DateDifferenceResult {
        val (first, second) = if (start.isBefore(end)) Pair(start, end) else Pair(end, start)
        val period = Period.between(first, second)
        val totalDays = ChronoUnit.DAYS.between(first, second)

        // Calculate working days (Mon-Fri)
        var workingDays = 0L
        var curr = first
        while (curr.isBefore(second)) {
            if (curr.dayOfWeek != DayOfWeek.SATURDAY && curr.dayOfWeek != DayOfWeek.SUNDAY) {
                workingDays++
            }
            curr = curr.plusDays(1)
        }

        return DateDifferenceResult(
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            workingDays = workingDays
        )
    }

    fun addDays(date: LocalDate, days: Long): LocalDate = date.plusDays(days)

    fun subtractDays(date: LocalDate, days: Long): LocalDate = date.minusDays(days)
}
