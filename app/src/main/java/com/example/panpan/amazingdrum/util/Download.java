package com.example.panpan.amazingdrum.util;

import android.os.Environment;

public class Download {
	public final static String DrumType[] = new String[] { "43a", "43b",
			"blues", "funk", "jazz", "pop1", "pop2", "pop3", "pop4", "pop5",
			"rock1", "rock2", "rock3", "rock4" };
	public final static String DrumSoundType[] = new String[] { "CLASSIC",
			"FUNK", "HIP HOP", "JAZZ", "METAL", "POP", "REGGAE" };

	public static String getResourcePath() {
		return Environment.getExternalStorageDirectory().getPath()+"/AeroBand";
		//return "/sdcard/AeroBand";
	}

	public static String getBassPath() {
		return getResourcePath() + "/BandSound/贝斯1";
	}

	public static String getLocalSongPath() {
		return getResourcePath() + "/Song";
	}

	public static String getEasySongPath() {
		return getResourcePath() + "/EasySong";
	}

	public static String getLocalEasySongPath() {
		return getResourcePath() + "/EasySong";
	}

	public static String getCachePath() {
		return getResourcePath() + "/cache";
	}

	public static String getApkPath() {
		return getResourcePath() + "/update.apk";
	}

	public static String getHexPath() {
		return getResourcePath() + "/update.hex";
	}

	public static String getNoticeImagePath() {
		return getResourcePath() + "/notice_image.png";
	}

	public static String getDefaultDrumPath() {
		return getResourcePath() + "/DefaultDrum";
	}
}
