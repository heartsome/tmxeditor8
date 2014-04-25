/**
 * MergeTmxDialog.java
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

package net.heartsome.cat.te.ui.mergetmx.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.ui.mergetmx.MergeTmx;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class MergeTmxDialog extends HsAbstractProgressDialog implements IRunnableWithProgress {
	private Table table;
	private TableViewer tableViewer;
	private List<String> fileList = new ArrayList<String>();
	private Text tgtTxt;
	private boolean confirm = true;
	private String tgtFileLC;

	public MergeTmxDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.TITLE);
	}

	/**
	 * 初始化对话框位置
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(600, 450);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.MergeTmxDilog.mergeTmx.title"));
	}

	@Override
	public Composite createClientArea(Composite clientContainer) {
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(clientContainer);

		Group fileGroup = new Group(clientContainer, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fileGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(fileGroup);
		fileGroup.setText(Messages.getString("dialog.MergeTmxDilog.tmxFile"));

		tableViewer = new TableViewer(fileGroup, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		table = tableViewer.getTable();
		GridData tableData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		tableData.heightHint = 50;
		table.setLayoutData(tableData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		String[] headerNames = new String[] { Messages.getString("dialog.MergeTmxDilog.header.name1"),
				Messages.getString("dialog.MergeTmxDilog.header.name2") };
		int[] styles = new int[] { SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < headerNames.length; i++) {
			TableColumn column = new TableColumn(table, styles[i]);
			column.setText(headerNames[i]);
		}
		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(fileList);
		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.1, 0.88 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});

		// 按钮区
		Composite btnCmp = new Composite(fileGroup, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).grab(false, true).applyTo(btnCmp);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(btnCmp);

		Button addBtn = new Button(btnCmp, SWT.NONE);
		addBtn.setText(Messages.getString("dialog.MergeTmxDilog.addTmxFile"));
		setButtonLayoutData(addBtn);
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
				dialog.setFilterExtensions(new String[] { "*.tmx", "*.*" });
				dialog.open();
				String filterPath = dialog.getFilterPath();
				String[] fileNameS = dialog.getFileNames();
				String fileLC = null;
				for (String fileName : fileNameS) {
					fileLC = new File(filterPath + File.separator + fileName).getAbsolutePath();
					if (!fileList.contains(fileLC)) {
						fileList.add(fileLC);
						tableViewer.refresh();
					}
				}
				check();
			}
		});

		Button deleteBtn = new Button(btnCmp, SWT.NONE);
		deleteBtn.setText(Messages.getString("dialog.MergeTmxDilog.deleteTmxFile"));
		setButtonLayoutData(deleteBtn);
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = tableViewer.getSelection();
				if (selection != null && !selection.isEmpty()) {
					if (selection != null && !selection.isEmpty()) {
						int[] indices = table.getSelectionIndices();
						for (int index : indices) {
							String fileLC = table.getItem(index).getText(1);
							fileList.remove(fileLC);
						}
						tableViewer.refresh();
					}
				}
				check();
			}
		});

		// 保存路径
		Composite saveCmp = new Composite(clientContainer, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(saveCmp);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(saveCmp);

		Label saveLbl = new Label(saveCmp, SWT.NONE);
		saveLbl.setText(Messages.getString("dialog.MergeTmxDilog.saveTo"));

		tgtTxt = new Text(saveCmp, SWT.BORDER | SWT.READ_ONLY);
		tgtTxt.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(tgtTxt);

		Button browerBtn = new Button(saveCmp, SWT.NONE);
		browerBtn.setText(Messages.getString("dialog.MergeTmxDilog.brower"));
		browerBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.SAVE);
				dialog.setFilterExtensions(new String[] { "*.tmx", "*.*" });
				String curtgtFileLC = dialog.open();
				if (curtgtFileLC != null) {
					tgtTxt.setText(curtgtFileLC);
				}
				check();
			}
		});

		return null;
	}

	private void check() {
		setOkBtnEnable((fileList.size() >= 2) && !tgtTxt.getText().isEmpty());
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog#getDisplayHelpUrl()
	 */
	@Override
	public String getDisplayHelpUrl() {
		return "/net.heartsome.cat.te.ui.help/html/{0}/ch03.html#tmx_merge_id";
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask(Messages.getString("dialog.MergeTmxDilog.mergeTmx.taskName"), 1);
		for (final String fileLC : fileList) {
			if (!new File(fileLC).exists()) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						OpenMessageUtils.openMessage(IStatus.INFO,
								MessageFormat.format(Messages.getString("dialog.MergeTmxDilog.merge.info.msg"), fileLC));
					}
				});
				throw new OperationCanceledException();
			}
			
			// 判断是否是空文件
			if (new File(fileLC).length() <= 0) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						OpenMessageUtils.openMessage(IStatus.INFO,
								MessageFormat.format(Messages.getString("dialog.MergeTmxDilog.merge.fileNull.msg"), fileLC));
					}
				});
				throw new OperationCanceledException();
			}
			
		}
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				tgtFileLC = tgtTxt.getText();
			}
		});
		if (new File(tgtFileLC).exists()) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					confirm = OpenMessageUtils.openConfirmMessage(MessageFormat.format(
							Messages.getString("dialog.MergeTmxDilog.merge.config.msg"), tgtFileLC));
				}
			});
			if (!confirm) {
				throw new OperationCanceledException();
			}
		}

		MergeTmx mergeTmx = new MergeTmx(fileList, tgtFileLC);
		mergeTmx.beginMerge(new SubProgressMonitor(monitor, 1));
		monitor.done();

	}

	public class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof String) {
				switch (columnIndex) {
				case 0:
					return "" + (fileList.indexOf(String.valueOf(element)) + 1);
				case 1:
					return String.valueOf(element);
				default:
					break;
				}
			}
			return null;
		}
	}

}
