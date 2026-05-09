package com.wanderer.journal.ui.pages.main.settings.sub;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wanderer.journal.R;
import com.wanderer.journal.databinding.ActivityDataManageBinding;
import com.wanderer.journal.enums.DataType;
import com.wanderer.journal.enums.DirectoryPaths;
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.helpers.file.FileHelper;
import com.wanderer.journal.helpers.file.SAFHelper;
import com.wanderer.journal.helpers.file.ZipHelper;
import com.wanderer.journal.helpers.file.backup.BackupHelperBase;
import com.wanderer.journal.ui.others.dialogs.ProgressDialog;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingClickableTextView;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingOptionViewBase;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
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

                    importData();
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
        importDataOption.setFunctionListener(v -> {
            //TODO:完成选择文件逻辑
        });
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
        for (DataType type : DataType.values()) {
            if (true) { //TODO:完善过滤逻辑
                BackupHelperBase<?, ?> backupHelper = type.createBackupHelper(this);
                taskList.add(backupHelper.exportDataToTempFile(this));
            }
        }

        //并行执行数据导出逻辑
        disposables.add(Completable.merge(taskList)
                .andThen(zipBackupTask(uri))
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
     * 从文件导入数据
     */
    private void importData() {
        //显示进度条对话框
        ProgressDialog progressDialog = new ProgressDialog(this, "导入数据", "正在扫描备份文件……");
        progressDialog.buildDialog(
                null,
                () -> {
                    disposables.clear();
                    Toast.makeText(this, "已取消数据导入", Toast.LENGTH_SHORT).show();
                },
                false);
        progressDialog.show();
    }

    /**
     * 创建将导出的临时备份文件打包为压缩包的任务
     *
     * @param targetUri 目标压缩包的 Uri
     * @return 是否成功
     */
    public Completable zipBackupTask(Uri targetUri) {
        File tempDir = DirectoryPaths.DATA_TEMP.getDir(this);
        File imageDir = DirectoryPaths.MEDIA.getDir(this);

        return Completable.fromAction(() -> {
            if (tempDir == null) {
                throw new IOException("无法获取临时数据目录");
            }

            // 1. 获取 SAF 的输出流
            OutputStream os = getContentResolver().openOutputStream(targetUri);
            if (os == null) {
                throw new IOException("无法打开输出流，请检查权限或路径");
            }

            try {
                // 2. 调用压缩逻辑
                // 现在的逻辑是直接流式写入 SAF 指向的文件，不再需要中间的临时 Zip 文件
                ZipHelper.createBackupZip(tempDir, imageDir, os);
            } finally {
                // 3. 无论成功失败，清理临时 JSON 目录
                FileHelper.clearTempDataDir(this);
            }
        });
    }
}