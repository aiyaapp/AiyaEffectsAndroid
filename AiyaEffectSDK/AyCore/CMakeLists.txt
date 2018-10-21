# 设置cmake的版本
cmake_minimum_required(VERSION 3.4.1)

# 导入头文件
include_directories(${CMAKE_SOURCE_DIR}/../AyCore/src/main/include)
include_directories(${CMAKE_SOURCE_DIR}/../AyBeauty/src/main/include)
include_directories(${CMAKE_SOURCE_DIR}/../AyEffect/src/main/include)
include_directories(${CMAKE_SOURCE_DIR}/../AyFaceTrack/src/main/include)
include_directories(${CMAKE_SOURCE_DIR}/../AyShortVideoEffect/src/main/include)

# 搜集指定路径下的源文件的文件名，将输出结果列表储存在指定的变量中
aux_source_directory(src/main/cpp cpp_src)

# 将指定的源文件生成目标文件
add_library(AyCoreJni SHARED ${cpp_src})

# 将目标文件与外部库文件进行链接
target_link_libraries(AyCoreJni
                        ${CMAKE_SOURCE_DIR}/../AyCore/libs/${ANDROID_ABI}/libAiyaEffectLib.so
                        ${CMAKE_SOURCE_DIR}/../AyCore/libs/${ANDROID_ABI}/libBaseEffects.so
                        ${CMAKE_SOURCE_DIR}/../AyCore/libs/${ANDROID_ABI}/libAyCoreSdk.so)
