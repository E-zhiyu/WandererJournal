package com.wanderer.journal.ui.pages.read;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.databinding.ActivityDiaryReadBinding;
import com.wanderer.journal.enums.KeyStrings;
import com.wanderer.journal.enums.LogTags;
import com.wanderer.journal.enums.TagStrings;
import com.wanderer.journal.helpers.time.DateTimePickerHelper;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphUiModel;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphViewModel;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphViewModelFactory;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphAdapter;
import com.wanderer.journal.ui.others.bottom.emotion.EmotionTagSelectBottomSheet;
import com.wanderer.journal.ui.pages.write.WriteActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;

public class DiaryReadActivity extends AppCompatActivity {
    private ActivityDiaryReadBinding binding;               //绑定的XML布局
    private LocalDate initDiaryDate = null;                 //初始页的日期
    private LocalDate pendingTargetDate = null;             //加载列表时需要跳转的日期
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiaryReadBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            binding.coordinatorLayout.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            //列表视图
            binding.contentRecycler.setPadding(
                    systemBars.left,
                    0,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
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
     * 接收 Intent 中传递的数据
     */
    private void receiveIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        //起始页日期
        String initDate = bundle.getString(KeyStrings.INIT_DATE.getS());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        initDiaryDate = LocalDate.parse(initDate, formatter);
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //搜索组件
        initSearchComponents();

        //日记段落列表
        initRecyclerView();
    }

    /**
     * 初始化搜索组件
     */
    private void initSearchComponents() {
        //先隐藏搜索框
        binding.appBarLayout.setExpanded(false, false);

        //TODO:完成剩下的
        binding.diaryContentSearchView.setupWithSearchBar(binding.contentSearchBar);
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        pendingTargetDate = initDiaryDate;

        //设置适配器
        ParagraphAdapter adapter = new ParagraphAdapter(
                (paragraph, view) -> {
                    PopupMenu menu = new PopupMenu(this, view, Gravity.END);
                    menu.getMenuInflater().inflate(R.menu.menu_paragraph_edit, menu.getMenu());

                    menu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_modify_content) {
                            Intent skip2Write = new Intent(DiaryReadActivity.this, WriteActivity.class);
                            Bundle bundle = new Bundle();

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            String date = paragraph.getCreateTime().format(formatter);
                            bundle.putString(KeyStrings.WRITE_DIARY_DATE.getS(), date); //段落所在的日期
                            bundle.putLong(KeyStrings.WRITE_MODIFY_PARAGRAPH_ID.getS(), paragraph.getParagraphId());    //段落 ID

                            skip2Write.putExtras(bundle);
                            startActivity(skip2Write);
                            return true;
                        } else if (item.getItemId() == R.id.action_modify_time) {
                            updateParagraphCreateTime(paragraph);
                            return true;
                        } else if (item.getItemId() == R.id.action_modify_emotion) {
                            modifyEmotion(paragraph);
                            return true;
                        } else if (item.getItemId() == R.id.action_delete_paragraph) {
                            deleteParagraph(paragraph);
                            return true;
                        } else {
                            return false;
                        }
                    });

                    menu.show();
                }
        );
        adapter.addLoadStateListener(loadStates -> {
            boolean isNotLoading = loadStates.getRefresh() instanceof LoadState.NotLoading;
            boolean endOfPaginationReached = loadStates.getAppend().getEndOfPaginationReached();

            if (isNotLoading && endOfPaginationReached) {
                if (adapter.getItemCount() == 0) {
                    binding.emptyText.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyText.setVisibility(View.GONE);
                }
            } else {
                binding.emptyText.setVisibility(View.GONE);
            }

            // 当刷新完成且数据已提交到 UI
            if (isNotLoading && pendingTargetDate != null) {
                // 遍历当前已加载的列表，找到对应的日期项
                int position = -1;
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    ParagraphUiModel item = adapter.peek(i); // 使用 peek 不触发分页加载
                    if (item instanceof ParagraphUiModel.Item) {
                        if (((ParagraphUiModel.Item) item).model.getParagraph().getCreateTime().toLocalDate().equals(pendingTargetDate)) {
                            position = i;
                            break;
                        }
                    }
                }

                if (position != -1) {
                    // 关键：强制滚动到该位置，并将偏移量设为 0 (置顶)
                    if (binding.contentRecycler.getLayoutManager() != null) {
                        ((LinearLayoutManager) binding.contentRecycler.getLayoutManager())
                                .scrollToPositionWithOffset(position, 0);
                        pendingTargetDate = null; // 跳转完成，清空标记
                    }
                }
            }
            return Unit.INSTANCE;
        });
        binding.contentRecycler.setAdapter(adapter);

        //监听数据库的响应
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphViewModelFactory factory = new ParagraphViewModelFactory(db);
        ParagraphViewModel viewModel = new ViewModelProvider(this, factory).get(ParagraphViewModel.class);
        disposable.add(viewModel.getPagingDataFlow(initDiaryDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pagingData ->
                        adapter.submitData(getLifecycle(), pagingData)
                )
        );
    }

    /**
     * 更新段落创建日期
     *
     * @param paragraph 原来的段落实例
     */
    private void updateParagraphCreateTime(@NonNull ParagraphEntity paragraph) {
        DateTimePickerHelper.selectDateTime(
                paragraph.getCreateTime(),
                getSupportFragmentManager(),
                timePicker -> {
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    LocalDateTime newDateTime = paragraph.getCreateTime()
                            .withHour(hour)
                            .withMinute(minute);

                    ParagraphEntity newParagraph = new ParagraphEntity(
                            paragraph.getParentDiaryId(),
                            paragraph.getContent(),
                            newDateTime
                    );
                    newParagraph.setParagraphId(paragraph.getParagraphId());

                    DiaryDatabase db = DiaryDatabase.getInstance(this);
                    ParagraphDao paragraphDao = db.paragraphDao();
                    disposable.add(paragraphDao.updateParagraphCompletable(newParagraph)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> {
                                        Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "段落创建时间修改成功");
                                        Toast.makeText(this, "段落创建时间修改成功", Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        ExceptionHelper.showExceptionDialog(this, throwable);
                                        Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落创建时间修改失败");
                                    }
                            )
                    );
                }
        );
    }

    /**
     * 修改情绪标签
     *
     * @param paragraph 需要修改情绪标签的段落
     */
    private void modifyEmotion(@NonNull ParagraphEntity paragraph) {
        //实例化底部对话框并显示
        EmotionTagSelectBottomSheet bottomSheet = new EmotionTagSelectBottomSheet(
                paragraph.getParagraphId(),
                (model, isChecked) -> {
                    EmotionParagraphRefEntity ref = new EmotionParagraphRefEntity(
                            model.getEmotionTag().getEmotionId(),
                            paragraph.getParagraphId()
                    );
                    EmotionTagDao dao = DiaryDatabase.getInstance(this).emotionTagDao();

                    if (isChecked) {
                        disposable.add(dao.insertEmotionParagraphRefCompletable(ref)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        () -> Log.i(
                                                LogTags.DIARY_READ_ACTIVITY.n(),
                                                "段落编号：" + paragraph.getParagraphId() +
                                                        "，添加情绪标签：" + model.getEmotionTag().getEmotionId()
                                        ),
                                        e -> ExceptionHelper.showExceptionDialog(this, e)
                                )
                        );
                    } else {
                        disposable.add(dao.deleteEmotionParagraphRefCompletable(ref)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        () -> Log.i(
                                                LogTags.DIARY_READ_ACTIVITY.n(),
                                                "段落编号：" + paragraph.getParagraphId() + "，删除情绪标签：" +
                                                        model.getEmotionTag().getEmotionId()
                                        ),
                                        e -> {
                                            Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落的情绪标签移除失败");
                                            ExceptionHelper.showExceptionDialog(this, e);
                                        }
                                )
                        );
                    }
                },
                (model, value) -> {
                    EmotionParagraphRefEntity ref = new EmotionParagraphRefEntity(
                            model.getEmotionTag().getEmotionId(),
                            paragraph.getParagraphId()
                    );
                    ref.setDegree(value);
                    EmotionTagDao dao = DiaryDatabase.getInstance(this).emotionTagDao();

                    disposable.add(dao.updateEmotionParagraphRefCompletable(ref)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> Log.i(
                                            LogTags.DIARY_READ_ACTIVITY.n(),
                                            "段落编号：" + paragraph.getParagraphId() +
                                                    "，情绪标签：" + model.getEmotionTag().getEmotionId() +
                                                    "，更新情绪强烈程度为" + value
                                    ),
                                    e -> {
                                        ExceptionHelper.showExceptionDialog(this, e);
                                        Log.e(
                                                LogTags.DIARY_READ_ACTIVITY.n(),
                                                "段落编号：" + paragraph.getParagraphId() +
                                                        "，情绪标签：" + model.getEmotionTag().getEmotionId() +
                                                        "情绪强烈程度更新失败"
                                        );
                                    }
                            )
                    );
                }
        );
        bottomSheet.show(getSupportFragmentManager(), TagStrings.EMOTION_TAG_SELECT_BOTTOM_SHEET.getTag());
    }

    /**
     * 删除段落
     *
     * @param paragraph 待删除的段落实例
     */
    private void deleteParagraph(ParagraphEntity paragraph) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_paragraph)
                .setMessage("此操作将删除该段落，确定继续吗？")
                .setPositiveButton("确定", (dialogInterface, i) -> {
                    DiaryDatabase db = DiaryDatabase.getInstance(this);
                    ParagraphDao dao = db.paragraphDao();

                    disposable.add(dao.deleteParagraphCompletable(paragraph)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {
                                Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "段落删除成功");
                                Toast.makeText(this, "段落删除成功", Toast.LENGTH_SHORT).show();
                            }, throwable -> {
                                Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落删除失败");
                                ExceptionHelper.showExceptionDialog(this, throwable);
                            })
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }
}