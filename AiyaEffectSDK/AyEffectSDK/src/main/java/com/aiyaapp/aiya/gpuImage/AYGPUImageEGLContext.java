package com.aiyaapp.aiya.gpuImage;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.concurrent.Semaphore;

import static android.opengl.EGL14.*;

public class AYGPUImageEGLContext {

    private EGLDisplay eglDisplay;
    private EGLSurface surface;
    private static EGLContext context;

    private static HandlerThread handlerThread;
    private static Handler glesHandler;
    static {
        handlerThread = new HandlerThread("com.aiyaapp.gpuimage");
        handlerThread.start();
        glesHandler = new Handler(handlerThread.getLooper());

    }

    public boolean initEGLWindow(Object nativeWindow) {
        eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        if (eglDisplay == null) {
            return false;
        }

        int[] versions = new int[2];
        if (!eglInitialize(eglDisplay, versions, 0, versions, 1)) {
            return false;
        }

        int[] attrs = {
                EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                EGL_DEPTH_SIZE, 16,
                EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!eglChooseConfig(eglDisplay, attrs, 0, configs, 0, configs.length, numConfigs, 0)) {
            return false;
        }

        surface = eglCreateWindowSurface(eglDisplay, configs[0], nativeWindow, new int[]{EGL_NONE}, 0);
        if (surface == EGL_NO_SURFACE) {
            return false;
        }

        int[] contextAttrs = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
        };
        if (context == null) {
            context = eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                    contextAttrs, 0);
        }
        if (context == EGL_NO_CONTEXT) {
            return false;
        }

        return eglMakeCurrent(eglDisplay, surface, surface, context);
    }

    public boolean makeCurrent() {
        if (eglDisplay != null && surface != null && context != null) {
            return eglMakeCurrent(eglDisplay, surface, surface, context);
        } else {
            return false;
        }
    }

    public void setTimeStemp(long time) {
        if (eglDisplay != null && surface != null) {
            EGLExt.eglPresentationTimeANDROID(eglDisplay, surface, time);
        }
    }

    public boolean swapBuffers() {
        if (eglDisplay != null && surface != null) {
            return eglSwapBuffers(eglDisplay, surface);
        } else {
            return false;
        }
    }

    public void destroyEGLWindow() {
        if (eglDisplay != null && surface != null) {
            eglDestroySurface(eglDisplay, surface);
        }
    }

    public static void syncRunOnRenderThread(final Runnable runnable) {
        Thread thread = Thread.currentThread();

        if (thread == handlerThread) {
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
    }
}