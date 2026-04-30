package com.wanderer.journal.data.save.db.entities;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.wanderer.journal.data.save.db.Tables;
import com.wanderer.journal.data.save.db.converters.UriConverter;

@Entity(
        tableName = Tables.MEDIA,
        foreignKeys = @ForeignKey(
                entity = Paragraph.class,
                parentColumns = "paragraphId",      //参照的列名
                childColumns = "parentParagraphId", //子列名
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("parentParagraphId")}     //索引
)
@TypeConverters({UriConverter.class})
public class Media {
    @PrimaryKey(autoGenerate = true)
    public long mediaId;            //自增主键
    public Uri fileUri;             //文件Uri
    public long parentParagraphId;  //所属的段落的编号

    /**
     * 媒体文件实体类的构造方法
     *
     * @param parentParagraphId 所属段落的编号
     * @param fileUri           媒体文件的Uri
     */
    public Media(long parentParagraphId, Uri fileUri) {
        this.parentParagraphId = parentParagraphId;
        this.fileUri = fileUri;
    }
}
