package com.wanderer.journal.ui.pages.life_note;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.classes.InfoShower;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.entities.LifeNoteEntity;
import com.wanderer.journal.data.save.db.entities.LifeNoteHistoryEntity;
import com.wanderer.journal.data.save.db.services.LifeNoteService;
import com.wanderer.journal.databinding.ActivityLifeNoteInputBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.helpers.appearance.VisibilityHelper;

import java.time.LocalDateTime;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LifeNoteInputActivity extends AppCompatActivity {
    private ActivityLifeNoteInputBinding binding;   //绑定的 XML 布局
    @Nullable
    private Bundle initBundle;                      //包含初始化数据的数据包
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLifeNoteInputBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            //滚动布局中的线性布局
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
        //工具栏
        if (initBundle != null) {
            binding.toolbar.setTitle(R.string.modify_life_note);

            //初始化输入框内容
            DiaryDatabase db = DiaryDatabase.getInstance(this);
            long noteId = initBundle.getLong(KeyStrings.LIFE_NOTE_ID.getS(), 0);
            disposable.add(db.lifeNoteDao().getLifeNoteOptionalSingleById(noteId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            noteOptional -> {
                                if (noteOptional.isEmpty()) return;

                                LifeNoteEntity lifeNote = noteOptional.get();
                                binding.insightInput.setText(lifeNote.getInsight());            //洞见
                                binding.elaborationInput.setText(lifeNote.getElaboration());    //阐述
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        }
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //洞见
        binding.insightInput.setOnFocusChangeListener((view, b) -> {
            if (b) {
                binding.insightLayout.setError(null);
            } else {
                String input = String.valueOf(binding.insightInput.getText());
                if (input.trim().isEmpty()) {
                    binding.insightLayout.setError("洞见不能为空");
                }
            }
        });

        //确认按钮
        binding.confirmButton.setOnClickListener(view -> {
            String err = verifyInput();
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
                return;
            }

            onConfirm();
        });

        //修改历史列表
        if (initBundle != null) {
            long noteId = initBundle.getLong(KeyStrings.LIFE_NOTE_ID.getS(), 0);
            LifeNoteHistoryListAdapter historyListAdapter = new LifeNoteHistoryListAdapter(
                    (entity, anchor) -> InfoShower.showLifeNoteHistory(this, entity),
                    this::showEmotionTagPopupMenu
            );
            binding.historyRecycler.setAdapter(historyListAdapter);
            DiaryDatabase db = DiaryDatabase.getInstance(this);
            disposable.add(db.lifeNoteDao().getLifeNoteHistoryFlowableByNoteId(noteId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            historyList -> {
                                historyListAdapter.submitList(historyList);
                                boolean isVisible = !historyList.isEmpty();
                                VisibilityHelper.toggleVisibilityWithFade(binding.historyRecycler, isVisible);
                                VisibilityHelper.toggleVisibilityWithFade(binding.modifyHistoryTitle, isVisible);
                                VisibilityHelper.toggleVisibilityWithFade(binding.inputHistoryDivider, isVisible);
                            }
                    )
            );
        }
    }

    /**
     * 校验输入的内容
     *
     * @return 错误提示，无错误则返回 null
     */
    @Nullable
    private String verifyInput() {
        String err = null;

        String insight = String.valueOf(binding.insightInput.getText()).trim();

        if (insight.trim().isEmpty()) {
            err = "洞见不能为空";
            binding.insightLayout.setError(err);
        }

        return err;
    }

    /**
     * 确认按钮点击回调
     */
    private void onConfirm() {
        //获取输入内容
        String insight = String.valueOf(binding.insightInput.getText()).trim();
        String elaboration = String.valueOf(binding.elaborationInput.getText()).trim();

        //实例化数据实体
        LocalDateTime dateTime = LocalDateTime.now();
        LifeNoteEntity lifeNote = new LifeNoteEntity(insight, elaboration, dateTime);

        //更新数据库
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        if (initBundle == null) {
            disposable.add(db.lifeNoteDao().insertLifeNote(lifeNote)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "成功添加人生笔记", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        } else {
            long noteId = initBundle.getLong(KeyStrings.LIFE_NOTE_ID.getS());
            lifeNote.setNoteId(noteId);
            disposable.add(LifeNoteService.modifyLifeNoteCompletable(db, lifeNote)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "成功修改人生笔记", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        }
    }

    /**
     * 显示PopupMenu
     *
     * @param history 需要被操作的历史记录
     * @param view    PopupMenu绑定的视图
     */
    private void showEmotionTagPopupMenu(LifeNoteHistoryEntity history, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.menu_life_note_history_edit, popupMenu.getMenu());

        //设置监听
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_life_note_history) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("删除历史记录")
                        .setMessage("确定要删除该历史记录吗？此操作无法撤销。")
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            DiaryDatabase db = DiaryDatabase.getInstance(this);
                            disposable.add(db.lifeNoteDao().deleteLifeNoteHistory(history)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(
                                            () -> Toast.makeText(this, "历史记录已删除", Toast.LENGTH_SHORT).show(),
                                            e -> ExceptionHelper.showExceptionDialog(this, e)
                                    )
                            );
                        })
                        .setNegativeButton("取消", null)
                        .show();

                return true;
            }
            return false;
        });

        popupMenu.show();
    }
}