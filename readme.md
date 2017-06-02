# AiyaEffectsSDK

---

[![](https://jitpack.io/v/aiyaapp/AiyaEffectsAndroid.svg)](https://jitpack.io/#aiyaapp/AiyaEffectsAndroid)

[IOS版AiyaEffectsSDK](https://github.com/aiyaapp/AiyaEffectsIOS)

[Android版集成到金山云的示例](https://github.com/aiyaapp/AiyaEffectsWithKSVCAndroid)

[Android版集成到ZegoLive的示例](https://github.com/aiyaapp/AiyaEffectsWithZegoAndroid)

[IOS版集成到金山云的示例](https://github.com/aiyaapp/AiyaEffectsWithKSVCIOS)

[IOS版集成到ZegoLive的示例](https://github.com/aiyaapp/AiyaEffectsWithZegoIOS)

# 1、版本信息
**当前版本：AiyaEffects SDK V3.0.0** 	[查看历史版本](doc/version_info.md)

**功能更新**
- 修复了部分bug
- 资源文件打包
- 人脸识别算法优化
- 加入了更多特效

# 2、运行环境说明
AiyaEffectsSDK minSdkVersion为18，即Android4.3以上可用。

# 3. SDK功能说明
AiyaEffectsSDK可用于相机、图片处理、直播等多种情景，主要功能如下：

- 可提供2D序列帧特效
- face mask特效
- 3D静态特效和动画特效
- 提供多种美颜算法

# 4. 集成说明
为使用户快速集成AiyaEffectsSDK功能，AiyaEffectsSDK提供了CameraView和AiyaController两个类。CameraView是使用Camera1 API配合GLSurfaceView提供相机预览功能。而AiyaController只关注对图像流的处理，数据源和视图由用户指定。使用AiyaController也可快速实现CameraView的所有功能。
## 使用CameraView集成
### 1、导入Module
使用AiyaEffectsSDK需先导入AiyaEffectsSDK的Module，然后在需要用到AiyaEffectsSDK的项目Module中增加对AiyaEffectsSDK的依赖。
AiyaEffectsSDK已经加入jitpack仓库，用户也可以通过仓库的来集成。利用jitpack仓库集成，需要先在settings.gradle中加入jitpack仓库：
```gradle
allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io'}
    }
}
```
然后在需要使用AiyaEffectsSDK的项目build.gradle中加入对AiyaEffectsSDK的依赖：
```gradle
compile 'com.github.aiyaapp:AiyaEffectsAndroid:v2.1.0'
```

### 2、注册appId，获取appKey
进入[官网](http://www.bbtexiao.com/)，申请免费使用AiyaEffectsSDK，注册appId，获取appKey。

### 3、AndroidManifest.xml文件配置
使用AiyaEffectsSDK，必须在App Module中添加：
```xml
<uses-feature android:glEsVersion="0x00020000" android:required="true"/>

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
其他权限需要根据App自身需求添加。Android6.0需要动态申请权限，具体请参照Android官网。

### 4、初始化
初始化调用`AiyaEffects.getInstance().init(final Context context, final String licensePath,final String appKey)`，第一个参数为App的Context，第二个参数为AiyaEffectsSDK的license文件路径，第三个参数为当前应用的appKey。在初始化过程中，会进行鉴权，若鉴权失败，则AiyaEffectsSDK无法正常运行。所以建议在开始初始化前，为AiyaEffects注册状态监听器，监听初始化状态，示例如下：
```java
 final ActionObserver observer=new ActionObserver() {
      @Override
      public void onAction(Event event) {
          if(event.eventType== Event.RESOURCE_FAILED){
              Log.e("resource failed");
              AiyaEffects.getInstance().unRegisterObserver(this);
          }else if(event.eventType== Event.RESOURCE_READY){
              Log.e("resource ready");
          }else if(event.eventType== Event.INIT_FAILED){
              Log.e("init failed");
              Toast.makeText(LoadActivity.this, "注册失败，请检查网络", Toast.LENGTH_SHORT)
                  .show();
              AiyaEffects.getInstance().unRegisterObserver(this);
          }else if(event.eventType== Event.INIT_SUCCESS){
              Log.e("init success");
              setContentView(R.layout.activity_load);
              AiyaEffects.getInstance().unRegisterObserver(this);
          }
      }
  };
  AiyaEffects.getInstance().registerObserver(observer);
  AiyaEffects.getInstance().init(LoadActivity.this,getExternalFilesDir(null)
      .getAbsolutePath()+"/config","");
```
常见状态如下：
- INIT_SUCCESS 初始化成功
- RESOURCE_FAILED 资源准备失败
- INIT_FAILED 初始化失败

### 5、在布局中增加CameraView
```xml
<com.aiyaapp.aiya.widget.CameraView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/mSurfaceView" />
```

### 6、CameraView实例获取及控制
引入AiyaEffectsSDK module、完成AiyaEffectsSDK的初始化、给应用添加相关权限后、增加CameraView视图后就可以获取CameraView实例，并进行特效处理了。处理示例代码如下：
```java
private void initCameraView(){
	//获取cameraview实例
	mCameraView = (CameraView)findViewById(R.id.mCameraView);
	//设置特效，参数为特效的配置文件路径
	mCameraView.setEffect(effectJsonPath);
	//设置美颜等级0-6，0表示不美颜
	mCameraView.setFairLevel(level);
	//设置回调
	mCameraView.setFrameCallback(bmpWidth,bmpHeight,this);

	//默认使用前置摄像头，此方法可切换摄像头
    //mCameraView.switchCamera();
	//触发拍照回调
	//mCameraView.takePhoto();
	//触发录制回调
	//mCameraView.startRecord();
	//停止录制回调
	//mCameraView.stopRecord();
}

@Override
public void onFrame(final byte[] bytes,long time) {
    if(isTakePhoto){
	    //拍照回调
        saveBitmapAsync(bytes,bmpWidth,bmpHeight);
    }else{
	    //编码回调
		mEncoder.feedData(bytes,time);
    }
}

@Override
protected void onResume() {
    super.onResume();
    if(mCameraView!=null){
        mCameraView.onResume();
    }
}

@Override
protected void onPause() {
    super.onPause();
    if(mCameraView!=null){
        mCameraView.onPause();
    }
}

@Override
protected void onDestroy() {
    super.onDestroy();
    if(mCameraView!=null){
        mCameraView.onDestroy();
    }
}
```

## 使用AiyaController集成
使用AiyaController集成与使用CameraView类似，前四步完全一样。相对CameraView，AiyaController更加灵活。初始化AiyaEffectsSDK后，集成AiyaController的步骤如下:

### 1、提供数据源
AiyaController可以接受以SurfaceTexture共享出来的数据流，在Demo中，提供了包括Camera1 API、Camera2 API、视频流三类数据源示例，分别为Camera1Model、Camera2Model和MediaModel。用户可直接使用，也可以根据它们的实现自行定制。它们所做的工作主要为：

- 1、 实现Renderer。
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

-  2、将Renderer的实例设置给Controller：
```java
mRenderer=new SampleRender();
controller.setRenderer(mRenderer)
```

### 2、准备接收处理后数据的视窗
AiyaController给图像流增加特效、美颜处理后的，会将数据输出出来。接收输出的对象可以为Surface、SurfaceTexture、SurfaceHodler或者TextureView。这些对象可以通过Java代码new，也可以在布局中增加相关视图来获得。

### 3、使用AiyaController处理数据并展示
以Camera1Model提供数据、SurfaceView展示处理结果为例。取得SurfaceView后，通过`getHolder()`方法，取得SurfaceView的SurfaceHolder。然后给SurfaceHolder增加回调，并在回调中，做相应处理：
```java
//实例化AiyaController
mAiyaController=new AiyaController(SurfaceHolderActivity.this);
//设置AiyaController的回调，用于获取增加特效后的图像数据
mAiyaController.setFrameCallback(bmpWidth,bmpHeight,SurfaceHolderActivity.this);
//实例化数据源
mCamera1Model=new Camera1Model();
mSurfaceView= (SurfaceView)findViewById(R.id.mSurfaceView);
//取得展示视窗，并增加监听，在监听中调用AiyaController相关方法
mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mNowHolder=holder;
        mAiyaController.surfaceCreated(holder);
        mAiyaModel.attachToController(mAiyaController);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth=width;
        mHeight=height;
        mAiyaController.surfaceChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mAiyaController.surfaceDestroyed();
        mNowHolder=null;
    }
});
```

### 4、生命周期相关处理
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

### 5、其他处理
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


## 高级使用

1. 自定义滤镜
在AiyaController和CameraView中可以使用自定义滤镜来实现更多功能，AiyaEffectsSDK滤镜基类为AFilter，存在包com.aiyaapp.camera.sdk.filter之下。AiyaEffectsSDK中包含了水印滤镜、黑白滤镜、美颜滤镜的示例。需要自定义滤镜，可参照Beauty、GrayFilter、WaterMarkFilter等类进行实现。

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


## 注意事项
1. 尽量避免在FrameCallback的回调方法中做耗时工作，以免阻塞渲染线程。
2. 尽量避免多次初始化AiyaEffectsSDK，在不需要试用AiyaEffectsSDK时，调用release，释放掉AiyaEffectsSDK的资源。
3. 集成文档一定要看。AiyaEffectsSDK里面的东西如果不是必要的话，尽量别改，不然出现了问题，我们都不太好确定是什么问题。如果不是自己要渲染操作，AiyaController+AiyaModel基本都能满足集成。自定义滤镜、增加水印之类的AiyaController也有接口。如果在使用AiyaEffectsSDK中，一定要自己进行渲染操作，就利用AiyaEffectFilter，参照AiyaController来使用。

# 5. 资源说明
贴纸资源制作规范请参照其他相关文档。

# 6. 常见问题
1. **为什么选择特效后却没有特效显示？**
>SDK使用需要传入正确的appid和appkey，其中appid为应用的applicationId,请务必保证appkey与appid的正确，否则特效无法显示，另外请确保你的应用有联网权限。

# 7. 联系方式
邮箱：<liudawei@aiyaapp.com>