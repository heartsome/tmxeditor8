/**
 * UpdateDataOperation.java
 *
 * Version information :
 *
 * Date:2013/5/17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */

package net.heartsome.cat.te.tmxeditor.editor.nattable.undo;

import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable.BodyLayer;
import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.CellEditorCanonicalValue;
import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.TeActiveCellEditor;
import net.heartsome.cat.te.tmxeditor.editor.nattable.commond.AutoResizeCurrentRowsCommand;
import net.heartsome.cat.te.tmxeditor.editor.nattable.commond.UpdateDataCommand;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.edit.command.EditCellCommand;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.selection.command.SelectCellCommand;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class UpdateDataOperation extends AbstractOperation {
	private final Object oldValue;
	private final UpdateDataCommand command;

	private final BodyLayer bodyLayer;
	private final NatTable table;

	private final int rowIndex;
	private final int colIndex;

	public UpdateDataOperation(NatTable table, BodyLayer bodyLayer, UpdateDataCommand command) {
		super("tmxeditorundoable");
		this.command = command;
		this.bodyLayer = bodyLayer;
		this.table = table;

		colIndex = command.getColumnPosition();
		rowIndex = command.getRowPosition();

		TmxTU currentTu = (TmxTU) bodyLayer.getDataLayer().getDataValueByPosition(colIndex, rowIndex);
		if (colIndex == 1) {
			// source
			if (currentTu.getSource() != null) {
				oldValue = currentTu.getSource().getFullText();
			} else {
				oldValue = "";
			}
		} else if (colIndex == 2) {
			// target
			if (currentTu.getTarget() != null) {
				oldValue = currentTu.getTarget().getFullText();
			} else {
				oldValue = "";
			}
		} else {
			oldValue = null;
		}
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		CellEditorCanonicalValue newValue = null;
		Object obj = command.getNewValue();
		if (obj instanceof CellEditorCanonicalValue) {
			newValue = (CellEditorCanonicalValue) obj;
		}
		if (oldValue == null || newValue == null || oldValue.equals(newValue.getNewFullValue())) {
			return Status.CANCEL_STATUS;
		}

		return updateData(newValue.getNewFullValue(), false);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		CellEditorCanonicalValue newValue = null;
		Object obj = command.getNewValue();
		if (obj instanceof CellEditorCanonicalValue) {
			newValue = (CellEditorCanonicalValue) obj;
		}
		if (newValue == null) {
			return Status.CANCEL_STATUS;
		}
		TeActiveCellEditor.commit();
		return updateData(newValue.getNewFullValue(), true);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if (oldValue == null) {
			return Status.CANCEL_STATUS;
		}
		TeActiveCellEditor.commit();
		return updateData((String) oldValue, true);
	}

	private IStatus updateData(String value, boolean move) {
		if (table == null || table.isDisposed()) {
			return Status.CANCEL_STATUS;
		}
		if (rowIndex == -1 || colIndex == -1) {
			return Status.CANCEL_STATUS;
		}
		DataLayer dataLayer = bodyLayer.getDataLayer();

		// 修改值并刷新 UI。
		dataLayer.getDataProvider().setDataValue(colIndex, rowIndex, value);

		// 此操作会自动调整选中单元格进入可见区域
		if (move) { // 跳转到相应的行
			int rowPosition = command.getRowPosition();
			table.doCommand(new AutoResizeCurrentRowsCommand(table, new int[] { rowPosition + 1 }, table
					.getConfigRegistry()));
			bodyLayer.getSelectionLayer().doCommand(
					new SelectCellCommand(bodyLayer.getSelectionLayer(), colIndex, rowIndex, false, false));
			table.doCommand(new EditCellCommand(null, null, null));
		}
		return Status.OK_STATUS;
	}
}
