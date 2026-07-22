cmake_minimum_required(VERSION 3.22.1)

project("spotlocal_audio")

# C++ Audio DSP & Oboe Bridge Library
add_library(
        spotlocal_audio
        SHARED
        native-audio.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        spotlocal_audio
        ${log-lib}
)
