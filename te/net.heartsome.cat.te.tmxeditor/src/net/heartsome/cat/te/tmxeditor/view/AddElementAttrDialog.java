package net.heartsome.cat.te.tmxeditor.view;

import java.text.MessageFormat;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.te.tmxeditor.resource.Messages;
import net.heartsome.cat.te.tmxeditor.view.PropertiesView.AttrCollector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <b>除非要在 tu tuv 中添加非标准属性，否则勿使用此类。</b> 添加自定义属性添加或修改对话框
 * @author Austen 2013-06-18
 */
public class AddElementAttrDialog extends Dialog {

	/** OK button 状态标识.依次为属性名、属性值。 */
	private int okButtonFlag = 0;

	private Text txtValue;
	private Text txtName;

	private TmxTU tu;
	private TmxSegement tuv;
	private AttrCollector attr;

	private Text txtErrorInfo;
	private Button btnAllTu;
	private Button btnFiltered;
	private Button btnCurrentSelected;

	public AddElementAttrDialog(Shell parentShell, TmxTU tu, AttrCollector attr) {
		super(parentShell);
		this.tu = tu;
		this.attr = attr;
	}

	public AddElementAttrDialog(Shell parentShell, TmxSegement tuv, AttrCollector attr) {
		super(parentShell);
		this.tuv = tuv;
		this.attr = attr;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (tu != null) {
			newShell.setText(Messages.getString("tmxeditor.addElementAttrDialog.title"));
		} else {
			newShell.setText(Messages.getString("tmxeditor.addElementAttrDialog.addFixAttr.title"));
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite compostie = new Composite(parent, SWT.NONE);
		compostie.setLayout(new GridLayout(2, false));
		GridDataFactory.createFrom(new GridData(GridData.FILL_BOTH)).hint(500, SWT.DEFAULT).applyTo(compostie);

		// 通用 GridData
		GridData gdText = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).create();
		// 自定义属性名称
		Label nameLbl = new Label(compostie, SWT.NONE);
		nameLbl.setText(Messages.getString("tmxeditor.addElementAttrDialog.attrName"));
		txtName = new Text(compostie, SWT.BORDER);
		GridDataFactory.createFrom(gdText).applyTo(txtName);
		txtName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String str = txtName.getText();
				if (str.isEmpty() || hasAttr(str)) {
					setFlag(okButtonFlag, -1);
				} else {
					attr.name = txtName.getText();
					setFlag(okButtonFlag, 1);
				}
			}
		});
		// 自定义属性值
		Label valueLbl = new Label(compostie, SWT.NONE);
		valueLbl.setText(Messages.getString("tmxeditor.addElementAttrDialog.attrValue"));
		txtValue = new Text(compostie, SWT.BORDER);
		GridDataFactory.createFrom(gdText).applyTo(txtValue);
		txtValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (txtValue.getText().isEmpty()) {
					setFlag(okButtonFlag, -2);
				} else {
					attr.value = txtValue.getText();
					setFlag(okButtonFlag, 2);
				}
			}
		});
		// 作用组
		Group applyGroup = new Group(compostie, SWT.NONE);
		applyGroup.setText(Messages.getString("tmxeditor.addElementAttrDialog.apply"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).applyTo(applyGroup);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(applyGroup);
		// 当前选中行
		btnCurrentSelected = new Button(applyGroup, SWT.RADIO);
		btnCurrentSelected.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnCurrentSelected.setText(Messages.getString("tmxeditor.addElementAttrDialog.current.line"));
		btnCurrentSelected.setSelection(true);
		btnCurrentSelected.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnCurrentSelected.getSelection()) {
					attr.scope = PropertiesView.SELECTED_TU;
				}
			}
		});

		// 所有过滤结果
		btnFiltered = new Button(applyGroup, SWT.RADIO);
		btnFiltered.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnFiltered.setText(Messages.getString("tmxeditor.addElementAttrDialog.allFilterResults"));
		btnFiltered.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnFiltered.getSelection()) {
					attr.scope = PropertiesView.FILTERED_TU;
				}
			}
		});

		// 整个文件/记忆库
		btnAllTu = new Button(applyGroup, SWT.RADIO);
		btnAllTu.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnAllTu.setText(Messages.getString("tmxeditor.addElementAttrDialog.allFileDataabase"));
		txtErrorInfo = new Text(parent, SWT.READ_ONLY);
		GridDataFactory.createFrom(new GridData(GridData.FILL_BOTH)).span(1, 2).align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(txtErrorInfo);
		return compostie;
	}

	private boolean hasAttr(String str) {
		if (tu != null) {
			if ("creationuser".equalsIgnoreCase(str.trim()) && tu.getCreationUser() != null
					|| "creationdate".equalsIgnoreCase(str.trim()) && tu.getCreationDate() != null
					|| "changedate".equalsIgnoreCase(str.trim()) && tu.getChangeDate() != null
					|| "changeuser".equalsIgnoreCase(str.trim()) && tu.getChangeUser() != null
					|| "creationtool".equalsIgnoreCase(str.trim()) && tu.getCreationTool() != null
					|| "creationtoolversion".equalsIgnoreCase(str.trim()) && tu.getCreationToolVersion() != null
					|| tu.getAttributes() != null && tu.getAttributes().containsKey(str.trim())) {
				txtErrorInfo.setText(MessageFormat.format(Messages.getString("tmxeditor.addElementAttrDialog.addWarn.msg1"), str));
				return true;
			}
		} else if (tuv != null) {
			if ("creationuser".equals(str.trim()) && tuv.getCreationUser() != null || "creationdate".equals(str.trim())
					&& tuv.getCreationDate() != null || "changedate".equals(str.trim()) && tuv.getChangeDate() != null
					|| "changeuser".equals(str.trim()) && tuv.getChangeUser() != null
					|| "creationtool".equals(str.trim()) && tuv.getCreationTool() != null
					|| "creationtoolversion".equals(str.trim()) && tuv.getCreationToolVersion() != null
					|| tuv.getAttributes() != null && tuv.getAttributes().containsKey(str.trim())) {
				txtErrorInfo.setText(MessageFormat.format(Messages.getString("tmxeditor.addElementAttrDialog.addWarn.msg1"), str));
				return true;
			}
		}
		txtErrorInfo.setText("");
		return false;
	}

	private void setFlag(int flag, int i) {
		int p = Math.abs(i);
		if (i > 0) {
			okButtonFlag = okButtonFlag | (1 << (p - 1));
		} else if (i < 0) {
			okButtonFlag = (okButtonFlag | (1 << (p - 1))) ^ (1 << (p - 1));
		}
		getButton(Dialog.OK).setEnabled(okButtonFlag == Math.pow(2, 2) - 1);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(Dialog.OK).setEnabled(false);
	}

	@Override
	protected void okPressed() {
		// if (tu != null) {
		// if ("creationuser".equalsIgnoreCase(attr.name)) {
		// tu.setCreationUser(attr.value);
		// } else if ("creationdate".equalsIgnoreCase(attr.name)) {
		// tu.setCreationDate(attr.value);
		// } else if ("changedate".equalsIgnoreCase(attr.name)) {
		// tu.setChangeDate(attr.value);
		// } else if ("changeuser".equalsIgnoreCase(attr.name)) {
		// tu.setChangeUser(attr.value);
		// } else if ("creationtool".equalsIgnoreCase(attr.name)) {
		// tu.setCreationTool(attr.value);
		// } else if ("creationtoolversion".equalsIgnoreCase(attr.name)) {
		// tu.setCreationToolVersion(attr.value);
		// } else {
		// if (tu.getAttributes() == null) {
		// tu.setAttributes(new HashMap<String, String>());
		// }
		// tu.getAttributes().put(attr.name.trim(), attr.value);
		// }
		// } else {
		// if (tuv != null) {
		// if ("creationuser".equalsIgnoreCase(attr.name)) {
		// tuv.setCreationUser(attr.value);
		// } else if ("creationdate".equalsIgnoreCase(attr.name)) {
		// tuv.setCreationDate(attr.value);
		// } else if ("changedate".equalsIgnoreCase(attr.name)) {
		// tuv.setChangeDate(attr.value);
		// } else if ("changeuser".equalsIgnoreCase(attr.name)) {
		// tuv.setChangeUser(attr.value);
		// } else if ("creationtool".equalsIgnoreCase(attr.name)) {
		// tuv.setCreationTool(attr.value);
		// } else if ("creationtoolversion".equalsIgnoreCase(attr.name)) {
		// tuv.setCreationToolVersion(attr.value);
		// } else {
		// if (tuv.getAttributes() == null) {
		// tuv.setAttributes(new HashMap<String, String>());
		// }
		// tuv.getAttributes().put(attr.name.trim(), attr.value);
		// }
		// }
		// }

		if (btnAllTu.getSelection()) {
			attr.scope = PropertiesView.ALL_TU;
		} else if (btnCurrentSelected.getSelection()) {
			attr.scope = PropertiesView.SELECTED_TU;
		} else {
			attr.scope = PropertiesView.FILTERED_TU;
		}
		super.okPressed();
	}

	public static void main(String[] args) {
		System.out.println(Math.pow(2, 3));
	}
}
