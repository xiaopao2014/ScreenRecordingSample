package com.serenegiant.media.saver;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import com.serenegiant.media.BaseDataListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author linliangbin
 * @date 2019/7/17 10:08
 * @desc 将视频码流直接保存
 **/

public class H264DataSaver extends BaseDataListener {

    private BufferedOutputStream fileOut;

    public H264DataSaver() {
        super();
        File f = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".h264");
        try {
            fileOut = new BufferedOutputStream(new FileOutputStream(f));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法三：
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexFun3(byte[] bytes) {
        StringBuffer buf = new StringBuffer();
        for(byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format(" 0x%02x ", new Integer(b & 0xff)));
        }

        return buf.toString();
    }


    /**
     * @author linliangbin
     * @desc 保存H264码流
     * @params
     * @update
     * @create 2019/7/16 18:41
     **/
    public void writeFile(OutputStream out, ByteBuffer sourceData) throws IOException {

        byte[] outData = new byte[sourceData.limit()];
        sourceData.get(outData);

//        Log.i("MediaEncoder", "length:" + outData.length+",date:" + bytesToHexFun3(outData));
        if(out == null){
            return;
        }
        out.write(outData, 0, outData.length);
        out.flush();

    }


    @Override
    public void onDataAvailable(int trackIndex, int bufferIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        try {
            writeFile(fileOut, byteBuf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void stopRecording() {
        super.stopRecording();
        try {
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onOutputFormatChange(MediaFormat mediaFormat) {
        return 0;
    }
}
