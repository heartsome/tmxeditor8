/**
 * TmxEditorDataProvider.java
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

package net.heartsome.cat.te.tmxeditor.editor.nattable;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess;
import net.heartsome.cat.te.core.tmxdata.TmxTuCache;
import net.sourceforge.nattable.data.IColumnAccessor;
import net.sourceforge.nattable.data.IRowDataProvider;

public class TmxEditorDataProvider<T> implements IRowDataProvider<T> {

	private TmxTuCache cache = new TmxTuCache(200);
	private AbstractTmxDataAccess dataAccessor;
	protected IColumnAccessor<T> columnAccessor;

	public TmxEditorDataProvider(AbstractTmxDataAccess dataAccessor, IColumnAccessor<T> columnAccessor) {
		this.dataAccessor = dataAccessor;
		this.columnAccessor = columnAccessor;
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		if (columnIndex == 0) {
			return rowIndex + 1;
		}
		if (columnIndex == 4) {
			return ""; // TODO 等待实现painter
		}
		return getRowObject(rowIndex);
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		if (newValue == null) {
			return;
		}
		TmxTU tu = (TmxTU) getRowObject(rowIndex);
		if (tu == null) {
			return;
		}
		String id = this.dataAccessor.getTuIndentifierByRowIndex(rowIndex);
		TmxSegement tuv = null;
		if (columnIndex == 1) { // source
			tuv = tu.getSource();
		} else if (columnIndex == 2) {// target
			tuv = tu.getTarget();
		}
		if (tuv == null) {
			return;
		}
		// long l = System.currentTimeMillis();
		this.dataAccessor.updateTuvContent(id, (String) newValue, tu, tuv);
		this.dataAccessor.setDirty(true);
		// System.out.println("save: "+(System.currentTimeMillis() - l));
	}

	@Override
	public int getColumnCount() {
		return columnAccessor.getColumnCount();
	}

	@Override
	public int getRowCount() {
		return this.dataAccessor.getDisplayTuCount();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getRowObject(int rowIndex) {
		TmxTU tu = cache.getElement(rowIndex);
		if (tu == null) {
			String id = dataAccessor.getDisplayTuIdentifiers().get(rowIndex);
			tu = dataAccessor.getTuByIdentifier(id);
			cache.addElement(rowIndex, tu);
		}
		return (T) tu;
	}

	@Override
	public int indexOfRowObject(T rowObject) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void clearCache() {
		cache.clear();
	}
}
