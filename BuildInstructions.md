# Introduction #
The following are some instructions on how to build the application. It is recommended that you have some experience with Android development, in particular with the Native Development Kit.

Also, I have only tried building the project using Xubuntu 12.04 32-bit and Mac OS X 10.8. Both were successful.

# Steps #
  1. Download the Android SDK. You'll need version 9 (2.3) at least. You'll also want Eclipse and the ADT. Make sure it's all set up and running.
  1. Download <a href='https://www.crystax.net/en/android/ndk'>CrystaX's NDK</a>, not the official one, since Crawl needs support for wide characters. As of the time of writing, we're using [r8](https://code.google.com/p/dungeon-crawl-android/source/detail?r=8).
  1. Clone the repository. The repository contains the actual code for Dungeon Crawl in a submodule, which in turn has submodules, which you'll need to build the code. So, for great justice, clone the code using **git clone --recursive https://michaelbarlow7@code.google.com/p/dungeon-crawl-android/**. If you've cloned it without the --recursive flag, you'll need to get the submodules by entering the directory you just cloned and typing **git submodule update --init --recursive**
  1. Once you've done that, cd into **android-crawl-console** and run setup.sh. This will generate some necessary files and symlinks. In Mac OS X, in order to "make docs" successfully, I needed to modify the Makefile so that any references to "/Developer/SDKs" pointed insted to "/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/".
  1. You should be right to build the native library. Run the **ndk-build** executable from CrystaX's NDK that you downloaded from within the project. This can take up to about half an hour.
  1. Import the project into Eclipse (New Project->Android->Project from existing source, then navigate to the root directory of the project).
  1. Run the project as an Android application. Hopefully, it works!

By reviewing the branch, you should be able to see the modifications I've made to the code. As of September 13, 2012, the following files have been added

  * Android.mk
  * Application.mk
  * main.h

And the following files have been modified
  * crash.cc:22
  * syscalls.h:24
  * contrib/lua/src/llex.c:181
  * contrib/sqlite/sqlite3.c:27246
  * initfile.cc:1635
  * libutill.cc (commented out any ANDROID code referring to SDL)