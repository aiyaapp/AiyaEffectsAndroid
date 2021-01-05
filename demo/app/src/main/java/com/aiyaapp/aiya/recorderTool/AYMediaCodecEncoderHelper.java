package com.aiyaapp.aiya.recorderTool;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.util.Log;

import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;

public class AYMediaCodecEncoderHelper {

    static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding

    static final String MIME_TYPE_AUDIO = "audio/mp4a-latm"; // audio encoder

    /**
     * 视频编码器信息
     */
    public static class CodecInfo {
        public int maxWidth;
        public int maxHeight;
        public int fps;
        public int bitRate;
    }

    /**
     * 获取视频编码器信息
     */
    public static CodecInfo getAvcSupportedFormatInfo() {
        MediaCodecInfo mediaCodecInfo = selectCodec(MIME_TYPE);
        if (mediaCodecInfo == null) { // not supported
            return null;
        }
        MediaCodecInfo.CodecCapabilities cap = mediaCodecInfo.getCapabilitiesForType(MIME_TYPE);
        if (cap == null) { // not supported
            return null;
        }
        CodecInfo info = new CodecInfo();
        int highestLevel = 0;
        for (MediaCodecInfo.CodecProfileLevel lvl : cap.profileLevels) {
            if (lvl.level > highestLevel) {
                highestLevel = lvl.level;
            }
        }
        int maxWidth, maxHeight, bitRate, fps;
        switch (highestLevel) {
            // Do not support Level 1 to 2.
            case MediaCodecInfo.CodecProfileLevel.AVCLevel1:
            case MediaCodecInfo.CodecProfileLevel.AVCLevel11:
            case MediaCodecInfo.CodecProfileLevel.AVCLevel12:
            case MediaCodecInfo.CodecProfileLevel.AVCLevel13:
            case MediaCodecInfo.CodecProfileLevel.AVCLevel1b:
            case MediaCodecInfo.CodecProfileLevel.AVCLevel2:
                return null;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel21:
                maxWidth = 352;
                maxHeight = 576;
                bitRate = 4000000;
                fps = 25;
                break;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel22:
                maxWidth = 720;
                maxHeight = 480;
                bitRate = 4000000;
                fps = 15;
                break;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel3:
                maxWidth = 720;
                maxHeight = 480;
                bitRate = 10000000;
                fps = 30;
                break;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel31:
                maxWidth = 1280;
                maxHeight = 720;
                bitRate = 14000000;
                fps = 30;
                break;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel32:
                maxWidth = 1280;
                maxHeight = 720;
                bitRate = 20000000;
                fps = 60;
                break;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel4: // only try up to 1080p
            default:
                maxWidth = 1920;
                maxHeight = 1080;
                bitRate = 20000000;
                fps = 30;
                break;
        }
        info.maxWidth = maxWidth;
        info.maxHeight = maxHeight;
        info.fps = fps;
        info.bitRate = bitRate;
        Log.i(AYGPUImageConstants.TAG, "AVC Level 0x" + Integer.toHexString(highestLevel) + " bit rate " + bitRate +
                " fps " + info.fps + " w " + maxWidth + " h " + maxHeight);
        return info;
    }

    /**
     * 选择指定的编码器, 没有找到返回null
     */
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }
}
