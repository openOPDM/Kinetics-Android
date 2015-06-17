# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

# DSPFilter library, build statically
#
include $(CLEAR_VARS)

LOCAL_MODULE    := dspfilter

LOCAL_C_INCLUDES := $(LOCAL_PATH)/DSPFilters/include
LOCAL_SRC_FILES := DSPFilters/source/Butterworth.cpp DSPFilters/source/Cascade.cpp DSPFilters/source/Biquad.cpp \
					DSPFilters/source/PoleFilter.cpp

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/DSPFilters/include

include $(BUILD_STATIC_LIBRARY)

# JNI module, the main one
#
include $(CLEAR_VARS)

LOCAL_MODULE    := kinetics-filter-lib
LOCAL_SRC_FILES := FilterLib.cpp

LOCAL_STATIC_LIBRARIES := dspfilter pst

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)


