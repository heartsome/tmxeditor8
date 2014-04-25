/**
 * Docx2Tmx.java
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

package net.heartsome.cat.te.core.converter.docx;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.util.LanguageUtils;
import net.heartsome.cat.te.core.bean.File2TmxConvertBean;
import net.heartsome.cat.te.core.bean.TmxTemplet;
import net.heartsome.cat.te.core.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

public class Docx2Tmx extends net.heartsome.cat.te.core.converter.AbstractFile2Tmx {

	public static Logger LOGGER = LoggerFactory.getLogger(Docx2Tmx.class.getName());
	private final String ns_w = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private int appendOffset = -1;
	private List<String> langs = new LinkedList<String>();

	public void doCovnerter(net.heartsome.cat.te.core.bean.File2TmxConvertBean bean, IProgressMonitor monitor)
			throws Exception {

		if (bean.appendExistTmxFilePath == null) {// 新生成文件
			if (bean.sourceFilePath == null || (!new File(bean.sourceFilePath).exists())) {
				throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));
			}
			this.newTmxFile(bean, monitor);
		} else {
			this.appendTmxFile(bean, monitor);
		}
	}

	/**
	 * 将 docx 文件转换成新的 tmx 文件
	 * @param bean
	 */
	private void newTmxFile(File2TmxConvertBean bean, IProgressMonitor monitor) throws Exception {

		BufferedWriter bwriter = null;
		try {
			bwriter = new BufferedWriter(new FileWriter(new File(bean.newTmxFilePath)));
		} catch (IOException e) {
			LOGGER.error("程序错误", e);
			throw e;
		}

		monitor.beginTask(Messages.getString("converter.docx2tmx.docx.parsedocx"), 100);
		monitor.setTaskName(Messages.getString("converter.docx2tmx.docx.parsedocx"));
		monitor.worked(1);

		VTDGen vg = new VTDGen();
		if (!vg.parseZIPFile(bean.sourceFilePath, "word/document.xml", true)) {
			File file = new File(bean.sourceFilePath);
			Exception e = new Exception(MessageFormat.format(Messages.getString("converter.docx2tmx.docx.error"),
					file.getName()));
			LOGGER.info(e.getMessage(), e);
			throw e;
		}
		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(9);

		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 90);
		subMonitor.beginTask("", 100);
		subMonitor.setTaskName(Messages.getString("converter.docx2tmx.docx.readdocx"));

		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace("w", ns_w);
		try {
			ap.selectXPath("/w:document/w:body/w:tbl/w:tr");// 逐行解析
			if (ap.evalXPath() != -1) {
				getLangs(bean, vn);// 第一行为语言代码
				valiteLangs(bean);
			} else {
				Exception e = new Exception(Messages.getString("converter.docx2tmx.docx.invaild"));
				LOGGER.info(e.getMessage());
				throw e;
			}
			String xmlDecl = TmxTemplet.genertateTmxXmlDeclar();
			bwriter.write(xmlDecl);
			TmxHeader header = TmxTemplet.generateTmxHeader(bean.srcLangCode, "unknown", "sentence",
					"Microsoft Word", null, null, null);
			bwriter.write("<tmx version=\"1.4\">\n");
			bwriter.write(TmxTemplet.header2Xml(header));
			bwriter.write("<body>\n");
			String tuContent;

			AutoPilot tmpAp = new AutoPilot(vn);
			tmpAp.declareXPathNameSpace("w", ns_w);
			tmpAp.selectXPath("count(/w:document/w:body/w:tbl/w:tr)");
			double total = tmpAp.evalXPathToNumber() - 1;
			int worked = 0;
			int count = 0;
			int tmp = 0;

			while (ap.evalXPath() != -1) {
				if (monitor.isCanceled()) {
					return;
				}
				tuContent = getTuFromTr(bean, vn);
				if (tuContent != null) {
					bwriter.write(tuContent);
				}
				tmp = (int) ((count++ / total) * 100);
				if (tmp > worked) {
					subMonitor.worked(tmp - worked);
					worked = tmp;
				}
			}
			subMonitor.done();
			bwriter.write("</body>\n</tmx>");
			bwriter.flush();
			monitor.done();
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("写入文件时出错", e);
		} finally {
			try {
				if (bwriter != null) {
					bwriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void valiteLangs(File2TmxConvertBean bean) throws Exception {
		for (int i = 0; i < langs.size(); i++) {
			if (!LocaleService.getDefaultLanguage().containsKey(LanguageUtils.convertLangCode(langs.get(i)))) {
				throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
			}
		}

		Set<String> testSet = new HashSet<String>();
		testSet.add(bean.srcLangCode);
		testSet.addAll(langs);
		if (testSet.size() != langs.size() + 1) {
			throw new Exception(Messages.getString("converter.common.vaild.duplicatelangcode.error"));
		}
	}

	/**
	 * 将 tmx 内容附加到指定文件中
	 * @param bean
	 * @param bos
	 * @param fos
	 * @throws Exception
	 */
	private void appendTmxFile(File2TmxConvertBean bean, IProgressMonitor monitor) throws Exception {

		BufferedOutputStream bos = null;
		monitor.beginTask(Messages.getString(""), 100);
		monitor.worked(1);
		monitor.setTaskName(Messages.getString("converter.docx2tmx.docx.testappend"));

		// 附加 tmx 文件的源语言
		String appendFileSrcLang = getSrclangAndTuOffset(bean.appendExistTmxFilePath);

		monitor.worked(4);
		// 解析 docx 的文件内容
		VTDGen vg = new VTDGen();
		if (!vg.parseZIPFile(bean.sourceFilePath, "word/document.xml", true)) {
			Exception e = new Exception(MessageFormat.format(Messages.getString("converter.docx2tmx.docx.error"),
					bean.sourceFilePath));
			LOGGER.error("", e);
			throw e;
		}
		monitor.worked(5);

		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 90);
		subMonitor.beginTask(Messages.getString("converter.docx2tmx.docx.readdocx"), 100);
		subMonitor.setTaskName(Messages.getString("converter.docx2tmx.docx.readdocx"));

		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main");
		try {
			ap.selectXPath("/w:document/w:body/w:tbl/w:tr");// 逐行解析
			if (ap.evalXPath() == -1) {
				Exception e = new Exception(Messages.getString("converter.docx2tmx.docx.invaild"));
				LOGGER.info(e.getMessage(), e);
				throw e;
			}
			getLangs(bean, vn);
			valiteLangs(bean);
			if (!appendFileSrcLang.equalsIgnoreCase("*all*")) {
				if (!appendFileSrcLang.equalsIgnoreCase(bean.srcLangCode)) {
					Exception e = new Exception(Messages.getString("converter.common.appendtmx.diffsrcLang.error"));
					LOGGER.error(e.getMessage(), e);
					throw e;
				}
			}
			try {
				FileOutputStream fos = new FileOutputStream(new File(bean.appendExistTmxFilePath), true);
				bos = new BufferedOutputStream(fos);
				FileChannel fc = fos.getChannel();
				fc.truncate(appendOffset);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
				throw e;
			}
			AutoPilot tmpAp = new AutoPilot(vn);
			tmpAp.declareXPathNameSpace("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main");
			tmpAp.selectXPath("count(/w:document/w:body/w:tbl/w:tr)");
			double total = tmpAp.evalXPathToNumber() - 1;
			int worked = 0;
			int count = 0;
			int tmp = 0;

			String tuContent;
			while (ap.evalXPath() != -1) {
				if (monitor.isCanceled()) {
					return;
				}
				tuContent = getTuFromTr(bean, vn);
				if (tuContent != null) {
					bos.write(tuContent.getBytes());
					bos.write('\r');
					bos.write('\n');
				}
				tmp = (int) ((count++ / total) * 100);
				if (tmp > worked) {
					subMonitor.worked(tmp - worked);
					worked = tmp;
				}
			}
			bos.write("</body></tmx>".getBytes());
			bos.flush();
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null) {
					bos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取源语言，计算 tmx 文档的 最后 tu 的偏移，以便附加新内容
	 * @param file
	 * @return
	 */
	private String getSrclangAndTuOffset(String file) throws Exception {
		// 检测追加文件的源语言
		String appendFileSrcLang = "";
		VTDGen vg = new VTDGen();
		if (!vg.parseFile(file, true)) {
			Exception e = new Exception(Messages.getString("converter.common.appendtmx.wrongTmx"));
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
		try {
			VTDNav vn = vg.getNav();
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/tmx/header/@srclang");
			if (ap.evalXPath() != -1) {
				appendFileSrcLang = vn.toString(vn.getCurrentIndex() + 1);
			} else {
				LOGGER.error(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileContentError"));
			}
			ap.resetXPath();
			ap.selectXPath("/tmx/body/tu[last()]");
			if (ap.evalXPath() != -1) {
				long l = vn.getElementFragment();
				int offset = (int) l;
				int length = (int) (l >> 32);
				appendOffset = offset + length;
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
		return appendFileSrcLang;
	}

	/**
	 * 解析 docx 中的源语言和目标语言，将其存入 bean 中
	 * @param bean
	 * @param vn
	 */
	private void getLangs(File2TmxConvertBean bean, VTDNav vn) throws Exception {
		vn.push();
		int testLangFlag = 0;

		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace("w", ns_w);
		try {
			ap.selectXPath("./w:tc");
			if (ap.evalXPath() != -1) {
				bean.srcLangCode = getTcPlainText(vn).trim();
				testLangFlag++;
			}
			while (ap.evalXPath() != -1) {
				String lang = getTcPlainText(vn).trim();
				langs.add(lang);
				testLangFlag++;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			vn.pop();
		}
		if (testLangFlag < 2) {
			Exception e = new Exception(Messages.getString("converter.docx2tmx.docx.invaild"));
			LOGGER.info(e.getMessage());
			throw e;
		}
	}

	/**
	 * 根据表格行（其中：第一列为源语言，第二列为目标语言）生成 tu code <br>
	 * <b>请注意，此方法只能生成双语言 tmx 文件，不能生成多语言 tmx</b>
	 * @param bean
	 * @return
	 * @throws Exception
	 */
	private String getTuFromTr(File2TmxConvertBean bean, VTDNav vn) throws Exception {
		vn.push();
		StringBuilder builder = new StringBuilder();
		AutoPilot ap = new AutoPilot(vn);
		ap.declareXPathNameSpace("w", ns_w);
		try {
			ap.selectXPath("./w:tc");

			builder.append("<tu ").append(">\n");
			addTuPropNode(builder, bean.customeAttr);
			// should we check the source is empty?
			if (ap.evalXPath() != -1) {
				builder.append("<tuv xml:lang=\"").append(bean.srcLangCode).append("\">\n");
				builder.append("<seg>").append(getTcPlainText(vn)).append("</seg>\n");
				builder.append("</tuv>\n");
			}
			int index = 0;
			while (ap.evalXPath() != -1 && index < langs.size()) {
				builder.append("<tuv xml:lang=\"").append(langs.get(index)).append("\">\n");
				builder.append("<seg>").append(getTcPlainText(vn)).append("</seg>\n");
				builder.append("</tuv>\n");
				index++;
			}
			builder.append("</tu>\n");
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		} finally {
			vn.pop();
		}
		return builder.toString();
	}

	/**
	 * 获取单元格（tc 节点）中的的纯文本内容（tc 中纯文本内容由&lt;w:t&gt;包裹）<br>
	 * 请注意，vn 必须已导航至 tc节点下
	 * @return
	 * @throws NavException
	 */
	private String getTcPlainText(VTDNav vn4documentXml) throws Exception {
		vn4documentXml.push();
		StringBuilder builder = new StringBuilder();
		AutoPilot ap = new AutoPilot(vn4documentXml);
		ap.declareXPathNameSpace("w", ns_w);
		try {
			ap.selectXPath(".//w:t/text()");
			while (ap.evalXPath() != -1) {
				builder.append(vn4documentXml.toRawString(vn4documentXml.getCurrentIndex()));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
		vn4documentXml.pop();
		return builder.toString();
	}

	/**
	 * 添加自定义属性
	 * @param builder
	 * @param map
	 */
	private void addTuPropNode(StringBuilder builder, Map<String, String> map) {
		if (map == null || map.size() == 0) {
			return;
		}
		for (Entry<String, String> entry : map.entrySet()) {
			builder.append("<prop type=\"").append(entry.getKey()).append("\">").append(entry.getValue())
					.append("</prop>\n");
		}
	}
}
