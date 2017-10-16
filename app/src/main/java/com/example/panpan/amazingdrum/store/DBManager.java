package com.example.panpan.amazingdrum.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库管理类
 * */
public class DBManager
{
	private final ChordDBHelper helper;
	private final SQLiteDatabase db;
	private static DBManager dbManager;

	public static DBManager getInstance(Context context)
	{

		if (dbManager == null)
		{
			dbManager = new DBManager(context);
		}
		return dbManager;
	}

	private DBManager(Context context)
	{

		helper = new ChordDBHelper(context);
		// 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
		// mFactory);
		// 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
		db = helper.getWritableDatabase();
	}

	/**
	 * add chords
	 * 
	 * @param chords
	 */
	public void add(List<Chord> chords)
	{

		db.beginTransaction(); // 开始事务
		try
		{
			String insert_data;
			for (Chord chord : chords)
			{
				if (queryInfo(chord.type, chord.name) == null)
				{
					insert_data = "insert into " + ChordDBHelper.TABLE_NAME
							+ "(type,name,info) values " + "('" + chord.type
							+ "','" + chord.name + "','" + chord.info + "');";
					db.execSQL(insert_data);
					if (!isTypeExist(chord.type))
						addType(chord.type);
				}
			}
			db.setTransactionSuccessful(); // 设置事务成功完成
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * add chords
	 * 
	 * @return true 添加成功 false 添加失败
	 * @param gtpTxtPath
	 * @throws Exception
	 */
	public void add(String gtpTxtPath) throws Exception
	{
		AttachChordLoader chordLoader = new AttachChordLoader();
		chordLoader.loadFromFile(gtpTxtPath);
		if (chordLoader.chordInfo.size() > 0)
		{
			for (Entry<String, String> data : chordLoader.chordInfo.entrySet())
			{
				Chord chord = new Chord();
				chord.name = data.getKey();
				chord.info = data.getValue();
				chord.type = "自定义";
				if (!this.add(chord))
				{

				}
			}
		}
	}

	/**
	 * add chords
	 * 
	 * @return true 添加成功 false 添加失败
	 * @param chords
	 */
	public boolean add(Chord chord)
	{

		boolean result = true;
		db.beginTransaction(); // 开始事务
		try
		{
			String insert_data;
			if (queryInfo(chord.type, chord.name) == null)
			{
				insert_data = "insert into " + ChordDBHelper.TABLE_NAME
						+ "(type,name,info) values " + "('" + chord.type
						+ "','" + chord.name + "','" + chord.info + "');";
				db.execSQL(insert_data);
				if (!isTypeExist(chord.type))
					addType(chord.type);
			} else
				result = false;

			db.setTransactionSuccessful(); // 设置事务成功完成
		} catch (Exception e)
		{
			result = false;
			e.printStackTrace();
		} finally
		{
			db.endTransaction(); // 结束事务
		}
		return result;
	}

	/**
	 * update chord's data
	 * 
	 * @param name
	 *            newData
	 */
	public void updateChordInfo(String type, String newType)
	{

		ContentValues cv = new ContentValues();
		cv.put("type", newType);
		db.update(ChordDBHelper.TABLE_NAME, cv, "type=?", new String[] { type });
		db.update(ChordDBHelper.TABLE_NAME2, cv, "type=?",
				new String[] { type });
	}

	/**
	 * update chord's data
	 * 
	 * @param name
	 *            newData
	 * @return 返回修改的条数
	 */
	public int updateChordName(String name, String newName)
	{
		ContentValues cv = new ContentValues();
		cv.put("name", newName);
		return db.update(ChordDBHelper.TABLE_NAME, cv, "name=?",
				new String[] { name });
	}

	/**
	 * update chord's data
	 * 
	 * @param name
	 *            newData
	 * @return 返回修改的条数
	 */
	public int updateChordInfo(String type, String name, String newInfo)
	{

		if (newInfo != null)
		{
			ContentValues cv = new ContentValues();
			cv.put("info", newInfo);
			return db.update(ChordDBHelper.TABLE_NAME, cv, "type=? AND name=?",
					new String[] { type, name });
		} else if (name != null)
		{
			ContentValues cv = new ContentValues();
			cv.put("name", name);
			return db.update(ChordDBHelper.TABLE_NAME, cv, "type=?",
					new String[] { type });
		}
		return 0;
	}

	/**
	 * delete chord
	 * 
	 * @param name
	 */
	public int deleteChord(String type, String name)
	{

		if (name != null && type == null)
		{
			return db.delete(ChordDBHelper.TABLE_NAME, "name=?",
					new String[] { name });
		}
		if (name == null && type != null)
		{

			db.delete(ChordDBHelper.TABLE_NAME2, "type=?",
					new String[] { type });
			return db.delete(ChordDBHelper.TABLE_NAME, "type=?",
					new String[] { type });
		}
		if (name != null && type != null)
		{
			return db.delete(ChordDBHelper.TABLE_NAME, "type=? AND name=?",
					new String[] { type, name });
		}
		return 0;
	}

	/**
	 * type name 参数 查询info， type 参数 查询 name， 无参数 查询type， name 参数 查询info
	 * （因为name是唯一的）
	 * */
	public String[] queryInfo(String type, String name)
	{

		String info[] = null;
		if (name != null && type != null)
		{
			Cursor c = db.rawQuery("SELECT * FROM " + ChordDBHelper.TABLE_NAME
					+ " WHERE type=? AND name=?", new String[] { type, name });
			if (c.getCount() == 0)
			{
				c.close();
				return null;
			}
			info = new String[c.getCount()];
			int index = 0;
			while (c.moveToNext())
			{
				info[index++] = c.getString(c.getColumnIndex("info"));
			}
			c.close();
		} else if (name == null && type != null)
		{
			Cursor c = db.rawQuery("SELECT * FROM " + ChordDBHelper.TABLE_NAME
					+ " WHERE type=?", new String[] { type });
			if (c.getCount() == 0)
			{
				c.close();
				return null;
			}
			info = new String[c.getCount()];
			int index = 0;
			while (c.moveToNext())
			{
				info[index++] = c.getString(c.getColumnIndex("name"));
			}
			c.close();
		} else if (name == null && type == null)
		{
			Cursor c = db.rawQuery(
					"SELECT * FROM " + ChordDBHelper.TABLE_NAME2, null);
			if (c.getCount() == 0)
			{
				c.close();
				return null;
			}
			info = new String[c.getCount()];
			int index = 0;
			while (c.moveToNext())
			{
				info[index++] = c.getString(c.getColumnIndex("type"));
			}
			c.close();
		} else
		{
			Cursor c = db.rawQuery("SELECT * FROM " + ChordDBHelper.TABLE_NAME
					+ " WHERE name=?", new String[] { name });
			if (c.getCount() == 0)
			{
				c.close();
				return null;
			}
			info = new String[c.getCount()];
			int index = 0;
			while (c.moveToNext())
			{
				info[index++] = c.getString(c.getColumnIndex("info"));
			}
			c.close();
		}
		return info;
	}

	/**
	 * @return result[0] type, result[1] info, null无记录
	 * */
	public String[] queryInfo(String name)
	{

		String result[] = null;
		Cursor c = db.rawQuery("SELECT * FROM " + ChordDBHelper.TABLE_NAME
				+ " WHERE name=?", new String[] { name });
		if (c.getCount() == 0)
		{
			c.close();
			return null;
		}
		result = new String[2];
		while (c.moveToNext())
		{
			result[0] = c.getString(c.getColumnIndex("type"));
			result[1] = c.getString(c.getColumnIndex("info"));
		}
		c.close();
		return result;
	}

	/**
	 * query all chords, return list
	 * 
	 * @return List<Chord>
	 */
	public List<Chord> query()
	{

		ArrayList<Chord> chords = new ArrayList<Chord>();
		Cursor c = queryTheCursor();
		while (c.moveToNext())
		{
			Chord chord = new Chord();
			chord.type = c.getString(c.getColumnIndex("type"));
			chord.name = c.getString(c.getColumnIndex("name"));
			chord.info = c.getString(c.getColumnIndex("info"));
			chords.add(chord);
		}
		c.close();
		return chords;
	}

	/**
	 * query all chords, return cursor
	 * 
	 * @return Cursor
	 */
	public Cursor queryTheCursor()
	{

		Cursor c = db.rawQuery("SELECT * FROM " + ChordDBHelper.TABLE_NAME,
				null);
		return c;
	}

	/**
	 * query specialType chords, return cursor
	 * 
	 * @return Cursor
	 */
	public Cursor queryTypeCursor(String type)
	{

		if (type == null)
			return queryTheCursor();
		Cursor c = db.rawQuery("SELECT * FROM " + ChordDBHelper.TABLE_NAME
				+ " WHERE type=?", new String[] { type });
		return c;
	}

	private boolean isTypeExist(String type)
	{

		Cursor c = db.rawQuery("SELECT * FROM " + ChordDBHelper.TABLE_NAME2
				+ " WHERE type=?", new String[] { type });
		if (c.getCount() == 0)
		{
			c.close();
			return false;
		}
		c.close();
		return true;
	}

	private void addType(String type)
	{

		String insert_data = "insert into " + ChordDBHelper.TABLE_NAME2
				+ "(type) values ('" + type + "');";
		db.execSQL(insert_data);
	}

	/**
	 * close database
	 */
	public void closeDB()
	{
		if (dbManager != null)
		{
			db.close();
			helper.close();
			dbManager = null;
		}
	}
}
