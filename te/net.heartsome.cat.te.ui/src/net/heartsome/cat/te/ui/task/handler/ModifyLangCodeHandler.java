/**
 * ModifyLangCodeHandler.java
 *
 * Version information :
 *
 * Date:2013-7-31
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.ui.task.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.core.bean.TmxPropertiesBean;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileDataAccess;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditor;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.ui.resource.Messages;
import net.heartsome.cat.te.ui.task.ui.ModifyLangCodeDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author  Austen
 * @version 
 * @since   JDK1.6
 */
public class ModifyLangCodeHandler extends AbstractHandler implements IHandler {

	/** (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Shell shell = HandlerUtil.getActiveShell(event);
		final TmxEditorViewer viewer = TmxEditorViewer.getInstance();
		final TmxEditor editor =  viewer.getTmxEditor();
		TmxPropertiesBean propbean= editor.getTmxProperties(true);
		//收集信息
		final Map<String, String> resultMap = new HashMap<String, String>();
		ModifyLangCodeDialog dialog = new ModifyLangCodeDialog(shell, propbean, resultMap);
		if (Dialog.OK == dialog.open()) {
			if(!OpenMessageUtils.openConfirmMessage(Messages.getString("dialog.ModifyLangCodeDialog.checkMsg3"))){
				return null;
			}
			final TmxLargeFileDataAccess access = (TmxLargeFileDataAccess) editor.getTmxDataAccess();
			ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(shell);
			try {
				progressDialog.run(true, false, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						access.batchModifyLangcode(resultMap, monitor);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
			editor.reCreateUI();
			IOperationHistory histor = OperationHistoryFactory.getOperationHistory();
			histor.dispose(PlatformUI.getWorkbench().getOperationSupport().getUndoContext(), true, true, true);
			access.setDirty(true);
			viewer.setFocus();
		}
		return null;
	}

}
