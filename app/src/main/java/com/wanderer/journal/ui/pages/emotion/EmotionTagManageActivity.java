package com.wanderer.journal.ui.pages.emotion;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.databinding.ActivityEmotionTagManageBinding;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;

import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EmotionTagManageActivity extends AppCompatActivity {
    private ActivityEmotionTagManageBinding binding;    //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmotionTagManageBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            binding.recycler.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //添加按钮
        binding.addFab.setOnClickListener(view -> {
            Intent skip2EmotionTagAdd = new Intent(this, EmotionTagInputActivity.class);
            startActivity(skip2EmotionTagAdd);
        });
        ViewEdgeHelper.setMarginToNavigation(binding.addFab, this); //确保永远与底部导航栏有一定距离
        AppearanceAnimationHelper.attachMorphAnimation(binding.addFab);

        //情绪标签列表
        EmotionTagAdapter adapter = new EmotionTagAdapter(
                emotionTag -> {
                    Intent skip2EmotionTagModify = new Intent(this, EmotionTagInputActivity.class);
                    Bundle bundle = getEmotionTagModifyBundle(emotionTag);
                    skip2EmotionTagModify.putExtras(bundle);
                    startActivity(skip2EmotionTagModify);
                },
                this::showEmotionTagPopupMenu
        );
        binding.recycler.setAdapter(adapter);
        EmotionTagDao emotionTagDao = DiaryDatabase.getInstance(this).emotionTagDao();
        disposable.add(emotionTagDao.getAllEmotionTagFlowable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        emotionTagList -> {
                            adapter.submitList(emotionTagList);

                            if (emotionTagList.isEmpty()) {
                                binding.emptyText.setVisibility(View.VISIBLE);
                            } else {
                                binding.emptyText.setVisibility(View.GONE);
                            }
                        }
                )
        );
    }

    /**
     * 将情绪标签实体的属性放到{@link Bundle}中，以便传递给编辑界面
     *
     * @param emotionTag 被传递的情绪标签
     * @return 装有情绪标签数据的{@link Bundle}
     */
    @NonNull
    private static Bundle getEmotionTagModifyBundle(@NonNull EmotionTagEntity emotionTag) {
        Bundle bundle = new Bundle();

        long emotionTagId = emotionTag.getEmotionId();
        bundle.putLong(KeyStrings.EMOTION_TAG_ID.getS(), emotionTagId); //情绪标签 ID
        String name = emotionTag.getName();
        bundle.putString(KeyStrings.EMOTION_TAG_NAME.getS(), name);     //情绪标签名称
        String description = emotionTag.getDescription();
        bundle.putString(KeyStrings.EMOTION_TAG_DESCRIPTION.getS(), description);   //情绪标签描述
        int emotionTypeOrdinal = emotionTag.getType();
        bundle.putInt(KeyStrings.EMOTION_TAG_TYPE.getS(), emotionTypeOrdinal);  //情绪标签种类
        return bundle;
    }

    /**
     * 删除情绪标签
     *
     * @param emotionTag 待删除的情绪标签
     */
    private void deleteEmotionTag(EmotionTagEntity emotionTag) {
        EmotionTagDao emotionTagDao = DiaryDatabase.getInstance(this).emotionTagDao();
        disposable.add(emotionTagDao.deleteEmotionTagCompletable(emotionTag)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> Toast.makeText(this, "情绪标签删除成功", Toast.LENGTH_SHORT).show(),
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 显示PopupMenu
     *
     * @param emotionTag 需要被操作的情绪标签
     * @param view       PopupMenu绑定的视图
     */
    private void showEmotionTagPopupMenu(EmotionTagEntity emotionTag, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.menu_emotion_tag_edit, popupMenu.getMenu());

        //设置监听
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_emotion_tag) {
                //获取段落数量
                EmotionTagDao dao = DiaryDatabase
                        .getInstance(EmotionTagManageActivity.this)
                        .emotionTagDao();
                disposable.add(dao.getParagraphCountSingleByEmotionTagId(emotionTag.getEmotionId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                count -> {
                                    //生成对话框消息
                                    String message;
                                    if (count == 0) {
                                        message = "该标签没有被使用，确认删除它吗？";
                                    } else {
                                        message = String.format(
                                                Locale.getDefault(),
                                                "该标签被%d条段落使用，确认删除它吗？",
                                                count
                                        );
                                    }

                                    //显示对话框
                                    new MaterialAlertDialogBuilder(EmotionTagManageActivity.this)
                                            .setTitle(R.string.delete_emotion_tag)
                                            .setMessage(message)
                                            .setPositiveButton(
                                                    "确定",
                                                    (dialogInterface, i) ->
                                                            deleteEmotionTag(emotionTag)
                                            )
                                            .setNegativeButton("取消", null)
                                            .show();
                                }
                        )
                );

                return true;
            }
            return false;
        });

        popupMenu.show();
    }
}