package net.heartsome.cat.te.tmxeditor.editor.nattable.action;

import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.CellEditor;
import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.TeActiveCellEditor;
import net.heartsome.cat.te.tmxeditor.editor.nattable.layer.LayerUtil;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.edit.command.EditCellCommand;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.ui.action.IKeyAction;
import net.sourceforge.nattable.ui.matcher.LetterOrDigitKeyEventMatcher;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.eclipse.swt.events.KeyEvent;

/**
 * 按下字母键、数字键、F2，触发单元格进入编辑模式
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class KeyEditAction implements IKeyAction {

	public void run(NatTable natTable, KeyEvent event) {
		Character character = null;
		if (LetterOrDigitKeyEventMatcher.isLetterOrDigit(event.character) || event.character == ' ') {
			character = Character.valueOf(event.character);
		}
		ViewportLayer viewportLayer = LayerUtil.getLayer(natTable, ViewportLayer.class);
		SelectionLayer selectionLayer = LayerUtil.getLayer(natTable, SelectionLayer.class);
		int[] selectedRowIndexs = selectionLayer.getFullySelectedRowPositions();
		if (selectedRowIndexs.length == 0) {
			return;
		}
		// 先记录下可见区域的范围
		int originRowPosition = viewportLayer.getOriginRowPosition();
		int rowCount = viewportLayer.getRowCount(); // 总行数
		int rowIndex = selectedRowIndexs[0];
		if (rowIndex < originRowPosition || rowIndex > originRowPosition + rowCount) {
			PositionCoordinate p = selectionLayer.getLastSelectedCellPosition();
			if( p.columnPosition !=1 &&  p.columnPosition != 2){
				 p.columnPosition = 2;
			}
			natTable.doCommand(new SelectCellCommand(selectionLayer, p.columnPosition, p.rowPosition, false, false));
		}
		natTable.doCommand(new EditCellCommand(null, null, null));
		if (character != null) {
			ICellEditor iCellEditor = TeActiveCellEditor.getCellEditor();
			if (iCellEditor != null && iCellEditor instanceof CellEditor) {
				CellEditor editor = (CellEditor) iCellEditor;
				editor.insertCanonicalValue(character);
			}
		}
	}

}