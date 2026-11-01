package com.hourstracker.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class SettingsDialog { Rate, Weekend, Tax, Currency }

@Composable
fun SettingsScreen(vm: TrackerViewModel) {
    val s = LocalStrings.current
    val scheme = MaterialTheme.colorScheme
    val settings = vm.settings
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }

    // Only the themes that belong to the chosen mode are shown. In "System" mode that is
    // whichever set the phone is using right now (light or dark).
    val systemDark = isSystemInDarkTheme()
    val showDarkThemes = settings.themeMode == 2 || (settings.themeMode == 0 && systemDark)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // ---- pay
        SectionHeader(text = s.secPay)
        GroupCard {
            SettingRow(s.hourlyRate) {
                Pill(
                    text = if (settings.hourlyRate > 0.0) {
                        settings.currency + formatNumber(settings.hourlyRate, s) + s.perHour
                    } else {
                        s.set
                    },
                    onClick = { dialog = SettingsDialog.Rate }
                )
            }
            RowDivider()
            SettingRow(s.weekendRate, subtitle = s.weekendSubtitle) {
                Pill(
                    text = multiplierLabel(settings.weekendMultiplier, s),
                    onClick = { dialog = SettingsDialog.Weekend }
                )
            }
            RowDivider()
            SettingRow(s.taxRate) {
                Pill(
                    text = if (settings.taxRate > 0.0) formatNumber(settings.taxRate, s) + "%" else s.set,
                    onClick = { dialog = SettingsDialog.Tax }
                )
            }
            RowDivider()
            SettingRow(s.showTax) {
                Switch(
                    checked = settings.showTax,
                    onCheckedChange = { checked -> vm.updateSettings { it.copy(showTax = checked) } }
                )
            }
            RowDivider()
            SettingRow(s.currency) {
                Pill(text = settings.currency, onClick = { dialog = SettingsDialog.Currency })
            }
        }

        // ---- time format
        SectionHeader(text = s.secTime)
        GroupCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(s.timeFormat, fontSize = 17.sp)
                Spacer(Modifier.height(10.dp))
                Segmented(
                    options = listOf(s.fmt24, s.fmt12),
                    selected = if (settings.use12h) 1 else 0,
                    onSelect = { index -> vm.updateSettings { it.copy(use12h = index == 1) } }
                )
            }
        }

        // ---- appearance
        SectionHeader(text = s.secAppearance)
        GroupCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(s.mode, fontSize = 17.sp)
                Spacer(Modifier.height(10.dp))
                Segmented(
                    options = listOf(s.modeSystem, s.modeLight, s.modeDark),
                    selected = settings.themeMode,
                    onSelect = { index -> vm.updateSettings { it.copy(themeMode = index) } }
                )
            }
            RowDivider()
            Column(modifier = Modifier.padding(16.dp)) {
                if (showDarkThemes) {
                    Text(s.darkTheme, fontSize = 17.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val names = listOf(s.themeMidnight, s.themeBlack)
                        DarkPalettes.forEachIndexed { index, palette ->
                            ThemeSwatch(
                                palette = palette,
                                name = names[index],
                                selected = settings.darkTheme == index,
                                onClick = { vm.updateSettings { it.copy(darkTheme = index) } },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Text(s.lightTheme, fontSize = 17.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val names = listOf(s.themeClassic, s.themeMint)
                        LightPalettes.forEachIndexed { index, palette ->
                            ThemeSwatch(
                                palette = palette,
                                name = names[index],
                                selected = settings.lightTheme == index,
                                onClick = { vm.updateSettings { it.copy(lightTheme = index) } },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // ---- language
        SectionHeader(text = s.secLanguage)
        GroupCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Segmented(
                    options = listOf("English", "Українська"),
                    selected = if (settings.language == "uk") 1 else 0,
                    onSelect = { index ->
                        vm.updateSettings { it.copy(language = if (index == 1) "uk" else "en") }
                    }
                )
            }
        }

        Text(
            text = s.settingsNote,
            color = scheme.onSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
    }

    when (dialog) {
        SettingsDialog.Rate -> RateDialog(vm) { dialog = null }
        SettingsDialog.Weekend -> ValueDialog(
            title = s.weekendRate,
            label = s.multiplierLabel,
            initial = formatNumber(settings.weekendMultiplier, s),
            keyboardType = KeyboardType.Decimal,
            maxLength = 5,
            onSave = { text ->
                val value = parseDecimal(text)
                if (value != null && value > 0.0 && value <= 10.0) {
                    vm.updateSettings { it.copy(weekendMultiplier = value) }
                }
                dialog = null
            },
            onReset = {
                vm.updateSettings { it.copy(weekendMultiplier = 1.5) }
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        SettingsDialog.Tax -> TaxDialog(vm) { dialog = null }
        SettingsDialog.Currency -> CurrencyDialog(
            current = settings.currency,
            onSelect = { symbol ->
                vm.updateSettings { it.copy(currency = symbol) }
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        null -> Unit
    }
}

/** A tiny preview of a theme: header bar, a card and the accent colour. */
@Composable
private fun ThemeSwatch(
    palette: AppPalette,
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val frame = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(frame)
                .background(palette.scheme.background)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) scheme.primary else scheme.outlineVariant,
                    shape = frame
                )
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(palette.header)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(palette.scheme.surface)
                )
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(palette.scheme.primary)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = (if (selected) "✓ " else "") + name,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) scheme.primary else scheme.onSurface,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
    }
}
