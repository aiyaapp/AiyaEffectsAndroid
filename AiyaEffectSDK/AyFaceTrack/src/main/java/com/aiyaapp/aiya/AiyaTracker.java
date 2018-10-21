package com.aiyaapp.aiya;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import com.aiyaapp.aiya.base.IFaceTracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 用于人脸特征点查找，可以提供给其他需要用到人脸识别的模块进行使用
 *
 * @author wuwang
 */

public class AiyaTracker implements IFaceTracker {

    private long nativeId = 0;

    public static final int IMAGE_TYPE_Y = 1;
    public static final int IMAGE_TYPE_RGBA = 2;

    private final Object LOCK = new Object();

    private Context mContext;
    private SharedPreferences mSp;

    public AiyaTracker(Context context) {
        this.mContext = context.getApplicationContext();
        mSp = this.mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        init();
    }


    public int track(int type, byte[] input, int width, int height, float[] output) {
        return _track(nativeId, type, input, width, height, output);
    }


    public int track(int type, byte[] input, int width, int height) {
        return _track(nativeId, type, input, width, height);
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }


    @Override
    public int init() {
        synchronized (LOCK) {
            if (nativeId == 0) {
                nativeId = _createNativeObj(0);
                //不同版本可能用到的模型文件不同，所以需要判断版本，不同版本就要删除掉原有的模型文件
                //后续让底层尽量支持直接读取assets中的模型，就不需要这一步了
                File folder = mContext.getExternalFilesDir(null);
                if (folder == null) {
                    folder = mContext.getFilesDir();
                }
                if (folder != null) {
                    String dstPath = folder.getAbsolutePath() + "/config";
                    int lastTrackCode = mSp.getInt("trackVersionCode", 1);
                    if (lastTrackCode != getVersionCode()) {
                        deleteFile(new File(dstPath));
                        mSp.edit().putString("trackVersionName", getVersionName())
                                .putInt("trackVersionCode", getVersionCode()).apply();
                    }


                    //TODO 判断文件是否存在，不存在就拷贝过去
                    if (!new File(dstPath).exists()) {
                        copyFileFromAssets("config", dstPath, mContext.getAssets());
                    }
                    return _init(nativeId, dstPath);
                } else {
                    return -1;
                }
            }
        }
        return 0;
    }

    /**
     * 获取当前模块的版本代码
     *
     * @return 版本代码
     */
    public int getVersionCode() {
        return _getVersionCode();
    }

    /**
     * 获取当前模块的版本名
     *
     * @return 版本名
     */
    public String getVersionName() {
        return _getVersionName();
    }

    private boolean deleteFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    deleteFile(f);
                }
            } else {
                if (!file.delete()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean copyFileFromAssets(String src, String dst, AssetManager manager) {
        try {
            String[] files = manager.list(src);
            if (files.length > 0) {     //如果是文件夹
                File folder = new File(dst);
                if (!folder.exists()) {
                    boolean b = folder.mkdirs();
                    if (!b) {
                        return false;
                    }
                }
                for (String fileName : files) {
                    if (!copyFileFromAssets(src + File.separator + fileName, dst +
                            File.separator + fileName, manager)) {
                        return false;
                    }
                }
            } else {  //如果是文件
                if (!copyAssetsFile(src, dst, manager)) {
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean copyAssetsFile(String src, String dst, AssetManager manager) {
        InputStream in;
        OutputStream out;
        try {
            File file = new File(dst);
            if (!file.exists()) {
                in = manager.open(src);
                out = new FileOutputStream(dst);
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
                out.close();
                in.close();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public long getFaceDataID() {
        return _getFaceDataID();
    }

    @Override
    public String getType() {
        return "FaceData";
    }

    @Override
    public long getId() {
        return nativeId;
    }

    @Override
    public int release() {
        int ret = 0;
        synchronized (LOCK) {
            if (nativeId != 0) {
                ret = _release(nativeId);
                nativeId = 0;
            }
        }
        return ret;
    }

    public static native long requestId();

    private static native long _createNativeObj(int type);

    private static native long _getFaceDataID();

    private static native int _init(long id, String data);

    private static native int _track(long id, int type, byte[] input, int width, int height, float[] output);

    private static native int _track(long id, int type, byte[] input, int width, int height);

    private static native int _release(long id);

    private static native int _getVersionCode();

    private static native String _getVersionName();

    static {
        System.loadLibrary("AyCoreSdk");
        System.loadLibrary("AyCoreJni");
        System.loadLibrary("AyFaceTrackJni");
    }

}
