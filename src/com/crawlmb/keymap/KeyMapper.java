package com.crawlmb.keymap;

import java.util.Iterator;
import java.util.HashMap;
import android.view.KeyEvent;
import android.content.SharedPreferences;

public class KeyMapper
{

	String KEY_KEYMAPVERSION = "crawl.keymapversion";

	String KEY_VIRTKEYKEY = "crawl.virtkeykey";
	String KEY_ZOOMINKEY = "crawl.zoominkey";
	String KEY_ZOOMOUTKEY = "crawl.zoomoutkey";

	String KEY_CTRLKEY = "crawl.ctrlkey";
	String KEY_ESCKEY = "crawl.esckey";
	String KEY_LALTKEY = "crawl.laltkey";
	String KEY_RALTKEY = "crawl.raltkey";
	String KEY_LSHIFTKEY = "crawl.lshiftkey";
	String KEY_RSHIFTKEY = "crawl.rshiftkey";
	String KEY_ENTERKEY = "crawl.enterkey";
	String KEY_SPACEKEY = "crawl.spacekey";
	String KEY_TABKEY = "crawl.tabkey";
	String KEY_BKSPACEKEY = "crawl.bkspacekey";
	String KEY_UPKEY = "crawl.upkey";
	String KEY_DOWNKEY = "crawl.downkey";
	String KEY_LEFTKEY = "crawl.leftkey";
	String KEY_RIGHTKEY = "crawl.rightkey";

	String KEY_AMPKEY = "crawl.ampkey";
	String KEY_ASTKEY = "crawl.astkey";
	String KEY_ATKEY = "crawl.atkey";
	String KEY_BSLASHKEY = "crawl.bslashkey";
	String KEY_COLONKEY = "crawl.colonkey";
	String KEY_COMMAKEY = "crawl.commakey";
	String KEY_DOLLARKEY = "crawl.dollarkey";
	String KEY_DQUOTEKEY = "crawl.dquotekey";
	String KEY_EQUALKEY = "crawl.equalkey";
	String KEY_EXCLKEY = "crawl.exclkey";
	String KEY_FSLASHKEY = "crawl.fslashkey";
	String KEY_GTKEY = "crawl.gtkey";
	String KEY_LBKEY = "crawl.lbkey";
	String KEY_LCKEY = "crawl.lckey";
	String KEY_LPKEY = "crawl.lpkey";
	String KEY_LTKEY = "crawl.ltkey";
	String KEY_MINUSKEY = "crawl.minuskey";
	String KEY_PERCENTKEY = "crawl.percentkey";
	String KEY_PERIODKEY = "crawl.periodkey";
	String KEY_PIPEKEY = "crawl.pipekey";
	String KEY_PLUSKEY = "crawl.pluskey";
	String KEY_POUNDKEY = "crawl.poundkey";
	String KEY_QUESTKEY = "crawl.questkey";
	String KEY_RBKEY = "crawl.rbkey";
	String KEY_RCKEY = "crawl.rckey";
	String KEY_RPKEY = "crawl.rpkey";
	String KEY_SCOLONKEY = "crawl.scolonkey";
	String KEY_SQUOTEKEY = "crawl.squotekey";
	String KEY_TILDEKEY = "crawl.tildekey";
	String KEY_BACKTICKKEY = "crawl.backtickkey";
	String KEY_UNDERKEY = "crawl.underkey";

	String KEY_AKEY = "crawl.akey";
	String KEY_BKEY = "crawl.bkey";
	String KEY_CKEY = "crawl.ckey";
	String KEY_DKEY = "crawl.dkey";
	String KEY_EKEY = "crawl.ekey";
	String KEY_FKEY = "crawl.fkey";
	String KEY_GKEY = "crawl.gkey";
	String KEY_HKEY = "crawl.hkey";
	String KEY_IKEY = "crawl.ikey";
	String KEY_JKEY = "crawl.jkey";
	String KEY_KKEY = "crawl.kkey";
	String KEY_LKEY = "crawl.lkey";
	String KEY_MKEY = "crawl.mkey";
	String KEY_NKEY = "crawl.nkey";
	String KEY_OKEY = "crawl.okey";
	String KEY_PKEY = "crawl.pkey";
	String KEY_QKEY = "crawl.qkey";
	String KEY_RKEY = "crawl.rkey";
	String KEY_SKEY = "crawl.skey";
	String KEY_TKEY = "crawl.tkey";
	String KEY_UKEY = "crawl.ukey";
	String KEY_VKEY = "crawl.vkey";
	String KEY_WKEY = "crawl.wkey";
	String KEY_XKEY = "crawl.xkey";
	String KEY_YKEY = "crawl.ykey";
	String KEY_ZKEY = "crawl.zkey";

	String KEY_0KEY = "crawl.0key";
	String KEY_1KEY = "crawl.1key";
	String KEY_2KEY = "crawl.2key";
	String KEY_3KEY = "crawl.3key";
	String KEY_4KEY = "crawl.4key";
	String KEY_5KEY = "crawl.5key";
	String KEY_6KEY = "crawl.6key";
	String KEY_7KEY = "crawl.7key";
	String KEY_8KEY = "crawl.8key";
	String KEY_9KEY = "crawl.9key";

	private HashMap<String, KeyMap> keymapAll = new HashMap<String, KeyMap>(); // indexed
																				// by
																				// pref
																				// key
	private HashMap<String, KeyMap> keymapAssign = new HashMap<String, KeyMap>(); // indexed
																					// by
																					// key
																					// assignment
	private SharedPreferences pref;

	public enum KeyAction
	{
		None, AltKey, ArrowDownKey, ArrowLeftKey, ArrowRightKey, ArrowUpKey, CtrlKey, CharacterKey, EnterKey,
		EscKey, Period, ShiftKey, Space, Tab, VirtualKeyboard, ZoomIn, ZoomOut, BackspaceKey, DeleteKey;

		public static KeyAction convert(int value)
		{
			return KeyAction.class.getEnumConstants()[value];
		}

		public static KeyAction convert(String value)
		{
			return KeyAction.valueOf(value);
		}
	};

	public KeyMapper(SharedPreferences pref)
	{
		this.pref = pref;
		init(false);
	}

	public KeyMap findKeyMapByAssign(String pref_val)
	{
		return keymapAssign.get(pref_val);
	}

	public KeyMap findKeyMapByPrefKey(String pref_key)
	{
		return keymapAll.get(pref_key);
	}

	public void initKeyMap(String pref_key, KeyAction act)
	{
		KeyMap map = new KeyMap(pref_key, act);
		keymapAll.put(map.getPrefKey(), map);
	}

	public void initKeyMap(String pref_key, char character)
	{
		KeyMap map = new KeyMap(pref_key, character);
		keymapAll.put(map.getPrefKey(), map);
	}

	public KeyMap assignKeyMap(String pref_key, int key_code)
	{
		return assignKeyMap(pref_key, key_code, false, false);
	}

	public KeyMap assignKeyMap(String pref_key, char char_code)
	{
		return assignKeyMap(pref_key, char_code, false, true);
	}

	public KeyMap assignKeyMap(String pref_key, int key_code, boolean alt_mod, boolean char_mod)
	{
		KeyMap map = findKeyMapByPrefKey(pref_key);

		if (map != null)
		{

			// assign new key map
			map.assign(key_code, alt_mod, char_mod);

			// remove old key assignment
			String oldval = pref.getString(pref_key, "");
			if (oldval != null && oldval.length() > 0)
			{
				keymapAssign.remove(oldval);
			}

			// remove duplicate new key assignment
			KeyMap prevMap = keymapAssign.put(map.getPrefValue(), map);
			if (prevMap != null)
			{
				prevMap.clear();
			}

			return prevMap;
		}
		return null;
	}

	public void clearKeyMap(String pref_val)
	{
		KeyMap map = keymapAssign.remove(pref_val);
		if (map != null)
			map.clear();
	}

	public void init(boolean forceReset)
	{
		KeyMap map;

		if (keymapAll.size() == 0)
		{

			initKeyMap(KEY_VIRTKEYKEY, KeyAction.VirtualKeyboard);
			initKeyMap(KEY_ZOOMINKEY, KeyAction.ZoomIn);
			initKeyMap(KEY_ZOOMOUTKEY, KeyAction.ZoomOut);
			initKeyMap(KEY_BKSPACEKEY, KeyAction.BackspaceKey);
			initKeyMap(KEY_CTRLKEY, KeyAction.CtrlKey);
			initKeyMap(KEY_ENTERKEY, KeyAction.EnterKey);
			initKeyMap(KEY_ESCKEY, KeyAction.EscKey);
			initKeyMap(KEY_DOWNKEY, KeyAction.ArrowDownKey);
			initKeyMap(KEY_LEFTKEY, KeyAction.ArrowLeftKey);
			initKeyMap(KEY_RIGHTKEY, KeyAction.ArrowRightKey);
			initKeyMap(KEY_UPKEY, KeyAction.ArrowUpKey);
			initKeyMap(KEY_LALTKEY, KeyAction.AltKey);
			initKeyMap(KEY_RALTKEY, KeyAction.AltKey);
			initKeyMap(KEY_LSHIFTKEY, KeyAction.ShiftKey);
			initKeyMap(KEY_RSHIFTKEY, KeyAction.ShiftKey);
			initKeyMap(KEY_SPACEKEY, KeyAction.Space);
			initKeyMap(KEY_TABKEY, KeyAction.Tab);

			initKeyMap(KEY_AMPKEY, '&');
			initKeyMap(KEY_ASTKEY, '*');
			initKeyMap(KEY_ATKEY, '@');
			initKeyMap(KEY_BSLASHKEY, '\\');
			initKeyMap(KEY_COLONKEY, ':');
			initKeyMap(KEY_COMMAKEY, ',');
			initKeyMap(KEY_DOLLARKEY, '$');
			initKeyMap(KEY_DQUOTEKEY, '"');
			initKeyMap(KEY_EQUALKEY, '=');
			initKeyMap(KEY_EXCLKEY, '!');
			initKeyMap(KEY_FSLASHKEY, '/');
			initKeyMap(KEY_GTKEY, '>');
			initKeyMap(KEY_LBKEY, '[');
			initKeyMap(KEY_LCKEY, '{');
			initKeyMap(KEY_LPKEY, '(');
			initKeyMap(KEY_LTKEY, '<');
			initKeyMap(KEY_MINUSKEY, '-');
			initKeyMap(KEY_PERCENTKEY, '%');
			initKeyMap(KEY_PERIODKEY, KeyAction.Period);
			initKeyMap(KEY_PIPEKEY, '|');
			initKeyMap(KEY_PLUSKEY, '+');
			initKeyMap(KEY_POUNDKEY, '#');
			initKeyMap(KEY_QUESTKEY, '?');
			initKeyMap(KEY_RBKEY, ']');
			initKeyMap(KEY_RCKEY, '}');
			initKeyMap(KEY_RPKEY, ')');
			initKeyMap(KEY_SCOLONKEY, ';');
			initKeyMap(KEY_SQUOTEKEY, '\'');
			initKeyMap(KEY_TILDEKEY, '~');
			initKeyMap(KEY_BACKTICKKEY, '`');
			initKeyMap(KEY_UNDERKEY, '_');

			initKeyMap(KEY_AKEY, 'a');
			initKeyMap(KEY_BKEY, 'b');
			initKeyMap(KEY_CKEY, 'c');
			initKeyMap(KEY_DKEY, 'd');
			initKeyMap(KEY_EKEY, 'e');
			initKeyMap(KEY_FKEY, 'f');
			initKeyMap(KEY_GKEY, 'g');
			initKeyMap(KEY_HKEY, 'h');
			initKeyMap(KEY_IKEY, 'i');
			initKeyMap(KEY_JKEY, 'j');
			initKeyMap(KEY_KKEY, 'k');
			initKeyMap(KEY_LKEY, 'l');
			initKeyMap(KEY_MKEY, 'm');
			initKeyMap(KEY_NKEY, 'n');
			initKeyMap(KEY_OKEY, 'o');
			initKeyMap(KEY_PKEY, 'p');
			initKeyMap(KEY_QKEY, 'q');
			initKeyMap(KEY_RKEY, 'r');
			initKeyMap(KEY_SKEY, 's');
			initKeyMap(KEY_TKEY, 't');
			initKeyMap(KEY_UKEY, 'u');
			initKeyMap(KEY_VKEY, 'v');
			initKeyMap(KEY_WKEY, 'w');
			initKeyMap(KEY_XKEY, 'x');
			initKeyMap(KEY_YKEY, 'y');
			initKeyMap(KEY_ZKEY, 'z');

			initKeyMap(KEY_0KEY, '0');
			initKeyMap(KEY_1KEY, '1');
			initKeyMap(KEY_2KEY, '2');
			initKeyMap(KEY_3KEY, '3');
			initKeyMap(KEY_4KEY, '4');
			initKeyMap(KEY_5KEY, '5');
			initKeyMap(KEY_6KEY, '6');
			initKeyMap(KEY_7KEY, '7');
			initKeyMap(KEY_8KEY, '8');
			initKeyMap(KEY_9KEY, '9');
		}

		keymapAssign.clear();

		int version = pref.getInt(KEY_KEYMAPVERSION, 0);
		if (version == 0 || forceReset)
		{

			SharedPreferences.Editor ed = pref.edit();

			// zero all keymap prefs
			for (Iterator<KeyMap> iter = keymapAll.values().iterator(); iter.hasNext();)
			{
				map = iter.next();
				ed.putString(map.getPrefKey(), "");
			}

			assignKeyMap(KEY_VIRTKEYKEY, KeyEvent.KEYCODE_CAMERA);
			assignKeyMap(KEY_ZOOMINKEY, KeyEvent.KEYCODE_VOLUME_UP);
			assignKeyMap(KEY_ZOOMOUTKEY, KeyEvent.KEYCODE_VOLUME_DOWN);

			assignKeyMap(KEY_BKSPACEKEY, KeyEvent.KEYCODE_DEL);
			assignKeyMap(KEY_CTRLKEY, KeyEvent.KEYCODE_DPAD_CENTER);
			assignKeyMap(KEY_LSHIFTKEY, KeyEvent.KEYCODE_SHIFT_LEFT);
			assignKeyMap(KEY_RSHIFTKEY, KeyEvent.KEYCODE_SHIFT_RIGHT);
			assignKeyMap(KEY_LALTKEY, KeyEvent.KEYCODE_ALT_LEFT);
			assignKeyMap(KEY_RALTKEY, KeyEvent.KEYCODE_ALT_RIGHT);
			assignKeyMap(KEY_ESCKEY, KeyEvent.KEYCODE_BACK);
			assignKeyMap(KEY_ENTERKEY, KeyEvent.KEYCODE_ENTER);
			assignKeyMap(KEY_SPACEKEY, KeyEvent.KEYCODE_SPACE);
			assignKeyMap(KEY_TABKEY, KeyEvent.KEYCODE_TAB);
			assignKeyMap(KEY_DOWNKEY, KeyEvent.KEYCODE_DPAD_DOWN);
			assignKeyMap(KEY_LEFTKEY, KeyEvent.KEYCODE_DPAD_LEFT);
			assignKeyMap(KEY_RIGHTKEY, KeyEvent.KEYCODE_DPAD_RIGHT);
			assignKeyMap(KEY_UPKEY, KeyEvent.KEYCODE_DPAD_UP);

			assignKeyMap(KEY_AMPKEY, '&');
			assignKeyMap(KEY_ASTKEY, '*');
			assignKeyMap(KEY_ATKEY, '@');
			assignKeyMap(KEY_BSLASHKEY, '\\');
			assignKeyMap(KEY_COLONKEY, ':');
			assignKeyMap(KEY_COMMAKEY, ',');
			assignKeyMap(KEY_DOLLARKEY, '$');
			assignKeyMap(KEY_DQUOTEKEY, '"');
			assignKeyMap(KEY_EQUALKEY, '=');
			assignKeyMap(KEY_EXCLKEY, '!');
			assignKeyMap(KEY_FSLASHKEY, '/');
			assignKeyMap(KEY_GTKEY, '>');
			assignKeyMap(KEY_LBKEY, '[');
			assignKeyMap(KEY_LCKEY, '{');
			assignKeyMap(KEY_LPKEY, '(');
			assignKeyMap(KEY_LTKEY, '<');
			assignKeyMap(KEY_MINUSKEY, '-');
			assignKeyMap(KEY_PERCENTKEY, '%');
			assignKeyMap(KEY_PERIODKEY, '.');
			assignKeyMap(KEY_PIPEKEY, '|');
			assignKeyMap(KEY_PLUSKEY, '+');
			assignKeyMap(KEY_POUNDKEY, '#');
			assignKeyMap(KEY_QUESTKEY, '?');
			assignKeyMap(KEY_RBKEY, ']');
			assignKeyMap(KEY_RCKEY, '}');
			assignKeyMap(KEY_RPKEY, ')');
			assignKeyMap(KEY_SCOLONKEY, ';');
			assignKeyMap(KEY_SQUOTEKEY, '\'');
			assignKeyMap(KEY_TILDEKEY, '~');
			assignKeyMap(KEY_BACKTICKKEY, '`');
			assignKeyMap(KEY_UNDERKEY, '_');

			assignKeyMap(KEY_AKEY, 'a');
			assignKeyMap(KEY_BKEY, 'b');
			assignKeyMap(KEY_CKEY, 'c');
			assignKeyMap(KEY_DKEY, 'd');
			assignKeyMap(KEY_EKEY, 'e');
			assignKeyMap(KEY_FKEY, 'f');
			assignKeyMap(KEY_GKEY, 'g');
			assignKeyMap(KEY_HKEY, 'h');
			assignKeyMap(KEY_IKEY, 'i');
			assignKeyMap(KEY_JKEY, 'j');
			assignKeyMap(KEY_KKEY, 'k');
			assignKeyMap(KEY_LKEY, 'l');
			assignKeyMap(KEY_MKEY, 'm');
			assignKeyMap(KEY_NKEY, 'n');
			assignKeyMap(KEY_OKEY, 'o');
			assignKeyMap(KEY_PKEY, 'p');
			assignKeyMap(KEY_QKEY, 'q');
			assignKeyMap(KEY_RKEY, 'r');
			assignKeyMap(KEY_SKEY, 's');
			assignKeyMap(KEY_TKEY, 't');
			assignKeyMap(KEY_UKEY, 'u');
			assignKeyMap(KEY_VKEY, 'v');
			assignKeyMap(KEY_WKEY, 'w');
			assignKeyMap(KEY_XKEY, 'x');
			assignKeyMap(KEY_YKEY, 'y');
			assignKeyMap(KEY_ZKEY, 'z');

			assignKeyMap(KEY_0KEY, '0');
			assignKeyMap(KEY_1KEY, '1');
			assignKeyMap(KEY_2KEY, '2');
			assignKeyMap(KEY_3KEY, '3');
			assignKeyMap(KEY_4KEY, '4');
			assignKeyMap(KEY_5KEY, '5');
			assignKeyMap(KEY_6KEY, '6');
			assignKeyMap(KEY_7KEY, '7');
			assignKeyMap(KEY_8KEY, '8');
			assignKeyMap(KEY_9KEY, '9');

			for (Iterator<KeyMap> iter = keymapAssign.values().iterator(); iter.hasNext();)
			{
				map = iter.next();
				ed.putString(map.getPrefKey(), map.getPrefValue());
			}
			ed.putString("crawl.ctrldoubletap", "EnterKey");
			ed.putInt(KEY_KEYMAPVERSION, 1);
			ed.commit();
		}
		else
		{
			for (Iterator<KeyMap> iter = keymapAll.values().iterator(); iter.hasNext();)
			{
				map = iter.next();
				map.loadFromPref();
				if (map.isAssigned())
					keymapAssign.put(map.getPrefValue(), map);
			}
		}
	}

	public KeyAction getCtrlDoubleTapAction()
	{
		return KeyAction.convert(pref.getString("crawl.ctrldoubletap", "EnterKey"));
	}

	public KeyAction getCenterScreenTapAction()
	{
		return KeyAction.convert(pref.getString("crawl.centerscreentap", "Space"));
	}
}