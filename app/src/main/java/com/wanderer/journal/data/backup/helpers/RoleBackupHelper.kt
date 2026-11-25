package com.wanderer.journal.data.backup.helpers

import android.content.Context
import com.wanderer.journal.auxiliary.classes.ImportResult
import com.wanderer.journal.auxiliary.enums.types.BackupDataType
import com.wanderer.journal.data.backup.maps.RoleDataMap
import com.wanderer.journal.data.save.db.DiaryDb

/**
 * 角色数据的备份帮助器。
 */
class RoleBackupHelper(context: Context) : BackupHelperBase<DiaryDb, RoleDataMap>(
    db = DiaryDb.getInstance(context),
    dataSerializer = RoleDataMap.serializer(),
    tempDataFileName = requireNotNull(BackupDataType.ROLE.latestVersionFileName),
) {
    private val backupDao = db.dataBackupDao()

    override suspend fun getAllDataInMap(): RoleDataMap = backupDao.exportRoleData()

    override suspend fun saveDataInMapToDb(
        context: Context,
        map: RoleDataMap,
    ): ImportResult = backupDao.importRoleData(map)
}
