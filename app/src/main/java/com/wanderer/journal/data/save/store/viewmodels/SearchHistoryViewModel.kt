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
import com.wanderer.journal.SearchHistory
import com.wanderer.journal.data.save.store.searchHistoryStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchHistoryViewModel(
    private val dataStore: DataStore<SearchHistory>
) : ViewModel() {
    val searchHistory: StateFlow<SearchHistory> = dataStore.data
        .catch { if (it is IOException) emit(SearchHistory.getDefaultInstance()) else throw it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SearchHistory.getDefaultInstance()
        )

    fun addDiaryContentHistory(search: String) = viewModelScope.launch {
        dataStore.updateData {
            val newList = addNewHistory(search, it.diaryContentList)
            it.toBuilder().clearDiaryContent().addAllDiaryContent(newList).build()
        }
    }

    fun removeDiaryContentHistory(history: String) = viewModelScope.launch {
        dataStore.updateData {
            val newList = it.diaryContentList.toMutableList()
            newList.remove(history)
            it.toBuilder().clearDiaryContent().addAllDiaryContent(newList).build()
        }
    }

    fun addRoleHistory(search: String) = viewModelScope.launch {
        dataStore.updateData {
            val newList = addNewHistory(search, it.roleInfoList)
            it.toBuilder().clearRoleInfo().addAllRoleInfo(newList).build()
        }
    }

    fun removeRoleHistory(history: String) = viewModelScope.launch {
        dataStore.updateData {
            val newList = it.roleInfoList.toMutableList()
            newList.remove(history)
            it.toBuilder().clearRoleInfo().addAllRoleInfo(newList).build()
        }
    }

    fun addLifeNoteHistory(search: String) = viewModelScope.launch {
        dataStore.updateData {
            val newList = addNewHistory(search, it.lifeNoteList)
            it.toBuilder().clearLifeNote().addAllLifeNote(newList).build()
        }
    }

    fun removeLifeNoteHistory(history: String) = viewModelScope.launch {
        dataStore.updateData {
            val newList = it.lifeNoteList.toMutableList()
            newList.remove(history)
            it.toBuilder().clearLifeNote().addAllLifeNote(newList).build()
        }
    }

    /**
     * 添加一个新的搜索历史
     * @param element 新搜索项
     * @param list 原有的搜索历史列表
     * @return 添加元素后的列表（限制长度为15）
     */
    private fun addNewHistory(element: String, list: MutableList<String>): List<String> {
        list.remove(element)
        list.add(0, element)
        return list.take(15)
    }
}

/**
 * 获取搜索历史 DataStore ViewModel
 */
@Composable
fun dsSearchHistoryViewModel(): SearchHistoryViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer {
                SearchHistoryViewModel(context.searchHistoryStore)
            }
        }
    )
}