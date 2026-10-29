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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class SettingsDialog { Rate, Tax, Currency }

@Composable
fun SettingsScreen(vm: TrackerViewModel) {
    val settings = vm.settings
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp)
    ) {
        SettingRow("Hourly Rate:") {
            Pill(
                text = if (settings.hourlyRate > 0.0) {
                    settings.currency + formatNumber(settings.hourlyRate) + "/hr"
                } else {
                    "Set"
                },
                onClick = { dialog = SettingsDialog.Rate }
            )
        }
        SettingRow("Tax Rate:") {
            Pill(
                text = if (settings.taxRate > 0.0) formatNumber(settings.taxRate) + "%" else "Set",
                onClick = { dialog = SettingsDialog.Tax }
            )
        }
        SettingRow("Currency symbol:") {
            Pill(text = settings.currency, onClick = { dialog = SettingsDialog.Currency })
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "The hourly rate is saved on the device and applied to every day in every week.\n\n" +
                "Weeks run from Monday to Sunday. Times use the 24-hour format (00:00 - 23:59). " +
                "If the end time is earlier than the start time, the shift is counted as ending after midnight.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }

    when (dialog) {
        SettingsDialog.Rate -> RateDialog(vm) { dialog = null }
        SettingsDialog.Tax -> TaxDialog(vm) { dialog = null }
        SettingsDialog.Currency -> ValueDialog(
            title = "Currency symbol",
            label = "Symbol, e.g. £ or \$",
            initial = settings.currency,
            keyboardType = KeyboardType.Text,
            maxLength = 3,
            onSave = { text ->
                val symbol = text.trim()
                if (symbol.isNotEmpty()) vm.setCurrency(symbol)
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        null -> Unit
    }
}

@Composable
private fun SettingRow(label: String, content: @Composable () -> Unit) {
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
