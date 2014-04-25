package net.heartsome.cat.te.core.qa;

/**
 * 品质检查结果数据类
 * @author  robert	2013-09-22
 * @version 
 * @since   JDK1.6
 */
public class QAResultBean {
	private String tuID;
	
	private String lineNumber;
	private String qaType;
	private String qaTypeText;
	private String mergeID;
	private String srcContent;
	private String tgtContent;
	
	
	public QAResultBean(){ }
	
	public QAResultBean(String tuID, String qaType, String lineNumber, String qaTypeText, String mergeID, String srcContent, String tgtContent){
		this.tuID = tuID;
		this.qaType = qaType;
		this.lineNumber = lineNumber;
		this.qaTypeText = qaTypeText;
		this.mergeID = mergeID;
		this.srcContent = srcContent;
		this.tgtContent = tgtContent;
	}

	public String getTuID() {
		return tuID;
	}

	public void setTuID(String tuID) {
		this.tuID = tuID;
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

	public String getQaTypeText() {
		return qaTypeText;
	}

	public void setQaTypeText(String qaTypeText) {
		this.qaTypeText = qaTypeText;
	}

	public String getMergeID() {
		return mergeID;
	}

	public void setMergeID(String mergeID) {
		this.mergeID = mergeID;
	}

	public String getSrcContent() {
		return srcContent;
	}

	public void setSrcContent(String srcContent) {
		this.srcContent = srcContent;
	}

	public String getTgtContent() {
		return tgtContent;
	}

	public void setTgtContent(String tgtContent) {
		this.tgtContent = tgtContent;
	}



	
	
	
}
