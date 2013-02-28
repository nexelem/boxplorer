package com.nexelem.boxplorer;

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

import com.nexelem.boxplorer.utils.NfcTagReader;

public class NfcReceiverActivity extends Activity {

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
			ndef.addDataType("*/*"); /*
									 * Handles all MIME based dispatches. You
									 * should specify only the ones that you
									 * need.
									 */
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		this.intentFiltersArray = new IntentFilter[] { ndef, };

		this.techListsArray = new String[][] { new String[] { NfcF.class.getName(), MifareUltralight.class.getName(), NfcA.class.getName(), Ndef.class.getName() } };

		Tag tagFromIntent = this.getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(NfcAdapter.EXTRA_TAG, tagFromIntent);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		this.startActivity(intent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		System.out.println(NfcTagReader.getUUID(tagFromIntent));
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
