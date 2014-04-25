package net.heartsome.cat.te.ui.task.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.ui.dialog.HsAbstractHelpDilog;
import net.heartsome.cat.common.util.LanguageUtils;
import net.heartsome.cat.te.core.bean.TmxPropertiesBean;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteLangCodeDialog extends HsAbstractHelpDilog {

	public static Logger LOGGER = LoggerFactory.getLogger(DeleteLangCodeDialog.class.getName());

	private String srclang;
	/** 收集需要删除的语言对. */
	private List<String> deleteLangCode;
	/** 目标语言集合. */
	private List<String> tgtlangList;
	/** 语言库. */
	private List<Language> langstore;
	/** 目标语言在语言库中的index. */
	private Map<String, Integer> langIndexMap = new HashMap<String, Integer>();

	/**
	 * 删除语言代码对话框
	 * @param parentShell
	 *            Shell
	 * @param propbean
	 *            TmxPropertiesBean TMX 文件属性bean
	 * @param deleteLangCode
	 *            收集需要删除的语言代码
	 */
	public DeleteLangCodeDialog(Shell parentShell, TmxPropertiesBean propbean, List<String> deleteLangCode) {
		super(parentShell);
		this.deleteLangCode = deleteLangCode;
		srclang = propbean.getSrcLang();

		// 语言库
		langstore = new ArrayList<Language>(LocaleService.getDefaultLanguage().values());
		// 计算语言代码在语言库中的index，用于初始化选择框
		tgtlangList = new LinkedList<String>();
		tgtlangList.add(LanguageUtils.convertLangCode(srclang));
		for (String lang : propbean.getTargetLang()) {
			tgtlangList.add(LanguageUtils.convertLangCode(lang));
		}
		int i = 0;
		for (Language language : langstore) {
			if (langIndexMap.size() == tgtlangList.size()) {
				break;
			}
			if (tgtlangList.contains(language.getCode())) {
				langIndexMap.put(language.getCode(), i);
			}
			i++;
		}
		tgtlangList.remove(0);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.deleteLangCodeDialog.title"));
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(GridLayoutFactory.createFrom(new GridLayout(3, false)).margins(10, 10).create());
		composite.setLayoutData(GridDataFactory.createFrom(new GridData(GridData.FILL_BOTH)).hint(400, SWT.DEFAULT)
				.create());

		Label lblOldSrclang = new Label(composite, SWT.NONE);
		lblOldSrclang.setText(Messages.getString("dialog.deleteLangCodeDialog.srcLable"));
		new Label(composite, SWT.NONE);
		// Button btnSrclang = new Button(composite, SWT.CHECK);
		// btnSrclang.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		//
		// }
		// });
		Label txtSrclang = new Label(composite, SWT.NONE);
		txtSrclang.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSrclang.setText(langIndexMap.get(srclang) == null ? srclang : langstore.get(langIndexMap.get(srclang))
				.toString());
		int i = 0;
		for (final String lang : tgtlangList) {
			Label lblOldTgtLang = new Label(composite, SWT.NONE);
			if (i++ < 1) {
				lblOldTgtLang.setText(Messages.getString("dialog.deleteLangCodeDialog.tgtLable"));
			}
			final Button btnTgtlang = new Button(composite, SWT.CHECK);
			btnTgtlang.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnTgtlang.getSelection()) {
						deleteLangCode.add(lang);
					} else {
						deleteLangCode.remove(lang);
					}
				}
			});
			Text txtTgtlang = new Text(composite, SWT.NONE);
			txtTgtlang.setEditable(false);
			txtTgtlang.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtTgtlang
					.setText(langIndexMap.get(lang) == null ? lang : langstore.get(langIndexMap.get(lang)).toString());
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
		if (deleteLangCode.size() == tgtlangList.size()) {
			MessageDialog.openError(getShell(), Messages.getString("ui.all.dialog.error"),
					Messages.getString("te.ui.deleteLangHandler.error.deleteAllTgtMsg"));
			return;
		}
		setReturnCode(OK);
		close();
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractHelpDilog#getDisplayHelpUrl()
	 */
	@Override
	public String getDisplayHelpUrl() {
		// TODO Auto-generated method stub
		return "/net.heartsome.cat.te.ui.help/html/{0}/ch03.html#del_lang_id";
	}

}
