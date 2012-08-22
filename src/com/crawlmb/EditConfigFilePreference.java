package com.crawlmb;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;

public class EditConfigFilePreference extends Preference
{
	public EditConfigFilePreference(Context context)
	{
		super(context);
	}
	
	public EditConfigFilePreference(Context context, String key)
	{
		super(context);
		String title = context.getResources().getString(R.string.config_file_prefererence_title, key);
		setKey(key);
		setTitle(title);
		
		setEditConfigFileIntent(key);
	}

	private void setEditConfigFileIntent(String key)
	{
		File file = new File(getContext().getFilesDir() + "/settings/" + key + ".txt");
		Uri uri = Uri.fromFile(file);
		Intent editConfigIntent = new Intent(Intent.ACTION_VIEW ,uri);
		editConfigIntent.setDataAndType(uri, "text/plain"); 
		editConfigIntent.setClass(getContext(), ConfigEditor.class);
		setIntent(editConfigIntent);
	}

}
