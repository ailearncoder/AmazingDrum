package com.example.panpan.amazingdrum.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 和弦储存数据库
 * */
public class ChordDBHelper extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME = "chord.db";
	public static final String TABLE_NAME = "chord_data";
	public static final String TABLE_NAME2 = "chord_data2";
	public static final int DATABASE_VERSION = 1;

	public ChordDBHelper(Context context)
	{

		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public ChordDBHelper(Context context, String name, CursorFactory factory,
			int version)
	{

		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	// 数据库第一次被创建时onCreate会被调用
	@Override
	public void onCreate(SQLiteDatabase db)
	{

		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT,type VARCHAR,name VARCHAR,info VARCHAR)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME2
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT,type VARCHAR)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{

		// TODO Auto-generated method stub
		// 修改表person， 添加一个字符类型为String名为other的列（即一个字段）
		db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN other STRING");
	}

}
