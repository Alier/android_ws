/*
 * This file is going to be given away, so don't put any code here nor leak any symbols you defined somewhere else, like the column name, etc
 */
package com.cisco.android.yamba.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class YambaContract {
	private YambaContract() {
	}

	public static final String AUTHORITY = "com.cisco.android.yamba.timeline";

	public static final Uri BASE_URI = new Uri.Builder()
			.scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY)
			.build();

	/* The Timeline database */
	public static final class Timeline {
		private Timeline() {
		}

		// MIME sub-type for our row, has to begin with vnd -> stands for vendor
		public static final String MIME_SUBTYPE = "/vnd.com.cisco.yamba.timeline";
		// our table's ITEM type
		public static final String ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ MIME_SUBTYPE;
		// our table's DIR type
		public static final String DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ MIME_SUBTYPE;

		public static final String TABLE = "timeline";
		public static final Uri URI = BASE_URI.buildUpon().appendPath(TABLE)
				.build();

		public static final class Columns {
			private Columns() {
			}

			public static final String ID = BaseColumns._ID;
			public static final String TIMESTAMP = "timestamp";
			public static final String USER = "user";
			public static final String STATUS = "status";
			public static final String MAX_TIMESTAMP = "max_timestamp";
		}
	}
}