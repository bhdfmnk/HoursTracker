package com.hourstracker.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreviewScreen(vm: TrackerViewModel) {
    val s = LocalStrings.current
    val palette = LocalPalette.current
    val scheme = MaterialTheme.colorScheme
    val settings = vm.settings
    val week = buildWeek(vm.weekStart, vm.entries, settings)
    val line = scheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = scheme.surface,
                contentColor = scheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = weekRangeLabel(vm.weekStart, s),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = line)
                PreviewRow(s.colDay, s.colStart, s.colEnd, s.colHours, s.colPay, true, scheme.onSurface)
                HorizontalDivider(color = line)
                Spacer(Modifier.height(4.dp))

                week.days.forEachIndexed { i, day ->
                    val has = day.hasTimes
                    PreviewRow(
                        day = s.dayShort(day.date.dayOfWeek),
                        start = day.startMin?.let { formatClock(it, settings.use12h) } ?: "--",
                        end = day.endMin?.let { formatClock(it, settings.use12h) } ?: "--",
                        hours = if (has) formatDuration(week.dayMinutes[i], s) else "--",
                        pay = if (has) formatMoney(week.dayCents[i], settings.currency, s) else "--",
                        bold = false,
                        payColor = if (has) palette.payGreen else scheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = line)
                Spacer(Modifier.height(12.dp))

                SummaryRow(s.totalHours.uppercase(s.locale) + ":", formatDuration(week.totalMinutes, s), scheme.onSurface, true)
                SummaryRow(s.totalPay.uppercase(s.locale) + ":", formatMoney(week.grossCents, settings.currency, s), palette.payGreen, true)
                if (settings.taxActive) {
                    SummaryRow(
                        s.tax + " (" + formatNumber(settings.taxRate, s) + "%):",
                        "-" + formatMoney(week.taxCents, settings.currency, s),
                        scheme.error,
                        false
                    )
                    SummaryRow(s.netPay + ":", formatMoney(week.netCents, settings.currency, s), palette.payGreen, true)
                }
                SummaryRow(s.daysWorked + ":", week.daysWorked.toString(), scheme.onSurface, false)

                val notes = week.days.filter { it.note.isNotBlank() }
                if (notes.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = line)
                    Spacer(Modifier.height(12.dp))
                    Text(s.notes.uppercase(s.locale), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.height(8.dp))
                    notes.forEach { day ->
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = s.dayShort(day.date.dayOfWeek),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(end = 20.dp)
                            )
                            Text(day.note.trim(), fontSize = 15.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PreviewRow(
    day: String,
    start: String,
    end: String,
    hours: String,
    pay: String,
    bold: Boolean,
    payColor: Color
) {
    val weight = if (bold) FontWeight.Bold else FontWeight.Normal
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(day, Modifier.weight(0.12f), fontWeight = weight, fontSize = 14.sp, maxLines = 1)
        Text(start, Modifier.weight(0.215f), fontWeight = weight, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
        Text(end, Modifier.weight(0.215f), fontWeight = weight, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
        Text(hours, Modifier.weight(0.21f), fontWeight = weight, fontSize = 13.sp, textAlign = TextAlign.End, maxLines = 1, softWrap = false)
        Text(
            text = pay,
            modifier = Modifier.weight(0.24f),
            color = if (bold) Color.Unspecified else payColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color, bold: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (bold) 17.sp else 15.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (bold) 17.sp else 15.sp
        )
    }
}
