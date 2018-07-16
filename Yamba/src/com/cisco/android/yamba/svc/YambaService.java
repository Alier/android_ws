package com.cisco.android.yamba.svc;

import java.util.ArrayList;
import java.util.List;

import com.cisco.android.yamba.BuildConfig;
import com.cisco.android.yamba.YambaApplication;
import com.cisco.android.yamba.data.YambaContract;
import com.marakana.android.yamba.clientlib.YambaClient.Status;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

public class YambaService extends IntentService {

	private static final String TAG = "YambaService";
	private static final String KEY_OP = "operation";
	private static final String KEY_STATUS = "key_status";

	private static final int OP_POST = 1;
	private static final int OP_POLL = 2;
	private static final int OP_START_POLL = 3;

	private static final String SUCC_MSG = "Successful!";
	private static final String FAIL_MSG = "Failed!";

	private static final int POLL_INTERVAL = 60 * 1000;
	private static final int INTENT_CODE = 6; // random as of now we don't need
												// to distinguish, only one
												// pendingIntent

	public YambaService() {
		super(TAG);
	}

	public static void post(Context cxt, String status) {
		// file new intent and start the new AsyncTask

		// explicit intent needs to take into parameter, one Context, one is the
		// class, which basically tells the Intent which class is handling it.
		Intent i = new Intent(cxt, YambaService.class);
		i.putExtra(KEY_OP, OP_POST);
		i.putExtra(KEY_STATUS, status);

		cxt.startService(i);
	}

	public static void startPoll(Context cxt) {
		Intent i = new Intent(cxt, YambaService.class); // this takes Service
														// object as Context in.
		i.putExtra(KEY_OP, OP_START_POLL);
		cxt.startService(i);
	}

	// RUN on DAEMON thread !!!
	public void doStartPoll() {
		// make a new Intent to be fired by Alarm manager
		Intent i = new Intent(this, YambaService.class); // this takes
															// Application
															// object as Context
															// in.
		i.putExtra(KEY_OP, OP_POLL);

		// wrapping the intent into pending intent
		((AlarmManager) getSystemService(Context.ALARM_SERVICE)).setRepeating(
				AlarmManager.RTC, System.currentTimeMillis() + 100,
				POLL_INTERVAL, PendingIntent.getService(this, INTENT_CODE, i,
						PendingIntent.FLAG_UPDATE_CURRENT));
	}

	// RUN on DAEMON thread !!!
	protected void onHandleIntent(Intent intent) {
		int op = intent.getIntExtra(KEY_OP, 0);
		switch (op) {
		case OP_POST:
			doPost(intent.getStringExtra(KEY_STATUS));
			break;
		case OP_POLL:
			doPoll();
			break;
		case OP_START_POLL:
			doStartPoll();
			break;
		default:
			Log.w(TAG, "Unexpected Op!");
			break;
		}

	}

	// RUN on DAEMON thread !!!
	private void doPoll() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Polling: ");
		}

		try {
			process_status(((YambaApplication) getApplication())
					.getYambaClient().poll());
		} catch (Exception e) {
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "doPoll failed: " + e);
			}
		}

		if (BuildConfig.DEBUG) {
			Log.d(TAG, SUCC_MSG);
		}

	}

	// RUN on DAEMON thread !!!
	private void doPost(String status) {
		// do actually post to the network
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Posting: " + status);
		}

		try {
			((YambaApplication) getApplication()).getYambaClient().post(status);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "doPost" + FAIL_MSG);
			}
		}

		if (BuildConfig.DEBUG) {
			Log.d(TAG, SUCC_MSG);
		}
	}

	private void process_status(List<Status> timeline) {
		// we only want the status we don't have in db yet.
		long t = getLastStatusCreatedAt();

		List<ContentValues> status_new = new ArrayList<ContentValues>();
		
		// just Log it right now
		for (Status status : timeline) {
			long createdAt = status.getCreatedAt().getTime();
			if (t < createdAt) {
				ContentValues val = new ContentValues();
				val.put(YambaContract.Timeline.Columns.ID, status.getId());
				val.put(YambaContract.Timeline.Columns.TIMESTAMP,createdAt);
				val.put(YambaContract.Timeline.Columns.USER, status.getUser());
				val.put(YambaContract.Timeline.Columns.STATUS, status.getMessage());
				status_new.add(val);
				//Log.d(TAG, status.getUser() + ": " + status.getMessage());
			}
		}
		
		int recs = status_new.size();
		if( recs > 0) {
			ContentValues[] data = new ContentValues[recs]; 
			this.getContentResolver().bulkInsert(
					YambaContract.Timeline.URI, 
					status_new.toArray(data));
		}
	}

	private long getLastStatusCreatedAt() {
		//SELECT max(created_at) FROM timeline
		// this would actually call getType in the Content Provider. 
		Cursor c = this.getContentResolver().query(YambaContract.Timeline.URI,
				new String[] {YambaContract.Timeline.Columns.MAX_TIMESTAMP}, 
				null, null, null);
		try { return (c.moveToNext()) ? c.getLong(0) : Long.MIN_VALUE; } 
		finally { c.close(); }
	}
}
