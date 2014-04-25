package net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor;

import net.heartsome.cat.te.tmxeditor.editor.nattable.commond.UpdateDataCommand;
import net.sourceforge.nattable.edit.ICellEditHandler;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;

public class CellEditHandler implements ICellEditHandler {

	private final ICellEditor cellEditor;
	private final ILayer layer;
	private final int columnIndex;
	private final int rowIndex;

	public CellEditHandler(ICellEditor cellEditor, ILayer layer, int columnIndex, int rowIndex) {
		this.cellEditor = cellEditor;
		this.layer = layer;
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}
	
	/**
	 * {@inheritDoc}
 	 * Note: Assumes that the value is valid.<br/>
	 */
	public boolean commit(MoveDirectionEnum direction, boolean closeEditorAfterCommit) {
		Object canonicalValue = cellEditor.getCanonicalValue();
		switch (direction) {
		case LEFT:
			layer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, 1, false, false));
			break;
		case RIGHT:
			layer.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, 1, false, false));
			break;
		}
		boolean committed = layer.doCommand(new UpdateDataCommand(layer, columnIndex, rowIndex, canonicalValue));
		if(committed && closeEditorAfterCommit){
			cellEditor.close();
			return true;
		}
		return committed;
	}
	
}