package com.serenegiant.media;
/*
 * libcommon
 * utility/helper classes for myself
 *
 * Copyright (c) 2014-2016 saki t_saki@serenegiant.com
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
*/

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.serenegiant.common.BuildConfig;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaMovieRecorder extends AbstractRecorder {
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static final String TAG = "MediaMovieRecorder";

	public interface IRecorderCallback {
		public void onPrepared(MediaMovieRecorder recorder);
		public void onStart(MediaMovieRecorder recorder);
		public void onStop(MediaMovieRecorder recorder);
	}

	private final MediaMuxer mMediaMuxer;	// API >= 18
	private IRecorderCallback mRecorderCallback;
	private final boolean hasAudioEncoder;

	public MediaMovieRecorder(final String output_path, final boolean audio_recording) throws IOException {
		super(output_path);
		mMediaMuxer = new MediaMuxer(output_path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
		new VideoEncoder(this, mMediaCodecCallback);
		if (audio_recording) {
			new AudioEncoder(this, mMediaCodecCallback);
		}
		hasAudioEncoder = audio_recording;
	}

	public void setCallback(final IRecorderCallback callback) {
		mRecorderCallback = callback;
	}

	public IRecorderCallback getCallback() {
		return mRecorderCallback;
	}

	public void setVideoSize(final int width, final int height) {
		((VideoEncoder)mVideoEncoder).setVideoSize(width, height);
	}

	@Override
	public int getWidth() {
		return mVideoEncoder != null ? ((VideoEncoder)mVideoEncoder).getWidth() : 0;
	}

	@Override
	public int getHeight() {
		return mVideoEncoder != null ? ((VideoEncoder)mVideoEncoder).getHeight() : 0;
	}

	@Override
	public Surface getInputSurface() throws IllegalStateException {
		return ((VideoEncoder)mVideoEncoder).getInputSurface();
	}

	@Override
	protected void internal_start() {
		if (DEBUG) Log.v(TAG, "internal_start:");
		mMediaMuxer.start();
	}

	@Override
	protected void internal_stop() {
		if (DEBUG) Log.v(TAG, "internal_stop:");
		mMediaMuxer.stop();
		mMediaMuxer.release();
	}

	@Override
	int addTrack(final MediaFormat format) {
		if (mIsStarted)
			throw new IllegalStateException("muxer already started");
		final int trackIx = mMediaMuxer.addTrack(format);
		if (DEBUG) Log.i(TAG, "addTrack:trackNum=" + mEncoderCount + ",trackIx=" + trackIx + ",format=" + format);
		return trackIx;
	}

	@Override
	void writeSampleData(final int trackIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo) {
		if (mIsStarted)
			mMediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
	}

	private final IMediaCodecCallback mMediaCodecCallback = new IMediaCodecCallback() {
		@Override
		public void onPrepared(IMediaCodec codec) {
			final boolean isPrepared = mVideoEncoder.isPrepared() && (!hasAudioEncoder || mAudioEncoder.isPrepared());
			if (DEBUG) Log.v(TAG, "onPrepared:isPrepared=" + isPrepared);
			if (isPrepared && (mRecorderCallback != null)) {
				try {
					mRecorderCallback.onPrepared(MediaMovieRecorder.this);
				} catch (Exception e) {
					Log.w(TAG, e);
				}
			}
		}

		@Override
		public void onStart(IMediaCodec codec) {
			final boolean isStarted = mVideoEncoder.isRunning() && (!hasAudioEncoder || mAudioEncoder.isRunning());
			if (DEBUG) Log.v(TAG, "onStart:isStarted=" + isStarted);
			if (isStarted && (mRecorderCallback != null)) {
				try {
					mRecorderCallback.onStart(MediaMovieRecorder.this);
				} catch (Exception e) {
					Log.w(TAG, e);
				}
			}
		}

		@Override
		public boolean onFrameAvailable(IMediaCodec codec, long presentationTimeUs) {
			return false;
		}

		@Override
		public void onStop(IMediaCodec codec) {
			if (DEBUG) Log.v(TAG, "onStop:codec=" + codec);
			if (mRecorderCallback != null) {
				try {
					mRecorderCallback.onStop(MediaMovieRecorder.this);
					release();
				} catch (Exception e) {
					Log.w(TAG, e);
				}
			}
		}

		@Override
		public void onRelease(IMediaCodec codec) {
			if (DEBUG) Log.v(TAG, "onRelease:codec=" + codec);
		}

		@Override
		public boolean onError(IMediaCodec codec, Exception e) {
			return false;
		}
	};

}
