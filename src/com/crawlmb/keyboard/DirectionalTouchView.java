package com.crawlmb.keyboard;

import android.content.Context;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.ScaleGestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.crawlmb.keylistener.GameKeyListener;
import com.crawlmb.PassThroughListener;
import com.crawlmb.Preferences;
import com.crawlmb.keylistener.KeyListener;

public class DirectionalTouchView extends View implements  GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener
{

	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleGestureDetector;
	private KeyListener keyListener = null;
	private PassThroughListener passThroughListener;
	
	public DirectionalTouchView(Context context, KeyListener keyListener)
	{
		super(context);
		gestureDetector = new GestureDetector(context, this);
		scaleGestureDetector = new ScaleGestureDetector(context, this);
		this.keyListener = keyListener;
	}
	
	public void setPassThroughListener(PassThroughListener onGestureListener)
	{
		this.passThroughListener = onGestureListener;
	}
	

	@Override
	public boolean onDown(MotionEvent e) 
	{
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
	{
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) 
	{
		passThroughListener.onLongPress(e);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
	{
		//We want to pass the scroll motion to the terminal view
		passThroughListener.onScroll(e1, e2, distanceX, distanceY);
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) 
	{
		
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
	public boolean onTouchEvent(MotionEvent event)
	{
	  if (event.getAction() == MotionEvent.ACTION_UP)
    {
      // Save position data once the user has finished manipulating the TermView
      passThroughListener.savePosition();
    }
    boolean scaleGestureHandled =  scaleGestureDetector.onTouchEvent(event);
    boolean gestureHandled = gestureDetector.onTouchEvent(event);
    return scaleGestureHandled || gestureHandled;
	}

  @Override
  public boolean onScale(ScaleGestureDetector detector)
  {
    return passThroughListener.onScale(detector);
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector detector)
  {
    return passThroughListener.onScaleBegin(detector);
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector detector)
  {
  }

}
