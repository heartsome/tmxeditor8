package net.heartsome.cat.te.core.bean;

import net.heartsome.cat.common.util.TextUtil;

public class SimpleTUData {

	private String modifyTime;

	private String srcText;

	private String tgtText;

	private String tuId;

	public SimpleTUData() {
	}

	/**
	 * @param srcText
	 * @param tgtText
	 * @param tuId
	 * @param modifyTime
	 */
	public SimpleTUData(String srcText, String tgtText, String tuId, String modifyTime) {
		super();
		if (null != srcText) {
			this.srcText = TextUtil.trimString(srcText);
		}
		if (null != tgtText) {
			this.tgtText = TextUtil.trimString(tgtText);
		}
		this.tuId = tuId;
		this.modifyTime = modifyTime;
	}

	public String getModifyTime() {
		return this.modifyTime;
	}

	public String getSrcText() {
		return this.srcText;
	}

	public String getTgtText() {
		return this.tgtText;
	}

	public String getTuId() {
		return this.tuId;
	}

	public boolean isEmpty() {
		if (null == tuId) {
			return true;
		}
		if (null == srcText && null == tgtText) {
			return true;
		}
		return false;
	}

	public boolean isDulicateEquals(SimpleTUData other, boolean ignoreCase) {
		if (ignoreCase) {
			if (this.getSrcText().equalsIgnoreCase(other.getSrcText())
					&& this.getTgtText().equalsIgnoreCase(other.getTgtText())) {
				return true;
			} else {
				return false;
			}
		} else {
			if (this.getSrcText().equals(other.getSrcText()) && this.getTgtText().equals(other.getTgtText())) {
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean isSrcSameDiffTgtEquals(SimpleTUData other, boolean ignoreCase) {
		if (ignoreCase) {
			return this.getSrcText().equalsIgnoreCase(other.getSrcText());
		} else {
			return this.getSrcText().equals(other.getSrcText());				
		
		}
	}

	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}

	public void setSrcText(String srcText) {
		this.srcText = srcText;
	}

	public void setTgtText(String tgtText) {
		this.tgtText = tgtText;
	}

	public void setTuId(String tuId) {
		this.tuId = tuId;
	}

	@Override
	public String toString() {
		return tuId + "	" + "[" + modifyTime + "][" + srcText + "]	" + "	[" + tgtText + "]";
	}

}