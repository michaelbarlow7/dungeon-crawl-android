/*
 * File: TermView.java Purpose: Terminal-base view for Android application
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class TermView extends View implements OnGestureListener
{

	Typeface tfStd;
	Typeface tfTiny;
	Bitmap bitmap;
	Canvas canvas;
	Paint fore;
	Paint back;
	Paint cursor;

	public int canvas_width = 0;
	public int canvas_height = 0;

	private int char_height = 0;
	private int char_width = 0;
	private int font_text_size = 0;

	private Vibrator vibrator;
	private boolean vibrate;
	private Handler handler = null;
	private StateManager state = null;

	private GestureDetector gesture;

	public TermView(Context context)
	{
		super(context);
		initTermView(context);
		handler = ((GameActivity) context).getHandler();
		state = ((GameActivity) context).getStateManager();
	}

	public TermView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initTermView(context);
		handler = ((GameActivity) context).getHandler();
		state = ((GameActivity) context).getStateManager();
	}
	
	@Override
	public boolean onCheckIsTextEditor()
	{
		return true;
	}
	
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs)
	{
		outAttrs.inputType = InputType.TYPE_NULL;
		return new CrawlInputConnection(this, false);
	}

	protected void initTermView(Context context)
	{
		fore = new Paint();
		fore.setTextAlign(Paint.Align.LEFT);
		if (isHighRes())
			fore.setAntiAlias(true);
		setForeColor(Color.WHITE);

		back = new Paint();
		setBackColor(Color.BLACK);

		cursor = new Paint();
		cursor.setColor(Color.GREEN);
		cursor.setStyle(Paint.Style.STROKE);
		cursor.setStrokeWidth(0);

		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

		setFocusableInTouchMode(true);
		gesture = new GestureDetector(context, this);
	}

	protected void onDraw(Canvas canvas)
	{
		if (bitmap != null)
		{
			canvas.drawBitmap(bitmap, 0, 0, null);

			int x = state.stdscr.col * (char_width);
			int y = (state.stdscr.row + 1) * char_height;

			// due to font "scrunch", cursor is sometimes a bit too big
			int cl = Math.max(x, 0);
			int cr = Math.min(x + char_width, canvas_width - 1);
			int ct = Math.max(y - char_height, 0);
			int cb = Math.min(y, canvas_height - 1);

			if (state.stdscr.cursor_visible)
			{
				canvas.drawRect(cl, ct, cr, cb, cursor);
			}
		}
	}

	public void computeCanvasSize()
	{
		canvas_width = Preferences.cols * char_width;
		canvas_height = Preferences.rows * char_height;
	}

	protected void setForeColor(int a)
	{
		fore.setColor(a);
	}

	protected void setBackColor(int a)
	{
		back.setColor(a);
	}
	
	public void addKey(int c)
	{
		state.addKey(c);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// We want to pass all KeyEvents to the activity
		return false;
	}


	public void autoSizeFontByHeight(int maxHeight)
	{
		if (maxHeight == 0)
			maxHeight = getMeasuredHeight();
		setFontFace();

		// HACK -- keep 480x320 fullscreen as-is
		if (!isHighRes())
		{
			setFontSizeLegacy();
		}
		else
		{
			font_text_size = 6;
			do
			{
				font_text_size += 1;
				setFontSize(font_text_size, false);
			} while (char_height * Preferences.rows <= maxHeight);

			font_text_size -= 1;
			setFontSize(font_text_size);
		}
		// Log.d("Crawl","autoSizeFontHeight "+font_text_size);
	}

	public void autoSizeFontByWidth(int maxWidth)
	{
		if (maxWidth == 0)
			maxWidth = getMeasuredWidth();
		setFontFace();

		// HACK -- keep 480x320 fullscreen as-is
		if (!isHighRes())
		{
			setFontSizeLegacy();
		}
		else
		{
			font_text_size = 6;
			do
			{
				font_text_size += 1;
				setFontSize(font_text_size, false);
			} while (char_width * Preferences.cols <= maxWidth);

			font_text_size -= 1;
			setFontSize(font_text_size);
		}
		// Log.d("Crawl","autoSizeFontWidth "+font_text_size+","+maxWidth);
	}

	public boolean isHighRes()
	{
		Display display =
				((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int maxWidth = display.getWidth();
		int maxHeight = display.getHeight();

		// Log.d("Crawl","isHighRes "+maxHeight+","+maxWidth +","+
		// (Math.max(maxWidth,maxHeight)>480));
		return Math.max(maxWidth, maxHeight) > 480;
	}

	private void setFontSizeLegacy()
	{
		font_text_size = 12;
		char_height = 12;
		char_width = 6;
		setFontSize(font_text_size);
	}

	private void setFontFace()
	{
		if (!isHighRes())
		{
			tfTiny = Typeface.createFromAsset(getResources().getAssets(), "6x12.ttf");
			fore.setTypeface(tfTiny);
		}
		else
		{
			tfStd = Typeface.createFromAsset(getResources().getAssets(), "VeraMoBd.ttf");
			// tfStd = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
			fore.setTypeface(tfStd);
		}
	}

	public void increaseFontSize()
	{
		setFontSize(font_text_size + 1);
	}

	public void decreaseFontSize()
	{
		setFontSize(font_text_size - 1);
	}

	private void setFontSize(int size)
	{
		setFontSize(size, true);
	}

	private void setFontSize(int size, boolean persist)
	{

		setFontFace();

		if (size < 6)
			size = 6;
		else if (size > 48)
			size = 48;

		font_text_size = size;

		fore.setTextSize(font_text_size);

		if (persist)
		{
			if (Preferences.isScreenPortraitOrientation())
				Preferences.setPortraitFontSize(font_text_size);
			else
				Preferences.setLandscapeFontSize(font_text_size);
		}

		char_height = (int) Math.ceil(fore.getFontSpacing());
		char_width = (int) fore.measureText("X", 0, 1);
		// Log.d("Crawl","setSizeFont "+fore.measureText("X", 0, 1));
	}

	@Override
	protected void onMeasure(int widthmeasurespec, int heightmeasurespec)
	{
		int height = MeasureSpec.getSize(heightmeasurespec);
		int width = MeasureSpec.getSize(widthmeasurespec);

		int fs = 0;
		if (Preferences.isScreenPortraitOrientation())
			fs = Preferences.getPortraitFontSize();
		else
			fs = Preferences.getLandscapeFontSize();

		if (fs == 0)
			autoSizeFontByWidth(width);
		else
			setFontSize(fs, false);

		fore.setTextAlign(Paint.Align.LEFT);

		setMeasuredDimension(width, height);
		// Log.d("Crawl","onMeasure "+canvas_width+","+canvas_height+";"+width+","+height);
	}

	@Override
	public boolean onTouchEvent(MotionEvent me)
	{
		return gesture.onTouchEvent(me);
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		int newscrollx = this.getScrollX() + (int) distanceX;
		int newscrolly = this.getScrollY() + (int) distanceY;

		if (newscrollx < 0)
			newscrollx = 0;
		if (newscrolly < 0)
			newscrolly = 0;
		if (newscrollx >= canvas_width - getWidth())
			newscrollx = canvas_width - getWidth() + 1;
		if (newscrolly >= canvas_height - getHeight())
			newscrolly = canvas_height - getHeight() + 1;

		if (canvas_width <= getWidth())
			newscrollx = 0; // this.getScrollX();
		if (canvas_height <= getHeight())
			newscrolly = 0; // this.getScrollY();

		scrollTo(newscrollx, newscrolly);

		return true;
	}

	public boolean onDown(MotionEvent e)
	{
		return true;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		return true;
	}

	public boolean onSingleTapUp(MotionEvent event)
	{
		if (!Preferences.getEnableTouch())
			return false;

		int x = (int) event.getX();
		int y = (int) event.getY();

		int r, c;
		c = (x * 3) / getWidth();
		r = (y * 3) / getHeight();

		int key = (2 - r) * 3 + c + 1;

		switch (key)
		{
		case 1:
			key = StateManager.KEY_C1;
			break;
		case 2:
			key = StateManager.KEY_DOWN;
			break;
		case 3:
			key = StateManager.KEY_C3;
			break;
		case 4:
			key = StateManager.KEY_LEFT;
			break;
		// case '5': key = ' '; break; // now configurable below
		case 6:
			key = StateManager.KEY_RIGHT;
			break;
		case 7:
			key = StateManager.KEY_A1;
			break;
		case 8:
			key = StateManager.KEY_UP;
			break;
		case 9:
			key = StateManager.KEY_A3;
			break;
		default:
			break;
		}

		state.addDirectionKey(key);

		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		// Log.d("Crawl", "onSizeChanged");
		super.onSizeChanged(w, h, oldw, oldh);
		handler.sendEmptyMessage(CrawlDialog.Action.StartGame.ordinal());
	}

	public boolean onGameStart()
	{

		computeCanvasSize();

		// sanity
		if (canvas_width == 0 || canvas_height == 0)
			return false;

		// Log.d("Crawl","createBitmap "+canvas_width+","+canvas_height);
		bitmap = Bitmap.createBitmap(canvas_width, canvas_height, Bitmap.Config.RGB_565);
		canvas = new Canvas(bitmap);
		/*
		 * canvas.setDrawFilter(new PaintFlagsDrawFilter( Paint.DITHER_FLAG |
		 * Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG,0 )); // this seems
		 * to have no effect, why?
		 */

		return true;
	}

	public void drawPoint(int r, int c, char ch, int fcolor, int bcolor, boolean extendedErase)
	{
		float x = c * char_width;
		float y = r * char_height;

		if (canvas == null)
		{
			// OnSizeChanged has not been called yet
			Log.d("Crawl", "null canvas in drawPoint");
			return;
		}

		setBackColor(bcolor);

		canvas.drawRect(x, y, x + char_width + (extendedErase ? 1 : 0), y + char_height
				+ (extendedErase ? 1 : 0), back);

		if (ch != ' ')
		{
			String str = ch + "";

			setForeColor(fcolor);

			canvas.drawText(str, x, y + char_height - fore.descent(), fore);
		}
	}

	public void clear()
	{
		if (canvas != null)
		{
			canvas.drawPaint(back);
		}
	}

	public void noise()
	{
		if (vibrate)
		{
			vibrator.vibrate(50);
		}
	}

	public void onResume()
	{
		// Log.d("Crawl","Termview.onResume()");
		vibrate = Preferences.getVibrate();
	}

	public void onPause()
	{
		// Log.d("Crawl","Termview.onPause()");
		// this is the only guaranteed safe place to save state
		// according to SDK docs
		// state.gameThread.send(GameThread.Request.SaveGame);
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
	}

	@Override
	public void onShowPress(MotionEvent e)
	{
	}
}
