/**
 * BatchSelectionsBean.java
 *
 * Version information :
 *
 * Date:2013-8-6
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.ui.task.ui;

/**
 * 批量任务的选择
 * @author yule
 * @version
 * @since JDK1.6
 */
public class BatchSelectionsBean {

	/**
	 * 是否忽略标记
	 */
	private boolean isIgnoreTag;

	/**
	 * 是否删除空行
	 */
	private boolean isDeleteEmpty;

	/**
	 * 是否上次重复文本段
	 */
	private boolean isDeleteDupliacate;

	/**
	 * 是否删除相同原文不同译文
	 */
	private boolean isDeleteSameSrcDiffTgt;

	/**
	 * 是否删除段首，段末空格
	 */
	private boolean isTrimSegment;
    /**
     * 是否忽略大小写
     */
	private boolean isIgnoreCase;
	
	public boolean isIgnoreCase() {
		return this.isIgnoreCase;
	}

	public void setIgnoreCase(boolean isIgnoreCase) {
		this.isIgnoreCase = isIgnoreCase;
	}

	/** @return the isIgnoreTag */
	public boolean isIgnoreTag() {
		return isIgnoreTag;
	}

	/** @param isIgnoreTag the isIgnoreTag to set */
	public void setIgnoreTag(boolean isIgnoreTag) {
		this.isIgnoreTag = isIgnoreTag;
	}

	/** @return the isDeleteEmpty */
	public boolean isDeleteEmpty() {
		return isDeleteEmpty;
	}

	/** @param isDeleteEmpty the isDeleteEmpty to set */
	public void setDeleteEmpty(boolean isDeleteEmpty) {
		this.isDeleteEmpty = isDeleteEmpty;
	}

	/** @return the isDeleteDupliacate */
	public boolean isDeleteDupliacate() {
		return isDeleteDupliacate;
	}

	/** @param isDeleteDupliacate the isDeleteDupliacate to set */
	public void setDeleteDupliacate(boolean isDeleteDupliacate) {
		this.isDeleteDupliacate = isDeleteDupliacate;
	}

	/** @return the isDeleteSameSrcDiffTgt */
	public boolean isDeleteSameSrcDiffTgt() {
		return isDeleteSameSrcDiffTgt;
	}

	/** @param isDeleteSameSrcDiffTgt the isDeleteSameSrcDiffTgt to set */
	public void setDeleteSameSrcDiffTgt(boolean isDeleteSameSrcDiffTgt) {
		this.isDeleteSameSrcDiffTgt = isDeleteSameSrcDiffTgt;
	}

	/** @return the isTrimSegment */
	public boolean isTrimSegment() {
		return isTrimSegment;
	}

	/** @param isTrimSegment the isTrimSegment to set */
	public void setTrimSegment(boolean isTrimSegment) {
		this.isTrimSegment = isTrimSegment;
	}

	public int getDeleteTypeCount(){
		int count = 0;
		if(isDeleteEmpty()){
			count++;
		}
		if(isTrimSegment()){
			count++;
		}
		if(isDeleteSameSrcDiffTgt()){
			count++;
		}
		if(isDeleteDupliacate()){
			count++;
		}
		return count;
	}
}
