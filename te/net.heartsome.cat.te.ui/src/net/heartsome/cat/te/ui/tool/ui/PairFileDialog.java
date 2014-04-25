package net.heartsome.cat.te.ui.tool.ui;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.ui.languagesetting.LanguageLabelProvider;
import net.heartsome.cat.te.core.bean.PairFileBean;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PairFileDialog extends Dialog {
	
	private List<Language> langstore = new ArrayList<Language>(LocaleService.getDefaultLanguage().values());
	private PairFileBean bean;
	
	private Text txt1File;
	private Text txt2File;
	private Text txtSaveAs;
	private TableComboViewer tcv1Lang;
	private TableComboViewer tcv2Lang;
	private boolean machineTrans = false;
	private Button btnOpenEditor;

	Color colorlblMachine = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);

	/**
	 * Create the dialog.
	 * @param parentShell
	 * @param bean
	 */
	public PairFileDialog(Shell parentShell, PairFileBean bean) {
		super(parentShell);
		this.bean = bean;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.PairFileDialog.title"));
	}
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Composite cmpFileChose = new Composite(container, SWT.NONE);
		cmpFileChose.setLayoutData(GridDataFactory.createFrom(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1)).hint(800, SWT.DEFAULT).create());
		cmpFileChose.setLayout(new FillLayout(SWT.HORIZONTAL));

		Group group1File = new Group(cmpFileChose, SWT.NONE);
		group1File.setText(Messages.getString("dialog.PairFileDialog.srcGroupFile.lbl"));
		group1File.setToolTipText("");
		group1File.setLayout(new GridLayout(3, false));

		Label lbl1File = new Label(group1File, SWT.NONE);
		lbl1File.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lbl1File.setText(Messages.getString("dialog.PairFileDialog.srcGroup.File"));

		txt1File = new Text(group1File, SWT.BORDER | SWT.READ_ONLY);
		txt1File.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btn1FileBrowse = new Button(group1File, SWT.NONE);
		btn1FileBrowse.setText(Messages.getString("dialog.PairFileDialog.srcGroup.brower"));
		btn1FileBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txt1File.setText(openFile());

			}
		});

		Label lbl1Lang = new Label(group1File, SWT.NONE);
		lbl1Lang.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lbl1Lang.setText(Messages.getString("dialog.PairFileDialog.srcGroup.lang"));

		tcv1Lang = new TableComboViewer(group1File, SWT.READ_ONLY | SWT.BORDER);
		final TableCombo tc1 = tcv1Lang.getTableCombo();
		tc1.setShowTableLines(false);
		tc1.setShowTableHeader(false);
		tc1.setDisplayColumnIndex(-1);
		tc1.setShowImageWithinSelection(true);
		tc1.setShowColorWithinSelection(false);
		tc1.setShowFontWithinSelection(false);
		tc1.setVisibleItemCount(20);
		tc1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tcv1Lang.setLabelProvider(new LanguageLabelProvider());
		tcv1Lang.setContentProvider(new ArrayContentProvider());
		tcv1Lang.setInput(langstore);

		Group group2File = new Group(cmpFileChose, SWT.NONE);
		group2File.setText(Messages.getString("dialog.PairFileDialog.tgtGroupFile.lbl"));
		group2File.setLayout(new GridLayout(3, false));

		Label lbl2File = new Label(group2File, SWT.NONE);
		lbl2File.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lbl2File.setText(Messages.getString("dialog.PairFileDialog.tgtGroup.File"));

		txt2File = new Text(group2File, SWT.BORDER | SWT.READ_ONLY);
		txt2File.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btn2FileBrowse = new Button(group2File, SWT.NONE);
		btn2FileBrowse.setText(Messages.getString("dialog.PairFileDialog.tgtGroup.brower"));
		btn2FileBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txt2File.setText(openFile());
			}
		});

		Label lbl2Lang = new Label(group2File, SWT.NONE);
		lbl2Lang.setText(Messages.getString("dialog.PairFileDialog.tgtGroup.lang"));

		tcv2Lang = new TableComboViewer(group2File, SWT.READ_ONLY | SWT.BORDER);
		final TableCombo tc2 = tcv2Lang.getTableCombo();
		tc2.setShowTableLines(false);
		tc2.setShowTableHeader(false);
		tc2.setDisplayColumnIndex(-1);
		tc2.setShowImageWithinSelection(true);
		tc2.setShowColorWithinSelection(false);
		tc2.setShowFontWithinSelection(false);
		tc2.setVisibleItemCount(20);
		tc2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tcv2Lang.setLabelProvider(new LanguageLabelProvider());
		tcv2Lang.setContentProvider(new ArrayContentProvider());
		tcv2Lang.setInput(langstore);

		Composite cmpSaveAs = new Composite(container, SWT.NONE);
		cmpSaveAs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cmpSaveAs.setLayout(new GridLayout(3, false));

		Label lblSaveAs = new Label(cmpSaveAs, SWT.NONE);
		lblSaveAs.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblSaveAs.setText(Messages.getString("dialog.PairFileDialog.saveAsPath"));

		txtSaveAs = new Text(cmpSaveAs, SWT.BORDER | SWT.READ_ONLY);
		txtSaveAs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnBrowseSaveAs = new Button(cmpSaveAs, SWT.NONE);
		btnBrowseSaveAs.setText(Messages.getString("dialog.PairFileDialog.saveAsBrower"));
		btnBrowseSaveAs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getSaveURI();
			}
		});

		Composite cmpMachineTrans = new Composite(container, SWT.NONE);
		cmpMachineTrans.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmpMachineTrans.setLayout(new GridLayout(2, false));

		Label lblMachineTrans = new Label(cmpMachineTrans, SWT.NONE);
		lblMachineTrans.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblMachineTrans.setText(Messages.getString("dialog.PairFileDialog.start.mechieTranslate"));

		final Button btnMachineTrans = new Button(cmpMachineTrans, SWT.NONE);
		btnMachineTrans.setText(Messages.getString("dialog.PairFileDialog.start"));
		btnMachineTrans.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (machineTrans = !machineTrans) {
					btnMachineTrans.setText(Messages.getString("dialog.PairFileDialog.aready.start"));
				} else {
					btnMachineTrans.setText(Messages.getString("dialog.PairFileDialog.start"));
				}
			}
		});
		
		Composite cmpOpenInTmxEditor = new Composite(container, SWT.NONE);
		cmpOpenInTmxEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmpOpenInTmxEditor.setLayout(new FillLayout(SWT.HORIZONTAL));

		btnOpenEditor = new Button(cmpOpenInTmxEditor, SWT.CHECK);
		btnOpenEditor.setText(Messages.getString("dialog.PairFileDialog.openTmx"));

		Label seprator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		seprator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		return container;
	}

	protected String openFile() {
		FileDialog dialog = new FileDialog(getParentShell().getShell(), SWT.OPEN);
		String path = dialog.open();
		return path == null ? "" : path;
	}

	protected void getSaveURI() {
		FileDialog dialog = new FileDialog(getParentShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] { "*.tmx" });
		dialog.setOverwrite(true);
		String path = dialog.open();
		if (path != null) {
			txtSaveAs.setText(path);
		}
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
	public boolean close() {
		if (!colorlblMachine.isDisposed()) {
			colorlblMachine.dispose();
		}
		colorlblMachine = null;
		return super.close();
	}

	@Override
	protected void okPressed() {
		bean.setSavePath(txtSaveAs.getText());
		bean.setSrcLanguage((Language)((StructuredSelection)tcv1Lang.getSelection()).getFirstElement());
		bean.setSrcPath(txt1File.getText());
		bean.setTgtLanguage((Language)((StructuredSelection)tcv2Lang.getSelection()).getFirstElement());
		bean.setTgtPath(txt2File.getText());
		bean.setMachineTrans(machineTrans);
		bean.setOpenAfterPair(btnOpenEditor.getSelection());
		super.okPressed();
	}
}
