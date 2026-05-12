package com.wanderer.journal.ui.pages.write;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.services.DiaryService;
import com.wanderer.journal.databinding.ActivityWriteBinding;
import com.wanderer.journal.enums.KeyStrings;
import com.wanderer.journal.enums.LogTags;
import com.wanderer.journal.helpers.time.DateTimePickerHelper;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphAdapter;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphViewModel;
import com.wanderer.journal.ui.others.adapters.paragraph.ParagraphViewModelFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;

public class WriteActivity extends AppCompatActivity {
    private ActivityWriteBinding binding;                       //绑定的XML布局
    private ParagraphEntity modifyingParagraph = null;        //正在编辑的段落编号
    private OnBackPressedCallback backPressedCallback;          //返回手势监听
    private LocalDate diaryDate = LocalDate.now();  //父日记的日期
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

        receiveIntent();
        initViews();

        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                resetContentModifyMode();
            }
        };
        getOnBackPressedDispatcher().addCallback(backPressedCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
        disposable.dispose();
    }

    /**
     * 获取由父界面传递的参数
     */
    private void receiveIntent() {
        Intent intent = getIntent();
        Bundle dataBundle = intent.getExtras();
        if (dataBundle == null) {
            return;
        }

        //初始化日期数据
        try {
            String date = dataBundle.getString(KeyStrings.WRITE_DIARY_DATE.getS());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            diaryDate = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            Log.e(LogTags.WRITE_ACTIVITY.n(), "无法读取传递的日期数据，已默认设置为当前日期");
            diaryDate = LocalDate.now();
        }
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

            if (modifyingParagraph == null) {
                addParagraph(content);
            } else {
                updateParagraphContent(content, modifyingParagraph);
                resetContentModifyMode();
            }
        });

        //内容编辑关闭按钮
        binding.modifyCloseBtn.setOnClickListener(view -> resetContentModifyMode());
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecycler() {
        //设置适配器
        ParagraphAdapter adapter = new ParagraphAdapter(
                (paragraph, view) -> {
                    PopupMenu menu = new PopupMenu(this, view);
                    menu.getMenuInflater().inflate(R.menu.menu_paragraph_edit, menu.getMenu());

                    menu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_modify_content) {
                            //保存旧段落引用并启用自定义返回手势
                            modifyingParagraph = paragraph;
                            backPressedCallback.setEnabled(true);

                            //更新UI到编辑模式
                            binding.contentEditCard.setVisibility(View.VISIBLE);
                            binding.contentTextInput.setText(paragraph.getContent());
                            binding.originText.setText(paragraph.getContent());

                            return true;
                        } else if (item.getItemId() == R.id.action_modify_time) {
                            updateParagraphCreateTime(paragraph);
                            return true;
                        } else if (item.getItemId() == R.id.action_delete_paragraph) {
                            deleteParagraph(paragraph);
                            return true;
                        }

                        return false;
                    });

                    menu.show();
                }
        );
        adapter.addLoadStateListener(loadStates -> {
            boolean isNotLoading = loadStates.getRefresh() instanceof LoadState.NotLoading;
            boolean endOfPaginationReached = loadStates.getAppend().getEndOfPaginationReached();

            if (isNotLoading && endOfPaginationReached) {
                if (adapter.getItemCount() == 0) {
                    binding.emptyText.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyText.setVisibility(View.GONE);
                }
            } else {
                binding.emptyText.setVisibility(View.GONE);
            }
            return Unit.INSTANCE;
        });
        binding.contentRecycler.setAdapter(adapter);

        //监听数据库的响应
        ParagraphDao dao = DiaryDatabase.getInstance(this).paragraphDao();
        ParagraphViewModelFactory factory = new ParagraphViewModelFactory(dao, diaryDate, diaryDate.plusDays(1));
        ParagraphViewModel viewModel = new ViewModelProvider(this, factory).get(ParagraphViewModel.class);
        disposable.add(viewModel.getPagingDataFlow()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pagingData ->
                                adapter.submitData(getLifecycle(), pagingData),
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 添加新段落
     *
     * @param content 新段落的内容
     */
    private void addParagraph(String content) {
        //执行写入操作
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphDao paragraphDao = db.paragraphDao();

        disposable.add(DiaryService.getOrCreateDiaryIdByDate(diaryDate, this)
                .flatMap(diaryId -> {
                    ParagraphEntity newParagraph = new ParagraphEntity(diaryId, content, LocalDateTime.now());
                    return paragraphDao.insertParagraph(newParagraph);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        diaryId -> binding.contentTextInput.setText(null),
                        e -> ExceptionHelper.showExceptionDialog(this, e)
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
                        () -> {
                            Toast.makeText(this, "段落内容修改成功", Toast.LENGTH_SHORT).show();
                            Log.i(LogTags.WRITE_ACTIVITY.n(), "段落内容修改成功");
                        },
                        throwable -> {
                            ExceptionHelper.showExceptionDialog(this, throwable);
                            Log.e(LogTags.WRITE_ACTIVITY.n(), "段落内容修改失败");
                        }
                )
        );
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
                                        Log.i(LogTags.WRITE_ACTIVITY.n(), "段落创建时间修改成功");
                                        Toast.makeText(this, "段落创建时间修改成功", Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        ExceptionHelper.showExceptionDialog(this, throwable);
                                        Log.e(LogTags.WRITE_ACTIVITY.n(), "段落创建时间修改失败");
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
                                Log.i(LogTags.WRITE_ACTIVITY.n(), "段落删除成功");
                                Toast.makeText(this, "段落删除成功", Toast.LENGTH_SHORT).show();
                            }, throwable -> {
                                Log.e(LogTags.WRITE_ACTIVITY.n(), "段落删除失败");
                                ExceptionHelper.showExceptionDialog(this, throwable);
                            })
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 重置内容编辑模式
     */
    private void resetContentModifyMode() {
        binding.contentEditCard.setVisibility(View.GONE);
        modifyingParagraph = null;
        backPressedCallback.setEnabled(false);
        binding.contentTextInput.setText(null);
    }
}