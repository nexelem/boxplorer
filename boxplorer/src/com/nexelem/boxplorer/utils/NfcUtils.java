package com.nexelem.boxplorer.utils;

import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.PackageManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.util.Log;

import com.nexelem.boxplorer.activity.NfcWriter;
import com.nexelem.boxplorer.db.BusinessException;

public class NfcUtils {

	private static final String TAG = NfcUtils.class.getName();

	private static final String[][] techList = new String[][] { new String[] { NfcF.class.getName(), MifareUltralight.class.getName(), NfcA.class.getName(), Ndef.class.getName() } };

	public static final boolean isNfcAvailable(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
	}

	public static final String[][] getTechList() {
		return techList;
	}

	public static final PendingIntent getPendingIntent(Activity act) {
		return PendingIntent.getActivity(act, 0, new Intent(act, act.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	}

	public static final IntentFilter[] getIntentFilters() {
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType(NfcWriter.NFC_MIME_TYPE);
			return new IntentFilter[] { ndef, };
		} catch (MalformedMimeTypeException e) {
			Log.e(NfcUtils.TAG, "Unable to generate IntentFilters", e);
		}
		return new IntentFilter[] {};
	}

	public static final String getContent(Tag tagToRead) throws BusinessException {
		Ndef tag = Ndef.get(tagToRead);
		try {
			tag.connect();
			NdefMessage ndefM = tag.getNdefMessage();
			NdefRecord[] nrec = ndefM.getRecords();
			StringBuilder sb = new StringBuilder();
			for (NdefRecord rec : nrec) {
				sb.append(new String(rec.getPayload()));
			}
			return sb.toString();
		} catch (IOException e) {
			Log.e(TAG, "Unable to connect to NFC tag", e);
			throw new BusinessException(e);
		} catch (FormatException e) {
			Log.e(TAG, "NFC tag has unknown format", e);
			throw new BusinessException(e);
		}
	}

}
