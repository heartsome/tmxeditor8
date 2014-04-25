package net.heartsome.cat.te.tmxeditor.view;

import net.heartsome.cat.te.tmxeditor.resource.Messages;
import net.heartsome.cat.te.tmxeditor.view.PropertiesView.ElemCollector;

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
 * 添加 note 或 prop 节点
 * @author Austen 2013-06-18
 */
public class AddElementDialog extends Dialog {

	private int category = -1;

	protected ElemCollector elem;

	protected Button btnAllTu;
	protected Button btnFiltered;
	protected Button btnCurrentSelected;

	private Text txtNoteContent;
	private Text txtPropContent;

	private Text txtPropType;

	public AddElementDialog(Shell parentShell, int category, ElemCollector elem) {
		super(parentShell);
		this.category = category;
		this.elem = elem;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (category == PropertiesView.TU_NODE_NOTE) {
			newShell.setText(Messages.getString("tmxeditor.addElementDialog.title"));
		} else if (category == PropertiesView.TU_NODE_PROPS){
			newShell.setText(Messages.getString("tmxeditor.addElementDialog.addFixAttr.title"));
		} else if (category == PropertiesView.TU_PROP_COPY) {
			newShell.setText(Messages.getString("tmxeditor.addElementDialog.pasteprop.title"));
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite compostie = new Composite(parent, SWT.NONE);
		compostie.setLayout(new GridLayout(2, false));
		GridDataFactory.createFrom(new GridData(GridData.FILL_BOTH)).hint(500, SWT.DEFAULT).applyTo(compostie);

		if (category == PropertiesView.TU_NODE_NOTE) {
			createNoteComposite(compostie);
		} else if (category == PropertiesView.TU_NODE_PROPS) {
			creatPropComposite(compostie);
		} else if (category == PropertiesView.TU_PROP_COPY) {
			creatCopyComposite(compostie);
		}
		// 作用组
		Group applyGroup = new Group(compostie, SWT.NONE);
		applyGroup.setText(Messages.getString("tmxeditor.addElementDialog.apply"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(applyGroup);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(applyGroup);

		if (category != PropertiesView.TU_NODE_NOTE) {
			final Button btnOverwrite = new Button(compostie, SWT.CHECK);
			btnOverwrite.setText(Messages.getString("tmxeditor.addElementDialog.overwrite"));
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).applyTo(btnOverwrite);
			btnOverwrite.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					elem.overwrite = btnOverwrite.getSelection();
				}
			});
			btnOverwrite.setSelection(true);
			elem.overwrite = btnOverwrite.getSelection();
		}

		// 当前选中行
		btnCurrentSelected = new Button(applyGroup, SWT.RADIO);
		btnCurrentSelected.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnCurrentSelected.setText(Messages.getString("tmxeditor.addElementAttrDialog.current.line"));
		btnCurrentSelected.setSelection(true);
		btnCurrentSelected.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnCurrentSelected.getSelection()) {
					elem.scope = PropertiesView.SELECTED_TU;
				}
			}
		});

		// 所有过滤结果
		btnFiltered = new Button(applyGroup, SWT.RADIO);
		btnFiltered.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnFiltered.setText(Messages.getString("tmxeditor.addElementDialog.allFilterResults"));
		btnFiltered.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnFiltered.getSelection()) {
					elem.scope = PropertiesView.FILTERED_TU;
				}
			}
		});

		// 整个文件/记忆库
		btnAllTu = new Button(applyGroup, SWT.RADIO);
		btnAllTu.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnAllTu.setText(Messages.getString("tmxeditor.addElementDialog.allFileDataabase"));
		return compostie;
	}

	private void creatCopyComposite(Composite compostie) {
		// do nothing
	}

	private void createNoteComposite(Composite compostie) {
		// 自定义属性名称
		Label lblNote = new Label(compostie, SWT.NONE);
		lblNote.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		lblNote.setText(Messages.getString("tmxeditor.addElementDialog.noteContent"));
		txtNoteContent = new Text(compostie, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		txtNoteContent.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getButton(Dialog.OK).setEnabled(!txtNoteContent.getText().isEmpty());
			}
		});
		GridDataFactory.swtDefaults().hint(SWT.DEFAULT, 60).span(2, 1).align(SWT.FILL, SWT.FILL).grab(true, true)
				.applyTo(txtNoteContent);
	}

	private void creatPropComposite(Composite compostie) {
		// 通用 GridData
		GridData gdText = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).create();
		Label attrNameLbl = new Label(compostie, SWT.NONE);
		attrNameLbl.setText(Messages.getString("tmxeditor.addElementDialog.attrNameLbl"));
		txtPropType = new Text(compostie, SWT.BORDER);
		txtPropType.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getButton(Dialog.OK).setEnabled(
						!(txtPropType.getText().trim().isEmpty() || txtPropContent.getText().trim().isEmpty()));
			}
		});
		GridDataFactory.createFrom(gdText).applyTo(txtPropType);
		// 自定义属性名称
		Label lblProp = new Label(compostie, SWT.NONE);
		lblProp.setText(Messages.getString("tmxeditor.addElementDialog.customAttrContent"));
		txtPropContent = new Text(compostie, SWT.BORDER);
		txtPropContent.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getButton(Dialog.OK).setEnabled(
						!(txtPropType.getText().trim().isEmpty() || txtPropContent.getText().trim().isEmpty()));
			}
		});
		GridDataFactory.createFrom(gdText).applyTo(txtPropContent);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(Dialog.OK).setEnabled(false);
	}

	@Override
	protected void okPressed() {

		if (category == PropertiesView.TU_NODE_NOTE) {
			elem.content = txtNoteContent.getText();
		} else {
			elem.name = txtPropType.getText();
			elem.content = txtPropContent.getText();
		}
		if (btnAllTu.getSelection()) {
			elem.scope = PropertiesView.ALL_TU;
		} else if (btnCurrentSelected.getSelection()) {
			elem.scope = PropertiesView.SELECTED_TU;
		} else {
			elem.scope = PropertiesView.FILTERED_TU;
		}
		super.okPressed();
	}
}
