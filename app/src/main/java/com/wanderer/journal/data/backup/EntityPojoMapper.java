package com.wanderer.journal.data.backup;

import android.net.Uri;

import com.wanderer.journal.data.backup.pojo.DiaryPojo;
import com.wanderer.journal.data.backup.pojo.EmotionTagPojo;
import com.wanderer.journal.data.backup.pojo.MediaPojo;
import com.wanderer.journal.data.backup.pojo.ParagraphPojo;
import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.data.save.db.converters.UriConverter;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;
import com.wanderer.journal.data.save.db.entities.composite.EmotionTagWithParagraph;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EntityPojoMapper {
    EntityPojoMapper INSTANCE = Mappers.getMapper(EntityPojoMapper.class);

    @Mapping(target = "diaryDate", source = "diaryDate", qualifiedByName = "longToDate")
    DiaryEntity toDiaryEntity(DiaryPojo pojo);

    List<DiaryEntity> toDiaryEntityList(List<DiaryPojo> pojoList);

    @Mapping(target = "diaryDate", source = "diaryDate", qualifiedByName = "dateToLong")
    DiaryPojo toDiaryPojo(DiaryEntity entity);

    List<DiaryPojo> toDiaryPojoList(List<DiaryEntity> entityList);

    @Named("longToDate")
    default LocalDate longToDate(long timeMillis) {
        return DateTimeConverter.toLocalDate(timeMillis);
    }

    @Named("dateToLong")
    default long dateToLong(LocalDate date) {
        return DateTimeConverter.fromLocalDate(date);
    }

    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "longToTime")
    ParagraphEntity toParagraphEntity(ParagraphPojo pojo);

    List<ParagraphEntity> toParagraphEntityList(List<ParagraphPojo> pojoList);

    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "timeToLong")
    ParagraphPojo toParagraphPojo(ParagraphEntity entity);

    List<ParagraphPojo> toParagraphPojoList(List<ParagraphEntity> entityList);

    @Named("longToTime")
    default LocalDateTime longToTime(long timeMillis) {
        return DateTimeConverter.toLocalDateTime(timeMillis);
    }

    @Named("timeToLong")
    default long timeToLong(LocalDateTime time) {
        return DateTimeConverter.fromLocalDateTime(time);
    }

    @Mapping(target = "fileUri", source = "fileUri", qualifiedByName = "strToUri")
    MediaEntity toMediaEntity(MediaPojo pojo);

    List<MediaEntity> toMediaEntityList(List<MediaPojo> pojoList);

    @Mapping(target = "fileUri", source = "fileUri", qualifiedByName = "uriToStr")
    MediaPojo toMediaPojo(MediaEntity entity);

    List<MediaPojo> toMediaPojoList(List<MediaEntity> entityList);

    @Named("strToUri")
    default Uri strToUri(String string) {
        return UriConverter.toUri(string);
    }

    @Named("uriToStr")
    default String uriToStr(Uri uri) {
        return UriConverter.fromUri(uri);
    }

    @Mapping(target = "emotionTag.emotionId",source = "emotionId")
    @Mapping(target = "emotionTag.name",source = "name")
    @Mapping(target = "emotionTag.description",source = "description")
    //paragraphIdList类型和名称一致，自动映射
    EmotionTagWithParagraph toEmotionTagWithParagraph(EmotionTagPojo pojo);

    List<EmotionTagWithParagraph> toEmotionTagWithParagraphList(List<EmotionTagPojo> pojoList);

    @Mapping(target = "emotionId",source = "emotionTag.emotionId")
    @Mapping(target = "name",source = "emotionTag.name")
    @Mapping(target = "description",source = "emotionTag.description")
    //paragraphIdList类型和名称一致，自动映射
    EmotionTagPojo toEmotionTagPojo(EmotionTagWithParagraph emotionTagWithParagraph);

    List<EmotionTagPojo> toEmotionTagPojoList(List<EmotionTagWithParagraph> emotionTagWithParagraphList);
}
