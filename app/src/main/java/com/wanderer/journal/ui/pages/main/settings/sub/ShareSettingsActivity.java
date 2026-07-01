package com.wanderer.journal.ui.pages.main.settings.sub;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.enums.RadiusStyle;
import com.wanderer.journal.data.save.preference.ShareSettingsPreference;
import com.wanderer.journal.databinding.ActivityShareSettingsBinding;
import com.wanderer.journal.ui.others.dialogs.EditTextDialogBuilder;
import com.wanderer.journal.ui.pages.main.settings.components.SettingClickableTextView;
import com.wanderer.journal.ui.pages.main.settings.components.SettingSwitchView;

public class ShareSettingsActivity extends AppCompatActivity {
    private ActivityShareSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShareSettingsBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        initViews();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        initContentSwitches();

        //分享图片底部文本
        SettingClickableTextView bottomTextOption = new SettingClickableTextView(
                this,
                binding.pictureBottomTextOption,
                R.string.share_picture_bottom_text,
                "设置分享图片的底部文本",
                R.drawable.outline_text_ad_24,
                RadiusStyle.SINGLE
        );
        bottomTextOption.setFunctionListener(view -> {
            String bottomText = ShareSettingsPreference.getPictureBottomText(this);
            new EditTextDialogBuilder(
                    this,
                    ContextCompat.getString(this, R.string.share_picture_bottom_text),
                    "底部文本",
                    bottomText
            )
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", inputStr -> {
                        ShareSettingsPreference.setPictureBottomText(this, inputStr);
                        Toast.makeText(this, "底部文本已修改", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
    }

    /**
     * 初始化分享内容开关
     */
    private void initContentSwitches() {
        //媒体
        boolean enableMedia = ShareSettingsPreference.getSwitchStat(this, ShareSettingsPreference.KEY_MEDIA);
        SettingSwitchView mediaSwitch = new SettingSwitchView(
                this,
                binding.mediaSwitch,
                R.string.include_media,
                "打开后分享内容将包含媒体预览图",
                R.drawable.outline_perm_media_24,
                RadiusStyle.TOP
        );
        mediaSwitch.setChecked(enableMedia);
        mediaSwitch.setFunctionListener((compoundButton, b) ->
                ShareSettingsPreference.setSwitchStat(this, ShareSettingsPreference.KEY_MEDIA, b)
        );

        //情绪标签
        boolean enableEmotion = ShareSettingsPreference.getSwitchStat(this, ShareSettingsPreference.KEY_EMOTION);
        SettingSwitchView emotionSwitch = new SettingSwitchView(
                this,
                binding.emotionSwitch,
                R.string.emotion_tag,
                "打开后分享内容将包含情绪标签",
                R.drawable.outline_mood_24,
                RadiusStyle.MIDDLE
        );
        emotionSwitch.setChecked(enableEmotion);
        emotionSwitch.setFunctionListener((compoundButton, b) ->
                ShareSettingsPreference.setSwitchStat(this, ShareSettingsPreference.KEY_EMOTION, b)
        );

        //时间
        boolean enableTime = ShareSettingsPreference.getSwitchStat(this, ShareSettingsPreference.KEY_TIME);
        SettingSwitchView timeSwitch = new SettingSwitchView(
                this,
                binding.timeSwitch,
                R.string.paragraph_time,
                "打开后分享内容将包括段落时间",
                R.drawable.outline_nest_clock_farsight_analog_24,
                RadiusStyle.BOTTOM
        );
        timeSwitch.setChecked(enableTime);
        timeSwitch.setFunctionListener((compoundButton, b) ->
                ShareSettingsPreference.setSwitchStat(this, ShareSettingsPreference.KEY_TIME, b)
        );
    }
}