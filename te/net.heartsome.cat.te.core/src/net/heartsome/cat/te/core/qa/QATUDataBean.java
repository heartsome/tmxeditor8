package net.heartsome.cat.te.core.qa;

/**
 * te 品质检查的 tu pojo 类。
 * @author  Mac
 * @version 
 * @since   JDK1.6
 */
public class QATUDataBean {
	/** 针对　tmx ，每个　tu 的 id */
	private String tuID;
	/** 源文的全文本 */
	private String srcPureText;
	/** 译文的全文本 */
	private String tgtPureText;
	/** 带标记的源文本，该属性用于标记一致性检查 */
	private String srcContent;
	/** 带标记的译文本，该属性用于标记一致性检查 */
	private String tgtContent;
	private String lineNumber;
	
	public QATUDataBean(){}
	
	public String getTuID() {
		return tuID;
	}
	public void setTuID(String tuID) {
		this.tuID = tuID;
	}
	public String getSrcPureText() {
		return srcPureText;
	}
	public void setSrcPureText(String srcPureText) {
		this.srcPureText = srcPureText;
	}
	public String getTgtPureText() {
		return tgtPureText;
	}
	public void setTgtPureText(String tgtPureText) {
		this.tgtPureText = tgtPureText;
	}
	public String getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
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
