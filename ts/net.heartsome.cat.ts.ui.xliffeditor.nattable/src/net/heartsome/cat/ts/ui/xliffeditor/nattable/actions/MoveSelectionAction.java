package net.heartsome.cat.ts.ui.xliffeditor.nattable.actions;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.action.AbstractKeySelectAction;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;

import org.eclipse.swt.events.KeyEvent;

public class MoveSelectionAction extends AbstractKeySelectAction {

	public MoveSelectionAction(MoveDirectionEnum direction) {
		super(direction);
	}

	public MoveSelectionAction(MoveDirectionEnum direction, boolean shiftMask, boolean ctrlMask) {
		super(direction, shiftMask, ctrlMask);
	}

	@Override
	public void run(NatTable natTable, KeyEvent event) {
		HsMultiActiveCellEditor.commit(true);
		super.run(natTable, event);
		natTable.doCommand(new MoveSelectionCommand(getDirection(), 1, isShiftMask(), isControlMask()));
		HsMultiCellEditorControl.activeSourceAndTargetCell(XLIFFEditorImplWithNatTable.getCurrent());
	}

}
