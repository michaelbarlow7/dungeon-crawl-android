/*
 * File: PreferencesActivity.java Purpose: Preferences activity for Android
 * application
 * 
 * Copyright (c) 2009 David Barr, Sergey Belinsky
 * 
 * This work is free software; you can redistribute it and/or modify it under
 * the terms of either:
 * 
 * a) the GNU General Public License as published by the Free Software
 * Foundation, version 2, or
 * 
 * b) the "Angband licence": This software may be copied and distributed for
 * educational, research, and not for profit purposes provided that this
 * copyright and statement are included in all such copies. Other copyrights may
 * also apply.
 */

package com.crawlmb.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.crawlmb.EditConfigFilePreference;
import com.crawlmb.Preferences;
import com.crawlmb.R;
import com.crawlmb.keymap.KeyMapPreference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PreferencesActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private final class CopySaveDirectoryTask extends
            AsyncTask<String, Void, Boolean> {
        public String TAG = "CopySaveDirectoryTask";
        private String source;
        private String destination;
        private boolean reloadCrawl;

        public CopySaveDirectoryTask(boolean reloadCrawl) {
            this.reloadCrawl = reloadCrawl;
        }

        void deleteRecursive(File fileOrDirectory) {
            if (fileOrDirectory.isDirectory())
                for (File child : fileOrDirectory.listFiles())
                    deleteRecursive(child);

            fileOrDirectory.delete();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            source = params[0];
            destination = params[1];
            deleteRecursive(new File(destination));
            boolean result = copyFileOrDir(source, "");

            return result;
        }

        private boolean copyFileOrDir(String prefix, String fileName) {
            String originalPath = prefix + "/" + fileName;
            File originalPathFile = new File(originalPath);
            File[] listOfFiles = originalPathFile.listFiles();
            if (listOfFiles == null) // then we know it's a file, not a
            // directory
            {
                return copyFile(originalPath, fileName);
            } else {
                String destPath = destination + "/" + fileName;

                File dir = new File(destPath);
                boolean mkdirSuccess = dir.mkdir();
                if (!mkdirSuccess) {
                    return false;
                }

                for (int i = 0; i < listOfFiles.length; i++) {
                    boolean result = copyFileOrDir(prefix, fileName + "/"
                            + listOfFiles[i].getName());
                    if (!result) {
                        return false;
                    }
                }
                return true;
            }
        }

        private boolean copyFile(String originalPath, String fileName) {
            String destname = destination + "/" + fileName;
            Log.d(TAG, "Copying: " + fileName + " to " + destname);
            File destinationFile = new File(destname);
            try {
                destinationFile.createNewFile();

                InputStream in = new FileInputStream(originalPath);
                OutputStream out = new FileOutputStream(destinationFile, false);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException ex) {
                Log.e(TAG, "Exception occured copying " + fileName + ": " + ex);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            removeDialog(DIALOG_COPY_FILES_PROGRESS);
            int toastTextResource;
            if (result) {
                toastTextResource = R.string.files_copied_successfully;
                if (reloadCrawl) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("reloadCrawl", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            } else {
                toastTextResource = R.string.error_copying_files;
            }
            Toast.makeText(PreferencesActivity.this, toastTextResource,
                    Toast.LENGTH_LONG).show();
        }
    }

    private static final int DIALOG_COPY_FILES_PROGRESS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Preferences.NAME);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        setHelpIntent();

        setConfigFilePreferences();

        setCharacterFilesIntent();

        setCustomizeKeyboardIntent();

        addExportMorguePreference();
        addRestoreMorguePreference();

        addExportSavePreference();
        addRestoreSavePreference();
    }

    private void addExportMorguePreference() {
        DialogPreference backupDialogPreference = new DialogPreference(this,
                null) {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Get stuff in edittext, back it up to that directory
                        EditText directoryBox = (EditText) getDialog()
                                .findViewById(R.id.directoryBox);
                        String destination = directoryBox.getText().toString();
                        backupDirectory(destination, "/morgue");
                        break;
                }
            }
        };
        backupDialogPreference.setDialogLayoutResource(R.layout.backup_morgue_dialog);
        backupDialogPreference.setDialogTitle(R.string.backup_morgue_directory);
        backupDialogPreference.setTitle(R.string.backup_morgue_directory);
        backupDialogPreference.setPositiveButtonText(R.string.backup);
        backupDialogPreference.setNegativeButtonText(android.R.string.cancel);

        PreferenceCategory saveFilesCategory = (PreferenceCategory) findPreference("morgue");
        saveFilesCategory.addPreference(backupDialogPreference);
    }

    private void addExportSavePreference() {
        DialogPreference backupDialogPreference = new DialogPreference(this,
                null) {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Get stuff in edittext, back it up to that directory
                        EditText directoryBox = (EditText) getDialog()
                                .findViewById(R.id.directoryBox);
                        String destination = directoryBox.getText().toString();
                        backupDirectory(destination, "/saves");
                        break;
                }
            }
        };
        backupDialogPreference.setDialogLayoutResource(R.layout.backup_saves_dialog);
        backupDialogPreference.setDialogTitle(R.string.backup_save_directory);
        backupDialogPreference.setTitle(R.string.backup_save_directory);
        backupDialogPreference.setPositiveButtonText(R.string.backup);
        backupDialogPreference.setNegativeButtonText(android.R.string.cancel);

        PreferenceCategory saveFilesCategory = (PreferenceCategory) findPreference("saveFiles");
        saveFilesCategory.addPreference(backupDialogPreference);

    }

    private void addRestoreSavePreference() {
        DialogPreference backupDialogPreference = new DialogPreference(this,
                null) {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Get stuff in edittext, restore it from that directory
                        EditText directoryBox = (EditText) getDialog()
                                .findViewById(R.id.directoryBox);
                        String source = directoryBox.getText().toString();
                        restoreDirectory(source, "/saves");
                        break;
                }
            }
        };
        backupDialogPreference.setDialogLayoutResource(R.layout.restore_saves_dialog);
        backupDialogPreference.setDialogTitle(R.string.restore_save_directory);
        backupDialogPreference.setTitle(R.string.restore_save_directory);
        backupDialogPreference.setPositiveButtonText(R.string.restore);
        backupDialogPreference.setNegativeButtonText(android.R.string.cancel);

        PreferenceCategory saveFilesCategory = (PreferenceCategory) findPreference("saveFiles");
        saveFilesCategory.addPreference(backupDialogPreference);

    }

    private void addRestoreMorguePreference() {
        DialogPreference backupDialogPreference = new DialogPreference(this,
                null) {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Get stuff in edittext, restore it from that directory
                        EditText directoryBox = (EditText) getDialog()
                                .findViewById(R.id.directoryBox);
                        String source = directoryBox.getText().toString();
                        restoreDirectory(source, "/morgue");
                        break;
                }
            }
        };
        backupDialogPreference.setDialogLayoutResource(R.layout.restore_morgue_dialog);
        backupDialogPreference.setDialogTitle(R.string.restore_morgue_directory);
        backupDialogPreference.setTitle(R.string.restore_morgue_directory);
        backupDialogPreference.setPositiveButtonText(R.string.restore);
        backupDialogPreference.setNegativeButtonText(android.R.string.cancel);

        PreferenceCategory saveFilesCategory = (PreferenceCategory) findPreference("morgue");
        saveFilesCategory.addPreference(backupDialogPreference);

    }

    private void backupDirectory(String destination, String relativeDir) {
        showDialog(DIALOG_COPY_FILES_PROGRESS);
        new CopySaveDirectoryTask(false).execute(getFilesDir() + relativeDir,
                destination + relativeDir);
    }

    private void restoreDirectory(String source, String relativeDir) {
        showDialog(DIALOG_COPY_FILES_PROGRESS);
        new CopySaveDirectoryTask(true).execute(source + relativeDir,
                getFilesDir() + relativeDir);
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_COPY_FILES_PROGRESS:
                ProgressDialog filesCopyingDialog = new ProgressDialog(this);
                filesCopyingDialog.setMessage(getString(R.string.copying_files));
                return filesCopyingDialog;
            default:
                break;
        }
        return null;
    }

    private void setHelpIntent() {
        Preference helpPreference = findPreference("help");
        Intent helpIntent = new Intent(this, HelpActivity.class);
        helpPreference.setIntent(helpIntent);
    }

    private void setCharacterFilesIntent() {
        Preference characterFilesPreference = findPreference("character_files");
        Intent characterFilesIntent = new Intent(this,
                CharacterFilesActivity.class);
        characterFilesPreference.setIntent(characterFilesIntent);
    }

    private void setCustomizeKeyboardIntent() {
        Preference characterFilesPreference = findPreference("custom_keyboard");
        Intent characterFilesIntent = new Intent(this,
                CustomKeyboardActivity.class);
        characterFilesPreference.setIntent(characterFilesIntent);
    }

    private void setConfigFilePreferences() {
        PreferenceCategory configFilePreferences = (PreferenceCategory) findPreference("configFiles");
        String[] configFiles = getResources().getStringArray(
                R.array.config_files);
        for (int i = 0; i < configFiles.length; i++) {
            EditConfigFilePreference editConfigFilePreference = new EditConfigFilePreference(
                    this, configFiles[i]);
            configFilePreferences.addPreference(editConfigFilePreference);
        }

        // The macro file is a special case. We should only show this setting if
        // the file exists
        File macroFile = new File(getFilesDir() + "/settings/macro.txt");
        if (macroFile.exists()) {
            EditConfigFilePreference editConfigFilePreference = new EditConfigFilePreference(
                    this, "macro");
            configFilePreferences.addPreference(editConfigFilePreference);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setSummaryAll(getPreferenceScreen());
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        SharedPreferences pref = getSharedPreferences(Preferences.NAME,
                MODE_PRIVATE);

        if (pref.getBoolean(Preferences.KEY_FULLSCREEN, true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setSummaryAll(PreferenceScreen pScreen) {
        for (int i = 0; i < pScreen.getPreferenceCount(); i++) {
            Preference pref = pScreen.getPreference(i);
            setSummaryPref(pref);
        }
    }

    public void setSummaryPref(Preference pref) {
        if (pref == null)
            return;

        String key = pref.getKey();
        if (key == null)
            key = "";

        if (pref instanceof KeyMapPreference) {
            KeyMapPreference kbPref = (KeyMapPreference) pref;
            String desc = kbPref.getDescription();
            pref.setSummary(desc);
        } else if (pref instanceof PreferenceCategory) {
            PreferenceCategory prefCat = (PreferenceCategory) pref;
            int count = prefCat.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                setSummaryPref(prefCat.getPreference(i));
            }
        } else if (pref instanceof PreferenceScreen) {
            setSummaryAll((PreferenceScreen) pref);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.compareTo(Preferences.KEY_ACTIVEPROFILE) == 0
                || key.compareTo(Preferences.KEY_PROFILES) == 0) {
            setSummaryAll(getPreferenceScreen());
        } else {
            Preference pref = findPreference(key);
            setSummaryPref(pref);
        }
    }

}
