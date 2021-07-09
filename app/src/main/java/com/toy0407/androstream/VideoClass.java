package com.toy0407.androstream;

import android.graphics.Bitmap;

public class VideoClass {
    String name, streamlink;

    public VideoClass() {
        this.name = null;
        this.streamlink = null;
    }
    public VideoClass(String name, String streamlink) {
        this.name = name;
        this.streamlink = streamlink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreamlink() {
        return streamlink;
    }

    public void setStreamlink(String streamlink) {
        this.streamlink = streamlink;
    }
}
