package com.wanderer.journal.data.save.store.viewmodels

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wanderer.journal.DiaryDraft
import com.wanderer.journal.data.save.store.diaryDraftStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryDraftViewModel(
    private val dataStore: DataStore<DiaryDraft>
) : ViewModel() {
    val diaryDraft: StateFlow<DiaryDraft> = dataStore.data
        .catch { if (it is IOException) emit(DiaryDraft.getDefaultInstance()) else throw it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DiaryDraft.getDefaultInstance()
        )

    fun setDiaryDraft(draft: String) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setDraft(draft).build() }
    }
}

/**
 * 获取日记草稿 DataStore ViewModel
 */
@Composable
fun dsDiaryDraftViewModel(): DiaryDraftViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer {
                DiaryDraftViewModel(context.diaryDraftStore)
            }
        }
    )
}