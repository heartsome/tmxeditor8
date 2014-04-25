package net.heartsome.cat.te.tmxeditor.editor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.core.bean.ExportBean;
import net.heartsome.cat.te.tmxeditor.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ExportDialog extends HsAbstractProgressDialog implements IRunnableWithProgress {
	private Text textNewFile;
	private Text textAppendFile;

	private TmxEditor editor;
	private Button allFilterBtn;
	private Button selectedBtn;
	private Button allBtn;

	private Button newFileBtn;
	private Button appendBtn;

	private Button bBtn1;
	private Button bBtn2;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ExportDialog(Shell parentShell, TmxEditor editor) {
		super(parentShell);
		this.editor = editor;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("Dialog.ExportDialog.dialogTitle")); //$NON-NLS-1$
	}

	@Override
	public Composite createClientArea(Composite clientContainer) {
		clientContainer.setLayout(new GridLayout(1, false));

		Group group = new Group(clientContainer, SWT.NONE);
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setText(Messages.getString("Dialog.ExportDialog.exportGroupTitle")); //$NON-NLS-1$

		allFilterBtn = new Button(group, SWT.RADIO);
		allFilterBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		allFilterBtn.setText(Messages.getString("Dialog.ExportDialog.allFilterRadioBtn")); //$NON-NLS-1$
		allFilterBtn.setSelection(true);

		selectedBtn = new Button(group, SWT.RADIO);
		selectedBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		selectedBtn.setText(Messages.getString("Dialog.ExportDialog.selecteRowRadioBtn")); //$NON-NLS-1$

		allBtn = new Button(group, SWT.RADIO);
		allBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		allBtn.setText(Messages.getString("Dialog.ExportDialog.allTmxDbRadioBtn")); //$NON-NLS-1$

		Group groupSave = new Group(clientContainer, SWT.NONE);
		groupSave.setLayout(new GridLayout(3, false));
		groupSave.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		groupSave.setText(Messages.getString("Dialog.ExportDialog.saveTytpeGroupTitle")); //$NON-NLS-1$

		newFileBtn = new Button(groupSave, SWT.RADIO);
		newFileBtn.setText(Messages.getString("Dialog.ExportDialog.newTmxLabel")); //$NON-NLS-1$
		newFileBtn.setSelection(true);
		newFileBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				bBtn1.setEnabled(true);
				bBtn2.setEnabled(false);
			}
		});

		textNewFile = new Text(groupSave, SWT.BORDER);
		textNewFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textNewFile.setEditable(false);

		bBtn1 = new Button(groupSave, SWT.NONE);
		bBtn1.setText(Messages.getString("Dialog.ExportDialog.browserLabel")); //$NON-NLS-1$
		bBtn1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
				String[] filterExt = { "*.tmx", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
				dlg.setFilterExtensions(filterExt);
				final String filePath = dlg.open();
				if (filePath == null) {
					return;
				}
				textNewFile.setText(filePath);
				// File file = new File(filePath);
				// if (file.exists()) {
				// String msg = Messages.getString("tmxeditor.tmxEditorViewer.saveAsMsg1");
				// msg = MessageFormat.format(msg, filePath);
				// if (!OpenMessageUtils.openConfirmMessage(msg)) {
				// return;
				// }
				// }
			}
		});

		appendBtn = new Button(groupSave, SWT.RADIO);
		appendBtn.setText(Messages.getString("Dialog.ExportDialog.appendTmxLabel")); //$NON-NLS-1$
		appendBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				bBtn1.setEnabled(false);
				bBtn2.setEnabled(true);
			}
		});
		textAppendFile = new Text(groupSave, SWT.BORDER);
		textAppendFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textAppendFile.setEditable(false);

		bBtn2 = new Button(groupSave, SWT.NONE);
		bBtn2.setText(Messages.getString("Dialog.ExportDialog.browserLabel")); //$NON-NLS-1$
		bBtn2.setEnabled(false);
		bBtn2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell(), SWT.SINGLE | SWT.SELECTED);
				String[] filterExt = { "*.tmx", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
				dlg.setFilterExtensions(filterExt);
				final String filePath = dlg.open();
				if (filePath == null) {
					return;
				}
				textAppendFile.setText(filePath);
			}
		});
		return clientContainer;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		final ExportBean b = new ExportBean();
		getShell().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				int exportScope = -1;
				if (allFilterBtn.getSelection()) {
					exportScope = 1;
				} else if (selectedBtn.getSelection()) {
					exportScope = 2;
				} else if (allBtn.getSelection()) {
					exportScope = 3;
				}
				b.setExportScope(exportScope);
				String filePth = null;
				if (newFileBtn.getSelection()) {
					filePth = textNewFile.getText();
				} else {
					filePth = textAppendFile.getText();
				}
				b.setTargetFile(filePth);
				if (exportScope == 2) {
					String[] ida = editor.getSelectIdentifiers();
					List<String> ids = Arrays.asList(ida);
					b.setSelectIds(ids);
				}
				b.setAppend(appendBtn.getSelection());
			}
		});
		editor.getTmxDataAccess().saveAs(monitor, b);
	}

	public boolean validate() {
		if (newFileBtn.getSelection()) {
			String filePth = textNewFile.getText();
			if (filePth == null || filePth.length() == 0) {
				String msg = Messages.getString(Messages.getString("Dialog.ExportDialog.selectTmxMsg")); //$NON-NLS-1$
				OpenMessageUtils.openMessage(IStatus.INFO, msg);
				return false;
			}
			File file = new File(filePth);
			if (file.exists()) {
				String msg = Messages.getString("tmxeditor.tmxEditorViewer.saveAsMsg1"); //$NON-NLS-1$
				msg = MessageFormat.format(msg, filePth);
				if (!OpenMessageUtils.openConfirmMessage(msg)) {
					return false;
				}
			}
		} else {
			String filePth = textAppendFile.getText();
			if (filePth == null || filePth.length() == 0) {
				String msg = Messages.getString(Messages.getString("Dialog.ExportDialog.appendTmxMsg")); //$NON-NLS-1$
				OpenMessageUtils.openMessage(IStatus.ERROR, msg);
				return false;
			}
			File file = new File(filePth);
			if (!file.exists()) {
				String msg = Messages.getString(Messages.getString("Dialog.ExportDialog.appendTmxNotExistMsg")); //$NON-NLS-1$
				OpenMessageUtils.openMessage(IStatus.INFO, msg);
			}
		}
		return true;
	}

	@Override
	protected void okPressed() {
		if (!validate()) {
			return;
		}
		super.okPressed();
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

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(548, 320);
	}
}
