package com.wanderer.journal.ui.pages.statistics;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
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

import com.google.android.material.chip.Chip;
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.auxiliary.enums.text.EmotionType;
import com.wanderer.journal.auxiliary.enums.text.RoleRelationship;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.entities.RoleEntity;
import com.wanderer.journal.data.save.db.entities.composite.EmotionTagUseCountModel;
import com.wanderer.journal.data.save.db.services.DiaryService;
import com.wanderer.journal.data.save.db.services.ParagraphService;
import com.wanderer.journal.data.save.preference.TipPreference;
import com.wanderer.journal.databinding.ActivityStatisticsBinding;
import com.wanderer.journal.databinding.PopupWindowMemeryPixelBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.ui.others.decoration.MonthHeaderDecoration;
import com.wanderer.journal.ui.pages.DiaryReadActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
        binding.getRoot().postDelayed(this::initGuide, 250);
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
        AppearanceHelper.setRadius(
                this,
                binding.continuousCountCard,
                AppearanceHelper.MEDIUM_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS
        );
        AppearanceHelper.setRadius(
                this,
                binding.maxContinuousCard,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.MEDIUM_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS
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
        AppearanceHelper.setRadius(
                this,
                binding.maxCharacterCountCard,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.MEDIUM_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS
        );
        AppearanceHelper.setRadius(
                this,
                binding.averageCharacterCountCard,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.MEDIUM_CARD_RADIUS
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

        //角色统计
        initRoleStatisticsCard();

        //情绪标签统计
        initEmotionStatisticsCard();
    }

    /**
     * 初始化用户引导
     */
    private void initGuide() {
        //角色引用的方法
        TipPreference.showTip(
                binding.weekLayout,
                Gravity.END,
                "点击记忆像素可查看当天日记",
                TipPreference.KEY_MEMERY_PIXEL_CHECK,
                1
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

                    //设置文本和按钮可见性
                    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
                    windowBinding.dateText.setText(model.getDiaryDate().format(formatter));
                    String lenStr;
                    if (model.getDiaryLength() == 0) {
                        lenStr = "无日记";
                        windowBinding.checkDiaryBtn.setVisibility(View.GONE);
                    } else {
                        lenStr = model.getDiaryLength() + "字符";
                        windowBinding.checkDiaryBtn.setVisibility(View.VISIBLE);
                    }
                    windowBinding.diaryLengthText.setText(lenStr);

                    //设置查看日记按钮点击监听
                    windowBinding.checkDiaryBtn.setOnClickListener(view1 -> {
                        Intent skip2DiaryRead = new Intent(this, DiaryReadActivity.class);
                        Bundle bundle = new Bundle();

                        bundle.putLong(KeyStrings.INIT_DATE.getS(), DateTimeConverter.fromLocalDate(model.getDiaryDate()));

                        skip2DiaryRead.putExtras(bundle);
                        startActivity(skip2DiaryRead);

                        //让浮窗消失
                        popupWindow.dismiss();
                    });

                    //设置背景以允许点击外部消失
                    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    popupWindow.setOutsideTouchable(true);

                    //测量并计算位置，将悬浮窗精确显示在 Chip 的正上方
                    windowBinding.getRoot().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    int popupHeight = windowBinding.getRoot().getMeasuredHeight();
                    int popupWidth = windowBinding.getRoot().getMeasuredWidth();

                    //xOff: 居中对齐, yOff: 放在上方
                    int xOffset = (view.getWidth() - popupWidth) / 2;
                    int yOffset = -(view.getHeight() + popupHeight) - AppearanceHelper.dpToPx(this, 5);

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
                            adapter.submitList(modelList, () ->
                                    layoutManager.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0)
                            );

                            //处理装饰器以绘制月份标签
                            if (headerDecoration != null) {
                                binding.memeryPixelRecycler.removeItemDecoration(headerDecoration);
                            }
                            headerDecoration = new MonthHeaderDecoration(modelList, this);
                            binding.memeryPixelRecycler.addItemDecoration(headerDecoration);
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 初始化角色统计卡片
     */
    private void initRoleStatisticsCard() {
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        disposable.add(db.roleDao().getAllRoleFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        roleList -> {
                            //设置卡片可见性
                            int cardVisibility = roleList.isEmpty() ? View.GONE : View.VISIBLE;
                            binding.roleStatisticsCard.setVisibility(cardVisibility);

                            if (roleList.isEmpty()) return;

                            //获取常用角色
                            List<RoleEntity> commonRoleList = roleList.stream()
                                    .filter(role -> role.getUseCount() > 0)
                                    .limit(5)
                                    .collect(Collectors.toList());
                            int commonRoleVisibility = commonRoleList.isEmpty() ? View.GONE : View.VISIBLE;
                            binding.commonRoleTitle.setVisibility(commonRoleVisibility);
                            binding.commonRoleChipGroup.setVisibility(commonRoleVisibility);

                            //将常用角色添加到 ChipGroup 中
                            binding.commonRoleChipGroup.removeAllViews();
                            for (RoleEntity role : commonRoleList) {
                                Chip chip = new Chip(this);
                                chip.setClickable(false);
                                chip.setText(String.format(
                                        Locale.getDefault(),
                                        "%s ×%d",
                                        role.generateDisplayName(),
                                        role.getUseCount()
                                ));

                                binding.commonRoleChipGroup.addView(chip);
                            }

                            //获取角色关系数据
                            Map<Integer, Long> roleRelationshipMap = roleList.stream()
                                    .collect(Collectors.groupingBy(
                                            RoleEntity::getRelationship,
                                            LinkedHashMap::new,
                                            Collectors.counting()
                                    ));

                            //显示角色关系数据
                            RoleRelationship[] relationships = RoleRelationship.values();
                            binding.roleRelationshipGroup.removeAllViews();
                            for (Map.Entry<Integer, Long> entry : roleRelationshipMap.entrySet()) {
                                String relationship = relationships[entry.getKey()].getTitle();
                                long count = entry.getValue();

                                Chip chip = new Chip(this);
                                chip.setClickable(false);
                                chip.setText(String.format(
                                        Locale.getDefault(),
                                        "%s ×%d",
                                        relationship,
                                        count
                                ));

                                binding.roleRelationshipGroup.addView(chip);
                            }
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 初始化情绪标签统计数据
     */
    private void initEmotionStatisticsCard() {
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        disposable.add(db.emotionTagDao().getUsedEmotionTagFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        usedModelList -> {
                            if (usedModelList.isEmpty()) {
                                binding.emotionStatisticsCard.setVisibility(View.GONE);
                                return;
                            }

                            //添加使用的标签到 ChipGroup
                            List<EmotionTagUseCountModel> commonEmotionTagList = usedModelList.stream()
                                    .limit(5)
                                    .collect(Collectors.toList());
                            binding.commonEmotionChipGroup.removeAllViews();
                            for (EmotionTagUseCountModel model : commonEmotionTagList) {
                                Chip chip = new Chip(this);
                                chip.setClickable(false);
                                chip.setText(String.format(
                                        Locale.getDefault(),
                                        "%s ×%d",
                                        model.getEmotionTag().getName(),
                                        model.getUseCount()
                                ));

                                binding.commonEmotionChipGroup.addView(chip);
                            }

                            //获取不同种类标签的数量
                            Map<Integer, List<EmotionTagUseCountModel>> typeMap = usedModelList.stream()
                                    .collect(Collectors.groupingBy(
                                            model -> model.getEmotionTag().getType(),
                                            LinkedHashMap::new,
                                            Collectors.toList()
                                    ));

                            //将种类计数数据添加到 ChipGroup 中
                            EmotionType[] types = EmotionType.values();
                            binding.emotionTypeChipGroup.removeAllViews();
                            for (Map.Entry<Integer, List<EmotionTagUseCountModel>> entry : typeMap.entrySet()) {
                                String typeTitle = types[entry.getKey()].getTitle();
                                int typeCount = entry.getValue().stream()
                                        .map(EmotionTagUseCountModel::getUseCount)
                                        .reduce(0, Integer::sum);

                                Chip chip = new Chip(this);
                                chip.setClickable(false);
                                chip.setText(String.format(
                                        Locale.getDefault(),
                                        "%s ×%d",
                                        typeTitle,
                                        typeCount
                                ));

                                binding.emotionTypeChipGroup.addView(chip);
                            }
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }
}