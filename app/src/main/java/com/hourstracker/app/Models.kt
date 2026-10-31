package com.hourstracker.app

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private const val MINUTES_PER_DAY = 24 * 60

data class Settings(
    val hourlyRate: Double = 0.0,
    val taxRate: Double = 0.0,
    val showTax: Boolean = true,
    val currency: String = "£",
    /** false = 24-hour clock (13:30), true = 12-hour clock with AM/PM (1:30 PM). */
    val use12h: Boolean = false,
    /** "en" or "uk". */
    val language: String = "en",
    /** 0 = follow system, 1 = light, 2 = dark. */
    val themeMode: Int = 0,
    val lightTheme: Int = 0,
    val darkTheme: Int = 0
) {
    /** Tax is shown and subtracted only when it is switched on and greater than zero. */
    val taxActive: Boolean
        get() = showTax && taxRate > 0.0
}

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

fun two(n: Int): String = if (n in 0..9) "0$n" else n.toString()

/** A time of day: "13:30" (24-hour) or "1:30 PM" (12-hour). */
fun formatClock(totalMinutes: Int, h12: Boolean): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    if (!h12) return two(h) + ":" + two(m)
    val suffix = if (h >= 12) "PM" else "AM"
    val shown = if (h % 12 == 0) 12 else h % 12
    return shown.toString() + ":" + two(m) + " " + suffix
}

/** A length of time as hh:mm, e.g. a 30 minute break is "00:30". */
fun formatHm(totalMinutes: Int): String = two(totalMinutes / 60) + ":" + two(totalMinutes % 60)

/** e.g. "10h 25m" */
fun formatDuration(totalMinutes: Int, s: Strings): String =
    (totalMinutes / 60).toString() + s.hourUnit + " " + (totalMinutes % 60).toString() + s.minUnit

fun formatMoney(cents: Long, currency: String, s: Strings): String =
    currency + String.format(s.locale, "%.2f", cents / 100.0)

fun formatNumber(value: Double, s: Strings): String =
    if (value == Math.floor(value)) {
        value.toLong().toString()
    } else {
        String.format(s.locale, "%.2f", value).trimEnd('0').trimEnd(',', '.')
    }

fun parseDecimal(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0.0 }

fun weekRangeLabel(weekStart: LocalDate, s: Strings): String {
    val end = weekStart.plusDays(6)
    val noYear = DateTimeFormatter.ofPattern(s.patternNoYear, s.locale)
    val withYear = DateTimeFormatter.ofPattern(s.patternWithYear, s.locale)
    return if (weekStart.year == end.year) {
        weekStart.format(noYear) + " - " + end.format(withYear)
    } else {
        weekStart.format(withYear) + " - " + end.format(withYear)
    }
}

fun fullDateLabel(date: LocalDate, s: Strings): String =
    date.format(DateTimeFormatter.ofPattern(s.patternFull, s.locale))

fun buildReportText(weekStart: LocalDate, week: WeekSummary, settings: Settings, s: Strings): String {
    val sb = StringBuilder()
    sb.appendLine(s.appTitle)
    sb.appendLine(weekRangeLabel(weekStart, s))
    sb.appendLine()
    week.days.forEachIndexed { i, day ->
        val name = s.dayShort(day.date.dayOfWeek)
        if (!day.hasTimes) {
            sb.appendLine("$name  --")
        } else {
            val start = formatClock(day.startMin ?: 0, settings.use12h)
            val end = formatClock(day.endMin ?: 0, settings.use12h)
            sb.appendLine(
                name + "  " + start + " - " + end + "  " +
                    formatDuration(week.dayMinutes[i], s) + "  " +
                    formatMoney(week.dayCents[i], settings.currency, s)
            )
            if (day.breakMin > 0) {
                val extra = if (day.subtractBreak) "" else " (not subtracted)"
                sb.appendLine("     " + s.colBreak.lowercase(s.locale) + " " + formatHm(day.breakMin) + extra)
            }
        }
    }
    sb.appendLine()
    sb.appendLine(s.totalHours + ": " + formatDuration(week.totalMinutes, s))
    sb.appendLine(s.totalPay + ": " + formatMoney(week.grossCents, settings.currency, s))
    if (settings.taxActive) {
        sb.appendLine(s.tax + " (" + formatNumber(settings.taxRate, s) + "%): -" + formatMoney(week.taxCents, settings.currency, s))
        sb.appendLine(s.netPay + ": " + formatMoney(week.netCents, settings.currency, s))
    }
    sb.appendLine(s.daysWorked + ": " + week.daysWorked)
    val notes = week.days.filter { it.note.isNotBlank() }
    if (notes.isNotEmpty()) {
        sb.appendLine()
        sb.appendLine(s.notes + ":")
        notes.forEach { sb.appendLine(s.dayShort(it.date.dayOfWeek) + ": " + it.note.trim()) }
    }
    return sb.toString().trimEnd()
}
