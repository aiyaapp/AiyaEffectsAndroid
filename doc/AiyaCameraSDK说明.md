AiyaCameraSDK 说明文档

# 1、版本信息
最新版本 V2.0.0

AiyaCamera SDK V2.0.0
>
**功能更新**
- 废弃AiyaCameraView，增加CameraView，提供设置CameraController接口，使用户可以自由设置Camera参数（Camera1 API）。
- 增加AiyaController和AiyaModel类，将图像数据源、特效处理及展示视图分离。支持Camera2 API、视频特效处理。

AiyaCamera SDK V1.3.1
>
**功能更新**
- 去除Drawer概念，将Drawer并入到Filter概念中
- 增加AiyaEffectFilter，提升集成速度及灵活度

AiyaCamera SDK V1.3.0
>
**功能更新**
- 提高美颜性能，优化美白效果

AiyaCamera SDK V1.2.3
>
**功能更新**
- 优化预览，提高预览帧率
- 优化美颜效果

AiyaCamera SDK V1.2.2
>
**功能更新：**
- 将Track和Process封装成Filter，使SDK在GPUImage中可以快速集成

AiyaCamera SDK V1.2.1
>
**功能更新：**
- 增加对Assets中特效选取的功能
- 扩展AiyaCameraView，允许自定义Filter

AiyaCameraSDK V1.1.3
>
**功能更新：**
- 修复有些贴纸图片没显示的问题
- 支持新的贴纸位置

AiyaCamera SDK V1.1.0
>
**功能更新：**
- 增加扩展设置接口
- 优化贴纸流程
- 增加美颜功能
- 增加快速集成相机功能

AiyaCamera SDK V1.0.0
>
**功能更新：**
完成贴纸效果

# 2、运行环境说明
AiyaCameraSDK minSdkVersion为15，即Android4.0以上可用。

# 3. SDK功能说明
AiyaCameraSDK的主要功能是定位图像数据中的人脸，为人脸增加有趣的贴纸效果，可用于相机、图片处理、直播等多种情景。

# 4、架构设计
AiyaCameraSDK（后面简写为SDK）的技术核心是人脸追踪和特效渲染。SDK总流程图如下：

![这里写图片描述](http://img.blog.csdn.net/20170308144015160?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvanVuemlh/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

SDK架构分为三层：
1. **最底层**是核心接口及围绕核心接口形成的单例类。SDK的功能是据此为基础实现的。对外主要包括AiyaCameraEffect及AiyaEffect两个单例类，分别用于人脸有关的特效和人脸无关的特效。
2. **中间层**为一系列的Filter，Filter类是利用OpenGL来进行图像的渲染和处理，他们都继承于AFilter类。这些Filter可以很方便的运用于其他诸如GPUImage之类的框架中，也可直接在GL环境中进行调用。用户可以根据这些Filter进行扩展，自定义实现丰富的功能。
3. **最外层**是SDK的功能控件，这些功能控件使用户可以更快速的将SDK集成到项目中。主要包括CameraView(数据、处理、输出一体)和AiyaController(剥离了数据和视图)。

# 5、API 说明
## 类图
SDK主要类图如下：
![这里写图片描述](http://img.blog.csdn.net/20170308143950566?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvanVuemlh/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
## 类说明
- Filter类都是继承AFilter，其中NoFilter和OesFilter为最基础的实现，OesFilter用来接收处理诸如视频、相机的会不断变化的图像流，NoFilter用于接收处理其他2D图像。
- PrepareFilter调用了AiyaCameraEffect的track(用于人脸捕获)方法。ProcessFilter调用了AiyaCameraEffect的process(用于特效处理)方法，而track方法必须和process方法配合使用，所以PrepareFilter也必须和ProcessFilter方法配合使用。
- AiyaEffectFilter调用了PrepareFilter及ProcessFilter，并依据SDK处理流程做出了实现。而且在process前后都可以添加filter，所以通常情况下，使用AiyaEffectFilter就可以实现SDK功能的高度自定义。
- 作为SDK架构的最外层，CameraView内部调用的是AiyaEffectFilter，相机应用可直接使用CameraView进行集成，直播主播端、视频通话等，也可直接使用AiyaCameraView。
- **2.0版本SDK在AiyaEffectFilter的基础上增加了AiyaController和AiyaModel类，将图像数据源、特效处理及展示视图进行了剥离，用户可使用AiyaController和AiyaModel类，自由配置图像的数据源及展示视图来使用SDK的特效功能。**

## API说明
SDK的主要入口为单例模式的AiyaCameraEffect及单例模式的AiyaEffect。如果使用的特效与人脸相关的，请使用AiyaCameraEffect。使用的特效和人脸无关，请使用AiyaEffect。AiyaCameraEffect的主要API及参数如下（AiyaEffect的API与AiyaCameraEffect相近，相比AiyaCameraEffect少了track接口）：
```java
/**
* SDK 初始化
* @param context 上下文
* @param licensePath license绝对路径
*/
void init(final Context context, final String licensePath, String appKey);

/**
* 设置贴纸特效路径
* @param effectPath 贴纸特效的绝对路径
*/
void setEffect(String effectPath);

/**
* 设置参数
* @param key 参数名
* @param value 参数值
*/
void set(String key, int value);

void set(String key, Object obj);

/**
* 人脸追踪，必须在GL环境中调用
* @param trackData 需要追踪的原始图片数据
* @param info 追踪结果
*/
void track(byte[] trackData, float[] info, int trackIndex);

/**
* 处理图片数据，必须在GL环境中调用
* @param textureId   纹理Id
* @param trackIndex
*/
void process(int textureId, int trackIndex);

/**
* 设置图片数据处理回调
* @param callback 回调
*/
void setProcessCallback(ProcessCallback callback);

/**
* 设置人脸捕捉回调
* @param callback
*/
void setTrackCallback(TrackCallback callback);

/**
* 注册状态观察者
* @param observer
*/
void registerObserver(StateObserver observer);

/**
* 删除已注册的状态观察者
* @param observer
*/
void unRegisterObserver(StateObserver observer);

/**
* 停止贴纸特效
*/
@Deprecated
void stopEffect();

/**
* 获取参数
* @param key 参数的KEy
* @return 参数值
*/
int get(String key);

/**
* 释放SDK资源
*/
void release();
```
其中set和get方法的Key主要有：
```java
String SET_BEAUTY_LEVEL="beauty_level";     //美颜等级，1-6，不在范围内表示关闭美颜
String SET_EFFECT_ON="effects_on";          //特效开关1开0关

String SET_IN_WIDTH="in_width";             //处理图片的输入宽度
String SET_IN_HEIGHT="in_height";           //处理图片的输入高度
String SET_TRACK_WIDTH="track_width";       //track图片的宽度
String SET_TRACK_HEIGHT="track_height";     //track图片的高度

String SET_OUT_WIDTH="out_width";           //输出宽度
String SET_OUT_HEIGHT="out_height";         //输出高度

String SET_ASSETS_MANAGER="assets_manager"; //AssetsManager
```

# 6、集成说明
## 1、导入Module
目前AiyaCameraSDK暂不支持仓库自动集成，只能手动集成。使用AiyaCameraSDK需先导入AiyaCameraSDK的Module，然后在需要用到AiyaCameraSDK的项目Module中增加对AiyaCameraSDK的依赖。

## 2、AndroidManifest.xml文件配置
使用AiyaCameraSDK，必须在App Module中添加：
```xml
<uses-feature android:glEsVersion="0x00020000" android:required="true"/>

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
其他权限需要根据App自身需求添加，如AiyaCameraSDK用于相机应用，则应添加Camera权限等。Android6.0需要动态申请权限，具体请参照Android官网。

## 3、初始化
初始化调用`AiyaCameraEffect.getInstance().init(final Context context, final String licensePath)`，第一个参数为App的Context，第二个参数为AiyaCameraSDK的license文件路径,第三个参数为当前应用的appKey。在初始化过程中，会进行鉴权，若鉴权失败，则AiyaCameraSDK无法正常运行。所以建议在开始初始化前，为AiyaCameraEffect注册状态监听器，监听初始化状态，示例如下：
```java
final StateObserver observer=new StateObserver() {
    @Override
    public void onStateChange(State state) {
        if(state==State.RESOURCE_FAILED){
            Log.e("resource failed");
        }else if(state==State.RESOURCE_READY){
            Log.e("resource ready");
        }else if(state==State.INIT_FAILED){
            Log.e("init failed");
            Toast.makeText(LoadActivity.this, "注册失败，请检查网络", Toast.LENGTH_SHORT)
                .show();
            AiyaCameraEffect.getInstance().unRegisterObserver(this);
        }else if(state==State.INIT_SUCCESS){
            Log.e("init success");
            setContentView(R.layout.activity_load);
            AiyaCameraEffect.getInstance().unRegisterObserver(this);
        }
    }
};
AiyaCameraEffect.getInstance().registerObserver(observer);
AiyaCameraEffect.getInstance().init(LoadActivity.this,getExternalFilesDir(null)
    .getAbsolutePath()+"/146-563-918-415-578-677-783-748-043-705-956.vlc","");
```
常见状态如下：
- RESOURCE_READY 资源准备成功
- INIT_SUCCESS 初始化成功
- RESOURCE_FAILED 资源准备失败
- INIT_FAILED 初始化失败

**建议在Application的onCreate方法中或者在App的入口Activity中对AiyaCameraSDK进行初始化。**

## 4、设置参数
在使用AiyaCameraSdk处理图像数据时，需要指定被处理的图像数据的宽高，设置方法为：
```java
AiyaCameraEffect.getInstance().set(AiyaCameraEffect.SET_IN_WIDTH,dataWidth);
AiyaCameraEffect.getInstance().set(AiyaCameraEffect.SET_IN_HEIGHT,dataHeight);
```

## 5、设置贴纸路径
处理图像数据，为图像数据增加贴纸效果，需要使用`AiyaCameraEffect.getInstance().setEffect(final String effectJsonPath)`指定贴纸效果的配置文件路径。`setEffect(null)`可取消贴纸效果。

## 6、处理图像数据
为指定图像数据增加贴纸效果，调用`AiyaCameraEffect.getInstance().process(int textureId, int trackIndex) `来实现，该方法必须在GLThread中调用。其第一个参数为纹理id，第二个参数为使用track的数据的标记，目前仅支持0和1。在调用该方法前，必须先调用`AiyaCameraEffect.getInstance().track(trackData, infos, textureIndex);`来进行人脸捕捉，为process获取必要的数据。**track和process方法都必须在GL线程中调用。**

## 7、释放SDK占用资源
当不在需要使用贴纸效果时，调用`AiyaCameraEffect.getInstance().release()`来释放SDK占用的资源。若后面需要重新使用贴纸效果，则需要重新初始化。

# 7. 资源说明
贴纸资源制作规范请参照其他相关文档。

# 8. 常见问题
1. 特效绘制流程在AiyaEffectFilter中的draw方法中，如需自行实现，需要保证先调用PrepareFilter的draw方法，再调用ProcessFilter的draw方法。
2. 确保应用有网络权限。非正式license可以试用一分钟特效，一分钟后特效将会失效。
3. SDK中Filter也可与GPUImage配合试用。
4. 尽量避免多次初始化AiyaCameraSDK，在不需要试用AiyaCameraSDK时，调用release，释放掉AiyaCameraSDK的资源。
5. 避免不必要的数据导出。

# 9. License说明
暂无