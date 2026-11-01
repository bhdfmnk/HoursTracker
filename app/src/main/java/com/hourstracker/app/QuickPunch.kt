package com.hourstracker.app

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime

private class ManualTarget(val date: LocalDate, val isStart: Boolean)

/**
 * The "Start shift / End shift" card.
 * One tap records the current time. A double tap or a long press opens the time dialog
 * so the time can be typed in by hand instead.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickPunch(vm: TrackerViewModel) {
    val s = LocalStrings.current
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val settings = vm.settings

    // "now" is refreshed every 20 seconds so the running time stays current.
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(20_000L)
            now = LocalDateTime.now()
        }
    }
    var manual by remember { mutableStateOf<ManualTarget?>(null) }

    val today = now.toLocalDate()
    val state = punchState(now, vm.entryFor(today), vm.entryFor(today.minusDays(1)))

    fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun openManual() {
        manual = when (state.kind) {
            PunchKind.Start -> ManualTarget(state.date, true)
            PunchKind.End -> ManualTarget(state.date, false)
            PunchKind.Done -> ManualTarget(state.date, false)
        }
    }

    fun onTap() {
        val stamp = LocalDateTime.now()
        val minutes = stamp.hour * 60 + stamp.minute
        val clock = formatClock(minutes, settings.use12h)
        when (state.kind) {
            PunchKind.Start -> {
                vm.update(stamp.toLocalDate()) { e -> e.copy(startMin = minutes) }
                toast(s.punchStarted + " " + clock)
            }
            PunchKind.End -> {
                vm.update(state.date) { e -> e.copy(endMin = minutes) }
                toast(s.punchEnded + " " + clock)
            }
            PunchKind.Done -> toast(s.punchAlready)
        }
        now = stamp
    }

    val statusText = when (state.kind) {
        PunchKind.Start -> s.punchNotStarted
        PunchKind.End -> {
            val start = state.startMin ?: 0
            val dayWord = if (state.date != today) " " + s.yesterday else ""
            s.punchStartedAt + " " + formatClock(start, settings.use12h) + dayWord +
                " · " + formatDuration(elapsedMinutes(now, state.date, start), s)
        }
        PunchKind.Done -> {
            val entry = vm.entryFor(state.date)
            formatClock(entry.startMin ?: 0, settings.use12h) + " - " +
                formatClock(entry.endMin ?: 0, settings.use12h) + " · " +
                formatDuration(entry.workedMinutes, s)
        }
    }

    val buttonText = when (state.kind) {
        PunchKind.Start -> s.punchStart
        PunchKind.End -> s.punchEnd
        PunchKind.Done -> s.punchDone
    }
    val buttonColor = when (state.kind) {
        PunchKind.Start -> scheme.primary
        PunchKind.End -> scheme.error
        PunchKind.Done -> scheme.surfaceVariant
    }
    val buttonTextColor = when (state.kind) {
        PunchKind.Start -> scheme.onPrimary
        PunchKind.End -> scheme.onError
        PunchKind.Done -> scheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = s.today + " · " + dayMonthLabel(today, s),
                color = scheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Text(text = statusText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(text = s.punchHint, color = scheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(buttonColor)
                .combinedClickable(
                    role = Role.Button,
                    onClick = { onTap() },
                    onDoubleClick = { openManual() },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        openManual()
                    }
                )
                .padding(horizontal = 18.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buttonText,
                color = buttonTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1
            )
        }
    }

    val target = manual
    if (target != null) {
        val entry = vm.entryFor(target.date)
        val current = if (target.isStart) entry.startMin else entry.endMin
        val stamp = LocalDateTime.now()
        TimeDialog(
            title = if (target.isStart) s.setStartTime else s.setEndTime,
            mode = if (settings.use12h) TimeMode.Clock12 else TimeMode.Clock24,
            initialMinutes = current ?: (stamp.hour * 60 + stamp.minute),
            hint = if (settings.use12h) s.hint12 else s.hint24,
            onSave = { minutes ->
                vm.update(target.date) { e ->
                    if (target.isStart) e.copy(startMin = minutes) else e.copy(endMin = minutes)
                }
                manual = null
            },
            onReset = {
                vm.update(target.date) { e ->
                    if (target.isStart) e.copy(startMin = null) else e.copy(endMin = null)
                }
                manual = null
            },
            onDismiss = { manual = null }
        )
    }
}
