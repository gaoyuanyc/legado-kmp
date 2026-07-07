// N-API bridge for KMP shared library
// This file exports functions that call into libshared.so via KNOI

#include <napi/native_api.h>
#include <hilog/log.h>

// KNOI init entry point - called from ArkTS side
static napi_value InitBridge(napi_env env, napi_value exports) {
    // KNOI handles the bridge initialization internally
    // This module simply needs to be loaded by ArkTS
    return exports;
}

static napi_module demoModule = {
    .nm_version = 1,
    .nm_filename = nullptr,
    .nm_register_func = InitBridge,
    .nm_modname = "entry",
    .nm_flags = 0,
    .reserved = { 0 },
};

extern "C" __attribute__((constructor)) void RegisterEntryModule(void) {
    napi_module_register(&demoModule);
}
