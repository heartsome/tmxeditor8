package net.heartsome.cat.ts.ui.rtf.exporter;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.rtf.Activator;
import net.heartsome.cat.ts.ui.rtf.RTFConstants;
import net.heartsome.cat.ts.ui.rtf.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XLIFF 文件导出 RTF 文件的工具类
 * @author peason
 * @version
 * @since JDK1.6
 */
public class XLIFF2RTFUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(XLIFF2RTFUtil.class.getName());

	public XLIFF2RTFUtil() {

	}

	/**
	 * xliff 文件导出为 rtf 文件
	 * @param xliffFile
	 *            xliff 文件对象
	 * @param srcLang
	 *            源语言
	 * @param tgtLang
	 *            目标语言
	 * @param isExportStatus
	 *            导出文本段状态
	 * @param isExportComment
	 *            是否导出批注
	 * @param saveRtfDirectory
	 *            RTF 文件保存目录
	 */
	public boolean exporter(IFile xliffFile, String srcLang, String tgtLang, boolean isExportStatus,
			boolean isExportComment, String saveRtfDirectory, boolean isExcludeOrOnlyExport,
			ArrayList<Boolean> lstSelection) {
		return exporter(ResourceUtils.iFileToOSPath(xliffFile), srcLang, tgtLang, isExportStatus, isExportComment,
				saveRtfDirectory, isExcludeOrOnlyExport, lstSelection);
	}

	/**
	 * xliff 文件导出为 rtf 文件
	 * @param xliffPath
	 *            xliff 文件路径
	 * @param srcLang
	 *            源语言
	 * @param tgtLang
	 *            目标语言
	 * @param isExportStatus
	 *            导出文本段状态
	 * @param isExportComment
	 *            是否导出批注
	 * @param saveRtfDirectory
	 *            RTF 文件保存目录
	 */
	public boolean exporter(String xliffPath, String srcLang, String tgtLang, boolean isExportStatus,
			boolean isExportComment, String saveRtfDirectory, boolean isExcludeOrOnlyExport,
			ArrayList<Boolean> lstSelection) {
		File out = new File(saveRtfDirectory);
		out.mkdir();
		String fileSeparator = System.getProperty("file.separator");
		String xliffName = xliffPath.substring(xliffPath.lastIndexOf(fileSeparator) + fileSeparator.length());
		xliffName = xliffName.substring(0, xliffName.lastIndexOf("."));
		// 当文件名中无目标语言后缀时，添加目标语言后缀
		if (xliffName.indexOf(tgtLang) < 0) {
			xliffName = xliffName + "_" + tgtLang;
		}
		File file = new File(saveRtfDirectory + fileSeparator + xliffName + ".rtf");
		if (file.exists()) {
			// Bug #2269：连续导出同一 xliff 文件的 RTF 文件，保存文件名存在问题
			String msg = Messages.getString("exporter.XLIFF2RTFUtil.msg2");
			Object args[] = { file.getAbsolutePath() };
			if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
					Messages.getString("exporter.XLIFF2RTFUtil.msg.title2"), new MessageFormat(msg).format(args))) {
				if (!file.delete()) {
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
							Messages.getString("exporter.XLIFF2RTFUtil.msg.title"), 
							MessageFormat.format(Messages.getString("exporter.XLIFF2RTFUtil.msg3"), file.getAbsolutePath()));
					return false;
				}
			} else {
				return false;
			}
		}

		RTFTemplateExporter rtfTemplate = new RTFTemplateExporter(saveRtfDirectory, xliffPath, srcLang, tgtLang,
				isExportStatus, isExportComment, isExcludeOrOnlyExport, lstSelection);

		try {
			Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
			String path = RTFConstants.RTF_DEFAULT_MODEL_PATH;
			URL defaultUrl = buddle.getEntry(RTFConstants.RTF_DEFAULT_MODEL_RELATIVE_PATH);
			if (isExportComment || isExportStatus) {
				if (isExportComment && isExportStatus) {
					if (CommonFunction.getSystemLanguage().equalsIgnoreCase("zh")) {
						path = RTFConstants.COMMENTS_AND_STATUS_ZH;
						defaultUrl = buddle.getEntry(RTFConstants.COMMENTS_AND_STATUS_ZH_RELATIVE_PATH);
					} else if (CommonFunction.getSystemLanguage().equalsIgnoreCase("en")) {
						path = RTFConstants.COMMENTS_AND_STATUS_EN;
						defaultUrl = buddle.getEntry(RTFConstants.COMMENTS_AND_STATUS_EN_RELATIVE_PATH);
					}
				} else if (isExportStatus) {
					if (CommonFunction.getSystemLanguage().equalsIgnoreCase("zh")) {
						path = RTFConstants.RTF_MODEL_WITH_STATUS_ZH;
						defaultUrl = buddle.getEntry(RTFConstants.RTF_MODEL_WITH_STATUS_ZH_RELATIVE_PATH);
					} else if (CommonFunction.getSystemLanguage().equalsIgnoreCase("en")) {
						path = RTFConstants.RTF_MODEL_WITH_STATUS_EN;
						defaultUrl = buddle.getEntry(RTFConstants.RTF_MODEL_WITH_STATUS_EN_RELATIVE_PATH);
					}
				} else if (isExportComment) {
					path = RTFConstants.RTF_MODEL_WITH_COMMENTS;
					defaultUrl = buddle.getEntry(RTFConstants.RTF_MODEL_WITH_COMMENTS_RELATIVE_PATH);
				}
			}

			String rtfSource = FileLocator.toFileURL(defaultUrl).getPath();
			rtfTemplate.run(rtfSource);

			// 重命名生成的文件
			String rtfOutput = saveRtfDirectory + fileSeparator
					+ path.substring(path.lastIndexOf(fileSeparator) + fileSeparator.length()) + "."
					+ rtfTemplate.getRtfTemplateImpl() + ".out.rtf";
			new File(rtfOutput).renameTo(file);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(Messages.getString("exporter.XLIFF2RTFUtil.logger"), e);
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
					Messages.getString("exporter.XLIFF2RTFUtil.msg.title"),
					Messages.getString("exporter.XLIFF2RTFUtil.msg"));
			return false;
		}
	}
}
