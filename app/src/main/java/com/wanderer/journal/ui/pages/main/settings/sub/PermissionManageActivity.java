package com.wanderer.journal.ui.pages.main.settings.sub;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.StringRes;
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

public class PermissionManageActivity extends AppCompatActivity {
    private ActivityPermissionManageBinding binding;                //绑定的XML视图
    private SettingClickableTextView camera, notification, battery, alarm;  //权限申请视图

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
        camera.setFunctionListener(v -> showExplanationDialog(
                R.string.camera_permission,
                "该权限允许应用调用摄像头，应用范围如下：\n" +
                        "- 写日记时使用系统相机添加媒体文件\n",
                () -> PermissionHelper.requestPermission(
                        this,
                        null,
                        new PermissionHelper.PermissionCallback() {
                            @Override
                            public void allGranted(List<IPermission> permissionList) {
                                Toast.makeText(PermissionManageActivity.this, "权限已授予", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void requested(List<IPermission> grantedList, List<IPermission> deniedList) {
                                boolean noDenied = deniedList.isEmpty();
                                boolean noGranted = grantedList.isEmpty();
                                if (noDenied && !noGranted) {
                                    Toast.makeText(PermissionManageActivity.this, "权限已授予", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PermissionManageActivity.this, "请求被拒绝", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        PermissionLists.getCameraPermission()
                )
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
        notification.setFunctionListener(v -> showExplanationDialog(
                R.string.notification_permission,
                "该权限允许应用发送通知，应用范围如下：\n" +
                        "- 没写日记时发送提醒通知\n",
                () -> PermissionHelper.requestPermission(
                        this,
                        null,
                        new PermissionHelper.PermissionCallback() {
                            @Override
                            public void allGranted(List<IPermission> permissionList) {
                                Toast.makeText(PermissionManageActivity.this, "权限已授予", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void requested(List<IPermission> grantedList, List<IPermission> deniedList) {
                                boolean noDenied = deniedList.isEmpty();
                                boolean noGranted = grantedList.isEmpty();
                                if (noDenied && !noGranted) {
                                    Toast.makeText(PermissionManageActivity.this, "权限已授予", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PermissionManageActivity.this, "请求被拒绝", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        PermissionLists.getPostNotificationsPermission()
                )
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
            autoStart.setFunctionListener(v -> showExplanationDialog(
                            R.string.auto_start_permission,
                            "该权限是定制安卓中特有的权限，其允许应用在后台启动服务，应用范围如下：\n" +
                                    "- 确保定时任务能够在后台自动执行\n",
                            () -> PermissionHelper.requestAutoStartPermission(this)
                    )
            );
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
                R.drawable.outline_battery_android_4_24,
                RadiusStyle.MIDDLE
        );
        battery.setFunctionListener(v -> showExplanationDialog(
                R.string.ignore_battery_optimization,
                "将电池优化策略设置为“无限制”能够一定程度保障后台自动任务执行。",
                () -> PermissionHelper.requestBatteryIgnoration(this)
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
        alarm.setFunctionListener(v -> showExplanationDialog(
                R.string.alarm_permission,
                "该权限允许应用执行某些定时任务，以实现一些自动化功能，应用范围如下：\n" +
                        "- 根据用户设置自动检测当天是否有日记并发送通知提醒\n",
                () -> PermissionHelper.requestPermission(
                        this,
                        null,
                        new PermissionHelper.PermissionCallback() {
                            @Override
                            public void allGranted(List<IPermission> permissionList) {
                                Toast.makeText(PermissionManageActivity.this, "权限已授予", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void requested(List<IPermission> grantedList, List<IPermission> deniedList) {
                                boolean noDenied = deniedList.isEmpty();
                                boolean noGranted = grantedList.isEmpty();
                                if (noDenied && !noGranted) {
                                    Toast.makeText(PermissionManageActivity.this, "权限已授予", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PermissionManageActivity.this, "请求被拒绝", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        PermissionLists.getScheduleExactAlarmPermission()
                )
        ));
        alarm.setOnLongClickListener(view -> {
            WandererJournal.lockLifecycleObserver(true);
            XXPermissions.startPermissionActivity(this, PermissionLists.getScheduleExactAlarmPermission());
            return true;
        });
    }

    /**
     * 显示权限解释对话框
     *
     * @param title   对话框标题
     * @param message 对话框内容，支持Markdown格式
     * @param action  点击确定按钮后执行的操作
     */
    private void showExplanationDialog(@StringRes int title, String message, Runnable action) {
        new MarkdownDialogBuilder(this, getString(title), message)
                .setPositiveButton("前往设置", (dialogInterface, i) -> action.run())
                .setNegativeButton("取消", null)
                .show();
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