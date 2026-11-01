package com.hourstracker.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

private enum class DayDialog { Start, End, Break, Multiplier }

@Composable
fun DayDetailScreen(vm: TrackerViewModel, date: LocalDate, onBack: () -> Unit) {
    val s = LocalStrings.current
    val palette = LocalPalette.current
    val scheme = MaterialTheme.colorScheme
    val settings = vm.settings
    val entry = vm.entryFor(date)
    val minutes = entry.workedMinutes
    val cents = dayPayCents(entry, settings)
    val multiplier = rateMultiplier(entry, settings)
    val multiplierNote = when {
        entry.rateMultiplier != null -> s.multCustom
        isWeekend(date) -> s.multWeekend
        else -> s.multStandard
    }
    var dialog by remember { mutableStateOf<DayDialog?>(null) }
    val timeMode = if (settings.use12h) TimeMode.Clock12 else TimeMode.Clock24
    val timeHint = if (settings.use12h) s.hint12 else s.hint24
    val empty = "--:--"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // back arrow, short day name and the full date
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = s.back,
                    tint = scheme.primary
                )
            }
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = s.dayShort(date.dayOfWeek),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = fullDateLabel(date, s),
                    color = scheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }

        SectionHeader(
            text = s.workHours,
            trailing = {
                Text(
                    text = s.reset,
                    color = scheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .clickable(role = Role.Button) {
                            vm.update(date) { e ->
                                e.copy(startMin = null, endMin = null, breakMin = 0, subtractBreak = true)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        )

        GroupCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(s.colStart, fontSize = 15.sp, color = scheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Pill(
                        text = entry.startMin?.let { formatClock(it, settings.use12h) } ?: empty,
                        onClick = { dialog = DayDialog.Start },
                        modifier = Modifier.fillMaxWidth(),
                        large = true
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(s.colEnd, fontSize = 15.sp, color = scheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Pill(
                        text = entry.endMin?.let { formatClock(it, settings.use12h) } ?: empty,
                        onClick = { dialog = DayDialog.End },
                        modifier = Modifier.fillMaxWidth(),
                        large = true
                    )
                }
            }
            RowDivider()
            SettingRow(s.breakTime) {
                Pill(
                    text = if (entry.breakMin > 0) formatHm(entry.breakMin) else s.set,
                    onClick = { dialog = DayDialog.Break }
                )
            }
            RowDivider()
            SettingRow(s.subtractBreak) {
                Switch(
                    checked = entry.subtractBreak,
                    onCheckedChange = { checked ->
                        vm.update(date) { e -> e.copy(subtractBreak = checked) }
                    }
                )
            }
            RowDivider()
            SettingRow(s.rateMultiplier, subtitle = multiplierNote) {
                Pill(
                    text = multiplierLabel(multiplier, s),
                    onClick = { dialog = DayDialog.Multiplier }
                )
            }
            RowDivider()
            SettingRow(s.hoursWorked) {
                Text(
                    text = formatDuration(minutes, s),
                    color = palette.payGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
            RowDivider()
            SettingRow(s.earned) {
                Text(
                    text = formatMoney(cents, settings.currency, s),
                    color = palette.payGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }

        SectionHeader(text = s.notes)
        GroupCard {
            OutlinedTextField(
                value = entry.note,
                onValueChange = { text -> vm.update(date) { e -> e.copy(note = text) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp)
                    .padding(12.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
    }

    when (dialog) {
        DayDialog.Start -> TimeDialog(
            title = s.setStartTime,
            mode = timeMode,
            initialMinutes = entry.startMin,
            hint = timeHint,
            onSave = { m ->
                vm.update(date) { e -> e.copy(startMin = m) }
                dialog = null
            },
            onReset = {
                vm.update(date) { e -> e.copy(startMin = null) }
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        DayDialog.End -> TimeDialog(
            title = s.setEndTime,
            mode = timeMode,
            initialMinutes = entry.endMin,
            hint = timeHint,
            onSave = { m ->
                vm.update(date) { e -> e.copy(endMin = m) }
                dialog = null
            },
            onReset = {
                vm.update(date) { e -> e.copy(endMin = null) }
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        DayDialog.Break -> TimeDialog(
            title = s.breakTime,
            mode = TimeMode.Duration,
            initialMinutes = entry.breakMin.takeIf { it > 0 },
            hint = s.hintBreak,
            onSave = { m ->
                vm.update(date) { e -> e.copy(breakMin = m) }
                dialog = null
            },
            onReset = {
                vm.update(date) { e -> e.copy(breakMin = 0) }
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        DayDialog.Multiplier -> ValueDialog(
            title = s.rateMultiplier,
            label = s.dayMultiplierLabel,
            initial = formatNumber(multiplier, s),
            keyboardType = KeyboardType.Decimal,
            maxLength = 5,
            onSave = { text ->
                val value = parseDecimal(text)
                if (value != null && value > 0.0 && value <= 10.0) {
                    vm.update(date) { e -> e.copy(rateMultiplier = value) }
                }
                dialog = null
            },
            onReset = {
                vm.update(date) { e -> e.copy(rateMultiplier = null) }
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        null -> Unit
    }
}
