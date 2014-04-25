package net.heartsome.cat.te.ui.task.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.ui.Activator;
import net.heartsome.cat.common.ui.dialog.HsAbstractHelpDilog;
import net.heartsome.cat.common.ui.languagesetting.LanguageLabelProvider;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.common.util.LanguageUtils;
import net.heartsome.cat.te.core.bean.TmxPropertiesBean;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModifyLangCodeDialog extends HsAbstractHelpDilog {

	public static Logger LOGGER = LoggerFactory.getLogger(ModifyLangCodeDialog.class.getName());

	/** 修改前源语言代码. */
	private String oldSrclang;
	/** 修改前目标语言代码集合 */
	private List<String> oldTgtlangList;

	private TmxPropertiesBean propbean;
	private Map<String, String> resultMap = null;
	private List<Language> langstore = new ArrayList<Language>(LocaleService.getDefaultLanguage().values());
	private Map<String, Integer> langIndexMap = new HashMap<String, Integer>();
	private Map<Integer, TableComboViewer> newTgtlangMap;

	private TableComboViewer tcvNew;
	private Map<String, Image> imageCache = new HashMap<String, Image>();
	/**
	 * 
	 * @param parentShell shell
	 * @param propbean TmxpropertiesBean
	 * @param resultMap 存放需要修改的语言代码<li>key：源语言</li><li>value：修改后语言代码</li>
	 */
	public ModifyLangCodeDialog(Shell parentShell, TmxPropertiesBean propbean, Map<String, String> resultMap) {
		super(parentShell);
		this.propbean = propbean;
		this.resultMap = resultMap;
		Collections.sort(langstore, new Comparator<Language>() {
			public int compare(Language o1, Language o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		oldSrclang = LanguageUtils.convertLangCode(propbean.getSrcLang());

		// 修正 propbean 中的语言代码，时期符合规范
		oldTgtlangList = new LinkedList<String>();
		oldTgtlangList.add(oldSrclang);
		for (String lang : propbean.getTargetLang()) {
			oldTgtlangList.add(LanguageUtils.convertLangCode(lang));
		}
		String langcode = null;
		for (int i = 0; i < langstore.size(); i++) {
			langcode = langstore.get(i).getCode();
			if (oldTgtlangList.contains(langcode)) {
				langIndexMap.put(langcode, i);
			}
			if (oldTgtlangList.size() == langIndexMap.size()) {
				break;
			}
		}
		oldTgtlangList.remove(0);
		newTgtlangMap = new HashMap<Integer, TableComboViewer>();
		
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.ModifyLangCodeDialog.title"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// 修改前
		Group groupOld = new Group(composite, SWT.NONE);
		groupOld.setText(Messages.getString("dialog.ModifyLangCodeDialog.groupOldlbl"));
		groupOld.setLayout(new GridLayout(2, false));
		groupOld.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label lblOldSrclang = new Label(groupOld, SWT.NONE);
		lblOldSrclang.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblOldSrclang.setText(Messages.getString("dialog.ModifyLangCodeDialog.groupOld.scritem"));
		CLabel label = new CLabel(groupOld, SWT.BORDER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		
		Language srcLan = langstore.get(langIndexMap.get(oldSrclang));
		label.setText(langIndexMap.get(oldSrclang) == null ? 
				oldSrclang : langstore.get(langIndexMap.get(oldSrclang)).toString());
		String imagePath = srcLan.getImagePath();
		if (imagePath != null && !imagePath.equals("")) {				
			ImageDescriptor imageDesc = Activator.getImageDescriptor(imagePath);
			if (imageDesc != null) {
				ImageData data = imageDesc.getImageData().scaledTo(16, 12);
				Image image = new Image(Display.getDefault(), data);				
				// 销毁原来的图片
				Image im = imageCache.put(srcLan.getCode(), image);
				if (im != null && !im.isDisposed()) {
					im.dispose();
				}
				label.setImage(image);
			}
		}
		int i = 0;
		for (String lang : oldTgtlangList) {
			Label lblOldTgtLang = new Label(groupOld, SWT.NONE);
			if (i++ < 1) {
				lblOldTgtLang.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
				lblOldTgtLang.setText(Messages.getString("dialog.ModifyLangCodeDialog.groupOld.tgtitem"));
			}
			label = new CLabel(groupOld, SWT.BORDER);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			if (langIndexMap.get(lang) != null) {
				Language lan = langstore.get(langIndexMap.get(lang));
				label.setText(lan.toString());
				imagePath = lan.getImagePath();
				if (imagePath != null && !imagePath.equals("")) {				
					ImageDescriptor imageDesc = Activator.getImageDescriptor(imagePath);
					if (imageDesc != null) {
						ImageData data = imageDesc.getImageData().scaledTo(16, 12);
						Image image = new Image(Display.getDefault(), data);				
						// 销毁原来的图片
						Image im = imageCache.put(lan.getCode(), image);
						if (im != null && !im.isDisposed()) {
							im.dispose();
						}
						label.setImage(image);
					}
				}
			} else {
				label.setText(lang);
			}
		}

		// 修改后
		Group groupNew = new Group(composite, SWT.NONE);
		groupNew.setText(Messages.getString("dialog.ModifyLangCodeDialog.groupNewlbl"));
		groupNew.setLayout(new GridLayout(2, false));
		groupNew.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Label lblNewSrclang = new Label(groupNew, SWT.NONE);
		lblNewSrclang.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblNewSrclang.setText(Messages.getString("dialog.ModifyLangCodeDialog.groupNew.scritem"));
		tcvNew = new TableComboViewer(groupNew, SWT.READ_ONLY | SWT.BORDER);
		final TableCombo tcNew = tcvNew.getTableCombo();
		tcNew.setShowTableLines(false);
		tcNew.setShowTableHeader(false);
		tcNew.setDisplayColumnIndex(-1);
		tcNew.setShowImageWithinSelection(true);
		tcNew.setShowColorWithinSelection(false);
		tcNew.setShowFontWithinSelection(false);
		tcNew.setVisibleItemCount(20);
		tcNew.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tcvNew.setLabelProvider(new LanguageLabelProvider());
		tcvNew.setContentProvider(new ArrayContentProvider());
		tcvNew.setInput(langstore);
//		tcvNew.addSelectionChangedListener(new ISelectionChangedListener() {
//			public void selectionChanged(SelectionChangedEvent event) {
//				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
//				Language srcLanguage = (Language) selection.getFirstElement();
//				oldSrclang = srcLanguage.getCode();
//			}
//		});
		tcNew.select(langIndexMap.get(propbean.getSrcLang()));

		i = 0;
		for (String lang : oldTgtlangList) {
			Label lbl = new Label(groupNew, SWT.NONE);
			if (i < 1) {
				lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
				lbl.setText(Messages.getString("dialog.ModifyLangCodeDialog.groupNew.tgtitem"));

			}
			TableComboViewer tcvNewTgt = new TableComboViewer(groupNew, SWT.READ_ONLY | SWT.BORDER);
			TableCombo tcNewTgt = tcvNewTgt.getTableCombo();
			tcNewTgt.setShowTableLines(false);
			tcNewTgt.setShowTableHeader(false);
			tcNewTgt.setDisplayColumnIndex(-1);
			tcNewTgt.setShowImageWithinSelection(true);
			tcNewTgt.setShowColorWithinSelection(false);
			tcNewTgt.setShowFontWithinSelection(false);
			tcNewTgt.setVisibleItemCount(20);
			tcNewTgt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			tcvNewTgt.setLabelProvider(new LanguageLabelProvider());
			tcvNewTgt.setContentProvider(new ArrayContentProvider());
			tcNewTgt.setText("");
			tcvNewTgt.setInput(langstore);
			if (langIndexMap.get(lang) ==  null) {
				tcNewTgt.setText(lang);
			} else {
				tcNewTgt.select(langIndexMap.get(lang));
			}
			newTgtlangMap.put(i, tcvNewTgt);
			i++;
		}

		return composite;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		Language modifyLang = null;
		IStructuredSelection selection = null;
		// 对比源语言
		selection = (IStructuredSelection) tcvNew.getSelection();
		Language srcLang = (Language) selection.getFirstElement();
		String srcCode = srcLang.getCode();
		if (!srcLang.getCode().equals(oldSrclang)) {
			resultMap.put(oldSrclang, srcLang.getCode());
		}
		// 对比目标语言
		List<String> langs = new ArrayList<String>();
		for (int i = 0; i < oldTgtlangList.size(); i++) {
			selection = (IStructuredSelection) newTgtlangMap.get(i).getSelection();
			if (selection.isEmpty()) {
				String code = newTgtlangMap.get(i).getTableCombo().getText();
				langs.add(code);
				if(langs.contains(code)){
					// ERROR
					OpenMessageUtils.openMessage(IStatus.ERROR, Messages.getString("dialog.ModifyLangCodeDialog.checkMsg2"));
					newTgtlangMap.get(i).getTableCombo().setFocus();
					return;
				}
			} else {
				modifyLang = (Language) selection.getFirstElement();
				String code = modifyLang.getCode();
				if(code.equals(srcCode)){
					// ERROR
					OpenMessageUtils.openMessage(IStatus.ERROR, Messages.getString("dialog.ModifyLangCodeDialog.checkMsg1"));
					newTgtlangMap.get(i).getTableCombo().setFocus();
					return;
				}
				if(langs.contains(code)){
					// ERROR
					OpenMessageUtils.openMessage(IStatus.ERROR, Messages.getString("dialog.ModifyLangCodeDialog.checkMsg2"));
					newTgtlangMap.get(i).getTableCombo().setFocus();
					return;
				}
				langs.add(code);
				if (!code.equals(oldTgtlangList.get(i))) {
					resultMap.put(oldTgtlangList.get(i), modifyLang.getCode());
				}
			}
		}
		if (resultMap.size() > 0) {
			setReturnCode(OK);
			close();
		} else {
			cancelPressed();
		}
	}
	
	@Override
	public boolean close() {
		for (String code : imageCache.keySet()) {
			Image im = imageCache.get(code);
			if (im != null && !im.isDisposed()) {
				im.dispose();
			}
		}
		imageCache.clear();
		return super.close();
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractHelpDilog#getDisplayHelpUrl()
	 */
	@Override
	public String getDisplayHelpUrl() {
		return "/net.heartsome.cat.te.ui.help/html/{0}/ch03.html#edit_lang_id";
	}
}
