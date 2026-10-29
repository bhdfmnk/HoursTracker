package com.hourstracker.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Light-blue rounded button that shows a value (a time, a rate...). */
@Composable
fun Pill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

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

/**
 * 24-hour time entry: HH and MM (00:00 - 23:59).
 * Also used for the break length, where the value is a duration in hh:mm.
 */
@Composable
fun TimeDialog(
    title: String,
    initialMinutes: Int?,
    hint: String,
    onSave: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    fun selectAll(text: String) = TextFieldValue(text, TextRange(0, text.length))

    var hh by remember { mutableStateOf(selectAll(initialMinutes?.let { two(it / 60) } ?: "")) }
    var mm by remember { mutableStateOf(selectAll(initialMinutes?.let { two(it % 60) } ?: "")) }
    val hhFocus = remember { FocusRequester() }
    val mmFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) { runCatching { hhFocus.requestFocus() } }

    val h = hh.text.toIntOrNull()
    val m = if (mm.text.isEmpty()) 0 else mm.text.toIntOrNull()
    val valid = h != null && h in 0..23 && m != null && m in 0..59

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("Enter time (24h):", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TimeField(
                        value = hh,
                        label = "HH",
                        imeAction = ImeAction.Next,
                        modifier = Modifier
                            .width(88.dp)
                            .focusRequester(hhFocus),
                        onChange = { v ->
                            val typed = v.text != hh.text
                            hh = v
                            if (typed && v.text.length == 2) {
                                runCatching { mmFocus.requestFocus() }
                            }
                        }
                    )
                    Text(":", fontSize = 28.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    TimeField(
                        value = mm,
                        label = "MM",
                        imeAction = ImeAction.Done,
                        modifier = Modifier
                            .width(88.dp)
                            .focusRequester(mmFocus),
                        onChange = { v -> mm = v }
                    )
                }
                Text(hint, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onReset) { Text("Reset") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
                TextButton(
                    enabled = valid,
                    onClick = { onSave((h ?: 0) * 60 + (m ?: 0)) }
                ) { Text("Save") }
            }
        }
    )
}

/** One text/number field in a dialog (hourly rate, tax rate, currency). */
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
        confirmButton = {
            Row {
                if (onReset != null) {
                    TextButton(onClick = onReset) { Text("Reset") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
                TextButton(onClick = { onSave(value.text) }) { Text("Save") }
            }
        }
    )
}

@Composable
fun RateDialog(vm: TrackerViewModel, onDismiss: () -> Unit) {
    val s = vm.settings
    ValueDialog(
        title = "Hourly Rate",
        label = "Amount per hour (" + s.currency + ")",
        initial = if (s.hourlyRate > 0.0) formatNumber(s.hourlyRate) else "",
        keyboardType = KeyboardType.Decimal,
        maxLength = 8,
        onSave = { text ->
            parseDecimal(text)?.let { vm.setHourlyRate(it) }
            onDismiss()
        },
        onDismiss = onDismiss
    )
}

@Composable
fun TaxDialog(vm: TrackerViewModel, onDismiss: () -> Unit) {
    val s = vm.settings
    ValueDialog(
        title = "Tax Rate",
        label = "Tax, % of pay",
        initial = if (s.taxRate > 0.0) formatNumber(s.taxRate) else "",
        keyboardType = KeyboardType.Decimal,
        maxLength = 5,
        onSave = { text ->
            parseDecimal(text)?.takeIf { it <= 100.0 }?.let { vm.setTaxRate(it) }
            onDismiss()
        },
        onReset = {
            vm.setTaxRate(0.0)
            onDismiss()
        },
        onDismiss = onDismiss
    )
}
