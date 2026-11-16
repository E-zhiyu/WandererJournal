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
import com.wanderer.journal.DiaryAlarm
import com.wanderer.journal.data.save.store.diaryAlarmStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiaryAlarmViewModel(
    private val dataStore: DataStore<DiaryAlarm>
) : ViewModel() {
    val diaryAlarm: StateFlow<DiaryAlarm> = dataStore.data
        .catch { if (it is IOException) emit(DiaryAlarm.getDefaultInstance()) else throw it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DiaryAlarm.getDefaultInstance()
        )

    fun setSwitchOpen(isOpen: Boolean) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setSwitch(isOpen).build() }
    }

    fun addAlarmTime(time: String) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().addAlarmTime(time).build() }
    }

    fun removeAlarmTime(time: String) = viewModelScope.launch {
        dataStore.updateData {
            val newList = it.alarmTimeList.toMutableList()
            newList.remove(time)
            it.toBuilder().clearAlarmTime().addAllAlarmTime(newList).build()
        }
    }
}

/**
 * 获取日记提醒设置项 DataStore ViewModel
 */
@Composable
fun dsDiaryAlarmViewModel(): DiaryAlarmViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer {
                DiaryAlarmViewModel(context.diaryAlarmStore)
            }
        }
    )
}