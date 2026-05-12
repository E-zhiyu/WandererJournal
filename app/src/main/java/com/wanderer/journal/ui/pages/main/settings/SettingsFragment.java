package com.wanderer.journal.ui.pages.main.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.preference.AppSettingsPreference;
import com.wanderer.journal.data.save.preference.SecurityPreference;
import com.wanderer.journal.databinding.FragmentSettingsBinding;
import com.wanderer.journal.enums.RadiusStyle;
import com.wanderer.journal.enums.options.AuthOpportunity;
import com.wanderer.journal.enums.options.ThemeMode;
import com.wanderer.journal.helpers.BiometricHelper;
import com.wanderer.journal.helpers.appearance.ThemeHelper;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingClickableTextView;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingSpinnerView;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingSwitchView;
import com.wanderer.journal.ui.pages.main.settings.sub.AboutActivity;
import com.wanderer.journal.ui.pages.main.settings.sub.DataManageActivity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;    //绑定的XML布局

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //初始化软件设置
        initAppSettings();

        //数据管理设置
        SettingClickableTextView dataManage = new SettingClickableTextView(
                requireContext(),
                binding.dataManageOption,
                R.string.data_manage,
                "点击跳转数据管理界面",
                R.drawable.outline_database_24,
                RadiusStyle.SINGLE
        );
        dataManage.setFunctionListener(view -> {
            Intent skip2DataManage = new Intent(requireContext(), DataManageActivity.class);
            startActivity(skip2DataManage);
        });

        //初始化安全设置
        initSecuritySettings();

        //关于软件
        SettingClickableTextView aboutOption = new SettingClickableTextView(
                requireContext(),
                binding.aboutOption,
                R.string.about_software,
                null,
                R.drawable.outline_info_24,
                RadiusStyle.SINGLE
        );
        aboutOption.setFunctionListener(view -> {
            Intent skip2About = new Intent(requireContext(), AboutActivity.class);
            startActivity(skip2About);
        });
    }

    /**
     * 初始化软件设置
     */
    private void initAppSettings() {
        //主题模式
        SettingClickableTextView themeModeOption = new SettingClickableTextView(
                requireContext(),
                binding.themeModeOption,
                R.string.theme_mode,
                "切换深浅色模式",
                R.drawable.outline_dark_mode_24,
                RadiusStyle.TOP
        );
        themeModeOption.setFunctionListener(v -> showThemeModeSelectDialog());

        //动态配色
        SettingSwitchView dynamicColorOption = new SettingSwitchView(
                requireContext(),
                binding.dynamicColorOption,
                R.string.dynamic_color,
                "将壁纸颜色作为APP主题色",
                R.drawable.outline_colorize_24,
                RadiusStyle.BOTTOM
        );
        dynamicColorOption.setChecked(AppSettingsPreference.getDynamicColorStat(requireContext()));
        dynamicColorOption.setFunctionListener(
                (buttonView, isChecked) -> {
                    AppSettingsPreference.setDynamicColorStat(requireContext(), isChecked);
                    ThemeHelper.switchDynamicColorWithAnimation(requireActivity(), isChecked);
                }
        );
    }

    /**
     * 初始化安全设置
     */
    private void initSecuritySettings() {
        //身份验证开关
        SettingSwitchView authenticationSwitch = new SettingSwitchView(
                requireContext(),
                binding.authenticationSwitch,
                R.string.authentication,
                "进入APP时需要进行身份验证",
                R.drawable.outline_security_24,
                RadiusStyle.TOP
        );
        boolean isAuthOpened = SecurityPreference.getAuthSwitchStat(requireContext());
        authenticationSwitch.setChecked(isAuthOpened);
        authenticationSwitch.setFunctionListener(new CompoundButton.OnCheckedChangeListener() {
            private boolean isBlocked = false;  //监听器是否阻塞

            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
                //被阻塞时不执行操作，防止无限递归
                if (isBlocked) {
                    return;
                }

                String title = b ? "启用身份验证" : "关闭身份验证";

                BiometricHelper.showBiometricPrompt(
                        title,
                        "请验证您的身份",
                        requireActivity(),
                        new BiometricHelper.AuthCallback() {
                            @Override
                            public void onSuccess() {
                                SecurityPreference.setAuthSwitchStat(requireContext(), b);
                                Toast.makeText(requireContext(), b ? "已启用身份验证" : "已关闭身份验证", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(int errCode, CharSequence errStr) {
                                Toast.makeText(requireContext(), errStr, Toast.LENGTH_SHORT).show();

                                isBlocked = true;
                                if (errCode == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
                                        || errCode == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
                                ) {
                                    //设备无锁屏/不支持生物验证时关闭身份验证
                                    SecurityPreference.setAuthSwitchStat(requireContext(), false);

                                    //开关设置为关闭
                                    authenticationSwitch.setChecked(false);
                                } else {
                                    //开关设置为原始状态
                                    authenticationSwitch.setChecked(!b);
                                }
                                isBlocked = false;
                            }

                            @Override
                            public void onFailed() {

                            }
                        }
                );
            }
        });

        //身份验证时机
        SettingSpinnerView authenticationOpportunity = new SettingSpinnerView(
                requireContext(),
                binding.authenticationOpportunityOption,
                R.string.authentication_opportunity,
                "进行身份验证的时机",
                R.drawable.outline_safety_check_24,
                RadiusStyle.MIDDLE
        );
        int opportunityCode = SecurityPreference.getAuthOpportunity(requireContext());
        authenticationOpportunity.setSpinnerText(AuthOpportunity.values()[opportunityCode].getTitle());
        authenticationOpportunity.setFunctionListener(view -> {
            PopupMenu opportunityMenu = new PopupMenu(requireContext(), authenticationOpportunity.getFunctionComponent());

            //初始化菜单项
            for (AuthOpportunity opportunity : AuthOpportunity.values()) {
                int groupId = opportunity.getGroupId();
                int itemId = opportunity.getItemId();
                int order = opportunity.getOrder();
                String title = opportunity.getTitle();
                opportunityMenu.getMenu().add(groupId, itemId, order, title);
            }

            //设置选项点击监听
            opportunityMenu.setOnMenuItemClickListener(item -> {
                //获取选项编号列表
                List<Integer> itemIdList = Arrays.stream(AuthOpportunity.values())
                        .map(AuthOpportunity::getItemId)
                        .collect(Collectors.toList());

                //判断是否选中
                if (itemIdList.contains(item.getItemId())) {
                    int index = itemIdList.indexOf(item.getItemId());
                    SecurityPreference.setAuthOpportunity(requireContext(), index);
                    authenticationOpportunity.setSpinnerText(item.getTitle());
                    return true;
                } else {
                    return false;
                }
            });

            opportunityMenu.show();
        });

        //最近任务隐藏
        SettingSwitchView hideRecentTask = new SettingSwitchView(
                requireContext(),
                binding.hideRecentTaskOption,
                R.string.hide_recent_task,
                "在最近任务列表中隐藏",
                R.drawable.outline_visibility_off_24,
                RadiusStyle.BOTTOM
        );
        boolean isHidden = SecurityPreference.getHideRecentTask(requireContext());
        hideRecentTask.setChecked(isHidden);
        hideRecentTask.setFunctionListener(
                (compoundButton, checked) ->
                        SecurityPreference.setHideRecentTask(checked, requireContext())
        );
    }

    /**
     * 显示主题模式选择对话框
     */
    private void showThemeModeSelectDialog() {
        String[] themeModeStr = Arrays.stream(ThemeMode.values())
                .map(ThemeMode::getTitle)
                .toArray(String[]::new);
        int themeMode = AppSettingsPreference.getThemeMode(requireContext());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("主题模式")
                .setSingleChoiceItems(themeModeStr, themeMode, (dialog, which) -> {
                    AppSettingsPreference.setThemeMode(requireContext(), which);
                    ThemeHelper.switchNightModeWithAnimation(requireActivity(), which);
                    dialog.dismiss();
                })
                .setNegativeButton("关闭", null)
                .show();
    }
}