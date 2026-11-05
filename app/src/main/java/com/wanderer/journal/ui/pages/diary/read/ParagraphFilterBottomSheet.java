package com.wanderer.journal.ui.pages.diary.read;

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

import com.wanderer.journal.data.save.db.DiaryDb;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.databinding.BottomSheetEmotionTagFilterBinding;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceHelper;
import com.wanderer.journal.ui.others.adapters.emotion.EmotionTagFilterAdapter;
import com.wanderer.journal.ui.others.bottom.BaseBottomSheetDialogFragment;

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
            viewModel.notifyFilterUpdated();
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
        //设置适配器
        ReadViewModel viewModel = new ViewModelProvider(requireActivity()).get(ReadViewModel.class);
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
        binding.mediaFilterChip.setChecked(viewModel.getFilterMedia());
        binding.mediaFilterChip.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setFilterMedia(isChecked)
        );

        //位置过滤 Chip
        binding.locationFilterChip.setChecked(viewModel.isFilterLocation());
        binding.locationFilterChip.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setFilterLocation(isChecked)
        );
    }
}
