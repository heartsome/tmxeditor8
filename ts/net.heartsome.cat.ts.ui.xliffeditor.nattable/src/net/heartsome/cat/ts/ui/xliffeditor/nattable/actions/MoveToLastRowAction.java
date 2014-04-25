package net.heartsome.cat.ts.ui.xliffeditor.nattable.actions;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.action.AbstractKeySelectAction;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;

import org.eclipse.swt.events.KeyEvent;

public class MoveToLastRowAction extends AbstractKeySelectAction {

	public MoveToLastRowAction() {
		super(MoveDirectionEnum.DOWN, false, false);
	}

	@Override
	public void run(NatTable natTable, KeyEvent event) {
		HsMultiActiveCellEditor.commit(true);
		super.run(natTable, event);
		natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, SelectionLayer.MOVE_ALL, isShiftMask(), isControlMask()));
		HsMultiCellEditorControl.activeSourceAndTargetCell(XLIFFEditorImplWithNatTable.getCurrent());
	}

}
