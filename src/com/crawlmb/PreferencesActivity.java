/*
 * File: PreferencesActivity.java
 * Purpose: Preferences activity for Android application
 *
 * Copyright (c) 2009 David Barr, Sergey Belinsky
 * 
 * This work is free software; you can redistribute it and/or modify it
 * under the terms of either:
 *
 * a) the GNU General Public License as published by the Free Software
 *    Foundation, version 2, or
 *
 * b) the "Angband licence":
 *    This software may be copied and distributed for educational, research,
 *    and not for profit purposes provided that this copyright and statement
 *    are included in all such copies.  Other copyrights may also apply.
 */

package com.crawlmb;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.view.WindowManager;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.preference.PreferenceCategory;

public class PreferencesActivity
	extends PreferenceActivity implements OnSharedPreferenceChangeListener {    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesName(Preferences.NAME);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onResume() {
		super.onResume();

		setSummaryAll(getPreferenceScreen());
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		SharedPreferences pref = getSharedPreferences(Preferences.NAME,
				MODE_PRIVATE);

		if (pref.getBoolean(Preferences.KEY_FULLSCREEN, true)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	@Override protected void onPause() {
		super.onPause(); 
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	} 

	private void setSummaryAll(PreferenceScreen pScreen) {        
		for (int i = 0; i < pScreen.getPreferenceCount(); i++) {
            Preference pref = pScreen.getPreference(i);            
			setSummaryPref(pref);
		}
	} 

	public void setSummaryPref(Preference pref) {
		if (pref == null) return;

		String key = pref.getKey();
		if (key == null) key = "";

		if (pref instanceof KeyMapPreference) {                
			KeyMapPreference kbPref = (KeyMapPreference) pref;     
			String desc = kbPref.getDescription();
			pref.setSummary(desc); 
		}
		else if (pref instanceof PreferenceCategory) {
			PreferenceCategory prefCat = (PreferenceCategory)pref;
			int count = prefCat.getPreferenceCount();
			for (int i=0; i < count; i++) {
				setSummaryPref(prefCat.getPreference(i));
			}
		}
//		else if (pref instanceof ProfileListPreference) {
//			ProfileListPreference plPref = (ProfileListPreference) pref;     
//			String desc = plPref.getDescription();
//			pref.setSummary(desc); 
//		} 
//		else if (pref instanceof ProfileCheckBoxPreference ) {
//			ProfileCheckBoxPreference pcPref = (ProfileCheckBoxPreference) pref;     
//			if (key.compareTo(Preferences.KEY_SKIPWELCOME)==0) {
//				pcPref.setChecked(Preferences.getActiveProfile().getSkipWelcome());
//			}
//			else if (key.compareTo(Preferences.KEY_AUTOSTARTBORG)==0) {
//				pcPref.setChecked(Preferences.getActiveProfile().getAutoStartBorg());
//			}
//		} 
		else if (pref instanceof PreferenceScreen) {
			setSummaryAll((PreferenceScreen) pref); 
		} 
		else if (key.compareTo(Preferences.KEY_GAMEPROFILE)==0) {
			pref.setSummary(Preferences.getActiveProfile().getName());
		} 
	}

	public void	onSharedPreferenceChanged(SharedPreferences
										  sharedPreferences, String key) { 		
		if (key.compareTo(Preferences.KEY_ACTIVEPROFILE)==0 
			|| key.compareTo(Preferences.KEY_PROFILES)==0) {
			setSummaryAll(getPreferenceScreen());			
		}
		else {
			Preference pref = findPreference(key); 
			setSummaryPref(pref);
		}
	}
}
