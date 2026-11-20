package com.wanderer.journal.ui.pages.main.settings.sub

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.data.save.store.viewmodels.dsDiaryShareViewModel
import com.wanderer.journal.ui.pages.main.settings.SettingCategoryTitle
import com.wanderer.journal.ui.pages.main.settings.SettingClickableItem
import com.wanderer.journal.ui.pages.main.settings.SettingSwitchItem
import com.wanderer.journal.ui.theme.MaterialAppTheme

@Composable
fun ShareSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    val vm = dsDiaryShareViewModel()
    val shareSettings by vm.diaryShare.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(id = R.string.share_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollState
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .nestedScroll(scrollState.nestedScrollConnection)
                .padding(horizontal = Interval.SidePadding),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
        ) {
            item {
                SettingCategoryTitle(stringResource(R.string.share_content))
            }

            // 媒体
            item {
                SettingSwitchItem(
                    title = stringResource(R.string.media),
                    subtitle = stringResource(R.string.share_media_subtitle),
                    icon = painterResource(R.drawable.outline_perm_media_24),
                    normalRadius = Radius.TopArray,
                    checked = shareSettings.media,
                    onCheckedChange = { checked -> vm.setMedia(checked) }
                )
            }

            // 情绪标签
            item {
                Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                SettingSwitchItem(
                    title = stringResource(R.string.emotion_tag),
                    subtitle = stringResource(R.string.share_emotion_tag_subtitle),
                    icon = painterResource(R.drawable.outline_mood_24),
                    normalRadius = Radius.MiddleArray,
                    checked = shareSettings.emotion,
                    onCheckedChange = { checked -> vm.setEmotion(checked) }
                )
            }

            // 时间
            item {
                Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                SettingSwitchItem(
                    title = stringResource(R.string.paragraph_time),
                    subtitle = stringResource(R.string.share_paragraph_time_subtitle),
                    icon = painterResource(R.drawable.outline_nest_clock_farsight_analog_24),
                    normalRadius = Radius.MiddleArray,
                    checked = shareSettings.time,
                    onCheckedChange = { checked -> vm.setTime(checked) }
                )
            }

            // 位置
            item {
                Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                SettingSwitchItem(
                    title = stringResource(R.string.paragraph_location),
                    subtitle = stringResource(R.string.share_paragraph_location_subtitle),
                    icon = painterResource(R.drawable.outline_location_on_24),
                    normalRadius = Radius.BottomArray,
                    checked = shareSettings.location,
                    onCheckedChange = { checked -> vm.setLocation(checked) }
                )
            }

            item {
                SettingCategoryTitle(stringResource(R.string.others))
            }

            item {
                Spacer(modifier = Modifier.height(Interval.AdjacentPadding))
                SettingClickableItem(
                    title = stringResource(R.string.share_picture_bottom_text),
                    subtitle = stringResource(R.string.share_bottom_text_subtitle),
                    icon = painterResource(R.drawable.outline_text_ad_24),
                    normalRadius = Radius.SingleArray,
                    onClick = {
                        // TODO:弹出对话框
                    }
                )
            }
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SharePreview() {
    MaterialAppTheme {
        ShareSettingsScreen {}
    }
}