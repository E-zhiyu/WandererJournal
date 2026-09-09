package com.wanderer.journal.auxiliary.classes;

import android.net.Uri;

public class MediaFileInfo {
    private final Uri uri;      //文件 Uri
    private final long size;    //大小(B)
    private final String name;  //文件名

    public MediaFileInfo(Uri uri, long size, String name) {
        this.uri = uri;
        this.size = size;
        this.name = name;
    }

    public Uri getUri() {
        return uri;
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }
}
