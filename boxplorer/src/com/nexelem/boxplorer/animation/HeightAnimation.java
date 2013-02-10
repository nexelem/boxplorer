package com.nexelem.boxplorer.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class HeightAnimation extends Animation {

	int mFromHeight;
	View mView;
	private boolean hide;
	
	public HeightAnimation(View view, boolean hide) {
	    this.mView = view;
	    this.mFromHeight = view.getHeight();
	    this.hide = hide;
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
	    int newHeight;
	    newHeight = (int) (mFromHeight * (hide ? 1 : 0) - interpolatedTime);
	    mView.getLayoutParams().height = newHeight;
	    //mView.setAlpha(interpolatedTime);
	    mView.requestLayout();
	}
	
	@Override
	public void initialize(int width, int height, int parentWidth,
	        int parentHeight) {
	    super.initialize(width, height, parentWidth, parentHeight);
	}
	
	@Override
	public boolean willChangeBounds() {
	    return true;
	}
}