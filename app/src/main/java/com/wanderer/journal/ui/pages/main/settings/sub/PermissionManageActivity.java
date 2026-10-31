package com.wanderer.journal.ui.pages.main.settings.sub;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.permission.PermissionLists;
import com.hjq.permissions.permission.base.IPermission;
import com.wanderer.journal.R;
import com.wanderer.journal.WandererJournal;
import com.wanderer.journal.auxiliary.enums.RadiusStyle;
import com.wanderer.journal.databinding.ActivityPermissionManageBinding;
import com.wanderer.journal.helpers.permission.PermissionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.ui.others.dialogs.MarkdownDialogBuilder;
import com.wanderer.journal.ui.pages.main.settings.components.SettingClickableTextView;

import java.util.List;
import java.util.function.Function;

public class PermissionManageActivity extends AppCompatActivity {
    private ActivityPermissionManageBinding binding;                //绑定的XML视图
    private SettingClickableTextView camera, notification, battery, alarm;  //权限申请视图
    private final PermissionHelper.PermissionCallback REQUEST_CALLBACK = new PermissionHelper.PermissionCallback() {
        @Override
        public void allGranted(List<IPermission> permissionList) {
            Toast.makeText(PermissionManageActivity.this, "权限已授予", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void requested(@NonNull List<IPermission> grantedList, @NonNull List<IPermission> deniedList, Function<List<IPermission>, String> descriptionFun) {
            boolean noDenied = deniedList.isEmpty();
            boolean noGranted = grantedList.isEmpty();
            if (noDenied && !noGranted) {
                Toast.makeText(PermissionManageActivity.this, "权限已授予", Toast.LENGTH_SHORT).show();
            } else if (!XXPermissions.isDoNotAskAgainPermissions(PermissionManageActivity.this, deniedList)) {
                Toast.makeText(PermissionManageActivity.this, "请求被拒绝", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PermissionManageActivity.this, "请手动开启权限", Toast.LENGTH_SHORT).show();
                XXPermissions.startPermissionActivity(PermissionManageActivity.this, deniedList);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityPermissionManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            binding.scrollView.setPadding(
                    0,
                    0,
                    0,
                    systemBars.bottom + AppearanceHelper.dpToPx(this, 15)
            );
            return insets;
        });

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //刷新权限授予情况指示器
        refreshPermissionStat();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        //相机权限
        camera = new SettingClickableTextView(
                this,
                binding.cameraOption,
                R.string.camera_permission,
                "允许使用摄像头",
                R.drawable.baseline_photo_camera_24,
                RadiusStyle.TOP
        );
        camera.setFunctionListener(v -> PermissionHelper.requestPermission(
                this,
                permissionList ->
                        "该权限允许应用调用摄像头，应用范围如下：\n- 写日记时使用系统相机添加媒体文件\n",
                REQUEST_CALLBACK,
                PermissionLists.getCameraPermission()
        ));
        camera.setOnLongClickListener(view -> {
            WandererJournal.lockLifecycleObserver(true);
            XXPermissions.startPermissionActivity(this, PermissionLists.getCameraPermission());
            return true;
        });

        //通知权限
        notification = new SettingClickableTextView(
                this,
                binding.notificationOption,
                R.string.notification_permission,
                "允许发送通知",
                R.drawable.outline_notification_settings_24,
                RadiusStyle.MIDDLE
        );
        notification.setFunctionListener(v -> PermissionHelper.requestPermission(
                this,
                permissionList ->
                        "该权限允许应用发送通知，应用范围如下：\n- 没写日记时发送提醒通知\n",
                REQUEST_CALLBACK,
                PermissionLists.getPostNotificationsPermission()
        ));
        notification.setOnLongClickListener(view -> {
            WandererJournal.lockLifecycleObserver(true);
            XXPermissions.startPermissionActivity(this, PermissionLists.getPostNotificationsPermission());
            return true;
        });

        //自启动权限
        if (PermissionHelper.isAutoStartDefined()) {
            SettingClickableTextView autoStart = new SettingClickableTextView(
                    this,
                    binding.autoStartOption,
                    R.string.auto_start_permission,
                    "允许在后台启动服务",
                    R.drawable.outline_autorenew_24,
                    RadiusStyle.MIDDLE
            );
            autoStart.setFunctionListener(v -> new MarkdownDialogBuilder(
                    this,
                    getString(R.string.auto_start_permission),
                    "该权限是定制安卓中特有的权限，其允许应用在后台启动服务，应用范围如下：\n" +
                            "- 确保定时任务能够在后台自动执行\n")
                    .setPositiveButton("前往设置", (dialogInterface, i) ->
                            PermissionHelper.requestAutoStartPermission(this)
                    )
                    .setNegativeButton("取消", null)
                    .show());
            autoStart.setOnLongClickListener(v -> {
                PermissionHelper.requestAutoStartPermission(this);
                return true;
            });
        } else {
            binding.autoStartOption.getRoot().setVisibility(View.GONE);
        }

        //电池优化策略
        battery = new SettingClickableTextView(
                this,
                binding.batteryOption,
                R.string.ignore_battery_optimization,
                "设置安卓原生的电池优化策略",
                R.drawable.outline_battery_android_frame_3_24,
                RadiusStyle.MIDDLE
        );
        battery.setFunctionListener(v -> PermissionHelper.requestBatteryIgnoration(
                this,
                permissionList ->
                        "该设置项是原生安卓的一个重要设置项，可以更加底层地调整应用的省电策略。" +
                                "如果需要确保应用在后台能够运行，请在电池优化设置界面找到对应应用，点击开关左侧的文本并勾选“无限制”。" +
                                "影响范围如下：\n" +
                                "- 日记提醒是否能够准点发送通知\n"
        ));
        battery.setOnLongClickListener(view -> {
            WandererJournal.lockLifecycleObserver(true);
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivity(intent);
            return true;
        });

        //精确闹钟权限
        alarm = new SettingClickableTextView(
                this,
                binding.alarmOption,
                R.string.alarm_permission,
                "允许设置定时任务",
                R.drawable.outline_alarm_24,
                RadiusStyle.BOTTOM
        );
        alarm.setFunctionListener(v -> PermissionHelper.requestPermission(
                this,
                permissionList ->
                        "该权限允许应用执行某些定时任务，以实现一些自动化功能，应用范围如下：\n" +
                                "- 根据用户设置自动检测当天是否有日记并发送通知提醒\n",
                REQUEST_CALLBACK,
                PermissionLists.getScheduleExactAlarmPermission()
        ));
        alarm.setOnLongClickListener(view -> {
            WandererJournal.lockLifecycleObserver(true);
            XXPermissions.startPermissionActivity(this, PermissionLists.getScheduleExactAlarmPermission());
            return true;
        });
    }

    /**
     * 刷新权限授予情况指示器
     */
    private void refreshPermissionStat() {
        final String GRANTED = "已授予";
        final String NOT_GRANTED = "未授予";

        //相机权限
        boolean isCameraGranted = XXPermissions.isGrantedPermission(this, PermissionLists.getCameraPermission());
        camera.getFunctionComponent().setText(isCameraGranted ? GRANTED : NOT_GRANTED);

        //通知权限
        boolean isNotificationGranted = XXPermissions.isGrantedPermission(this, PermissionLists.getPostNotificationsPermission());
        notification.getFunctionComponent().setText(isNotificationGranted ? GRANTED : NOT_GRANTED);

        //电池优化
        boolean isBatteryIgnored = XXPermissions.isGrantedPermission(this, PermissionLists.getRequestIgnoreBatteryOptimizationsPermission());
        battery.getFunctionComponent().setText(isBatteryIgnored ? "已忽略" : "未忽略");

        //精确闹钟权限
        boolean isAlarmGranted = XXPermissions.isGrantedPermission(this, PermissionLists.getScheduleExactAlarmPermission());
        alarm.getFunctionComponent().setText(isAlarmGranted ? GRANTED : NOT_GRANTED);
    }
}