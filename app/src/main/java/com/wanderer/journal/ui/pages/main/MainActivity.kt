package com.wanderer.journal.ui.pages.main

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wanderer.journal.auxiliary.enums.unique.LogTags
import com.wanderer.journal.data.save.preference.AppSettingsPreference
import com.wanderer.journal.data.save.preference.VersionPreference
import com.wanderer.journal.helpers.UpdateHelper
import com.wanderer.journal.ui.pages.ScreenRoutes
import com.wanderer.journal.ui.pages.main.home.HomeScreen
import com.wanderer.journal.ui.pages.main.settings.SettingsScreen
import com.wanderer.journal.ui.pages.main.settings.sub.AboutScreen
import com.wanderer.journal.ui.pages.main.settings.sub.TroubleshootingScreen
import com.wanderer.journal.ui.theme.MaterialAppTheme
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (VersionPreference.getAutoUpdateCheck(this)) {
            Log.d(LogTags.MAIN_ACTIVITY.n(), "自动检测更新中……")
            disposable.add(UpdateHelper.checkUpdate(this, false))
        }

        val startIndex = AppSettingsPreference.getFirstScreen(this).coerceIn(
            0, MainDestination.entries.lastIndex
        )

        setContent {
            MaterialAppTheme {
                MainScreen(startIndex = startIndex)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}

@Composable
private fun MainScreen(startIndex: Int) {
    // 外层导航：承载二级页面（About / Troubleshooting）
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.MainTabs,
    ) {
        composable<ScreenRoutes.MainTabs> {
            MainTabs(
                startIndex = startIndex
            )
        }

        composable<ScreenRoutes.About> {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTroubleshooting = {
                    navController.navigate(ScreenRoutes.Troubleshooting)
                }
            )
        }

        composable<ScreenRoutes.Troubleshooting> {
            TroubleshootingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainTabs(
    startIndex: Int
) {
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { MainDestination.entries.size }
    )
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                MainDestination.entries.forEachIndexed { index, destination ->
                    val selected = pagerState.currentPage == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                // 与 ViewPager2 setCurrentItem(index) 行为一致
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(destination.iconRes),
                                contentDescription = stringResource(destination.titleRes)
                            )
                        },
                        label = { Text(stringResource(destination.titleRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                ),
            beyondViewportPageCount = 2,
            userScrollEnabled = true
        ) { page ->
            when (MainDestination.entries[page]) {
//                MainDestination.DIARY -> FragmentHost { DiaryFragment() }
                MainDestination.HOME -> HomeScreen()
                MainDestination.SETTINGS -> SettingsScreen(
                    disposable = CompositeDisposable()
                )
            }
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showSystemUi = true
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun Preview() {
    MaterialAppTheme {
        MainTabs(2)
    }
}