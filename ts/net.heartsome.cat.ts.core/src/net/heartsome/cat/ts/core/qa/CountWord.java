package net.heartsome.cat.ts.core.qa;

import java.util.StringTokenizer;

/**
 * 字数统计类
 * @author robert	2011-12-10
 */
public class CountWord {
	public CountWord(){
		
		
	}
	
	public static int wordCount(String str, String lang) {
		if (lang.toLowerCase().startsWith("zh")) { //$NON-NLS-1$
			return chineseCount(str);
		}
		return europeanCount(str);
	}
	
	private static int chineseCount(String str) {
		// basic idea is that we need to remove unicode that higher than 255
		// and then we count by europeanCount
		// after that remove 0-255 unicode value and just count character
		StringBuffer european = new StringBuffer();
		int chineseCount = 0;
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char chr = chars[i];
			if (chr <= 255 || chr == '\u00A0' || chr == '\u3001' || chr == '\u3002' || chr == '\uff1a'
					|| chr == '\uff01' || chr == '\uff1f' || chr == '\u4ecb') {
				european.append(chr);
			} else {
				chineseCount++;
			}
		}
		int euroCount = europeanCount(european.toString());
		return euroCount + chineseCount;
	}
	
	private static int europeanCount(String source) {
		int wordnum = 0;
		StringTokenizer tok = new StringTokenizer(source, " \t\r\n()?\u00A0\u3001\u3002\uff1a\uff01\uff1f\u4ecb"); //$NON-NLS-1$
		String charsInNumber = ".,-/<>"; //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			String str = tok.nextToken();
			if (charsInNumber.indexOf(str) < 0 && !isFormatNumber(str)) {
				StringTokenizer tok2 = new StringTokenizer(str, charsInNumber);
				while (tok2.hasMoreTokens()) {
					str = tok2.nextToken();
					wordnum++;
				}
			}
		}

		return wordnum;
	}
	
	
	public static boolean isFormatNumber(String str) {
		char[] chars = str.toCharArray();
		boolean hasDigit = false;
		for (int i = 0; i < chars.length; i++) {
			if (Character.isDigit(chars[i])) {
				hasDigit = true;
			} else if (chars[i] != '/' && chars[i] != '.' && chars[i] != ',' && chars[i] != '-' && chars[i] != '>'
					&& chars[i] != '<') {
				return false;
			}
		}
		return hasDigit;
	}
	
}
