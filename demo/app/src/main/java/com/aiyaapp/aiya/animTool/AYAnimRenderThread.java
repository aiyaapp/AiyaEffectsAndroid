package com.aiyaapp.aiya.animTool;

import android.os.SystemClock;

import java.util.concurrent.locks.ReentrantLock;

public class AYAnimRenderThread extends Thread {

    // 最后一帧的时间
    long lastRenderTime = SystemClock.elapsedRealtime();

    // 控制渲染开始结束
    ReentrantLock renderLock = new ReentrantLock(true);
    boolean renderStop = false;

    public AYAnimRenderHandler renderHandler;

    @Override
    public void run() {
        while (true) {

            renderLock.lock();

            if (renderStop) {
                renderLock.unlock();
                return;
            }

            if (renderHandler != null) {
                renderHandler.render();
            }

            renderLock.unlock();

            while (true) {
                long currentTime = SystemClock.elapsedRealtime();
                if (currentTime - lastRenderTime < 33) {
                    SystemClock.sleep(5);
                } else {
                    lastRenderTime += 33;
                    break;
                }
            }
        }
    }

    public void stopRenderThread() {
        renderLock.lock();
        renderStop = true;
        renderLock.unlock();
    }
}
