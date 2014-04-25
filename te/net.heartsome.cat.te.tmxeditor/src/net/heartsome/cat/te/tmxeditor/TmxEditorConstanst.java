/**
 * TmxEditorConstanst.java
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

import java.util.regex.Pattern;

public interface TmxEditorConstanst {
	int SEGMENT_LINE_SPACING = 1;
	int SEGMENT_TOP_MARGIN = 2;
	int SEGMENT_BOTTOM_MARGIN = 2;
	int SEGMENT_RIGHT_MARGIN = 2;
	int SEGMENT_LEFT_MARGIN = 2;
	
	public static final char TAB_CHARACTER = '\u2192';
	public static final char LINE_SEPARATOR_CHARACTER= '\u21B2';
	public static final char SPACE_CHARACTER = '\u2219';	
	public static final Pattern NONPRINTING_PATTERN = Pattern.compile("[\u2192\u21B2\u2219]+");
	
	public static final String TMX_EDITOR_SHOWHIDEN_NONPRINTCHARACTER = "net.heartsome.cat.te.tmxeditor.nonPrinttingCharacter";
}
