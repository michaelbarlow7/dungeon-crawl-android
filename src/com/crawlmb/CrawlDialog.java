package com.crawlmb;

import android.os.Message;
import android.content.DialogInterface;
import android.app.AlertDialog;

import com.crawlmb.activity.GameActivity;

public class CrawlDialog
{
	private GameActivity activity;
	private StateManager state;

	public enum Action
	{
		GameFatalAlert, StartGame, OnGameExit, ToggleKeyboard;

		public static Action convert(int value)
		{
			return Action.class.getEnumConstants()[value];
		}
	};

	public CrawlDialog(GameActivity a, StateManager s)
	{
		activity = a;
		state = s;
	}

	public void HandleMessage(Message msg)
	{

		switch (Action.convert(msg.what))
		{
		case ToggleKeyboard:
			activity.toggleKeyboard();
			break;
		case GameFatalAlert: // fatal error from crawl (native side)
			fatalAlert(state.getFatalError());
			break;
		case StartGame: // start crawl
			state.gameThread.send(GameThread.Request.StartGame);
			break;
		case OnGameExit: // crawl is exiting
			state.gameThread.send(GameThread.Request.OnGameExit);
			break;
		}
	}

	public void restoreDialog()
	{
		if (state.fatalError)
			fatalAlert(state.getFatalError());
	}

	public int fatalAlert(String msg)
	{
		new AlertDialog.Builder(activity).setTitle("Crawl").setMessage(msg)
				.setNegativeButton("OK", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						state.fatalMessage = "";
						state.fatalError = false;
						activity.finish();
					}
				}).show();
		return 0;
	}

}