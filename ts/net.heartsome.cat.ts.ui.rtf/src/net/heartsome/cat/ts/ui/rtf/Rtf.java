package net.heartsome.cat.ts.ui.rtf;

import java.io.IOException;

/**
 * 如果向 RTF 中写入的内容包含中文字符，要调用此类的 asRtf 方法
 * 
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class Rtf {
//	private final static Charset CHARSET_GBK = Charset.forName("GBK");

	/**
	 * Private constructor. The user will not instantiate this class.
	 */
	private Rtf() {
	}

	/**
	 * Converts a given char sequence into RTF format and stream it to the
	 * {@code Appendable}.
	 * 
	 * @param out
	 * @param rawText
	 * @throws IOException
	 */
	private static void asRtf(Appendable out, String rawText)
			throws IOException {
//		byte[] bytes = rawText.getBytes(CHARSET_GBK);
//
//		assert rawText.length() == bytes.length;
//
//		for (int i = 0; i < bytes.length; i++) {
//			char b = (char) (bytes[i] & 0xFF);
//
//			if (b == '\n')
//				out.append("\\par\n");
//			else if (b == '\t')
//				out.append("\\tab\n");
//			else if (b == '\\')
//				out.append("\\\\");
//			else if (b == '{')
//				out.append("\\{");
//			else if (b == '}')
//				out.append("\\}");
//			else if (b < 127)
//				out.append(b);
//			else if (b < 0xff) // Use encoding 'xx if value 0x7f < char < 0xff
//				out.append("\\'" + Integer.toHexString(b));
//			else
//				// Use Unicode and ask the char from the String object
//				out.append("\\u").append(Integer.toString(rawText.charAt(i))).append('?');
//		}
		
		int length = rawText.length();
		for (int i = 0; i < length; i++) {
			char c = rawText.charAt(i);
			if (c == '\n') {
				out.append("\\par\n");
			} else if (c == '\t') {
				out.append("\\tab\n");
			} else if (c <= '\u007F') {
				out.append(c);
			} else if (c == '\uE007') {
				out.append("}"); //$NON-NLS-1$
			} else if (c == '\uE008') {
				out.append("{"); //$NON-NLS-1$
			} else if (c == '\uE011') {
				out.append("\\\\"); //$NON-NLS-1$
			} else {
				// Forget about conversion rules that use \' because there
				// can
				// be encoding issues, specially when handling Mac text.
				// Do the OpenOffice trick for all extended characters
				// instead.
				out.append("\\uc0\\u" + (int) c + " "); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * Converts a given string to an encoded RTF sting. A new line character
	 * will be converted to \par.
	 * 
	 * @param rawText
	 *            Raw text.
	 * @return RTF encoded string.
	 */
	public static String asRtf(String rawText) {
		if (rawText == null)
			return null;

		StringBuilder result = new StringBuilder(rawText.length() * 2);
		try {
			asRtf(result, rawText);
		} catch (IOException e) {
			// If this will happen we are really in trouble
			throw new RtfException(e);
		}
		return result.toString();
	}
	
	/**
	 * 替换 string 中的不可见字符，注意：string 为16进制字符串</br>
	 * 将来如果发现新的不可见字符，需修改此方法
	 * @param string
	 * @return
	 */
	public static String replaceInvisibleChar(String string) {
//		将16进制字符C2A0替换为20（空格），因 C2A0 看起来像一个空格
		string = string.toUpperCase().replaceAll("C2A0", "20");
		return string;
	}
}
