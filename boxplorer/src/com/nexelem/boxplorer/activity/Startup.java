package com.nexelem.boxplorer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.nexelem.boxplorer.R;

public class Startup extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.startup);
		this.getActionBar().hide();

		View logo = this.findViewById(R.id.logo);
		View all = this.findViewById(R.id.startup);

		AlphaAnimation alpha = new AlphaAnimation(0, 1);
		alpha.setInterpolator(new AccelerateInterpolator());
		alpha.setDuration(500);
		alpha.setFillAfter(true);
		alpha.setStartOffset(500);
		logo.startAnimation(alpha);

		AlphaAnimation fadeout = new AlphaAnimation(1, 0);
		fadeout.setInterpolator(new AccelerateInterpolator());
		fadeout.setDuration(500);
		fadeout.setFillAfter(true);
		fadeout.setStartOffset(2000);
		fadeout.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				Intent intent = new Intent(Startup.this, Main.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Startup.this.startActivity(intent);
				Startup.this.finish();
			}
		});
		all.startAnimation(fadeout);
	}

}
