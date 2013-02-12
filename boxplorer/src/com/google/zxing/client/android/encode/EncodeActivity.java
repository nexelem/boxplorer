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

package com.google.zxing.client.android.encode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.Intents;
import com.nexelem.boxplorer.R;

/**
 * This class encodes data from an Intent into a QR code, and then displays it
 * full screen so that another person can scan it with their device.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class EncodeActivity extends Activity {

	private static final String TAG = EncodeActivity.class.getSimpleName();

	private static final String USE_VCARD_KEY = "USE_VCARD";

	private QRCodeEncoder qrCodeEncoder;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Intent intent = this.getIntent();
		if (intent == null) {
			this.finish();
		} else {
			String action = intent.getAction();
			if (Intents.Encode.ACTION.equals(action)
					|| Intent.ACTION_SEND.equals(action)) {
				this.setContentView(R.layout.encode_qr);
			} else {
				this.finish();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// This assumes the view is full screen, which is a good assumption
		WindowManager manager = (WindowManager) this
				.getSystemService(WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		int smallerDimension = width < height ? width : height;
		smallerDimension = (smallerDimension * 7) / 8;

		Intent intent = this.getIntent();
		if (intent == null) {
			return;
		}

		try {
			boolean useVCard = intent.getBooleanExtra(USE_VCARD_KEY, false);
			this.qrCodeEncoder = new QRCodeEncoder(this, intent,
					smallerDimension, useVCard);
			Bitmap bitmap = this.qrCodeEncoder.encodeAsBitmap();
			if (bitmap == null) {
				Log.w(TAG, "Could not encode barcode");
				this.qrCodeEncoder = null;
				return;
			}

			ImageView view = (ImageView) this.findViewById(R.id.image_view);
			view.setImageBitmap(bitmap);

			TextView contents = (TextView) this
					.findViewById(R.id.contents_text_view);
			if (intent.getBooleanExtra(Intents.Encode.SHOW_CONTENTS, true)) {
				contents.setText(this.qrCodeEncoder.getDisplayContents());
				this.setTitle(this.qrCodeEncoder.getTitle());
			} else {
				contents.setText("");
				this.setTitle("");
			}
		} catch (WriterException e) {
			Log.w(TAG, "Could not encode barcode", e);
			this.qrCodeEncoder = null;
		}
	}

}
