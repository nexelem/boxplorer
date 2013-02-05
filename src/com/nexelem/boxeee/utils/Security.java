package com.nexelem.boxeee.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Security {

	public static String generateSha256(String msg) throws Exception {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");

		} catch (NoSuchAlgorithmException ex) {
			throw new Exception("Unable to generate SHA-256 code");
		}
		String msgToDigest = msg + System.currentTimeMillis();
		return String.valueOf(digest.digest(msgToDigest.getBytes()));
	}
}
