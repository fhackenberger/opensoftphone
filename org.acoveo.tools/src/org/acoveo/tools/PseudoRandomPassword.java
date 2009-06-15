package org.acoveo.tools;

import java.util.Random;

public class PseudoRandomPassword {

	public static String generatePassword(CharSequence allowedCharacters, int length) {
		StringBuffer buffer = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			buffer.append(allowedCharacters.charAt(random.nextInt(allowedCharacters.length())));
		}
		return buffer.toString();
	}

	public static String generatePassword() {
		return generatePassword("123456789abcdefghijklmnopqrstuvwxyz", 8);
	}
}
