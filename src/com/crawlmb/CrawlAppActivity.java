package com.crawlmb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CrawlAppActivity extends Activity 
{
	public static final String TAG = CrawlAppActivity.class.getName();
	private Button button;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        button = (Button) findViewById(R.id.button);
        
        
    }
    	
	public void onInitClick(View v)
	{
		makeFiles();
		String initFileLocation = getFilesDir().getAbsolutePath().toString() + "/init.txt";
        initGame(initFileLocation);
	}
    
	/**
	 * Makes folders necessary to run the game. Also copies across the 'dat' folder, the contents of which are in our assets folder
	 */
	private void makeFiles()
	{
		// These should simply return false if they already exist, so it won't overwrite anything
		mkdir("/saves");
		mkdir("/saves/db");
		mkdir("/saves/des");
		mkdir("/morgue");
		boolean dataNeedsTransferring = new File(getFilesDir() + "/dat").mkdirs();
		FileOutputStream fileOutputStream;
		try
		{
			fileOutputStream = openFileOutput("init.txt", MODE_WORLD_WRITEABLE);
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		String crawlDirSpecification = "crawl_dir = " + getFilesDir() + "\n";
		String saveDirSpecification = "save_dir = " + getFilesDir() + "/saves\n";
		String morgueDirSpecification = "morgue_dir = " + getFilesDir() + "/morgue\n";
		bufferedOutputStream.write(crawlDirSpecification.getBytes());
		bufferedOutputStream.write(saveDirSpecification.getBytes());
		bufferedOutputStream.write(morgueDirSpecification.getBytes());
		bufferedOutputStream.flush();
		bufferedOutputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if (dataNeedsTransferring)
		{
			copyFileOrDir("dat");
		}
		
		Toast.makeText(this, "FILES COPIED", Toast.LENGTH_LONG).show();
	}

	private void mkdir(String relativePath)
	{
		//FIXME: making files using this method, because we can't seem to make files using the native code for some reason
		File saveDir = new File(getFilesDir() + relativePath);
		saveDir.mkdirs();
		chmod(saveDir.getAbsolutePath(), 0777);
	}
	
	private void copyFileOrDir(String path)
	{
		AssetManager assetManager = getAssets();
		String assets[] = null;
		try
		{
			assets = assetManager.list(path);
			if (assets.length == 0) //then we know it's a file, not a directory
			{
				copyFile(path);
			}
			else
			{
				String fullPath = getFilesDir().toString() + "/" + path;
				
				File dir = new File(fullPath);
				dir.mkdir();
				chmod(fullPath, 0777);
				for (int i = 0; i < assets.length; i++)
				{
					copyFileOrDir(path + "/" + assets[i]);
				}
			}
		}
		catch (IOException ex)
		{
			Log.e(TAG, "IOException: " + ex);
		}
	}
	
	
	private void copyFile(String fileName)
	{
		AssetManager assetManager = getAssets();
		String destname = getFilesDir().toString() + "/" + fileName;
		Log.d(TAG, "Copying: " + fileName + " to " + destname);
		File newasset = new File(destname);
		try
		{
			newasset.createNewFile();
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(newasset));
			BufferedInputStream in = new BufferedInputStream(assetManager.open(fileName));
			int b;
			while((b = in.read()) != -1)
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
		}
		chmod(destname, 0666);
	}

	public void logd(final String msg) 
	{
		Log.d(TAG,msg);
	}


    public native String initGame(String initFileLocation);

    static 
    {
        System.loadLibrary("crawl");
    }
    
	private void chmod(String filename, int permissions)
	{
		// Using undocumented method, as per Nethack app
		
		try
		{
		    Class<?> fileUtils = Class.forName("android.os.FileUtils");
		    Method setPermissions =
		        fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
		    int a = (Integer) setPermissions.invoke(null, filename, permissions, -1, -1);
		    if(a != 0)
		    {
		    	// This will probably always happen now when running from SD card (or a Samsung phone),
		    	// so don't generate error spew.
				//	Log.i("NetHackDbg", "android.os.FileUtils.setPermissions() returned " + a + " for '" + filename + "', probably didn't work.");
		    }
		}
		catch(ClassNotFoundException e)
		{
			Log.i("NetHackDbg", "android.os.FileUtils.setPermissions() failed - ClassNotFoundException.");
		}
		catch(IllegalAccessException e)
		{
			Log.i("NetHackDbg", "android.os.FileUtils.setPermissions() failed - IllegalAccessException.");
		}
		catch(InvocationTargetException e)
		{
			Log.i("NetHackDbg", "android.os.FileUtils.setPermissions() failed - InvocationTargetException.");
		}
		catch(NoSuchMethodException e)
		{
			Log.i("NetHackDbg", "android.os.FileUtils.setPermissions() failed - NoSuchMethodException.");
		}
	}
}