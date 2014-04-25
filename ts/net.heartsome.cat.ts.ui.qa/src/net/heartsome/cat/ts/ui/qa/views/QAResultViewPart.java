package net.heartsome.cat.ts.ui.qa.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.model.QAResultBean;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.util.MultiFilesOper;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 品质检查结果视图
 * @author robert 2011-11-12
 */
public class QAResultViewPart extends ViewPart implements PropertyChangeListener {

	/** 常量，视图ID。 */
	public static final String ID = "net.heartsome.cat.ts.ui.qa.views.QAResultViewPart";
	private Composite parent;
	private TableViewer tableViewer;
	private Table table;
	private QAResult qaResult;
	private IWorkspaceRoot root;
	private IWorkbenchWindow window;
	/** 列表中所显示的数据，这个是用来排序的，其值随列表数据删除时删除 */
	private List<QAResultBean> inputData = new ArrayList<QAResultBean>();
	private Image errorImg;
	private Image warningImg;
	private boolean isMultiFile;
	private MultiFilesOper oper;
	private final static String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
	public final static Logger logger = LoggerFactory.getLogger(QAResultViewPart.class.getName());
	/** 标识当前品质检查结果视图所处理的文件路径的集合 */
	private List<String> filePathList = null;

	private Image deleteImage = Activator.getImageDescriptor("images/delete.png").createImage();
	
	public QAResultViewPart() {
		errorImg = Activator.getImageDescriptor("icons/error.png").createImage();
		warningImg = Activator.getImageDescriptor("icons/warning.png").createImage();
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		root = ResourcesPlugin.getWorkspace().getRoot();
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		createTable();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		if (qaResult != null) {
			qaResult.listeners.removePropertyChangeListener(this);
		}
		if(errorImg != null && !errorImg.isDisposed()){
			errorImg.dispose();
		}
		if(warningImg != null && !warningImg.isDisposed()){
			warningImg.dispose();
		}
		if (deleteImage != null && !deleteImage.isDisposed()) {
			deleteImage.dispose();
		}
		super.dispose();
	}

	/** labelProvider. */
	private ITableLabelProvider labelProvider = new ITableLabelProvider() {
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				if (element instanceof QAResultBean) {
					QAResultBean bean = (QAResultBean) element;
					if (0 == bean.getTipLevel()) {
						return errorImg;
					} else if (1 == bean.getTipLevel()) {
						return warningImg;
					}
				}
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof QAResultBean) {
				QAResultBean bean = (QAResultBean) element;
				switch (columnIndex) {
				case 0:
					return "";
				case 1:
					return bean.getLineNumber();
				case 2:
					return bean.getQaType();
				case 3:
					return bean.getErrorTip();
				case 4:
					return bean.getResource();
				case 5:
					return bean.getLangPair();
				default:
					return "";
				}
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {

		}

		public void dispose() {

		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {

		}

	};

	public void createTable() {

		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		table = tableViewer.getTable();
		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(tableData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer.setLabelProvider(labelProvider);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(inputData);
		// tableViewer.setSorter(new QASorter());

		String[] columnNames = new String[] { Messages.getString("qa.views.QAResultViewPart.columnTipLevel"),
				Messages.getString("qa.views.QAResultViewPart.columnErrorLine"),
				Messages.getString("qa.views.QAResultViewPart.columnQAType"),
				Messages.getString("qa.views.QAResultViewPart.columnErrorTip"),
				Messages.getString("qa.views.QAResultViewPart.columnFilePath"),
				Messages.getString("qa.views.QAResultViewPart.columnLangPair") };

		// 第0列为错误级别
		TableColumn tipLevelColumn = new TableColumn(table, SWT.BOTTOM | SWT.CENTER);
		tipLevelColumn.setText(columnNames[0]);
		// tipLevelColumn.setAlignment(SWT.RIGHT_TO_LEFT);
		tipLevelColumn.addSelectionListener(new SelectionAdapter() {
			boolean asc = true;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(asc ? QASorter.tipLevel_ASC : QASorter.tipLevel_DESC);
				asc = !asc;
			}
		});

		// 第一列为行号
		TableColumn lineNumberColumn = new TableColumn(table, SWT.LEFT);
		lineNumberColumn.setText(columnNames[1]);
		lineNumberColumn.addSelectionListener(new SelectionAdapter() {
			boolean asc = true;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(asc ? QASorter.lineNumber_ASC : QASorter.lineNumber_DESC);
				asc = !asc;
			}
		});

		// 第二列为检查类型
		TableColumn qaTypeColumn = new TableColumn(table, SWT.LEFT);
		qaTypeColumn.setText(columnNames[2]);
		qaTypeColumn.addSelectionListener(new SelectionAdapter() {
			boolean asc = true;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(asc ? QASorter.qaType_ASC : QASorter.qaType_DESC);
				asc = !asc;
			}
		});

		// 第三行为错误描述，不添加排序功能
		TableColumn qaTipColumn = new TableColumn(table, SWT.LEFT);
		qaTipColumn.setText(columnNames[3]);

		// 第四行为资源(路径)
		TableColumn resourceCln = new TableColumn(table, SWT.LEFT);
		resourceCln.setText(columnNames[4]);
		resourceCln.addSelectionListener(new SelectionAdapter() {
			boolean asc = true;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(asc ? QASorter.resource_ASC : QASorter.resource_DESC);
				asc = !asc;
			}
		});

		// 第五列，语言对
		TableColumn langPairCln = new TableColumn(table, SWT.LEFT);
		langPairCln.setText(columnNames[5]);
		langPairCln.addSelectionListener(new SelectionAdapter() {
			boolean asc = true;

			@Override
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(asc ? QASorter.langPair_ASC : QASorter.langPair_DESC);
				asc = !asc;
			}
		});
		// 初始化时默认以行号进行排序
		tableViewer.setSorter(QASorter.lineNumber_ASC);

		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.08, 0.08, 0.14, 0.36, 0.2, 0.13 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});

		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				locationRow();
			}
		});

		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == QAConstant.QA_CENTERKEY_1 || e.keyCode == QAConstant.QA_CENTERKEY_2) {
					locationRow();
				}
			}
		});

		createPropMenu();

	}
	
	

	/**
	 * 创建右键参数
	 */
	private void createPropMenu() {
		Menu propMenu = new Menu(table);
		table.setMenu(propMenu);

		MenuItem deletWarnItem = new MenuItem(propMenu, SWT.NONE);
		deletWarnItem.setText(Messages.getString("views.QAResultViewPart.deletWarnItem"));
		deletWarnItem.setImage(deleteImage);
		deletWarnItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				QAResultBean bean;
				for (int i = 0; i < inputData.size(); i++) {
					bean = inputData.get(i);
					// 0为错误，1为警告
					if (1 == bean.getTipLevel()) {
						inputData.remove(bean);
						i--;
					}
				}
				tableViewer.refresh();
			}
		});

		MenuItem deleteAllItem = new MenuItem(propMenu, SWT.NONE);
		deleteAllItem.setText(Messages.getString("views.QAResultViewPart.deleteAllItem"));
		deleteAllItem.setImage(deleteImage);
		deleteAllItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inputData.clear();
				tableViewer.refresh();
			}
		});
		
		propMenu.addDisposeListener(new DisposeListener() {
			
			public void widgetDisposed(DisposeEvent e) {
				if(deleteImage!= null && !deleteImage.isDisposed()){
					deleteImage.dispose();
				}
			}
		});
		
//		MenuItem ignoreSpellItem = new MenuItem(propMenu, SWT.NONE);
//		ignoreSpellItem.setText(Messages.getString("views.QAResultViewPart.ignoreSpellItem"));
	}

	/**
	 * 将品质检查的结果传到这里来
	 */
	public void setTableData(final String[] qaResultData) {
		try {
			Display.getCurrent().asyncExec(new Runnable() {
				public void run() {
					TableItem ti = new TableItem(table, 0);
					ti.setText(qaResultData);
					// ti.setImage(index, image);
					table.update();
					// tableViewer.setInput(qaResultData);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.views.QAResultViewPart.log1"), e);
		}
	}

	public void propertyChange(final PropertyChangeEvent evt) {
		/*
		 * 备注，传过来的数据是一个 ArrayList<QAResultBean>, 每组数据都是相同的 rowId
		 */
		if ("printData".equals(evt.getPropertyName())) {
			try {
				Display.getDefault().syncExec(new Runnable() {
					@SuppressWarnings("unchecked")
					public void run() {
						Object obj = evt.getNewValue();
						if (obj instanceof List) {
							List<QAResultBean> objList = (List<QAResultBean>) obj;
							if (objList.size() <= 0) {
								return;
							}
							
//							StringBuffer sb = new StringBuffer();
//							sb.append("是否是自动检查　＝　" + qaResult.isAutoQA());
//							sb.append("\n");
//							sb.append("是否处理同一对象 = " + qaResult.isSameOperObjForAuto());
//							MessageDialog.openInformation(getSite().getShell(), "用于测试", sb.toString());
							
							
							String rowId = objList.get(0).getRowId();
							// 如果是自动检查。那么要删除之前的记录
							if (qaResult.isAutoQA()) {
								if (qaResult.isSameOperObjForAuto()) {
									for(int i = 0; i < inputData.size(); i ++){
										QAResultBean bean = inputData.get(i);
										if (rowId.equals(bean.getRowId())) {
											inputData.remove(bean);
											i --;
										}
									}
								}else {
//									MessageDialog.openInformation(getSite().getShell(), "通知", "这里要清空数据 + filePathList.length = " + filePathList.size());
									inputData.clear();
									tableViewer.refresh();
									
									filePathList = qaResult.getFilePathList();
									qaResult.setSameOperObjForAuto(true);
								}
							}
							inputData.addAll(objList);
							tableViewer.refresh();
							
							if (qaResult.isAutoQA()) {
								tableViewer.setSelection(new StructuredSelection(objList));
							}
						}else if (obj instanceof String) {
							// 这是针对自动品质检查，若一个文本段没有错误，那么就将这个文本段之前的提示进行清除
							if (qaResult.isAutoQA()) {
								if (qaResult.isSameOperObjForAuto()) {
									String rowId = (String) obj;
									for(int i = 0; i < inputData.size(); i ++){
										QAResultBean bean = inputData.get(i);
										if (rowId.equals(bean.getRowId())) {
											inputData.remove(bean);
											i --;
										}
									}
								}else {
									inputData.clear();
									tableViewer.refresh();
									
									filePathList = qaResult.getFilePathList();
									qaResult.setSameOperObjForAuto(true);
								}
							}
							
							tableViewer.refresh();
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.views.QAResultViewPart.log1"), e);
			}
		} else if ("isMultiFiles".equals(evt.getPropertyName())) {
			try {
				Display.getCurrent().syncExec(new Runnable() {
					public void run() {
						isMultiFile = (Boolean) ((Object[]) evt.getNewValue())[0];
						if (isMultiFile) {
							oper = (MultiFilesOper) ((Object[]) evt.getNewValue())[1];
						}else {
							oper = null;
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.views.QAResultViewPart.log1"), e);
			}
		}
	}

	public void registLister(QAResult qaResult) {
		this.qaResult = qaResult;
		this.qaResult.listeners.addPropertyChangeListener(this);
		
		if (filePathList != null && filePathList.size() > 0) {
			// 自动品质检查这里是不能保存相关信息的
			if (!qaResult.isAutoQA()) {
				filePathList = this.qaResult.getFilePathList();
			}else {
				boolean isSameOperObj = true;
				List<String> curFilePathList = this.qaResult.getFilePathList();
				if (curFilePathList.size() == filePathList.size()) {
					for(String filePath : filePathList){
						if (curFilePathList.contains(filePath)) {
							curFilePathList.remove(filePath);
						}else {
							isSameOperObj = false;
							break;
						}
					}
				}else {
					isSameOperObj = false;
				}
				this.qaResult.setSameOperObjForAuto(isSameOperObj);
			}
		}else {
			filePathList = this.qaResult.getFilePathList();
		}
	}

	/**
	 * 双击或按回车键，将品质检查结果中的数据定位到翻译界面上去。
	 */
	public void locationRow() {
		TableItem[] items = table.getSelection();
		if (items.length <= 0) {
			return;
		}
		// 获取第一行选择的值
		TableItem item = items[0];
		String fileFullPath = item.getText(4);
		// 如果是合并打开的文件
		if (isMultiFile) {
			IXliffEditor xliffEditor = openMultiFilesEditor();
			if (xliffEditor == null) {
				return;
			}
			String lineNumber = item.getText(1);
			// 跳转到错误行
			xliffEditor.setFocus();
			xliffEditor.jumpToRow(Integer.parseInt(lineNumber) - 1, true);
			return;
		} else {
			// 检查该文件是否已经打开，如果没有打开，就在界面上打开,再返回这个
			IXliffEditor xliffEditor = openEditor(fileFullPath);
			if (xliffEditor == null) {
				return;
			}
			String lineNumber = item.getText(1);
			// 跳转到错误行
			xliffEditor.setFocus();
			xliffEditor.jumpToRow(Integer.parseInt(lineNumber) - 1, false);
		}
	}

	public IXliffEditor openEditor(String fileFullPath) {
		IFile ifile = root.getFileForLocation(root.getLocation().append(fileFullPath));
		FileEditorInput fileInput = new FileEditorInput(ifile);

		IEditorReference[] editorRefer = window.getActivePage().findEditors(fileInput, XLIFF_EDITOR_ID,
				IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);

		IEditorPart editorPart = null;

		IXliffEditor xliffEditor = null;
		if (editorRefer.length >= 1) {
			editorPart = editorRefer[0].getEditor(true);
			xliffEditor = (IXliffEditor) editorPart;
			// 若该文件未激活，激活此文件
			if (window.getActivePage().getActiveEditor() != editorPart) {
				window.getActivePage().activate(editorPart);
			}
			// 对于已经打开过的文件，进行重排序
			xliffEditor.resetOrder();
		} else { // 如果文件没有打开，那么先打开文件
			try {
				if(!validateXliffCanOpen(ifile)){
					return null;
				}
				xliffEditor = (IXliffEditor) window.getActivePage().openEditor(fileInput, XLIFF_EDITOR_ID, true,
						IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
			} catch (PartInitException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.views.QAResultViewPart.log2"), e);
			}
		}

		return xliffEditor;
	}

	/**
	 * 处理合并打开文件 nattable editor的相关问题
	 * @return ;
	 */
	public IXliffEditor openMultiFilesEditor() {
		IXliffEditor xliffEditor = null;
		FileEditorInput fileInput = new FileEditorInput(oper.getCurMultiTempFile());

		IEditorReference[] editorRefer = window.getActivePage().findEditors(fileInput, XLIFF_EDITOR_ID,
				IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);

		IEditorPart editorPart = null;

		if (editorRefer.length >= 1) {
			editorPart = editorRefer[0].getEditor(true);
			xliffEditor = (IXliffEditor) editorPart;
			// 若该文件未激活，激活此文件
			if (window.getActivePage().getActiveEditor() != editorPart) {
				window.getActivePage().activate(editorPart);
			}
			// 对于已经打开过的文件，进行重排序
			xliffEditor.resetOrder();
		} else { // 如果文件没有打开，那么先打开文件
			try {
				// 如果保存合并打开所有信息的临时文件已经被删除，那么，重新生成临时文件
				if (!oper.getCurMultiTempFile().getLocation().toFile().exists()) {
					// 检查这两个文件是否重新进行合并打开了的。
					IFile findMultiTempIfile = oper.getMultiFilesTempIFile(true);
					if (findMultiTempIfile != null) {
						fileInput = new FileEditorInput(findMultiTempIfile);
						oper.setCurMultiTempFile(findMultiTempIfile);
					} else {
						//先验证这些所处理的文件是否有已经被打开的
						List<IFile> openedFileList = oper.getOpenedIfile();
						if (openedFileList.size() > 0) {
							String openFileStr = "";
							for(IFile ifile : openedFileList){
								openFileStr += "\t" + ifile.getFullPath().toOSString() + "\n";
							}
							MessageDialog.openInformation(getSite().getShell(), Messages.getString("views.QAResultViewPart.msgTitle"), 
									MessageFormat.format(Messages.getString("qa.views.QAResultViewPart.addTip1"), openFileStr));
							return null;
						}
						
						// 如果选中的文件没有合并打开，那么就重新打开它们
						IFile multiIFile = oper.createMultiTempFile();
						if (multiIFile != null && multiIFile.exists()) {
							fileInput = new FileEditorInput(multiIFile);
							oper.setCurMultiTempFile(multiIFile);
						} else {
							MessageDialog.openInformation(getSite().getShell(),
									Messages.getString("views.QAResultViewPart.msgTitle"),
									Messages.getString("views.QAResultViewPart.msg1"));
							return null;
						}
						
						xliffEditor = (IXliffEditor) window.getActivePage().openEditor(fileInput, XLIFF_EDITOR_ID, true,
								IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
					}
					
				}
			} catch (PartInitException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.views.QAResultViewPart.log2"), e);
			}
		}

		return xliffEditor;
	}

	/**
	 * 验证当前要单个打开的文件是否已经被合并打开，针对单个文件的品质检查点击结果进行定位
	 * @return
	 */
	public boolean validateXliffCanOpen(IFile iFile){
		IEditorReference[] editorRes = window.getActivePage().getEditorReferences();
		for (int i = 0; i < editorRes.length; i++) {
			IXliffEditor editor = (IXliffEditor) editorRes[i].getEditor(true);
			if (editor.isMultiFile()) {
				if (editor.getMultiFileList().indexOf(iFile.getLocation().toFile()) != -1) {
					MessageDialog.openInformation(getSite().getShell(), Messages.getString("views.QAResultViewPart.msgTitle"), 
						MessageFormat.format(Messages.getString("qa.views.QAResultViewPart.addTip2"), iFile.getFullPath().toOSString()));
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 品质检查显示数据的列表排序类
	 * @author robert
	 * @version
	 * @since JDK1.6
	 */
	static class QASorter extends ViewerSorter {
		private static final int tipLevel_ID = 1;
		private static final int lineNumber_ID = 2; // 错语行号
		private static final int qaType_ID = 3; // 检查类型
		private static final int resource_ID = 5; // 第四列错语描述不技持排序，这是第五列，路径
		private static final int langPair_ID = 6; // 第六列，语言对

		public static final QASorter tipLevel_ASC = new QASorter(tipLevel_ID);
		public static final QASorter tipLevel_DESC = new QASorter(-tipLevel_ID);

		public static final QASorter lineNumber_ASC = new QASorter(lineNumber_ID);
		public static final QASorter lineNumber_DESC = new QASorter(-lineNumber_ID);

		public static final QASorter qaType_ASC = new QASorter(qaType_ID);
		public static final QASorter qaType_DESC = new QASorter(-qaType_ID);

		public static final QASorter resource_ASC = new QASorter(resource_ID);
		public static final QASorter resource_DESC = new QASorter(-resource_ID);

		public static final QASorter langPair_ASC = new QASorter(langPair_ID);
		public static final QASorter langPair_DESC = new QASorter(-langPair_ID);

		private int sortType;

		private QASorter(int sortType) {
			this.sortType = sortType;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			QAResultBean bean1 = (QAResultBean) e1;
			QAResultBean bean2 = (QAResultBean) e2;
			switch (sortType) {
			case tipLevel_ID: {
				int lineNumber1 = bean1.getTipLevel();
				int lineNumber2 = bean2.getTipLevel();
				return lineNumber1 > lineNumber2 ? 1 : -1;
			}
			case -tipLevel_ID: {
				int lineNumber1 = bean1.getTipLevel();
				int lineNumber2 = bean2.getTipLevel();
				return lineNumber1 > lineNumber2 ? -1 : 1;
			}
			case lineNumber_ID: {
				int lineNumber1 = Integer.parseInt(bean1.getLineNumber());
				int lineNumber2 = Integer.parseInt(bean2.getLineNumber());
				return lineNumber1 > lineNumber2 ? 1 : -1;
			}
			case -lineNumber_ID: {
				int lineNumber1 = Integer.parseInt(bean1.getLineNumber());
				int lineNumber2 = Integer.parseInt(bean2.getLineNumber());
				return lineNumber1 > lineNumber2 ? -1 : 1;
			}

			case qaType_ID: {
				String qaType1 = bean1.getQaType();
				String qaType2 = bean2.getQaType();
				return qaType1.compareToIgnoreCase(qaType2);
			}
			case -qaType_ID: {
				String qaType1 = bean1.getQaType();
				String qaType2 = bean2.getQaType();
				return qaType2.compareToIgnoreCase(qaType1);
			}

			case resource_ID: {
				String resource1 = bean1.getResource();
				String resource2 = bean2.getResource();
				return resource1.compareToIgnoreCase(resource2);
			}
			case -resource_ID: {
				String resource1 = bean1.getResource();
				String resource2 = bean2.getResource();
				return resource2.compareToIgnoreCase(resource1);
			}

			case langPair_ID: {
				String langPair1 = bean1.getLangPair();
				String langPair2 = bean2.getLangPair();
				return langPair1.compareToIgnoreCase(langPair2);
			}
			case -langPair_ID: {
				String langPair1 = bean1.getLangPair();
				String langPair2 = bean2.getLangPair();
				return langPair2.compareToIgnoreCase(langPair1);
			}
			}
			return 0;
		}
	}

	/**
	 * 清除结果显示视图的列表中的数据
	 */
	public void clearTableData() {
		inputData.clear();
		table.removeAll();
	}
	
	
}
