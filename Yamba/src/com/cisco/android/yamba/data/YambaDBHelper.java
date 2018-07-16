package com.cisco.android.yamba.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class YambaDBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "yamba.db";
	private static final int VERSION = 1;
	
	//my column names
	public static final String TABLE = "timeline";
	public static final String COL_ID = "id";
	public static final String COL_USER = "user";
	public static final String COL_STATUS = "status";
	public static final String COL_TIME = "created_at";
	
	//if db not exist, call OnCreate; if db exist but older version, call onUpgrade
	public YambaDBHelper(Context ctx) {
		super(ctx, DB_NAME, null, VERSION); //suggestion, always use standard cursor
	}

	@Override
	public void onCreate(SQLiteDatabase db) { //Android will create the DB if it's not exist, we just need to initialize it
		//SQLite is not full-pledged DB, suggest stick with four basic types: INTEGER FLOAT TEXT BLOB
		db.execSQL("CREATE TABLE " + TABLE + "("
            + COL_ID + " INTEGER PRIMARY KEY, "
            + COL_TIME + " INTEGER, "
            + COL_USER + " TEXT, "
            + COL_STATUS + " TEXT)");
	}

	//called if the passed in VERSION is higher than it of the exist DB
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE "+ TABLE);
		//we just drop the old table as it's just cache, the server still have all the data, we just need to pull it again
		onCreate(db);
	}
}
