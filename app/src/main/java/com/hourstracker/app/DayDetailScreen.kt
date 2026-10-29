package com.hourstracker.app

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

private enum class DayDialog { Start, End, Break }

@Composable
fun DayDetailScreen(vm: TrackerViewModel, date: LocalDate, onBack: () -> Unit) {
    val entry = vm.entryFor(date)
    val settings = vm.settings
    val minutes = entry.workedMinutes
    val cents = payCents(minutes, settings.hourlyRate)
    var dialog by remember { mutableStateOf<DayDialog?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // header: back + day name + full date
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(Modifier.padding(start = 4.dp)) {
                Text(shortDay(date), fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text(
                    fullDateLabel(date),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Work Hours", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(
                "Reset",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable {
                        vm.update(date) { e ->
                            e.copy(startMin = null, endMin = null, breakMin = 0, subtractBreak = true)
                        }
                    }
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Start", fontSize = 17.sp)
                    Spacer(Modifier.height(8.dp))
                    Pill(
                        text = entry.startMin?.let { formatClock(it) } ?: "--:--",
                        onClick = { dialog = DayDialog.Start },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("End", fontSize = 17.sp)
                    Spacer(Modifier.height(8.dp))
                    Pill(
                        text = entry.endMin?.let { formatClock(it) } ?: "--:--",
                        onClick = { dialog = DayDialog.End },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Break Time", fontSize = 17.sp)
                Button(onClick = { dialog = DayDialog.Break }, shape = CircleShape) {
                    Text(
                        if (entry.breakMin > 0) formatClock(entry.breakMin) else "Set",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Subtract break from hours", fontSize = 17.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = entry.subtractBreak,
                    onCheckedChange = { checked ->
                        vm.update(date) { e -> e.copy(subtractBreak = checked) }
                    }
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hours Worked", fontSize = 17.sp)
                Text(
                    formatDuration(minutes),
                    color = AppColors.payGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Earned", fontSize = 17.sp)
                Text(
                    formatMoney(cents, settings.currency),
                    color = AppColors.payGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }

        Text(
            "Notes",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            OutlinedTextField(
                value = entry.note,
                onValueChange = { text -> vm.update(date) { e -> e.copy(note = text) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
    }

    when (dialog) {
        DayDialog.Start -> TimeDialog(
            title = "Set Start Time",
            initialMinutes = entry.startMin,
            hint = "Example: 09:30 or 17:45",
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
            title = "Set End Time",
            initialMinutes = entry.endMin,
            hint = "Example: 09:30 or 17:45",
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
            title = "Break Time",
            initialMinutes = entry.breakMin.takeIf { it > 0 },
            hint = "Break length, hh:mm. Example: 00:30",
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
        null -> Unit
    }
}
