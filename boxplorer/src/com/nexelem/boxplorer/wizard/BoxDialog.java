package com.nexelem.boxplorer.wizard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.nexelem.boxplorer.R;
import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.util.QRCodeUtils;
import com.nexelem.boxplorer.utils.ObjectKeeper;

public class BoxDialog extends DialogFragment {
	
	private Box box;
	private boolean isQr;
	private boolean isNfc;
	
	private int step;
	private ViewFlipper flipper;
	private boolean update = false;

	public BoxDialog(){
	}
	
	public BoxDialog(Box box){
		this.box = box;
		update = true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("name", box.getName());
		outState.putString("localization", box.getLocation());
		outState.putBoolean("isQr", isQr);
		outState.putBoolean("isNfc", isNfc);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
				
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.wizard, null);
        
        flipper = (ViewFlipper) view.findViewById(R.id.flipper);
        
        View step1 = inflater.inflate(R.layout.wizard_box_1, null);
        View step2 = inflater.inflate(R.layout.wizard_box_2, null);
        View step3 = inflater.inflate(R.layout.wizard_box_3, null);

        flipper.addView(step1,0);
        flipper.addView(step2,1);
        flipper.addView(step3,2);

        flipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right));
        
        step = 0;

        // Step 1 
        final EditText name = (EditText) step1.findViewById(R.id.box_add_name);
		final EditText localization = (EditText) step1.findViewById(R.id.box_add_localization);
		final CheckBox isQr = (CheckBox) step1.findViewById(R.id.box_add_qr);
		final CheckBox isNfc = (CheckBox) step1.findViewById(R.id.box_add_nfc);
		
		if(box != null){
			name.setText(box.getName());
			localization.setText(box.getLocation());
		} else {
			box = new Box();
		}
		// Step 2
		final ImageView image = (ImageView) step2.findViewById(R.id.box_add_generated_qr);

		if(savedInstanceState != null){
			name.setText(savedInstanceState.getString("name"));
			localization.setText(savedInstanceState.getString("localization"));
			isQr.setChecked(savedInstanceState.getBoolean("isQr"));
			isNfc.setChecked(savedInstanceState.getBoolean("isNfc"));
		}
		
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    
	    builder.setTitle("Add box");
	    builder.setPositiveButton("Create", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
	    
	    builder.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
	    
	    builder.setView(view);
	    
	    final AlertDialog dialog = builder.create();
	    dialog.show();
	    
	    Button positive = (Button) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
	    positive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				switch(step){
				case 0:
					if(name.getEditableText().length() == 0){
						Toast.makeText(getActivity(), "Insert box name", Toast.LENGTH_LONG).show();
						return;
					} 
					
					BoxDialog.this.box.setName(name.getEditableText().toString());
					BoxDialog.this.box.setLocation(localization.getEditableText().toString());
					BoxDialog.this.isQr = isQr.isChecked();
					BoxDialog.this.isNfc = isNfc.isChecked();

					try {
						if(update)
							ObjectKeeper.getInstance().getBoxService().update(box);
						else
							ObjectKeeper.getInstance().getBoxService().create(box);
						ObjectKeeper.getInstance().getListAdapter().updateListAdapterData();
						ObjectKeeper.getInstance().getListAdapter().notifyDataSetChanged();
					} catch (BusinessException e) {
						e.printStackTrace();
					}
					
					if(isQr.isChecked()){
						setStep(1);
						image.setImageBitmap(QRCodeUtils.generateQRCode(getActivity(), box.getId().toString()));
					} else if(isNfc.isChecked()){
						setStep(2);
					} else {
						dialog.cancel();
					}
					break;
					
				case 1:
					if(isNfc.isChecked()){
						setStep(2);
					} else {
						dialog.cancel();
					}
					break;
				case 2:
					dialog.cancel();
				}				
			}	
		});
	    
	    Button negative = (Button) dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
	    negative.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switch(step){
				case 0:
					dialog.cancel();
					break;
				case 1:
					setStep(0);
					break;
				case 2:
					if(isQr.isChecked()){
						setStep(1);
					} else {
						setStep(0);
					}
					break;
				}
			}	
		});
	    
	    return dialog;
	}
	
	private void setStep(int i){
		flipper.setDisplayedChild(i);
		step = i;
	}
	
}
