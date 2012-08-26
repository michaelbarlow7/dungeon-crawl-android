package com.crawlmb;

import android.content.Context;
import android.view.LayoutInflater;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;

public class CrawlKeyboard implements OnKeyboardActionListener
{
	KeyboardView virtualKeyboardView;
	Keyboard virtualKeyboardQwerty;
	Keyboard virtualKeyboardSymbols;
	Keyboard virtualKeyboardSymbolsShift;
	StateManager state = null;

	CrawlKeyboard(Context ctx)
	{
		state = ((GameActivity)ctx).getStateManager();

		virtualKeyboardQwerty = new Keyboard(ctx, R.xml.keyboard_qwerty);
		virtualKeyboardSymbols = new Keyboard(ctx, R.xml.keyboard_sym);
		virtualKeyboardSymbolsShift = new Keyboard(ctx, R.xml.keyboard_symshift);
		LayoutInflater inflater = LayoutInflater.from(ctx);
		virtualKeyboardView = (KeyboardView)inflater.inflate(
				R.layout.input, null);
		virtualKeyboardView.setKeyboard(virtualKeyboardQwerty);
		virtualKeyboardView.setOnKeyboardActionListener(this);
	}

	private void handleShift()
	{
		Keyboard currentKeyboard = virtualKeyboardView.getKeyboard();

		if(currentKeyboard == virtualKeyboardQwerty)
		{
			// checkToggleCapsLock();
			virtualKeyboardView.setShifted(/*capslock*/ !virtualKeyboardView.isShifted());
		}
	}
	
	public void onKey(int primaryCode, int[] keyCodes)
	{
		char c = 0;
		if(primaryCode == Keyboard.KEYCODE_DELETE)
		{
			c = 8;
		}
		else if(primaryCode == Keyboard.KEYCODE_SHIFT)
		{
			handleShift();
		}
		else if(primaryCode == Keyboard.KEYCODE_ALT)
		{
			Keyboard current = virtualKeyboardView.getKeyboard();
			if(current == virtualKeyboardSymbolsShift)
			{
				virtualKeyboardView.setKeyboard(virtualKeyboardQwerty);
			}
			else
			{
				virtualKeyboardView.setKeyboard(virtualKeyboardSymbolsShift);
			}
		}
		else if(primaryCode == Keyboard.KEYCODE_MODE_CHANGE)
		{
			Keyboard current = virtualKeyboardView.getKeyboard();
			if(current == virtualKeyboardSymbols)
			{
				current = virtualKeyboardQwerty;
			}
			else
			{
				current = virtualKeyboardSymbols;
			}
			virtualKeyboardView.setKeyboard(current);
			if(current == virtualKeyboardSymbols)
			{
				current.setShifted(false);
			}
		}
       	else
		{
			c = (char)primaryCode;			
			if(virtualKeyboardView.getKeyboard() == virtualKeyboardQwerty && virtualKeyboardView.isShifted())
			{
				c = Character.toUpperCase(c);
				virtualKeyboardView.setShifted(false);
			}
		}
		if(c != 0)
		{
			state.addKey(c);
		}
	}
	
	public void onPress(int primaryCode)
	{
	}
	public void onRelease(int primaryCode)
	{
	}
	public void onText(CharSequence text)
	{
	}
	public void swipeDown()
	{
	}
	public void swipeLeft()
	{
	}
	public void swipeRight()
	{
	}
	public void swipeUp()
	{
	}
}
