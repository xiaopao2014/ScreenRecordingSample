package com.serenegiant.media;

import com.serenegiant.media.saver.MP4DataSaver;

import java.io.IOException;

public abstract class BaseDataListener implements DataListener {
    public static final boolean DEBUG = false;    // TODO set false on release
    public static final String TAG = MP4DataSaver.class.getSimpleName();

    public int mEncoderCount, mStatredCount;
    public boolean mIsStarted;
    private volatile boolean mIsPaused;
    private MediaEncoder mVideoEncoder;

    public BaseDataListener() {
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


    @Override
    public synchronized void pauseRecording() {
        mIsPaused = true;
        if (mVideoEncoder != null)
            mVideoEncoder.pauseRecording();
    }

    @Override
    public synchronized void resumeRecording() {
        if (mVideoEncoder != null)
            mVideoEncoder.resumeRecording();
        mIsPaused = false;
    }

    public synchronized boolean isPaused() {
        return mIsPaused;
    }

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


}
