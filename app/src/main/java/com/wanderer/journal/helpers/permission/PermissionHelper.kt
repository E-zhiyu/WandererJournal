package com.wanderer.journal.helpers.permission

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import com.hjq.device.compat.DeviceOs
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.wanderer.journal.WandererJournal

// 用户选择不再提示权限时还申请权限的事件
class PermissionDoNotShowAgainEvent(
    val message: String,
    val permissionList: List<IPermission> = emptyList()
)

// 权限请求事件
sealed class PermissionRequest(
    val descriptionFun: (List<IPermission>) -> String = { "" },
    val onDoNotAskAgain: ((List<IPermission>) -> Unit)? = null,
    val onRequested: (List<IPermission>, List<IPermission>) -> Unit,
) {
    // 其他权限
    class Common(
        val onAllGranted: (List<IPermission>) -> Unit,
        onRequested: (List<IPermission>, List<IPermission>) -> Unit,
        descriptionFun: (List<IPermission>) -> String = { "" },
        onDoNotAskAgain: ((List<IPermission>) -> Unit)? = null,
        val permissions: List<IPermission>
    ) : PermissionRequest(
        descriptionFun = descriptionFun,
        onDoNotAskAgain = onDoNotAskAgain,
        onRequested = onRequested,
    )

    // 位置权限
    class Location(
        val onlyCoarseLocation: (() -> Unit)? = null,
        val hasFineLocation: () -> Unit,
        onRequested: (List<IPermission>, List<IPermission>) -> Unit,
        descriptionFun: (List<IPermission>) -> String = { "" },
        onDoNotAskAgain: ((List<IPermission>) -> Unit)? = null,
    ) : PermissionRequest(
        descriptionFun = descriptionFun,
        onDoNotAskAgain = onDoNotAskAgain,
        onRequested = onRequested,
    )

    // 自启动权限
    class AutoStart(
        descriptionFun: () -> String = { "" },
    ) : PermissionRequest(
        descriptionFun = { descriptionFun() },
        onRequested = { _, _ -> },
    )
}

/**
 * 判断是否定义了自启动权限
 *
 * @return 是否定义了自启动权限
 */
fun isAutoStartDefined(): Boolean {
    //如果能识别到系统名称但是没有自启动权限，返回false
    if (DeviceOs.isOneUi() || DeviceOs.isMyOs() || DeviceOs.isZuxOs() || DeviceOs.isZui()) {
        return false
    }

    //判断系统名称是否为空，为空说明为类原生系统，否则为拥有自启动设置的定制安卓
    return DeviceOs.getOsName().isNotEmpty()
}

/**
 * 申请自启动权限
 *
 * @param context 上下文
 */
fun requestAutoStartPermission(context: Context) {
    if (!isAutoStartDefined()) {
        Toast.makeText(context, "当前系统无需申请自启动权限", Toast.LENGTH_SHORT).show()
        return
    }

    val intent: Intent
    if (!DeviceOs.isHyperOs() && !DeviceOs.isMiui()) {
        Toast.makeText(context, "请手动前往自启动设置界面授权", Toast.LENGTH_SHORT).show()
        intent = Intent(Settings.ACTION_SETTINGS)
    } else {
        intent = Intent()
        intent.setComponent(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        )
    }

    WandererJournal.lockLifecycleObserver(true)
    context.startActivity(intent)
}

/**
 * 申请权限
 *
 * @param context 上下文
 * @param onAllGranted 没有权限需要再申请的回调
 * @param onDoNotAskAgain 用户选择不再询问后再申请权限的回调
 * @param onRequested 权限申请情况回调
 * @param permissions 需要申请的权限
 */
fun requestPermission(
    context: Context,
    activity: Activity?,
    onAllGranted: (List<IPermission>) -> Unit,
    onDoNotAskAgain: ((List<IPermission>) -> Unit)? = null,
    onRequested: (List<IPermission>, List<IPermission>) -> Unit,
    permissions: List<IPermission>
) {
    if (permissions.isEmpty()) return

    if (XXPermissions.isGrantedPermissions(context, permissions)) {
        onAllGranted(permissions)
    } else if (
        onDoNotAskAgain != null &&
        activity != null &&
        XXPermissions.isDoNotAskAgainPermissions(activity, permissions)
    ) {
        onDoNotAskAgain(permissions)
    } else {
        WandererJournal.lockLifecycleObserver(true)
        XXPermissions.with(context)
            .permissions(permissions)
            .request { grantedList, deniedList ->
                WandererJournal.lockLifecycleObserver(false)
                onRequested(grantedList, deniedList)
            }
    }
}

/**
 * 申请位置权限
 *
 * @param context 上下文
 * @param hasFineLocation 已经拥有精确位置权限的回调
 * @param onlyCoarseLocation 仅有粗略位置的回调，若不为 null 会拦截接下来的精确位置申请逻辑
 * @param onDoNotAskAgain 用户选择不再询问后再申请权限的回调
 * @param onRequested 权限申请情况回调
 */
fun requestLocationPermission(
    context: Context,
    activity: Activity?,
    hasFineLocation: () -> Unit,
    onlyCoarseLocation: (() -> Unit)? = null,
    onDoNotAskAgain: ((List<IPermission>) -> Unit)? = null,
    onRequested: (List<IPermission>, List<IPermission>) -> Unit
) {
    val fineLocation = PermissionLists.getAccessFineLocationPermission()
    val coarseLocation = PermissionLists.getAccessCoarseLocationPermission()
    val isFineGranted = XXPermissions.isGrantedPermission(context, fineLocation)
    val isCoarseGranted = XXPermissions.isGrantedPermission(context, coarseLocation)
    val permissionArray = arrayOf(fineLocation, coarseLocation)
    if (isFineGranted && isCoarseGranted) {
        hasFineLocation()
    } else if (isCoarseGranted && onlyCoarseLocation != null) {
        onlyCoarseLocation()
    } else if (
        (activity != null) &&
        (onDoNotAskAgain != null) &&
        XXPermissions.isDoNotAskAgainPermissions(activity, permissionArray)
    ) {
        onDoNotAskAgain(permissionArray.asList())
    } else {
        WandererJournal.lockLifecycleObserver(true)
        XXPermissions.with(context)
            .permissions(permissionArray)
            .request { grantedList, deniedList ->
                WandererJournal.lockLifecycleObserver(false)
                onRequested(grantedList, deniedList)
            }
    }
}