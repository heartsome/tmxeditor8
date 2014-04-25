/**
 * TmxEditorFindReplace.java
 *
 * Version information :
 *
 * Date:2013-8-7
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.editor.nattable.search;

import java.util.List;

import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileContainer;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileDataAccess;
import net.heartsome.cat.te.core.utils.TeCoreUtils;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.jface.text.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;

/**
 * Find implement with TMX file opened in NatTable
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxFileFindReplaceImpl extends AbstractFindReplace {

	public static final Logger LOGGER = LoggerFactory.getLogger(TmxFileFindReplaceImpl.class);
	private TmxLargeFileDataAccess tmxDataAccess;

	public TmxFileFindReplaceImpl(TmxLargeFileDataAccess dataAccess) {
		super(dataAccess);
		this.tmxDataAccess = dataAccess;
	}

	public FindReasult find(int startRow, int offset, String lang, String findStr) {
		TmxLargeFileContainer container = (TmxLargeFileContainer) tmxDataAccess.getTmxContainer();
		List<String> tuIdentifier = tmxDataAccess.getDisplayTuIdentifiers();
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		try {
			List<String> subFiles = container.getSubFiles();
			String tuIdStr = tuIdentifier.get(startRow);
			String[] strs = TeCoreUtils.parseTuIndentifier(tuIdStr);
			int startf = subFiles.indexOf(strs[0]);
			if (searchForward) {
				String xpath = "./following::tu/tuv[lower-case(@xml:lang)='" + lang.toLowerCase() + "']/seg";
				for (int i = startf; i < subFiles.size(); i++) {
					VTDUtils vu = container.getVTDUtils(subFiles.get(i));
					AutoPilot apTemp = new AutoPilot(vu.getVTDNav());
					apTemp.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
					
					if (i == startf
							&& vu.pilot(apTemp, "/tmx/body/tu[@hsid='" + strs[1] + "']/tuv[lower-case(@xml:lang)='"
									+ lang.toLowerCase() + "']/seg") != -1) {

					} else {
						apTemp.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
						if (vu.pilot(apTemp,"/tmx/body/tu/tuv[lower-case(@xml:lang)='" + lang.toLowerCase() + "']/seg") == -1) {
							continue;
						}
					}
					FindReasult result = forwardReadContent4Match(subFiles.get(i), vu, findStr, offset);
					if (result != null) {
						return result;
					}
					ap.bind(vu.getVTDNav());
					ap.selectXPath(xpath);
					offset = -1;
					while (ap.evalXPath() != -1) {
						result = forwardReadContent4Match(subFiles.get(i), vu, findStr, offset);
						if (result != null) {
							return result;
						}
					}
				}
			} else {
				String xpath = "tuv[lower-case(@xml:lang)='" + lang.toLowerCase() + "']/seg";
				for (int i = startf; i >= 0; i--) {
					VTDUtils vu = container.getVTDUtils(subFiles.get(i));
					AutoPilot apTemp = new AutoPilot(vu.getVTDNav());
					apTemp.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
					if (i == startf
							&& vu.pilot(apTemp, "/tmx/body/tu[@hsid='" + strs[1] + "']/tuv[lower-case(@xml:lang)='"
									+ lang.toLowerCase() + "']/seg") != -1) {

					} else {
						if (vu.pilot(apTemp, "/tmx/body/tu[last()]/tuv[lower-case(@xml:lang)='" + lang.toLowerCase() + "']/seg") == -1) {
							continue;
						}
					}
					FindReasult result = forwardReadContent4Match(subFiles.get(i), vu, findStr, offset);
					if (result != null) {
						return result;
					}
					ap.bind(vu.getVTDNav());
					ap.selectXPath(xpath);
					offset = -1;


					vu.getVTDNav().toElement(VTDNav.PARENT);
					vu.getVTDNav().toElement(VTDNav.PARENT);
					// TU
					while(vu.getVTDNav().toElement(VTDNav.PREV_SIBLING)){
						vu.getVTDNav().push();
						ap.resetXPath();
						ap.selectXPath(xpath);
						if(ap.evalXPath() != -1){
							result = forwardReadContent4Match(subFiles.get(i), vu, findStr, offset);
							if (result != null) {
								return result;
							}
						}
						vu.getVTDNav().pop();
					}					
				}
			}
		} catch (VTDException e) {
			LOGGER.error("", e);
		}
		return null;
	}

	/**
	 * @param vu
	 *            The VTDNav token must at TU's SEG node;
	 * @param findStr
	 *            target to find in TU;
	 * @param offset
	 *            the start offset of TU content to begin find
	 * @return The {@link FindReasult}
	 * @throws VTDException
	 *             ;
	 */
	private FindReasult forwardReadContent4Match(String subFile, VTDUtils vu, String findStr, int offset)
			throws VTDException {
		TmxInnerTagParser parser = TmxInnerTagParser.getInstance();
		String text = vu.getElementContent();
		StringBuilder b = new StringBuilder(text);
		parser.parseInnerTag(b);
		text = TextUtil.resetSpecialString(b.toString());
		if (text == null || text.length() < findStr.length()) {
			return null;
		}
		Region r = matchString(offset, text, findStr);
		if (r != null) {
			vu.getVTDNav().push();
			vu.getVTDNav().toElement(VTDNav.PARENT);
			vu.getVTDNav().toElement(VTDNav.PARENT);
			String hsid = vu.getCurrentElementAttribut("hsid", null);
			vu.getVTDNav().pop();
			if (hsid != null && hsid.length() != 0) {
				String tuIdentifier = subFile + TeCoreConstant.ID_MARK + hsid;
				if (tmxDataAccess.getDisplayTuIdentifiers().contains(tuIdentifier)) {
					return new FindReasult(r, subFile + TeCoreConstant.ID_MARK + hsid);
				}
			}
		}
		return null;
	}
}
