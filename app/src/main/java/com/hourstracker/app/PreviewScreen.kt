package com.hourstracker.app

import androidx.compose.foundation.background
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
    val settings = vm.settings
    val week = buildWeek(vm.weekStart, vm.entries, settings)
    val green = AppColors.payGreen
    val muted = AppColors.muted
    val line = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    weekRangeLabel(vm.weekStart),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = line)
                PreviewRow("Day", "Start", "End", "Hours", "Pay", true, line)
                HorizontalDivider(color = line)
                Spacer(Modifier.height(4.dp))

                week.days.forEachIndexed { i, day ->
                    val has = day.hasTimes
                    PreviewRow(
                        day = shortDay(day.date),
                        start = day.startMin?.let { formatClock(it) } ?: "--",
                        end = day.endMin?.let { formatClock(it) } ?: "--",
                        hours = if (has) formatDuration(week.dayMinutes[i]) else "--",
                        pay = if (has) formatMoney(week.dayCents[i], settings.currency) else "--",
                        bold = false,
                        payColor = if (has) green else muted
                    )
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = line)
                Spacer(Modifier.height(12.dp))

                SummaryRow("TOTAL HOURS:", formatDuration(week.totalMinutes), line, true)
                SummaryRow("TOTAL PAY:", formatMoney(week.grossCents, settings.currency), green, true)
                if (settings.taxRate > 0.0) {
                    SummaryRow(
                        "Tax (" + formatNumber(settings.taxRate) + "%):",
                        "-" + formatMoney(week.taxCents, settings.currency),
                        Color(0xFFD9534F),
                        false
                    )
                    SummaryRow("Net pay:", formatMoney(week.netCents, settings.currency), green, true)
                }
                SummaryRow("Days Worked:", week.daysWorked.toString(), line, false)

                val notes = week.days.filter { it.note.isNotBlank() }
                if (notes.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = line)
                    Spacer(Modifier.height(12.dp))
                    Text("NOTES", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.height(8.dp))
                    notes.forEach { day ->
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text(
                                shortDay(day.date),
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
        Text(day, Modifier.weight(0.13f), fontWeight = weight, fontSize = 15.sp)
        Text(start, Modifier.weight(0.2f), fontWeight = weight, fontSize = 13.sp, textAlign = TextAlign.Center)
        Text(end, Modifier.weight(0.2f), fontWeight = weight, fontSize = 13.sp, textAlign = TextAlign.Center)
        Text(hours, Modifier.weight(0.22f), fontWeight = weight, fontSize = 14.sp, textAlign = TextAlign.End)
        Text(
            pay,
            Modifier.weight(0.25f),
            color = if (bold) Color.Unspecified else payColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.End
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
            label,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (bold) 18.sp else 16.sp
        )
        Text(
            value,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (bold) 18.sp else 16.sp
        )
    }
}
