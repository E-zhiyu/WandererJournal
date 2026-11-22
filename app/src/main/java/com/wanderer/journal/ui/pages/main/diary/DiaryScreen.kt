package com.wanderer.journal.ui.pages.main.diary

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.CustomDateTimeFormatter
import com.wanderer.journal.auxiliary.classes.Interval
import com.wanderer.journal.auxiliary.classes.Radius
import com.wanderer.journal.auxiliary.classes.text.RoleRefTextRule
import com.wanderer.journal.auxiliary.enums.unique.KeyStrings
import com.wanderer.journal.data.save.db.converters.DateTimeConverter
import com.wanderer.journal.data.save.db.entities.DiaryEntity
import com.wanderer.journal.data.save.db.entities.composite.ui.DiaryContentUnionModel
import com.wanderer.journal.helpers.text.hierarchicToAnnotatedString
import com.wanderer.journal.ui.components.LargeRadiusDropdownMenu
import com.wanderer.journal.ui.components.PressableCard
import com.wanderer.journal.ui.components.PressableFAB
import com.wanderer.journal.ui.components.dialogs.DatePickerDialogWithFilter
import com.wanderer.journal.ui.components.dialogs.ExceptionDialog
import com.wanderer.journal.ui.components.rememberFabVisible
import com.wanderer.journal.ui.pages.diary.read.ReadActivity
import com.wanderer.journal.ui.pages.diary.write.WriteActivity
import com.wanderer.journal.ui.pages.role.RoleInputActivity
import com.wanderer.journal.ui.theme.MaterialAppTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

// 日期选择模式：跳转 or 改某篇日记的日期
sealed interface DatePickerMode {
    data object Skip : DatePickerMode                               // 日记列表跳转到指定日期
    data class DateChange(val diary: DiaryEntity) : DatePickerMode  // 修改这篇日记的日期
}

// 日期选择对话框显示请求
data class DatePickerRequest(
    val mode: DatePickerMode,                       // 日期选择的请求种类
    val initDate: LocalDate?,                       // 日期选择对话框的初始日期
    val dateFilter: ((Long) -> Boolean)? = null,    // 判断日期是否可以选择的函数
    val yearRange: IntRange? = null                 // 可选年份的范围
)

@Composable
fun DiaryScreen(
    vm: DiaryViewModel = diaryViewModel(),
) {
    val context = LocalContext.current
    val state by vm.uiState.collectAsStateWithLifecycle()
    val events by vm.events.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackHostState = remember { SnackbarHostState() }

    // 一次性事件（Toast / 错误）
    var exceptionMessage: String? by remember { mutableStateOf(null) }
    LaunchedEffect(events) {
        events?.let { evt ->
            when (evt) {
                is DiaryUiEvent.Toast -> snackHostState.showSnackbar(evt.message)
                is DiaryUiEvent.Error -> exceptionMessage = evt.throwable.message ?: ""
            }
            vm.consumeEvent()
        }
    }

    // 日期选择对话框状态：null = 不显示；非 null = 显示，且携带模式
    var datePickerRequest by remember {
        mutableStateOf<DatePickerRequest?>(null)
    }

    var flashDiaryId by remember { mutableStateOf<Long?>(null) }    // 需要闪烁的日记 id；null 表示没有

    val fabVisible by rememberFabVisible(listState)
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
                        val diaryYears = state.diaries.map { it.diary.diaryDate.year }.toSet()
                        datePickerRequest = DatePickerRequest(
                            mode = DatePickerMode.Skip,
                            initDate = state.diaries.getOrNull(listState.firstVisibleItemIndex)?.diary?.diaryDate,
                            dateFilter = { pickerMillis ->
                                val diaryDateMillis =
                                    state.diaries.map { DateTimeConverter.fromLocalDate(it.diary.diaryDate) }
                                        .toSet()
                                diaryDateMillis.contains(pickerMillis)
                            },
                            yearRange = IntRange(diaryYears.min(), diaryYears.max())
                        )
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
            PressableFAB(
                modifier = Modifier.animateFloatingActionButton(
                    visible = fabVisible,
                    alignment = Alignment.Center,
                ),
                onClick = { context.startActivity(Intent(context, WriteActivity::class.java)) }
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
                            isLast = index == state.diaries.lastIndex,
                            highlighted = flashDiaryId == item.diary.diaryId,
                            onHighlightFinished = { flashDiaryId = null },
                            {
                                val today = LocalDate.now()
                                datePickerRequest = DatePickerRequest(
                                    mode = DatePickerMode.DateChange(item.diary),
                                    initDate = item.diary.diaryDate,
                                    dateFilter = { pickerMillis ->
                                        val todayMillis = DateTimeConverter.fromLocalDate(today)
                                        pickerMillis <= todayMillis
                                    },
                                    yearRange = IntRange(today.year - 150, today.year)
                                )
                            },
                            {
                                vm.deleteDiary(item.diary, context)
                            }
                        )
                    }
                }
            }
        }
    }

    // 错误信息对话框
    exceptionMessage?.let {
        ExceptionDialog(
            { exceptionMessage = null },
            exceptionMessage ?: ""
        )
    }

    // 日期选择对话框
    datePickerRequest?.let { req ->
        DatePickerDialogWithFilter(
            initialDate = req.initDate,
            selectableDates = req.dateFilter,
            yearRange = req.yearRange,
            onDismiss = { datePickerRequest = null },
            onConfirm = { selected ->
                when (val mode = req.mode) {
                    is DatePickerMode.Skip -> vm.findScrollTargetPosition(
                        targetDate = selected,
                        onResult = { position, itemCount, targetId ->
                            scope.launch {
                                // 折叠 AppBar + 滚动
                                scrollBehavior.state.heightOffset =
                                    scrollBehavior.state.heightOffsetLimit

                                // 先瞬间跳到目标附近，再动画走最后一段
                                val firstItemIndex = listState.firstVisibleItemIndex
                                if (position > firstItemIndex + 10 || position < firstItemIndex - 10) {
                                    val jumpTarget = if (firstItemIndex > position)
                                        (position - 10).coerceAtLeast(0) else
                                        (position + 10).coerceAtMost(itemCount - 1)
                                    listState.scrollToItem(jumpTarget)
                                }
                                listState.animateScrollToItem(position)

                                // 修改闪烁的日记的编号
                                flashDiaryId = targetId
                            }
                        }
                    )

                    is DatePickerMode.DateChange -> {
                        vm.updateDiaryDate(mode.diary, selected)
                    }
                }
                datePickerRequest = null
            },
        )
    }
}

@Composable
fun DiaryListItem(
    item: DiaryContentUnionModel,
    isFirst: Boolean,
    isLast: Boolean,
    highlighted: Boolean,                 // 新增
    onHighlightFinished: () -> Unit,      // 新增
    onChangeDate: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    // 闪烁强度：0f = 正常，0.8f = 高亮峰值
    val flash = remember { Animatable(0f) }
    LaunchedEffect(highlighted, item.diary.diaryId) {
        if (highlighted) {
            // 闪烁动画
            flash.animateTo(1f, tween(280))
            flash.animateTo(0f, tween(280))
            onHighlightFinished()
        }
    }

    // 闪烁颜色
    val baseColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val flashColor = MaterialTheme.colorScheme.secondaryContainer
    val animatedColor = lerp(baseColor, flashColor, flash.value)

    // 首项/末项圆角
    val normalRadius = when {
        isFirst && isLast -> Radius.SingleArray
        isFirst -> Radius.TopArray
        isLast -> Radius.BottomArray
        else -> Radius.MiddleArray
    }

    // 手指点击卡片的偏移量
    var pointOffset by remember {
        mutableStateOf(Offset.Zero)
    }
    var expanded by remember { mutableStateOf(false) }                  // 下拉框是否展开

    // 富文本渲染
    val highlightColor = MaterialTheme.colorScheme.primary
    val annotated = remember(item.paragraphFragment) {
        val rules = listOf(object : RoleRefTextRule() {
            override fun onClick(clickData: String) {
                // TODO:将富文本逻辑完全迁移至 Kotlin 后可以删除该方法，而是使用 onLinkClick 统一监听
            }
        })
        hierarchicToAnnotatedString(
            raw = item.paragraphFragment,
            highlightedKeywords = null,
            highlightColor = highlightColor,
            rules = rules,
            onLinkClick = { key, data ->
                if (key == KeyStrings.ROLE_ID.v()) {
                    val intent = Intent(context, RoleInputActivity::class.java).apply {
                        putExtra(key, data.toLong())
                    }
                    context.startActivity(intent)
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Interval.AdjacentPadding)
    ) {
        PressableCard(
            normalRadius = normalRadius,
            colors = CardDefaults.cardColors(containerColor = animatedColor),
            onClick = { _ ->
                val intent = Intent(context, ReadActivity::class.java).apply {
                    putExtra(
                        KeyStrings.INIT_DATE.v(),
                        DateTimeConverter.fromLocalDate(item.diary.diaryDate)
                    )
                }
                context.startActivity(intent)
            },
            onLongClick = { offset ->
                pointOffset = offset
                expanded = true
            }
        ) {
            Row(
                modifier = Modifier.padding(
                    start = 15.dp,
                    top = 10.dp,
                    end = 15.dp,
                    bottom = 10.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(
                        text = item.diary.diaryDate.format(CustomDateTimeFormatter.DATE_WITH_WEEK),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    // 富文本摘要
                    Text(
                        text = annotated,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF888888),
                        fontSize = 14.sp,
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "${item.charCount}字",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }

        // 长按菜单：DropdownMenu
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        pointOffset.x.roundToInt(),
                        pointOffset.y.roundToInt()
                    )
                }
                .size(0.dp)
        ) {
            LargeRadiusDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(Radius.Large),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.modify_date)) },
                    onClick = {
                        expanded = false
                        onChangeDate()
                    },
                    shape = MenuDefaults.leadingItemShape
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete_diary)) },
                    onClick = {
                        expanded = false
                        onDelete()
                    },
                    shape = MenuDefaults.trailingItemShape
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
private fun DiaryScreenPreview() {
    MaterialAppTheme {
        DiaryScreen()
    }
}