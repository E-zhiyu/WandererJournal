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
import com.wanderer.journal.Update
import com.wanderer.journal.data.save.store.updateStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UpdateViewModel(
    private val dataStore: DataStore<Update>
) : ViewModel() {
    val update: StateFlow<Update> = dataStore.data
        .catch { if (it is IOException) emit(Update.getDefaultInstance()) else throw it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Update.getDefaultInstance()
        )

    fun setSkipVersionCode(code: Int) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setSkipVersionCode(code).build() }
    }

    fun setApkUri(uri: String) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setApkUri(uri).build() }
    }

    fun setAutoCheckUpdateOpen(isOpen: Boolean) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setAutoUpdateCheck(isOpen).build() }
    }
}

/**
 * 获取应用更新设置项 DataStore ViewModel
 */
@Composable
fun dsUpdateViewModel(): UpdateViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer {
                UpdateViewModel(context.updateStore)
            }
        }
    )
}