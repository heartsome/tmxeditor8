/**
 * CorrectWriter.java
 *
 * Version information :
 *
 * Date:2013-12-18
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.utils.tmxvalidator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.te.core.bean.TmxTemplet;
import net.heartsome.cat.te.core.resource.Messages;

import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;

/**
 * @author Austen
 * @version
 * @since JDK1.6
 */
public class CorrectWriter {

	private boolean correctEncoding;
	private String encoding = "UTF-8";
	private String location = null;
	private BufferedWriter bw = null;
	private ElementStack stack = new ElementStack();
	private TmxSchema schema = new TmxSchema();
	private Record record = new Record();

	public CorrectWriter(String filePath) throws IOException {
		File file = new File(filePath);
		location = filePath;
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
	}

	public void write(String str) throws IOException {
		bw.write(str);
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void close() throws IOException {
		if (bw != null) {
			bw.append("\n</body>");
			bw.append("\n</tmx>");
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			record.changed = false;
		}

	}

	public void setCorrectEncoding(boolean correctEncoding) {
		this.correctEncoding = correctEncoding;
	}

	public boolean isCorrectEncoding() {
		return correctEncoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public void writeXmlDecl(String version, String encoding, String stanlone) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append("<?xml version=\"").append(version).append("\" encoding=\"").append(encoding == null ? this.encoding : encoding.toUpperCase())
				.append("\"?>\n");
		bw.write(builder.toString());
		record.xmldecl = true;
	}

	public void writeDoctype(String root, String publicid, String systemid) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append("<!DOCTYPE ").append(root).append(' ');
		if (publicid != null) {
			builder.append("PUBLIC \"").append(publicid).append("\" \"").append(systemid).append("\"");
		} else {
			builder.append("SYSTEM \"").append(systemid).append("\"");
		}
		builder.append('>');
		builder.append('\n');
		bw.write(builder.toString());
		record.doctype = true;
	}

	/**
	 * 跳过错误的 tu
	 * @param qname
	 * @param attributes
	 * @throws IOException
	 *             ;
	 * @throws RepairableException 
	 */
	public void writeStartElement(QName qname, XMLAttributesImpl attributes) throws IOException, RepairableException {

		// build XML string
		String xmlStr = buildStartElementXmlStr(qname, attributes);
		
		// if root
		if (qname.rawname.equals("tmx")) {
			if (!record.root) {
				// before append root
				checkDoctype();
				
				stack.pushElement(qname);
				bw.append(xmlStr);
				bw.append("\n");
				record.root = true;
				return;
			} else {
				throw new RepairableException(Messages.getString("tmxeditor.tmxFileValidator.autofix.unknown"));
			}
		}

		// push header
		if (qname.rawname.equals("header")) {
			if (!record.header) {
				// before append header
				checkRoot();
				stack.pushElement(qname);
				bw.append(xmlStr);
				record.header = true;
				return;
			} else {
				throw new RepairableException(Messages.getString("tmxeditor.tmxFileValidator.autofix.unknown"));
			}
		}

		// push body
		if (qname.rawname.equals("body")) {
			if (!record.body) {
				// before append body
				checkHeader();
				
				stack.pushElement(qname);
				bw.append(xmlStr);
				record.body = true;
				return;
			} else {
				throw new RepairableException(Messages.getString("tmxeditor.tmxFileValidator.autofix.unknown"));
			}
		}
		
		// check if tu node set
		if ("tu".equals(qname.rawname)) {// 下个 tu
			if (record.tuset) {
				// before append tu set
				checkBody();
				
				record.xmls.clear();
				record.tuset = true;
				record.xmls.append(xmlStr);
				stack.pushElement(qname);
				// if root element lost!
				if (stack.fSize == 0) {
					QName tmxq = new QName();
					tmxq.localpart = "tmx";
					tmxq.prefix = "";
					tmxq.rawname = "tmx";
					tmxq.uri = "";
					stack.pushElement(tmxq);
				}
			} else {// 介个tu
				QName tq = new QName();
				stack.lastElement(tq);
				record.tuset = !schema.isInvalid(qname) && schema.isElementAccept(tq, qname);
				record.xmls.append(xmlStr);
				stack.pushElement(qname);
			}
		}
		
		if (stack.fSize != 0) {
			QName lqn = new QName();
			stack.lastElement(lqn);
			if (schema.isElementAccept(lqn, qname)) {
				record.xmls.append(xmlStr);
				stack.pushElement(qname);
			}
		}
		
	}
	
	public void writeEndElement(QName qname) throws IOException, RepairableException {
		
		if (stack.fSize == 0) {
			return;
		}
		
		if (record.body && !record.tuset) {
			return;
		}
		
		// 是否合法
		if (schema.isInvalidElement(qname)) {
			record.tuset = false;
			record.xmls.clear();
			return;
		}

		// 是否配对
		QName tq = new QName();
		stack.lastElement(tq);
		if (qname.rawname.equals(tq.rawname)) {
			// build XML string
			StringBuilder builder = new StringBuilder();
			builder.append("</").append(qname.rawname).append(">\n");
			record.xmls.append(builder.toString());
			stack.popElement(tq);
		} else {
			record.tuset = false;
			record.xmls.clear();
		}

		// ok flush this tu
		if (tq.rawname.equals("tu") && record.tuset) {
			record.countTu++;
			record.tuset = false;
			bw.write(record.xmls.ch, record.xmls.offset, record.xmls.length);
		}
	}

	public void write(XMLString content) throws IOException, RepairableException {
		if (stack.fSize == 0) {
			return;
		}
		QName tq = new QName();
		stack.lastElement(tq);
		record.textAccept = schema.isTextAccept(tq, content);
		if (record.textAccept) {
			record.xmls.append(content);
		} else {
			record.textAccept = false;
			record.xmls.clear();
			record.tuset = false;
			throw new RepairableException(
					MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.autofix.errorcode"), tq.rawname, ""));
		}
	}

	class Record {

		int countTu = 0;
		
		boolean changed = false;

		boolean xmldecl = false;
		boolean doctype = false;
		boolean root = false;
		boolean header = false;
		boolean body = false;
		boolean tuset = false;
		boolean headerset = false;
		boolean textAccept = false;
		
		// 1 for header 2 for body
		int writing = 0;

		XMLStringBuffer xmls = new XMLStringBuffer();
	}

	public boolean hasChanged() {
		return record.changed;
	}

	public String getLocation() {
		return location;
	}

	private String buildStartElementXmlStr(QName qname, XMLAttributesImpl attributes) {
		
		boolean format = attributes.getLength() > 3;
		
		StringBuilder builder = new StringBuilder();
		builder.append("<").append(qname.rawname);
		for (int i = 0; i < attributes.getLength(); i++) {
			if (format) {
				builder.append("\n\t");
			}
			builder.append(' ').append(attributes.getName(i)).append('=').append('\"').append(attributes.getValue(i))
					.append('\"');
		}
		builder.append('>');
		return builder.toString();
	}
	
	private void checkDeclaration() throws IOException {
		if (!record.xmldecl) {
			bw.append("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n");
			record.changed = true;
			record.xmldecl = true;
		}
	}

	private void checkDoctype() throws IOException {
		checkDeclaration();
		// doctype
		if (!record.doctype) {
			bw.append("<!DOCTYPE tmx PUBLIC \"-//LISA OSCAR:1998//DTD for Translation Memory eXchange//EN\" \"tmx14.dtd\">\n");
			record.changed = true;
			record.doctype = true;
		}
	}

	private void checkRoot() throws IOException {
		checkDoctype();
		if (!record.root) {
			bw.append("<tmx version=\"1.4\">");
			stack.pushElement(new QName(null, "tmx", "tmx", null));
			record.changed = true;
			record.root = true;
		}
	}

	private void checkHeader() throws IOException {
		checkRoot();
		if (!record.header) {
			TmxHeader header = TmxTemplet.generateTmxHeader("*all*", null, null, null, null, null, null);
			bw.append(TmxTemplet.header2Xml(header));
			record.changed = true;
			record.header = true;
		}
	}
	
	private void checkBody() throws IOException {
		checkHeader();
		if (!record.body) {
			bw.append("<body>");
			record.changed = true;
			record.body = true;
		}
	}
}
