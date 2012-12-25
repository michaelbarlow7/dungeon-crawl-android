/*
 * File: GameActivity.java Purpose: Generic ui functions in Android application
 * 
 * Copyright (c) 2010 David Barr, Sergey Belinsky
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

package com.crawlmb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.os.Handler;
import android.os.Message;

public class GameActivity extends Activity // implements OnScoreSubmitObserver {
{

	public static StateManager state = null;
	private CrawlDialog dialog = null;

	private RelativeLayout screenLayout = null;
	private TermView term = null;

	protected Handler handler = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Log.d("Crawl", "onCreate");

		if (state == null)
		{
			state = new StateManager();
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();

		if (dialog == null)
			dialog = new CrawlDialog(this, state);
		final CrawlDialog crawlDialog = dialog;
		handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				crawlDialog.HandleMessage(msg);
			}
		};

		rebuildViews();
	}

	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = new MenuInflater(getApplication());
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{//TODO: Add help and quit menu options
		Intent intent;
		switch (item.getNumericShortcut())
		{
		case '1'://Help
			 intent = new Intent(this, HelpActivity.class);
			 startActivity(intent);
			break;
		case '2'://Preferences
			intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			break;
		case '3'://Quit
			finish();
			break;
		case '4'://Reset terminal position
		  term.resetTerminalPosition();
		  break;
		case '5'://Lock terminal position
		  break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void finish()
	{
		// Log.d("Crawl","finish");
		state.gameThread.send(GameThread.Request.StopGame);
		super.finish();
	}

	protected void rebuildViews()
	{
		synchronized (StateManager.progress_lock)
		{
			// Log.d("Crawl","rebuildViews");

			int orient = Preferences.getOrientation();
			switch (orient)
			{
			case 0: // sensor
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				break;
			case 1: // portrait
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
			case 2: // landscape
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			}

			if (screenLayout != null)
				screenLayout.removeAllViews();
			screenLayout = new RelativeLayout(this);

			term = new TermView(this);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			term.setLayoutParams(layoutParams);
			term.setFocusable(true);
			registerForContextMenu(term);
			state.link(term, handler);

			screenLayout.addView(term);

			String keyboardType;
			if (Preferences.isScreenPortraitOrientation())
				keyboardType = Preferences.getPortraitKeyboard();
			else
				keyboardType = Preferences.getLandscapeKeyboard();

			String[] keyboards = getResources().getStringArray(R.array.virtualKeyboardValues);

			if (keyboardType.equals(keyboards[1])) // Crawl Keyboard
			{
				CrawlKeyboard virtualKeyboard = new CrawlKeyboard(this);
				screenLayout.addView(virtualKeyboard.virtualKeyboardView);
				
				//Add directional-key view
				addDirectionalKeyView(virtualKeyboard.virtualKeyboardView.getId());
				
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
			else if (keyboardType.equals(keyboards[2])) // System Keyboard
			{
				InputMethodManager inputMethodManager =
						(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			}
			else
			{
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}

			setContentView(screenLayout);
			dialog.restoreDialog();

			term.invalidate();
		}
	}

	private void addDirectionalKeyView(int virtualKeyboardId) {
		DirectionalTouchView view = new DirectionalTouchView(this);
		RelativeLayout.LayoutParams directionalLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
			LayoutParams.FILL_PARENT);
		directionalLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		directionalLayoutParams.addRule(RelativeLayout.ABOVE, virtualKeyboardId);
		view.setLayoutParams(directionalLayoutParams);
		view.setTermView(term);
		screenLayout.addView(view);
	}

	public void openContextMenu()
	{
		super.openContextMenu(term);
	}

	public void toggleKeyboard()
	{
		int currentKeyboard;
		if (Preferences.isScreenPortraitOrientation())
		{
			currentKeyboard = Integer.parseInt(Preferences.getPortraitKeyboard());
			if (currentKeyboard == 2) // System keyboard
			{
				toggleSystemKeyboard();
				return;
			}
			currentKeyboard = currentKeyboard == 0 ? 1 : 0;
			Preferences.setPortraitKeyboard(String.valueOf(currentKeyboard));
		}
		else
		{
			currentKeyboard = Integer.parseInt(Preferences.getLandscapeKeyboard());
			if (currentKeyboard == 2) // System keyboard
			{
				toggleSystemKeyboard();
				return;
			}
			currentKeyboard = currentKeyboard == 0 ? 1 : 0;
			Preferences.setLandscapeKeyboard(String.valueOf(currentKeyboard));
		}

		rebuildViews();
	}

	private void toggleSystemKeyboard()
	{
		InputMethodManager inputMethodManager =
				(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	@Override
	protected void onResume()
	{
		// Log.d("Crawl", "onResume");
		super.onResume();

		setScreen();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (!state.onKeyDown(keyCode, event))
		{
			return super.onKeyDown(keyCode, event);
		}
		else
		{
			return true;
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (!state.onKeyUp(keyCode, event))
		{
			return super.onKeyUp(keyCode, event);
		}
		else
		{
			return true;
		}
	}

	public void setScreen()
	{
		if (Preferences.getFullScreen())
		{
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		else
		{
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	public Handler getHandler()
	{
		return handler;
	}

	public StateManager getStateManager()
	{
		return state;
	}

}