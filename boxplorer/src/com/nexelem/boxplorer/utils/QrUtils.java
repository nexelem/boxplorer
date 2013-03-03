package com.nexelem.boxplorer.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.encode.QRCodeEncoder;
import com.nexelem.boxplorer.db.BusinessException;

/**
 * Klasa pomocnicza do obslugi kodow QR
 * 
 * @author darek zon
 * 
 */
public class QrUtils {

	private static final String TAG = QrUtils.class.getName();

	public static Bitmap generateQRCode(Activity activity, String content) throws BusinessException {
		try {
			Intent intent = new Intent(Intents.Encode.ACTION);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
			intent.putExtra(Intents.Encode.DATA, content);
			intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
			QRCodeEncoder qrCodeEncoder = null;
			qrCodeEncoder = new QRCodeEncoder(activity, intent, 200, false);
			return qrCodeEncoder.encodeAsBitmap();

		} catch (WriterException e) {
			Log.e(QrUtils.TAG, "Application was unable to generate Bitmap for QRCode", e);
			throw new BusinessException(e, "Unable to generate Bitmap");
		}
	}

}
