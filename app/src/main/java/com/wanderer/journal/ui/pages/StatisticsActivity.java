package com.wanderer.journal.ui.pages;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.databinding.ActivityStatisticsBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class StatisticsActivity extends AppCompatActivity {
    private ActivityStatisticsBinding binding;  //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //订阅列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
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
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS
        );
        AppearanceAnimationHelper.setRadius(
                this,
                binding.maxContinuousCard,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                AppearanceAnimationHelper.MEDIUM_CARD_RADIUS
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
}