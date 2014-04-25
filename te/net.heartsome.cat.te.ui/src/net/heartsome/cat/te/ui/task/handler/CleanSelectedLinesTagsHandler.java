/**
 * CleanTagsHandler.java
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
import java.util.Arrays;

import net.heartsome.cat.te.core.tmxdata.TmxLargeFileDataAccess;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditor;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 清除内部标记
 * @author Austen
 * @version
 * @since JDK1.6
 */
public class CleanSelectedLinesTagsHandler extends AbstractHandler {

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final TmxEditor editor = TmxEditorViewer.getInstance().getTmxEditor();
		editor.commit();		
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event));
			final TmxLargeFileDataAccess access = (TmxLargeFileDataAccess) editor.getTmxDataAccess();
			try {
				dialog.run(true, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						access.clearSelectLinesInnerTag(monitor ,Arrays.asList(editor.getSelectIdentifiers()));
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								editor.loadDataAndReFreshUI(null, true);
							}
						});
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			IOperationHistory histor = OperationHistoryFactory.getOperationHistory();
			histor.dispose(PlatformUI.getWorkbench().getOperationSupport().getUndoContext(), true, true, true);
	
		return null;
	}

}
