package com.wanderer.journal.ui.pages.main.settings.sub.permission

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 权限授予状态
class PermissionState(
    val camera: Boolean,
    val notification: Boolean,
    val battery: Boolean,
    val alarm: Boolean,
    val fineLocation: Boolean,
    val coarseLocation: Boolean
)

class PermissionScreenViewModel(app: Application) : AndroidViewModel(application = app) {
    private val _permissionStates = MutableStateFlow<PermissionState?>(null)
    val permissionStates: StateFlow<PermissionState?> = _permissionStates.asStateFlow()
    private val snackMessageChannel = Channel<String>(Channel.BUFFERED) // Snackbar 一次性文本频道
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

    /**
     * 刷新权限授予状态
     */
    fun refreshPermissionStates() {
        val states = PermissionState(
            camera = XXPermissions.isGrantedPermission(
                application,
                PermissionLists.getCameraPermission()
            ),
            notification = XXPermissions.isGrantedPermission(
                application,
                PermissionLists.getPostNotificationsPermission()
            ),
            battery = XXPermissions.isGrantedPermission(
                application,
                PermissionLists.getRequestIgnoreBatteryOptimizationsPermission()
            ),
            alarm = XXPermissions.isGrantedPermission(
                application,
                PermissionLists.getScheduleExactAlarmPermission()
            ),
            fineLocation = XXPermissions.isGrantedPermission(
                application,
                PermissionLists.getAccessFineLocationPermission()
            ),
            coarseLocation = XXPermissions.isGrantedPermission(
                application,
                PermissionLists.getAccessCoarseLocationPermission()
            )
        )
        _permissionStates.value = states
    }
}