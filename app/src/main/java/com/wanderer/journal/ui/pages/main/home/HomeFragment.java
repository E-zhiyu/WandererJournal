package com.wanderer.journal.ui.pages.main.home;

import android.content.Intent;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;    //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //订阅列表

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
        //日期统计卡片
        initDateCard();

        //计数卡片
        initDiaryCountCard();

        //情绪标签卡片
        initEmotionTagCountCard();
    }

    /**
     * 初始化日期统计卡片
     */
    private void initDateCard() {
        //设置卡片圆角
        AppearanceAnimationHelper.setRadius(
                requireContext(),
                binding.dateCard,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS
        );

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
                        binding.dateDifferenceText.setText(R.string.unknown_difference_of_date);
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd\nEEEE");
                        binding.startDateText.setText(date.format(formatter));

                        //计算时间差
                        long difference = ChronoUnit.DAYS.between(date, LocalDate.now());
                        binding.dateDifferenceText.setText(String.format(
                                Locale.getDefault(),
                                "距今%d天",
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
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS
        );

        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        DiaryDao diaryDao = db.diaryDao();
        ParagraphDao paragraphDao = db.paragraphDao();

        //日记数量
        disposable.add(diaryDao.getDiaryCountFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(count -> binding.diaryCountText.setText(String.valueOf(count)))
        );

        //段落数量
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
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS
        );
        AppearanceAnimationHelper.attachMorphAnimation(binding.emotionTagCountCard);

        //设置情绪标签管理入口
        binding.emotionTagCountCard.setOnClickListener(view -> {
            Intent skip2EmotionTagManage = new Intent(requireContext(), EmotionTagManageActivity.class);
            startActivity(skip2EmotionTagManage);
        });

        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        EmotionTagDao emotionTagDao = db.emotionTagDao();

        //情绪标签数量
        disposable.add(emotionTagDao.getEmotionTagCountFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(count -> binding.emotionTagCountText.setText(String.valueOf(count)))
        );
    }
}