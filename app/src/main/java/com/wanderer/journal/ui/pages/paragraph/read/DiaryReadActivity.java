package com.wanderer.journal.ui.pages.paragraph.read;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.ui.others.bottom.ParagraphContentModifySheet;
import com.wanderer.journal.ui.pages.paragraph.ParagraphAdapter;

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
                                                        () -> Toast.makeText(this, "段落内容更新成功", Toast.LENGTH_SHORT).show(),
                                                        throwable -> {
                                                            ExceptionHelper.showExceptionDialog(this, throwable);
                                                            Log.e(LogTags.DIARY_READ_ACTIVITY.n(), "段落修改失败");
                                                        }
                                                )
                                        );
                                    },
                                    paragraph.getContent()
                            );
                            sheet.show(getSupportFragmentManager(), TagStrings.PARAGRAPH_CONTENT_MODIFY_SHEET.getTag());

                            return true;
                        } else if (item.getItemId() == R.id.action_modify_time) {
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
}