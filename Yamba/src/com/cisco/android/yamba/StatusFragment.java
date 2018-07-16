package com.cisco.android.yamba;

import com.cisco.android.yamba.svc.YambaService;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class StatusFragment extends Fragment {
	private static final int LOW_AVAIL_INPUT = 10;
	private static final int MAX_INPUT_LEN = 140;
	private EditText statusText;
	private TextView count;
	private Button button;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.status, container);
		
		statusText = (EditText)view.findViewById(R.id.input);
		count = (TextView) view.findViewById(R.id.count);
		button = (Button) view.findViewById(R.id.submit);

		statusText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				updateCount();
			}

		});

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				submit();

			}
		});
		
		return view;
	}

	protected void updateCount() {
		// get the current remaining chars allowed
		int remaining_count = MAX_INPUT_LEN - statusText.getText().toString().length();
		count.setText(String.valueOf(remaining_count));

		// make the color green if remaining is >10
		// make the color red if remaining is < 10
		if (remaining_count > 0 && remaining_count < LOW_AVAIL_INPUT) {
			count.setTextColor(Color.YELLOW);
		} else if (remaining_count == 0) {
			count.setTextColor(Color.RED);
		}
	}

	void submit() {
		String status = statusText.getText().toString();
		if(TextUtils.isEmpty(status)) { return; }
		
		if (BuildConfig.DEBUG) {
			Log.d("***", "Submitted");
		}
		
		YambaService.post(this.getActivity(),status);
		
		//reset the text field
		count.setText(String.valueOf(140));
		count.setTextColor(getResources().getColor(R.color.green));
		statusText.setText("");
	}
	
	public StatusFragment() {
		// TODO Auto-generated constructor stub
	}

}
