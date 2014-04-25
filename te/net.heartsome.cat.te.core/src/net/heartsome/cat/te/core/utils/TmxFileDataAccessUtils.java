/**
 * TmxFileDataAccessUtils.java
 *
 * Version information :
 *
 * Date:2013-5-30
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.utils;

import net.heartsome.cat.common.bean.TmxContexts;
import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.util.LanguageUtils;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.database.Utils;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;

/**
 * The Util of TMX file data access
 * @author Jason
 * @version 1.0
 * @since JDK1.6
 */
public class TmxFileDataAccessUtils {

	/**
	 * Read TU attributes for VTDNav
	 * @param vu
	 *            <code>VTDUtils<code>, The VTDNav cursor position must at TU node
	 * @param tu
	 *            the results save into this instance
	 * @throws VTDException
	 *             ;
	 */
	public static void readTUAttr4VTDNav(VTDUtils vu, TmxTU tu) throws VTDException {
		VTDNav vn = vu.getVTDNav();
		vn.push();
		AutoPilot apAttributes = new AutoPilot(vu.getVTDNav());
		apAttributes.selectXPath("@*");
		int inx = -1;
		while ((inx = apAttributes.evalXPath()) != -1) {
			String name = vn.toString(inx);
			inx = vn.getAttrVal(name);
			String value = inx != -1 ? vn.toString(inx) : "";
			// tuid, o-encoding, datatype, usagecount, lastusagedate, creationtool, creationtoolversion, creationdate,
			// creationid, changedate, segtype, changeid, o-tmf, srclang.
			if (name.equals("tuid")) {
				tu.setTuId(value);
			} else if (name.equals("creationtool")) {
				tu.setCreationTool(value);
			} else if (name.equals("creationtoolversion")) {
				tu.setCreationToolVersion(value);
			} else if (name.equals("creationdate")) {
				tu.setCreationDate(value);
			} else if (name.equals("creationid")) {
				tu.setCreationUser(value);
			} else if (name.equals("changedate")) {
				tu.setChangeDate(value);
			} else if (name.equals("changeid")) {
				tu.setChangeUser(value);
			} else {
				tu.appendAttribute(name, value);
			}
		}
		vn.pop();
	}

	/**
	 * Read current TU element all note child,<br>
	 * the <code>TmxNote<code> dbPk value is the position in siblings
	 * @param vu
	 *            <code>VTDUtils<code>,The VTDNav cursor position must at TU node
	 * @param tu
	 *            current TU storage <code>TmxTU<code>
	 * @throws VTDException
	 *             ;
	 */
	public static void readTUNote4VTDNav(VTDUtils vu, TmxTU tu) throws VTDException {
		VTDNav vn = vu.getVTDNav();
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./note");
		int dbPk = 1;
		while (ap.evalXPath() != -1) {
			String fragment = vu.getElementContent();
			TmxNote note = new TmxNote();
			note.setContent(fragment);
			int inx = vn.getAttrVal("xml:lang");
			String value = inx != -1 ? vn.toString(inx) : null;
			if (value != null) {
				note.setXmlLang(value);
			}
			inx = vn.getAttrVal("o-encoding");
			value = inx != -1 ? vn.toString(inx) : null;
			if (value != null) {
				note.setXmlLang(value);
			}
			note.setDbPk(dbPk);
			dbPk++;
			tu.appendNote(note);
		}
		vn.pop();
	}

	/**
	 * Read current TU element all prop child,<br>
	 * will read the contexts into <code>TmxTu<code> Contexts, instead of save in <code>TmxProp<code><br>
	 * the <code>TmxProp<code> dbPk value is the position in siblings
	 * @param vu
	 *            <code>VTDUtils<code>,The VTDNav cursor position must at TU node
	 * @param tu
	 *            current TU storage <code>TmxTU<code>
	 * @throws VTDException
	 */
	public static void readTUProp4VTDNav(VTDUtils vu, TmxTU tu) throws VTDException {
		VTDNav vn = vu.getVTDNav();
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./prop");
		int dbPk = 0;
		while (ap.evalXPath() != -1) {
			dbPk++;
			String content = vu.getElementContent();
			if (content == null) {
				continue;
			}
			content = TextUtil.resetSpecialString(content);
			int inx = vn.getAttrVal("type");
			String typeValue = inx != -1 ? vn.toString(inx) : null;
			if (typeValue == null) {
				continue;
			}
			if (typeValue.equals(TmxContexts.PRE_CONTEXT_NAME)) {
				tu.appendContext(TmxContexts.PRE_CONTEXT_NAME, content.trim());
			} else if (typeValue.equals(TmxContexts.NEXT_CONTEXT_NAME)) {
				tu.appendContext(TmxContexts.NEXT_CONTEXT_NAME, content.trim());
			} else if (typeValue.equals("x-Context")) {
				// Trados TMX file
				String[] contexts = content.split(",");
				if (contexts.length == 2) {
					tu.appendContext(TmxContexts.PRE_CONTEXT_NAME, contexts[0].trim());
					tu.appendContext(TmxContexts.NEXT_CONTEXT_NAME, contexts[1].trim());
				}
			} else {
				TmxProp p = new TmxProp(typeValue, content);
				p.setDbPk(dbPk);
				tu.appendProp(p);
			}
		}
		vn.pop();
	}

	/**
	 * Read current TU element all TUV child base on current source and target language,<br>
	 * @param vu
	 *            <code>VTDUtils<code>,The VTDNav cursor position must at TU node
	 * @param tu
	 *            current TU storage <code>TmxTU<code>
	 * @param srcLang
	 *            current need load source language
	 * @param tgtLang
	 *            current need load target language
	 * @throws VTDException
	 *             ;
	 */
	public static void readTUTuv4VTDNav(VTDUtils vu, TmxTU tu, String srcLang, String tgtLang) throws VTDException {
		VTDNav vn = vu.getVTDNav();
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./tuv");
		int dbPk = 0;
		// TUV 节点下的Note,Prop节点暂时不处理，所以此处暂时不解析
		while (ap.evalXPath() != -1) {
			dbPk++;
			int inx = vn.getAttrVal("xml:lang");
			inx = inx == -1 ? vn.getAttrVal("lang") : inx;
			String lang = inx != -1 ? vn.toString(inx) : null;
			lang = LanguageUtils.convertLangCode(lang);
			if (lang == null) {
				continue;
			}
			TmxSegement segment = null;
			vn.push();
			if (vu.pilot("./seg") != -1) {
				String fullText = vu.getElementContent();
				// String pureText = DocUtils.getTmxTbxPureText(vu);
				if (fullText == null/* || pureText == null || fullText.equals("") || pureText.equals("") */) {
					vn.pop();
					continue;
				}
				segment = new TmxSegement(fullText, Utils.convertLangCode(lang));
				segment.setDbPk(dbPk);
				if (lang.equalsIgnoreCase(srcLang)) {
					tu.setSource(segment);
				} else if (lang.equalsIgnoreCase(tgtLang)) {
					tu.setTarget(segment);
				} else {
					tu.appendSegement(segment);
				}
			}
			vn.pop();
			if (segment != null) {
				segment.setCreationTool(vu.getCurrentElementAttribut("creationtool", null));
				segment.setCreationToolVersion(vu.getCurrentElementAttribut("creationtoolversion", null));
				segment.setCreationDate(vu.getCurrentElementAttribut("creationdate", null));
				segment.setCreationUser(vu.getCurrentElementAttribut("creationid", null));
				segment.setChangeDate(vu.getCurrentElementAttribut("changedate", null));
				segment.setChangeUser(vu.getCurrentElementAttribut("changeid", null));
			}
		}
		if (tu.getTarget() == null) {
			TmxSegement segment = new TmxSegement("", Utils.convertLangCode(tgtLang));
			segment.setDbPk(-1);
			tu.setTarget(segment);
		}
		vn.pop();
	}
}
