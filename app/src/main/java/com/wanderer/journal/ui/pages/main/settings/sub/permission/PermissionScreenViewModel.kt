package com.wanderer.journal.ui.pages.main.settings.sub.permission

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class PermissionScreenViewModel(app: Application) : AndroidViewModel(application = app) {
    private val snackMessageChannel = Channel<String>(Channel.BUFFERED)
    fun getMessageChannel(): Channel<String> {
        return snackMessageChannel
    }

    /**
     * 发送用于显示在 Snackbar 中的文本
     */
    fun sendSnackMessage(message: String) {
        viewModelScope.launch {
            snackMessageChannel.send(message)
        }
    }
}