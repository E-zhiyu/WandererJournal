package com.wanderer.journal.data.backup.helpers

import android.content.Context
import com.wanderer.journal.auxiliary.classes.ImportResult
import com.wanderer.journal.auxiliary.enums.types.BackupDataType
import com.wanderer.journal.data.backup.maps.LifeNoteDataMap
import com.wanderer.journal.data.save.db.DiaryDb

/**
 * 人生笔记数据的备份帮助器。
 */
class LifeNoteBackupHelper(context: Context) : BackupHelperBase<DiaryDb, LifeNoteDataMap>(
    db = DiaryDb.getInstance(context),
    dataSerializer = LifeNoteDataMap.serializer(),
    tempDataFileName = requireNotNull(BackupDataType.LIFE_NOTE.latestVersionFileName),
) {
    private val backupDao = db.dataBackupDao()

    override suspend fun getAllDataInMap(): LifeNoteDataMap = backupDao.exportLifeNoteData()

    override suspend fun saveDataInMapToDb(
        context: Context,
        map: LifeNoteDataMap,
    ): ImportResult = backupDao.importLifeNoteData(map)
}
