/**
 * TmxFileDataAccess.java
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

package net.heartsome.cat.te.core.tmxdata;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.te.core.bean.ExportBean;
import net.heartsome.cat.te.core.bean.TmxEditorFilterBean;
import net.heartsome.cat.te.core.bean.TmxPropertiesBean;
import net.heartsome.cat.te.core.utils.TmxFileDataAccessUtils;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * TMX文件访问类，用于存取文件中的TU
 * @author Jason
 * @version 1.0
 * @since JDK1.6
 * @deprecated 处理单文件，不适用于大文件，功能不完全
 */
public class TmxFileDataAccess extends AbstractTmxDataAccess {
	public static final Logger LOGGER = LoggerFactory.getLogger(TmxFileDataAccess.class);

	private TmxFileContainer container;

	/**
	 * Constructor
	 * @param container
	 *            <code>TmxFileContainer<code>
	 * @throws Exception
	 *             VTD parse Exception
	 */
	public TmxFileDataAccess(TmxFileContainer container) {
		super(container);
		this.container = container;
	}

	public String retrieveTuXml(int tuIdentifier) {
		return null;
	}

	public void closeTmxDataAccess(IProgressMonitor monitor) {
		this.container = null;
	}

	@Override
	public void save(IProgressMonitor monitor) {
		// Nothing to do
	}
	
	@Override
	public void saveAs(IProgressMonitor monitor, ExportBean exportBean) {
		// Nothing to do
	}
	
	@Override
	public boolean isSourceExist() {
		return false;
	}
	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#loadTmxProperties()
	 */
	public TmxPropertiesBean loadTmxProperties() {
		TmxPropertiesBean bean = new TmxPropertiesBean();
		TmxHeader header = container.getHeaderNode();
		String srcLang = header.getSrclang();
		if (srcLang == null || srcLang.equals("*all*")) {
			bean.setSrcLang(null);
		} else {
			bean.setSrcLang(srcLang);
		}
		bean.setVersion(header.getTmxVersion());
		bean.setCreationDate(DateUtils.formatDateFromUTC(header.getCreationdate()));
		bean.setCreator(header.getCreationid());
		bean.setCreationTooles(header.getCreationtool());
		bean.setTuNumber(container.getTuTotalNumber());
		bean.setLocation(container.getFilePath());
		bean.setFileSize(container.getFileSize() + "");
		List<String> tgtLang = new ArrayList<String>();
		tgtLang.addAll(container.getAllLanguages());
		tgtLang.remove(srcLang);
		bean.setTargetLang(tgtLang);
		return bean;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#loadDisplayTuIdentifierByFilter(net.heartsome.cat.te.core.bean.TmxEditorFilterBean,
	 *      java.lang.String, java.lang.String, boolean)
	 */
	public void loadDisplayTuIdentifierByFilter(IProgressMonitor monitor, TmxEditorFilterBean filterBean, String srcLang, String tgtLang, String srcSearchText, String tgtSearchText) {
		Assert.isLegal(srcLang != null && srcLang.length() > 0 && tgtLang != null && tgtLang.length() > 0);
		super.currSrcLang = srcLang;
		super.currTgtLang = tgtLang;
		super.tuIdentifiers.clear();
		if (filterBean == null) { // 查询所有
			List<Integer> piMap = container.getTUIndexCache(false);
			if (langList.size() == 1) { // 不需要语言过滤,加载全部TU
				for (int i = 1; i <= piMap.size(); i++) {
					super.tuIdentifiers.add(i + "");
				}
				return;
			}

			VTDUtils vu = container.getVTDUtils();
			VTDNav vn = vu.getVTDNav();
			vn.push();
			try {
				vu.pilot("/tmx/body");
				AutoPilot ap = new AutoPilot(vn);
				String tuXpath = "./descendant::tu[tuv[@xml:lang='" + srcLang + "'] and tuv[@xml:lang='" + tgtLang
						+ "']]";
				ap.selectXPath("count(" + tuXpath + ")");
				int tuNumber = (int) ap.evalXPathToNumber();
				if (tuNumber == piMap.size()) {
					for (int i = 1; i <= tuNumber; i++) {
						super.tuIdentifiers.add(i + "");
					}
				} else {
					ap.resetXPath();
					ap.selectXPath(tuXpath);
					while (ap.evalXPath() != -1) {
						int idex = vn.getCurrentIndex();
						super.tuIdentifiers.add((piMap.indexOf(idex) + 1) + "");
					}
				}
			} catch (VTDException e) {
				// TODO: handle exception
				e.printStackTrace();
			} finally {
				vn.pop();
			}
		}

		// TODO 需要实现过滤条件
	}

    @Override
    public String addTu(TmxTU tu, String selTuIdentifer) {
    	return null;
    }

	@Override
	public void deleteTus(String[] tuIdentifiers, IProgressMonitor monitor) {

	}

	/**
	 * 1.将newText写入到Tmx文件或者TmDb <br>
	 * 2.将newText更新到TmxSegement对象中。
	 * @param newText
	 * @param tu
	 * @param tuv
	 **/
	public void updateTuvContent(String identifier, String newText, TmxTU tu, TmxSegement tuv) {
		if (tuv.getFullText().equals(newText)) { // 新内容和原来的内容一样
			return;
		}
		VTDUtils vu = container.getVTDUtils();
		VTDNav vn = vu.getVTDNav();
		vn.push();
		try {
			if (vu.pilot("//tu[" + tu.getTmId() + "]") == -1) {
				return;
			}
			int tuvIdentifier = tuv.getDbPk();
			if (vu.pilot("./tuv[" + tuvIdentifier + "]/seg") != -1) {
				XMLModifier xm = new XMLModifier(vn);
				xm.remove();
				xm.insertAfterElement("<seg>" + newText + "</seg>");
				container.save(xm);
				tuv.setFullTextWithParseTag(newText);
			}
		} catch (VTDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			vn.pop();
		}
	}

	public void updateTuNote(String tuIdnetifier, TmxTU tu, TmxNote note, String newContent) {
		String xpath = "/tmx/body/tu[position()=%s]/note[position()=%s]/text()";
		xpath = String.format(xpath, tu.getTmId(), note.getDbPk());
		VTDUtils util = container.getVTDUtils();
		AutoPilot ap = new AutoPilot();
		ap.bind(util.getVTDNav());
		try {
			util.getVTDNav().push();
			ap.selectXPath(xpath);
			int index;
			if ((index = ap.evalXPath()) != -1) {
				XMLModifier xm = new XMLModifier();
				xm.bind(util.getVTDNav());
				// TODO 转义
				xm.updateToken(index, newContent);
				container.save(xm);
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			util.getVTDNav().pop();
		}
	}

	public void deleteTuNote(String tuIdentifier, TmxTU tu, TmxNote deleteNote) {
		String xpath = "/tmx/body/tu[position()=%s]/note[position()=%s]";
		xpath = String.format(xpath, tuIdentifier, deleteNote.getDbPk());
		VTDUtils util = container.getVTDUtils();
		AutoPilot ap = new AutoPilot();
		ap.bind(util.getVTDNav());
		try {
			util.getVTDNav().push();
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				XMLModifier xm = new XMLModifier();
				xm.bind(util.getVTDNav());
				xm.remove();
				container.save(xm);
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} finally {
			util.getVTDNav().pop();
		}
	}

	public int addTuNote(String tuIdentifier, TmxTU tu, String content) {
		int notePosition = 1;
		StringBuffer buf = new StringBuffer();
		VTDUtils util = container.getVTDUtils();
		VTDNav vn = util.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			vn.push();
			buf.append("/tmx/body/tu[position()=").append(tu.getTmId()).append("]");
			ap.selectXPath(buf.toString());
			if (ap.evalXPath() != -1) {
				buf.setLength(0);
				// TODO 转义
				String noteStr = buf.append("<note>").append(content).append("</note>").toString();
				XMLModifier xm = new XMLModifier(vn);
				if (vn.toElement(VTDNav.FIRST_CHILD, "note")) {
					do {
						notePosition++;
					} while (vn.toElement(VTDNav.NS, "note"));
					xm.insertAfterElement(noteStr);
				} else if (vn.toElement(VTDNav.FC, "tuv")) {
					xm.insertBeforeElement(noteStr);
				}
				container.save(xm);
			} else {
				notePosition = -1;
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} finally {
			util.getVTDNav().pop();
		}
		return notePosition;
	}

	public int addTuProp(String tuIdentifier, TmxTU tu, String propType, String newContent) {
		int propPosition = 1;
		StringBuffer buf = new StringBuffer();
		VTDUtils util = container.getVTDUtils();
		VTDNav vn = util.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			vn.push();
			buf.append("/tmx/body/tu[position()=").append(tu.getTmId()).append("]");
			ap.selectXPath(buf.toString());
			if (ap.evalXPath() != -1) {
				String propStr = "<prop type=\"%s\">%s</prop>";
				// TODO 转义
				propStr = String.format(propStr, propType, newContent);
				XMLModifier xm = new XMLModifier(vn);
				if (vn.toElement(VTDNav.FIRST_CHILD, "prop")) {
					do {
						propPosition++;
					} while (vn.toElement(VTDNav.NS, "prop"));
					xm.insertAfterElement(propStr);
				} else if (vn.toElement(VTDNav.FC, "tuv")) {
					xm.insertBeforeElement(propStr);
				}
				container.save(xm);
			} else {
				propPosition = -1;
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} finally {
			util.getVTDNav().pop();
		}
		return propPosition;
	}

	public void addTuAttribute(String tuIdentifier, TmxTU tu, String name, String value) {

		StringBuffer buf = new StringBuffer();
		VTDUtils util = container.getVTDUtils();
		VTDNav vn = util.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			vn.push();
			buf.append("/tmx/body/tu[position()=").append(tu.getTmId()).append("]");
			ap.selectXPath(buf.toString());
			if (ap.evalXPath() != -1) {
				XMLModifier xm = new XMLModifier(vn);
				buf.setLength(0);
				// TODO 转义
				buf.append(" ").append(name).append("=\"").append(value).append("\"").append(" ");
				xm.insertAttribute(buf.toString());
				container.save(xm);
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} finally {
			vn.pop();
		}
	}

	public void addTuvAttribute(String tuIdentifier, TmxSegement tuv, String name, String value) {
		StringBuffer buf = new StringBuffer();
		VTDUtils util = container.getVTDUtils();
		VTDNav vn = util.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			vn.push();
			buf.append("/tmx/body/tu[position()=").append(tuIdentifier).append("]/tuv[position()=")
					.append(tuv.getDbPk()).append("]");
			ap.selectXPath(buf.toString());
			if (ap.evalXPath() != -1) {
				XMLModifier xm = new XMLModifier(vn);
				buf.setLength(0);
				// TODO 转义
				buf.append(" ").append(name).append("=\"").append(value).append("\"").append(" ");
				xm.insertAttribute(buf.toString());
				container.save(xm);
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} finally {
			vn.pop();
		}
	}

	public void deleteTuAttribute(String tuIdentifier, TmxTU tu, String name) {
		StringBuffer buf = new StringBuffer();
		VTDUtils util = container.getVTDUtils();
		VTDNav vn = util.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			vn.push();
			buf.append("/tmx/body/tu[position()=").append(tuIdentifier).append("]/@").append(name);
			ap.selectXPath(buf.toString());
			int index = 0;
			if ((index = ap.evalXPath()) != -1) {
				XMLModifier xm = new XMLModifier(vn);
				xm.removeAttribute(index);
				container.save(xm);
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} finally {
			vn.pop();
		}
	}

	public void updateTuvAttribute(String tuIdentifier, TmxSegement tuv, String name, String newvalue) {
		StringBuffer buf = new StringBuffer();
		VTDUtils util = container.getVTDUtils();
		VTDNav vn = util.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			vn.push();
			buf.append("/tmx/body/tu[position()=").append(tuIdentifier).append("]/tuv[position()=")
					.append(tuv.getDbPk()).append("]/@").append(name);
			ap.selectXPath(buf.toString());
			int index = 0;
			if ((index = ap.evalXPath()) != -1) {
				XMLModifier xm = new XMLModifier(vn);
				// TODO 编码问题，转义问题
				xm.updateToken(index + 1, newvalue.getBytes());
				container.save(xm);
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			vn.pop();
		}
	}

	public void updateTuProp(String tuIdentifier, TmxTU tu, TmxProp prop, String propType, String newContent) {
		StringBuffer buf = new StringBuffer();
		VTDUtils util = container.getVTDUtils();
		VTDNav vn = util.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			buf.append("/tmx/body/tu[position()=").append(tu.getTmId()).append("]/prop[position()=")
					.append(prop.getDbPk()).append("]");
			ap.selectXPath(buf.toString());
			if (ap.evalXPath() != -1) {
				int typeIndex = vn.getAttrVal("type");
				ap.selectXPath("./text()");
				int textIndex = ap.evalXPath();
				XMLModifier xm = new XMLModifier(vn);
				// TODO 编码问题，转义问题
				if (typeIndex != -1) {
					xm.updateToken(typeIndex, propType);
				}
				if (textIndex != -1) {
					xm.updateToken(textIndex, newContent);
				}
				container.save(xm);
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void deleteTuProp(String tuIdentifier, TmxTU tu, TmxProp deleteProp) {
		StringBuffer buf = new StringBuffer();
		VTDUtils util = container.getVTDUtils();
		VTDNav vn = util.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			buf.append("/tmx/body/tu[position()=").append(tu.getTmId()).append("]/prop[position()=")
					.append(deleteProp.getDbPk()).append("]");
			ap.selectXPath(buf.toString());
			if (ap.evalXPath() != -1) {
				XMLModifier xm = new XMLModifier(vn);
				xm.remove();
				container.save(xm);
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		}
	}

	public void deleteTuvAttribute(String tuIdentifier, TmxSegement tuv, String name) {
		StringBuffer buf = new StringBuffer();
		VTDUtils util = container.getVTDUtils();
		VTDNav vn = util.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			buf.append("/tmx/body/tu[position()=").append(tuIdentifier).append("]/tuv[position()=")
					.append(tuv.getDbPk()).append("]/@").append(name);
			ap.selectXPath(buf.toString());
			int index = 0;
			if ((index = ap.evalXPath()) != -1) {
				XMLModifier xm = new XMLModifier(vn);
				xm.removeAttribute(index);
				container.save(xm);
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		}
	}

	public void updateTuAttribute(String tuIdentifier, TmxTU tu, String name, String newValue) {
		StringBuffer buf = new StringBuffer();
		VTDUtils util = container.getVTDUtils();
		VTDNav vn = util.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			buf.append("/tmx/body/tu[position()=").append(tu.getTmId()).append("]/@").append(name);
			ap.selectXPath(buf.toString());
			int index = 0;
			if ((index = ap.evalXPath()) != -1) {
				XMLModifier xm = new XMLModifier(vn);
				xm.updateToken(index + 1, newValue.getBytes());
				container.save(xm);
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return 包含在当显示的记录中
	 * @param tgtLangCode
	 **/
	public List<Integer> getTgtEmptyTU(String tgtLangCode) {
		return null;
	}

	/**
	 * @return 包含在当显示的记录中
	 * @param srcLang
	 * @param tgtLang
	 * @param boolean isIgnoreTag
	 **/
	public List<Integer> getDuplicatedTU(String srcLang, String tgtLang, boolean isIgnoreTag) {
		return null;
	}

	/**
	 * @return 包含在当显示的记录中
	 * @param srcLang
	 * @param tgtLang
	 * @param boolean isIgnoreTag
	 **/
	public List<Integer> getDuplicatedSrcDiffTgtTU(String srcLang, String tgtLang, boolean isIgnoreTag) {
		return null;
	}

	/**
	 * 首选在缓存中取值，如果缓存中没有，则从物理存储中读取值，最后添加缓存中。
	 * @param tuIdentifier
	 **/
	static final String xpath = "/tmx/body/tu[__id__]";

	public TmxTU getTuByIdentifier(String tuIdentifier) {
		VTDUtils vu = container.getVTDUtils();
		VTDNav vn = vu.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		TmxTU tu = new TmxTU();
		try {
			String xp = xpath.replace("__id__", tuIdentifier + "");
			ap.selectXPath(xp);
			if (ap.evalXPath() != -1) {
				TmxFileDataAccessUtils.readTUAttr4VTDNav(vu, tu);
				TmxFileDataAccessUtils.readTUNote4VTDNav(vu, tu);
				TmxFileDataAccessUtils.readTUProp4VTDNav(vu, tu);
				TmxFileDataAccessUtils.readTUTuv4VTDNav(vu, tu, super.currSrcLang, super.currTgtLang);
			}
		} catch (VTDException e1) {
			e1.printStackTrace();
		}
		tu.setTmId(Integer.parseInt(tuIdentifier));
		return tu;
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#deleteTgtEmpty(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean deleteTgtEmpty(IProgressMonitor monitor, boolean ignoreTag) {
		// TODO Auto-generated method stub
		return false;
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#deleteDupaicate(org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	@Override
	public boolean deleteDupaicate(IProgressMonitor monitor, boolean ignoreTag,boolean ignoreCase) {
		// TODO Auto-generated method stub
		return false;
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#deleteSameSrcDiffTgt(org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	@Override
	public boolean deleteSameSrcDiffTgt(IProgressMonitor monitor, boolean ignoreTag,boolean ignoreCase) {
		// TODO Auto-generated method stub
		return false;
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#deleteEndsSpaces(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean deleteEndsSpaces(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return false;
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchUpdateTuAttr(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void batchUpdateTuAttr(IProgressMonitor monitor, String name, String value, String filter) {
		// TODO Auto-generated method stub
		
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchDeleteTuAttr(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	@Override
	public void batchDeleteTuAttr(IProgressMonitor monitor, String name, String filter) {
		// TODO Auto-generated method stub
		
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchAddTmxProp(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void batchAddTmxProp(IProgressMonitor monitor, String type, String content, String filter) {
		// TODO Auto-generated method stub
		
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchUpdateTmxProp(org.eclipse.core.runtime.IProgressMonitor, net.heartsome.cat.common.bean.TmxProp, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void batchUpdateTmxProp(IProgressMonitor monitor, TmxProp prop, String propType, String content,
			String filter) {
		// TODO Auto-generated method stub
		
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchDeleteTmxProp(org.eclipse.core.runtime.IProgressMonitor, net.heartsome.cat.common.bean.TmxProp, java.lang.String)
	 */
	@Override
	public void batchDeleteTmxProp(IProgressMonitor monitor, TmxProp prop, String filter) {
		// TODO Auto-generated method stub
		
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchUpdateTuvAttr(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.util.List, java.lang.String)
	 */
	@Override
	public void batchUpdateTuvAttr(IProgressMonitor monitor, String name, String value, List<String> langs,
			String filter) {
		// TODO Auto-generated method stub
		
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchDeleteTuvAttr(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.util.List, java.lang.String)
	 */
	@Override
	public void batchDeleteTuvAttr(IProgressMonitor monitor, String name, List<String> langs, String filter) {
		// TODO Auto-generated method stub
		
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchAddTmxNote(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	@Override
	public void batchAddTmxNote(IProgressMonitor monitor, String content, String filter) {
		// TODO Auto-generated method stub
		
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchUpdateTmxNote(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void batchUpdateTmxNote(IProgressMonitor monitor, String oldContent, String newContent, String filter) {
		// TODO Auto-generated method stub
		
	}

	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchDeleteTmxNote(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	@Override
	public void batchDeleteTmxNote(IProgressMonitor monitor, String content, String filter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beginQA(String srcLangCode, String tgtLangCode, boolean ignoreTag, boolean ignoreCase) {
		// do nothing
	}
	
	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}
}
