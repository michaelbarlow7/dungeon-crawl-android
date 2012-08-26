package com.crawlmb;

import android.content.SharedPreferences;
import android.content.Context;
import android.util.AttributeSet;
import android.preference.DialogPreference;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.view.KeyEvent;
import android.os.Parcelable;

public class KeyMapPreference extends DialogPreference implements DialogInterface.OnClickListener
{

	protected boolean alt_mod = false;
	protected boolean char_mod = false;
	protected int key_code = 0;

	public KeyMapPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setDialogTitle("Press a hardware key...");
		setPositiveButtonText("Clear");
		setNegativeButtonText("Cancel");
	}

	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
	{
		super.onPrepareDialogBuilder(builder);

		alt_mod = false;
		char_mod = false;
		key_code = 0;

		builder.setOnKeyListener(new DialogInterface.OnKeyListener()
		{
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
			{

				if (event.getAction() != KeyEvent.ACTION_DOWN || event.getRepeatCount() > 0)
					return true;

				if (handleModifier(keyCode))
					return true;

				saveKeyAssignment(keyCode, event);
				dialog.dismiss();
				return true;
			}
		});
	}

	protected boolean handleModifier(int keyCode)
	{
		switch (keyCode)
		{
		case KeyEvent.KEYCODE_FOCUS: // eat focus for now (light camera press)
		case KeyEvent.KEYCODE_SHIFT_LEFT: // eat shift for now
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			return true;
		case KeyEvent.KEYCODE_ALT_LEFT: // allow alt
		case KeyEvent.KEYCODE_ALT_RIGHT:
			alt_mod = !alt_mod;
			return true;
		default:
			return false;
		}
	}

	protected void saveKeyAssignment(int keyCode, KeyEvent event)
	{
		if (event.isAltPressed())
			alt_mod = true;

		int meta = 0;
		if (alt_mod)
		{
			meta |= KeyEvent.META_ALT_ON;
			meta |= KeyEvent.META_ALT_LEFT_ON;
		}
		int ch = event.getUnicodeChar(meta);
		char_mod = (ch > 32 && ch < 127);
		key_code = char_mod ? ch : keyCode;

		if (key_code == KeyEvent.KEYCODE_BACK)
		{
			new AlertDialog.Builder(getContext()).setTitle("Crawl").setMessage("Really assign the Back key?")
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int whichButton)
						{
						}
					}).setPositiveButton("OK", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int whichButton)
						{
							saveMap();
						}
					}).show();
		}
		else
		{
			saveMap();
		}
	}

	public void saveMap()
	{
		KeyMap prevMap = Preferences.getKeyMapper().assignKeyMap(getKey(), key_code, alt_mod, char_mod);

		SharedPreferences.Editor ed = getSharedPreferences().edit();
		if (prevMap != null)
			ed.putString(prevMap.getPrefKey(), "");
		ed.putString(getKey(), getValue());

		ed.commit();
	}

	public String getValue()
	{
		if (key_code == 0)
			return "";
		else
			return KeyMap.stringValue(key_code, alt_mod, char_mod);
	}

	public String getDescription()
	{
		SharedPreferences settings = getSharedPreferences();
		String val = settings.getString(getKey(), "");

		String desc = "<none>";

		if (val != null && val.length() > 0)
		{
			if (val.startsWith("C"))
				desc = val.substring(1).toUpperCase();
			else
				desc = keyCodeDescription(Integer.parseInt(val), val.startsWith("0"));
		}

		return desc;
	}

	protected String keyCodeDescription(int code, boolean alt)
	{
		switch (code)
		{
		case KeyEvent.KEYCODE_ALT_LEFT:
			return "Left Alt";
		case KeyEvent.KEYCODE_ALT_RIGHT:
			return "Right Alt";
		case KeyEvent.KEYCODE_SHIFT_LEFT:
			return "Left Shift";
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			return "Right Shift";
		case KeyEvent.KEYCODE_CAMERA:
			return "Camera";
		case KeyEvent.KEYCODE_SEARCH:
			return "Search";
		case KeyEvent.KEYCODE_BACK:
			return "Back";
		case KeyEvent.KEYCODE_MENU:
			return "Menu";
		case KeyEvent.KEYCODE_DEL:
			return "Backspace";
		case KeyEvent.KEYCODE_SPACE:
			return "Space";
		case KeyEvent.KEYCODE_VOLUME_UP:
			return "Volume Up";
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			return "Volume Down";
		case KeyEvent.KEYCODE_DPAD_CENTER:
			return "D-Pad Center";
		case KeyEvent.KEYCODE_DPAD_LEFT:
			return "D-Pad Left";
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			return "D-Pad Right";
		case KeyEvent.KEYCODE_DPAD_DOWN:
			return "D-Pad Down";
		case KeyEvent.KEYCODE_DPAD_UP:
			return "D-Pad Up";
		case KeyEvent.KEYCODE_SOFT_LEFT:
			return "Left Softkey";
		case KeyEvent.KEYCODE_SOFT_RIGHT:
			return "Right Softkey";
		case KeyEvent.KEYCODE_SYM:
			return "Sym";
		case KeyEvent.KEYCODE_TAB:
			return "Tab";
			// case KeyEvent.KEYCODE_PAGE_DOWN:
			// return "Page Down";
			// case KeyEvent.KEYCODE_PAGE_UP:
			// return "Page Up";
		case KeyEvent.KEYCODE_NOTIFICATION:
			return "Notification";
		case KeyEvent.KEYCODE_MUTE:
			return "Mute";
		case KeyEvent.KEYCODE_ENDCALL:
			return "End Call";
		case KeyEvent.KEYCODE_CALL:
			return "Call";
		case KeyEvent.KEYCODE_CLEAR:
			return "Clear";
			// case KeyEvent.KEYCODE_BUTTON_L1:
			// return "L1";
			// case KeyEvent.KEYCODE_BUTTON_L2:
			// return "L2";
			// case KeyEvent.KEYCODE_BUTTON_MODE:
			// return "Mode";
			// case KeyEvent.KEYCODE_BUTTON_R1:
			// return "R1";
			// case KeyEvent.KEYCODE_BUTTON_R2:
			// return "R2";
			// case KeyEvent.KEYCODE_BUTTON_SELECT:
			// return "Select";
			// case KeyEvent.KEYCODE_BUTTON_START:
			// return "Start";
			// case KeyEvent.KEYCODE_BUTTON_THUMBL:
			// return "Left Thumb";
			// case KeyEvent.KEYCODE_BUTTON_THUMBR:
			// return "Right Thumb";
		case KeyEvent.KEYCODE_ENTER:
			return "Enter";
		case 97: // Samsung Epic 4G
			return "Emoticon";
		default:
			return (alt ? "Alt+" : "") + "Key " + code;
		}
	}

	public void onClick(DialogInterface dialog, int which)
	{
		if (which == -1)
		{ // Clear
			String oldValue = getSharedPreferences().getString(getKey(), "");
			Preferences.getKeyMapper().clearKeyMap(oldValue);

			SharedPreferences.Editor ed = getSharedPreferences().edit();
			ed.putString(getKey(), "");
			ed.commit();
		}
		else
		{
			// Toast.makeText(context, "Cancel was clicked",
			// Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected Parcelable onSaveInstanceState()
	{

		// be free little dialog
		onActivityDestroy();

		return super.onSaveInstanceState();
	}
}
