package com.nexelem.boxplorer.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.os.Bundle;


public class NfcReceiver extends Activity {

	private IntentFilter[] intentFiltersArray;
	private String[][] techListsArray;
	private PendingIntent pendingIntent;
	private NfcAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.adapter = NfcAdapter.getDefaultAdapter(this);
		this.pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndef.addDataType(NfcWriter.NFC_MIME_TYPE);
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		this.intentFiltersArray = new IntentFilter[] { ndef, };

		this.techListsArray = new String[][] { new String[] { NfcF.class.getName(), MifareUltralight.class.getName(), NfcA.class.getName(), Ndef.class.getName() } };

		Tag tagFromIntent = this.getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Intent intent = new Intent(this, Main.class);
		intent.putExtra(NfcAdapter.EXTRA_TAG, tagFromIntent);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		this.startActivity(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.adapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.adapter.enableForegroundDispatch(this, this.pendingIntent, this.intentFiltersArray, this.techListsArray);

	}

}
