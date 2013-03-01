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
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.nexelem.boxplorer.R;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

	private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192, 128, 64 };
	private static final long ANIMATION_DELAY = 80L;
	private static final int CURRENT_POINT_OPACITY = 0xA0;
	private static final int MAX_RESULT_POINTS = 20;
	private static final int POINT_SIZE = 6;

	private CameraManager cameraManager;
	private final Paint paint;
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;
	private final int laserColor;
	private final int resultPointColor;
	private int scannerAlpha;
	private List<ResultPoint> possibleResultPoints;
	private List<ResultPoint> lastPossibleResultPoints;

	// This constructor is used when the class is built from an XML resource.
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Initialize these once for performance rather than calling them every
		// time in onDraw().
		this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		Resources resources = this.getResources();
		this.maskColor = resources.getColor(R.color.viewfinder_mask);
		this.resultColor = resources.getColor(R.color.result_view);
		this.laserColor = resources.getColor(R.color.viewfinder_laser);
		this.resultPointColor = resources.getColor(R.color.possible_result_points);
		this.scannerAlpha = 0;
		this.possibleResultPoints = new ArrayList<ResultPoint>(5);
		this.lastPossibleResultPoints = null;
	}

	public void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		if (this.cameraManager == null) {
			return; // not ready yet, early draw before done configuring
		}
		Rect frame = this.cameraManager.getFramingRect();
		if (frame == null) {
			return;
		}
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// Draw the exterior (i.e. outside the framing rect) darkened
		this.paint.setColor(this.resultBitmap != null ? this.resultColor : this.maskColor);
		canvas.drawRect(0, 0, width, frame.top, this.paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, this.paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, this.paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, this.paint);

		if (this.resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			this.paint.setAlpha(CURRENT_POINT_OPACITY);
			canvas.drawBitmap(this.resultBitmap, null, frame, this.paint);
		} else {

			// Draw a red "laser scanner" line through the middle to show
			// decoding is active
			this.paint.setColor(this.laserColor);
			this.paint.setAlpha(SCANNER_ALPHA[this.scannerAlpha]);
			this.scannerAlpha = (this.scannerAlpha + 1) % SCANNER_ALPHA.length;
			int middle = (frame.height() / 2) + frame.top;
			canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, this.paint);

			Rect previewFrame = this.cameraManager.getFramingRectInPreview();
			float scaleX = frame.width() / (float) previewFrame.width();
			float scaleY = frame.height() / (float) previewFrame.height();

			List<ResultPoint> currentPossible = this.possibleResultPoints;
			List<ResultPoint> currentLast = this.lastPossibleResultPoints;
			int frameLeft = frame.left;
			int frameTop = frame.top;
			if (currentPossible.isEmpty()) {
				this.lastPossibleResultPoints = null;
			} else {
				this.possibleResultPoints = new ArrayList<ResultPoint>(5);
				this.lastPossibleResultPoints = currentPossible;
				this.paint.setAlpha(CURRENT_POINT_OPACITY);
				this.paint.setColor(this.resultPointColor);
				synchronized (currentPossible) {
					for (ResultPoint point : currentPossible) {
						canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX), frameTop + (int) (point.getY() * scaleY), POINT_SIZE, this.paint);
					}
				}
			}
			if (currentLast != null) {
				this.paint.setAlpha(CURRENT_POINT_OPACITY / 2);
				this.paint.setColor(this.resultPointColor);
				synchronized (currentLast) {
					float radius = POINT_SIZE / 2.0f;
					for (ResultPoint point : currentLast) {
						canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX), frameTop + (int) (point.getY() * scaleY), radius, this.paint);
					}
				}
			}

			// Request another update at the animation interval, but only
			// repaint the laser line,
			// not the entire viewfinder mask.
			this.postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE, frame.top - POINT_SIZE, frame.right + POINT_SIZE, frame.bottom + POINT_SIZE);
		}
	}

	public void drawViewfinder() {
		Bitmap resultBitmap = this.resultBitmap;
		this.resultBitmap = null;
		if (resultBitmap != null) {
			resultBitmap.recycle();
		}
		this.invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		this.resultBitmap = barcode;
		this.invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		List<ResultPoint> points = this.possibleResultPoints;
		synchronized (points) {
			points.add(point);
			int size = points.size();
			if (size > MAX_RESULT_POINTS) {
				// trim it
				points.subList(0, size - (MAX_RESULT_POINTS / 2)).clear();
			}
		}
	}

}
