package net.heartsome.cat.ts.ui.rtf.innertag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.ts.ui.rtf.PlaceHolderRTFModeBuilder;

/**
 * 内部标记工具类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class InnerTagUtil {

	private static PlaceHolderRTFModeBuilder placeHolderCreater = new PlaceHolderRTFModeBuilder();

	private InnerTagUtil() {
	}

	/**
	 * 将带内部标记的文本由XML格式转换为显示格式的文本
	 * @param originalValue
	 *            原始的带内部标记的XML格式的文本
	 * @return ;
	 */
	public static TreeMap<String, InnerTagBean> parseXmlToDisplayValue(StringBuffer originalValue) {
		// 得到标签映射map（key: 内部标记；value: 内部标记实体）
		TreeMap<String, InnerTagBean> tags = new TreeMap<String, InnerTagBean>(new Comparator<String>() {
			public int compare(String str1, String str2) {
				int num1 = InnerTagUtil.getStyledTagNum(str1);
				int num2 = InnerTagUtil.getStyledTagNum(str2);
				if (num1 == num2) {
					return str1.indexOf(String.valueOf(num1)) - str2.indexOf(String.valueOf(num1));
				}
				return num1 - num2;
			}
		});
		if (originalValue == null || originalValue.length() == 0) {
			return tags;
		}

		RTFInnerTagFactory innerTagFactory = new RTFInnerTagFactory(originalValue.toString(), placeHolderCreater);
		originalValue.replace(0, originalValue.length(), innerTagFactory.getText()); // 提取标记之后的文本。

		List<InnerTagBean> innerTagBeans = innerTagFactory.getInnerTagBeans();

		if (innerTagBeans != null && innerTagBeans.size() > 0) {
			for (int i = 0; i < innerTagBeans.size(); i++) {
				String placeHolder = placeHolderCreater.getPlaceHolder(innerTagBeans, i);
				tags.put(placeHolder, innerTagBeans.get(i));
			}
		}

		return tags;
	}
	
	/**
	 * 将带内部标记的文本由XML格式转换为显示格式的文本
	 * @param originalValue
	 *            原始的带内部标记的XML格式的文本
	 * @return ;
	 */
	public static ArrayList<SegmentText> parseXml(StringBuffer originalValue) {
		if (originalValue == null || originalValue.length() == 0) {
			return null;
		}

		RTFInnerTagFactory innerTagFactory = new RTFInnerTagFactory(originalValue.toString(), placeHolderCreater);
		originalValue.replace(0, originalValue.length(), innerTagFactory.getText()); // 提取标记之后的文本。

		return innerTagFactory.getLstSegment();
	}

	/**
	 * 得到内部标记索引号。
	 * @param innerTag
	 *            内部标记
	 * @return ;
	 */
	public static int getStyledTagNum(String innerTag) {
		int res = 0;
		for (int i = 0; i < innerTag.length(); i++) {
			char ch = innerTag.charAt(i);
			if (Character.isDigit(ch)) {
				res = res * 10 + Integer.parseInt(String.valueOf(ch));
			} else {
				if (res > 0) {
					return res;
				}
			}
		}
		return -1;
	}
}
