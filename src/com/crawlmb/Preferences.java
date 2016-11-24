package com.crawlmb;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.app.AlertDialog;

import com.crawlmb.keyboard.CrawlKeyboardWrapper.KeyboardType;
import com.crawlmb.keymap.KeyMapper;

final public class Preferences
{

	public static final int rows = 24;
	public static final int cols = 80;

	public static final String NAME = "crawl";

	public static final String KEY_VIBRATE = "crawl.vibrate";
	public static final String KEY_FULLSCREEN = "crawl.fullscreen";
	public static final String KEY_ORIENTATION = "crawl.orientation";
	public static final String KEY_SKIPSPLASH = "crawl.skipsplash";

	public static final String KEY_ENABLETOUCH = "crawl.enabletouch";
	public static final String KEY_PORTRAITKB = "crawl.portraitkb";
	public static final String KEY_LANDSCAPEKB = "crawl.landscapekb";
	public static final String KEY_PORTRAITFONTSIZE = "crawl.portraitfontsize";
	public static final String KEY_LANDSCAPEFONTSIZE = "crawl.landscapefontsize";
	public static final String KEY_KEYBOARDTRANSPARENCY = "crawl.keyboardtransparency";

	public static final String KEY_PROFILES = "crawl.profiles";
	public static final String KEY_ACTIVEPROFILE = "crawl.activeprofile";

    private static final String KEYBOARD_LAYOUT_COUNT = "layout_count";
    public static final String KEYBOARD_LAYOUT_CURRENT = "layout_current";
    public static final String KEYBOARD_LABEL_PREFIX = "label_";
    public static final String KEYBOARD_CODE_PREFIX = "code_";

	private static final String KEY_HAPTICFEEDBACKENABLED = "crawl.hapticfeedbackenabled";
	private static final String KEY_KEYBOARDARROWSENABLED = "crawl.keyboardarrowsenabled";

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
		ed.apply();
	}
	
	public static boolean getSkipSplash()
	{
		return sharedPreferences.getBoolean(Preferences.KEY_SKIPSPLASH, false);
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

	public static int getKeyboardTransparency(){
		return sharedPreferences.getInt(Preferences.KEY_KEYBOARDTRANSPARENCY, 140);
	}

	public static void setKeyboardTransparency(int transparency) {
		sharedPreferences.edit().putInt(Preferences.KEY_KEYBOARDTRANSPARENCY, transparency).apply();
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
		ed.apply();
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
		ed.apply();
	}

	public static int getPortraitFontSize()
	{
		return sharedPreferences.getInt(Preferences.KEY_PORTRAITFONTSIZE, 0);
	}

	public static void setPortraitFontSize(int value)
	{
		SharedPreferences.Editor ed = sharedPreferences.edit();
		ed.putInt(Preferences.KEY_PORTRAITFONTSIZE, value);
		ed.apply();
	}

	public static int getLandscapeFontSize()
	{
		return sharedPreferences.getInt(Preferences.KEY_LANDSCAPEFONTSIZE, 0);
	}

	public static void setLandscapeFontSize(int value)
	{
		SharedPreferences.Editor ed = sharedPreferences.edit();
		ed.putInt(Preferences.KEY_LANDSCAPEFONTSIZE, value);
		ed.apply();
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

	public static boolean getHapticFeedbackEnabled() 
	{
		return sharedPreferences.getBoolean(Preferences.KEY_HAPTICFEEDBACKENABLED, true);
	}

    public static int getLayoutCount(){
        return sharedPreferences.getInt(Preferences.KEYBOARD_LAYOUT_COUNT, 0);
    }

    public static void setCustomLayoutCount(int layoutCount){
        sharedPreferences.edit().putInt(KEYBOARD_LAYOUT_COUNT, layoutCount).apply();
    }

    public static void setCurrentKeyboardLayout(int currentKeyboardLayout){
        sharedPreferences.edit().putInt(KEYBOARD_LAYOUT_CURRENT, currentKeyboardLayout).apply();
    }

    public static int getCurrentKeyboardLayout(){
        return sharedPreferences.getInt(KEYBOARD_LAYOUT_CURRENT, 0);
    }

	public static boolean getKeyboardArrowsEnabled(){
		return sharedPreferences.getBoolean(KEY_KEYBOARDARROWSENABLED, true);
	}

    public static SharedPreferences getCurrentKeyboardPreferences(Context context, KeyboardType keyboardType){
        int currentType = sharedPreferences.getInt(KEYBOARD_LAYOUT_CURRENT, 0);
        if (currentType == 0){
            return null;
        }
        return getKeyboardPreferences(context, currentType, keyboardType);
    }

    public static SharedPreferences getKeyboardPreferences(Context context, int id, KeyboardType keyboardType){
        if (keyboardType == null){
            return null;
        }
        String preferenceName = keyboardType.name() + '_' + id;
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, 0);

        return sharedPreferences;
    }

    public static void deleteLayout(Context context, int layoutNumber){
        // Delete entries in associated layout files
        for (KeyboardType keyboardType : KeyboardType.values()){
            String preferenceName = keyboardType.name() + '_' + layoutNumber;
            SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, 0);
            sharedPreferences.edit().clear().apply();
        }
        setCurrentKeyboardLayout(0);
        setCustomLayoutCount(0);
    }

    public static void addNewKeyboardLayout() {
        setCustomLayoutCount(1);
        setCurrentKeyboardLayout(1);
    }

    public static void addKeybindingToLayout(Context context, KeyboardType keyboardType, int keyIndex, int newCode, String label){
        String sharedPreferenceName = keyboardType.name() + "_1"; // Change this if we're using multiple layouts
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferenceName, 0);

        String codePreferenceName = KEYBOARD_CODE_PREFIX + keyIndex;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(codePreferenceName, newCode);

        String labelPreferenceName = KEYBOARD_LABEL_PREFIX + keyIndex;
        editor.putString(labelPreferenceName, label);

        editor.apply();
    }

    public static void clearKeybindingInLayout(Context context, KeyboardType keyboardType, int keyIndex){
        String sharedPreferenceName = keyboardType.name() + "_1"; // Change this if we're using multiple layouts
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferenceName, 0);

        String codePreferenceName = KEYBOARD_CODE_PREFIX + keyIndex;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(codePreferenceName);

        String labelPreferenceName = KEYBOARD_LABEL_PREFIX + keyIndex;
        editor.remove(labelPreferenceName);

        editor.apply();
    }

}
