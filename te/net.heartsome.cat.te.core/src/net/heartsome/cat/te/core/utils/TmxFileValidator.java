/**
 * TmxFileValidator.java
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
package net.heartsome.cat.te.core.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.te.core.bean.TmxTemplet;
import net.heartsome.cat.te.core.resource.Messages;
import net.heartsome.cat.te.core.utils.TmxScanner.ErrorDescription;
import net.heartsome.xml.vtdimpl.EmptyFileException;
import net.heartsome.xml.vtdimpl.VTDLoader;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * TMX文件验证
 * @author robert 2012-03-14
 * @version
 * @since JDK1.6
 */
public class TmxFileValidator implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(TmxFileValidator.class);

	private String split = "--------------------------------------------------------------";

	private Map<String, VTDNav> vnMap = new HashMap<String, VTDNav>();
	private Color red;
	/** 当前处理TMX文件的版本号 */
	private String version;
	private Hashtable<String, String> languages;
	private Hashtable<String, String> countries;

	private Hashtable<String, String> tuids;
	private int balance;
	private Hashtable<String, String> ids;

	private StyledText styledText;
	private ProgressBar progressBar;

	private String tmxLocation;

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public TmxFileValidator(String tmxLocation, Shell shell) {
		red = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	}

	private void printWarning(final String warn) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				styledText.append(warn);
			}
		});
	}

	private void printlnWarning(String warn) {
		printWarning(warn + '\n');
	}

	private void printError(final String error) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				StyleRange range = new StyleRange(styledText.getText().length(), error.length(), red, null);
				styledText.append(error);
				styledText.setStyleRange(range);
			}
		});
	}

	private void printlnError(String error) {
		printError(error + '\n');
	}

	private void printInfo(final String info) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				styledText.append(info);
			}
		});
	}

	private void printlnInfo(String info) {
		printInfo(info + '\n');
	}

	public void validate() {
		boolean output = false;
		boolean repair = false;
		File file = new File(tmxLocation);
		VTDGen vg = null;
		VTDNav vn = null;
		AutoPilot ap = null;
		XMLModifier xm = null;
		try {
			String encoding = FileEncodingDetector.detectFileEncoding(file);
			try {
				vg = VTDLoader.loadVTDGen(file, encoding);
			} catch (Exception e) {
				try {
					vg = repair();
					repair = true;
				} catch (IOException e1) {
					LOGGER.error(e1.getMessage(), e1);
					printError(Messages.getString("tmxeditor.tmxFileValidator.autofix.failure"));
					return;
				}
			}
			if (vg == null) {
				return;
			}

			vn = vg.getNav();
			ap = new AutoPilot(vn);
			xm = new XMLModifier(vn);
			
			// 规范性验证
			printlnInfo(split);
			printlnInfo(MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.standard.msg"), tmxLocation));

			// 加载语言与国家
			languages = TextUtil.plugin_loadLanguages();
			countries = TextUtil.plugin_loadCoutries();

			// 验证一级
			ap.selectXPath("/tmx/*");
			
			// 验证 header
			ap.selectXPath("/tmx/header");
			if (ap.evalXPath() == -1) {
				printError(Messages.getString("tmxeditor.tmxFileValidator.autofix.failure"));
				return;
			}
			
			// 验证 body
			ap.selectXPath("count(/tmx/body)");
			if (ap.evalXPathToNumber() != 1) {
				printError(Messages.getString("tmxeditor.tmxFileValidator.autofix.failure"));
				return;
			}
			
			// 验证 tu
			ap.selectXPath("/tmx/body/tu");
			while (ap.evalXPath() != -1) {
				
			}
			
			// 验证 body
			
			
			// 创建临时文件
			// String tempLocation = tmpFile.getAbsolutePath();
			// styledText.append(Messages.getString("tmxeditor.tmxFileValidator.useDtdValidate"));

			// if (!parseXML(tempLocation)) {
			// String parseErrorTip = MessageFormat.format(
			// Messages.getString(Messages.getString("tmxeditor.tmxFileValidator.parseTmxTempleFileFaild")),
			// tempLocation);
			// StyleRange range = new StyleRange(styledText.getText().length(), parseErrorTip.length(), red, null);
			// styledText.append(parseErrorTip);
			// styledText.setStyleRange(range);
			// return;
			// }
			// styledText.append(Messages.getString("tmxeditor.tmxFileValidator.validingTmxFile"));


			// 判断源语言
			// VTDNav vn = vnMap.get(tmxLocation);
			// AutoPilot ap = new AutoPilot(vn);
			// XMLModifier xm = new XMLModifier(vn);

			// ap.selectXPath("/tmx/header");
			// if (ap.evalXPath() == -1) {// no header
			// // write default
			// } else {
			// // header Required attributes: creationtool, creationtoolversion, segtype, o-tmf, adminlang, srclang,
			// // datatype.
			// String[] requiredAttrs = new String[] { "creationtool", "creationtoolversion", "segtype", "o-tmf",
			// "adminlang", "srclang", "datatype" };
			// AutoPilot tmp = new AutoPilot(vn);
			// for (String attr : requiredAttrs) {
			// vn.push();
			// tmp.selectXPath("./@" + attr);
			// if (tmp.evalXPath() == -1) {
			// String tmpAttr = " " + attr + "=\"" + defaultAttrValue(attr) + "\"";
			// xm.insertAttribute(tmpAttr);
			// printWarning(MessageFormat.format(
			// Messages.getString("tmxeditor.tmxFileValidator.standard.missReqAttr"), "header", attr)
			// + " ,");
			// printlnWarning(Messages.getString("tmxeditor.tmxFileValidator.autofix") + tmpAttr);
			// } else {
			// String tmpAttrValue = vn.toRawString(vn.getAttrVal(attr));
			// if (!verifyAttrValue(attr, tmpAttrValue)) {
			// printWarning(MessageFormat
			// .format(Messages.getString("tmxeditor.tmxFileValidator.standard.errlangcode"),
			// tmpAttrValue));
			// printWarning(", ");
			// printlnWarning(Messages.getString("tmxeditor.tmxFileValidator.autofix")
			// + defaultAttrValue(attr));
			// }
			// }
			// vn.pop();
			// }
			// }
			//
			// // String srcLanguage = getAttribute(tmxLocation, "/tmx/header/@srclang", null);
			// // if (srcLanguage == null) {
			// // styledText.append(Messages.getString("tmxeditor.tmxFileValidator.repareSrcLang"));
			// //
			// // }
			// // // if (!"*all*".equals(srcLanguage) && !checkLang(srcLanguage)) {
			// // // throw new Exception(MessageFormat.format("错误：源语言 {0} 不正确。", srcLanguage));
			// // // }
			// //
			// // else {
			// // styledText.append(MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.srcLang"),
			// // srcLanguage));
			// // }
			//			//			if (!srcLanguage.equals("*all*")) { //$NON-NLS-1$
			// // validSrcLanguage(tmxLocation, srcLanguage);
			// // }
			// //
			// // tuids = new Hashtable<String, String>();
			// // recurse(tmxLocation);
			//
			 printlnInfo(split);
			 printlnInfo(Messages.getString("tmxeditor.tmxFileValidator.validatePassed"));

		} catch (Exception e) {
			LOGGER.error("", e);
			e.printStackTrace();
			String errorTip = e.getMessage();
			printlnError(errorTip);
		}
	}

	private VTDGen repair() throws IOException {
		// 文档错误
		printlnInfo(split);
		printlnWarning(MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.parseError.msg"),
				tmxLocation));
		TmxScanner scanner = new TmxScanner(tmxLocation);
		scanner.setProgressBar(progressBar);
		StringBuilder error = null;

		while (!scanner.isFinish()) {
			if (scanner.scanTmxDocument() == null) {
				for (ErrorDescription errordes : scanner.errorFIFO) {
					error = new StringBuilder();
					error.append("[ line : ").append(errordes.lineNumber).append(" ]").append(errordes.description);
					printlnError(error.toString());
				}
			}
		}
		tmxLocation = scanner.scanTmxDocument();

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				progressBar.setSelection(100);
			}
		});
		try {
			File file = new File(tmxLocation);
			if (file.exists()) {
				return VTDLoader.loadVTDGen(file, FileEncodingDetector.detectFileEncoding(new File(tmxLocation)));
			}
			return null;
		} catch (Exception e) {
			scanner.clear();
			printError(Messages.getString("tmxeditor.tmxFileValidator.autofix.failure"));
			return null;
		}
	}

	private boolean verifyAttrValue(String attr, String rawString) {
		if (attr.equals("srclang")) {
			// 是否该检测？
		}
		return true;
	}

	private String defaultAttrValue(String attr) {
		return "Undefined";
	}

	/**
	 * 解析TMX文件 ;
	 */
	private boolean parseXML(String xmlLocation) {
		VTDGen vg = new VTDGen();
		boolean result = vg.parseFile(xmlLocation, true);
		if (result) {
			VTDNav vtdNav = vg.getNav();
			vnMap.put(xmlLocation, vtdNav);
		}
		return result;
	}

	/**
	 * 通过验证指定文件的头元素是否是TMX，来验证该文件是不是一个TMX文件
	 * @return
	 * @throws Exception
	 *             ;
	 */
	private boolean validTmxRoot(String tmxLocation) throws Exception {
		VTDNav vn = vnMap.get(tmxLocation);
		AutoPilot ap = new AutoPilot(vn);
		Assert.isNotNull(vn,
				MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.canntFindFile"), tmxLocation));
		ap.selectXPath("/tmx");

		if (ap.evalXPath() != -1) {
			return true;
		}
		return false;
	}

	/**
	 * 获取属性值
	 * @param xpath
	 * @param defaultValue
	 * @return
	 * @throws Exception
	 *             ;
	 */
	private String getAttribute(String tmxLocation, String xpath, String defaultValue) throws Exception {
		VTDNav vn = vnMap.get(tmxLocation);
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		Assert.isNotNull(vn,
				MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.canntFindFile"), tmxLocation));
		ap.selectXPath(xpath);

		int index;
		if ((index = ap.evalXPath()) != -1) {
			return vn.toString(index + 1);
		}
		vn.pop();
		return defaultValue;
	}

	/**
	 * 获取属性值
	 * @param xpath
	 * @param defaultValue
	 * @return
	 * @throws Exception
	 *             ;
	 */
	private String getAttribute(VTDNav vn, String attriName, String defaultValue) throws Exception {
		vn.push();
		int index;
		if ((index = vn.getAttrVal(attriName)) != -1) {
			return vn.toString(index);
		}
		vn.pop();
		return defaultValue;
	}

	/**
	 * 创建一个用于编辑的临时TMX文件
	 */
	public File createTmpFile(String tmxLocation) throws Exception {
		File tmpFile = null;
		File folder = null;
		File curFolder = new File(".");

		if (TeCoreUtils.OS_WINDOWS == TeCoreUtils.getCurrentOS()) {
			folder = new File(curFolder.getAbsoluteFile() + TeCoreUtils.getFileSeparator() + "~$temp");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			folder.deleteOnExit();
			String sets = "attrib +H \"" + folder.getAbsolutePath() + "\"";
			// 运行命令串
			Runtime.getRuntime().exec(sets);
		} else {
			folder = new File(curFolder.getAbsoluteFile() + TeCoreUtils.getFileSeparator() + ".temp");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			folder.deleteOnExit();
		}

		tmpFile = File.createTempFile("tmp", ".TMX", folder);
		tmpFile.deleteOnExit();

		VTDNav vn = vnMap.get(tmxLocation);
		Assert.isNotNull(vn,
				MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.canntFindFile"), tmxLocation));
		XMLModifier xm = new XMLModifier(vn);
		save(xm, tmpFile);
		return tmpFile;
	}

	/**
	 * 循环每一个tu节点，进一步判断源语言
	 * @param tmxLocation
	 */
	private void validSrcLanguage(String tmxLocation, String srcLanguage) throws Exception {
		VTDNav vn = vnMap.get(tmxLocation);
		Assert.isNotNull(vn,
				MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.canntFindFile"), tmxLocation));
		AutoPilot ap = new AutoPilot(vn);
		AutoPilot tuvAp = new AutoPilot(vn);
		ap.selectXPath("/tmx/body/tu");

		int index;
		while (ap.evalXPath() != -1) {
			boolean found = false;
			vn.push();
			tuvAp.selectXPath("./tuv");
			while (tuvAp.evalXPath() != -1) {
				String lang = "";
				if ((index = vn.getAttrVal("xml:lang")) != -1) {
					lang = vn.toString(index);
				}
				if ("".equals(lang)) {
					if (version.equals("1.1") || version.equals("1.2")) {
						if ((index = vn.getAttrVal("lang")) != -1) { //$NON-NLS-1$
							lang = vn.toString(index);
						}
					} else {
						throw new Exception(Messages.getString("tmxeditor.tmxFileValidator.tuvLangError"));
					}
				}
				if (lang.equals("")) {
					throw new Exception(Messages.getString("tmxeditor.tmxFileValidator.tuvLangError"));
				}

				if (lang.equals(srcLanguage)) {
					found = true;
				}
			}
			if (!found) {
				throw new Exception(MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.lacktuLang"),
						srcLanguage));
			}
			vn.pop();
		}
	}

	/**
	 * 验证所有节点的属性与seg节点的内容
	 * @param tmxLocation
	 * @throws Exception
	 *             ;
	 */
	private void recurse(String tmxLocation) throws Exception {
		VTDNav vn = vnMap.get(tmxLocation);
		Assert.isNotNull(vn,
				MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.canntFindFile"), tmxLocation));
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("/tmx//*");

		VTDUtils vu = new VTDUtils(vn);
		Map<String, String> attributesMap;

		// 先验证所有的属性值
		while (ap.evalXPath() != -1) {
			attributesMap = getAllAttributes(vn);
			checkAttribute(attributesMap, vu);
		}

		// 再验证seg节点下的内容
		ap.resetXPath();
		ap.selectXPath("/tmx/body/tu/tuv/seg");
		while (ap.evalXPath() != -1) {
			balance = 0;
			ids = null;
			ids = new Hashtable<String, String>();
			String segFrag = vu.getElementFragment();

			vn.push();
			AutoPilot segChildAP = new AutoPilot(vn);
			segChildAP.selectXPath("./*");
			while (segChildAP.evalXPath() != -1) {
				String nodeName = vu.getCurrentElementName();
				System.out.println("nodeName = " + nodeName);
				if ("bpt".equals(nodeName)) { //$NON-NLS-1$
					balance += 1;
					if (version.equals("1.4")) { //$NON-NLS-1$
						String s = getAttribute(vn, "i", ""); //$NON-NLS-1$
						if (!ids.containsKey(s)) {
							ids.put(s, "1");
						} else {
							if (ids.get(s).equals("-1")) {
								ids.put(s, "0");
							} else {
								throw new Exception(
										Messages.getString("tmxeditor.tmxFileValidator.error.duplicateElement_type_1")
												+ "\n" + segFrag + "\n");
							}
						}
					}
				}

				if ("ept".equals(nodeName)) { //$NON-NLS-1$
					balance -= 1;
					if (version.equals("1.4")) { //$NON-NLS-1$
						String s = getAttribute(vn, "i", ""); //$NON-NLS-1$
						if (!ids.containsKey(s)) {
							ids.put(s, "-1");
						} else {
							if (ids.get(s).equals("1")) {
								ids.put(s, "0");
							} else {
								throw new Exception(
										Messages.getString("tmxeditor.tmxFileValidator.error.duplicateElement_type_2")
												+ "\n" + segFrag + "\n");
							}
						}
					}
				}
			}

			vn.pop();

			if (balance != 0) {
				throw new Exception(Messages.getString("tmxeditor.tmxFileValidator.error.elementcountWrong") + "\n"
						+ vu.getElementFragment() + "\n");
			}
			if (ids.size() > 0) {
				@SuppressWarnings("rawtypes")
				Enumeration en = ids.keys();
				while (en.hasMoreElements()) {
					if (!ids.get(en.nextElement()).equals("0")) { //$NON-NLS-1$
						throw new Exception(Messages.getString("tmxeditor.tmxFileValidator.error.elementNotMacth")
								+ "\n" + vu.getElementFragment() + "\n");
					}
				}
			}
		}
	}

	private void checkAttribute(Map<String, String> attributesMap, VTDUtils vu) throws Exception {
		Iterator<Entry<String, String>> it = attributesMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			String name = entry.getKey();
			String value = entry.getValue();

			if (name.equals("lang") || name.equals("adminlang") || name.equals("xml:lang")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (!checkLang(value)) {
					throw new Exception(MessageFormat.format(
							Messages.getString("tmxeditor.tmxFileValidator.error.WrongLangCode"), value)
							+ "\n" + vu.getElementFragment() + "\n");
				}
			}
			if (name.equals("lastusagedate") || name.equals("changedate") || name.equals("creationdate")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (!checkDate(value)) {
					throw new Exception(MessageFormat.format(
							Messages.getString("tmxeditor.tmxFileValidator.error.wrongdateformat"), value)
							+ "\n" + vu.getElementFragment() + "\n");
				}
			}
			if (name.equals("tuid")) { //$NON-NLS-1$
				if (!tuids.containsKey(value)) {
					tuids.put(value, "");
				} else {
					throw new Exception(MessageFormat.format(
							Messages.getString("tmxeditor.tmxFileValidator.error.duplicateTuid"), value)
							+ "\n" + vu.getElementFragment() + "\n");
				}
			}
		}
	}

	/**
	 * 获取所有属性
	 * @param vn
	 * @return
	 * @throws Exception
	 *             ;
	 */
	private Map<String, String> getAllAttributes(VTDNav vn) throws Exception {
		vn.push();
		AutoPilot apAttributes = new AutoPilot(vn);
		Map<String, String> attributes = new HashMap<String, String>();
		apAttributes.selectXPath("./@*");
		int inx = -1;
		while ((inx = apAttributes.evalXPath()) != -1) {
			String name = vn.toString(inx);
			inx = vn.getAttrVal(name);
			String value = inx != -1 ? vn.toString(inx) : "";
			attributes.put(name, value);

		}
		vn.pop();
		return attributes;
	}

	private boolean save(XMLModifier xm, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			xm.output(bos); // 写入文件
			bos.close();
			fos.close();
			return true;
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return false;
	}

	private boolean checkDate(String date) {
		// YYYYMMDDThhmmssZ
		if (date.length() != 16) {
			return false;
		}
		if (date.charAt(8) != 'T') {
			return false;
		}
		if (date.charAt(15) != 'Z') {
			return false;
		}
		try {
			int year = Integer.parseInt("" + date.charAt(0) + date.charAt(1) + date.charAt(2) + date.charAt(3)); //$NON-NLS-1$
			if (year < 0) {
				return false;
			}
			int month = Integer.parseInt("" + date.charAt(4) + date.charAt(5)); //$NON-NLS-1$
			if (month < 1 || month > 12) {
				return false;
			}
			int day = Integer.parseInt("" + date.charAt(6) + date.charAt(7)); //$NON-NLS-1$
			switch (month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				if (day < 1 || day > 31) {
					return false;
				}
				break;
			case 4:
			case 6:
			case 9:
			case 11:
				if (day < 1 || day > 30) {
					return false;
				}
				break;
			case 2:
				// check for leap years
				if (year % 4 == 0) {
					if (year % 100 == 0) {
						// not all centuries are leap years
						if (year % 400 == 0) {
							if (day < 1 || day > 29) {
								return false;
							}
						} else {
							// not leap year
							if (day < 1 || day > 28) {
								return false;
							}
						}
					}
					if (day < 1 || day > 29) {
						return false;
					}
				} else if (day < 1 || day > 28) {
					return false;
				}
			}
			int hour = Integer.parseInt("" + date.charAt(9) + date.charAt(10)); //$NON-NLS-1$
			if (hour < 0 || hour > 23) {
				return false;
			}
			int min = Integer.parseInt("" + date.charAt(11) + date.charAt(12)); //$NON-NLS-1$
			if (min < 0 || min > 59) {
				return false;
			}
			int sec = Integer.parseInt("" + date.charAt(13) + date.charAt(14)); //$NON-NLS-1$
			if (sec < 0 || sec > 59) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private boolean checkLang(String lang) {
		if (lang.startsWith("x-") || lang.startsWith("X-")) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}

		// Accepted formats are:
		// xx: ISO 639-1
		// xxx: ISO 639-2
		// xx-YY: ISO-639-1 + ISO3166-1
		// xxx-YY: ISO-639-2 + ISO3166-1

		int len = lang.length();
		if (len != 2 && len != 3 && len != 5 && len != 6) {
			return false;
		}
		if (!isAlpha(lang.charAt(0)) || !isAlpha(lang.charAt(1))) {
			return false;
		}
		if (len == 5 && lang.charAt(2) != '-') {
			return false;
		}
		if (len == 5 && (!isAlpha(lang.charAt(3)) || !isAlpha(lang.charAt(4)))) {
			return false;
		}
		if (len == 6 && lang.charAt(3) != '-') {
			return false;
		}
		if (len == 6 && (!isAlpha(lang.charAt(2)) || !isAlpha(lang.charAt(4)) || !isAlpha(lang.charAt(5)))) {
			return false;
		}
		String[] parts = lang.split("-"); //$NON-NLS-1$
		if (!languages.containsKey(parts[0].toLowerCase())) {
			return false;
		}
		if (parts.length == 2) {
			if (!countries.containsKey(parts[1].toUpperCase())) {
				return false;
			}
		}
		return true;
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	/**
	 * 简单测试编码
	 * @param fileName
	 * @return ;
	 */
	private String getXMLEncoding(String fileName) {
		// return UTF-8 as default
		String result = "UTF-8"; //$NON-NLS-1$
		try {
			// check if there is a BOM (byte order mark)
			// at the start of the document
			FileInputStream inputStream = new FileInputStream(fileName);
			byte[] array = new byte[2];
			inputStream.read(array);
			inputStream.close();
			byte[] lt = "<".getBytes(); //$NON-NLS-1$
			byte[] feff = { -1, -2 };
			byte[] fffe = { -2, -1 };
			if (array[0] != lt[0]) {
				// there is a BOM, now check the order
				if (array[0] == fffe[0] && array[1] == fffe[1]) {
					return "UTF-16BE"; //$NON-NLS-1$
				}
				if (array[0] == feff[0] && array[1] == feff[1]) {
					return "UTF-16LE"; //$NON-NLS-1$
				}
			}
			// check declared encoding
			FileReader input = new FileReader(fileName);
			BufferedReader buffer = new BufferedReader(input);
			String line = buffer.readLine();
			input.close();
			if (line.startsWith("<?")) { //$NON-NLS-1$
				line = line.substring(2, line.indexOf("?>")); //$NON-NLS-1$
				line = line.replaceAll("\'", "\""); //$NON-NLS-1$ //$NON-NLS-2$
				StringTokenizer tokenizer = new StringTokenizer(line);
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if (token.startsWith("encoding")) { //$NON-NLS-1$
						result = token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return result;
	}

	public StyledText getStyledText() {
		return styledText;
	}

	public void setStyledText(StyledText styledText) {
		this.styledText = styledText;
	}

	public String getTmxLocation() {
		return tmxLocation;
	}

	public void setTmxLocation(String tmxLocation) {
		this.tmxLocation = tmxLocation;
	}

	@Override
	public void run() {
		validate();
	}
}
