/**
 * TmxValidatorDialog.java
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

package net.heartsome.cat.te.ui.tmxvalidator.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.core.utils.tmxvalidator.TmxValidator;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog
 * @author austen 2013-08-14
 * @version
 * @since JDK1.6
 */

public class TmxValidatorDialog extends HsAbstractProgressDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(TmxValidatorDialog.class);

	private Text txtTmxFilePath;
	private StyledText styledText;

	private Button btnVerifyTmxFile;

	public TmxValidatorDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public Composite createClientArea(Composite composite) {
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(composite);

		// row
		Label lblTmxFile = new Label(composite, SWT.NONE);
		lblTmxFile.setText(Messages.getString("dialog.TmxValidatorDialog.label.tmxfile"));
		txtTmxFilePath = new Text(composite, SWT.BORDER);
		txtTmxFilePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtTmxFilePath.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				File file = new File(txtTmxFilePath.getText());
				getButton(IDialogConstants.OK_ID).setEnabled(file.exists());
			}
		});
		Button btnBrowse = new Button(composite, SWT.NONE);
		btnBrowse.setText(Messages.getString("newtmx.NewTmxFileDialog.browseBtn"));
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openFile();
			}
		});// end row

		// row
		styledText = new StyledText(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.READ_ONLY);
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		styledText.setText(""); // end row

		return composite;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.TmxValidatorDialog.label.tmxtitle"));
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 400);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnVerifyTmxFile = createButton(parent, IDialogConstants.OK_ID,
				Messages.getString("dialog.TmxValidatorDialog.label.verify"), true);
		btnVerifyTmxFile.setEnabled(false);
	}

	@Override
	protected void okPressed() {
		styledText.setText("");
		final String tmxFilePath = txtTmxFilePath.getText();
		try {
			run(isFork(), canCancel(), new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					TmxValidator validator = new TmxValidator(monitor);
					validator.setReport(true);
					validator.setNormalize(true);
					validator.setDebug(true);
					validator.setStyledText(styledText);
					validator.validate(tmxFilePath);
				}
			});
		} catch (Exception e) {
			LOGGER.error("", e);
			OpenMessageUtils.openMessage(IStatus.ERROR, e.getMessage());
		}
	}

	@Override
	public String getDisplayHelpUrl() {
		return "";
	}

	private void openFile() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		String[] extensions = { "*.tmx" }; //$NON-NLS-1$ //$NON-NLS-2$
		fd.setFilterExtensions(extensions);
		String[] names = { Messages.getString("dialog.TmxValidatorDialog.label.tmxex") };
		/* Messages.getString("dialog.TmxValidatorDialog.label.allex" */
		fd.setFilterNames(names);
		String path = fd.open();
		txtTmxFilePath.setText(path == null ? "" : path);
	}
}
