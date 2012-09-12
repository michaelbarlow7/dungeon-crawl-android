package com.crawlmb;

import java.io.File;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class CharacterFilesActivity extends ListActivity
{
	private String[] charFiles;

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		
		File morgueDirFile = new File(getFilesDir() + "/morgue");
		charFiles = morgueDirFile.list();
		if (charFiles == null || charFiles.length == 0)
		{
			//Maybe should show a dialog here or something?
			Toast.makeText(this, "No character files stored", Toast.LENGTH_LONG).show();
			return;
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, charFiles);
		setListAdapter(adapter);
	}
	
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id)
	{
		String charFileName = charFiles[position];
		File file = new File(getFilesDir() + "/morgue/" + charFileName);
		Uri uri = Uri.fromFile(file);
		Intent intent = new Intent();
		intent.setDataAndType(uri, "text/plain"); 
		intent.setClass(this, CharFileViewer.class);
		startActivity(intent);
	}

}
