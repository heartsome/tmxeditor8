/**
 * TmxHeaderTemple.java
 *
 * Version information :
 *
 * Date:2013-9-9
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.bean;

import java.util.Calendar;
import java.util.Locale;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.util.DateUtils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class TmxTemplet {

	/**
	 * 生成 TMX Header 节点
	 * @param srcLang
	 *            不能为空，符合语言代码标准
	 * @param dataType
	 *            为空时，默认值为unknown
	 * @param segtype
	 *            为空时，默认值为sentence
	 * @param otmf
	 *            为空时，默认值为 unknown
	 * @param creationtool
	 *            为空时，默认值为 RCP 产品配置名称
	 * @param createToolVersion
	 *            为空时，默认值为 RCP 产品版本号
	 * @param creationid
	 *            为空时，取系统属性 user.name
	 * @return ;
	 */
	public static TmxHeader generateTmxHeader(String srcLang, String dataType, String segtype, String otmf,
			String creationtool, String createToolVersion, String creationid) {
		Assert.isLegal(srcLang != null && srcLang.length() != 0);
		if (dataType == null || dataType.length() == 0) {
			dataType = "unknown";
		}
		if (segtype == null || segtype.length() == 0) {
			segtype = "sentence";
		}
		if (otmf == null || otmf.length() == 0) {
			otmf = "unknown";
		}
		if (creationtool == null || creationtool.length() == 0) {
			creationtool = getProductName();
		}
		if (createToolVersion == null || createToolVersion.length() == 0) {
			createToolVersion = getProductVersion();
		}
		if (creationid == null || creationid.length() == 0) {
			creationid = System.getProperty("user.name");
		}
		String nowUTC = DateUtils.formatToUTC(Calendar.getInstance().getTimeInMillis());
		TmxHeader header = new TmxHeader();
		header.setAdminlang(Locale.getDefault().toString().replaceAll("_", "-"));
		header.setSrclang(srcLang);
		header.setCreationtool(creationtool);
		header.setCreationtoolversion(getProductVersion());
		header.setDatatype(dataType);
		header.setOtmf(otmf);
		header.setSegtype(segtype);
		header.setCreationid(creationid);
		header.setCreationdate(nowUTC);
		return header;
	}

	/**
	 * 将 Header 生成 XML 内容
	 * @param tmxHeader
	 * @return ;
	 */
	public static String header2Xml(TmxHeader tmxHeader) {
		String lineSp = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		sb.append("<header").append(lineSp);
		sb.append("	creationtool=");
		sb.append(wrapTextUseQout(tmxHeader.getCreationtool())).append(lineSp);
		sb.append("	creationtoolversion=");
		sb.append(wrapTextUseQout(tmxHeader.getCreationtoolversion())).append(lineSp);
		sb.append("	srclang=");
		sb.append(wrapTextUseQout(tmxHeader.getSrclang())).append(lineSp);
		sb.append("	adminlang=");
		sb.append(wrapTextUseQout(tmxHeader.getAdminlang())).append(lineSp);
		sb.append("	datatype=");
		sb.append(wrapTextUseQout(tmxHeader.getDatatype())).append(lineSp);
		sb.append("	o-tmf=");
		sb.append(wrapTextUseQout(tmxHeader.getOtmf())).append(lineSp);
		sb.append("	segtype=");
		sb.append(wrapTextUseQout(tmxHeader.getSegtype())).append(lineSp);
		sb.append("	creationid=");
		sb.append(wrapTextUseQout(tmxHeader.getCreationid())).append(lineSp);
		sb.append("	creationdate=");
		sb.append(wrapTextUseQout(tmxHeader.getCreationdate()));
		sb.append(">").append(lineSp);
		sb.append("</header>").append(lineSp);
		return sb.toString();
	}

	/**
	 * 生成 TMX XML申明部分
	 * @return ;
	 */
	public static String genertateTmxXmlDeclar() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(System.getProperty("line.separator"));
		sb.append(
				"<!DOCTYPE tmx PUBLIC \"-//LISA OSCAR:1998//DTD for Translation Memory eXchange//EN\" \"tmx14.dtd\" >")
				.append(System.getProperty("line.separator"));
		return sb.toString();
	}

	private static String wrapTextUseQout(String srcText) {
		return "\"" + srcText + "\"";
	}

	public static String getProductVersion() {
		String version = System.getProperty("version", "");//$NON-NLS-1$ //$NON-NLS-2$
		return (null == version || version.isEmpty()) ? "8.0" : version;
	}

	public static String getProductName() {
		String name = Platform.getProduct().getName();
		return (null == name || name.isEmpty()) ? "Heartsome TMX Editor" : name;
	}
}
