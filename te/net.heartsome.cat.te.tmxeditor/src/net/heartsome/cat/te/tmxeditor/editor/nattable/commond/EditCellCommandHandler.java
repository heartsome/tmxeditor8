package net.heartsome.cat.te.tmxeditor.editor.nattable.commond;

import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable;
import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.CellEditController;
import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.edit.command.EditCellCommand;

public class EditCellCommandHandler extends AbstractLayerCommandHandler<EditCellCommand> {
	private TmxEditorImpWithNattable editor;
	public EditCellCommandHandler(TmxEditorImpWithNattable editor) {
		this.editor = editor;
	}
	
	public Class<EditCellCommand> getCommandClass() {
		return EditCellCommand.class;
	}

	@Override
	public boolean doCommand(EditCellCommand command) {
		return CellEditController.editCellInline(editor);
	}

}