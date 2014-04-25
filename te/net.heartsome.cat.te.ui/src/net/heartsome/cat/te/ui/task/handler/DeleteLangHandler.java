/**
 * DeleteLangHandler.java
 *
 * Version information :
 *
 * Date:2013-8-1
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.ui.task.handler;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.core.bean.TmxPropertiesBean;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileDataAccess;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditor;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.ui.resource.Messages;
import net.heartsome.cat.te.ui.task.ui.DeleteLangCodeDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Austen
 * @version
 * @since JDK1.6
 */
public class DeleteLangHandler extends AbstractHandler {

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		final TmxEditor editor = TmxEditorViewer.getInstance().getTmxEditor();
		TmxPropertiesBean propbean = editor.getTmxProperties(true);
		if (!(propbean.getTargetLang().size() > 1)) {
			MessageDialog.openError(shell, Messages.getString("ui.all.dialog.error"),
					Messages.getString("te.ui.deleteLangHandler.error.desc"));
			return null;
		}

		final List<String> deleteLangCode = new LinkedList<String>();
		DeleteLangCodeDialog dialog = new DeleteLangCodeDialog(shell, propbean, deleteLangCode);
		if (Dialog.OK == dialog.open()) {
			if (deleteLangCode.size() < 1) {
				return null;
			}
			String checkMsg = MessageFormat.format(Messages.getString("te.ui.deleteLangHanlder.checkMsg"),
					deleteLangCode.toArray());
			if (!OpenMessageUtils.openConfirmMessage(checkMsg)) {
				return null;
			}
			ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(shell);
			try {
				progressDialog.run(true, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						final TmxLargeFileDataAccess access = (TmxLargeFileDataAccess) editor.getTmxDataAccess();
						monitor.beginTask(Messages.getString("te.ui.deleteLangHandler.executeOP"), 3);
						access.batchdeleteTuvBylang(deleteLangCode, new SubProgressMonitor(monitor, 2));

						final IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								access.loadDisplayTuIdentifierByFilter(subMonitor, null, access.getCurrSrcLang(),
										access.getCurrTgtLang(), "", "");
							}
						});
						monitor.done();
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			IOperationHistory histor = OperationHistoryFactory.getOperationHistory();
			histor.dispose(PlatformUI.getWorkbench().getOperationSupport().getUndoContext(), true, true, true);
		}
		editor.reCreateUI();
		editor.resetFileter();
		return null;
	}
}
