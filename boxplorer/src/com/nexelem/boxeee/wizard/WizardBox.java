package com.nexelem.boxeee.wizard;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nexelem.boxplorer.R;

public class WizardBox extends DialogFragment {

    private static final int STEPS_NUMBER = 3;
	private ViewPager pager;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.wizard, container);
        getDialog().setTitle("Add box");
        
        if(pager == null){
	        pager = (ViewPager) view.findViewById(R.id.pager);
	        PageAdapter adapter = new PageAdapter(getChildFragmentManager(), this);
	        pager.setAdapter(adapter);
        }
        return view;
	}
	
	public ViewPager getPager(){
		return pager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}

    private class PageAdapter extends FragmentStatePagerAdapter {
        private WizardBox wizardBox;

		public PageAdapter(FragmentManager fm, WizardBox wizardBox) {
            super(fm);
            this.wizardBox = wizardBox;
        }

        @Override
        public Fragment getItem(int position) {
        	switch(position){
        	case 0: 
            	return new WizardBoxStepOne(wizardBox);
        	case 1:
            	return new WizardBoxStepTwo();
        	case 2:
            	return new WizardBoxStepThree();
        	}
        	return null;
        }

        @Override
        public int getCount() {
            return STEPS_NUMBER;
        }
    }
}
