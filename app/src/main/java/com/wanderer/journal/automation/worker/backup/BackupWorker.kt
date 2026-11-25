package com.wanderer.journal.automation.worker.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.enums.ChannelInfo
import com.wanderer.journal.auxiliary.enums.intent.NotificationID
import com.wanderer.journal.auxiliary.enums.types.BackupDataType
import com.wanderer.journal.auxiliary.enums.unique.KeyStrings
import com.wanderer.journal.auxiliary.enums.unique.LogTags
import com.wanderer.journal.data.backup.BackupJson
import com.wanderer.journal.data.backup.helpers.BackupHelperBase
import com.wanderer.journal.data.backup.maps.DataInfoMap
import com.wanderer.journal.data.save.preference.AutoBackupPreference
import com.wanderer.journal.helpers.NotificationHelper
import com.wanderer.journal.helpers.file.FileHelper
import com.wanderer.journal.helpers.file.ZipHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

/**
 * 数据导出 Worker。
 *
 * 由原来的 {@code RxWorker} 改为 [CoroutineWorker]，备份任务通过 Kotlin 协程并发执行。
 */
class BackupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 获取传入的参数
        val context = applicationContext
        val inputData = inputData
        val inputUriStr = inputData.getString(KeyStrings.BACKUP_TARGET.v())
        val choices = inputData.getBooleanArray(KeyStrings.BACKUP_CHOICES.v())

        // 收集需要执行的备份任务
        val backupHelpers = if (choices != null) {
            BackupDataType.entries
                .filter { choices.getOrNull(it.ordinal) == true }
                .map { it.createBackupHelper(context) }
        } else {
            // 处理自动备份不传递选择状态的情况
            BackupDataType.entries.map { it.createBackupHelper(context) }
        }

        // 确定备份位置
        val finalBackupFileUri: Uri
        if (inputUriStr != null) {
            finalBackupFileUri = inputUriStr.toUri()
        } else {
            val savedBackupTarget = AutoBackupPreference.getBackupDirectoryUri(context)
            if (savedBackupTarget.isEmpty()) {
                Log.e(LogTags.BACKUP_WORKER.n(), "无法获取备份目标位置")
                return Result.failure()
            }

            // 创建备份文件
            val fileName = FileHelper.generateBackupFileName()
            val backupDir = DocumentFile.fromTreeUri(context, Uri.parse(savedBackupTarget))
            if (backupDir == null) {
                Log.e(LogTags.BACKUP_WORKER.n(), "无法将目标位置 Uri 转换为 DocumentFile")
                return Result.failure()
            }
            val backupFile = backupDir.createFile("application/zip", fileName)
            if (backupFile == null) return Result.failure()
            Log.d(LogTags.BACKUP_WORKER.n(), "备份文件成功创建")

            finalBackupFileUri = backupFile.uri
        }

        // 执行备份逻辑
        sendProgressNotification(context) // 发送通知
        val includeMedia =
            choices == null || choices.getOrNull(BackupDataType.DIARY.ordinal) == true

        return try {
            // 写入备份数据信息
            writeDataInfoToFile(context, choices)

            // 并发导出各类数据
            coroutineScope {
                backupHelpers.map { helper ->
                    async { helper.exportDataToTempFile(context) }
                }.awaitAll()
            }

            // 打包备份文件
            ZipHelper.createBackupFile(finalBackupFileUri, context, includeMedia)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LogTags.BACKUP_WORKER.n(), "备份失败\n${e.message}")
            Result.failure()
        } finally {
            // 发送通知
            sendCompletionNotification(context)
        }
    }

    /**
     * 将备份数据信息写入到 JSON 文件中。
     *
     * @param context 上下文
     * @param choice 备份数据类型的选择状况
     */
    private suspend fun writeDataInfoToFile(context: Context, choice: BooleanArray?) =
        withContext(Dispatchers.IO) {
            val infoMap = BackupHelperBase.generateDataInfo(context, choice)
            val content = BackupJson.encodeToString(DataInfoMap.serializer(), infoMap)
            FileHelper.writeContentToTempDataFile(
                context,
                BackupHelperBase.DATA_INFO_JSON,
                content,
            )
        }

    /**
     * 发送进度条通知。
     *
     * @param context 上下文
     */
    private fun sendProgressNotification(context: Context) {
        // 构建通知
        val builder = NotificationCompat.Builder(context, ChannelInfo.BACKUP_AND_RESTORE.id)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.export_data))
            .setContentText("正在导出数据……")
            .setProgress(0, 0, true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)

        // 发送通知
        NotificationHelper.sendNotification(
            NotificationID.BACKUP_AND_RESTORE.ordinal,
            builder,
            context,
        )
    }

    /**
     * 发送导出完成通知。
     *
     * @param context 上下文
     */
    private fun sendCompletionNotification(context: Context) {
        val channelID = ChannelInfo.BACKUP_AND_RESTORE.id
        val notificationBuilder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.export_data))
            .setContentText("数据导出完成")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setTimeoutAfter(5000)
            .setAutoCancel(true)
        NotificationHelper.sendNotification(
            NotificationID.BACKUP_AND_RESTORE.ordinal,
            notificationBuilder,
            context,
        )
    }
}
