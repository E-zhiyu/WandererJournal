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
import com.wanderer.journal.AutoBackup
import com.wanderer.journal.data.save.store.autoBackupStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AutoBackupViewModel(
    private val dataStore: DataStore<AutoBackup>
) : ViewModel() {
    val autoBackup: StateFlow<AutoBackup> = dataStore.data
        .catch { if (it is IOException) emit(AutoBackup.getDefaultInstance()) else throw it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AutoBackup.getDefaultInstance()
        )

    fun setSwitchOpen(isOpen: Boolean) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setSwitchStat(isOpen).build() }
    }

    fun setBackupDir(dirUri: String) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setBackupDirectory(dirUri).build() }
    }

    fun setBackupFrequency(frequency: Int) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setBackupFrequency(frequency).build() }
    }
}

/**
 * 获取自动备份设置项 DataStore ViewModel
 */
@Composable
fun dsAutoBackupViewModel(): AutoBackupViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer {
                AutoBackupViewModel(context.autoBackupStore)
            }
        }
    )
}