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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.result.ResultButtonListener;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.result.ResultHandlerFactory;
import com.nexelem.boxplorer.R;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {

	private static final String TAG = CaptureActivity.class.getSimpleName();

	private static final long DEFAULT_INTENT_RESULT_DURATION_MS = 1500L;

	private static final String PRODUCT_SEARCH_URL_PREFIX = "http://www.google";
	private static final String PRODUCT_SEARCH_URL_SUFFIX = "/m/products/scan";
	private static final String[] ZXING_URLS = { "http://zxing.appspot.com/scan", "zxing://scan/" };
	private static final String RETURN_CODE_PLACEHOLDER = "{CODE}";
	private static final String RETURN_URL_PARAM = "ret";
	private static final String RAW_PARAM = "raw";

	public static final int HISTORY_REQUEST_CODE = 0x0000bacc;

	private static final Set<ResultMetadataType> DISPLAYABLE_METADATA_TYPES = EnumSet.of(ResultMetadataType.ISSUE_NUMBER, ResultMetadataType.SUGGESTED_PRICE, ResultMetadataType.ERROR_CORRECTION_LEVEL, ResultMetadataType.POSSIBLE_COUNTRY);

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	private TextView statusView;
	private View resultView;
	private Result lastResult;
	private boolean hasSurface;
	private IntentSource source;
	private String sourceUrl;
	private String returnUrlTemplate;
	private boolean returnRaw;
	private Collection<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private BeepManager beepManager;

	ViewfinderView getViewfinderView() {
		return this.viewfinderView;
	}

	public Handler getHandler() {
		return this.handler;
	}

	CameraManager getCameraManager() {
		return this.cameraManager;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Window window = this.getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setContentView(R.layout.capture_qr);

		this.hasSurface = false;
		this.inactivityTimer = new InactivityTimer(this);
		this.beepManager = new BeepManager(this);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// CameraManager must be initialized here, not in onCreate(). This is
		// necessary because we don't
		// want to open the camera driver and measure the screen size if we're
		// going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the
		// wrong size and partially
		// off screen.
		this.cameraManager = new CameraManager(this.getApplication());

		this.viewfinderView = (ViewfinderView) this.findViewById(R.id.viewfinder_view);
		this.viewfinderView.setCameraManager(this.cameraManager);

		this.resultView = this.findViewById(R.id.result_view);
		this.statusView = (TextView) this.findViewById(R.id.status_view);

		this.handler = null;
		this.lastResult = null;

		this.resetStatusView();

		SurfaceView surfaceView = (SurfaceView) this.findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (this.hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			this.initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}

		this.beepManager.updatePrefs();

		this.inactivityTimer.onResume();

		Intent intent = this.getIntent();

		this.source = IntentSource.NATIVE_APP_INTENT;
		this.decodeFormats = null;
		this.characterSet = null;

		if (intent != null) {

			String action = intent.getAction();
			String dataString = intent.getDataString();

			if (Intents.Scan.ACTION.equals(action)) {

				// Scan the formats the intent requested, and return the result
				// to the calling activity.
				this.source = IntentSource.NATIVE_APP_INTENT;
				this.decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);

				if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
					int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
					int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
					if ((width > 0) && (height > 0)) {
						this.cameraManager.setManualFramingRect(width, height);
					}
				}

				String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
				if (customPromptMessage != null) {
					this.statusView.setText(customPromptMessage);
				}

			} else if ((dataString != null) && dataString.contains(PRODUCT_SEARCH_URL_PREFIX) && dataString.contains(PRODUCT_SEARCH_URL_SUFFIX)) {

				// Scan only products and send the result to mobile Product
				// Search.
				this.source = IntentSource.PRODUCT_SEARCH_LINK;
				this.sourceUrl = dataString;
				this.decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;

			} else if (isZXingURL(dataString)) {

				// Scan formats requested in query string (all formats if none
				// specified).
				// If a return URL is specified, send the results there.
				// Otherwise, handle it ourselves.
				this.source = IntentSource.ZXING_LINK;
				this.sourceUrl = dataString;
				Uri inputUri = Uri.parse(this.sourceUrl);
				this.returnUrlTemplate = inputUri.getQueryParameter(RETURN_URL_PARAM);
				this.returnRaw = inputUri.getQueryParameter(RAW_PARAM) != null;
				this.decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);

			}

			this.characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

		}
	}

	private static boolean isZXingURL(String dataString) {
		if (dataString == null) {
			return false;
		}
		for (String url : ZXING_URLS) {
			if (dataString.startsWith(url)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onPause() {
		if (this.handler != null) {
			this.handler.quitSynchronously();
			this.handler = null;
		}
		this.inactivityTimer.onPause();
		this.cameraManager.closeDriver();
		if (!this.hasSurface) {
			SurfaceView surfaceView = (SurfaceView) this.findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		this.inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (this.source == IntentSource.NATIVE_APP_INTENT) {
				this.setResult(RESULT_CANCELED);
				this.finish();
				return true;
			}
			if (((this.source == IntentSource.NONE) || (this.source == IntentSource.ZXING_LINK)) && (this.lastResult != null)) {
				this.restartPreviewAfterDelay(0L);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_FOCUS:
		case KeyEvent.KEYCODE_CAMERA:
			// Handle these events so they don't launch the Camera app
			return true;
			// Use volume up/down to turn on light
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			this.cameraManager.setTorch(false);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			this.cameraManager.setTorch(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		return super.onOptionsItemSelected(item);
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (this.handler == null) {
			this.savedResultToShow = result;
		} else {
			if (result != null) {
				this.savedResultToShow = result;
			}
			if (this.savedResultToShow != null) {
				Message message = Message.obtain(this.handler, R.id.decode_succeeded, this.savedResultToShow);
				this.handler.sendMessage(message);
			}
			this.savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!this.hasSurface) {
			this.hasSurface = true;
			this.initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		this.hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode) {
		this.inactivityTimer.onActivity();
		this.lastResult = rawResult;
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);

		boolean fromLiveScan = barcode != null;
		if (fromLiveScan) {
			// Then not from history, so beep/vibrate and we have an image to
			// draw on
			this.beepManager.playBeepSoundAndVibrate();
			this.drawResultPoints(barcode, rawResult);
		}

		switch (this.source) {
		case NATIVE_APP_INTENT:
		case PRODUCT_SEARCH_LINK:
			this.handleDecodeExternally(rawResult, resultHandler, barcode);
			break;
		case ZXING_LINK:
			if (this.returnUrlTemplate == null) {
				this.handleDecodeInternally(rawResult, resultHandler, barcode);
			} else {
				this.handleDecodeExternally(rawResult, resultHandler, barcode);
			}
			break;
		case NONE:
			this.handleDecodeExternally(rawResult, resultHandler, barcode);
			break;
		}
	}

	/**
	 * Superimpose a line for 1D or dots for 2D to highlight the key features of
	 * the barcode.
	 * 
	 * @param barcode
	 *            A bitmap of the captured image.
	 * @param rawResult
	 *            The decoded results which contains the points to draw.
	 */
	private void drawResultPoints(Bitmap barcode, Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if ((points != null) && (points.length > 0)) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(this.getResources().getColor(R.color.result_points));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1]);
			} else if ((points.length == 4) && ((rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A) || (rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13))) {
				// Hacky special case -- draw two lines, for the barcode and
				// metadata
				drawLine(canvas, paint, points[0], points[1]);
				drawLine(canvas, paint, points[2], points[3]);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					canvas.drawPoint(point.getX(), point.getY(), paint);
				}
			}
		}
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b) {
		canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
	}

	// Put up our own UI for how to handle the decoded contents.
	private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
		this.statusView.setVisibility(View.GONE);
		this.viewfinderView.setVisibility(View.GONE);
		this.resultView.setVisibility(View.VISIBLE);

		ImageView barcodeImageView = (ImageView) this.findViewById(R.id.barcode_image_view);
		if (barcode == null) {
			barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher));
		} else {
			barcodeImageView.setImageBitmap(barcode);
		}

		TextView formatTextView = (TextView) this.findViewById(R.id.format_text_view);
		formatTextView.setText(rawResult.getBarcodeFormat().toString());

		TextView typeTextView = (TextView) this.findViewById(R.id.type_text_view);
		typeTextView.setText(resultHandler.getType().toString());

		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		String formattedTime = formatter.format(new Date(rawResult.getTimestamp()));
		TextView timeTextView = (TextView) this.findViewById(R.id.time_text_view);
		timeTextView.setText(formattedTime);

		TextView metaTextView = (TextView) this.findViewById(R.id.meta_text_view);
		View metaTextViewLabel = this.findViewById(R.id.meta_text_view_label);
		metaTextView.setVisibility(View.GONE);
		metaTextViewLabel.setVisibility(View.GONE);
		Map<ResultMetadataType, Object> metadata = rawResult.getResultMetadata();
		if (metadata != null) {
			StringBuilder metadataText = new StringBuilder(20);
			for (Map.Entry<ResultMetadataType, Object> entry : metadata.entrySet()) {
				if (DISPLAYABLE_METADATA_TYPES.contains(entry.getKey())) {
					metadataText.append(entry.getValue()).append('\n');
				}
			}
			if (metadataText.length() > 0) {
				metadataText.setLength(metadataText.length() - 1);
				metaTextView.setText(metadataText);
				metaTextView.setVisibility(View.VISIBLE);
				metaTextViewLabel.setVisibility(View.VISIBLE);
			}
		}

		TextView contentsTextView = (TextView) this.findViewById(R.id.contents_text_view);
		CharSequence displayContents = resultHandler.getDisplayContents();
		contentsTextView.setText(displayContents);
		// Crudely scale betweeen 22 and 32 -- bigger font for shorter text
		int scaledSize = Math.max(22, 32 - (displayContents.length() / 4));
		contentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);

		TextView supplementTextView = (TextView) this.findViewById(R.id.contents_supplement_text_view);
		supplementTextView.setText("");
		supplementTextView.setOnClickListener(null);
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferencesActivity.KEY_SUPPLEMENTAL, true)) {
		}

		int buttonCount = resultHandler.getButtonCount();
		ViewGroup buttonView = (ViewGroup) this.findViewById(R.id.result_button_view);
		buttonView.requestFocus();
		for (int x = 0; x < ResultHandler.MAX_BUTTON_COUNT; x++) {
			TextView button = (TextView) buttonView.getChildAt(x);
			if (x < buttonCount) {
				button.setVisibility(View.VISIBLE);
				button.setText(resultHandler.getButtonText(x));
				button.setOnClickListener(new ResultButtonListener(resultHandler, x));
			} else {
				button.setVisibility(View.GONE);
			}
		}
	}

	// Briefly show the contents of the barcode, then handle the result outside
	// Barcode Scanner.
	private void handleDecodeExternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {

		if (barcode != null) {
			this.viewfinderView.drawResultBitmap(barcode);
		}

		long resultDurationMS;
		if (this.getIntent() == null) {
			resultDurationMS = DEFAULT_INTENT_RESULT_DURATION_MS;
		} else {
			resultDurationMS = this.getIntent().getLongExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS, DEFAULT_INTENT_RESULT_DURATION_MS);
		}

		// Since this message will only be shown for a second, just tell the
		// user what kind of
		// barcode was found (e.g. contact info) rather than the full contents,
		// which they won't
		// have time to read.
		if (resultDurationMS > 0) {
			this.statusView.setText(this.getString(resultHandler.getDisplayTitle()));
		}

		if (this.source == IntentSource.NATIVE_APP_INTENT) {

			// Hand back whatever action they requested - this can be changed to
			// Intents.Scan.ACTION when
			// the deprecated intent is retired.
			Intent intent = new Intent(this.getIntent().getAction());
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.putExtra(Intents.Scan.RESULT, rawResult.toString());
			intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.getBarcodeFormat().toString());
			byte[] rawBytes = rawResult.getRawBytes();
			if ((rawBytes != null) && (rawBytes.length > 0)) {
				intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes);
			}
			Map<ResultMetadataType, ?> metadata = rawResult.getResultMetadata();
			if (metadata != null) {
				if (metadata.containsKey(ResultMetadataType.UPC_EAN_EXTENSION)) {
					intent.putExtra(Intents.Scan.RESULT_UPC_EAN_EXTENSION, metadata.get(ResultMetadataType.UPC_EAN_EXTENSION).toString());
				}
				Integer orientation = (Integer) metadata.get(ResultMetadataType.ORIENTATION);
				if (orientation != null) {
					intent.putExtra(Intents.Scan.RESULT_ORIENTATION, orientation.intValue());
				}
				String ecLevel = (String) metadata.get(ResultMetadataType.ERROR_CORRECTION_LEVEL);
				if (ecLevel != null) {
					intent.putExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL, ecLevel);
				}
				@SuppressWarnings("unchecked")
				Iterable<byte[]> byteSegments = (Iterable<byte[]>) metadata.get(ResultMetadataType.BYTE_SEGMENTS);
				if (byteSegments != null) {
					int i = 0;
					for (byte[] byteSegment : byteSegments) {
						intent.putExtra(Intents.Scan.RESULT_BYTE_SEGMENTS_PREFIX + i, byteSegment);
						i++;
					}
				}
			}
			this.sendReplyMessage(R.id.return_scan_result, intent, resultDurationMS);

		} else if (this.source == IntentSource.PRODUCT_SEARCH_LINK) {

			// Reformulate the URL which triggered us into a query, so that the
			// request goes to the same
			// TLD as the scan URL.
			int end = this.sourceUrl.lastIndexOf("/scan");
			String replyURL = this.sourceUrl.substring(0, end) + "?q=" + resultHandler.getDisplayContents() + "&source=zxing";
			this.sendReplyMessage(R.id.launch_product_query, replyURL, resultDurationMS);

		} else if (this.source == IntentSource.ZXING_LINK) {

			// Replace each occurrence of RETURN_CODE_PLACEHOLDER in the
			// returnUrlTemplate
			// with the scanned code. This allows both queries and REST-style
			// URLs to work.
			if (this.returnUrlTemplate != null) {
				CharSequence codeReplacement = this.returnRaw ? rawResult.getText() : resultHandler.getDisplayContents();
				try {
					codeReplacement = URLEncoder.encode(codeReplacement.toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// can't happen; UTF-8 is always supported. Continue, I
					// guess, without encoding
				}
				String replyURL = this.returnUrlTemplate.replace(RETURN_CODE_PLACEHOLDER, codeReplacement);
				this.sendReplyMessage(R.id.launch_product_query, replyURL, resultDurationMS);
			}

		}
	}

	private void sendReplyMessage(int id, Object arg, long delayMS) {
		Message message = Message.obtain(this.handler, id, arg);
		if (delayMS > 0L) {
			this.handler.sendMessageDelayed(message, delayMS);
		} else {
			this.handler.sendMessage(message);
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (this.cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			this.cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (this.handler == null) {
				this.handler = new CaptureActivityHandler(this, this.decodeFormats, this.characterSet, this.cameraManager);
			}
			this.decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			this.displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			this.displayFrameworkBugMessageAndExit();
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.getString(R.string.app_name));
		builder.setMessage(this.getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (this.handler != null) {
			this.handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		this.resetStatusView();
	}

	private void resetStatusView() {
		this.resultView.setVisibility(View.GONE);
		this.statusView.setText(R.string.msg_default_status);
		this.statusView.setVisibility(View.VISIBLE);
		this.viewfinderView.setVisibility(View.VISIBLE);
		this.lastResult = null;
	}

	public void drawViewfinder() {
		this.viewfinderView.drawViewfinder();
	}
}
