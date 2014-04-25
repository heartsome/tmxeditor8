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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.common.util.LanguageUtils;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.bean.ExportBean;
import net.heartsome.cat.te.core.bean.TmxEditorFilterBean;
import net.heartsome.cat.te.core.bean.TmxPropertiesBean;
import net.heartsome.cat.te.core.converter.tbx.AppendTmxWriter;
import net.heartsome.cat.te.core.qa.QATrigger;
import net.heartsome.cat.te.core.resource.Messages;
import net.heartsome.cat.te.core.utils.TeCoreUtils;
import net.heartsome.cat.te.core.utils.TmxFileDataAccessUtils;
import net.heartsome.cat.te.core.utils.TmxFilterQueryUtil;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Display;
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
 * TMX 文件访问类，用于存取文件中的TU。<br>
 * TMX 文件在解析时会拆分成一定数量的子文件，然后使用本类统一管理所有子文件。<br>
 * {@link AbstractTmxDataAccess#tuIdentifiers} 在当前实现中存储的是一个能在一组子文件中唯一标识一个 TU 的信息，组成：文件名+分割符+TU Position
 * @author Jason
 * @version 1.0
 * @since JDK1.6
 */
public class TmxLargeFileDataAccess extends AbstractTmxDataAccess {

	public static final Logger LOGGER = LoggerFactory.getLogger(TmxLargeFileDataAccess.class);

	private TmxLargeFileContainer container;

	/**
	 * Constructor
	 * @param container
	 *            <code>TmxFileContainer<code>
	 * @throws Exception
	 *             VTD parse Exception
	 */
	public TmxLargeFileDataAccess(TmxLargeFileContainer container) {
		super(container);
		this.container = container;
		TmxHeader header = container.getTmxHeader();
		String srcLang = header.getSrclang();
		if (srcLang == null || srcLang.equals("*all*")) {
			srcLang = null;
		}
		List<String> allLangs = container.getAllLanguages(false);
		super.langList.addAll(allLangs);
		super.currSrcLang = srcLang == null ? langList.get(0) : srcLang;
		langList.remove(super.currSrcLang);
		super.currTgtLang = langList.get(0);
	}

	public String retrieveTuXml(int tuIdentifier) {
		return null;
	}

	public void closeTmxDataAccess(IProgressMonitor monitor) throws Exception {
		if (this.container != null) {
			this.container.close();
		}
		this.container = null;
		// this.tuIdentifiers.clear();
	}

	@Override
	public void save(IProgressMonitor monitor) throws Exception {
		container.save(monitor);
	}

	@Override
	public boolean isSourceExist() {
		String mainFile = container.getMainFilePath();
		File f = new File(mainFile);
		return !f.exists();
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#saveAs(org.eclipse.core.runtime.IProgressMonitor,
	 *      net.heartsome.cat.te.core.bean.ExportBean)
	 */
	@Override
	public void saveAs(IProgressMonitor monitor, ExportBean exportBean) {
		int scope = exportBean.getExportScope();
		Map<String, List<String>> ids = null;
		switch (scope) {
		case 1: // all filter TU
			ids = parseIdentiterIds(tuIdentifiers);
			break;
		case 2: // all selected TU
			ids = parseIdentiterIds(exportBean.getSelectIds());
			break;
		case 3: // all TU
			ids = null;
			break;
		}
		if (!exportBean.isAppend()) {
			String newFilePath = exportBean.getTargetFile();
			File newFile = new File(newFilePath);
			try {
				FileOutputStream os = new FileOutputStream(newFile);
				try {
					String encoding = "UTF-8";
					writeString(os, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", encoding);
					if (container.getDoctypeContent() != null && container.getDoctypeContent().length() != 0) {
						writeString(os, "<!DOCTYPE" + container.getDoctypeContent() + ">\n", encoding);
					}
					if (container.getTmxVersion() != null && container.getTmxVersion().length() != 0) {
						writeString(os, "<tmx version=\"" + container.getTmxVersion() + "\">\n", encoding);
					} else {
						writeString(os, "<tmx>\n", encoding);
					}
					List<String> subFiles = container.getSubFiles();
					VTDUtils hvu = container.getVTDUtils(subFiles.get(0));
					AutoPilot hap = new AutoPilot();
					hap.bind(hvu.getVTDNav());
					hap.selectXPath("/tmx/header");
					if (hap.evalXPath() != -1) {
						writeString(os, hvu.getElementFragment() + "\n", encoding);
					}
					writeString(os, "<body>\n", encoding);
					if (ids == null) {
						monitor.beginTask("", subFiles.size() + 10);
					} else {
						monitor.beginTask("", ids.size() + 10);
					}
					monitor.worked(10);
					for (String subFile : subFiles) {
						List<String> list = null;
						if (ids != null) {
							list = ids.get(subFile);
							if (null == list || list.isEmpty()) {
								continue;
							}
						} else {
							monitor.worked(1);
							if (monitor.isCanceled()) {
								throw new OperationCanceledException();
							}
						}
						VTDUtils vu = container.getVTDUtils(subFile);
						VTDNav vn = vu.getVTDNav();
						AutoPilot ap = new AutoPilot(vn);
						ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
						ap.selectXPath("/tmx/body/tu[tuv[lower-case(@xml:lang)='" + super.currSrcLang.toLowerCase()
								+ "'] and tuv[lower-case(@xml:lang)='" + super.currTgtLang.toLowerCase() + "']]");

						AutoPilot tuvAp = new AutoPilot(vn);
						tuvAp.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
						tuvAp.selectXPath("./tuv[lower-case(@xml:lang)!='" + super.currSrcLang.toLowerCase()
								+ "' and lower-case(@xml:lang)!='" + super.currTgtLang.toLowerCase() + "']");

						while (ap.evalXPath() != -1) {
							int hsIdIdex = vn.getAttrVal("hsid");
							if (hsIdIdex == -1) {
								continue;
							}
							if (ids != null) {
								monitor.worked(1);
								if (monitor.isCanceled()) {
									throw new OperationCanceledException();
								}
								if (!list.contains(vn.toString(hsIdIdex))) {
									continue;
								}
							}
							StringBuilder sb = new StringBuilder(vu.getElementFragment()
									.replaceFirst("\\s{1}hsid='\\d+'\\s+", " ").replaceFirst("\\s+>", ">"));
							vn.push();
							tuvAp.resetXPath();
							while (tuvAp.evalXPath() != -1) {
								String str = vu.getElementFragment();
								int i = sb.indexOf(str);
								sb.replace(i, i + str.length(), "");
								i = sb.indexOf("\n\n"); // clean empty line
								sb.replace(i, i + 2, "\n");
							}
							vn.pop();
							sb.append('\n');
							writeString(os, sb.toString(), encoding);
						}
					}
					writeString(os, "</body>\n", encoding);
					writeString(os, "</tmx>", encoding);
				} catch (final VTDException e) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							OpenMessageUtils.openMessage(IStatus.ERROR, e.getMessage());
						}
					});
					LOGGER.error("", e);
				} finally {
					os.close();
				}
			} catch (final IOException e) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						OpenMessageUtils.openMessage(IStatus.ERROR, e.getMessage());
					}
				});
				LOGGER.error("", e);
			}
		} else {
			AppendTmxWriter tmxAppendWriter;
			try {
				tmxAppendWriter = new AppendTmxWriter(exportBean.getTargetFile());
				String aSrcLang = tmxAppendWriter.getSrcLang();
				if (currSrcLang.equals(aSrcLang)) {
					tmxAppendWriter.startAppend();

					List<String> subFiles = container.getSubFiles();
					if (ids == null) {
						monitor.beginTask("", subFiles.size() + 10);
					} else {
						monitor.beginTask("", ids.size() + 10);
					}
					monitor.worked(10);
					for (String subFile : subFiles) {
						List<String> list = null;
						if (ids != null) {
							list = ids.get(subFile);
							if (null == list || list.isEmpty()) {
								continue;
							}
						} else {
							monitor.worked(1);
							if (monitor.isCanceled()) {
								throw new OperationCanceledException();
							}
						}
						VTDUtils vu = container.getVTDUtils(subFile);
						VTDNav vn = vu.getVTDNav();
						AutoPilot ap = new AutoPilot(vn);
						ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
						ap.selectXPath("/tmx/body/tu[tuv[lower-case(@xml:lang)='" + super.currSrcLang.toLowerCase()
								+ "'] and tuv[lower-case(@xml:lang)='" + super.currTgtLang.toLowerCase() + "']]");

						AutoPilot tuvAp = new AutoPilot(vn);
						tuvAp.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
						tuvAp.selectXPath("./tuv[lower-case(@xml:lang)!='" + super.currSrcLang.toLowerCase()
								+ "' and lower-case(@xml:lang)!='" + super.currTgtLang.toLowerCase() + "']");

						while (ap.evalXPath() != -1) {
							int hsIdIdex = vn.getAttrVal("hsid");
							if (hsIdIdex == -1) {
								continue;
							}
							if (ids != null) {
								monitor.worked(1);
								if (monitor.isCanceled()) {
									throw new OperationCanceledException();
								}
								if (!list.contains(vn.toString(hsIdIdex))) {
									continue;
								}
							}
							StringBuilder sb = new StringBuilder(vu.getElementFragment()
									.replaceFirst("\\s{1}hsid='\\d+'\\s+", " ").replaceFirst("\\s+>", ">"));
							vn.push();
							tuvAp.resetXPath();
							while (tuvAp.evalXPath() != -1) {
								String str = vu.getElementFragment();
								int i = sb.indexOf(str);
								sb.replace(i, i + str.length(), "");
								i = sb.indexOf("\n\n"); // clean empty line
								sb.replace(i, i + 2, "\n");
							}
							vn.pop();
							tmxAppendWriter.writeXmlString("\n" + sb.toString());
						}
					}
					tmxAppendWriter.writeEnd();
					tmxAppendWriter.closeOutStream();
				} else {
					OpenMessageUtils.openMessage(IStatus.ERROR, "");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#loadTmxProperties()
	 */
	public TmxPropertiesBean loadTmxProperties() {
		TmxPropertiesBean bean = new TmxPropertiesBean();
		TmxHeader header = container.getTmxHeader();
		String srcLang = header.getSrclang();
		if (srcLang == null || srcLang.equals("*all*")) {
			srcLang = currSrcLang;
		}
		bean.setSrcLang(srcLang);
		bean.setVersion(header.getTmxVersion());
		bean.setCreationDate(DateUtils.formatDateFromUTC(header.getCreationdate()));
		bean.setCreator(header.getCreationid());
		bean.setCreationTooles(header.getCreationtool());
		bean.setCreationTooleVersion(header.getCreationtoolversion());
		bean.setTuNumber(container.getTuTotalNumber(true));
		bean.setLocation(container.getMainFilePath());
		bean.setFileSize(container.getFileSize() + "");
		List<String> tgtLang = new ArrayList<String>();
		tgtLang.addAll(container.getAllLanguages(true));
		tgtLang.remove(srcLang);
		bean.setTargetLang(tgtLang);
		return bean;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#loadDisplayTuIdentifierByFilter(net.heartsome.cat.te.core.bean.TmxEditorFilterBean,
	 *      java.lang.String, java.lang.String, boolean)
	 */
	public void loadDisplayTuIdentifierByFilter(IProgressMonitor monitor, TmxEditorFilterBean filterBean,
			String srcLang, String tgtLang, String srcSearchText, String tgtSearchText) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		Assert.isLegal(srcLang != null && srcLang.length() > 0 && tgtLang != null && tgtLang.length() > 0);
		super.currSrcLang = srcLang;
		super.currTgtLang = tgtLang;

		monitor.beginTask("", 1);
		monitor.setTaskName(Messages.getString("core.dataAccess.filter.data.taskname"));
		if (filterBean == null || (filterBean != null && TeCoreConstant.FILTERID_allSeg.equals(filterBean.getId()))) { // 查询所有
			filterTu(srcSearchText, tgtSearchText, srcLang, tgtLang, new SubProgressMonitor(monitor, 1));
		} else if (TeCoreConstant.FILTERID_srcSameWIthTgtSeg.equals(filterBean.getId())) {
			// 源文与译文相同
			TmxFilterQueryUtil filterQuery = new TmxFilterQueryUtil(container, srcLang, tgtLang);
			List<String> filterResultList = filterQuery.getSrcSameWithTgtTuIdentifiers(new SubProgressMonitor(monitor,
					1));
			if (filterResultList != null) {
				super.tuIdentifiers = filterResultList;
			}
		} else if (TeCoreConstant.FILTERID_srcSameButTgtSeg.equals(filterBean.getId())) {
			// 相同源文不同译文
			TmxFilterQueryUtil filterQuery = new TmxFilterQueryUtil(container, srcLang, tgtLang);
			List<String> filterResultList = filterQuery
					.getSrcSameButTgtTUIdentifies(new SubProgressMonitor(monitor, 1));
			if (filterResultList != null) {
				super.tuIdentifiers = filterResultList;
			}
		} else if (TeCoreConstant.FILTERID_tgtSameButSrcSeg.equals(filterBean.getId())) {
			// 译文相同，但源文不同
			TmxFilterQueryUtil filterQuery = new TmxFilterQueryUtil(container, srcLang, tgtLang);
			List<String> filterResultList = filterQuery
					.getTgtSameButSrcTUIdentifies(new SubProgressMonitor(monitor, 1));
			if (filterResultList != null) {
				super.tuIdentifiers = filterResultList;
			}
		} else if (TeCoreConstant.FILTERID_duplicateSeg.equals(filterBean.getId())) {
			// 重复的文本段
			TmxFilterQueryUtil filterQuery = new TmxFilterQueryUtil(container, srcLang, tgtLang);
			List<String> filterResultList = filterQuery.getDuplicateSegTUIdentifies(new SubProgressMonitor(monitor, 1));
			if (filterResultList != null) {
				super.tuIdentifiers = filterResultList;
			}
		} else if (TeCoreConstant.FILTERID_withNoteSeg.equals(filterBean.getId())) {
			// 带批注的文本段
			TmxFilterQueryUtil filterQuery = new TmxFilterQueryUtil(container, srcLang, tgtLang);
			List<String> filterResultList = filterQuery.getWithNoteSegTUIdentifies(new SubProgressMonitor(monitor, 1));
			if (filterResultList != null) {
				super.tuIdentifiers = filterResultList;
			}
		} else if (TeCoreConstant.FILTERID_withGarbleSeg.equals(filterBean.getId())) {
			// 带乱码的文本段
			TmxFilterQueryUtil filterQuery = new TmxFilterQueryUtil(container, srcLang, tgtLang);
			List<String> filterResultList = filterQuery
					.getWithGarbleSegTUIdentifies(new SubProgressMonitor(monitor, 1));
			if (filterResultList != null) {
				super.tuIdentifiers = filterResultList;
			}
		} else if (TeCoreConstant.FILTERID_tgtNullSeg.equals(filterBean.getId())) {
			// 译文为空的文本段
			TmxFilterQueryUtil filterQuery = new TmxFilterQueryUtil(container, srcLang, tgtLang);
			List<String> filterResultList = filterQuery.getTgtNullSegTUIdentifies(new SubProgressMonitor(monitor, 1));
			if (filterResultList != null) {
				super.tuIdentifiers = filterResultList;
			}
		} else {
			// 自定义过滤器的实现 // UNDO 自定义过滤器还没有修改关于译文为空，以及字符串相等的判断标准。
			TmxFilterQueryUtil filterQuery = new TmxFilterQueryUtil(container, srcLang, tgtLang);
			List<String> filterResultList = filterQuery.getCustomFilterTuIdentifies(new SubProgressMonitor(monitor, 1),
					filterBean);
			if (filterResultList != null) {
				super.tuIdentifiers = filterResultList;
			}
		}
		monitor.done();
	}

	@Override
	public String addTu(TmxTU tu, String selTuIdentifer) {
		VTDUtils vu = null;
		String hsid = null;
		String subFile = null;
		try {
			if (selTuIdentifer == null) {
				List<String> subFiles = container.getSubFiles();
				if (subFiles.size() == 0) {
					return null;
				}
				subFile = subFiles.get(subFiles.size() - 1);
				vu = container.getVTDUtils(subFile);
				if (vu.pilot("/tmx/body/tu[last()]") == -1) {
					return null;
				}
				hsid = vu.getCurrentElementAttribut("hsid", "1");
				hsid = Integer.parseInt(hsid) + 1 + "";
			} else {
				String[] strs = TeCoreUtils.parseTuIndentifier(selTuIdentifer);
				if (strs == null) {
					return null;
				}
				subFile = strs[0];
				String id = strs[1];
				vu = container.getVTDUtils(subFile);
				hsid = generateNewTuHsId(vu);
				if (vu.pilot("/tmx/body/tu[@hsid='" + id + "']") == -1) {
					return null;
				}
			}
			String newTuId = subFile + TeCoreConstant.ID_MARK + hsid;
			StringBuffer sb = new StringBuffer();
			sb.append("\n<tu hsid='" + hsid + "'");
			if (tu.getCreationUser() != null) {
				sb.append(" creationid=\"" + tu.getCreationUser() + "\"");
				sb.append(" changeid=\"" + tu.getCreationUser() + "\"");
			}
			if (tu.getCreationDate() != null) {
				sb.append(" creationdate=\"" + tu.getCreationDate() + "\"");
				sb.append(" changedate=\"" + tu.getCreationDate() + "\"");
			}

			if (null != tu.getCreationTool()) {
				sb.append(" creationtool=\"" + tu.getCreationTool() + "\"");
			}
			if (null != tu.getCreationToolVersion()) {
				sb.append(" creationtoolversion=\"" + tu.getCreationToolVersion() + "\"");
			}
			sb.append(">\n");
			sb.append("<tuv xml:lang=\"" + tu.getSource().getLangCode() + "\"><seg></seg></tuv>\n");
			sb.append("<tuv xml:lang=\"" + tu.getTarget().getLangCode() + "\"><seg></seg></tuv>\n");
			sb.append("</tu>");
			XMLModifier xm = new XMLModifier(vu.getVTDNav());
			if (selTuIdentifer == null) {
				xm.insertAfterElement(sb.toString());
			} else {
				xm.insertBeforeElement(sb.toString());
			}
			save2SubFile(subFile, vu, xm);
			return newTuId;
		} catch (VTDException e) {
			LOGGER.error("", e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("", e);
		}
		return null;
	}

	@Override
	public void deleteTus(String[] tuIdentifiers, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		List<String> subFiles = container.getSubFiles();
		Map<String, List<String>> ids = parseIdentiterIds(Arrays.asList(tuIdentifiers));
		monitor.beginTask("", tuIdentifiers.length);
		try {
			for (String subFile : subFiles) {
				List<String> list = ids.get(subFile);
				if (null == list || list.isEmpty()) {
					continue;
				}
				VTDUtils vu = container.getVTDUtils(subFile);
				VTDNav vn = vu.getVTDNav();
				AutoPilot ap = new AutoPilot(vn);

				AutoPilot tuvCountAp = new AutoPilot(vn);
				tuvCountAp.selectXPath("count(./tuv)");

				AutoPilot tuvAp = new AutoPilot(vn);
				tuvAp.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
				tuvAp.selectXPath("./tuv[lower-case(@xml:lang)='" + super.currTgtLang.toLowerCase() + "']");
				ap.selectXPath("/tmx/body/tu");

				XMLModifier xm = new XMLModifier(vn);

				while (ap.evalXPath() != -1) {
					int hsIdIdex = vn.getAttrVal("hsid");
					if (hsIdIdex == -1) {
						continue;
					}
					if (list.contains(vn.toString(hsIdIdex))) {
						tuvCountAp.resetXPath();
						int childCount = (int) tuvCountAp.evalXPathToNumber();

						if (childCount >= 3) {// 如果TUV数量大于3删除TUV
							tuvAp.resetXPath();
							vn.push();

							if (tuvAp.evalXPath() != -1) {
								xm.remove(vn.getElementFragment());
							}

							vn.pop();
						} else {// 如果TUV语言对小于3，直接删除TU
							xm.remove(vn.getElementFragment());
						}
						monitor.worked(1);
					}
				}
				save2SubFile(subFile, vu, xm);
			}
		} catch (VTDException e) {
			LOGGER.error("", e);
		}

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
		String[] strs = TeCoreUtils.parseTuIndentifier(identifier);
		if (strs == null) {
			return;
		}
		VTDUtils vu = container.getVTDUtils(strs[0]);
		int tuvIdentifier = tuv.getDbPk();
		XMLModifier xm = new XMLModifier();
		try {
			xm.bind(vu.getVTDNav());
		} catch (ModifyException e) {
			LOGGER.error("", e);
		}
		if (tuvIdentifier != -1) {
			String xpath = "/tmx/body/tu[@hsid='" + strs[1] + "']/tuv[" + tuvIdentifier + "]/seg";
			String changedate = DateUtils.formatToUTC(Calendar.getInstance().getTimeInMillis());
			String changeid = System.getProperty("user.name");
			if (vu.pilot(xpath) != -1) {
				try {
					xm.remove();
					xm.insertAfterElement("<seg>" + newText + "</seg>");
					VTDNav vn = vu.getVTDNav();
					vn.toElement(VTDNav.PARENT); // tuv
					int index = vn.getAttrVal("changedate");
					if (index != -1) {
						xm.updateToken(index, changedate);
						tuv.setChangeDate(changedate);
					}
					index = vn.getAttrVal("changeid");
					if (index != -1) {
						xm.updateToken(index, changeid);
						tuv.setChangeUser(changeid);
					}
					vn.toElement(VTDNav.PARENT); // TU
					int tuChangeIdIdx = vn.getAttrVal("changeid");
					int tuChangeDateIdx = vn.getAttrVal("changedate");
					if (tuChangeDateIdx == -1 && tuChangeIdIdx == -1) {
						xm.insertAttribute(" changeid=\"" + changeid + "\" changedate=\"" + changedate + "\"");
					} else if (tuChangeDateIdx != -1 && tuChangeIdIdx != -1) {
						xm.updateToken(tuChangeIdIdx, changeid);
						xm.updateToken(tuChangeDateIdx, changedate);
					} else {
						if (tuChangeIdIdx == -1) {
							xm.insertAttribute(" changeid=\"" + changeid + "\"");
						} else {
							xm.updateToken(tuChangeIdIdx, changeid);
						}
						if (tuChangeDateIdx == -1) {
							xm.insertAttribute(" changedate=\"" + changedate + "\"");
						} else {
							xm.updateToken(tuChangeDateIdx, changedate);
						}
					}
					save2SubFile(strs[0], vu, xm);// update file and VTDNav
				} catch (VTDException e) {
					LOGGER.error("", e);
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("", e);
				}
				tu.setChangeDate(changedate);
				tu.setChangeUser(changeid);
				tuv.setFullTextWithParseTag(newText);// update TU
			}
		} else {
			// 删除 没有 seg　节点的　tuv (针对　lang == tgtLang) --robert 2013-11-25
			String xpath = "/tmx/body/tu[@hsid='" + strs[1] + "']/tuv[lower-case(@xml:lang)='"
					+ tuv.getLangCode().toLowerCase() + "']";
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			vu.getVTDNav().push();
			try {
				String newTuvContent = "<tuv xml:lang=\"" + tuv.getLangCode() + "\"><seg>" + newText + "</seg></tuv>\n";
				xm.bind(vu.getVTDNav());
				ap.selectXPath(xpath);
				if (ap.evalXPath() != -1) {
					xm.remove();
					xm.insertAfterElement(newTuvContent);
				} else {
					xpath = "/tmx/body/tu[@hsid='" + strs[1] + "']";
					ap.selectXPath(xpath);
					if (ap.evalXPath() != -1) {
						xm.insertBeforeTail(newTuvContent);
					}
				}
			} catch (Exception e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
			vu.getVTDNav().pop();

			// addNode(xm, vu, "/tmx/body/tu[@hsid='" + strs[1] + "']", "<tuv xml:lang=\"" + tuv.getLangCode()
			// + "\"><seg>" + newText + "</seg></tuv>\n");
			save2SubFile(strs[0], vu, xm);// update file and VTDNav
			tuv.setFullTextWithParseTag(newText);// update TU
			List<TmxSegement> segements = tu.getSegments();
			if (segements == null) {
				tuv.setDbPk(2);
			} else {
				int size = segements.size() + 1;
				int pk = size + 1;
				tuv.setDbPk(pk);
			}

		}
	}

	public void updateTuNote(String tuIdentifier, TmxTU tu, TmxNote note, String newContent) {
		if (note.getContent().equals(newContent)) { // 新内容和原来的内容一样
			return;
		}
		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return;
		}
		String subFile = strs[0];
		String id = strs[1];

		VTDUtils vu = container.getVTDUtils(subFile);
		if (vu == null) {
			return;
		}
		newContent = TextUtil.cleanString(newContent);
		String xpath = "/tmx/body/tu[@hsid='" + id + "']/note[" + note.getDbPk() + "]";
		XMLModifier xm = new XMLModifier();
		try {
			xm.bind(vu.getVTDNav());
			if (vu.pilot(xpath) != -1) {
				StringBuffer buf = new StringBuffer().append(vu.getElementHead()).append(newContent).append("</note>");
				xm.remove();
				xm.insertAfterElement(buf.toString());
				save2SubFile(subFile, vu, xm);
				note.setContent(newContent);
				setDirty(true);
			}
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateTuNote(Map<String, TmxTU> tus, TmxNote note, String content) {
		if (tus.size() == 1) {
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				updateTuNote(entry.getKey(), entry.getValue(), note, content);
				return;
			}
		}
		
		if (content != null) {
			content = TextUtil.cleanSpecialString(content);
		}

		// key--file; value--hsid
		Map<String, List<String>> ids = new HashMap<String, List<String>>();
		// key--identifier; value--dbpk
		Map<String, Integer> noteIds = new HashMap<String, Integer>();
		for (Entry<String, TmxTU> entry : tus.entrySet()) {
			TmxTU tu = entry.getValue();
			List<TmxNote> notes = tu.getNotes();
			if (notes == null) {
				continue;
			}
			boolean shouldModify = false;
			for (TmxNote checkNote : notes) {
				if (note.getContent().equals(checkNote.getContent())) {// 是否相等
					noteIds.put(entry.getKey(), checkNote.getDbPk());
					shouldModify = true;
					break;
				}
			}
			if (!shouldModify) {
				continue;
			}

			String[] strs = TeCoreUtils.parseTuIndentifier(entry.getKey());
			if (strs == null || strs.length < 2) {
				continue;
			}
			if (!ids.containsKey(strs[0])) {
				ids.put(strs[0], new LinkedList<String>());
			}
			ids.get(strs[0]).add(strs[1]);
		}

		for (Entry<String, List<String>> entry : ids.entrySet()) {
			// 子文件
			String subFile = entry.getKey();
			List<String> hsids = entry.getValue();

			VTDUtils vu = container.getVTDUtils(subFile);
			VTDNav vn = vu.getVTDNav();
			AutoPilot ap = new AutoPilot(vn);
			AutoPilot tmpAp = new AutoPilot(vn);
			XMLModifier xm = null;
			try {
				xm = new XMLModifier(vu.getVTDNav());
			} catch (ModifyException e) {
				LOGGER.error(e.getMessage(), e);
				continue;
			}

			try {
				ap.selectXPath("/tmx/body/tu");
				while (ap.evalXPath() != -1 && !hsids.isEmpty()) {
					String hsid = vn.toString(vn.getAttrVal("hsid"));
					if (hsid == null || !hsids.contains(hsid)) {
						continue;
					}
					vn.push();
					hsids.remove(hsid);
					String identifier = new StringBuilder().append(subFile).append(TeCoreConstant.ID_MARK).append(hsid)
							.toString();
					int dbpk = noteIds.get(identifier);
					tmpAp.selectXPath(new StringBuilder().append("./note[").append(dbpk).append("]").toString());
					if (tmpAp.evalXPath() != -1) {
						xm.remove();
						List<TmxNote> notes = tus.get(identifier).getNotes();
						if (content != null) {
							xm.insertAfterElement(new StringBuilder().append("<note>").append(content)
									.append("</note>").toString());
							if (notes.size() >= dbpk - 1) {
								notes.get(dbpk - 1).setContent(content);
							}
						} else {
							if (notes.size() >= dbpk - 1) {
								notes.remove(dbpk - 1);
							}
						}
						setDirty(true);
					}
					vn.pop();
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
			save2SubFile(subFile, vu, xm);
		}
	}

	public void deleteTuNote(String tuIdentifier, TmxTU tu, TmxNote deleteNote) {
		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return;
		}
		String subFile = strs[0];
		String id = strs[1];

		VTDUtils vu = container.getVTDUtils(subFile);
		if (vu == null) {
			return;
		}
		int noteId = deleteNote.getDbPk();
		String xpath = "/tmx/body/tu[@hsid='" + id + "']/note[" + noteId + "]";

		XMLModifier xm = vu.delete(xpath);
		if (xm != null) {
			save2SubFile(subFile, vu, xm);
			setDirty(true);
			tu.getNotes().remove(deleteNote);
			for (TmxNote note : tu.getNotes()) {
				if (note.getDbPk() > deleteNote.getDbPk()) {
					note.setDbPk(note.getDbPk() - 1);
				}
			}
		}
	}

	@Override
	public void deleteTuNote(Map<String, TmxTU> tus, TmxNote deleteNote) {
		if (tus.size() == 1) {
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				deleteTuNote(entry.getKey(), entry.getValue(), deleteNote);
				return;
			}
		}
		updateTuNote(tus, deleteNote, null);
	}

	public int addTuNote(String tuIdentifier, TmxTU tu, String content) {
		int id = -1;
		if (content == null || content.length() == 0) {
			return id;
		}

		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return id;
		}
		VTDUtils vu = container.getVTDUtils(strs[0]);
		if (vu == null) {
			return id;
		}

		List<TmxNote> notes = tu.getNotes();
		if (notes == null) {
			notes = new ArrayList<TmxNote>();
		}

		content = TextUtil.cleanString(content);
		String xpath = "//tu[@hsid='" + strs[1] + "']";
		if (vu.pilot(xpath) != -1) {
			try {
				XMLModifier xm = new XMLModifier(vu.getVTDNav());
				xm.insertAfterHead("<note>" + content + "</note>");
				save2SubFile(strs[0], vu, xm);
				id = notes.size() + 1;
				// 完成文件操作后，需要添加 note 到 TU 的位置。
				TmxNote note = new TmxNote();
				note.setContent(content);
				note.setDbPk(id);
				notes.add(note);
				tu.setNotes(notes);
				setDirty(true);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}
		return id;
	}

	public int addTuNote(Map<String, TmxTU> tus, String content) {
		if (tus.size() == 1) {
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				return addTuNote(entry.getKey(), entry.getValue(), content);
			}
		}

		// key--file; value--hsid
		Map<String, List<String>> ids = new HashMap<String, List<String>>();
		for (Entry<String, TmxTU> entry : tus.entrySet()) {
			String[] strs = TeCoreUtils.parseTuIndentifier(entry.getKey());
			if (strs == null || strs.length < 2) {
				continue;
			}
			if (!ids.containsKey(strs[0])) {
				ids.put(strs[0], new LinkedList<String>());
			}
			ids.get(strs[0]).add(strs[1]);
		}

		for (Entry<String, List<String>> entry : ids.entrySet()) {
			// 子文件
			String subFile = entry.getKey();
			List<String> hsids = entry.getValue();

			VTDUtils vu = container.getVTDUtils(subFile);
			VTDNav vn = vu.getVTDNav();
			AutoPilot ap = new AutoPilot(vn);
			XMLModifier xm = null;
			try {
				xm = new XMLModifier(vu.getVTDNav());
			} catch (ModifyException e) {
				LOGGER.error(e.getMessage(), e);
				continue;
			}

			for (String hsid : hsids) {
				// 缓存
				TmxTU tu = tus.get(new StringBuilder().append(subFile).append(TeCoreConstant.ID_MARK).append(hsid)
						.toString());
				List<TmxNote> notes = tu.getNotes();
				if (notes == null) {
					notes = new ArrayList<TmxNote>();
					tu.setNotes(notes);
				}
				try {
					ap.selectXPath("/tmx/body/tu");
					while (ap.evalXPath() != -1) {
						if (hsid.equals(vn.toString(vn.getAttrVal("hsid")))) {
							xm.insertAfterHead("<note>" + content + "</note>");
							TmxNote note = new TmxNote();
							note.setContent(content);
							note.setDbPk(notes.size() + 1);
							notes.add(note);
							setDirty(true);
							break;
						}
					}
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
			save2SubFile(subFile, vu, xm);
		}
		return tus.size();
	}

	public void addTuAttribute(String tuIdentifier, TmxTU tu, String name, String value) {
		if (tuIdentifier == null || tuIdentifier.length() == 0 || value == null || value.length() == 0 || name == null
				|| name.length() == 0) {
			return;
		}
		Map<String, String> attrs = tu.getAttributes();
		if (attrs != null && attrs.containsKey(name)) {
			return;
		}

		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return;
		}
		String subFile = strs[0];
		String id = strs[1];
		value = TextUtil.cleanSpecialString(value);
		String attrStr = " " + name + "=\"" + value + "\"";
		String xpath = "//tu[@hsid='" + id + "']";
		VTDUtils vu = container.getVTDUtils(subFile);
		XMLModifier xm = new XMLModifier();
		try {
			xm.bind(vu.getVTDNav());
		} catch (ModifyException e) {
			LOGGER.error("", e);
		}
		addNodeAttribute(xm, vu, xpath, attrStr);
		save2SubFile(subFile, vu, xm);// update file and VTDNav
		setDirty(true);
		tu.appendAttribute(name, value);// update TU
	}

	public void updateTuAttribute(String tuIdentifier, TmxTU tu, String name, String newValue) {
		if (tuIdentifier == null || tuIdentifier.length() == 0 || newValue == null || newValue.length() == 0
				|| name == null || name.length() == 0) {
			return;
		}
		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return;
		}
		String subFile = strs[0];
		String id = strs[1];
		newValue = TextUtil.cleanSpecialString(newValue);
		String xpath = "/tmx/body/tu[@hsid='" + id + "']";
		VTDUtils vu = container.getVTDUtils(subFile);
		XMLModifier xm = new XMLModifier();
		try {
			xm.bind(vu.getVTDNav());
		} catch (ModifyException e) {
			LOGGER.error("", e);
		}
		if (updateAttrByXpath(xm, xpath, vu, name, newValue) != null) {
			save2SubFile(subFile, vu, xm);// update file and VTDNvn
			updateCacheTuAttr(tu, name, newValue);
			setDirty(true);
		}
	}

	@Override
	public void updateTuAttribute(Map<String, TmxTU> tus, String name, String newValue) {
		if (tus.size() == 1) {
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				updateTuAttribute(entry.getKey(), entry.getValue(), name, newValue);
			}
			return;
		}

		// key--file; value--hsid
		Map<String, List<String>> ids = new HashMap<String, List<String>>();
		for (Entry<String, TmxTU> entry : tus.entrySet()) {
			String[] strs = TeCoreUtils.parseTuIndentifier(entry.getKey());
			if (strs == null || strs.length < 2) {
				continue;
			}
			if (!ids.containsKey(strs[0])) {
				ids.put(strs[0], new LinkedList<String>());
			}
			ids.get(strs[0]).add(strs[1]);
		}

		for (Entry<String, List<String>> entry : ids.entrySet()) {
			// 子文件
			String subFile = entry.getKey();
			List<String> hsids = entry.getValue();

			VTDUtils vu = container.getVTDUtils(subFile);
			VTDNav vn = vu.getVTDNav();
			AutoPilot ap = new AutoPilot(vn);
			AutoPilot tmpAp = new AutoPilot(vn);
			XMLModifier xm = null;
			try {
				xm = new XMLModifier(vu.getVTDNav());
			} catch (ModifyException e) {
				LOGGER.error(e.getMessage(), e);
				continue;
			}

			try {
				ap.selectXPath("/tmx/body/tu");
				while (ap.evalXPath() != -1) {
					String hsid = vu.getCurrentElementAttribut("hsid", "");
					if (hsids.contains(hsid)) {
						String identifier = new StringBuilder().append(subFile).append(TeCoreConstant.ID_MARK)
								.append(hsid).toString();
						int index = -1;
						vn.push();
						if (newValue == null) {// 删除
							tmpAp.selectXPath("./@" + name);
							if ((index = tmpAp.evalXPath()) != -1) {
								xm.removeAttribute(index);
							}
						} else {// 有则更新，无则添加
							tmpAp.selectXPath("./@" + name);
							if ((index = tmpAp.evalXPath()) != -1) {
								xm.updateToken(index + 1, newValue);
							} else {
								xm.insertAttribute(" " + name + "=\"" + newValue + "\"");
							}
						}
						vn.pop();
						updateCacheTuAttr(tus.get(identifier), name, newValue);
						setDirty(true);
					}
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
			save2SubFile(subFile, vu, xm);
		}
	}

	private XMLModifier updateAttrByXpath(XMLModifier xm, String xpath, VTDUtils vu, String name, String newValue) {
		VTDNav vn = vu.getVTDNav();
		AutoPilot ap = new AutoPilot();
		try {
			ap.bind(vn);
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				int index = vn.getAttrVal(name);
				if (index == -1) {
					xm.insertAttribute(" " + name + "=\"" + newValue + "\"");
				} else {
					xm.updateToken(index, newValue);
				}
			} else {
				return null;
			}
			return xm;
		} catch (Exception e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public void deleteTuAttribute(String tuIdentifier, TmxTU tu, String name) {
		if (tuIdentifier == null || tuIdentifier.length() == 0 || name == null || name.length() == 0) {
			return;
		}
		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return;
		}
		String subFile = strs[0];
		String id = strs[1];

		String xpath = "/tmx/body/tu[@hsid='" + id + "']/@" + name;
		VTDUtils vu = container.getVTDUtils(subFile);
		XMLModifier xm = deleteAttrByXpath(xpath, vu);
		if (xm != null) {
			save2SubFile(subFile, vu, xm);
			updateCacheTuAttr(tu, name, null);
			setDirty(true);
		}
	}

	@Override
	public void deleteTuAttribute(Map<String, TmxTU> tus, String name) {
		if (tus.size() == 1) {
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				deleteTuAttribute(entry.getKey(), entry.getValue(), name);
				return;
			}
		}
		updateTuAttribute(tus, name, null);
	}

	private XMLModifier deleteAttrByXpath(String xpath, VTDUtils vu) {
		try {
			AutoPilot ap = new AutoPilot(vu.getVTDNav());
			XMLModifier xm = new XMLModifier(vu.getVTDNav());
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				xm.remove();
			} else {
				return null;
			}
			return xm;
		} catch (Exception e) {
			LOGGER.error("", e);
			return null;
		}
	}

	public void addTuvAttribute(String tuIdentifier, TmxSegement tuv, String name, String value) {
		if (tuIdentifier == null || tuIdentifier.length() == 0 || value == null || value.length() == 0 || name == null
				|| name.length() == 0 || tuv == null) {
			return;
		}
		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return;
		}
		String subFile = strs[0];
		String id = strs[1];
		int tuvId = tuv.getDbPk();

		VTDUtils vu = container.getVTDUtils(subFile);
		if (vu == null) {
			return;
		}

		Map<String, String> attrs = tuv.getAttributes();
		if (attrs != null && attrs.containsKey(name)) {
			return;
		}

		value = TextUtil.cleanSpecialString(value);
		String attributeStr = " " + name + "=\"" + value + "\"";
		String xpath = "/tmx/body/tu[@hsid='" + id + "']/tuv[" + tuvId + "]";
		XMLModifier xm = new XMLModifier();
		try {
			xm.bind(vu.getVTDNav());
		} catch (ModifyException e) {
			LOGGER.error("", e);
		}
		addNodeAttribute(xm, vu, xpath, attributeStr);

		save2SubFile(subFile, vu, xm); // update file and VTDNav
		setDirty(true);
		tuv.appendAttribute(name, value); // update tuv
	}

	public void updateTuvAttribute(String tuIdentifier, TmxSegement tuv, String name, String newValue) {
		if (tuIdentifier == null || tuIdentifier.length() == 0 || newValue == null || newValue.length() == 0
				|| name == null || name.length() == 0) {
			return;
		}
		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return;
		}
		String subFile = strs[0];
		String id = strs[1];
		int tuvId = tuv.getDbPk();
		newValue = TextUtil.cleanSpecialString(newValue);
		String xpath = "/tmx/body/tu[@hsid='" + id + "']/tuv[" + tuvId + "]";
		VTDUtils vu = container.getVTDUtils(subFile);
		XMLModifier xm = new XMLModifier();
		try {
			xm.bind(vu.getVTDNav());
		} catch (ModifyException e) {
			LOGGER.error("", e);
		}
		if (updateAttrByXpath(xm, xpath, vu, name, newValue) != null) {
			save2SubFile(subFile, vu, xm);
			updateCacheTuvAttr(tuv, name, newValue);
			setDirty(true);
		}
	}

	public void deleteTuvAttribute(String tuIdentifier, TmxSegement tuv, String name) {
		if (tuIdentifier == null || tuIdentifier.length() == 0 || name == null || name.length() == 0) {
			return;
		}
		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return;
		}
		String subFile = strs[0];
		String id = strs[1];
		int tuvid = tuv.getDbPk();
		String xpath = "/tmx/body/tu[@hsid='" + id + "']/tuv[" + tuvid + "]/@" + name;
		VTDUtils vu = container.getVTDUtils(subFile);
		XMLModifier xm = deleteAttrByXpath(xpath, vu);
		if (xm != null) {
			save2SubFile(subFile, vu, xm);
			updateCacheTuvAttr(tuv, name, null);
			setDirty(true);
		}
	}

	public int addTuProp(String tuIdentifier, TmxTU tu, String propType, String newContent) {
		int id = -1;
		if (propType == null || newContent == null || propType.isEmpty() || newContent.isEmpty()) {
			return -1;
		}
		
		// build prop
		TmxProp prop = new TmxProp();
		prop.setValue(newContent);
		prop.setName(propType);
		
		// clean string
		propType = TextUtil.cleanSpecialString(propType.trim());
		newContent = TextUtil.cleanSpecialString(newContent.trim());

		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return id;
		}
		VTDUtils vu = container.getVTDUtils(strs[0]);
		if (vu == null) {
			return id;
		}

		List<TmxProp> props = tu.getProps();
		if (props == null) {
			props = new ArrayList<TmxProp>();
		}
		id = props.size() + 1;

//		newContent = TextUtil.cleanString(newContent);
		String xpath = "/tmx/body/tu[@hsid='" + strs[1] + "']";
		XMLModifier xm = new XMLModifier();
		try {
			VTDNav vn = vu.getVTDNav();
			xm.bind(vn);
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/tmx/body/tu[@hsid='" + strs[1] + "']/prop[last()]");
			if (ap.evalXPath() != -1) {
				xm.insertAfterElement("<prop type=\"" + propType + "\">" + newContent + "</prop>");
			} else {
				ap.selectXPath(xpath);
				if (ap.evalXPath() != -1) {
					xm.insertAfterHead("<prop type=\"" + propType + "\">" + newContent + "</prop>");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
		}
//		addNode(xm, vu, xpath, "<prop type=\"" + propType + "\">" + newContent + "</prop>");
		save2SubFile(strs[0], vu, xm);// update file and VTDNav
		// 完成文件操作后，需要添加 prop 到 TU 的位置。
		prop.setDbPk(id);
		props.add(prop);
		tu.setProps(props);
		setDirty(true);
		return id;
	}

	@Override
	public int addTuProp(Map<String, TmxTU> tus, String propType, String newContent) {

		if (tus.size() == 1) {
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				return addTuProp(entry.getKey(), entry.getValue(), propType, newContent);
			}
		}
		
		if (propType == null || newContent == null || propType.isEmpty() || newContent.isEmpty()) {
			return -1;
		}
		
		propType = TextUtil.cleanSpecialString(propType.trim());
		newContent = TextUtil.cleanSpecialString(newContent.trim());
		

		// key--file; value--hsid
		Map<String, List<String>> ids = new HashMap<String, List<String>>();
		for (Entry<String, TmxTU> entry : tus.entrySet()) {
			String[] strs = TeCoreUtils.parseTuIndentifier(entry.getKey());
			if (strs == null || strs.length < 2) {
				continue;
			}
			if (!ids.containsKey(strs[0])) {
				ids.put(strs[0], new LinkedList<String>());
			}
			ids.get(strs[0]).add(strs[1]);
		}

		for (Entry<String, List<String>> entry : ids.entrySet()) {
			// 子文件
			String subFile = entry.getKey();
			List<String> hsids = entry.getValue();

			VTDUtils vu = container.getVTDUtils(subFile);
			VTDNav vn = vu.getVTDNav();
			AutoPilot ap = new AutoPilot(vn);
			XMLModifier xm = null;
			try {
				xm = new XMLModifier(vu.getVTDNav());
			} catch (ModifyException e) {
				LOGGER.error(e.getMessage(), e);
				continue;
			}

			for (String hsid : hsids) {
				// 缓存
				TmxTU tu = tus.get(new StringBuilder().append(subFile).append(TeCoreConstant.ID_MARK).append(hsid)
						.toString());
				List<TmxProp> props = tu.getProps();
				if (props == null) {
					props = new ArrayList<TmxProp>();
					tu.setProps(props);
				}
				try {
					ap.selectXPath("/tmx/body/tu");
					while (ap.evalXPath() != -1) {
						if (hsid.equals(vn.toString(vn.getAttrVal("hsid")))) {
							xm.insertAfterHead("<prop type=\"" + propType + "\">" + newContent + "</prop>");
							TmxProp prop = new TmxProp();
							prop.setName(propType);
							prop.setValue(newContent);
							prop.setDbPk(props.size() + 1);
							props.add(prop);
							setDirty(true);
							break;
						}
					}
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
			save2SubFile(subFile, vu, xm);
		}
		return tus.size();
	}

	public void updateTuProp(String tuIdentifier, TmxTU tu, TmxProp prop, String propType, String newContent) {
		if (newContent == null || newContent.length() == 0 || propType == null || propType.length() == 0 || tu == null
				|| prop == null) {
			return;
		}
		
		propType = TextUtil.cleanSpecialString(propType);
		newContent = TextUtil.cleanSpecialString(newContent);
		
		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return;
		}
		String subFile = strs[0];
		String id = strs[1];
		List<TmxProp> props = tu.getProps();
		if (props == null || !props.contains(prop)) {
			return;
		}
		int propid = prop.getDbPk();
		String xpath = "/tmx/body/tu[@hsid='" + id + "']/prop[" + propid + "]";
		String nodeFragment = "<prop type=\"" + propType + "\">" + newContent + "</prop>";
		VTDUtils vu = container.getVTDUtils(subFile);
		if (vu == null) {
			return;
		}
		XMLModifier xm = new XMLModifier();
		try {
			xm.bind(vu.getVTDNav());
		} catch (ModifyException e) {
			LOGGER.error("", e);
		}
		if (updateNode(xm, vu, xpath, nodeFragment)) {
			save2SubFile(subFile, vu, xm); // update file and VTDNav
			// update TmxProp
			prop.setName(propType);
			prop.setValue(newContent);
			setDirty(true);
		}
	}

	@Override
	public void updateTuProp(Map<String, TmxTU> tus, TmxProp prop, String propType, String newContent) {
		
		if (tus.size() == 1) {
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				updateTuProp(entry.getKey(), entry.getValue(), prop, propType, newContent);
				return;
			}
		}
		
		if (propType != null) {
			propType = TextUtil.cleanSpecialString(propType);
		}
		if (newContent != null) {
			newContent = TextUtil.cleanSpecialString(newContent);
		}

		// key--file; value--hsid
		Map<String, List<String>> ids = new HashMap<String, List<String>>();
		// key--identifier; value--dbpk
		Map<String, Integer> propIds = new HashMap<String, Integer>();
		for (Entry<String, TmxTU> entry : tus.entrySet()) {
			TmxTU tu = entry.getValue();
			List<TmxProp> props = tu.getProps();
			if (props == null) {
				continue;
			}
			boolean shouldModify = false;
			for (TmxProp checkProp : props) {
				if (prop.getValue().equals(checkProp.getValue()) && prop.getName().equals(checkProp.getName())) {// 是否需要更新
					propIds.put(entry.getKey(), checkProp.getDbPk());
					shouldModify = true;
					break;
				}
			}
			if (!shouldModify) {
				continue;
			}

			String[] strs = TeCoreUtils.parseTuIndentifier(entry.getKey());
			if (strs == null || strs.length < 2) {
				continue;
			}
			if (!ids.containsKey(strs[0])) {
				ids.put(strs[0], new LinkedList<String>());
			}
			ids.get(strs[0]).add(strs[1]);
		}

		for (Entry<String, List<String>> entry : ids.entrySet()) {
			// 子文件
			String subFile = entry.getKey();
			List<String> hsids = entry.getValue();

			VTDUtils vu = container.getVTDUtils(subFile);
			VTDNav vn = vu.getVTDNav();
			AutoPilot ap = new AutoPilot(vn);
			AutoPilot tmpAp = new AutoPilot(vn);
			XMLModifier xm = null;
			try {
				xm = new XMLModifier(vu.getVTDNav());
			} catch (ModifyException e) {
				LOGGER.error(e.getMessage(), e);
				continue;
			}

			try {
				ap.selectXPath("/tmx/body/tu");
				while (ap.evalXPath() != -1 && !hsids.isEmpty()) {
					String hsid = vn.toString(vn.getAttrVal("hsid"));
					if (hsid == null || !hsids.contains(hsid)) {
						continue;
					}
					vn.push();
					hsids.remove(hsid);
					String identifier = new StringBuilder().append(subFile).append(TeCoreConstant.ID_MARK).append(hsid)
							.toString();
					int dbpk = propIds.get(identifier);
					tmpAp.selectXPath(new StringBuilder().append("./prop[").append(dbpk).append("]").toString());
					if (tmpAp.evalXPath() != -1) {
						xm.remove();
						List<TmxProp> props = tus.get(identifier).getProps();
						if (propType != null) {
							// "<prop type=\"" + propType + "\">" + newContent + "</prop>"
							xm.insertAfterElement(new StringBuilder().append("<prop type=\"").append(propType)
									.append("\">").append(newContent).append("</prop>").toString());
							if (props.size() >= dbpk - 1) {
								props.get(dbpk - 1).setName(propType);
								props.get(dbpk - 1).setValue(newContent);
							}
						} else {
							if (props.size() >= dbpk - 1) {
								props.remove(dbpk - 1);
							}
						}
						setDirty(true);
					}
					vn.pop();
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
			save2SubFile(subFile, vu, xm);
		}
	}

	public void deleteTuProp(String tuIdentifier, TmxTU tu, TmxProp deleteProp) {
		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		if (strs == null) {
			return;
		}
		String subFile = strs[0];
		String id = strs[1];

		VTDUtils vu = container.getVTDUtils(subFile);
		if (vu == null) {
			return;
		}
		int noteId = deleteProp.getDbPk();
		String xpath = "/tmx/body/tu[@hsid='" + id + "']/prop[" + noteId + "]";
		XMLModifier xm = new XMLModifier();
		AutoPilot ap = new AutoPilot(vu.getVTDNav());

		try {
			xm.bind(vu.getVTDNav());
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				xm.remove();
				tu.getProps().remove(deleteProp);
				save2SubFile(subFile, vu, xm);
				setDirty(true);
				for (TmxProp prop : tu.getProps()) {
					if (prop.getDbPk() > deleteProp.getDbPk()) {
						prop.setDbPk(prop.getDbPk() - 1);
					}
				}
			}
		} catch (ModifyException e) {
			e.printStackTrace();
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteTuProp(Map<String, TmxTU> tus, TmxProp deleteProp) {
		if (tus.size() == 1) {
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				deleteTuProp(entry.getKey(), entry.getValue(), deleteProp);
				return;
			}
		}
		updateTuProp(tus, deleteProp, null, null);
	}
	@Override
	public void deleteTuPropByType(Map<String, TmxTU> tus, String name) {
		
		if (name == null) {
			return;
		}
		String oldName = name;
		name = TextUtil.cleanSpecialString(name);
		
		// key--file; value--hsid
		Map<String, List<String>> subfile_hsid = new HashMap<String, List<String>>();
		// key--identifier; value--dbpk
		Map<String, Integer> propIds = new HashMap<String, Integer>();
		for (Entry<String, TmxTU> entry : tus.entrySet()) {
			TmxTU tu = entry.getValue();
			List<TmxProp> props = tu.getProps();
			if (props == null) {
				continue;
			}
			boolean shouldModify = false;
			for (TmxProp checkProp : props) {
				if (oldName.equals(checkProp.getName())) {// 是否需要更新
					propIds.put(entry.getKey(), checkProp.getDbPk());
					shouldModify = true;
					break;
				}
			}
			if (!shouldModify) {
				continue;
			}

			String[] strs = TeCoreUtils.parseTuIndentifier(entry.getKey());
			if (strs == null || strs.length < 2) {
				continue;
			}
			if (!subfile_hsid.containsKey(strs[0])) {
				subfile_hsid.put(strs[0], new LinkedList<String>());
			}
			subfile_hsid.get(strs[0]).add(strs[1]);
		}

		for (Entry<String, List<String>> entry : subfile_hsid.entrySet()) {
			// 子文件
			String subFile = entry.getKey();
			List<String> hsids = entry.getValue();

			VTDUtils vu = container.getVTDUtils(subFile);
			VTDNav vn = vu.getVTDNav();
			AutoPilot ap = new AutoPilot(vn);
			AutoPilot tmpAp = new AutoPilot(vn);
			XMLModifier xm = null;
			try {
				xm = new XMLModifier(vu.getVTDNav());
			} catch (ModifyException e) {
				LOGGER.error(e.getMessage(), e);
				continue;
			}

			try {
				ap.selectXPath("/tmx/body/tu");
				while (ap.evalXPath() != -1 && !hsids.isEmpty()) {
					String hsid = vn.toString(vn.getAttrVal("hsid"));
					if (hsid == null || !hsids.contains(hsid)) {
						continue;
					}
					hsids.remove(hsid);// has removed
					boolean rmProp = false;
					vn.push();
					
					tmpAp.selectXPath("./prop");
					while (tmpAp.evalXPath() != -1) {
						String type = vn.toRawString(vn.getAttrVal("type"));
						if (name.equals(type)) {
							xm.remove();
							rmProp = true;
						}
					}
					if (rmProp) {
						String identifier = new StringBuilder().append(subFile).append(TeCoreConstant.ID_MARK).append(hsid).toString();
						List<TmxProp> rmProps = new ArrayList<TmxProp>();
						for (TmxProp prop : tus.get(identifier).getProps()) {
							rmProps.add(prop);
						}
						List<TmxProp> rdbkProps = tus.get(identifier).getProps();
						rdbkProps.removeAll(rmProps);
					}
					vn.pop();
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
			save2SubFile(subFile, vu, xm);
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
	public TmxTU getTuByIdentifier(String tuIdentifier) {
		String[] strs = TeCoreUtils.parseTuIndentifier(tuIdentifier);
		TmxTU tu = new TmxTU();
		if (strs == null) {
			return tu;
		}
		String subFile = strs[0];
		String id = strs[1];
		VTDUtils vu = container.getVTDUtils(subFile);
		VTDNav vn = vu.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("//tu[@hsid='" + id + "']");
			if (ap.evalXPath() != -1) {
				TmxFileDataAccessUtils.readTUAttr4VTDNav(vu, tu);
				TmxFileDataAccessUtils.readTUNote4VTDNav(vu, tu);
				TmxFileDataAccessUtils.readTUProp4VTDNav(vu, tu);
				TmxFileDataAccessUtils.readTUTuv4VTDNav(vu, tu, super.currSrcLang, super.currTgtLang);
			}
		} catch (VTDException e1) {
			e1.printStackTrace();
		}
		tu.setTmId(Integer.parseInt(id));
		return tu;
	}

	private void save2SubFile(String subFile, VTDUtils vu, XMLModifier xm) {
		try {
			VTDNav vn = xm.outputAndReparse();
			vu.bind(vn);

			xm = new XMLModifier(vn);
			FileOutputStream fos = new FileOutputStream(subFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			xm.output(bos); // 写入文件
			bos.close();
			fos.close();
		} catch (VTDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 更新 xpath 指定的整个节点，先删除这个节点，然后再插入 nodeFragment
	 * @param subFile
	 *            当前 XML 的绝对路径
	 * @param vu
	 *            当前 XML 文件的 {@link VTDUtils} 对象
	 * @param xpath
	 *            定位到当前处理节点的位置
	 * @param nodeFragment
	 *            整个节点的内容，如：<note>xx<note>，如果内容为空或者 null 则只做删除;
	 */
	private boolean updateNode(XMLModifier xm, VTDUtils vu, String xpath, String nodeFragment) {
		if (nodeFragment == null || nodeFragment.length() == 0) {
			return false;
		}
		try {
			if (vu.pilot(xpath) != -1) {
				xm.remove();
				xm.insertAfterElement(nodeFragment);
				return true;
			}
		} catch (VTDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private void addNode(XMLModifier xm, VTDUtils vu, String xpath, String nodeFragment) {
		if (nodeFragment == null || nodeFragment.length() == 0) {
			return;
		}
		try {
			if (vu.pilot(xpath) != -1) {
				xm.bind(vu.getVTDNav());
				xm.insertAfterHead(nodeFragment);
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	/**
	 * @param subFile
	 * @param vu
	 * @param xpath
	 *            定位到目标节点
	 * @param attributeStr
	 *            为一个符合 xml 属性的字符串，如 attrName = "attrValue";
	 */
	private void addNodeAttribute(XMLModifier xm, VTDUtils vu, String xpath, String attributeStr) {
		try {
			if (vu.pilot(xpath) != -1) {
				xm.insertAttribute(attributeStr);
			}
		} catch (VTDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 批量修时使用的上下文类，提供以下接口：<br>
	 * <li>遍历整个文档时使用的 xpath 语句</li> <li>将修改应用到“整个文件/记忆库”</li> <li>将修改应用到“新当前过滤结果”</li>
	 * @author Austen
	 * @version
	 * @since JDK1.6
	 */
	protected abstract class BatchModifyContext {
		/**
		 * 遍历整个文档时使用的 xpath 语句
		 * @return xpath 语句;
		 */
		abstract String getBaseXpath();

		/**
		 * 将修改应用到“整个文件/记忆库”
		 * @param vu
		 *            {@link VTDUtils} 实例，已经定位到 getBaseXpath() 所指定的位置
		 * @param xm
		 *            {@link XMLModifier} 实例，已绑定 vu;
		 */
		abstract void modifyAllItems(VTDUtils vu, XMLModifier xm);

		/**
		 * 将修改应用到“新当前过滤结果”
		 * @param vu
		 *            {@link VTDUtils} 实例，已经定位到 getBaseXpath() 所指定的位置
		 * @param xm
		 *            {@link XMLModifier} 实例，已绑定 vu
		 * @param set
		 *            过滤结果集合
		 * @return 更新的节点数;
		 */
		abstract int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set);

		/**
		 * 在批量处理一个文件之前，如果需要修改 header，do it;
		 */
		void modifyHeader(VTDUtils vu, XMLModifier xm) {
		}
	}

	/**
	 * 批量修改文档。此方法主要封装 xpath 语句循环以及 进度条显示，<br>
	 * 至于 fragment 以何种修改方式体现文件中，请实现 {@link BatchModifyContext} 接口。
	 * @param monitor
	 *            进度条支持
	 * @param xpath
	 *            xpath
	 * @param fragment
	 *            修改片段
	 * @param filter
	 *            应用策略，如果为 {@link TeCoreConstant#FILTERID_allSeg} 则表示更新至整个文档，否则只更新当前过滤结果
	 * @param cxt
	 *            修改文档时使用的上下文, 参见 {@link BatchModifyContext};
	 */
	private void batchModifyDocument(IProgressMonitor monitor, String fragment, String filter,
			List<String> filterIdentifier, BatchModifyContext cxt) {
		// 进度条指标
		int works = 0;// 需完成
		int count = 0;// 计数器
		int worked = 0;// 已完成

		String xpath = cxt.getBaseXpath();
		if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {// 整个库
			monitor.beginTask(Messages.getString("core.fileAccess.applyChang2AllFile.taskname"), container
					.getSubFiles().size());
			for (String subFile : container.getSubFiles()) {
				VTDUtils vu = container.getVTDUtils(subFile);
				try {
					XMLModifier xm = new XMLModifier(vu.getVTDNav());
					cxt.modifyHeader(vu, xm);
					AutoPilot ap = new AutoPilot(vu.getVTDNav());
					ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
					ap.selectXPath("count(" + xpath + ")");
					double total = ap.evalXPathToNumber();// 总数
					works = (int) (total > 100 ? 100 : total);// 总共工作数
					IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
					subMonitor.beginTask("", works + 1);// 加一的原因，保存时暂用

					int tmp = 0;
					ap.selectXPath(xpath);
					while (ap.evalXPath() != -1) {
						cxt.modifyAllItems(vu, xm);
						tmp = (int) (count++ * 100 / total);
						if (tmp > worked) {
							subMonitor.worked(tmp - worked);
							worked = tmp;
						}
					}
					save2SubFile(subFile, vu, xm);
					subMonitor.worked(1);
					subMonitor.done();
				} catch (Exception e) {
					LOGGER.error("", e);
				}

			}
			monitor.done();
		} else {// 当前过滤结果
			// 查询所有改动子文件，以及 tu hsid
			Map<String, Set<String>> subFileMap = new HashMap<String, Set<String>>();
			String[] subid = null;
			List<String> fileIndentiFier = filterIdentifier == null ? tuIdentifiers : filterIdentifier;
			for (String identifier : fileIndentiFier) {
				subid = identifier.split(TeCoreConstant.ID_MARK);
				if (subFileMap.containsKey(subid[0])) {
					subFileMap.get(subid[0]).add(subid[1]);
				} else {
					Set<String> list = new HashSet<String>();
					list.add(subid[1]);
					subFileMap.put(subid[0], list);
				}
			}

			monitor.beginTask(Messages.getString("core.fileAccess.resetfilter.datas.taskname"), subFileMap.size());
			for (Entry<String, Set<String>> entry : subFileMap.entrySet()) {
				String subFile = entry.getKey();
				Set<String> hsidSet = entry.getValue();

				int total = hsidSet.size();
				works = total > 100 ? 100 : total;

				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
				subMonitor.beginTask("", works + 1);

				VTDUtils vu = container.getVTDUtils(subFile);
				AutoPilot ap = new AutoPilot(vu.getVTDNav());
				ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
				XMLModifier xm = null;
				try {
					xm = new XMLModifier(vu.getVTDNav());
					ap.selectXPath(xpath);
					int tmp = 0;
					while (ap.evalXPath() != -1) {
						if (cxt.modifyFilterItems(vu, xm, hsidSet) > 0) {
							tmp = (int) (count++ * 100 / total);
							if (tmp > worked) {
								subMonitor.worked(tmp - worked);
								worked = tmp;
							}
						}
					}
					save2SubFile(entry.getKey(), vu, xm);
					subMonitor.worked(1);
					subMonitor.done();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			monitor.done();
		}
		setDirty(true);
	}

	/**
	 * 清除选中行标记
	 * @param selectTuIds
	 *            ;
	 */
	public void clearSelectLinesInnerTag(IProgressMonitor monitor, final List<String> selectTuIds) {
		final String lowerCaseSrclang = super.currSrcLang.toLowerCase();
		final String lowerCaseTgtlang = super.currTgtLang.toLowerCase();
		batchModifyDocument(monitor, null, null, selectTuIds, new BatchModifyContext() {
			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				if (null == set || set.isEmpty()) {
					return 0;
				}

				int count = 0;
				try {
					String hsId = vu.getCurrentElementAttribut("hsid", "");
					if (set.contains(hsId)) {
						VTDNav vn = vu.getVTDNav();
						vn.push();
						String politSegXpath = "./tuv[lower-case(@xml:lang)='" + lowerCaseSrclang
								+ "' or lower-case(@xml:lang)='" + lowerCaseTgtlang + "']/seg";
						AutoPilot ap = new AutoPilot(vn);
						ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
						ap.selectXPath(politSegXpath);
						while (ap.evalXPath() != -1) {
							String oldContent = vu.getElementContent();
							StringBuffer sbrbt = new StringBuffer();
							sbrbt.append("<seg>").append(TmxInnerTagParser.getInstance().getTmxPureText(oldContent))
									.append("</seg>");
							xm.remove();
							xm.insertAfterElement(sbrbt.toString());
						}
						vn.pop();
						count++;
					}
				} catch (NavException e) {
					LOGGER.error("remove select line innertag erorr", e);
				} catch (XPathParseException e) {
					LOGGER.error("remove select line innertag erorr", e);
				} catch (XPathEvalException e) {
					LOGGER.error("remove select line innertag erorr", e);
				} catch (ModifyException e) {
					LOGGER.error("remove select line innertag erorr", e);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				return count;
			}

			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				// do noting
			}

			@Override
			public String getBaseXpath() {
				return "/tmx/body/tu";
			}

		});
	}

	/**
	 * @deprecated 除非允许在 tu 节点中添加属性，否则无需使用此方法
	 */
	public void batchAddTuAttr(IProgressMonitor monitor, final String name, String value, String filter) {
		// do nothing
	}

	@Override
	public void batchUpdateTuAttr(IProgressMonitor monitor, String name, String value, String filter) {

		final String cleanName = TextUtil.cleanSpecialString(name);
		final String cleanValue = value == null ? null : TextUtil.cleanSpecialString(value);
		final String fragment = cleanValue == null ? null : " " + cleanName + "=\"" + cleanValue + "\"";

		batchModifyDocument(monitor, fragment, filter, null, new BatchModifyContext() {
			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				try {
					VTDNav vn = vu.getVTDNav();
					if (vn.hasAttr(cleanName)) {
						vn.push();
						AutoPilot ap = new AutoPilot(vn);
						ap.selectXPath("./@" + cleanName);
						int index = ap.evalXPath();
						if (index != -1) {
							if (cleanValue == null) {
								xm.removeAttribute(index);
							} else {
								xm.updateToken(index + 1, cleanValue);
							}
						}
						vn.pop();
					} else if (fragment != null) {
						xm.insertAttribute(fragment);
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}

			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				try {
					String attr = vu.getCurrentElementAttribut("hsid", "");
					if (set.contains(attr)) {
						VTDNav vn = vu.getVTDNav();
						vn.push();
						if (vn.hasAttr(cleanName)) {
							AutoPilot ap = new AutoPilot(vn);
							ap.selectXPath("./@" + cleanName);
							int index = ap.evalXPath();
							if (index != -1) {
								if (cleanValue != null) {
									xm.updateToken(index + 1, cleanValue);
								} else {
									xm.removeAttribute(index);
								}
							}
						} else if (fragment != null) {
							xm.insertAttribute(fragment);
						}
						vn.pop();
						return 1;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}

			@Override
			public String getBaseXpath() {
				return "/tmx/body/tu";
			}
		});
		setDirty(true);
	}

	@Override
	public void batchDeleteTuAttr(IProgressMonitor monitor, String name, String filter) {
		batchUpdateTuAttr(monitor, name, null, filter);
	}

	@Override
	public void batchAddTmxProp(IProgressMonitor monitor, String type, String content, String filter) {
		final String xpath = "/tmx/body/tu";
		final String clearType = TextUtil.cleanSpecialString(type);
		final String clearContent = TextUtil.cleanSpecialString(content);
		final String fragment = new StringBuffer("<prop type=\"").append(clearType).append("\">").append(clearContent)
				.append("</prop>").toString();

		batchModifyDocument(monitor, fragment, filter, null, new BatchModifyContext() {
			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				try {
					String attr = vu.getCurrentElementAttribut("hsid", "");
					if (set.contains(attr)) {
						xm.insertAfterHead(fragment);
						return 1;
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
				return 0;
			}

			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				try {
					xm.insertAfterHead(fragment);
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}

			@Override
			public String getBaseXpath() {
				return xpath;
			}
		});
		setDirty(true);
	}

	@Override
	public void batchUpdateTmxProp(IProgressMonitor monitor, final TmxProp prop, String propType, String content,
			final String filter) {

		final String cleanPropType = propType == null ? null : TextUtil.cleanSpecialString(propType);
		final String cleanContent = content == null ? null : TextUtil.cleanSpecialString(content);

		StringBuilder builder = new StringBuilder();
		builder.append("concat(");
		builder.append(")");
		
		final String oldCleanContent = TextUtil.cleanSpecialString(prop.getValue());
		
		batchModifyDocument(monitor, null, filter, null, new BatchModifyContext() {
			@Override
			public String getBaseXpath() {
				if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {
					return "/tmx/body/tu/prop[@type=" + TextUtil.attributeValue(prop.getName()) + "]";
				}
				return "/tmx/body/tu[prop[@type=" + TextUtil.attributeValue(prop.getName()) + "]]";
			}

			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				try {
					// type 相等已在 xpath 中过滤
					if (oldCleanContent.equals(vu.getElementContent())) {// 检测是否需要更新
						if (cleanPropType != null || cleanContent != null) {
							xm.insertAfterElement(getNewFragment(vu.getElementHead()));
						}
						xm.remove();// 删除操作
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}

			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				int hasmodify = 0;
				try {
					if (set.contains(vu.getCurrentElementAttribut("hsid", ""))) {// 检测是否在更新之列
						AutoPilot ap = new AutoPilot(vu.getVTDNav());
						vu.getVTDNav().push();
						ap.selectXPath("./prop[@type=" + TextUtil.attributeValue(prop.getName()) + "]");
						String fragment = null;
						while (ap.evalXPath() != -1) {
							if (oldCleanContent.equals(vu.getElementContent())) {// 检测是否含有需更新节点
								if (cleanPropType != null || cleanContent != null) {
									fragment = getNewFragment(vu.getElementHead());
									xm.insertAfterElement(fragment);
								}
								xm.remove();
								hasmodify++;
							}
						}
						vu.getVTDNav().pop();

					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
				return hasmodify;
			}

			private String getNewFragment(String header) {
				String regex = "type\\s*=\\s*([\"']).*\\1";
				if (!prop.getName().equals(cleanPropType)) {// 是否更改 type 属性
					header = header.replaceFirst(regex, cleanPropType == null ? "" : "type=\"" + cleanPropType + "\"");
				}
				return new StringBuffer(header).append(cleanContent == null ? "" : cleanContent).append("</prop>")
						.toString();
			}
		});
		prop.setName(propType);
		prop.setValue(content);
	}

	@Override
	public void batchDeleteTmxProp(IProgressMonitor monitor, TmxProp prop, String filter) {
		batchUpdateTmxProp(monitor, prop, null, null, filter);
	}
	@Override
	public void batchDeleteTmxPropByType(IProgressMonitor monitor, String type, final String filter) {
		
		final String cleanPropType = TextUtil.cleanSpecialString(type);
		final String all_xpath = "/tmx/body/tu/prop[@type=\"" + cleanPropType + "\"]";
		final String filter_xpath = "/tmx/body/tu[prop[@type=\"" + cleanPropType + "\"]]";
		batchModifyDocument(monitor, null, filter, null,new BatchModifyContext() {
			@Override
			public String getBaseXpath() {
				if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {
					return all_xpath;
				}
				return filter_xpath;
			}

			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				try {
					xm.remove();// 删除操作
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}

			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				int hasmodify = 0;
				try {
					if (set.contains(vu.getCurrentElementAttribut("hsid", ""))) {// 检测是否在更新之列
						AutoPilot ap = new AutoPilot(vu.getVTDNav());
						vu.getVTDNav().push();
						ap.selectXPath("./prop[@type=\"" + cleanPropType + "\"]");
						while (ap.evalXPath() != -1) {
							xm.remove();
							hasmodify++;
						}
						vu.getVTDNav().pop();
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
				return hasmodify;
			}
		});
	}

	/**
	 * @deprecated 除非可以在 tuv 节点中添加属性，否则不适用此方法。
	 */
	public void batchAddTuvAttr(IProgressMonitor monitor, String name, String value, String lang, String filter) {
		// do nothing
	}

	@Override
	public void batchUpdateTuvAttr(IProgressMonitor monitor, String name, String value, final List<String> langs,
			final String filter) {

		final String cleanName = TextUtil.cleanSpecialString(name);
		final String cleanValue = value == null ? null : TextUtil.cleanSpecialString(value);
		final String fragment = cleanValue == null ? null : " " + cleanName + "=\"" + cleanValue + "\"";

		final StringBuffer predicate = new StringBuffer();
		for (int i = 0; i < langs.size(); i++) {
			if (i == langs.size() - 1) {
				predicate.append("[lower-case(@xml:lang)=\"").append(langs.get(i).toLowerCase()).append("\"]");
			} else {
				predicate.append("lower-case(@xml:lang)=\"").append(langs.get(i).toLowerCase()).append("\" or ");
			}
		}

		batchModifyDocument(monitor, fragment, filter, null, new BatchModifyContext() {
			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				try {
					String attr = vu.getCurrentElementAttribut("hsid", "");
					if (set.contains(attr)) {
						VTDNav vn = vu.getVTDNav();
						vn.push();
						AutoPilot ap = new AutoPilot(vn);
						ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
						ap.selectXPath("./tuv" + predicate.toString());
						int index = -1;
						while ((index = ap.evalXPath()) != -1) {// 定位到 tuv 的 属性值
							if ((index = vn.getAttrVal(cleanName)) != -1) {// 有，则修改值
								if (cleanValue != null) {
									xm.updateToken(index, cleanValue);
								} else {
									xm.removeAttribute(index - 1);
								}
							} else if (fragment != null) {
								xm.insertAttribute(fragment);
							}
						}
						;
						vn.pop();
						return 1;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}

			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				try {
					VTDNav vn = vu.getVTDNav();
					if (vn.hasAttr(cleanName)) {
						vn.push();
						AutoPilot ap = new AutoPilot(vn);
						ap.selectXPath("./@" + cleanName);
						int index = ap.evalXPath();
						if (index != -1) {
							if (cleanValue == null) {
								xm.removeAttribute(index);
							} else {
								xm.updateToken(index + 1, cleanValue);
							}
						}
						vn.pop();
					} else if (fragment != null) {
						xm.insertAttribute(fragment);
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}

			@Override
			public String getBaseXpath() {
				StringBuffer buf = new StringBuffer("/tmx/body/tu[tuv");
				buf.append(predicate.toString());
				buf.append("]");
				String xpath = TeCoreConstant.FILTERID_allSeg.equals(filter) ? "/tmx/body/tu/tuv" : buf.toString();
				return xpath;
			}
		});
	}

	@Override
	public void batchDeleteTuvAttr(IProgressMonitor monitor, String name, List<String> langs, String filter) {
		batchUpdateTuvAttr(monitor, name, null, langs, filter);
	}

	/**
	 * 批量增加 note 节点，提供进度条支持。
	 * @param monitor
	 *            进度条;
	 * @param content
	 *            节点内容;
	 * @param filter
	 *            增加策略：<b>如果为{@link TeCoreConstant#FILTERID_allSeg}则更新整个文件/记忆库，否则更新当前过滤结果</b>;
	 */
	@Override
	public void batchAddTmxNote(IProgressMonitor monitor, String content, final String filter) {
		final String xpath = "/tmx/body/tu";
		final String fragment = "<note>" + TextUtil.cleanSpecialString(content) + "</note>";
		batchModifyDocument(monitor, fragment, filter, null, new BatchModifyContext() {

			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				try {
					xm.insertAfterHead(fragment);
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}

			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				try {
					String attr = vu.getCurrentElementAttribut("hsid", "");
					if (set.contains(attr)) {
						xm.insertAfterHead(fragment);
						return 1;
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
				return 0;
			}

			@Override
			public String getBaseXpath() {
				return xpath;
			}
		});
		setDirty(true);
	}

	/**
	 * 批量更新 note 内容。影响范围：所有及节点内容 equals(content) 的 note 节点。
	 * @param monitor
	 *            进度条支持
	 * @param oldContent
	 *            原内容
	 * @param newcontent
	 *            新内容，为空时删除此节点。
	 * @param filter
	 *            修改策略：<b>如果为{@link TeCoreConstant#FILTERID_allSeg}则更新整个文件/记忆库，否则更新当前过滤结果</b>;
	 */
	@Override
	public void batchUpdateTmxNote(IProgressMonitor monitor, String oldContent, String newcontent, final String filter) {

		final String oldCleanContent = TextUtil.cleanSpecialString(oldContent);
		final String newCleanContent = newcontent == null ? null : TextUtil.cleanSpecialString(newcontent);

		this.batchModifyDocument(monitor, null, filter, null, new BatchModifyContext() {

			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				int hasModify = 0;
				String newFragment = null;
				try {
					String attr = vu.getCurrentElementAttribut("hsid", "");
					if (set.contains(attr)) {// 检测是在更新之列
						AutoPilot ap = new AutoPilot(vu.getVTDNav());
						vu.getVTDNav().push();
						ap.selectXPath("./note");
						while (ap.evalXPath() != -1) {
							if (oldCleanContent.equals(vu.getElementContent())) {// 检测是否含有需更新节点
								newFragment = newCleanContent == null ? null : new StringBuffer(vu.getElementHead())
										.append(newCleanContent).append("</note>").toString();
								if (newFragment != null) {
									xm.insertAfterElement(newFragment);
								}
								xm.remove();
								hasModify++;
							}
						}
						vu.getVTDNav().pop();
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
				return hasModify;
			}

			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				try {
					if (oldCleanContent.equals(vu.getElementContent())) {// 是否是需更新的 note
						String newFragment = newCleanContent == null ? null : new StringBuffer(vu.getElementHead())
								.append(newCleanContent).append("</note>").toString();
						if (newFragment != null) {
							xm.insertAfterElement(newFragment);
						}
						xm.remove();
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}

			@Override
			public String getBaseXpath() {
				if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {
					return "/tmx/body/tu/note";
				} else {
					return "/tmx/body/tu[note]";
				}
			}
		});
	}

	/**
	 * 删除相同 content 的批注
	 * @param monitor
	 * @param content
	 *            ;
	 */
	@Override
	public void batchDeleteTmxNote(IProgressMonitor monitor, String content, String filter) {
		batchUpdateTmxNote(monitor, content, null, filter);
	}

	/**
	 * 批量删除指定语言代码
	 * @author austen
	 * @param langs
	 *            需要删除的语言代码集合;
	 * @param monitor
	 *            进度条
	 */
	public void batchdeleteTuvBylang(final List<String> langs, IProgressMonitor monitor) {
		final List<String> deleteLangs = new LinkedList<String>();
		for (String str : langs) {
			deleteLangs.add(str.toLowerCase());
		}

		batchModifyDocument(monitor, null, TeCoreConstant.FILTERID_allSeg, null, new BatchModifyContext() {

			@Override
			int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				return 0;
			}

			@Override
			void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				VTDNav vn = vu.getVTDNav();
				AutoPilot ap = new AutoPilot(vn);
				ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
				try {
					vn.push();
					String lang = null;
					ap.selectXPath("./tuv/@xml:lang");
					if (ap.evalXPathToBoolean()) {
						lang = "xml:lang";
					} else {
						ap.selectXPath("./tuv/@lang");
						if (ap.evalXPathToBoolean()) {
							lang = "lang";
						}
					}
					if (lang == null) {
						vn.pop();
						return;
					}
					int total = 0;
					ap.selectXPath("count(./tuv)");
					total = (int) ap.evalXPathToNumber();
					int count = 0;
					for (String lowcase : deleteLangs) {
						vn.push();
						ap.selectXPath("./tuv[lower-case(@" + lang + ")='" + lowcase + "']");
						if (ap.evalXPath() != -1) {
							xm.remove();
							count++;
						}
						vn.pop();
					}
					if (total - 1 == count) {
						xm.remove();
					}
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				} finally {
					vn.pop();
				}
			}

			@Override
			String getBaseXpath() {
				return "/tmx/body/tu";
			}
		});

		container.getAllLanguages(false).removeAll(langs);
		super.langList.removeAll(langs);
		if (langs.contains(currTgtLang) && langList.size() != 0) {
			currTgtLang = langList.get(0);
		}
		monitor.done();
	}

	/**
	 * 批量修改语言代码
	 * @param map
	 *            存放需要修改的语言代码， <li>key：源语言代码</li> <li>value：修改后的语言代码</li> <b>注意此处存放的均为标准语言代码</b>
	 * @param monitor
	 *            进度条支持
	 */
	public void batchModifyLangcode(final Map<String, String> map, IProgressMonitor monitor) {
		final List<String> cLangs = container.getAllLanguages(false);
		batchModifyDocument(monitor, null, TeCoreConstant.FILTERID_allSeg, null, new BatchModifyContext() {

			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				// do nothing
				return 0;
			}

			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				VTDNav vn = vu.getVTDNav();
				vn.push();
				try {
					int langIndex = -1;
					String lang = null;
					AutoPilot ap = new AutoPilot(vn);
					ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
					ap.selectXPath("./@xml:lang");
					if ((langIndex = ap.evalXPath()) != -1) {
						lang = vn.toRawString(langIndex + 1);
					} else {
						ap.selectXPath("./@lang");
						if ((langIndex = ap.evalXPath()) != -1) {
							lang = vn.toRawString(langIndex + 1);
						}
					}
					// 标准码
					String oldlang = null;
					if (lang == null || !map.containsKey(oldlang = LanguageUtils.convertLangCode(lang))) {
						vn.pop();
						return;
					}

					String newLang = map.get(oldlang);
					if (oldlang.equalsIgnoreCase(currSrcLang)) {
						currSrcLang = newLang;
					} else if (oldlang.equalsIgnoreCase(currTgtLang)) {
						currTgtLang = newLang;
					}

					if (cLangs.remove(oldlang)) {
						cLangs.add(newLang);
					}
					xm.updateToken(langIndex + 1, newLang);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					vn.pop();
				}
			}

			@Override
			public String getBaseXpath() {
				return "/tmx/body/tu/tuv";
			}

			@Override
			void modifyHeader(VTDUtils vu, XMLModifier xm) {
				String lang = LanguageUtils.convertLangCode(currSrcLang);
				if (map.containsKey(lang)) {
					VTDNav vn = vu.getVTDNav();
					vn.push();
					AutoPilot ap = new AutoPilot(vn);
					try {
						ap.selectXPath("/tmx/header/@srclang");
						if (ap.evalXPath() != -1) {
							xm.updateToken(vn.getCurrentIndex() + 1, map.get(lang));
						}
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						e.printStackTrace();
					}
					vn.pop();
				}
			}
		});
		super.langList.clear();
		super.langList.addAll(cLangs);
		super.langList.remove(currSrcLang);
		container.getTmxHeader().setSrclang(currSrcLang);
	}

	/**
	 * 清理内部标记，所有的内部标记。
	 * @param monitor
	 *            ;
	 */
	public void cleanDisplayTuInnerTag(IProgressMonitor monitor) {
		batchModifyDocument(monitor, null, TeCoreConstant.FILTERID_allSeg, null, new BatchModifyContext() {

			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				// do nothing
				return 0;
			}

			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				try {
					String oldContent = vu.getElementContent();
					if (oldContent.indexOf("<") != -1) {
						StringBuffer sbrbt = new StringBuffer();
						sbrbt.append(vu.getElementHead())
								.append(TmxInnerTagParser.getInstance().getTmxPureText(oldContent)).append("</seg>");
						xm.remove();
						xm.insertAfterElement(sbrbt.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			@Override
			public String getBaseXpath() {
				return "/tmx/body/tu/tuv/seg";
			}
		});
	}

	public void batchTrimSegs(IProgressMonitor monitor) {

		batchModifyDocument(monitor, null, null, null, new BatchModifyContext() {

			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				try {
					String content = vu.getElementContent();
					String trimContent = content.trim();
					if (trimContent.length() != content.length()) {
						xm.remove();
						xm.insertAfterElement(new StringBuffer(vu.getElementHead()).append(trimContent)
								.append("</seg>").toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}

			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				// do nothing
			}

			@Override
			public String getBaseXpath() {
				return "/tmx/body/tu/tuv/seg";
			}
		});

	}

	/**
	 * 删除空行 ;
	 */
	public boolean deleteTgtEmpty(IProgressMonitor monitor, boolean ignoreTag) {
		final int deleteTU = 0;
		final int deleteTgtTuv = 1;
		final int deleteSrcTuv = 2;
		List<String> subFiles = container.getSubFiles();
		boolean hasDirty = false;
		monitor.beginTask("", subFiles.size() * 100);
		monitor.setTaskName(Messages.getString("core.fileAccess.delete.empty.tasknmae"));
		for (String subFile : subFiles) {
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);

			VTDUtils vu = container.getVTDUtils(subFile);
			VTDNav vn = vu.getVTDNav();
			AutoPilot ap = new AutoPilot(vn);
			AutoPilot tuCountAp = new AutoPilot(vn);

			AutoPilot tuvCountAp = new AutoPilot(vn);
			AutoPilot tuvTgtAp = new AutoPilot(vn);
			tuvTgtAp.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);

			AutoPilot tuvSrcAp = new AutoPilot(vn);
			tuvSrcAp.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			int deleteFlag = -1;
			try {
				tuCountAp.selectXPath("count(/tmx/body/tu)");
				int tuCount = (int) tuCountAp.evalXPathToNumber();
				subMonitor.beginTask("", tuCount);
				XMLModifier xm = new XMLModifier(vn);
				ap.selectXPath("/tmx/body/tu");
				tuvCountAp.selectXPath("count(./tuv)");
				tuvTgtAp.selectXPath("./tuv[lower-case(@xml:lang)='" + super.currTgtLang.toLowerCase() + "']/seg");
				tuvSrcAp.selectXPath("./tuv[lower-case(@xml:lang)='" + super.currSrcLang.toLowerCase() + "']");
				while (ap.evalXPath() != -1) {
					subMonitor.worked(1);
					deleteFlag = -1;// init flag
					tuvCountAp.resetXPath();
					int childCount = (int) tuvCountAp.evalXPathToNumber();

					if (childCount == 0) {// 1、如果没有tuv节点,删除整个TU
						hasDirty = true;
						xm.remove(vn.getElementFragment());
						continue;
					}

					vn.push();// 开始导航到SRC——tuv
					boolean hasSrcTuv = false;
					tuvSrcAp.resetXPath();
					if (tuvSrcAp.evalXPath() != -1) {
						hasSrcTuv = true;
					}
					vn.pop();
					if (!hasSrcTuv) {
						continue;
					}

					if (1 == childCount) { // 2、有源节点，但是没有目标节点 ，删除整个TU
						deleteFlag = deleteTU;
					} else if (2 == childCount) {
						vn.push();
						String elementPureText = null;
						tuvTgtAp.resetXPath();
						if (tuvTgtAp.evalXPath() != -1) {
							String fullText = vu.getElementContent();
							elementPureText = ignoreTag ? TmxInnerTagParser.getInstance().getTmxPureText(fullText)
									: fullText;
						}
						if (null != elementPureText && elementPureText.trim().isEmpty()) { // fix bug
							deleteFlag = deleteTU;
						}
						vn.pop();
					} else if (3 <= childCount) {

						vn.push();
						String elementPureText = null;
						tuvTgtAp.resetXPath();
						if (tuvTgtAp.evalXPath() != -1) {
							String fullText = vu.getElementContent();
							elementPureText = ignoreTag ? TmxInnerTagParser.getInstance().getTmxPureText(fullText)
									: fullText;
						}
						/*
						 * if (null == elementPureText) { deleteFlag = deleteSrcTuv; } else if
						 * (elementPureText.trim().isEmpty()) { deleteFlag = deleteTgtTuv; }
						 */// fix bug
						if (null != elementPureText && elementPureText.trim().isEmpty()) {
							deleteFlag = deleteTgtTuv;
						}
						vn.pop();

					}

					// : delete tu by flag
					vn.push();
					if (deleteFlag == deleteTU) {
						xm.remove(vn.getElementFragment());
						hasDirty = true;
					} else if (deleteFlag == deleteTgtTuv) {
						tuvTgtAp.resetXPath();
						if (tuvTgtAp.evalXPath() != -1) {
							vn.toElement(VTDNav.PARENT);
							xm.remove(vn.getElementFragment());
							hasDirty = true;
						}

					} else if (deleteFlag == deleteSrcTuv) {
						tuvSrcAp.resetXPath();
						if (tuvSrcAp.evalXPath() != -1) {
							xm.remove(vn.getElementFragment());
							hasDirty = true;
						}
					}
					vn.pop();
				}

				save2SubFile(subFile, vu, xm);
				subMonitor.done();

			} catch (XPathParseException e) {
				e.printStackTrace();
			} catch (XPathEvalException e) {
				e.printStackTrace();
			} catch (NavException e) {
				e.printStackTrace();
			} catch (ModifyException e) {
				e.printStackTrace();
			} finally {
				subMonitor.done();
			}
		}
		monitor.done();
		return hasDirty;
	}

	/**
	 * 删除重复文本段 删除重复的TU，原文和译文相同。删除时如果TU中只有2个TUV，则直接删除TU；如果TU中有超过2个TUV则只删除当前TUV 保留最新的TUV
	 * @return ;
	 */
	public boolean deleteDupaicate(IProgressMonitor monitor, boolean ignoreTag, boolean ignoreCase) {
		monitor.beginTask("", 100);
		SubProgressMonitor subFilerJob = new SubProgressMonitor(monitor, 40);
		subFilerJob.setTaskName(Messages.getString("core.fileAccess.filterDupliacteSegment"));
		TmxFilterQueryUtil filterQuery = new TmxFilterQueryUtil(container, super.currSrcLang, super.currTgtLang);
		filterQuery.setIngoreTag(ignoreTag);
		filterQuery.setIgnoreCase(ignoreCase);
		List<String> filterResultList = filterQuery.getDuplicate4DeleteIds(subFilerJob);
		subFilerJob.done();
		if (filterResultList.size() == 0) {
			return false;
		}

		SubProgressMonitor subDeleteJob = new SubProgressMonitor(monitor, 60);
		subDeleteJob.setTaskName(Messages.getString("core.fileAccess.deleteDuplicateSegment"));
		deleteTus(filterResultList.toArray(new String[] {}), subDeleteJob);
		subDeleteJob.done();

		return true;
	}

	/**
	 * 删除相同原文不同译文的TU,删除时如果TU中只有2个TUV，则直接删除TU；如果TU中有超过2个TUV则只删除当前TUV
	 * @param ignoreTag
	 * @return ;
	 */

	public boolean deleteSameSrcDiffTgt(IProgressMonitor monitor, boolean ignoreTag, boolean ignoreCase) {
		monitor.beginTask("", 100);
		SubProgressMonitor subFilerJob = new SubProgressMonitor(monitor, 40);
		subFilerJob.setTaskName(Messages.getString("core.fileAccess.filterSameSrcDiffTgtSegment"));
		TmxFilterQueryUtil filterQuery = new TmxFilterQueryUtil(container, super.currSrcLang, super.currTgtLang);
		filterQuery.setIngoreTag(ignoreTag);
		filterQuery.setIgnoreCase(ignoreCase);
		List<String> filterResultList = filterQuery.getSrcSameButTgtDiff4DeleteIds(subFilerJob);
		subFilerJob.done();
		if (filterResultList.size() == 0) {
			return false;
		}

		SubProgressMonitor subDeleteJob = new SubProgressMonitor(monitor, 60);
		subDeleteJob.setTaskName(Messages.getString("core.fileAccess.deleteSameSrcDiffTgtSegment"));
		deleteTus(filterResultList.toArray(new String[] {}), subDeleteJob);
		subDeleteJob.done();

		return true;

	}

	/**
	 * 将TUid按照文件分类
	 * @param filterResultList
	 * @return ;
	 */
	public Map<String, List<String>> parseIdentiterIds(List<String> filterResultList) {
		Map<String, List<String>> subFileIds = new HashMap<String, List<String>>();
		for (String id : filterResultList) {
			String[] parseTuIndentifier = TeCoreUtils.parseTuIndentifier(id);
			if (subFileIds.get(parseTuIndentifier[0]) == null) {
				List<String> ids = new ArrayList<String>();
				ids.add(parseTuIndentifier[1]);
				subFileIds.put(parseTuIndentifier[0], ids);
			} else {
				subFileIds.get(parseTuIndentifier[0]).add(parseTuIndentifier[1]);
			}
		}
		return subFileIds;
	}

	public String generateNewTuHsId(VTDUtils vu) {
		// String hsid = "1";
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		try {
			ap.selectXPath("/tmx/body/tu");
			int max = 0;
			while (ap.evalXPath() != -1) {
				String hsid = vu.getCurrentElementAttribut("hsid", "1");
				int id = Integer.parseInt(hsid);
				if (id > max) {
					max = id;
				}
			}
			return max + 1 + "";
		} catch (VTDException e) {
			LOGGER.error("", e);
		}
		return "1";
	}

	/**
	 * :删除段末段首空格 (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#deleteEndsSpaces(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean deleteEndsSpaces(IProgressMonitor monitor) {
		batchTrimSegs(monitor);
		return false;
	}

	@Override
	public void beginQA(String srcLangCode, String tgtLangCode, boolean ignoreTag, boolean ignoreCase) {
		QATrigger trigger = new QATrigger();
		trigger.beginTMXQA(container, srcLangCode, tgtLangCode, tuIdentifiers);
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	private void filterTu(String srcSearchStr, String tgtSearchStr, String srcLang, String tgtLang,
			IProgressMonitor monitor) {
		List<String> tuIds = new ArrayList<String>();
		List<String> subFiles = container.getSubFiles();
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		String tuXpath = null;
		monitor.beginTask("", subFiles.size());
		try {
			if (langList.size() == 1 && (srcSearchStr == null || srcSearchStr.length() == 0)
					&& (tgtSearchStr == null || tgtSearchStr.length() == 0)) {
				// 加载所有的 TU ,不需要语言过滤。
				tuXpath = "./tu";
				for (String subFile : subFiles) {
					VTDUtils vu = container.getVTDUtils(subFile);
					VTDNav vn = vu.getVTDNav();
					vu.pilot("/tmx/body");
					ap.bind(vn);
					ap.selectXPath(tuXpath);
					monitor.worked(1);
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					while (ap.evalXPath() != -1) {
						String hsid = vu.getCurrentElementAttribut("hsid", null);
						if (hsid == null) {
							continue;
						}
						String id = subFile + TeCoreConstant.ID_MARK + hsid;
						tuIds.add(id);
					}
				}
				monitor.done();
				super.tuIdentifiers.clear();
				super.tuIdentifiers.addAll(tuIds);
				return;
			}
			tuXpath = "./descendant::tu[tuv[lower-case(@xml:lang)='" + srcLang.toLowerCase() + "'] "
					+ "and tuv[lower-case(@xml:lang)='" + tgtLang.toLowerCase() + "']]";
			for (String subFile : subFiles) {
				monitor.worked(1);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				VTDUtils vu = container.getVTDUtils(subFile);
				VTDNav vn = vu.getVTDNav();
				vu.pilot("/tmx/body");
				ap.bind(vn);
				ap.selectXPath(tuXpath);
				whiletu: while (ap.evalXPath() != -1) { // 循环 TU
					String hsid = vu.getCurrentElementAttribut("hsid", null);
					if (hsid == null) {
						continue whiletu;
					}
					vn.push();
					if (vn.toElement(VTDNav.FIRST_CHILD)) {
						String nodeName = vn.toRawString(vn.getCurrentIndex());
						if (nodeName.equalsIgnoreCase("tuv")) {
							String lang = vu.getCurrentElementAttribut("xml:lang", null);
							if (lang == null
									|| !checkSearchString(vu, lang, srcLang, tgtLang, srcSearchStr, tgtSearchStr)) {
								vn.pop();
								continue whiletu;
							}
						}
						while (vn.toElement(VTDNav.NEXT_SIBLING)) { // 循环 TU 所有子节点
							nodeName = vn.toRawString(vn.getCurrentIndex());
							if (nodeName.equalsIgnoreCase("tuv")) {
								String lang = vu.getCurrentElementAttribut("xml:lang", null);
								if (lang == null
										|| !checkSearchString(vu, lang, srcLang, tgtLang, srcSearchStr, tgtSearchStr)) {
									vn.pop();
									continue whiletu;
								}
							}
						}
					}
					vn.pop();
					String id = subFile + TeCoreConstant.ID_MARK + hsid;
					tuIds.add(id);
				}
			}
			monitor.done();
			super.tuIdentifiers.clear();
			super.tuIdentifiers.addAll(tuIds);
		} catch (VTDException e) {
			LOGGER.error("", e);
		}
	}

	private boolean checkSearchString(VTDUtils vu, String lang, String srcLang, String tgtLang, String srcSearchStr,
			String tgtSearchStr) throws VTDException {
		VTDNav vn = vu.getVTDNav();
		if (lang.equalsIgnoreCase(srcLang) && srcSearchStr != null && srcSearchStr.length() != 0) { // 源语言
			vn.push();
			String pureText = getSegPureText(vu);
			vn.pop();
			if (pureText != null && pureText.length() != 0) {
				pureText = pureText.replaceAll("&lt;", "<");
				pureText = pureText.replaceAll("&gt;", ">");
				pureText = pureText.replaceAll("&quot;", "\"");
				pureText = pureText.replaceAll("&amp;", "&");
				pureText = pureText.replaceAll(System.getProperty("line.separator"), "\n");
			}
			if (pureText == null || pureText.length() == 0 || pureText.indexOf(srcSearchStr) == -1) {
				return false;
			}
		} else if (lang.equalsIgnoreCase(tgtLang) && tgtSearchStr != null && tgtSearchStr.length() != 0) { // 目标语言
			vn.push();
			String pureText = getSegPureText(vu);
			vn.pop();
			if (pureText != null && pureText.length() != 0) {
				pureText = pureText.replaceAll("&lt;", "<");
				pureText = pureText.replaceAll("&gt;", ">");
				pureText = pureText.replaceAll("&quot;", "\"");
				pureText = pureText.replaceAll("&amp;", "&");
				pureText = pureText.replaceAll(System.getProperty("line.separator"), "\n");
			}
			if (pureText == null || pureText.length() == 0 || pureText.indexOf(tgtSearchStr) == -1) {
				return false;
			}
		}
		return true;
	}

	private String getSegPureText(VTDUtils vu) throws VTDException {
		VTDNav vn = vu.getVTDNav();
		String pureText = null;
		if (vn.toElement(VTDNav.FIRST_CHILD)) { // 取seg内容
			String nodeName = vn.toRawString(vn.getCurrentIndex());
			if (nodeName.equalsIgnoreCase("seg")) {
				pureText = TmxInnerTagParser.getInstance().getTmxPureText(vu.getElementContent());
			} else {
				vn.push();
				while (vn.toElement(VTDNav.NEXT_SIBLING)) {
					nodeName = vn.toRawString(vn.getCurrentIndex());
					if (nodeName.equalsIgnoreCase("seg")) {
						pureText = TmxInnerTagParser.getInstance().getTmxPureText(vu.getElementContent());
						break;
					}
				}
				vn.pop();
			}
		}
		return pureText;
	}

	public void updateTuPropType(java.util.Map<String,TmxTU> tus, TmxProp prop, String propType) {
		if (propType == null) {
			return;
		}
		propType = TextUtil.cleanSpecialString(propType);

		// key--file; value--hsid
		Map<String, List<String>> ids = new HashMap<String, List<String>>();
		// key--identifier; value--dbpk
		Map<String, Integer> propIds = new HashMap<String, Integer>();
		for (Entry<String, TmxTU> entry : tus.entrySet()) {
			TmxTU tu = entry.getValue();
			List<TmxProp> props = tu.getProps();
			if (props == null) {
				continue;
			}
			boolean shouldModify = false;
			for (TmxProp checkProp : props) {
				if (prop.getName().equals(checkProp.getName())) {// 是否需要更新
					propIds.put(entry.getKey(), checkProp.getDbPk());
					shouldModify = true;
					break;
				}
			}
			if (!shouldModify) {
				continue;
			}

			String[] strs = TeCoreUtils.parseTuIndentifier(entry.getKey());
			if (strs == null || strs.length < 2) {
				continue;
			}
			if (!ids.containsKey(strs[0])) {
				ids.put(strs[0], new LinkedList<String>());
			}
			ids.get(strs[0]).add(strs[1]);
		}

		for (Entry<String, List<String>> entry : ids.entrySet()) {
			// 子文件
			String subFile = entry.getKey();
			List<String> hsids = entry.getValue();

			VTDUtils vu = container.getVTDUtils(subFile);
			VTDNav vn = vu.getVTDNav();
			AutoPilot ap = new AutoPilot(vn);
			AutoPilot tmpAp = new AutoPilot(vn);
			XMLModifier xm = null;
			try {
				xm = new XMLModifier(vu.getVTDNav());
			} catch (ModifyException e) {
				LOGGER.error(e.getMessage(), e);
				continue;
			}

			try {
				ap.selectXPath("/tmx/body/tu");
				while (ap.evalXPath() != -1 && !hsids.isEmpty()) {
					String hsid = vn.toString(vn.getAttrVal("hsid"));
					if (hsid == null || !hsids.contains(hsid)) {
						continue;
					}
					vn.push();
					hsids.remove(hsid);
					tmpAp.selectXPath(new StringBuilder().append("./prop[@type='" + prop.getName() + "']/@type").toString());
					int index = -1;
					while ((index = tmpAp.evalXPath()) != -1) {
						xm.updateToken(index+1, propType);
						setDirty(true);
					}
					vn.pop();
					String identifier = new StringBuilder().append(subFile).append(TeCoreConstant.ID_MARK).append(hsid).toString();
					for (TmxProp rmp : tus.get(identifier).getProps()) {
						if (rmp.getName().equals(prop.getName())) {
							rmp.setName(propType);
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
			save2SubFile(subFile, vu, xm);
		}
	};
	public void batchUpdateTmxPropType(IProgressMonitor monitor, final TmxProp prop, String propType, final String filter) {

		if (propType == null) {
			return;
		}
		
		final String cleanPropType = propType == null ? null : TextUtil.cleanSpecialString(propType);

		batchModifyDocument(monitor, null, filter, null,new BatchModifyContext() {
			private String all_xpath = "/tmx/body/tu/prop[@type='" + prop.getName() + "']/@type";
			private String filter_xpath = "/tmx/body/tu[prop[@type=\"" + prop.getName() + "\"]]";
			@Override
			public String getBaseXpath() {
				if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {
					return all_xpath;
				}
				return filter_xpath;
			}

			@Override
			public void modifyAllItems(VTDUtils vu, XMLModifier xm) {
				try {
					xm.updateToken(vu.getVTDNav().getCurrentIndex() + 1, cleanPropType);
				} catch (Exception e) {
					LOGGER.error("", e);
				}
			}

			@Override
			public int modifyFilterItems(VTDUtils vu, XMLModifier xm, Set<String> set) {
				int hasmodify = 0;
				try {
					if (set.contains(vu.getCurrentElementAttribut("hsid", ""))) {// 检测是否在更新之列
						VTDNav vn = vu.getVTDNav();
						vn.push();
						AutoPilot ap = new AutoPilot(vn);
						String xpath = "./prop/@type";
						ap.selectXPath(xpath);
						int index = -1;
						while ((index = ap.evalXPath()) != -1) {
							xm.updateToken(index + 1, cleanPropType);
							hasmodify++;
						}
						vn.pop();
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
				return hasmodify;
			}
		});
	};
}
