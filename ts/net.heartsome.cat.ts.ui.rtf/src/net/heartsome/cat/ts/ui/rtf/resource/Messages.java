package net.heartsome.cat.ts.ui.rtf.resource;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 国际化工具类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class Messages {
	public static final String DIALOG_EXPORT_TITLE = "dialog.ExportRTFDilaog.Title";
	
	public static final String DIALOG_EXPORT_XLIFF = "dialog.ExportRTFDilaog.lable.xliff";
	
	public static final String DIALOG_EXPORT_XLIFF_BROWSE = "dialog.ExportRTFDilaog.btnBrowseXLIFF";
	
	public static final String DIALOG_EXPORT_INFO = "dialog.ExportRTFDilaog.lable.lblInfo";
	
	public static final String DIALOG_EXPORT_GROUP = "dialog.ExportRTFDilaog.grpExport";
	
	public static final String DIALOG_EXPORT_STATUS = "dialog.ExportRTFDilaog.btnStatus";
	
	public static final String DIALOG_EXPORT_COMMENT = "dialog.ExportRTFDilaog.btnComment";
	
	public static final String DIALOG_EXPORT_PATH_RTF = "dialog.ExportRTFDilaog.label.rtfPath";
	
	public static final String DIALOG_EXPORT_RTF_BROWSE = "dialog.ExportRTFDilaog.btnBrowseRTF";
	
	public static final String DIALOG_EXPORT_INFO_0 = "dialog.ExportRTFDilaog.btnBrowseXLIFF.msg0";
	
	public static final String DIALOG_EXPORT_INFO_1 = "dialog.ExportRTFDilaog.btnBrowseXLIFF.msg1";
	
	public static final String DIALOG_EXPORT_INFO_2 = "dialog.ExportRTFDilaog.btnBrowseRTF.msg0";
	
	public static final String DIALOG_EXPORT_OK_TITLE = "dialog.ExportRTFDilaog.ok.title";
	
	public static final String DIALOG_EXPORT_OK_MSG_0 = "dialog.ExportRTFDilaog.ok.msg0";
	
	public static final String DIALOG_EXPORT_OK_MSG_1 = "dialog.ExportRTFDilaog.ok.msg1";
	
	public static final String DIALOG_EXPORT_OK_MSG_2 = "dialog.ExportRTFDilaog.ok.msg2";
	
	public static final String DIALOG_EXPORT_OK_MSG_3 = "dialog.ExportRTFDilaog.ok.msg3";
	
	public static final String DIALOG_EXPORT_OK_MSG_4 = "dialog.ExportRTFDilaog.ok.msg4";
	
	public static final String DIALOG_IMPORT_TITLE = "dialog.ImportRTFDialog.Title";
	
	public static final String DIALOG_IMPORT_XLIFF = "dialog.ImportRTFDialog.lable.xliff";
	
	public static final String DIALOG_IMPORT_XLIFF_BROWSE = "dialog.ImportRTFDialog.btnBrowseXLIFF";
	
	public static final String DIALOG_IMPORT_RTF = "dialog.ImportRTFDialog.label.rtf";
	
	public static final String DIALOG_IMPORT_RTF_BROWSE = "dialog.ImportRTFDialog.btnBrowseRTF";
	
	public static final String DIALOG_IMPORT_XLIFF_INFO_0 = "dialog.ImportRTFDialog.btnBrowseXLIFF.msg0";
	
	public static final String DIALOG_IMPORT_XLIFF_INFO_1 = "dialog.ImportRTFDialog.btnBrowseXLIFF.msg1";
	
	public static final String DIALOG_IMPORT_RTF_INFO = "dialog.ImportRTFDialog.btnBrowseRTF.msg";
	
	public static final String DIALOG_IMPORT_RTF_FILTER = "dialog.ImportRTFDialog.btnBrowseRTF.filter";
	
	public static final String DIALOG_IMPORT_OK_TITLE = "dialog.ImportRTFDialog.ok.title";
	
	public static final String DIALOG_IMPORT_OK_MSG_0 = "dialog.ImportRTFDialog.ok.msg0";
	
	public static final String DIALOG_IMPORT_OK_MSG_1 = "dialog.ImportRTFDialog.ok.msg1";
	
	public static final String DIALOG_IMPORT_OK_JOB_TITLE = "dialog.ImportRTFDialog.ok.jobTitle";
	
	public static final String IMPORTRTFTOXLIFF_JOBTITLE_0 = "importer.ImportRTFToXLIFF.jobTitle.0";
	
	public static final String IMPORTRTFTOXLIFF_JOBTITLE_1 = "importer.ImportRTFToXLIFF.jobTitle.1";
	
	public static final String IMPORTRTFTOXLIFF_JOBTITLE_2 = "importer.ImportRTFToXLIFF.jobTitle.2";
	
	public static final String IMPORTRTFTOXLIFF_JOBTITLE_3 = "importer.ImportRTFToXLIFF.jobTitle.3";
	
	public static final String IMPORTRTFTOXLIFF_JOBTITLE_4 = "importer.ImportRTFToXLIFF.jobTitle.4";
	
	public static final String IMPORTRTFTOXLIFF_JOBTITLE_5 = "importer.ImportRTFToXLIFF.jobTitle.5";
	
	public static final String IMPORTRTFTOXLIFF_JOBTITLE_6 = "importer.ImportRTFToXLIFF.jobTitle.6";
	
	public static final String IMPORTRTFTOXLIFF_JOBTITLE_7 = "importer.ImportRTFToXLIFF.jobTitle.7";
	
	public static final String IMPORTRTFTOXLIFF_JOBTITLE_8 = "importer.ImportRTFToXLIFF.jobTitle.8";
	
	public static final String IMPORTRTFTOXLIFF_JOBTITLE_9 = "importer.ImportRTFToXLIFF.jobTitle.9";
	
	public static final String IMPORTRTFTOXLIFF_JOBTITLE_10 = "importer.ImportRTFToXLIFF.jobTitle.10";
	
	private static final String BUNDLE_NAME = "net.heartsome.cat.ts.ui.rtf.resource.rtf";

	private static ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	public static String getString(String key) {
		try {
			return BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
}
