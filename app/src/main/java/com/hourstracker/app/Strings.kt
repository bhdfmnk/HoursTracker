package com.hourstracker.app

import java.time.DayOfWeek
import java.util.Locale

data class CurrencyOption(val code: String, val symbol: String, val name: String)

/** All user-visible text in two languages. uk = true -> Ukrainian, otherwise English. */
class Strings(val uk: Boolean) {
    private fun p(en: String, ukr: String): String = if (uk) ukr else en

    val locale: Locale = if (uk) Locale.forLanguageTag("uk-UA") else Locale.ENGLISH

    // --- general
    val appTitle = p("Hours Report", "Звіт годин")
    val tabSettings = p("Settings", "Налаштування")
    val tabForm = p("Form", "Форма")
    val tabPreview = p("Preview", "Перегляд")
    val tabShare = p("Share", "Поділитися")
    val set = p("Set", "Задати")
    val reset = p("Reset", "Скинути")
    val cancel = p("Cancel", "Скасувати")
    val save = p("Save", "Зберегти")
    val back = p("Back", "Назад")
    val prevWeek = p("Previous week", "Попередній тиждень")
    val nextWeek = p("Next week", "Наступний тиждень")

    // --- units and table
    val hourUnit = p("h", "г")
    val minUnit = p("m", "хв")
    val perHour = p("/hr", "/год")
    val colDay = p("Day", "День")
    val colStart = p("Start", "Початок")
    val colEnd = p("End", "Кінець")
    val colTime = p("Time", "Час")
    val colPay = p("Pay", "Оплата")
    val colHours = p("Hours", "Години")
    val colBreak = p("Break", "Перерва")

    // --- pay
    val hourlyRate = p("Hourly Rate", "Погодинна ставка")
    val taxRate = p("Tax Rate", "Податок")
    val totalHours = p("Total Hours", "Усього годин")
    val totalPay = p("Total Pay", "Усього оплата")
    val tax = p("Tax", "Податок")
    val netPay = p("Net Pay", "Після податку")
    val daysWorked = p("Days Worked", "Відпрацьовано днів")
    val total = p("Total", "Разом")

    // --- time dialogs
    val setStartTime = p("Set Start Time", "Час початку")
    val setEndTime = p("Set End Time", "Час закінчення")
    val breakTime = p("Break Time", "Перерва")
    val enterTime = p("Enter time:", "Введіть час:")
    val hint24 = p("Example: 09:30 or 17:45", "Приклад: 09:30 або 17:45")
    val hint12 = p("Example: 9:30 AM or 5:45 PM", "Приклад: 9:30 AM або 5:45 PM")
    val hintBreak = p("Break length, hh:mm. Example: 00:30", "Тривалість перерви, гг:хв. Приклад: 00:30")
    val hh = p("HH", "ГГ")
    val mm = p("MM", "ХВ")

    // --- day detail
    val workHours = p("Work Hours", "Робочі години")
    val subtractBreak = p("Subtract break from hours", "Відняти перерву від годин")
    val hoursWorked = p("Hours Worked", "Відпрацьовано")
    val earned = p("Earned", "Зароблено")
    val notes = p("Notes", "Нотатки")
    val note = p("Note", "Нотатка")

    // --- share and export
    val shareIntro = p(
        "Send this week's report as text, or export it as a file.",
        "Надішліть звіт за тиждень текстом або експортуйте його у файл."
    )
    val shareText = p("Share text", "Надіслати текстом")
    val copy = p("Copy", "Копіювати")
    val copied = p("Copied", "Скопійовано")
    val exportTitle = p("Export", "Експорт")
    val fileShare = p("Share", "Надіслати")
    val fileSave = p("Save", "Зберегти")
    val saved = p("Saved", "Збережено")
    val exportFailed = p("Could not create the file", "Не вдалося створити файл")
    val chooserTitle = p("Share report", "Надіслати звіт")
    val exportDate = p("Date", "Дата")
    val hoursHm = p("Hours (h:mm)", "Години (г:хв)")
    val hoursDecimal = p("Hours (decimal)", "Години (десяткові)")
    val fileExcel = "Excel (.xlsx)"
    val filePdf = "PDF"
    val fileCsv = "CSV"

    // --- weekend rate / per-day multiplier
    val weekendRate = p("Weekend rate", "Ставка на вихідних")
    val weekendSubtitle = p("Saturday and Sunday", "Субота й неділя")
    val multiplierLabel = p("Multiplier (1 = same rate)", "Коефіцієнт (1 = та сама ставка)")
    val rateMultiplier = p("Rate multiplier", "Коефіцієнт ставки")
    val dayMultiplierLabel = p("Multiplier for this day", "Коефіцієнт для цього дня")
    val multStandard = p("Standard rate", "Звичайна ставка")
    val multWeekend = p("Weekend default", "За замовчуванням для вихідних")
    val multCustom = p("Set for this day only", "Задано лише для цього дня")
    val rateX = p("Rate ×", "Ставка ×")

    // --- start / end shift button
    val today = p("Today", "Сьогодні")
    val yesterday = p("yesterday", "учора")
    val punchStart = p("Start shift", "Почати зміну")
    val punchEnd = p("End shift", "Завершити зміну")
    val punchDone = p("Recorded", "Записано")
    val punchNotStarted = p("Not started", "Не розпочато")
    val punchStartedAt = p("Started", "Початок")
    val punchHint = p(
        "Tap: now. Double-tap or hold: enter manually.",
        "Дотик: зараз. Подвійний дотик або утримання: ввести вручну."
    )
    val punchStarted = p("Shift started at", "Зміну розпочато о")
    val punchEnded = p("Shift ended at", "Зміну завершено о")
    val punchAlready = p(
        "Today's shift is already recorded. Double-tap or hold the button to edit the end time.",
        "Зміну за сьогодні вже записано. Подвійний дотик або утримання відкриє редагування часу закінчення."
    )

    // --- settings
    val secPay = p("Pay", "Оплата")
    val secTime = p("Time", "Час")
    val secAppearance = p("Appearance", "Вигляд")
    val secLanguage = p("Language", "Мова")
    val currency = p("Currency", "Валюта")
    val currencyTitle = p("Currency symbol", "Символ валюти")
    val customSymbol = p("Custom symbol", "Власний символ")
    val customSymbolHint = p("Symbol, up to 4 characters", "Символ, до 4 знаків")
    val showTax = p("Show tax on main screen", "Показувати податок на головному екрані")
    val timeFormat = p("Time format", "Формат часу")
    val fmt24 = p("24-hour", "24 години")
    val fmt12 = "AM / PM"
    val mode = p("Mode", "Режим")
    val modeSystem = p("System", "Системний")
    val modeLight = p("Light", "Світлий")
    val modeDark = p("Dark", "Темний")
    val lightTheme = p("Light theme", "Світла тема")
    val darkTheme = p("Dark theme", "Темна тема")
    val themeClassic = p("Classic", "Класика")
    val themeMint = p("Mint", "М'ята")
    val themeMidnight = p("Midnight", "Північ")
    val themeBlack = p("Black", "Чорна")
    val rateTitle = p("Hourly Rate", "Погодинна ставка")
    val taxTitle = p("Tax Rate", "Податок")
    val taxLabel = p("Tax, % of pay", "Податок, % від оплати")
    val settingsNote = p(
        "The hourly rate is saved on this device and applied to every day of every week. " +
            "On Saturdays and Sundays it is multiplied by the weekend rate (1.5 by default); " +
            "a single day can have its own multiplier. " +
            "Weeks run from Monday to Sunday. If a shift ends earlier than it starts, " +
            "it is counted as ending after midnight.",
        "Погодинна ставка зберігається на пристрої й застосовується до кожного дня кожного тижня. " +
            "У суботу й неділю вона множиться на коефіцієнт вихідних (за замовчуванням 1,5); " +
            "для окремого дня можна задати свій коефіцієнт. " +
            "Тиждень іде з понеділка по неділю. Якщо зміна закінчується раніше, ніж починається, " +
            "вона рахується через північ."
    )

    fun rateLabel(symbol: String): String = p("Amount per hour ($symbol)", "Сума за годину ($symbol)")

    val currencies: List<CurrencyOption> = listOf(
        CurrencyOption("USD", "\$", p("US Dollar", "Долар США")),
        CurrencyOption("EUR", "€", p("Euro", "Євро")),
        CurrencyOption("GBP", "£", p("British Pound", "Фунт стерлінгів")),
        CurrencyOption("JPY", "¥", p("Japanese Yen", "Японська єна")),
        CurrencyOption("CNY", "CN¥", p("Chinese Yuan", "Китайський юань")),
        CurrencyOption("AUD", "A\$", p("Australian Dollar", "Австралійський долар")),
        CurrencyOption("CAD", "C\$", p("Canadian Dollar", "Канадський долар")),
        CurrencyOption("CHF", "CHF", p("Swiss Franc", "Швейцарський франк")),
        CurrencyOption("PLN", "zł", p("Polish Zloty", "Польський злотий")),
        CurrencyOption("UAH", "₴", p("Ukrainian Hryvnia", "Українська гривня"))
    )

    // --- dates
    private val daysEn = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val daysUk = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")

    fun dayShort(day: DayOfWeek): String = (if (uk) daysUk else daysEn)[day.value - 1]

    val patternNoYear = if (uk) "d MMM" else "MMM d"
    val patternWithYear = if (uk) "d MMM yyyy" else "MMM d, yyyy"
    val patternFull = if (uk) "EEEE, d MMMM yyyy" else "EEEE, MMMM d, yyyy"
}
