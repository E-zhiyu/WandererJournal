package com.wanderer.journal.ui.pages.emotion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.auxiliary.enums.dropdown.EmotionType;
import com.wanderer.journal.databinding.ActivityEmotionTagInputBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.ImmHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.ui.others.adapters.NoFilteringArrayAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EmotionTagInputActivity extends AppCompatActivity {
    private ActivityEmotionTagInputBinding binding;         //绑定的XML布局
    private boolean isModifyMode = false;                   //是否为编辑模式
    private long emotionTagId = 0;                          //正在编辑的情绪标签的 ID
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表
    private EmotionType emotionType = EmotionType.NEUTRAL;  //情绪种类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmotionTagInputBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
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
                int keyboardHeight = Math.max(systemBars.bottom, imeInsets.bottom);
                binding.getRoot().setPadding(systemBars.left, 0, systemBars.right, keyboardHeight);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
        disposable.dispose();
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

        emotionTagId = bundle.getLong(KeyStrings.EMOTION_TAG_ID.getS());    //情绪标签 ID
        String name = bundle.getString(KeyStrings.EMOTION_TAG_NAME.getS());
        binding.nameInput.setText(name);                                    //名称
        String description = bundle.getString(KeyStrings.EMOTION_TAG_DESCRIPTION.getS());
        binding.descriptionInput.setText(description);                      //描述
        int emotionTypeOrdinal = bundle.getInt(KeyStrings.EMOTION_TAG_TYPE.getS());
        emotionType = EmotionType.values()[emotionTypeOrdinal];             //情绪标签种类
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //名称
        binding.nameInput.setOnFocusChangeListener((view, b) -> {
            if (b) {
                binding.nameLayout.setError(null);
            } else {
                String input = String.valueOf(binding.nameInput.getText());
                if (input.isEmpty()) {
                    binding.nameLayout.setError("名称不能为空");
                }
            }
        });
        ImmHelper.showImm(binding.nameInput);   //弹出输入法

        //情绪类型
        List<String> typeTitleList = Arrays.stream(EmotionType.values())
                .map(EmotionType::getTitle)
                .collect(Collectors.toList());
        NoFilteringArrayAdapter<String> typeAdapter = new NoFilteringArrayAdapter<>(this, typeTitleList);
        binding.typeInput.setAdapter(typeAdapter);
        binding.typeInput.setText(emotionType.getTitle());
        binding.typeInput.setOnItemClickListener(
                (adapterView, view, i, l) ->
                        emotionType = EmotionType.values()[i]
        );

        //确认按钮
        binding.confirmButton.setOnClickListener(view -> {
            String err = verifyInput();
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
                return;
            }

            onConfirm();
        });
        AppearanceAnimationHelper.attachMorphAnimation(binding.confirmButton);

        //取消按钮
        binding.cancelButton.setOnClickListener(view -> finish());
        AppearanceAnimationHelper.attachMorphAnimation(binding.cancelButton);
    }

    /**
     * 校验输入的内容
     *
     * @return 错误提示，无错误则返回 null
     */
    @Nullable
    private String verifyInput() {
        String err = null;

        if (String.valueOf(binding.nameInput.getText()).isEmpty()) {
            err = "名称不能为空";
            binding.nameLayout.setError(err);
        }

        return err;
    }

    /**
     * 确认按钮回调
     */
    private void onConfirm() {
        //获取输入内容
        String name = String.valueOf(binding.nameInput.getText());
        String description = String.valueOf(binding.descriptionInput.getText());

        //实例化情绪标签并获取Dao接口
        EmotionTagEntity emotionTag = new EmotionTagEntity(name, description, emotionType.ordinal());
        EmotionTagDao dao = DiaryDatabase.getInstance(this).emotionTagDao();

        //保存到数据库
        if (isModifyMode) {
            emotionTag.setEmotionId(emotionTagId);
            disposable.add(dao.updateEmotionTagCompletable(emotionTag)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "情绪标签修改成功", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        } else {
            disposable.add(dao.insertEmotionTagCompletable(emotionTag)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "成功添加情绪标签", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        }
    }
}