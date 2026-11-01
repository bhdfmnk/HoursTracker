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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
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
    val s = LocalStrings.current
    val palette = LocalPalette.current
    val scheme = MaterialTheme.colorScheme
    val settings = vm.settings
    val week = buildWeek(vm.weekStart, vm.entries, settings)
    var dialog by remember { mutableStateOf<FormDialog?>(null) }
    val timeMode = if (settings.use12h) TimeMode.Clock12 else TimeMode.Clock24

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Start / end the shift with one tap.
        QuickPunch(vm)

        // The table: header and seven days.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.surface)
        ) {
            HorizontalDivider(color = scheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderCell(s.colDay, 0.14f)
                HeaderCell(s.colStart, 0.215f)
                HeaderCell(s.colEnd, 0.215f)
                HeaderCell(s.colTime, 0.215f)
                HeaderCell(s.colPay, 0.215f)
            }
            HorizontalDivider(color = scheme.outlineVariant)

            week.days.forEachIndexed { i, day ->
                val minutes = week.dayMinutes[i]
                val cents = week.dayCents[i]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Cell(0.14f, onClick = { onOpenDay(day.date) }) {
                        Text(
                            text = s.dayShort(day.date.dayOfWeek),
                            color = scheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Cell(0.215f, onClick = { dialog = FormDialog.Time(day.date, true) }) {
                        TimeOrSet(day.startMin, settings.use12h)
                    }
                    Cell(0.215f, onClick = { dialog = FormDialog.Time(day.date, false) }) {
                        TimeOrSet(day.endMin, settings.use12h)
                    }
                    Cell(0.215f) {
                        Text(
                            text = formatDuration(minutes, s),
                            color = if (minutes > 0) scheme.primary else scheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                    Cell(0.215f) {
                        Text(
                            text = formatMoney(cents, settings.currency, s),
                            color = if (cents > 0) palette.payGreen else scheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
                HorizontalDivider(color = scheme.outlineVariant)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Hourly rate and tax rate: two compact cards side by side.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniCard(
                label = s.hourlyRate,
                value = if (settings.hourlyRate > 0.0) {
                    settings.currency + formatNumber(settings.hourlyRate, s) + s.perHour
                } else {
                    s.set
                },
                onClick = { dialog = FormDialog.Rate },
                modifier = Modifier.weight(1f),
                wide = !settings.showTax
            )
            if (settings.showTax) {
                MiniCard(
                    label = s.taxRate,
                    value = if (settings.taxRate > 0.0) formatNumber(settings.taxRate, s) + "%" else s.set,
                    onClick = { dialog = FormDialog.Tax },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Totals for the week.
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(palette.totalsBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TotalRow(s.totalHours + ":", formatDuration(week.totalMinutes, s), palette.totalsBlue)
            TotalRow(s.totalPay + ":", formatMoney(week.grossCents, settings.currency, s), palette.totalsGreen)
            if (settings.taxActive) {
                TotalRow(
                    s.tax + " (" + formatNumber(settings.taxRate, s) + "%):",
                    "-" + formatMoney(week.taxCents, settings.currency, s),
                    palette.negative
                )
                TotalRow(s.netPay + ":", formatMoney(week.netCents, settings.currency, s), palette.totalsGreen)
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    when (val d = dialog) {
        is FormDialog.Time -> {
            val entry = vm.entryFor(d.date)
            TimeDialog(
                title = if (d.isStart) s.setStartTime else s.setEndTime,
                mode = timeMode,
                initialMinutes = if (d.isStart) entry.startMin else entry.endMin,
                hint = if (settings.use12h) s.hint12 else s.hint24,
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
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Cell(weight) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
    }
}

@Composable
private fun RowScope.Cell(
    weight: Float,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val base = Modifier
        .weight(weight)
        .fillMaxHeight()
    Box(
        modifier = if (onClick != null) base.clickable(role = Role.Button, onClick = onClick) else base,
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun TimeOrSet(minutes: Int?, h12: Boolean) {
    if (minutes != null) {
        Text(
            text = formatClock(minutes, h12),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false
        )
    } else {
        Text(
            text = LocalStrings.current.set,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun TotalRow(label: String, value: String, valueColor: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}
