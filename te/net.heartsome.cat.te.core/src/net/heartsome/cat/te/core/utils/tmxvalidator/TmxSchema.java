/**
 * TmxSchema.java
 *
 * Version information :
 *
 * Date:2013-12-17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.utils.tmxvalidator;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;

/**
 * @author Austen
 * @version
 * @since JDK1.6
 */
public class TmxSchema {

	public static final SymbolTable tmxElements = new SymbolTable(10);
	public static final SymbolTable inlineElements = new SymbolTable(7);
	public static final SymbolTable acceptText = new SymbolTable();
	
	private boolean passed = false;

	private Map<String, Integer> map = new HashMap<String, Integer>();
	
	public TmxSchema() {
		// <tmx>, <header>, <map/>, <body> , <note>, <prop>, <tu>, <tuv>, <seg>, <ude>.
		// Inline elements <bpt>, <ept>, <hi>, <it>, <ph>, <sub>, <ut>.
		tmxElements.addSymbol("tmx");
		tmxElements.addSymbol("header");
		tmxElements.addSymbol("map");
		tmxElements.addSymbol("body");
		tmxElements.addSymbol("note");
		tmxElements.addSymbol("prop");
		tmxElements.addSymbol("tu");
		tmxElements.addSymbol("tuv");
		tmxElements.addSymbol("seg");
		tmxElements.addSymbol("ude");
		
		inlineElements.addSymbol("bpt");
		inlineElements.addSymbol("ept");
		inlineElements.addSymbol("hi");
		inlineElements.addSymbol("it");
		inlineElements.addSymbol("ph");
		inlineElements.addSymbol("sub");
		inlineElements.addSymbol("ut");
		
		acceptText.addSymbol("note");
		acceptText.addSymbol("prop");
		acceptText.addSymbol("seg");
		acceptText.addSymbol("bpt");
		acceptText.addSymbol("ept");
		acceptText.addSymbol("hi");
		acceptText.addSymbol("sub");
		
		map.put("tmx", 1);
		map.put("header", 12);
		map.put("map", 13);
		map.put("ude", 14);
		map.put("body", 22);
		map.put("tu", 23);
		map.put("tuv", 24);
		map.put("seg", 25);
		
		map.put("bpt", 201);
		map.put("ept", 202);
		map.put("hi", 203);
		map.put("it", 204);
		map.put("ph", 205);
		map.put("sub", 206);
		map.put("ut", 207);
		
	}
	
	public boolean isPassed() {
		return passed;
	}
	
	public boolean isInvalid(QName qname) {
		return isInvalidElement(qname) && isInvalidInlineElement(qname);
	}
	
	public boolean isInvalidElement(QName qname) {
		return !tmxElements.containsSymbol(qname.rawname);
	}
	
	public boolean isInvalidInlineElement(QName qname) {
		return inlineElements.containsSymbol(qname.rawname);
	}

	public boolean isEmpty(QName qname) {
		return qname.equals("ude");
	}
	
	public boolean isTextAccept(QName qname, XMLString content) {
		boolean spaces = true;
		for (int i = content.offset; i < content.offset + content.length; i++) {
			spaces = spaces &&  XMLChar.isSpace(content.ch[i]);
		}
		if (spaces) {
			return true;
		}
		return acceptText.containsSymbol(qname.rawname);
	}
	
	public boolean isElementAccept(QName qname, QName qname1) {
		
		if (isInvalidElement(qname1)) {
			return false;
		}

		if (qname.rawname.equals("prop") || qname.rawname.equals("note")) {
			return false;
		}
		if (qname1.rawname.equals("prop") || qname1.rawname.equals("note")) {
			int i = map.get(qname.rawname);
			return i == 12 || i == 23 || i == 24;
		}
		return map.get(qname.rawname) + 1 == map.get(qname1.rawname);
	}
	
	
}
