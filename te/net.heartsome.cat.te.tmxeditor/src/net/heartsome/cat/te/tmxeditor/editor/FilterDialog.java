/**
 * FilterDialog.java
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.core.bean.TmxEditorFilterBean;
import net.heartsome.cat.te.core.utils.TmxCustomFilterUtil;
import net.heartsome.cat.te.tmxeditor.Activator;
import net.heartsome.cat.te.tmxeditor.resource.Messages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * 过滤器对话框（自定义过滤器）
 * @author Robert 2013-06-14
 */
public class FilterDialog extends Dialog {
	private TableViewer tableViewer;
	private static final String filterName = "filterName";
	private static final String filterUseable = "filterUseable";

	private final Image checkedImg = Activator.getImageDescriptor("images/checked.gif").createImage();
	private final Image uncheckedImg = Activator.getImageDescriptor("images/unchecked.gif").createImage();
	private TmxCustomFilterUtil filterUtil;
	private List<TmxEditorFilterBean> filterList = new ArrayList<TmxEditorFilterBean>();

	public FilterDialog(Shell parentShell) {
		super(parentShell);
		initFilter();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("tmxeditor.filterdialog.title"));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button okBtn = getButton(IDialogConstants.OK_ID);
		okBtn.setText(Messages.getString("tmxeditor.all.dialog.okBtn"));
		Button cancelBtn = getButton(IDialogConstants.CANCEL_ID);
		cancelBtn.setText(Messages.getString("tmxeditor.all.dialog.cancelBtn"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tParent = (Composite) super.createDialogArea(parent);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parentData.widthHint = 650;
		parentData.heightHint = 400;
		tParent.setLayoutData(parentData);

		// 自定义过滤设置
		Group customGroup = new Group(tParent, SWT.NONE);
		customGroup.setText(Messages.getString("tmxeditor.filterdialog.custom.title"));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(customGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(customGroup);

		Label titleLbl = new Label(customGroup, SWT.NONE);
		titleLbl.setText(Messages.getString("tmxeditor.filterdialog.custom.display.itmes"));

		new Label(customGroup, SWT.NONE);

		// -自定义过滤器设置中的过滤器列表
		tableViewer = new TableViewer(customGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		final Table table = tableViewer.getTable();
		GridData tableData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		tableData.heightHint = 50;
		table.setLayoutData(tableData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] columnNames = new String[] { Messages.getString("tmxeditor.filterdialog.colum.name.disc"),
				Messages.getString("tmxeditor.filterdialog.colum.name.use") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(50);
		}

		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		tableViewer.setInput(filterList);

		CellEditor[] cellEditors = new CellEditor[2];
		cellEditors[0] = null;
		cellEditors[1] = new CheckboxCellEditor(table);
		tableViewer.setColumnProperties(new String[] { filterName, filterUseable });
		tableViewer.setCellEditors(cellEditors);
		tableViewer.setCellModifier(cellModifier);

		tableViewer.refresh();

		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.7, 0.25 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});

		// -自定义过滤器设置中的按扭
		Composite btnCmp = new Composite(customGroup, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.FILL).grab(false, true).applyTo(btnCmp);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(btnCmp);
		Button newBtn = new Button(btnCmp, SWT.NONE);
		newBtn.setText(Messages.getString("tmxeditor.filterdialog.newFilter"));
		setButtonLayoutData(newBtn);
		newBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// UNDO 这里还没有处理
				FilterRegularDialog dialog = new FilterRegularDialog(getShell());
				if (dialog.open() == IDialogConstants.OK_ID) {
					TmxEditorFilterBean bean = dialog.getCurBean();
					if (bean != null) {
						try {
							filterList.add(dialog.getCurBean());
							tableViewer.setInput(filterList);
							tableViewer.refresh();
						} catch (Exception e2) {
							OpenMessageUtils.openMessage(IStatus.WARNING, e2.getMessage());
						}
					}
				}
			}
		});

		final Button editorBtn = new Button(btnCmp, SWT.NONE);
		editorBtn.setText(Messages.getString("tmxeditor.filterdialog.editFilter"));
		setButtonLayoutData(editorBtn);
		editorBtn.setEnabled(false);
		editorBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editorFilter();
			}
		});

		final Button deleteBtn = new Button(btnCmp, SWT.NONE);
		deleteBtn.setText(Messages.getString("tmxeditor.filterdialog.deleteFilter"));
		setButtonLayoutData(deleteBtn);
		deleteBtn.setEnabled(false);
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = tableViewer.getSelection();
				if (!selection.isEmpty() && selection != null && selection instanceof IStructuredSelection) {
					boolean reponse = OpenMessageUtils.openConfirmMessage(Messages.getString("tmxeditor.filterdialog.config.delete.msg"));
					if (!reponse) {
						return;
					}

					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					@SuppressWarnings("unchecked")
					Iterator<TmxEditorFilterBean> iter = structuredSelection.iterator();
					while (iter.hasNext()) {
						Object obj = iter.next();
						if (obj instanceof TmxEditorFilterBean) {
							filterList.remove(obj);
						}
					}
					try {
						tableViewer.setInput(filterList);
						tableViewer.refresh();
					} catch (Exception e2) {
						OpenMessageUtils.openMessage(IStatus.WARNING, e2.getMessage());
					}
				} else {
					OpenMessageUtils.openMessage(IStatus.WARNING, Messages.getString("tmxeditor.filterdialog.warn.delete"));
				}
			}
		});

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (table.getSelectionCount() <= 0) {
					editorBtn.setEnabled(false);
					deleteBtn.setEnabled(false);
				} else if (table.getSelectionCount() == 1) {
					editorBtn.setEnabled(true);
					deleteBtn.setEnabled(true);
				} else {
					editorBtn.setEnabled(false);
					deleteBtn.setEnabled(true);
				}
			}
		});

		// 双击进入编辑模式
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (table.getSelectionCount() == 1) {
					editorFilter();
				}
			}
		});

		return tParent;
	}

	/**
	 * 保存所有数据
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		try {
			filterUtil.saveFilters(filterList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		super.okPressed();
	}

	@Override
	public boolean close() {
		if (checkedImg != null && !checkedImg.isDisposed()) {
			checkedImg.dispose();
		}
		if (uncheckedImg != null && !uncheckedImg.isDisposed()) {
			uncheckedImg.dispose();
		}
		return super.close();
	}

	/**
	 * 修改过滤器
	 */
	private void editorFilter() {
		ISelection selection = tableViewer.getSelection();
		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			@SuppressWarnings("unchecked")
			Iterator<TmxEditorFilterBean> iter = structuredSelection.iterator();
			TmxEditorFilterBean bean = iter.next();

			FilterRegularDialog dialog = new FilterRegularDialog(getShell(), filterUtil, bean);
			int result = dialog.open();
			if (result == IDialogConstants.OK_ID) {
				TmxEditorFilterBean resultBean = dialog.getCurBean();
				String id = resultBean.getId();
				try {
					for (int i = 0; i < filterList.size(); i++) {
						TmxEditorFilterBean filterBean = filterList.get(i);
						if (id.equals(filterBean.getId())) {
							filterList.remove(i);
							filterList.add(i, resultBean);
							break;
						}
					}
					
					tableViewer.setInput(filterList);
					tableViewer.refresh();
					tableViewer.setSelection(new StructuredSelection(resultBean));
				} catch (Exception e) {
					OpenMessageUtils.openMessage(IStatus.WARNING, e.getMessage());
				}
			}
		} else {

		}
	}

	/**
	 * 初始化　过滤器文件处理工具类，即打开　过滤器存储文件，获取相应内容
	 */
	private void initFilter() {
		filterUtil = new TmxCustomFilterUtil();
		filterList = filterUtil.getAllCustomFilters();
		System.out.println(filterList.size());
	}

	/**
	 * tableViewer的标签提供器
	 * @author robert
	 */
	class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 1) {
				if (element instanceof TmxEditorFilterBean) {
					if (((TmxEditorFilterBean) element).isUseable()) {
						return checkedImg;
					} else {
						return uncheckedImg;
					}
				}
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof TmxEditorFilterBean) {
				TmxEditorFilterBean bean = (TmxEditorFilterBean) element;
				switch (columnIndex) {
				case 0:
					return bean.getName();
				default:
					break;
				}

			}
			return null;
		}
	}

	/** cellModifier. */
	private ICellModifier cellModifier = new ICellModifier() {
		public boolean canModify(Object element, String property) {
			return true;
		}

		public Object getValue(Object element, String property) {
			if (filterUseable.equals(property) && (element instanceof TmxEditorFilterBean)) {
				if (element instanceof TmxEditorFilterBean) {
					System.out.println("element.isUseable = " + ((TmxEditorFilterBean) element).isUseable());
					return ((TmxEditorFilterBean) element).isUseable();
				}
			}
			return null;
		}

		public void modify(Object element, String property, Object value) {
			if (filterUseable.equals(property) && (element instanceof TableItem) && (value instanceof Boolean)) {
				TableItem item = (TableItem) element;
				TmxEditorFilterBean bean = (TmxEditorFilterBean) item.getData();
				boolean newValue = (Boolean) value;
				System.out.println(newValue);
				bean.setUseable(newValue);
				tableViewer.refresh();
			}
		}
	};

}
