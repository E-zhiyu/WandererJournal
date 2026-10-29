package com.wanderer.journal.data.backup.helpers;

import android.content.Context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanderer.journal.auxiliary.classes.ImportResult;
import com.wanderer.journal.auxiliary.enums.types.BackupDataType;
import com.wanderer.journal.auxiliary.enums.types.ImportDataVersionType;
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
    protected ImportResult saveDataInMapToDb(Context context, RoleDataMap map) {
        return db.dataBackupDao().importRoleData(map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.ROLE.getLatestVersionFileName();
    }

    @Override
    protected RoleDataMap getDataMap(String json, ImportDataVersionType versionType) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, mapClass);
    }
}
