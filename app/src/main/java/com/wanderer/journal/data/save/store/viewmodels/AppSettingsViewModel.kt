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
import com.wanderer.journal.AppSettings
import com.wanderer.journal.data.save.store.appSettingStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppSettingsViewModel(
    private val dataStore: DataStore<AppSettings>
) : ViewModel() {
    val appSettings: StateFlow<AppSettings> = dataStore.data
        .catch { if (it is IOException) emit(AppSettings.getDefaultInstance()) else throw it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AppSettings.getDefaultInstance()
        )

    /**
     * 读取一次 AppSettings
     */
    suspend fun loadAppSettingsOnce(): AppSettings =
        dataStore.data
            .catch { if (it is IOException) emit(AppSettings.getDefaultInstance()) else throw it }
            .first()

    fun setThemeMode(themeMode: Int) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setThemeMode(themeMode).build() }
    }

    fun setDynamicColor(dynamicColor: Boolean) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setDynamicColor(dynamicColor).build() }
    }

    fun setFirstScreen(firstScreen: Int) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setFirstScreen(firstScreen).build() }
    }
}

/**
 * 获取应用设置项 DataStore ViewModel
 */
@Composable
fun dsAppSettingsViewModel(): AppSettingsViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer {
                AppSettingsViewModel(context.appSettingStore)
            }
        }
    )
}