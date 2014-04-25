/**
 * TmxEditorFilter.java
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
import java.util.List;

import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.bean.TmxEditorFilterBean;
import net.heartsome.cat.te.core.utils.TmxCustomFilterUtil;
import net.heartsome.cat.te.tmxeditor.Activator;
import net.heartsome.cat.te.tmxeditor.resource.Messages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.PlatformUI;

/**
 * tmx 编辑器过滤器的处理以及过滤
 * @author robert 2013-06-21
 */
public class TmxEditorFilter {
	ComboViewer comboViewer;
	private int filterBtnLenght;

	private List<TmxEditorFilterBean> customeFilters;
	/**
	 * 系统过滤器，包括 所有文本段、源文和译文相同的文本段、源文相同，译文不同的文本段、译文相同，源文不同的文本段、带有批注的文本段、存在乱码的文本段
	 */
	private List<TmxEditorFilterBean> systemFilters;

	Image filterImage = null;

	public TmxEditorFilter() {
		filterImage = Activator.getImageDescriptor("images/view/filter.png").createImage();
	}

	public void createFilterUI(Composite container) {
		Composite filterComp1 = new Composite(container, SWT.NONE);
		filterComp1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		filterComp1.setLayout(new GridLayout(2, false));

		comboViewer = new ComboViewer(filterComp1, SWT.READ_ONLY);
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setInput(loadAllFilters());
		comboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof TmxEditorFilterBean) {
					return ((TmxEditorFilterBean) element).getName();
				}
				return null;
			}
		});
		Combo combo = comboViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		combo.select(0);
		Button customFilterBtn = new Button(filterComp1, SWT.NONE);
		// customFilterBtn.setText(Messages.getString("tmxeditor.filter.customFilterBtbLable"));
		customFilterBtn.setImage(filterImage);
		filterBtnLenght = customFilterBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
		customFilterBtn.setToolTipText(Messages.getString("tmxeditor.filter.customFilterBtbLable"));
		customFilterBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FilterDialog dialog = new FilterDialog(shell);
				int openResult = dialog.open();
				if (openResult == IDialogConstants.OK_ID) {
					IStructuredSelection selection = (IStructuredSelection)comboViewer.getSelection();
					TmxEditorFilterBean selectionFilter = null;
					if (selection.getFirstElement() instanceof TmxEditorFilterBean) {
						selectionFilter = (TmxEditorFilterBean) (selection.getFirstElement());
					}
					
					List<TmxEditorFilterBean> filtersInput = loadAllFilters();
					comboViewer.setInput(filtersInput);
					// 如果自定义过滤器更改后，之前选择系统过滤器，保持不变，否则重置为所有文本段
					if (selectionFilter.isCustom()) {
						for(TmxEditorFilterBean bean : filtersInput){
							if (!bean.isCustom() && bean.getId().equals(TeCoreConstant.FILTERID_allSeg)) {
								comboViewer.setSelection(new StructuredSelection(bean));
								break;
							}
						}
					}else {
						comboViewer.getCombo().setText(selectionFilter.getName());
					}
				}
			}
		});
		customFilterBtn.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (filterImage != null && !filterImage.isDisposed()) {
					filterImage.dispose();
				}
			}
		});

		Composite filterComp2 = new Composite(container, SWT.NONE);
		filterComp2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout filterComp2Gl = new GridLayout(2, false);
		filterComp2Gl.marginTop = -5;
		filterComp2.setLayout(filterComp2Gl);

		final IPreferenceStore store = net.heartsome.cat.te.core.Activator.getDefault().getPreferenceStore();
		final Button ignoreCaseCheckbtn = new Button(filterComp2, SWT.CHECK);
		ignoreCaseCheckbtn.setText(Messages.getString("tmxeditor.filter.ignoreCase"));
		ignoreCaseCheckbtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreCase));
		ignoreCaseCheckbtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				store.setValue(TeCoreConstant.FILTER_ignoreCase, ignoreCaseCheckbtn.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				store.setValue(TeCoreConstant.FILTER_ignoreCase, ignoreCaseCheckbtn.getSelection());
			}
		});

		final Button ignoreTagCheckBtn = new Button(filterComp2, SWT.CHECK);
		ignoreTagCheckBtn.setText(Messages.getString("tmxeditor.filter.ignoreTag"));
		System.out.println("ignoreTag = " + store.getBoolean(TeCoreConstant.FILTER_ignoreTag));
		ignoreTagCheckBtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreTag));
		ignoreTagCheckBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				store.setValue(TeCoreConstant.FILTER_ignoreTag, ignoreTagCheckBtn.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				store.setValue(TeCoreConstant.FILTER_ignoreTag, ignoreTagCheckBtn.getSelection());
			}
		});
		

		// 当切换　tabfolder 时，给两个忽略项设置动态变化，因为　qa 与过滤器共用一套过滤参数。
		final TabFolder tab = (TabFolder)(container.getParent().getParent());
		tab.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tab.getSelectionIndex() == 0) {
					ignoreCaseCheckbtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreCase));
					ignoreTagCheckBtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreTag));
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (tab.getSelectionIndex() == 1) {
					ignoreCaseCheckbtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreCase));
					ignoreTagCheckBtn.setSelection(store.getBoolean(TeCoreConstant.FILTER_ignoreTag));
				}
			}
		});
	}

	/**
	 * 获取当前选中的过滤器
	 * @return
	 */
	public TmxEditorFilterBean getCurrentFilter() {
		ISelection selection = comboViewer.getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection strSelection = (StructuredSelection) selection;
			if (strSelection.getFirstElement() instanceof TmxEditorFilterBean) {
				return (TmxEditorFilterBean) (strSelection.getFirstElement());
			}
		}
		return null;
	}

	/**
	 * 加载自定义过滤器
	 * @return
	 */
	private List<TmxEditorFilterBean> loadSystemFilters() {
		if (systemFilters == null) {
			systemFilters = new ArrayList<TmxEditorFilterBean>();
			// 先写入系统过滤器，
			String xpath = "";
			systemFilters.add(new TmxEditorFilterBean(TeCoreConstant.FILTERID_allSeg, Messages
					.getString("tmxeditor.tmxEditorFilter.allFilter.name"), xpath));
			xpath = "";
			systemFilters.add(new TmxEditorFilterBean(TeCoreConstant.FILTERID_srcSameWIthTgtSeg, Messages
					.getString("tmxeditor.tmxEditorFilter.srcTgtSame.name"), xpath));
			xpath = "";
			systemFilters.add(new TmxEditorFilterBean(TeCoreConstant.FILTERID_srcSameButTgtSeg, Messages
					.getString("tmxeditor.tmxEditorFilter.srcSameDiffTgt"), xpath));
			xpath = "";
			systemFilters.add(new TmxEditorFilterBean(TeCoreConstant.FILTERID_tgtSameButSrcSeg, Messages
					.getString("tmxeditor.tmxEditorFilter.tgtSameDiffsrc"), xpath));
			xpath = "";
			systemFilters.add(new TmxEditorFilterBean(TeCoreConstant.FILTERID_duplicateSeg, Messages
					.getString("tmxeditor.tmxEditorFilter.duplicateSeg"), xpath));
			xpath = "";
			systemFilters.add(new TmxEditorFilterBean(TeCoreConstant.FILTERID_withNoteSeg, Messages
					.getString("tmxeditor.tmxEditorFilter.hasNote.name"), xpath));
			xpath = "";
			systemFilters.add(new TmxEditorFilterBean(TeCoreConstant.FILTERID_withGarbleSeg, Messages
					.getString("tmxeditor.tmxEditorFilter.hasGarbled.name"), xpath));
			xpath = "";
			systemFilters.add(new TmxEditorFilterBean(TeCoreConstant.FILTERID_tgtNullSeg, Messages
					.getString("tmxeditor.tmxEditorFilter.tgtNullSeg"), xpath));
		}

		return systemFilters;
	}

	/**
	 * 加载可以使用的自定义过滤器
	 * @return
	 */
	private List<TmxEditorFilterBean> loadUseableCustomeFilters() {
		TmxCustomFilterUtil util = new TmxCustomFilterUtil();
		customeFilters = util.getUseableCustomFilters();
		return customeFilters;
	}

	/**
	 * 加载所有的过滤器，包括系统的与自定义的
	 * @return
	 */
	public List<TmxEditorFilterBean> loadAllFilters() {
		List<TmxEditorFilterBean> allFilterList = new ArrayList<TmxEditorFilterBean>();
		allFilterList.addAll(loadSystemFilters());
		allFilterList.addAll(loadUseableCustomeFilters());
		return allFilterList;
	}

	public int getFilterBtnLenght() {
		return filterBtnLenght;
	}
}
