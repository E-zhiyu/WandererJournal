package com.wanderer.journal.ui.others.bottom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.wanderer.journal.auxiliary.enums.KeyStrings;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.entities.composite.ui.EmotionTagUiModel;
import com.wanderer.journal.databinding.BottomSheetEmotionTagSelectBinding;
import com.wanderer.journal.databinding.PopupWindowSliderBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.adapters.emotion.EmotionTagSelectAdapter;
import com.wanderer.journal.ui.others.viewmodel.EmotionTagSelectViewModel;
import com.wanderer.journal.ui.pages.emotion.EmotionTagInputActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EmotionTagSelectBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetEmotionTagSelectBinding binding;             //绑定的XML布局
    private long paragraphId;                                       //正在选择情绪标签的段落编号
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            paragraphId = getArguments().getLong(KeyStrings.PARAGRAPH_ID.getS());
            EmotionTagSelectViewModel viewModel = new ViewModelProvider(requireActivity()).get(EmotionTagSelectViewModel.class);
            viewModel.setParagraphId(paragraphId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEmotionTagSelectBinding.inflate(inflater, container, false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

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
     * 创建新实例的方法
     *
     * @param paragraphId 正在选择情绪标签的段落 ID
     * @return 情绪标签选择对话框
     */
    @NonNull
    public static EmotionTagSelectBottomSheet newInstance(long paragraphId) {
        EmotionTagSelectBottomSheet bottomSheet = new EmotionTagSelectBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putLong(KeyStrings.PARAGRAPH_ID.getS(), paragraphId);
        bottomSheet.setArguments(bundle);
        return bottomSheet;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //情绪标签列表
        EmotionTagSelectAdapter adapter = new EmotionTagSelectAdapter(
                (model, view) -> {
                    if (model == null) {
                        Intent skip2EmotionAdd = new Intent(requireContext(), EmotionTagInputActivity.class);
                        startActivity(skip2EmotionAdd);
                        return;
                    }

                    //仅当没有选中时才报告给 ViewModel
                    if (!model.isChecked()) {
                        EmotionTagSelectViewModel viewModel = new ViewModelProvider(requireActivity()).get(EmotionTagSelectViewModel.class);
                        viewModel.setCheckedEmotionTag(model.getEmotionTag(), true, 1);
                    }

                    //显示悬浮滑块
                    showSuspendedSlider(model, view);
                },
                model -> {
                    if (model == null) {
                        return;
                    }

                    //目标状态与当前状态相同则不触发监听
                    if (!model.isChecked()) {
                        return;
                    }

                    EmotionTagSelectViewModel viewModel = new ViewModelProvider(requireActivity()).get(EmotionTagSelectViewModel.class);
                    viewModel.setCheckedEmotionTag(model.getEmotionTag(), false, 1);
                }
        );
        binding.recycler.setAdapter(adapter);

        //获取数据源
        EmotionTagDao dao = DiaryDatabase.getInstance(requireContext()).emotionTagDao();
        disposable.add(dao.getSelectableEmotionTagFlowable(paragraphId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        emotionList -> {
                            emotionList.add(null);  //加上一个 null 占位符，作为添加标签的功能按钮
                            adapter.submitList(emotionList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );
    }

    /**
     * 显示悬浮滑块
     *
     * @param model  数据模型
     * @param anchor 浮动滑块绑定的视图
     */
    private void showSuspendedSlider(@NonNull EmotionTagUiModel model, @NonNull View anchor) {
        // 1. 加载悬浮窗布局
        Context context = anchor.getContext();
        PopupWindowSliderBinding popupWindowBinding = PopupWindowSliderBinding.inflate(
                LayoutInflater.from(context)
        );

        // 2. 初始化 Slider 的值
        popupWindowBinding.popupSlider.setValue(model.getDegree());

        // 3. 创建 PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                popupWindowBinding.getRoot(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // true 代表点击外部时自动消失
        );

        // 设置背景以允许点击外部消失（MDC 环境下可选）
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);

        // 4. 设置 Slider 滑动监听
        popupWindowBinding.popupSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int degree = (int) slider.getValue();
                EmotionTagSelectViewModel viewModel = new ViewModelProvider(requireActivity()).get(EmotionTagSelectViewModel.class);
                viewModel.setCheckedEmotionTag(model.getEmotionTag(), true, degree);
            }
        });

        // 5. 测量并计算位置，将悬浮窗精确显示在 Chip 的正上方
        popupWindowBinding.getRoot().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupWindowBinding.getRoot().getMeasuredHeight();
        int popupWidth = popupWindowBinding.getRoot().getMeasuredWidth();

        // xOff: 居中对齐, yOff: 放在 Chip 上方（注意负数表示向上偏移）
        int xOffset = (anchor.getWidth() - popupWidth) / 2;
        int yOffset = -(anchor.getHeight() + popupHeight);

        popupWindow.showAsDropDown(anchor, xOffset, yOffset);
    }
}
