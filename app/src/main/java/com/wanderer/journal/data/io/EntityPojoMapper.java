package com.wanderer.journal.data.io;

import android.net.Uri;

import com.wanderer.journal.data.io.pojo.DiaryPojo;
import com.wanderer.journal.data.io.pojo.MediaPojo;
import com.wanderer.journal.data.io.pojo.ParagraphPojo;
import com.wanderer.journal.data.save.db.converters.DateTimeConverter;
import com.wanderer.journal.data.save.db.converters.UriConverter;
import com.wanderer.journal.data.save.db.entities.DiaryEntity;
import com.wanderer.journal.data.save.db.entities.MediaEntity;
import com.wanderer.journal.data.save.db.entities.ParagraphEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface EntityPojoMapper {
    EntityPojoMapper INSTANCE = Mappers.getMapper(EntityPojoMapper.class);

    @Mapping(target = "diaryDate", source = "diaryDate", qualifiedByName = "longToDate")
    DiaryEntity toDiaryEntity(DiaryPojo pojo);

    @Mapping(target = "diaryDate", source = "diaryDate", qualifiedByName = "dateToLong")
    DiaryPojo toDiaryPojo(DiaryEntity entity);

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

    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "timeToLong")
    ParagraphPojo toParagraphPojo(ParagraphEntity entity);

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

    @Mapping(target = "fileUri", source = "fileUri", qualifiedByName = "uriToStr")
    MediaPojo toMediaPojo(MediaEntity entity);

    @Named("strToUri")
    default Uri strToUri(String string) {
        return UriConverter.toUri(string);
    }

    @Named("uriToStr")
    default String uriToStr(Uri uri) {
        return UriConverter.fromUri(uri);
    }
}
