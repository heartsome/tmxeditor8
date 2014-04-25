/**
 * PairFileBean.java
 *
 * Version information :
 *
 * Date:2013-8-22
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.bean;

import net.heartsome.cat.common.locale.Language;

/**
 * 文件配对
 * @author  Austen
 * @version 
 * @since   JDK1.6
 */
public class PairFileBean {

	/**  生成 tmx 的源语言. */
	private Language srcLanguage;
	private String srcPath;
	
	/**  生成 tmx 的目标语言. */
	private Language tgtLanguage;
	private String tgtPath;
	
	/**  saveAsUri. */
	private String savePath;

	private boolean machineTrans = false;
	private boolean openAfterPair = true;
	
	public Language getSrcLanguage() {
		return srcLanguage;
	}

	public void setSrcLanguage(Language srcLanguage) {
		this.srcLanguage = srcLanguage;
	}

	public String getSrcPath() {
		return srcPath;
	}

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}

	public Language getTgtLanguage() {
		return tgtLanguage;
	}

	public void setTgtLanguage(Language tgtLanguage) {
		this.tgtLanguage = tgtLanguage;
	}

	public String getTgtPath() {
		return tgtPath;
	}

	public void setTgtPath(String tgtPath) {
		this.tgtPath = tgtPath;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public boolean isMachineTrans() {
		return machineTrans;
	}

	public void setMachineTrans(boolean machineTrans) {
		this.machineTrans = machineTrans;
	}

	public boolean isOpenAfterPair() {
		return openAfterPair;
	}

	public void setOpenAfterPair(boolean openAfterPair) {
		this.openAfterPair = openAfterPair;
	}
	
}
