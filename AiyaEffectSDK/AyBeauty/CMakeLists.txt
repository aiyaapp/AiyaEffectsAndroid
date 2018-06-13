cmake_minimum_required(VERSION 3.4.1)
set(lib-path ${CMAKE_SOURCE_DIR}/../compile/beauty/${ANDROID_ABI})
set(compile-path ${CMAKE_SOURCE_DIR}/../output/core/${ANDROID_ABI})
message(${lib-path})
include_directories(${CMAKE_SOURCE_DIR}/../include)
include_directories(src/main/cpp/include)

aux_source_directory(src/main/cpp cpp_src)
set(all_src ${cpp_src})
add_library(AyBeauty SHARED ${all_src})
target_link_libraries(AyBeauty
                      ${compile-path}/libAyCoreSdk.so
                      ${compile-path}/libAyCoreSdkJni.so
                      ${lib-path}/libBeauty.so
                      ${lib-path}/libFaceprocess.so
                      ${lib-path}/libBaseEffects.so
                      ${lib-path}/libAiyaEffectLib.so)