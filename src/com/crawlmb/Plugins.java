package com.crawlmb;

import java.util.zip.ZipInputStream;
import android.os.Environment;

final public class Plugins
{
	public enum Plugin
	{
		Angband(0), Angband306(1), FrogKnows(2);
		private int id;

		private Plugin(int id)
		{
			this.id = id;
		}

		public int getId()
		{
			return id;
		}

		public static Plugin convert(int value)
		{
			return Plugin.class.getEnumConstants()[value];
		}
	}

	static final String DEFAULT_PROFILE = "0~Default~PLAYER~0~0|0~Borg~BORGSAVE~1~1";
	public static String LoaderLib = "loader-angband";
	static final int KEY_A1 = 0534; /* upper left of keypad */
	static final int KEY_A3 = 0535; /* upper right of keypad */
	static final int KEY_B2 = 0536; /* center of keypad */// CURRENTLY NOT USED
	static final int KEY_C1 = 0537; /* lower left of keypad */
	static final int KEY_C3 = 0540; /* lower right of keypad */
	static final int KEY_DOWN = 0402; /* down-arrow key */
	static final int KEY_UP = 0403; /* up-arrow key */
	static final int KEY_LEFT = 0404; /* left-arrow key */
	static final int KEY_RIGHT = 0405; /* right-arrow key */

	public static String getFilesDir(Plugin p)
	{
		switch (p)
		{
		case Angband:
			return "angband";
		case Angband306:
			return "306";
		default:
			return p.toString().toLowerCase();
		}
	}

	public static int getKeyDown()
	{
		return KEY_DOWN;
	}

	public static int getKeyUp()
	{
		return KEY_UP;
	}

	public static int getKeyLeft()
	{
		return KEY_LEFT;
	}

	public static int getKeyRight()
	{
		return KEY_RIGHT;
	}

	public static int getKeyEnter()
	{
		return 0x9C;
	}

	public static int getKeyTab()
	{
		return '\t';
	}

	public static int getKeyDelete()
	{
		return 0x9E;
	}

	public static int getKeyBackspace()
	{
		return 0x9F;
	}

	public static int getKeyEsc()
	{
		return 0x1B;
	}

	public static ZipInputStream getPluginZip(int plugin)
	{
		return null;
	}

	public static String getPluginCrc(int plugin)
	{
		return null;
	}

	public static String getUpgradePath(Plugin p)
	{
		switch (p)
		{
		case Angband:
			return Environment.getExternalStorageDirectory()
					+ "/Android/data/org.angdroid.angband/files/libangband320/save";
		default:
			return "";
		}
	}
}
