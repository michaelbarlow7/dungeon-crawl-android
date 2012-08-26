package com.crawlmb;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.app.AlertDialog;

final public class Preferences
{

	public static final int rows = 24;
	public static final int cols = 80;

	static final String NAME = "crawl";

	static final String KEY_VIBRATE = "crawl.vibrate";
	static final String KEY_FULLSCREEN = "crawl.fullscreen";
	static final String KEY_ORIENTATION = "crawl.orientation";

	static final String KEY_ENABLETOUCH = "crawl.enabletouch";
	static final String KEY_PORTRAITKB = "crawl.portraitkb";
	static final String KEY_LANDSCAPEKB = "crawl.landscapekb";
	static final String KEY_PORTRAITFONTSIZE = "crawl.portraitfontsize";
	static final String KEY_LANDSCAPEFONTSIZE = "crawl.landscapefontsize";
	static final String KEY_ALWAYSRUN = "crawl.alwaysrun";

	static final String KEY_GAMEPLUGIN = "crawl.gameplugin";
	static final String KEY_GAMEPROFILE = "crawl.gameprofile";
	static final String KEY_SKIPWELCOME = "crawl.skipwelcome";
	static final String KEY_AUTOSTARTBORG = "crawl.autostartborg";

	static final String KEY_PROFILES = "crawl.profiles";
	static final String KEY_ACTIVEPROFILE = "crawl.activeprofile";

	static final String KEY_INSTALLEDVERSION = "crawl.installedversion";

	private static SharedPreferences sharedPreferences;
	private static int fontSize = 17;
	private static Resources resources;

	private static KeyMapper keymapper;

	Preferences()
	{
	}

	public static void init(Resources res, SharedPreferences sharedPreferences)
	{
		Preferences.sharedPreferences = sharedPreferences;
		resources = res;

		keymapper = new KeyMapper(sharedPreferences);
	}

	public static Resources getResources()
	{
		return resources;
	}

	public static String getString(String key)
	{
		return sharedPreferences.getString(key, "");
	}

	public static boolean getFullScreen()
	{
		return sharedPreferences.getBoolean(Preferences.KEY_FULLSCREEN, true);
	}

	public static void setFullScreen(boolean value)
	{
		SharedPreferences.Editor ed = sharedPreferences.edit();
		ed.putBoolean(Preferences.KEY_FULLSCREEN, value);
		ed.commit();
	}

	public static boolean isScreenPortraitOrientation()
	{
		Configuration config = resources.getConfiguration();
		return (config.orientation == Configuration.ORIENTATION_PORTRAIT);
	}

	public static int getOrientation()
	{
		return Integer.parseInt(sharedPreferences.getString(Preferences.KEY_ORIENTATION, "0"));
	}

	public static int getDefaultFontSize()
	{
		return fontSize;
	}

	public static int setDefaultFontSize(int value)
	{
		return fontSize = value;
	}

	public static boolean getVibrate()
	{
		return sharedPreferences.getBoolean(Preferences.KEY_VIBRATE, false);
	}

	public static String getPortraitKeyboard()
	{
		return sharedPreferences.getString(Preferences.KEY_PORTRAITKB,
				resources.getString(R.string.portraitkb_default));
	}

	public static void setPortraitKeyboard(String value)
	{
		SharedPreferences.Editor ed = sharedPreferences.edit();
		ed.putString(Preferences.KEY_PORTRAITKB, value);
		ed.commit();
	}

	public static String getLandscapeKeyboard()
	{
		return sharedPreferences.getString(Preferences.KEY_LANDSCAPEKB,
				resources.getString(R.string.landscapekb_default));
	}

	public static void setLandscapeKeyboard(String value)
	{
		SharedPreferences.Editor ed = sharedPreferences.edit();
		ed.putString(Preferences.KEY_LANDSCAPEKB, value);
		ed.commit();
	}

	public static int getPortraitFontSize()
	{
		return sharedPreferences.getInt(Preferences.KEY_PORTRAITFONTSIZE, 0);
	}

	public static void setPortraitFontSize(int value)
	{
		SharedPreferences.Editor ed = sharedPreferences.edit();
		ed.putInt(Preferences.KEY_PORTRAITFONTSIZE, value);
		ed.commit();
	}

	public static int getLandscapeFontSize()
	{
		return sharedPreferences.getInt(Preferences.KEY_LANDSCAPEFONTSIZE, 0);
	}

	public static void setLandscapeFontSize(int value)
	{
		SharedPreferences.Editor ed = sharedPreferences.edit();
		ed.putInt(Preferences.KEY_LANDSCAPEFONTSIZE, value);
		ed.commit();
	}

	public static boolean getEnableTouch()
	{
		return sharedPreferences.getBoolean(Preferences.KEY_ENABLETOUCH, true);
	}

	public static int alert(Context ctx, String title, String msg)
	{
		new AlertDialog.Builder(ctx).setTitle(title).setMessage(msg)
				.setNegativeButton("OK", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
					}
				}).show();
		return 0;
	}

	public static KeyMapper getKeyMapper()
	{
		return keymapper;
	}
}
