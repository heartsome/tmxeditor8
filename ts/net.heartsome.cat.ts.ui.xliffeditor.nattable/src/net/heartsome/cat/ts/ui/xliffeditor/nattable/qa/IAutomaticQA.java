package net.heartsome.cat.ts.ui.xliffeditor.nattable.qa;

import net.heartsome.cat.ts.core.file.XLFHandler;

/**
 * 自动品质检查的接口
 * @author robert	2012-05-16
 */
public interface IAutomaticQA {
	/**
	 * 开始自动品质检查
	 * @param isAddToDb
	 * @param rowId
	 * @return
	 */
	public String beginAutoQa(boolean isAddToDb, String rowId, boolean needInitQAResultViewer);
	public void setInitData(XLFHandler handler);
}
