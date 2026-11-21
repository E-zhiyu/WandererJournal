package com.wanderer.journal.ui.pages.main.home

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wanderer.journal.data.save.db.DiaryDb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class HomeUiState(
    val startDateText: String? = null,
    val dateDifferenceText: String? = null,
    val diaryCount: String? = null,
    val paragraphCount: String? = null,
    val emotionTagCount: String? = null,
    val roleCount: String? = null,
    val lifeNoteCount: String? = null,
    val currentTip: String = "",
    val errorMessage: String? = null,
)

class HomeViewModel(context: Context) : ViewModel() {

    private val db = DiaryDb.getInstance(context)

    // 唯一可变的状态源：只放"业务数据 + 提示文本 + 错误"
    private val _tips = MutableStateFlow("")

    // 6 个 DAO 的 Flow，直接 combine
    private val startDateFlow = db.diaryDao().getEarliestDiaryDateFlow()
    private val diaryCountFlow = db.diaryDao().getDiaryCountFlow()
    private val paragraphCountFlow = db.paragraphDao().getParagraphCountFlow()
    private val emotionCountFlow = db.emotionTagDao().getEmotionTagCountFlow()
    private val roleCountFlow = db.roleDao().getRoleCountFlow()
    private val lifeNoteCountFlow = db.lifeNoteDao().getLifeNoteCountFlow()

    private data class Counts(
        val diary: Int,
        val paragraph: Int,
        val emotion: Int,
        val role: Int,
        val lifeNote: Int,
    )

    // 先组合前 5 个（Int 计数类），再与外部的 String/错误流组合
    private val countsFlow = combine(
        diaryCountFlow,
        paragraphCountFlow,
        emotionCountFlow,
        roleCountFlow,
        lifeNoteCountFlow
    ) { diary, paragraph, emotion, role, lifeNote ->
        // 这里返回一个自定义的计数数据类，或直接构造中间状态
        Counts(diary, paragraph, emotion, role, lifeNote)
    }

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 再组合剩余的部分
    private val _uiState: StateFlow<HomeUiState> = combine(
        startDateFlow,      // LocalDate?
        countsFlow,         // Counts
        _tips,              // String
        _error              // String?
    ) { startDate, counts, tip, error ->
        val (startText, diffText) = formatStartDate(startDate)
        HomeUiState(
            startDateText = startText,
            dateDifferenceText = diffText,
            diaryCount = counts.diary.toString(),
            paragraphCount = counts.paragraph.toString(),
            emotionTagCount = counts.emotion.toString(),
            roleCount = counts.role.toString(),
            lifeNoteCount = counts.lifeNote.toString(),
            currentTip = tip,
            errorMessage = error,
        )
    }.catch { e -> _error.value = e.message }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState

    private fun formatStartDate(date: LocalDate?): Pair<String?, String?> {
        return if (date == null) {
            NOT_APPLICABLE to NOT_APPLICABLE
        } else {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd\nEEEE")
            val diff = ChronoUnit.DAYS.between(date, LocalDate.now())
            date.format(formatter) to String.format(Locale.getDefault(), "%d天", diff)
        }
    }

    fun nextTip(pool: List<String>) {
        if (pool.isEmpty()) return
        _tips.value = "tip : " + pool.random()
    }

    fun consumeError() {
        _error.value = null
    }

    companion object {
        const val NOT_APPLICABLE = "—"
    }
}

@Composable
fun homeViewModel(): HomeViewModel {
    val context = LocalContext.current
    return viewModel(
        factory = viewModelFactory {
            initializer { HomeViewModel(context) }
        }
    )
}