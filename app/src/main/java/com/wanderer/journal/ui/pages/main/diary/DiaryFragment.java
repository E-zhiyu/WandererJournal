package com.wanderer.journal.ui.pages.main.diary;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.databinding.FragmentDiaryBinding;
import com.wanderer.journal.enums.LogTags;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.AppearanceAnimationHelper;
import com.wanderer.journal.ui.pages.read.DiaryReadActivity;
import com.wanderer.journal.ui.pages.write.WriteActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DiaryFragment extends Fragment {
    private FragmentDiaryBinding binding;   //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //订阅列表

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDiaryBinding.inflate(inflater, container, false);

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
        //添加FAB
        AppearanceAnimationHelper.attachMorphAnimation(binding.addFab);
        AppearanceAnimationHelper.setupFloatingBtnBehaviour(binding.diaryRecycler, binding.addFab);
        binding.addFab.setOnClickListener(view -> {
            //TODO:这里换成当天日记编辑界面
            Intent skip2DiaryContent = new Intent(requireContext(), WriteActivity.class);
            startActivity(skip2DiaryContent);
        });

        //日记列表
        DiaryAdapter adapter = new DiaryAdapter(diary -> {
            Intent skip2DiaryContent = new Intent(requireContext(), DiaryReadActivity.class);
            startActivity(skip2DiaryContent);
        });
        binding.diaryRecycler.setAdapter(adapter);
        DiaryDatabase db = DiaryDatabase.getInstance(requireContext());
        disposable.add(db.diaryDao().getAllDiariesFlowable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        adapter::submitList,
                        e -> {
                            ExceptionHelper.showExceptionDialog(requireContext(), e);
                            Log.e(LogTags.DIARY_FRAGMENT.n(), "日记数据库读取失败");
                            //TODO:停止刷新
                        },
                        () -> {
                            //TODO:完成回调
                        }
                )
        );
    }
}