package com.crawlmb;

import android.view.View;
import android.view.inputmethod.BaseInputConnection;

public class CrawlInputConnection extends BaseInputConnection
{
	TermView termView;

	public CrawlInputConnection(View targetView, boolean fullEditor) 
	{
		super(targetView, fullEditor);
		termView = (TermView) targetView;
	}

	public boolean commitText(CharSequence text, int newCursorPosition) 
	{	
		if (text.length() == 0)
		{
			return true;
		}
		char c = text.charAt(0);
		termView.addKey(c);
		return true;
	}
}
