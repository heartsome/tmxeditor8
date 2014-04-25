/**
 * TmxEditorSelection.java
 *
 * Version information :
 *
 * Date:2013-6-9
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.bean;

import java.util.LinkedHashMap;
import java.util.Map;

import net.heartsome.cat.common.bean.TmxTU;

/**
 * @author Austen
 * @version
 * @since JDK1.6
 */
public class TmxEditorSelection {
	
	private String identifier;
	private int selectedColumn = -1;
	private LinkedHashMap<String, TmxTU> selectedTus;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public int getSelectedColumn() {
		return selectedColumn;
	}

	public void setSelectedColumn(int selectedColumn) {
		this.selectedColumn = selectedColumn;
	}
	
	public void addTu(String identifier, TmxTU tu) {
		if (selectedTus == null) {
			selectedTus = new LinkedHashMap<String, TmxTU>();
		}
		selectedTus.put(identifier, tu);
	}
	
	public TmxTU getTu(String identifier) {
		return selectedTus.get(identifier);
	}
	
	public Map<String, TmxTU> getTus() {
		return selectedTus == null ? new LinkedHashMap<String, TmxTU>() : selectedTus;
	}
	
	public TmxTU getDisplayTu() {
		return selectedTus == null ? null : selectedTus.get(identifier);
	}
	
}
