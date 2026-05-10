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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.R;
import com.wanderer.journal.databinding.ActivityDataManageBinding;
import com.wanderer.journal.enums.BackupDataType;
import com.wanderer.journal.enums.DirectoryPaths;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.helpers.file.FileHelper;
import com.wanderer.journal.helpers.file.SAFHelper;
import com.wanderer.journal.helpers.file.ZipHelper;
import com.wanderer.journal.helpers.file.backup.BackupHelperBase;
import com.wanderer.journal.ui.others.dialogs.MultiChoiceDialog;
import com.wanderer.journal.ui.others.dialogs.ProgressDialog;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingClickableTextView;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingOptionViewBase;

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
    private ActivityResultLauncher<Intent> importDataLauncher, exportDataLauncher;  //活动启动器

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
        initDataManageSettings();
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

                    exportData(data.getData());
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
                SettingOptionViewBase.RadiusStyle.TOP
        );
        exportDataOption.setFunctionListener(v -> {
            //生成默认文件名
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd(HHmmss)");
            String fileName = String.format(
                    Locale.getDefault(),
                    "WandererJournalBackup_%s.zip",
                    LocalDateTime.now().format(formatter)
            );
            SAFHelper.createDocumentViaSAF(
                    "application/zip",
                    fileName,
                    exportDataLauncher
            );
        });

        //导入数据
        SettingClickableTextView importDataOption = new SettingClickableTextView(
                this,
                binding.importDataOption,
                R.string.import_data,
                "从外部文件导入数据",
                R.drawable.outline_download_24,
                SettingOptionViewBase.RadiusStyle.BOTTOM
        );
        importDataOption.setFunctionListener(v -> SAFHelper.openDocumentViaSAF(
                new String[]{"application/zip"},
                importDataLauncher
        ));
    }

    /**
     * 导出数据到文件
     *
     * @param uri 用户通过 SAF 创建的 zip 文件的 Uri
     */
    private void exportData(Uri uri) {
        //显示进度条对话框
        ProgressDialog progressDialog = new ProgressDialog(this, "导出数据", "正在导出数据……");
        progressDialog.buildDialog(
                null,
                () -> {
                    disposables.clear();
                    Toast.makeText(this, "已取消数据导出", Toast.LENGTH_SHORT).show();
                },
                false);
        progressDialog.show();

        //收集用户没有忽略的数据类型
        List<Completable> taskList = new ArrayList<>();
        for (BackupDataType type : BackupDataType.values()) {
            if (true) { //TODO:完善过滤逻辑
                BackupHelperBase<?, ?> backupHelper = type.createBackupHelper(this);
                taskList.add(backupHelper.exportDataToTempFile(this));
            }
        }

        //并行执行数据导出逻辑
        disposables.add(Completable.merge(taskList)
                .andThen(ZipHelper.zipBackupTask(uri, this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    Toast.makeText(this, "数据导出完毕", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }, e -> {
                    ExceptionHelper.showExceptionDialog(this, e);
                    progressDialog.dismiss();
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
        ProgressDialog progressDialog = new ProgressDialog(this, "扫描文件", "正在扫描备份文件……");
        progressDialog.buildDialog(
                null,
                () -> {
                    disposables.clear();
                    Toast.makeText(this, "已取消数据导入", Toast.LENGTH_SHORT).show();
                },
                false
        );
        progressDialog.show();

        //扫描压缩包并显示多选对话框
        disposables.add(ZipHelper.scanZipFile(uri, this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(fileNameList -> {
                    progressDialog.dismiss();

                    //实例化选项列表
                    List<MultiChoiceDialog.ChoiceItem> itemList = Arrays.stream(BackupDataType.values())
                            .map(backupDataType -> {
                                if (fileNameList.contains(backupDataType.getFileName())) {
                                    return new MultiChoiceDialog.ChoiceItem(
                                            true,
                                            backupDataType.getTitle(),
                                            true
                                    );
                                } else {
                                    return new MultiChoiceDialog.ChoiceItem(
                                            false,
                                            backupDataType.getTitle(),
                                            false
                                    );
                                }
                            })
                            .collect(Collectors.toList());

                    //显示多选对话框
                    MultiChoiceDialog dialog = new MultiChoiceDialog(
                            this,
                            "导入数据",
                            itemList,
                            checkedStatList -> importData(uri, checkedStatList)
                    );
                    dialog.buildDialog(() -> {

                            },
                            true
                    );
                    dialog.show();
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
        boolean isAllFalse = true;
        for (boolean b : checkedStatList) {
            if (b) {
                isAllFalse = false;
                break;
            }
        }
        if (isAllFalse) {
            Toast.makeText(this, "请选择至少一个选项", Toast.LENGTH_SHORT).show();
            return;
        }

        //显示扫描文件的进度条对话框
        ProgressDialog progressDialog = new ProgressDialog(this, "导入数据", "正在导入备份数据……");
        progressDialog.buildDialog(
                null,
                () -> {
                    disposables.clear();
                    Toast.makeText(this, "已取消数据导入", Toast.LENGTH_SHORT).show();
                },
                false
        );
        progressDialog.show();

        //获取需要解压的文件名列表
        List<String> allowedFileNameList = Arrays.stream(BackupDataType.values())
                .filter(backupDataType -> checkedStatList.get(backupDataType.ordinal()))
                .map(BackupDataType::getFileName)
                .collect(Collectors.toList());

        //解压文件并导入数据
        disposables.add(ZipHelper.unpackWithFilter(this, uri, DirectoryPaths.DATA_TEMP.getDir(this), allowedFileNameList)
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
}