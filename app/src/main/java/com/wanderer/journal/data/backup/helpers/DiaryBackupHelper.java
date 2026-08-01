package com.wanderer.journal.data.backup.helpers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wanderer.journal.data.backup.maps.DiaryDataMap;
import com.wanderer.journal.data.save.db.DiaryDb;
import com.wanderer.journal.auxiliary.enums.BackupDataType;

public class DiaryBackupHelper extends BackupHelperBase<DiaryDb, DiaryDataMap> {
    public DiaryBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<DiaryDataMap> getMapClass() {
        return DiaryDataMap.class;
    }

    @Override
    protected DiaryDb getDatabase(Context context) {
        return DiaryDb.getInstance(context);
    }

    @Override
    protected DiaryDataMap getAllDataInMap() {
        return db.dataBackupDao().exportDiaryData();
    }

    @Override
    protected void saveDataInMapToDb(@NonNull DiaryDataMap map) {
        db.dataBackupDao().importDiaryData(map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.DIARY.getFileName();
    }
}
