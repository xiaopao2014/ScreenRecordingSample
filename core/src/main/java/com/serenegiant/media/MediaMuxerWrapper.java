package com.serenegiant.media;
/*
 * ScreenRecordingSample
 * Sample project to cature and save audio from internal and video from screen as MPEG4 file.
 *
 * Copyright (c) 2014-2016 saki t_saki@serenegiant.com
 *
 * File name: MediaMuxerWrapper.java
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

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaMuxerWrapper implements ScreenDataConsumer {
    private static final boolean DEBUG = false;    // TODO set false on release
    private static final String TAG = MediaMuxerWrapper.class.getSimpleName();

    private String mOutputPath;
    private final MediaMuxer mMediaMuxer;    // API >= 18
    private int mEncoderCount, mStatredCount;
    private boolean mIsStarted;
    private volatile boolean mIsPaused;
    private MediaEncoder mVideoEncoder;

    /**
     * Constructor
     *
     * @param _ext extension of output file
     * @throws IOException
     */
    public MediaMuxerWrapper() throws IOException {
        try {
            mOutputPath = Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".mp4";
        } catch (final NullPointerException e) {
            throw new RuntimeException("This app has no permission of writing external storage");
        }
        mMediaMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mEncoderCount = mStatredCount = 0;
        mIsStarted = false;
    }


    public synchronized void prepare() throws IOException {
        if (mVideoEncoder != null)
            mVideoEncoder.prepare();
    }

    public synchronized void startRecording() {
        if (mVideoEncoder != null)
            mVideoEncoder.startRecording();
    }

    public synchronized void stopRecording() {
        if (mVideoEncoder != null)
            mVideoEncoder.stopRecording();
        mVideoEncoder = null;
    }

    public synchronized boolean isStarted() {
        return mIsStarted;
    }

    public synchronized void pauseRecording() {
        mIsPaused = true;
        if (mVideoEncoder != null)
            mVideoEncoder.pauseRecording();
    }

    public synchronized void resumeRecording() {
        if (mVideoEncoder != null)
            mVideoEncoder.resumeRecording();
        mIsPaused = false;
    }

    public synchronized boolean isPaused() {
        return mIsPaused;
    }

//**********************************************************************
//**********************************************************************

    /**
     * assign encoder to this calss. this is called from encoder.
     *
     * @param encoder instance of MediaVideoEncoderBase
     */
    public void addEncoder(final MediaEncoder encoder) {
        if (encoder instanceof MediaVideoEncoderBase) {
            if (mVideoEncoder != null)
                throw new IllegalArgumentException("Video encoder already added.");
            mVideoEncoder = encoder;
        } else
            throw new IllegalArgumentException("unsupported encoder");
        mEncoderCount = (mVideoEncoder != null ? 1 : 0);
    }

    /**
     * request start recording from encoder
     *
     * @return true when muxer is ready to write
     */
    /*package*/
    synchronized boolean start() {
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

    /**
     * request stop recording from encoder when encoder received EOS
     */
    public synchronized void stop() {
        if (DEBUG) Log.v(TAG, "stop:mStatredCount=" + mStatredCount);
        mStatredCount--;
        if ((mEncoderCount > 0) && (mStatredCount <= 0)) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mIsStarted = false;
            if (DEBUG) Log.v(TAG, "MediaMuxer stopped:");
        }
    }

    @Override
    public int onOutputFormatChange(MediaFormat mediaFormat) {

        int index = mMediaMuxer.addTrack(mediaFormat);
        if (!start()) {
            // we should wait until muxer is ready
            synchronized (this) {
                while (!this.isStarted())
                    try {
                        this.wait(100);
                    } catch (final InterruptedException e) {
                        break;
                    }
            }
        }
        return index;
    }

    /**
     * assign encoder to muxer
     *
     * @param format
     * @return minus value indicate error
     */
    private synchronized int addTrack(final MediaFormat format) {
        if (mIsStarted)
            throw new IllegalStateException("muxer already started");
        final int trackIx = mMediaMuxer.addTrack(format);
        if (DEBUG)
            Log.i(TAG, "addTrack:trackNum=" + mEncoderCount + ",trackIx=" + trackIx + ",format=" + format);
        return trackIx;
    }

    /**
     * write encoded data to muxer
     *
     * @param trackIndex
     * @param byteBuf
     * @param bufferInfo
     */
    private void writeSampleData(final int trackIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo) {
        if (mStatredCount > 0)
            mMediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
    }

    @Override
    public void onDataAvailable(int trackIndex, int byteBufIndex,ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        writeSampleData(trackIndex, byteBuf, bufferInfo);
    }
}
