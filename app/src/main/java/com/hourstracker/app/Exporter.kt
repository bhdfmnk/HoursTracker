package com.hourstracker.app

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Builds the week report as a PDF, CSV or Excel (.xlsx) file, entirely on the device. */
object Exporter {
    const val MIME_PDF = "application/pdf"
    const val MIME_CSV = "text/csv"
    const val MIME_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

    fun baseName(weekStart: LocalDate): String =
        "HoursReport_" + weekStart.toString() + "_" + weekStart.plusDays(6).toString()

    private class ExportRow(
        val date: LocalDate,
        val day: String,
        val start: String,
        val end: String,
        val brk: String,
        val minutes: Int,
        val hasTimes: Boolean,
        val cents: Long,
        val note: String
    )

    private fun rowsOf(week: WeekSummary, settings: Settings, s: Strings): List<ExportRow> =
        week.days.mapIndexed { i, d ->
            ExportRow(
                date = d.date,
                day = s.dayShort(d.date.dayOfWeek),
                start = d.startMin?.let { formatClock(it, settings.use12h) } ?: "",
                end = d.endMin?.let { formatClock(it, settings.use12h) } ?: "",
                brk = if (d.breakMin > 0) formatHm(d.breakMin) else "",
                minutes = week.dayMinutes[i],
                hasTimes = d.hasTimes,
                cents = week.dayCents[i],
                note = d.note.trim()
            )
        }

    private fun money(cents: Long): String = String.format(Locale.ROOT, "%.2f", cents / 100.0)
    private fun decimalHours(minutes: Int): String = String.format(Locale.ROOT, "%.2f", minutes / 60.0)
    private fun preciseHours(minutes: Int): String = String.format(Locale.ROOT, "%.6f", minutes / 60.0)

    // ------------------------------------------------------------------ CSV

    private fun csvCell(value: String): String {
        val needsQuotes = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuotes) "\"" + value.replace("\"", "\"\"") + "\"" else value
    }

    fun csv(weekStart: LocalDate, week: WeekSummary, settings: Settings, s: Strings): ByteArray {
        val sb = StringBuilder()
        sb.append('﻿') // byte order mark: Excel then reads the file as UTF-8

        fun line(vararg cells: String) {
            sb.append(cells.joinToString(",") { csvCell(it) }).append("\r\n")
        }

        line(
            s.exportDate, s.colDay, s.colStart, s.colEnd, s.colBreak,
            s.hoursHm, s.hoursDecimal, s.colPay + " (" + settings.currency + ")", s.note
        )
        rowsOf(week, settings, s).forEach { r ->
            line(
                r.date.toString(), r.day, r.start, r.end, r.brk,
                if (r.hasTimes) formatHm(r.minutes) else "",
                if (r.hasTimes) decimalHours(r.minutes) else "",
                if (r.hasTimes) money(r.cents) else "",
                r.note
            )
        }
        line(
            s.total, "", "", "", "",
            formatHm(week.totalMinutes), decimalHours(week.totalMinutes), money(week.grossCents), ""
        )
        if (settings.taxActive) {
            line(
                s.tax + " (" + formatNumber(settings.taxRate, s) + "%)", "", "", "", "",
                "", "", "-" + money(week.taxCents), ""
            )
            line(s.netPay, "", "", "", "", "", "", money(week.netCents), "")
        }
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    // ----------------------------------------------------------------- XLSX

    private fun xmlEscape(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            when {
                ch == '&' -> sb.append("&amp;")
                ch == '<' -> sb.append("&lt;")
                ch == '>' -> sb.append("&gt;")
                ch == '"' -> sb.append("&quot;")
                ch < ' ' && ch != '\n' && ch != '\t' -> Unit // characters XML does not allow
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private const val XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    private const val NS_MAIN = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
    private const val NS_REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
    private const val NS_PKG_REL = "http://schemas.openxmlformats.org/package/2006/relationships"

    private const val CONTENT_TYPES = XML_HEAD +
        "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
        "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
        "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
        "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
        "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
        "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
        "</Types>"

    private const val ROOT_RELS = XML_HEAD +
        "<Relationships xmlns=\"" + NS_PKG_REL + "\">" +
        "<Relationship Id=\"rId1\" Type=\"" + NS_REL + "/officeDocument\" Target=\"xl/workbook.xml\"/>" +
        "</Relationships>"

    private const val WORKBOOK = XML_HEAD +
        "<workbook xmlns=\"" + NS_MAIN + "\" xmlns:r=\"" + NS_REL + "\">" +
        "<sheets><sheet name=\"Hours\" sheetId=\"1\" r:id=\"rId1\"/></sheets>" +
        "<calcPr fullCalcOnLoad=\"1\"/>" +
        "</workbook>"

    private const val WORKBOOK_RELS = XML_HEAD +
        "<Relationships xmlns=\"" + NS_PKG_REL + "\">" +
        "<Relationship Id=\"rId1\" Type=\"" + NS_REL + "/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
        "<Relationship Id=\"rId2\" Type=\"" + NS_REL + "/styles\" Target=\"styles.xml\"/>" +
        "</Relationships>"

    // style 0 = normal, 1 = bold, 2 = number 0.00, 3 = bold number 0.00
    private const val STYLES = XML_HEAD +
        "<styleSheet xmlns=\"" + NS_MAIN + "\">" +
        "<fonts count=\"2\">" +
        "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
        "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
        "</fonts>" +
        "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills>" +
        "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>" +
        "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
        "<cellXfs count=\"4\">" +
        "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" +
        "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>" +
        "<xf numFmtId=\"2\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>" +
        "<xf numFmtId=\"2\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\"/>" +
        "</cellXfs>" +
        "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
        "</styleSheet>"

    private fun colName(index: Int): String = ('A' + index).toString()

    private fun textCell(col: Int, row: Int, text: String, style: Int = 0): String =
        if (text.isEmpty()) {
            ""
        } else {
            "<c r=\"" + colName(col) + row + "\" s=\"" + style + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">" +
                xmlEscape(text) + "</t></is></c>"
        }

    private fun numberCell(col: Int, row: Int, value: String, style: Int): String =
        "<c r=\"" + colName(col) + row + "\" s=\"" + style + "\"><v>" + value + "</v></c>"

    private fun formulaCell(col: Int, row: Int, formula: String, cached: String, style: Int): String =
        "<c r=\"" + colName(col) + row + "\" s=\"" + style + "\"><f>" + formula + "</f><v>" + cached + "</v></c>"

    private fun rowXml(row: Int, cells: List<String>): String =
        "<row r=\"" + row + "\">" + cells.joinToString("") + "</row>"

    fun xlsx(weekStart: LocalDate, week: WeekSummary, settings: Settings, s: Strings): ByteArray {
        val rows = rowsOf(week, settings, s)
        val sheetRows = StringBuilder()

        // row 1: title
        sheetRows.append(
            rowXml(1, listOf(textCell(0, 1, s.appTitle + " - " + weekRangeLabel(weekStart, s), 1)))
        )

        // row 3: column headers
        val headers = listOf(
            s.exportDate, s.colDay, s.colStart, s.colEnd, s.colBreak,
            s.hoursHm, s.hoursDecimal, s.colPay + " (" + settings.currency + ")", s.note
        )
        sheetRows.append(rowXml(3, headers.mapIndexed { i, h -> textCell(i, 3, h, 1) }))

        // rows 4..10: the seven days
        val firstDay = 4
        rows.forEachIndexed { i, r ->
            val n = firstDay + i
            val cells = mutableListOf<String>()
            cells.add(textCell(0, n, r.date.toString()))
            cells.add(textCell(1, n, r.day))
            cells.add(textCell(2, n, r.start))
            cells.add(textCell(3, n, r.end))
            cells.add(textCell(4, n, r.brk))
            if (r.hasTimes) {
                cells.add(textCell(5, n, formatHm(r.minutes)))
                cells.add(numberCell(6, n, preciseHours(r.minutes), 2))
                cells.add(numberCell(7, n, money(r.cents), 2))
            }
            cells.add(textCell(8, n, r.note))
            sheetRows.append(rowXml(n, cells))
        }
        val lastDay = firstDay + rows.size - 1

        // totals: sums are real formulas, so the sheet stays correct if cells are edited
        val totalRow = lastDay + 1
        sheetRows.append(
            rowXml(
                totalRow,
                listOf(
                    textCell(0, totalRow, s.total, 1),
                    textCell(5, totalRow, formatHm(week.totalMinutes), 1),
                    formulaCell(6, totalRow, "SUM(G" + firstDay + ":G" + lastDay + ")", preciseHours(week.totalMinutes), 3),
                    formulaCell(7, totalRow, "SUM(H" + firstDay + ":H" + lastDay + ")", money(week.grossCents), 3)
                )
            )
        )
        if (settings.taxActive) {
            val taxRow = totalRow + 1
            val netRow = totalRow + 2
            sheetRows.append(
                rowXml(
                    taxRow,
                    listOf(
                        textCell(0, taxRow, s.tax + " (" + formatNumber(settings.taxRate, s) + "%)", 1),
                        numberCell(7, taxRow, "-" + money(week.taxCents), 2)
                    )
                )
            )
            sheetRows.append(
                rowXml(
                    netRow,
                    listOf(
                        textCell(0, netRow, s.netPay, 1),
                        formulaCell(7, netRow, "H" + totalRow + "+H" + taxRow, money(week.netCents), 3)
                    )
                )
            )
        }

        val widths = listOf(12, 8, 11, 11, 9, 13, 16, 14, 40)
        val cols = StringBuilder("<cols>")
        widths.forEachIndexed { i, w ->
            cols.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                .append("\" width=\"").append(w).append("\" customWidth=\"1\"/>")
        }
        cols.append("</cols>")

        val sheet = XML_HEAD +
            "<worksheet xmlns=\"" + NS_MAIN + "\">" +
            cols.toString() +
            "<sheetData>" + sheetRows.toString() + "</sheetData>" +
            "</worksheet>"

        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun put(name: String, content: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            put("[Content_Types].xml", CONTENT_TYPES)
            put("_rels/.rels", ROOT_RELS)
            put("xl/workbook.xml", WORKBOOK)
            put("xl/_rels/workbook.xml.rels", WORKBOOK_RELS)
            put("xl/styles.xml", STYLES)
            put("xl/worksheets/sheet1.xml", sheet)
        }
        return out.toByteArray()
    }

    // ------------------------------------------------------------------ PDF

    private fun paint(size: Float, bold: Boolean = false, argb: Int = 0xFF1C1C1E.toInt()): Paint {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.textSize = size
        p.color = argb
        p.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        return p
    }

    private fun wrap(text: String, p: Paint, maxWidth: Float): List<String> {
        val out = mutableListOf<String>()
        for (paragraph in text.split('\n')) {
            var rest = paragraph
            if (rest.isEmpty()) {
                out.add("")
                continue
            }
            while (rest.isNotEmpty()) {
                val fit = p.breakText(rest, true, maxWidth, null)
                if (fit <= 0) break
                var cut = fit
                if (fit < rest.length) {
                    val space = rest.lastIndexOf(' ', fit - 1)
                    if (space > 0) cut = space + 1
                }
                out.add(rest.substring(0, cut).trimEnd())
                rest = rest.substring(cut)
            }
        }
        return out
    }

    fun pdf(weekStart: LocalDate, week: WeekSummary, settings: Settings, s: Strings): ByteArray {
        val rows = rowsOf(week, settings, s)
        val pageW = 595
        val pageH = 842
        val margin = 40f
        val left = margin
        val right = pageW - margin

        val title = paint(22f, true)
        val sub = paint(13f, false, 0xFF5C5C63.toInt())
        val head = paint(11f, true, 0xFF5C5C63.toInt())
        val body = paint(12f)
        val bodyBold = paint(12f, true)
        val green = paint(12f, true, 0xFF1B7F46.toInt())
        val big = paint(14f, true)
        val bigGreen = paint(14f, true, 0xFF1B7F46.toInt())
        val red = paint(12f, true, 0xFFC62828.toInt())
        val rule = paint(1f, false, 0xFFB0B0B5.toInt())

        val doc = PdfDocument()
        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber).create())
        var canvas = page.canvas
        var y = 56f

        fun ensure(space: Float) {
            if (y + space > pageH - margin) {
                doc.finishPage(page)
                pageNumber += 1
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber).create())
                canvas = page.canvas
                y = margin + 16f
            }
        }

        fun textAt(text: String, x: Float, p: Paint) {
            canvas.drawText(text, x, y, p)
        }

        fun textRight(text: String, xRight: Float, p: Paint) {
            canvas.drawText(text, xRight - p.measureText(text), y, p)
        }

        fun hline() {
            canvas.drawLine(left, y, right, y, rule)
        }

        // column positions
        val colStart = 110f
        val colEnd = 190f
        val colBreak = 275f
        val colHours = 430f
        val colPay = right

        textAt(s.appTitle, left, title)
        y += 22f
        textAt(weekRangeLabel(weekStart, s), left, sub)
        y += 28f

        hline()
        y += 18f
        textAt(s.colDay, left, head)
        textAt(s.colStart, colStart, head)
        textAt(s.colEnd, colEnd, head)
        textAt(s.colBreak, colBreak, head)
        textRight(s.colHours, colHours, head)
        textRight(s.colPay, colPay, head)
        y += 8f
        hline()

        rows.forEach { r ->
            ensure(30f)
            y += 22f
            textAt(r.day + " " + r.date.dayOfMonth, left, body)
            textAt(if (r.hasTimes) r.start else "--", colStart, body)
            textAt(if (r.hasTimes) r.end else "--", colEnd, body)
            textAt(if (r.brk.isNotEmpty()) r.brk else "--", colBreak, body)
            textRight(if (r.hasTimes) formatDuration(r.minutes, s) else "--", colHours, body)
            textRight(if (r.hasTimes) formatMoney(r.cents, settings.currency, s) else "--", colPay, green)
        }

        y += 10f
        hline()
        ensure(110f)
        y += 24f
        textAt(s.totalHours, left, big)
        textRight(formatDuration(week.totalMinutes, s), colPay, big)
        y += 22f
        textAt(s.totalPay, left, big)
        textRight(formatMoney(week.grossCents, settings.currency, s), colPay, bigGreen)
        if (settings.taxActive) {
            y += 20f
            textAt(s.tax + " (" + formatNumber(settings.taxRate, s) + "%)", left, body)
            textRight("-" + formatMoney(week.taxCents, settings.currency, s), colPay, red)
            y += 20f
            textAt(s.netPay, left, bodyBold)
            textRight(formatMoney(week.netCents, settings.currency, s), colPay, green)
        }
        y += 20f
        textAt(s.daysWorked, left, body)
        textRight(week.daysWorked.toString(), colPay, body)

        val withNotes = rows.filter { it.note.isNotEmpty() }
        if (withNotes.isNotEmpty()) {
            y += 14f
            ensure(60f)
            hline()
            y += 24f
            textAt(s.notes, left, big)
            withNotes.forEach { r ->
                val lines = wrap(r.note, body, right - colStart)
                ensure(24f)
                y += 22f
                textAt(r.day, left, bodyBold)
                lines.forEachIndexed { i, ln ->
                    if (i > 0) {
                        ensure(18f)
                        y += 16f
                    }
                    textAt(ln, colStart - 30f, body)
                }
            }
        }

        doc.finishPage(page)
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }
}
