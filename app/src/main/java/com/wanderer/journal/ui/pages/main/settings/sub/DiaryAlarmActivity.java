package com.wanderer.journal.ui.pages.main.settings.sub;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.enums.RadiusStyle;
import com.wanderer.journal.data.save.preference.DiaryAlarmPreference;
import com.wanderer.journal.databinding.ActivityDiaryAlarmBinding;
import com.wanderer.journal.helpers.PermissionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
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
    private PermissionHelper permissionHelper;  //权限申请帮助器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiaryAlarmBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        addPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        permissionHelper.start();
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
                boolean isAlarmGranted = PermissionHelper.SpecialPermissionType.ALARM.isGranted(this);
                boolean isNotificationGranted;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    isNotificationGranted = PermissionHelper.isRuntimePermissionGranted(
                            Manifest.permission.POST_NOTIFICATIONS,
                            this
                    );
                } else {
                    isNotificationGranted = true;
                }

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
                //有自启动权限的系统弹出提示
                if (PermissionHelper.isAutoStartDefined()) {
                    Toast.makeText(this, "提醒已开启，请确保已授予自启动权限", Toast.LENGTH_SHORT).show();
                }
                AlarmHelper.setDiaryAlarm(this);
            } else {
                AlarmHelper.cancelDiaryAlarm(this);
            }
        });
        return diaryAlarmSwitch;
    }

    /**
     * 向 PermissionHelper 提交需要申请的权限
     */
    private void addPermissions() {
        permissionHelper = new PermissionHelper(this);

        //通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionHelper.addPermission(
                    Manifest.permission.POST_NOTIFICATIONS,
                    "通知权限：允许应用发送通知提醒"
            );
        }

        //自启动权限
        permissionHelper.addPermission(
                PermissionHelper.SpecialPermissionType.AUTO_START,
                "自启动权限",
                "请授予自启动权限以允许在后台触发定时任务"
        );

        //精确闹钟权限
        permissionHelper.addPermission(
                PermissionHelper.SpecialPermissionType.ALARM,
                "精确闹钟权限",
                "请授予精确闹钟权限以允许应用在指定时刻发送通知提醒"
        );
    }

    /**
     * 切换日记提醒时间组件的可见性
     *
     * @param isVisible     是否可见
     * @param needAnimation 是否需要动画效果
     */
    private void setAlarmComponentsVisibility(boolean isVisible, boolean needAnimation) {
        if (needAnimation) {
            AppearanceAnimationHelper.setVisibilityWithFade(binding.alarmTimeTitle, isVisible);
            AppearanceAnimationHelper.setVisibilityWithFade(binding.alarmTimeRecycler, isVisible);
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