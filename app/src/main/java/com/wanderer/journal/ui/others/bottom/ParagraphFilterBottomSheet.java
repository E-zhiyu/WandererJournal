package com.wanderer.journal.ui.others.bottom;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.databinding.BottomSheetEmotionTagFilterBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.adapters.emotion.EmotionTagFilterAdapter;
import com.wanderer.journal.ui.others.viewmodel.ParagraphFilterViewModel;
import com.wanderer.journal.ui.pages.emotion.EmotionTagInputActivity;

import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ParagraphFilterBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetEmotionTagFilterBinding binding;                         //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEmotionTagFilterBinding.inflate(inflater, container, false);

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
     * 初始化视图
     */
    private void initViews() {
        //设置适配器
        ParagraphFilterViewModel viewModel = new ViewModelProvider(requireActivity()).get(ParagraphFilterViewModel.class);
        EmotionTagFilterAdapter adapter = new EmotionTagFilterAdapter(
                viewModel.getCheckedEmotionIdSet(),
                (emotionTag, isChecked) -> {
                    if (emotionTag != null) {
                        long emotionId = emotionTag.getEmotionId();
                        Set<Long> checkedEmotionIdSet = viewModel.getCheckedEmotionIdSet();
                        if (isChecked) {
                            checkedEmotionIdSet.add(emotionId);
                        } else {
                            checkedEmotionIdSet.remove(emotionId);
                        }
                    }
                },
                () -> {
                    Intent skip2EmotionInput = new Intent(requireContext(), EmotionTagInputActivity.class);
                    startActivity(skip2EmotionInput);
                }
        );
        binding.emotionTagRecycler.setAdapter(adapter);

        //获取数据源
        EmotionTagDao emotionTagDao = DiaryDatabase.getInstance(requireContext()).emotionTagDao();
        disposable.add(emotionTagDao.getAllEmotionTagFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        emotionTagList -> {
                            emotionTagList.add(null);  //加上一个 null 占位符，作为添加标签的功能按钮
                            adapter.submitList(emotionTagList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );

        //清除按钮
        binding.clearBtn.setOnClickListener(view -> {
            viewModel.clearFilter();
            dismiss();
        });

        //图片过滤 Chip
        binding.mediaFilterChip.setChecked(viewModel.getFilterMedia());
        binding.mediaFilterChip.setOnCheckedChangeListener((compoundButton, b) ->
                viewModel.setFilterMedia(b)
        );
    }
}
