package com.wanderer.journal.data.backup.helpers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wanderer.journal.data.backup.maps.DiaryDataMap;
import com.wanderer.journal.data.save.db.DiaryDatabase;
import com.wanderer.journal.auxiliary.enums.BackupDataType;

public class DiaryBackupHelper extends BackupHelperBase<DiaryDatabase, DiaryDataMap> {
    public DiaryBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<DiaryDataMap> getMapClass() {
        return DiaryDataMap.class;
    }

    @Override
    protected DiaryDatabase getDatabase(Context context) {
        return DiaryDatabase.getInstance(context);
    }

    @Override
    protected DiaryDataMap getAllDataInMap() {
        return db.dataBackupDao().exportAllDiaryData();
    }

    @Override
    protected void saveDataInMapToDb(@NonNull DiaryDataMap map) {
        db.dataBackupDao().importAllDiaryData(map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.DIARY.getFileName();
    }
}
