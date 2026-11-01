package com.hourstracker.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Design system: one set of building blocks used by every screen.
//   spacing  - 16dp page gutters, 12dp between blocks
//   shape    - 12dp rounded cards, fully rounded pills
//   touch    - every tappable element is at least 40-48dp tall
// ---------------------------------------------------------------------------

/** Light-tinted rounded button that shows a value (a time, a rate...). */
@Composable
fun Pill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, large: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .heightIn(min = if (large) 48.dp else 40.dp)
            .clip(CircleShape)
            .background(scheme.primaryContainer)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = if (large) 24.dp else 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = scheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (large) 18.sp else 16.sp,
            maxLines = 1
        )
    }
}

/** Small caption above a group of rows. */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        if (trailing != null) trailing()
    }
}

/** White (or dark-elevated) rounded block that holds a group of rows. */
@Composable
fun GroupCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface),
        content = content
    )
}

@Composable
fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

/** A label (with an optional small line under it) on the left and a control on the right. */
@Composable
fun SettingRow(
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(text = label, fontSize = 17.sp)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        content()
    }
}

/**
 * Compact tappable card with a caption and a value (hourly rate, tax rate).
 * When it is the only card in its row (wide = true) the caption and value sit on one line.
 */
@Composable
fun MiniCard(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    wide: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val cardModifier = modifier
        .clip(RoundedCornerShape(12.dp))
        .background(scheme.surface)
        .clickable(role = Role.Button, onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 10.dp)
    if (wide) {
        Row(
            modifier = cardModifier.heightIn(min = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = scheme.onSurfaceVariant, fontSize = 15.sp, maxLines = 1)
            Text(
                text = value,
                color = scheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1
            )
        }
    } else {
        Column(modifier = cardModifier) {
            Text(text = label, color = scheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 1)
            Text(
                text = value,
                color = scheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1
            )
        }
    }
}

/** iOS-style segmented control. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(scheme.surfaceVariant)
            .padding(3.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) scheme.primary else Color.Transparent)
                    .clickable(role = Role.RadioButton) { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) scheme.onPrimary else scheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 1
                )
            }
        }
    }
}

/** The week switcher shown above the Form / Preview / Share pages. Compact on purpose. */
@Composable
fun WeekNavigator(vm: TrackerViewModel) {
    val s = LocalStrings.current
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { vm.shiftWeek(-1) }, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = s.prevWeek,
                tint = scheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = weekRangeLabel(vm.weekStart, s),
            modifier = Modifier
                .weight(1f)
                .clickable { vm.goToCurrentWeek() }
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
            color = scheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        IconButton(onClick = { vm.shiftWeek(1) }, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = s.nextWeek,
                tint = scheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------------------

enum class TimeMode { Clock24, Clock12, Duration }

@Composable
private fun TimeField(
    value: TextFieldValue,
    label: String,
    imeAction: ImeAction,
    modifier: Modifier,
    onChange: (TextFieldValue) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { v ->
            val digits = v.text.filter { it in '0'..'9' }.take(2)
            onChange(TextFieldValue(digits, TextRange(digits.length)))
        },
        modifier = modifier.onFocusChanged { state ->
            if (state.isFocused) {
                onChange(value.copy(selection = TextRange(0, value.text.length)))
            }
        },
        label = { Text(label) },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction)
    )
}

@Composable
private fun AmPmButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) scheme.primary else scheme.surfaceVariant)
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) scheme.onPrimary else scheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Time entry. Clock24: HH 00-23, MM 00-59. Clock12: HH 1-12, MM and AM/PM buttons.
 * Duration: a length of time as hh:mm (used for the break).
 * The result is minutes: since midnight for the two clocks, a length for Duration.
 */
@Composable
fun TimeDialog(
    title: String,
    mode: TimeMode,
    initialMinutes: Int?,
    hint: String,
    onSave: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalStrings.current

    fun selectAll(text: String) = TextFieldValue(text, TextRange(0, text.length))

    val initHour = initialMinutes?.let { it / 60 }
    val initHourText = when {
        initHour == null -> ""
        mode == TimeMode.Clock12 -> (if (initHour % 12 == 0) 12 else initHour % 12).toString()
        else -> two(initHour)
    }

    var hh by remember { mutableStateOf(selectAll(initHourText)) }
    var mm by remember { mutableStateOf(selectAll(initialMinutes?.let { two(it % 60) } ?: "")) }
    var pm by remember { mutableStateOf(initHour != null && initHour >= 12) }
    val hhFocus = remember { FocusRequester() }
    val mmFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) { runCatching { hhFocus.requestFocus() } }

    val h = hh.text.toIntOrNull()
    val m = if (mm.text.isEmpty()) 0 else mm.text.toIntOrNull()
    val hourOk = when (mode) {
        TimeMode.Clock12 -> h != null && h in 1..12
        TimeMode.Clock24 -> h != null && h in 0..23
        TimeMode.Duration -> hh.text.isEmpty() || (h != null && h in 0..23)
    }
    val valid = hourOk && m != null && m in 0..59

    fun result(): Int {
        val hour = h ?: 0
        val minute = m ?: 0
        val hour24 = if (mode == TimeMode.Clock12) (hour % 12) + (if (pm) 12 else 0) else hour
        return hour24 * 60 + minute
    }

    val fieldWidth = if (mode == TimeMode.Clock12) 72.dp else 88.dp

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(s.enterTime, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TimeField(
                        value = hh,
                        label = s.hh,
                        imeAction = ImeAction.Next,
                        modifier = Modifier
                            .width(fieldWidth)
                            .focusRequester(hhFocus),
                        onChange = { v ->
                            val typed = v.text != hh.text
                            hh = v
                            if (typed && v.text.length == 2) {
                                runCatching { mmFocus.requestFocus() }
                            }
                        }
                    )
                    Text(":", fontSize = 28.sp, modifier = Modifier.padding(horizontal = 6.dp))
                    TimeField(
                        value = mm,
                        label = s.mm,
                        imeAction = ImeAction.Done,
                        modifier = Modifier
                            .width(fieldWidth)
                            .focusRequester(mmFocus),
                        onChange = { v -> mm = v }
                    )
                    if (mode == TimeMode.Clock12) {
                        Column(
                            modifier = Modifier.padding(start = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AmPmButton("AM", !pm) { pm = false }
                            AmPmButton("PM", pm) { pm = true }
                        }
                    }
                }
                Text(hint, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) { Text(s.reset) }
                TextButton(onClick = onDismiss) { Text(s.cancel) }
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onSave(result()) }) { Text(s.save) }
        }
    )
}

/** One text/number field in a dialog (hourly rate, tax rate). */
@Composable
fun ValueDialog(
    title: String,
    label: String,
    initial: String,
    keyboardType: KeyboardType,
    maxLength: Int,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    onReset: (() -> Unit)? = null
) {
    val s = LocalStrings.current
    var value by remember { mutableStateOf(TextFieldValue(initial, TextRange(0, initial.length))) }
    val focus = remember { FocusRequester() }

    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { v ->
                    val cleaned = if (keyboardType == KeyboardType.Decimal) {
                        v.text.filter { it in '0'..'9' || it == '.' || it == ',' }
                    } else {
                        v.text
                    }
                    val t = cleaned.take(maxLength)
                    value = TextFieldValue(t, TextRange(t.length))
                },
                label = { Text(label) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus)
            )
        },
        dismissButton = {
            Row {
                if (onReset != null) {
                    TextButton(onClick = onReset) { Text(s.reset) }
                }
                TextButton(onClick = onDismiss) { Text(s.cancel) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(value.text) }) { Text(s.save) }
        }
    )
}

@Composable
fun RateDialog(vm: TrackerViewModel, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    val settings = vm.settings
    ValueDialog(
        title = s.rateTitle,
        label = s.rateLabel(settings.currency),
        initial = if (settings.hourlyRate > 0.0) formatNumber(settings.hourlyRate, s) else "",
        keyboardType = KeyboardType.Decimal,
        maxLength = 8,
        onSave = { text ->
            parseDecimal(text)?.let { value -> vm.updateSettings { it.copy(hourlyRate = value) } }
            onDismiss()
        },
        onDismiss = onDismiss
    )
}

@Composable
fun TaxDialog(vm: TrackerViewModel, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    val settings = vm.settings
    ValueDialog(
        title = s.taxTitle,
        label = s.taxLabel,
        initial = if (settings.taxRate > 0.0) formatNumber(settings.taxRate, s) else "",
        keyboardType = KeyboardType.Decimal,
        maxLength = 5,
        onSave = { text ->
            parseDecimal(text)?.takeIf { it <= 100.0 }?.let { value ->
                vm.updateSettings { it.copy(taxRate = value) }
            }
            onDismiss()
        },
        onReset = {
            vm.updateSettings { it.copy(taxRate = 0.0) }
            onDismiss()
        },
        onDismiss = onDismiss
    )
}

/** One equal-sized tile of the currency grid. It shows only the symbol. */
@Composable
private fun CurrencyTile(
    symbol: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) scheme.primary else scheme.surfaceVariant)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = if (selected) scheme.onPrimary else scheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

/** Ten common currencies as a grid of symbols, plus a "+" tile for a custom symbol. */
@Composable
fun CurrencyDialog(current: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    val isPreset = s.currencies.any { it.symbol == current }
    var customMode by remember { mutableStateOf(!isPreset) }
    var custom by remember { mutableStateOf(if (isPreset) "" else current) }
    val columns = 4
    val tiles: List<CurrencyOption?> = s.currencies + listOf<CurrencyOption?>(null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.currencyTitle) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                tiles.chunked(columns).forEach { rowTiles ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTiles.forEach { option ->
                            if (option == null) {
                                CurrencyTile(
                                    symbol = "+",
                                    description = s.customSymbol,
                                    selected = customMode,
                                    onClick = { customMode = true },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                CurrencyTile(
                                    symbol = option.symbol,
                                    description = option.name + " (" + option.code + ")",
                                    selected = !customMode && option.symbol == current,
                                    onClick = { onSelect(option.symbol) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        repeat(columns - rowTiles.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
                if (customMode) {
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = custom,
                        onValueChange = { custom = it.take(4) },
                        label = { Text(s.customSymbolHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
        confirmButton = {
            if (customMode) {
                TextButton(
                    enabled = custom.isNotBlank(),
                    onClick = { onSelect(custom.trim()) }
                ) { Text(s.save) }
            }
        }
    )
}
