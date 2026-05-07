package com.wanderer.journal.ui.pages.main.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.preference.AppSettingsPreference;
import com.wanderer.journal.databinding.FragmentSettingsBinding;
import com.wanderer.journal.enums.options.ThemeMode;
import com.wanderer.journal.helpers.appearance.ThemeHelper;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingClickableTextView;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingOptionViewBase;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingSwitchView;

import java.util.Arrays;

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
                SettingOptionViewBase.RadiusStyle.TOP
        );
        themeModeOption.setFunctionListener(v -> showThemeModeSelectDialog());

        //动态配色
        SettingSwitchView dynamicColorOption = new SettingSwitchView(
                requireContext(),
                binding.dynamicColorOption,
                R.string.dynamic_color,
                "将壁纸颜色作为APP主题色",
                R.drawable.outline_colorize_24,
                SettingOptionViewBase.RadiusStyle.BOTTOM
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