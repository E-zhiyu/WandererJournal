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
import com.wanderer.journal.DiaryShare
import com.wanderer.journal.data.save.store.appSettingStore
import com.wanderer.journal.data.save.store.diaryShareStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryShareViewModel(
    private val dataStore: DataStore<DiaryShare>
) : ViewModel() {
    val diaryShare: StateFlow<DiaryShare> = dataStore.data
        .catch { if (it is IOException) emit(DiaryShare.getDefaultInstance()) else throw it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DiaryShare.getDefaultInstance()
        )

    fun setMedia(shouldShare: Boolean) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setMedia(shouldShare).build() }
    }

    fun setEmotion(shouldShare: Boolean) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setEmotion(shouldShare).build() }
    }

    fun setTime(shouldShare: Boolean) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setTime(shouldShare).build() }
    }

    fun setLocation(shouldShare: Boolean) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setLocation(shouldShare).build() }
    }

    fun setBottomText(bottomText: String) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setBottomText(bottomText).build() }
    }
}

/**
 * 获取日记分享设置项 DataStore ViewModel
 */
@Composable
fun dsDiaryShareViewModel(): DiaryShareViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer {
                DiaryShareViewModel(context.diaryShareStore)
            }
        }
    )
}

