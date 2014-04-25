package net.heartsome.cat.ts.ui.rtf;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import net.sourceforge.rtf.format.rtfcode.IRTFCode;

/**
 * 对 RTFCodeString 格式化的类（屏蔽 DefaultRTFCodeStringFormat 对 RTFCodeString 的格式化）
 * @author peason
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("serial")
public class RTFCodeStringFormat extends Format {

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (obj == null) {
			return null;
		}
		String content = null;
		if (obj instanceof IRTFCode) {
			content = ((IRTFCode) obj).getContent();
		} else if (obj instanceof String) {
			content = (String) obj;
		}
		if (content != null) {
			return new StringBuffer(content);
		} else {
			return null;
		}
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		// TODO Auto-generated method stub
		return null;
	}

}
