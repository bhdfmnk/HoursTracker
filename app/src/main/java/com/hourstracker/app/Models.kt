package com.hourstracker.app

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private const val MINUTES_PER_DAY = 24 * 60

data class Settings(
    val hourlyRate: Double = 0.0,
    val taxRate: Double = 0.0,
    val currency: String = "£"
)

/**
 * One day of work. Times are stored as minutes since midnight (0..1439),
 * so 13:30 is 810. If the end is earlier than the start the shift is
 * treated as ending after midnight (e.g. 07:30 -> 00:50 = 17h 20m).
 */
data class DayEntry(
    val date: LocalDate,
    val startMin: Int? = null,
    val endMin: Int? = null,
    val breakMin: Int = 0,
    val subtractBreak: Boolean = true,
    val note: String = ""
) {
    val isEmpty: Boolean
        get() = startMin == null && endMin == null && breakMin == 0 && subtractBreak && note.isBlank()

    val hasTimes: Boolean
        get() = startMin != null && endMin != null

    val workedMinutes: Int
        get() {
            val s = startMin ?: return 0
            val e = endMin ?: return 0
            var diff = e - s
            if (diff < 0) diff += MINUTES_PER_DAY
            val net = if (subtractBreak) diff - breakMin else diff
            return net.coerceAtLeast(0)
        }
}

data class WeekSummary(
    val days: List<DayEntry>,
    val dayMinutes: List<Int>,
    val dayCents: List<Long>,
    val totalMinutes: Int,
    val grossCents: Long,
    val taxCents: Long,
    val netCents: Long,
    val daysWorked: Int
)

fun weekStartOf(date: LocalDate): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun payCents(minutes: Int, rate: Double): Long =
    Math.round(minutes * rate * 100.0 / 60.0)

fun buildWeek(weekStart: LocalDate, entries: Map<LocalDate, DayEntry>, settings: Settings): WeekSummary {
    val days = (0L..6L).map { offset ->
        val date = weekStart.plusDays(offset)
        entries[date] ?: DayEntry(date)
    }
    val minutes = days.map { it.workedMinutes }
    val cents = minutes.map { payCents(it, settings.hourlyRate) }
    val gross = cents.sum()
    val tax = Math.round(gross * settings.taxRate / 100.0)
    return WeekSummary(
        days = days,
        dayMinutes = minutes,
        dayCents = cents,
        totalMinutes = minutes.sum(),
        grossCents = gross,
        taxCents = tax,
        netCents = gross - tax,
        daysWorked = minutes.count { it > 0 }
    )
}

// ---------- formatting ----------

fun two(n: Int): String = String.format(Locale.ROOT, "%02d", n)

/** 24-hour clock, e.g. 13:30 */
fun formatClock(totalMinutes: Int): String = two(totalMinutes / 60) + ":" + two(totalMinutes % 60)

/** e.g. 10h 25m */
fun formatDuration(totalMinutes: Int): String = "${totalMinutes / 60}h ${totalMinutes % 60}m"

fun formatMoney(cents: Long, currency: String): String =
    currency + String.format(Locale.getDefault(), "%.2f", cents / 100.0)

fun formatNumber(value: Double): String =
    if (value == Math.floor(value)) {
        value.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", value).trimEnd('0').trimEnd(',', '.')
    }

fun parseDecimal(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0.0 }

fun shortDay(date: LocalDate): String =
    date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

fun weekRangeLabel(weekStart: LocalDate): String {
    val end = weekStart.plusDays(6)
    val locale = Locale.getDefault()
    val noYear = DateTimeFormatter.ofPattern("MMM d", locale)
    val withYear = DateTimeFormatter.ofPattern("MMM d, yyyy", locale)
    return if (weekStart.year == end.year) {
        weekStart.format(noYear) + " - " + end.format(withYear)
    } else {
        weekStart.format(withYear) + " - " + end.format(withYear)
    }
}

fun fullDateLabel(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.getDefault()))

fun buildReportText(weekStart: LocalDate, week: WeekSummary, settings: Settings): String {
    val sb = StringBuilder()
    sb.appendLine("Hours Report")
    sb.appendLine(weekRangeLabel(weekStart))
    sb.appendLine()
    week.days.forEachIndexed { i, day ->
        if (!day.hasTimes) {
            sb.appendLine(shortDay(day.date) + "  --")
        } else {
            val start = formatClock(day.startMin ?: 0)
            val end = formatClock(day.endMin ?: 0)
            sb.appendLine(
                shortDay(day.date) + "  " + start + " - " + end + "  " +
                    formatDuration(week.dayMinutes[i]).padStart(8) + "  " +
                    formatMoney(week.dayCents[i], settings.currency)
            )
            if (day.breakMin > 0) {
                val note = if (day.subtractBreak) "" else " (not subtracted)"
                sb.appendLine("     break " + formatClock(day.breakMin) + note)
            }
        }
    }
    sb.appendLine()
    sb.appendLine("Total hours: " + formatDuration(week.totalMinutes))
    sb.appendLine("Total pay: " + formatMoney(week.grossCents, settings.currency))
    if (settings.taxRate > 0.0) {
        sb.appendLine("Tax (" + formatNumber(settings.taxRate) + "%): -" + formatMoney(week.taxCents, settings.currency))
        sb.appendLine("Net pay: " + formatMoney(week.netCents, settings.currency))
    }
    sb.appendLine("Days worked: " + week.daysWorked)
    val notes = week.days.filter { it.note.isNotBlank() }
    if (notes.isNotEmpty()) {
        sb.appendLine()
        sb.appendLine("Notes:")
        notes.forEach { sb.appendLine(shortDay(it.date) + ": " + it.note.trim()) }
    }
    return sb.toString().trimEnd()
}
