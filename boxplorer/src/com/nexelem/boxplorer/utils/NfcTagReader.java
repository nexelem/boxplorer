package com.nexelem.boxplorer.utils;

import java.io.IOException;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

public class NfcTagReader {

	public static String getUUID(Tag tagToRead) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
