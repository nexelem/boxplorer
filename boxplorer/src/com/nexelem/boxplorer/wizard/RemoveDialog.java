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

public class RemoveDialog extends DialogFragment {

	private int title;
	private int message;
	private OnClickListener onRejectListener;
	private OnClickListener onAcceptListener;

	public RemoveDialog(){
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		View v = inflater.inflate(R.layout.remove_dialog, container, false);
		
		// Title
		TextView title = (TextView) v.findViewById(R.id.remove_title);
		title.setText(this.title);
		title.setTypeface(Fonts.REGULAR_FONT);
		
		// Message
		TextView message = (TextView) v.findViewById(R.id.remove_question);
		message.setText(this.message);
		message.setTypeface(Fonts.REGULAR_FONT);
		
		Button cancelButton = (Button) v.findViewById(R.id.button_cancel);
		cancelButton.setTypeface(Fonts.REGULAR_FONT);
		cancelButton.setOnClickListener(this.onRejectListener);

		Button saveButton = (Button) v.findViewById(R.id.button_ok);
		saveButton.setTypeface(Fonts.REGULAR_FONT);
		saveButton.setOnClickListener(this.onAcceptListener);
		
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

	public void setTitle(int title) {
		this.title = title;
	}

	public void setMessage(int message) {
		this.message = message;
	}

	public void setOnRejectListener(OnClickListener onRejectListener) {
		this.onRejectListener = onRejectListener;
	}

	public void setOnAcceptListener(OnClickListener onAcceptListener) {
		this.onAcceptListener = onAcceptListener;
	}

}
