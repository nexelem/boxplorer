package com.nexelem.boxplorer.utils;

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
import android.widget.Toast;

public class NfcWriterActivity extends Activity {

	private NfcAdapter adapter;
	private boolean writingMode = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this.setContentView(R.layout.main);
		this.adapter = NfcAdapter.getDefaultAdapter(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (this.writingMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			byte[] text = new String(intent.getStringExtra(Intent.EXTRA_UID)).getBytes(Charset.forName("US-ASCII"));

			NdefRecord record = new NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, "application/com.nexelem.boxplorer".getBytes(Charset.forName("US-ASCII")), new byte[0], text);
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

	static class WriteTask extends AsyncTask<Void, Void, Void> {
		Activity host = null;
		NdefMessage msg = null;
		Tag tag = null;
		String text = null;

		WriteTask(Activity host, NdefMessage msg, Tag tag) {
			this.host = host;
			this.msg = msg;
			this.tag = tag;
		}

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
								this.text = "Tag refused to format";
							}
						} catch (Exception e) {
							this.text = "Tag refused to connect";
						} finally {
							formatable.close();
						}
					} else {
						this.text = "Tag does not support NDEF";
					}
				} else {
					ndef.connect();

					try {
						if (!ndef.isWritable()) {
							this.text = "Tag is read-only";
						} else if (ndef.getMaxSize() < size) {
							this.text = "Message is too big for tag";
						} else {
							ndef.writeNdefMessage(this.msg);
						}
					} catch (Exception e) {
						this.text = "Tag refused to connect";
					} finally {
						ndef.close();
					}
				}
			} catch (Exception e) {
				Log.e("URLTagger", "Exception when writing tag", e);
				this.text = "General exception: " + e.getMessage();
			}

			return (null);
		}

		@Override
		protected void onPostExecute(Void unused) {
			if (this.text != null) {
				Toast.makeText(this.host, this.text, Toast.LENGTH_SHORT).show();
			}
			this.host.finish();
		}
	}
}
