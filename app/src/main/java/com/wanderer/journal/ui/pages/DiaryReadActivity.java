package com.wanderer.journal.ui.pages;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.biometric.BiometricManager;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.auxiliary.classes.InfoShower;
import com.wanderer.journal.auxiliary.enums.types.ParagraphPrivacyType;
import com.wanderer.journal.auxiliary.enums.unique.TransitionName;
import com.wanderer.journal.auxiliary.enums.unique.ViewTags;
import com.wanderer.journal.data.save.db.DiaryDb;
import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.data.save.db.entities.EmotionParagraphRefEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.composite.ui.ParagraphUiModel;
import com.wanderer.journal.data.save.db.entities.composite.union.ParagraphUnionModel;
import com.wanderer.journal.data.save.db.services.EmotionTagService;
import com.wanderer.journal.data.save.db.services.ParagraphService;
import com.wanderer.journal.data.save.preference.SearchHistoryPreference;
import com.wanderer.journal.data.save.preference.TipPreference;
import com.wanderer.journal.databinding.ActivityDiaryReadBinding;
import com.wanderer.journal.auxiliary.enums.unique.KeyStrings;
import com.wanderer.journal.auxiliary.enums.unique.LogTags;
import com.wanderer.journal.auxiliary.enums.unique.TagStrings;
import com.wanderer.journal.databinding.ViewHolderSeparatorTextChipBinding;
import com.wanderer.journal.helpers.BackPressedCallbackHelper;
import com.wanderer.journal.helpers.BiometricHelper;
import com.wanderer.journal.helpers.SearchHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.helpers.appearance.ScrollHelper;
import com.wanderer.journal.helpers.appearance.VisibilityHelper;
import com.wanderer.journal.helpers.text.TextHelper;
import com.wanderer.journal.helpers.time.DateTimePickerHelper;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.decoration.sticky.StickyHeaderItemDecoration;
import com.wanderer.journal.ui.others.dialogs.EditTextDialogBuilder;
import com.wanderer.journal.ui.others.selections.paragraph.ParagraphKeyProvider;
import com.wanderer.journal.ui.others.selections.paragraph.ParagraphLookup;
import com.wanderer.journal.ui.others.viewmodel.EmotionTagSelectViewModel;
import com.wanderer.journal.ui.others.viewmodel.ParagraphFilterViewModel;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphPagingAdapter;
import com.wanderer.journal.ui.others.bottom.ParagraphFilterBottomSheet;
import com.wanderer.journal.ui.others.bottom.EmotionTagSelectBottomSheet;
import com.wanderer.journal.ui.pages.media.FullScreenMediaActivity;
import com.wanderer.journal.ui.pages.share.SharePreviewActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class DiaryReadActivity extends AppCompatActivity {
    private ActivityDiaryReadBinding binding;                               //绑定的XML布局
    @Nullable
    private Bundle initBundle = null;                                       //传递初始化数据的数据包
    private final CompositeDisposable disposable = new CompositeDisposable();           //多线程任务订阅队列
    private ParagraphPagingAdapter adapter;                                 //段落列表适配器
    private List<Long> searchMatchedparagraphIdList;                        //符合过滤条件的段落的位置列表
    private int currentIndex = -1;                                          //搜索匹配项列表当前元素的下标
    private final AtomicInteger initScrollPosition = new AtomicInteger(-1);   //界面加载时初始滚动到的位置
    @Nullable
    private Function0<Unit> pageUpdatedListener = null;
    private BackPressedCallbackHelper backHelper;                           //返回监听帮助器
    private BackPressedCallbackHelper.BackHandler searchBackHandler;        //搜索返回处理器
    private BackPressedCallbackHelper.BackHandler shareChoiceBackHandler;   //分享日记时多选模式的返回处理器
    private SelectionTracker<Long> selectionTracker;                        //段落分享选择器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiaryReadBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            //列表视图
            binding.contentRecycler.setPadding(
                    systemBars.left,
                    0,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        initBundle = getIntent().getExtras();
        initViews();
        initGuide();
        observeLiveData();
        initBackHandlers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //移除待滚动的任务
        Object scrollTask = binding.contentRecycler.getTag(ViewTags.RECYCLER_SCROLL_RUNNABLE.getT());
        if (scrollTask instanceof Runnable) {
            binding.contentRecycler.removeCallbacks((Runnable) scrollTask);
        }

        //移除页面加载监听器
        if (pageUpdatedListener != null) {
            adapter.removeOnPagesUpdatedListener(pageUpdatedListener);
            pageUpdatedListener = null;
        }

        disposable.dispose();
        binding = null;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //搜索组件
        initSearchComponents();

        //日记段落列表
        initRecyclerView();

        //数量指示器
        binding.matchedItemsCounter.setOnClickListener(view ->
                new EditTextDialogBuilder(this, "跳转位置", null)
                        .setInputType(InputType.TYPE_CLASS_NUMBER)
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", input -> {
                            //判断是否有内容
                            if (searchMatchedparagraphIdList == null || searchMatchedparagraphIdList.isEmpty()) {
                                Toast.makeText(this, "无匹配的搜索项，无法跳转", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            //判断范围
                            int pos = Integer.parseInt(input);
                            if (pos <= 0 || pos > searchMatchedparagraphIdList.size()) {
                                Toast.makeText(this, "请输入有效范围内的位置", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            //更新计数器
                            currentIndex = pos - 1;
                            String counterText = String.format(
                                    Locale.getDefault(),
                                    "%d/%d",
                                    currentIndex + 1,
                                    searchMatchedparagraphIdList.size()
                            );
                            binding.filteredParagraphCounterText.setText(counterText);

                            //执行跳转逻辑
                            scrollToParagraph(searchMatchedparagraphIdList.get(currentIndex));
                        })
                        .show());
        AppearanceHelper.attachMorphAnimation(binding.matchedItemsCounter);

        //向上按钮
        binding.upFab.setOnClickListener(view -> {
            //判空
            if (searchMatchedparagraphIdList == null || searchMatchedparagraphIdList.isEmpty()) {
                return;
            }

            //滚动列表
            int model = searchMatchedparagraphIdList.size();
            currentIndex = (currentIndex + model - 1) % model;
            Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "当前匹配项下标：" + currentIndex);
            scrollToParagraph(searchMatchedparagraphIdList.get(currentIndex));

            //更新计数器
            String counterText = String.format(
                    Locale.getDefault(),
                    "%d/%d",
                    currentIndex + 1,
                    model
            );
            binding.filteredParagraphCounterText.setText(counterText);
        });
        AppearanceHelper.attachMorphAnimation(binding.upFab);

        //向下按钮
        binding.downFab.setOnClickListener(view -> {
            //判空
            if (searchMatchedparagraphIdList == null || searchMatchedparagraphIdList.isEmpty()) {
                return;
            }

            //滚动视图
            int model = searchMatchedparagraphIdList.size();
            currentIndex = (currentIndex + model + 1) % model;
            Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "当前匹配项下标：" + currentIndex);
            scrollToParagraph(searchMatchedparagraphIdList.get(currentIndex));

            //更新计数器
            String counterText = String.format(
                    Locale.getDefault(),
                    "%d/%d",
                    currentIndex + 1,
                    model
            );
            binding.filteredParagraphCounterText.setText(counterText);
        });
        AppearanceHelper.attachMorphAnimation(binding.downFab);

        //搜索跳转组件布局
        AppearanceHelper.setMarginToNavigation(binding.searchSkipLayout, this);

        //多词搜索模式切换按钮
        binding.multiSearchModeSwitchBtn.setOnClickListener(view -> {
            ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
            viewModel.toggleKeywordMode();
            if (viewModel.isAndMode()) {
                binding.multiSearchModeSwitchBtn.setText(R.string.multi_word_search_and);
            } else {
                binding.multiSearchModeSwitchBtn.setText(R.string.multi_word_search_or);
            }
        });

        //符合过滤条件的段落的下标
        ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
        DiaryDb db = DiaryDb.getInstance(this);
        disposable.add(viewModel.getFilteredParagraphIds(db)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        idList -> {
                            searchMatchedparagraphIdList = idList;

                            //高亮段落
                            if (!idList.isEmpty()) {
                                adapter.setHighlightTarget(
                                        viewModel.getValidKeywordList(),
                                        viewModel.getCheckedEmotionIdSet(),
                                        idList
                                );
                            } else {
                                adapter.clearHighlight();
                            }

                            //设置跳转位置卡片文本
                            currentIndex = idList.size() - 1;
                            if (idList.isEmpty()) {
                                binding.filteredParagraphCounterText.setText(R.string.not_applicable);
                            } else {
                                String counterText = String.format(
                                        Locale.getDefault(),
                                        "%d/%d",
                                        currentIndex + 1,
                                        idList.size()
                                );
                                binding.filteredParagraphCounterText.setText(counterText);

                                scrollToParagraph(searchMatchedparagraphIdList.get(currentIndex));
                            }
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 初始化引导内容
     */
    private void initGuide() {
        TipPreference.showTip(
                binding.contentSearchBar,
                Gravity.BOTTOM,
                "用空格隔开多个关键词可以实现多词搜索",
                TipPreference.KEY_READ_MULTI_SEARCH,
                1
        );
    }

    /**
     * 初始化返回手势处理
     */
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
                ParagraphFilterViewModel viewModel = new ViewModelProvider(DiaryReadActivity.this).get(ParagraphFilterViewModel.class);
                viewModel.clearFilter();
                return true;
            }

            @Override
            public int getPriority() {
                return 2;
            }
        };

        //多选模式返回处理器
        shareChoiceBackHandler = new BackPressedCallbackHelper.BackHandler() {
            @Override
            public boolean handleBack() {
                setShareSelectMode(false);
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
        SearchHelper.initSearchComponents(
                binding.contentSearchBar,
                binding.diaryContentSearchView,
                binding.searchHistoryRecycler,
                binding.clearHistoryBtn,
                SearchHistoryPreference.KEY_DIARY_CONTENT,
                keyword -> {
                    ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
                    viewModel.executeSearch(keyword);
                },
                item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_emotion_select) {
                        ParagraphFilterBottomSheet bottomSheet = new ParagraphFilterBottomSheet();
                        bottomSheet.show(getSupportFragmentManager(), TagStrings.EMOTION_FILTER_BOTTOM_SHEET.t());

                        return true;
                    } else if (id == R.id.action_share) {
                        if (!adapter.getSelectMode()) {
                            TipPreference.showTip(
                                    binding.appBarLayout,
                                    Gravity.BOTTOM,
                                    "长按拖动可以快速多选",
                                    TipPreference.KEY_SHARE_MULTI_CHOICE,
                                    1
                            );

                            Toast.makeText(this, "选完再次点击即可分享", Toast.LENGTH_SHORT).show();
                            setShareSelectMode(true);
                        } else {
                            //判空
                            if (!selectionTracker.hasSelection()) {
                                Toast.makeText(this, "请选择至少一条日记段落", Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            //获取所有选择的段落 ID
                            long[] selectedIds = StreamSupport.stream(selectionTracker.getSelection().spliterator(), false)
                                    .mapToLong(Long::longValue)
                                    .toArray();

                            //创建 Intent
                            Intent skip2SharePreview = new Intent(this, SharePreviewActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putLongArray(KeyStrings.SHARED_PARAGRAPH_ID.v(), selectedIds);
                            skip2SharePreview.putExtras(bundle);

                            //跳转界面
                            startActivity(skip2SharePreview);
                        }
                        return true;
                    } else if (id == R.id.action_skip_date) {
                        //获取当前正在显示的段落的日期
                        LocalDate currentDate;
                        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.contentRecycler.getLayoutManager();
                        if (layoutManager != null) {
                            int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                            ParagraphUiModel model = adapter.peek(firstVisiblePosition);
                            if (model instanceof ParagraphUiModel.Separator) {
                                currentDate = ((ParagraphUiModel.Separator) model).date;
                            } else if (model instanceof ParagraphUiModel.Item) {
                                currentDate = ((ParagraphUiModel.Item) model).model.getParagraph().getCreateTime().toLocalDate();
                            } else {
                                currentDate = LocalDate.now();
                            }
                        } else {
                            currentDate = LocalDate.now();
                        }

                        //显示日期选择对话框
                        DateTimePickerHelper.selectDate(
                                currentDate,
                                getSupportFragmentManager(),
                                selection -> {
                                    LocalDate selectedDate = DateTimePickerHelper.getLocalDateFromTimeMilli(selection);
                                    skipToTargetDate(selectedDate);
                                }
                        );
                        return true;
                    } else if (id == R.id.action_change_hidden_paragraph_visibility) {
                        changeHiddenParagraphVisibility();
                        return true;
                    }

                    return false;
                }
        );
    }

    /**
     * 初始化 RecyclerView
     */
    private void initRecyclerView() {
        //设置适配器
        ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
        adapter = new ParagraphPagingAdapter(
                viewModel.contentDisplayIdSet,
                this::showParagraphModifyMenu,
                (position, mediaView, mediaList) -> {
                    String[] uriStrArray = mediaList.stream()
                            .map(MediaEntity::getFileUri)
                            .map(Uri::toString)
                            .toArray(String[]::new);

                    //实例化 Intent 并放入数据
                    Intent skip2FullScreen = new Intent(this, FullScreenMediaActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putStringArray(KeyStrings.FILE_URIS.v(), uriStrArray);
                    bundle.putInt(KeyStrings.VIEW_HOLDER_POSITION.v(), position);
                    skip2FullScreen.putExtras(bundle);

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this,
                            mediaView,
                            TransitionName.FULLSCREEN_MEDIA.getS()
                    );

                    startActivity(skip2FullScreen, options.toBundle());
                },
                (roleId, view) -> InfoShower.showRole(this, disposable, roleId)
        );
        binding.contentRecycler.setAdapter(adapter);

        //应用粘性头部装饰器
        StickyHeaderItemDecoration<ViewHolderSeparatorTextChipBinding> decoration = new StickyHeaderItemDecoration<>(
                adapter,
                ViewHolderSeparatorTextChipBinding::inflate,
                (binding, data) -> binding.separatorText.setText(data)
        );
        binding.contentRecycler.addItemDecoration(decoration);

        //为适配器绑定选择追踪器
        selectionTracker = new SelectionTracker.Builder<>(
                TagStrings.PARAGRAPH_SELECTION.t(),
                binding.contentRecycler,
                new ParagraphKeyProvider(adapter),
                new ParagraphLookup(binding.contentRecycler),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
                new SelectionTracker.SelectionPredicate<>() {
                    @Override
                    public boolean canSetStateForKey(@NonNull Long key, boolean nextState) {
                        if (key < 0) return false;

                        boolean isItem = false;
                        List<ParagraphUiModel> snapshot = adapter.snapshot();
                        for (int i = 0; i < snapshot.size(); i++) {
                            ParagraphUiModel item = snapshot.get(i);
                            if ((item instanceof ParagraphUiModel.Item) && ((ParagraphUiModel.Item) item).model.getParagraph().getParagraphId() == key) {
                                isItem = true;
                                break;
                            }
                        }
                        return adapter != null && adapter.getSelectMode() && isItem;
                    }

                    @Override
                    public boolean canSetStateAtPosition(int position, boolean nextState) {
                        if (adapter == null) {
                            return false;
                        }

                        try {
                            boolean isSelectMode = adapter.getSelectMode();
                            boolean isItem = adapter.peek(position) instanceof ParagraphUiModel.Item;
                            return isSelectMode && isItem;
                        } catch (IndexOutOfBoundsException e) {
                            return false;
                        }
                    }

                    @Override
                    public boolean canSelectMultiple() {
                        return true;
                    }
                }
        ).build();
        adapter.setSelectionTracker(selectionTracker);

        //设置多选追踪器选择监听
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                if (selectionTracker.hasSelection()) {
                    new Handler(Looper.getMainLooper()).post(() -> setShareSelectMode(true));

                    int size = selectionTracker.getSelection().size();
                    Log.d(LogTags.WRITE_ACTIVITY.n(), "已选择：" + size);
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> setShareSelectMode(false));
                    Log.d(LogTags.WRITE_ACTIVITY.n(), "选择已清除");
                }
            }
        });

        //监听数据库的响应
        DiaryDb db = DiaryDb.getInstance(this);
        if (initBundle != null && initBundle.getLong(KeyStrings.INIT_DATE.v(), -1) != -1) {
            long initDateTimestamp = initBundle.getLong(KeyStrings.INIT_DATE.v());
            Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "初始日期：" + initDateTimestamp);
            LocalDate initDiaryDate = DateTimeConverter.toLocalDate(initDateTimestamp);
            disposable.add(db.paragraphDao().getAdjustedPositionSingle(initDiaryDate, viewModel.isHiddenParagraphShown())
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
        } else {    //没有传递参数直接从最顶部开始
            initScrollPosition.set(0);
            disposable.add(viewModel.getPagingDataFlow(0, db)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            pagingData -> adapter.submitData(getLifecycle(), pagingData),
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        }

        //添加页面加载监听，用以滚动到初始位置
        pageUpdatedListener = new Function0<>() {
            @Override
            public Unit invoke() {
                if (initScrollPosition.get() != -1) {
                    Object savedScrollTask = binding.contentRecycler.getTag(ViewTags.RECYCLER_SCROLL_RUNNABLE.getT());
                    if (savedScrollTask instanceof Runnable) {
                        binding.contentRecycler.removeCallbacks((Runnable) savedScrollTask);
                        binding.contentRecycler.postDelayed((Runnable) savedScrollTask, 200);
                    } else {
                        Runnable task = () -> {
                            //控制视图显示
                            VisibilityHelper.toggleVisibilityWithFade(binding.recyclerLoadingIndicator, false);
                            if (adapter.getItemCount() == 0) {
                                VisibilityHelper.toggleVisibilityWithFade(binding.emptyText, true);
                            } else if (adapter.getItemCount() != 0) {
                                VisibilityHelper.toggleVisibilityWithFade(binding.emptyText, false);
                            }

                            //执行滚动操作
                            int position = initScrollPosition.get();
                            if (position < 0) return;
                            else if (position >= adapter.getItemCount()) {
                                position = adapter.getItemCount() - 1;
                            }
                            Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "pagesUpdated count=" + adapter.getItemCount());
                            Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "LoadState 触发精确滚动位置：" + initScrollPosition.get());
                            scrollContentRecycler(
                                    position,
                                    new ScrollHelper.PagingRecyclerScrollListener() {
                                        @Override
                                        public void onSucceed(int successPosition) {
                                            //折叠标题栏
                                            binding.appBarLayout.setExpanded(false);

                                            Log.d(LogTags.DIARY_READ_ACTIVITY.n(), "初始化滚动成功");
                                            initScrollPosition.set(-1);
                                            binding.contentRecycler.setTag(ViewTags.RECYCLER_SCROLL_RUNNABLE.getT(), null);
                                        }

                                        @Override
                                        public void onRetry(int failCount) {
                                            Log.w(LogTags.DIARY_READ_ACTIVITY.n(), "初始化滚动重试次数：" + failCount);
                                        }

                                        @Override
                                        public void onFailed() {
                                            Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "初始化滚动失败");
                                            initScrollPosition.set(-1);
                                            binding.contentRecycler.setTag(ViewTags.RECYCLER_SCROLL_RUNNABLE.getT(), null);
                                        }
                                    }
                            );

                            //移除页面加载监听器
                            adapter.removeOnPagesUpdatedListener(this);
                        };
                        binding.contentRecycler.setTag(ViewTags.RECYCLER_SCROLL_RUNNABLE.getT(), task);
                        binding.contentRecycler.postDelayed(task, 200);
                    }
                }
                return Unit.INSTANCE;
            }
        };
        adapter.addOnPagesUpdatedListener(pageUpdatedListener);
    }

    /**
     * 开始监听 ViewModel 的 LiveData
     */
    private void observeLiveData() {
        ParagraphFilterViewModel filterViewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
        filterViewModel.getFilterUpdatedLiveData().observe(this, v ->
                setSearchMode(!filterViewModel.isNoFilter())
        );

        //段落的情绪标签选择状态
        EmotionTagSelectViewModel emotionTagSelectViewModel = new ViewModelProvider(this).get(EmotionTagSelectViewModel.class);
        emotionTagSelectViewModel.getCheckedEmotionTag().observe(this, emotionTagEntity -> {
            long paragraphId = emotionTagSelectViewModel.getParagraphId();
            long emotionId = emotionTagEntity.getEmotionId();
            int degree = emotionTagSelectViewModel.getDegree();
            boolean isChecked = emotionTagSelectViewModel.isChecked();

            EmotionParagraphRefEntity refEntity = new EmotionParagraphRefEntity(emotionId, paragraphId, degree);
            DiaryDb db = DiaryDb.getInstance(this);
            if (isChecked) {
                disposable.add(EmotionTagService.addOrUpdateEmotionTagRef(refEntity, db)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> Log.i(
                                        LogTags.DIARY_READ_ACTIVITY.n(),
                                        "添加情绪标签引用，段落编号：" + paragraphId +
                                                "，情绪标签：" + emotionId +
                                                "，强烈程度：" + degree
                                ),
                                e -> ExceptionHelper.showExceptionDialog(this, e)
                        )
                );
            } else {
                disposable.add(db.emotionTagDao().deleteEmotionParagraphRefCompletable(refEntity)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> Log.i(LogTags.DIARY_READ_ACTIVITY.n(),
                                        "删除情绪标签引用，段落编号：" + paragraphId +
                                                "，情绪标签：" + emotionId
                                ),
                                e -> ExceptionHelper.showExceptionDialog(this, e)
                        )
                );
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
            @Nullable ScrollHelper.PagingRecyclerScrollListener listener
    ) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (binding == null) return;

            VisibilityHelper.toggleVisibilityWithFade(binding.recyclerLoadingIndicator, true);
        });

        //执行滚动逻辑
        final int MAX_RETRY = 10;
        if (binding.contentRecycler.getLayoutManager() != null) {
            ScrollHelper.scrollPagingRecycler(
                    binding.contentRecycler,
                    (LinearLayoutManager) binding.contentRecycler.getLayoutManager(),
                    adapter,
                    targetPosition,
                    AppearanceHelper.dpToPx(this, 63),
                    MAX_RETRY,
                    new ScrollHelper.PagingRecyclerScrollListener() {
                        @Override
                        public void onSucceed(int successPosition) {
                            if (listener != null) {
                                listener.onSucceed(successPosition);
                            }

                            VisibilityHelper.toggleVisibilityWithFade(binding.recyclerLoadingIndicator, false);
                            Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "跳转成功");
                        }

                        @Override
                        public void onRetry(int failCount) {
                            if (listener != null) {
                                listener.onRetry(failCount);
                            }

                            VisibilityHelper.toggleVisibilityWithFade(binding.recyclerLoadingIndicator, false);
                            Log.w(LogTags.DIARY_READ_ACTIVITY.n(), "跳转失败重试，次数：" + failCount);
                        }

                        @Override
                        public void onFailed() {
                            if (listener != null) {
                                listener.onFailed();
                            }

                            VisibilityHelper.toggleVisibilityWithFade(binding.recyclerLoadingIndicator, false);
                            Toast.makeText(DiaryReadActivity.this, "跳转失败", Toast.LENGTH_SHORT).show();
                            Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "跳转失败，请尝试点击右侧按钮跳转至附近");
                        }
                    }
            );
        }
    }

    /**
     * 滚动到指定的段落
     *
     * @param paragraphId 段落编号
     * @param queriedPos  从数据库中查询到的具体位置
     */
    private void scrollToParagraph(long paragraphId, int queriedPos, int recursionDepth) {
        //在缓存中查找
        List<ParagraphUiModel> currentSnapshot = adapter.snapshot();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ParagraphUiModel uiModel = currentSnapshot.get(i);
            if (uiModel instanceof ParagraphUiModel.Item) {
                ParagraphEntity paragraph = ((ParagraphUiModel.Item) uiModel).model.getParagraph();
                if (paragraph.getParagraphId() == paragraphId) {
                    scrollContentRecycler(i, null);
                    return;
                }
            }
        }

        //防止递归过多次
        final int MAX_DEPTH = 5;
        if (recursionDepth > MAX_DEPTH) return;

        //滚动到缓存外触发分页加载
        if (queriedPos == RecyclerView.NO_POSITION) {
            //缓存中找不到则从数据库中读取具体位置
            DiaryDb db = DiaryDb.getInstance(this);
            ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
            disposable.add(db.paragraphDao().getParagraphPositionById(paragraphId, viewModel.isHiddenParagraphShown())
                    .subscribeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            paragraphPos -> scrollContentRecycler(paragraphPos, new ScrollHelper.PagingRecyclerScrollListener() {
                                @Override
                                public void onSucceed(int successPosition) {
                                    binding.contentRecycler.postDelayed(
                                            () -> scrollToParagraph(paragraphId, paragraphPos, recursionDepth + 1),
                                            200
                                    );
                                }

                                @Override
                                public void onRetry(int count) {

                                }

                                @Override
                                public void onFailed() {
                                    Toast.makeText(DiaryReadActivity.this, "滚动失败", Toast.LENGTH_SHORT).show();
                                }
                            }),
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        } else {
            scrollContentRecycler(queriedPos, new ScrollHelper.PagingRecyclerScrollListener() {
                @Override
                public void onSucceed(int successPosition) {
                    binding.contentRecycler.postDelayed(
                            () -> scrollToParagraph(paragraphId, queriedPos, recursionDepth + 1),
                            200
                    );
                }

                @Override
                public void onRetry(int count) {

                }

                @Override
                public void onFailed() {

                }
            });
        }
    }

    /**
     * 滚动到指定的段落
     *
     * @param paragraphId 段落编号
     */
    private void scrollToParagraph(long paragraphId) {
        scrollToParagraph(paragraphId, RecyclerView.NO_POSITION, 0);
    }

    /**
     * 显示段落修改菜单
     *
     * @param model 需要修改的段落的数据模型
     * @param view  下拉菜单锚点
     */
    private void showParagraphModifyMenu(@NonNull ParagraphUnionModel model, View view) {
        ParagraphEntity paragraph = model.getParagraph();
        PopupMenu menu = new PopupMenu(this, view, Gravity.END);
        menu.getMenuInflater().inflate(R.menu.menu_paragraph_edit, menu.getMenu());

        menu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_modify_content) {
                Intent skip2Write = new Intent(DiaryReadActivity.this, WriteActivity.class);
                Bundle bundle = new Bundle();

                bundle.putLong(KeyStrings.INIT_DATE.v(), DateTimeConverter.fromLocalDateTime(paragraph.getCreateTime())); //段落所在的日期
                bundle.putLong(KeyStrings.WRITE_MODIFY_PARAGRAPH_ID.v(), paragraph.getParagraphId());    //段落 ID

                skip2Write.putExtras(bundle);
                startActivity(skip2Write);
                return true;
            } else if (id == R.id.action_modify_time) {
                modifyCreateTime(paragraph);
                return true;
            } else if (id == R.id.action_modify_emotion) {
                modifyEmotion(paragraph);
                return true;
            } else if (id == R.id.action_change_privacy_type) {
                switchParagraphPrivacyType(paragraph);
                return true;
            } else if (id == R.id.action_copy_paragraph) {
                TextHelper.copyToClipBoard(this, "日记段落", paragraph.getContent());
                Toast.makeText(this, "段落内容已复制", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_delete_paragraph) {
                deleteParagraph(paragraph);
                return true;
            } else {
                return false;
            }
        });

        menu.show();
    }

    /**
     * 更新段落创建日期
     *
     * @param paragraph 原来的段落实例
     */
    private void modifyCreateTime(@NonNull ParagraphEntity paragraph) {
        DateTimePickerHelper.selectTime(
                paragraph.getCreateTime(),
                getSupportFragmentManager(),
                timePicker -> {
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    LocalDateTime newDateTime = paragraph.getCreateTime()
                            .withHour(hour)
                            .withMinute(minute);

                    DiaryDb db = DiaryDb.getInstance(this);
                    disposable.add(db.diaryDao().getEarliestDiaryDateSingle()
                            .flatMap(optional -> {
                                LocalDate startDate = optional.orElseGet(LocalDate::now);
                                ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
                                return ParagraphService.modifyCreateTime(
                                        paragraph.getParagraphId(),
                                        startDate,
                                        newDateTime,
                                        viewModel.isHiddenParagraphShown(),
                                        db
                                );
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    pos -> {
                                        Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "段落时间修改成功");
                                        Toast.makeText(this, "段落时间修改成功", Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        ExceptionHelper.showExceptionDialog(this, throwable);
                                        Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落时间修改失败");
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
        EmotionTagSelectBottomSheet bottomSheet = EmotionTagSelectBottomSheet.newInstance(paragraph.getParagraphId());
        bottomSheet.show(getSupportFragmentManager(), TagStrings.EMOTION_SELECT_BOTTOM_SHEET.t());
    }

    /**
     * 切换段落的隐私类别
     *
     * @param paragraph 需要修改隐私类别的段落
     */
    private void switchParagraphPrivacyType(@NonNull ParagraphEntity paragraph) {
        //获取种类和标题数组
        ParagraphPrivacyType[] types = ParagraphPrivacyType.values();
        String[] titles = Arrays.stream(types)
                .map(ParagraphPrivacyType::getTitle)
                .toArray(String[]::new);

        //显示对话框
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.switch_privacy_type)
                .setSingleChoiceItems(titles, paragraph.getPrivacyType(), (dialogInterface, i) -> {
                    dialogInterface.dismiss();

                    ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
                    if ((i == ParagraphPrivacyType.HIDE_FROM_LIST.ordinal() ||
                            paragraph.getPrivacyType() == ParagraphPrivacyType.HIDE_FROM_LIST.ordinal()) &&
                            viewModel.isNotAuthed()) {
                        BiometricHelper.showBiometricPrompt("隐私段落保护", "您正试图查看受保护的段落", this, new BiometricHelper.AuthCallback() {
                            @Override
                            public void onSuccess() {
                                DiaryDb db = DiaryDb.getInstance(DiaryReadActivity.this);
                                disposable.add(db.paragraphDao().updatePrivacyTypeCompletable(paragraph.getParagraphId(), i)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(
                                                () -> Toast.makeText(DiaryReadActivity.this, "隐私类别修改成功", Toast.LENGTH_SHORT).show(),
                                                e -> ExceptionHelper.showExceptionDialog(DiaryReadActivity.this, e)
                                        )
                                );
                                viewModel.setIsAuthed(true);
                            }

                            @Override
                            public void onError(int errCode, CharSequence errStr) {
                                Toast.makeText(DiaryReadActivity.this, errStr, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailed() {
                            }
                        });
                    } else {
                        //从隐藏内容切换为别的类型时取消显示其内容
                        if (paragraph.getPrivacyType() == ParagraphPrivacyType.HIDE_CONTENT.ordinal() &&
                                i != ParagraphPrivacyType.HIDE_CONTENT.ordinal()) {
                            viewModel.contentDisplayIdSet.remove(paragraph.getParagraphId());
                        }

                        DiaryDb db = DiaryDb.getInstance(this);
                        disposable.add(db.paragraphDao().updatePrivacyTypeCompletable(paragraph.getParagraphId(), i)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        () -> Toast.makeText(this, "隐私类别修改成功", Toast.LENGTH_SHORT).show(),
                                        e -> ExceptionHelper.showExceptionDialog(this, e)
                                )
                        );
                    }
                })
                .setNegativeButton("取消", null)
                .show();
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
                .addTarget(binding.searchSkipLayout)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(250);

        //通知布局即将发生变化
        TransitionManager.beginDelayedTransition(binding.getRoot(), set);

        if (!isSearchMode) {
            binding.contentSearchBar.setText(null);
            binding.searchSkipLayout.setVisibility(View.GONE);

            backHelper.unregisterHandler(searchBackHandler);
        } else {
            binding.searchSkipLayout.setVisibility(View.VISIBLE);

            backHelper.registerHandler(searchBackHandler);
        }
    }

    /**
     * 设置分享时的段落多选模式是否开启
     *
     * @param isSelectMode 是否开启段落多选模式
     */
    private void setShareSelectMode(boolean isSelectMode) {
        if (isSelectMode == adapter.getSelectMode()) return;

        //更新 UI
        adapter.setSelectMode(isSelectMode);
        if (isSelectMode) {
            backHelper.registerHandler(shareChoiceBackHandler);
        } else {
            backHelper.unregisterHandler(shareChoiceBackHandler);

            selectionTracker.clearSelection();  //清空多选
        }
    }

    /**
     * 跳转到对应的日期
     *
     * @param targetDate 目标日期
     */
    private void skipToTargetDate(@NonNull LocalDate targetDate) {
        //跳转到对应位置
        DiaryDb db = DiaryDb.getInstance(this);
        ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
        disposable.add(db.paragraphDao().getDiaryStartPositionByDate(targetDate, viewModel.isHiddenParagraphShown())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        paragraphIdOptional -> {
                            if (paragraphIdOptional.isEmpty()) {
                                scrollContentRecycler(adapter.getItemCount() - 1, null);
                            } else {
                                scrollToParagraph(paragraphIdOptional.get());
                            }
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 搜索框菜单点击显示隐藏段落的回调
     */
    private void changeHiddenParagraphVisibility() {
        ParagraphFilterViewModel viewModel = new ViewModelProvider(this).get(ParagraphFilterViewModel.class);
        if (viewModel.isNotAuthed() && !viewModel.isHiddenParagraphShown()) {
            BiometricHelper.showBiometricPrompt("隐私保护", "您正试图查看受保护的段落", this, new BiometricHelper.AuthCallback() {
                @Override
                public void onSuccess() {
                    viewModel.showHiddenParagraph(true);
                    Toast.makeText(DiaryReadActivity.this, "已显示受保护的段落", Toast.LENGTH_SHORT).show();
                    viewModel.setIsAuthed(true);
                }

                @Override
                public void onError(int errCode, CharSequence errStr) {
                    if (errCode == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                        viewModel.showHiddenParagraph(true);
                        Toast.makeText(DiaryReadActivity.this, "请设置锁屏验证方式以保护隐私段落", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DiaryReadActivity.this, errStr, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailed() {
                }
            });
        } else {
            boolean currentStat = viewModel.isHiddenParagraphShown();
            viewModel.showHiddenParagraph(!currentStat);

            String tip = currentStat ? "已隐藏受保护的段落" : "已显示受保护的段落";
            Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
        }
    }
}