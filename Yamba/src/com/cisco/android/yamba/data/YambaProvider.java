package com.cisco.android.yamba.data;

import com.cisco.android.yamba.BuildConfig;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public class YambaProvider extends ContentProvider {
	private static final String TAG = "CP";

	private static final int TIMELINE_DIR = 1;
	private static final int TIMELINE_ITEM = 2;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		uriMatcher.addURI(YambaContract.AUTHORITY,
				YambaContract.Timeline.TABLE, TIMELINE_DIR);
		uriMatcher.addURI(YambaContract.AUTHORITY, YambaContract.Timeline.TABLE
				+ "/#", TIMELINE_ITEM);
	}

	private static final ProjectionMap PROJ_MAP_TIMELINE = new ProjectionMap.Builder()
			.addColumn(YambaContract.Timeline.Columns.ID, 
					YambaDBHelper.COL_ID)
			.addColumn(YambaContract.Timeline.Columns.TIMESTAMP,
					YambaDBHelper.COL_TIME)
			.addColumn(YambaContract.Timeline.Columns.USER,
					YambaDBHelper.COL_USER)
			.addColumn(YambaContract.Timeline.Columns.STATUS,
					YambaDBHelper.COL_STATUS)
			.addColumn(YambaContract.Timeline.Columns.MAX_TIMESTAMP,
					"max(" + YambaDBHelper.COL_TIME + ")").build();

	private static final ColumnMap COL_MAP_TIMELINE = new ColumnMap.Builder()
			.addColumn(YambaContract.Timeline.Columns.ID, 
					YambaDBHelper.COL_ID, 
					ColumnMap.Type.STRING)
			.addColumn(YambaContract.Timeline.Columns.TIMESTAMP,
					YambaDBHelper.COL_TIME, 
					ColumnMap.Type.LONG)
			.addColumn(YambaContract.Timeline.Columns.USER,
					YambaDBHelper.COL_USER, 
					ColumnMap.Type.STRING)
			.addColumn(YambaContract.Timeline.Columns.STATUS,
					YambaDBHelper.COL_STATUS,
					ColumnMap.Type.STRING)
			.build();

	private YambaDBHelper dbHelper;

	//RUN on UI thread
	public boolean onCreate() {
		dbHelper = new YambaDBHelper(this.getContext());
		return (dbHelper != null);
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case TIMELINE_ITEM:
			return YambaContract.Timeline.ITEM_TYPE;
		case TIMELINE_DIR:
			return YambaContract.Timeline.DIR_TYPE;
		default:
			return null;
		}
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "bulk insert: " + uri);
		}

		switch (uriMatcher.match(uri)) {
		case TIMELINE_DIR:
			break;

		default:
			throw new UnsupportedOperationException(
					"URI unsupported in bulk insert: " + uri);
		}

		SQLiteDatabase db = getDB();
		int count = 0;
		try {
			db.beginTransaction();
			for (ContentValues row : values) {
				row = COL_MAP_TIMELINE.translateCols(row);
				if (0 < db.insert(YambaDBHelper.TABLE, null, row)) {
					count++;
				}
			}
			db.setTransactionSuccessful(); // need to call this, otherwise it's getting rolled back
		} finally {
			db.endTransaction();
		}

		if (0 < count) {
			getContext().getContentResolver().notifyChange(
					YambaContract.Timeline.URI, null);
		}

		return count;
	}

	//in SQL: SELECT <proj> FROM <uri path> WHERE <sel> <selArgs> ORDER BY <sort>
	// SElECT realcol1 AS virtcol1 , realcol2 AS virtcol2
	// FROM <uri path>
	// WHERE <sel> <selArgs>
	// ORDER BY virtcol2 ASD
	public Cursor query(Uri uri, String[] proj, String sel, String[] selArgs,
			String sort) {
		long pk = -1;

		if (BuildConfig.DEBUG) {
			Log.d(TAG, "query: " + uri);
		}
		switch (uriMatcher.match(uri)) {
		case TIMELINE_ITEM:
			pk = ContentUris.parseId(uri);

		case TIMELINE_DIR:
			break;

		default:
			throw new IllegalArgumentException("URI unsupported in query: "
					+ uri);
		}

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			qb.setStrict(true);
		}

		qb.setProjectionMap(PROJ_MAP_TIMELINE.getProjectionMap());

		qb.setTables(YambaDBHelper.TABLE);

		if (0 <= pk) {
			qb.appendWhere(YambaDBHelper.COL_ID + "=" + pk); // can only use once. adding a (xxx = xxx) to WHERE
		}

		Cursor c = qb.query(getDB(), proj, sel, selArgs, null, null, sort);

		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	private SQLiteDatabase getDB() {
		// always get a WritableDataBase. Get a Readable database is returning
		// the same thing, but in extreme condition when there is not enough
		// space in file system, you can get readable but not writable , then
		// the next time when file system has space and someone is requesting a
		// writable, system will detect that's readonly and would close it, and
		// re-cache it as a writable thus your readable pointer becomes invalid
		return dbHelper.getWritableDatabase();
	}

}
