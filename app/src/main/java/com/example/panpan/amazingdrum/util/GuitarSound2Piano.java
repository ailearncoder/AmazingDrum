package com.example.panpan.amazingdrum.util;

/** 根据吉他品弦信息获取钢琴音频 */
public class GuitarSound2Piano {
	private static final String stringInfo[] = new String[] { "e5", "b4", "g4",
			"d4", "a3", "e3" };
	private static final String note[] = new String[] { "C", "C#", "D", "D#",
			"E", "F", "F#", "G", "G#", "A", "A#", "B" };

	// E和B没有M
	/** 弦品 */
	public static String getPianoName(int string, int fret) {
		if (string < 1)
			return "0";
		String result = stringInfo[string - 1];
		for (int i = 0; i < fret; i++)
			result = getNext(result);
		return result;
	}

	// E和B没有M
	/** 弦品 */
	public static String getElectricGuitarName(int fileName) {
		int string = fileName / 100, fret = fileName % 100;
		int offset = 0;
		if (fret == 23)
			return "M" + string + ".ogg";
		switch (string) {
		case 1:// 100E 4
			offset = 4;
			break;
		case 2:// 200B 11
			offset = 11;
			break;
		case 3:// 300G 7
			offset = 7;
			break;
		case 4:// 400D 2
			offset = 2;
			break;
		case 5:// 500A 9
			offset = 9;
			break;
		case 6:// 600E 4
			offset = 4;
			break;
		default:
			break;
		}
		offset += fret;
		offset %= note.length;
		return "" + fileName + note[offset] + ".ogg";
	}

	/** 弦品 */
	public static int getDrumName(int string, int fret) {
		if (fret == 36 || fret == 35)
			return 1;
		if (fret == 40 || fret == 38)
			return 2;
		if (fret == 42 || fret == 44)
			return 3;
		if (fret == 57 || fret == 2)
			return 4;
		if (fret == 43 || fret == 50)
			return 5;
		if (fret == 47)
			return 6;
		if (fret == 41 || fret == 45)
			return 7;
		if (fret == 46 || fret == 49 || fret == 52 || fret == 55 || fret == 57)
			return 8;
		if (fret == 51 || fret == 53 || fret == 59)
			return 9;
		if (fret == 0)
			return 0;
		return 3;
	}

	private static String getNext(String now) {
		if (now.length() == 3)// c0m d0m f0m g0m a0m
		{
			char num = now.charAt(0);
			char num2 = now.charAt(1);
			switch (num) {
			case 'c':
				return "d" + num2;
			case 'd':
				return "e" + num2;
			case 'f':
				return "g" + num2;
			case 'g':
				return "a" + num2;
			case 'a':
				return "b" + num2;
			default:
				return null;
			}
		}
		if (now.length() == 2)// c0 d0 e0 f0 g0 a0 b0
		{
			char num = now.charAt(0);
			char num2 = now.charAt(1);
			switch (num) {
			case 'c':
				return "c" + num2 + "m";
			case 'd':
				return "d" + num2 + "m";
			case 'e':
				return "f" + num2;
			case 'f':
				return "f" + num2 + "m";
			case 'g':
				return "g" + num2 + "m";
			case 'a':
				return "a" + num2 + "m";
			case 'b':
				num2++;
				return "c" + num2;
			default:
				return null;
			}
		}
		return null;
	}
}
