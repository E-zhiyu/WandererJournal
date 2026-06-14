package com.wanderer.journal.data.backup.helpers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.enums.BackupDataType;
import com.wanderer.journal.data.backup.maps.RoleDataMap;
import com.wanderer.journal.data.save.db.DiaryDatabase;

public class RoleBackupHelper extends BackupHelperBase<DiaryDatabase, RoleDataMap> {
    public RoleBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<RoleDataMap> getMapClass() {
        return RoleDataMap.class;
    }

    @Override
    protected DiaryDatabase getDatabase(Context context) {
        return DiaryDatabase.getInstance(context);
    }

    @Override
    protected RoleDataMap getAllDataInMap() {
        return db.dataBackupDao().exportAllRoleData();
    }

    @Override
    protected void saveDataInMapToDb(@NonNull RoleDataMap map) {
        db.dataBackupDao().importAllRoleData(map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.ROLE.getFileName();
    }
}
