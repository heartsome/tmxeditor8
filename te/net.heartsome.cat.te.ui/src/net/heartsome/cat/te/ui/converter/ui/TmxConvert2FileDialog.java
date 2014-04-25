/**
 * TmxConvert2FileDialog.java
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

package net.heartsome.cat.te.ui.converter.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.core.bean.TmxConvert2FileBean;
import net.heartsome.cat.te.core.converter.AbstractTmx2File;
import net.heartsome.cat.te.core.converter.ConverterFactory;
import net.heartsome.cat.te.tmxeditor.Activator;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TMX文件转换为其他文件
 * @author yule
 * @version
 * @since JDK1.6
 */
public class TmxConvert2FileDialog extends HsAbstractProgressDialog implements IRunnableWithProgress {

	public final static Logger LOGGER = LoggerFactory.getLogger(TmxConvert2FileDialog.class);

	private static final String FILETYPECOMBO_INDEX = "net.heartsome.cat.te.ui.converter.ui.TmxConvert2FileDialog.comboIndex";

	private static final String PROPERTYCHECKBTN_ENABLE = "net.heartsome.cat.te.ui.converter.ui.TmxConvert2FileDialog.propertyEnable";

	private static final String PROPERTYCHECKBTN_SELECT = "net.heartsome.cat.te.ui.converter.ui.TmxConvert2FileDialog.propertySelect";

	private static final String SRCBTN_ENABLE = "net.heartsome.cat.te.ui.converter.ui.TmxConvert2FileDialog.srcEnable";

	private static final String SRCBTN_SELECT = "net.heartsome.cat.te.ui.converter.ui.TmxConvert2FileDialog.srcSelect";

	private static final String SAVEASBTN_ENABLE = "net.heartsome.cat.te.ui.converter.ui.TmxConvert2FileDialog.saveAsEnable";

	private static final String SAVEASBTN_SELECT = "net.heartsome.cat.te.ui.converter.ui.TmxConvert2FileDialog.saveAsSelect";

	private List tmxList;

	private Button addTmxBtn;

	private Button removeTmxBtn;

	private Combo fileTypeCombo;

	private Button propertyCheckBtn;

	private Button srcBtn;

	private Button saveAsBtn;

	private Text text;

	private Button browserBtn;

	private static final Map<Integer, String> allNames = ConverterFactory.getAllTmx2FileConverterName();

	public TmxConvert2FileDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.SHELL_TRIM);

	}

	@Override
	protected Point getInitialSize() {
		return new Point(620, 500);
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getText());
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog#createClientArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Composite createClientArea(Composite clientContainer) {
		Composite parent = clientContainer;
		parent.setLayout(new GridLayout(2, false));
		// 1、创建转换列表区域
		Label converTmxsLable = new Label(parent, SWT.NONE);
		converTmxsLable.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		converTmxsLable.setText(Messages.getString("dialog.TmxConvert2FileDialog.waitlist"));
		new Label(parent, SWT.NONE);
		// list
		tmxList = new List(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		tmxList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// 2 、创建操作转换列表区域的按钮
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout rowLayout = new GridLayout(1, false);
		rowLayout.marginRight = 0;
		rowLayout.verticalSpacing = 10;
		composite.setLayout(rowLayout);
		composite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		// add
		addTmxBtn = new Button(composite, SWT.NONE);
		addTmxBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addTmxBtn.setText(Messages.getString("dialog.TmxConvert2FileDialog.add"));
		// remove
		removeTmxBtn = new Button(composite, SWT.NONE);
		removeTmxBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeTmxBtn.setText(Messages.getString("dialog.TmxConvert2FileDialog.remove"));

		// 3、文件类型选择区域
		Composite fileTypeSelectArea = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		fileTypeSelectArea.setLayout(gridLayout);
		fileTypeSelectArea.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Label lblNewLabel_1 = new Label(fileTypeSelectArea, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText(Messages.getString("dialog.TmxConvert2FileDialog.convertto"));
		// type
		fileTypeCombo = new Combo(fileTypeSelectArea, SWT.READ_ONLY);
		fileTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(parent, SWT.NONE);

		// 4、转换属性选择区域
		propertyCheckBtn = new Button(parent, SWT.CHECK);
		propertyCheckBtn.setText(Messages.getString("dialog.TmxConvert2FileDialog.needproperty"));
		new Label(parent, SWT.NONE);

		// 5、存储路径选择区域
		Group savaPathGroup = new Group(parent, SWT.NONE);
		GridLayout saveLayout = new GridLayout(1, false);
		saveLayout.marginLeft = 0;
		savaPathGroup.setLayout(saveLayout);

		savaPathGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		savaPathGroup.setText(Messages.getString("dialog.TmxConvert2FileDialog.path"));
		// 源文件
		srcBtn = new Button(savaPathGroup, SWT.RADIO);
		srcBtn.setText(Messages.getString("dialog.TmxConvert2FileDialog.srcpath"));

		Composite saveAsComposite = new Composite(savaPathGroup, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = -5;
		saveAsComposite.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		saveAsComposite.setLayoutData(gridData);
		// 另存为
		saveAsBtn = new Button(saveAsComposite, SWT.RADIO);
		saveAsBtn.setText(Messages.getString("dialog.TmxConvert2FileDialog.saveaspath"));
		// filePath
		text = new Text(saveAsComposite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		browserBtn = new Button(saveAsComposite, SWT.NONE);
		browserBtn.setText(Messages.getString("dialog.TmxConvert2FileDialog.browser"));

		// 6、 初始化数据以及按钮状态
		setInitState();
		addListener();
		loadDialogSettings();
		return clientContainer;
	}

	private String getText() {
		return Messages.getString("dialog.TmxConvert2FileDialog.title");
	}

	// okPress ,执行代码示例
	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		TmxConvert2FileBean counverBean = getCounverBean();

		int fileCount = counverBean.tmxFilePath.size();
		AbstractTmx2File tmx2FileConverter = ConverterFactory.getTmx2FileConverter(counverBean.targetFileType);

		monitor.beginTask(Messages.getString("dialog.TmxConvert2FileDialog.convert.task.msg1"), fileCount * 100 + 10);
		String tmxPath = null;
		File targetFile = null;
		String currentConvertFileName = "";
		try {
			final String exitsFileName = checkTargetFileExits(counverBean);
			if (!exitsFileName.isEmpty()) {
				final boolean isConfirm[] = new boolean[1];
				isConfirm[0] = true;
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						isConfirm[0] = OpenMessageUtils.openConfirmMessage(MessageFormat.format(
								Messages.getString("dialog.TmxConvert2FileDialog.overwriteFile"), exitsFileName));

					}
				});
				if (!isConfirm[0]) {
					monitor.setCanceled(true);
					return;
				}

			}
			monitor.worked(10);
			for (int i = 0; i < fileCount; i++) {
				tmxPath = counverBean.tmxFilePath.get(i);
				final File f = new File(tmxPath);
				currentConvertFileName = f.getName();
				if (!f.exists() || !f.canRead()) {
					throw new Exception(MessageFormat.format(
							Messages.getString("dialog.TmxConvert2FileDialog.sourceFileNotexist"), f.getName()));
				}
				monitor.setTaskName(MessageFormat.format(
						Messages.getString("dialog.TmxConvert2FileDialog.convert.task.msg2"), tmxPath));

				targetFile = getTargetFile(tmxPath, counverBean, i);
				if (null == targetFile) {// 取消覆盖目标文件
					continue;
				}
				if (!targetFile.exists() || !targetFile.canWrite()) {
					throw new Exception(
							MessageFormat.format(Messages.getString("dialog.TmxConvert2FileDialog.targetFileNotexist"),
									targetFile.getName()));
				}
				if (monitor.isCanceled()) {
					return;
				} else {
					// TODO: 处理HSTM转换包含单元属性的情况
					if (counverBean.targetFileType == ConverterFactory.FILE_TYPE_HSTM) {
						tmxPath = getHstmConvertPath(tmxPath);
					}
					SubProgressMonitor subFileJob = new SubProgressMonitor(monitor, 100);
					tmx2FileConverter.doCovnerter(tmxPath, targetFile, subFileJob);
				}
				monitor.worked(1);
			}

		} catch (Exception e) {
			if (null != targetFile) {
				targetFile.delete();
			}
			LOGGER.error("", e);
			throw new InterruptedException(MessageFormat.format(
					Messages.getString("dialog.TmxConvert2FileDialog.convert.task.error"), currentConvertFileName,
					e.getMessage()));
		} finally {
			monitor.done();
		}

	}

	private File getTargetFile(String tmxPath, TmxConvert2FileBean counverBean, int index) throws Exception {
		File file= getTargetFile0(tmxPath, counverBean, index);
		final String dirName = file.getParent();
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				throw new Exception(MessageFormat.format(
						Messages.getString("dialog.TmxConvert2FileDialog.targetFileNotexist"), dirName));
			}
		}
		return file;

	}

	private int getTargetNameCount(TmxConvert2FileBean counverBean, int index, String name) {
		if (!counverBean.isNewTargetPath) {
			return 0;
		}
		int count = -1;
		for (int i = 0; i <= index; i++) {
			String string = counverBean.tmxFilePath.get(i);
			File file = new File(string);
			if (file.getName().equals(name)) {
				count++;
			}
		}
		return count;
	}

	private File getTargetFile0(String tmxPath, TmxConvert2FileBean counverBean, int index) {
		File tmxFile = new File(tmxPath);
		String name = tmxFile.getName();
		int targetNameCount = getTargetNameCount(counverBean, index, name);
		if (0 == targetNameCount) {
			name = name.substring(0, name.lastIndexOf(".")) + ".";
		} else {
			name = name.substring(0, name.lastIndexOf(".")) + "_(" + targetNameCount + ").";
		}
		File file = null;
		String path = null;
		if (counverBean.isNewTargetPath) {
			path = counverBean.newTargetPath;
		} else {
			path = tmxFile.getParent();
		}

		if (path.endsWith(File.separator)) {
			file = new File(path + name + allNames.get(counverBean.targetFileType));
		} else {
			file = new File(path + File.separator + name + allNames.get(counverBean.targetFileType));
		}
		return file;
	}

	protected String checkTargetFileExits(TmxConvert2FileBean counverBean) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < counverBean.tmxFilePath.size(); i++) {
			File file= getTargetFile0(counverBean.tmxFilePath.get(i), counverBean, i);
			if (file.exists()) {
				sb.append(file.getName() + "\n");
			}

		}
		return sb.toString();
	}

	/**
	 * 设置初始化数据 ;
	 */
	private void setInitState() {

		removeTmxBtn.setEnabled(false);

		fileTypeCombo.add(Messages.getString("dialog.TmxConvert2FileDialog.fileType.docx"));
		fileTypeCombo.add(Messages.getString("dialog.TmxConvert2FileDialog.fileType.xlsx"));
		fileTypeCombo.add(Messages.getString("dialog.TmxConvert2FileDialog.fileType.txt"));
		fileTypeCombo.add(Messages.getString("dialog.TmxConvert2FileDialog.fileType.tbx"));
		fileTypeCombo.add(Messages.getString("dialog.TmxConvert2FileDialog.fileType.hstm"));
		fileTypeCombo.setText(Messages.getString("dialog.TmxConvert2FileDialog.fileType.docx"));

		propertyCheckBtn.setEnabled(false);
		propertyCheckBtn.setSelection(false);

		text.setEditable(false);
		srcBtn.setEnabled(true);
		srcBtn.setSelection(false);

		saveAsBtn.setEnabled(true);
		saveAsBtn.setSelection(true);
		browserBtn.setEnabled(true);

	}

	/**
	 * 添加监听器 ;
	 */
	private void addListener() {

		addTmxBtn.addSelectionListener(new AddTmxFileCommand());
		tmxList.addSelectionListener(new TmxListFileSelectCommand());
		removeTmxBtn.addSelectionListener(new RemoveTmxFileCommand());
		fileTypeCombo.addSelectionListener(new SelectFileTypeCommand());
		browserBtn.addSelectionListener(new BrowserDirCommand());
		srcBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browserBtn.setEnabled(false);
				saveAsBtn.setSelection(false);
				setOkState();
			}
		});
		saveAsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browserBtn.setEnabled(true);
				srcBtn.setSelection(false);
				setOkState();
			}
		});
	}

	/**
	 * 添加文件
	 * @author yule
	 * @version
	 * @since JDK1.6
	 */
	private class AddTmxFileCommand extends SelectionAdapter {

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {

			FileDialog fDialog = new FileDialog(getShell(), SWT.MULTI);
			fDialog.setFilterExtensions(new String[] { "*.tmx" });
			fDialog.open();
			String filterPath = fDialog.getFilterPath();
			String[] fileNames = fDialog.getFileNames();
			if (fileNames == null || fileNames.length == 0) {
				return;
			}
			String absolutePath = null;
			for (String fileName : fileNames) {
				absolutePath = new File(filterPath + File.separator + fileName).getAbsolutePath();
				if (Arrays.asList(tmxList.getItems()).contains(absolutePath)) {
					continue;
				}
				tmxList.add(absolutePath);
			}
			setOkState();
		}
	}

	/**
	 * 删除文件
	 * @author yule
	 * @version
	 * @since JDK1.6
	 */
	private class RemoveTmxFileCommand extends SelectionAdapter {
		/**
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (tmxList.getSelectionCount() <= 0) {
				removeTmxBtn.setEnabled(false);
				return;
			}
			tmxList.remove(tmxList.getSelectionIndices());
			if (tmxList.getItemCount() <= 0) {
				removeTmxBtn.setEnabled(false);
			}
			setOkState();
		}

	}

	private class TmxListFileSelectCommand extends SelectionAdapter {

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (tmxList.getSelectionCount() > 0) {
				removeTmxBtn.setEnabled(true);
			}
		}
	}

	/**
	 * 选择文件类型
	 * @author yule
	 * @version
	 * @since JDK1.6
	 */
	private class SelectFileTypeCommand extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			int selectIndex = fileTypeCombo.getSelectionIndex();
			if (-1 == selectIndex) {
				return;
			}
			int hstmIndex = 4;// hstm 的索引位置
			if (hstmIndex == selectIndex) {
				propertyCheckBtn.setEnabled(true);
			} else {
				propertyCheckBtn.setEnabled(false);
				propertyCheckBtn.setSelection(false);
			}
		}
	}

	/**
	 * 选择另存为的文件路径
	 * @author yule
	 * @version
	 * @since JDK1.6
	 */
	private class BrowserDirCommand extends SelectionAdapter {

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			dialog.open();

			String filterPath = dialog.getFilterPath();
			if (null == filterPath || filterPath.isEmpty()) {
				return;
			}
			text.setText(filterPath);
			setOkState();
		}
	}

	public TmxConvert2FileBean getCounverBean() {
		final TmxConvert2FileBean bean = new TmxConvert2FileBean();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				bean.tmxFilePath = Arrays.asList(tmxList.getItems());
				bean.isNewTargetPath = saveAsBtn.getSelection();
				if (bean.isNewTargetPath) {
					bean.newTargetPath = text.getText();
				}
				switch (fileTypeCombo.getSelectionIndex()) {
				case 0:
					bean.targetFileType = ConverterFactory.FILE_TYPE_DOCX;
					break;
				case 1:
					bean.targetFileType = ConverterFactory.FILE_TYPE_XLSX;
					break;
				case 2:
					bean.targetFileType = ConverterFactory.FILE_TYPE_TXT;
					break;
				case 3:
					bean.targetFileType = ConverterFactory.FILE_TYPE_TBX;
					break;
				case 4:
					bean.targetFileType = ConverterFactory.FILE_TYPE_HSTM;
					break;
				default:
					bean.targetFileType = ConverterFactory.FILE_TYPE_DOCX;
					break;
				}
			}
		});

		return bean;
	}

	private String getHstmConvertPath(String tmxPath) {
		final Map<String, String> map = new HashMap<String, String>();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (propertyCheckBtn.isEnabled() && propertyCheckBtn.getSelection()) {
					map.put("key", "value");
				}
			}
		});
		if (!map.isEmpty()) {
			tmxPath = "TRUE" + tmxPath;
		}
		return tmxPath;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog#close()
	 */
	@Override
	public boolean close() {
		if (getShell() != null && !getShell().isDisposed()) {
			writeDialogSettings();
		}
		return super.close();
	}

	private void writeDialogSettings() {
		IDialogSettings dialogSettings = getDialogSettings();
		int fileTypeSelectIndex = fileTypeCombo.getSelectionIndex();
		dialogSettings.put(FILETYPECOMBO_INDEX, fileTypeSelectIndex == -1 ? 0 : fileTypeSelectIndex);

		dialogSettings.put(PROPERTYCHECKBTN_ENABLE, propertyCheckBtn.isEnabled());
		dialogSettings.put(PROPERTYCHECKBTN_SELECT, propertyCheckBtn.getSelection());

		dialogSettings.put(SRCBTN_ENABLE, srcBtn.isEnabled());
		dialogSettings.put(SRCBTN_SELECT, srcBtn.getSelection());

		dialogSettings.put(SAVEASBTN_ENABLE, saveAsBtn.isEnabled());
		dialogSettings.put(SAVEASBTN_SELECT, saveAsBtn.getSelection());
	}

	private void loadDialogSettings() {
		IDialogSettings dialogSettings = getDialogSettings();
		try {
			fileTypeCombo.select(dialogSettings.getInt(FILETYPECOMBO_INDEX));
		} catch (NumberFormatException e) {
			return;
		}
		propertyCheckBtn.setEnabled(dialogSettings.getBoolean(PROPERTYCHECKBTN_ENABLE));
		propertyCheckBtn.setSelection(dialogSettings.getBoolean(PROPERTYCHECKBTN_SELECT));

		srcBtn.setEnabled(dialogSettings.getBoolean(SRCBTN_ENABLE));
		srcBtn.setSelection(dialogSettings.getBoolean(SRCBTN_SELECT));

		saveAsBtn.setEnabled(dialogSettings.getBoolean(SAVEASBTN_ENABLE));
		saveAsBtn.setSelection(dialogSettings.getBoolean(SAVEASBTN_SELECT));
	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings fDialogSettings = settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings = settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}

	/**
	 * 设置ok按钮的状态 ;
	 */
	private void setOkState() {
		if (tmxList.getItemCount() > 0) {
			if (saveAsBtn.getSelection()) {
				if (text.getText() != null && !text.getText().isEmpty()) {
					setOkBtnEnable(true);
				} else {
					setOkBtnEnable(false);
				}
			} else {
				setOkBtnEnable(true);
			}
		} else {
			setOkBtnEnable(false);
		}
	}

	/**
	 * 帮助连接URL
	 */
	@Override
	public String getDisplayHelpUrl() {
		return "/net.heartsome.cat.te.ui.help/html/{0}/ch07.html#tmx_to_id";
	}

}
