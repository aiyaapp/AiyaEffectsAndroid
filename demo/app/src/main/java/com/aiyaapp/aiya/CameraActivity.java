package com.aiyaapp.aiya;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.aiyaapp.aiya.adapter.Adapter;
import com.aiyaapp.aiya.cameraTool.AYCameraPreviewListener;
import com.aiyaapp.aiya.cameraTool.AYCameraPreviewWrap;
import com.aiyaapp.aiya.cameraTool.AYPreviewView;
import com.aiyaapp.aiya.cameraTool.AYPreviewViewListener;
import com.aiyaapp.aiya.gpuImage.AYGPUImageConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageContentMode.kAYGPUImageScaleAspectFill;
import static com.aiyaapp.aiya.gpuImage.AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageRotateRight;

public class CameraActivity extends AppCompatActivity implements AYCameraPreviewListener, AYPreviewViewListener, Adapter.OnItemClickListener, SeekBar.OnSeekBarChangeListener {

    Camera camera;
    AYCameraPreviewWrap cameraPreviewWrap;
    AYPreviewView surfaceView;

    AYEffectHandler effectHandler;

    private Adapter adapter;
    private SeekBar seekBar;

    private int mode;
    private Object data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        surfaceView = findViewById(R.id.camera_preview);
        surfaceView.setListener(this);
        surfaceView.setContentMode(kAYGPUImageScaleAspectFill);

        initUI();

        try {
            initData();
        } catch (Exception e) {
            throw new IllegalStateException("读取资源文件发生错误");
        }
    }

    private void initUI() {
        RecyclerView recyclerView = findViewById(R.id.recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new Adapter();
        adapter.setOnItemClickListener(this);

        recyclerView.setAdapter(adapter);

        seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);

        findViewById(R.id.effect_btn).setOnClickListener(v -> {
            adapter.setMode(0);
            seekBar.setProgress(0);
            seekBar.setVisibility(View.INVISIBLE);
        });

        findViewById(R.id.beauty_btn).setOnClickListener(v -> {
            adapter.setMode(1);
            seekBar.setProgress(0);
            seekBar.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.style_btn).setOnClickListener(v -> {
            adapter.setMode(2);
            seekBar.setProgress(0);
            seekBar.setVisibility(View.VISIBLE);
        });
    }

    private void initData() throws IOException {
        String effectDataRootPath = getCacheDir() + File.separator + "effect" + File.separator + "data";
        String effectIconRootPath = getCacheDir() + File.separator + "effect" + File.separator + "icon";

        String styleDataRootPath = getCacheDir() + File.separator + "style" + File.separator + "data";
        String styleIconRootPath = getCacheDir() + File.separator + "style" + File.separator + "icon";

        // 获取特效文件名
        List<String> effectNameList = new ArrayList<>();
        File[] effectRes = new File(effectDataRootPath).listFiles(pathname -> !pathname.getName().startsWith("."));
        for (File file : effectRes) {
            effectNameList.add(file.getName());
        }
        Collections.sort(effectNameList);

        // 获取图标, 名称, 特效路径
        List<Object[]> effectDataList = new ArrayList<>();
        for (String effectName : effectNameList) {

            String effectPath = effectDataRootPath + File.separator + effectName + File.separator + "meta.json";

            // 解析json文件中的name
            StringBuilder stringBuilder = new StringBuilder();
            InputStream inputStream = new FileInputStream(effectPath);
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(isr);
            String jsonLine;
            while ((jsonLine = reader.readLine()) != null) {
                stringBuilder.append(jsonLine);
            }
            reader.close();
            isr.close();
            inputStream.close();

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String iconPath = effectIconRootPath + File.separator + effectName + ".png";
            String name = jsonObject.optString("name");

            effectDataList.add(new Object[]{iconPath, name, effectPath});
        }
        adapter.refreshEffectData(effectDataList);

        // 美颜数据
        List<Object[]> beautyDataList = new ArrayList<>();
        beautyDataList.add(new Object[]{R.mipmap.beautify, "磨皮", "smooth"});
        beautyDataList.add(new Object[]{R.mipmap.beautify, "红润", "ruby"});
        beautyDataList.add(new Object[]{R.mipmap.beautify, "美白", "white"});
        beautyDataList.add(new Object[]{R.mipmap.beautify, "大眼", "bigEye"});
        beautyDataList.add(new Object[]{R.mipmap.beautify, "瘦脸", "slimFace"});
        adapter.refreshBeautifyData(beautyDataList);

        // 滤镜数据
        List<String> styleNameList = new ArrayList<>();
        File[] styleRes = new File(styleDataRootPath).listFiles(pathname -> !pathname.getName().startsWith("."));
        for (File file : styleRes) {
            styleNameList.add(file.getName());
        }
        Collections.sort(styleNameList);

        // 获取图标, 名称, 滤镜路径
        List<Object[]> styleDataList = new ArrayList<>();
        for (String styleName : styleNameList) {
            String iconPath = styleIconRootPath + File.separator + styleName;
            String name = styleName.substring("00".length(), styleName.length() - ".png".length());
            String stylePath = styleDataRootPath + File.separator + styleName;
            styleDataList.add(new Object[]{iconPath, name, stylePath});
        }
        adapter.refreshStyleData(styleDataList);
    }

    // ---------- UI变化 ----------

    @Override
    public void onItemClick(int mode, Object data) {
        this.mode = mode;
        this.data = data;

        if (mode == 0) {
            // 设置特效
            effectHandler.setEffectPath(data.toString());
            effectHandler.setEffectPlayCount(0);

        } else if (mode == 1) {
            // 设置美颜算法类型
            effectHandler.setBeautyType(AyBeauty.AY_BEAUTY_TYPE.AY_BEAUTY_TYPE_3);

        } else if (mode == 2) {
            // 设置滤镜
            effectHandler.setStyle(BitmapFactory.decodeFile(data.toString()));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mode == 1) {
            // 设置美颜
            if ("smooth".contentEquals(data.toString())) {
                effectHandler.setIntensityOfSmooth(progress / 100.f);

            } else if ("ruby".contentEquals(data.toString())) {
                effectHandler.setIntensityOfSaturation(progress / 100.f);

            } else if ("white".contentEquals(data.toString())) {
                effectHandler.setIntensityOfWhite(progress / 100.f);

            } else if ("bigEye".contentEquals(data.toString())) {
                effectHandler.setIntensityOfBigEye(progress / 100.f);

            } else if ("slimFace".contentEquals(data.toString())) {
                effectHandler.setIntensityOfSlimFace(progress / 100.f);

            }

        } else if (mode == 2) {
            // 设置滤镜
            effectHandler.setIntensityOfStyle(progress / 100.f);

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    // ---------- 渲染相关 ----------

    /**
     * 打开硬件设备
     */
    private void openHardware() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        camera = Camera.open(1);
        setCameraDisplayOrientation(this, camera);

        cameraPreviewWrap = new AYCameraPreviewWrap(camera);
        cameraPreviewWrap.setPreviewListener(this);
        cameraPreviewWrap.setRotateMode(kAYGPUImageRotateRight);
        cameraPreviewWrap.startPreview(surfaceView.eglContext);
    }

    /**
     * @see <a
     * href="http://stackoverflow.com/questions/12216148/android-screen-orientation-differs-between-devices">SO
     * post</a>
     */
    public static  void setCameraDisplayOrientation(Context context, Camera mCamera) {
        final int rotationOffset;
        // Check "normal" screen orientation and adjust accordingly
        int naturalOrientation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        if (naturalOrientation == Surface.ROTATION_0) {
            rotationOffset = 0;
        } else if (naturalOrientation == Surface.ROTATION_90) {
            rotationOffset = 90;
        } else if (naturalOrientation == Surface.ROTATION_180) {
            rotationOffset = 180;
        } else if (naturalOrientation == Surface.ROTATION_270) {
            rotationOffset = 270;
        } else {
            // just hope for the best (shouldn't happen)
            rotationOffset = 0;
        }

        int result;

        /* check API level. If upper API level 21, re-calculate orientation. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.hardware.Camera.CameraInfo info =
                    new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(0, info);
            int cameraOrientation = info.orientation;
            result = (cameraOrientation - rotationOffset + 360) % 360;
        } else {
            /* if API level is lower than 21, use the default value */
            result = 90;
        }

        /*set display orientation*/
        mCamera.setDisplayOrientation(result);
    }


    /**
     * 关闭硬件设备
     */
    private void closeHardware() {
        // 关闭相机
        if (camera != null) {
            cameraPreviewWrap.stopPreview();
            cameraPreviewWrap = null;
            camera.release();
            camera = null;
        }
    }

    public void createEffectHandler() {
        effectHandler = new AYEffectHandler(this);
        effectHandler.setRotateMode(AYGPUImageConstants.AYGPUImageRotationMode.kAYGPUImageFlipVertical);
        effectHandler.setIntensityOfSlimFace(0);
        effectHandler.setIntensityOfBigEye(0);
        effectHandler.setEffectPath("");
    }


    public void destroyEffectHandler() {
        if (effectHandler != null) {
            effectHandler.destroy();
            effectHandler = null;
        }
    }

    @Override
    public void cameraVideoOutput(int texture, int width, int height, long timestamp) {
        // 渲染特效美颜
        if (effectHandler != null) {
            effectHandler.processWithTexture(texture, width, height);
        }

        // 渲染到surfaceView
        if (surfaceView != null) {
            surfaceView.render(texture, width, height);
        }
    }


    @Override
    public void createGLEnvironment() {
        openHardware();

        surfaceView.eglContext.syncRunOnRenderThread(this::createEffectHandler);
    }

    @Override
    public void destroyGLEnvironment() {
        closeHardware();

        surfaceView.eglContext.syncRunOnRenderThread(this::destroyEffectHandler);
    }
}
