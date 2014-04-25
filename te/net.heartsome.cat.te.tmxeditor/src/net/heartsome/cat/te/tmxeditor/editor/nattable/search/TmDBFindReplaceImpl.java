/**
 * TmDBFindReplaceImpl.java
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

import java.util.List;

import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.te.core.tmxdata.DatabaseDataAccess;

import org.eclipse.jface.text.Region;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmDBFindReplaceImpl extends AbstractFindReplace {
	private DatabaseDataAccess dataAccess;

	// private FindData lastFindData;
	// private int lastStartRow = -1;
	// private List<Integer> lastTuPks;

	public TmDBFindReplaceImpl(DatabaseDataAccess dataAccess) {
		super(dataAccess);
		this.dataAccess = dataAccess;
	}

	@Override
	public FindReasult find(int startRow, int offset, String lang, String findStr) {
		List<String> disPkList = dataAccess.getDisplayTuIdentifiers();
		int startPk = Integer.parseInt(disPkList.get(startRow));
		// if (!(lastTuPks != null && lastTuPks.size() != 0 && lastFindData != null && lastStartRow == startRow &&
		// lastFindData
		// .equals(searchForward, caseSensitive, wholeWord, regExSearch, lang, findStr))) {
		List<Integer> lastTuPks = dataAccess.searchText(findStr, lang, startPk, searchForward, caseSensitive, regExSearch);
		// }
		if (lastTuPks == null || lastTuPks.size() == 0) {
			return null;
		}
		// if (lastFindData == null) {
		// lastFindData = new FindData(searchForward, caseSensitive, wholeWord, regExSearch, lang, findStr);
		// }
		if (searchForward) {
			if(lastTuPks.get(0) != startPk){
				offset = -1;
			}
			for (int i = 0; i < lastTuPks.size(); i++) {
				int tuPk = lastTuPks.get(i);
				if (!disPkList.contains(tuPk + "")) {
					continue;
				}
				TmxTU tu = dataAccess.getTuByIdentifier(tuPk + "");
				String fullText = null;
				if (tu.getSource().getLangCode().equals(lang)) {
					fullText = tu.getSource().getFullText();
				} else if (tu.getTarget().getLangCode().equals(lang)) {
					fullText = tu.getSource().getFullText();
				} else {
					return null;
				}
				Region r = matchString(offset, fullText, findStr);
				if (r != null) {
					// lastStartRow = startRow + i;
					return new FindReasult(r, tuPk + "");
				}
				offset = -1;
			}
		} else {
			if(lastTuPks.get(lastTuPks.size() - 1) != startPk){
				offset = -1;
			}
			for (int i = lastTuPks.size() - 1; i >= 0; i--) {
				int tuPk = lastTuPks.get(i);
				if (!disPkList.contains(tuPk + "")) {
					continue;
				}
				TmxTU tu = dataAccess.getTuByIdentifier(tuPk + "");
				String fullText = null;
				if (tu.getSource().getLangCode().equals(lang)) {
					fullText = tu.getSource().getFullText();
				} else if (tu.getTarget().getLangCode().equals(lang)) {
					fullText = tu.getSource().getFullText();
				} else {
					return null;
				}
				Region r = matchString(offset, fullText, findStr);
				if (r != null) {
					// lastStartRow = startRow - (lastTuPks.size() - 1 - i);
					return new FindReasult(r, tuPk + "");
				}
				offset = -1;
			}
		}
		return null;
	}

	class FindData {
		private boolean searchForward = true;

		private boolean caseSensitive;

		private boolean wholeWord;

		private boolean regExSearch;

		private String lang;
		private String findStr;

		public FindData(boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch,
				String lang, String findStr) {
			this.searchForward = searchForward;
			this.caseSensitive = caseSensitive;
			this.wholeWord = wholeWord;
			this.regExSearch = regExSearch;
			this.lang = lang;
			this.findStr = findStr;
		}

		public boolean equals(boolean searchForward, boolean caseSensitive, boolean wholeWord, boolean regExSearch,
				String lang, String findStr) {
			if (this.searchForward == searchForward && this.caseSensitive == caseSensitive
					&& this.wholeWord == wholeWord && this.regExSearch == regExSearch && this.lang.equals(lang)
					&& this.findStr.equals(findStr)) {
				return true;
			}
			return false;
		}

	}
}
