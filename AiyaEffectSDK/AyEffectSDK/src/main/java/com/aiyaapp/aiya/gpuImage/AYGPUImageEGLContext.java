package com.aiyaapp.aiya.gpuImage;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.concurrent.Semaphore;

import static android.opengl.EGL14.*;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.TAG;

public class AYGPUImageEGLContext {

    private EGLDisplay eglDisplay;
    private EGLSurface surface;
    private EGLContext context;

    private HandlerThread handlerThread;
    private Handler glesHandler;

    /**
     * 创建EGLContext, 绑定window
     */
    public boolean initWithEGLWindow(final Object nativeWindow) {

        // 初始化GL执行线程
        handlerThread = new HandlerThread("com.aiyaapp.gpuimage");
        handlerThread.start();
        glesHandler = new Handler(handlerThread.getLooper());

        // 创建EGLWindow
        final boolean[] result = new boolean[1];

        final Semaphore semaphore = new Semaphore(0);

        glesHandler.post(new Runnable() {
            @Override
            public void run() {
                result[0] = createAndBindEGLWindow(nativeWindow);
                semaphore.release();
            }
        });

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0];
    }

    private boolean createAndBindEGLWindow(Object nativeWindow) {
        eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        if (eglDisplay == null) {
            Log.d(TAG, "eglGetDisplay error " + eglGetError());
            return false;
        }

        int[] versions = new int[2];
        if (!eglInitialize(eglDisplay, versions, 0, versions, 1)) {
            Log.d(TAG, "eglInitialize error " + eglGetError());
            return false;
        }

        int[] attrs = {
                EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                EGL_DEPTH_SIZE, 16,
                EGL_STENCIL_SIZE, 8,
                EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!eglChooseConfig(eglDisplay, attrs, 0, configs, 0, configs.length, numConfigs, 0)) {
            Log.d(TAG, "eglChooseConfig error " + eglGetError());
            return false;
        }

        surface = eglCreateWindowSurface(eglDisplay, configs[0], nativeWindow, new int[]{EGL_NONE}, 0);
        if (surface == EGL_NO_SURFACE) {
            Log.d(TAG, "eglCreateWindowSurface error " + eglGetError());
            return false;
        }

        int[] contextAttrs = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
        };
        context = eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                    contextAttrs, 0);
        if (context == EGL_NO_CONTEXT) {
            Log.d(TAG, "eglCreateContext error " + eglGetError());
            return false;
        }

        if (!eglMakeCurrent(eglDisplay, surface, surface, context)) {
            Log.d(TAG, "eglMakeCurrent error " + eglGetError());
            return false;
        }

        Log.d(TAG, "创建 eglCreateContext");
        return true;
    }

    public boolean makeCurrent() {
        if (eglDisplay != null && surface != null && context != null) {
            // Log.d(TAG,"makeCurrent " + Thread.currentThread());
            return eglMakeCurrent(eglDisplay, surface, surface, context);
        } else {
            return false;
        }
    }

    public boolean makeCurrent(EGLDisplay eglDisplay, EGLSurface surface) {
        if (eglDisplay != null && surface != null && context != null) {
            // Log.d(TAG,"makeCurrent " + Thread.currentThread());
            return eglMakeCurrent(eglDisplay, surface, surface, context);
        } else {
            return false;
        }
    }

    public boolean setTimestamp(long time) {
        if (eglDisplay != null && surface != null) {
            return EGLExt.eglPresentationTimeANDROID(eglDisplay, surface, time);
        } else {
            return false;
        }
    }

    public boolean setTimestamp(EGLDisplay eglDisplay, EGLSurface surface, long time) {
        if (eglDisplay != null && surface != null) {
            return EGLExt.eglPresentationTimeANDROID(eglDisplay, surface, time);
        } else {
            return false;
        }
    }

    public boolean swapBuffers() {
        if (eglDisplay != null && surface != null) {
            return eglSwapBuffers(eglDisplay, surface);
        } else {
            return false;
        }
    }

    public boolean swapBuffers(EGLDisplay eglDisplay, EGLSurface surface) {
        if (eglDisplay != null && surface != null) {
            return eglSwapBuffers(eglDisplay, surface);
        } else {
            return false;
        }
    }

    public void destroyEGLWindow() {
        if (eglDisplay != null && context != null) {
            eglDestroyContext(eglDisplay, context);
            Log.d(TAG, "销毁 eglContext");
        }

        if (eglDisplay != null && surface != null) {
            eglDestroySurface(eglDisplay, surface);
        }

        if (eglDisplay != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
            }
        }

        if (handlerThread != null) {
            handlerThread.quit();
        }
    }

    public void syncRunOnRenderThread(final Runnable runnable) {

        if (eglDisplay != null && surface != null && context != null && glesHandler != null) {
            Thread thread = Thread.currentThread();

            if (thread == glesHandler.getLooper().getThread()) {
                runnable.run();
            } else {

                final Semaphore semaphore = new Semaphore(0);

                glesHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                        semaphore.release();
                    }
                });

                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            runnable.run();
        }
    }

    public static class Helper {

        public EGLDisplay eglDisplay;
        public EGLSurface surface;

        /**
         * 绑定EGLWindow
         */
        public boolean generateEGLWindow(Object nativeWindow) {

            eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
            if (eglDisplay == null) {
                Log.d(TAG, "eglGetDisplay error " + eglGetError());
                return false;
            }

            int[] versions = new int[2];
            if (!eglInitialize(eglDisplay, versions, 0, versions, 1)) {
                Log.d(TAG, "eglInitialize error " + eglGetError());
                return false;
            }

            int[] attrs = {
                    EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                    EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                    EGL_RED_SIZE, 8,
                    EGL_GREEN_SIZE, 8,
                    EGL_BLUE_SIZE, 8,
                    EGL_ALPHA_SIZE, 8,
                    EGL_DEPTH_SIZE, 16,
                    EGL_STENCIL_SIZE, 8,
                    EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!eglChooseConfig(eglDisplay, attrs, 0, configs, 0, configs.length, numConfigs, 0)) {
                Log.d(TAG, "eglChooseConfig error " + eglGetError());
                return false;
            }

            surface = eglCreateWindowSurface(eglDisplay, configs[0], nativeWindow, new int[]{EGL_NONE}, 0);
            if (surface == EGL_NO_SURFACE) {
                Log.d(TAG, "eglCreateWindowSurface error " + eglGetError());
                return false;
            }

            return true;
        }


        public void destroyEGLWindow() {

            if (eglDisplay != null && surface != null) {
                eglDestroySurface(eglDisplay, surface);
            }
        }
    }
}