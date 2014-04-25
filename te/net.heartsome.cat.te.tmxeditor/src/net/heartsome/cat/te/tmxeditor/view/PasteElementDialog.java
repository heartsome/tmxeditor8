/**
 * PasteElementDialog.java
 *
 * Version information :
 *
 * Date:2013-12-26
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.view;

import net.heartsome.cat.te.tmxeditor.view.PropertiesView.ElemCollector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Austen
 * @version
 * @since JDK1.6
 */
public class PasteElementDialog extends AddElementDialog {

	public PasteElementDialog(Shell parentShell, ElemCollector collector) {
		super(parentShell, PropertiesView.TU_PROP_COPY, collector);
	}

	private PasteElementDialog(Shell parentShell, int category, ElemCollector elem) {
		super(parentShell, category, elem);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(Dialog.OK).setEnabled(true);
	}
	
	@Override
	protected void okPressed() {
		if (btnAllTu.getSelection()) {
			elem.scope = PropertiesView.ALL_TU;
		} else if (btnCurrentSelected.getSelection()) {
			elem.scope = PropertiesView.SELECTED_TU;
		} else {
			elem.scope = PropertiesView.FILTERED_TU;
		}
		setReturnCode(OK);
		close();
	}
}
