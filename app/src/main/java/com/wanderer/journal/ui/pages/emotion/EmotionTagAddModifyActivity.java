package com.wanderer.journal.ui.pages.emotion;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.R;
import com.wanderer.journal.databinding.ActivityEmotionTagAddModifyBinding;
import com.wanderer.journal.enums.KeyStrings;

import java.util.List;

public class EmotionTagAddModifyActivity extends AppCompatActivity {
    private ActivityEmotionTagAddModifyBinding binding; //绑定的XML布局
    private boolean isModifyMode;                       //是否为编辑模式
    private long emotionTagId;                          //正在编辑的情绪标签的 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmotionTagAddModifyBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //设置键盘动画监听器
        ViewCompat.setWindowInsetsAnimationCallback(binding.getRoot(), new WindowInsetsAnimationCompat.Callback(
                WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP
        ) {
            @NonNull
            @Override
            public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                // 获取当前帧键盘（IME）和系统栏的高度
                Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                // 计算键盘弹起的高度（减去底部导航栏的高度，防止重复偏移）
                int keyboardHeight = Math.max(0, imeInsets.bottom);
                binding.getRoot().setPadding(systemBars.left, systemBars.top, systemBars.right, keyboardHeight);

                return insets;
            }

            @Override
            public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                super.onEnd(animation);
            }
        });

        receiveIntent();
        initViews();
    }

    /**
     * 接收父界面传递的数据
     */
    private void receiveIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        //修改标题和修改标志位
        binding.toolbar.setTitle(R.string.modify_emotion_tag);
        isModifyMode = true;

        //情绪标签 ID
        emotionTagId = bundle.getLong(KeyStrings.EMOTION_TAG_ID.getS());
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //TODO:完成初始化方法和布局
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());
    }
}