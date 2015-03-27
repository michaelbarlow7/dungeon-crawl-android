package com.crawlmb.keyboard;

import com.crawlmb.R;
import com.crawlmb.keylistener.KeyListener;

import android.content.Context;
import android.util.SparseArray;
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
    public static enum SpecialKey
    {
        ARROWDOWNKEY(R.drawable.sym_keyboard_down, 0402),
        ARROWLEFTKEY(R.drawable.sym_keyboard_left, 0404),
        ARROWRIGHTKEY(R.drawable.sym_keyboard_right, 0405),
        ARROWUPKEY(R.drawable.sym_keyboard_up, 0403),
        ENTERKEY(R.drawable.sym_keyboard_return, 13),
        ESCKEY(R.drawable.sym_keyboard_esc, 0x1B),
        TAB(R.drawable.sym_keyboard_tab, '\t'),
        BACKSPACEKEY(R.drawable.sym_keyboard_delete, 0x9F);

        private final int resourceId;
        private final int code;
        private static SparseArray<SpecialKey> codeToKeyMap;

        SpecialKey(int resourceId, int code){
            this.resourceId = resourceId;
            this.code = code;
            getCodeToKeyMap().put(code, this);
        }

        public static SparseArray<SpecialKey> getCodeToKeyMap(){
            if (codeToKeyMap == null){
                codeToKeyMap = new SparseArray<SpecialKey>();
            }
            return codeToKeyMap;
        }

        public int getResourceId(){
            return resourceId;
        }

        public int getCode(){
            return code;
        }

    }

	KeyListener keyListener = null;

	public CrawlKeyboardWrapper(Context context, KeyListener keyListener)
	{
		this.keyListener = keyListener;

		virtualKeyboardQwerty = new Keyboard(context, R.xml.keyboard_qwerty);
		virtualKeyboardSymbols = new Keyboard(context, R.xml.keyboard_sym);
		virtualKeyboardSymbolsShift = new Keyboard(context, R.xml.keyboard_symshift);
		LayoutInflater inflater = LayoutInflater.from(context);
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

    @Override
	public void onKey(int keyIndex, int primaryCode, int[] keyCodes)
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
			keyListener.addKey(c, keyIndex);
		}
	}

    public KeyboardType getCurrentKeyboardType(){
        Keyboard current = virtualKeyboardView.getKeyboard();
        if ( current == virtualKeyboardQwerty){
            return KeyboardType.QWERTY;
        }
        if ( current == virtualKeyboardSymbols){
            return KeyboardType.SYMBOLS;
        }
        if ( current == virtualKeyboardSymbolsShift){
            return KeyboardType.SYMBOLS_SHIFT;
        }

        // Shouldn't happen
        return null;
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
