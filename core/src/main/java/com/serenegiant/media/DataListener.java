package com.serenegiant.media;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface DataListener {

    public void addEncoder(final MediaEncoder encoder);

    public void prepare() throws IOException;

    public void startRecording();

    public int onOutputFormatChange(MediaFormat mediaFormat);

    public void onDataAvailable(final int trackIndex, int bufferIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo);

    public void pauseRecording();

    public void resumeRecording();

    public void stopRecording();
}
