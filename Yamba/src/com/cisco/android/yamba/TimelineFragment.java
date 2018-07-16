package com.cisco.android.yamba;

import com.cisco.android.yamba.data.YambaContract;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TimelineFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	
	private static final int LOADER_ID = 7;
	
	private static final String[] COLUMNS = {
		YambaContract.Timeline.Columns.ID,
		YambaContract.Timeline.Columns.USER,
		YambaContract.Timeline.Columns.TIMESTAMP,
		YambaContract.Timeline.Columns.STATUS
	};
	
	private static final String[] FROM = new String[COLUMNS.length-1];
	static {
		System.arraycopy(COLUMNS, 1, FROM, 0, FROM.length);
	}

	private static final int[] TO = {
		R.id.user,
		R.id.time,
		R.id.status
	};
	
	public TimelineFragment() {
		// TODO Auto-generated constructor stub
	}
	
	static class TimelineViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int colIndex) {
            if (R.id.time != view.getId()) { return false; }

            long t = cursor.getLong(colIndex);
            ((TextView) view).setText(
                (0 >= t)
                    ? "long ago"
                    : DateUtils.getRelativeTimeSpanString(t, System.currentTimeMillis(), 0));
            return true;
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		//create adapter
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				this.getActivity(),
	    		R.layout.timeline_row,
	    		null,
	    		FROM,
	    		TO, 
	    		0);
	   
        setListAdapter(adapter);
        adapter.setViewBinder(new TimelineViewBinder());
		getLoaderManager().initLoader(LOADER_ID, null, this);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(  // this will cause a query into ContentProvider
				this.getActivity(), 
				YambaContract.Timeline.URI, 
				COLUMNS, 
				null, 
				null, 
				YambaContract.Timeline.Columns.TIMESTAMP + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
		((SimpleCursorAdapter) getListAdapter()).swapCursor(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
	}

}
