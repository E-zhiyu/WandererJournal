package com.wanderer.journal.data.save.db.daos

import android.content.Context
import android.net.Uri
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wanderer.journal.data.save.db.entities.MediaEntity
import com.wanderer.journal.helpers.file.FileHelper

@Dao
interface MediaDao {
    /**
     * 插入媒体。
     *
     * @param mediaEntityList 新媒体实例列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMedia(mediaEntityList: List<MediaEntity>)

    /**
     * 删除媒体记录。
     *
     * @param mediaEntityList 需要删除的媒体记录列表
     */
    @Delete
    fun deleteMedia(mediaEntityList: List<MediaEntity>)

    /**
     * 通过段落编号删除媒体记录。
     *
     * @param paragraphId 需要删除媒体记录的段落编号
     */
    @Query("DELETE FROM medias WHERE parentParagraphId = :paragraphId")
    fun deleteMediaByParagraphId(paragraphId: Long)

    /**
     * 媒体删除事务，同时删除数据库中的记录和文件系统中的文件。
     *
     * @param mediaEntityList 需要删除的媒体列表
     * @param context 上下文
     */
    @Transaction
    fun deleteMediasAndFiles(mediaEntityList: List<MediaEntity>, context: Context) {
        deleteMedia(mediaEntityList)

        // 删除文件
        for (media in mediaEntityList) {
            media.fileUri?.let { FileHelper.deleteFile(it, context) }
        }
    }

    /**
     * 通过段落 ID 获取其绑定的媒体数据。
     *
     * @param paragraphId 段落 ID
     * @return 媒体实体列表
     */
    @Query("SELECT * FROM medias WHERE parentParagraphId = :paragraphId")
    fun getMediaByParagraphId(paragraphId: Long): List<MediaEntity>

    /**
     * 获取数据库中所有媒体文件的 Uri。
     *
     * 用于媒体列表界面判断扫描到的文件是否被数据库引用。
     * 这里过滤掉 `null`，避免 `Uri` 类型转换器收到空值。
     */
    @Query("SELECT DISTINCT fileUri FROM medias WHERE fileUri IS NOT NULL")
    fun getAllMediaFileUris(): List<Uri>
}
