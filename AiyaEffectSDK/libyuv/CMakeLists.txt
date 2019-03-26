# 设置cmake的版本
cmake_minimum_required(VERSION 3.4.1)

# 支持汇编语言编译
enable_language(ASM)

# 导入头文件
include_directories(${CMAKE_SOURCE_DIR}/../libyuv/src/main/include)

# 搜集指定路径下的源文件的文件名，将输出结果列表储存在指定的变量中
aux_source_directory(src/main/cpp cpp_src)
aux_source_directory(src/main/cpp/libyuv cpp_src)

# 将指定的源文件生成目标文件
add_library(libyuv SHARED ${cpp_src})

# 将目标文件与外部库文件进行链接
target_link_libraries(libyuv)
