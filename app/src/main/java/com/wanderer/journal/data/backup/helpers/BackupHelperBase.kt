package com.wanderer.journal.data.backup.helpers

import android.content.Context
import android.content.pm.PackageManager
import androidx.room.RoomDatabase
import com.wanderer.journal.auxiliary.classes.ImportResult
import com.wanderer.journal.auxiliary.enums.types.BackupDataType
import com.wanderer.journal.auxiliary.enums.types.ImportDataVersionType
import com.wanderer.journal.data.backup.BackupJson
import com.wanderer.journal.data.backup.maps.DataInfoMap
import com.wanderer.journal.helpers.AboutHelper
import com.wanderer.journal.helpers.file.FileHelper
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer

/**
 * 数据备份帮助器基类。
 *
 * @param D 数据库实例类型
 * @param M 数据字典类型
 * @param db 数据库实例
 * @param dataSerializer 数据字典的序列化器
 * @param tempDataFileName 临时数据文件名称
 */
abstract class BackupHelperBase<D : RoomDatabase, M : Any>(
    protected val db: D,
    private val dataSerializer: KSerializer<M>,
    private val tempDataFileName: String,
) {
    /**
     * 从数据库中获取数据集合。
     */
    protected abstract suspend fun getAllDataInMap(): M

    /**
     * 将数据集合中的数据保存至数据库。
     */
    protected abstract suspend fun saveDataInMapToDb(context: Context, map: M): ImportResult

    /**
     * 将 JSON 文本反序列化为数据集合。
     *
     * 备份 POJO 均通过 [com.wanderer.journal.data.backup.BackupJson] 解析：
     * 无法映射为 Kotlin 数据类的未知字段会被忽略，缺失或为 `null` 的字段使用 Kotlin 默认值。
     *
     * @param json 需要反序列化的 JSON 文本
     * @param versionType 数据版本种类
     * @return 数据集合
     */
    @Suppress("UNUSED_PARAMETER")
    protected open fun getDataMap(json: String, versionType: ImportDataVersionType): M =
        BackupJson.decodeFromString(dataSerializer, json)

    /**
     * 从临时文件中导入数据。
     *
     * @param context 上下文
     * @param file 临时文件对象
     * @param versionType 导入数据的版本种类
     * @return 数据导入的结果
     */
    suspend fun importDataFromTempFile(
        context: Context,
        file: File,
        versionType: ImportDataVersionType,
    ): ImportResult = withContext(Dispatchers.IO) {
        // 读取文件内容
        val json = FileHelper.readContent(file)

        // 得到数据字典实例
        val dataMap = getDataMap(json, versionType)

        // 将对应的数据写入数据库
        saveDataInMapToDb(context, dataMap)
    }

    /**
     * 导出数据并将 JSON 序列化后的字符串写入到临时数据文件中。
     *
     * @param context 上下文
     */
    suspend fun exportDataToTempFile(context: Context) = withContext(Dispatchers.IO) {
        // 将数据库中的数据转换为 JSON 字符串
        val content = BackupJson.encodeToString(dataSerializer, getAllDataInMap())

        // 写入临时数据文件
        FileHelper.writeContentToTempDataFile(context, tempDataFileName, content)
    }

    companion object {
        const val DATA_INFO_JSON = "DataInfo.json"

        /**
         * 生成备份数据信息集合。
         *
         * @param context 上下文
         * @param choice 备份数据选择情况
         * @return 备份数据信息集合
         */
        @JvmStatic
        @Throws(PackageManager.NameNotFoundException::class)
        fun generateDataInfo(context: Context, choice: BooleanArray?): DataInfoMap {
            val versionCode = AboutHelper.getVersionCode(context)
            val fileNameList = BackupDataType.entries.map { type ->
                if (choice == null || choice.getOrNull(type.ordinal) == true) {
                    type.latestVersionFileName
                } else {
                    null
                }
            }

            return DataInfoMap(
                appVersionCode = versionCode,
                fileNames = fileNameList,
            )
        }
    }
}
