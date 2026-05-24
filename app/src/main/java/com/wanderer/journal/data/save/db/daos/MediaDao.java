package com.wanderer.journal.data.save.db.daos;

import android.content.Context;
import android.net.Uri;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.helpers.file.FileHelper;

import java.util.List;

@Dao
public interface MediaDao {
    /**
     * 插入媒体
     *
     * @param mediaEntityList 新媒体实例列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMedia(List<MediaEntity> mediaEntityList);

    /**
     * 删除媒体记录
     *
     * @param mediaEntityList 需要删除的媒体记录列表
     */
    @Delete
    void deleteMedia(List<MediaEntity> mediaEntityList);

    /**
     * 媒体删除事务，同时删除数据库中的记录和文件系统中的文件
     *
     * @param mediaEntityList 需要删除的媒体列表
     * @param context         上下文
     */
    @Transaction
    default void deleteMediasAndFiles(List<MediaEntity> mediaEntityList, Context context) {
        deleteMedia(mediaEntityList);

        //删除文件
        for (MediaEntity media : mediaEntityList) {
            Uri mediaUri = media.getFileUri();
            FileHelper.deleteFile(mediaUri, context);
        }
    }

    /**
     * 通过段落 ID 获取其绑定的媒体数据
     *
     * @param paragraphId 段落 ID
     * @return 媒体实体列表
     */
    @Query("SELECT * FROM medias WHERE parentParagraphId = :paragraphId")
    List<MediaEntity> getMediaByParagraphId(long paragraphId);

    /**
     * 读取所有数据用于导出
     *
     * @return 媒体实体列表
     */
    @Query("SELECT * FROM medias")
    List<MediaEntity> exportData();

    /**
     * 删除所有数据，准备导入新数据
     */
    @Query("DELETE FROM medias")
    void clear();

    /**
     * 导入新数据
     *
     * @param mediaEntityList 新数据实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void importData(List<MediaEntity> mediaEntityList);
}
