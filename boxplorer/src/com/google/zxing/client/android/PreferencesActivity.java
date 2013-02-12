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

package com.google.zxing.client.android;

import java.util.ArrayList;
import java.util.Collection;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

/**
 * The main settings activity.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class PreferencesActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public static final String KEY_DECODE_1D = "preferences_decode_1D";
	public static final String KEY_DECODE_QR = "preferences_decode_QR";
	public static final String KEY_DECODE_DATA_MATRIX = "preferences_decode_Data_Matrix";
	public static final String KEY_CUSTOM_PRODUCT_SEARCH = "preferences_custom_product_search";

	public static final String KEY_PLAY_BEEP = "preferences_play_beep";
	public static final String KEY_VIBRATE = "preferences_vibrate";
	public static final String KEY_COPY_TO_CLIPBOARD = "preferences_copy_to_clipboard";
	public static final String KEY_FRONT_LIGHT = "preferences_front_light";
	public static final String KEY_BULK_MODE = "preferences_bulk_mode";
	public static final String KEY_REMEMBER_DUPLICATES = "preferences_remember_duplicates";
	public static final String KEY_SUPPLEMENTAL = "preferences_supplemental";
	public static final String KEY_AUTO_FOCUS = "preferences_auto_focus";
	public static final String KEY_SEARCH_COUNTRY = "preferences_search_country";

	public static final String KEY_DISABLE_CONTINUOUS_FOCUS = "preferences_disable_continuous_focus";
	// public static final String KEY_DISABLE_EXPOSURE =
	// "preferences_disable_exposure";

	public static final String KEY_HELP_VERSION_SHOWN = "preferences_help_version_shown";

	private CheckBoxPreference decode1D;
	private CheckBoxPreference decodeQR;
	private CheckBoxPreference decodeDataMatrix;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		this.disableLastCheckedPref();
	}

	private void disableLastCheckedPref() {
		Collection<CheckBoxPreference> checked = new ArrayList<CheckBoxPreference>(
				3);
		if (this.decode1D.isChecked()) {
			checked.add(this.decode1D);
		}
		if (this.decodeQR.isChecked()) {
			checked.add(this.decodeQR);
		}
		if (this.decodeDataMatrix.isChecked()) {
			checked.add(this.decodeDataMatrix);
		}
		boolean disable = checked.size() < 2;
		CheckBoxPreference[] checkBoxPreferences = { this.decode1D,
				this.decodeQR, this.decodeDataMatrix };
		for (CheckBoxPreference pref : checkBoxPreferences) {
			pref.setEnabled(!(disable && checked.contains(pref)));
		}
	}

}
