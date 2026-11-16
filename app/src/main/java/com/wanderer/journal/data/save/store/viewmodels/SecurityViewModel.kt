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
import com.wanderer.journal.SecuritySettings
import com.wanderer.journal.data.save.store.securityStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val dataStore: DataStore<SecuritySettings>
) : ViewModel() {
    val securitySettings: StateFlow<SecuritySettings> = dataStore.data
        .catch { if (it is IOException) emit(SecuritySettings.getDefaultInstance()) else throw it }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SecuritySettings.getDefaultInstance()
        )

    fun setAuthSwitchOpen(isOpen: Boolean) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setAuthSwitch(isOpen).build() }
    }

    fun setAuthOpportunity(opportunity: Int) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setAuthOpportunity(opportunity).build() }
    }

    fun setExcludeSwitchOpen(isOpen: Boolean) = viewModelScope.launch {
        dataStore.updateData { it.toBuilder().setExcludeRecentTask(isOpen).build() }
    }
}

/**
 * 获取隐私设置项 DataStore ViewModel
 */
@Composable
fun dsSecurityViewModel(): SecurityViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel(
        factory = viewModelFactory {
            initializer {
                SecurityViewModel(context.securityStore)
            }
        }
    )
}