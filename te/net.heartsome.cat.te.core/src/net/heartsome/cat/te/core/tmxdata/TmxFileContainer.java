/**
 * TmxFileContainer.java
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.util.LanguageUtils;
import net.heartsome.cat.document.TmxReadException;
import net.heartsome.cat.te.core.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.EncodingException;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * TMX 文件容器，打开TMX文件时，负责封闭文件相关属性，以及解析TMX文件
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxFileContainer extends TmxContainer {
	public static final Logger LOGGER = LoggerFactory.getLogger(TmxFileContainer.class);
	private String filePath;
	private TmxHeader headerNode;
	private int tuTotalNumber = 0;
	private VTDUtils vu;

	private List<Integer> tuIndexCache;

	/**
	 * Constructor
	 * @param filePath
	 *            TMX file path in OS
	 */
	public TmxFileContainer(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Parse current TMX file with VTD
	 * @return <code>VTDUtils<code>
	 * @throws Exception
	 *             A set of VTD parse exception;
	 */
	public void parseFileWithVTD() throws Exception {
		File f = new File(this.filePath);
		if (!f.exists() || f.isDirectory()) {
			throw new FileNotFoundException(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileNotFound"));
		}
		VTDGen vg = parseFile(f);
		vu = new VTDUtils(vg.getNav());
		loadTmxHeader(vu);
		getTUIndexCache(true);
		vg.clear();
	}

	/** @return the filePath */
	public String getFilePath() {
		return filePath;
	}

	/** Compute file size */
	public long getFileSize() {
		File f = new File(filePath);
		return f.length();
	}

	/** @return the headerNode */
	public TmxHeader getHeaderNode() {
		return headerNode;
	}

	/** @return the tuTotalNumber */
	public int getTuTotalNumber() {
		return tuTotalNumber;
	}

	public VTDUtils getVTDUtils() {
		return vu;
	}

	/**
	 * Get all tuv node xml:lang attribute value.
	 * @return ;
	 */
	public List<String> getAllLanguages() {
		List<String> tgtLangs = new ArrayList<String>(2);
		VTDNav vn = vu.getVTDNav();
		vn.push();
		try {
			// vu.pilot("/tmx/body");
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("//tuv");
			while (ap.evalXPath() != -1) {
				int inx = vn.getAttrVal("xml:lang");
				inx = inx == -1 ? vn.getAttrVal("lang") : inx;
				String lang = inx != -1 ? vn.toString(inx) : null;
				lang = LanguageUtils.convertLangCode(lang);
				if (tgtLangs.contains(lang)) {
					continue;
				}
				tgtLangs.add(lang);
			}
		} catch (VTDException e) {
			LOGGER.error("", e);
		} finally {
			vn.pop();
		}
		return tgtLangs;
	}

	/**
	 * Get TU node VTD index cache, can use this data compute TU node position in body node
	 * @param isReload
	 *            is or not reload the cache, for example reparse the TMX file need reload.
	 * @return The TU node VTD index in a ArrayList;
	 */
	public List<Integer> getTUIndexCache(boolean isReload) {
		if (tuIndexCache == null) {
			tuIndexCache = new ArrayList<Integer>();
		}
		if (isReload) {
			tuIndexCache.clear();
		} else {
			return tuIndexCache;
		}
		VTDNav vn = vu.getVTDNav();
		vn.push();
		try {
			vu.pilot("/tmx/body");
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("./tu");
			while (ap.evalXPath() != -1) {
				int idex = vn.getCurrentIndex();
				tuIndexCache.add(idex);
				this.tuTotalNumber++;
			}
		} catch (VTDException e) {
			LOGGER.error("", e);
		} finally {
			vn.pop();
		}
		return tuIndexCache;
	}

	/**
	 * Parse current TMX file
	 * @param file
	 * @return
	 * @throws Exception
	 *             VTD parse file exception;
	 */
	private VTDGen parseFile(File file) throws Exception {
		VTDGen vg = new VTDGen();
		FileInputStream fis = null;
		String message = "";
		try {
			fis = new FileInputStream(file);
			byte[] b = new byte[(int) file.length()];

			int offset = 0;
			int numRead = 0;
			int numOfBytes = 1048576;// I choose this value randomally,
			// any other (not too big) value also can be here.
			if (b.length - offset < numOfBytes) {
				numOfBytes = b.length - offset;
			}
			while (offset < b.length && (numRead = fis.read(b, offset, numOfBytes)) >= 0) {
				offset += numRead;
				if (b.length - offset < numOfBytes) {
					numOfBytes = b.length - offset;
				}
			}
			vg.setDoc(b);
			vg.parse(true);
		} catch (IOException e) {
			LOGGER.error("", e);
			message = Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError");
			throw new Exception(message, e);
		} catch (EncodingException e) {
			LOGGER.error("", e);
			message = Messages.getString("tmxdata.TmxFileContainer.parseTmxFileEncodingError");
			throw new TmxReadException(message, e);
		} catch (ParseException e) {
			LOGGER.error("", e);
			String errMsg = e.getMessage();
			if (errMsg.indexOf("invalid encoding") != -1) { // 编码异常
				message = Messages.getString("tmxdata.TmxFileContainer.parseTmxFileEncodingError");
			} else {
				message = Messages.getString("tmxdata.TmxFileContainer.parseTmxFileContentError");
			}
			throw new TmxReadException(message + e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				}
			}
		}
		return vg;
	}

	/**
	 * Load TMX header node,and pilot to Body XMLElement
	 * @param vu
	 *            <code>VTDUtils<code>
	 * @throws Exception
	 *             Error TMX structure or Error header info
	 */
	private void loadTmxHeader(VTDUtils vu) throws Exception {
		headerNode = new TmxHeader();
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		String rootPath = "/tmx";
		try {
			ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			ap.selectXPath(rootPath);
			if (ap.evalXPath() == -1) {
				throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));
			}
			int t = vu.getVTDNav().getAttrVal("version");
			if (t != -1) {
				String tmxVersion = vu.getVTDNav().toString(t).trim();
				headerNode.setTmxVersion(tmxVersion);
			} else {
				headerNode.setTmxVersion("");
			}
			ap.resetXPath();
			ap.selectXPath("/tmx/header");
			if (ap.evalXPath() == -1) {
				throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));
			}
			int id = vu.getVTDNav().getAttrVal("srclang");
			if (id == -1) {
				throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));
			}
			headerNode.setSrclang(LanguageUtils.convertLangCode(vu.getVTDNav().toString(id).trim()));
			ap.resetXPath();
			ap.selectXPath("@*");
			int inx = -1;
			while ((inx = ap.evalXPath()) != -1) {
				String name = vu.getVTDNav().toString(inx);
				inx = vu.getVTDNav().getAttrVal(name);
				String value = inx != -1 ? vu.getVTDNav().toString(inx) : "";
				if (name.equals("adminlang")) {
					headerNode.setAdminlang(value);
				} else if (name.equals("changedate")) {
					headerNode.setChangedate(value);
				} else if (name.equals("changeid")) {
					headerNode.setChangeid(value);
				} else if (name.equals("creationdate")) {
					headerNode.setCreationdate(value);
				} else if (name.equals("creationid")) {
					headerNode.setCreationid(value);
				} else if (name.equals("creationtool")) {
					headerNode.setCreationtool(value);
				} else if (name.equals("creationtoolversion")) {
					headerNode.setCreationtoolversion(value);
				} else if (name.equals("datatype")) {
					headerNode.setDatatype(value);
				} else if (name.equals("o-encoding")) {
					headerNode.setOencoding(value);
				} else if (name.equals("o-tmf")) {
					headerNode.setOtmf(value);
				} else if (name.equals("segtype")) {
					headerNode.setSegtype(value);
				}
			}

			if (vu.pilot("/tmx/body") == -1) {
				throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));
			}
		} catch (VTDException e) {
			LOGGER.error("", e);
			throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"), e);
		}
	}

	/**
	 * 保存文件，重新解析 VTDNav，刷新 VTDUtils
	 * @param xm
	 *            ;
	 */
	public void save(XMLModifier xm) {
		try {
			this.vu.bind(xm.outputAndReparse());
			
			FileOutputStream fos = new FileOutputStream(this.filePath);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			xm.output(bos); // 写入文件
			bos.close();
			fos.close();
		} catch (TranscodeException e) {
			e.printStackTrace();
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	}
}
