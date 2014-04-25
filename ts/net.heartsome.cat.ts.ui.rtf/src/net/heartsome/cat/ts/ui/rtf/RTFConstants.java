package net.heartsome.cat.ts.ui.rtf;

/**
 * XLIFF 与 RTF 文件相互转化时所用到的常量接口
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public interface RTFConstants {
	
	/** XLIFF 中的标记在 RTF 文档中的显示形式 */
	String TAG_RTF = "<hs:tag>";
	
//	/** RTF 标记中的文本样式标记 */
//	String RTF_TEXT = "\\lang1024\\langfe1024\\noproof\\insrsid15945054 \\loch\\af0\\dbch\\af13\\hich\\f0 ";
	
	/** RTF 标记中的加粗样式 */
	String RTF_BOLD = "\\b";
	
	/** RTF 标记中的加前景红色 */
	String RTF_COLOR_RED = "\\cf6";
	
	/** models 文件夹路径 */
	String RTF_MODEL_PATH = "models";
	
	String fileSeparator = System.getProperty("file.separator");
	
	/** 默认模板文件路径 */
	String RTF_DEFAULT_MODEL_PATH = RTF_MODEL_PATH + fileSeparator + "default_model.rtf";
	String RTF_DEFAULT_MODEL_RELATIVE_PATH = RTF_MODEL_PATH + "/default_model.rtf";
	
	/** 带文本段状态字段的模板文件路径 */
	String RTF_MODEL_WITH_STATUS_ZH = RTF_MODEL_PATH + fileSeparator + "model_with_status_zh.rtf";
	String RTF_MODEL_WITH_STATUS_ZH_RELATIVE_PATH = RTF_MODEL_PATH + "/model_with_status_zh.rtf";
	String RTF_MODEL_WITH_STATUS_EN = RTF_MODEL_PATH + fileSeparator + "model_with_status_en.rtf";
	String RTF_MODEL_WITH_STATUS_EN_RELATIVE_PATH = RTF_MODEL_PATH + "/model_with_status_en.rtf";
	
	/** 带批注状态字段的模板文件路径 */
	String RTF_MODEL_WITH_COMMENTS = RTF_MODEL_PATH + fileSeparator + "model_with_comment.rtf";
	String RTF_MODEL_WITH_COMMENTS_RELATIVE_PATH = RTF_MODEL_PATH + "/model_with_comment.rtf";
	
	/** 带文本段状态字段和批注状态字段的模板文件路径 */
	String COMMENTS_AND_STATUS_ZH = RTF_MODEL_PATH + fileSeparator + "model_with_comment_and_status_zh.rtf";
	String COMMENTS_AND_STATUS_ZH_RELATIVE_PATH = RTF_MODEL_PATH + "/model_with_comment_and_status_zh.rtf";
	String COMMENTS_AND_STATUS_EN = RTF_MODEL_PATH + fileSeparator + "model_with_comment_and_status_en.rtf";
	String COMMENTS_AND_STATUS_EN_RELATIVE_PATH = RTF_MODEL_PATH + "/model_with_comment_and_status_en.rtf";
	
	String RTF_MODEL_COLUMN_ID = "ID";
	
	String RTF_MODEL_COLUMN_COMMENTS = "Comments";
	
	String RTF_MODEL_COLUMN_STATUS = "Status";
	
	/** 未翻译 */
	int STATUS_NOT_TRANSLATE = 0;
	
	/** 草稿 */
	int STATUS_NEW = 1;
	
	/** 完成翻译 */
	int STATUS_TRANSLATED = 2;
	
	/** 已批准 */
	int STATUS_APPROVED = 3;
	
	/** 签发 */
	int STATUS_SIGNED_OFF = 4;
	
	/** 锁定 */
	int STATUS_LOCKED = 7;
	
	/** 不添加到记忆库 */
	int STATUS_NOT_SEND_TO_TM = 8;
	
	/** 疑问 */
	int STATUS_NEED_REVIEW = 9;
	
	String EXPORT_GROUP_IMAGE_PATH = "icons/export_info.png";
}
