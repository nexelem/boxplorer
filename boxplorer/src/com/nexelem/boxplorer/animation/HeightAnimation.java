package com.nexelem.boxplorer.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class HeightAnimation extends Animation {

	int fromHeight;
	View mView;
	int toHeight;
	
	public HeightAnimation(View view, int toHeight) {
	    this.mView = view;
	    this.fromHeight = view.getHeight();
	    this.toHeight = toHeight;
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
	    int newHeight = (int) (fromHeight +((toHeight - fromHeight)* interpolatedTime));
	    mView.getLayoutParams().height = newHeight;
	    mView.requestLayout();
	}
	
	@Override
	public void initialize(int width, int height, int parentWidth,int parentHeight) {
	    super.initialize(width, height, parentWidth, parentHeight);
	}
	
	@Override
	public boolean willChangeBounds() {
	    return true;
	}
}