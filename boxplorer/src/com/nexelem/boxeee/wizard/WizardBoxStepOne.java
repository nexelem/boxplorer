package com.nexelem.boxeee.wizard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.nexelem.boxplorer.R;

public class WizardBoxStepOne extends Fragment {
	
	private WizardBox wizardBox;

	public WizardBoxStepOne(WizardBox wizardBox) {
		this.wizardBox = wizardBox;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.wizard_box_1, container, false);
        
        Button next = (Button) rootView.findViewById(R.id.next);
        next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				wizardBox.getPager().setCurrentItem(1);
			}
		});
        
        return rootView;
    }
}
