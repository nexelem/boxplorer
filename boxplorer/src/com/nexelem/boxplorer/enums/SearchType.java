package com.nexelem.boxplorer.enums;

public enum SearchType {
	TEXT(""), QR(":qr"), VOICE(":voice"), NFC(":nfc");

	private String searchTag;

	SearchType(String tag) {
		this.searchTag = tag;
	}

	public String getSearchTag() {
		return this.searchTag;
	}
}
