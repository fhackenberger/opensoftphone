package org.acoveo.tools;

public class StringTools {
	public static void trimSpaces(StringBuilder stringBuilder) {
		int length = stringBuilder.length();
		if(length == 0) {
			return;
		}
		int firstNonWhitespaceIndex = 0;
		for(int index = 0; index < length; index++) {
			char ch = stringBuilder.charAt(index);
			if(ch > '\u0020') {
				firstNonWhitespaceIndex = index;
				break;
			}
		}
		if(firstNonWhitespaceIndex >= length) {
			stringBuilder.replace(0, length, "");
			return;
		}
		int lastNonWhitespaceIndex = length-1;
		for(int index = length-1; index >= firstNonWhitespaceIndex; index--) {
			char ch = stringBuilder.charAt(index);
			if(ch > '\u0020') {
				lastNonWhitespaceIndex = index;
				break;
			}
		}
		stringBuilder.replace(0, firstNonWhitespaceIndex, "");
		stringBuilder.replace(lastNonWhitespaceIndex + 1, length, "");
	}
}
