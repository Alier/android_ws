package com.cisco.android.yamba;

import java.util.List;

import com.cisco.android.yamba.svc.YambaService;
import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClient.Status;
import com.marakana.android.yamba.clientlib.YambaClientException;

import android.app.Application;
import android.util.Log;

public class YambaApplication extends Application {
	private static final String TAG = "YambaApplication";
	private SafeYambaClient client;
	private final static int MAX_POSTS = 10;

	public YambaApplication() {
		Log.d(TAG,"application constructor");
	}

	//wrapping an class as we're not sure whether it's thread-safe
	public class SafeYambaClient {
		private final YambaClient yClient;

		public SafeYambaClient(String usr, String pwd, String url) {
			yClient = new YambaClient(usr, pwd, url);
		}

		public synchronized void post(String status)
				throws YambaClientException {
			yClient.postStatus(status);
		}

		public synchronized List<Status> poll() throws YambaClientException {
			return yClient.getTimeline(MAX_POSTS);
		}
	}

	//put here as the life span of the application is the longest
	@Override
	public void onCreate() {
		super.onCreate();
		YambaService.startPoll(this);
	}


	public synchronized SafeYambaClient getYambaClient() {
		if (null == client) {
			// late initialization. You could put this in constructor, then don't need to make this method synchronized
			client = new SafeYambaClient("student", "password",
					"http://yamba.marakana.com/api");
		}
		return client;
	}
}
