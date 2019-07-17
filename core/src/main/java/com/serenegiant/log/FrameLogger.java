package com.serenegiant.log;

import android.util.Log;

import java.util.HashMap;

public class FrameLogger {

    private static FrameLogger frameLogger = new FrameLogger();


    public static FrameLogger getInstance() {
        return frameLogger;
    }

    static class TagFrame {
        int count;
        long startTime;
    }

    HashMap<String, TagFrame> tagMap = new HashMap<>();

    public synchronized void frame(String tag) {
        long currentTime = System.currentTimeMillis();
        TagFrame tagFrame = tagMap.get(tag);
        if (tagFrame == null) {
            tagFrame = new TagFrame();
            tagFrame.count = 0;
            tagFrame.startTime = currentTime;
            tagMap.put(tag, tagFrame);
        } else {
            tagFrame.count++;
            if (currentTime - tagFrame.startTime >= 1000) {
                Log.i("FrameLogger", tag + " fps= " + tagFrame.count);
                tagFrame.count = 0;
                tagFrame.startTime = currentTime;
            } else {
//                Log.i("FrameLogger", tag + " frame " +tagFrame.count);
            }
        }
    }
}
