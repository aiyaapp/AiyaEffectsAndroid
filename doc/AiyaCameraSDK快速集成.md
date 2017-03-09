# 快速集成相机特效
为使用户快速集成AiyaCameraSDK功能，AiyaCameraSDK提供了CameraView和AiyaController两个类。CameraView是使用Camera1 API配合GLSurfaceView提供相机预览功能。而AiyaController只关注对图像流的处理，数据源和视图由用户指定。使用AiyaController也可快速实现CameraView的所有功能。
## 使用AiyaController集成
### 1、导入Module
目前AiyaCameraSDK暂不支持仓库自动集成，只能手动集成。使用AiyaCameraSDK需先导入AiyaCameraSDK的Module，然后在需要用到AiyaCameraSDK的项目Module中增加对AiyaCameraSDK的依赖。

### 2、AndroidManifest.xml文件配置
使用AiyaCameraSDK，必须在App Module中添加：
```xml
<uses-feature android:glEsVersion="0x00020000" android:required="true"/>

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
其他权限需要根据App自身需求添加。Android6.0需要动态申请权限，具体请参照Android官网。

### 3、在布局中加入展示视图
通常情况下，用户是需要预览SDK处理效果的，如果用户不需要预览，而是期望直接将图像数据处理后编码或者推流等处理，可忽略此步骤。
展示视图一般为TextureView、SurfaceHolder或其他可提供SurfaceTexture、Surface、SurfaceHolder的View。提供一个View，在Java代码中动态创建Surface并绑定到View上，也是可以的，但是并不建议这样做。
```xml
<SurfaceView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/mSurfaceView" />
```
### 4、初始化
初始化调用`AiyaCameraEffect.getInstance().init(final Context context, final String licensePath)`，第一个参数为App的Context，第二个参数为AiyaCameraSDK的license文件路径,第三个参数为当前应用的appKey。在初始化过程中，会进行鉴权，若鉴权失败，则AiyaCameraSDK无法正常运行。所以建议在开始初始化前，为AiyaCameraEffect注册状态监听器，监听初始化状态，示例如下：
```java
final StateObserver observer=new StateObserver() {
    @Override
    public void onStateChange(State state) {
        Log.e("state-->"+state.getMsg());
        if(state==State.RESOURCE_READY){
            initEffectMenu();
        }else if(state==State.INIT_FAILED){
            Toast.makeText(CameraActivity.this, "注册失败，请检查网络", Toast.LENGTH_SHORT)
                .show();
        }
        Log.e("onState Change finish");
    }
};
AiyaCameraEffect.getInstance().registerObserver(observer);
AiyaCameraEffect.getInstance().init(this,getFilesDir().getAbsolutePath(),appKey);
```
常见状态如下：
- RESOURCE_READY 资源准备成功
- INIT_SUCCESS 初始化成功
- RESOURCE_FAILED 资源准备失败
- INIT_FAILED 初始化失败

### 5、提供数据源
AiyaController可以接受以SurfaceTexture共享出来的数据流，在Demo中，提供了包括Camera1 API、Camera2 API、视频流三类数据源示例，分别为Camera1Model、Camera2Model和MediaModel。用户自行实现数据源时，主要需要以下步骤：

- 1、 实现AiyaModel接口，在attachToController方法中，给AiyaController增加Renderer监听。
```java
public class SampleModel implements AiyaModel{

    private AiyaController mController;

    @Override
    public void attachToController(final AiyaController controller) {
        //do something...
        this.mController=controller;
        controller.setRenderer(new SampleRender());
    }
}
```

- 2、 实现Renderer。
```java
private class SampleRender implements Renderer{
        @Override
        public void onDestroy() {
            //在此处释放销毁数据源
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //在此处创建数据源
            //......
            //务必在此处设置将数据源中图像的大小告知AiyaController
            //controller.setDataSize(width,height);
            //对于相机需要将相机ID作为ImageDirection告知AiyaController
            //controller.setImageDirection(cameraId);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //数据源图像大小改变
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            //如果数据需要回收利用，在此处执行
        }
}
```

### 6、使用AiyaController处理数据并展示
以Camera1Model提供数据、SurfaceView展示处理结果为例。取得SurfaceView后，通过`getHolder()`方法，取得SurfaceView的SurfaceHolder。然后给SurfaceHolder增加回调，并在回调中，做相应处理：
```java
mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //调用AiyaController的surfaceCreated(holder)方法
        //将数据源附加到AiyaController中
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //调用AiyaController的surfaceChanged方法
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //调用AiyaController的surfaceDestroyed方法
    }
});
```

### 7、生命周期相关处理
同使用SurfaceView、GLSurfaceView类似，使用AiyaController也需要根据Activity或者Fragment等的生命周期，做相应的处理：
```java
@Override
protected void onResume() {
    super.onResume();
    if(mAiyaController!=null){
        mAiyaController.onResume();
    }
}

@Override
protected void onPause() {
    super.onPause();
    if (mAiyaController!=null){
        mAiyaController.onPause();
    }
}

@Override
protected void onDestroy() {
    super.onDestroy();
    if(mAiyaController!=null){
        mAiyaController.destroy();
    }
}
```

### 8、其他处理
AiyaController更多API:
```java
//设置特效
public void setEffect(String effect);
//设置美颜等级，0-6
public void setFairLevel(int level);
//视图大小和数据源图像大小不同时，图像的展示方式
public void setShowType(int type);
//设置处理完成后的回调和回调图像的大小
public void setFrameCallback(int width,int height,FrameCallback frameCallback);
//开始持续回调，录制、推流等使用
public void startRecord();
//停止持续回调
public void stopRecord();
//回调一帧数据，拍照、截屏等使用
public void takePhoto();
```

## 使用CameraView集成
使用CameraView集成与使用AiyaController类似，比AiyaController更简单。前四步基本一样，第三步将布局中的SurfaceView替换为CameraView。然后获取CameraView实例后，就可以给相机增加特效处理、美颜处理等。

### 1、设置特效和美颜
使用CameraView可以快速的得到一个相机的预览。
当你需要为预览中的人脸增加特效时，可调用CameraView的setEffect方法来指定特效的文件路径，如`mCameraView.setEffect(path);`。当设置路径不存在或者为null时，将不会显示任何贴纸效果。
利用`mCameraView.setFairLevel(mBeautyFlag);`可以快速的设置美颜效果，美颜等级为1-6级。当美颜等级被设置不在此范围中时，为关闭美颜的效果。

### 2、拍照和录制
使用CameraView可以快速的实现拍照和视频录制的功能。无论拍照和使录制视频，我们都需要为CameraView设置帧回调。设置方法为`setFrameCallback(int,int,FrameCallback);`，前两个参数分别为帧数据的宽和高，第三个参数为回调方法。
1. 拍照
调用CameraView的`takePhoto()`方法，当预览帧被读取时，设置的FrameCallback会被回调。回调方法onFrame的第一个参数byte[] bytes为获得的RGBA图片数据,第二个参数为图片的时间戳。
2. 录制
与拍照类似，调用CameraView的`startRecord()`方法后，每一帧数据可读时，FrameCallback将会被回调。FrameCallback回调的时间是不确定的，逐帧回调顺序是确定的。
停止录制，调用`stopRecord()`方法。

## 高级使用
1. 自定义滤镜
在AiyaController和CameraView中可以使用自定义滤镜来实现更多功能，AiyaCameraSDK滤镜基类为AFilter，存在包com.aiyaapp.camera.sdk.filter之下。AiyaCameraSDK中包含了水印滤镜、黑白滤镜、美颜滤镜的示例。需要自定义滤镜，可参照Beauty、GrayFilter、WaterMarkFilter等类进行实现。

2. 使用自定义滤镜
在使用AiyaController或者CameraView时，如果希望在预览和输出中增加其他元素，比如自定义美颜滤镜、复古滤镜、水印等可调用：
```java
/**
 * 增加滤镜
 * @param filter 滤镜
 * @param isBeforeProcess 滤镜是否在加特效前增加
 */
public void addFilter(AFilter filter,boolean isBeforeProcess);
```
第二个参数为true还是false视需求而定，通常美颜滤镜之类的滤镜为true,水印之类的滤镜为false。


# 其他
1. CameraView默认使用前置摄像头，切换摄像头，可调用`mCameraView.switchCamera();`。
2. 尽量避免在FrameCallback的回调方法中做耗时工作，以免阻塞渲染线程。

