package com.wanderer.journal.data.backup.helpers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wanderer.journal.auxiliary.enums.BackupDataType;
import com.wanderer.journal.data.backup.maps.RoleDataMap;
import com.wanderer.journal.data.save.db.DiaryDb;

public class RoleBackupHelper extends BackupHelperBase<DiaryDb, RoleDataMap> {
    public RoleBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<RoleDataMap> getMapClass() {
        return RoleDataMap.class;
    }

    @Override
    protected DiaryDb getDatabase(Context context) {
        return DiaryDb.getInstance(context);
    }

    @Override
    protected RoleDataMap getAllDataInMap() {
        return db.dataBackupDao().exportRoleData();
    }

    @Override
    protected void saveDataInMapToDb(@NonNull RoleDataMap map) {
        db.dataBackupDao().importRoleData(map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.ROLE.getFileName();
    }
}
