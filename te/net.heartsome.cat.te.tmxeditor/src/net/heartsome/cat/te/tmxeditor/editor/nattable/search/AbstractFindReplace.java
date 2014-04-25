/**
 * AbstractFindReplace.java
 *
 * Version information :
 *
 * Date:2013-8-16
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.editor.nattable.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess;
import net.heartsome.cat.te.tmxeditor.TmxEditorConstanst;
import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable;

/**
 * Find , Find content in TmxDataAccess, and then display result in NatTable
 * @author Jason
 * @version
 * @since JDK1.6
 */
public abstract class AbstractFindReplace {

	protected boolean searchForward = true;

	protected boolean caseSensitive;

	protected boolean wholeWord;

	protected boolean regExSearch;

	public AbstractFindReplace(AbstractTmxDataAccess dataAccess) {

	}

	public void setSearchStategy(boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
		this.searchForward = searchForward;
		this.caseSensitive = caseSensitive;
		this.wholeWord = wholeWord;
		this.regExSearch = regExSearch;
	}

	/**
	 * do find
	 * @param startRow
	 *            Start row to find
	 * @param offset
	 *            Start find offset In start row
	 * @param lang
	 *            Language to find, correspond to column in NatTable
	 * @param findStr
	 *            Content to find
	 * @return Find result;
	 */
	public abstract FindReasult find(int startRow, int offset, String lang, String findStr);

	/**
	 * @param offset
	 * @param text
	 *            Full Text
	 * @param findStr
	 * @return ;
	 */
	protected Region matchString(int offset, String text, String findStr) {
		if (TmxEditorImpWithNattable.showNonPrinttingChar) {
			findStr = findStr.replaceAll("\\n", TmxEditorConstanst.LINE_SEPARATOR_CHARACTER + "\n");
			findStr = findStr.replaceAll("\\t", TmxEditorConstanst.TAB_CHARACTER + "\u200B");
			findStr = findStr.replaceAll(" ", TmxEditorConstanst.SPACE_CHARACTER + "\u200B");
			text = text.replaceAll("\\n", TmxEditorConstanst.LINE_SEPARATOR_CHARACTER + "\n");
			text = text.replaceAll("\\t", TmxEditorConstanst.TAB_CHARACTER + "\u200B");
			text = text.replaceAll(" ", TmxEditorConstanst.SPACE_CHARACTER + "\u200B");
		}
		List<Integer> tagPositions = new ArrayList<Integer>();
		Matcher matcher = PlaceHolderEditModeBuilder.PATTERN.matcher(text);
		int tempOffset = offset;
		while (matcher.find()) {
			int start = matcher.start();
			if (offset != -1 && start < tempOffset) {
				offset--;
			}
			tagPositions.add(start - tagPositions.size());
		}
		text = matcher.replaceAll("");
		if (offset != -1 && searchForward) {
			if (offset >= text.length() || offset + findStr.length() > text.length()) {
				return null;
			}
		} else if (offset != -1 && !searchForward) {
			if (offset - findStr.length() < 1) {
				return null;
			}
			offset = offset == text.length() ? offset - 1 : offset;
		}
		Document doc = new Document(text);
		FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(doc);
		try {
			IRegion region = adapter.find(offset, findStr, searchForward, caseSensitive, wholeWord, regExSearch);
			if (region != null) {
				int s = region.getOffset();
				int e = s + region.getLength();
				int ns = s;
				int ne = e;
				for (int tagp : tagPositions) {
					if (s >= tagp) {
						ns += 1;
					}
					if (e > tagp) {
						ne += 1;
					}
					if (tagp >= e) {
						break;
					}
				}
				return new Region(ns, ne - ns);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return null;
	}
}
