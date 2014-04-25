package net.heartsome.cat.te.tmxeditor.view;

import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.te.tmxeditor.resource.Messages;
import net.heartsome.cat.te.tmxeditor.view.PropertiesView.AttrCollector;
import net.heartsome.cat.te.tmxeditor.view.PropertiesView.ElemCollector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * <b>除非要在 tu tuv 中添加非标准属性，否则勿使用此类。</b> 添加自定义属性添加或修改对话框
 * @author Austen 2013-06-18
 */
public class DeleteConfirmDialog extends Dialog {

	private AttrCollector attr;

	private Button btnAllTu;
	private Button btnFiltered;
	private Button btnCurrentSelected;
	private ElemCollector elem;
	private int category = -1;

	public DeleteConfirmDialog(Shell parentShell, int category, AttrCollector attr) {
		super(parentShell);
		this.attr = attr;
		this.category = category;
	}

	public DeleteConfirmDialog(Shell parentShell, int category, ElemCollector elem) {
		super(parentShell);
		this.elem = elem;
		this.category = category;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		String title = Messages.getString("tmxeditor.all.dialog.confirm");
		switch (category) {
		case PropertiesView.TU_ATTRS:
			title = Messages.getString("tmxeditor.deleteConfigDialog.title.tu");
			break;
		case PropertiesView.TUV_ATTRS:
			title = Messages.getString("tmxeditor.deleteConfigDialog.title.tuv");
			break;
		case PropertiesView.TU_NODE_NOTE:
			title = Messages.getString("tmxeditor.deleteConfigDialog.title.note");
			break;
		case PropertiesView.TU_NODE_PROPS:
			title = Messages.getString("tmxeditor.deleteConfigDialog.title.prop");
			break;
		}
		newShell.setText(title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite compostie = new Composite(parent, SWT.NONE);
		compostie.setLayout(new GridLayout(1, false));
		GridDataFactory.createFrom(new GridData(GridData.FILL_BOTH)).hint(500, SWT.DEFAULT).applyTo(compostie);

		// 作用组
		Group applyGroup = new Group(compostie, SWT.NONE);
		applyGroup.setText(Messages.getString("tmxeditor.deleteConfigDialog.apply"));
		applyGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(applyGroup);
		// 当前选中行
		btnCurrentSelected = new Button(applyGroup, SWT.RADIO);
		btnCurrentSelected.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnCurrentSelected.setText(Messages.getString("tmxeditor.deleteConfigDialog.current.line"));
		btnCurrentSelected.setSelection(true);

		// 所有过滤结果
		btnFiltered = new Button(applyGroup, SWT.RADIO);
		btnFiltered.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnFiltered.setText(Messages.getString("tmxeditor.deleteConfigDialog.allFilterResults"));

		// 整个文件/记忆库
		btnAllTu = new Button(applyGroup, SWT.RADIO);
		btnAllTu.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnAllTu.setText(Messages.getString("tmxeditor.deleteConfigDialog.allFileDataabase"));
		return compostie;
	}

	@Override
	protected void okPressed() {
		int scope = -1;
		if (btnAllTu.getSelection()) {
			scope = PropertiesView.ALL_TU;
		} else if (btnCurrentSelected.getSelection()) {
			scope = PropertiesView.SELECTED_TU;
		} else {
			scope = PropertiesView.FILTERED_TU;
		}
		if (attr != null) {
			attr.scope = scope;
		} else {
			elem.scope = scope;
		}
		super.okPressed();
	}
}
