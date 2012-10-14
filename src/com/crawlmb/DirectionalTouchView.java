package com.crawlmb;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class DirectionalTouchView extends View implements  OnGestureListener
{

	private GestureDetector gesture;
	private StateManager state = null;
	private OnGestureListener passThroughGestureListener;
	
	public DirectionalTouchView(Context context) 
	{
		super(context);
		gesture = new GestureDetector(context, this);
		state = ((GameActivity) context).getStateManager();
	}
	
	public void setTermView(OnGestureListener onGestureListener)
	{
		this.passThroughGestureListener = onGestureListener;
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
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
	{
		//We want to pass the scroll motion to the terminal view
		passThroughGestureListener.onScroll(e1, e2, distanceX, distanceY);
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
		case 5: 
		   key = StateManager.KEY_B2; 
		   break; 
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
	public boolean onTouchEvent(MotionEvent event)
	{
		return gesture.onTouchEvent(event);
	}

}
