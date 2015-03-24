package com.crawlmb.keymap;

import android.content.Context;
import android.util.AttributeSet;
import android.preference.DialogPreference;
import android.content.DialogInterface;

import com.crawlmb.Preferences;

public class KeyMapResetPreference extends DialogPreference implements DialogInterface.OnClickListener
{
	public KeyMapResetPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setDialogTitle("Really reset all keys?");
		setPositiveButtonText("OK");
		setNegativeButtonText("Cancel");
	}

	public void onClick(DialogInterface dialog, int which)
	{
		if (which == -1)
		{ // OK
			Preferences.getKeyMapper().init(true);
		}
		else
		{
		}
	}
}
