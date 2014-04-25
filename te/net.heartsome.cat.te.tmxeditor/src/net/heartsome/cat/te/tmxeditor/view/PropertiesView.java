/**
 * PropertiesView.java
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

package net.heartsome.cat.te.tmxeditor.view;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.bean.TmxEditorSelection;
import net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess;
import net.heartsome.cat.te.tmxeditor.Activator;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditor;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.tmxeditor.resource.Messages;
import net.heartsome.cat.te.tmxeditor.view.provider.PropertiesLablerProvider;
import net.heartsome.cat.te.tmxeditor.view.provider.TuPropContentProvider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesView extends ViewPart implements IPartListener {

	public static final String ID = "net.heartsome.cat.te.tmxeditor.editor.TmxPropertiesViewer";

	/** TU 属性. */
	public static final int TU_ATTRS = 0;
	/** TU 的 Prop 子节点. */
	public static final int TU_NODE_PROPS = 1;
	/** TUV 属性. */
	public static final int TUV_ATTRS = 2;
	/** TU 的 Note 子节点. */
	public static final int TU_NODE_NOTE = 3;

	public static final int TU_PROP_COPY = 4;
	/** 当前行. */
	public static final int SELECTED_TU = 1;
	/** 过滤出的TU. */
	public static final int FILTERED_TU = 2;
	/** 所有TU. */
	public static final int ALL_TU = 3;

	public static Logger LOGGER = LoggerFactory.getLogger(PropertiesView.class.getName());

	private int commandFromID = 0;

	private AbstractTmxDataAccess tmxDataAccess;

	private ScrolledComposite scrolledComposite;
	private Composite compostie;
	private Map<Object, TableViewer> tableViewerManager = new HashMap<Object, TableViewer>();

	private AddAction addAction = new AddAction();
	private EditAction editAction = new EditAction();
	private DeleteAction deleteAction = new DeleteAction();

	private TmxEditorOpenCloseListener editorOpenCloseListener = new TmxEditorOpenCloseListener() {

		@Override
		public void editorOpened(AbstractTmxDataAccess dataAccess) {
			setDataAccess(dataAccess);
			setFocus(); // 不调用此方法将法监听到来自 TmxEditorViewer 的选择事件
		}

		@Override
		public void editorClosed() {
			PropertiesView.this.tmxDataAccess = null;
			tableViewerManager.get(TU_ATTRS).setInput(null);
			tableViewerManager.get(TU_NODE_PROPS).setInput(null);
			tableViewerManager.get(TUV_ATTRS).setInput(null);
			tableViewerManager.get(TU_NODE_NOTE).setInput(null);
			commandFromID = 0;
			currentSelected = null;
			setFocus();
		}
	};

	TmxEditorSelection currentSelected;
	private ISelectionListener tmxEditorSelectionListener = new ISelectionListener() {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part == null || selection == null) {
				return;
			}
			if (!(part instanceof TmxEditorViewer)) {
				return;
			}
			if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
				return;
			}
			// tgtLangcodeInTmxEditor = TmxEditorViewer.getInstance().getTmxEditor().getTgtLang();
			StructuredSelection struct = (StructuredSelection) selection;
			Object obj = struct.getFirstElement();
			if (obj instanceof TmxEditorSelection) {
				currentSelected = (TmxEditorSelection) obj;
				tableViewerManager.get(TU_ATTRS).setInput(new TableViewerInput(TU_ATTRS, currentSelected));
				tableViewerManager.get(TUV_ATTRS).setInput(new TableViewerInput(TUV_ATTRS, currentSelected));
				tableViewerManager.get(TU_NODE_NOTE).setInput(null);
				tableViewerManager.get(TU_NODE_NOTE).setInput(new TableViewerInput(TU_NODE_NOTE, currentSelected));
				tableViewerManager.get(TU_NODE_PROPS).setInput(new TableViewerInput(TU_NODE_PROPS, currentSelected));
				compostie.layout();
				scrolledComposite.setMinSize(compostie.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		}
	};

	/**
	 * 获取当前视图对象，此方法只在相关 RCP handler 中调用，不宜在其他地方使用。
	 * @return ;
	 */
	public static PropertiesView getInstance() {
		IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ID);
		if (viewPart != null && viewPart instanceof PropertiesView) {
			return (PropertiesView) viewPart;
		}
		return null;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getPage().addPostSelectionListener(tmxEditorSelectionListener);
		TmxEditorViewer editorViewer = TmxEditorViewer.getInstance();
		if (editorViewer == null) {
			return;
		}
		editorViewer.addOpenCloseListener(editorOpenCloseListener);
		getSite().getPage().addPartListener(this);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		addToolBarAction();
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayout(new FillLayout());
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		compostie = new Composite(scrolledComposite, SWT.NONE);
		compostie.setLayout(new GridLayout(1, false));

		// TU 属性
		TableViewer tbvTu = createTuAttrTable(createMenuManager());
		tableViewerManager.put(TU_ATTRS, tbvTu);

		// TUV 属性
		TableViewer tbvProp = createTuPropTable(createMenuManager());
		tableViewerManager.put(TU_NODE_PROPS, tbvProp);

		// Prop 节点
		TableViewer tbvTuv = createTuvAttrTable(createMenuManager());
		tableViewerManager.put(TUV_ATTRS, tbvTuv);

		// Note 节点
		TableViewer tbvNote = createTuNoteTable(createMenuManager());
		tableViewerManager.put(TU_NODE_NOTE, tbvNote);

		scrolledComposite.setContent(compostie);
		scrolledComposite.setMinSize(compostie.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setGlobalActionHandler();
	}

	private TableViewer createTuAttrTable(MenuManager menuManager) {
		// ui
		Label lblTuProp = new Label(compostie, SWT.NONE);
		lblTuProp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		lblTuProp.setText(Messages.getString("tmxeditor.view.PropertiesView.fixedProp"));
		final TableViewer tbv = new TableViewer(compostie, SWT.NO_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		final TableLayout tableLayout = new TableLayout();
		final Table tbl = tbv.getTable();
		tbl.setLinesVisible(true);
		tbl.setLayout(tableLayout);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		tbl.setLayoutData(gd);
		tableLayout.addColumnData(new ColumnWeightData(4));
		new TableColumn(tbl, SWT.NONE).setResizable(true);
		tableLayout.addColumnData(new ColumnWeightData(7));
		new TableColumn(tbl, SWT.NONE).setResizable(true);
		tbv.setContentProvider(new TuPropContentProvider());

		// data
		tbv.setLabelProvider(new PropertiesLablerProvider());
		tbv.setContentProvider(new TuPropContentProvider());

		Menu menu = menuManager.createContextMenu(tbl);
		tbl.setMenu(menu);
		tbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				commandFromID = TU_ATTRS;
				setCopyPasteEnable();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				commandFromID = TU_ATTRS;
				editAction.run();
			}
		});
		tbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addAction.setEnabled(false);
				editAction.setEnabled(true);
				String[] items = (String[]) tbv.getElementAt(tbl.getSelectionIndex());
				boolean canDelete = !items[1].isEmpty();
				deleteAction.setEnabled(canDelete);
			}
		});
		return tbv;
	}

	private TmxProp currentTuPropSelecet;

	private TableViewer createTuPropTable(MenuManager menuManager) {
		// ui
		Label lblTuProp = new Label(compostie, SWT.NONE);
		lblTuProp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		lblTuProp.setText(Messages.getString("tmxeditor.view.PropertiesView.customProp"));
		final TableViewer tbv = new TableViewer(compostie, SWT.NO_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		final TableLayout tableLayout = new TableLayout();
		final Table tbl = tbv.getTable();
		tbl.setLinesVisible(true);
		tbl.setLayout(tableLayout);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		tbl.setLayoutData(gd);
		tableLayout.addColumnData(new ColumnWeightData(4));
		new TableColumn(tbl, SWT.NONE).setResizable(true);
		tableLayout.addColumnData(new ColumnWeightData(7));
		new TableColumn(tbl, SWT.NONE).setResizable(true);
		tbv.setContentProvider(new TuPropContentProvider());

		// data
		tbv.setLabelProvider(new PropertiesLablerProvider());
		tbv.setContentProvider(new TuPropContentProvider());
		// menu
		menuManager.insertBefore("add", copyTuAttributeAction);
		menuManager.insertAfter("ProperCopyAttribute", pasteTuAttributeAction);
		menuManager.insertAfter("ProperPasteAttribute", new Separator());

		Menu menu = menuManager.createContextMenu(tbl);
		tbl.setMenu(menu);
		tbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				commandFromID = TU_NODE_PROPS;
				setCopyPasteEnable();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				commandFromID = TU_NODE_PROPS;
				Object obj = tbv.getElementAt(tbl.getSelectionIndex());
				if (obj instanceof TmxProp) {
					if (editAction.isEnabled()) {
						editAction.run();
					}
				} else {
					addAction.run();
				}
			}

		});
		tbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object prop = tbv.getElementAt(tbl.getSelectionIndex());
				boolean isSelect = prop instanceof TmxProp;
				if (prop instanceof TmxProp) {
					TmxProp tmxProp = (TmxProp) prop;
					currentTuPropSelecet = tmxProp;
				} else {
					currentTuPropSelecet = null;
				}
				addAction.setEnabled(true);
				editAction.setEnabled(isSelect);
				deleteAction.setEnabled(isSelect);
				
				if (isSelect) {
					boolean disableEdit = false;
					TmxProp p = (TmxProp) prop;
					String name = p.getName();
					String value = p.getValue();
					out : for (Entry<String, TmxTU> entry : currentSelected.getTus().entrySet()) {
						List<TmxProp> props = entry.getValue().getProps();
						if (props == null) {
							disableEdit = true;
							break;
						}
						for (TmxProp testProp : props) {
							if (value.equals(testProp.getValue()) && name.equals(testProp.getName())) {
								continue out;
							}
						}
						disableEdit = true;
						break;
					}
					editAction.setEnabled(!disableEdit);
					deleteAction.setEnabled(!disableEdit);
				}
				
			}
		});
		return tbv;
	}

	private TableViewer createTuvAttrTable(MenuManager menuManager) {
		// ui
		Label lblTuProp = new Label(compostie, SWT.NONE);
		lblTuProp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		lblTuProp.setText(Messages.getString("tmxeditor.view.PropertiesView.tuvProp"));
		final TableViewer tbv = new TableViewer(compostie, SWT.NO_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		final TableLayout tableLayout = new TableLayout();
		final Table tbl = tbv.getTable();
		tbl.setLinesVisible(true);
		tbl.setLayout(tableLayout);
		tbl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		tableLayout.addColumnData(new ColumnWeightData(4));
		new TableColumn(tbl, SWT.NONE).setResizable(true);
		tableLayout.addColumnData(new ColumnWeightData(7));
		new TableColumn(tbl, SWT.NONE).setResizable(true);
		tbv.setContentProvider(new TuPropContentProvider());

		// data
		tbv.setLabelProvider(new PropertiesLablerProvider());
		tbv.setContentProvider(new TuPropContentProvider());

		// menu
		Menu menu = menuManager.createContextMenu(tbl);
		tbl.setMenu(menu);
		tbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				commandFromID = TUV_ATTRS;
				setCopyPasteEnable();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				commandFromID = TUV_ATTRS;
				editAction.run();
			}
		});
		tbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enable = currentSelected.getTus() != null && currentSelected.getTus().size() == 1;
				addAction.setEnabled(false);
				editAction.setEnabled(enable);
				String[] str = (String[]) tbv.getElementAt(tbl.getSelectionIndex());
				boolean deleteEnable = !str[1].isEmpty();
				deleteAction.setEnabled(enable && deleteEnable);
			}
		});
		return tbv;
	}

	private TableViewer createTuNoteTable(MenuManager menuManager) {
		// ui
		Label lblTuProp = new Label(compostie, SWT.NONE);
		lblTuProp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		lblTuProp.setText(Messages.getString("tmxeditor.view.PropertiesView.noteProp"));
		final TableViewer tbv = new TableViewer(compostie, SWT.NO_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		final TableLayout tableLayout = new TableLayout();
		final Table tbl = tbv.getTable();
		tbl.setLinesVisible(true);
		tbl.setLayout(tableLayout);
		tbl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		tableLayout.addColumnData(new ColumnWeightData(4));
		new TableColumn(tbl, SWT.NONE).setResizable(true);
		tableLayout.addColumnData(new ColumnWeightData(7));
		new TableColumn(tbl, SWT.NONE).setResizable(true);
		tbv.setContentProvider(new TuPropContentProvider());

		// data
		tbv.setLabelProvider(new PropertiesLablerProvider(tbl));
		tbv.setContentProvider(new TuPropContentProvider());
		// menu
		Menu menu = menuManager.createContextMenu(tbl);
		tbl.setMenu(menu);
		tbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				commandFromID = TU_NODE_NOTE;
				setCopyPasteEnable();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				commandFromID = TU_NODE_NOTE;
				Object obj = tbv.getElementAt(tbl.getSelectionIndex());
				if (obj instanceof TmxNote) {
					if (editAction.isEnabled()) {
						editAction.run();
					}
				} else {
					if (addAction.isEnabled()) {
						addAction.run();
					}
				}
			}
		});
		tbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object obj = tbv.getElementAt(tbl.getSelectionIndex());
				boolean enable = obj instanceof TmxNote;
				addAction.setEnabled(true);
				editAction.setEnabled(enable);
				deleteAction.setEnabled(enable);
				
				if (enable) {
					boolean disableEdit = false;
					TmxNote note = (TmxNote) obj;
					String content = note.getContent();
					out : for (Entry<String, TmxTU> entry : currentSelected.getTus().entrySet()) {
						List<TmxNote> notes = entry.getValue().getNotes();
						if (notes == null) {
							disableEdit = true;
							break;
						}
						for (TmxNote tn : notes) {
							if (content.equals(tn.getContent())) {
								continue out;
							}
						}
						disableEdit = true;
						break;
					}
					editAction.setEnabled(!disableEdit);
					deleteAction.setEnabled(!disableEdit);
				}
			}
		});
		return tbv;
	}

	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(tmxEditorSelectionListener);
		getSite().getPage().removePartListener(this);
		getViewSite().getActionBars().clearGlobalActionHandlers();
		super.dispose();
	}

	@Override
	public void setFocus() {
		if (compostie != null && !compostie.isDisposed()) {
			compostie.setFocus();
		}
	}

	private MenuManager createMenuManager() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.add(addAction);
		menuMgr.add(editAction);
		menuMgr.add(deleteAction);
		return menuMgr;
	}

	private void addToolBarAction() {
		// 工具栏上添加按钮
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(addAction);
		toolBarManager.add(editAction);
		toolBarManager.add(deleteAction);
	}

	private void setDataAccess(AbstractTmxDataAccess tmxDataAccess) {
		this.tmxDataAccess = tmxDataAccess;
	}

	/**
	 * 数据过多，可能主面板滚动;
	 */
	private void autoFit() {
		compostie.layout();
		scrolledComposite.setMinSize(compostie.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * 刷新属性界面
	 * @param tableViewer
	 *            ;
	 */
	private void updateTableViewer(TableViewer tableViewer) {
		tableViewer.setInput(null);
		TmxTU tu = currentSelected.getDisplayTu();
		List<TmxProp> props = tu.getProps();
		tableViewer.setInput(new TableViewerInput(commandFromID, currentSelected));
	}

	/**
	 * 是否刷新属性界面，
	 * @param isReload
	 *            是否重新装载 tu 缓存;
	 */
	private void reloadAndUpdateUI(boolean isReload) {
		if (isReload) {
			TmxEditor editor = TmxEditorViewer.getInstance().getTmxEditor();
			editor.commit();
			editor.loadDataAndReFreshUI(null, true);
			if (currentSelected != null && currentSelected.getTus() != null) {
				for (Entry<String, TmxTU> entry : currentSelected.getTus().entrySet()) {
					TmxTU tu = tmxDataAccess.getTuByIdentifier(entry.getKey());
					entry.setValue(tu);
				}
			}
		} else {
			TmxEditorViewer.getInstance().getTmxEditor().refreshUI();
		}
		TmxEditorViewer.getInstance().setFocus();
		updateTableViewer(tableViewerManager.get(commandFromID));
	}

	class AddAction extends Action {
		public AddAction() {
			setId("add");
			setText(Messages.getString("tmxeditor.propertiesView.add"));
			setToolTipText(Messages.getString("tmxeditor.propertiesView.add.toolTip"));
			setImageDescriptor(Activator.getImageDescriptor("images/view/addProp.png"));
			setActionDefinitionId("net.heartsome.cat.te.tmxeditor.addproper");
		}

		@Override
		public void run() {
			if (currentSelected == null) {
				return;
			}
			switch (commandFromID) {
			case TU_ATTRS:
			case TUV_ATTRS:
				break;
			case TU_NODE_NOTE: {
				final ElemCollector elemCollector = new ElemCollector();
				addNode(elemCollector, new IModifyByScope() {
					@Override
					public void selectedTu() {
						tmxDataAccess.addTuNote(currentSelected.getTus(), elemCollector.content);
					}

					@Override
					public void filterTu(IProgressMonitor monitor) {
						tmxDataAccess.batchAddTmxNote(monitor, elemCollector.content, null);
					}

					@Override
					public void allTu(IProgressMonitor monitor) {
						tmxDataAccess.batchAddTmxNote(monitor, elemCollector.content, TeCoreConstant.FILTERID_allSeg);
					}
				});
				break;
			}
			case TU_NODE_PROPS: {
				final ElemCollector elemCollector = new ElemCollector();
				addNode(elemCollector, new IModifyByScope() {
					@Override
					public void selectedTu() {
						if (elemCollector.overwrite) {
							tmxDataAccess.deleteTuPropByType(currentSelected.getTus(), elemCollector.name);
						}
						tmxDataAccess.addTuProp(currentSelected.getTus(), elemCollector.name, elemCollector.content);
					}

					@Override
					public void filterTu(IProgressMonitor monitor) {
						if (elemCollector.overwrite) {
							tmxDataAccess.batchDeleteTmxPropByType(monitor, elemCollector.name, null);
						}
						tmxDataAccess.batchAddTmxProp(monitor, elemCollector.name, elemCollector.content, null);
					}

					@Override
					public void allTu(IProgressMonitor monitor) {
						if (elemCollector.overwrite) {
							tmxDataAccess.batchDeleteTmxPropByType(monitor, elemCollector.name,
									TeCoreConstant.FILTERID_allSeg);
						}
						tmxDataAccess.batchAddTmxProp(monitor, elemCollector.name, elemCollector.content,
								TeCoreConstant.FILTERID_allSeg);
					}
				});
				break;
			}
			}
			autoFit();// 调整滚动面板
		}

	}

	public void addNode(final ElemCollector elemCollector, final IModifyByScope cxt) {
		AddElementDialog dialog = new AddElementDialog(getSite().getShell(), commandFromID, elemCollector);
		if (dialog.open() == Dialog.OK) {
			if (elemCollector.scope == ALL_TU) {
				batchTaskWithProgress(new IBatchTask() {
					@Override
					public void run(IProgressMonitor monitor) {
						cxt.allTu(monitor);
					}
				});
			} else if (elemCollector.scope == FILTERED_TU) {
				batchTaskWithProgress(new IBatchTask() {
					@Override
					public void run(IProgressMonitor monitor) {
						cxt.filterTu(monitor);
					}
				});
			} else {
				cxt.selectedTu();
			}
			reloadAndUpdateUI(true);
		}
	}

	public void addPasteNode(final ElemCollector elemCollector, final IModifyByScope cxt) {
		if (elemCollector.scope == ALL_TU) {
			batchTaskWithProgress(new IBatchTask() {
				@Override
				public void run(IProgressMonitor monitor) {
					cxt.allTu(monitor);
				}
			});
		} else if (elemCollector.scope == FILTERED_TU) {
			batchTaskWithProgress(new IBatchTask() {
				@Override
				public void run(IProgressMonitor monitor) {
					cxt.filterTu(monitor);
				}
			});
		} else {
			cxt.selectedTu();
			reloadAndUpdateUI(elemCollector.scope != SELECTED_TU);
		}
	}

	class EditAction extends Action {
		public EditAction() {
			setText(Messages.getString("tmxeditor.propertiesView.eidt"));
			setToolTipText(Messages.getString("tmxeditor.propertiesView.edit.toolTip"));
			setImageDescriptor(Activator.getImageDescriptor("images/view/editProp.png"));
			setActionDefinitionId("net.heartsome.cat.te.tmxeditor.editproper");
		}

		@Override
		public void run() {
			if (currentSelected == null) {
				return;
			}
			final TableViewer tableViewer = tableViewerManager.get(commandFromID);
			if (tableViewer.getSelection().isEmpty()) {
				return;
			}
			final Object selectedElem = tableViewer.getElementAt(tableViewer.getTable().getSelectionIndex());// 获取选中元素
			switch (commandFromID) {
			case TU_ATTRS:
				final AttrCollector attrCollector = new AttrCollector();
				editAttr(selectedElem, attrCollector, new IModifyByScope() {
					@Override
					public void selectedTu() {
						tmxDataAccess.updateTuAttribute(currentSelected.getTus(), attrCollector.name,
								attrCollector.value);
					}

					@Override
					public void filterTu(IProgressMonitor monitor) {
						tmxDataAccess.batchUpdateTuAttr(monitor, attrCollector.name, attrCollector.value, null);
						// tmxDataAccess.updateCacheTuAttr(currentSelected.getTus(), attrCollector.name,
						// attrCollector.value);
					}

					@Override
					public void allTu(IProgressMonitor monitor) {
						tmxDataAccess.batchUpdateTuAttr(monitor, attrCollector.name, attrCollector.value,
								TeCoreConstant.FILTERID_allSeg);
						// tmxDataAccess.updateCacheTuAttr(currentSelected.getTus(), attrCollector.name,
						// attrCollector.value);
					}
				});
				break;
			case TUV_ATTRS:
				TmxTU tu = null;
				TmxSegement seg = null;
				if ((tu = currentSelected.getDisplayTu()) != null) {
					seg = currentSelected.getSelectedColumn() > 1 ? tu.getTarget() : tu.getSource();
				}
				if (tu == null || seg == null) {
					OpenMessageUtils.openMessage(IStatus.ERROR,
							Messages.getString(Messages.getString("tmxeditor.view.PropertiesView.errormsg0")));
					return;
				}
				if (seg.getDbPk() == -1) {
					OpenMessageUtils.openMessage(IStatus.ERROR,
							Messages.getString("tmxeditor.view.PropertiesView.errormsg1"));
					return;
				}
				final AttrCollector attrCollector1 = new AttrCollector();
				final TmxSegement tmpSeg = seg;
				editAttr(selectedElem, attrCollector1, new IModifyByScope() {
					@Override
					public void selectedTu() {
						tmxDataAccess.updateTuvAttribute(currentSelected.getIdentifier(), tmpSeg, attrCollector1.name,
								attrCollector1.value);
					}

					@Override
					public void filterTu(IProgressMonitor monitor) {
						List<String> langs = new LinkedList<String>();
						langs.add(tmpSeg.getLangCode());
						tmxDataAccess.batchUpdateTuvAttr(monitor, attrCollector1.name, attrCollector1.value, langs,
								null);
						tmxDataAccess.updateCacheTuvAttr(tmpSeg, attrCollector1.name, attrCollector1.value);
					}

					@Override
					public void allTu(IProgressMonitor monitor) {
						List<String> langs = new LinkedList<String>();
						langs.add(tmpSeg.getLangCode());
						tmxDataAccess.batchUpdateTuvAttr(monitor, attrCollector1.name, attrCollector1.value, langs,
								TeCoreConstant.FILTERID_allSeg);
						tmxDataAccess.updateCacheTuvAttr(tmpSeg, attrCollector1.name, attrCollector1.value);
					}
				});
				break;
			case TU_NODE_NOTE:
				final ElemCollector elemCollector = new ElemCollector();
				eidtNode(selectedElem, elemCollector, new IModifyByScope() {
					TmxNote note = (TmxNote) selectedElem;

					@Override
					public void selectedTu() {
						tmxDataAccess.updateTuNote(currentSelected.getTus(), note, elemCollector.content);
					}

					@Override
					public void filterTu(IProgressMonitor monitor) {
						tmxDataAccess.batchUpdateTmxNote(monitor, note.getContent(), elemCollector.content, null);
					}

					@Override
					public void allTu(IProgressMonitor monitor) {
						tmxDataAccess.batchUpdateTmxNote(monitor, note.getContent(), elemCollector.content,
								TeCoreConstant.FILTERID_allSeg);
					}
				});
				break;
			case TU_NODE_PROPS:
				final ElemCollector elemCollector1 = new ElemCollector();
				eidtNode(selectedElem, elemCollector1, new IModifyByScope() {
					TmxProp prop = (TmxProp) selectedElem;

					@Override
					public void selectedTu() {
						if (elemCollector1.typeOnly) {
							tmxDataAccess.updateTuPropType(currentSelected.getTus(), prop, elemCollector1.name);
						} else {
							tmxDataAccess.updateTuProp(currentSelected.getTus(), prop, elemCollector1.name,
									elemCollector1.content);
						}
					}

					@Override
					public void filterTu(IProgressMonitor monitor) {
						if (elemCollector1.typeOnly) {
							tmxDataAccess.batchUpdateTmxPropType(monitor, prop, elemCollector1.name, null);
						} else {
							tmxDataAccess.batchUpdateTmxProp(monitor, prop, elemCollector1.name, elemCollector1.content,
									null);
						}
					}

					@Override
					public void allTu(IProgressMonitor monitor) {
						if (elemCollector1.typeOnly) {
							tmxDataAccess.batchUpdateTmxPropType(monitor, prop, elemCollector1.name, TeCoreConstant.FILTERID_allSeg);
						} else {
							tmxDataAccess.batchUpdateTmxProp(monitor, prop, elemCollector1.name, elemCollector1.content,
									TeCoreConstant.FILTERID_allSeg);
						}
					}
				});
			}
		}

		void editAttr(Object selectedElem, AttrCollector attrCollector, final IModifyByScope cxt) {
			final String[] item = (String[]) selectedElem;
			attrCollector.name = item[0];
			attrCollector.value = item[1];
			EditElementAttrDialog dialog = new EditElementAttrDialog(getSite().getShell(), commandFromID, attrCollector);
			if (dialog.open() == Dialog.OK) {
				if (attrCollector.scope == ALL_TU) {
					batchTaskWithProgress(new IBatchTask() {
						@Override
						public void run(IProgressMonitor monitor) {
							cxt.allTu(monitor);
						}
					});
				} else if (attrCollector.scope == FILTERED_TU) {
					batchTaskWithProgress(new IBatchTask() {
						@Override
						public void run(IProgressMonitor monitor) {
							cxt.filterTu(monitor);
						}
					});
				} else {
					cxt.selectedTu();
				}
				reloadAndUpdateUI(attrCollector.scope != SELECTED_TU);
			}
		}

		void eidtNode(Object selectedElem, ElemCollector elemCollector, final IModifyByScope cxt) {
			final TmxNote note = commandFromID == TU_NODE_NOTE ? (TmxNote) selectedElem : null;
			final TmxProp prop = note == null ? (TmxProp) selectedElem : null;
			if (note != null) {
				elemCollector.content = note.getContent();
			} else {
				elemCollector.name = prop.getName();
				elemCollector.content = prop.getValue();
			}
			EditElementDialog dialog = new EditElementDialog(getSite().getShell(), commandFromID, elemCollector);
			if (dialog.open() == Dialog.OK) {
				if (elemCollector.scope == ALL_TU) {
					batchTaskWithProgress(new IBatchTask() {
						@Override
						public void run(IProgressMonitor monitor) {
							cxt.allTu(monitor);
						}
					});
				} else if (elemCollector.scope == FILTERED_TU) {
					batchTaskWithProgress(new IBatchTask() {
						@Override
						public void run(IProgressMonitor monitor) {
							cxt.filterTu(monitor);
						}
					});
				} else {
					cxt.selectedTu();
				}
				reloadAndUpdateUI(elemCollector.scope != SELECTED_TU);
			}
		}
	}

	class DeleteAction extends Action {
		public DeleteAction() {
			setText(Messages.getString("tmxeditor.propertiesView.delete"));
			setToolTipText(Messages.getString("tmxeditor.propertiesView.delet.toolTip"));
			setImageDescriptor(Activator.getImageDescriptor("images/view/deleteProp.png"));
			setActionDefinitionId("net.heartsome.cat.te.tmxeditor.deleteproper");
		}

		@Override
		public void run() {
			if (currentSelected == null) {
				return;
			}
			final TableViewer tableViewer = tableViewerManager.get(commandFromID);
			if (tableViewer.getSelection().isEmpty()) {
				return;
			}
			final Object selectedElem = tableViewer.getElementAt(tableViewer.getTable().getSelectionIndex());

			switch (commandFromID) {
			case TU_ATTRS:
				deleteAttr(new IModifyByScope() {
					String[] item = (String[]) selectedElem;

					@Override
					public void selectedTu() {
						tmxDataAccess.deleteTuAttribute(currentSelected.getTus(), item[0]);
					}

					@Override
					public void filterTu(IProgressMonitor monitor) {
						tmxDataAccess.batchDeleteTuAttr(monitor, item[0], null);
					}

					@Override
					public void allTu(IProgressMonitor monitor) {
						tmxDataAccess.batchDeleteTuAttr(monitor, item[0], TeCoreConstant.FILTERID_allSeg);
					}
				});
				break;
			case TUV_ATTRS:
				for (Entry<String, TmxTU> entry : currentSelected.getTus().entrySet()) {
					final String identifier = entry.getKey();
					final TmxSegement seg = currentSelected.getSelectedColumn() > 1 ? entry.getValue().getTarget()
							: entry.getValue().getSource();

					if (seg == null) {
						return;
					}
					deleteAttr(new IModifyByScope() {
						String[] item = (String[]) selectedElem;

						@Override
						public void selectedTu() {
							tmxDataAccess.deleteTuvAttribute(identifier, seg, item[0]);
						}

						@Override
						public void filterTu(IProgressMonitor monitor) {
							List<String> langs = new LinkedList<String>();
							langs.add(seg.getLangCode());
							tmxDataAccess.batchDeleteTuvAttr(monitor, item[0], langs, null);
						}

						@Override
						public void allTu(IProgressMonitor monitor) {
							List<String> langs = new LinkedList<String>();
							langs.add(seg.getLangCode());
							tmxDataAccess.batchDeleteTuvAttr(monitor, item[0], langs, TeCoreConstant.FILTERID_allSeg);
						}
					});
					break;
				}
				break;
			case TU_NODE_NOTE:
				final TmxNote note = (TmxNote) selectedElem;
				deleteNode(new IModifyByScope() {
					@Override
					public void selectedTu() {
						tmxDataAccess.deleteTuNote(currentSelected.getTus(), note);
					}

					@Override
					public void filterTu(IProgressMonitor monitor) {
						tmxDataAccess.batchDeleteTmxNote(monitor, note.getContent(), null);
					}

					@Override
					public void allTu(IProgressMonitor monitor) {
						tmxDataAccess.batchDeleteTmxNote(monitor, note.getContent(), TeCoreConstant.FILTERID_allSeg);
					}
				});
				break;
			case TU_NODE_PROPS:
				deleteNode(new IModifyByScope() {
					TmxProp prop = (TmxProp) selectedElem;

					@Override
					public void selectedTu() {
						tmxDataAccess.deleteTuProp(currentSelected.getTus(), prop);
					}

					@Override
					public void filterTu(IProgressMonitor monitor) {
						tmxDataAccess.batchDeleteTmxProp(monitor, prop, null);
					}

					@Override
					public void allTu(IProgressMonitor monitor) {
						tmxDataAccess.batchDeleteTmxProp(monitor, prop, TeCoreConstant.FILTERID_allSeg);
					}
				});
			}
		}

		private void deleteAttr(final IModifyByScope dab) {
			final AttrCollector attrCollector = new AttrCollector();
			DeleteConfirmDialog dialog = new DeleteConfirmDialog(getSite().getShell(), commandFromID, attrCollector);
			if (dialog.open() == Dialog.OK) {
				if (attrCollector.scope == ALL_TU) {
					batchTaskWithProgress(new IBatchTask() {
						@Override
						public void run(IProgressMonitor monitor) {
							dab.allTu(monitor);
						}
					});
				} else if (attrCollector.scope == FILTERED_TU) {
					batchTaskWithProgress(new IBatchTask() {
						@Override
						public void run(IProgressMonitor monitor) {
							dab.filterTu(monitor);
						}
					});
				} else {
					dab.selectedTu();
				}
				reloadAndUpdateUI(attrCollector.scope != SELECTED_TU);
			}
		}

		private void deleteNode(final IModifyByScope dab) {
			final ElemCollector elemCollector = new ElemCollector();
			DeleteConfirmDialog dialog = new DeleteConfirmDialog(getSite().getShell(), commandFromID, elemCollector);
			if (dialog.open() == Dialog.OK) {
				if (elemCollector.scope == ALL_TU) {
					batchTaskWithProgress(new IBatchTask() {
						@Override
						public void run(IProgressMonitor monitor) {
							dab.allTu(monitor);
						}
					});
				} else if (elemCollector.scope == FILTERED_TU) {
					batchTaskWithProgress(new IBatchTask() {
						@Override
						public void run(IProgressMonitor monitor) {
							dab.filterTu(monitor);
						}
					});
				} else {
					dab.selectedTu();
				}
				reloadAndUpdateUI(elemCollector.scope != SELECTED_TU);
			}
		}
	}

	public void deleteOverWriteNode(final ElemCollector elemCollector, final IModifyByScope dab) {
		if (elemCollector.scope == ALL_TU) {
			batchTaskWithProgress(new IBatchTask() {
				@Override
				public void run(IProgressMonitor monitor) {
					dab.allTu(monitor);
				}
			});
		} else if (elemCollector.scope == FILTERED_TU) {
			batchTaskWithProgress(new IBatchTask() {
				@Override
				public void run(IProgressMonitor monitor) {
					dab.filterTu(monitor);
				}
			});
		} else {
			dab.selectedTu();
		}
	}

	private interface IModifyByScope {
		void allTu(IProgressMonitor monitor);

		void filterTu(IProgressMonitor monitor);

		void selectedTu();
	}

	/**
	 * 批处理接口
	 * @author Austen
	 * @version
	 * @since JDK1.6
	 */
	private interface IBatchTask {
		void run(IProgressMonitor monitor);
	}

	private void batchTaskWithProgress(final IBatchTask task) {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getSite().getShell());
		try {
			dialog.run(true, false, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					task.run(monitor);
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 属性收集器
	 * @author Austen
	 * @version
	 * @since JDK1.6
	 */
	class AttrCollector {
		/**
		 * 可选值为<br>
		 * PropertiesView.SELECTED_TU，PropertiesView.FILTERED_TU,PropertiesView.ALL_TU
		 */
		int scope;
		String name;
		String value;
	}

	/**
	 * 元素收集器
	 * @author Austen
	 * @version
	 * @since JDK1.6
	 */
	class ElemCollector {
		/**
		 * 可选值为<br>
		 * PropertiesView.SELECTED_TU，PropertiesView.FILTERED_TU,PropertiesView.ALL_TU
		 */
		int scope;
		/** 新增 prop 节点时 覆盖所有. */
		boolean overwrite = false;
		/** 修改 prop 节点时，只修改其“type”. */
		boolean typeOnly = false;
		String name;
		String content;

		@Override
		public String toString() {
			return "[overwrite" + overwrite + "]scope[" + scope + "]";
		}
	}

	/**
	 * tableviewer 输入数据
	 * @author Austen
	 * @version
	 * @since JDK1.6
	 */
	public class TableViewerInput {
		TableViewerInput(int category, TmxEditorSelection selection) {
			this.category = category;
			this.selection = selection;
		}

		/** 填充类型.请使用 PropertiesView.SELECTED_TU，PropertiesView.FILTERED_TU,PropertiesView.ALL_TU */
		public int category;
		public TmxEditorSelection selection;
	}

	@Override
	public void partActivated(IWorkbenchPart part) {

	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {

	}

	@Override
	public void partClosed(IWorkbenchPart part) {

	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		addAction.setEnabled(false);
		editAction.setEnabled(false);
		deleteAction.setEnabled(false);

	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

	private CopyActionHandler copyTuAttributeAction = new CopyActionHandler();
	private PasteActionHandler pasteTuAttributeAction = new PasteActionHandler();

	private class PasteActionHandler extends Action {

		@SuppressWarnings("restriction")
		protected PasteActionHandler() {
			super(WorkbenchMessages.Workbench_paste);
			setId("ProperPasteAttribute");//$NON-NLS-1$
			setActionDefinitionId(ActionFactory.PASTE.getCommandId());
			setImageDescriptor(Activator.getImageDescriptor("images/menu/edit/paste.png"));
			setEnabled(true);
		}

		public void runWithEvent(Event event) {
			final ElemCollector elemCollector = new ElemCollector();
			if (TmxPropTransfer.getIns().getTmxProp() == null) {
				return;
			}
			elemCollector.name = TmxPropTransfer.getIns().getPropName();
			elemCollector.content = TmxPropTransfer.getIns().getPropValue();
			PasteElementDialog dlg = new PasteElementDialog(getSite().getShell(), elemCollector);
			int open = dlg.open();
			if (open == Dialog.OK) {
				// TODO
				if (elemCollector.overwrite) {
					// fisrt delete the prop
					deleteOverWriteNode(elemCollector, new IModifyByScope() {
						TmxProp prop = TmxPropTransfer.getIns().getTmxProp();

						@Override
						public void selectedTu() {
							tmxDataAccess.deleteTuProp(currentSelected.getTus(), prop);
						}

						@Override
						public void filterTu(IProgressMonitor monitor) {
							tmxDataAccess.batchDeleteTmxProp(monitor, prop, null);
						}

						@Override
						public void allTu(IProgressMonitor monitor) {
							tmxDataAccess.batchDeleteTmxProp(monitor, prop, TeCoreConstant.FILTERID_allSeg);
						}
					});
					// secend add the prop content
					addPasteNode(elemCollector, new IModifyByScope() {
						@Override
						public void selectedTu() {
							tmxDataAccess.addTuProp(currentSelected.getTus(), elemCollector.name, elemCollector.content);
						}

						@Override
						public void filterTu(IProgressMonitor monitor) {
							tmxDataAccess.batchAddTmxProp(monitor, elemCollector.name, elemCollector.content, null);
						}

						@Override
						public void allTu(IProgressMonitor monitor) {
							tmxDataAccess.batchAddTmxProp(monitor, elemCollector.name, elemCollector.content,
									TeCoreConstant.FILTERID_allSeg);
						}
					});

				} else {
					addPasteNode(elemCollector, new IModifyByScope() {
						@Override
						public void selectedTu() {
							tmxDataAccess.addTuProp(currentSelected.getTus(), elemCollector.name, elemCollector.content);
						}

						@Override
						public void filterTu(IProgressMonitor monitor) {
							tmxDataAccess.batchAddTmxProp(monitor, elemCollector.name, elemCollector.content, null);
						}

						@Override
						public void allTu(IProgressMonitor monitor) {
							tmxDataAccess.batchAddTmxProp(monitor, elemCollector.name, elemCollector.content,
									TeCoreConstant.FILTERID_allSeg);
						}
					});

				}
				reloadAndUpdateUI(elemCollector.scope != SELECTED_TU);
			}

		}

		/**
		 * Update state.
		 */
		public void updateEnabledState() {
			setEnabled(TmxPropTransfer.getIns().getTmxProp() != null);
		}
	}

	@SuppressWarnings("restriction")
	private class CopyActionHandler extends Action {
		protected CopyActionHandler() {
			super(WorkbenchMessages.Workbench_copy);
			setId("ProperCopyAttribute");//$NON-NLS-1$
			setEnabled(true);
			setImageDescriptor(Activator.getImageDescriptor("images/menu/edit/copy.png"));
			setActionDefinitionId(ActionFactory.COPY.getCommandId());
		}

		public void runWithEvent(Event event) {
			TmxPropTransfer.getIns().setTmxProp(currentTuPropSelecet);
		}

		/**
		 * Update the state.
		 */
		public void updateEnabledState() {
			setEnabled(currentTuPropSelecet != null);
		}
	}

	protected void setGlobalActionHandler() {
		getViewSite().getActionBars().setGlobalActionHandler("net.heartsome.cat.te.tmxeditor.addproper", addAction);
		getViewSite().getActionBars().setGlobalActionHandler("net.heartsome.cat.te.tmxeditor.editproper", editAction);
		getViewSite().getActionBars().setGlobalActionHandler("net.heartsome.cat.te.tmxeditor.deleteproper",
				deleteAction);
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), copyTuAttributeAction);
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteTuAttributeAction);
	}

	private void setCopyPasteEnable() {
		if (commandFromID == TU_NODE_PROPS) {
			copyTuAttributeAction.updateEnabledState();
			pasteTuAttributeAction.updateEnabledState();
		} else {
			copyTuAttributeAction.setEnabled(false);
			pasteTuAttributeAction.setEnabled(false);
		}
	}

	static class TmxPropTransfer {

		private static final TmxPropTransfer transfer = new TmxPropTransfer();

		private TmxProp tmxProp;

		private TmxPropTransfer() {

		}

		public static TmxPropTransfer getIns() {
			return transfer;
		}

		public void setTmxProp(TmxProp tmxProp) {
			if (null == tmxProp) {
				return;
			} else if (tmxProp == this.tmxProp) {
				return;
			}
			this.tmxProp = tmxProp;
		}

		public String getPropName() {
			if (null != tmxProp) {
				return tmxProp.getName();
			}
			return null;
		}

		public String getPropValue() {
			if (null != tmxProp) {
				return tmxProp.getValue();
			}
			return null;
		}

		public TmxProp getTmxProp() {
			return this.tmxProp;
		}
	}
}
