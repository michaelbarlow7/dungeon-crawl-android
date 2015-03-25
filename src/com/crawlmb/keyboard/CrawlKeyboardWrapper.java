package com.crawlmb.keyboard;

import com.crawlmb.R;
import com.crawlmb.StateManager;
import com.crawlmb.activity.GameActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.inputmethodservice.Keyboard;

public class CrawlKeyboardWrapper implements CrawlKeyboardView.OnKeyboardActionListener
{
	public CrawlKeyboardView virtualKeyboardView;
	public Keyboard virtualKeyboardQwerty;
	public Keyboard virtualKeyboardSymbols;
	public Keyboard virtualKeyboardSymbolsShift;

    public static enum KeyboardType {
        QWERTY,
        SYMBOLS,
        SYMBOLS_SHIFT
    }

	StateManager state = null;

	public CrawlKeyboardWrapper(Context ctx)
	{
		state = ((GameActivity)ctx).getStateManager();

		virtualKeyboardQwerty = new Keyboard(ctx, R.xml.keyboard_qwerty);
		virtualKeyboardSymbols = new Keyboard(ctx, R.xml.keyboard_sym);
		virtualKeyboardSymbolsShift = new Keyboard(ctx, R.xml.keyboard_symshift);
		LayoutInflater inflater = LayoutInflater.from(ctx);
		virtualKeyboardView = (CrawlKeyboardView)inflater.inflate(
				R.layout.input, null);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		virtualKeyboardView.setLayoutParams(layoutParams);
		virtualKeyboardView.setKeyboard(virtualKeyboardQwerty, KeyboardType.QWERTY);
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
				virtualKeyboardView.setKeyboard(virtualKeyboardQwerty, KeyboardType.QWERTY);
			}
			else
			{
				virtualKeyboardView.setKeyboard(virtualKeyboardSymbolsShift, KeyboardType.SYMBOLS_SHIFT);
			}
		}
		else if(primaryCode == Keyboard.KEYCODE_MODE_CHANGE)
		{
			Keyboard current = virtualKeyboardView.getKeyboard();
			if(current == virtualKeyboardSymbols)
			{
                virtualKeyboardView.setKeyboard(virtualKeyboardQwerty, KeyboardType.QWERTY);
			}
			else
			{
                virtualKeyboardView.setKeyboard(virtualKeyboardSymbols, KeyboardType.SYMBOLS);
                virtualKeyboardView.setShifted(false);
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
