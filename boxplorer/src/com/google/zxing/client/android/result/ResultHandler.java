/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.result;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.zxing.Result;
import com.google.zxing.client.android.PreferencesActivity;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ResultParser;

/**
 * A base class for the Android-specific barcode handlers. These allow the app
 * to polymorphically suggest the appropriate actions for each data type.
 * 
 * This class also contains a bunch of utility methods to take common actions
 * like opening a URL. They could easily be moved into a helper object, but it
 * can't be static because the Activity instance is needed to launch an intent.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public abstract class ResultHandler {

	private static final String TAG = ResultHandler.class.getSimpleName();

	public static final int MAX_BUTTON_COUNT = 4;

	private final ParsedResult result;
	private final Activity activity;
	private final Result rawResult;
	private final String customProductSearch;

	ResultHandler(Activity activity, ParsedResult result) {
		this(activity, result, null);
	}

	ResultHandler(Activity activity, ParsedResult result, Result rawResult) {
		this.result = result;
		this.activity = activity;
		this.rawResult = rawResult;
		this.customProductSearch = this.parseCustomSearchURL();
	}

	public ParsedResult getResult() {
		return this.result;
	}

	boolean hasCustomProductSearch() {
		return this.customProductSearch != null;
	}

	Activity getActivity() {
		return this.activity;
	}

	/**
	 * Indicates how many buttons the derived class wants shown.
	 * 
	 * @return The integer button count.
	 */
	public abstract int getButtonCount();

	/**
	 * The text of the nth action button.
	 * 
	 * @param index
	 *            From 0 to getButtonCount() - 1
	 * @return The button text as a resource ID
	 */
	public abstract int getButtonText(int index);

	/**
	 * Execute the action which corresponds to the nth button.
	 * 
	 * @param index
	 *            The button that was clicked.
	 */
	public abstract void handleButtonPress(int index);

	/**
	 * Some barcode contents are considered secure, and should not be saved to
	 * history, copied to the clipboard, or otherwise persisted.
	 * 
	 * @return If true, do not create any permanent record of these contents.
	 */
	public boolean areContentsSecure() {
		return false;
	}

	/**
	 * Create a possibly styled string for the contents of the current barcode.
	 * 
	 * @return The text to be displayed.
	 */
	public CharSequence getDisplayContents() {
		String contents = this.result.getDisplayResult();
		return contents.replace("\r", "");
	}

	/**
	 * A string describing the kind of barcode that was found, e.g.
	 * "Found contact info".
	 * 
	 * @return The resource ID of the string.
	 */
	public abstract int getDisplayTitle();

	/**
	 * A convenience method to get the parsed type. Should not be overridden.
	 * 
	 * @return The parsed type, e.g. URI or ISBN
	 */
	public final ParsedResultType getType() {
		return this.result.getType();
	}

	/**
	 * Like {@link #launchIntent(Intent)} but will tell you if it is not
	 * handle-able via {@link ActivityNotFoundException}.
	 * 
	 * @throws ActivityNotFoundException
	 */
	void rawLaunchIntent(Intent intent) {
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			Log.d(TAG, "Launching intent: " + intent + " with extras: "
					+ intent.getExtras());
			this.activity.startActivity(intent);
		}
	}

	private String parseCustomSearchURL() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.activity);
		String customProductSearch = prefs.getString(
				PreferencesActivity.KEY_CUSTOM_PRODUCT_SEARCH, null);
		if ((customProductSearch != null)
				&& (customProductSearch.trim().length() == 0)) {
			return null;
		}
		return customProductSearch;
	}

	String fillInCustomSearchURL(String text) {
		if (this.customProductSearch == null) {
			return text; // ?
		}
		String url = this.customProductSearch.replace("%s", text);
		if (this.rawResult != null) {
			url = url.replace("%f", this.rawResult.getBarcodeFormat()
					.toString());
			if (url.contains("%t")) {
				ParsedResult parsedResultAgain = ResultParser
						.parseResult(this.rawResult);
				url = url.replace("%t", parsedResultAgain.getType().toString());
			}
		}
		return url;
	}

}
