package com.hourstracker.app

import android.content.Context
import org.json.JSONObject
import java.time.LocalDate

/** Saves settings and day entries on the device (SharedPreferences, JSON). */
class Storage(context: Context) {
    private val prefs = context.getSharedPreferences("hours_tracker", Context.MODE_PRIVATE)

    fun loadSettings(): Settings = Settings(
        hourlyRate = prefs.getString("rate", null)?.toDoubleOrNull() ?: 0.0,
        weekendMultiplier = prefs.getString("weekend_mult", null)?.toDoubleOrNull() ?: 1.5,
        taxRate = prefs.getString("tax", null)?.toDoubleOrNull() ?: 0.0,
        showTax = prefs.getBoolean("show_tax", true),
        currency = prefs.getString("currency", null) ?: "£",
        use12h = prefs.getBoolean("use12h", false),
        language = prefs.getString("lang", null) ?: "en",
        themeMode = prefs.getInt("theme_mode", 0),
        lightTheme = prefs.getInt("theme_light", 0),
        darkTheme = prefs.getInt("theme_dark", 0)
    )

    fun saveSettings(settings: Settings) {
        prefs.edit()
            .putString("rate", settings.hourlyRate.toString())
            .putString("weekend_mult", settings.weekendMultiplier.toString())
            .putString("tax", settings.taxRate.toString())
            .putBoolean("show_tax", settings.showTax)
            .putString("currency", settings.currency)
            .putBoolean("use12h", settings.use12h)
            .putString("lang", settings.language)
            .putInt("theme_mode", settings.themeMode)
            .putInt("theme_light", settings.lightTheme)
            .putInt("theme_dark", settings.darkTheme)
            .apply()
    }

    fun loadEntries(): Map<LocalDate, DayEntry> {
        val raw = prefs.getString("entries", null) ?: return emptyMap()
        return try {
            val root = JSONObject(raw)
            val result = HashMap<LocalDate, DayEntry>()
            for (key in root.keys()) {
                val o = root.getJSONObject(key)
                val date = LocalDate.parse(key)
                result[date] = DayEntry(
                    date = date,
                    startMin = if (o.has("s")) o.getInt("s") else null,
                    endMin = if (o.has("e")) o.getInt("e") else null,
                    breakMin = o.optInt("b", 0),
                    subtractBreak = o.optBoolean("sub", true),
                    note = o.optString("n", ""),
                    rateMultiplier = if (o.has("m")) o.getDouble("m") else null
                )
            }
            result
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveEntries(entries: Map<LocalDate, DayEntry>) {
        val root = JSONObject()
        for ((date, entry) in entries) {
            if (entry.isEmpty) continue
            val o = JSONObject()
            entry.startMin?.let { o.put("s", it) }
            entry.endMin?.let { o.put("e", it) }
            o.put("b", entry.breakMin)
            o.put("sub", entry.subtractBreak)
            o.put("n", entry.note)
            entry.rateMultiplier?.let { o.put("m", it) }
            root.put(date.toString(), o)
        }
        prefs.edit().putString("entries", root.toString()).apply()
    }
}
