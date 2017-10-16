package com.example.panpan.amazingdrum.store;

public class Chord
{
	/** 自定义类型 */
	public static final String TYPE_CUSTOM = "0";
	/** 本地类型 */
	public static final String TYPE_LOCAL = "1";
	public String type = TYPE_LOCAL;
	public String name;
	public String info;

	public Chord()
	{

	}

	public Chord(String name, String info)
	{

		this.name = name;
		this.info = info;
	}
}