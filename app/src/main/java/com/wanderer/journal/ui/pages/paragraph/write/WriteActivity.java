package com.wanderer.journal.ui.pages.paragraph.write;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.DiaryDao;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.databinding.ActivityWriteBinding;
import com.wanderer.journal.enums.LogTags;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.ui.pages.paragraph.ParagraphAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class WriteActivity extends AppCompatActivity {
    private ActivityWriteBinding binding;   //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWriteBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeBars = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, imeBars.bottom);
            binding.contentInputLayout.setPadding(
                    ViewEdgeHelper.dpToPx(this, 10),
                    ViewEdgeHelper.dpToPx(this, 10),
                    ViewEdgeHelper.dpToPx(this, 10),
                    systemBars.bottom);
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
        //初始化RecyclerView
        initRecycler();

        //发送按钮
        binding.sendBtn.setOnClickListener(view -> {
            //获取输入内容
            String content = String.valueOf(binding.contentTextInput.getText());
            if (content.isEmpty()) {
                return;
            }

            //执行写入操作
            DiaryDatabase db = DiaryDatabase.getInstance(this);
            ParagraphDao paragraphDao = db.paragraphDao();
            DiaryDao diaryDao = db.diaryDao();
            disposable.add(
                    Observable.fromCallable(() -> {
                                Long diaryId = diaryDao.getDiaryIdByDate(LocalDate.now());
                                if (diaryId == null) {
                                    DiaryEntity newDiary = new DiaryEntity(LocalDate.now());
                                    diaryId = diaryDao.insertDiary(newDiary);
                                }

                                if (diaryId == null) {
                                    Log.e(LogTags.WRITE_ACTIVITY.n(), "无法新建日记");
                                    return null;
                                }

                                ParagraphEntity newParagraph = new ParagraphEntity(diaryId, content, LocalDateTime.now());
                                return paragraphDao.insertParagraph(newParagraph);
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(id -> {
                                if (id != null) {
                                    binding.contentTextInput.setText(null);
                                } else {
                                    Toast.makeText(this, "日记片段写入失败", Toast.LENGTH_SHORT).show();
                                }
                            })
            );
        });
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecycler() {
        //设置适配器
        ParagraphAdapter adapter = new ParagraphAdapter();
        binding.contentRecycler.setAdapter(adapter);

        //监听数据库的响应
        ParagraphDao dao = DiaryDatabase.getInstance(this).paragraphDao();
        WriteViewModelFactory factory = new WriteViewModelFactory(dao);
        WriteViewModel viewModel = new ViewModelProvider(this, factory).get(WriteViewModel.class);
        disposable.add(viewModel.getPagingDataFlow()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pagingData ->
                        adapter.submitData(getLifecycle(), pagingData)
                )
        );
    }
}