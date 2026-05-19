package com.wanderer.journal.ui.pages.main.settings.sub;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.data.save.db.daos.ParagraphDao;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.services.DiaryService;
import com.wanderer.journal.databinding.ActivityDataManageBinding;
import com.wanderer.journal.enums.BackupDataType;
import com.wanderer.journal.enums.RadiusStyle;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.StringHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.helpers.file.FileHelper;
import com.wanderer.journal.helpers.file.SAFHelper;
import com.wanderer.journal.helpers.file.ZipHelper;
import com.wanderer.journal.helpers.file.backup.BackupHelperBase;
import com.wanderer.journal.helpers.time.DateParseHelper;
import com.wanderer.journal.ui.others.dialogs.MultiChoiceDialogBuilder;
import com.wanderer.journal.ui.others.dialogs.ProgressDialogBuilder;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingClickableTextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DataManageActivity extends AppCompatActivity {
    private ActivityDataManageBinding binding;  //绑定的XML布局
    private final CompositeDisposable disposables = new CompositeDisposable();      //多线程任务列表
    private ActivityResultLauncher<Intent> importDataLauncher, exportDataLauncher;  //数据导入和导出的启动器
    private ActivityResultLauncher<Intent> appendFromFileLauncher;                  //从外部文件追加段落的启动器
    private ActivityResultLauncher<Intent> importDiaryLauncher;                     //导入日记启动器
    private List<Boolean> exportChoiceStatList = null;                              //导出数据时的选项选择情况
    private boolean exportIncludeMedia = false;                                     //导出时是否包含媒体文件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDataManageBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.scrollView.setPadding(
                    0,
                    0,
                    0,
                    systemBars.bottom + ViewEdgeHelper.dpToPx(this, 15)
            );
            return insets;
        });

        initActivityLaunchers();
        initViews();
    }

    /**
     * 初始化活动启动器
     */
    private void initActivityLaunchers() {
        exportDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                        FileHelper.clearTempDataDir(this);
                        return;
                    }

                    exportData(data.getData(), exportChoiceStatList, exportIncludeMedia);
                }
        );

        importDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                        FileHelper.clearTempDataDir(this);
                        return;
                    }

                    showImportChoiceDialog(data.getData());
                }
        );

        appendFromFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                        FileHelper.clearTempDataDir(this);
                        return;
                    }

                    showFileInfosDialog(data.getData());
                }
        );

        importDiaryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                        FileHelper.clearTempDataDir(this);
                        return;
                    }

                    showImportDiaryDialog(data.getData());
                }
        );
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //初始化数据管理设置
        initDataManageSettings();

        //初始化日记数据设置
        initDiaryDataSettings();
    }

    /**
     * 初始化数据管理条目
     */
    private void initDataManageSettings() {
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //导出数据
        SettingClickableTextView exportDataOption = new SettingClickableTextView(
                this,
                binding.exportDataOption,
                R.string.export_data,
                "将应用数据以文件形式保存",
                R.drawable.outline_file_export_24,
                RadiusStyle.TOP
        );
        exportDataOption.setFunctionListener(v -> showExportChoiceDialog());

        //导入数据
        SettingClickableTextView importDataOption = new SettingClickableTextView(
                this,
                binding.importDataOption,
                R.string.import_data,
                "从外部文件导入数据",
                R.drawable.outline_download_24,
                RadiusStyle.BOTTOM
        );
        importDataOption.setFunctionListener(v -> SAFHelper.openDocumentViaSAF(
                new String[]{"application/zip"},
                importDataLauncher
        ));
    }

    /**
     * 初始化日记数据设置
     */
    private void initDiaryDataSettings() {
        //从外部文本追加
        SettingClickableTextView appendFromFileOption = new SettingClickableTextView(
                this,
                binding.appendFromFileOption,
                R.string.append_paragraph,
                "从外部文件中追加段落",
                R.drawable.outline_convert_to_text_24,
                RadiusStyle.TOP
        );
        appendFromFileOption.setFunctionListener(view -> SAFHelper.openDocumentViaSAF(
                new String[]{"text/plain"},
                appendFromFileLauncher
        ));

        //从文本文件导入日记
        SettingClickableTextView importDiaryOption = new SettingClickableTextView(
                this,
                binding.importDiaryOption,
                R.string.import_diary,
                "从文本文件中导入日记",
                R.drawable.outline_note_stack_add_24,
                RadiusStyle.BOTTOM
        );
        importDiaryOption.setFunctionListener(view -> SAFHelper.openDocumentViaSAF(
                new String[]{"text/plain"},
                importDiaryLauncher
        ));
    }

    /**
     * 显示导出数据时的多选对话框
     */
    private void showExportChoiceDialog() {
        //实例化选项列表
        List<MultiChoiceDialogBuilder.ChoiceItem> itemList = Arrays.stream(BackupDataType.values())
                .map(backupDataType ->
                        new MultiChoiceDialogBuilder.ChoiceItem(true, backupDataType.getTitle(), true)
                )
                .collect(Collectors.toList());

        //显示多选对话框
        new MultiChoiceDialogBuilder(this, "导出数据", itemList)
                .setPositiveButton("确定", checkedStatList -> {
                    //判断是否没有选择任何一个选项
                    if (checkedStatList.stream().noneMatch(Boolean::booleanValue)) {
                        Toast.makeText(this, "请选择至少一个选项", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //保存选择结果引用
                    exportChoiceStatList = checkedStatList;
                    exportIncludeMedia = checkedStatList.get(0);

                    //生成默认文件名
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd(HHmmss)");
                    String fileName = String.format(
                            Locale.getDefault(),
                            "WandererJournalBackup_%s.zip",
                            LocalDateTime.now().format(formatter)
                    );

                    //打开 SAF 用于创建压缩包文件
                    SAFHelper.createDocumentViaSAF(
                            "application/zip",
                            fileName,
                            exportDataLauncher
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 导出数据到文件
     *
     * @param uri          用户通过 SAF 创建的 zip 文件的 Uri
     * @param checkedStats 备份数据选项选择情况
     * @param includeMedia 是否导出媒体文件
     */
    private void exportData(Uri uri, List<Boolean> checkedStats, boolean includeMedia) {
        //显示进度条对话框
        AlertDialog progressDialog = new ProgressDialogBuilder(this, "导出数据", "正在导出数据……")
                .setNegativeButton("取消", (dialogInterface, i) -> {
                    disposables.clear();
                    Toast.makeText(this, "已取消数据导出", Toast.LENGTH_SHORT).show();
                })
                .show();

        //收集用户没有忽略的数据类型，并将这些数据导出为临时文件
        List<Completable> taskList = new ArrayList<>();
        for (BackupDataType type : BackupDataType.values()) {
            if (checkedStats.get(type.ordinal())) {
                BackupHelperBase<?, ?> backupHelper = type.createBackupHelper(this);
                taskList.add(backupHelper.exportDataToTempFile(this));
            }
        }

        //并行执行数据导出逻辑
        disposables.add(Completable.merge(taskList)
                .andThen(ZipHelper.createBackupFile(uri, this, includeMedia))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Toast.makeText(this, "数据导出完毕", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    FileHelper.clearTempDataDir(this);
                }, e -> {
                    ExceptionHelper.showExceptionDialog(this, e);
                    progressDialog.dismiss();
                    FileHelper.clearTempDataDir(this);
                })
        );
    }

    /**
     * 扫描备份文件并显示多选对话框
     *
     * @param uri SAF 返回的 Uri 实例
     */
    private void showImportChoiceDialog(Uri uri) {
        //显示扫描文件的进度条对话框
        AlertDialog progressDialog = new ProgressDialogBuilder(this, "扫描文件", "正在扫描文件……")
                .setNegativeButton("取消", (dialogInterface, i) -> {
                    disposables.clear();
                    Toast.makeText(this, "已取消数据导入", Toast.LENGTH_SHORT).show();
                })
                .show();

        //扫描压缩包并显示多选对话框
        disposables.add(ZipHelper.scanBackupFile(uri, this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(fileNameList -> {
                    progressDialog.dismiss();

                    //实例化选项列表
                    List<MultiChoiceDialogBuilder.ChoiceItem> itemList = Arrays.stream(BackupDataType.values())
                            .map(backupDataType -> {
                                if (fileNameList.contains(backupDataType.getFileName())) {
                                    return new MultiChoiceDialogBuilder.ChoiceItem(
                                            true,
                                            backupDataType.getTitle(),
                                            true
                                    );
                                } else {
                                    return new MultiChoiceDialogBuilder.ChoiceItem(
                                            false,
                                            backupDataType.getTitle(),
                                            false
                                    );
                                }
                            })
                            .collect(Collectors.toList());

                    //显示多选对话框
                    new MultiChoiceDialogBuilder(this, "导入数据", itemList)
                            .setPositiveButton("确认", checkedStatList -> importData(uri, checkedStatList))
                            .setNegativeButton("取消", null)
                            .show();
                }, e -> {
                    progressDialog.dismiss();
                    ExceptionHelper.showExceptionDialog(this, e);
                })
        );
    }

    /**
     * 将用户选择的数据导入到数据库中
     *
     * @param uri             备份文件的 Uri
     * @param checkedStatList 用户选择的选项状态，选项的下标与{@link BackupDataType}的枚举序数一一对应
     */
    private void importData(Uri uri, @NonNull List<Boolean> checkedStatList) {
        //判断是否选择了数据
        if (checkedStatList.stream().noneMatch(Boolean::booleanValue)) {
            Toast.makeText(this, "请选择至少一个选项", Toast.LENGTH_SHORT).show();
            return;
        }

        //显示扫描文件的进度条对话框
        AlertDialog progressDialog = new ProgressDialogBuilder(this, "导入数据", "正在导入数据……")
                .setNegativeButton("取消", (dialogInterface, i) -> {
                    disposables.clear();
                    Toast.makeText(this, "已取消数据导入", Toast.LENGTH_SHORT).show();
                })
                .show();

        //获取需要解压的文件名列表
        List<String> allowedFileNameList = Arrays.stream(BackupDataType.values())
                .filter(backupDataType -> checkedStatList.get(backupDataType.ordinal()))
                .map(BackupDataType::getFileName)
                .collect(Collectors.toList());
        boolean includeMedia = checkedStatList.get(0);

        //解压文件并导入数据
        disposables.add(ZipHelper.unpackBackupFileWithFilter(this, uri, allowedFileNameList, includeMedia)
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(file -> {
                    //根据文件名判断数据类型
                    BackupDataType type = BackupDataType.fromFileName(file.getName());

                    //使用对应的备份Helper导入数据
                    if (type != null) {
                        BackupHelperBase<?, ?> helper = type.createBackupHelper(this);
                        return helper.importDataFromTempFile(file);
                    } else {
                        return Completable.complete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            Toast.makeText(this, "数据导入成功", Toast.LENGTH_SHORT).show();
                            FileHelper.clearTempDataDir(this);
                            progressDialog.dismiss();
                        },
                        e -> {
                            ExceptionHelper.showExceptionDialog(this, e);
                            progressDialog.dismiss();
                        }
                )
        );
    }

    /**
     * 从外部文件追加段落
     *
     * @param uri 通过 SAF 选择的文件的 Uri
     */
    private void showFileInfosDialog(Uri uri) {
        disposables.add(FileHelper.readContentWithLastModifyTime(uri, this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        data -> {
                            //获取基本数据
                            String content = data.getContent();
                            LocalDateTime lastModifyTime = data.getLastModifyTime();
                            int lineCount = StringHelper.countLinesSplit(content);

                            //判断删除空白字符后是否为空字符串
                            if (content.trim().isEmpty()) {
                                Toast.makeText(this, "所选文件为空", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            //生成提示消息
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                            String message = String.format(
                                    Locale.getDefault(),
                                    "所选文件信息如下：\n行数：%d\n最后编辑时间：%s\n确认追加该文件中的所有内容吗？",
                                    lineCount, lastModifyTime.format(formatter)
                            );

                            new MaterialAlertDialogBuilder(this)
                                    .setTitle(R.string.append_paragraph)
                                    .setMessage(message)
                                    .setPositiveButton("确认", (dialogInterface, i) ->
                                            appendParagraphsFromFile(content, lastModifyTime)
                                    )
                                    .setNegativeButton("取消", null)
                                    .show();
                        }
                )
        );
    }

    /**
     * 从文件中追加段落
     *
     * @param content 文件中的文本内容
     * @param time    文件最后编辑时间
     */
    private void appendParagraphsFromFile(@NonNull String content, @NonNull LocalDateTime time) {
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphDao paragraphDao = db.paragraphDao();

        LocalDate date = time.toLocalDate();
        disposables.add(DiaryService.getOrCreateDiaryIdByDate(date, this)
                .flatMapCompletable(diaryId -> {
                    //生成段落实体列表
                    List<ParagraphEntity> paragraphEntityList = new ArrayList<>();
                    String[] lines = content.split("\n");
                    for (String line : lines) {
                        if (line.trim().isEmpty()) continue;

                        paragraphEntityList.add(new ParagraphEntity(diaryId, line.trim(), time));
                    }
                    return paragraphDao.insertParagraphCompletable(paragraphEntityList);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Toast.makeText(this, "段落追加完毕", Toast.LENGTH_SHORT).show(),
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 显示导入日记的确认对话框
     *
     * @param uri 待导入的文本文件 Uri
     */
    private void showImportDiaryDialog(Uri uri) {
        disposables.add(Observable.fromCallable(() -> FileHelper.getLines(uri, this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        count -> {
                            //构建提示文本
                            String message = String.format(
                                    Locale.getDefault(),
                                    "警告：在导入日记前请备份数据！\n" +
                                            "该文件共%d行，确认导入其中的日记数据吗？\n" +
                                            "（仅支持将日期作为单独行并放在日记内容前的格式）",
                                    count
                            );

                            //显示对话框
                            new MaterialAlertDialogBuilder(this)
                                    .setTitle(R.string.import_diary)
                                    .setMessage(message)
                                    .setPositiveButton("确定", (dialogInterface, i) ->
                                            importDiaryFromFile(uri, count)
                                    )
                                    .setNegativeButton("取消", null)
                                    .show();
                        }
                )
        );
    }

    /**
     * 通过多线程从外部文本文件中导入日记
     *
     * @param uri       待读取的文件的 Uri
     * @param lineCount 文本文件总行数
     */
    private void importDiaryFromFile(Uri uri, int lineCount) {
        //显示对话框
        ProgressDialogBuilder progressDialogBuilder = new ProgressDialogBuilder(this, "导入日记", "正在导入日记……");
        AlertDialog progressDialog = progressDialogBuilder
                .setNegativeButton("取消", (dialogInterface, i) -> {
                    Toast.makeText(this, "已取消日记导入", Toast.LENGTH_SHORT).show();
                    disposables.clear();
                })
                .show();

        //获取数据库实例
        DiaryDatabase db = DiaryDatabase.getInstance(this);
        ParagraphDao paragraphDao = db.paragraphDao();

        //创建导入任务
        Observable<Integer> task = Observable.create(emitter -> {
            LocalDate currentDate = null;
            List<ParagraphEntity> currentParagraphs = new ArrayList<>();
            int processedLines = 0;

            try (InputStream is = getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    // 1. 业务逻辑：解析日期与段落
                    LocalDate parsedDate = DateParseHelper.parseFlexible(line);
                    if (parsedDate != null) {
                        if (currentDate != null && !currentParagraphs.isEmpty()) {
                            paragraphDao.insertDiaryWithParagraphs(currentDate, currentParagraphs, this);
                            currentParagraphs.clear();
                        }
                        currentDate = parsedDate;
                    } else if (currentDate != null) {
                        String content = line.trim();
                        if (!content.isEmpty()) {
                            currentParagraphs.add(new ParagraphEntity(0, content, currentDate.atTime(0, 0)));
                        }
                    }

                    // 2. 进度计算与发射
                    processedLines++;
                    emitter.onNext(processedLines);

                    // 3. 检查背压/取消（可选）
                    if (emitter.isDisposed()) return;
                }
            } catch (Exception e) {
                emitter.onError(e);
            } finally {
                // 收尾工作
                if (currentDate != null && !currentParagraphs.isEmpty()) {
                    paragraphDao.insertDiaryWithParagraphs(currentDate, currentParagraphs, this);
                }

                emitter.onNext(processedLines);
                emitter.onComplete();
            }
        });

        //执行任务
        disposables.add(task
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        processedLines -> {
                            //切换为确定进度模式
                            progressDialogBuilder.setIndeterminate(false);

                            //更新进度条
                            progressDialogBuilder.updateProgress(processedLines, lineCount, "正在解析文本内容……");
                        },
                        e -> {
                            ExceptionHelper.showExceptionDialog(this, e);
                            progressDialog.dismiss();
                        },
                        () -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "日记导入完毕", Toast.LENGTH_SHORT).show();
                        }
                )
        );
    }
}