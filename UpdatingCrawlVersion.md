The aim of this project is to keep up with official stable release of Dungeon Crawl: Stone Soup. Since a clone of the official project is added a submodule, we use git to update this code, and then make sure it compiles and runs within our environment. The following outlines the process for updating your version of Dungeon Crawl:Stone Soup from the official repositories.

# How to update crawl #
  1. cd to the submodule directory (if submodules have been downloaded properly it should be in `<root-dir>`/android-crawl-console)
  1. Checkout the master branch using `git checkout master`
  1. Pull in the latest version of the official repository, including tags, using `git pull -t <repo>`. At the time of writing, the official repository is at **git://gitorious.org/crawl/crawl.git**
  1. Run `git tag` and look for the version of Crawl that you're after. It should typically be named the same as the version you want (for example "0.11.0")
  1. Checkout the android branch using `git checkout android`
  1. Merge the tag you wanted into this branch. This can be tricky, since branches can have very old common ancestors and there might be conflicts. See the Merge Process section for an outline on how best to do this
  1. Run the shell script at the base directory. Might need to edit it to generate more things or whatever
  1. Run a build. You may get some build errors here.
  1. Try running the program!

Once it's running successfully, we want to commit and push our Crawl submodule changes, and then we want to commit and push this submodule change within our main repository as well:

  1. Whilst within the submodule directory (android-crawl-console), ensure all proper modifications are added, and commit.
  1. Tag HEAD as '`<version>-android`'
  1. Push these changes to the gitorious repository (`git push git@gitorious.org:~barbs/crawl/android-crawl-console.git android` should work here).
  1. Run source/util/gen\_ver.pl again, then do another build, so that the build version changes correctly
  1. Update the "totalFiles" variable in CrawlAppActivity.java to the number of files actually copied when running the app (this gets logged).
  1. Change the minimum install version in CrawlAppActivity (might change this to just get the versionCode dynamically
  1. Change to the root (android) directory, add and commit and the submodule changes.
  1. Push these changes to the Google Code repository.

## Merge Process ##
  1. Ensure you've checkout the android branch
  1. Merge in the tag, using the recursive-theirs strategy (mostly, we want the tag's changes to the files, though we'll need to take care our changes haven't been overwritten). Run `git merge -s recursive -X theirs <tag> --no-commit`. If you get merge errors at this stage, that's ok.
  1. Add any new relevant .cc files to Android.mk. Typing `git status` will show any new files. Not sure if this is the best way to do it.
  1. Check if anything has happened to our Crawl specific modifications. Android.mk, Application.mk and main.h should still exist. The following files have modifications: crash.cc:22, contrib/lua/src/llex.c:181, contrib/sqlite/sqlite3.c:27246, initfile.cc:1635. Note that, since the tiles project has now been merged into trunk, we might not need as many of our own changes.
  1. Remove any .cc files from Android.mk that have since been deleted.
  1. Check for any strange merge problems. Sometimes the merge algorithm used can produce strange results, such as duplicating functions in a file. The best way to catch these is to type `git diff --name-only <tag> --`. Our crawl specific files, our .gitmodules, .gitignore files etc should be listed. If you find any that look out of place, check its diff by typing `git diff <tag> -- <file>`.  We'll usually want to get the `<tag>` version of the file, which we get by going `git checkout <tag> <file>`. If you miss any files at this stage, the compiler should pick them up anyway

## Updating source ##
  1. Update the crawl code by navigating to the directory and executing "git push git@gitorious.org:~barbs/crawl/android-crawl-console.git"
  1. Assuming you've set it up correctly, you should be able to update the app code by navigating to the base directory and going "git push"
## Releasing new apks ##
  1. Don't forget to update the versionCode and versionName in the manifest. versionCode is an integer simply incremented by 1, versionName is the same as the Crawl version, with a letter that is incremented for additional updates of the app
  1. Add a commit signifying the release version. The option "--allow-empty" might be handy here
  1. Export the application, using my keystore. The filename should use the same format (e.g. `CrawlApp-0-11-0.apk`)
  1. Upload the apk to this site. Add release notes here
  1. Update the current version on the home page
  1. Upload the apk to Google Play. Don't forget to add release notes and activate.