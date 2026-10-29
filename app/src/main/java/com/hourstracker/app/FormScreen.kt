package com.hourstracker.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

private sealed interface FormDialog {
    data class Time(val date: LocalDate, val isStart: Boolean) : FormDialog
    data object Rate : FormDialog
    data object Tax : FormDialog
}

@Composable
fun FormScreen(vm: TrackerViewModel, onOpenDay: (LocalDate) -> Unit) {
    val settings = vm.settings
    val week = buildWeek(vm.weekStart, vm.entries, settings)
    var dialog by remember { mutableStateOf<FormDialog?>(null) }
    val green = AppColors.payGreen
    val muted = AppColors.muted
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        WeekNavigator(vm)

        // table header
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            HeaderCell("Day", 0.15f)
            HeaderCell("Start", 0.2f)
            HeaderCell("End", 0.2f)
            HeaderCell("Time", 0.225f)
            HeaderCell("Pay", 0.225f)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        week.days.forEachIndexed { i, day ->
            val minutes = week.dayMinutes[i]
            val cents = week.dayCents[i]
            Row(Modifier.fillMaxWidth().height(60.dp), verticalAlignment = Alignment.CenterVertically) {
                Cell(0.15f, onClick = { onOpenDay(day.date) }) {
                    Text(shortDay(day.date), color = primary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Cell(0.2f, onClick = { dialog = FormDialog.Time(day.date, true) }) {
                    TimeOrSet(day.startMin)
                }
                Cell(0.2f, onClick = { dialog = FormDialog.Time(day.date, false) }) {
                    TimeOrSet(day.endMin)
                }
                Cell(0.225f) {
                    Text(
                        formatDuration(minutes),
                        color = if (minutes > 0) primary else muted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                }
                Cell(0.225f) {
                    Text(
                        formatMoney(cents, settings.currency),
                        color = if (cents > 0) green else muted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Spacer(Modifier.height(16.dp))

        SettingCard("Hourly Rate:") {
            Pill(
                text = if (settings.hourlyRate > 0.0) {
                    settings.currency + formatNumber(settings.hourlyRate) + "/hr"
                } else {
                    "Set"
                },
                onClick = { dialog = FormDialog.Rate }
            )
        }
        SettingCard("Tax Rate:") {
            Pill(
                text = if (settings.taxRate > 0.0) formatNumber(settings.taxRate) + "%" else "Set",
                onClick = { dialog = FormDialog.Tax }
            )
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TotalsBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TotalRow("Total Hours:", formatDuration(week.totalMinutes), TotalsBlue)
            TotalRow("Total Pay:", formatMoney(week.grossCents, settings.currency), TotalsGreen)
            if (settings.taxRate > 0.0) {
                TotalRow(
                    "Tax (" + formatNumber(settings.taxRate) + "%):",
                    "-" + formatMoney(week.taxCents, settings.currency),
                    Color(0xFFFFB4A9)
                )
                TotalRow("Net Pay:", formatMoney(week.netCents, settings.currency), TotalsGreen)
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    when (val d = dialog) {
        is FormDialog.Time -> {
            val entry = vm.entryFor(d.date)
            TimeDialog(
                title = if (d.isStart) "Set Start Time" else "Set End Time",
                initialMinutes = if (d.isStart) entry.startMin else entry.endMin,
                hint = "Example: 09:30 or 17:45",
                onSave = { minutes ->
                    vm.update(d.date) { e ->
                        if (d.isStart) e.copy(startMin = minutes) else e.copy(endMin = minutes)
                    }
                    dialog = null
                },
                onReset = {
                    vm.update(d.date) { e ->
                        if (d.isStart) e.copy(startMin = null) else e.copy(endMin = null)
                    }
                    dialog = null
                },
                onDismiss = { dialog = null }
            )
        }
        FormDialog.Rate -> RateDialog(vm) { dialog = null }
        FormDialog.Tax -> TaxDialog(vm) { dialog = null }
        null -> Unit
    }
}

@Composable
private fun WeekNavigator(vm: TrackerViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { vm.shiftWeek(-1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous week")
        }
        Text(
            text = weekRangeLabel(vm.weekStart),
            modifier = Modifier
                .weight(1f)
                .clickable { vm.goToCurrentWeek() },
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        IconButton(onClick = { vm.shiftWeek(1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next week")
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Cell(weight) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

@Composable
private fun RowScope.Cell(
    weight: Float,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val base = Modifier.weight(weight).fillMaxHeight()
    Box(
        modifier = if (onClick != null) base.clickable(onClick = onClick) else base,
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun TimeOrSet(minutes: Int?) {
    if (minutes != null) {
        Text(formatClock(minutes), fontSize = 15.sp, fontWeight = FontWeight.Medium)
    } else {
        Text(
            "Set",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun SettingCard(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        content()
    }
}

@Composable
private fun TotalRow(label: String, value: String, valueColor: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
