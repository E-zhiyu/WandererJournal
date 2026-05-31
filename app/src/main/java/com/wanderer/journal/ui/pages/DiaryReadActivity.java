package com.wanderer.journal.ui.pages;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.enums.TransitionName;
import com.wanderer.journal.auxiliary.interfaces.PagingRecyclerScrollListener;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.services.ParagraphService;
import com.wanderer.journal.data.save.preference.SearchHistoryPreference;
import com.wanderer.journal.databinding.ActivityDiaryReadBinding;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.auxiliary.enums.LogTags;
import com.wanderer.journal.auxiliary.enums.TagStrings;
import com.wanderer.journal.helpers.BackPressedCallbackHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.helpers.time.DateTimePickerHelper;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.adapters.SearchHistoryAdapter;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphViewModel;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphAdapter;
import com.wanderer.journal.ui.others.bottom.emotion.filter.EmotionTagFilterBottomSheet;
import com.wanderer.journal.ui.others.bottom.emotion.select.EmotionTagSelectBottomSheet;
import com.wanderer.journal.ui.others.dialogs.ProgressDialogBuilder;
import com.wanderer.journal.ui.pages.media.FullScreenMediaActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;

public class DiaryReadActivity extends AppCompatActivity {
    private ActivityDiaryReadBinding binding;                               //绑定的XML布局
    private LocalDate initDiaryDate = null;                                 //初始页的日期
    private final CompositeDisposable disposable = new CompositeDisposable();   //多线程任务订阅队列
    private ParagraphAdapter adapter;                                       //段落列表适配器
    private final AtomicInteger initScrollPosition = new AtomicInteger(-1); //界面加载时初始滚动到的位置
    private final Runnable scrollToInit = this::scrollRecyclerToInitPosition;   //滚动到初始位置的 Runnable 实例
    private BackPressedCallbackHelper backHelper;                           //返回监听帮助器
    private BackPressedCallbackHelper.BackHandler searchBackHandler;        //搜索返回处理器
    private final List<Long> checkedEmotionTagIdList = new ArrayList<>();   //选中的情绪标签 ID 列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiaryReadBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

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
        observeLiveData();
        initBackHandlers();
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
        Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "初始日期：" + initDate);
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

        //向上按钮
        binding.upFab.setOnClickListener(view -> {
            ParagraphViewModel viewModel = new ViewModelProvider(this).get(ParagraphViewModel.class);
            viewModel.jumpToPrevious();
        });
        AppearanceAnimationHelper.attachMorphAnimation(binding.upFab);

        //向下按钮
        binding.downFab.setOnClickListener(view -> {
            ParagraphViewModel viewModel = new ViewModelProvider(this).get(ParagraphViewModel.class);
            viewModel.jumpToNext();
        });
        AppearanceAnimationHelper.attachMorphAnimation(binding.downFab);

        //搜索跳转组件布局
        ViewEdgeHelper.setMarginToNavigation(binding.searchSkipLayout, this);
    }

    private void initBackHandlers() {
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                backHelper.dispatchBackPressed();
            }
        };
        getOnBackPressedDispatcher().addCallback(backPressedCallback);
        backHelper = new BackPressedCallbackHelper(backPressedCallback);

        //搜索返回处理器
        searchBackHandler = new BackPressedCallbackHelper.BackHandler() {
            @Override
            public boolean handleBack() {
                setSearchMode(false);
                return true;
            }

            @Override
            public int getPriority() {
                return 1;
            }
        };
    }

    /**
     * 初始化搜索组件
     */
    private void initSearchComponents() {
        //搜索历史显示
        SearchHistoryAdapter historyAdapter = new SearchHistoryAdapter(
                SearchHistoryPreference.KEY_DIARY_CONTENT,
                keyword -> {
                    binding.diaryContentSearchView.hide();
                    binding.contentSearchBar.setText(keyword);

                    //获取 ViewModel
                    DiaryDatabase db = DiaryDatabase.getInstance(this);
                    ParagraphViewModel viewModel = new ViewModelProvider(this).get(ParagraphViewModel.class);

                    //执行搜索
                    disposable.add(viewModel.executeSearch(keyword, checkedEmotionTagIdList, db)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    positionList -> {
                                        if (!positionList.isEmpty()) {
                                            adapter.setHighlightTarget(keyword, checkedEmotionTagIdList, positionList);
                                        } else {
                                            adapter.clearHighlight();
                                        }
                                        setSearchMode(true);
                                    },
                                    e -> ExceptionHelper.showExceptionDialog(this, e)
                            )
                    );
                }
        );
        List<String> initList = SearchHistoryPreference.getHistory(
                SearchHistoryPreference.KEY_DIARY_CONTENT,
                this
        );
        historyAdapter.submitList(new ArrayList<>(initList));
        binding.searchHistoryRecycler.setAdapter(historyAdapter);

        //设置清除搜索历史按钮点击监听
        binding.clearHistoryBtn.setOnClickListener(v -> {
            SearchHistoryPreference.clearHistory(
                    SearchHistoryPreference.KEY_DIARY_CONTENT,
                    this
            );
            historyAdapter.submitList(new ArrayList<>());
        });

        //设置搜索监听
        binding.diaryContentSearchView.getEditText().setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                //收起搜索视图并保存搜索词
                String keyword = String.valueOf(binding.diaryContentSearchView.getEditText().getText());
                binding.diaryContentSearchView.hide();
                binding.contentSearchBar.setText(keyword);

                //保存搜索历史
                List<String> historyList = SearchHistoryPreference.addKeyword(
                        keyword,
                        SearchHistoryPreference.KEY_DIARY_CONTENT,
                        this
                );
                historyAdapter.submitList(new ArrayList<>(historyList));

                //获取 ViewModel
                DiaryDatabase db = DiaryDatabase.getInstance(this);
                ParagraphViewModel viewModel = new ViewModelProvider(this).get(ParagraphViewModel.class);

                //执行搜索
                disposable.add(viewModel.executeSearch(keyword, checkedEmotionTagIdList, db)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                positionList -> {
                                    if (!positionList.isEmpty()) {
                                        adapter.setHighlightTarget(keyword, checkedEmotionTagIdList, positionList);
                                    } else {
                                        adapter.clearHighlight();
                                    }
                                    setSearchMode(true);
                                },
                                e -> ExceptionHelper.showExceptionDialog(this, e)
                        )
                );

                return true;
            } else {
                return false;
            }
        });

        //设置 SearchBar 的菜单按钮点击监听
        binding.contentSearchBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_emotion_select) {
                EmotionTagFilterBottomSheet bottomSheet = new EmotionTagFilterBottomSheet(
                        checkedEmotionTagIdList,
                        (emotionTag, isChecked) -> {
                            long emotionId = emotionTag.getEmotionId();
                            if (isChecked && !checkedEmotionTagIdList.contains(emotionId)) {
                                checkedEmotionTagIdList.add(emotionId);
                            } else if (!isChecked) {
                                checkedEmotionTagIdList.remove(emotionId);
                            }
                        },
                        checkedEmotionTagIdList::clear
                );
                bottomSheet.setOnDismissListener(() -> {
                    DiaryDatabase db = DiaryDatabase.getInstance(this);
                    String keyword = (String) binding.contentSearchBar.getText();
                    ParagraphViewModel viewModel = new ViewModelProvider(this).get(ParagraphViewModel.class);
                    disposable.add(viewModel.executeSearch(keyword, checkedEmotionTagIdList, db)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    positionList -> {
                                        if (!positionList.isEmpty()) {
                                            adapter.setHighlightTarget(keyword, checkedEmotionTagIdList, positionList);
                                        } else {
                                            adapter.clearHighlight();
                                        }
                                        setSearchMode(true);
                                    },
                                    e -> ExceptionHelper.showExceptionDialog(this, e)
                            )
                    );
                });
                bottomSheet.show(getSupportFragmentManager(), TagStrings.EMOTION_FILTER_BOTTOM_SHEET.getTag());

                return true;
            }

            return false;
        });
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        //设置适配器
        adapter = new ParagraphAdapter(
                (model, view) -> {
                    ParagraphEntity paragraph = model.getParagraph();

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
                },
                (position, mediaView, mediaList) -> {
                    String[] uriStrArray = mediaList.stream()
                            .map(MediaEntity::getFileUri)
                            .map(Uri::toString)
                            .toArray(String[]::new);

                    //实例化 Intent 并放入数据
                    Intent skip2FullScreen = new Intent(this, FullScreenMediaActivity.class);
                    skip2FullScreen.putExtra(KeyStrings.FILE_URIS.getS(), uriStrArray);
                    skip2FullScreen.putExtra(KeyStrings.VIEW_HOLDER_POSITION.getS(), position);

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            mediaView,
                            TransitionName.PARAGRAPH_MEDIA.getS()
                    );

                    startActivity(skip2FullScreen, options.toBundle());
                }
        );

        //监听数据库的响应
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphViewModel viewModel = new ViewModelProvider(this).get(ParagraphViewModel.class);
        disposable.add(db.paragraphDao().getAdjustedPositionSingle(initDiaryDate)
                .flatMapPublisher(
                        initPosition -> {
                            initScrollPosition.set(initPosition);
                            return viewModel.getPagingDataFlow(initPosition, db);
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        pagingData -> adapter.submitData(getLifecycle(), pagingData),
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );

        //添加页面加载监听，用以滚动到初始位置
        adapter.addOnPagesUpdatedListener(() -> {
            if (initScrollPosition.get() != -1) {
                //500毫秒的间隔防抖
                binding.contentRecycler.removeCallbacks(scrollToInit);
                binding.contentRecycler.postDelayed(scrollToInit, 100);
            }
            return Unit.INSTANCE;
        });
        binding.contentRecycler.setAdapter(adapter);
    }

    /**
     * 将段落内容列表滚动到初始位置
     */
    private void scrollRecyclerToInitPosition() {
        if (binding.contentRecycler.getLayoutManager() != null) {
            if (initScrollPosition.get() != -1) {
                Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "pagesUpdated count=" + adapter.getItemCount());
                Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "LoadState 触发精确滚动位置：" + initScrollPosition.get());

                scrollContentRecycler(
                        initScrollPosition.get(),
                        false,
                        new PagingRecyclerScrollListener() {
                            @Override
                            public void onSucceed() {
                                TransitionSet set = new TransitionSet()
                                        .addTransition(new Fade())
                                        .setInterpolator(new FastOutSlowInInterpolator())
                                        .setDuration(250);
                                TransitionManager.beginDelayedTransition(binding.centerLayout, set);

                                binding.recyclerLoadingIndicator.setVisibility(View.GONE);
                                if (adapter.getItemCount() == 0) {
                                    binding.emptyText.setVisibility(View.VISIBLE);
                                } else {
                                    binding.emptyText.setVisibility(View.GONE);
                                }

                                Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "初始化滚动成功");
                                initScrollPosition.set(-1);
                            }

                            @Override
                            public void onRetry(int failCount) {
                                Log.w(LogTags.DIARY_READ_ACTIVITY.n(), "初始化滚动重试次数：" + failCount);
                            }

                            @Override
                            public void onFailed() {
                                TransitionSet set = new TransitionSet()
                                        .addTransition(new Fade())
                                        .setInterpolator(new FastOutSlowInInterpolator())
                                        .setDuration(250);
                                TransitionManager.beginDelayedTransition(binding.centerLayout, set);

                                binding.recyclerLoadingIndicator.setVisibility(View.GONE);
                                if (adapter.getItemCount() == 0) {
                                    binding.emptyText.setVisibility(View.VISIBLE);
                                } else {
                                    binding.emptyText.setVisibility(View.GONE);
                                }

                                Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "初始化滚动失败");
                                initScrollPosition.set(-1);
                            }
                        }
                );
            }
        }
    }

    /**
     * 开始监听 ViewModel 的 LiveData
     */
    private void observeLiveData() {
        ParagraphViewModel viewModel = new ViewModelProvider(this).get(ParagraphViewModel.class);

        //匹配项的位置列表
        viewModel.getMatchedPositions().observe(this, positionList -> {
            Integer index = viewModel.getCurrentMatchIndex().getValue();
            if (positionList == null || index == null) {
                return;
            }

            if (positionList.isEmpty()) {
                binding.counterText.setText(R.string.not_applicable);
                Toast.makeText(this, "未找到匹配的搜索项", Toast.LENGTH_SHORT).show();
            } else {
                //更新跳转数量指示器
                String counterText = String.format(
                        Locale.getDefault(),
                        "%d/%d",
                        positionList.size() - index,
                        positionList.size()
                );
                binding.counterText.setText(counterText);
            }
        });

        //当前匹配项下标
        viewModel.getCurrentMatchIndex().observe(this, index -> {
            //判断是否为默认占位符
            if (index == -1) {
                return;
            }

            //获取目标下标
            List<Integer> positionList = viewModel.getMatchedPositions().getValue();
            if (index >= 0 && positionList != null && !positionList.isEmpty() && index < positionList.size()) {
                int targetPosition = positionList.get(index);

                //更新跳转数量指示器
                String counterText = String.format(
                        Locale.getDefault(),
                        "%d/%d",
                        positionList.size() - index,
                        positionList.size()
                );
                binding.counterText.setText(counterText);

                //滚动列表
                scrollContentRecycler(targetPosition, true, null);
            }
        });
    }

    /**
     * 滚动日记内容 RecyclerView，并显示进度条对话框
     *
     * @param targetPosition 需要滚动到的位置
     * @param listener       滚动状态监听器
     */
    private void scrollContentRecycler(
            int targetPosition,
            boolean withDialog,
            @Nullable PagingRecyclerScrollListener listener
    ) {
        //构建滚动进度条
        int maxRetryCount = 10;
        ProgressDialogBuilder builder = new ProgressDialogBuilder(
                this,
                "加载日记内容",
                "正在跳转至目标位置……"
        );
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);    //不可取消

        //执行滚动逻辑
        AppearanceAnimationHelper.scrollPagingRecycler(
                binding.contentRecycler,
                (LinearLayoutManager) binding.contentRecycler.getLayoutManager(),
                adapter,
                targetPosition,
                maxRetryCount,
                750,
                new PagingRecyclerScrollListener() {
                    @Override
                    public void onSucceed() {
                        if (listener != null) {
                            listener.onSucceed();
                        }

                        if (dialog.isShowing()) {
                            Toast.makeText(DiaryReadActivity.this, "跳转成功", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                        Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "跳转成功");
                    }

                    @Override
                    public void onRetry(int failCount) {
                        if (listener != null) {
                            listener.onRetry(failCount);
                        }

                        if (withDialog) {
                            dialog.show();
                            builder.setIndeterminate(false);
                            builder.updateProgress(failCount, maxRetryCount, "正在加载日记内容……");
                        }
                        Log.w(LogTags.DIARY_READ_ACTIVITY.n(), "跳转失败重试，次数：" + failCount);
                    }

                    @Override
                    public void onFailed() {
                        if (listener != null) {
                            listener.onFailed();
                        }

                        if (dialog.isShowing()) {
                            Toast.makeText(DiaryReadActivity.this, "跳转失败", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                        Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "跳转失败，请尝试点击右侧按钮跳转至附近");
                    }
                }
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
        bottomSheet.show(getSupportFragmentManager(), TagStrings.EMOTION_SELECT_BOTTOM_SHEET.getTag());
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
                .setPositiveButton("确定", (dialogInterface, i) -> disposable.add(ParagraphService.deleteParagraphAndMedia(paragraph, this)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(() -> {
                            Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "段落删除成功");
                            Toast.makeText(this, "段落删除成功", Toast.LENGTH_SHORT).show();
                        }, throwable -> {
                            Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落删除失败");
                            ExceptionHelper.showExceptionDialog(this, throwable);
                        })
                ))
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 设置搜索模式
     *
     * @param isSearchMode 是否为搜索模式
     */
    private void setSearchMode(boolean isSearchMode) {
        //定义过渡动画
        TransitionSet set = new TransitionSet()
                .addTransition(new Slide(Gravity.END))
                .addTransition(new Fade())
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(250);

        //通知布局即将发生变化
        TransitionManager.beginDelayedTransition(binding.searchSkipLayout, set);

        if (!isSearchMode) {
            binding.contentSearchBar.setText(null);
            binding.searchSkipLayout.setVisibility(View.GONE);

            backHelper.unregisterHandler(searchBackHandler);
            adapter.clearHighlight();   //清除文本高亮
        } else {
            binding.searchSkipLayout.setVisibility(View.VISIBLE);

            backHelper.registerHandler(searchBackHandler);
        }
    }
}