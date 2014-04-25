/**
 * ExportBean.java
 *
 * Version information :
 *
 * Date:2013-12-26
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.bean;

import java.util.List;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ExportBean {

	private int exportScope;

	private String targetFile;

	private boolean isAppend;

	private List<String> selectIds;

	public ExportBean() {
	}
	
	public ExportBean(int exportScope, String targetFile, boolean isAppend) {
		this.exportScope = exportScope;
		this.targetFile = targetFile;
		this.isAppend = isAppend;
	}

	public int getExportScope() {
		return exportScope;
	}

	public void setExportScope(int exportScope) {
		this.exportScope = exportScope;
	}

	public String getTargetFile() {
		return targetFile;
	}

	public void setTargetFile(String targetFile) {
		this.targetFile = targetFile;
	}

	public boolean isAppend() {
		return isAppend;
	}

	public void setAppend(boolean isAppend) {
		this.isAppend = isAppend;
	}

	public List<String> getSelectIds() {
		return selectIds;
	}

	public void setSelectIds(List<String> selectIds) {
		this.selectIds = selectIds;
	}
}
