package net.heartsome.cat.ts.ui.rtf.dialog;

import java.text.MessageFormat;
import java.util.ArrayList;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.common.ui.dialog.FileFolderSelectionDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.dialog.HelpDialog;
import net.heartsome.cat.ts.ui.rtf.Activator;
import net.heartsome.cat.ts.ui.rtf.RTFConstants;
import net.heartsome.cat.ts.ui.rtf.exporter.XLIFF2RTFUtil;
import net.heartsome.cat.ts.ui.rtf.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 导出 RTF 文件对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("restriction")
public class ExportRTFDilaog extends HelpDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class.getName());
	
	private static final String STORE_RTF_PATH = "net.heartsome.cat.ts.ui.rtf.dialog.ExportRTFDilaog.RtfPath";

	/** 用户工作空间根目录 */
	private IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

	/** 项目集合，值为项目的路径 */
	private ArrayList<String> lstProject = new ArrayList<String>();

	/** xliff 文件路径文本框 */
	private Text txtXLIFFPath;

	/** xliff 文件浏览按钮 */
	private Button btnBrowseXLIFF;

	/** 文本段状态复选框 */
	private Button btnStatus;

	/** 文本段批注复选框 */
	private Button btnComment;

	/** RTF 文件路径文本框 */
	private Text txtRTFPath;

	/** RTF 文件浏览按钮 */
	private Button btnBrowseRTF;

	/** 所选 xliff 文件的完整路径 */
	private String strXliffFullPath;

	/** 所选 xliff 文件的相对路径 */
	private String strXliffRelativePath;
	
	/** 导出过滤方式的排除按钮 */
	private Button btnExclude;
	
	/** 导出过滤方式的仅导出按钮 */
	private Button btnOnlyExport;
	
	/** 排除锁定的文本段 */
	private Button btnExcludeLocked;
	
	/** 排除上下文匹配的文本段 */
	private Button btnExclude101;
	
	/** 排除完全匹配的文本段 */
	private Button btnExclude100;
	
	/** 仅导出带批注的文本段 */
	private Button btnOnlyExportNote;
	
	/** 仅导出带疑问的文本段 */
	private Button btnOnlyExportReview;
	
	private Group groupExclude;
	
	private Group groupOnlyExport;

	/**
	 * 构造方法
	 * @param parentShell
	 */
	public ExportRTFDilaog(Shell parentShell, String strXliffRelativePath, String strXliffFullPath) {
		super(parentShell);
		this.strXliffRelativePath = strXliffRelativePath;
		this.strXliffFullPath = strXliffFullPath;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString(Messages.DIALOG_EXPORT_TITLE));
		
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// ROBERTHELP 导出 rtf
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(
					"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s05.html#export-xliff-to-rtf", language);
		setHelpUrl(helpUrl);
		super.createButtonsForButtonBar(parent);
	}


	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().extendedMargins(5, 5, 0, 0).numColumns(1).applyTo(tparent);
		GridDataFactory.fillDefaults().hint(600, 420).grab(true, true).applyTo(tparent);

		Group grpExport = new Group(tparent, SWT.None);
		grpExport.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpExport.setLayout(new GridLayout());
		grpExport.setText(Messages.getString(Messages.DIALOG_EXPORT_GROUP));
		
		HsImageLabel imageLabel = new HsImageLabel(
				Messages.getString(Messages.DIALOG_EXPORT_INFO),
				Activator.getImageDescriptor(RTFConstants.EXPORT_GROUP_IMAGE_PATH));
		Composite cmp = imageLabel.createControl(grpExport);
		cmp.setLayout(new GridLayout());
		Composite cmpTemp = (Composite) imageLabel.getControl();
		cmpTemp.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite cmpContent = new Composite(cmpTemp, SWT.None);
		cmpContent.setLayout(new GridLayout(3, false));
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		cmpContent.setLayoutData(data);
		
		new Label(cmpContent, SWT.None).setText(Messages.getString(Messages.DIALOG_EXPORT_XLIFF));
		txtXLIFFPath = new Text(cmpContent, SWT.BORDER);
		txtXLIFFPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtXLIFFPath.setEditable(false);
		if (strXliffRelativePath != null) {
			txtXLIFFPath.setText(strXliffRelativePath);
		}
		btnBrowseXLIFF = new Button(cmpContent, SWT.None);
		btnBrowseXLIFF.setText(Messages.getString(Messages.DIALOG_EXPORT_XLIFF_BROWSE));

		btnStatus = new Button(cmpContent, SWT.CHECK);
		btnStatus.setText(Messages.getString(Messages.DIALOG_EXPORT_STATUS));
		btnStatus.setSelection(true);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(btnStatus);

		btnComment = new Button(cmpContent, SWT.CHECK);
		btnComment.setText(Messages.getString(Messages.DIALOG_EXPORT_COMMENT));
		GridDataFactory.fillDefaults().span(3, 1).applyTo(btnComment);

		Group groupFilter = new Group(tparent, SWT.None);
		groupFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupFilter.setLayout(new GridLayout(2, false));
		groupFilter.setText(Messages.getString("dialog.ExportRTFDilaog.groupFilter"));
		btnExclude = new Button(groupFilter, SWT.RADIO);
		btnExclude.setText(Messages.getString("dialog.ExportRTFDilaog.btnExclude"));
		btnExclude.setSelection(true);
		btnOnlyExport = new Button(groupFilter, SWT.RADIO);
		btnOnlyExport.setText(Messages.getString("dialog.ExportRTFDilaog.btnOnlyExport"));
		
		Composite cmpFilter = new Composite(tparent, SWT.None);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).spacing(30, 5).applyTo(cmpFilter);
		cmpFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupExclude = new Group(cmpFilter, SWT.None);
		groupExclude.setLayout(new GridLayout());
		groupExclude.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupExclude.setText(Messages.getString("dialog.ExportRTFDilaog.groupExclude"));
		btnExcludeLocked = new Button(groupExclude, SWT.CHECK);
		btnExcludeLocked.setText(Messages.getString("dialog.ExportRTFDilaog.btnExcludeLocked"));
		btnExcludeLocked.setSelection(true);
		btnExclude101 = new Button(groupExclude, SWT.CHECK);
		btnExclude101.setText(Messages.getString("dialog.ExportRTFDilaog.btnExclude101"));
		btnExclude100 = new Button(groupExclude, SWT.CHECK);
		btnExclude100.setText(Messages.getString("dialog.ExportRTFDilaog.btnExclude100"));
		groupOnlyExport = new Group(cmpFilter, SWT.None);
		groupOnlyExport.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupOnlyExport.setLayout(new GridLayout());
		groupOnlyExport.setText(Messages.getString("dialog.ExportRTFDilaog.groupOnlyExport"));
		btnOnlyExportNote = new Button(groupOnlyExport, SWT.RADIO);
		btnOnlyExportNote.setText(Messages.getString("dialog.ExportRTFDilaog.btnOnlyExportNote"));
		btnOnlyExportNote.setSelection(true);
		btnOnlyExportNote.setEnabled(false);
		btnOnlyExportReview = new Button(groupOnlyExport, SWT.RADIO);
		btnOnlyExportReview.setText(Messages.getString("dialog.ExportRTFDilaog.btnOnlyExportReview"));
		btnOnlyExportReview.setEnabled(false);
		groupOnlyExport.setEnabled(false);
		
		Composite cmpRTF = new Composite(tparent, SWT.None);
		GridDataFactory.fillDefaults().applyTo(cmpRTF);
		cmpRTF.setLayout(new GridLayout(3, false));
		new Label(cmpRTF, SWT.None).setText(Messages.getString(Messages.DIALOG_EXPORT_PATH_RTF));
		txtRTFPath = new Text(cmpRTF, SWT.BORDER);
		txtRTFPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtRTFPath.setEditable(false);
		btnBrowseRTF = new Button(cmpRTF, SWT.None);
		btnBrowseRTF.setText(Messages.getString(Messages.DIALOG_EXPORT_RTF_BROWSE));

		for (IProject project : root.getProjects()) {
			lstProject.add(project.getLocation().toOSString());
		}

		initListener();
		imageLabel.computeSize();
		initRtfPath();
		
		return parent;
	}

	private void initRtfPath() {
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		String strRtfPath = dialogSettings.get(STORE_RTF_PATH);
		if (strRtfPath != null && !strRtfPath.trim().equals("")) {
			txtRTFPath.setText(strRtfPath);
		}
	}

	/**
	 * 初始化按钮监听 ;
	 */
	private void initListener() {
		btnBrowseXLIFF.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent event) {
				FileFolderSelectionDialog dialog = new FileFolderSelectionDialog(getShell(), false, IResource.FILE) {
					// 打开对话框时展开树形目录
					public void create() {
						super.create();
						super.getTreeViewer().expandAll();
					}
				};
				dialog.setTitle(Messages.getString(Messages.DIALOG_EXPORT_INFO_0));
				dialog.setMessage(Messages.getString(Messages.DIALOG_EXPORT_INFO_1));
				dialog.setDoubleClickSelects(true);
				dialog.setAllowMultiple(false);
				try {

					dialog.setInput(EFS.getStore(root.getLocationURI()));
				} catch (CoreException e1) {
					LOGGER.error(Messages.getString("dialog.ExportRTFDilaog.btnBrowseXLIFF.logger"), e1);
					MessageDialog.openInformation(getShell(), Messages.getString(Messages.DIALOG_EXPORT_OK_TITLE),
							Messages.getString("dialog.ExportRTFDilaog.btnBrowseXLIFF.msg2"));
				}
				dialog.addFilter(new ViewerFilter() {

					@Override
					public boolean select(Viewer viewer, Object parentElement, Object element) {
						if (element instanceof LocalFile) {
							LocalFile folder = (LocalFile) element;
							if (folder.getName().equalsIgnoreCase(".hsConfig")
									|| folder.getName().equalsIgnoreCase(".metadata")) {
								return false;
							}
							if (lstProject.contains(folder.toString())) {
								return true;
							}
							String xliffFolderPath = folder.toString();
							for (String projectPath : lstProject) {
								String path1 = projectPath + System.getProperty("file.separator") + Constant.FOLDER_XLIFF;
								if (xliffFolderPath.startsWith(path1)) {
									return true;
								}
							}
						}
						return false;
					}
				});
				dialog.create();
				dialog.open();

				if (dialog.getResult() != null) {
					Object obj = dialog.getFirstResult();
					IFile file = root.getFileForLocation(Path.fromOSString(obj.toString()));
					txtXLIFFPath.setText(file.getFullPath().toOSString());
					strXliffFullPath = ResourceUtils.iFileToOSPath(file);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnBrowseRTF.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setText(Messages.getString(Messages.DIALOG_EXPORT_INFO_2));
				String defaultPath = System.getProperty("user.home");
				if (txtRTFPath.getText() != null && !txtRTFPath.getText().trim().equals("")) {
					defaultPath = txtRTFPath.getText();
				}
				dialog.setFilterPath(defaultPath);
				String path = dialog.open();
				if (path != null) {
					txtRTFPath.setText(path);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		btnExclude.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				boolean isSelection = btnExclude.getSelection();
				groupExclude.setEnabled(isSelection);
				btnExcludeLocked.setEnabled(isSelection);
				btnExclude101.setEnabled(isSelection);
				btnExclude100.setEnabled(isSelection);
				groupOnlyExport.setEnabled(!isSelection);
				btnOnlyExportNote.setEnabled(!isSelection);
				btnOnlyExportReview.setEnabled(!isSelection);
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		btnOnlyExport.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				boolean isSelection = btnOnlyExport.getSelection();
				groupExclude.setEnabled(!isSelection);
				btnExcludeLocked.setEnabled(!isSelection);
				btnExclude101.setEnabled(!isSelection);
				btnExclude100.setEnabled(!isSelection);
				groupOnlyExport.setEnabled(isSelection);
				btnOnlyExportNote.setEnabled(isSelection);
				btnOnlyExportReview.setEnabled(isSelection);
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
	}

	@Override
	protected void okPressed() {
		if (txtXLIFFPath.getText() == null || txtXLIFFPath.getText().trim().equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString(Messages.DIALOG_EXPORT_OK_TITLE),
					Messages.getString(Messages.DIALOG_EXPORT_OK_MSG_0));
			return;
		}
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		dialogSettings.put(STORE_RTF_PATH, txtRTFPath.getText().trim());
		
		XLFValidator.resetFlag();
		if (!XLFValidator.validateXliffFile(strXliffFullPath)) {
			return;
		}
		XLFValidator.resetFlag();

		VTDGen vg = new VTDGen();
		String srcLanguage = null;
		String tgtLanguage = null;
		if (vg.parseFile(strXliffFullPath, true)) {
			VTDNav vn = vg.getNav();
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace(XLFHandler.hsNSPrefix, XLFHandler.hsR7NSUrl);
			try {
				VTDUtils vu = new VTDUtils(vn);
				vn.push();
				ap.selectXPath("/xliff//file");

				if (ap.evalXPath() != -1) {
					srcLanguage = vu.getCurrentElementAttribut("source-language", "");
					tgtLanguage = vu.getCurrentElementAttribut("target-language", "");
				}
			} catch (NavException e) {
				LOGGER.error(Messages.getString("dialog.ExportRTFDilaog.logger"), e);
				MessageDialog.openInformation(getShell(), Messages.getString(Messages.DIALOG_EXPORT_OK_TITLE),
						Messages.getString("dialog.ExportRTFDilaog.ok.msg5"));
			} catch (XPathParseException e) {
				LOGGER.error(Messages.getString("dialog.ExportRTFDilaog.logger"), e);
				MessageDialog.openInformation(getShell(), Messages.getString(Messages.DIALOG_EXPORT_OK_TITLE),
						Messages.getString("dialog.ExportRTFDilaog.ok.msg5"));
			} catch (XPathEvalException e) {
				LOGGER.error(Messages.getString("dialog.ExportRTFDilaog.logger"), e);
				MessageDialog.openInformation(getShell(), Messages.getString(Messages.DIALOG_EXPORT_OK_TITLE),
						Messages.getString("dialog.ExportRTFDilaog.ok.msg5"));
			}
		}
		final String strRTFPath = txtRTFPath.getText();
		if (strRTFPath == null || strRTFPath.trim().equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString(Messages.DIALOG_EXPORT_OK_TITLE),
					Messages.getString(Messages.DIALOG_EXPORT_OK_MSG_3));
			return;
		}

		final String srcLang = srcLanguage;
		final String tgtLang = tgtLanguage;
		final boolean isExclude = btnExclude.getSelection();
		final ArrayList<Boolean> lstSelection = new ArrayList<Boolean>();
		if (isExclude) {
			lstSelection.add(btnExcludeLocked.getSelection());
			lstSelection.add(btnExclude101.getSelection());
			lstSelection.add(btnExclude100.getSelection());
		} else {
			lstSelection.add(btnOnlyExportNote.getSelection());
			lstSelection.add(btnOnlyExportReview.getSelection());
		}
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				XLIFF2RTFUtil util = new XLIFF2RTFUtil();
				if (util.exporter(strXliffFullPath, srcLang, tgtLang, btnStatus.getSelection(), btnComment.getSelection(),
						strRTFPath, isExclude, lstSelection)) {
					MessageDialog.openInformation(getShell(), Messages.getString(Messages.DIALOG_EXPORT_OK_TITLE),
							Messages.getString(Messages.DIALOG_EXPORT_OK_MSG_4));
				}
			}
		});
		close();
	}
}
