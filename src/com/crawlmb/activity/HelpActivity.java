/*
 * File: HelpActivity.java Purpose: Help activity for Android application
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

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.crawlmb.Preferences;
import com.crawlmb.R;

public class HelpActivity extends Activity
{
	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		setContentView(R.layout.help);
		this.setTitle(String.format("Crawl: Help"));
		WebView help = (WebView) findViewById(R.id.help_text);

		WebSettings settings = help.getSettings();
		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
		settings.setUseWideViewPort(false);
		help.loadUrl("file:///android_asset/help.html");
		help.computeScroll();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

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
}
