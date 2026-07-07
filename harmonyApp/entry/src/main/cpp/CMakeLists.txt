cmake_minimum_required(VERSION 3.4.1)
project(entry)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)

add_library(entry SHARED
    napi_bridge.cpp
)

target_link_libraries(entry PUBLIC
    libace_napi.z.so
    libhilog_ndk.z.so
)
