/**
 * NewTmxFileDialog.java
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

package net.heartsome.cat.te.ui.newtmx.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.ui.dialog.HsAbstractHelpDilog;
import net.heartsome.cat.common.ui.languagesetting.LanguageLabelProvider;
import net.heartsome.cat.te.core.bean.TmxTemplet;
import net.heartsome.cat.te.tmxeditor.TmxEditorUtils;
import net.heartsome.cat.te.ui.Activator;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 创建　tmx 文件 2013-06-07
 * @author robert
 * @version
 * @since JDK1.6
 */
public class NewTmxFileDialog extends HsAbstractHelpDilog {
	/** 保存 tmx 文件存放路径的文本框 */
	private Text locationTxt;
	private String newFilePath = null;
	private List<Language> languages;
	private Language srcLanguage;
	private Language tgtLanguage;
	private TableComboViewer srcLangComboViewer;
	private TableComboViewer tgtLangComboViewer;
	private FileOutputStream output = null;
	public static Logger LOGGER = LoggerFactory.getLogger(NewTmxFileDialog.class.getName());

	public NewTmxFileDialog(Shell parentShell) {
		super(parentShell);
		// 获取语言列表
		languages = new ArrayList<Language>(LocaleService.getDefaultLanguage().values());
		Collections.sort(languages, new Comparator<Language>() {
			public int compare(Language o1, Language o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.TITLE | SWT.CLOSE;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setMaximized(false);
		newShell.setMinimized(false);
		newShell.setText(Messages.getString("newTmx.NewTmxFileDialog.title"));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button okBtn = getButton(IDialogConstants.OK_ID);
		okBtn.setText(Messages.getString("ui.all.dialog.ok"));
		Button cancelBtn = getButton(IDialogConstants.CANCEL_ID);
		cancelBtn.setText(Messages.getString("ui.all.dialog.cancel"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parentData.widthHint = 450;
		parentData.heightHint = 150;
		tparent.setLayoutData(parentData);

		// 设置新创建的文件所保存的位置
		Composite locationCmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(locationCmp);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(locationCmp);

		Label lcLbl = new Label(locationCmp, SWT.NONE);
		lcLbl.setText(Messages.getString("newtmx.NewTmxFileDialog.saveLCLbl"));

		locationTxt = new Text(locationCmp, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(locationTxt);
		locationTxt.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		Button browseBtn = new Button(locationCmp, SWT.NONE);
		browseBtn.setText(Messages.getString("newtmx.NewTmxFileDialog.browseBtn"));
		browseBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseTmxLc();
			}
		});

		Group langGroup = new Group(tparent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(langGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(langGroup);
		langGroup.setText(Messages.getString("newtmx.NewTmxFileDialog.langGroup"));

		// 源语言
		Label srcLangLbl = new Label(langGroup, SWT.NONE);
		srcLangLbl.setText(Messages.getString("newtmx.NewTmxFileDialog.srcLang"));

		srcLangComboViewer = new TableComboViewer(langGroup, SWT.READ_ONLY | SWT.BORDER);
		TableCombo tableCombo = srcLangComboViewer.getTableCombo();
		tableCombo.setShowTableLines(false);
		tableCombo.setShowTableHeader(false);
		tableCombo.setDisplayColumnIndex(-1);
		tableCombo.setShowImageWithinSelection(true);
		tableCombo.setShowColorWithinSelection(false);
		tableCombo.setShowFontWithinSelection(false);
		tableCombo.setVisibleItemCount(20);
		srcLangComboViewer.getTableCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		srcLangComboViewer.setLabelProvider(new LanguageLabelProvider());
		srcLangComboViewer.setContentProvider(new ArrayContentProvider());
		srcLangComboViewer.setInput(languages);
		srcLangComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				srcLanguage = (Language) selection.getFirstElement();
			}
		});

		// 目标语言
		Label tgtLangLbl = new Label(langGroup, SWT.NONE);
		tgtLangLbl.setText(Messages.getString("newtmx.NewTmxFileDialog.tgtLang"));

		tgtLangComboViewer = new TableComboViewer(langGroup, SWT.READ_ONLY | SWT.BORDER);
		tableCombo = tgtLangComboViewer.getTableCombo();
		tableCombo.setShowTableLines(false);
		tableCombo.setShowTableHeader(false);
		tableCombo.setDisplayColumnIndex(-1);
		tableCombo.setShowImageWithinSelection(true);
		tableCombo.setShowColorWithinSelection(false);
		tableCombo.setShowFontWithinSelection(false);
		tableCombo.setVisibleItemCount(20);
		tgtLangComboViewer.getTableCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tgtLangComboViewer.setLabelProvider(new LanguageLabelProvider());
		tgtLangComboViewer.setContentProvider(new ArrayContentProvider());
		tgtLangComboViewer.setInput(languages);
		tgtLangComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				tgtLanguage = (Language) selection.getFirstElement();
			}
		});

		return tparent;
	}

	private void browseTmxLc() {
		FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
		String[] filterExt = { "*.tmx", "*.*" };
		dlg.setFilterExtensions(filterExt);
		String filePath = dlg.open();
		if (filePath != null) {
			locationTxt.setText(filePath);
		}
	}

	@Override
	protected void okPressed() {
		// 首先验证数据
		newFilePath = null;
		String tmxLC = locationTxt.getText().trim();
		if (tmxLC == null || tmxLC.isEmpty()) {
			MessageDialog.openWarning(getShell(), Messages.getString("ui.all.dialog.error"),
					Messages.getString("newtmx.NewTmxFileDialog.tmxLocationNotNull"));
			return;
		}

		String srcLang = null;
		if (srcLanguage == null || (srcLang = srcLanguage.getCode().trim()).isEmpty()) {
			MessageDialog.openWarning(getShell(), Messages.getString("ui.all.dialog.error"),
					Messages.getString("newtmx.NewTmxFileDialog.notSelectSrcLang"));
			return;
		}

		String tgtLang = null;
		if (tgtLanguage == null || (tgtLang = tgtLanguage.getCode().trim()).isEmpty()) {
			MessageDialog.openWarning(getShell(), Messages.getString("ui.all.dialog.error"),
					Messages.getString("newtmx.NewTmxFileDialog.notSelectTgtLang"));
			return;
		}

		if (srcLang.equalsIgnoreCase(tgtLang)) {
			MessageDialog.openWarning(getShell(), Messages.getString("ui.all.dialog.error"),
					Messages.getString("newtmx.NewTmxFileDialog.srcLangEqualsTgtLang"));
			return;
		}
		// 　判断文件是否重复
		if (new File(tmxLC).exists()) {
			boolean result = MessageDialog.openConfirm(getShell(), Messages.getString("ui.all.dialog.warning"),
					MessageFormat.format(Messages.getString("newtmx.NewTmxFileDialog.tmxExsit"), tmxLC));
			if (!result) {
				return;
			}
		}

		IStatus createResult = createFile(tmxLC, srcLang, tgtLang);
		if (createResult.getSeverity() == IStatus.ERROR) {
			MessageDialog.openWarning(getShell(), Messages.getString("ui.all.dialog.error"),
					Messages.getString("newtmx.NewTmxFileDialog.createFail") + "\n" + createResult.getMessage());
			return;
		}
		newFilePath = tmxLC;
		super.okPressed();
	}

	/**
	 * The new file path, or null
	 * @return ;
	 */
	public String getNewFilePath() {
		return newFilePath;
	}

	/**
	 * 创建一个新的 tmx 文件
	 * @param filePath
	 * @param srcLang
	 * @param tgtLang
	 */
	private IStatus createFile(String filePath, String srcLang, String tgtLang) {
		try {
			TmxHeader header = TmxTemplet.generateTmxHeader(srcLang, null, null, null, null, null, null);
			TmxTU tu = TmxEditorUtils.createTmxTu(srcLang, tgtLang);
			File newTmxFile = new File(filePath);
			output = new FileOutputStream(newTmxFile);

			// 创建　tmx 信息
			writeString(TmxTemplet.genertateTmxXmlDeclar());
			writeString("<tmx version=\"1.4\">\n");
			writeString(TmxTemplet.header2Xml(header));
			writeString("<body>\n");
			// 开始创建　tu
			// UNDO 这里还有些属性未完善
			StringBuffer sb = new StringBuffer();
			sb.append("<tu");
			if (tu.getCreationUser() != null) {
				sb.append(" creationid=\"" + tu.getCreationUser() + "\"");
			}
			if (tu.getChangeUser() != null) {
				sb.append(" changeid=\"" + tu.getChangeUser() + "\"");
			}
			if (tu.getCreationDate() != null) {
				sb.append(" creationdate=\"" + tu.getCreationDate() + "\"");
			}
			if (tu.getChangeDate() != null) {
				sb.append(" changedate=\"" + tu.getChangeDate() + "\"");
			}
			if (tu.getCreationTool() != null) {
				sb.append(" creationtool=\"" + tu.getCreationTool() + "\"");
			}
			if (tu.getCreationToolVersion() != null) {
				sb.append(" creationtoolversion=\"" + tu.getCreationToolVersion() + "\"");
			}
			if (tu.getAttributes() != null)
				for (Entry<String, String> entry : tu.getAttributes().entrySet()) {
					sb.append(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
				}
			sb.append(">\n");
			writeString(sb.toString());

			sb = new StringBuffer();
			sb.append("<tuv xml:lang=\"" + tu.getSource().getLangCode() + "\"><seg></seg></tuv>\n");
			sb.append("<tuv xml:lang=\"" + tu.getTarget().getLangCode() + "\"><seg></seg></tuv>\n");
			writeString(sb.toString());
			writeString("</tu>\n");

			writeString("</body>\n");
			writeString("</tmx>\n");
			return new Status(IStatus.OK, Activator.PLUGIN_ID, null);
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
		} catch (Exception e) {
			LOGGER.error("", e);
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, null);
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (Exception e2) {
				LOGGER.error("", e2);
			}
		}
	}

	private void writeString(String string) throws IOException {
		output.write(string.getBytes());
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractHelpDilog#getDisplayHelpUrl()
	 */
	@Override
	public String getDisplayHelpUrl() {
		return "/net.heartsome.cat.te.ui.help/html/{0}/ch03.html#create_tmx_id";
	}

}
