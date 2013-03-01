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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.nexelem.boxplorer.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.common.BitMatrix;

/**
 * This class does the work of decoding the user's request and extracting all
 * the data to be encoded in a barcode.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class QRCodeEncoder {

	private static final String TAG = QRCodeEncoder.class.getSimpleName();

	private static final int WHITE = 0xFFFFFFFF;
	private static final int BLACK = 0xFF000000;

	private final Activity activity;
	private String contents;
	private String displayContents;
	private String title;
	private BarcodeFormat format;
	private final int dimension;
	private final boolean useVCard;

	public QRCodeEncoder(Activity activity, Intent intent, int dimension,
			boolean useVCard) throws WriterException {
		this.activity = activity;
		this.dimension = dimension;
		this.useVCard = useVCard;
		String action = intent.getAction();
		if (action.equals(Intents.Encode.ACTION)) {
			this.encodeContentsFromZXingIntent(intent);
		} else if (action.equals(Intent.ACTION_SEND)) {
			this.encodeContentsFromShareIntent(intent);
		}
	}

	String getContents() {
		return this.contents;
	}

	String getDisplayContents() {
		return this.displayContents;
	}

	String getTitle() {
		return this.title;
	}

	boolean isUseVCard() {
		return this.useVCard;
	}

	// It would be nice if the string encoding lived in the core ZXing library,
	// but we use platform specific code like PhoneNumberUtils, so it can't.
	private boolean encodeContentsFromZXingIntent(Intent intent) {
		// Default to QR_CODE if no format given.
		String formatString = intent.getStringExtra(Intents.Encode.FORMAT);
		this.format = null;
		if (formatString != null) {
			try {
				this.format = BarcodeFormat.valueOf(formatString);
			} catch (IllegalArgumentException iae) {
				// Ignore it then
			}
		}
		if ((this.format == null) || (this.format == BarcodeFormat.QR_CODE)) {
			String type = intent.getStringExtra(Intents.Encode.TYPE);
			if ((type == null) || (type.length() == 0)) {
				return false;
			}
			this.format = BarcodeFormat.QR_CODE;
			this.encodeQRCodeContents(intent, type);
		} else {
			String data = intent.getStringExtra(Intents.Encode.DATA);
			if ((data != null) && (data.length() > 0)) {
				this.contents = data;
				this.displayContents = data;
				this.title = this.activity.getString(R.string.contents_text);
			}
		}
		return (this.contents != null) && (this.contents.length() > 0);
	}

	// Handles send intents from multitude of Android applications
	private void encodeContentsFromShareIntent(Intent intent)
			throws WriterException {
		// Check if this is a plain text encoding, or contact
		if (intent.hasExtra(Intent.EXTRA_STREAM)) {
			this.encodeFromStreamExtra(intent);
		} else {
			this.encodeFromTextExtras(intent);
		}
	}

	private void encodeFromTextExtras(Intent intent) throws WriterException {
		// Notice: Google Maps shares both URL and details in one text, bummer!
		String theContents = ContactEncoder.trim(intent
				.getStringExtra(Intent.EXTRA_TEXT));
		if (theContents == null) {
			theContents = ContactEncoder.trim(intent
					.getStringExtra("android.intent.extra.HTML_TEXT"));
			// Intent.EXTRA_HTML_TEXT
			if (theContents == null) {
				theContents = ContactEncoder.trim(intent
						.getStringExtra(Intent.EXTRA_SUBJECT));
				if (theContents == null) {
					String[] emails = intent
							.getStringArrayExtra(Intent.EXTRA_EMAIL);
					if (emails != null) {
						theContents = ContactEncoder.trim(emails[0]);
					} else {
						theContents = "?";
					}
				}
			}
		}

		// Trim text to avoid URL breaking.
		if ((theContents == null) || (theContents.length() == 0)) {
			throw new WriterException("Empty EXTRA_TEXT");
		}
		this.contents = theContents;
		// We only do QR code.
		this.format = BarcodeFormat.QR_CODE;
		if (intent.hasExtra(Intent.EXTRA_SUBJECT)) {
			this.displayContents = intent.getStringExtra(Intent.EXTRA_SUBJECT);
		} else if (intent.hasExtra(Intent.EXTRA_TITLE)) {
			this.displayContents = intent.getStringExtra(Intent.EXTRA_TITLE);
		} else {
			this.displayContents = this.contents;
		}
		this.title = this.activity.getString(R.string.contents_text);
	}

	// Handles send intents from the Contacts app, retrieving a contact as a
	// VCARD.
	private void encodeFromStreamExtra(Intent intent) throws WriterException {
		this.format = BarcodeFormat.QR_CODE;
		Bundle bundle = intent.getExtras();
		if (bundle == null) {
			throw new WriterException("No extras");
		}
		Uri uri = (Uri) bundle.getParcelable(Intent.EXTRA_STREAM);
		if (uri == null) {
			throw new WriterException("No EXTRA_STREAM");
		}
		byte[] vcard;
		String vcardString;
		try {
			InputStream stream = this.activity.getContentResolver()
					.openInputStream(uri);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[2048];
			int bytesRead;
			while ((bytesRead = stream.read(buffer)) > 0) {
				baos.write(buffer, 0, bytesRead);
			}
			vcard = baos.toByteArray();
			vcardString = new String(vcard, 0, vcard.length, "UTF-8");
		} catch (IOException ioe) {
			throw new WriterException(ioe);
		}
		Log.d(TAG, "Encoding share intent content:");
		Log.d(TAG, vcardString);

	}

	private void encodeQRCodeContents(Intent intent, String type) {
		if (type.equals(Contents.Type.TEXT)) {
			String data = intent.getStringExtra(Intents.Encode.DATA);
			if ((data != null) && (data.length() > 0)) {
				this.contents = data;
				this.displayContents = data;
				this.title = this.activity.getString(R.string.contents_text);
			}
		}
	}

	public Bitmap encodeAsBitmap() throws WriterException {
		String contentsToEncode = this.contents;
		if (contentsToEncode == null) {
			return null;
		}
		Map<EncodeHintType, Object> hints = null;
		String encoding = guessAppropriateEncoding(contentsToEncode);
		if (encoding != null) {
			hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
			hints.put(EncodeHintType.CHARACTER_SET, encoding);
		}
		MultiFormatWriter writer = new MultiFormatWriter();
		BitMatrix result;
		try {
			result = writer.encode(contentsToEncode, this.format,
					this.dimension, this.dimension, hints);
		} catch (IllegalArgumentException iae) {
			// Unsupported format
			return null;
		}
		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	private static String guessAppropriateEncoding(CharSequence contents) {
		// Very crude at the moment
		for (int i = 0; i < contents.length(); i++) {
			if (contents.charAt(i) > 0xFF) {
				return "UTF-8";
			}
		}
		return null;
	}

}
