package net.heartsome.cat.te.ui.encodingconveter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;

import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncodingConverterDialog extends HsAbstractProgressDialog {
	public static final Logger LOGGER = LoggerFactory.getLogger(EncodingConverterDialog.class);
	private Text srcFileText;
	private Text tgtFileText;
	private Combo srcEndcodingCombo;
	private Combo tgtEncodingComb;

	private String tgtFilePath;
	private String srcFilePath;
	private Button srcFileBtn;
	private boolean finishClose;
	private String newFilePath;

	public EncodingConverterDialog(Shell parentShell, String filePath) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.TITLE);
		this.srcFilePath = filePath;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.EncodingConverterDialog.title"));
		setOkBtnEnable(true);
	}

	@Override
	protected void okPressed() {
		if (srcFilePath == null || srcFilePath.length() == 0) {
			OpenMessageUtils
					.openMessage(IStatus.ERROR, Messages.getString("dialog.EncodingConverterDialog.srcFileMsg"));
			return;
		}
		final String srcEncoding = srcEndcodingCombo.getText();
		if (srcEncoding.length() == 0) {
			OpenMessageUtils.openMessage(IStatus.ERROR,
					Messages.getString("dialog.EncodingConverterDialog.srcEncodingMsg"));
			return;
		}
		if (tgtFilePath == null || tgtFilePath.length() == 0) {
			OpenMessageUtils
					.openMessage(IStatus.ERROR, Messages.getString("dialog.EncodingConverterDialog.tgtFileMsg"));
			return;
		}
		final String tgtFileEncoding = tgtEncodingComb.getText();
		if (tgtFileEncoding.length() == 0) {
			OpenMessageUtils.openMessage(IStatus.ERROR,
					Messages.getString("dialog.EncodingConverterDialog.tgtEncodingMsg"));
			return;
		}
		if(tgtFilePath.equalsIgnoreCase(srcFilePath)){
			OpenMessageUtils.openMessage(IStatus.ERROR,
					Messages.getString("dialog.EncodingConverterDialog.tgtSameSrcPathMsg"));
			return;
		}
		File f = new File(tgtFilePath);
		if (f.exists()) {
			if (!OpenMessageUtils.openConfirmMessage(Messages.getString("dialog.EncodingConverterDialog.tgtFileExist"))) {
				return;
			}
		}
		try {
			run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					newFilePath = null;
					BufferedReader reader = null;
					BufferedWriter writer = null;
					try {
						File srcfile = new File(srcFilePath);
						long size = srcfile.length();
						reader = new BufferedReader(new InputStreamReader(new FileInputStream(srcfile), srcEncoding));
						writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tgtFilePath),
								tgtFileEncoding));
						monitor.beginTask(Messages.getString("dialog.EncodingConverterDialog.convertTask"), (int) size);
						monitor.worked(10);
						int count = -1;
						char[] cbuf = new char[8192];
						count = reader.read(cbuf);
						if (count > 0) {
							StringBuilder sb = new StringBuilder();
							sb.append(cbuf, 0, 500); // 取前100个字符进行判断
							int idx = sb.indexOf("<?xml");
							if (idx != -1) { // is xml
								int endIdx = sb.indexOf("?>");
								if (endIdx != -1) {
									sb.replace(idx, endIdx + 2, "<?xml version=\"1.0\" encoding=\"" + tgtFileEncoding
											+ "\"?>");
									writer.write(sb.toString());
									writer.write(cbuf, 500, count - 500);
									count = reader.read(cbuf);
								}
							}
						}
						while (count > 0) {
							monitor.worked(count * 2);
							writer.write(cbuf, 0, count);
							count = reader.read(cbuf);
						}
						writer.flush();
						monitor.done();
						newFilePath = tgtFilePath;
					} catch (Exception e) {
						LOGGER.error("", e);
						newFilePath = null;
						return;
					} finally {
						try {
							reader.close();
							writer.close();
						} catch (Exception e) { // do nothing
							LOGGER.error("", e);
						}
					}
				}
			});
			if (tgtFilePath != null) {
				OpenMessageUtils.openMessage(IStatus.INFO,
						Messages.getString("dialog.EncodingConverterDialog.covnertFinish"));
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}

		if (finishClose) {
			close();
		}
	}

	public String getNewFilePath() {
		return newFilePath;
	};

	@Override
	public Composite createClientArea(Composite clientContainer) {
		clientContainer.setLayout(new GridLayout(3, false));

		Label srcLbl = new Label(clientContainer, SWT.NONE);
		srcLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		srcLbl.setText(Messages.getString("dialog.EncodingConverterDialog.srcLabel"));

		srcFileText = new Text(clientContainer, SWT.BORDER | SWT.READ_ONLY);
		srcFileText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		srcFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		srcFileBtn = new Button(clientContainer, SWT.NONE);
		srcFileBtn.setText(Messages.getString("dialog.EncodingConverterDialog.srcBrowseLabel"));
		srcFileBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fDialog = new FileDialog(getShell(), SWT.SINGLE);
				fDialog.setFilterExtensions(new String[] { "*.tmx", "*.*" });
				String path = fDialog.open();
				if (path == null || path.length() == 0) {
					return;
				}
				File f = new File(path);
				String encoding = FileEncodingDetector.detectFileEncoding(f);
				srcEndcodingCombo.setText(encoding);
				srcFileText.setText(path);
				srcFilePath = path;
//				tgtFilePath = fileTargetFile(srcFilePath, "UTF-8");
//				tgtFileText.setText(tgtFilePath);
//				tgtEncodingComb.setText("UTF-8");
			}
		});

		Label srcFileEncodingLbl = new Label(clientContainer, SWT.NONE);
		srcFileEncodingLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		srcFileEncodingLbl.setText(Messages.getString("dialog.EncodingConverterDialog.srcEncodingLabel"));

		srcEndcodingCombo = new Combo(clientContainer, SWT.NONE | SWT.READ_ONLY);
		GridData gdSrcECombo = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gdSrcECombo.widthHint = 130;
		srcEndcodingCombo.setLayoutData(gdSrcECombo);
		new Label(clientContainer, SWT.NONE);

		Label spLbl = new Label(clientContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
		spLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));

		Label tgtSrcLbl = new Label(clientContainer, SWT.NONE);
		tgtSrcLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		tgtSrcLbl.setText(Messages.getString("dialog.EncodingConverterDialog.tgtFileLabel"));

		tgtFileText = new Text(clientContainer, SWT.BORDER | SWT.READ_ONLY);
		tgtFileText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		tgtFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button tgtBtn = new Button(clientContainer, SWT.NONE);
		tgtBtn.setText(Messages.getString("dialog.EncodingConverterDialog.tgtBrowseLabel"));
		tgtBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fDialog = new FileDialog(getShell(), SWT.SAVE | SWT.SINGLE);
				fDialog.setFilterExtensions(new String[] {"*.tmx" ,"*.*" });
				String path = fDialog.open();
				if (path == null || path.length() == 0) {
					return;
				}
				tgtFileText.setText(path);
				tgtFilePath = path;
			}
		});

		Label tgtEncodingLbl = new Label(clientContainer, SWT.NONE);
		tgtEncodingLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		tgtEncodingLbl.setText(Messages.getString("dialog.EncodingConverterDialog.tgtEncodingLabel"));

		tgtEncodingComb = new Combo(clientContainer, SWT.NONE | SWT.READ_ONLY);
		GridData gdTgtECombo = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gdTgtECombo.widthHint = 130;
		tgtEncodingComb.setLayoutData(gdTgtECombo);
		new Label(clientContainer, SWT.NONE);

		initData();
		return null;
	}

	private void initData() {
		String[] codeArray = LocaleService.getPageCodes();
		srcEndcodingCombo.setItems(codeArray);
		tgtEncodingComb.setItems(codeArray);
		if (srcFilePath != null && srcFilePath.length() != 0) {
			File f = new File(srcFilePath);
			if (f.exists()) {
				String encoding = FileEncodingDetector.detectFileEncoding(f);
				srcEndcodingCombo.setText(encoding);
				srcFileText.setText(srcFilePath);
				tgtFilePath = fileTargetFile(srcFilePath, "UTF-8");
				tgtFileText.setText(tgtFilePath);
				tgtEncodingComb.setText("UTF-8");
				finishClose = true;
			}
			srcFileBtn.setEnabled(false);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("dialog.EncodingConverterDialog.convertBtn"),
				true);
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("dialog.EncodingConverterDialog.closeBtn"),
				false);
	}

	private String fileTargetFile(String srcFilePath, String encoding) {
		int idx = srcFilePath.lastIndexOf('.');
		String path = "";
		if (idx != -1) {
			String pre = srcFilePath.substring(0, idx);
			String ext = srcFilePath.substring(idx, srcFilePath.length());
			path = pre + "_" + encoding + ext;
		} else {
			path = srcFilePath + encoding;
		}
		return path;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog#getDisplayHelpUrl()
	 */
	@Override
	public String getDisplayHelpUrl() {
		return "/net.heartsome.cat.te.ui.help/html/{0}/ch07.html#code_trans_id";
	}
}
