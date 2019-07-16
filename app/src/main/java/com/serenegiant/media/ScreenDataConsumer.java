package com.serenegiant.media;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface ScreenDataConsumer {

    public void onDataAvailable(final int trackIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo);

    public void stop();

    public int onOutputFormatChange(MediaFormat mediaFormat);
}
