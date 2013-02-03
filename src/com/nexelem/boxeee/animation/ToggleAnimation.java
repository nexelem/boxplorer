package com.nexelem.boxeee.animation;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ToggleAnimation extends Animation {
	
    private LinearLayout childView;
    private LayoutParams params;
    private int start, end;
    private boolean visible = false;
    private boolean ended = false;

    public ToggleAnimation(LinearLayout view, int duration) {
        setDuration(duration);
        childView = view;
        params = (LayoutParams) view.getLayoutParams();

        visible = (view.getVisibility() == View.VISIBLE);
        start = params.bottomMargin;
        end = (start == 0 ? (0 - view.getHeight()) : 0);
   
        view.setVisibility(View.VISIBLE);
        setInterpolator(new AccelerateInterpolator());
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        if (interpolatedTime < 1.0f) {
        	params.bottomMargin = start + (int) ((end - start) * interpolatedTime);
        	childView.requestLayout();
        } else if (!ended) {
        	params.bottomMargin = end;
            childView.requestLayout();
            if (visible) {
            	childView.setVisibility(View.GONE);
            }
            ended = true;
        }
    }
}