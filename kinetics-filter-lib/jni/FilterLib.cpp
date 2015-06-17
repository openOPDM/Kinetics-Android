/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include "FilterLib.h"
#include "DspFilters/Butterworth.h"
#include "util/utils.h"
#include "PST/PWT.hpp"
#include "util/NativeCache.h"

//f.d.
void performCalculations(jobject thiz, JNIEnv* env, std::vector<double>& tsVec,
		std::vector<double>& xVec, std::vector<double>& yVec,
		std::vector<double>& zVec);

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void *reserved) {
	JNIEnv* env = 0;
	vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);

	NativeCache::initInstance(env);

	return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL Java_org_kineticsfoundation_lib_FilterLibrary_process(
		JNIEnv* env, jobject thiz, jobjectArray data) {

	jsize size = env->GetArrayLength(data);
	if (size <= 0) {
		return;
	}
	std::vector<double> tsVec, xVec, yVec, zVec;

	NativeCache cache = NativeCache::getInstance();
	for (jsize i = 0; i < size; ++i) {
		jobject item = env->GetObjectArrayElement(data, i);

		jdouble ts = env->GetDoubleField(item, cache.getTsFieldId());
		tsVec.push_back(ts);

		jfloatArray values = reinterpret_cast<jfloatArray>(env->GetObjectField(
				item, cache.getValuesFieldId()));

		jfloat* lockedValues = env->GetFloatArrayElements(values, NULL);

		xVec.push_back(*(lockedValues));
		yVec.push_back(*(lockedValues + 1));
		zVec.push_back(*(lockedValues + 2));

		env->ReleaseFloatArrayElements(values, lockedValues, JNI_ABORT);

		env->DeleteLocalRef(values);
		env->DeleteLocalRef(item);
	}
	performCalculations(thiz, env, tsVec, xVec, yVec, zVec);
}

void performCalculations(jobject thiz, JNIEnv* env, std::vector<double>& tsVec,
		std::vector<double>& xVec, std::vector<double>& yVec,
		std::vector<double>& zVec) {

	double tsSum = 0;
	for (int i = 0; i < tsVec.size() - 1; ++i) {
		tsSum += (tsVec[i + 1] - tsVec[i]);
	}

//double freq = 1. / (double) (tsSum / (tsVec.size() - 1));
	double freq = 1. / (tsVec[1] - tsVec[0]);
	double duration = tsVec.back() - tsVec[0];

	double *axises[3] = { xVec.data(), yVec.data(), zVec.data() };

	Dsp::SimpleFilter<Dsp::Butterworth::LowPass<4>, 3> low_pass;
	low_pass.setup(4, freq, 3.75);
	low_pass.process(xVec.size(), axises);

	NativeCache cache = NativeCache::getInstance();

	double jerk = PWT::JERK(tsVec.begin(), tsVec.end(), xVec.begin(),
			yVec.begin(), zVec.begin());
	env->SetDoubleField(thiz, cache.getJerkFieldId(), jerk);

	double RMS = PWT::RMS(tsVec.begin(), tsVec.end(), xVec.begin(),
			yVec.begin(), zVec.begin());
	env->SetDoubleField(thiz, cache.getRmsFieldId(), RMS);

	double path = PWT::path(tsVec.begin(), tsVec.end(), xVec.begin(),
			yVec.begin(), zVec.begin());
	double dist = PWT::distance(tsVec.begin(), tsVec.end(), xVec.begin(),
			yVec.begin(), zVec.begin());
//    self.meanFrequency = PWT::MF(path, dist, duration);
	double area = PWT::area(dist, duration);
	env->SetDoubleField(thiz, cache.getAreaFieldId(), area);
}

