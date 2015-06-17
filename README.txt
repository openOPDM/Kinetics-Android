This directory contains Android client application for Kinetics POC project.

******* Android NDK *******
Project uses android-ndk-r9d. With never version you will received error message "Can not resolve automatically a toolchain to use. Please specify one."

Also please setup environment variables
- ANDROID_SDK - path to your Android sdk location with installed Android-21,
- ANDROID_NDK_HOME - math to your Android NDK r9d location,
- M2_HOME - path to your Apache Maven 3.0.5 loaction

******* BUILD HOW-TO *******

Our project is based on Apache Maven 3.0.5. So, to build it you need to setup one in your system.

There are 2 scripts to build everything you need. They are based on Bash, so you will have to run them on Bash friendly environment.

build.sh - builds Debug version of application;
build_release.sh - builds Release version with Proguard obfuscation and code signing.
