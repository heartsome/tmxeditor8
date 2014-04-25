package net.heartsome.cat.te.tmxeditor.view.provider;

import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.util.TextUtil;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class PropertiesLablerProvider implements ITableLabelProvider {

	private Table table;

	public PropertiesLablerProvider() {

	}

	/**
	 * 需要序号列，请使用此构造方法
	 * @param table
	 *            传入table 的实例
	 */
	public PropertiesLablerProvider(Table table) {
		this.table = table;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof String[]) {
			String[] kv = (String[]) element;
			switch (columnIndex) {
			case 0:
				return kv[0];
			case 1:
				return kv[1];
			case 3:
				return kv.length > 2 ? kv[2] : "";
			default:
				return "unknown";
			}
		} else if (element instanceof TmxNote) {
			TmxNote note = (TmxNote) element;
			switch (columnIndex) {
			case 0:
				int no = 0;
				for (TableItem item : table.getItems()) {
					no++;
					if (item.getText().isEmpty()) {
						break;
					}
				}
				return String.valueOf(no++);
			case 1:
				return note.getContent();
			}
		} else if (element instanceof TmxProp) {
			TmxProp prop = (TmxProp) element;
			switch (columnIndex) {
			case 0:
				return prop.getName();
			case 1:
				return prop.getValue();
			case 2:
				return String.valueOf(prop.getDbPk());
			}
		}
		return String.valueOf(columnIndex);
	}
}
