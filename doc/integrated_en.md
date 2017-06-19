# AiyaEffectsSDK

---

[![](https://jitpack.io/v/aiyaapp/AiyaEffectsAndroid.svg)](https://jitpack.io/#aiyaapp/AiyaEffectsAndroid)

[IOS AiyaEffectsSDK](https://github.com/aiyaapp/AiyaEffectsIOS)

[Android AiyaEffectsSDK With KSVC Demo](https://github.com/aiyaapp/AiyaEffectsWithKSVCAndroid)

[Android AiyaEffectsSDK With ZegoLive Demo](https://github.com/aiyaapp/AiyaEffectsWithZegoAndroid)

[IOS AiyaEffectsSDK With KSVC Demo](https://github.com/aiyaapp/AiyaEffectsWithKSVCIOS)

[IOS AiyaEffectsSDK With ZegoLive Demo](https://github.com/aiyaapp/AiyaEffectsWithZegoIOS)

# 1. Version Info
**current version:AiyaEffects SDK V3.0.0** 	[version history](doc/version_info.md)

**update**
- fix some bug
- package effect resource files
- optimize face recognition algorithm
- add more effects

# 2. Runtime Environment
AiyaEffectsSDK minSdkVersion 18(Android4.3+).

# 3. SDK Function Declaration 
AiyaEffectsSDK can be used to camera/picture processing/live telecast and so on. The main functions are as follows:

- provide 2D sequence frame effect
- face mask effect
- 3D static/animation effects 
- a variety of beauty algorithm 

# 4. How To Integrate
For the user to integrate AiyaEffectsSDK functions quickly,AiyaEffectsSDK has provided CameraView and AiyaController. The CameraView has used Camera1API with GLSurfaceView to provide camera preview function.And the AiyaController only focuses on the treatment of image stream. Using AiyaController can also implemente the function of CameraView quickly.
## Use CameraView To Integrate
### 1.Import Module
Using AiyaEffectsSDK needs to import AiyaEffectsSDK module, And then add the dependence on the project which needs to use AiyaEffectsSDK. AiyaEffectsSDK has been added to jitpack repertory. So ,User can also use jitpack repertory to integrate AiyaEffectsSDK.Using jitpack repertory to integrate AiyaEffectsSDK,user needs to add jitpack support in the settings.gradle:
```gradle
allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io'}
    }
}
```
And then add the dependence about AiyaEffectsSDK in the build.gradle on the project:
```gradle
compile 'com.github.aiyaapp:AiyaEffectsAndroid:v3.0.0'
```

### 2. Register AppId And Get AppKey
Enter [AiyaEffectsSDK Official Website](http://www.bbtexiao.com/) , apply to use AiyaEffectsSDK freely. User needs to register appId and get appKey.

### 3. AndroidManifest.xml Configuration
Using AiyaEffectsSDK needs to add the following permissions on the app module:
```xml
<uses-feature android:glEsVersion="0x00020000" android:required="true"/>

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
other permissions depend on the app's requirement.Requesting permissions at run time is required by Android6.0+. More details reference to Android Developer Official Website. 

### 4. Initialization
Invoking `AiyaEffects.getInstance().init(final Context context, final String licensePath,final String appKey)` to initialize AiyaEffectsSDK. The first parameter is  the Context of the app. The second parameter is the path to save the cache of config file.The third parameter is the appKey of the application.
In the initialization processing,AiyaEffectsSDK will carry out authentication.If authentication failed,AiyaEffectsSDK won't run normally.So,register a observer for the AiyaEffectsSDK before initialization is needed.Example code:
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
              Toast.makeText(LoadActivity.this, "register failed,please check the network.", Toast.LENGTH_SHORT)
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
The common state is as follows :
- INIT_SUCCESS initialize success
- RESOURCE_FAILED prepare resource failed
- INIT_FAILED initialize failed

### 5. Add CameraView On The Layout
```xml
<com.aiyaapp.aiya.widget.CameraView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/mSurfaceView" />
```

### 6. CameraView Control
After add CameraView to the layout, user can get the CameraView instance and control it:
```java
private void initCameraView(){
	//get cameraview instance
	mCameraView = (CameraView)findViewById(R.id.mCameraView);
	//set effect,parameter is the path of the effect configuration file
	mCameraView.setEffect(effectJsonPath);
	//set fair level 0-6,0 means does not use fair.
	mCameraView.setFairLevel(level);
	//set frame callback
	mCameraView.setFrameCallback(bmpWidth,bmpHeight,this);

	//default use front camera ,use these method to swich camera
    //mCameraView.switchCamera();
	//take photo
	//mCameraView.takePhoto();
	//start record
	//mCameraView.startRecord();
	//stop record
	//mCameraView.stopRecord();
}

@Override
public void onFrame(final byte[] bytes,long time) {
    if(isTakePhoto){
        saveBitmapAsync(bytes,bmpWidth,bmpHeight);
    }else{
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

## Use AiyaController To Integrate
Using AiyaController to integrate is samilar to using CameraView, former four steps are exactly the same.Compared to CameraView,AiyaController is more flexible. After initialization, using AiyaController as following:

### 1.Provide Data Source
AiyaController can receive the data stream which was shared by SurfaceTexture.In the demo,Camera1 API/Camera2 API and media stream data source examples are supplied.Users can use those directly and also can write by themselves according to those implement.Their main jobs are as following:
- 1. implement Renderer
```java
private class SampleRender implements Renderer{
        @Override
        public void onDestroy() {
			//todo: release data source
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //todo: create data source
            //......
			//must tell the image size of image stream in the data source to the AiyaController 
            //controller.setDataSize(width,height);
			//must tell the direction to the AiyaController,if using camera, parameter is the camera id.
            //controller.setImageDirection(cameraId);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //image size in the image stream changed
        }

        @Override
        public void onDrawFrame(GL10 gl) {
			//if data needs to be recycler, invoking here
        }
}
```

-  2.Add The Instance of Renderer To The Controller
```java
mRenderer=new SampleRender();
controller.setRenderer(mRenderer)
```

### 2.Prepare View To Receive The Processed Data
AiyaController will export data after adding effect to the image stream.The output receiver can be a Surface,a SurfaceTexture,a SurfaceHolder or a TextureView. Those object can be created by Java code and also can be obtained from xml layout.

### 3.Using AiyaController To Process Data And Show It
Taking Camera1Model provides data and SurfaceView show the processed result for example.After obtained SurfaceView instance, use `getHolder()` method to get the SurfaceHolder of the SurfaceView. And then,add callback to the SurfaceHolder, and do the corresponding processing in the callback:
```java
//instance AiyaController
mAiyaController=new AiyaController(SurfaceHolderActivity.this);
//set callback to the AiyaController, used to get processed data
mAiyaController.setFrameCallback(bmpWidth,bmpHeight,SurfaceHolderActivity.this);
//instance data source
mCamera1Model=new Camera1Model();
mSurfaceView= (SurfaceView)findViewById(R.id.mSurfaceView);
//get the show view, add callback to it, and invoke AiyaController method in the callback
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

### 4.Coherent Processing About Lifecycle
Similar to using SurfaceView/GLSurfaceView,Using AiyaController needs to do something according to Activity or Fragment lifecycle:
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

### 5.Other API
More AiyaController API:
```java
//set effect
public void setEffect(String effect);
//set fair level��0-6
public void setFairLevel(int level);
//show type when data size is not same with view size
public void setShowType(int type);
//set image data size of callback frame
public void setFrameCallback(int width,int height,FrameCallback frameCallback);
//start record, used in record or live telecast
public void startRecord();
//stop record
public void stopRecord();
//take photo
public void takePhoto();
```


## Advanced Usage

1.  User-Defined Filter
User-Defined Filter can be used in AiyaController and CameraView to achieve more function.The base class for AiyaEffectsSDK's filter is AFilter,below package of com.aiyaapp.camera.sdk.filter.AiyaEffectsSDK contains watermark/white-black/beauty filter samples. Users can realize by themselves according to Beauty/GrayFilter/WaterMarkFilter and so on.

2. Use User-Defined Filter
If user want to add other filter when using AiyaController or CameraView,Just invoke the method:
```java
/**
 * add filter
 * @param filter filter
 * @param isBeforeProcess does add the filter before processed
 */
public void addFilter(AFilter filter,boolean isBeforeProcess);
```
The second parameter is according to demand.


## Matters Need Attention
1. Avoid to do time-consuming work in the FrameCallback in order to avoid blocking the thread of renderer
2. Avoid to initialize AiyaEffectsSDK multi times and invoke `release()` method to release the AiyaEffectsSDK's resource if does not need to use AiyaEffectsSDK.

# 5. About Resource
Effect resource production specification refer to other documention.

# 6. Frequently Asked Questions
1. **Why no effect showed after effect has been selected**
>Correct appid and appkey are needed by the AiyaEffectsSDK.And appid is the applicationId.Please make sure that the appkey and appid are correct,otherwise, effect won't show.And make sure that the application has network permission.

# 7. Contact Information
Email:<liudawei@aiyaapp.com>