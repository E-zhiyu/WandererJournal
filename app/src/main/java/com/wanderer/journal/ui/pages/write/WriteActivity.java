package com.wanderer.journal.ui.pages.write;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.services.DiaryService;
import com.wanderer.journal.databinding.ActivityWriteBinding;
import com.wanderer.journal.enums.KeyStrings;
import com.wanderer.journal.enums.LogTags;
import com.wanderer.journal.helpers.ImmHelper;
import com.wanderer.journal.helpers.time.DateTimePickerHelper;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphAdapter;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphUiModel;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphViewModel;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphViewModelFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;

public class WriteActivity extends AppCompatActivity {
    private ActivityWriteBinding binding;                   //绑定的XML布局
    private ParagraphEntity modifyingParagraph = null;      //正在编辑的段落
    private OnBackPressedCallback backPressedCallback;      //返回手势监听
    private LocalDate diaryDate = LocalDate.now();          //父日记的日期
    private LocalDate pendingTargetDate = null;             //加载列表时需要跳转的日期
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWriteBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            binding.contentInputLayout.setPadding(
                    ViewEdgeHelper.dpToPx(this, 10),
                    ViewEdgeHelper.dpToPx(this, 10),
                    ViewEdgeHelper.dpToPx(this, 10),
                    systemBars.bottom);

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
                int keyboardHeight = Math.max(0, imeInsets.bottom - systemBars.bottom);
                binding.getRoot().setPadding(systemBars.left, systemBars.top, systemBars.right, keyboardHeight);

                return insets;
            }

            @Override
            public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                super.onEnd(animation);
                // 动画结束时，如果需要将 Translation 转换为永久的 Padding，可以在这里处理
            }
        });

        receiveIntent();
        initViews();

        //注册返回手势监听
        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                setEditMode(false, null);
            }
        };
        getOnBackPressedDispatcher().addCallback(backPressedCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
        disposable.dispose();
    }

    /**
     * 获取由父界面传递的参数
     */
    private void receiveIntent() {
        Intent intent = getIntent();
        Bundle dataBundle = intent.getExtras();
        if (dataBundle == null) {
            return;
        }

        //初始化日期数据
        try {
            String date = dataBundle.getString(KeyStrings.WRITE_DIARY_DATE.getS());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            diaryDate = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            Log.e(LogTags.WRITE_ACTIVITY.n(), "无法读取传递的日期数据，已默认设置为当前日期");
            diaryDate = LocalDate.now();
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //初始化RecyclerView
        initRecycler();

        //发送按钮
        binding.sendBtn.setOnClickListener(view -> {
            //获取输入内容
            String content = String.valueOf(binding.contentTextInput.getText());
            if (content.isEmpty()) {
                return;
            }

            if (modifyingParagraph == null) {
                addParagraph(content);
            } else {
                updateParagraphContent(content, modifyingParagraph);
                setEditMode(false, null);
            }
        });

        //内容编辑关闭按钮
        binding.modifyCloseBtn.setOnClickListener(view -> setEditMode(false, null));
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecycler() {
        //设置适配器
        ParagraphAdapter adapter = new ParagraphAdapter(
                (paragraph, view) -> {
                    PopupMenu menu = new PopupMenu(this, view, Gravity.END);
                    menu.getMenuInflater().inflate(R.menu.menu_paragraph_edit, menu.getMenu());

                    menu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_modify_content) {
                            //保存旧段落引用并启用自定义返回手势
                            modifyingParagraph = paragraph;
                            backPressedCallback.setEnabled(true);

                            //更新UI到编辑模式
                            setEditMode(true, paragraph);

                            return true;
                        } else if (item.getItemId() == R.id.action_modify_time) {
                            updateParagraphCreateTime(paragraph);
                            return true;
                        } else if (item.getItemId() == R.id.action_delete_paragraph) {
                            deleteParagraph(paragraph);
                            return true;
                        }

                        return false;
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
            return Unit.INSTANCE;
        });
        binding.contentRecycler.setAdapter(adapter);

        //监听数据库的响应
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphViewModelFactory factory = new ParagraphViewModelFactory(db, diaryDate, diaryDate.plusDays(1));
        ParagraphViewModel viewModel = new ViewModelProvider(this, factory).get(ParagraphViewModel.class);
        disposable.add(viewModel.getPagingDataFlow()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pagingData ->
                                adapter.submitData(getLifecycle(), pagingData),
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );

        //跳转到指定日期
        pendingTargetDate = diaryDate;
        viewModel.scrollToDate(diaryDate);

        //添加加载监听器用于精细调控加载位置
        adapter.addLoadStateListener(loadStates -> {
            // 当刷新完成且数据已提交到 UI
            if (loadStates.getRefresh() instanceof LoadState.NotLoading && pendingTargetDate != null) {
                // 遍历当前已加载的列表，找到对应的日期项
                int position = -1;
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    ParagraphUiModel item = adapter.peek(i); // 使用 peek 不触发分页加载
                    if (item instanceof ParagraphUiModel.Item) {
                        if (((ParagraphUiModel.Item) item).paragraph.getCreateTime().toLocalDate().equals(pendingTargetDate)) {
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
            return null;
        });
    }

    /**
     * 添加新段落
     *
     * @param content 新段落的内容
     */
    private void addParagraph(String content) {
        //执行写入操作
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphDao paragraphDao = db.paragraphDao();

        disposable.add(DiaryService.getOrCreateDiaryIdByDate(diaryDate, this)
                .flatMap(diaryId -> {
                    ParagraphEntity newParagraph = new ParagraphEntity(diaryId, content, LocalDateTime.now());
                    return paragraphDao.insertParagraph(newParagraph);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        diaryId -> binding.contentTextInput.setText(null),
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 更新段落内容
     *
     * @param newContent      新内容
     * @param originParagraph 原本的段落实体
     */
    private void updateParagraphContent(String newContent, @NonNull ParagraphEntity originParagraph) {
        ParagraphEntity newParagraph = new ParagraphEntity(
                originParagraph.getParentDiaryId(),
                newContent,
                originParagraph.getCreateTime()
        );
        newParagraph.setParagraphId(originParagraph.getParagraphId());

        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphDao paragraphDao = db.paragraphDao();
        disposable.add(paragraphDao.updateParagraphContent(newParagraph)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            Toast.makeText(this, "段落内容修改成功", Toast.LENGTH_SHORT).show();
                            Log.i(LogTags.WRITE_ACTIVITY.n(), "段落内容修改成功");
                        },
                        throwable -> {
                            ExceptionHelper.showExceptionDialog(this, throwable);
                            Log.e(LogTags.WRITE_ACTIVITY.n(), "段落内容修改失败");
                        }
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
                    disposable.add(paragraphDao.updateParagraphContent(newParagraph)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> {
                                        Log.i(LogTags.WRITE_ACTIVITY.n(), "段落创建时间修改成功");
                                        Toast.makeText(this, "段落创建时间修改成功", Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        ExceptionHelper.showExceptionDialog(this, throwable);
                                        Log.e(LogTags.WRITE_ACTIVITY.n(), "段落创建时间修改失败");
                                    }
                            )
                    );
                }
        );
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

                    //判断是否为正在编辑的段落，是则退出编辑
                    if (modifyingParagraph != null && paragraph.getParagraphId() == modifyingParagraph.getParagraphId()) {
                        setEditMode(false, null);
                    }

                    //多线程删除段落
                    disposable.add(dao.deleteParagraph(paragraph)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {
                                Log.i(LogTags.WRITE_ACTIVITY.n(), "段落删除成功");
                                Toast.makeText(this, "段落删除成功", Toast.LENGTH_SHORT).show();
                            }, throwable -> {
                                Log.e(LogTags.WRITE_ACTIVITY.n(), "段落删除失败");
                                ExceptionHelper.showExceptionDialog(this, throwable);
                            })
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 设置编辑模式
     *
     * @param isEditMode         是否启用编辑模式
     * @param modifyingParagraph 如果启用编辑模式，该参数传递的是正在编辑的段落实体
     */
    private void setEditMode(boolean isEditMode, ParagraphEntity modifyingParagraph) {
        // 定义过渡动画：组合滑入和渐变
        // Slide(Gravity.BOTTOM) 会让 View 看起来是从底部“抽出来”的
        TransitionSet set = new TransitionSet()
                .addTransition(new Slide(Gravity.BOTTOM))
                .addTransition(new Fade())
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(250); // Telegram 的动画通常很短促，200-300ms 最合适

        // 关键：通知布局即将发生变化
        TransitionManager.beginDelayedTransition(binding.getRoot(), set);

        // 执行状态改变
        if (isEditMode) {
            this.modifyingParagraph = modifyingParagraph;
            binding.originText.setText(modifyingParagraph.getContent());
            binding.contentEditCard.setVisibility(View.VISIBLE);

            //自动显示输入法
            ImmHelper.showImm(binding.contentTextInput);
        } else {
            this.modifyingParagraph = null;
            binding.contentEditCard.setVisibility(View.GONE);
            binding.contentTextInput.setText(null);         //清空输入框
        }

        backPressedCallback.setEnabled(isEditMode);
    }
}