package com.crawlmb;

import java.util.HashMap;

import android.util.Log;

public class NativeWrapper
{
	// Load native library
	static
	{
		System.loadLibrary("crawl");
	}

	private TermView term = null;
	private StateManager state = null;

	private String display_lock = "lock";
	private static final String TAG = NativeWrapper.class.getCanonicalName();
	public static final int A_NORMAL = 0;
	public static final int A_REVERSE = 0x100;
	public static final int A_STANDOUT = 0x200;
	public static final int A_BOLD = 0x400;
	public static final int A_UNDERLINE = 0x800;
	public static final int A_BLINK = 0x1000;
	public static final int A_DIM = 0x2000;

	// #define A_ALTCHARSET 0x4000

	// Call native methods from library
	// native void gameStart(int argc, String[] argv);
	public void gameStart()
	{
		showLoadingMessage();

		String initFilePath = term.getContext().getFilesDir() + "/settings/init.txt";
		initGame(initFilePath);

	}

	private void showLoadingMessage()
	{
		int window = 0;
		String launchingGame = term.getResources().getString(R.string.launching_game);
		waddnstr(window, launchingGame.length(), launchingGame.getBytes());
		wrefresh(window);
	}

	public native String initGame(String initFileLocation);

	// Not sure what these two methods do
	native int gameQueryInt(int argc, String[] argv);

	native String gameQueryString(int argc, String[] argv);

	public NativeWrapper(StateManager s)
	{
		state = s;
	}

	public void link(TermView t)
	{
		synchronized (display_lock)
		{
			term = t;
		}
	}

	public int getch(final int v)
	{
		state.gameThread.setFullyInitialized();
		wrefresh(0);
		int key = state.getKey(v);
		return key;
	}

	// this is called from native thread just before exiting
	public void onGameExit()
	{
		state.handler.sendEmptyMessage(CrawlDialog.Action.OnGameExit.ordinal());
		// Log.d(TAG, "onGameExit()");
	}

	public boolean onGameStart()
	{
		// Log.d(TAG, "onGameStart()");
		synchronized (display_lock)
		{
			return term.onGameStart();
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

	public void decreaseFontSize()
	{
		// Log.d(TAG, "decreaseFontSize()");
		synchronized (display_lock)
		{
			term.decreaseFontSize();
			resize();
		}
	}

	public void flushinp()
	{
		// Log.d(TAG, "flushinp()");
		state.clearKeys();
	}

	public void fatal(String msg)
	{
		// Log.d(TAG, "fatal("+msg+")");
		synchronized (display_lock)
		{
			state.fatalMessage = msg;
			state.fatalError = true;
			state.handler.sendMessage(state.handler.obtainMessage(
					CrawlDialog.Action.GameFatalAlert.ordinal(), 0, 0, msg));
		}
	}

	public void warn(String msg)
	{
		// Log.d(TAG, "warn("+msg+")");
		synchronized (display_lock)
		{
			state.warnMessage = msg;
			state.warnError = true;
			state.handler.sendMessage(state.handler.obtainMessage(CrawlDialog.Action.GameWarnAlert.ordinal(),
					0, 0, msg));
		}
	}

	public void resize()
	{
		// Log.d(TAG, "resize()");
		synchronized (display_lock)
		{
			term.onGameStart(); // recalcs TermView canvas dimension
			frosh(null);
		}
	}

	public void wrefresh(int w)
	{
		// Log.d(TAG, "wrefresh("+w+")");
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
				frosh(t);
		}
	}

	private void frosh(TermWindow w)
	{
		synchronized (display_lock)
		{
			/* for forcing a redraw due to an Android event, w should be null */

			TermWindow v = state.virtscr;
			if (w != null)
				v.overwrite(w);

			/* mark ugly points, i.e. those clobbered by anti-alias overflow */
			for (int c = 0; c < v.cols; c++)
			{
				for (int r = 0; r < v.rows; r++)
				{
					TermWindow.TermPoint p = v.buffer[r][c];
					if (p.isDirty || w == null)
					{
						if (r < v.rows - 1)
						{
							TermWindow.TermPoint u = v.buffer[r + 1][c];
							u.isUgly = !u.isDirty;
						}
						if (c < v.cols - 1)
						{
							TermWindow.TermPoint u = v.buffer[r][c + 1];
							u.isUgly = !u.isDirty;
						}
						if (c < v.cols - 1 && r < v.rows - 1)
						{
							TermWindow.TermPoint u = v.buffer[r + 1][c + 1];
							u.isUgly = !u.isDirty;
						}
					}
				}
			}

			for (int r = 0; r < v.rows; r++)
			{
				for (int c = 0; c < v.cols; c++)
				{
					TermWindow.TermPoint p = v.buffer[r][c];
					if (p.isDirty || p.isUgly || w == null)
					{
						drawPoint(r, c, p, p.isDirty || w == null);
					}
				}
			}

			term.postInvalidate();

			if (w == null)
				term.onScroll(null, null, 0, 0); // sanitize scroll position
		}
	}

	private void drawPoint(int r, int c, TermWindow.TermPoint p, boolean extendErase)
	{

		int color = p.Color & 0xFF;

		boolean standout =
				((p.Color & A_STANDOUT) != 0) || ((p.Color & A_BOLD) != 0) || ((p.Color & A_UNDERLINE) != 0);

		boolean reverse = ((p.Color & A_REVERSE) != 0);

		TermWindow.ColorPair cp = TermWindow.pairs.get(color);
		if (cp == null || cp.fColor == cp.bColor)
			cp = TermWindow.defaultColor;

		int fColor = cp.fColor;
		if (standout)
		{
			int fColorInt = color % 8;
			fColorInt += (fColor < 8 ? 8 : -8);
			fColor = TermWindow.color_table.get(fColorInt);
		}

		// if (p.Char != ' ') { Formatter fmt = new Formatter();
		// fmt.format("fcolor:%x bcolor:%x original:%x", fColor, cp.bColor,
		// p.Color);
		// Log.d("Crawl","frosh '"+p.Char+"' "+fmt); }

		if (reverse)
			term.drawPoint(r, c, p.Char, cp.bColor, fColor, extendErase);
		else
			term.drawPoint(r, c, p.Char, fColor, cp.bColor, extendErase);

		p.isDirty = false;
		p.isUgly = false;
	}

	public void waddnstr(final int w, final int n, final byte[] cp)
	{
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
			{
				t.addnstr(n, cp);
			}
		}
	}

	public int mvwinch(final int w, final int r, final int c)
	{
		// Log.d(TAG, "mvwinch " +w+r+c);
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
				return t.mvinch(r, c);
			else
				return 0;
		}
	}

	public void init_pair(final int p, final int f, final int b)
	{
		// Log.d(TAG, "init_pair " +p+f+b);
		synchronized (display_lock)
		{
			TermWindow.init_pair(p, f, b);
		}
	}

	public void init_color(final int c, final int rgb)
	{
		// Log.d(TAG, "init_color " +c+rgb);
		synchronized (display_lock)
		{
			TermWindow.init_color(c, rgb);
		}
	}

	public void scroll(final int w)
	{
		// Log.d(TAG, "scroll " +w);
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
				t.scroll();
		}
	}

	public int wattrget(final int w, final int r, final int c)
	{
		// Log.d(TAG, "wattrget "+ w+r+c);
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
				return t.attrget(r, c);
			else
				return 0;
		}
	}

	public void wattrset(final int w, final int a)
	{
		// Log.d(TAG, "wattrset "+ w+a);
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
				t.attrset(a);
		}
	}

	public void whline(final int w, final byte c, final int n)
	{
		// Log.d(TAG, "whline "+ w+c+n);
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
				t.hline((char) c, n);
		}
	}

	public void wclear(final int w)
	{
		// Log.d(TAG, "wclear "+ w);
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
			{
				t.clear();
				t.move(0, 0);
			}
		}
	}

	public void wclrtoeol(final int w)
	{
		// Log.d(TAG, "wclrtoeol "+ w);
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
				t.clrtoeol();
		}
	}

	public void wclrtobot(final int w)
	{
		// Log.d(TAG, "wclrtobot "+ w);
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
				t.clrtobot();
		}
	}

	public void noise()
	{
		// Log.d(TAG, "noise");
		synchronized (display_lock)
		{
			if (term != null)
				term.noise();
		}
	}

	public void wmove(int w, final int y, final int x)
	{
		// Log.d(TAG, "wmove" + w + y + x);
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
				t.move(y, x);
		}
	}

	public void curs_set(final int v)
	{
		// Log.d(TAG, "curs_set" +v);
		if (v == 1)
		{
			state.stdscr.cursor_visible = true;
		}
		else if (v == 0)
		{
			state.stdscr.cursor_visible = false;
		}
	}

	public void touchwin(final int w)
	{
		// Log.d(TAG, "touchwin" +w);
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
				t.touch();
		}
	}

	public int newwin(final int rows, final int cols, final int begin_y, final int begin_x)
	{
		// Log.d(TAG, "newwin" + rows + cols + begin_y + begin_x);
		synchronized (display_lock)
		{
			int w = state.newWin(rows, cols, begin_y, begin_x);
			return w;
		}
	}

	public void delwin(final int w)
	{
		// Log.d(TAG, "delwin"+w);
		synchronized (display_lock)
		{
			state.delWin(w);
		}
	}

	public void initscr()
	{
		// Log.d(TAG, "initscr()");
		synchronized (display_lock)
		{
		}
	}

	public void overwrite(final int wsrc, final int wdst)
	{
		// Log.d(TAG, "overwrite()"+wsrc+wdst);
		synchronized (display_lock)
		{
			TermWindow td = state.getWin(wdst);
			TermWindow ts = state.getWin(wsrc);
			if (td != null && ts != null)
				td.overwrite(ts);
		}
	}

	int getcury(final int w)
	{
		// Log.d(TAG, "getCury" + w);
		TermWindow t = state.getWin(w);
		if (t != null)
		{
			Log.d(TAG, "t not null, returning " + t.getcury());
			return t.getcury();
		}
		else
		{
			Log.d(TAG, "t was NULL, returning 0");
			return 0;
		}
	}

	int getcurx(final int w)
	{
		// Log.d(TAG, "getCurx" + w);
		TermWindow t = state.getWin(w);
		if (t != null)
			return t.getcurx();
		else
			return 0;
	}

	public int wctomb(byte[] pmb, byte character)
	{
		byte[] ba = new byte[1];
		ba[0] = character;
		byte[] wc;
		int wclen = 0;
		// Log.d(TAG,"wctomb("+pmb+","+character+")");
		try
		{
			String str = new String(ba, "ISO-8859-1");
			wc = str.getBytes("UTF-8");
			for (int i = 0; i < wc.length; i++)
			{
				pmb[i] = wc[i];
				wclen++;
			}
		}
		catch (java.io.UnsupportedEncodingException e)
		{
			// Log.d(TAG, "wctomb: " + e);
		}
		return wclen;
	}

	public int mbstowcs(final byte[] wcstr, final byte[] mbstr, final int max)
	{
		// Log.d(TAG,"mbstowcs("+wcstr+","+mbstr+","+max+")");
		try
		{
			String str = new String(mbstr, "UTF-8");
			// Log.d("Crawl", "str = |" + str + "|");
			byte[] wc = str.getBytes("ISO-8859-1");
			// Log.d("Crawl", "wc.length = " + wc.length);
			// Log.d("Crawl", "wcstr.length = " + wcstr.length);
			int i;
			if (max == 0)
			{
				// someone just wants to check the length
				return wc.length;
			}
			for (i = 0; i < wc.length && i < max; i++)
			{
				// Log.d("Crawl", "i = " + i);
				wcstr[i] = wc[i];
			}
			return i;
		}
		catch (java.io.UnsupportedEncodingException e)
		{
			Log.d(TAG, "mbstowcs: " + e);
		}
		return -1;
	}

	public int wcstombs(final byte[] mbstr, final byte[] wcstr, final int max)
	{
		// Log.d(TAG,"mcstombs("+mbstr+","+wcstr+","+max+")");
		try
		{
			String str = new String(wcstr, "ISO-8859-1");
			// Log.d("Crawl", "str = |" + str + "|");
			byte[] mb = str.getBytes("UTF-8");
			// Log.d("Crawl", "mb.length = " + mb.length);
			// Log.d("Crawl", "mbstr.length = " + mbstr.length);
			int i;
			if (max == 0)
			{
				// someone just wants to check the length
				return mb.length;
			}
			for (i = 0; i < mb.length && i < max; i++)
			{
				// Log.d("Crawl", "i = " + i);
				mbstr[i] = mb[i];
			}
			return i;
		}
		catch (java.io.UnsupportedEncodingException e)
		{
			// Log.d("Crawl", "wcstombs: " + e);
		}
		return -1;
	}

	// Score myComplexScore;
	HashMap scoreMap = null;

	public void score_submit(final byte[] score, final byte[] level)
	{
		// Log.d(TAG,"score_submit WTAT");
		Double myscore = 0.0;
		int mylevel = 0;
		try
		{
			String strScore = new String(score, "UTF-8");
			String strLevel = new String(level, "UTF-8");
			// Log.d("Crawl","score = \"" + strScore + "\"");
			myscore = Double.parseDouble(strScore.trim());
			mylevel = Integer.parseInt(strLevel.trim());
		}
		catch (java.io.UnsupportedEncodingException e)
		{
			// Log.d("Crawl", "score: " + e);
		}
		// myComplexScore = new Score(new Double(myscore), scoreMap);
		// myComplexScore.setLevel(mylevel);
		// state.handler.sendMessage(state.handler.obtainMessage(Crawl.Action.Score.ordinal(),
		// 0, 0,
		// myComplexScore));
	}

	public void score_start()
	{
		// Log.d(TAG,"score_start() WTAT");
		scoreMap = new HashMap();
	}

	public void score_detail(final byte[] name, final byte[] value)
	{
		// Log.d(TAG,"score_detail() WTAT");
		try
		{
			String strName = new String(name, "UTF-8");
			String strValue = new String(value, "UTF-8");
			// Log.d("Crawl","score detail: \"" + strName + "\" = \"" +
			// strValue + "\"");
			scoreMap.put(strName, strValue.trim());
		}
		catch (java.io.UnsupportedEncodingException e)
		{
			Log.d("Crawl", "score: " + e);
		}
	}

	public void fakecursorxy(int x, int y, int w)
	{
		synchronized (display_lock)
		{
			TermWindow t = state.getWin(w);
			if (t != null)
			{
				t.fakecursorxy(x - 1, y - 1);
			}
		}
	}
}
