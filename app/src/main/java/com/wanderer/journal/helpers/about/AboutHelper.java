package com.wanderer.journal.helpers.about;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wanderer.journal.R;
import com.wanderer.journal.databinding.ViewMarkdownTextBinding;
import com.wanderer.journal.helpers.ExceptionHelper;

import io.noties.markwon.Markwon;

public class AboutHelper {
    private final static String CHANGELOG = "# v1.1.0\n" +
            "\n" +
            "### 新增功能\n" +
            "\n" +
            "- 新增日记提醒功能，会在忘记写日记时发送通知提醒用户\n" +
            "- 添加权限管理界面\n" +
            "\n" +
            "### 优化内容\n" +
            "\n" +
            "- 写日记界面恢复草稿后光标移动到末尾\n" +
            "\n" +
            "# v1.0.2\n" +
            "\n" +
            "### BUG修复\n" +
            "\n" +
            "- 修复读日记界面跳转按钮的阴影被裁切的BUG\n" +
            "- 修复搜索关键词不会填充到SearchBar的BUG\n" +
            "\n" +
            "### 优化内容\n" +
            "\n" +
            "- 优化日记列表的文字显示\n" +
            "- 读日记界面现在会显示正在过滤的情绪标签\n" +
            "\n" +
            "# v1.0.1\n" +
            "\n" +
            "### BUG修复\n" +
            "\n" +
            "- 修复读日记界面搜索段落时没有搜索词也没有筛选情绪标签时搜索行为会异常响应的BUG\n" +
            "\n" +
            "# v1.0.0\n" +
            "\n" +
            "> 该版本为首个Release版本，以下为核心功能概述\n" +
            "\n" +
            "### 日记管理\n" +
            "\n" +
            "- 在写日记界面以聊天软件的方式记日记\n" +
            "- 在日记界面，保存的日记项会按照日期由近到远排序\n" +
            "- 所有保存的日记段落都能在读日记界面中查看，并且以时间由远到近的顺序排序\n" +
            "- 在读日记和写日记界面可以对日记段落进行编辑\n" +
            "\n" +
            "### 情绪标记\n" +
            "\n" +
            "- 支持自定义情绪标签\n" +
            "- 在读日记和写日记界面中可以为日记段落绑定多个情绪标签，并为每个情绪标签单独设置强烈等级\n" +
            "- 绑定的情绪标签会出现在对应日记段落文本内容的下方\n" +
            "\n" +
            "### 日记搜索\n" +
            "\n" +
            "- 支持搜索日记段落内容以及情绪标签\n" +
            "- 搜索成功后能够在多个搜索结果之间进行跳转\n" +
            "- 搜索成功后会对搜索的关键词或者情绪标签进行高亮处理，未匹配搜索结果的日记段落则正常显示\n" +
            "\n" +
            "### 媒体保存\n" +
            "\n" +
            "- 支持为日记段落添加多个媒体文件\n" +
            "- 添加的媒体文件会显示在日记段落文本内容的上方\n" +
            "- 点击显示的媒体文件缩略图能够查看媒体文件大图\n" +
            "\n" +
            "### 数据管理\n" +
            "\n" +
            "- 所有数据均保存在本地\n" +
            "- 支持将数据导出为备份文件，且支持从备份文件中恢复数据\n" +
            "- 支持从外部文本文件中追加日记段落，追加的段落的时间与文本文件最后编辑时间一致\n" +
            "- 支持从文本文件导入整个日记，前提是该文本文件用单独的日期行隔开了不同日期的日记内容\n" +
            "\n" +
            "### 隐私保护\n" +
            "\n" +
            "- 内置身份验证模块，启用和关闭都需要验证身份\n" +
            "- 启用身份验证功能后，每次进入应用都将触发身份验证逻辑（进入软件时与上次身份验证间隔一定时间才会要求验证身份），支持调整两次身份验证的最小时间间隔\n" +
            "- 身份验证的方式取决于设备的屏幕解锁方式";

    /**
     * 获取版本名称
     *
     * @param context 上下文
     * @return 版本名称字符串
     * @throws PackageManager.NameNotFoundException 包名未找到引发的异常
     */
    public static String getVersionName(@NonNull Context context) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
        return packageInfo.versionName;
    }

    /**
     * 获取应用名称
     *
     * @param context 上下文
     * @return 应用名称
     */
    @NonNull
    public static String getAppName(@NonNull Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA
            );
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return "ManagerAssistant";
        }
    }

    /**
     * 显示更新日志对话框
     *
     * @param context 上下文
     */
    public static void showChangelog(Context context) {
        //获取自定义弹窗视图
        ViewMarkdownTextBinding markdownTextBinding = ViewMarkdownTextBinding.inflate(
                LayoutInflater.from(context)
        );

        //渲染 Markdown 文本
        Markwon markwon = Markwon.create(context);
        markwon.setMarkdown(markdownTextBinding.mdTextviewInDialog, CHANGELOG);

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.changelog)
                .setView(markdownTextBinding.getRoot())
                .setPositiveButton("关闭", null)
                .show();
    }
}
