package com.hourstracker.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import java.time.LocalDate

class TrackerViewModel(app: Application) : AndroidViewModel(app) {
    private val storage = Storage(app)

    var settings by mutableStateOf(storage.loadSettings())
        private set

    var entries by mutableStateOf(storage.loadEntries())
        private set

    /** Monday of the week that is currently shown. */
    var weekStart by mutableStateOf(weekStartOf(LocalDate.now()))
        private set

    fun shiftWeek(weeks: Long) {
        weekStart = weekStart.plusWeeks(weeks)
    }

    fun goToCurrentWeek() {
        weekStart = weekStartOf(LocalDate.now())
    }

    fun entryFor(date: LocalDate): DayEntry = entries[date] ?: DayEntry(date)

    fun update(date: LocalDate, change: (DayEntry) -> DayEntry) {
        val updated = change(entryFor(date))
        entries = if (updated.isEmpty) entries - date else entries + (date to updated)
        storage.saveEntries(entries)
    }

    fun setHourlyRate(value: Double) {
        settings = settings.copy(hourlyRate = value)
        storage.saveSettings(settings)
    }

    fun setTaxRate(value: Double) {
        settings = settings.copy(taxRate = value)
        storage.saveSettings(settings)
    }

    fun setCurrency(value: String) {
        settings = settings.copy(currency = value)
        storage.saveSettings(settings)
    }
}
