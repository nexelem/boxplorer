package com.nexelem.boxplorer.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.encode.QRCodeEncoder;

public class QRCodeUtils {
	
	public static Bitmap generateQRCode(Activity activity, String content){
		try {
			Intent intent = new Intent(Intents.Encode.ACTION);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
			intent.putExtra(Intents.Encode.DATA, content);
			intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
			QRCodeEncoder qrCodeEncoder = null;
			qrCodeEncoder = new QRCodeEncoder(activity, intent, 200,false);
			return qrCodeEncoder.encodeAsBitmap();
		
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

}
