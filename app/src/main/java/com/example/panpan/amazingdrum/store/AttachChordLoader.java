package com.example.panpan.amazingdrum.store;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/** 从txt文件中加载和弦信息 */
public class AttachChordLoader
{
	public HashMap<String, String> chordInfo = new HashMap<String, String>();

	public void loadFromFile(String path) throws Exception
	{
		InputStream inputStream = new FileInputStream(path);
		InputStreamReader inputReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputReader);
		String string, name, info;
		int index;
		while ((string = bufferedReader.readLine()) != null)
		{
			string = string.trim();
			if (string.length() > 2)
			{
				// System.out.println(string);
				index = string.indexOf(':');
				name = string.substring(0, index);
				info = string.substring(index + 1, string.length());
				// System.out.println("name=" + name + "info=" + info);
				chordInfo.put(name, info);
			}
		}
		bufferedReader.close();
		inputReader.close();
		inputStream.close();
	}
}
