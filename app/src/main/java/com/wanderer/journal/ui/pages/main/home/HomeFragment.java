package com.wanderer.journal.ui.pages.main.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.databinding.FragmentHomeBinding;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;

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
        initCountCard();
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
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS
        );

        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        DiaryDao diaryDao = db.diaryDao();

        //日记开始日期
        disposable.add(diaryDao.getEarliestDiaryDate()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(dateOptional -> {
                    LocalDate date = dateOptional.orElse(null);
                    if (date == null) {
                        binding.startDateText.setText("您还未开始写日记");
                        binding.dateDifferenceText.setVisibility(View.GONE);
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String tip = String.format(
                                Locale.getDefault(),
                                "最早日期：\n%s",
                                date.format(formatter)
                        );
                        binding.startDateText.setText(tip);

                        //计算日期差
                        LocalDate now = LocalDate.now();
                        long difference = ChronoUnit.DAYS.between(date, now);
                        binding.dateDifferenceText.setVisibility(View.VISIBLE);
                        binding.dateDifferenceText.setText(String.format(
                                Locale.getDefault(),
                                "距今已有%d天",
                                difference
                        ));
                    }
                })
        );


    }

    /**
     * 初始化计数卡片
     */
    private void initCountCard() {
        //设置卡片圆角
        AppearanceAnimationHelper.setRadius(
                requireContext(),
                binding.countCard,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS
        );

        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        DiaryDao diaryDao = db.diaryDao();
        ParagraphDao paragraphDao = db.paragraphDao();

        //日记数量
        disposable.add(diaryDao.getDiaryCount()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(count -> {
                    String tip = count == 0 ?
                            "没有任何日记" :
                            String.format(
                                    Locale.getDefault(),
                                    "日记天数：%d",
                                    count
                            );
                    binding.diaryCountText.setText(tip);
                })
        );

        //段落数量
        disposable.add(paragraphDao.getParagraphCount()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(count -> {
                    String tip = count == 0 ?
                            "没有任何段落" :
                            String.format(
                                    Locale.getDefault(),
                                    "总段落数：%d",
                                    count
                            );
                    binding.paragraphCountText.setText(tip);
                })
        );
    }
}