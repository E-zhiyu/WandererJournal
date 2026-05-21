package com.wanderer.journal.data.save.db.entities;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "medias",
        foreignKeys = @ForeignKey(
                entity = ParagraphEntity.class,
                parentColumns = "paragraphId",      //参照的列名
                childColumns = "parentParagraphId", //子列名
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("parentParagraphId")}     //索引
)
public class MediaEntity {
    @PrimaryKey(autoGenerate = true)
    private long mediaId;            //自增主键
    private Uri fileUri;             //文件Uri
    private long parentParagraphId;  //所属的段落的编号

    /**
     * 媒体文件实体类的构造方法
     *
     * @param parentParagraphId 所属段落的编号
     * @param fileUri           媒体文件的Uri
     */
    public MediaEntity(long parentParagraphId, Uri fileUri) {
        this.parentParagraphId = parentParagraphId;
        this.fileUri = fileUri;
        this.mediaId = 0;   //分配为0，便于数据库自动分配
    }

    public long getMediaId() {
        return mediaId;
    }

    public void setMediaId(long mediaId) {
        this.mediaId = mediaId;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public long getParentParagraphId() {
        return parentParagraphId;
    }

    public void setParentParagraphId(long parentParagraphId) {
        this.parentParagraphId = parentParagraphId;
    }

    /**
     * 获取与 Uri 相关的哈希值作为适配器多选的 ID，由于不含主键，因此还适用于临时媒体文件阶段
     *
     * @return {@link #fileUri#hashCode()}得到的哈希值
     */
    public long getItemId() {
        return fileUri.hashCode();
    }
}
