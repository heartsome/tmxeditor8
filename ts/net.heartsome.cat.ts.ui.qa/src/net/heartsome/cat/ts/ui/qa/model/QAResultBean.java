package net.heartsome.cat.ts.ui.qa.model;

/**
 * 品质检查结果pojo类
 * @author  robert	2012-04-20
 * @version 
 * @since   JDK1.6
 */
public class QAResultBean {
	/** 错误行号 */
	private String lineNumber;
	/** 品质检查类型 */
	private String qaType;
	/** 错误提示语句 */
	private String errorTip;
	/** 文件路径 */
	private String resource;
	private String langPair;
	private String rowId;
	/** 品质检查级别，若为0，则为错误，若为1，则为警告 */
	private int tipLevel;
	/** 品质检查项，取值为 QA_TERM, QA_PARAGRAPH 等等 */
	private String qaItem;
	/** 是否是自动拼写检查 */
	private boolean isAutoQA = false;
	
	
	public QAResultBean (){
	}
	
	public QAResultBean (String lineNumber, String qaType, String errorTip, String resource, String langPair, String rowId, int tipLevel, String qaItem){
		this.lineNumber = lineNumber;
		this.qaType = qaType;
		this.errorTip = errorTip;
		this.resource = resource;
		this.langPair = langPair;
		this.rowId = rowId;
		this.tipLevel = tipLevel;
		this.qaItem = qaItem;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getQaType() {
		return qaType;
	}

	public void setQaType(String qaType) {
		this.qaType = qaType;
	}

	public String getErrorTip() {
		return errorTip;
	}

	public void setErrorTip(String errorTip) {
		this.errorTip = errorTip;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getLangPair() {
		return langPair;
	}

	public void setLangPair(String langPair) {
		this.langPair = langPair;
	}

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public int getTipLevel() {
		return tipLevel;
	}

	public void setTipLevel(int tipLevel) {
		this.tipLevel = tipLevel;
	}

	public String getQaItem() {
		return qaItem;
	}

	public void setQaItem(String qaItem) {
		this.qaItem = qaItem;
	}

	public boolean isAutoQA() {
		return isAutoQA;
	}

	public void setAutoQA(boolean isAutoQA) {
		this.isAutoQA = isAutoQA;
	}
	
}
