package com.hourstracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Navy.toArgb()))
        setContent {
            HoursTrackerTheme {
                App()
            }
        }
    }
}

@Composable
fun App(vm: TrackerViewModel = viewModel()) {
    var mainTab by rememberSaveable { mutableIntStateOf(0) }
    var subTab by rememberSaveable { mutableIntStateOf(0) }
    var openDayText by rememberSaveable { mutableStateOf<String?>(null) }
    val openDay = openDayText?.let { LocalDate.parse(it) }

    BackHandler(enabled = openDay != null) { openDayText = null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text("Hours Report", color = Color.White, fontSize = 24.sp)
        }

        AppTabs(listOf("Hours Report", "Settings"), mainTab) { mainTab = it }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            if (mainTab == 1) {
                SettingsScreen(vm)
            } else if (openDay != null) {
                DayDetailScreen(vm, openDay) { openDayText = null }
            } else {
                Column(Modifier.fillMaxSize()) {
                    AppTabs(listOf("Form", "Preview", "Share"), subTab) { subTab = it }
                    Box(Modifier.weight(1f)) {
                        when (subTab) {
                            0 -> FormScreen(vm) { date -> openDayText = date.toString() }
                            1 -> PreviewScreen(vm)
                            else -> ShareScreen(vm)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selected,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        labels.forEachIndexed { index, label ->
            Tab(
                selected = selected == index,
                onClick = { onSelect(index) },
                text = { Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
