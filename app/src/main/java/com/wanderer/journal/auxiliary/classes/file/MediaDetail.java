package com.wanderer.journal.auxiliary.classes.file;

import java.time.LocalDateTime;

public class MediaDetail {
    private final LocalDateTime time;   //创建日期（或者最后编辑日期）
    private final long size;            //文件大小
    private final int width;            //像素宽度
    private final int height;           //像素高度
    private final String fileName;      //文件名称
    private final String device;        //拍摄设备
    private final double aperture;      //光圈大小
    private final double shutter;       //快门速度
    private final int iso;              //ISO值
    private final double focalLength;   //焦距
    private final boolean isFlashUsed;  //是否使用闪光灯

    public MediaDetail(LocalDateTime time, long size, int width, int height, String fileName, String device, double aperture, double shutter, int iso, double focalLength, boolean isFlashUsed) {
        this.time = time;
        this.size = size;
        this.width = width;
        this.height = height;
        this.fileName = fileName;
        this.device = device;
        this.aperture = aperture;
        this.shutter = shutter;
        this.iso = iso;
        this.focalLength = focalLength;
        this.isFlashUsed = isFlashUsed;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public long getSize() {
        return size;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getDevice() {
        return device;
    }

    public double getAperture() {
        return aperture;
    }

    public double getShutter() {
        return shutter;
    }

    public int getIso() {
        return iso;
    }

    public double getFocalLength() {
        return focalLength;
    }

    public boolean isFlashUsed() {
        return isFlashUsed;
    }

    public String getFileName() {
        return fileName;
    }
}
