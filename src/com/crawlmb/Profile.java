package com.crawlmb;

import android.util.Log;

public class Profile {

	protected int id = 0;
	protected String name = "";
    protected String saveFile = "";
	protected int flags = 0;
    protected int plugin = 0;
	protected static String dl = "~";

	public Profile(int id, String name, String saveFile, int flags, int plugin) {
		this.id = id;
		this.name = name;
		this.saveFile = saveFile;
		this.flags = flags;
		this.plugin = plugin;
	}

	public Profile() {}

	public String toString() {
		return name;
	}

	public int getId() {
		return id;
	}
	public void setId(int value) {
		id = value;
	}

	public String getName() {
		return name;
	}
	public void setName(String value) {
		name = value;
	}

	public String getSaveFile() {
		return saveFile;
	}
	public void setSaveFile(String value) {
		saveFile = value;
	}

	public boolean getAutoStartBorg() {
		return (flags & 0x0000001)!=0;
	}
	public void setAutoStartBorg(boolean value) {
		if (value)
			flags |= 0x00000001;
		else
			flags &= ~0x00000001;
	}

	public boolean getSkipWelcome() {
		return (flags & 0x0000002)!=0;
	}
	public void setSkipWelcome(boolean value) {
		if (value)
			flags |= 0x00000002;
		else
			flags &= ~0x00000002;
	}

	public int getPlugin() {
		return plugin;
	}
	public void setPlugin(int value) {
		plugin = value;
	}

	public String serialize() {
		return id+dl+name+dl+saveFile+dl+flags+dl+plugin;
	}
	public static Profile deserialize(String value) {
		String[] tk = value.split(dl);
		Profile p = new Profile();
		if (tk.length>0) try {p.id = Integer.parseInt(tk[0]);} catch (Exception ex) {}
		if (tk.length>1) try {p.name = tk[1];} catch (Exception ex) {}
		if (tk.length>2) try {p.saveFile = tk[2];} catch (Exception ex) {}
		if (tk.length>3) try {p.flags = Integer.parseInt(tk[3]);} catch (Exception ex) {}
		if (tk.length>4) try {p.plugin = Integer.parseInt(tk[4]);} catch (Exception ex) {}
		return p;
	}
}
