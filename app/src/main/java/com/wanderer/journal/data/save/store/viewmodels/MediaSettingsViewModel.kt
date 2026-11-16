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
import com.wanderer.journal.MediaSettings
import com.wanderer.journal.data.save.store.mediaSettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MediaSettingsViewModel(
    private val dataStore: DataStore<MediaSettings>
) : ViewModel() {
    val mediaSettings: StateFlow<MediaSettings> = dataStore.data
        .catch { if (it is IOException) emit(MediaSettings.getDefaultInstance()) else throw it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            MediaSettings.getDefaultInstance()
        )

    fun setHdrDisplay(display: Boolean) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setShowHdr(display).build() }
    }
}

/**
 * 获取媒体设置项 DataStore ViewModel
 */
@Composable
fun dsMediaSettingsViewModel(): MediaSettingsViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer {
                MediaSettingsViewModel(context.mediaSettingsStore)
            }
        }
    )
}