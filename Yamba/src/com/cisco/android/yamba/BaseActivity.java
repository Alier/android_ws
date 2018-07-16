package com.cisco.android.yamba;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends Activity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.main, menu);
		//newPage(TimelineActivity.class);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.item_about) {
			Toast.makeText(this, R.string.about_description, Toast.LENGTH_LONG).show();
		} else if (id == R.id.item_status) {
			newPage(StatusActivity.class);
		} else if (id == R.id.item_timeline) {
			newPage(TimelineActivity.class);
		} else {
			return super.onContextItemSelected(item);
		}

		return true;
	}

	private void newPage(Class<?> page) {
		Intent i = new Intent(this,page);
		i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		this.startActivity(i);
	}

	public BaseActivity() {
		// TODO Auto-generated constructor stub
	}

}
