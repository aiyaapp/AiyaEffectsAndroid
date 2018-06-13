## 项目概述

1. 该项目为AiyaSDK的Android版本，是对之前版本AiyaEffectsSDK的重构，重构后的初始版本为4.0.0.
2. 项目采用模块化设计，在C++端，是以一个模块为核心，包含认证及基础接口，其他模块都依赖于核心模块。在Java端，对各个功能模块进行封装，使Java端能够方便调用。

## 模块说明

当前项目中共有五个模块，各个模块的核心功能都在C++中实现：

- aiyaeffectcore：核心Module，被其他Module所依赖。动态库libAyCoreSdk.so为所以对接JNI的中间层所依赖。aiyaeffectcore编译后会产生libAyCoreSdkJni.so，此库为其他模块中所有JNI层所依赖。
- aiyatrack：人脸检测及特征点对齐模块，依赖核心库。包含libaftk.so及libsimd.so，编译后产生libAyTrack.so，为JNI层。aiyatrack库中，assets下包含config目录，此目录下文件为人脸检测及对齐相关的必要的模型文件。
- aiyagift：人脸特效及礼物特效库，依赖核心库，可与aiyatrack库组合使用。不组合时，只提供礼物特效功能，组合使用在提供礼物特效的功能的基础上，也能提供脸萌功能。包含libassimp.so、libgameplay.so及libayeffects.so库，编译后产生libAyGift.so，为JNI层。
- shadereffect：短视频效果库，依赖核心库。包含libBaseEffects.so、libAiyaAe3dLib.so、libAiyaEffectLib.so及libShortVideo.so四个库。编译后产生libAyShaderEffects.so，为JNI层。
- beauty：美颜库，依赖核心库。包含libBaseEffects.so、libAiyaEffectLib.so及libBeauty.so。编译后产生libAyBeauty.so，为JNI库。在beauty的module中只有libBeauty.so时防止demo编译冲突无法通过，在对外导出时需注意此项。

## Maven集成

- 在app的build.gradle中添加

```java
    allprojects {
        repositories {
            maven { url "https://dl.bintray.com/aiyaapp/sdk" }
        }
    }

```
  
- 添加引用
```java
    compile 'com.aiyaapp.aiya:AyCore:v4.0.0'
    compile 'com.aiyaapp.aiya:AyEffect:v4.0.0'
    compile 'com.aiyaapp.aiya:AyBeauty:v4.0.0'
    compile 'com.aiyaapp.aiya:AyFaceTrack:v4.0.0'
    compile 'com.aiyaapp.aiya:AyShortVideoEffect:v4.0.0'
    compile 'com.wuwang.aavt:aavt:a0.2.0'

```

**另外，aiyagift、shadereffect及beauty这几个库都依赖aavt项目(com.wuwang.aavt:aavt:a0.2.9，渲染框架)，用于渲染用。**
