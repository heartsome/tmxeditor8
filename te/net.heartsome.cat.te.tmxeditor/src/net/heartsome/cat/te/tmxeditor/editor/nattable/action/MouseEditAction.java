package net.heartsome.cat.te.tmxeditor.editor.nattable.action;

import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.TeActiveCellEditor;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.edit.command.EditCellCommand;
import net.sourceforge.nattable.selection.command.SelectCellCommand;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

/**
 * 当进入编辑模式后，刷新删除光标后内容和删除标记前内容的 Command
 * @author peason
 * @version
 * @since JDK1.6
 */
public class MouseEditAction extends net.sourceforge.nattable.edit.action.MouseEditAction {

	public void run(NatTable natTable, MouseEvent event) {
		int columnPosition = natTable.getColumnPositionByX(event.x);
		int rowPosition = natTable.getRowPositionByY(event.y);
	
		boolean withShiftMask = (event.stateMask & SWT.SHIFT) != 0;
		boolean withCtrlMask = (event.stateMask & SWT.CTRL) != 0;
		natTable.doCommand(new SelectCellCommand(natTable, columnPosition, rowPosition, withShiftMask, withCtrlMask));
		if(columnPosition == 1 || columnPosition == 2){
			natTable.doCommand(new EditCellCommand(null, null, null));
		} else {
			TeActiveCellEditor.commit();
		}
	}
}
