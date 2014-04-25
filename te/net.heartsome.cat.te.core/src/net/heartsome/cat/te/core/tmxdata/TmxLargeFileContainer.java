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

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.cat.common.util.LanguageUtils;
import net.heartsome.cat.te.core.file.merge.MergeFilesWhenCloseTmx;
import net.heartsome.cat.te.core.file.split.SplitFileWhenOpenTmx;
import net.heartsome.cat.te.core.resource.Messages;
import net.heartsome.cat.te.core.utils.TeCoreUtils;
import net.heartsome.xml.vtdimpl.EmptyFileException;
import net.heartsome.xml.vtdimpl.VTDLoader;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.EntityException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * TMX 文件容器，打开TMX文件时，负责封闭文件相关属性，以及解析TMX文件
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxLargeFileContainer extends TmxContainer {
	public static final Logger LOGGER = LoggerFactory.getLogger(TmxLargeFileContainer.class);
	private String mainfilePath;
	private TmxHeader header;
	private int tuTotalNumber = 0;

	// 将大文件拆分成小文件，然后一起将这些文件打开
	private List<String> subFiles;
	private Map<String, VTDUtils> vus;
	private List<String> allLangList;
	private String mainFileEncoding;
	private String tmxVersion;
	private String doctypeContent;

	/**
	 * Constructor
	 * @param filePath
	 *            TMX file path in OS
	 */
	public TmxLargeFileContainer() {
		vus = new HashMap<String, VTDUtils>();
	}

	/**
	 * 打开指定文件
	 * @param mainFilePath
	 * @throws Exception
	 *             ;
	 */
	public boolean openFile(String mainFilePath, IProgressMonitor monitor) throws Exception {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 50);
		this.mainfilePath = mainFilePath;
		File mainFile = new File(mainFilePath);
		if (!mainFile.exists()) {
			throw new FileNotFoundException(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileNotFound"));
		}
		monitor.worked(15);
		mainFileEncoding = FileEncodingDetector.detectFileEncoding(mainFile);
		VTDGen vg = null;
		try {
			vg = VTDLoader.loadVTDGen(mainFile, mainFileEncoding);
		} catch (EmptyFileException e) {
			throw new Exception(Messages.getString("tmxdata.TmxFileContainer.emptyFileMsg"));
		} catch (EntityException e) {
			String msg = e.getMessage();
			if (msg != null && msg.indexOf("Line Number:") != -1) {
				int s = msg.indexOf("Line Number: ") + "Line Number: ".length();
				int end = msg.indexOf("Offset: ");
				String line = msg.substring(s, end).trim();
				String col = msg.substring(end + "Offset: ".length(), msg.length()).trim();
				String mg = Messages.getString("tmxdata.TmxFileContainer.entityErrorLineColmsg");
				mg = MessageFormat.format(mg, line, col);
				throw new Exception(mg);
			}
		} catch (ParseException e) {
			String msg = e.getMessage();
			if (msg != null && msg.indexOf("Invalid char after") != -1 && msg.indexOf("Line Number:") != -1) {
				int s = msg.indexOf("Line Number: ") + "Line Number: ".length();
				int end = msg.indexOf("Offset: ");
				String line = msg.substring(s, end).trim();
				String col = msg.substring(end + "Offset: ".length(), msg.length()).trim();
				String mg = Messages.getString("tmxdata.TmxFileContainer.parseLineColmsg");
				mg = MessageFormat.format(mg, line, col);
				throw new Exception(mg);
			} else {
				throw e;
			}
		}

		VTDNav vn = vg.getNav();
		vg.clear();
		loadTmxHeader(vn);
		initTmxVersion(vn);
		boolean r = beforeOpenFile(vn, new SubProgressMonitor(monitor, 20));
		if (!r) {
			return r;
		}
		vn = null;

		openSubFiles(new SubProgressMonitor(monitor, 10));
		loadAllLang(new SubProgressMonitor(monitor, 5));
		if (allLangList.size() < 2) {
			TeCoreUtils.deleteFolder(new File(subFiles.get(0)).getParentFile());
			throw new Exception(Messages.getString("tmxdata.TmxFileContainer.oneLanguageMsg"));
		}
		monitor.done();
		return true;
	}

	/**
	 * 打开指定的主文件和对应的子文件
	 * @param mainFilePath
	 *            主文件完整路径
	 * @param subFiles
	 *            子文件完整路径
	 * @throws Exception
	 *             ;
	 */
	public void openFile(String mainFilePath, List<String> subFiles, IProgressMonitor monitor) throws Exception {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		this.mainfilePath = mainFilePath;
		File file = new File(mainFilePath);
		mainFileEncoding = FileEncodingDetector.detectFileEncoding(file);
		VTDGen vg = VTDLoader.loadVTDGen(file, mainFileEncoding);
		VTDNav vn = vg.getNav();
		initTmxVersion(vn);
		this.subFiles = subFiles;
		monitor.beginTask("", 10);
		openSubFiles(new SubProgressMonitor(monitor, 6));
		loadAllLang(new SubProgressMonitor(monitor, 2));
		monitor.worked(2);
		VTDUtils vu = vus.get(subFiles.get(0));
		loadTmxHeader(vu.getVTDNav());
		monitor.done();
	}

	/**
	 * 关闭当前文件容器，关闭后子文件将全部删除。 ;
	 */
	public void close() {
		if (subFiles.size() == 0) {
			return;
		}

		String subFile = subFiles.get(0);
		File f = new File(subFile);
		if (f.exists()) {
			File p = f.getParentFile();
			TeCoreUtils.deleteFolder(p);
		}
		vus.clear();
		subFiles.clear();
	}

	/**
	 * 保存当前修改，实际上是将子文件内容合并到主文件中
	 * @return true 成功保存，false 失败;
	 * @throws Exception
	 */
	public boolean save(IProgressMonitor monitor) throws Exception {
		MergeFilesWhenCloseTmx m = new MergeFilesWhenCloseTmx();
		if (mainFileEncoding == null || mainFileEncoding.length() == 0) {
			mainFileEncoding = "utf-8";
		}
		boolean r = false;
		try {
			r = m.mergeTempFile(vus, subFiles, mainfilePath, mainFileEncoding, tmxVersion, doctypeContent, monitor);
		} catch (FileNotFoundException e) {
			if (!new File(mainfilePath).canWrite()) {
				// 不可写。
				throw new Exception(Messages.getString("tmxdata.TmxFileContainer.cantsaveReadOnly"));
			} else {
				throw e;
			}
		}
		return r;
	}

	/** @return the mainfilePath */
	public String getMainFilePath() {
		return mainfilePath;
	}

	/** Compute file size */
	public long getFileSize() {
		File f = new File(mainfilePath);
		return f.length();
	}

	/** @return the tuTotalNumber */
	public int getTuTotalNumber(boolean isReload) {
		if (isReload) {
			tuTotalNumber = 0;
			for (String file : subFiles) {
				VTDUtils vu = vus.get(file);
				tuTotalNumber += countFileTuNumber(vu);
			}
		}
		return tuTotalNumber;
	}

	/** @return the headerNode */
	public TmxHeader getTmxHeader() {
		return header;
	}

	/**
	 * 获取子文件
	 * @return ;
	 */
	public List<String> getSubFiles() {
		return subFiles;
	}

	/**
	 * 得到指定子文件的 VTDUtils 对象
	 * @param subFile
	 *            子文件完整路径
	 * @return VTDUtils;
	 */
	public VTDUtils getVTDUtils(String subFile) {
		return vus.get(subFile);
	}

	/**
	 * Get all tuv node xml:lang attribute value.
	 * @return ;
	 */
	public List<String> getAllLanguages(boolean isReload) {
		if (isReload) {
			loadAllLang(null);
		}
		return this.allLangList;
	}
	
	public String getDoctypeContent() {
		return doctypeContent;
	}

	public String getTmxVersion() {
		return tmxVersion;
	}
	
	private void openSubFiles(IProgressMonitor monitor) throws Exception {
		if (subFiles == null || subFiles.size() == 0) {
			throw new FileNotFoundException(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileNotFound"));
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", subFiles.size());
		VTDGen vg = new VTDGen();
		for (String file : subFiles) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			File f = new File(file);
			if (!f.exists() || f.isDirectory()) {
				throw new FileNotFoundException(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileNotFound"));
			}
			parseFile(vg, f, new SubProgressMonitor(monitor, 1));
			VTDUtils vu = new VTDUtils(vg.getNav());
			vus.put(file, vu);
			tuTotalNumber += countFileTuNumber(vu);
			vg.clear();
		}
		monitor.done();
	}

	/**
	 * Load TMX header node,and pilot to Body XMLElement
	 * @param vu
	 *            <code>VTDUtils<code>
	 * @return Head node fragment;
	 * @throws Exception
	 *             Error TMX structure or Error header info
	 */
	private void loadTmxHeader(VTDNav vn) throws Exception {
		header = new TmxHeader();
		AutoPilot ap = new AutoPilot(vn);
		String rootPath = "/tmx";
		try {
			ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			ap.selectXPath(rootPath);
			if (ap.evalXPath() == -1) {
				throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));
			}
			int t = vn.getAttrVal("version");
			if (t != -1) {
				String tmxVersion = vn.toString(t).trim();
				header.setTmxVersion(tmxVersion);
			} else {
				header.setTmxVersion("");
			}
			ap.resetXPath();
			ap.selectXPath("/tmx/header");
			if (ap.evalXPath() == -1) {
				throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));
			}

			int id = vn.getAttrVal("srclang");
			if (id == -1) {
				throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));
			}
			header.setSrclang(LanguageUtils.convertLangCode(vn.toString(id).trim()));
			ap.resetXPath();
			ap.selectXPath("@*");
			int inx = -1;
			while ((inx = ap.evalXPath()) != -1) {
				String name = vn.toString(inx);
				inx = vn.getAttrVal(name);
				String value = inx != -1 ? vn.toString(inx) : "";
				if (name.equals("adminlang")) {
					header.setAdminlang(value);
				} else if (name.equals("changedate")) {
					header.setChangedate(value);
				} else if (name.equals("changeid")) {
					header.setChangeid(value);
				} else if (name.equals("creationdate")) {
					header.setCreationdate(value);
				} else if (name.equals("creationid")) {
					header.setCreationid(value);
				} else if (name.equals("creationtool")) {
					header.setCreationtool(value);
				} else if (name.equals("creationtoolversion")) {
					header.setCreationtoolversion(value);
				} else if (name.equals("datatype")) {
					header.setDatatype(value);
				} else if (name.equals("o-encoding")) {
					header.setOencoding(value);
				} else if (name.equals("o-tmf")) {
					header.setOtmf(value);
				} else if (name.equals("segtype")) {
					header.setSegtype(value);
				}
			}

			ap.selectXPath("/tmx/body");
			if (ap.evalXPath() == -1) {
				throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));
			}
			ap.selectXPath("/tmx/body/tu");
			if (ap.evalXPath() == -1) {
				throw new Exception(Messages.getString("tmxdata.TmxFileContainer.emptyBodyNodeMsg"));
			}
		} catch (VTDException e) {
			LOGGER.error("", e);
			throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"), e);
		}
	}

	private void loadAllLang(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (this.allLangList == null) {
			this.allLangList = new ArrayList<String>(2);
		}
		allLangList.clear();
		monitor.beginTask("", subFiles.size());
		for (String subFile : subFiles) {
			monitor.worked(1);
			VTDUtils vu = vus.get(subFile);
			if (vu == null) {
				continue;
			}
			VTDNav vn = vu.getVTDNav();
			vn.push();
			try {
				AutoPilot ap = new AutoPilot(vn);
				ap.selectXPath("//tuv");
				while (ap.evalXPath() != -1) {
					int inx = vn.getAttrVal("xml:lang");
					inx = inx == -1 ? vn.getAttrVal("lang") : inx;
					String lang = inx != -1 ? vn.toString(inx) : null;
					lang = LanguageUtils.convertLangCode(lang);
					if (allLangList.contains(lang)) {
						continue;
					}
					allLangList.add(lang);
				}
			} catch (VTDException e) {
				LOGGER.error("", e);
			} finally {
				vn.pop();
			}
		}
		monitor.done();
	}

	private boolean beforeOpenFile(VTDNav vn, IProgressMonitor monitor) throws Exception {
		SplitFileWhenOpenTmx s = new SplitFileWhenOpenTmx();
		int tuNum = countFileTuNumber(new VTDUtils(vn));
		subFiles = s.splitFile(monitor, mainfilePath, vn, tuNum);
		return true;
	}

	private void initTmxVersion(VTDNav vn) throws NavException {
		vn.toElement(VTDNav.ROOT);
		int rootIdx = vn.getCurrentIndex();
		if (vn.toString(rootIdx).equalsIgnoreCase("tmx")) {
			int t = vn.getAttrVal("version");
			if (t != -1) {
				tmxVersion = vn.toString(t);
			}
		}
		for (int i = 0; i < rootIdx; i++) {
			int ty = vn.getTokenType(i);
			if (ty == VTDNav.TOKEN_DTD_VAL) {
				doctypeContent = vn.toString(i);
			}
		}
	}

	/**
	 * for test do not call on client
	 * @param subFiles
	 *            ;
	 * @deprecated
	 */
	public void setSubFiles(List<String> subFiles) {
		this.subFiles = subFiles;
	}
}
