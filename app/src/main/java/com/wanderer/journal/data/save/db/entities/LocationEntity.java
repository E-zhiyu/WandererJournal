package com.wanderer.journal.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.util.Objects;

@Entity(
        tableName = "locations",
        primaryKeys = {"paragraphId"},
        foreignKeys = {
                @ForeignKey(
                        entity = ParagraphEntity.class,
                        parentColumns = "paragraphId",
                        childColumns = "paragraphId",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class LocationEntity {
    private long paragraphId;   //段落编号（外键）
    private double latitude;    //纬度
    private double longitude;   //经度
    private double altitude;    //海拔

    public LocationEntity(long paragraphId, double latitude, double longitude, double altitude) {
        this.paragraphId = paragraphId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public long getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(long paragraphId) {
        this.paragraphId = paragraphId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LocationEntity that = (LocationEntity) o;
        return Double.compare(latitude, that.latitude) == 0 && Double.compare(longitude, that.longitude) == 0 && Double.compare(altitude, that.altitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, altitude);
    }
}
