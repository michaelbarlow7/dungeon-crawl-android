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

package com.crawlmb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceCategory;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{

	private final class CopySaveDirectoryTask extends AsyncTask<String, Void, Boolean>
  {
    public String TAG = "CopySaveDirectoryTask";
    private String destination;

    @Override
    protected Boolean doInBackground(String... params)
    {
      destination = params[0];
      boolean result = copyFileOrDir("saves");
      
      return result;
    }

    private boolean copyFileOrDir(String fileName)
    {
      String originalPath = getFilesDir() + "/" + fileName;
      File originalPathFile = new File(originalPath);
      File[] listOfFiles = originalPathFile.listFiles();
      if (listOfFiles == null) // then we know it's a file, not a directory
      {
        return copyFile(fileName);
      }
      else
      {
        String destPath = destination + "/" + fileName;

        File dir = new File(destPath);
        boolean mkdirSuccess = dir.mkdir();
        if (!mkdirSuccess)
        {
          return false;
        }
        
        for (int i = 0; i < listOfFiles.length; i++)
        {
          boolean result = copyFileOrDir(fileName + "/" + listOfFiles[i].getName());
          if (!result)
          {
            return false;
          }
        }
        return true;
      }
    }

    private boolean copyFile(String fileName)
    {
      String destname = destination + "/" + fileName;
      Log.d(TAG, "Copying: " + fileName + " to " + destname);
      File destinationFile = new File(destname);
      try
      {
        destinationFile.createNewFile();
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destinationFile, false));
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(getFilesDir() + "/" + fileName));
        int b;
        while ((b = in.read()) != -1)
        {
          out.write(b);
        }
        out.flush();
        out.close();
        in.close();
      }
      catch (IOException ex)
      {
        Log.e(TAG, "Exception occured copying " + fileName + ": " + ex);
        return false;
      }
      return true;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
      removeDialog(DIALOG_COPY_FILES_PROGRESS);
      int toastTextResource;
      if (result)
      {
        toastTextResource = R.string.files_copied_successfully;
      }
      else
      {
        toastTextResource = R.string.error_copying_files;
      }
      Toast.makeText(PreferencesActivity.this, toastTextResource, Toast.LENGTH_LONG).show();
    }
  }

  private static final int DIALOG_COPY_FILES_PROGRESS = 0;

  @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesName(Preferences.NAME);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		setConfigFilePreferences();
		
		setCharacterFilesIntent();
		
		addExportSavePreference();
	}

	private void addExportSavePreference()
  {
	  DialogPreference backupDialogPreference = new DialogPreference(this, null)
    {
	    @Override
	    public void onClick(DialogInterface dialog, int which)
	    {
	      switch (which)
	      {
	      case DialogInterface.BUTTON_POSITIVE:
	        // Get stuff in edittext, back it up to that directory
	        EditText directoryBox = (EditText) getDialog().findViewById(R.id.directoryBox);
	        String destination = directoryBox.getText().toString();
	        backupSaveDirectory(destination);
	        break;
	      }
	    }
    };
    backupDialogPreference.setDialogLayoutResource(R.layout.backup_dialog);
    backupDialogPreference.setDialogTitle(R.string.backup_save_directory);
    backupDialogPreference.setTitle(R.string.backup_save_directory);
    backupDialogPreference.setPositiveButtonText(R.string.backup);
    backupDialogPreference.setNegativeButtonText(android.R.string.cancel);
    
    PreferenceCategory saveFilesCategory = (PreferenceCategory) findPreference("saveFiles");
    saveFilesCategory.addPreference(backupDialogPreference);
    
  }

  private void backupSaveDirectory(String destination)
  {
    showDialog(DIALOG_COPY_FILES_PROGRESS);
    new CopySaveDirectoryTask().execute(destination);
  }
  
  @Override
  public Dialog onCreateDialog(int id)
  {
    switch (id)
    {
    case DIALOG_COPY_FILES_PROGRESS:
      ProgressDialog filesCopyingDialog = new ProgressDialog(this);
      filesCopyingDialog.setMessage(getString(R.string.copying_files));
      return filesCopyingDialog;
    default:
      break;
    }
    return null;
  }

  private void setCharacterFilesIntent()
	{
		Preference characterFilesPreference = findPreference("character_files");
		Intent characterFilesIntent = new Intent(this, CharacterFilesActivity.class);
		characterFilesPreference.setIntent(characterFilesIntent);
	}

	private void setConfigFilePreferences()
	{
		PreferenceCategory configFilePreferences = (PreferenceCategory) findPreference("configFiles");
		String[] configFiles = getResources().getStringArray(R.array.config_files);
		for (int i = 0; i < configFiles.length; i++)
		{
			EditConfigFilePreference editConfigFilePreference =
					new EditConfigFilePreference(this, configFiles[i]);
			configFilePreferences.addPreference(editConfigFilePreference);
		}

		// The macro file is a special case. We should only show this setting if
		// the file exists
		File macroFile = new File(getFilesDir() + "/settings/macro.txt");
		if (macroFile.exists())
		{
			EditConfigFilePreference editConfigFilePreference = new EditConfigFilePreference(this, "macro");
			configFilePreferences.addPreference(editConfigFilePreference);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		setSummaryAll(getPreferenceScreen());
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		SharedPreferences pref = getSharedPreferences(Preferences.NAME, MODE_PRIVATE);

		if (pref.getBoolean(Preferences.KEY_FULLSCREEN, true))
		{
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		else
		{
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	private void setSummaryAll(PreferenceScreen pScreen)
	{
		for (int i = 0; i < pScreen.getPreferenceCount(); i++)
		{
			Preference pref = pScreen.getPreference(i);
			setSummaryPref(pref);
		}
	}

	public void setSummaryPref(Preference pref)
	{
		if (pref == null)
			return;

		String key = pref.getKey();
		if (key == null)
			key = "";

		if (pref instanceof KeyMapPreference)
		{
			KeyMapPreference kbPref = (KeyMapPreference) pref;
			String desc = kbPref.getDescription();
			pref.setSummary(desc);
		}
		else if (pref instanceof PreferenceCategory)
		{
			PreferenceCategory prefCat = (PreferenceCategory) pref;
			int count = prefCat.getPreferenceCount();
			for (int i = 0; i < count; i++)
			{
				setSummaryPref(prefCat.getPreference(i));
			}
		}
		else if (pref instanceof PreferenceScreen)
		{
			setSummaryAll((PreferenceScreen) pref);
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.compareTo(Preferences.KEY_ACTIVEPROFILE) == 0 || key.compareTo(Preferences.KEY_PROFILES) == 0)
		{
			setSummaryAll(getPreferenceScreen());
		}
		else
		{
			Preference pref = findPreference(key);
			setSummaryPref(pref);
		}
	}
}
