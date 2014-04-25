/**
 * DeleteEmptySegment.java
 *
 * Version information :
 *
 * Date:2013-8-5
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.ui.task.handler;

import java.lang.reflect.InvocationTargetException;

import net.heartsome.cat.te.tmxeditor.editor.TmxEditor;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.ui.resource.Messages;
import net.heartsome.cat.te.ui.task.ui.BatchJobsDialog;
import net.heartsome.cat.te.ui.task.ui.BatchSelectionsBean;

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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class DeleteBatchJobsHandler extends AbstractHandler {

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BatchJobsDialog jobDialog = new BatchJobsDialog(Display.getDefault().getActiveShell());
		jobDialog.open();
		if (jobDialog.getReturnCode() == Dialog.OK) {
			final BatchSelectionsBean jobs = jobDialog.getJobs();
			if (null == jobs || jobs.getDeleteTypeCount() == 0) {
				return null;
			}
			boolean confirm = MessageDialog.openConfirm(HandlerUtil.getActiveShell(event),
					Messages.getString("te.ui.deleteBatchJobsHandler.warn.msg"),
					Messages.getString("te.ui.deleteBatchJobsHandler.warn.desc"));
			final TmxEditor editor = TmxEditorViewer.getInstance().getTmxEditor();
			editor.commit();
			if (confirm) {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event));
				try {
					dialog.run(true, false, new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {
							// editor.cleartInnerTag(monitor);
							monitor.beginTask(Messages.getString("te.ui.deleteBatchJobsHandler.begin.task.name"),
									jobs.getDeleteTypeCount() * 100);
							SubProgressMonitor deleteEmptyJob = new SubProgressMonitor(monitor, 100);
							if (jobs.isDeleteEmpty()) {// 删除空文本段
								editor.clearEmptyTU(deleteEmptyJob,jobs.isIgnoreTag());
							} else {
								deleteEmptyJob.done();
							}

							SubProgressMonitor trimSegmentJob = new SubProgressMonitor(monitor, 100);
							if (jobs.isTrimSegment()) {// 删除段末段首空格
								editor.clearEndsSpaces(trimSegmentJob);
							} else {
								trimSegmentJob.done();
							}

							SubProgressMonitor deleteDuplicateJob = new SubProgressMonitor(monitor, 100);
							if (jobs.isDeleteDupliacate()) {// 删除重复文本
								editor.cleartDuplicatedTU(deleteDuplicateJob, jobs.isIgnoreTag(),jobs.isIgnoreCase());
							} else {
								deleteDuplicateJob.done();
							}

							SubProgressMonitor deleteSameSrcDiffTgtJob = new SubProgressMonitor(monitor, 100);
							if (jobs.isDeleteSameSrcDiffTgt()) {// 删除原文相同，译文不同的文本段
								editor.cleartDuplicatedSrcDiffTgtTU(deleteSameSrcDiffTgtJob, jobs.isIgnoreTag(),jobs.isIgnoreCase());
							} else {
								deleteSameSrcDiffTgtJob.done();
							}

							IOperationHistory histor = OperationHistoryFactory.getOperationHistory();
							histor.dispose(PlatformUI.getWorkbench().getOperationSupport().getUndoContext(), true,
									true, true);

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
			}

		}
		return null;

	}

}
