package com.wanderer.journal.ui.pages.main.settings.sub;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.LifecycleManager;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.enums.RadiusStyle;
import com.wanderer.journal.databinding.ActivityPermissionManageBinding;
import com.wanderer.journal.databinding.ViewMarkdownTextBinding;
import com.wanderer.journal.helpers.PermissionHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingClickableTextView;

import io.noties.markwon.Markwon;

public class PermissionManageActivity extends AppCompatActivity {
    private ActivityPermissionManageBinding binding;                //绑定的XML视图
    private final ActivityResultLauncher<String> runtimeLauncher =  //申请运行时权限的启动器
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Toast.makeText(this, "成功授予该权限", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "该权限被拒绝，请长按进入系统设置后授权", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
    private SettingClickableTextView camera, notification, alarm;   //权限申请视图

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
                    systemBars.bottom + ViewEdgeHelper.dpToPx(this, 15)
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
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
                () -> requestRuntimePermission(Manifest.permission.CAMERA)
        ));
        camera.setOnLongClickListener(view -> {
            Intent skip2Settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            skip2Settings.setData(uri);
            startActivity(skip2Settings);
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
                        () -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestRuntimePermission(Manifest.permission.POST_NOTIFICATIONS);
                            } else {
                                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
                            }
                        }
                )
        );
        notification.setOnLongClickListener(view -> {
            Intent skip2Settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            skip2Settings.setData(uri);
            startActivity(skip2Settings);
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
                                    "- 在退出应用后自动启动通知监听服务，确保自动记账功能能够运行\n",
                            () -> {
                                Intent skip2AutoStartPermission = PermissionHelper.buildAutoStartPermissionIntent(this);
                                LifecycleManager.startExternalActivity(this, skip2AutoStartPermission);
                            }
                    )
            );
        } else {
            binding.autoStartOption.getRoot().setVisibility(View.GONE);
        }

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
                () -> {
                    Intent skip2ExactAlarm = PermissionHelper.buildExactAlarmIntent(this);
                    LifecycleManager.startExternalActivity(this, skip2ExactAlarm);
                }
        ));
    }

    /**
     * 显示权限解释对话框
     *
     * @param title   对话框标题
     * @param message 对话框内容，支持Markdown格式
     * @param action  点击确定按钮后执行的操作
     */
    private void showExplanationDialog(@StringRes int title, String message, Runnable action) {
        //获取自定义弹窗视图
        ViewMarkdownTextBinding markdownTextBinding = ViewMarkdownTextBinding.inflate(getLayoutInflater());

        // 渲染 Markdown 文本
        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(markdownTextBinding.mdTextviewInDialog, message);

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(markdownTextBinding.getRoot())
                .setPositiveButton("前往设置", (dialog, which) -> action.run())
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 申请运行时权限
     *
     * @param permission 运行时权限
     */
    private void requestRuntimePermission(String permission) {
        if (PermissionHelper.isRuntimePermissionGranted(permission, this)) {
            Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
        } else {
            runtimeLauncher.launch(permission);
        }
    }

    /**
     * 刷新权限授予情况指示器
     */
    private void refreshPermissionStat() {
        final String GRANTED = "已授予";
        final String NOT_GRANTED = "未授予";

        //相机权限
        boolean isCameraGranted = PermissionHelper.isRuntimePermissionGranted(Manifest.permission.CAMERA, this);
        if (isCameraGranted) {
            camera.getFunctionComponent().setText(GRANTED);
        } else {
            camera.getFunctionComponent().setText(NOT_GRANTED);
        }

        //通知权限
        boolean isNotificationGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                PermissionHelper.isRuntimePermissionGranted(Manifest.permission.POST_NOTIFICATIONS, this);
        if (isNotificationGranted) {
            notification.getFunctionComponent().setText(GRANTED);
        } else {
            notification.getFunctionComponent().setText(NOT_GRANTED);
        }

        //精确闹钟权限
        boolean isAlarmGranted = PermissionHelper.SpecialPermissionType.ALARM.isGranted(this);
        if (isAlarmGranted) {
            alarm.getFunctionComponent().setText(GRANTED);
        } else {
            alarm.getFunctionComponent().setText(NOT_GRANTED);
        }
    }
}