package com.crawlmb;

import java.util.zip.ZipInputStream;
import java.util.Scanner;
import java.io.InputStream;
import android.os.Environment;

final public class Plugins {
	public enum Plugin {
		Angband(0), Angband306(1), FrogKnows(2);
		private int id;

		private Plugin(int id) {
			this.id = id;
		}
		public int getId() {
			return id;
		}
		public static Plugin convert(int value) {
			return Plugin.class.getEnumConstants()[value];
		}
	}

	static final String DEFAULT_PROFILE = "0~Default~PLAYER~0~0|0~Borg~BORGSAVE~1~1";
	public static String LoaderLib ="loader-angband";

	public static String getFilesDir(Plugin p) {
		switch (p) {
		case Angband: return "angband";
		case Angband306: return "306";
		default: return p.toString().toLowerCase();
		}
	}

	public static int getKeyDown(Plugin p) {
		switch (p) {
		case Angband: return 0x80;
		default: return '2';
		}
	}

	public static int getKeyUp(Plugin p) {
		switch (p) {
		case Angband: return 0x83;
		default: return '8';
		}
	}

	public static int getKeyLeft(Plugin p) {
		switch (p) {
		case Angband: return 0x81;
		default: return '4';
		}
	}

	public static int getKeyRight(Plugin p) {
		switch (p) {
		case Angband: return 0x82;
		default: return '6';
		}
	}

	public static int getKeyEnter(Plugin p) {
		switch (p) {
		case Angband: return 0x9C;
		default: return '\r';
		}
	}

	public static int getKeyTab(Plugin p) {
		switch (p) {
		case Angband: return 0x9D;
		default: return '\t';
		}
	}

	public static int getKeyDelete(Plugin p) {
		switch (p) {
		case Angband: return 0x9E;
		default: return 0x7F;
		}
	}

	public static int getKeyBackspace(Plugin p) {
		switch (p) {
		case Angband: return 0x9F;
		default: return '\b';
		}
	}

	public static int getKeyEsc(Plugin p) {
		switch (p) {
		case Angband: return 0xE000;
		default: return 0x1B;
		}
	}

	public static ZipInputStream getPluginZip(int plugin) {
//		InputStream is = null;
//		if (plugin == Plugin.Angband.getId())
//			is = Preferences.getResources().openRawResource(R.raw.zipangband);
//		else if (plugin == Plugin.Angband306.getId())
//			is = Preferences.getResources().openRawResource(R.raw.zipangband306);
//		else if (plugin == Plugin.FrogKnows.getId())
//			is = Preferences.getResources().openRawResource(R.raw.zipfrogknows);
//		return new ZipInputStream(is);
		return null;
	}
	public static String getPluginCrc(int plugin) {
//		InputStream is = null;
//		if (plugin == Plugin.Angband.getId())
//			is = Preferences.getResources().openRawResource(R.raw.crcangband);
//		else if (plugin == Plugin.Angband306.getId())
//			is = Preferences.getResources().openRawResource(R.raw.crcangband306);
//		else if (plugin == Plugin.FrogKnows.getId())
//			is = Preferences.getResources().openRawResource(R.raw.crcfrogknows);
//		return new Scanner(is).useDelimiter("\\A").next().trim();
		return null;
	}

	public static String getUpgradePath(Plugin p) {
		switch (p) {
		case Angband: 
			return Environment.getExternalStorageDirectory()
				+ "/Android/data/org.angdroid.angband/files/libangband320/save";
		default: return "";
		}		
	}

	public static String getStartBorgSequence() {
		Plugin p = Preferences.getActivePlugin();
		switch(p) {
		// tbd: case Angband: return "```=g22222y``\r\r`\r\r`\r2\r\r*\r\r```\032  y`\032z";
		case Angband306: return "```=722222y``\r\r\r\rX\r\r\032  y`\032z";
		default: return "";
		}
	}
}
