package com.nexelem.boxplorer;

import android.app.Activity;
import android.graphics.Typeface;

public class Fonts {
	
	public static Typeface EXTRA_LIGHT_FONT;
	public static Typeface LIGHT_FONT;
	public static Typeface REGULAR_FONT;

	public static void loadFonts(Activity activity){
		EXTRA_LIGHT_FONT = Typeface.createFromAsset(activity.getAssets(), "fonts/TitilliumWeb-ExtraLight.ttf");  
		LIGHT_FONT = Typeface.createFromAsset(activity.getAssets(), "fonts/TitilliumWeb-Light.ttf");  
		REGULAR_FONT = Typeface.createFromAsset(activity.getAssets(), "fonts/TitilliumWeb-Regular.ttf");  
	}
}
