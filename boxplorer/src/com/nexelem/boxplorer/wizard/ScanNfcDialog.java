package com.nexelem.boxplorer.wizard;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.nexelem.boxplorer.Fonts;
import com.nexelem.boxplorer.R;

public class ScanNfcDialog extends DialogFragment {

	public ScanNfcDialog(){
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		View v = inflater.inflate(R.layout.scan_nfc_dialog, container, false);
		
		// Title
		TextView title = (TextView) v.findViewById(R.id.scan_title);
		title.setTypeface(Fonts.REGULAR_FONT);
		
		// Message
		TextView info = (TextView) v.findViewById(R.id.scan_info);
		info.setTypeface(Fonts.REGULAR_FONT);
		
		Button cancelButton = (Button) v.findViewById(R.id.button_cancel);
		cancelButton.setTypeface(Fonts.REGULAR_FONT);
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		return v;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

}
