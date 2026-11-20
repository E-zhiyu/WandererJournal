package com.wanderer.journal.ui.pages.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wanderer.journal.AppSettings
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.enums.unique.LogTags
import com.wanderer.journal.data.save.preference.VersionPreference
import com.wanderer.journal.data.save.store.viewmodels.dsAppSettingsViewModel
import com.wanderer.journal.helpers.UpdateHelper
import com.wanderer.journal.ui.ExpandingMask
import com.wanderer.journal.ui.pages.ScreenRoutes
import com.wanderer.journal.ui.pages.main.diary.DiaryScreen
import com.wanderer.journal.ui.pages.main.home.HomeScreen
import com.wanderer.journal.ui.pages.main.settings.SettingsScreen
import com.wanderer.journal.ui.pages.main.settings.sub.AboutScreen
import com.wanderer.journal.ui.pages.main.settings.sub.PermissionScreen
import com.wanderer.journal.ui.pages.main.settings.sub.ShareSettingsScreen
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

        setContent {
            MaterialAppTheme {
                MainScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}

@Composable
private fun MainScreen(
    onSettingsLoaded: (AppSettings) -> Unit = {}
) {
    // 外层导航：承载二级页面（About / Troubleshooting）
    val navController = rememberNavController()

    // 读取 DataStore 数据
    val appSettingsViewModel = dsAppSettingsViewModel()
    var appSettings by remember { mutableStateOf<AppSettings?>(null) }
    LaunchedEffect(Unit) {
        appSettings = appSettingsViewModel.loadAppSettingsOnce()
        onSettingsLoaded(appSettings!!)
    }

    // ---- 开屏动画进度 ----
    // 只有在 settings 到达后才启动扩张动画
    val animatable = remember { Animatable(0f) }
    val iconAlpha = remember { Animatable(1f) }
    var splashVisible by remember { mutableStateOf(true) }

    LaunchedEffect(appSettings) {
        if (appSettings == null) return@LaunchedEffect
        // 扩张 + 淡出并行
        launch {
            animatable.animateTo(
                1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }
        iconAlpha.animateTo(
            0f,
            animationSpec = tween(durationMillis = 450, easing = LinearEasing)
        )
        splashVisible = false   // 动画结束，移除遮罩
    }

    Box(Modifier.fillMaxSize()) {
        // 真实界面（此时 settings 已就绪）
        if (appSettings != null) {
            NavHost(
                navController = navController,
                startDestination = ScreenRoutes.MainTabs,
            ) {
                // 主界面
                composable<ScreenRoutes.MainTabs> {
                    MainTabs(
                        startIndex = appSettings!!.firstScreen,
                        navController = navController
                    )
                }

                // 权限设置
                composable<ScreenRoutes.PermissionSettings> {
                    PermissionScreen { navController.popBackStack() }
                }

                // 关于软件
                composable<ScreenRoutes.About> {
                    AboutScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToTroubleshooting = {
                            navController.navigate(ScreenRoutes.Troubleshooting)
                        }
                    )
                }

                // 疑难解答
                composable<ScreenRoutes.Troubleshooting> {
                    TroubleshootingScreen { navController.popBackStack() }
                }

                // 分享设置
                composable<ScreenRoutes.ShareSettings> {
                    ShareSettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // 开屏遮罩
        if (splashVisible) {
            ExpandingMask(
                progress = animatable.value,
                iconAlpha = iconAlpha.value,
                maskColor = MaterialTheme.colorScheme.background, // 与系统闪屏背景一致
                iconSize = 120.dp,
                iconCornerRadius = Radius.XLarge,
            )
        }
    }
}

@Composable
private fun MainTabs(
    startIndex: Int,
    navController: NavController
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
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding()
                ),
            beyondViewportPageCount = 2,
            userScrollEnabled = true
        ) { page ->
            when (MainDestination.entries[page]) {
                MainDestination.DIARY -> DiaryScreen()
                MainDestination.HOME -> HomeScreen()
                MainDestination.SETTINGS -> SettingsScreen(
                    disposable = CompositeDisposable(),
                    onSkipToPermission = { navController.navigate(ScreenRoutes.PermissionSettings) },
                    onSkipToAbout = { navController.navigate(ScreenRoutes.About) },
                    onSkipToShare = { navController.navigate(ScreenRoutes.ShareSettings) }
                )
            }
        }
    }
}