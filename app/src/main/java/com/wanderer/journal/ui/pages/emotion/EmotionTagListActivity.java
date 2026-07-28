package com.wanderer.journal.ui.pages.emotion;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.wanderer.journal.data.save.db.services.EmotionTagService;
import com.wanderer.journal.databinding.ActivityEmotionTagListBinding;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.databinding.ViewHolderSeparatorTextChipBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderItemDecoration;
import com.wanderer.journal.ui.others.dialogs.MarkdownDialogBuilder;

import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EmotionTagListActivity extends AppCompatActivity {
    private ActivityEmotionTagListBinding binding;    //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmotionTagListBinding.inflate(getLayoutInflater());

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

        //功能说明按钮
        binding.helpBtn.setOnClickListener(view -> {
            final String EXPLANATION = "### 1. 主要功能\n" +
                    "情绪标签用于为日记段落标记情绪，方便阅读时了解当时的心情，同时也可作为段落筛选条件实现快速查找。\n" +
                    "\n" +
                    "### 2. 使用方法\n" +
                    "以下场景可以使用到情绪标签：\n" +
                    "\n" +
                    "- 在`读日记`和`写日记`界面点击段落，在弹出的菜单中点击“修改情绪标签”为段落标记情绪标签；\n" +
                    "- 在`读日记`界面中搜索段落时通过情绪标签缩小搜索范围。" +
                    "\n" +
                    "### 3. 强烈程度\n" +
                    "\n" +
                    "- 当`日记段落`被`情绪标签`标记时，可以选择情绪标签的强烈程度以反映了当时的`心情`；\n" +
                    "> 例：某个段落记录了特别令人高兴的事情时，可以为其标记“高兴”，并将强烈程度设置为5。\n" +
                    "- 情绪标签的强烈程度会以`罗马数字`的形式显示在`标签名称`后面。\n" +
                    "> 例：某个段落被标记为“伤心”，且该标签强烈程度为3，在段落内容下方会显示“伤心 III”字样。\n";
            new MarkdownDialogBuilder(this, "功能介绍", EXPLANATION)
                    .setNegativeButton("关闭", null)
                    .show();
        });

        //添加按钮
        binding.addFab.setOnClickListener(view -> {
            Intent skip2EmotionTagAdd = new Intent(this, EmotionTagInputActivity.class);
            startActivity(skip2EmotionTagAdd);
        });
        AppearanceHelper.setMarginToNavigation(binding.addFab, this); //确保永远与底部导航栏有一定距离
        AppearanceHelper.attachMorphAnimation(binding.addFab);

        //情绪标签列表
        EmotionTagListAdapter adapter = new EmotionTagListAdapter(
                (emotionTag, anchor) -> {
                    Intent skip2EmotionTagModify = new Intent(this, EmotionTagInputActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong(KeyStrings.EMOTION_TAG_ID.getS(), emotionTag.getEmotionId());
                    skip2EmotionTagModify.putExtras(bundle);
                    startActivity(skip2EmotionTagModify);
                },
                this::showEmotionTagPopupMenu
        );
        binding.recycler.setAdapter(adapter);
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        disposable.add(EmotionTagService.getAllEmotionTagWithSeparator(db)
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
        StickyHeaderItemDecoration<ViewHolderSeparatorTextChipBinding> decoration = new StickyHeaderItemDecoration<>(
                adapter,
                ViewHolderSeparatorTextChipBinding::inflate,
                (sBinding, text) -> sBinding.separatorText.setText(text)
        );
        binding.recycler.addItemDecoration(decoration);
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
                        .getInstance(EmotionTagListActivity.this)
                        .emotionTagDao();
                disposable.add(dao.getParagraphCountSingleById(emotionTag.getEmotionId())
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
                                    new MaterialAlertDialogBuilder(EmotionTagListActivity.this)
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