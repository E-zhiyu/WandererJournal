package com.wanderer.journal.ui.pages.main.settings.sub

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.data.save.store.viewmodels.dsDiaryShareViewModel
import com.wanderer.journal.ui.pages.main.settings.SettingCategoryTitle
import com.wanderer.journal.ui.pages.main.settings.SettingSwitchItem

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
                Spacer(modifier = Modifier.height(Interval.DefaultPadding))
                SettingCategoryTitle(stringResource(R.string.share_content))
            }

            item {
                SettingSwitchItem(
                    title = stringResource(R.string.include_media),
                    icon = painterResource(R.drawable.outline_perm_media_24),
                    normalRadius = Radius.TopArray,
                    checked = shareSettings.media,
                    onCheckedChange = { checked-> vm.setMedia(checked) }
                )
            }
        }
    }
}