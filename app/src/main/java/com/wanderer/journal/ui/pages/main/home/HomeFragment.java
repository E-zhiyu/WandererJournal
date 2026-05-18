package com.wanderer.journal.ui.pages.main.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.databinding.FragmentHomeBinding;
import com.wanderer.journal.enums.KeyStrings;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.ui.pages.emotion.EmotionTagManageActivity;
import com.wanderer.journal.ui.pages.read.DiaryReadActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;        //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //订阅列表
    private LocalDate earliestDiaryDate = null; //最早的日记日期

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
            if (earliestDiaryDate == null) {
                Toast.makeText(requireContext(), "还没有日记", Toast.LENGTH_SHORT).show();
            } else {
                Intent skip2DiaryRead = new Intent(requireContext(), DiaryReadActivity.class);
                Bundle bundle = new Bundle();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String date = earliestDiaryDate.format(formatter);
                bundle.putString(KeyStrings.INIT_DATE.getS(), date);

                skip2DiaryRead.putExtras(bundle);
                startActivity(skip2DiaryRead);
            }
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
                        //清空最早日期引用
                        earliestDiaryDate = null;

                        binding.startDateText.setText(R.string.cant_get);
                        binding.dateDifferenceText.setText(R.string.unknown);
                    } else {
                        //保存最早日期引用
                        earliestDiaryDate = date;

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
            //TODO:跳转到日记界面
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
}