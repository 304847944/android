LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := jni_mix

LOCAL_SRC_FILES := find_name.cpp get_cpu.cpp get_encrypt.cpp get_decrypt.cpp aes.cpp

LOCAL_CPPFLAGS += -fexceptions
LOCAL_WHOLE_STATIC_LIBRARIES += android_support
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
$(call import-module, android/support)
