package com.crawlmb;

import android.graphics.Color;

import com.crawlmb.keylistener.GameKeyListener;
import com.crawlmb.view.TermView;

public class NativeWrapper
{
	// Load native library
	static
	{
		System.loadLibrary("crawl");
	}

	private TermView term = null;
	private GameKeyListener keyListener = null;

	private final String display_lock = "lock";
	private static final String TAG = NativeWrapper.class.getCanonicalName();

	public void gameStart()
	{
		initGame(term.getContext().getFilesDir().getPath());
	}

	private void showLoadingMessage()
	{
		synchronized (display_lock)
		{
            String[] loadingMessageArray = term.getResources().getStringArray(R.array.loading_message_array);
            for (int i = 0; i < loadingMessageArray.length; i++){
            	String loadingMessageLine = loadingMessageArray[i];
				for (int j = 0; j < loadingMessageLine.length(); j++){
					term.drawPoint(i, j, loadingMessageLine.charAt(j), Color.WHITE, Color.BLACK, false);
				}
			}
			term.invalidate();
		}
	}

	public native void initGame(String initFileLocation);

	public NativeWrapper(GameKeyListener s)
	{
		keyListener = s;
	}

	public int getch(final int v)
	{
		keyListener.gameThread.setFullyInitialized();
		int key = keyListener.getKey(v);
		return key;
	}

	// this is called from native thread just before exiting
	public void onGameExit()
	{
		keyListener.handler.sendEmptyMessage(CrawlDialog.Action.OnGameExit.ordinal());
		// Log.d(TAG, "onGameExit()");
	}

	private native void refreshTerminal();

	public boolean onGameStart()
	{
		// Log.d(TAG, "onGameStart()");
		synchronized (display_lock)
		{
			boolean result = term.onGameStart();
			showLoadingMessage();
			return result;
		}
	}

	public void increaseFontSize()
	{
		// Log.d(TAG, "increaseFontSzie()");
		synchronized (display_lock)
		{
			term.increaseFontSize();
			resize();
		}
	}

	public void link(TermView t)
	{
		synchronized (display_lock)
		{
			term = t;
		}
	}

	public void decreaseFontSize()
	{
		// Log.d(TAG, "decreaseFontSize()");
		synchronized (display_lock)
		{
			term.decreaseFontSize();
			resize();
		}
	}

	public void fatal(String msg)
	{
		// Log.d(TAG, "fatal("+msg+")");
		synchronized (display_lock)
		{
			keyListener.fatalMessage = msg;
			keyListener.fatalError = true;
			keyListener.handler.sendMessage(keyListener.handler.obtainMessage(
					CrawlDialog.Action.GameFatalAlert.ordinal(), 0, 0, msg));
		}
	}

	public void resize()
	{
		// Log.d(TAG, "resize()");
		synchronized (display_lock)
		{
			term.onGameStart(); // recalcs TermView canvas dimension
			refreshTerminal();
		}
	}

	public void printTerminalChar(int y, int x, char c, int fgcolor, int bgcolor)
	{
		synchronized (display_lock)
		{
			// Formatter fmt = new Formatter();
			// fmt.format("fgcolor:%x bgcolor:%x ", fgcolor, bgcolor);
			// Log.d("Crawl","printingTerminalChar, y is: " + y + ", x is: " + x
			// + ", character is: " + c + ", " + fmt);
			term.drawPoint(y, x, c, fgcolor, bgcolor, false);
		}
	}

	public void invalidateTerminal()
	{
		synchronized (display_lock)
		{
			term.postInvalidate();
		}
	}

}
