/**
 * Tmx2Docx.java
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.document.TmxReader;
import net.heartsome.cat.document.TmxReaderEvent;
import net.heartsome.cat.te.core.Activator;
import net.heartsome.cat.te.core.converter.AbstractTmx2File;
import net.heartsome.cat.te.core.resource.Messages;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

public class Tmx2Docx extends AbstractTmx2File {

	public static final String NAME = "docx";
	public static Logger LOGGER = LoggerFactory.getLogger(Tmx2Docx.class.getName());

	private String template = "";

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.AbstractTmx2File#doCovnerter(java.lang.String, java.io.File)
	 */
	public void doCovnerter(String tmxFile, File targetFile, IProgressMonitor monitor) throws Exception {

		// 1.解析 tmx
		monitor.beginTask(Messages.getString("converter.common.monitor.info.start"), 100);
		monitor.setTaskName(Messages.getString("converter.common.monitor.info.start"));
		monitor.worked(1);
		tmxReader = new TmxReader(new File(tmxFile));

		if (monitor.isCanceled()) {
			return;
		}

		monitor.worked(8);// done 1

		// 2. 加载模板
		monitor.setTaskName(Messages.getString("converter.common.monitor.info.docx.loadtemplate"));
		template = "";
		DocumentXml document = null;
		try {
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			URL url = bundle.getEntry("res/template.docx");
			template = FileLocator.toFileURL(url).getPath();
			document = new DocumentXml(template);// 读取模板 document.xml
		} catch (IOException e) {
			LOGGER.error(Messages.getString("converter.tmx2docx.template.notfound"), e);
			throw new Exception(Messages.getString("converter.tmx2docx.template.notfound"));
		}
		monitor.worked(1);// done 2

		List<String> langs = tmxReader.getLangs();// 获取语言
		DocxTable table = new DocxTable(langs, document);

		// 3. 处理 tmx 内容
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 90);
		// 计算步长
		double total = tmxReader.getTotalTu();
		subMonitor.beginTask("", 100);
		subMonitor.setTaskName(Messages.getString("converter.common.monitor.info.readtmx"));

		TmxReaderEvent event = null;
		double count = 0;
		int worked = 0;
		int tmp = 0;
		loop: while (true) {
			if (monitor.isCanceled()) {
				return;
			}
			event = tmxReader.read();
			switch (event.getState()) {
			case TmxReaderEvent.END_FILE:
				break loop;
			case TmxReaderEvent.NORMAL_READ:
				table.newRow(event.getTu());
				break;
			default:
				continue;
			}
			tmp = (int) ((count++ / total) * 100);
			if (worked < tmp) {
				subMonitor.worked(tmp - worked);
				worked = tmp;
			}
		}
		table.close();

		File documentFile = document.save();

		StyleXml style = new StyleXml(template);
		style.createTableStyle("a3");
		File stylesFile = style.save();
		subMonitor.done();// done 3

		// 4.保存
		monitor.setTaskName(Messages.getString("converter.common.monitor.info.saving"));
		Map<String, File> map = new HashMap<String, File>();
		map.put("word/document.xml", documentFile);
		map.put("word/styles.xml", stylesFile);
		// 保存文件时，文件可能无法读写，比如其他进程占用
		try {
			replaceZipEntry(template, targetFile, map);
		} catch (Exception e) {
			LOGGER.error("保存文件时，文件可能无法读写，比如其他进程占用", e);
			throw e;
		}
		monitor.worked(1);// done 4

		monitor.done();
	}

	/**
	 * 读取 docx 的 document.xml
	 * @author austen
	 */
	class DocumentXml {
		private String documentxmlTail = null;

		// 使用 VTD 操作document.xml
		private VTDNav vn;
		private AutoPilot ap;

		// 修改的 document.xml 文件存储到临时文件
		private File tmpFile;
		private BufferedWriter tmpFileWriter;

		/**
		 * 将 docx 模板中的 document.xml，复制到临时文件中<br>
		 * <b>注意</b><br>
		 * <li>为了防止对模板文件的损坏，所有的操作都是在临时文件中进行的</li> <li>并不是将 document.xml 里的所有内容都复制到临时文件中，只复制到&lt;w:body&gt;</li>
		 * @param template
		 */
		protected DocumentXml(String template) throws Exception {
			try {
				tmpFile = File.createTempFile("document", "xml");
				tmpFileWriter = new BufferedWriter(new FileWriter(tmpFile));
			} catch (IOException e) {
				LOGGER.error("无法创建临时文件", e);
				e.printStackTrace();
			}

			VTDGen vg = new VTDGen();
			if (!vg.parseZIPFile(template, "word/document.xml", true)) {
				LOGGER.error(Messages.getString("converter.tmx2docx.template.notfound"));
				throw new Exception(Messages.getString("converter.tmx2docx.template.notfound"));
			}
			vn = vg.getNav();
			ap = new AutoPilot(vn);
			ap.declareXPathNameSpace("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main");
			try {
				ap.selectXPath("/w:document/w:body");
				if (ap.evalXPath() != -1) {
					int offset = vn.getTokenOffset(vn.getCurrentIndex());
					tmpFileWriter.write(vn.toRawString(0, offset - 1));// document 头
					tmpFileWriter.write("<w:body>");// 所有内容都应存放在 body 节点下
				}
				long l = vn.getElementFragment();
				documentxmlTail = vn.toRawString((int) l, (int) (l >> 32)).replaceFirst("<w:body[\\s\\S]*?>", "");
			} catch (Exception e) {
				LOGGER.error(Messages.getString("converter.tmx2docx.template.notfound"), e);
				throw new Exception(Messages.getString("converter.tmx2docx.template.notfound"));
			}

		}

		/**
		 * 根据指定 xpath，向 document.xml 中写入内容，内容存放至 body 节点
		 * @param content
		 *            <i>e.g.</i>&lt;w:tbl&gt;&lt;w:tr&gt;&lt;/w:tr&gt;&lt;/w: tbl&gt;
		 * @param xpath
		 */
		protected DocumentXml write(String content) {
			try {
				tmpFileWriter.write(content);
			} catch (Exception e) {
				LOGGER.error("内部错误", e);
				e.printStackTrace();
			}
			return this;
		}

		/**
		 * 保存此 document.xml 文件
		 * @return 保存的文件
		 */
		protected File save() {
			try {
				tmpFileWriter.write(documentxmlTail);
				tmpFileWriter.write("</w:document>");// 关闭document
				tmpFileWriter.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (tmpFileWriter != null) {
					try {
						tmpFileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return tmpFile;
		}
	}

	/**
	 * 表格
	 * @author austen
	 */
	class DocxTable {

		private String firstColumnWidth;// 第一列宽
		private String langColumnWidth;// 平均语言列宽度

		private List<String> langList;// 所有语言代码，用作表头
		private DocumentXml document;
		private int firstColumnIndex = 0;// 如果目标语言是 *all*

		// 临时存储
		private Map<String, TmxSegement> segmap = new HashMap<String, TmxSegement>();

		protected DocxTable(List<String> langList, DocumentXml document) {
			this.langList = langList;
			this.document = document;
			if (langList.get(0).equalsIgnoreCase("*all*")) {
				firstColumnIndex = 1;
				int avg = 8522 / (langList.size() - 1);
				langColumnWidth = String.valueOf(avg);
				firstColumnWidth = String.valueOf(avg + 8522 % langList.size());
			} else {
				int avg = 8522 / langList.size();
				langColumnWidth = String.valueOf(avg);
				firstColumnWidth = String.valueOf(avg + 8522 % langList.size());
			}

			// 写表头
			String tablecode = "<w:tbl><w:tblPr><w:tblStyle w:val=\"a3\"/><w:tblW w:w=\"8522\" w:type=\"dxa\"/>"
					+ "<w:tblInd w:w=\"0\" w:type=\"dxa\"/>"
					+ "<w:tblLayout w:type=\"fixed\"/>"
					+ "<w:tblLook w:val=\"04A0\" w:firstRow=\"1\" w:lastRow=\"0\" w:firstColumn=\"1\" w:lastColumn=\"0\" w:noHBand=\"0\" w:noVBand=\"1\" /></w:tblPr>";
			document.write(tablecode);
			document.write("<w:tr>");

			String styledCode = "<w:rPr><w:b/></w:rPr>";
			TableColumn firstColumn = new TableColumn(document);
			firstColumn.setLayout(firstColumnWidth, "dxa");
			firstColumn.setStyleText(new StyleText(styledCode, langList.get(firstColumnIndex)));
			firstColumn.close();
			for (int i = firstColumnIndex + 1; i < langList.size(); i++) {
				TableColumn column = new TableColumn(document);
				column.setLayout(langColumnWidth, "dxa");
				column.setStyleText(new StyleText(styledCode, langList.get(i)));
				column.close();
			}
			document.write("</w:tr>");
		}

		public void close() {
			document.write("</w:tbl>");
		}

		protected void newRow(TmxTU tu) {
			segmap.clear();
			if (tu.getSource() != null && firstColumnIndex < 1) {// not *all*
				segmap.put(langList.get(0).toLowerCase(), tu.getSource());
			}
			if (tu.getSegments() != null) {
				for (TmxSegement seg : tu.getSegments()) {
					segmap.put(seg.getLangCode().toLowerCase(), seg);
				}
			}
			document.write("<w:tr>");// 打开行

			String langCode = null;
			// 第一行可能宽度略大
			langCode = langList.get(firstColumnIndex).toLowerCase();
			TableColumn firstColumn = new TableColumn(document);
			firstColumn.setLayout(firstColumnWidth, "dxa");
			firstColumn.setStyleText(new StyleText(null, segmap.containsKey(langCode) ? segmap.get(langCode)
					.getPureText() : ""));
			firstColumn.close();
			for (int i = firstColumnIndex + 1; i < langList.size(); i++) {// 语言列
				langCode = langList.get(i).toLowerCase();
				TableColumn column = new TableColumn(document);
				column.setLayout(langColumnWidth, "dxa");
				column.setStyleText(new StyleText(null, segmap.containsKey(langCode) ? segmap.get(langCode)
						.getPureText() : ""));
				column.close();
			}
			document.write("</w:tr>\n");// 关闭行
		}
	}

	class TableColumn {

		private DocumentXml document;

		TableColumn(DocumentXml document) {
			this.document = document;
			document.write("<w:tc>");
		}

		// 请扩展
		void setLayout(String width, String type) {
			document.write("<w:tcPr>").write("<w:tcW w:w=\"").write(width).write("\"").write(" w:type=\"").write(type)
					.write("\"").write(" />").write("</w:tcPr>");
			document.write("<w:p>");
		}

		void setStyleText(StyleText st) {
			document.write(st.toString());
		}

		void close() {
			document.write("</w:p>").write("</w:tc>");
		}
	}

	/**
	 * 带有样式的文本
	 */
	class StyleText {
		private String text;
		private String styleCode;

		StyleText() {
		}

		StyleText(String styleCode, String text) {
			this.text = text;
			this.styleCode = styleCode;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setStyleCode(String styleCode) {
			this.styleCode = styleCode;
		}

		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("<w:r>").append(styleCode == null ? "" : styleCode).append("<w:t>")
					.append(text == null ? "" : text).append("</w:t>").append("</w:r>");
			return builder.toString();
		}
	}

	/**
	 * 样式文档
	 * @author austen
	 */
	class StyleXml {

		VTDNav vn = null;
		AutoPilot ap = null;
		XMLModifier xm = null;

		public StyleXml(String template) throws Exception {
			VTDGen vg = new VTDGen();
			vg.parseZIPFile(template, "word/styles.xml", true);
			vn = vg.getNav();
			ap = new AutoPilot(vn);
			ap.declareXPathNameSpace("w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main");
			try {
				xm = new XMLModifier(vn);
			} catch (ModifyException e) {
				Exception e1 = new Exception(Messages.getString("converter.tmx2docx.template.notfound"));
				throw e1;
			}
		}

		/**
		 * 为表格追加样式
		 * @param styleId
		 *            样式 ID
		 */
		void createTableStyle(String styleId) {
			String style = new StringBuilder()
					.append("<w:style w:type=\"table\" w:styleId=\"")
					.append(styleId)
					.append("\"><w:name w:val=\"Table Grid\" /><w:basedOn w:val=\"a1\" /><w:uiPriority w:val=\"59\" /><w:tblPr><w:tblInd w:w=\"0\" w:type=\"pct\" /><w:tblBorders><w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\" /><w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\" /><w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\" /><w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\" /><w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\" /><w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\" /></w:tblBorders><w:tblCellMar><w:top w:w=\"0\" w:type=\"dxa\" /><w:left w:w=\"108\" w:type=\"dxa\" /><w:bottom w:w=\"0\" w:type=\"dxa\" /><w:right w:w=\"108\" w:type=\"dxa\" /></w:tblCellMar></w:tblPr></w:style>")
					.toString();
			try {
				ap.selectXPath("/w:styles");
				ap.evalXPath();
				xm.insertBeforeTail(style);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 获取存储的临时文件
		 * @return
		 */
		protected File save() {
			File tmpFile = null;
			try {
				tmpFile = File.createTempFile("styles", "xml");
				xm.output(tmpFile.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return tmpFile;
		}
	}

	/**
	 * zip 文件替换方法
	 * @param zipFilePath
	 *            zip文件路径
	 * @param savePath
	 *            另存路径为...
	 * @param replaceMap
	 *            替换文件表，key 存放替换路径，value 存放替换的文件
	 */
	protected void replaceZipEntry(String zipFilePath, File saveFile, Map<String, File> replaceMap) throws Exception {

		int buff_size = 1024;
		byte[] b = new byte[buff_size];
		ZipInputStream zis = null;
		ZipOutputStream zos = null;
		try {
			zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(new File(zipFilePath)), buff_size));
			zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(saveFile), buff_size));
			ZipEntry entry = null;
			int count = -1;
			while ((entry = zis.getNextEntry()) != null) {
				ZipEntry outEntry = new ZipEntry(entry.getName());
				zos.putNextEntry(outEntry);
				if (replaceMap.containsKey(entry.getName())) {
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(replaceMap.get(entry
							.getName())), buff_size);
					while ((count = bis.read(b, 0, buff_size)) != -1) {
						zos.write(b, 0, count);
					}
				} else {
					while ((count = zis.read(b, 0, buff_size)) != -1) {
						zos.write(b, 0, count);
					}
				}
				zos.closeEntry();
				zis.closeEntry();
			}
			zos.flush();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (zis != null)
					zis.close();
				if (zos != null)
					zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
