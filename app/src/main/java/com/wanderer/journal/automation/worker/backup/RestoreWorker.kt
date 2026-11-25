package com.wanderer.journal.automation.worker.backup

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.classes.ImportResult
import com.wanderer.journal.auxiliary.enums.ChannelInfo
import com.wanderer.journal.auxiliary.enums.intent.NotificationID
import com.wanderer.journal.auxiliary.enums.types.BackupDataType
import com.wanderer.journal.auxiliary.enums.types.ImportDataVersionType
import com.wanderer.journal.auxiliary.enums.unique.KeyStrings
import com.wanderer.journal.auxiliary.enums.unique.LogTags
import com.wanderer.journal.helpers.NotificationHelper
import com.wanderer.journal.helpers.file.ZipHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 数据恢复 Worker。
 *
 * 由原来的 {@code RxWorker} 改为 [CoroutineWorker]，文件解压和数据导入通过 Kotlin 协程执行。
 */
class RestoreWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 获取输入的数据
        val context = applicationContext
        val inputData = inputData
        val inputUriStr = inputData.getString(KeyStrings.BACKUP_TARGET.v())
        val choices = inputData.getBooleanArray(KeyStrings.BACKUP_CHOICES.v())

        // 判断输入参数是否为空
        if (inputUriStr == null) {
            Log.e(LogTags.RESTORE_WORKER.n(), "无法获取恢复文件的位置")
            return Result.failure()
        }

        // 解压文件并写入数据
        sendProgressNotification(context) // 发送通知
        val uri = inputUriStr.toUri()
        val resultFlagList = BackupDataType.entries.map { type ->
            if (choices == null || choices.getOrNull(type.ordinal) == true) {
                ImportResult.missed()
            } else {
                ImportResult.success()
            }
        }.toMutableList() // 存有数据导入结果的列表

        return try {
            // 读取备份文件信息
            val infoMap = withContext(Dispatchers.IO) {
                ZipHelper.getDataInfoFromBackupFile(context, uri)
            } ?: throw RuntimeException("无法读取备份文件信息，请选择正确的备份文件")

            // 识别数据版本
            val versionType =
                ImportDataVersionType.getDataVersionByVersionCode(infoMap.appVersionCode)

            // 获取需要解压的文件名
            val allowedFileNameSet = mutableSetOf<String>()
            infoMap.fileNames.forEachIndexed { i, fileName ->
                val selected = choices == null ||
                        (i < choices.size && choices[i])
                if (selected && fileName != null) {
                    allowedFileNameSet.add(fileName)
                }
            }

            val includeMedia = choices == null || (
                    choices.getOrNull(BackupDataType.DIARY.ordinal) == true &&
                            infoMap.fileNames.getOrNull(BackupDataType.DIARY.ordinal) != null
                    )

            // 解压备份文件
            val extractedFiles = ZipHelper.unpackBackupFileWithFilter(
                context,
                uri,
                allowedFileNameSet,
                includeMedia,
            )

            // 逐个导入数据
            for (file in extractedFiles) {
                // 根据文件名判断数据类型
                val type = BackupDataType.fromFileName(file.name, versionType) ?: continue

                // 使用对应的备份 Helper 导入数据
                try {
                    val helper = type.createBackupHelper(context)
                    resultFlagList[type.ordinal] =
                        helper.importDataFromTempFile(context, file, versionType)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    resultFlagList[type.ordinal] = ImportResult.failure(e)
                    Log.e(LogTags.RESTORE_WORKER.n(), "导入失败：\n${e.message}")
                }
            }

            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LogTags.RESTORE_WORKER.n(), "恢复失败：\n${e.message}")
            Result.failure()
        } finally {
            // 发送通知
            sendCompletionNotification(context, resultFlagList, choices)
        }
    }

    /**
     * 根据数据导入情况生成通知内容。
     *
     * @param resultList 数据导入标识符列表
     * @param choices 用户导入数据的选择情况
     * @return 数据导入反馈通知的内容
     */
    private fun buildResultMessage(
        resultList: List<ImportResult>,
        choices: BooleanArray?,
    ): String {
        val strBuilder = StringBuilder("数据导入发生异常")

        // 根据结果生成反馈信息
        for (dataType in BackupDataType.entries) {
            val result = resultList[dataType.ordinal]
            if (result is ImportResult.Success ||
                (choices != null && choices.getOrNull(dataType.ordinal) == false)
            ) {
                continue
            }

            strBuilder.append("，“").append(dataType.title).append("”").append(result.title)
        }

        return strBuilder.append("。").toString()
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
            .setContentTitle(context.getString(R.string.import_data))
            .setContentText("正在导入数据……")
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
     * 发送导入完成通知。
     *
     * @param context 上下文
     * @param resultFlagList 各数据类型的导入结果
     * @param choices 用户导入数据的选择情况
     */
    private fun sendCompletionNotification(
        context: Context,
        resultFlagList: List<ImportResult>,
        choices: BooleanArray?,
    ) {
        val allSuccess = resultFlagList.all {
            it.javaClass == ImportResult.Success::class.java
        }
        val message = if (allSuccess) {
            "所有数据均导入成功。"
        } else {
            buildResultMessage(resultFlagList, choices)
        }

        // 发送通知
        val channelID = ChannelInfo.BACKUP_AND_RESTORE.id
        val notificationBuilder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.import_data))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
        NotificationHelper.sendNotification(
            NotificationID.BACKUP_AND_RESTORE.ordinal,
            notificationBuilder,
            context,
        )
    }
}
