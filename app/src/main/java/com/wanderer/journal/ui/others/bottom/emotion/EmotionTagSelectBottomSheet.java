package com.wanderer.journal.ui.others.bottom.emotion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.slider.Slider;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.entities.composite.EmotionTagUiModel;
import com.wanderer.journal.databinding.BottomSheetEmotionTagSelectBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.bottom.BaseBottomSheetDialogFragment;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EmotionTagSelectBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetEmotionTagSelectBinding binding;             //绑定的XML布局
    private final long paragraphId;
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表
    private final OnCheckedChangedListener checkedChangedListener;  //标签选中状态变更监听
    private final OnSlidedListener slidedListener;                  //滑块滑动监听

    public interface OnCheckedChangedListener {
        /**
         * 标签选中状态变更回调
         *
         * @param model     数据原型
         * @param isChecked 是否被选中
         */
        void onCheckChanged(EmotionTagUiModel model, boolean isChecked);
    }

    public interface OnSlidedListener {
        /**
         * 滑块滑动回调
         *
         * @param model 数据原型
         * @param value 改变后的程度
         */
        void onSlided(EmotionTagUiModel model, int value);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEmotionTagSelectBinding.inflate(inflater, container, false);

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
     * 情绪标签选择对话框构造方法
     *
     * @param paragraphId            需要设置情绪标签的段落 ID
     * @param checkedChangedListener 情绪标签选择状态变更监听器
     * @param slidedListener         滑块滑动监听
     */
    public EmotionTagSelectBottomSheet(
            long paragraphId,
            OnCheckedChangedListener checkedChangedListener,
            OnSlidedListener slidedListener
    ) {
        this.paragraphId = paragraphId;
        this.checkedChangedListener = checkedChangedListener;
        this.slidedListener = slidedListener;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //情绪标签列表
        EmotionTagSelectAdapter adapter = new EmotionTagSelectAdapter(
                (model, view) -> {
                    //仅当没有选中时才触发上级监听
                    if (!model.isChecked()) {
                        checkedChangedListener.onCheckChanged(model, true);
                    }

                    //显示悬浮滑块
                    showSuspendedSlider(model, view);
                },
                model -> {
                    //目标状态与当前状态相同则不触发监听
                    if (!model.isChecked()) {
                        return;
                    }

                    checkedChangedListener.onCheckChanged(model, false);
                }
        );
        binding.recycler.setAdapter(adapter);
        EmotionTagDao dao = DiaryDatabase.getInstance(requireContext()).emotionTagDao();
        disposable.add(dao.getSelectableEmotionTagFlowable(paragraphId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        adapter::submitList,
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
        @SuppressLint("InflateParams") View popupView = LayoutInflater.from(context).inflate(R.layout.popup_window_slider, null);
        Slider popupSlider = popupView.findViewById(R.id.popup_slider);

        // 2. 初始化 Slider 的值
        popupSlider.setValue(model.getDegree());

        // 3. 创建 PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // true 代表点击外部时自动消失
        );

        // 设置背景以允许点击外部消失（MDC 环境下可选）
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);

        // 4. 设置 Slider 滑动监听
        popupSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int degree = (int) slider.getValue();
                slidedListener.onSlided(model, degree);
            }
        });

        // 5. 测量并计算位置，将悬浮窗精确显示在 Chip 的正上方
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();
        int popupWidth = popupView.getMeasuredWidth();

        // xOff: 居中对齐, yOff: 放在 Chip 上方（注意负数表示向上偏移）
        int xOffset = (anchor.getWidth() - popupWidth) / 2;
        int yOffset = -(anchor.getHeight() + popupHeight);

        popupWindow.showAsDropDown(anchor, xOffset, yOffset);
    }
}
