/*
 * NativeCache.h
 *
 *  Created on: Jul 18, 2013
 *      Author: akaverin
 */

#ifndef NATIVECACHE_H_
#define NATIVECACHE_H_

class NativeCache {

public:
	static void initInstance(JNIEnv* env) {
		static NativeCache instance(env);
		instancePtr = &instance;
	}

	static NativeCache getInstance() {
		return *instancePtr;
	}

	jfieldID getTsFieldId() const {
		return tsFieldId;
	}

	jfieldID getValuesFieldId() const {
		return valuesFieldId;
	}

	jfieldID getAreaFieldId() const {
		return areaFieldId;
	}

	jfieldID getJerkFieldId() const {
		return jerkFieldId;
	}

	jfieldID getRmsFieldId() const {
		return rmsFieldId;
	}

private:
	NativeCache(JNIEnv* env) {
		jclass clazz = env->FindClass("org/kineticsfoundation/lib/DataItem");

		tsFieldId = env->GetFieldID(clazz, "ts", "D");
		valuesFieldId = env->GetFieldID(clazz, "values", "[F");

		clazz = env->FindClass("org/kineticsfoundation/lib/FilterLibrary");

		jerkFieldId = env->GetFieldID(clazz, "jerk", "D");
		rmsFieldId = env->GetFieldID(clazz, "rms", "D");
		areaFieldId = env->GetFieldID(clazz, "area", "D");
	}

	jfieldID tsFieldId;
	jfieldID valuesFieldId;
	jfieldID jerkFieldId;
	jfieldID rmsFieldId;
	jfieldID areaFieldId;

	static NativeCache* instancePtr;

};

NativeCache* NativeCache::instancePtr = NULL;

#endif /* NATIVECACHE_H_ */
