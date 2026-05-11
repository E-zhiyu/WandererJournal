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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.databinding.ActivityDiaryReadBinding;
import com.wanderer.journal.enums.LogTags;
import com.wanderer.journal.enums.TagStrings;
import com.wanderer.journal.helpers.time.DateTimePickerHelper;
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
                            updateParagraphContent(paragraph);
                            return true;
                        } else if (item.getItemId() == R.id.action_modify_time) {
                            updateParagraphCreateTime(paragraph);

                            return true;
                        } else if (item.getItemId() == R.id.action_delete_paragraph) {
                            deleteParagraph(paragraph);
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
     * @param paragraph 原本的段落实体
     */
    private void updateParagraphContent(@NonNull ParagraphEntity paragraph) {
        ParagraphContentModifySheet sheet = new ParagraphContentModifySheet(
                newContent -> {
                    ParagraphEntity newParagraph = new ParagraphEntity(
                            paragraph.getParentDiaryId(),
                            newContent,
                            paragraph.getCreateTime()
                    );
                    newParagraph.setParagraphId(paragraph.getParagraphId());

                    DiaryDatabase db = DiaryDatabase.getInstance(this);
                    ParagraphDao paragraphDao = db.paragraphDao();
                    disposable.add(paragraphDao.updateParagraphContent(newParagraph)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> {
                                        Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "段落内容修改成功");
                                        Toast.makeText(this, "段落内容修改成功", Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        ExceptionHelper.showExceptionDialog(this, throwable);
                                        Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落内容修改失败");
                                    }
                            )
                    );
                },
                paragraph.getContent()
        );
        sheet.show(getSupportFragmentManager(), TagStrings.PARAGRAPH_CONTENT_MODIFY_SHEET.getTag());
    }

    /**
     * 更新段落创建日期
     *
     * @param paragraph 原来的段落实例
     */
    private void updateParagraphCreateTime(@NonNull ParagraphEntity paragraph) {
        DateTimePickerHelper.selectDateTime(
                paragraph.getCreateTime(),
                getSupportFragmentManager(),
                timePicker -> {
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    LocalDateTime newDateTime = paragraph.getCreateTime()
                            .withHour(hour)
                            .withMinute(minute);

                    ParagraphEntity newParagraph = new ParagraphEntity(
                            paragraph.getParentDiaryId(),
                            paragraph.getContent(),
                            newDateTime
                    );
                    newParagraph.setParagraphId(paragraph.getParagraphId());

                    DiaryDatabase db = DiaryDatabase.getInstance(this);
                    ParagraphDao paragraphDao = db.paragraphDao();
                    disposable.add(paragraphDao.updateParagraphContent(newParagraph)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> {
                                        Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "段落创建时间修改成功");
                                        Toast.makeText(this, "段落创建时间修改成功", Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        ExceptionHelper.showExceptionDialog(this, throwable);
                                        Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落创建时间修改失败");
                                    }
                            )
                    );
                }
        );
    }

    /**
     * 删除段落
     *
     * @param paragraph 待删除的段落实例
     */
    private void deleteParagraph(ParagraphEntity paragraph) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_paragraph)
                .setMessage("此操作将删除该段落，确定继续吗？")
                .setPositiveButton("确定", (dialogInterface, i) -> {
                    DiaryDatabase db = DiaryDatabase.getInstance(this);
                    ParagraphDao dao = db.paragraphDao();

                    disposable.add(dao.deleteParagraph(paragraph)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {
                                Log.i(LogTags.DIARY_READ_ACTIVITY.n(), "段落删除成功");
                                Toast.makeText(this, "段落删除成功", Toast.LENGTH_SHORT).show();
                            }, throwable -> {
                                Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落删除失败");
                                ExceptionHelper.showExceptionDialog(this, throwable);
                            })
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }
}