package com.wanderer.journal.ui.others.bottom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.data.save.db.entities.EmotionTagEntity;
import com.wanderer.journal.databinding.BottomSheetEmotionTagFilterBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.adapters.emotion.EmotionTagFilterAdapter;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DiaryParagraphFilterBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetEmotionTagFilterBinding binding;                         //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表
    private final List<Long> checkedEmotionTagIdList;                           //选中的情绪标签 ID 列表，用于初始化视图选中状态
    private final OnCheckedChangedListener checkedChangedListener;              //情绪标签选中状态变更监听器
    private final OnClearedListener clearedListener;                            //清除筛选的监听器
    private final OnOthersOptionChangedListener othersOptionChangedListener;    //其他过滤选项变更监听
    private final boolean initFilterMedia;                                      //初始化是否过滤媒体

    public interface OnCheckedChangedListener {
        /**
         * 标签选中状态变更回调
         *
         * @param emotionTag 改变选中状态的情绪标签
         * @param isChecked  是否被选中
         */
        void onCheckChanged(EmotionTagEntity emotionTag, boolean isChecked);
    }

    public interface OnClearedListener {
        /**
         * 清除筛选回调
         */
        void onCleared();
    }

    public interface OnOthersOptionChangedListener {
        /**
         * 媒体过滤切换回调
         *
         * @param isFiltered 是否过滤
         */
        void onMediaFilterChanged(boolean isFiltered);
    }

    /**
     * 情绪标签筛选对话框构造方法
     *
     * @param checkedEmotionTagIdList 已选中的情绪标签 ID 列表
     * @param initFilterMedia         初始化是否选择了媒体过滤 Chip
     * @param checkedChangedListener  情绪标签选中状态变更监听器
     * @param clearedListener         清空按钮点击监听
     */
    public DiaryParagraphFilterBottomSheet(
            List<Long> checkedEmotionTagIdList,
            boolean initFilterMedia,
            OnCheckedChangedListener checkedChangedListener,
            OnClearedListener clearedListener, OnOthersOptionChangedListener othersOptionChangedListener
    ) {
        this.checkedEmotionTagIdList = checkedEmotionTagIdList;
        this.initFilterMedia = initFilterMedia;
        this.checkedChangedListener = checkedChangedListener;
        this.clearedListener = clearedListener;
        this.othersOptionChangedListener = othersOptionChangedListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEmotionTagFilterBinding.inflate(inflater, container, false);

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
        //设置适配器
        EmotionTagFilterAdapter adapter = new EmotionTagFilterAdapter(
                checkedEmotionTagIdList,
                checkedChangedListener::onCheckChanged
        );
        binding.emotionTagRecycler.setAdapter(adapter);

        //获取数据源
        EmotionTagDao emotionTagDao = DiaryDatabase.getInstance(requireContext()).emotionTagDao();
        disposable.add(emotionTagDao.getAllEmotionTagFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        adapter::submitList,
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );

        //清除按钮
        binding.clearBtn.setOnClickListener(view -> {
            clearedListener.onCleared();
            dismiss();
        });

        //图片过滤 Chip
        binding.mediaFilterChip.setChecked(initFilterMedia);
        binding.mediaFilterChip.setOnCheckedChangeListener(
                (compoundButton, b) ->
                        othersOptionChangedListener.onMediaFilterChanged(b)
        );
    }
}
