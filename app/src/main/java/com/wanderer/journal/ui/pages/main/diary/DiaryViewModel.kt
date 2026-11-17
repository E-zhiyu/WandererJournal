package com.wanderer.journal.ui.pages.main.diary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wanderer.journal.data.save.db.DiaryDb
import com.wanderer.journal.data.save.db.entities.DiaryEntity
import com.wanderer.journal.data.save.db.entities.composite.ui.DiaryContentUnionModel
import com.wanderer.journal.data.save.db.services.DiaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface DiaryUiEvent {
    data class Toast(val message: String) : DiaryUiEvent
    data class Error(val throwable: Throwable) : DiaryUiEvent
}

data class DiaryUiState(
    val diaries: List<DiaryContentUnionModel> = emptyList(),
    val isEmpty: Boolean = true,
    val isLoading: Boolean = true,
)

class DiaryViewModel(app: Application) : AndroidViewModel(app) {

    private val db = DiaryDb.getInstance(app)

    // 1) 日记列表：DB → Flow，自动刷新
    private val diariesFlow = db.diaryDao().getAllDiariesFlow()

    val uiState: StateFlow<DiaryUiState> = diariesFlow
        .map { list -> DiaryUiState(list, list.isEmpty(), isLoading = false) }
        .catch { emit(DiaryUiState(isLoading = false)) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DiaryUiState(),
        )

    // 2) 一次性事件：Toast / 错误，用 StateFlow 承载，UI 侧消费后清空
    private val _events = MutableStateFlow<DiaryUiEvent?>(null)
    val events: StateFlow<DiaryUiEvent?> = _events.asStateFlow()

    fun consumeEvent() {
        _events.value = null
    }

    /**
     * 删除日记
     * @param diary 需要删除的日记
     */
    fun deleteDiary(diary: DiaryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                DiaryService.deleteDiaryAndParagraphMedias(diary, getApplication())
                _events.value = DiaryUiEvent.Toast("日记删除成功")
            } catch (e: Exception) {
                _events.value = DiaryUiEvent.Error(e)
            }
        }
    }

    /**
     * 更新日记日期
     * @param diary 需要更新日期的日记
     * @param targetDate 更新后的日期
     */
    fun updateDiaryDate(diary: DiaryEntity, targetDate: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.diaryDao().modifyDiaryDate(diary.diaryId, targetDate, db.paragraphDao())
                _events.value = DiaryUiEvent.Toast("日期更新完毕")
            } catch (e: Exception) {
                _events.value = DiaryUiEvent.Error(e)
            }
        }
    }

    /**
     * 目标日期在列表中的位置
     * @param targetDate 目标日期
     * @param onResult 查询成功的回调，包含查询到的下标
     */
    fun findScrollTargetPosition(targetDate: LocalDate, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            diariesFlow.collect { modelList ->
                var position = 0
                for (model in modelList) {
                    if (!model.diary.diaryDate.isAfter(targetDate)) {
                        break
                    }
                    position++
                }

                val message: String
                if (position >= modelList.size) {
                    message = "已跳转至最早的日期"
                } else if (position == 0) {
                    message = "已跳转至最晚的日期"
                } else {
                    val exact = modelList.getOrNull(position)?.diary?.diaryDate
                    message = if (exact != null && !exact.isEqual(targetDate)) {
                        "当天无日记，已跳转至相邻日期"
                    } else {
                        "已跳转至目标日期"
                    }
                }

                _events.value = DiaryUiEvent.Toast(message)
                onResult(position.coerceIn(0, modelList.lastIndex))
            }
        }
    }
}