package com.wanderer.journal.ui.pages.emotion;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.EmotionTagDao;
import com.wanderer.journal.databinding.ActivityEmotionTagManageBinding;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EmotionTagManageActivity extends AppCompatActivity {
    private ActivityEmotionTagManageBinding binding;    //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();   //任务订阅列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmotionTagManageBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.recycler.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
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

        //添加按钮
        binding.addFab.setOnClickListener(view -> {
            //TODO:跳转到添加界面
        });
        ViewEdgeHelper.setMarginToNavigation(binding.addFab, this); //确保永远与底部导航栏有一定距离

        //情绪标签列表
        EmotionTagAdapter adapter = new EmotionTagAdapter(
                emotionTag -> {
                    //TODO:完成回调
                }
        );
        binding.recycler.setAdapter(adapter);
        EmotionTagDao emotionTagDao = DiaryDatabase.getInstance(this).emotionTagDao();
        disposable.add(emotionTagDao.getAllEmotionTagFlowable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        emotionTagList -> {
                            adapter.submitList(emotionTagList);

                            if (emotionTagList.isEmpty()) {
                                binding.emptyText.setVisibility(View.VISIBLE);
                            } else {
                                binding.emptyText.setVisibility(View.GONE);
                            }
                        }
                )
        );
    }
}