package com.crawlmb;

import android.content.Context;
import android.util.AttributeSet;

public class KeyMapModPreference extends KeyMapPreference {

	public KeyMapModPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean handleModifier(int keyCode) {
		return false;
	}
}
