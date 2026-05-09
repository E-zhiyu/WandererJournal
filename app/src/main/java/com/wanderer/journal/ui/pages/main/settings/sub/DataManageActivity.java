package com.wanderer.journal.ui.pages.main.settings.sub;

import android.app.Activity;
import android.content.Intent;
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
import com.wanderer.journal.helpers.ExceptionHelper;
import com.wanderer.journal.helpers.appearance.ViewEdgeHelper;
import com.wanderer.journal.ui.others.dialogs.ProgressDialog;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingClickableTextView;
import com.wanderer.journal.ui.pages.main.settings.setting_option_views.SettingOptionViewBase;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
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
//                        dataIOHelper.clearTempFile();
                        return;
                    }

                    ProgressDialog progressDialog = new ProgressDialog(this, "导出数据", "正在导出数据……");
                    progressDialog.buildDialog(
                            null,
                            () -> {
                                disposables.clear();
                                Toast.makeText(this, "已取消数据导出", Toast.LENGTH_SHORT).show();
                            },
                            false);
                    progressDialog.show();

                    disposables.add(
                            Observable.fromCallable(() -> {
//                                        dataIOHelper.handleExportResult(data.getData());
                                        return true;
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(b -> Toast.makeText(this, "数据导出成功", Toast.LENGTH_SHORT).show(),
                                            e -> {
                                                ExceptionHelper.showExceptionDialog(this, e);
                                                progressDialog.dismiss();
                                            },
                                            progressDialog::dismiss
                                    )
                    );
                }
        );

        importDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
//                        dataIOHelper.clearTempFile();
                        return;
                    }

                    ProgressDialog progressDialog = new ProgressDialog(this, "导入数据", "正在扫描备份文件……");
                    progressDialog.buildDialog(
                            null,
                            () -> {
                                disposables.clear();
                                Toast.makeText(this, "已取消数据导入", Toast.LENGTH_SHORT).show();
                            },
                            false);
                    progressDialog.show();

                    disposables.add(
                            Observable.fromCallable(() -> {
//                                        dataIOHelper.handleImportResul(data.getData());
                                        return true;
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(b -> {
                                            },
                                            e -> {
                                                ExceptionHelper.showExceptionDialog(this, e);
                                                progressDialog.dismiss();
                                            },
                                            progressDialog::dismiss
                                    )
                    );

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
        exportDataOption.setFunctionListener(v -> exportData());

        //导入数据
        SettingClickableTextView importDataOption = new SettingClickableTextView(
                this,
                binding.importDataOption,
                R.string.import_data,
                "从外部文件导入数据",
                R.drawable.outline_download_24,
                SettingOptionViewBase.RadiusStyle.MIDDLE
        );
        importDataOption.setFunctionListener(v -> importData());
    }

    /**
     * 导出数据
     */
    private void exportData() {

    }

    /**
     * 从文件导入数据
     */
    private void importData() {

    }
}