/**
 * TmxEditorUtils.java
 *
 * Version information :
 *
 * Date:2013/5/17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */

package net.heartsome.cat.te.tmxeditor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.te.core.bean.TmxTemplet;

public class TmxEditorUtils {
	private static String creationTool = "Heartsome TM Server";
	
	private static String creationToolVersion = "2.0-1";
	
	static{
		creationTool =TmxTemplet.getProductName();
		creationToolVersion=TmxTemplet.getProductVersion();
	}
	
	public TmxEditorUtils() {
		
	}


	public static TmxTU createTmxTu(String srcLang, String tgtLang) {
		TmxTU tu = new TmxTU();
		tu.setCreationUser(System.getProperty("user.name"));
		tu.setCreationDate(getTmxCreationDate());
		tu.setCreationTool(creationTool);
		tu.setCreationToolVersion(creationToolVersion);

		TmxSegement srcSeg = new TmxSegement("", srcLang);
		srcSeg.setCreationDate(getTmxCreationDate());
		srcSeg.setCreationTool(creationTool);
		srcSeg.setCreationUser(System.getProperty("user.name"));
		tu.setSource(srcSeg);

		TmxSegement tgtSeg = new TmxSegement("", tgtLang);
		tgtSeg.setCreationDate(getTmxCreationDate());
		tgtSeg.setCreationTool(creationTool);
		tgtSeg.setCreationUser(System.getProperty("user.name"));
		tu.setTarget(tgtSeg);

		return tu;
	}

	/**
	 * 获取 tmx 创建时间 robert 2013-06-08
	 * @return ;
	 */
	public static String getTmxCreationDate() {

		Calendar calendar = Calendar.getInstance(Locale.US);
		String sec = (calendar.get(Calendar.SECOND) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.SECOND);
		String min = (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.MINUTE);
		String hour = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.HOUR_OF_DAY);
		String mday = (calendar.get(Calendar.DATE) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ calendar.get(Calendar.DATE);
		String mon = (calendar.get(Calendar.MONTH) < 9 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ (calendar.get(Calendar.MONTH) + 1);
		String longyear = "" + calendar.get(Calendar.YEAR); //$NON-NLS-1$

		String date = longyear + mon + mday + "T" + hour + min + sec + "Z"; //$NON-NLS-1$ //$NON-NLS-2$
		return date;
	}

	/**
	 * 将TU节点的属性生成一个字符串，用于显示在编辑器的属性列上。
	 * @param tu
	 *            <code>TmxTU</code>>
	 * @return ;
	 */
	public static String tuAttr2Str4UI(TmxTU tu) {
		StringBuilder sb = new StringBuilder();
		// String creationid = tu.getCreationUser();
		// if(creationid != null && creationid.length() > 0){
		// sb.append("creationid: ").append(creationid);
		// }
		// String creationdate = tu.getCreationDate();
		// if(creationdate != null && creationdate.length()>0){
		// sb.append(" creationdate: ").append(DateUtils.formatDateFromUTC(creationdate));
		// }
		String changedate = tu.getChangeDate();
		if (changedate != null && changedate.length() > 0) {
			sb.append("changedate: ").append(DateUtils.formatDateFromUTC(changedate));
		}

		String changeid = tu.getChangeUser();
		if (changeid != null && changeid.length() > 0) {
			sb.append(" changeid: ").append(changeid);
		}
		return sb.toString();
	}
	
	
	public static List<StyleRange> calculateSearchStringStyleRange(char[] source, char[] target, TextStyle style) {
		int sourceOffset = 0;
		int sourceCount = source.length;
		int targetOffset = 0, targetCount = target.length;

		char first = target[targetOffset];
		int max = sourceOffset + (sourceCount - targetCount);
		List<StyleRange> rangeList = new ArrayList<StyleRange>();
		for (int i = sourceOffset; i <= max; i++) {
			/* Look for first character. */
			if (source[i] != first) {
				while (++i <= max && source[i] != first)
					;
			}

			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				List<StyleRange> tempList = new ArrayList<StyleRange>();
				int start = i;
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end; j++, k++) {
					Matcher matcher = PlaceHolderEditModeBuilder.PATTERN.matcher(source[j] + "");
					if (matcher.matches()) {
						StyleRange range = new StyleRange(style);
						range.start = start;
						range.length = j - start;
						start = j + 1;
						k--;
						end++;
						if (end > sourceCount) {
							break;
						}
						tempList.add(range);
						continue;
					}
					if (source[j] != target[k]) {
						break;
					}
				}

				if (j == end) {
					/* Found whole string. */
					StyleRange range = new StyleRange(style);
					range.start = start;
					range.length = j - start;
					rangeList.addAll(tempList);
					rangeList.add(range);
				}
			}
		}
		return rangeList;
	}
}
