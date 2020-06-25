package com.aiyaapp.aiya;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.aiyaapp.aiya.animTool.AYAnimRenderThread;
import com.aiyaapp.aiya.animTool.AYAnimView;
import com.aiyaapp.aiya.animTool.AYAnimViewListener;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;
import com.aiyaapp.aiya.gpuImage.AYGPUImageFramebuffer;

import java.io.File;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageContentMode.kAYGPUImageScaleAspectFill;

public class AnimationActivity extends AppCompatActivity {

    AYAnimView animView;
    AYAnimHandler effectHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);

        // 第一个渲染视图
        animView = findViewById(R.id.animation_preview);
        animView.setListener(new AYAnimViewListener() {

            private AYGPUImageFramebuffer inputImageFramebuffer;
            private AYAnimRenderThread renderThread;

            @Override
            public void createGLEnvironment() {
                animView.eglContext.syncRunOnRenderThread(() -> {
                    animView.eglContext.makeCurrent();

                    effectHandler = new AYAnimHandler(AnimationActivity.this);
                    effectHandler.setRotateMode(AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageFlipVertical);
                    effectHandler.setEffectPlayFinishListener(() -> Log.d("AnimationActivity", "当前特效播放完成"));
                });

                renderThread = new AYAnimRenderThread();
                renderThread.renderHandler = () -> animView.eglContext.syncRunOnRenderThread(() -> {
                    animView.eglContext.makeCurrent();

                    int width = 1080;
                    int height = 1920;

                    if (inputImageFramebuffer == null) {
                        inputImageFramebuffer = new AYGPUImageFramebuffer(width, height);
                    }

                    inputImageFramebuffer.activateFramebuffer();

                    glClearColor(0, 0, 0, 0);
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
                renderThread.start();
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
        });
        animView.setContentMode(kAYGPUImageScaleAspectFill);

        findViewById(R.id.animation_play_bt).setOnClickListener(v -> {
            Button button = (Button) v;
            if (button.getText().equals("play")) {
                // 设置特效
                effectHandler.setEffectPath(getCacheDir() + File.separator + "effect" + File.separator + "data" + File.separator + "gaokongshiai" + File.separator + "meta.json");
                effectHandler.setEffectPlayCount(1);
                button.setText("stop");
            } else if (button.getText().equals("stop")) {
                // 设置特效
                effectHandler.setEffectPath("");
                button.setText("play");
            }
        });
    }

}
