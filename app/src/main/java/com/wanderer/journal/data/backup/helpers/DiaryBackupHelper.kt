package com.wanderer.journal.data.backup.helpers

import android.content.Context
import com.wanderer.journal.auxiliary.classes.ImportResult
import com.wanderer.journal.auxiliary.enums.types.BackupDataType
import com.wanderer.journal.data.backup.maps.DiaryDataMap
import com.wanderer.journal.data.save.db.DiaryDb

/**
 * 日记数据的备份帮助器。
 */
class DiaryBackupHelper(context: Context) : BackupHelperBase<DiaryDb, DiaryDataMap>(
    db = DiaryDb.getInstance(context),
    dataSerializer = DiaryDataMap.serializer(),
    tempDataFileName = requireNotNull(BackupDataType.DIARY.latestVersionFileName),
) {
    private val backupDao = db.dataBackupDao()

    override suspend fun getAllDataInMap(): DiaryDataMap = backupDao.exportDiaryData()

    override suspend fun saveDataInMapToDb(
        context: Context,
        map: DiaryDataMap,
    ): ImportResult = backupDao.importDiaryData(context, map)
}
