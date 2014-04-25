package net.heartsome.cat.ts.ui.xliffeditor.nattable.actions;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.XLIFFEditorSelectionPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;
import net.sourceforge.nattable.NatTable;

import org.eclipse.swt.events.MouseEvent;

/**
 * 执行此 Action 后会刷新 XLIFFEditorSelectionPropertyTester 的状态
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class SelectCellAction extends net.sourceforge.nattable.selection.action.SelectCellAction {
	@Override
	public void run(NatTable natTable, MouseEvent event) {
		HsMultiActiveCellEditor.commit(true);
		super.run(natTable, event);
		NattableUtil.refreshCommand(XLIFFEditorSelectionPropertyTester.PROPERTY_NAMESPACE,
				XLIFFEditorSelectionPropertyTester.PROPERTY_ENABLED);
		HsMultiCellEditorControl.activeSourceAndTargetCell(XLIFFEditorImplWithNatTable.getCurrent());
	}
}
