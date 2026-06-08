package com.wanderer.journal.ui.pages.statistics;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.services.DiaryService;
import com.wanderer.journal.data.save.db.services.ParagraphService;
import com.wanderer.journal.databinding.ActivityStatisticsBinding;
import com.wanderer.journal.databinding.PopupWindowMemeryPixelBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.ui.others.decoration.MonthHeaderDecoration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class StatisticsActivity extends AppCompatActivity {
    private ActivityStatisticsBinding binding;  //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //订阅列表
    private MonthHeaderDecoration headerDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            binding.scrollView.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
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

        //两个连续日期卡片
        AppearanceAnimationHelper.setRadius(
                this,
                binding.continuousCountCard,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS
        );
        AppearanceAnimationHelper.setRadius(
                this,
                binding.maxContinuousCard,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS
        );

        //连续日期数据
        DiaryDao dao = DiaryDatabase.getInstance(this).diaryDao();
        disposable.add(dao.getDiaryDateSingle(LocalDate.now())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        this::initDateContinuous,
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );

        //最大段落字符数量
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        AppearanceAnimationHelper.setRadius(
                this,
                binding.maxCharacterCountCard,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS
        );
        AppearanceAnimationHelper.setRadius(
                this,
                binding.averageCharacterCountCard,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS
        );
        disposable.add(ParagraphService.getDiaryLengthData(db)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        diaryLength -> {
                            int max = diaryLength.getMax();
                            int avg = diaryLength.getAvg();

                            String maxStr = max + "字";
                            String avgStr = avg + "字";
                            binding.maxCharacterCountText.setText(maxStr);
                            binding.averageCharacterCountText.setText(avgStr);

                            initMemeryPixelRecycler(max, avg);
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 初始化连续日期数据
     *
     * @param existedDateList 所有日记的日期
     */
    private void initDateContinuous(@NonNull List<LocalDate> existedDateList) {
        int currentContinuous = 0;  //当前连续天数
        int maxContinuous = 0;      //最大连续天数

        //遍历获取数据
        for (int i = 0; i < existedDateList.size() - 1; i++) {
            LocalDate current = existedDateList.get(i);
            LocalDate next = existedDateList.get(i + 1);

            //判断是否到了末尾，防止一直不写日记仍然有连续计数的情况
            if (i == existedDateList.size() - 2) {
                LocalDate now = LocalDate.now();
                long differenceToNow = ChronoUnit.DAYS.between(next, now);
                if (differenceToNow > 1) {
                    currentContinuous = 0;  //归零
                    continue;
                }
            }

            //计算两个日期间隔天数
            long difference = ChronoUnit.DAYS.between(current, next);
            if (difference == 1) {          //日期连续
                currentContinuous = currentContinuous == 0 ? 2 : currentContinuous + 1;
                maxContinuous = Math.max(maxContinuous, currentContinuous); //如果有更大值则更新
            } else if (difference > 1) {    //日期不连续
                currentContinuous = 0;      //归零当前连续值
            }
        }

        //更新连续天数文本
        binding.continuousCountText.setText(String.valueOf(currentContinuous));
        binding.maxContinuousText.setText(String.valueOf(maxContinuous));
    }

    /**
     * 初始化记忆像素
     *
     * @param maxDiaryLength 最大日记长度
     * @param avgDiaryLength 平均日记长度
     */
    private void initMemeryPixelRecycler(int maxDiaryLength, int avgDiaryLength) {
        //实例化一个7行的网格布局并应用于Recycler
        GridLayoutManager layoutManager = new GridLayoutManager(
                this,
                7, // 7行，代表周一到周日
                GridLayoutManager.HORIZONTAL, // 横向滚动，这样左边是旧日期，右边是新日期
                false
        );
        binding.memeryPixelRecycler.setLayoutManager(layoutManager);

        MemeryPixelAdapter adapter = new MemeryPixelAdapter(
                maxDiaryLength,
                avgDiaryLength,
                (model, view) -> {
                    if (model == null) {
                        return;
                    }

                    //构建悬浮视图
                    PopupWindowMemeryPixelBinding windowBinding = PopupWindowMemeryPixelBinding.inflate(
                            LayoutInflater.from(this)
                    );
                    PopupWindow popupWindow = new PopupWindow(
                            windowBinding.getRoot(),
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            true
                    );

                    //设置文本
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    windowBinding.dateText.setText(model.getDiaryDate().format(formatter));
                    String lenStr = model.getDiaryLength() == 0 ? "无日记" : model.getDiaryLength() + "字符";
                    windowBinding.diaryLengthText.setText(lenStr);

                    //设置背景以允许点击外部消失
                    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    popupWindow.setOutsideTouchable(true);

                    //测量并计算位置，将悬浮窗精确显示在 Chip 的正上方
                    windowBinding.getRoot().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    int popupHeight = windowBinding.getRoot().getMeasuredHeight();
                    int popupWidth = windowBinding.getRoot().getMeasuredWidth();

                    //xOff: 居中对齐, yOff: 放在上方
                    int xOffset = (view.getWidth() - popupWidth) / 2;
                    int yOffset = -(view.getHeight() + popupHeight) - ViewEdgeHelper.dpToPx(this, 5);

                    popupWindow.showAsDropDown(view, xOffset, yOffset);
                }
        );
        binding.memeryPixelRecycler.setAdapter(adapter);

        //查询数据
        LocalDate end = LocalDate.now();    //直到今天
        LocalDate start = end.plusYears(-1).plusDays(1);    //从去年的今天的第二天开始
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        disposable.add(DiaryService.getMemeryPixelData(start, end, db)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        modelList -> {
                            adapter.submitList(modelList);

                            //处理装饰器以绘制月份标签
                            if (headerDecoration != null) {
                                binding.memeryPixelRecycler.removeItemDecoration(headerDecoration);
                            }
                            headerDecoration = new MonthHeaderDecoration(modelList, this);
                            binding.memeryPixelRecycler.addItemDecoration(headerDecoration);

                            //滚动到最右侧
                            binding.memeryPixelRecycler.scrollToPosition(adapter.getItemCount() - 1);
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }
}