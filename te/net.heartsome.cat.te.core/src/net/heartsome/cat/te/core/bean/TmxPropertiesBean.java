/**
 * TmxPropertiesBean.java
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

public class TmxPropertiesBean {
	private String version;
	private String location;
	private String fileSize;
	private String creator;
	private String creationDate;
	private String creationTooles;
	private String creationTooleVersion;
	private int tuNumber;
	private String srcLang;
	private List<String> targetLang;

	public TmxPropertiesBean(){
		
	}
	/**
	 * @param location
	 * @param srcLang
	 * @param targetLang
	 */
	public TmxPropertiesBean(String location, String srcLang, List<String> targetLang) {
		super();
		this.location = location;
		this.srcLang = srcLang;
		this.targetLang = targetLang;
	}

	/** @return the version */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/** @return the location */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/** @return the fileSize */
	public String getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize
	 *            the fileSize to set
	 */
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	/** @return the creator */
	public String getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/** @return the creationDate */
	public String getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	/** @return the creationTooles */
	public String getCreationTooles() {
		return creationTooles;
	}

	/**
	 * @param creationTooles
	 *            the creationTooles to set
	 */
	public void setCreationTooles(String creationTooles) {
		this.creationTooles = creationTooles;
	}

	/** @return the tuNumber */
	public int getTuNumber() {
		return tuNumber;
	}

	/**
	 * @param tuNumber
	 *            the tuNumber to set
	 */
	public void setTuNumber(int tuNumber) {
		this.tuNumber = tuNumber;
	}

	/** @return the srcLang */
	public String getSrcLang() {
		return srcLang;
	}

	/**
	 * @param srcLang
	 *            the srcLang to set
	 */
	public void setSrcLang(String srcLang) {
		this.srcLang = srcLang;
	}

	/** @return the targetLang */
	public List<String> getTargetLang() {
		return targetLang;
	}

	/**
	 * @param targetLang
	 *            the targetLang to set
	 */
	public void setTargetLang(List<String> targetLang) {
		this.targetLang = targetLang;
	}
	public String getCreationTooleVersion() {
		return creationTooleVersion;
	}
	public void setCreationTooleVersion(String creationTooleVersion) {
		this.creationTooleVersion = creationTooleVersion;
	}

}
