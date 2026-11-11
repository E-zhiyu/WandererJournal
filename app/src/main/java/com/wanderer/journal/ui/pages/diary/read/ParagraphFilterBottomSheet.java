package com.wanderer.journal.ui.pages.diary.read;

import android.content.Context;
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
import com.wanderer.journal.data.save.db.DiaryDb;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.databinding.BottomSheetParagraphFilterBinding;
import com.wanderer.journal.databinding.PopupWindowSliderBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.ui.others.adapters.emotion.EmotionTagFilterAdapter;
import com.wanderer.journal.ui.others.bottom.BaseBottomSheetDialogFragment;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ParagraphFilterBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetParagraphFilterBinding binding;                          //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetParagraphFilterBinding.inflate(inflater, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    AppearanceHelper.dpToPx(requireContext(), 10),
                    AppearanceHelper.dpToPx(requireContext(), 10),
                    AppearanceHelper.dpToPx(requireContext(), 10),
                    systemBars.bottom
            );
            return insets;
        });

        initViews();

        //绑定消失监听器
        setOnDismissListener(() -> {
            ReadViewModel viewModel = new ViewModelProvider(requireActivity()).get(ReadViewModel.class);
            ReadViewModel.FilterConditions conditions = viewModel.getFilterConditions();
            viewModel.setFilterConditions(conditions);
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
        //读取已有的过滤条件
        ReadViewModel viewModel = new ViewModelProvider(requireActivity()).get(ReadViewModel.class);
        ReadViewModel.FilterConditions conditions = viewModel.getFilterConditions();

        //设置适配器
        EmotionTagFilterAdapter adapter = new EmotionTagFilterAdapter(
                conditions.emotionWhiteList.keySet(),
                (emotionTag, anchor) -> {
                    if (emotionTag == null) return;

                    //存入白名单
                    long emotionId = emotionTag.getEmotionId();
                    if (!conditions.emotionWhiteList.containsKey(emotionId)) {
                        conditions.emotionWhiteList.put(emotionId, 1);
                    }
                    showSuspendedSlider(emotionTag, anchor);
                },
                emotionTag -> {
                    if (emotionTag == null) return;
                    conditions.emotionWhiteList.remove(emotionTag.getEmotionId());
                }
        );
        binding.emotionTagRecycler.setAdapter(adapter);

        //获取数据源
        EmotionTagDao emotionTagDao = DiaryDb.getInstance(requireContext()).emotionTagDao();
        disposable.add(emotionTagDao.getEmotionTagFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        adapter::submitList,
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );

        //图片过滤 Chip
        binding.mediaFilterChip.setChecked(conditions.filterMedia);
        binding.mediaFilterChip.setOnCheckedChangeListener((buttonView, isChecked) ->
                conditions.filterMedia = isChecked
        );

        //位置过滤 Chip
        binding.locationFilterChip.setChecked(conditions.filterLocation);
        binding.locationFilterChip.setOnCheckedChangeListener((buttonView, isChecked) ->
                conditions.filterLocation = isChecked
        );
    }

    /**
     * 显示悬浮滑块
     *
     * @param emotionTag 标签
     * @param anchor     浮动滑块绑定的视图
     */
    private void showSuspendedSlider(@NonNull EmotionTagEntity emotionTag, @NonNull View anchor) {
        //加载悬浮窗布局
        Context context = anchor.getContext();
        PopupWindowSliderBinding popupWindowBinding = PopupWindowSliderBinding.inflate(
                LayoutInflater.from(context)
        );

        //初始化 Slider 的值
        ReadViewModel viewModel = new ViewModelProvider(requireActivity()).get(ReadViewModel.class);
        ReadViewModel.FilterConditions conditions = viewModel.getFilterConditions();
        Integer initValue = conditions.emotionWhiteList.getOrDefault(emotionTag.getEmotionId(), 1);
        popupWindowBinding.popupSlider.setValue(initValue == null ? 1 : initValue);

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
                conditions.emotionWhiteList.put(emotionTag.getEmotionId(), degree);
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
