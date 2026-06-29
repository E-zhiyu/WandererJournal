package com.wanderer.journal.ui.pages.emotion;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.auxiliary.enums.text.EmotionType;
import com.wanderer.journal.databinding.ActivityEmotionTagInputBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.ImmHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.ui.others.adapters.NoFilteringArrayAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EmotionTagInputActivity extends AppCompatActivity {
    private ActivityEmotionTagInputBinding binding;         //绑定的XML布局
    private Bundle initBundle = null;                       //传递初始化数据的数据包
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
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            binding.linearLayout.setPadding(
                    AppearanceHelper.dpToPx(this, 10),
                    AppearanceHelper.dpToPx(this, 10),
                    AppearanceHelper.dpToPx(this, 10),
                    imeInsets.bottom + AppearanceHelper.dpToPx(this, 10)
            );

            return insets;
        });

        initBundle = getIntent().getExtras();
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
        disposable.dispose();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //初始化部分视图的内容
        if (initBundle != null) {
            binding.toolbar.setTitle(R.string.modify_emotion_tag);
            String name = initBundle.getString(KeyStrings.EMOTION_TAG_NAME.getS());
            binding.nameInput.setText(name);                                    //名称
            String description = initBundle.getString(KeyStrings.EMOTION_TAG_DESCRIPTION.getS());
            binding.descriptionInput.setText(description);                      //描述
            int emotionTypeOrdinal = initBundle.getInt(KeyStrings.EMOTION_TAG_TYPE.getS());
            emotionType = EmotionType.values()[emotionTypeOrdinal];             //情绪标签种类
        }

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
        String name = String.valueOf(binding.nameInput.getText()).trim();
        String description = String.valueOf(binding.descriptionInput.getText()).trim();

        //实例化情绪标签并获取Dao接口
        EmotionTagEntity emotionTag = new EmotionTagEntity(name, description, emotionType.ordinal());
        EmotionTagDao dao = DiaryDatabase.getInstance(this).emotionTagDao();

        //保存到数据库
        if (initBundle != null) {
            long emotionTagId = initBundle.getLong(KeyStrings.EMOTION_TAG_ID.getS());
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