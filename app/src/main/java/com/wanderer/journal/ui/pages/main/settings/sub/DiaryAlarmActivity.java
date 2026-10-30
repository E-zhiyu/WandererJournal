package com.wanderer.journal.ui.pages.main.settings.sub;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hjq.permissions.XXPermissions;
import com.hjq.permissions.permission.PermissionLists;
import com.hjq.permissions.permission.base.IPermission;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.classes.permission.PermissionDescription;
import com.wanderer.journal.auxiliary.enums.RadiusStyle;
import com.wanderer.journal.data.save.preference.DiaryAlarmPreference;
import com.wanderer.journal.databinding.ActivityDiaryAlarmBinding;
import com.wanderer.journal.helpers.PermissionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.helpers.appearance.VisibilityHelper;
import com.wanderer.journal.helpers.time.AlarmHelper;
import com.wanderer.journal.helpers.time.DateTimePickerHelper;
import com.wanderer.journal.ui.others.adapters.AlarmTimeAdapter;
import com.wanderer.journal.ui.pages.main.settings.components.SettingClickableTextView;
import com.wanderer.journal.ui.pages.main.settings.components.SettingSwitchView;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DiaryAlarmActivity extends AppCompatActivity {
    private ActivityDiaryAlarmBinding binding;  //绑定的 XML 布局
    private AlarmTimeAdapter adapter;           //提醒时间显示适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiaryAlarmBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.scrollView.setPadding(
                    0,
                    0,
                    0,
                    systemBars.bottom + AppearanceHelper.dpToPx(this, 15)
            );
            return insets;
        });

        initViews();

        //申请权限
        PermissionHelper.requestPermission(
                this,
                new PermissionDescription(permissionList -> {
                    StringBuilder message = new StringBuilder();

                    if (XXPermissions.containsPermission(permissionList, PermissionLists.getPostNotificationsPermission())) {
                        message.append("请授予通知权限用于发生提醒通知。\n");
                    }
                    if (XXPermissions.containsPermission(permissionList, PermissionLists.getScheduleExactAlarmPermission())) {
                        message.append("请授予精确闹钟权限以允许应用在指定时刻发送通知提醒。\n");
                    }

                    return message.toString();
                }),
                new PermissionHelper.PermissionCallback() {
                    @Override
                    public void allGranted(List<IPermission> permissionList) {
                    }

                    @Override
                    public void requested(List<IPermission> grantedList, List<IPermission> deniedList) {
                        boolean noDenied = deniedList.isEmpty();

                        if (!noDenied) {
                            StringBuilder message = new StringBuilder();

                            //逐个权限添加通知
                            if (XXPermissions.containsPermission(deniedList, PermissionLists.getPostNotificationsPermission())) {
                                message.append("请授予通知权限用于发生提醒通知。\n");
                            }
                            if (XXPermissions.containsPermission(deniedList, PermissionLists.getScheduleExactAlarmPermission())) {
                                message.append("请授予精确闹钟权限以允许应用在指定时刻发送通知提醒。\n");
                            }

                            new MaterialAlertDialogBuilder(DiaryAlarmActivity.this)
                                    .setTitle("请求被拒绝")
                                    .setMessage(message)
                                    .setPositiveButton("前往设置", (dialog, which) ->
                                            XXPermissions.startPermissionActivity(DiaryAlarmActivity.this, deniedList)
                                    )
                                    .setNegativeButton("取消", null)
                                    .show();
                        }
                    }
                },
                PermissionLists.getPostNotificationsPermission(),
                PermissionLists.getScheduleExactAlarmPermission()
        );
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //开关
        boolean isSwitchOpened = DiaryAlarmPreference.getSwitchStat(this);
        SettingSwitchView diaryAlarmSwitch = getSettingSwitchView(isSwitchOpened);

        //提醒时间 RecyclerView
        adapter = new AlarmTimeAdapter(time -> {
            if (adapter == null) return;

            //生成移除了时间的列表并保存
            List<LocalTime> currentList = adapter.getCurrentList();
            List<LocalTime> removedTimeList = new ArrayList<>(currentList);
            removedTimeList.remove(time);
            DiaryAlarmPreference.setAlarmTime(this, removedTimeList);

            //更新 UI
            adapter.submitList(removedTimeList);

            //更新定时任务
            if (diaryAlarmSwitch.getFunctionComponent().isChecked()) {
                AlarmHelper.setDiaryAlarm(this);
            }

            if (removedTimeList.isEmpty()) {
                setAlarmComponentsVisibility(false, true);
            }
        });
        List<LocalTime> savedTimeList = DiaryAlarmPreference.getAlarmTime(this);
        adapter.submitList(savedTimeList);
        binding.alarmTimeRecycler.setAdapter(adapter);
        setAlarmComponentsVisibility(!savedTimeList.isEmpty(), false);

        //添加时间
        SettingClickableTextView addAlarmTimeOption = new SettingClickableTextView(
                this,
                binding.addAlarmTimeOption,
                R.string.add_alarm_time,
                "添加日记检查触发的具体时间",
                R.drawable.baseline_add_alarm_24,
                RadiusStyle.BOTTOM
        );
        addAlarmTimeOption.setFunctionListener(view -> DateTimePickerHelper.selectTime(
                LocalDateTime.now(),
                getSupportFragmentManager(),
                timePicker -> {
                    //生成时间
                    LocalTime selectedTime = LocalTime.of(
                            timePicker.getHour(),
                            timePicker.getMinute()
                    );

                    //判断是否在时间列表中
                    List<LocalTime> currentTimeList = adapter.getCurrentList();
                    if (currentTimeList.contains(selectedTime)) {
                        Toast.makeText(this, "不可添加重复的时间", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //保存时间数据
                    List<LocalTime> timeList = new ArrayList<>(currentTimeList);
                    timeList.add(selectedTime);
                    timeList.sort(null);
                    DiaryAlarmPreference.setAlarmTime(this, timeList);

                    //更新 UI 并提示
                    adapter.submitList(timeList);
                    Toast.makeText(this, "提醒时间添加成功", Toast.LENGTH_SHORT).show();

                    //更新定时任务
                    if (diaryAlarmSwitch.getFunctionComponent().isChecked()) {
                        AlarmHelper.setDiaryAlarm(this);
                    }

                    setAlarmComponentsVisibility(true, true);
                }
        ));
    }

    /**
     * 获取开关设置项
     *
     * @param isSwitchOpened 开关是否打开
     * @return 开关设置项实例
     */
    @NonNull
    private SettingSwitchView getSettingSwitchView(boolean isSwitchOpened) {
        SettingSwitchView diaryAlarmSwitch = new SettingSwitchView(
                this,
                binding.diaryAlarmSwitch,
                R.string.total_switch,
                "忘记写日记时发送通知提醒",
                R.drawable.outline_alarm_24,
                RadiusStyle.TOP
        );
        diaryAlarmSwitch.setChecked(isSwitchOpened);
        diaryAlarmSwitch.setFunctionListener((compoundButton, b) -> {
            //打开开关时检查权限
            if (b) {
                //判断权限授予情况
                boolean isAlarmGranted = XXPermissions.isGrantedPermission(this, PermissionLists.getScheduleExactAlarmPermission());
                boolean isNotificationGranted = XXPermissions.isGrantedPermission(this, PermissionLists.getPostNotificationsPermission());

                //若权限未授予则拒绝打开
                if (!isAlarmGranted || !isNotificationGranted) {
                    diaryAlarmSwitch.setChecked(false);
                    Toast.makeText(this, "请授予通知权限和精确闹钟权限", Toast.LENGTH_SHORT).show();
                    DiaryAlarmPreference.setSwitchStat(this, false);
                    return;
                }
            }

            //保存开关状态
            DiaryAlarmPreference.setSwitchStat(this, b);

            //安排提醒闹钟
            if (b) {
                AlarmHelper.setDiaryAlarm(this);
            } else {
                AlarmHelper.cancelDiaryAlarm(this);
            }
        });
        return diaryAlarmSwitch;
    }

    /**
     * 切换日记提醒时间组件的可见性
     *
     * @param isVisible     是否可见
     * @param needAnimation 是否需要动画效果
     */
    private void setAlarmComponentsVisibility(boolean isVisible, boolean needAnimation) {
        if (needAnimation) {
            VisibilityHelper.toggleVisibilityWithFade(binding.alarmTimeTitle, isVisible);
            VisibilityHelper.toggleVisibilityWithFade(binding.alarmTimeRecycler, isVisible);
        } else {
            if (isVisible) {
                binding.alarmTimeTitle.setVisibility(View.VISIBLE);
                binding.alarmTimeRecycler.setVisibility(View.VISIBLE);
            } else {
                binding.alarmTimeTitle.setVisibility(View.GONE);
                binding.alarmTimeRecycler.setVisibility(View.GONE);
            }
        }
    }
}