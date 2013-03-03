package com.nexelem.boxplorer.activity;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;

import com.nexelem.boxplorer.utils.NfcUtils;

/**
 * Aktywnosc wywolywana (przez dispatcher Androida) gdy odczytujemy tag NFC ale
 * aplikacja nie jest uruchomiona
 * 
 * @author darek zon
 */
public class NfcReceiver extends Activity {

	private NfcAdapter adapter;

	/**
	 * Gdy aktywnosc jest uruchomiona odczytujemy tag NFC i przekazujemy go do
	 * aktywnosci glownej w celu wyszukania elementu
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (NfcUtils.isNfcAvailable(this.getApplicationContext())) {
			this.adapter = NfcAdapter.getDefaultAdapter(this.getApplicationContext());
		}
		Tag tagFromIntent = this.getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Intent intent = new Intent(this, Main.class);
		intent.putExtra(NfcAdapter.EXTRA_TAG, tagFromIntent);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		this.startActivity(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (NfcUtils.isNfcAvailable(this.getApplicationContext())) {
			this.adapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (NfcUtils.isNfcAvailable(this.getApplicationContext())) {
			this.adapter.enableForegroundDispatch(this, NfcUtils.getPendingIntent(this), NfcUtils.getIntentFilters(), NfcUtils.getTechList());
		}
	}

}
