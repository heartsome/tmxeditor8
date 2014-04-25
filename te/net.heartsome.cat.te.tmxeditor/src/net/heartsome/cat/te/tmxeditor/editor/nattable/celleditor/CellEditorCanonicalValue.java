/**
 * CellCanonicalValue.java
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
package net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;

/**
 * 用于<code>CellEditor</code> 数据封装
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class CellEditorCanonicalValue {
	/**
	 * 当前编辑的TU
	 */
	private TmxTU tu;
	/**
	 * 当前编辑TU 下的 TUV
	 */
	private TmxSegement tuv;

	/**
	 * 编辑后TUV的完整文本内容，包含标记
	 */
	private String newFullValue;

	/**
	 * 编辑后TUV的纯文本内容，不包含标记
	 */
	private String newPureText;

	/**
	 * @param tu
	 * @param tuv
	 */
	public CellEditorCanonicalValue(TmxTU tu, TmxSegement tuv) {
		super();
		this.tu = tu;
		this.tuv = tuv;
	}

	/** @return the newFullValue */
	public String getNewFullValue() {
		return newFullValue;
	}

	/**
	 * @param newFullValue
	 *            the newFullValue to set
	 */
	public void setNewFullValue(String newFullValue) {
		this.newFullValue = newFullValue;
	}

	/** @return the newPureText */
	public String getNewPureText() {
		return newPureText;
	}

	/**
	 * @param newPureText
	 *            the newPureText to set
	 */
	public void setNewPureText(String newPureText) {
		this.newPureText = newPureText;
	}

	/** @return the tu */
	public TmxTU getTu() {
		return tu;
	}

	/** @return the tuv */
	public TmxSegement getTuv() {
		return tuv;
	}

}
