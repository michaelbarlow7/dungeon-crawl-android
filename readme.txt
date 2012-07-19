Steps I took:

1. Download Stone Soup source (at time 0.10.2)
2. Create file "Android.mk" with basic template (eg from hello-jni)
3. Added LOCAL_CPP_EXTENSION=.cc
4. Added all ".cc" files in source to LOCAL_SRC_FILES
5. Added "Application.mk" with APP_STL=gnustl_shared
6. Added to Android.mk LOCAL_C_INCLUDES := $(LOCAL_PATH)/rltiles
7. Ran source/util/art-data.pl to generate art-data.h and dc-unrand.txt (and tiledef-unrand.cc?)
8. Added instructions to Android.mk to build sqlite as a static library, added sqlite folder to LOCAL_SRC_FILES,
added LOCAL_STATIC_LIBRARIES:=libsqlite3
9. LUA included from contrib folder:
 a. Added build instruction for lua in Android.mk. 
 b. Added lua folder to LOCAL_SRC_FILES
 c. Added LOCAL_STATIC_LIBRARIES=liblua
10. Added LOCAL_CFLAGS += -frtti since otherwise 'dynamic_cast' can't be called in spl-miscast.h
11. (maybe move after running art-data.pl): ran 'make' in source/rltiles to generate
.cc and .h files. Added these to the source files and LOCAL_C_INCLUDES respectively.
12. Added "-fexceptions" to LOCAL_CFLAGS to enable exceptions (arena.cc had issues)
13. modified crash.cc to not define BACKTRACE_SUPPORTED when Android defined (might change
this to use log.h in this case)
14. Removed libunix.cc from Android.mk, added a highly modified and stubbed version libandroid.cc
15. Ran ./util/gen-mst.pl to generate mon-mst.h
NOTE: seems like all the .pl files in /util need to be run. Might get a shell script happening for this
16. Added ANDROID to list of OSes in syscalls.h:24 to define our fake fdatasync (crash in package.cc:219/226)
17. Removed tilecell.cc. We're not building with tiles right now (crawl before you walk. HA)
18. Actually removed a whole bunch of tile related files, commented in Android.mk
19. ran 'source/util/gen_ver.pl build.h' to generate version header build.h
20. ran 'util/gen-cflg.pl compflag.h none armeabi android android' to generate compflag.h
21. Editted llex.c (around line 181) to allow compiling for android (see http://www.badlogicgames.com/wordpress/?p=943)
22. Added zlib by adding LOCAL_LDLIBS := -lz
23. Editted sqlite3.c as per http://www.androiddiscuss.com/1-android-discuss/23750.html (line 25125)
24. Added prebuilt/levcomp.lex.cc and prebuilt/levcomp.tab.cc and header files in that dir to Android.mk
25 (things I forgot to document to get it running...they're in the changelists somewhere...I hope...D:)
ZOMG COMPILES

==================================
========HOW TO COMPILE============
==================================

So ideally, to build from the checked out code, one would:
1. Run jni/util/art-data.pl
2. Run jni/util/gen-mst.pl
3. Run jni/util/gen-luatags.pl to get dat/dlua/tags.lua
4. Run 'make' in jni/rltiles // Works for me, I'm running Xubuntu 12.04 32bit. You might need to cross compile
5. Run 'jni/util/gen_ver.pl build.h'
6. Run 'jni/util/gen-cflg.pl compflag.h none armeabi android android'
7. Then run $NDK/ndk-build from inside the project directory (assuming you have the NDK. We're using r8 atm).

To actually get the app to run, we also needed to:
8. Symlink source/dat to <android-root>/assets/dat
9. Symlink the docs folder to <android-root>/assets/docs
9. Add the root folder as an android project in eclipse.
10. Run from eclipse

I will eventually put the above stuff in a shell script :)
==================================
====LIST OF SYMLINKS =============
==================================
- Symlink source/ to <android-root>/jni
- Symlink source/dat to <android-root>/assets/dat
- Symlink docs/ to <android-root>/assets/dat (don't need pdfs though)
- Symlink text files in root directoy into docs/

==================================
========LICENSE AND CREDITS=======
==================================

Dungeon Crawl:Stone Soup is made by a bunch of cool people, listed in crawlCREDITS.txt. 
License information for Crawl is in license.txt. Pretty much all the native code 
(except the curses adapter) comes from this codebase.

The android app code (all java files) and the curses adapter (curses.c and curses.h)
are heavily derived from Angdroid (port of Angband for Android). Thanks goes to David Barr,
Sergey Belinsky and Dan Vernon. This part of the code is licensed under GNU GPL v2
(like most of Crawl). The details of this license are included in license.txt.

Thanks also goes to Frederik Farnstrom for his port of Nethack to android for inspiration.
