package net.heartsome.cat.ts.ui.rtf.dialog;

import java.text.MessageFormat;
import java.util.ArrayList;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.dialog.FileFolderSelectionDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.dialog.HelpDialog;
import net.heartsome.cat.ts.ui.rtf.Activator;
import net.heartsome.cat.ts.ui.rtf.importer.ImportRTFToXLIFF;
import net.heartsome.cat.ts.ui.rtf.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.util.ProgressIndicatorManager;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 导入 RTF 文件对话框
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
@SuppressWarnings("restriction")
public class ImportRTFDialog extends HelpDialog {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ImportRTFDialog.class);
	
	private static final String STORE_RTF_PATH = "net.heartsome.cat.ts.ui.rtf.dialog.ImportRTFDialog.RtfPath";

	/** 用户工作空间根目录 */
	private IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	
	/** XLIFFEditorImplWithNatTable 实例  */
	private XLIFFEditorImplWithNatTable xliffEditor;

	/** 项目集合，值为项目的路径 */
	private ArrayList<String> lstProject = new ArrayList<String>();

	/** xliff 文件路径文本框 */
	private Text txtXLIFFPath;

	/** xliff 文件浏览按钮 */
	private Button btnBrowseXLIFF;

	/** RTF 文件路径文本框 */
	private Text txtRTFPath;

	/** RTF 文件浏览按钮 */
	private Button btnBrowseRTF;

	/** 所选 xliff 文件的完整路径 */
	private String strXliffFullPath;
	
	/** 所选 xliff 文件的相对路径 */
	private String strXliffRelativePath;

	public ImportRTFDialog(Shell parentShell, XLIFFEditorImplWithNatTable xliffEditor, String strXliffRelativePath, String strXliffFullPath) {
		super(parentShell);
		this.xliffEditor = xliffEditor;
		this.strXliffRelativePath = strXliffRelativePath;
		this.strXliffFullPath = strXliffFullPath;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// ROBERTHELP 导入RTF
		String language = CommonFunction.getSystemLanguage();
		String helpUrl = MessageFormat.format(
					"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s05.html#import-rtf-to-xliff", language);
		setHelpUrl(helpUrl);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString(Messages.DIALOG_IMPORT_TITLE));
	}
	

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().extendedMargins(2, 2, 0, 0).numColumns(3).equalWidth(false).applyTo(tparent);
		GridDataFactory.fillDefaults().hint(510, 120).grab(true, true).applyTo(tparent);

		Group groupXLIFF = new Group(tparent, SWT.None);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		groupXLIFF.setLayoutData(data);
		groupXLIFF.setLayout(new GridLayout(3, false));
		groupXLIFF.setText(Messages.getString("dialog.ImportRTFDialog.groupXLIFF"));
		Label lblXLIFF = new Label(groupXLIFF, SWT.None);
		lblXLIFF.setText(Messages.getString(Messages.DIALOG_IMPORT_XLIFF));
//		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(lblXLIFF);
		txtXLIFFPath = new Text(groupXLIFF, SWT.BORDER);
		txtXLIFFPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtXLIFFPath.setEditable(false);
		if (strXliffRelativePath != null) {
			txtXLIFFPath.setText(strXliffRelativePath);
		}
		btnBrowseXLIFF = new Button(groupXLIFF, SWT.None);
		btnBrowseXLIFF.setText(Messages.getString(Messages.DIALOG_IMPORT_XLIFF_BROWSE));

		Label lblRTF = new Label(tparent, SWT.None);
		lblRTF.setText(Messages.getString(Messages.DIALOG_IMPORT_RTF));
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(lblRTF);
		txtRTFPath = new Text(tparent, SWT.BORDER);
		txtRTFPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtRTFPath.setEditable(false);
		btnBrowseRTF = new Button(tparent, SWT.None);
		btnBrowseRTF.setText(Messages.getString(Messages.DIALOG_IMPORT_RTF_BROWSE));
		
		for (IProject project : root.getProjects()) {
			lstProject.add(project.getLocation().toOSString());
		}
		initListener();
		initRtfPath();
		return tparent;
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
				dialog.setTitle(Messages.getString(Messages.DIALOG_IMPORT_XLIFF_INFO_0));
				dialog.setMessage(Messages.getString(Messages.DIALOG_IMPORT_XLIFF_INFO_1));
				dialog.setDoubleClickSelects(true);
				dialog.setAllowMultiple(false);
				try {

					dialog.setInput(EFS.getStore(root.getLocationURI()));
				} catch (CoreException e1) {
					LOGGER.error(Messages.getString("dialog.ImportRTFDialog.logger1"), e1);
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
				FileDialog dialog = new FileDialog(getShell());
				dialog.setText(Messages.getString(Messages.DIALOG_IMPORT_RTF_INFO));
				dialog.setFilterExtensions(new String[] { "*.rtf" });
				dialog.setFilterNames(new String[] {Messages.getString(Messages.DIALOG_IMPORT_RTF_FILTER)});
				String fileSep = System.getProperty("file.separator");
				if (txtRTFPath.getText() != null && !txtRTFPath.getText().trim().equals("")) {
					dialog.setFilterPath(txtRTFPath.getText().substring(0, txtRTFPath.getText().lastIndexOf(fileSep)));
					dialog.setFileName(txtRTFPath.getText().substring(txtRTFPath.getText().lastIndexOf(fileSep) + 1));
				} else {
					dialog.setFilterPath(System.getProperty("user.home"));
				}
				String path = dialog.open();
				if (path != null) {
					txtRTFPath.setText(path);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	@Override
	protected void okPressed() {
		if (txtXLIFFPath.getText() == null || txtXLIFFPath.getText().trim().equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString(Messages.DIALOG_IMPORT_OK_TITLE), Messages.getString(Messages.DIALOG_IMPORT_OK_MSG_0));
			return;
		}
		
		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		dialogSettings.put(STORE_RTF_PATH, txtRTFPath.getText().trim());
		
		XLFValidator.resetFlag();
		if (!XLFValidator.validateXliffFile(strXliffFullPath)) {
			return;
		}
		XLFValidator.resetFlag();
		final String strRTFPath = txtRTFPath.getText();
		if (strRTFPath == null || strRTFPath.trim().equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString(Messages.DIALOG_IMPORT_OK_TITLE), Messages.getString(Messages.DIALOG_IMPORT_OK_MSG_1));
			return;
		}
		Job job = new Job(Messages.getString(Messages.DIALOG_IMPORT_OK_JOB_TITLE)) {
			protected IStatus run(final IProgressMonitor monitor) {
				ImportRTFToXLIFF importer = new ImportRTFToXLIFF(xliffEditor);
				importer.convert(strXliffFullPath, strRTFPath, monitor);
				return Status.OK_STATUS;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter(){
			@Override
			public void running(IJobChangeEvent event) {
				ProgressIndicatorManager.displayProgressIndicator();
				super.running(event);
			}
			@Override
			public void done(IJobChangeEvent event) {
				ProgressIndicatorManager.hideProgressIndicator();
				super.done(event);
			}
		});
		job.setUser(true);
		job.schedule();
		close();
	}
}
