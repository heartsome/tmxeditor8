package net.heartsome.cat.te.ui.splitTmx;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.ui.Activator;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * 分割 tmx 文件的主窗体
 * @author  robert	2013-08-14
 * @version 
 * @since   JDK1.6
 */
public class SplitTmxDialog extends HsAbstractProgressDialog implements IRunnableWithProgress{
	private Spinner fileSumSpinner;
	private Button originalLcBtn;
	private Text splitFileLCText;
	private Text tgtFolderLCTxt;
	private Button saveAsBtn;
	private Button browerBtn;
	private String splitFileLC;
	private String tgtFolderLC;
	private int fileSum;

	protected SplitTmxDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.TITLE);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.SplitTmxDiialog.title"));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	/**
	 * 初始化对话框位置
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 350);
	}
	
	@Override
	public Composite createClientArea(Composite clientContainer) {
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(clientContainer);
		
		Group typeGroup = new Group(clientContainer, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(typeGroup);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(typeGroup);
		typeGroup.setText(Messages.getString("dialog.SplitTmxDiialog.groupTitle"));
		
		Label typeLbl = new Label(typeGroup, SWT.NONE);
		typeLbl.setText(Messages.getString("dialog.SplitTmxDiialog.group.avgSplit"));
		
		Composite spinnerCmp = new Composite(typeGroup, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).span(2, SWT.DEFAULT).applyTo(spinnerCmp);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(spinnerCmp);
		
		GridData spinnerdata = new GridData();
		spinnerdata.widthHint = 40;
		fileSumSpinner = new Spinner(spinnerCmp, SWT.BORDER);
		fileSumSpinner.setLayoutData(spinnerdata);
		fileSumSpinner.setMaximum(1000);
		fileSumSpinner.setMinimum(2);
		
		Label fileLbl = new Label(spinnerCmp, SWT.NONE);
		fileLbl.setText(Messages.getString("dialog.SplitTmxDiialog.group.subFile"));
		
		Label splitFileLCLbl = new Label(typeGroup, SWT.NONE);
		splitFileLCLbl.setText(Messages.getString("dialog.SplitTmxDiialog.spliteFile"));
		
		splitFileLCText = new Text(typeGroup, SWT.BORDER|SWT.READ_ONLY);
		splitFileLCText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(splitFileLCText);
		
		Button splitBrowerBtn = new Button(typeGroup, SWT.NONE);
		splitBrowerBtn.setText(Messages.getString("dialog.SplitTmxDiialog.brower"));
		splitBrowerBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[]{"*.tmx", "*.*"});
				String fileLC = dialog.open();
				if (fileLC != null) {
					splitFileLCText.setText(fileLC);
				}
				check();
			}
		});
		
		
		//　下面是存储路径
		Group lcGroup = new Group(clientContainer, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(lcGroup);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(lcGroup);
		lcGroup.setText(Messages.getString("dialog.SplitTmxDiialog.savePath"));
		
		originalLcBtn = new Button(lcGroup, SWT.RADIO);
		originalLcBtn.setText(Messages.getString("dialog.SplitTmxDiialog.originalPath"));
		originalLcBtn.setSelection(true);
		GridDataFactory.swtDefaults().span(3, SWT.DEFAULT).applyTo(originalLcBtn);
		

		saveAsBtn = new Button(lcGroup, SWT.RADIO);
		saveAsBtn.setText(Messages.getString("dialog.SplitTmxDiialog.saveAsPath"));
		
		tgtFolderLCTxt = new Text(lcGroup, SWT.BORDER|SWT.READ_ONLY);
		tgtFolderLCTxt.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(tgtFolderLCTxt);
		
		browerBtn = new Button(lcGroup, SWT.NONE);
		browerBtn.setText(Messages.getString("dialog.SplitTmxDiialog.brower"));
		
		browerBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
				dialog.setText(Messages.getString("dialog.SplitTmxDiialog.title"));
				String selectedDirectoryName = dialog.open();
				if (selectedDirectoryName != null) {
					tgtFolderLCTxt.setText(selectedDirectoryName);
				}
				check();
			}
		});
		
		saveAsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (saveAsBtn.getSelection()) {
					tgtFolderLCTxt.setEnabled(true);
					browerBtn.setEnabled(true);
				}else {
					tgtFolderLCTxt.setEnabled(false);
					browerBtn.setEnabled(false);
				}
				check();
			}
		});
		
		check();
		init();
		return clientContainer;
	}
	
	private void init(){
		if (getDialogSetting().getBoolean("splitTmx.isSaveBtnSelection")) {
			saveAsBtn.setSelection(true);
			originalLcBtn.setSelection(false);
		}else {
			saveAsBtn.setSelection(false);
			originalLcBtn.setSelection(true);
		}
		if (saveAsBtn.getSelection()) {
			tgtFolderLCTxt.setEnabled(true);
			browerBtn.setEnabled(true);
		}else {
			tgtFolderLCTxt.setEnabled(false);
			browerBtn.setEnabled(false);
		}
	}
	
	private IDialogSettings getDialogSetting(){
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings fDialogSettings = settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings = settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}
	
	private void check(){
		boolean isChecked = true;
		if (saveAsBtn.getSelection()) {
			isChecked = !tgtFolderLCTxt.getText().isEmpty();
		}
		if (splitFileLCText.getText().isEmpty()) {
			isChecked = false;
		}
		setOkBtnEnable(isChecked);
	}
	
	

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				getDialogSetting().put("splitTmx.isSaveBtnSelection", saveAsBtn.getSelection());
				splitFileLC = splitFileLCText.getText();
				if (originalLcBtn.getSelection()) {
					tgtFolderLC = new File(splitFileLC).getParent();
				}else {
					tgtFolderLC = tgtFolderLCTxt.getText();
				}
				fileSum = fileSumSpinner.getSelection();
				
				// 进行分割前的判断
				if (!new File(splitFileLC).exists()) {
					OpenMessageUtils.openMessage(IStatus.INFO, MessageFormat.format(Messages.getString("dialog.SplitTmxDiialog.split.info.msg"), splitFileLC));
					return;
				}
				
			}
		});
		
		SplitTmx splitTmx = new SplitTmx(splitFileLC, tgtFolderLC, fileSum);
		try {
			splitTmx.beginSplit(monitor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		monitor.done();
	}
	/** (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog#getDisplayHelpUrl()
	 */
	@Override
	public String getDisplayHelpUrl() {
		return "/net.heartsome.cat.te.ui.help/html/{0}/ch03.html#tmx_merge_id";
	}
}
