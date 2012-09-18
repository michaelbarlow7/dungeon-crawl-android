package com.crawlmb;

import android.app.Application;

public class CrawlApplication extends Application 
{

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		Preferences.init(getResources(), getSharedPreferences(Preferences.NAME, MODE_PRIVATE));
	}
}
