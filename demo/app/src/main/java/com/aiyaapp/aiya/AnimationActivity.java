package com.aiyaapp.aiya;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.aiyaapp.aiya.animTool.AYAnimRenderHandler;
import com.aiyaapp.aiya.animTool.AYAnimRenderThread;
import com.aiyaapp.aiya.animTool.AYAnimView;
import com.aiyaapp.aiya.animTool.AYAnimViewListener;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;
import com.aiyaapp.aiya.gpuImage.GPUImageCustomFilter.AYGPUImageEffectPlayFinishListener;

import java.util.concurrent.locks.ReentrantLock;

import static android.opengl.GLES20.*;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageContentMode.kAYGPUImageScaleAspectFill;

public class AnimationActivity extends AppCompatActivity implements AYAnimViewListener, AYAnimRenderHandler, AYGPUImageEffectPlayFinishListener {

    AYAnimView animView;

    AYAnimHandler effectHandler;

    AYGPUImageFramebuffer inputImageFramebuffer;

    AYAnimRenderThread renderThread;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_animation);
        animView = findViewById(R.id.animation_preview);

        animView.setListener(this);
        animView.setContentMode(kAYGPUImageScaleAspectFill);

        findViewById(R.id.animation_play_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                if (button.getText().equals("play")) {
                    // 设置特效
                    effectHandler.setEffectPath(getExternalCacheDir() + "/aiya/effect/shiwaitaoyuan/meta.json");
                    effectHandler.setEffectPlayCount(0);
                    button.setText("stop");
                } else if (button.getText().equals("stop")) {
                    // 设置特效
                    effectHandler.setEffectPath("");
                    button.setText("play");
                }
            }
        });
    }

    @Override
    public void createGLEnvironment() {
        animView.eglContext.syncRunOnRenderThread(() -> {
            animView.eglContext.makeCurrent();

            effectHandler = new AYAnimHandler(AnimationActivity.this);
            effectHandler.setEffectPlayFinishListener(this);
        });

        renderThread = new AYAnimRenderThread();
        renderThread.renderHandler = this;
        renderThread.start();
    }

    @Override
    public void render() {
        animView.eglContext.syncRunOnRenderThread(() -> {
            animView.eglContext.makeCurrent();

            int width = 1080;
            int height = 1920;

            if (inputImageFramebuffer == null) {
                inputImageFramebuffer = new AYGPUImageFramebuffer(width, height);
            }

            inputImageFramebuffer.activateFramebuffer();

            glClearColor(0,0,0,0);
            glClear(GL_COLOR_BUFFER_BIT);

            // 渲染特效美颜
            if (effectHandler != null) {
                effectHandler.processWithTexture(inputImageFramebuffer.texture[0], width, height);
            }

            // 渲染到surfaceView
            if (animView != null) {
                animView.render(inputImageFramebuffer.texture[0], width, height);
            }
        });
    }

    @Override
    public void destroyGLEnvironment() {

        if (renderThread != null) {
            renderThread.stopRenderThread();
            renderThread = null;
        }

        animView.eglContext.syncRunOnRenderThread(() -> {
            animView.eglContext.makeCurrent();

            if (effectHandler != null) {
                effectHandler.destroy();
                effectHandler = null;
            }

            if (inputImageFramebuffer != null) {
                inputImageFramebuffer.destroy();
                inputImageFramebuffer = null;
            }
        });
    }

    @Override
    public void playFinish() {
        Log.d("AnimationActivity", "当前特效播放完成");
    }
}
