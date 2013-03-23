package com.nexelem.boxplorer.activity;

import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.nexelem.boxplorer.R;

/**
 * Klasa odpowiedzialna za zapisywanie tagow NFC Nasluchuje w tle czy do
 * urzadzenia zostal przylozony tag NFC jesli tag zostal wykryty automatycznie
 * zostaje wpisana na niego wiadomosc
 * 
 * @author darek zon
 * 
 */
public class NfcWriter extends Activity {

	private static final String TAG = NfcWriter.class.getName();

	public static final int ACTIVITY_WRITE_NFC = 6444;
	public static final String NFC_MIME_TYPE = "application/com.nexelem.boxplorer";

	private NfcAdapter adapter;
	private boolean writingMode = false;
	private String uuid = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.uuid = this.getIntent().getStringExtra(Intent.EXTRA_UID);
		this.adapter = NfcAdapter.getDefaultAdapter(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (this.writingMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			byte[] text = this.uuid.getBytes(Charset.forName("US-ASCII"));

			NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, NfcWriter.NFC_MIME_TYPE.getBytes(Charset.forName("US-ASCII")), new byte[0], text);
			NdefMessage msg = new NdefMessage(new NdefRecord[] { record });

			new WriteTask(this, msg, tag).execute();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!this.writingMode) {
			IntentFilter discovery = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
			IntentFilter[] tagFilters = new IntentFilter[] { discovery };
			Intent intent = new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent pending = PendingIntent.getActivity(this, 0, intent, 0);
			this.writingMode = true;
			this.adapter.enableForegroundDispatch(this, pending, tagFilters, null);
		}
	}

	@Override
	public void onPause() {
		if (this.isFinishing()) {
			this.adapter.disableForegroundDispatch(this);
			this.writingMode = false;
		}
		super.onPause();
	}

	/**
	 * Metoda ustawia wartosci zwracane przez aktywnosc
	 * 
	 * @param status
	 * @param message
	 */
	public void sendReturnMessage(int status, int message) {
		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_TEXT, message);
		if (this.getParent() == null) {
			this.setResult(status, intent);
		} else {
			this.getParent().setResult(status, intent);
		}
		this.finish();
	}

	static class WriteTask extends AsyncTask<Void, Void, Void> {
		NfcWriter host = null;
		NdefMessage msg = null;
		Tag tag = null;
		int returnText;
		int returnStatus = Activity.RESULT_CANCELED;

		WriteTask(NfcWriter host, NdefMessage msg, Tag tag) {
			this.host = host;
			this.msg = msg;
			this.tag = tag;
		}

		/**
		 * Metoda zapisujaca w tle wiadomosc do tagu NFC Przed sprawdzeniem
		 * formatuje tag
		 */
		@Override
		protected Void doInBackground(Void... nop) {
			int size = this.msg.toByteArray().length;
			try {
				Ndef ndef = Ndef.get(this.tag);
				if (ndef == null) {
					NdefFormatable formatable = NdefFormatable.get(this.tag);

					if (formatable != null) {
						try {
							formatable.connect();
							try {
								formatable.format(this.msg);
							} catch (Exception e) {
								this.returnText = R.string.nfc_tag_refused_to_format;
							}
						} catch (Exception e) {
							this.returnText = R.string.nfc_tag_refused_to_connect;
						} finally {
							formatable.close();
						}
					} else {
						this.returnText = R.string.nfc_tag_does_not_support_ndef;
					}
				} else {
					ndef.connect();

					try {
						if (!ndef.isWritable()) {
							this.returnText = R.string.nfc_tag_is_read_only;
						} else if (ndef.getMaxSize() < size) {
							this.returnText = R.string.nfc_message_is_too_big;
						} else {
							ndef.writeNdefMessage(this.msg);
							this.returnText = R.string.nfc_tag_saved;
							this.returnStatus = Activity.RESULT_OK;
						}
					} catch (Exception e) {
						this.returnText = R.string.nfc_tag_refused_to_connect;
					} finally {
						ndef.close();
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "Exception when writing tag", e);
				this.returnText = R.string.nfc_general_exception;
			}

			return (null);
		}

		@Override
		protected void onPostExecute(Void unused) {
			this.host.sendReturnMessage(this.returnStatus, this.returnText);
		}
	}
}
