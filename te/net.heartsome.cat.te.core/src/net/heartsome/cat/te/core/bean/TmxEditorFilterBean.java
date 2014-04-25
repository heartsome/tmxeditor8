/**
 * TmxEditorFilterbean.java
 *
 * Version information :
 *
 * Date:2013/5/17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */

package net.heartsome.cat.te.core.bean;

import java.util.List;

/**
 * 过滤器规则封装类
 * @author robert	2013-06-19
 */
public class TmxEditorFilterBean {
	/** 唯一标识一个　filter 的属性 */
	private String id;
	/** 过虑器的描述，即名称 */
	private String name;
	/** 该过虑器是否可用 */
	private boolean useable;
	private boolean custom;
	/** 是否满足当前过滤器的所有条件，true：满足所有条件，　false：满足以下任意一条件 */
	private boolean fitAll;
	/** 源文的过滤条件 */
	private List<Property> srcFilter;
	/** 译文的过滤条件 */
	private List<Property> tgtFilter;
	/** 批注的过滤条件 */
	private List<Property> noteFilter;
	/** 固定属性的过滤条件，针对于　tu 头的属性 */
	private List<Property> fixedPropFilter;
	/** 自定义属性的过滤条件，针对　tu 节点下的　prop 节点 */
	private List<Property> customPropFilter;
	
	/** 针对过滤条件的查询语句 */
	private String xpath;
	
	public TmxEditorFilterBean(){ }

	/**
	 * 该构造函数专为　系统过滤器而开
	 * @param id
	 * @param name
	 */
	public TmxEditorFilterBean(String id, String name, String xpath){
		this.id = id;
		this.name = name;
		this.xpath = xpath;
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isUseable() {
		return useable;
	}

	public void setUseable(boolean useable) {
		this.useable = useable;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Property> getSrcFilter() {
		return srcFilter;
	}

	public void setSrcFilter(List<Property> srcFilter) {
		this.srcFilter = srcFilter;
	}

	public List<Property> getTgtFilter() {
		return tgtFilter;
	}

	public void setTgtFilter(List<Property> tgtFilter) {
		this.tgtFilter = tgtFilter;
	}

	public List<Property> getNoteFilter() {
		return noteFilter;
	}

	public void setNoteFilter(List<Property> noteFilter) {
		this.noteFilter = noteFilter;
	}

	public List<Property> getFixedPropFilter() {
		return fixedPropFilter;
	}

	public void setFixedPropFilter(List<Property> fixedPropFilter) {
		this.fixedPropFilter = fixedPropFilter;
	}

	public List<Property> getCustomPropFilter() {
		return customPropFilter;
	}

	public void setCustomPropFilter(List<Property> customPropFilter) {
		this.customPropFilter = customPropFilter;
	}

	public boolean isFitAll() {
		return fitAll;
	}

	public void setFitAll(boolean fitAll) {
		this.fitAll = fitAll;
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}
	
	
}
