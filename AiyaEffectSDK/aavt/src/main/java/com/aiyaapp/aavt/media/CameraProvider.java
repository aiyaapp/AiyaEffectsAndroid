/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aiyaapp.aavt.media;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.aiyaapp.aavt.log.AvLog;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * CameraProvider 相机数据
 *
 * @author wuwang
 * @version v1.0 2017:10:26 18:09
 */
public class CameraProvider implements ITextureProvider {

    private Camera mCamera;
    private int cameraId = 1;
    private Semaphore mFrameSem;
    private String tag = getClass().getSimpleName();
    private SurfaceTexture mSurface;

    @Override
    public Point open(final SurfaceTexture surface) {
        this.mSurface = surface;
        final Point size = new Point();
        try {
            mFrameSem = new Semaphore(0);
            if (!checkCameraFacing(cameraId)) {
                cameraId = 0;
            }
            mCamera = Camera.open(cameraId);
            mCamera.setPreviewTexture(surface);
            surface.setOnFrameAvailableListener(frameListener);
            Camera.Size s = mCamera.getParameters().getPreviewSize();
            mCamera.startPreview();
            size.x = s.height;
            size.y = s.width;
            AvLog.i(tag, "Camera Opened");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    @Override
    public void close() {
        mFrameSem.drainPermits();
        mFrameSem.release();
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void swithCamera() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            cameraId = cameraId == 0 ? 1 : 0;
            if (cameraId == 1 && !checkCameraFacing(cameraId)) {
                cameraId = 0;
            }
            mCamera = Camera.open(cameraId);
            mCamera.setPreviewTexture(mSurface);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean frame() {
        try {
            mFrameSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }




    @Override
    public long getTimeStamp() {
        return -1;
    }

    @Override
    public boolean isLandscape() {
        return true;
    }


    private SurfaceTexture.OnFrameAvailableListener frameListener = new SurfaceTexture.OnFrameAvailableListener() {

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mFrameSem.drainPermits();
            mFrameSem.release();
        }

    };

    private static boolean checkCameraFacing(final int facing) {
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }

}
