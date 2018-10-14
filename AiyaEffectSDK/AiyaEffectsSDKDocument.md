# 宝宝特效 AiyaEffectsSDK-v4.0.0alpha Android 使用手册 

## 1.概述
宝宝特效 AiyaEffectsSDK 涵盖Android、iOS两个平台，基于自主研发的人脸识别模块，作为一款动态贴纸和动画特效高效渲染的解决方案。

## 2.工程及SDK说明
此工程为AiyaSDK 使用的示例工程。工程中AiyaSDK包含的功能以Jar包及so库的形式提供，在libs文件夹下。当前版本的AiyaSDK采用模块化设计，当前主要包括：Core、Gift、Track、Beauty及ShortVideo模块。其中Core模块为必须模块，其他模块为功能模块，依赖于Core模块。功能模块之间可以任意组合。
例如Gift+Core模块，可用于直播全屏礼物。Track+Core模块，可用于人脸检测及特征点对齐。Gift+Track+Core模块，可提供脸萌效果等等。

## 集成说明
AiyaSDK的集成与使用主要步骤为：

1. 添加权限
```java
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET" />
```
2. 添加jcenter集成。
```java
    compile 'com.aiyaapp.aiya:AyCore:v4.0.1'
    compile 'com.aiyaapp.aiya:AyEffect:v4.0.1'
    compile 'com.aiyaapp.aiya:AyBeauty:v4.0.1'
    compile 'com.aiyaapp.aiya:AyFaceTrack:v4.0.1'
    compile 'com.aiyaapp.aiya:AyShortVideoEffect:v4.0.1'

```
3. 进入[哎吖宝宝特效](http://www.bbtexiao.com/site/free)官网，提交接入申请，填入applicationId，获取appKey。**applicationId一定不要乱填，确保填入你要集成的项目的applicationId，否则无法通过认证。**

4. AiyaSDK认证。对于所有模块的认证，流程都是一致的。每个模块都有一个入口类，应用中要使用某个模块的功能时，需要调用`AiyaEffects.registerComponent`方法，注册这个模块。先设置监听器，在接收INIT消息时，注册需要使用的模块。AUTH消息为认证结果，如果无法使用特效，很多时候是因为认证失败。特效注册后，认证成功之前默认为模块可用，所以无需等待认证结果，可放心执行其他任务。具体代码如下：
```java
//先设置监听器，后初始化
AiyaEffects.setEventListener(new IEventListener() {
    @Override
    public int onEvent(int i, int i1, String s) {
        if(i==Const.MSG_TYPE_INIT){
            AiyaEffects.registerComponent(AiyaGiftEffect.class);
        }
        Log.d("aiyaapp","MSG(type/ret/info):"+i+"/"+i1+"/"+s);
        return 0;
    }
});
AiyaEffects.init(getApplicationContext(),"your app key");
```
### 使用需要的模块的功能。
1. 以直播礼物为例，只需使用AiyaEffectTextureView即可
```java
   //添加布局
        <com.aiyaapp.aiya.render.AiyaMutilEffectView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/mGift"/>
       
    //初始化使用
         mGift= (AiyaMutilEffectView) findViewById(R.id.mGift);
         mGift.forbidChangeSizeWhenSurfaceRecreate(true);
         mGift.pauseIfSurfaceDestroyed(true);
         mGift.setEffect(AiyaMutilEffectView.Layer.TOP,"assets/modelsticker/gaokongshiai/meta.json");
         
   //释放销毁
       mGift.release();
       
   //添加动画监听
    mGift.setMultiAnimListener(new AiyaMutilEffectView.MultiAnimListener() {
               @Override
               public void onAnimEvent(AiyaMutilEffectView.Layer layer, int i, int i1, String s) {
                   if(i== Const.MSG_TYPE_INFO){
                       if(i1== AiyaGiftEffect.MSG_STAT_EFFECTS_END){
                           Log.e("wuwang","播放完成:"+layer.toString());
                           Log.e("wuwang","isAllAnimEnd:"+mGift.isAllAnimEnd());
                       }else if(i1==AiyaGiftEffect.MSG_STAT_EFFECTS_START){
                           Log.e("wuwang","播放开始:"+layer.toString());
                       }
                   }else if(i==Const.MSG_TYPE_ERROR){
                       Log.e("wuwang","错误："+layer.toString()+"/"+i+"/"+i1+"/"+s);
                   }
               }
           });
        
 ```
 
2. 人脸识别

 ```java
   mTrackFilter=new AyTrackFilter(context);
   mTrackFilter.drawToTexture(texture);
 ```
    
3. 美颜美型集成
```java
   // key: 美型类型(0, AiyaBeauty.TYPE1, AiyaBeauty.TYPE2 ,AiyaBeauty.TYPE3)
   mAiyaBeautyFilter = new AyBeautyFilter(key);
   //美颜等级控制，小数0-1
   mAiyaBeautyFilter.setDegree(degree);
   //美颜处理
   texture=mAiyaBeautyFilter.drawToTexture(texture);
   
   //大眼
    mBigEyeFilter=new AyBigEyeFilter();
    //大眼程度控制，0-100
    mBigEyeFilter.setDegree(degree);
   //大眼处理
    mBigEyeFilter.setFaceDataID(mTrackFilter.getFaceDataID());
    texture=mBigEyeFilter.drawToTexture(texture);
    
   //瘦脸
   mThinFaceFilter=new AyThinFaceFilter();
   //瘦脸程度控制，0-100
   mThinFaceFilter.setDegree(degree);
   //瘦脸处理
    mThinFaceFilter.setFaceDataID(mTrackFilter.getFaceDataID());
    texture=mThinFaceFilter.drawToTexture(texture);
     
 ```
 
4. 短视频特效处理
```java
   //mNowSvClazz:特效类型(如：LazyFilter,SvSpiritFreedFilter,SvShakeFilter等)
   SluggardSvEffectTool.getInstance().processTexture(texture,mWidth,mHeight,mNowSvClazz);
       
```
5. 礼物贴图特效
```java
     mEffectFilter=new AiyaGiftFilter(mContext,mTrackFilter);
     mEffectFilter.setEffect(path);
```
  

6. 具体请进入[哎吖SDKDemo](https://github.com/aiyaapp/AiyaEffectsAndroid/tree/dev)