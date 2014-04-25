package net.heartsome.cat.ts.ui.xliffeditor.nattable.actions;

import java.util.Arrays;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.NatTableConstant;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.DeleteToEndOrToTagPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
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
		if (character != null) {
			int[] selectedRowIndexs = XLIFFEditorImplWithNatTable.getCurrent().getSelectedRows();
			if(selectedRowIndexs.length == 0){
				return;
			}
			Arrays.sort(selectedRowIndexs);
			int rowIndex = selectedRowIndexs[selectedRowIndexs.length - 1];
			
			ViewportLayer viewportLayer = LayerUtil.getLayer(natTable, ViewportLayer.class);
			SelectionLayer selectionLayer = LayerUtil.getLayer(natTable, SelectionLayer.class);
			// 先记录下可见区域的范围
			int originRowPosition = viewportLayer.getOriginRowPosition();
			int rowCount = viewportLayer.getRowCount(); // 总行数
			XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();

			if(!editor.isHorizontalLayout()){
				rowIndex = rowIndex * 2;
			}
			if (rowIndex < originRowPosition || rowIndex > originRowPosition + rowCount) {
				HsMultiActiveCellEditor.commit(true);
				PositionCoordinate p = selectionLayer.getLastSelectedCellPosition();
				if(!editor.isHorizontalLayout()){
					natTable.doCommand(new SelectCellCommand(selectionLayer, p.columnPosition, p.rowPosition / 2 * 2, false, false));
				} else {
					natTable.doCommand(new SelectCellCommand(selectionLayer, p.columnPosition, p.rowPosition, false, false));
				}
				HsMultiCellEditorControl.activeSourceAndTargetCell(editor);
				StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getFocusCellEditor();
				if(cellEditor != null && cellEditor.getCellType().equals(NatTableConstant.TARGET)){
					cellEditor.insertCanonicalValue(character);
				}
			}
		}		
	}

}