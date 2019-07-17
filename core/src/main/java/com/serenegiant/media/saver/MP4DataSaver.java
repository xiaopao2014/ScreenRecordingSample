package com.serenegiant.media.saver;
/*
 * ScreenRecordingSample
 * Sample project to cature and save audio from internal and video from screen as MPEG4 file.
 *
 * Copyright (c) 2014-2016 saki t_saki@serenegiant.com
 *
 * File name: MP4DataSaver.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
 */

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import com.serenegiant.media.BaseDataListener;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * @author linliangbin
 * @date 2019/7/17 10:08
 * @desc 视频码流封装成MP4文件保存
 **/

public class MP4DataSaver extends BaseDataListener {


    private String mOutputPath;
    private final MediaMuxer mMediaMuxer;    // API >= 18
    private int tractIndex;

    /**
     * Constructor
     *
     * @throws IOException
     */
    public MP4DataSaver() throws IOException {
        try {
            mOutputPath = Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".mp4";
        } catch (final NullPointerException e) {
            throw new RuntimeException("This app has no permission of writing external storage");
        }
        mMediaMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }


    @Override
    public int onOutputFormatChange(MediaFormat mediaFormat) {
        tractIndex = mMediaMuxer.addTrack(mediaFormat);
        startMuxer();
        return tractIndex;
    }

    @Override
    public synchronized void stopRecording() {
        super.stopRecording();
        mStatredCount--;
        if ((mEncoderCount > 0) && (mStatredCount <= 0)) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mIsStarted = false;
            if (DEBUG) Log.v(TAG, "MediaMuxer stopped:");
        }
    }


    @Override
    public void onDataAvailable(int trackIndex, int byteBufIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        mMediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
    }

    synchronized boolean startMuxer() {
        if (DEBUG) Log.v(TAG, "start:");
        mStatredCount++;
        if ((mEncoderCount > 0) && (mStatredCount == mEncoderCount)) {
            mMediaMuxer.start();
            mIsStarted = true;
            notifyAll();
            if (DEBUG) Log.v(TAG, "MediaMuxer started:");
        }
        return mIsStarted;
    }
}
