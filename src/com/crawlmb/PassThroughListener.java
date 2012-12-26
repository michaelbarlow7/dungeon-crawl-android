package com.crawlmb;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public interface PassThroughListener
{
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
  
  public boolean onScale(ScaleGestureDetector detector);
  
  public boolean onScaleBegin(ScaleGestureDetector detector);
  
  public void savePosition();

}
