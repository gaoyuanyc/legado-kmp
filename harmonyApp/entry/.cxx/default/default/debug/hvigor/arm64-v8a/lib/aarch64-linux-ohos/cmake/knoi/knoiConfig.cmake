if(NOT TARGET knoi::knoi)
    add_library(knoi::knoi SHARED IMPORTED)
    set_target_properties(knoi::knoi PROPERTIES
        IMPORTED_LOCATION "D:/study/KMPDemo/legado-master/harmonyApp/oh_modules/.ohpm/@kuiklybase+knoi@0.0.4/oh_modules/@kuiklybase/knoi/libs/arm64-v8a/libknoi.so")
endif()
