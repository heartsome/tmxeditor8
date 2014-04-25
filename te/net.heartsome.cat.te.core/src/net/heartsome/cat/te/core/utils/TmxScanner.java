package net.heartsome.cat.te.core.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.cat.te.core.resource.Messages;

import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class TmxScanner {

	LinkedList<ErrorDescription> errorFIFO = new LinkedList<ErrorDescription>();
	ErrorCode errorCode = new ErrorCode();
	/** Scanner state: start of markup. */
	protected static final int SCANNER_STATE_START_OF_MARKUP = 1;

	/** Scanner state: comment. */
	protected static final int SCANNER_STATE_COMMENT = 2;

	/** Scanner state: processing instruction. */
	protected static final int SCANNER_STATE_PI = 3;

	/** Scanner state: DOCTYPE. */
	protected static final int SCANNER_STATE_DOCTYPE = 4;

	/** Scanner state: root element. */
	protected static final int SCANNER_STATE_ROOT_ELEMENT = 6;

	/** Scanner state: content. */
	protected static final int SCANNER_STATE_CONTENT = 7;

	/** Scanner state: reference. */
	protected static final int SCANNER_STATE_REFERENCE = 8;

	/** Scanner state: end of input. */
	protected static final int SCANNER_STATE_END_OF_INPUT = 13;

	/** Scanner state: terminated. */
	protected static final int SCANNER_STATE_TERMINATED = 14;

	/** Scanner state: CDATA section. */
	protected static final int SCANNER_STATE_CDATA = 15;

	/** Scanner state: Text declaration. */
	protected static final int SCANNER_STATE_TEXT_DECL = 16;
	private int scanState = 0;
	TmxDispatcher dispatcher = new TmxDispatcher();
	TmxEntityScanner entityScanner = null;

	private boolean complete;
	private XMLStringBuffer stringBuffer = new XMLStringBuffer();

	private XMLString fTempString = new XMLString();

	private ElementStack fElementStack = new ElementStack();

	private QName fElementQName = new QName();

	private int fEntityDepth;

	private XMLString fString = new XMLString();

	private XMLStringBuffer fStringBuffer = new XMLStringBuffer();

	private QName fCurrentElement;

	private XMLAttributesImpl fAttributes = new XMLAttributesImpl();

	private QName fAttributeQName = new QName();

	private XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();

	private boolean fScanningAttribute;

	private XMLStringBuffer fStringBuffer3 = new XMLStringBuffer();

	private boolean fNotifyCharRefs;

	private XMLString fTempString2 = new XMLString();

	private boolean fIsEntityDeclaredVC;

	private String[] pseudoAttributeValues = new String[3];

	String tmpFilePath = null;
	BufferedWriter writer;

	static boolean debug = false;

	boolean isGetContent = true;

	Stack<Boolean> stack = new Stack<Boolean>();

	private int wDepth = -1;

	Stack<String> scope = new Stack<String>();

	boolean reportError = false;
	private ProgressBar progressBar;

	double total = 1;
	
	private String encoding = null;
	
	public TmxScanner(String tmxlocation) {

		try {
			File file = new File(tmxlocation);
			encoding = FileEncodingDetector.detectFileEncoding(file);
			char[] b = new char[1024 * 8];
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file), encoding);
			try {
				int count = 0;
				while ((count = reader.read(b)) > 0) {
					total += count;
				}
			} catch (Exception e) {
				try {
					reader.close();
				} catch (IOException e1) {}
			}
			
			tmpFilePath = file.getAbsolutePath();
			tmpFilePath = tmpFilePath.substring(0, tmpFilePath.length() - 4) + "_fixed.tmx";

			if (debug) {
				tmpFilePath += ".xml";
			}

			writer = new BufferedWriter(new FileWriter(new File(tmpFilePath)));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			entityScanner = new TmxEntityScanner(tmxlocation, encoding);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		setScannerState(SCANNER_STATE_TEXT_DECL);
	}

	public static void main(String[] args) throws Exception {
		// TmxScanner scanner = new TmxScanner("res/abc.xml");
		TmxScanner scanner = new TmxScanner("C:\\Users\\Administrator\\Desktop\\新建文件夹\\i1.tmx");
		while (scanner.scanTmxDocument() == null);
	}

	public String scanTmxDocument() throws IOException {

		int wored = 0;
		int tmp = 1;
		errorFIFO.clear();
		while (!(complete = entityScanner.hasFinish())) {
			if (reportError) {
				printErrorDes(errorFIFO);
				reportError = false;
				break;
			}

			tmp = (int) ((entityScanner.getHasLoad() / total) * 100);
			if (tmp > wored) {
				final int d = tmp - wored;
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						progressBar.setSelection(progressBar.getSelection() + d);
					}
				});
				wored = tmp;
			}
			dispatcher.dispatch();
		}

		if (complete) {
			try {
				writer.flush();
				writer.close();
			} catch (Exception e) {
				// do nothing
			}
			return tmpFilePath;
		} else {
			return null;
		}
	}

	static void printErrorDes(LinkedList<ErrorDescription> errorFIFO) {
		if (!debug) {
			return;
		}
		for (ErrorDescription errordes : errorFIFO) {
			StringBuilder error = new StringBuilder();
			error.append("[ line : ").append(errordes.lineNumber).append(", offset : ").append(errordes.columnNumber)
					.append(" ]").append(errordes.description);
			System.err.println(error.toString());
		}
	}

	static void printState(int i) {
		if (!debug) {
			return;
		}
		switch (i) {
		case SCANNER_STATE_START_OF_MARKUP:
			System.out.println("markup");
			break;
		case SCANNER_STATE_COMMENT:
			System.out.println("comment");
			break;

		case SCANNER_STATE_DOCTYPE:
			System.out.println("doctype");
			break;

		case SCANNER_STATE_ROOT_ELEMENT:
			System.out.println("root element");
			break;

		case SCANNER_STATE_CONTENT:
			System.out.println("content");
			break;

		case SCANNER_STATE_REFERENCE:
			System.out.println("reference");
			break;

		case SCANNER_STATE_END_OF_INPUT:
			System.out.println("end of input");
			break;

		case SCANNER_STATE_TERMINATED:
			System.out.println("terminated");
			break;

		case SCANNER_STATE_CDATA:
			System.out.println("cdata");
			break;

		case SCANNER_STATE_TEXT_DECL:
			System.out.println("text decl");
			break;
		}
	}

	void scanXMLDeclOrTextDecl(boolean scanningTextDecl) throws IOException {
		scanningTextDecl = false;
		boolean ok = true;
		// pseudo-attribute values
		String version = null;
		String encoding = null;
		String standalone = null;

		// scan pseudo-attributes
		final int STATE_VERSION = 0;
		final int STATE_ENCODING = 1;
		final int STATE_STANDALONE = 2;
		final int STATE_DONE = 3;
		int state = STATE_VERSION;

		boolean dataFoundForTarget = false;
		boolean sawSpace = entityScanner.skipDeclSpaces();
		// since pseudoattributes are *not* attributes,
		// their quotes don't need to be preserved in external parameter entities.
		// the XMLEntityScanner#scanLiteral method will continue to
		// emit -1 in such cases when it finds a quote; this is
		// fine for other methods that parse scanned entities,
		// but not for the scanning of pseudoattributes. So,
		// temporarily, we must mark the current entity as not being "literal"
		// XMLEntityManager.ScannedEntity currEnt = fEntityManager.getCurrentEntity();
		// currEnt.literal = false;
		while (entityScanner.peekChar() != '?') {
			dataFoundForTarget = true;
			String name = scanPseudoAttribute(scanningTextDecl, fString);
			switch (state) {
			case STATE_VERSION: {
				if (name.equals("version")) {
					if (!sawSpace) {
						ok = false;
						error("not found space");
					}
					version = fString.toString();
					state = STATE_ENCODING;
					if (!versionSupported(version)) {
						ok = false;
						error("version not supported");
					}
				} else if (name.equals("encoding")) {
					if (!scanningTextDecl) {
						ok = false;
						error("not found version info");
					}
					if (!sawSpace) {
						ok = false;
						error("not found space");
					}
					encoding = fString.toString();
					state = scanningTextDecl ? STATE_DONE : STATE_STANDALONE;
				} else {
					if (scanningTextDecl) {
						ok = false;
						error("not found encoding decl");
					} else {
						ok = false;
						error("not found version decl");
					}
				}
				break;
			}
			case STATE_ENCODING: {
				if (name.equals("encoding")) {
					if (!sawSpace) {
						error("not found space");//
					}
					encoding = fString.toString();
					state = scanningTextDecl ? STATE_DONE : STATE_STANDALONE;
					// TODO: check encoding name; set encoding on
					// entity scanner
				} else if (!scanningTextDecl && name.equals("standalone")) {
					if (!sawSpace) {
						error("not found space");//
					}
					standalone = fString.toString();
					state = STATE_DONE;
					if (!standalone.equals("yes") && !standalone.equals("no")) {
						error("invalid vaule, needs 'yes' or 'no'");//
					}
				} else {
					error("not found encoding decl");
				}
				break;
			}
			case STATE_STANDALONE: {
				if (name.equals("standalone")) {
					if (!sawSpace) {
						error("not found space");//
					}
					standalone = fString.toString();
					state = STATE_DONE;
					if (!standalone.equals("yes") && !standalone.equals("no")) {

						error("invalid vaule, needs 'yes' or 'no'");//
					}
				} else {
					error("not found encoding decl");
				}
				break;
			}
			default: {
				error("not found more pseudo attrbuites");
				ok = false;
			}
			}
			sawSpace = entityScanner.skipDeclSpaces();
		}
		// restore original literal value
		// if (currLiteral)
		// currEnt.literal = true;
		// REVISIT: should we remove this error reporting?
		if (scanningTextDecl && state != STATE_DONE) {
			error("not found more pseudo attrbuites");
		}

		// If there is no data in the xml or text decl then we fail to report error
		// for version or encoding info above.
		if (scanningTextDecl) {
			if (!dataFoundForTarget && encoding == null) {
				error("not found more pseudo attrbuites");
			}
		} else {
			if (!dataFoundForTarget && version == null) {
				error("not found more pseudo attrbuites");
				ok = false;
			}
		}

		// end
		if (!entityScanner.skipChar('?')) {
			ok = false;
			error("not found end flag '?'");
		}
		if (!entityScanner.skipChar('>')) {
			ok = false;
			error("not found end flag '>'");
		}

		// fill in return array
		pseudoAttributeValues[0] = version;
		pseudoAttributeValues[1] = encoding;
		pseudoAttributeValues[2] = standalone;
		
		writer.append("<?xml version=\"1.0\"?>");
	}

	void scanPI() throws IOException {
		String target = null;
		target = entityScanner.scanName();
		if (target == null) {
			// TODO
			// reportFatalError("PITargetRequired", null);
		}

		// scan data
		scanPIData(target, fString);
	}

	// 自结束 ? true : false;
	boolean scanStartElement() throws IOException {

		// scan name
		entityScanner.scanQName(fElementQName);

		fCurrentElement = fElementQName;

		if (!scope.isEmpty()) {
			if (scope.lastElement().equals("note") || scope.lastElement().equals("prop")) {
				appendCharacter(encodingCharacter('<'));
				appendCharacter(fElementQName.localpart == null ? "" : fElementQName.localpart);
				setScannerState(SCANNER_STATE_CONTENT);
				return false;
			}
		}

		// scan attributes
		boolean empty = false;
		fAttributes.removeAllAttributes();
		do {
			boolean sawSpace = entityScanner.skipSpaces();
			int c = entityScanner.peekChar();
			if (c == '>') {
				entityScanner.scanChar();
				break;
			} else if (c == '/') {
				entityScanner.scanChar();
				if (!entityScanner.skipChar('>')) {
					// TODO
				}
				empty = true;
				break;
			} else if (!XMLChar.isNameStart(c) || !sawSpace) {
				// Second chance. Check if this character is a high surrogate of a valid name start character.
				// if (!isValidNameStartHighSurrogate(c) || !sawSpace) {
				// }
				error("illegal char '&#" + Integer.toHexString(c) + "'");
				appendCharacter(encodingCharacter(c));
				setScannerState(SCANNER_STATE_CONTENT);
				return false;
			}
			// get the attribute
			scanAttribute(fAttributes);
		} while (true);

		// push element stack
		fCurrentElement = fElementStack.pushElement(fElementQName);

		// if (fElementQName.rawname != null && !fElementQName.rawname.isEmpty()) {
		// writer.write('<');
		// writer.write(fElementQName.rawname);
		// QName qn = new QName();
		//
		// for (int i = 0; i < fAttributes.getLength(); i++) {
		// fAttributes.getName(i, qn);
		// writer.write(' ');
		// writer.write(qn.rawname);
		// writer.write("=");
		// writer.write("\"");
		// writer.write(fAttributes.getValue(i));
		// writer.write("\"");
		// }
		// if (empty) {
		// // 自加减
		// writer.write("/>");
		// fElementStack.popElement(new QName());
		// stack.pop();
		// } else {//有 content
		// writer.write('>');
		// writer.flush();
		// }
		// }

		return empty;
	}

	private String encodingCharacter(int c) {
		switch (c) {
		case '<':
			return "&lt;";
		case '>':
			return "&gt;";
		case '&':
			return "&amp;";

			// 似乎并不影响？？
			// case '\'':
			// return "&apos;";
			// case '\"':
			// return "&quot";
		}
		return String.valueOf((char) c);
	}

	int scanEndElement() throws IOException {

		if (scope.isEmpty()) {
			return 0;
		}

		String last = "";
		if (!scope.isEmpty()) {
			last = scope.lastElement();
			if (!entityScanner.skipString(last)) {// 不接受？
				QName tmp = new QName();
				entityScanner.scanQName(tmp);
				if (isInvalidTag(tmp.localpart)) {// 规范不接受
					errorCode.append("</").append(tmp.rawname);
					return 0;
				} else if (isLogicalTags(last, tmp.localpart, true)) {// 符合逻辑
					new ErrorDescription(entityScanner.getLineNumber(), entityScanner.getColumnNumber(),
							Messages.getString("tmxeditor.tmxFileValidator.autofix.missStart") + tmp.rawname);
					appendContent("<");
					appendContent(tmp.rawname);
					appendContent(">");
					appendContent(errorCode.toString());
					appendEndElem(tmp.rawname);
					return 0;
				} else {
					// what to do?
				}
				return 0;
			}
		}

		// fElementStack.popElement(fElementQName);

		// end
		if (entityScanner.skipSpaces()) {
			errorCode.append(" ");
		}

		if (!entityScanner.skipChar('>')) {
			return 0;
		}
		appendEndElem(scope.pop());
		errorCode.clear();// 清除错误数据
		return 0;
	}

	// int scanEndElement() throws IOException {
	//
	// String last = scope.pop();
	//
	// if (!entityScanner.skipString(fElementQName.rawname)) {
	// error("not found end tag");// TODO
	// }
	//
	// fElementStack.popElement(fElementQName);
	// if (fElementQName.rawname.equals("/tuv")) {
	// System.out.println();
	// }
	//
	// // end
	// entityScanner.skipSpaces();
	// if (!entityScanner.skipChar('>')) {
	// error("not found end tag");// TODO
	// }
	// fLastMarkupDepth = fMarkupDepth;
	// fMarkupDepth--;
	// // // we have increased the depth for two markup "<" characters
	// // fMarkupDepth--;
	// //
	// // // check that this element was opened in the same entity
	// // if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
	// // // TODO
	// // // reportFatalError("ElementEntityMismatch",
	// // // new Object[]{fCurrentElement.rawname});
	// // }
	// // TODO
	// if (fElementQName.rawname != null && !fElementQName.rawname.isEmpty()) {
	// writer.write("</");
	// writer.write(fElementQName.rawname);
	// writer.write(">");
	// }
	// return 0;
	// }

	private boolean isLogicalTags(String parent, String localpart, boolean rigorous) {
		if (rigorous) {
			if (parent.equals("note") || parent.equals("prop")) {
				return false;
			}
			if (localpart.equals("note") || localpart.equals("prop")) {
				return parent.equals("tu") || parent.equals("tuv") || parent.equals("header");
			}
			return testDepth(parent) < testDepth(localpart);
		} else {
			return testDepth(parent) < testDepth(localpart);
		}

	}

	void scanCDATASection(boolean complete2) {

	}

	int scanContent() throws IOException {

		XMLString content = fTempString;
		int c = entityScanner.scanContent(content);
		if (c == '\r') {
			// happens when there is the character reference &#13;
			entityScanner.scanChar();
			stringBuffer.clear();
			stringBuffer.append(content);
			stringBuffer.append((char) c);
			content = stringBuffer;
			c = -1;
		}

		String str = new String(content.ch, content.offset, content.length);
		if (str.trim().isEmpty()) {
			appendContent(str);
		} else {
			if (scope.isEmpty()) {
				return str.charAt(0);
			}
			String parent = scope.lastElement();
			if (parent.equals("note") || parent.equals("prop") || parent.equals("seg")) {
				appendContent(str);
			} else {
				errorCode.append(str);// 存储错误代码
				errorCode.setPosition(entityScanner.getLineNumber(), entityScanner.getColumnNumber());
				errorCode.setDescription(MessageFormat.format(
						Messages.getString("tmxeditor.tmxFileValidator.autofix.errorcode"), parent, str));
			}
		}

		if (c == ']' && fTempString.length == 0) {
			stringBuffer.clear();
			stringBuffer.append((char) entityScanner.scanChar());
			// remember where we are in case we get an endEntity before we
			// could flush the buffer out - this happens when we're parsing an
			// entity which ends with a ]
			// fInScanContent = true;
			//
			// We work on a single character basis to handle cases such as:
			// ']]]>' which we might otherwise miss.
			//
			if (entityScanner.skipChar(']')) {
				stringBuffer.append(']');
				while (entityScanner.skipChar(']')) {
					stringBuffer.append(']');
				}
				if (entityScanner.skipChar('>')) {

					// TODO
					// reportFatalError("CDEndInContent", null);
				}
			}
			// TODO
			// fInScanContent = false;
			c = -1;
		}
		return c;
	}

	void scanComment() {

	}

	private void scanPIData(String target, XMLString xs) throws IOException {

		// check target
		if (target.length() == 3) {
			char c0 = Character.toLowerCase(target.charAt(0));
			char c1 = Character.toLowerCase(target.charAt(1));
			char c2 = Character.toLowerCase(target.charAt(2));
			if (c0 == 'x' && c1 == 'm' && c2 == 'l') {
				// TODO
				// reportFatalError("ReservedPITarget", null);
			}
		}

		// spaces
		if (!entityScanner.skipSpaces()) {
			if (entityScanner.skipString("?>")) {
				// we found the end, there is no data
				xs.clear();
				return;
			} else {
				if (entityScanner.peekChar() == ':') {
					entityScanner.scanChar();
					XMLStringBuffer colonName = new XMLStringBuffer(target);
					colonName.append(':');
					String str = entityScanner.scanName();
					if (str != null)
						colonName.append(str);
					// TODO
					// reportFatalError("ColonNotLegalWithNS", new Object[] {colonName.toString()});
					entityScanner.skipSpaces();
				} else {
					// TODO
					// if there is data there should be some space
					// reportFatalError("SpaceRequiredInPI", null);
				}
			}
		}

		fStringBuffer.clear();
		// data
		if (entityScanner.scanData("?>", fStringBuffer)) {
			do {
				int c = entityScanner.peekChar();
				if (c != -1) {
					if (XMLChar.isHighSurrogate(c)) {
						scanSurrogates(fStringBuffer);
					} else if (XMLChar.isInvalid(c)) {
						// reportFatalError("InvalidCharInPI",
						// new Object[]{Integer.toHexString(c)});
						entityScanner.scanChar();
					}
				}
			} while (entityScanner.scanData("?>", fStringBuffer));
		}
		xs.setValues(fStringBuffer);
	}

	private void scanAttribute(XMLAttributesImpl attributes) throws IOException {

		// attribute name
		entityScanner.scanQName(fAttributeQName);

		// equals
		entityScanner.skipSpaces();
		if (!entityScanner.skipChar('=')) {
			error("not found '='");
		}
		entityScanner.skipSpaces();

		// content
		int oldLen = attributes.getLength();
		int attrIndex = attributes.addAttribute(fAttributeQName, XMLSymbols.fCDATASymbol, null);

		if (oldLen == attributes.getLength()) {
			error("Multiple attr");
		}

		// Scan attribute value and return true if the un-normalized and normalized value are the same
		boolean isSameNormalizedAttr = scanAttributeValue(fTempString, fTempString2, fAttributeQName.rawname,
				fIsEntityDeclaredVC, fCurrentElement.rawname, null);

		attributes.setValue(attrIndex, fTempString.toString());
		// If the non-normalized and normalized value are the same, avoid creating a new string.
		if (!isSameNormalizedAttr) {
			attributes.setNonNormalizedValue(attrIndex, fTempString2.toString());
		}
		attributes.setSpecified(attrIndex, true);
	}

	private void tryToFindAttrs(XMLAttributesImpl attributes, XMLStringBuffer buf) throws IOException {
		TmxEntityScanner scanner = new TmxEntityScanner(buf);
		while (!scanner.hasFinish()) {
			scanner.skipSpaces();
			// attribute name
			scanner.scanQName(fAttributeQName);
			// equals
			scanner.skipSpaces();
			if (!scanner.skipChar('=')) {
				error("not found '='");
				return;
			}
			int attrIndex = attributes.addAttribute(fAttributeQName, XMLSymbols.fCDATASymbol, null);
			boolean isSameNormalizedAttr = scanAttributeValue(fTempString, fTempString2, fAttributeQName.rawname,
					fIsEntityDeclaredVC, null, scanner);
			attributes.setValue(attrIndex, fTempString.toString());
			if (!isSameNormalizedAttr) {
				attributes.setNonNormalizedValue(attrIndex, fTempString2.toString());
			}
			attributes.setSpecified(attrIndex, true);
		}
	}

	private boolean scanAttributeValue(XMLString value, XMLString nonNormalizedValue, String atName,
			boolean checkEntities, String eleName, TmxEntityScanner scanner) throws IOException {
		// set default
		if (scanner == null) {
			scanner = entityScanner;
		}
		// quote
		int quote = scanner.peekChar();
		if (quote != '\'' && quote != '"') {
			error("not found quote");
		}

		scanner.scanChar();
		int entityDepth = fEntityDepth;

		int c = scanner.scanLiteral(quote, value);

		int fromIndex = 0;
		if (c == quote && (fromIndex = isUnchangedByNormalization(value)) == -1) {
			/** Both the non-normalized and normalized attribute values are equal. **/
			nonNormalizedValue.setValues(value);
			int cquote = scanner.scanChar();
			if (cquote != quote) {
				error("not found close quote");
			}
			return true;
		}

		fStringBuffer2.clear();
		fStringBuffer2.append(value);
		normalizeWhitespace(value, fromIndex);

		if (c != quote) {
			fScanningAttribute = true;
			fStringBuffer.clear();
			do {
				fStringBuffer.append(value);
				if (c == '&') {
					scanner.skipChar('&');
					if (entityDepth == fEntityDepth) {
						fStringBuffer2.append('&');
					}
					if (scanner.skipChar('#')) {
						if (entityDepth == fEntityDepth) {
							fStringBuffer2.append('#');
						}
						int ch = scanCharReferenceValue(fStringBuffer, fStringBuffer2);
						if (ch != -1) {
						}
					} else {
						String entityName = scanner.scanName();
						if (entityName == null) {
							error("not found reference des");
							// reportFatalError("NameRequiredInReference", null);
						} else if (entityDepth == fEntityDepth) {
							fStringBuffer2.append(entityName);
						}
						if (!scanner.skipChar(';')) {
							error("not found thie reference end ';'");
						} else if (entityDepth == fEntityDepth) {
							fStringBuffer2.append(';');
						}
						// if (entityName == fAmpSymbol) {
						if (entityName.equals("amp")) {
							fStringBuffer.append('&');
						}
						// else if (entityName == fAposSymbol) {
						else if (entityName.equals("apos")) {
							fStringBuffer.append('\'');
						} else if (entityName.equals("lt")) {
							fStringBuffer.append('<');
						} else if (entityName.equals("gt")) {
							fStringBuffer.append('>');
						} else if (entityName.equals("quot")) {
							fStringBuffer.append('"');
						} else {
							// if (fEntityManager.isExternalEntity(entityName)) {
							// reportFatalError("ReferenceToExternalEntity",
							// new Object[] { entityName });
							// }
							// else {
							// if (!fEntityManager.isDeclaredEntity(entityName)) {
							// //WFC & VC: Entity Declared
							// if (checkEntities) {
							// if (fValidation) {
							// fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
							// "EntityNotDeclared",
							// new Object[]{entityName},
							// XMLErrorReporter.SEVERITY_ERROR);
							// }
							// }
							// else {
							// reportFatalError("EntityNotDeclared",
							// new Object[]{entityName});
							// }
							// }
							// fEntityManager.startEntity(entityName, true);
							// }
						}
					}
				} else if (c == '<') {
					error("found '<' when scan attr value");
					scanner.scanChar();
					if (entityDepth == fEntityDepth) {
						fStringBuffer2.append((char) c);
					}
				} else if (c == '%' || c == ']') {
					scanner.scanChar();
					fStringBuffer.append((char) c);
					if (entityDepth == fEntityDepth) {
						fStringBuffer2.append((char) c);
					}
				} else if (c == '\n' || c == '\r') {
					scanner.scanChar();
					fStringBuffer.append(' ');
					if (entityDepth == fEntityDepth) {
						fStringBuffer2.append('\n');
					}
				} else if (c != -1 && XMLChar.isHighSurrogate(c)) {
					fStringBuffer3.clear();
					if (scanSurrogates(fStringBuffer3)) {
						fStringBuffer.append(fStringBuffer3);
						if (entityDepth == fEntityDepth) {
							fStringBuffer2.append(fStringBuffer3);
						}
					}
				} else if (c != -1 && XMLChar.isValid(c)) {
					error("found invalid char when scan attr value '&#" + Integer.toHexString(c) + "'");
					scanner.scanChar();
					if (entityDepth == fEntityDepth) {
						fStringBuffer2.append((char) c);
					}
				}
				c = scanner.scanLiteral(quote, value);
				if (entityDepth == fEntityDepth) {
					fStringBuffer2.append(value);
				}
				normalizeWhitespace(value);
			} while (c != quote || entityDepth != fEntityDepth);
			fStringBuffer.append(value);
			value.setValues(fStringBuffer);
			fScanningAttribute = false;
		}
		nonNormalizedValue.setValues(fStringBuffer2);

		// quote
		int cquote = scanner.scanChar();
		if (cquote != quote) {
			error("not found close quote");
		}
		return nonNormalizedValue.equals(value.ch, value.offset, value.length);
	}

	private void normalizeWhitespace(XMLString value, int fromIndex) {
		int end = value.offset + value.length;
		for (int i = value.offset + fromIndex; i < end; ++i) {
			int c = value.ch[i];
			// Performance: For XML 1.0 documents take advantage of
			// the fact that the only legal characters below 0x20
			// are 0x09 (TAB), 0x0A (LF) and 0x0D (CR). Since we've
			// already determined the well-formedness of these
			// characters it is sufficient (and safe) to check
			// against 0x20. -- mrglavas
			if (c < 0x20) {
				value.ch[i] = ' ';
			}
		}
	}

	private int isUnchangedByNormalization(XMLString value) {
		int end = value.offset + value.length;
		for (int i = value.offset; i < end; ++i) {
			int c = value.ch[i];
			// Performance: For XML 1.0 documents take advantage of
			// the fact that the only legal characters below 0x20
			// are 0x09 (TAB), 0x0A (LF) and 0x0D (CR). Since we've
			// already determined the well-formedness of these
			// characters it is sufficient (and safe) to check
			// against 0x20. -- mrglavas
			if (c < 0x20) {
				return i - value.offset;
			}
		}
		return -1;
	}

	private int scanCharReferenceValue(XMLStringBuffer buf, XMLStringBuffer buf2) throws IOException {
		// scan hexadecimal value
		boolean hex = false;
		if (entityScanner.skipChar('x')) {
			if (buf2 != null) {
				buf2.append('x');
			}
			hex = true;
			fStringBuffer3.clear();
			boolean digit = true;

			int c = entityScanner.peekChar();
			digit = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
			if (digit) {
				if (buf2 != null) {
					buf2.append((char) c);
				}
				entityScanner.scanChar();
				fStringBuffer3.append((char) c);

				do {
					c = entityScanner.peekChar();
					digit = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
					if (digit) {
						if (buf2 != null) {
							buf2.append((char) c);
						}
						entityScanner.scanChar();
						fStringBuffer3.append((char) c);
					}
				} while (digit);
			} else {
				return -1;
			}
		}

		// scan decimal value
		else {
			fStringBuffer3.clear();
			boolean digit = true;

			int c = entityScanner.peekChar();
			digit = c >= '0' && c <= '9';
			if (digit) {
				if (buf2 != null) {
					buf2.append((char) c);
				}
				entityScanner.scanChar();
				fStringBuffer3.append((char) c);

				do {
					c = entityScanner.peekChar();
					digit = c >= '0' && c <= '9';
					if (digit) {
						if (buf2 != null) {
							buf2.append((char) c);
						}
						entityScanner.scanChar();
						fStringBuffer3.append((char) c);
					}
				} while (digit);
			} else {
				return -1;
			}
		}

		// end
		if (!entityScanner.skipChar(';')) {
			return -1;
		}
		if (buf2 != null) {
			buf2.append(';');
		}

		// convert string to number
		int value = -1;
		try {
			value = Integer.parseInt(fStringBuffer3.toString(), hex ? 16 : 10);

			// character reference must be a valid XML character
			if (XMLChar.isInvalid(value)) {
				return -1;
			}
		} catch (NumberFormatException e) {
			return -1;
		}

		// append corresponding chars to the given buffer
		if (!XMLChar.isSupplemental(value)) {
			buf.append((char) value);
		} else {
			// character is supplemental, split it into surrogate chars
			buf.append(XMLChar.highSurrogate(value));
			buf.append(XMLChar.lowSurrogate(value));
		}

		// char refs notification code
		if (fNotifyCharRefs && value != -1) {
			String literal = "#" + (hex ? "x" : "") + fStringBuffer3.toString();
			if (!fScanningAttribute) {
				// fCharRefLiteral = literal;
			}
		}

		return value;
	}

	private void normalizeWhitespace(XMLString value) {
		int end = value.offset + value.length;
		for (int i = value.offset; i < end; ++i) {
			int c = value.ch[i];
			// Performance: For XML 1.0 documents take advantage of
			// the fact that the only legal characters below 0x20
			// are 0x09 (TAB), 0x0A (LF) and 0x0D (CR). Since we've
			// already determined the well-formedness of these
			// characters it is sufficient (and safe) to check
			// against 0x20. -- mrglavas
			if (c < 0x20) {
				value.ch[i] = ' ';
			}
		}
	}

	private boolean scanRootElementHook() {
		return false;
	}

	private void scanEntityReference() throws IOException {

		// name
		String name = entityScanner.scanName();
		if (name == null) {
			appendCharacter("&amp;");
			return;
		}
		// 符合标准
		if (name.equals("amp") || name.equals("lt") || name.equals("gt") || name.equals("apos") || name.equals("quot")) {
			if (entityScanner.skipChar(';')) {
				appendCharacter("&");
				appendCharacter(name);
				appendCharacter(";");
			}
		} else {
			appendCharacter("&amp;");
			appendCharacter(name);
		}
	}

	private void scanCharReference() throws IOException {
		fStringBuffer2.clear();
		boolean hex = entityScanner.peekChar() == 'x';
		int ch = scanCharReferenceValue(fStringBuffer2, null);
		if (ch != -1) {
			int c = Integer.valueOf(fStringBuffer3.toString(), hex ? 16 : 10);
			if (c < 0x20 && !XMLChar.isSpace(c)) {
				return;
			}
			if (hex) {//
				appendCharacter("&x");
			} else {
				appendCharacter("&");
			}
			appendCharacter(fStringBuffer3.toString());
			appendCharacter(";");
		} else {
			appendCharacter("&amp;#");
			appendCharacter(fStringBuffer3.toString());
		}
	}

	private boolean scanSurrogates(XMLStringBuffer buf) throws IOException {
		int high = entityScanner.scanChar();
		int low = entityScanner.peekChar();
		if (!XMLChar.isLowSurrogate(low)) {
			error("invalid char in content");
			return false;
		}
		entityScanner.scanChar();

		int c = XMLChar.supplemental((char) high, (char) low);

		// supplemental character must be a valid XML character
		if (XMLChar.isInvalid(c)) {
			error("invalid char in content");
			return false;
		}

		// fill in the buffer
		buf.append((char) high);
		buf.append((char) low);

		return true;
	}

	private void setScannerState(int newState) {
		scanState = newState;
	}

	class ElementStack {

		/** The stack data. */
		protected QName[] fElements;

		/** The size of the stack. */
		protected int fSize;

		/** Default constructor. */
		public ElementStack() {
			fElements = new QName[10];
			for (int i = 0; i < fElements.length; i++) {
				fElements[i] = new QName();
			}
		}

		/**
		 * Pushes an element on the stack.
		 * <p>
		 * <strong>Note:</strong> The QName values are copied into the stack. In other words, the caller does
		 * <em>not</em> orphan the element to the stack. Also, the QName object returned is <em>not</em> orphaned to the
		 * caller. It should be considered read-only.
		 * @param element
		 *            The element to push onto the stack.
		 * @return Returns the actual QName object that stores the
		 */
		public QName pushElement(QName element) {
			if (fSize == fElements.length) {
				QName[] array = new QName[fElements.length * 2];
				System.arraycopy(fElements, 0, array, 0, fSize);
				fElements = array;
				for (int i = fSize; i < fElements.length; i++) {
					fElements[i] = new QName();
				}
			}
			fElements[fSize].setValues(element);
			return fElements[fSize++];
		} // pushElement(QName):QName

		/**
		 * Pops an element off of the stack by setting the values of the specified QName.
		 * <p>
		 * <strong>Note:</strong> The object returned is <em>not</em> orphaned to the caller. Therefore, the caller
		 * should consider the object to be read-only.
		 */
		public void popElement(QName element) {
			element.setValues(fElements[--fSize]);
		} // popElement(QName)

		/** Clears the stack without throwing away existing QName objects. */
		public void clear() {
			fSize = 0;
		} // clear()

	} // class ElementStack

	class TmxDispatcher {
		void dispatch() throws IOException {
			boolean again;
			do {
				again = false;
				switch (scanState) {
				case SCANNER_STATE_CONTENT: {
					if (entityScanner.skipChar('<')) {
						setScannerState(SCANNER_STATE_START_OF_MARKUP);
						again = true;
					} else if (entityScanner.skipChar('&')) {
						setScannerState(SCANNER_STATE_REFERENCE);
						again = true;
					} else {
						do {
							int c = scanContent();
							if (c == '<') {
								entityScanner.scanChar();
								setScannerState(SCANNER_STATE_START_OF_MARKUP);
								break;
							} else if (c == '&') {
								entityScanner.scanChar();
								setScannerState(SCANNER_STATE_REFERENCE);
								break;
							} else if (c != -1 && XMLChar.isInvalid(c)) {
								if (XMLChar.isHighSurrogate(c)) {
									// special case: surrogates
									fStringBuffer.clear();
									if (scanSurrogates(fStringBuffer)) {
									}
								} else {
									// TODO
									// reportFatalError("InvalidCharInContent",
									// new Object[] {
									// Integer.toString(c, 16)});
									entityScanner.scanChar();
								}
							}
						} while (complete);
					}
					break;
				}
				case SCANNER_STATE_START_OF_MARKUP: {
					// fMarkupDepth++;
					if (entityScanner.skipChar('/')) {
						if (scanEndElement() == 0) {
							// if (elementDepthIsZeroHook()) {
							// return true;
							// }
						}
						setScannerState(SCANNER_STATE_CONTENT);
					} else if (XMLChar.isNameStart(entityScanner.peekChar())) {
						scanStartElement();
						tryWriteStart();
						setScannerState(SCANNER_STATE_CONTENT);
					} else if (entityScanner.skipChar('!')) {
						if (entityScanner.skipChar('-')) {
							// TODO
							// if (!entityScanner.skipChar('-')) {
							// reportFatalError("InvalidCommentStart",
							// null);
							// }
							setScannerState(SCANNER_STATE_COMMENT);
							again = true;
						} else if (entityScanner.skipString("[CDATA[")) {
							setScannerState(SCANNER_STATE_CDATA);
							again = true;
						}
						// TODO
						// else if (!scanForDoctypeHook()) {
						// reportFatalError("MarkupNotRecognizedInContent",
						// null);
						// }
					} else if (entityScanner.skipChar('?')) {
						setScannerState(SCANNER_STATE_PI);
						again = true;
					}
					// else if (isValidNameStartHighSurrogate(entityScanner.peekChar())) {
					// scanStartElement();
					// setScannerState(SCANNER_STATE_CONTENT);
					// }
					else {
						// TODO
						// reportFatalError("MarkupNotRecognizedInContent",
						// null);
						setScannerState(SCANNER_STATE_CONTENT);
					}
					break;
				}
				case SCANNER_STATE_COMMENT: {
					scanComment();
					setScannerState(SCANNER_STATE_CONTENT);
					break;
				}
				case SCANNER_STATE_PI: {
					scanPI();
					setScannerState(SCANNER_STATE_CONTENT);
					break;
				}
				case SCANNER_STATE_CDATA: {
					scanCDATASection(complete);
					setScannerState(SCANNER_STATE_CONTENT);
					break;
				}
				case SCANNER_STATE_REFERENCE: {
					// fMarkupDepth++;
					// NOTE: We need to set the state beforehand
					// because the XMLEntityHandler#startEntity
					// callback could set the state to
					// SCANNER_STATE_TEXT_DECL and we don't want
					// to override that scanner state.
					setScannerState(SCANNER_STATE_CONTENT);
					if (entityScanner.skipChar('#')) {
						scanCharReference();
					} else {
						scanEntityReference();
					}
					break;
				}
				case SCANNER_STATE_TEXT_DECL:
					// scan text decl
					if (entityScanner.skipString("<?xml")) {
						// fMarkupDepth++;
						// NOTE: special case where entity starts with a PI
						// whose name starts with "xml" (e.g. "xmlfoo")
						if (XMLChar.isName(entityScanner.peekChar())) {
							fStringBuffer.clear();
							fStringBuffer.append("xml");
							while (XMLChar.isNCName(entityScanner.peekChar())) {
								fStringBuffer.append((char) entityScanner.scanChar());
							}
							String target = new String(fStringBuffer.ch, fStringBuffer.offset, fStringBuffer.length);
							// fSymbolTable.addSymbol(fStringBuffer.ch, fStringBuffer.offset, fStringBuffer.length);
							scanPIData(target, fTempString);
						}
						// standard text declaration
						else {
							scanXMLDeclOrTextDecl(true);
						}
					}
					// now that we've straightened out the readers, we can read in chunks:
					setScannerState(SCANNER_STATE_CONTENT);
					break;
				case SCANNER_STATE_ROOT_ELEMENT: {
					if (scanRootElementHook()) {
						// return true;
					}
					setScannerState(SCANNER_STATE_CONTENT);
					break;
				}
				case SCANNER_STATE_DOCTYPE: {
					// TODO
					// reportFatalError("DoctypeIllegalInContent",
					// null);
					setScannerState(SCANNER_STATE_CONTENT);
				}
				}
			} while (complete || again);
		}
	}

	private boolean versionSupported(String version) {
		return version.equals("1.0");
	}

	public void tryWriteStart() throws IOException {

		String parent = null;
		if (scope != null && !scope.isEmpty()) {
			parent = scope.lastElement();
		}

		String localpart = fCurrentElement.localpart;
		if (wDepth < 1) {// tmx
			if (localpart.equals("tmx")) {
				appendStartElem(null, null);
				wDepth = 1;
				scope.push("tmx");
			} else {
				error("w===w: 没有找到 tmx 标记，可能不是 tmx 文件");
			}
		} else {
			if (isInvalidTag(localpart)) {// 不是 tmx 标准
				return;// 当做文本处理
			} else {
				boolean again = true;
				while (again) {
					again = false;
					// 逻辑性检测
					if (parent.equals("tmx")) {
						if (localpart.equals("header") || localpart.equals("body")) {// bingo!
							appendStartElem(null, null);
							parent = localpart;
						} else {// bad luck
							writerDefaultHeader();// default header
							again = true;
							parent = localpart;
						}
						scope.push(new String(parent));
					} else if (parent.equals("header")) {
						if (localpart.equals("note") || localpart.equals("prop") // bingo!
								|| localpart.equals("ude")) {
							appendStartElem(null, null);
							parent = localpart;
							scope.push(new String(parent));
						} else {
							if (localpart.equals("body") || isLogicalTags(parent, localpart, false)) {
								new ErrorDescription(entityScanner.getLineNumber(), entityScanner.getColumnNumber()
										- localpart.length(),
										Messages.getString("tmxeditor.tmxFileValidator.autofix.missEnd")
												+ scope.lastElement());
								appendEndElem(scope.pop());
								parent = scope.lastElement();
								again = true;
							} else {
								return;// 当做文本处理
							}
						}
					} else if (parent.equals("body")) {
						if (localpart.equals("tu")) {// bingo!
							appendStartElem(null, null);
							scope.push("tu");
						} else {
							if (isLogicalTags("body", localpart, false)) {// miss tu, 无论如何追加 tu!
								QName qName = new QName();
								qName.setValues(null, "tu", "tu", null);
								XMLAttributesImpl attrs = new XMLAttributesImpl();
								tryToFindAttrs(attrs, errorCode.buf);
								parent = "tu";
								scope.push(parent);// 改变范围
								new ErrorDescription(entityScanner.getLineNumber(), entityScanner.getColumnNumber(),
										Messages.getString("tmxeditor.tmxFileValidator.autofix.missStart")
												+ scope.lastElement());
								errorCode.clear();
								appendStartElem(qName, attrs);
								again = true;
							} else {// other tags,
								appendErrorCode();
							}
						}
					} else if (parent.equals("tu")) {
						if (localpart.equals("note") || localpart.equals("prop") // bingo!
								|| localpart.equals("tuv")) {
							appendStartElem(null, null);
							parent = localpart;
							scope.push(parent);
						} else {
							if (localpart.equals("seg")) {
								QName qName = new QName();
								qName.setValues(null, "tuv", "tuv", null);
								XMLAttributesImpl attrs = new XMLAttributesImpl();
								tryToFindAttrs(attrs, errorCode.buf);
								new ErrorDescription(entityScanner.getLineNumber(), entityScanner.getColumnNumber(),
										Messages.getString("tmxeditor.tmxFileValidator.autofix.end")
												+ scope.lastElement());
								appendStartElem(qName, attrs);
								scope.push("tuv");
								parent = "tuv";
								again = true;
							} else {
								if (localpart.equals("tu")) {
									appendEndElem(scope.pop());
									parent = scope.pop();
									scope.push(parent);
									again = true;
								}
							}
						}
					} else if (parent.equals("tuv")) {
						if (localpart.equals("note") || localpart.equals("prop") // bingo!
								|| localpart.equals("seg")) {
							appendStartElem(null, null);
							parent = localpart;
							scope.push(parent);
						} else {
							// errorDes = new ErrorDescription(entityScanner.getLineNumber(),
							// entityScanner.getColumnNumber(),
							// Messages.getString("tmxeditor.tmxFileValidator.autofix.missEnd") + scope.lastElement());
							return;// 当做文本处理
						}
					} else if (parent.equals("seg")) {

					} else if (parent.equals("note")) {
						if (localpart.equals("prop") || localpart.equals("tuv") || localpart.equals("seg")) {
							new ErrorDescription(entityScanner.getLineNumber(), entityScanner.getColumnNumber(),
									Messages.getString("tmxeditor.tmxFileValidator.autofix.missEnd")
											+ scope.lastElement());
							appendEndElem(scope.pop());
							parent = scope.pop();
							scope.push(parent);
							again = true;
						} else if (localpart.equals("tu")) {
							appendEndElem(scope.pop());
							parent = scope.pop();
							scope.push(parent);
							again = true;
						}
					} else if (parent.equals("prop")) {
						if (localpart.equals("prop") || localpart.equals("note") || localpart.equals("tuv")
								|| localpart.equals("seg")) {
							new ErrorDescription(entityScanner.getLineNumber(), entityScanner.getColumnNumber(),
									Messages.getString("tmxeditor.tmxFileValidator.autofix.missEnd")
											+ scope.lastElement());
							appendEndElem(scope.pop());
							parent = scope.pop();
							scope.push(parent);
							again = true;
						}
					} else if (parent.equals("ude")) {

					}
				}
			}
		}
	}

	private void writerDefaultTuv() {

	}

	private void writerDefaultHeader() {
		TmxHeader header = new TmxHeader();
	}

	// <tmx>, <header>, <map/>, <body> , <note>, <prop>, <tu>, <tuv>, <seg>, <ude>.
	// Inline elements <bpt>, <ept>, <hi>, <it>, <ph>, <sub>, <ut>.
	boolean isInvalidTag(String localpart) {

		Set<String> tagSeg = new HashSet<String>();
		tagSeg.add("tmx");
		tagSeg.add("header");
		tagSeg.add("map");
		tagSeg.add("body");
		tagSeg.add("note");
		tagSeg.add("prop");
		tagSeg.add("tu");
		tagSeg.add("tuv");
		tagSeg.add("seg");
		tagSeg.add("ude");
		return !tagSeg.contains(localpart);
	}

	private void appendStartElem(QName qName, XMLAttributesImpl attr) throws IOException {

		if (!errorCode.isEmpty()) {
			new ErrorDescription(errorCode.lineNumber, errorCode.columnNumber, errorCode.getDescription());
			errorCode.clear();
		}
		// set default
		if (qName == null) {
			qName = fCurrentElement;
		}
		if (attr == null) {
			attr = fAttributes;
		}

		writer.write('<');
		writer.write(qName.rawname);
		for (int i = 0; i < attr.getLength(); i++) {
			writer.write(' ');
			writer.write(attr.getQName(i));
			writer.write('=');
			writer.write('\"');
			writer.write(attr.getValue(i));
			writer.write('\"');
		}
		writer.write('>');

		// for debug
		if (debug) {
			StringBuffer buf = new StringBuffer();
			buf.append('<');
			buf.append(qName.rawname);
			for (int i = 0; i < attr.getLength(); i++) {
				buf.append(' ');
				buf.append(attr.getQName(i));
				buf.append('=');
				buf.append('\"');
				buf.append(attr.getValue(i));
				buf.append('\"');
			}
			buf.append('>');
			System.out.println("w====w:write start elem：" + buf.toString());
		}
		writer.flush();
		// end debug
	}

	private void appendErrorCode() throws IOException {
		QName qName = fCurrentElement;
		XMLAttributesImpl attr = fAttributes;

		StringBuilder builder = new StringBuilder();
		builder.append('<').append(qName.rawname);
		for (int i = 0; i < attr.getLength(); i++) {
			builder.append(' ').append(attr.getQName(i)).append('=').append('\"').append(attr.getValue(i)).append('\"');
		}
		builder.append('>');

		errorCode.append(builder.toString());

		// for debug
		if (debug) {
			System.out.println("error code: start elem：" + builder.toString());
		}
		// end debug
	}

	private void appendEndElem(String elem) throws IOException {
		writer.write("</");
		writer.write(elem);
		writer.write(">");

		// for debug
		if (debug) {
			StringBuffer buf = new StringBuffer();
			buf.append("</");
			buf.append(elem);
			buf.append('>');
			System.out.println("w====w:write end elem：" + buf.toString());
		}
		writer.flush();
		// end debug
	}

	private void appendContent(String str) throws IOException {
		writer.write(str);

		// for debug
		if (debug) {
			System.out.println("w====w:write content：" + str);
		}
		writer.flush();
		// end debug
	}

	private boolean appendCharacter(String character) throws IOException {
		if (scope.isEmpty()) {
			return false;
		}
		String parent = scope.lastElement();
		if (parent.equals("note") || parent.equals("prop") || parent.equals("seg")) {
			appendContent(character);
			return true;
		}
		return false;
	}

	private String scanPseudoAttribute(boolean scanningTextDecl, XMLString value) throws IOException {
		String name = entityScanner.scanName();
		if (name == null) {
			error("not found paseudo attribute");
		}
		entityScanner.skipDeclSpaces();
		if (!entityScanner.skipChar('=')) {
			error("not found '='");
		}
		entityScanner.skipDeclSpaces();
		int quote = entityScanner.peekChar();
		if (quote != '\'' && quote != '"') {
			error("not found quote when scan pseudo attribute");
		}
		entityScanner.scanChar();
		int c = entityScanner.scanLiteral(quote, value);
		if (c != quote) {
			fStringBuffer2.clear();
			do {
				fStringBuffer2.append(value);
				if (c != -1) {
					if (c == '&' || c == '%' || c == '<' || c == ']') {
						fStringBuffer2.append((char) entityScanner.scanChar());
					}
					// REVISIT: Even if you could reliably read non-ASCII chars
					// why bother scanning for surrogates here? Only ASCII chars
					// match the productions in XMLDecls and TextDecls. -- mrglavas
					else if (XMLChar.isHighSurrogate(c)) {
						scanSurrogates(fStringBuffer2);
					} else if (XMLChar.isInvalid(c)) {
						String key = scanningTextDecl ? "InvalidCharInTextDecl" : "InvalidCharInXMLDecl";
						error("invalid char '&#" + Integer.toHexString(c) + "'");
						// reportFatalError(key,
						// new Object[] {Integer.toString(c, 16)});
						entityScanner.scanChar();
					}
				}
				c = entityScanner.scanLiteral(quote, value);
			} while (c != quote);
			fStringBuffer2.append(value);
			value.setValues(fStringBuffer2);
		}
		if (!entityScanner.skipChar((char) quote)) {
			error("not found close quote");
		}

		// return
		return name;
	}

	static void error(String error) {
		if (debug) {
			System.err.println(error);
		}
	}

	public void copyQname(QName src, QName dest) {
		dest.localpart = src.localpart;
		dest.prefix = src.prefix;
		dest.rawname = src.rawname;
		dest.uri = src.uri;
	}

	public int testDepth(String str) {
		if (str == null) {
			return -1;
		}
		if (str.equals("tmx"))
			return 1;
		if (str.equals("header") || str.equals("body"))
			return 2;
		if (str.equals("tu") || str.equals("ude"))
			return 3;
		if (str.equals("tuv"))
			return 4;
		if (str.equals("note") || str.equals("prop") || str.equals("seg"))
			return 5;
		else
			return -1;
	}

	class TmxNode {
		int depth;
		QName qName = new QName();
		XMLAttributesImpl attributes = new XMLAttributesImpl();
		XMLStringBuffer xmlbuf = new XMLStringBuffer();
	}

	class TmxNodeStack {
		int defaultSize = 10;
		int position = -1;
		TmxNode[] tmxNodes = new TmxNode[defaultSize];

		void push(TmxNode node) {
			if (position == tmxNodes.length) {
				System.arraycopy(tmxNodes, 0, tmxNodes, 0, position + defaultSize);
			}
		}

		void pop(TmxNode node) {
			if (position < 0) {
				node = null;
			}
			node = tmxNodes[position];
			position--;
		}
	}

	class ErrorDescription {
		ErrorDescription(int lineNumber, int columnNumber, String description) {
			this.lineNumber = lineNumber;
			this.columnNumber = columnNumber;
			this.description = description;
			reportError = true;
			errorFIFO.add(this);
		}

		int lineNumber;
		int columnNumber;

		String description;
	}

	class ErrorCode {
		String description = null;
		int lineNumber = 0;
		int columnNumber = 0;

		XMLStringBuffer buf = new XMLStringBuffer();

		void clear() {
			buf.clear();
		}

		public String getDescription() {
			return description == null || description.isEmpty() ? Messages
					.getString("tmxeditor.tmxFileValidator.autofix.unknown") : description;
		}

		ErrorCode append(String str) {
			buf.append(str);
			setPosition(entityScanner.getLineNumber(), entityScanner.getColumnNumber());
			return this;
		}

		void setDescription(String description) {
			this.description = description;
		}

		boolean isEmpty() {
			return buf.length < 1;
		}

		private void setPosition(int lineNumber, int columnNumber) {
			if (buf.length > 0) {
				this.lineNumber = lineNumber;
				this.columnNumber = columnNumber;
			}
		}

		@Override
		public String toString() {
			return buf.toString();
		}
	}

	public boolean isFinish() {
		return complete;
	}

	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public void clear() {
		try {
			if (writer!= null) {
				writer.close();
			}
			if (tmpFilePath != null) {
				File file = new File(tmpFilePath);
				if (file.exists()) {
					file.delete();
				}
			}
		} catch (Exception e) {}
	}
}
