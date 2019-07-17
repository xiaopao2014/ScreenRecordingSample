package com.serenegiant.media;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ScreenDataConsumer {

    public void onDataAvailable(final int trackIndex,int bufferIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo);

    public void stop();

    public int onOutputFormatChange(MediaFormat mediaFormat);

    public void addEncoder(final MediaEncoder encoder);

    public void prepare() throws IOException;

    public void startRecording();
}
