package com.crawlmb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class CrawlAppActivity extends Activity
{
	public static final String TAG = CrawlAppActivity.class.getName();
	private static final int INSTALL_DIALOG_ID = 0;
	private ProgressDialog installDialog;

	private File dataDirectory;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setBackground();
		
		dataDirectory = new File(getFilesDir() + "/dat");
		if (dataDirectory.exists())
		{
			startGameActivity();
		}
		else
		{
			new InstallProgramTask().execute();
		}
	}

	private void setBackground()
	{
		int backgroundResource;
		int[] backgroundResources = { 
				R.drawable.title_denzi_dragon,
				R.drawable.title_denzi_evil_mage,
				R.drawable.title_denzi_invasion,
				R.drawable.title_denzi_kitchen_duty,
				R.drawable.title_denzi_summoner,
				R.drawable.title_denzi_undead_warrior,
				R.drawable.title_firemage,
				R.drawable.title_omndra_zot_demon,
				R.drawable.title_shadyamish_octm
		};
		Random generator = new Random();
		backgroundResource = backgroundResources[generator.nextInt(backgroundResources.length)];
		
		ImageView background = (ImageView) findViewById(R.id.background);
		background.setImageResource(backgroundResource);
	}

	@Override
	public Dialog onCreateDialog(int id)
	{
		if (id != INSTALL_DIALOG_ID)
		{
			return null;
		}
		installDialog = new ProgressDialog(this);
		installDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		installDialog.setTitle(R.string.install_dialog_title);
		installDialog.setIndeterminate(true);
		installDialog.setCancelable(false);
		return installDialog;

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
			if (a != 0)
			{
				// This will probably always happen now when running from SD
				// card (or a Samsung phone),
				// so don't generate error spew.
				// Log.i("NetHackDbg",
				// "android.os.FileUtils.setPermissions() returned " + a +
				// " for '" + filename + "', probably didn't work.");
			}
		}
		catch (ClassNotFoundException e)
		{
			Log.i("NetHackDbg", "android.os.FileUtils.setPermissions() failed - ClassNotFoundException.");
		}
		catch (IllegalAccessException e)
		{
			Log.i("NetHackDbg", "android.os.FileUtils.setPermissions() failed - IllegalAccessException.");
		}
		catch (InvocationTargetException e)
		{
			Log.i("NetHackDbg", "android.os.FileUtils.setPermissions() failed - InvocationTargetException.");
		}
		catch (NoSuchMethodException e)
		{
			Log.i("NetHackDbg", "android.os.FileUtils.setPermissions() failed - NoSuchMethodException.");
		}
	}

	private void startGameActivity()
	{
		// Wait 1.5 seconds, then start game
		Timer timer = new Timer();
		TimerTask gameStartTask = new TimerTask()
		{
			@Override
			public void run()
			{
				runOnUiThread(new startGameRunnable());
			}
		};
		timer.schedule(gameStartTask, 1500);
	}

	private final class startGameRunnable implements Runnable
	{
		@Override
		public void run()
		{
			Intent intent = new Intent(CrawlAppActivity.this, GameActivity.class);
			startActivity(intent);
			finish();
		}
	}

	private class InstallProgramTask extends AsyncTask<Void, Integer, Void>
	{
		private int totalFiles = 0;
		private int installedFiles = 0;;
		@Override
		protected void onPreExecute()
		{
			showDialog(INSTALL_DIALOG_ID);
		}
		
		@Override
		protected Void doInBackground(Void... params)
		{
			totalFiles = 284; //Number of files that need creating. Hard-coded I know, but
							  // counting them dynamically took a surprising amount of time
			if (installDialog != null)
			{
				installDialog.setIndeterminate(false);
				installDialog.setMax(totalFiles);
			}
			publishProgress(installedFiles);
			// These should simply return false if they already exist, so it
			// won't overwrite anything
			mkdir("/saves");
			publishProgress(++installedFiles);
			mkdir("/saves/db");
			publishProgress(++installedFiles);
			mkdir("/saves/des");
			publishProgress(++installedFiles);
			mkdir("/morgue");
			publishProgress(++installedFiles);

			copyFile("README.txt");
			copyFileOrDir("dat");
			copyFileOrDir("settings");
			writeInitFile();
			copyFileOrDir("docs");
			return null;
		}

		private void writeInitFile()
		{
			FileOutputStream fileOutputStream;
			try
			{
				fileOutputStream = new FileOutputStream(getFilesDir() + "/settings/init.txt",true);
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
				String androidWarning = "\n# THE FOLLOWING SETTINGS ARE HERE FOR THE ANDROID PORT\n# EDITTING THEM MAY CAUSE THE GAME TO STOP WORKING PROPERLY\n";
				String crawlDirSpecification = "crawl_dir = " + getFilesDir() + "\n";
				String saveDirSpecification = "save_dir = " + getFilesDir() + "/saves\n";
				String morgueDirSpecification = "morgue_dir = " + getFilesDir() + "/morgue\n";
				String macroDirSpecification = "macro_dir = " + getFilesDir() + "/settings\n";
				bufferedOutputStream.write(androidWarning.getBytes());
				bufferedOutputStream.write(crawlDirSpecification.getBytes());
				bufferedOutputStream.write(saveDirSpecification.getBytes());
				bufferedOutputStream.write(morgueDirSpecification.getBytes());
				bufferedOutputStream.write(macroDirSpecification.getBytes());
				bufferedOutputStream.flush();
				bufferedOutputStream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		} 
		private void mkdir(String relativePath)
		{
			// FIXME: making files using this method, because we can't seem to
			// make files using the native code for some reason
			File dir = new File(getFilesDir() + relativePath);
			dir.mkdirs();
			chmod(dir.getAbsolutePath(), 0777);
		}

		private void copyFileOrDir(String path)
		{
			AssetManager assetManager = getAssets();
			String assets[] = null;
			try
			{
				assets = assetManager.list(path);
				publishProgress(++installedFiles, totalFiles);
				if (assets.length == 0) // then we know it's a file, not a
										// directory
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
			}
			chmod(destname, 0666);
		}
		
		@Override
		protected void onProgressUpdate(Integer...values )
		{
			if (installDialog == null)
			{
				return;
			}
			installDialog.setProgress(values[0]);
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			removeDialog(INSTALL_DIALOG_ID);
			startGameActivity();
		}
	}
}