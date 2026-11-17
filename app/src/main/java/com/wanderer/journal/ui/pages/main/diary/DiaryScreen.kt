package com.wanderer.journal.ui.pages.main.diary

import android.content.Intent
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.CustomDateTimeFormatter
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.classes.text.RoleRefTextRule
import com.wanderer.journal.auxiliary.enums.unique.KeyStrings
import com.wanderer.journal.data.save.db.entities.composite.ui.DiaryContentUnionModel
import com.wanderer.journal.helpers.text.ParagraphTextConverter
import com.wanderer.journal.helpers.time.DateTimePickerHelper
import com.wanderer.journal.ui.components.ExceptionDialog
import com.wanderer.journal.ui.components.PressableCard
import com.wanderer.journal.ui.pages.diary.read.ReadActivity
import com.wanderer.journal.ui.pages.diary.write.WriteActivity
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun DiaryScreen(
    vm: DiaryViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by vm.uiState.collectAsStateWithLifecycle()
    val events by vm.events.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackHostState = remember { SnackbarHostState() }

    // 日期选择器触发状态（Material3 的 DatePickerDialog 是弹窗形式）
    var showDatePickerForSkip by remember { mutableStateOf(false) }
    var datePickerInitial by remember { mutableStateOf<LocalDate?>(null) }

    // 长按 FAB 的日期选择：同样复用 DatePickerDialog
    var showDatePickerForFab by remember { mutableStateOf(false) }

    // 一次性事件（Toast / 错误）
    var exceptionMessage by remember { mutableStateOf("") }
    LaunchedEffect(events) {
        events?.let { evt ->
            when (evt) {
                is DiaryUiEvent.Toast -> snackHostState.showSnackbar(evt.message)
                is DiaryUiEvent.Error -> exceptionMessage = evt.throwable.message ?: ""
            }
            vm.consumeEvent()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.diary)) },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = {
                        // 跳转日期：取当前首项作为初始值（近似原逻辑）
                        datePickerInitial = state.diaries.getOrNull(
                            listState.firstVisibleItemIndex
                        )?.diary?.diaryDate
                        showDatePickerForSkip = true
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_date_range_24),
                            contentDescription = stringResource(R.string.date_skip_btn)
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { context.startActivity(Intent(context, WriteActivity::class.java)) },
                modifier = Modifier
                    .padding(4.dp)
                    .combinedClickable(
                        onClick = {
                            context.startActivity(
                                Intent(
                                    context,
                                    WriteActivity::class.java
                                )
                            )
                        },
                        onLongClick = {
                            showDatePickerForFab = true
                        }
                    ),
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.btn_add))
            }
        },
        snackbarHost = { SnackbarHost(snackHostState) },
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = inner.calculateTopPadding())
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            if (state.isEmpty && !state.isLoading) {
                Text(
                    text = stringResource(R.string.no_diary),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 5.dp,
                        bottom = 5.dp,
                        start = Interval.SidePadding,
                        end = Interval.SidePadding
                    ),
                ) {
                    itemsIndexed(
                        items = state.diaries,
                        key = { _, item -> item.diary.diaryId }
                    ) { index, item ->
                        DiaryListItem(
                            item = item,
                            isFirst = index == 0,
                            isLast = index == state.diaries.lastIndex
                        )
                    }
                }
            }
        }
    }

    // 日期跳转对日期选择话框
    if (showDatePickerForSkip) {
        DatePickerDialog(
            initial = datePickerInitial,
            onDismiss = { showDatePickerForSkip = false },
            onConfirm = { targetDate ->
                vm.findScrollTargetPosition(
                    targetDate = targetDate,
                    onResult = { count ->
                        scope.launch {
                            // 判断越界 / 是否为当天无日记
                            val actualPos = if (count >= state.diaries.size) count - 1 else count
                            if (count >= state.diaries.size) {
                                snackHostState.showSnackbar("已跳转至最早的日期")
                            } else if (count == 0) {
                                snackHostState.showSnackbar("已跳转至最晚的日期")
                            } else {
                                val exact = state.diaries.getOrNull(actualPos)?.diary?.diaryDate
                                if (exact != null && !exact.isEqual(targetDate)) {
                                    snackHostState.showSnackbar("当天无日记，已跳转至相邻日期")
                                } else {
                                    snackHostState.showSnackbar("已跳转至目标日期")
                                }
                            }

                            // 折叠 AppBar + 滚动
                            scrollBehavior.state.heightOffset =
                                scrollBehavior.state.heightOffsetLimit
                            listState.animateScrollToItem(
                                actualPos.coerceIn(
                                    0,
                                    state.diaries.lastIndex
                                )
                            )
                        }
                    }
                )
            }
        )
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
fun DiaryListItem(
    item: DiaryContentUnionModel,
    isFirst: Boolean,
    isLast: Boolean
) {
    val context = LocalContext.current

    // 首项/末项圆角
    val normalRadius = when {
        isFirst && isLast -> Radius.SingleArray
        isFirst -> Radius.TopArray
        isLast -> Radius.BottomArray
        else -> Radius.MiddleArray
    }

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 1.dp)
    ) {
        PressableCard(
            normalRadius = normalRadius,
            onClick = {
                val intent = Intent(context, ReadActivity::class.java).apply {
                    putExtra(
                        KeyStrings.INIT_DATE.v(),
                        DateTimePickerHelper.fromLocalDate(item.diary.diaryDate)
                    )
                }
                context.startActivity(intent)
            },
            onLongClick = { expanded = true }
        ) {
            Column(Modifier.padding(start = 15.dp, top = 10.dp, end = 15.dp, bottom = 10.dp)) {
                Text(
                    text = item.diary.diaryDate.format(CustomDateTimeFormatter.DATE_WITH_WEEK),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 富文本摘要：用 AndroidView 包裹 TextView 承接 ParagraphTextConverter 的 CharSequence
                    //TODO:将其迁移至 Compose 的 AnnotatedString
                    AndroidView(
                        modifier = Modifier.weight(1f),
                        factory = { ctx ->
                            android.widget.TextView(ctx).apply {
                                maxLines = 1
                                ellipsize = android.text.TextUtils.TruncateAt.END
                                setTextColor(0xFF888888.toInt())
                                textSize = 14f
                            }
                        },
                        update = { tv ->
                            tv.text = ParagraphTextConverter.hierarchic(
                                context, null, item.paragraphFragment,
                                object : RoleRefTextRule() {
                                    override fun onClick(clickData: String) {}
                                }
                            )
                        }
                    )
                    Spacer(Modifier.width(20.dp))
                    Text(
                        text = "${item.charCount}字",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }

        // 长按菜单：DropdownMenu（等价于 PopupMenu，Gravity.END）
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete_diary)) },
                onClick = {
                    expanded = false
                    // TODO:调用删除方法
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.modify_date)) },
                onClick = {
                    expanded = false
                    // TODO:调用日期修改方法
                },
            )
        }
    }
}

@Composable
private fun DatePickerDialog(
    initial: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial?.let { DateTimePickerHelper.fromLocalDate(it) }
    )
    //TODO:修复对话框充满屏幕宽度的BUG
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onConfirm(DateTimePickerHelper.getLocalDateFromTimeMilli(it))
                }
                onDismiss()
            }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    ) {
        DatePicker(state = datePickerState)
    }
}