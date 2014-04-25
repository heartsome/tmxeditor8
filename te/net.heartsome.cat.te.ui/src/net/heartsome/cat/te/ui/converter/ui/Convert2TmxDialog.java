/**
 * Convert2TmxDialog.java
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.te.core.bean.File2TmxConvertBean;
import net.heartsome.cat.te.core.converter.AbstractFile2Tmx;
import net.heartsome.cat.te.core.converter.ConverterFactory;
import net.heartsome.cat.te.core.converter.TmxUtil;
import net.heartsome.cat.te.tmxeditor.Activator;
import net.heartsome.cat.te.ui.opentmx.handler.OpenTmxFileHandler;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.VTDGen;

public class Convert2TmxDialog extends HsAbstractProgressDialog implements IRunnableWithProgress {

	private static Logger LOGGER = LoggerFactory.getLogger(Convert2TmxDialog.class);
	/**
	 * 需要转换的源文件路径
	 */
	private Text srcPathText;

	/**
	 * 添加自定义属性
	 */
	private Button addCustomAttributeBtn;

	/**
	 * 转换到全新的TMX文件路径
	 */
	private Text newTmxPathText;
	/**
	 * 追加到已有的TMX文件路径
	 */
	private Text appendTmxPathText;

	private ScrolledComposite scroll;

	private Button browserSrcFileBtn;

	/**
	 * 自定义属性区域
	 */
	private Composite attributeArea;

	private Button conver2NewTmxBtn;

	private Button browserNewTmxPathBtn;

	private Button appendExistTmxPathBtn;

	private Button browserExistTmxPathBtn;

	private Button isOpenBtn;

	private File2TmxConvertBean convertBean;

	/**
	 * @param parentShell
	 */
	public Convert2TmxDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.SHELL_TRIM);
	}

	public AbstractFile2Tmx file2Tmx;

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog#createClientArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Composite createClientArea(Composite clientContainer) {

		clientContainer.setLayout(new GridLayout(1, false));
		clientContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// 1、 第一行源文件选择区域
		Composite srcFileSelectArea = new Composite(clientContainer, SWT.NONE);
		GridLayout srcFileLayout = new GridLayout(3, false);
		srcFileLayout.marginWidth = 0;
		srcFileSelectArea.setLayout(srcFileLayout);
		srcFileSelectArea.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label srcLable = new Label(srcFileSelectArea, SWT.NONE);
		srcLable.setText(Messages.getString("dialog.Convert2TmxDialog.srcLable"));
		srcLable.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

		srcPathText = new Text(srcFileSelectArea, SWT.BORDER);
		srcPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		browserSrcFileBtn = new Button(srcFileSelectArea, SWT.NONE);
		browserSrcFileBtn.setText(Messages.getString("dialog.Convert2TmxDialog.browserSrcFileBtn"));
		browserSrcFileBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		new Label(srcFileSelectArea, SWT.NONE);
		Link srcFileFormatLink = new Link(srcFileSelectArea, SWT.NONE);
		srcFileFormatLink.setText(MessageFormat.format("<a>{0}</a>",
				Messages.getString("dialog.Convert2TmxDialog.sourceFileFormatHelp")));

		srcFileFormatLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(getHelpUrl());
			}

			private String getHelpUrl() {
				String language = CommonFunction.getSystemLanguage();
				String helpUrl = MessageFormat.format(
						"/net.heartsome.cat.te.ui.help/html/{0}/ch07.html#source_file_id", language);
				return helpUrl;
			}
		});

		// 3、添加自定义属性
		addCustomAttributeBtn = new Button(clientContainer, SWT.CHECK);
		addCustomAttributeBtn.setText(Messages.getString("dialog.Convert2TmxDialog.addCustomAttributeBtn"));

		Group customAttributeGroup = new Group(clientContainer, SWT.NONE);
		customAttributeGroup.setLayout(new GridLayout(1, false));

		GridData customAttributeGroupGd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		// customAttributeGroupGd.heightHint = 150;
		customAttributeGroup.setLayoutData(customAttributeGroupGd);
		customAttributeGroup.setText(Messages.getString("dialog.Convert2TmxDialog.customAttributeGroup"));

		scroll = new ScrolledComposite(customAttributeGroup, SWT.V_SCROLL);
		scroll.setLayoutData(new GridData(GridData.FILL_BOTH));
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);

		attributeArea = new Composite(scroll, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(attributeArea);
		GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(1).applyTo(attributeArea);
		scroll.setContent(attributeArea);
		// createArributeArea(attributeArea);

		// 4、转换成目标TMX的区域
		Group converTargetGroup = new Group(clientContainer, SWT.NONE);
		converTargetGroup.setLayout(new GridLayout(3, false));
		converTargetGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		conver2NewTmxBtn = new Button(converTargetGroup, SWT.RADIO);
		conver2NewTmxBtn.setText(Messages.getString("dialog.Convert2TmxDialog.conver2NewTmxBtn"));

		newTmxPathText = new Text(converTargetGroup, SWT.BORDER);
		newTmxPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

		browserNewTmxPathBtn = new Button(converTargetGroup, SWT.NONE);
		browserNewTmxPathBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		browserNewTmxPathBtn.setText(Messages.getString("dialog.Convert2TmxDialog.browserNewTmxPathBtn"));

		appendExistTmxPathBtn = new Button(converTargetGroup, SWT.RADIO);
		appendExistTmxPathBtn.setText(Messages.getString("dialog.Convert2TmxDialog.appendExistTmxPathBtn"));

		appendTmxPathText = new Text(converTargetGroup, SWT.BORDER);
		appendTmxPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

		browserExistTmxPathBtn = new Button(converTargetGroup, SWT.NONE);
		browserExistTmxPathBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		browserExistTmxPathBtn.setText(Messages.getString("dialog.Convert2TmxDialog.browserExistTmxPathBtn"));

		isOpenBtn = new Button(clientContainer, SWT.CHECK);
		isOpenBtn.setText(Messages.getString("dialog.Convert2TmxDialog.isOpenBtn"));

		initDataState();
		loadDialogSettings();
		addLisners();

		return clientContainer;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.Convert2TmxDialog.title"));
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 570);
	}

	private class AddArributeCommand extends SelectionAdapter {

		private Composite parent;

		/**
		 * 
		 */
		public AddArributeCommand(Composite parent) {
			this.parent = parent;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			createArributeArea(parent, null, null);
			scroll.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			parent.layout();
			scroll.layout();
		}
	}

	private class DeleteAttibuteCommand extends SelectionAdapter {

		private Composite parent;

		/**
		 * 
		 */
		public DeleteAttibuteCommand(Composite parent) {
			this.parent = parent;

		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Button b = (Button) e.getSource();
			b.getParent().dispose();
			scroll.setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			parent.layout();
			scroll.layout();
			if (parent.getChildren().length == 0) {
				addCustomAttributeBtn.setSelection(false);
			}

		}
	}

	private void addLisners() {
		// 添加自定义属性
		addCustomAttributeBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (addCustomAttributeBtn.getSelection()) {
					disposeAttributeArea();
					createArributeArea(attributeArea, null, null);
					scroll.setMinSize(attributeArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					attributeArea.layout();
					scroll.layout();
				} else {
					disposeAttributeArea();
				}

			}
		});

		// 选择源文件
		browserSrcFileBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fDialog = new FileDialog(getShell(), SWT.SINGLE);
				fDialog.setFilterExtensions(new String[] { "*.docx;*.xlsx;*.txt;*.tbx;*.hstm", "*.docx", "*.xlsx",
						"*.txt", "*.tbx", "*.hstm" });
				fDialog.open();
				String filterPath = fDialog.getFilterPath();
				String fileName = fDialog.getFileName();
				if (fileName == null || fileName.isEmpty()) {
					return;
				}
				String filePath = filterPath + File.separator + fileName;
				srcPathText.setText(filePath);
				// 设置按钮状态
				setOKState();

			}
		});

		conver2NewTmxBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				browserNewTmxPathBtn.setEnabled(true);
				browserExistTmxPathBtn.setEnabled(false);
				setOKState();

			}
		});

		appendExistTmxPathBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browserNewTmxPathBtn.setEnabled(false);
				browserExistTmxPathBtn.setEnabled(true);
				setOKState();
			}
		});

		browserNewTmxPathBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.open();

				String filterPath = dialog.getFilterPath();
				if (null == filterPath || filterPath.isEmpty()) {
					return;
				}
				newTmxPathText.setText(filterPath);
				setOKState();

			}
		});

		browserExistTmxPathBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fDialog = new FileDialog(getShell(), SWT.SINGLE);
				fDialog.setFilterExtensions(new String[] { "*.tmx" });
				fDialog.open();
				String filterPath = fDialog.getFilterPath();
				String fileName = fDialog.getFileName();
				if (fileName == null || fileName.isEmpty()) {
					return;
				}
				String filePath = filterPath + File.separator + fileName;
				appendTmxPathText.setText(filePath);
				setOKState();
			}
		});
	}

	private Composite createArributeArea(Composite attibueArea, String key, String value) {
		Composite parent = new Composite(attibueArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(parent);
		GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(5).applyTo(parent);

		Text keyText = new Text(parent, SWT.BORDER);
		keyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		if (null != key) {
			keyText.setText(key);
		}

		Label assignLb = new Label(parent, SWT.NONE);
		assignLb.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		assignLb.setText(" = ");
		Text valueText = new Text(parent, SWT.BORDER);
		valueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (null != value) {
			valueText.setText(value);
		}
		Button addBtn = new Button(parent, SWT.NONE);
		addBtn.setText("+");
		addBtn.addSelectionListener(new AddArributeCommand(attibueArea));

		Button deletBtn = new Button(parent, SWT.NONE);
		deletBtn.setText("-");
		deletBtn.addSelectionListener(new DeleteAttibuteCommand(attibueArea));
		return attibueArea;
	}

	private void disposeAttributeArea() {
		Control[] children = attributeArea.getChildren();
		if (null == children || children.length == 0) {
			return;
		}
		for (Control control : children) {
			control.dispose();
			attributeArea.layout();
			scroll.layout();
		}
	}

	/**
	 * 得到自定义区域的属性和属性值
	 * @return ;
	 */
	public Map<String, String> getCustomArributes() {
		Map<String, String> attribute = new HashMap<String, String>();
		if (attributeArea != null && !attributeArea.isDisposed()) {
			// 自定义区的每一行控件
			Control[] children = attributeArea.getChildren();
			if (null == children || children.length == 0) {
				return attribute;
			}
			for (Control child : children) {
				Control[] children2 = ((Composite) child).getChildren();
				if (null == children2 || children2.length <= 3) {
					return attribute;
				}
				String key = ((Text) children2[0]).getText();
				String value = ((Text) children2[2]).getText();
				if (key != null && !key.isEmpty() && value != null && !value.isEmpty()) {
					attribute.put(key, value);
				}
			}

		}
		return attribute;
	}

	// 初始化控件状态和数据
	public void initDataState() {
		srcPathText.setEditable(false);
		newTmxPathText.setEditable(false);
		appendTmxPathText.setEditable(false);
		browserExistTmxPathBtn.setEnabled(false);
		conver2NewTmxBtn.setSelection(true);
		isOpenBtn.setSelection(false);
	}

	private void setOKState() {
		if (conver2NewTmxBtn.getSelection()) {
			if (!srcPathText.getText().isEmpty() && !newTmxPathText.getText().isEmpty()) {
				setOkBtnEnable(true);
			} else {
				setOkBtnEnable(false);
			}

		} else if (appendExistTmxPathBtn.getSelection()) {
			if (!srcPathText.getText().isEmpty() && !appendTmxPathText.getText().isEmpty()) {
				setOkBtnEnable(true);
			} else {
				setOkBtnEnable(false);
			}
		}
	}

	public File2TmxConvertBean getConvertBean() {

		final File2TmxConvertBean bean = new File2TmxConvertBean();
		Display.getDefault().syncExec(new Runnable() {
			/**
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {

				bean.sourceFilePath = srcPathText.getText();

				if (addCustomAttributeBtn.getSelection()) {
					bean.customeAttr = getCustomArributes();
				}

				if (appendExistTmxPathBtn.getSelection()) {
					bean.appendExistTmxFilePath = appendTmxPathText.getText();
				} else {
					bean.appendExistTmxFilePath = null;
				}

				if (conver2NewTmxBtn.getSelection()) {
					String text = srcPathText.getText();
					File file = new File(text);
					String name = file.getName();
					name = name.substring(0, name.lastIndexOf(".")) + ".";
					if (newTmxPathText.getText().endsWith(File.separator)) {
						bean.newTmxFilePath = newTmxPathText.getText() + name + "tmx";
					} else {
						bean.newTmxFilePath = newTmxPathText.getText() + File.separator + name + "tmx";
					}

				} else {
					bean.newTmxFilePath = null;
				}

				bean.isOpenTmx = isOpenBtn.getSelection();
			}

		});
		this.convertBean = bean;
		return bean;
	}

	public File2TmxConvertBean getConvertParams() {
		return this.convertBean;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		File2TmxConvertBean convertBean = getConvertBean();
		try {
			checkConvertBean(convertBean);
			if (convertBean.newTmxFilePath != null) {
				final File fileTemp = new File(convertBean.newTmxFilePath);
				final boolean[] r = new boolean[1];
				if (fileTemp.exists()) {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							r[0] = OpenMessageUtils.openConfirmMessage(MessageFormat.format(
									Messages.getString("dialog.TmxConvert2FileDialog.overwriteFile"),
									fileTemp.getName()));
						}
					});
					if (!r[0]) {
						return;
					}
				}
			}
			// TODO Auto-generated method stub
			AbstractFile2Tmx file2TmxConverter = ConverterFactory.getFile2TmxConverter(convertBean.sourceFilePath);
			monitor.beginTask("", 100);
			monitor.setTaskName(Messages.getString("dialog.Convert2TmxDialog.convert.task.msg1"));
			SubProgressMonitor clearUnableCharSub = new SubProgressMonitor(monitor, 10);
			SubProgressMonitor sub = new SubProgressMonitor(monitor, 90);

			if (null != convertBean.appendExistTmxFilePath) {
				clearUnableCharSub.beginTask("", 100);
				clearUnableCharSub.worked(40);
				VTDGen newVg = TmxUtil.clearUnableParseChars(convertBean.appendExistTmxFilePath);
				clearUnableCharSub.done();
				if (null == newVg) {
					clearUnableCharSub.done();
					throw new Exception(
							Messages.getString("dialog.Convert2TmxDialog.converter.common.appendtmx.wrongTmx"));
				}
				newVg.clear();
			} else {
				sub = new SubProgressMonitor(monitor, 100);
			}
			file2TmxConverter.doCovnerter(convertBean, sub);
		} catch (Exception e) {
			LOGGER.error("convert error", e);
			if (convertBean.newTmxFilePath != null) {
				File file = new File(convertBean.newTmxFilePath);
				file.delete();
			}
			throw new InterruptedException(MessageFormat.format(
					Messages.getString("dialog.Convert2TmxDialog.convert.task.error"), e.getMessage()));
		} finally {
			monitor.done();
		}
	}

	/**
	 * 检查文件权限
	 * @param convertBean
	 * @throws Exception
	 *             ;
	 */
	public void checkConvertBean(File2TmxConvertBean convertBean) throws Exception {
		File tempFile = null;
		if (convertBean.sourceFilePath != null) {
			tempFile = new File(convertBean.sourceFilePath);
			if (!tempFile.canRead()) {
				throw new Exception(MessageFormat.format(
						Messages.getString("dialog.TmxConvert2FileDialog.sourceFileNotexist"),
						convertBean.sourceFilePath));
			}
		}
		if (convertBean.appendExistTmxFilePath != null) {
			tempFile = new File(convertBean.appendExistTmxFilePath);
			if (!tempFile.canWrite()) {
				throw new Exception(MessageFormat.format(
						Messages.getString("dialog.TmxConvert2FileDialog.targetFileNotexist"),
						convertBean.appendExistTmxFilePath));
			}
		} else if (convertBean.newTmxFilePath != null) {
			tempFile = new File(convertBean.newTmxFilePath);
			File parentFile = tempFile.getParentFile();
			File createTempFile = null;// test the directory can write.
			try {
				createTempFile = File.createTempFile("test", ".temp", parentFile);
			} catch (Exception e) {
				throw new Exception(MessageFormat.format(
						Messages.getString("dialog.TmxConvert2FileDialog.targetFileNotexist"), tempFile.getParent()));
			} finally {
				if (null != createTempFile) {
					createTempFile.delete();
				}
			}

		}
	}

	/**
	 * 默认按下任务运行 (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		if (this instanceof IRunnableWithProgress) {
			try {
				run(isFork(), canCancel(), (IRunnableWithProgress) this);
				if (getProgressMonitor().isCanceled()) {
					return;
				}
				OpenMessageUtils.openMessage(IStatus.INFO, Messages.getString("dialog.Convert2TmxDialog.taskFinished"));
				setReturnCode(OK);
				close();
			} catch (Exception e) {
				LOGGER.error("", e);
				OpenMessageUtils.openMessage(IStatus.ERROR, "" + e.getMessage());
				return;
			}
		}
		File2TmxConvertBean convertBean = getConvertParams();
		String path = "";
		if (convertBean.isOpenTmx) {
			if (convertBean.appendExistTmxFilePath != null) {
				path = convertBean.appendExistTmxFilePath;
			} else {
				path = convertBean.newTmxFilePath;
			}
			OpenTmxFileHandler.open(new File(path));
		}
		return;
	}

	@Override
	public boolean close() {
		if (getShell() != null && !getShell().isDisposed()) {
			writeDialogSettings();
		}
		return super.close();
	}

	private void writeDialogSettings() {
		IDialogSettings dialogSettings = getDialogSettings();
		dialogSettings.put("isOpen", isOpenBtn.getSelection());
		String[] convertMap2StrArrays = convertMap2StrArrays();
		dialogSettings.put("isCustom", addCustomAttributeBtn.getSelection());
		dialogSettings.put("CustomValues", convertMap2StrArrays);
	}

	private void loadDialogSettings() {
		IDialogSettings dialogSettings = getDialogSettings();
		isOpenBtn.setSelection(dialogSettings.getBoolean("isOpen"));
		boolean isCustomSlect = dialogSettings.getBoolean("isCustom");
		if (!isCustomSlect) {
			return;
		}
		addCustomAttributeBtn.setSelection(isCustomSlect);
		String[] array = dialogSettings.getArray("CustomValues");
		if (null == array || array.length == 0) {
			if (isCustomSlect) {
				createArributeArea(attributeArea, null, null);
			}
			return;
		}
		for (String key_value : array) {
			String[] split = key_value.split(ID_MARK);
			createArributeArea(attributeArea, split[0], split[1]);
		}
	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings fDialogSettings = settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings = settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}

	public final static String ID_MARK = "\uFFF1";

	private String[] convertMap2StrArrays() {
		Map<String, String> customArributes = getCustomArributes();
		if (null == customArributes || customArributes.isEmpty()) {
			return new String[0];
		}
		String[] sArrays = new String[customArributes.size()];
		Set<Entry<String, String>> entrySet = customArributes.entrySet();
		int i = 0;
		for (Entry<String, String> entry : entrySet) {
			String key = entry.getKey();
			String value = entry.getValue();
			sArrays[i] = key + ID_MARK + value;
			i++;
		}
		return sArrays;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractProgressDialog#getDisplayHelpUrl()
	 */
	@Override
	public String getDisplayHelpUrl() {
		return "/net.heartsome.cat.te.ui.help/html/{0}/ch07.html#to_tmx_id";
	}
}
