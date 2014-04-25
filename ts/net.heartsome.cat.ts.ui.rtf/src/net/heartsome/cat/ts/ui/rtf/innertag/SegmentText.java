package net.heartsome.cat.ts.ui.rtf.innertag;

/**
 * 分割文本类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class SegmentText {
	
	/** 文本在字符串中的开始索引 */
	private int startIndex;
	
	/** 文本在字符串中的结束索引 */
	private int endIndex;
	
	/** 起始索引之间的内容 */
	private String content;

	/**
	 * 构造方法
	 * @param startIndex
	 * @param endIndex
	 * @param content
	 * @param isTag
	 */
	public SegmentText(int startIndex, int endIndex, String content) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.content = content;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
