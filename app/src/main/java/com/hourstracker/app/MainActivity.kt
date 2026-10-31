package com.hourstracker.app

import android.graphics.Color as AndroidColor
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(LightPalettes[0].header.toArgb()))
        setContent {
            val vm: TrackerViewModel = viewModel()
            HoursTrackerTheme(vm.settings) {
                App(vm)
            }
        }
    }
}

@Composable
fun App(vm: TrackerViewModel) {
    val s = LocalStrings.current
    val palette = LocalPalette.current
    val scheme = MaterialTheme.colorScheme
    var mainTab by rememberSaveable { mutableIntStateOf(0) }
    var openDayText by rememberSaveable { mutableStateOf<String?>(null) }
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val openDay = openDayText?.let { LocalDate.parse(it) }

    BackHandler(enabled = openDay != null) { openDayText = null }

    // Keep the system bars readable on whichever theme is active.
    val activity = LocalContext.current as? ComponentActivity
    LaunchedEffect(palette) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(palette.header.toArgb()),
            navigationBarStyle = if (palette.dark) {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            }
        )
    }

    // Surface provides the default text colour. Without it, text that has no colour of its
    // own stays black, which is invisible on the dark themes.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = scheme.background,
        contentColor = scheme.onBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.header)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(s.appTitle, color = Color.White, fontSize = 22.sp)
            }

            AppTabs(listOf(s.appTitle, s.tabSettings), mainTab) { mainTab = it }

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
                    Column(modifier = Modifier.fillMaxSize()) {
                        AppTabs(
                            labels = listOf(s.tabForm, s.tabPreview, s.tabShare),
                            selected = pagerState.currentPage
                        ) { page -> scope.launch { pagerState.animateScrollToPage(page) } }

                        WeekNavigator(vm)

                        // Swipe left / right to move between Form, Preview and Share.
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) { page ->
                            when (page) {
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
}

@Composable
private fun AppTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    TabRow(
        selectedTabIndex = selected,
        containerColor = scheme.surface,
        contentColor = scheme.primary
    ) {
        labels.forEachIndexed { index, label ->
            Tab(
                selected = selected == index,
                onClick = { onSelect(index) },
                text = { Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1) },
                selectedContentColor = scheme.primary,
                unselectedContentColor = scheme.onSurfaceVariant
            )
        }
    }
}
