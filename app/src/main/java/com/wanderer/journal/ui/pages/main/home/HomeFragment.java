package com.wanderer.journal.ui.pages.main.home;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.databinding.FragmentHomeBinding;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.ui.pages.emotion.EmotionTagManageActivity;
import com.wanderer.journal.ui.pages.DiaryReadActivity;
import com.wanderer.journal.ui.pages.statistics.StatisticsActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;        //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //订阅列表
    private final List<String> tipsList = new LinkedList<>();   //提示文本列表

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        disposable.dispose();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //随机提示文本
        binding.randomTipCard.setOnClickListener(view -> showNextRandomTipText());
        AppearanceAnimationHelper.attachMorphAnimation(binding.randomTipCard);
        showNextRandomTipText();

        initDateCard();
        initDiaryCountCard();
        initParagraphCountCard();
        initEmotionTagCountCard();
    }

    /**
     * 初始化日期统计卡片
     */
    private void initDateCard() {
        //设置卡片圆角
        AppearanceAnimationHelper.setRadius(
                requireContext(),
                binding.diaryDateCard,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS
        );

        //设置点击监听
        binding.diaryDateCard.setOnClickListener(view -> {
            Intent skip2DiaryRead = new Intent(requireContext(), DiaryReadActivity.class);
            startActivity(skip2DiaryRead);
        });
        AppearanceAnimationHelper.attachMorphAnimation(binding.diaryDateCard);

        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        DiaryDao diaryDao = db.diaryDao();

        //日记开始日期
        disposable.add(diaryDao.getEarliestDiaryDateFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(dateOptional -> {
                    LocalDate date = dateOptional.orElse(null);
                    if (date == null) {
                        binding.startDateText.setText(R.string.cant_get);
                        binding.dateDifferenceText.setText(R.string.unknown);
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd\nEEEE");
                        binding.startDateText.setText(date.format(formatter));

                        //计算时间差
                        long difference = ChronoUnit.DAYS.between(date, LocalDate.now());
                        binding.dateDifferenceText.setText(String.format(
                                Locale.getDefault(),
                                "%d天",
                                difference
                        ));
                    }
                })
        );
    }

    /**
     * 初始化计数卡片
     */
    private void initDiaryCountCard() {
        //设置卡片圆角
        AppearanceAnimationHelper.setRadius(
                requireContext(),
                binding.diaryCountCard,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS
        );

        //设置点击监听
        binding.diaryCountCard.setOnClickListener(view -> {
            Intent skip2DiaryStatistics = new Intent(requireContext(), StatisticsActivity.class);
            startActivity(skip2DiaryStatistics);
        });
        AppearanceAnimationHelper.attachMorphAnimation(binding.diaryCountCard);

        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        DiaryDao diaryDao = db.diaryDao();

        //日记数量
        disposable.add(diaryDao.getDiaryCountFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(count -> binding.diaryCountText.setText(String.valueOf(count)))
        );
    }

    /**
     * 初始化段落计数卡片
     */
    private void initParagraphCountCard() {
        //设置圆角
        AppearanceAnimationHelper.setRadius(
                requireContext(),
                binding.paragraphCountCard,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS
        );

        //设置点击监听
        binding.paragraphCountCard.setOnClickListener(view -> {
            Intent skip2DiaryRead = new Intent(requireContext(), DiaryReadActivity.class);
            startActivity(skip2DiaryRead);
        });
        AppearanceAnimationHelper.attachMorphAnimation(binding.paragraphCountCard);

        //段落数量
        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        ParagraphDao paragraphDao = db.paragraphDao();
        disposable.add(paragraphDao.getParagraphCountFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(count -> binding.paragraphCountText.setText(String.valueOf(count)))
        );
    }

    /**
     * 初始化情绪标签卡片
     */
    private void initEmotionTagCountCard() {
        //设置卡片圆角
        AppearanceAnimationHelper.setRadius(
                requireContext(),
                binding.emotionTagCountCard,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS
        );
        AppearanceAnimationHelper.attachMorphAnimation(binding.emotionTagCountCard);

        //设置情绪标签管理入口
        binding.emotionTagCountCard.setOnClickListener(view -> {
            Intent skip2EmotionTagManage = new Intent(requireContext(), EmotionTagManageActivity.class);
            startActivity(skip2EmotionTagManage);
        });

        //情绪标签数量
        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        EmotionTagDao emotionTagDao = db.emotionTagDao();
        disposable.add(emotionTagDao.getEmotionTagCountFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(count -> binding.emotionTagCountText.setText(String.valueOf(count)))
        );
    }

    /**
     * 显示下一个随机提示文本
     */
    private void showNextRandomTipText() {
        //如果提示文本列表为空，则重新获取提示文本资源
        if (tipsList.isEmpty()) {
            String[] tipsArray = getResources().getStringArray(R.array.tips_array);
            tipsList.addAll(Arrays.stream(tipsArray).collect(Collectors.toList()));

            //添加小米专属的提示文本
            String manufacturer = Build.MANUFACTURER.toLowerCase();
            if (manufacturer.contains("xiaomi")) {
                String[] xiaomiTips = getResources().getStringArray(R.array.xiaomi_tips);
                tipsList.addAll(Arrays.stream(xiaomiTips).collect(Collectors.toList()));
            }
        }

        //获取随机下标
        Random random = new Random();
        int randomNum = random.nextInt();
        if (randomNum < 0) {
            randomNum = -randomNum;
        }
        int tipIndex = randomNum % tipsList.size();

        //显示对应的文本
        String tip = "tip : " + tipsList.get(tipIndex);
        binding.randomTipText.setText(tip);

        //删除刚刚显示的文本防止重复
        tipsList.remove(tipIndex);
    }
}