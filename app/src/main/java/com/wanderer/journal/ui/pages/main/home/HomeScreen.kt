package com.wanderer.journal.ui.pages.main.home

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.ui.components.ExceptionDialog
import com.wanderer.journal.ui.components.PressableCard
import com.wanderer.journal.ui.pages.diary.read.ReadActivity
import com.wanderer.journal.ui.pages.emotion.EmotionTagListActivity
import com.wanderer.journal.ui.pages.life_note.LifeNoteListActivity
import com.wanderer.journal.ui.pages.role.RoleListActivity
import com.wanderer.journal.ui.pages.statistics.StatisticsActivity

@Composable
fun HomeScreen(
    vm: HomeViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by vm.uiState.collectAsStateWithLifecycle()
    val resource = LocalResources.current

    // 1) 第一次组合时喂入提示文本资源并展示一条
    val tipsArray = stringArrayResource(R.array.tips_array).toList()
    val xiaomiTips = remember {
        if (Build.MANUFACTURER.lowercase().contains("xiaomi"))
            resource.getStringArray(R.array.xiaomi_tips).toList()
        else emptyList()
    }
    LaunchedEffect(Unit) {
        vm.nextTip(tipsArray + xiaomiTips)
    }


    // 统一弹错误对话框
    var exceptionMessage by remember { mutableStateOf("") }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            exceptionMessage = msg
            vm.consumeError()
        }
    }

    // 折叠滚动行为，对应原 CollapsingToolbarLayout
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.home_page)) },
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Interval.SidePadding, vertical = 5.dp),
        ) {
            Spacer(Modifier.height(5.dp))

            // 随机提示卡片
            PressableCard(
                onClick = { vm.nextTip(tipsArray + xiaomiTips) },
                normalRadius = Radius.SingleArray,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = state.currentTip,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(10.dp))

            // 日期卡片
            PressableCard(
                onClick = {
                    val intent = Intent(context, ReadActivity::class.java)
                    context.startActivity(intent)
                },
                normalRadius = Radius.TopArray,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                ) {
                    val (startDateText, startDateTitle, diffText, diffTitle) = createRefs()
                    Text(
                        text = state.startDateText ?: stringResource(R.string.not_applicable),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.constrainAs(startDateText) {
                            top.linkTo(parent.top)
                            start.linkTo(startDateTitle.start)
                            end.linkTo(startDateTitle.end)
                            bottom.linkTo(startDateTitle.top)
                        },
                    )
                    Text(
                        text = stringResource(R.string.start_date),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.constrainAs(startDateTitle) {
                            top.linkTo(startDateText.bottom)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(diffTitle.start)
                        },
                    )
                    Text(
                        text = state.dateDifferenceText ?: stringResource(R.string.not_applicable),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.constrainAs(diffText) {
                            top.linkTo(parent.top)
                            start.linkTo(diffTitle.start)
                            end.linkTo(diffTitle.end)
                            bottom.linkTo(diffTitle.top)
                        },
                    )
                    Text(
                        text = stringResource(R.string.date_difference_to_today),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.constrainAs(diffTitle) {
                            bottom.linkTo(startDateTitle.bottom)
                            start.linkTo(startDateTitle.end)
                            end.linkTo(parent.end)
                            top.linkTo(diffText.bottom)
                        },
                    )
                }
            }
            Spacer(Modifier.height(2.dp))

            // 日记 + 段落 两列卡片
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Interval.AdjacentPadding)) {
                StatCard(
                    value = state.diaryCount,
                    title = stringResource(R.string.diary_count),
                    normalRadius = Radius.MiddleArray,
                    onClick = {
                        val intent = Intent(context, StatisticsActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    value = state.paragraphCount,
                    title = stringResource(R.string.paragraph_count),
                    normalRadius = Radius.MiddleArray,
                    onClick = {
                        val intent = Intent(context, StatisticsActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(2.dp))

            // 情绪 / 角色 / 认知 三列
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Interval.AdjacentPadding)) {
                StatCard(
                    value = state.emotionTagCount,
                    title = stringResource(R.string.emotion_tag),
                    normalRadius = arrayOf(Radius.Small, Radius.Small, Radius.Large, Radius.Small),
                    onClick = {
                        val intent = Intent(context, EmotionTagListActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    value = state.roleCount,
                    title = stringResource(R.string.role),
                    normalRadius = Radius.MiddleArray,
                    onClick = {
                        val intent = Intent(context, RoleListActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    value = state.lifeNoteCount,
                    title = stringResource(R.string.life_note),
                    normalRadius = arrayOf(Radius.Small, Radius.Small, Radius.Small, Radius.Large),
                    onClick = {
                        val intent = Intent(context, LifeNoteListActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    // 错误信息对话框
    if (exceptionMessage.isNotEmpty()) {
        ExceptionDialog(
            { exceptionMessage = "" },
            exceptionMessage
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String?,
    title: String,
    normalRadius: Array<Dp>,
    onClick: () -> Unit,
) {
    PressableCard(
        onClick = onClick,
        modifier = modifier,
        normalRadius = normalRadius
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = value ?: stringResource(R.string.infinity),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}