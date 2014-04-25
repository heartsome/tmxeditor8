/**
 * TmxEditorViewer.java
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

package net.heartsome.cat.te.tmxeditor.editor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.core.bean.TmxPropertiesBean;
import net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess;
import net.heartsome.cat.te.core.tmxdata.DataAccessFactory;
import net.heartsome.cat.te.core.tmxdata.ITmxDataChangeListener;
import net.heartsome.cat.te.core.tmxdata.TmxContainer;
import net.heartsome.cat.te.core.tmxdata.TmxContainerFactory;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileContainer;
import net.heartsome.cat.te.core.utils.TeCoreUtils;
import net.heartsome.cat.te.tmxeditor.editor.history.OpenRecord;
import net.heartsome.cat.te.tmxeditor.editor.history.TmxEditorHistory;
import net.heartsome.cat.te.tmxeditor.editor.history.TmxEditorHistoryItem;
import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.CellEditorGlobalActionHanlder;
import net.heartsome.cat.te.tmxeditor.resource.Messages;
import net.heartsome.cat.te.tmxeditor.view.TmxEditorOpenCloseListener;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.operations.UndoRedoActionGroup;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TmxEditor 编辑区域，他是一个单例（由 RCP 平台控制）
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxEditorViewer extends ViewPart implements ISaveablePart {
	public static final String ID = "net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer";

	public static final Logger LOGGER = LoggerFactory.getLogger(TmxEditorViewer.class);

	private TmxEditor tmxEditor;
	private Composite container;

	private List<TmxEditorOpenCloseListener> openCloseListener;

	private UndoRedoActionGroup undoRedoActionGroup;

	private IWorkbenchListener workbenchListener = new IWorkbenchListener() {

		@Override
		public boolean preShutdown(IWorkbench workbench, boolean forced) {
			return closeTmx();
		}

		@Override
		public void postShutdown(IWorkbench workbench) {
		}
	};

	/**
	 * 获取当前编辑器所在 Viewer
	 * @return ;
	 */
	public static TmxEditorViewer getInstance() {
		IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ID);
		if (viewPart != null && viewPart instanceof TmxEditorViewer) {
			return (TmxEditorViewer) viewPart;
		}
		return null;
	}

	public void recoverOpen(final String mainFile, final List<String> subFiles) {
		final AbstractTmxDataAccess[] dataAccessor = new AbstractTmxDataAccess[1];
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
		IRunnableWithProgress rwp = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.getString("tmxeditor.tmxeditorView.openFileTaskName"), 12);
				TmxLargeFileContainer container = new TmxLargeFileContainer();
				try {
					container.openFile(mainFile, subFiles, new SubProgressMonitor(monitor, 10));
				} catch (Exception e) {
					LOGGER.error("", e);
				}
				AbstractTmxDataAccess da = DataAccessFactory.createDataAccess(container);
				da.loadDisplayTuIdentifierByFilter(new SubProgressMonitor(monitor, 2), null, da.getCurrSrcLang(),
						da.getCurrTgtLang(), "", "");
				dataAccessor[0] = da;
				if (dataAccessor[0] == null) {
					return;
				}
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						openTmx(dataAccessor[0]);
					}
				});
				monitor.done();
			}
		};
		try {
			pmd.run(true, false, rwp);
		} catch (InvocationTargetException e) {
			LOGGER.error("", e);
			return;
		} catch (InterruptedException e) {
			LOGGER.error("", e);
			return;
		}
		if (dataAccessor[0] == null) {
			return;
		}
		fireOpenEvent(dataAccessor[0]);
		dataAccessor[0].setDirty(true);
		TmxEditorHistory.getInstance().add(TmxEditorHistoryItem.TYPE_TMX, mainFile);
		String title = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getText();
		title = title + "-" + mainFile;
		boolean readOnly = !new File(mainFile).canWrite();
		if (readOnly) {
			title += Messages.getString("tmxeditor.tmxeditorView.readOnly");
		}
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setText(title);
		if (readOnly) {
			OpenMessageUtils.openMessage(IStatus.INFO,
					Messages.getString("tmxeditor.tmxEditorViewer.fileReadOnlyOpenMsg"));
		}
		setFocus();
	}

	public void open(final File file) {
		if (tmxEditor != null && !closeTmx()) {
			return;
		}
		TeCoreUtils.closeQAViewer();
		final AbstractTmxDataAccess[] dataAccessor = new AbstractTmxDataAccess[1];
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(getSite().getShell());
		IRunnableWithProgress rwp = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.getString("tmxeditor.tmxeditorView.openFileTaskName"), 12);
				TmxContainer container = null;
				try {
					container = TmxContainerFactory.createLargeFileContainer(file, new SubProgressMonitor(monitor, 10));
				} catch (final Exception e) {
					LOGGER.error("", e);
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							OpenMessageUtils.openMessage(IStatus.INFO, e.getMessage());
						}
					});
					return;
				}
				if (container != null) {
					AbstractTmxDataAccess da = DataAccessFactory.createDataAccess(container);
					da.loadDisplayTuIdentifierByFilter(new SubProgressMonitor(monitor, 2), null, da.getCurrSrcLang(),
							da.getCurrTgtLang(), "", "");
					dataAccessor[0] = da;
				}
				if (dataAccessor[0] == null) {
					return;
				}
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						openTmx(dataAccessor[0]);
					}
				});
				monitor.done();
			}
		};
		try {
			pmd.run(true, true, rwp);
		} catch (InvocationTargetException e) {
			LOGGER.error("", e);
			Throwable t = e.getTargetException();
			if (t != null && t instanceof OutOfMemoryError) {
				OpenMessageUtils.openMessage(IStatus.INFO, Messages.getString("tmxeditor.tmxeditorView.outofmemory"));
			}
			return;
		} catch (InterruptedException e) {
			LOGGER.error("", e);
			return;
		}
		if (dataAccessor[0] == null) {
			return;
		}
		fireOpenEvent(dataAccessor[0]);
		new OpenRecord().saveOpenRecord(file.getAbsolutePath());
		TmxEditorHistory.getInstance().add(TmxEditorHistoryItem.TYPE_TMX, file.getAbsolutePath());
		String title = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getText();
		title = title + "-" + file.getAbsolutePath();
		boolean readOnly = !file.canWrite();
		if (readOnly) {
			title += Messages.getString("tmxeditor.tmxeditorView.readOnly");
		}
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setText(title);
		if (readOnly) {
			OpenMessageUtils.openMessage(IStatus.INFO,
					Messages.getString("tmxeditor.tmxEditorViewer.fileReadOnlyOpenMsg"));
		}
		setFocus();

	}

	public void open(final DatabaseModelBean db) {
		final AbstractTmxDataAccess[] dataAccessor = new AbstractTmxDataAccess[1];
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(getSite().getShell());
		IRunnableWithProgress rwp = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.getString("tmxeditor.tmxeditorView.openTMDBTaskName"), 3);
				monitor.worked(1);
				TmxContainer container = null;
				try {
					container = TmxContainerFactory.createContainer(db);
				} catch (final Exception e) {
					LOGGER.error("", e);
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							OpenMessageUtils.openMessage(IStatus.INFO, e.getMessage());
						}
					});
					return;
				}
				if (container == null) {
					return;
				}
				monitor.worked(1);
				AbstractTmxDataAccess da = DataAccessFactory.createDataAccess(container);
				TmxPropertiesBean bean = da.loadTmxProperties();
				if (bean == null) {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							OpenMessageUtils.openMessage(IStatus.INFO,
									Messages.getString("tmxeditor.tmxEditorViewer.emptyDbMsg"));
						}
					});
					return;
				}
				monitor.worked(1);
				da.loadDisplayTuIdentifierByFilter(null, null, da.getCurrSrcLang(), da.getCurrTgtLang(), "", "");
				dataAccessor[0] = da;
				if (dataAccessor[0] == null) {
					return;
				}
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						openTmx(dataAccessor[0]);
					}
				});
				monitor.done();
			}
		};
		try {
			pmd.run(true, true, rwp);
		} catch (InvocationTargetException e) {
			LOGGER.error("", e);
			return;
		} catch (InterruptedException e) {
			LOGGER.error("", e);
			return;
		}
		if (dataAccessor[0] == null) {
			return;
		}
		fireOpenEvent(dataAccessor[0]);
		boolean isReadOnly = false;
		String message = null;
		if (db.getItlDBLocation() != null && db.getItlDBLocation().length() != 0) {
			String path = db.getItlDBLocation() + File.separator + db.getDbName();
			TmxEditorHistory.getInstance().add(TmxEditorHistoryItem.TYPE_HSTM, path);
			String title = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getText();
			title = title + "-" + path;
			if (dataAccessor[0].isReadOnly()) {
				title += Messages.getString("tmxeditor.tmxeditorView.readOnly");
				isReadOnly = true;
				message = Messages.getString("tmxeditor.tmxEditorViewer.fileReadOnlyOpenMsg");
			}
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setText(title);
		} else {
			StringBuilder sb = new StringBuilder();
			String title = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getText();
			sb.append(title).append("-");
			sb.append(db.getHost()).append(":").append(db.getPort()).append("/").append(db.getDbName());
			if (dataAccessor[0].isReadOnly()) {
				sb.append(Messages.getString("tmxeditor.tmxeditorView.readOnly"));
				isReadOnly = true;
				message = Messages.getString("tmxeditor.tmxEditorViewer.DBReadOnlyOpenMsg");
			}
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setText(sb.toString());
		}
		if (isReadOnly && message != null) {
			OpenMessageUtils.openMessage(IStatus.INFO, message);
		}
		setFocus();
	}

	/**
	 * 打开一个已经初始好的 <code>AbstractTmxDataAccess</code>
	 * @param tmxDataAccess
	 *            ;
	 */
	private void openTmx(AbstractTmxDataAccess tmxDataAccess) {
		if (tmxDataAccess != null && container != null && !container.isDisposed()) {
			// 防止客户端将父容器 dispose
			Composite comp = new Composite(container, SWT.NONE);
			tmxEditor = new TmxEditor(tmxDataAccess);
			tmxEditor.createContent(this, comp);
			container.layout();
			setFocus();

			// 监听内容是否改变
			tmxDataAccess.addTmxDataChangeListener(new ITmxDataChangeListener() {

				@Override
				public void tmxDataChanged() {
					firePropertyChange(PROP_DIRTY);
				}
			});
		}
	}

	/**
	 * 关闭TmxEditor,同时关闭AbstractDataAccess
	 **/
	public boolean closeTmx() {
		if (tmxEditor == null) {
			return true;
		}
		if (!tmxEditor.closeTmxEditor()) {
			return false;
		}
		tmxEditor = null;
		Control[] childs = container.getChildren();
		for (Control c : childs) {
			if (c != null && !c.isDisposed()) {
				c.dispose();
			}
		}
		fireCloseEvent();
		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
		operationHistory.dispose(getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext(),
				true, true, true);
		setFocus();
		String title = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getText();
		String[] s = title.split("-");
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setText(s[0]);
		return true;
	}

	public void refresh() {

	}

	/**
	 * 此方法紧在 RCP handler 中使用，不宜在其他地方使用。 获取当前编辑器实际对象<code>TmxEditor</code><br>
	 * 可能是一个 NULL 值，如果没有打开任何 TMX 或 DB 前。
	 * @return ;
	 */
	public TmxEditor getTmxEditor() {
		return this.tmxEditor;
	}

	public void addOpenCloseListener(TmxEditorOpenCloseListener listener) {
		if (this.openCloseListener.contains(listener)) {
			return;
		}
		this.openCloseListener.add(listener);
	}

	public void removeOpenCloseListener(TmxEditorOpenCloseListener listener) {
		this.openCloseListener.remove(listener);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		this.openCloseListener = new ArrayList<TmxEditorOpenCloseListener>();
		undoRedoActionGroup = new UndoRedoActionGroup(getSite(), getSite().getWorkbenchWindow().getWorkbench()
				.getOperationSupport().getUndoContext(), true);
		PlatformUI.getWorkbench().addWorkbenchListener(workbenchListener);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		this.container = parent;
		CellEditorGlobalActionHanlder.getInstance().setIActionBars(getViewSite().getActionBars());
		DropTarget target = new DropTarget(parent, DND.DROP_MOVE | DND.DROP_COPY);
		Transfer[] tfs = new Transfer[] { FileTransfer.getInstance() };
		target.setTransfer(tfs);
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				Object o = event.data;
				if (o instanceof String[]) {
					String[] s = (String[]) o;
					if (s.length == 0 && s[0] == null || s[0].length() == 0) {
						return;
					}
					File f = new File(s[0]);
					if (f.exists() && f.isFile()) {
						open(f);
					}
				}
			}
		});
	}

	@Override
	public void setFocus() {
		if (container != null && !container.isDisposed()) {
			container.setFocus();
		}
		undoRedoActionGroup.fillActionBars(getViewSite().getActionBars());
	}

	@Override
	public void dispose() {
		PlatformUI.getWorkbench().removeWorkbenchListener(workbenchListener);
	}

	private void fireOpenEvent(AbstractTmxDataAccess dataAccess) {
		for (TmxEditorOpenCloseListener listener : openCloseListener) {
			listener.editorOpened(dataAccess);
		}
	}

	private void fireCloseEvent() {
		for (TmxEditorOpenCloseListener listener : openCloseListener) {
			listener.editorClosed();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		final String[] msgs = new String[1];
		IRunnableWithProgress p = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(Messages.getString("tmxeditor.tmxEditorViewer.savelabel"), 1);
				try {
					tmxEditor.save(new SubProgressMonitor(monitor, 1));
				} catch (Exception e) {
					msgs[0] = e.getMessage();
				}
				monitor.done();
			}
		};
		try {
			new ProgressMonitorDialog(getSite().getShell()).run(true, true, p);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		if (msgs[0] != null) {
			OpenMessageUtils.openMessage(IStatus.ERROR, msgs[0]);
		}
	}

	@Override
	public void doSaveAs() {
		tmxEditor.saveAs();
//		FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
//		String[] filterExt = { "*.tmx", "*.*" };
//		dlg.setFilterExtensions(filterExt);
//		final String filePath = dlg.open();
//		if (filePath == null) {
//			return;
//		}
//		File file = new File(filePath);
//		if (file.exists()) {
//			String msg = Messages.getString("tmxeditor.tmxEditorViewer.saveAsMsg1");
//			msg = MessageFormat.format(msg, filePath);
//			if (!OpenMessageUtils.openConfirmMessage(msg)) {
//				return;
//			}
//		}
//		IRunnableWithProgress p = new IRunnableWithProgress() {
//
//			@Override
//			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//				monitor.beginTask(Messages.getString("tmxeditor.tmxEditorViewer.savelabel"), 1);
//				try {
//					tmxEditor.saveAs(new SubProgressMonitor(monitor, 1), filePath);
//				} catch (final Exception e) {
//					Display.getDefault().syncExec(new Runnable() {
//
//						@Override
//						public void run() {
//							OpenMessageUtils.openMessage(IStatus.ERROR, e.getMessage());
//						}
//					});
//				}
//				monitor.done();
//			}
//		};
//		try {
//			new ProgressMonitorDialog(getSite().getShell()).run(true, true, p);
//		} catch (Exception e) {
//			LOGGER.error("", e);
//		}
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		if (tmxEditor != null) {
			return tmxEditor.isDirty();
		}
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return tmxEditor != null;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return false;
	}

	public void notifyOpenEvent(AbstractTmxDataAccess dataAccess) {
		fireOpenEvent(dataAccess);
	}
}
