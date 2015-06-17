#!/bin/bash

#The script below will perform next tasks:
# 1. Build Native library with NDK
# 2. Build final APK

echo "---------- Cleaning all resources..."
mvn clean

echo "---------- Running Maven build for Native lib..."
mvn --projects ./kinetics-filter-lib android:ndk-build -P stage

echo "---------- Building all APKs..."
mvn package -P stage
