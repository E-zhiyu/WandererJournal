package com.wanderer.journal.ui.others.bottom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.slider.Slider;
import com.wanderer.journal.auxiliary.enums.unique.KeyStrings;
import com.wanderer.journal.data.save.db.DiaryDb;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.databinding.BottomSheetEmotionTagSelectBinding;
import com.wanderer.journal.databinding.PopupWindowSliderBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.adapters.emotion.EmotionTagSelectAdapter;
import com.wanderer.journal.ui.others.viewmodel.EmotionTagSelectViewModel;
import com.wanderer.journal.ui.pages.emotion.EmotionTagInputActivity;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EmotionTagSelectBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetEmotionTagSelectBinding binding;             //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表
    private final Map<Long, Integer> emotionTagSelectMap = new HashMap<>();     //情绪标签选择数据

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEmotionTagSelectBinding.inflate(inflater, container, false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        //加载初始数据
        EmotionTagSelectViewModel viewModel = new ViewModelProvider(requireActivity()).get(EmotionTagSelectViewModel.class);
        Map<Long, Integer> emotionTagSelectMap = viewModel.getEmotionTagSelectMap();
        this.emotionTagSelectMap.putAll(emotionTagSelectMap);

        initViews();

        //添加消失监听
        setOnDismissListener(() -> {
            viewModel.setEmotionTagSelectMap(this.emotionTagSelectMap);
            setOnDismissListener(null);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.dispose();
        binding = null;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //情绪标签列表
        EmotionTagSelectAdapter adapter = new EmotionTagSelectAdapter(
                emotionTagSelectMap.keySet(),
                (entity, view) -> {
                    if (entity == null) {
                        return;
                    }

                    //仅当没有选中时才报告给 ViewModel
                    long emotionId = entity.getEmotionId();
                    if (!emotionTagSelectMap.containsKey(emotionId)) {
                        emotionTagSelectMap.put(emotionId, 1);
                    }

                    //显示悬浮滑块
                    showSuspendedSlider(entity, view);
                },
                model -> {
                    if (model == null) {
                        return;
                    }

                    //在 Map 中删除该情绪标签
                    emotionTagSelectMap.remove(model.getEmotionId());
                }
        );
        binding.recycler.setAdapter(adapter);

        //获取数据源
        EmotionTagDao dao = DiaryDb.getInstance(requireContext()).emotionTagDao();
        disposable.add(dao.getEmotionTagFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        adapter::submitList,
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );

        //添加按钮
        binding.addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EmotionTagInputActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 显示悬浮滑块用于修改情绪标签的等级
     *
     * @param emotionTag 情绪标签
     * @param anchor     浮动滑块绑定的视图
     */
    private void showSuspendedSlider(@NonNull EmotionTagEntity emotionTag, @NonNull View anchor) {
        //加载悬浮窗布局
        Context context = anchor.getContext();
        PopupWindowSliderBinding popupWindowBinding = PopupWindowSliderBinding.inflate(
                LayoutInflater.from(context)
        );

        //初始化 Slider 的值
        Integer savedDegree = emotionTagSelectMap.get(emotionTag.getEmotionId());
        popupWindowBinding.popupSlider.setValue(savedDegree == null ? 1 : savedDegree);

        //创建 PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                popupWindowBinding.getRoot(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // true 代表点击外部时自动消失
        );

        //设置 Slider 滑动监听
        Slider.OnSliderTouchListener sliderTouchListener = new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int degree = (int) slider.getValue();
                emotionTagSelectMap.put(emotionTag.getEmotionId(), degree);
            }
        };
        popupWindowBinding.popupSlider.addOnSliderTouchListener(sliderTouchListener);
        popupWindow.setOnDismissListener(() ->
                popupWindowBinding.popupSlider.removeOnSliderTouchListener(sliderTouchListener)
        );

        //测量并计算位置，将悬浮窗精确显示在 Chip 的正上方
        popupWindowBinding.getRoot().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupWindowBinding.getRoot().getMeasuredHeight();
        int popupWidth = popupWindowBinding.getRoot().getMeasuredWidth();

        // xOff: 居中对齐, yOff: 放在 Chip 上方（注意负数表示向上偏移）
        int xOffset = (anchor.getWidth() - popupWidth) / 2;
        int yOffset = -(anchor.getHeight() + popupHeight);

        popupWindow.showAsDropDown(anchor, xOffset, yOffset);
    }
}
