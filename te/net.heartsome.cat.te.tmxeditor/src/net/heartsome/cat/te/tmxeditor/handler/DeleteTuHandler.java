/**
 * DeleteTuHandler.java
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

package net.heartsome.cat.te.tmxeditor.handler;

import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditor;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.tmxeditor.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class DeleteTuHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		TmxEditorViewer viewer = TmxEditorViewer.getInstance();
		if (viewer == null) {
			return null;
		}
		TmxEditor editor = viewer.getTmxEditor();
		if (editor == null) {
			return null;
		}
		if (editor.getTmxDataAccess().getDisplayTuCount() == 0
				|| editor.getTmxEditorImpWithNattable().getSelectedRows().length == 0) {
			OpenMessageUtils.openMessage(IStatus.INFO, Messages.getString("tmxeditor.deleteTuHandler.noSelectedMsg"));
			return null;
		}
		boolean confirm = MessageDialog.openConfirm(HandlerUtil.getActiveShell(event),
				Messages.getString("tmxeditor.deleteTuHandler.warn.msg"),
				Messages.getString("tmxeditor.deleteTuHandler.warn.desc"));
		if (!confirm) {
			return null;
		}
		editor.deleteSelectedTu();
		IOperationHistory histor = OperationHistoryFactory.getOperationHistory();
		histor.dispose(PlatformUI.getWorkbench().getOperationSupport().getUndoContext(), true, true, true);
		return null;
	}
}
