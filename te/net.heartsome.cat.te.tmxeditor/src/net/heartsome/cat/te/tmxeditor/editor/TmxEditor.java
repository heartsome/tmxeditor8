/**
 * TmxEditor.java
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

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.bean.TmxEditorFilterBean;
import net.heartsome.cat.te.core.bean.TmxPropertiesBean;
import net.heartsome.cat.te.core.qa.QACommonFuction;
import net.heartsome.cat.te.core.qa.QAConstant;
import net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess;
import net.heartsome.cat.te.core.tmxdata.DatabaseDataAccess;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileDataAccess;
import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable;
import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.CellEditor;
import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.TeActiveCellEditor;
import net.heartsome.cat.te.tmxeditor.editor.nattable.search.FindReplaceDialog;
import net.heartsome.cat.te.tmxeditor.resource.Messages;
import net.sourceforge.nattable.edit.editor.ICellEditor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.ActionFactory;

/**
 * TmxEditor 编辑器的　viewer 层，编辑器的接口点。包括界面的初始化，　nattable 的创建等。
 * @author Jason, Robert
 * @version
 * @since JDK1.6
 */
public class TmxEditor {

	private Composite parentComposite;
	private AbstractTmxDataAccess tmxDataAccess;
	/** 当前源语言 */
	private String srcLangCode;
	/** 当前目标语言 */
	private String tgtLangCode;

	private TmxEditorFilter editorFilter;
	private Combo srcLangCombo;
	private Combo tgtLangCombo;
	private Text srcSearchText;
	private Text tgtSearchText;
	private String tgtSearchTitle = Messages.getString("tmxeditor.filter.targetSearchText.label");
	private String srcSearchTitle = Messages.getString("tmxeditor.filter.sourceSearchText.label");

	private TmxEditorImpWithNattable tmxEditorImpWithNattable;
	private TmxEditorViewer viewPart;

	private FindReplaceAction findAction = new FindReplaceAction();
	private FindReplaceDialog dialog;

	private KeyListener searchKeyListener = new KeyListener() {

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.stateMask == SWT.NONE && e.keyCode == SWT.CR) {
				commit();
				editorFilter.comboViewer.getCombo().select(0);
				beginToSeach();
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}
	};

	public TmxEditor(AbstractTmxDataAccess tmxDataAccess) {
		this.tmxDataAccess = tmxDataAccess;
		this.srcLangCode = tmxDataAccess.getCurrSrcLang();
		this.tgtLangCode = tmxDataAccess.getCurrTgtLang();
	}

	public void createContent(TmxEditorViewer viewPart, Composite container) {
		this.parentComposite = container;
		this.viewPart = viewPart;
		GridLayout containerGdLt = new GridLayout(1, true);
		containerGdLt.marginWidth = 0;
		containerGdLt.marginHeight = 0;
		containerGdLt.verticalSpacing = 5;
		containerGdLt.marginTop = 0;
		containerGdLt.marginLeft = 0;
		containerGdLt.marginRight = 0;
		container.setLayout(containerGdLt);

		// tab 设置，分为数据查询以及品质检查
		TabFolder tab = new TabFolder(container, SWT.NONE);
		tab.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		TabItem editorItm = new TabItem(tab, SWT.NONE);
		editorItm.setText(Messages.getString("tmxeditor.filter.editor"));

		TabItem qaItm = new TabItem(tab, SWT.NONE);
		qaItm.setText(Messages.getString("tmxeditor.filter.qa"));

		Composite editorCmp = new Composite(tab, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editorCmp);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(editorCmp);
		createEditorArea(editorCmp);
		editorItm.setControl(editorCmp);

		Composite qaCmp = new Composite(tab, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(qaCmp);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(qaCmp);
		createQaArea(qaCmp);
		qaItm.setControl(qaCmp);

		// create nattable composite
		Composite nattableComposite = new Composite(container, SWT.NONE);
		nattableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		createNattable(nattableComposite, srcLangCode, tgtLangCode);
	}

	private void createEditorArea(Composite editorCmp) {
		Group searchGroup = new Group(editorCmp, SWT.NONE);
		searchGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		searchGroup.setText(Messages.getString("tmxeditor.fitler.searchGroup"));
		searchGroup.setLayout(new GridLayout(4, false));

		// create language
		createLangUI(searchGroup);

		Group filterGroup = new Group(editorCmp, SWT.NONE);
		filterGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		filterGroup.setText(Messages.getString("tmxeditor.filter.fitlerGroup"));
		GridLayout filterGroupGl = new GridLayout(1, false);
		filterGroupGl.marginWidth = 0;
		filterGroupGl.marginHeight = 0;
		filterGroup.setLayout(filterGroupGl);

		// create Filter
		createTmxFilter(filterGroup);
	}

	/**
	 * 品质检查选项面板 --robert 2013-09-17
	 * @param qaCmp
	 *            ;
	 */
	private void createQaArea(Composite qaCmp) {
		final IPreferenceStore store = net.heartsome.cat.te.core.Activator.getDefault().getPreferenceStore();

		// >左边
		Group itemsGroup = new Group(qaCmp, SWT.NONE);
		itemsGroup.setText(Messages.getString("tmxeditor.QA.itemGroupTitle"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(itemsGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(itemsGroup);

		// >>第一层　composite
		Composite qaItemCmp = new Composite(itemsGroup, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(qaItemCmp);

		GridLayoutFactory.fillDefaults().numColumns(3).spacing(SWT.DEFAULT, LayoutConstants.getSpacing().y - 4).margins(0, 0).applyTo(qaItemCmp);
		// >>>第一层　第一行
		final Button tagConsistenceBtn = new Button(qaItemCmp, SWT.CHECK);
		tagConsistenceBtn.setText(QAConstant.QA_TagConsistenceText);

		final Button numberConsistenceBtn = new Button(qaItemCmp, SWT.CHECK);
		numberConsistenceBtn.setText(QAConstant.QA_NumberConsistenceText);

		final Button spaceOfParaCheckBtn = new Button(qaItemCmp, SWT.CHECK);
		spaceOfParaCheckBtn.setText(QAConstant.QA_SpaceOfParaCheckText);

		// >>>第一层　第二行
		final Button tgtNullBtn = new Button(qaItemCmp, SWT.CHECK);
		tgtNullBtn.setText(QAConstant.QA_TgtNullText);

		final Button srcSameButTgtBtn = new Button(qaItemCmp, SWT.CHECK);
		srcSameButTgtBtn.setText(QAConstant.QA_SrcSameButTgtText);

		final Button tgtSameButSrcBtn = new Button(qaItemCmp, SWT.CHECK);
		tgtSameButSrcBtn.setText(QAConstant.QA_TgtSameButSrcText);
		
		// >>>第一层　第三行
		final Button srcSameWithTgtBtn = new Button(qaItemCmp, SWT.CHECK);
		srcSameWithTgtBtn.setText(QAConstant.QA_SrcSameWithTgtText);
		
		new Label(qaItemCmp, SWT.NONE);
		new Label(qaItemCmp, SWT.NONE);

		// >>第二层
		Composite qaBtnCmp = new Composite(itemsGroup, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.FILL).grab(false, true).applyTo(qaBtnCmp);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).applyTo(qaBtnCmp);

		final Button qaBtn = new Button(qaBtnCmp, SWT.NONE);
		qaBtn.setLayoutData(new GridData(SWT.LEFT, SWT.END, false, true));
		qaBtn.setText(Messages.getString("tmxeditor.QA.qaExcuteBtnTitle"));
		// UNDO 这里因为还没有做　数据库的　qa，因此，当打开数据库时，qa 的按钮设置不可用状态 --robert 2013-10-15
		if (tmxDataAccess instanceof TmxLargeFileDataAccess) {
			qaBtn.setEnabled(true);
		} else {
			qaBtn.setEnabled(false);
		}

		qaBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 先将选择的值保存至缓存中。再执行品质检查
				StringBuffer useableQAItemSB = new StringBuffer();
				if (tagConsistenceBtn.getSelection()) {
					useableQAItemSB.append(QAConstant.QA_TagConsistence).append(";");
				}
				if (numberConsistenceBtn.getSelection()) {
					useableQAItemSB.append(QAConstant.QA_NumberConsistence).append(";");
				}
				if (spaceOfParaCheckBtn.getSelection()) {
					useableQAItemSB.append(QAConstant.QA_SpaceOfParaCheck).append(";");
				}
				if (tgtNullBtn.getSelection()) {
					useableQAItemSB.append(QAConstant.QA_TgtNull).append(";");
				}
				if (srcSameWithTgtBtn.getSelection()) {
					useableQAItemSB.append(QAConstant.QA_SrcSameWithTgt).append(";");
				}
				if (srcSameButTgtBtn.getSelection()) {
					useableQAItemSB.append(QAConstant.QA_SrcSameButTgt).append(";");
				}
				if (tgtSameButSrcBtn.getSelection()) {
					useableQAItemSB.append(QAConstant.QA_TgtSameButSrc).append(";");
				}
				store.setValue(QAConstant.PREF_useableQAItemStr, useableQAItemSB.toString());

				if (useableQAItemSB.length() <= 0) {
					OpenMessageUtils.openMessage(IStatus.INFO, Messages.getString("tmxeditor.tmxeditor.qaItemNull"));
					return;
				}
				commit();
				tmxDataAccess.beginQA(srcLangCode, tgtLangCode, true, true);

			}
		});

		// >右边
		Group operateGroup = new Group(qaCmp, SWT.NONE);
		operateGroup.setText(Messages.getString("tmxeditor.QA.operateGroupTitle"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(operateGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(operateGroup);

		// >>第一行, 这两个忽略项。跟　过滤查询的保持参数一致。
		final Button ignoreCaseBtn = new Button(operateGroup, SWT.CHECK);
		ignoreCaseBtn.setText(Messages.getString("tmxeditor.QA.igoureCaceBtnTitle"));
		ignoreCaseBtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreCase));
		ignoreCaseBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				store.setValue(TeCoreConstant.FILTER_ignoreCase, ignoreCaseBtn.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				store.setValue(TeCoreConstant.FILTER_ignoreCase, ignoreCaseBtn.getSelection());
			}
		});

		final Button ignoreTagBtn = new Button(operateGroup, SWT.CHECK);
		ignoreTagBtn.setText(Messages.getString("tmxeditor.QA.ignoreTagBtnTitle"));
		ignoreTagBtn.setToolTipText(Messages.getString("tmxeditor.QA.ignoreTagBtnToolTip"));
		ignoreTagBtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreTag));
		ignoreTagBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				store.setValue(TeCoreConstant.FILTER_ignoreTag, ignoreTagBtn.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				store.setValue(TeCoreConstant.FILTER_ignoreTag, ignoreTagBtn.getSelection());
			}
		});

		// 给品质检查所有选项设置值
		List<String> itemsArray = QACommonFuction.getUseableQAItems();

		tagConsistenceBtn.setSelection(itemsArray.contains(QAConstant.QA_TagConsistence));
		numberConsistenceBtn.setSelection(itemsArray.contains(QAConstant.QA_NumberConsistence));
		spaceOfParaCheckBtn.setSelection(itemsArray.contains(QAConstant.QA_SpaceOfParaCheck));
		tgtNullBtn.setSelection(itemsArray.contains(QAConstant.QA_TgtNull));
		srcSameWithTgtBtn.setSelection(itemsArray.contains(QAConstant.QA_SrcSameWithTgt));
		srcSameButTgtBtn.setSelection(itemsArray.contains(QAConstant.QA_SrcSameButTgt));
		tgtSameButSrcBtn.setSelection(itemsArray.contains(QAConstant.QA_TgtSameButSrc));

		// 当切换　tabfolder 时，给两个忽略项设置动态变化，因为　qa 与过滤器共用一套过滤参数。
		final TabFolder tab = (TabFolder) (qaCmp.getParent());
		tab.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tab.getSelectionIndex() == 1) {
					ignoreCaseBtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreCase));
					ignoreTagBtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreTag));
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (tab.getSelectionIndex() == 1) {
					ignoreCaseBtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreCase));
					ignoreTagBtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreTag));
				}
			}
		});
	}

	/**
	 * 如果srcLang为null，则说明当前编辑的对象是无源语言的TMX文件或者是TMXDB， <br>
	 * 需要根据目标语言来初始化源语言和目标语言选择组件。<br>
	 * 需要初始化{@link this#srcLangCode} 和 {@link this#tgtLangCode}
	 * @param srcLang
	 * @param tgtLang
	 **/
	private void createLangUI(Composite container) {
		// 所有语言的集合
		List<String> allLangList = new LinkedList<String>();
		allLangList.add(srcLangCode);
		allLangList.addAll(tmxDataAccess.getLangList());

		Label srcLangLabel = new Label(container, SWT.NONE);
		srcLangLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		srcLangLabel.setText(Messages.getString("tmxeditor.filter.srcLang"));

		srcLangCombo = new Combo(container, SWT.READ_ONLY);
		GridData srcLangCombgd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		srcLangCombgd.widthHint = 80;
		srcLangCombo.setLayoutData(srcLangCombgd);
		srcLangCombo.setItems(allLangList.toArray(new String[] {}));
		srcLangCombo.select(allLangList.indexOf(srcLangCode));

		srcSearchText = new Text(container, SWT.BORDER);
		srcSearchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label tgtLangLabel = new Label(container, SWT.NONE);
		tgtLangLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		tgtLangLabel.setText(Messages.getString("tmxeditor.filter.tgtLang"));

		tgtLangCombo = new Combo(container, SWT.READ_ONLY);
		tgtLangCombo.setLayoutData(srcLangCombgd);
		tgtLangCombo.setItems(allLangList.toArray(new String[] {}));
		tgtLangCombo.select(allLangList.indexOf(tgtLangCode));

		tgtSearchText = new Text(container, SWT.BORDER);
		tgtSearchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		srcSearchText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		srcSearchText.setText(srcSearchTitle);
		srcSearchText.addKeyListener(searchKeyListener);
		srcSearchText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				Text text = (Text) e.widget;
				if (srcSearchTitle.equals(text.getText())) {
					text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					text.setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				Text text = (Text) e.widget;
				if ("".equals(text.getText())) {
					text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
					text.setText(srcSearchTitle);
				}
			}
		});

		Button searchBtn = new Button(container, SWT.NONE);
		GridData gd_button = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_button.widthHint = 70;
		searchBtn.setLayoutData(gd_button);
		searchBtn.setText(Messages.getString("tmxeditor.filter.searchBtn"));
		searchBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editorFilter.comboViewer.getCombo().select(0);
				beginToSeach();
			}
		});

		tgtSearchText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		tgtSearchText.setText(tgtSearchTitle);
		tgtSearchText.addKeyListener(searchKeyListener);
		tgtSearchText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				Text text = (Text) e.widget;
				if (tgtSearchTitle.equals(text.getText())) {
					text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					text.setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				Text text = (Text) e.widget;
				if ("".equals(text.getText())) {
					text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
					text.setText(tgtSearchTitle);
				}
			}
		});
	}

	private TmxEditorFilter createTmxFilter(Composite container) {
		editorFilter = new TmxEditorFilter();
		editorFilter.createFilterUI(container);
		editorFilter.comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				tgtSearchText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
				tgtSearchText.setText(tgtSearchTitle);
				srcSearchText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
				srcSearchText.setText(srcSearchTitle);
				beginToSeach();
			}
		});
		return null;
	}

	/**
	 * 当品质检查结果视图中双击某行定位之前，先将所有过滤查询数据清空，并让过滤条件重置为所有文本段的选项。 --robert 2013-10-10
	 */
	public void resetFilterBeforLocation(String srcLang, String tgtLang) {
		srcLangCombo.select(Arrays.asList(srcLangCombo.getItems()).indexOf(srcLang));
		tgtLangCombo.select(Arrays.asList(tgtLangCombo.getItems()).indexOf(tgtLang));
		tgtSearchText.setText("");
		srcSearchText.setText("");
		editorFilter.comboViewer.getCombo().select(0);
		beginToSeach();
	}

	/**
	 * 开始查询
	 */
	public void beginToSeach() {
		String srcLang = srcLangCombo.getText();
		String tgtLang = tgtLangCombo.getText();
		if (srcLang.equals(tgtLang)) {
			OpenMessageUtils.openMessage(IStatus.WARNING, Messages.getString("tmxeditor.tmxeditor.warn.msg1"));
			return;
		}

		srcLangCode = srcLang;
		tgtLangCode = tgtLang;
		doFilter(editorFilter.getCurrentFilter());
	}

	private void doFilter(final TmxEditorFilterBean filter) {
		TeActiveCellEditor.commit();
		final String srcSearchStr = getSearchText(srcSearchText);
		final String tgtSearchStr = getSearchText(tgtSearchText);
		IRunnableWithProgress progress = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				tmxDataAccess.loadDisplayTuIdentifierByFilter(monitor, filter, srcLangCode, tgtLangCode, srcSearchStr,
						tgtSearchStr);
			}
		};
		try {
			new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, progress);
		} catch (Exception e) {
			e.printStackTrace();
		}
		tmxEditorImpWithNattable.setSrcSearchStr(srcSearchStr);
		tmxEditorImpWithNattable.setTgtSearchStr(tgtSearchStr);
		tmxEditorImpWithNattable.getTable().setFocus();
		tmxEditorImpWithNattable.refrush();
		tmxEditorImpWithNattable.selectCell(getTgtColumnIndex(), 0);
	}

	private String getSearchText(Text searchTextWidget) {
		String text = searchTextWidget.getText();
		if (text == null || text.equals(tgtSearchTitle) || text.equals(srcSearchTitle)) {
			return "";
		}
		return text;
	}

	private void createNattable(Composite container, String srcLang, String tgtLang) {
		container.setLayout(new FillLayout());
		tmxEditorImpWithNattable = new TmxEditorImpWithNattable(tmxDataAccess);
		tmxEditorImpWithNattable.createContents(container);
		tmxEditorImpWithNattable.getTable().addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
			}

			@Override
			public void focusGained(FocusEvent e) {
				TmxEditor.this.viewPart.getViewSite().getActionBars()
						.setGlobalActionHandler(ActionFactory.FIND.getId(), findAction);
				findAction.updateEnabledState();
			}
		});
		// 将 TmxEidotrImpWithNattable 提供的选择事件提供器注册给 viewer
		if (viewPart != null) {
			viewPart.getSite().setSelectionProvider(tmxEditorImpWithNattable.getSelectionProvider());
		}
	}

	public void saveAs()  {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				commit();
			}
		});
		ExportDialog dlg = new ExportDialog(parentComposite.getShell(), this);
		dlg.open();
	}

	public void save(IProgressMonitor monitor) throws Exception {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				commit();
			}
		});
		tmxDataAccess.save(monitor);
		tmxDataAccess.setDirty(false);
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				tmxEditorImpWithNattable.redraw();
				tmxEditorImpWithNattable.refreshSelectionedRow();
			}
		});
	}

	public boolean isDirty() {
		if (tmxDataAccess != null) {
			return tmxDataAccess.isDirty();
		}
		return false;
	}

	public boolean closeTmxEditor() {
		commit();
		int _choice = -1;
		if (isDirty() || tmxDataAccess.isSourceExist()) {
			String message = Messages.getString("tmxeditor.tmxeditor.savechangs");
			String[] buttons = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
					IDialogConstants.CANCEL_LABEL };
			MessageDialog d = new MessageDialog(parentComposite.getShell(),
					Messages.getString("tmxeditor.all.dialog.info"), null, message, MessageDialog.QUESTION, buttons, 0) {
				protected int getShellStyle() {
					return super.getShellStyle() | SWT.SHEET;
				}
			};
			_choice = d.open();
		}
		if (_choice == 2) {
			return false;
		}
		final int choice = _choice;
		final String[] msgs = new String[1];
		IRunnableWithProgress r = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(Messages.getString("tmxeditor.tmxEditorViewer.savelabel"), 1);
				try {
					if (choice == 0) {
						tmxDataAccess.save(monitor);
					}
					tmxDataAccess.closeTmxDataAccess(new SubProgressMonitor(monitor, 1));
				} catch (Exception e) {
					msgs[0] = e.getMessage();
				}
				monitor.done();
			}
		};
		try {
			new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, r);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (msgs[0] != null) {
			String msg = Messages.getString("tmxeditor.tmxeditor.savefailmsg");
			msg = MessageFormat.format(msg, msgs[0]);
			if (!OpenMessageUtils.openConfirmMessage(msg)) {
				return false;
			}
		}
		// 关闭时移除选择事件提供器
		if (viewPart != null) {
			viewPart.getSite().setSelectionProvider(null);
		}
		tmxEditorImpWithNattable.dispose();
		tmxEditorImpWithNattable = null;
		this.tmxDataAccess = null;
		if (dialog != null) {
			dialog.close();
		}
		findAction.updateEnabledState();
		return true;
	}

	public void refreshUI() {
		if (tmxEditorImpWithNattable != null) {
			tmxEditorImpWithNattable.refrush();
		}
	}

	/**
	 * 根据 TmxDataAccess 中现有的数据，从新创建界面，包括过滤面板和编辑器 ;
	 */
	public void reCreateUI() {
		if (this.parentComposite != null && !this.parentComposite.isDisposed() && this.viewPart != null) {
			int index = editorFilter.comboViewer.getCombo().getSelectionIndex();
			String srcText = getSearchText(srcSearchText);
			String tgtText = getSearchText(tgtSearchText);
			Control[] childs = this.parentComposite.getChildren();
			for (Control c : childs) {
				if (c != null && !c.isDisposed()) {
					c.dispose();
				}
			}
			this.srcLangCode = tmxDataAccess.getCurrSrcLang();
			this.tgtLangCode = tmxDataAccess.getCurrTgtLang();
			createContent(viewPart, parentComposite);
			if (srcText.length() != 0) {
				srcSearchText.setText(srcText);
				srcSearchText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			}
			if (tgtText.length() != 0) {
				tgtSearchText.setText(tgtText);
				srcSearchText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			}
			editorFilter.comboViewer.getCombo().select(index);
			viewPart.notifyOpenEvent(tmxDataAccess);
			this.parentComposite.pack();
			this.parentComposite.layout();
			this.parentComposite.getParent().layout();
		}
	}

	/**
	 * 重置过滤器 ;
	 */
	public void resetFileter() {
		editorFilter.comboViewer.getCombo().select(0);
	}

	public void addTu(TmxTU tu) {
		int[] rows = tmxEditorImpWithNattable.getSelectedRows();
		int selRow = 0;
		if (rows.length != 0) {
			selRow = rows[0];
		}
		List<String> disTuIds = tmxDataAccess.getDisplayTuIdentifiers();
		String tuIdentifer = null;
		if (disTuIds.size() != 0) {
			tuIdentifer = disTuIds.get(selRow);
		}
		commit();
		String id = tmxDataAccess.addTu(tu, tuIdentifer);
		if (id != null) {
			int currRow = selRow;
			if (tuIdentifer == null || tmxDataAccess instanceof DatabaseDataAccess) {
				disTuIds.add(id);
				currRow = disTuIds.size() - 1;
			} else {
				disTuIds.add(currRow, id);
			}
			tmxEditorImpWithNattable.refrush();
			tmxEditorImpWithNattable.selectCell(getSrcColumnIndex(), currRow);
			tmxEditorImpWithNattable.resizeRowsHeight();
			tmxEditorImpWithNattable.editSelectedCell();
			tmxDataAccess.setDirty(true);
		}
	}

	public void deleteSelectedTu() {
		int[] rows = tmxEditorImpWithNattable.getSelectedRows();
		if (rows.length == 0) {
			return;
		}
		Arrays.sort(rows);
		List<String> disTuIds = tmxDataAccess.getDisplayTuIdentifiers();
		if (disTuIds.size() == 0) {
			return;
		}
		String[] selTuIds = new String[rows.length];
		for (int i = 0; i < selTuIds.length; i++) {
			selTuIds[i] = disTuIds.get(rows[i]);
		}
		TeActiveCellEditor.close();
		tmxDataAccess.deleteTus(selTuIds, null);
		int firstRow = rows[0];
		int j = 0;
		for (int r : rows) {
			disTuIds.remove(r - j);
			j++;
		}
		refreshUI();
		while (firstRow > -1) {
			if (disTuIds.size() > firstRow) {
				tmxEditorImpWithNattable.selectCell(getTgtColumnIndex(), firstRow);
				tmxEditorImpWithNattable.resizeRowsHeight();
				tmxEditorImpWithNattable.editSelectedCell();
				break;
			}
			firstRow--;
		}
		tmxDataAccess.setDirty(true);
	}

	/**
	 * 支持进度条显示的清除内部标记
	 * @author Austen
	 * @param monitor
	 *            ;
	 */
	public void cleartInnerTag(IProgressMonitor monitor) {

		monitor.beginTask(Messages.getString("tmxeditor.tmxeditor.deleteIngerTag.taskName"), 5);
		int total = tmxDataAccess.getDisplayTuIdentifiers().size();
		int step = total > 500 ? (total / 500) + 1 : 1;

		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 5);
		subMonitor.beginTask("", total > 500 ? 500 : total);

		List<String> cleanList = new LinkedList<String>();
		TmxTU tu = null;
		int i = 0;
		for (String identifier : tmxDataAccess.getDisplayTuIdentifiers()) {
			tu = tmxDataAccess.getTuByIdentifier(identifier);
			if (tu.getSource() != null) {
				if (tu.getSource().getFullText().length() != tu.getSource().getPureText().length()) {
					cleanList.add(identifier);
				}
			}
			try {
				tu.getSegments().get(0).getAttributes();

			} catch (NullPointerException e) {
				System.out.println(identifier);
			}
			if (i++ % step == 0) {
				subMonitor.worked(1);
			}
		}
		subMonitor.done();
		monitor.done();
	}

	public List<String> getSplitePoints() {
		return null;
	}

	public TmxPropertiesBean getTmxProperties(boolean isReload) {
		return tmxDataAccess.loadTmxProperties();
	}

	/**
	 * 删除重复的TU，原文和译文相同。删除时如果TU中只有2个TUV，则直接删除TU；如果TU中有超过2个TUV则只删除当前TUV ;
	 */
	public void cleartDuplicatedTU(IProgressMonitor monitor, boolean ignoreTag, boolean ignoreCase) {
		boolean deleteTgtEmpty = tmxDataAccess.deleteDupaicate(monitor, ignoreTag, ignoreCase);
		if (!tmxDataAccess.isDirty()) {
			tmxDataAccess.setDirty(deleteTgtEmpty);
		}

	}

	/**
	 * 删除空的TU,是指将当前过滤结果中译文为空串的TUV删除,删除时如果TU中只有2个TUV，则直接删除TU；如果TU中有超过2个TUV则只删除当前TUV ;
	 */
	public void clearEmptyTU(IProgressMonitor monitor,boolean ignoreTag) {
		boolean deleteTgtEmpty = tmxDataAccess.deleteTgtEmpty(monitor,ignoreTag);
		if (!tmxDataAccess.isDirty()) {
			tmxDataAccess.setDirty(deleteTgtEmpty);
		}

	}

	/**
	 * 删除相同原文不同译文的TU,删除时如果TU中只有2个TUV，则直接删除TU；如果TU中有超过2个TUV则只删除当前TUV ;
	 * @param monitor
	 * @param ignoreTag
	 */
	public void cleartDuplicatedSrcDiffTgtTU(IProgressMonitor monitor, boolean ignoreTag, boolean ignoreCase) {
		boolean deleteTgtEmpty = tmxDataAccess.deleteSameSrcDiffTgt(monitor, ignoreTag, ignoreCase);
		if (!tmxDataAccess.isDirty()) {
			tmxDataAccess.setDirty(deleteTgtEmpty);
		}
	}

	public void clearEndsSpaces(IProgressMonitor monitor ) {
		boolean deleteTgtEmpty = tmxDataAccess.deleteEndsSpaces(monitor);
		if (!tmxDataAccess.isDirty()) {
			tmxDataAccess.setDirty(deleteTgtEmpty);
		}
	}

	/**
	 * 始终重新加载后台数据
	 * @param monitor
	 * @param reFreshUI
	 *            ; :是否刷新前台界面，
	 */
	public void loadDataAndReFreshUI(final IProgressMonitor monitor, final boolean reFreshUI) {
		final String srcSearchStr = getSearchText(srcSearchText);
		final String tgtSearchStr = getSearchText(tgtSearchText);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				tmxDataAccess.loadDisplayTuIdentifierByFilter(monitor, editorFilter.getCurrentFilter(), srcLangCode,
						tgtLangCode, srcSearchStr, tgtSearchStr);
				if (reFreshUI) {
					tmxEditorImpWithNattable.refrush();
					// tmxEditorImpWithNattable.selectCell(getTgtColumnIndex(), 0);
				}
			}
		});
	}

	public AbstractTmxDataAccess getTmxDataAccess() {
		return this.tmxDataAccess;
	}

	public TmxSegement getSelectedTuv() {
		return null;
	}

	public String getSrcLang() {
		return this.srcLangCode;
	}

	public String getTgtLang() {
		return this.tgtLangCode;
	}

	public void jumptoRow(int rowIndex) {

	}

	public int getSrcColumnIndex() {
		return 1;
	}

	public int getTgtColumnIndex() {
		return 2;
	}

	public void commit() {
		TeActiveCellEditor.commit();
	}

	class FindReplaceAction extends Action {

		public FindReplaceAction() {
			setEnabled(false);
		}

		@Override
		public void runWithEvent(Event event) {
			if (dialog == null) {
				dialog = new FindReplaceDialog(viewPart.getSite().getShell());
			}
			dialog.setTmxDataAccess(tmxDataAccess);
			dialog.setTmxEditorImpWithNattable(tmxEditorImpWithNattable);
			dialog.open();
			ICellEditor iCellEditor = TeActiveCellEditor.getCellEditor();
			if (iCellEditor != null && iCellEditor instanceof CellEditor) {
				CellEditor cellEditor = (CellEditor) iCellEditor;
				String selText = cellEditor.getTextViewer().getSelectionText();
				dialog.setFindText(selText);
			}
		}

		void updateEnabledState() {
			if (tmxEditorImpWithNattable != null) {
				setEnabled(true);
				return;
			}
			setEnabled(false);
		}

	}

	public TmxEditorImpWithNattable getTmxEditorImpWithNattable() {
		return tmxEditorImpWithNattable;
	}

	/**
	 * 获取选中行的id标示
	 * @return ;
	 */
	public String[] getSelectIdentifiers() {
		int[] selectedRows = tmxEditorImpWithNattable.getSelectedRows();
		if (null == selectedRows || selectedRows.length == 0) {
			return new String[0];
		}
		List<String> displayTuIdentifiers = this.tmxDataAccess.getDisplayTuIdentifiers();
		if (null == displayTuIdentifiers || displayTuIdentifiers.isEmpty()) {
			return new String[0];
		}
		String[] ids = new String[selectedRows.length];
		int currentSelectLineNumber = 0;
		for (int i = 0; i < selectedRows.length; i++) {
			currentSelectLineNumber = selectedRows[i];
			if (currentSelectLineNumber >= displayTuIdentifiers.size()) {
				return new String[0];
			}
			ids[i] = displayTuIdentifiers.get(currentSelectLineNumber);
		}
		return ids;
	}

}
