package com.wanderer.journal.ui.pages.paragraph.read;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.databinding.ActivityDiaryReadBinding;
import com.wanderer.journal.enums.LogTags;
import com.wanderer.journal.enums.TagStrings;
import com.wanderer.journal.helpers.DateTimePickerHelper;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.bottom.ParagraphContentModifySheet;
import com.wanderer.journal.ui.pages.paragraph.ParagraphAdapter;

import java.time.LocalDateTime;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DiaryReadActivity extends AppCompatActivity {
    private ActivityDiaryReadBinding binding;               //绑定的XML布局
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiaryReadBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarContainerLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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
        //搜索组件
        initSearchComponents();

        //日记段落列表
        initRecyclerView();
    }

    /**
     * 初始化搜索组件
     */
    private void initSearchComponents() {
        //绑定SearchView和SearchBar
        binding.diaryContentSearchView.setupWithSearchBar(binding.contentSearchBar);

        //TODO:完成剩下的
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        //设置适配器
        ParagraphAdapter adapter = new ParagraphAdapter(
                (paragraph, view) -> {
                    PopupMenu menu = new PopupMenu(this, view);
                    menu.getMenuInflater().inflate(R.menu.menu_paragraph_edit, menu.getMenu());

                    menu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_modify_content) {
                            ParagraphContentModifySheet sheet = new ParagraphContentModifySheet(
                                    newContent -> updateParagraphContent(newContent, paragraph),
                                    paragraph.getContent()
                            );
                            sheet.show(getSupportFragmentManager(), TagStrings.PARAGRAPH_CONTENT_MODIFY_SHEET.getTag());

                            return true;
                        } else if (item.getItemId() == R.id.action_modify_time) {
                            DateTimePickerHelper.selectDateTime(
                                    paragraph.getCreateTime(),
                                    getSupportFragmentManager(),
                                    timePicker -> {
                                        int hour = timePicker.getHour();
                                        int minute = timePicker.getMinute();
                                        LocalDateTime newDateTime = paragraph.getCreateTime()
                                                .withHour(hour)
                                                .withMinute(minute);

                                        updateParagraphCreateTime(newDateTime, paragraph);
                                    }
                            );

                            return true;
                        }

                        return false;
                    });

                    menu.show();
                }
        );
        binding.contentRecycler.setAdapter(adapter);

        //监听数据库的响应
        ParagraphDao dao = DiaryDatabase.getInstance(this).paragraphDao();
        ReadViewModelFactory factory = new ReadViewModelFactory(dao);
        ReadViewModel viewModel = new ViewModelProvider(this, factory).get(ReadViewModel.class);
        disposable.add(viewModel.getPagingDataFlow()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pagingData ->
                        adapter.submitData(getLifecycle(), pagingData)
                )
        );
    }

    /**
     * 更新段落内容
     *
     * @param newContent      新内容
     * @param originParagraph 原本的段落实体
     */
    private void updateParagraphContent(String newContent, @NonNull ParagraphEntity originParagraph) {
        ParagraphEntity newParagraph = new ParagraphEntity(
                originParagraph.getParentDiaryId(),
                newContent,
                originParagraph.getCreateTime()
        );
        newParagraph.setParagraphId(originParagraph.getParagraphId());

        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphDao paragraphDao = db.paragraphDao();
        disposable.add(paragraphDao.updateParagraphContent(newParagraph)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> Toast.makeText(this, "段落内容修改成功", Toast.LENGTH_SHORT).show(),
                        throwable -> {
                            ExceptionHelper.showExceptionDialog(this, throwable);
                            Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落内容修改失败");
                        }
                )
        );
    }

    /**
     * 更新段落创建日期
     *
     * @param newDateTime     新的日期时间
     * @param originParagraph 原来的段落实例
     */
    private void updateParagraphCreateTime(LocalDateTime newDateTime, @NonNull ParagraphEntity originParagraph) {
        ParagraphEntity newParagraph = new ParagraphEntity(
                originParagraph.getParentDiaryId(),
                originParagraph.getContent(),
                newDateTime
        );
        newParagraph.setParagraphId(originParagraph.getParagraphId());

        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphDao paragraphDao = db.paragraphDao();
        disposable.add(paragraphDao.updateParagraphContent(newParagraph)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> Toast.makeText(this, "段落创建时间修改成功", Toast.LENGTH_SHORT).show(),
                        throwable -> {
                            ExceptionHelper.showExceptionDialog(this, throwable);
                            Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落创建时间修改失败");
                        }
                )
        );
    }
}