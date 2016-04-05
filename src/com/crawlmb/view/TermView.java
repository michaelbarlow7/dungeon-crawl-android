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

package com.crawlmb.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.ScaleGestureDetector;

import com.crawlmb.CrawlDialog;
import com.crawlmb.CrawlInputConnection;
import com.crawlmb.keylistener.GameKeyListener;
import com.crawlmb.PassThroughListener;
import com.crawlmb.Preferences;
import com.crawlmb.R;
import com.crawlmb.activity.GameActivity;
import com.crawlmb.keylistener.KeyListener;

import java.util.Hashtable;

public class TermView extends View implements GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener, PassThroughListener
{

	private static final String LOCK_POSITIONING_PREFERENCE = "lockPositioningPreference";
  private static final String SCALE_FACTOR_PREFERENCE = "scaleFactor";
	private static final String SCROLL_X_PREFERENCE = "scrollX";
	private static final String SCROLL_Y_PREFERENCE = "scrollY";
	private static final String TAG = "TermView";
	public static final int MAX_FONT_SIZE = 48;
	public static final int MIN_FONT_SIZE = 6;
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

	private Handler handler = null;
	private KeyListener keyListener = null;

	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleGestureDetector;
	
	private float scaleFactor;
	
	private boolean lockPositioning;

	public TermView(Context context, KeyListener keyListener)
	{
		super(context);
		initTermView(context);
		handler = ((GameActivity) context).getHandler();
		this.keyListener = keyListener;
	}

	public TermView(Context context, AttributeSet attrs, GameKeyListener keyListener)
	{
		super(context, attrs);
		initTermView(context);
		handler = ((GameActivity) context).getHandler();
		this.keyListener = keyListener;
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
	  SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
	  scaleFactor = preferences.getFloat(SCALE_FACTOR_PREFERENCE, 1.0f);
	  scrollTo(preferences.getInt(SCROLL_X_PREFERENCE, 0), preferences.getInt(SCROLL_Y_PREFERENCE, 0));
	  lockPositioning = preferences.getBoolean(LOCK_POSITIONING_PREFERENCE, false);
	      
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

		setFocusableInTouchMode(true);
		gestureDetector = new GestureDetector(context, this);
		scaleGestureDetector = new ScaleGestureDetector(context, this);
	}

	protected void onDraw(Canvas canvas)
	{
		if (bitmap != null)
		{
	    canvas.scale(scaleFactor, scaleFactor);
			canvas.drawBitmap(bitmap, 0, 0, null);
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
		keyListener.addKey(c, 0);
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
			font_text_size = MIN_FONT_SIZE;
			do
			{
				font_text_size += 1;
				setFontSize(font_text_size, false);
			} while (char_height * Preferences.rows <= maxHeight && font_text_size < MAX_FONT_SIZE);

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
			font_text_size = MIN_FONT_SIZE;
			do
			{
				font_text_size += 1;
				setFontSize(font_text_size, false);
			} while (char_width * Preferences.cols <= maxWidth && font_text_size < MAX_FONT_SIZE);

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
			tfTiny = getTypeface("6x12.ttf");
			fore.setTypeface(tfTiny);
		}
		else
		{
			tfStd = getTypeface("VeraMoBd.ttf");
			fore.setTypeface(tfStd);
		}
	}

	private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

	public Typeface getTypeface(String assetPath) {
		synchronized (cache) {
			if (!cache.containsKey(assetPath)) {
				try {
					Typeface t = Typeface.createFromAsset(getContext().getAssets(),
							assetPath);
					cache.put(assetPath, t);
				} catch (Exception e) {
					Log.e("Crawl", "Could not get typeface '" + assetPath
							+ "' because " + e.getMessage());
					return null;
				}
			}
			return cache.get(assetPath);
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

		if (size < MIN_FONT_SIZE)
			size = MIN_FONT_SIZE;
		else if (size > MAX_FONT_SIZE)
			size = MAX_FONT_SIZE;

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
	  if (me.getAction() == MotionEvent.ACTION_UP)
	  {
	    // Save position data once the user has finished manipulating the TermView
	    savePosition();
	  }
	  boolean scaleGestureHandled =  scaleGestureDetector.onTouchEvent(me);
	  boolean gestureHandled = gestureDetector.onTouchEvent(me);
	  return scaleGestureHandled || gestureHandled;
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
	  if (lockPositioning)
	  {
	    return false;
	  }
		int newscrollx = this.getScrollX() + (int) distanceX;
		int newscrolly = this.getScrollY() + (int) distanceY;

		if (newscrollx < 0)
			newscrollx = 0;
		if (newscrolly < 0)
			newscrolly = 0;
		
		if (newscrollx >= canvas_width*scaleFactor - getWidth())
			newscrollx = (int) (canvas_width*scaleFactor - getWidth() + 1);
		if (newscrolly >= canvas_height*scaleFactor - getHeight())
			newscrolly = (int) (canvas_height*scaleFactor - getHeight() + 1);

		if (canvas_width*scaleFactor <= getWidth())
			newscrollx = 0; // this.getScrollX();
		if (canvas_height*scaleFactor <= getHeight())
			newscrolly = 0; // this.getScrollY();

		scrollTo(newscrollx, newscrolly); //TODO: Do this at the beginning according to preferences
		

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

	@Override
	public boolean onSingleTapUp(MotionEvent event)
	{
		if (!Preferences.getEnableTouch())
			return false;
		performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

		int x = (int) event.getX();
		int y = (int) event.getY();

		int r, c;
		c = (x * 3) / getWidth();
		r = (y * 3) / getHeight();

		int key = (2 - r) * 3 + c + 1;

		switch (key)
		{
		case 1:
			key = GameKeyListener.KEY_C1;
			break;
		case 2:
			key = GameKeyListener.KEY_DOWN;
			break;
		case 3:
			key = GameKeyListener.KEY_C3;
			break;
		case 4:
			key = GameKeyListener.KEY_LEFT;
			break;
	  case 5: 
      key = GameKeyListener.KEY_B2;
      break; 
		case 6:
			key = GameKeyListener.KEY_RIGHT;
			break;
		case 7:
			key = GameKeyListener.KEY_A1;
			break;
		case 8:
			key = GameKeyListener.KEY_UP;
			break;
		case 9:
			key = GameKeyListener.KEY_A3;
			break;
		default:
			break;
		}

		keyListener.addDirectionKey(key);

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

	//OnGestureListener methods
	@Override
	public void onLongPress(MotionEvent e)
	{
		performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		showContextMenu();
	}

	@Override
	public void onShowPress(MotionEvent e)
	{
	}

	//OnScaleGestureListener methods
  @Override
  public boolean onScale(ScaleGestureDetector detector)
  {
    if(lockPositioning)
    {
      return false;
    }
    scaleFactor *= detector.getScaleFactor();
    scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 2.0f));
    invalidate();
    return true;
  }
  
  @Override
  protected void onCreateContextMenu (ContextMenu menu)
  {
	MenuInflater inflater = new MenuInflater(getContext());
	inflater.inflate(R.menu.main, menu);
	menu.setHeaderTitle(R.string.menu);
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector detector)
  {
    if(lockPositioning)
    {
      return false;
    }
    return true;
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector detector)
  {
  }
  
  public void savePosition()
  {
	  SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
	  editor.putFloat(SCALE_FACTOR_PREFERENCE, scaleFactor);
	  editor.putInt(SCROLL_X_PREFERENCE, getScrollX());
	  editor.putInt(SCROLL_Y_PREFERENCE, getScrollY());
	  editor.commit();
  }
  
  public void resetTerminalPosition()
  {
    scrollTo(0, 0);
    scaleFactor = 1.0f;
    savePosition();
    invalidate();
  }
  
  public void toggleLockPosition()
  {
    lockPositioning = !lockPositioning;
	  SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
    editor.putBoolean(LOCK_POSITIONING_PREFERENCE, lockPositioning);
    editor.commit();
  }
  
  public boolean getLockPositioning()
  {
    return lockPositioning;
  }
}
