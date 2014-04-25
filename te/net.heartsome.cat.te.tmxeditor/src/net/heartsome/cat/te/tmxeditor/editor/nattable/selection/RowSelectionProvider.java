package net.heartsome.cat.te.tmxeditor.editor.nattable.selection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.te.core.bean.TmxEditorSelection;
import net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.ILayerListener;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.command.SelectRowsCommand;
import net.sourceforge.nattable.selection.event.CellSelectionEvent;
import net.sourceforge.nattable.selection.event.ISelectionEvent;
import net.sourceforge.nattable.util.ObjectUtils;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

public class RowSelectionProvider implements ISelectionProvider, ILayerListener {

	private SelectionLayer selectionLayer;
	private final boolean fullySelectedRowsOnly;
	private AbstractTmxDataAccess dataAccess;
	private Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();

	public RowSelectionProvider(SelectionLayer selectionLayer, boolean fullySelectedRowsOnly,
			AbstractTmxDataAccess dataAccess) {
		this.selectionLayer = selectionLayer;
		this.fullySelectedRowsOnly = fullySelectedRowsOnly;
		this.dataAccess = dataAccess;
		selectionLayer.addLayerListener(this);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	public ISelection getSelection() {
		int[] rowPositions = selectionLayer.getFullySelectedRowPositions();
		if (rowPositions.length > 0) {
			Arrays.sort(rowPositions);
			int rowPosition = rowPositions[rowPositions.length - 1];
			int rowIndex = selectionLayer.getRowIndexByPosition(rowPosition);
			return new StructuredSelection(rowIndex);
		}

		return new StructuredSelection();
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	public void setSelection(ISelection selection) {
		if (selectionLayer != null && selection instanceof IStructuredSelection) {
			selectionLayer.clear();
			List<Integer> rowIndexs = ((IStructuredSelection) selection).toList();
			Set<Integer> rowPositions = new HashSet<Integer>();
			for (Integer rowIndex : rowIndexs) {
				int rowPosition = selectionLayer.getRowPositionByIndex(rowIndex);
				rowPositions.add(Integer.valueOf(rowPosition));
			}
			selectionLayer.doCommand(new SelectRowsCommand(selectionLayer, 0, ObjectUtils.asIntArray(rowPositions),
					false, true));
		}
	}

	// private int currentRowPosition = -1;
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof ISelectionEvent) {
			// 如果没有可显示的数据，直接退出选中文本段
			if (dataAccess.getDisplayTuIdentifiers().size() <= 0) {
				return;
			}
			
			// 选择的是哪一列
			int columnIndex = ((CellSelectionEvent) event).getColumnPosition();
			int currentRowIndex = ((CellSelectionEvent) event).getRowPosition();
			if (columnIndex == -1) {
				columnIndex = 2;
			}
			
			// 选择了哪些行
			int[] rowPositions = selectionLayer.getFullySelectedRowPositions();
			if (fullySelectedRowsOnly && rowPositions.length == 0) {
				return;
			}

			// 装填 selection
			TmxEditorSelection selections = new TmxEditorSelection();
			DataLayer dataLayer = (DataLayer) selectionLayer.getUnderlyingLayerByPosition(0, 0);
			IDataProvider dataProvider = dataLayer.getDataProvider();
			for (int rowIndex : rowPositions) {
				Object obj = dataProvider.getDataValue(columnIndex, rowIndex);
				if (obj instanceof TmxTU) {
					String identifier = dataAccess.getDisplayTuIdentifiers().get(rowIndex);
					selections.addTu(identifier, (TmxTU) obj);
				}
			}
			
			
			// 用于显示的 row
			selections.setIdentifier(dataAccess.getDisplayTuIdentifiers().get(currentRowIndex));
			selections.setSelectedColumn(columnIndex);

			ISelection selection = new StructuredSelection(selections);
			for (ISelectionChangedListener listener : listeners) {
				SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
				listener.selectionChanged(e);
			}
		}
	}

}
