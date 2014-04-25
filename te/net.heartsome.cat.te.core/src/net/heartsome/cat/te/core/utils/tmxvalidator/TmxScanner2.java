package net.heartsome.cat.te.core.utils.tmxvalidator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;

import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.cat.te.core.Activator;
import net.heartsome.cat.te.core.resource.Messages;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 借鉴 xni 内部，修改运行方式。
 * @author Austen
 * @version
 * @since JDK1.6
 */
public class TmxScanner2 {

	private static final Logger LOGGER = LoggerFactory.getLogger(TmxScanner2.class);

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
	/** 自定义 */
	protected static final int SCANNER_STATE_ELEMENT_START = 17;
	protected static final int SCANNER_STATE_ELEMENT_END = 18;
	protected static final int SCANNER_STATE_ELEMENT_SELFCLOSE = 19;

	private boolean complete;
	private boolean scanningAttribute;
	private boolean fNotifyCharRefs;
	private boolean fIsEntityDeclaredVC;

	private int scanState = 0;

	private String tmpFilePath = null;
	private String[] pseudoAttributeValues = new String[3];

	private SymbolTable pitable = new SymbolTable();

	private QName elementQName = new QName();
	private QName fCurrentElement = new QName();
	private QName attributeQName = new QName();

	private XMLAttributesImpl attributes = new XMLAttributesImpl();

	private XMLString fString = new XMLString();
	private XMLString fTempString = new XMLString();
	private XMLString fTempString2 = new XMLString();

	private XMLStringBuffer stringBuffer = new XMLStringBuffer();
	private XMLStringBuffer fStringBuffer = new XMLStringBuffer();
	private XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();
	private XMLStringBuffer fStringBuffer3 = new XMLStringBuffer();

	private TmxEntityScanner2 entityScanner = null;
	
	private boolean illegalCharacter = false;

	/** 提示面板. */
	private ReportBoard board;
	/** 输出工具. */
	private CorrectWriter cwriter;

	public TmxScanner2(String tmpFilePath) {
		this.tmpFilePath = tmpFilePath;
	}

	public void run(IProgressMonitor monitor) {
		long total = 0L;

		// 1.load file
		monitor.beginTask(Messages.getString("tmxeditor.tmxFileValidator.validingTmxFile"), 100);
		board.info(Messages.getString("tmxeditor.tmxFileValidator.validingTmxFile"));
		monitor.worked(1);
		try {
			total = loadFile();
		} catch (Exception e) {
			board.error(e.getMessage());
			cancled();
			return;
		}

		boolean syntaxError = false;

		// 2.loop elements
		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 69);
		subMonitor.beginTask("", 100);
		subMonitor.setTaskName(Messages.getString("tmxeditor.tmxFileValidator.validingTmxFile"));
		int worked = 0;
		setScannerState(SCANNER_STATE_TEXT_DECL);
		boolean finish = false;
		do {
			if (monitor.isCanceled()) {
				cancled();
				return;
			} else {
				int tmp = (int) ((entityScanner.getHasLoad() * 100) / total);
				if (tmp > worked) {
					subMonitor.worked(tmp - worked);
				}
				worked = tmp;
			}

			try {
				next();
			} catch (RepairableException e) {
				 board.info(e);
				syntaxError = true;
				setScannerState(SCANNER_STATE_CONTENT);
			} catch (TmxEndEntityException e) {
				entityScanner.close();
				finish = true;
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
				break;
			}
		} while (!finish);
		subMonitor.done();

		try {
			cwriter.close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}


		// dtd check this file.
		String path = tmpFilePath;

		// let the board know if changed the file
		if (cwriter.hasChanged() || syntaxError) {
			path = cwriter.getLocation();
			board.info(MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.autofix.success"), path));
		} else {
			try {
				cwriter.close();
			} catch (Exception e) {} finally {
				try {
					new File(cwriter.getLocation()).delete();
				} catch (Exception e){}
			}
		}
		
		board.info("-----------------------------------------------");

		board.info(MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.standard.msg"), path));

		
		IProgressMonitor dtdMonitor = new SubProgressMonitor(monitor, 30);
		dtdMonitor.beginTask("", 100);
		dtdMonitor.setTaskName(MessageFormat.format(Messages.getString("tmxeditor.tmxFileValidator.standard.msg"), path));
		
		// DTD...
		final boolean[] dtdError = new boolean[1];
		SAXParser parser = new SAXParser();
		try {
			parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					RepairableException e = new RepairableException(exception.getMessage());
					e.setColumn(exception.getColumnNumber());
					e.setRow(exception.getLineNumber());
					board.info(e);
					dtdError[0] = true;
				}

				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					RepairableException e = new RepairableException(exception.getMessage());
					e.setColumn(exception.getColumnNumber());
					e.setRow(exception.getLineNumber());
					board.info(e);
					dtdError[0] = true;
				}

				@Override
				public void error(SAXParseException exception) throws SAXException {
					RepairableException e = new RepairableException(exception.getMessage());
					e.setColumn(exception.getColumnNumber());
					e.setRow(exception.getLineNumber());
					board.info(e);
					dtdError[0] = true;
				}
			});
			File copyFile = copdyDTD(path);
			parser.parse(new File(path).getAbsolutePath());
			if (copyFile != null) {
				File file = new File(copyFile.getParent() + "tmx14.dtd");
				file.delete();
				copyFile.renameTo(file);
			}
			if (!dtdError[0]) {
				board.info(Messages.getString("tmxeditor.tmxFileValidator.validatePassed"));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			board.error(e.getMessage());
		}
		monitor.done();
	}

	private void cancled() {
		try {
			if (entityScanner != null) {
				entityScanner.close();
			}
			if (cwriter != null) {
				cwriter.close();
				new File(cwriter.getLocation()).delete();
			}
			if (board != null) {
				board.dispose();
			}
		} catch (Exception e) {}

	}

	private File copdyDTD(String path) throws IOException {
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		URL url = bundle.getEntry("res/tmx14.dtd");

		String src = FileLocator.toFileURL(url).getPath();
		String dest = new File(path).getParent() + "tmx14.dtd";

		File file = new File(dest);
		File copyFile = null;
		if (file.exists()) {
			copyFile = new File(file.getAbsoluteFile() + ".copyte");
			file.renameTo(copyFile);
		}

		FileOutputStream fos = null;
		FileInputStream fis = null;
		try {
			fos = new FileOutputStream(new File(dest));
			FileChannel fosfc = fos.getChannel();
			fis = new FileInputStream(new File(src));
			FileChannel fc = fis.getChannel();
			fosfc.transferFrom(fc, 0, fis.available());
		} catch (IOException e) {
			throw e;
		} finally {
			if (fos != null) {
				fos.close();
			}
			if (fis != null) {
				fis.close();
			}
		}
		return copyFile;
	}

	public void setReportBoard(ReportBoard board) {
		this.board = board;
	}

	public void setCorrectWriter(CorrectWriter cwriter) {
		this.cwriter = cwriter;
	}

	protected void scanXMLDecl() throws IOException, RepairableException, TmxEndEntityException {
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

		while (entityScanner.peekChar() != '?') {
			dataFoundForTarget = true;
			String name = scanPseudoAttribute(false, fString);
			switch (state) {
			case STATE_VERSION: {
				if (name.equals("version")) {
					if (!sawSpace) {
						newRepairableException("[pseudo attribute]not found space.");
					}
					version = fString.toString();
					state = STATE_ENCODING;
					if (!"1.0".equals(version)) {
						newRepairableException("[pseudo attribute]version not supported.");
					}
				} else if (name.equals("encoding")) {
					if (!sawSpace) {
						newRepairableException("[pseudo attribute]not found space.");
					}
					encoding = fString.toString();
					state = STATE_STANDALONE;
					newRepairableException("[pseudo attribute]not found version info.");
				} else {
					newRepairableException("[pseudo attribute]:not found version decl");
				}
				break;
			}
			case STATE_ENCODING: {
				if (name.equals("encoding")) {
					if (!sawSpace) {
						newRepairableException("[pseudo attribute]:not found version decl");
					}
					encoding = fString.toString();
					state = STATE_STANDALONE;
				} else if (name.equals("standalone")) {
					if (!sawSpace) {
						newRepairableException("[pseudo attribute]:not found space.");
					}
					standalone = fString.toString();
					state = STATE_DONE;
					if (!standalone.equals("yes") && !standalone.equals("no")) {
						newRepairableException("invalid vaule, needs 'yes' or 'no'");//
					}
				} else {
					throw new RepairableException("not found encoding decl");
				}
				break;
			}
			case STATE_STANDALONE: {
				if (name.equals("standalone")) {
					if (!sawSpace) {
						throw new RepairableException("not found space");//
					}
					standalone = fString.toString();
					state = STATE_DONE;
					if (!standalone.equals("yes") && !standalone.equals("no")) {

						newRepairableException("[pseudo attribute]invalid vaule, needs 'yes' or 'no'");//
					}
				} else {
					newRepairableException("[pseudo attribute]not found encoding decl");
				}
				break;
			}
			default: {
				throw new RepairableException("[pseudo attribute]not found more pseudo attrbuites");
			}
			}
			sawSpace = entityScanner.skipDeclSpaces();
		}
		// restore original literal value
		// if (currLiteral)
		// currEnt.literal = true;

		if (!dataFoundForTarget && version == null) {
			newRepairableException("[pseudo attribute]miss pseudo attribute 'version'");
		}

		// end
		if (!entityScanner.skipChar('?')) {
			newRepairableException("[pseudo attribute]not found end flag '?'");
		}
		if (!entityScanner.skipChar('>')) {
			newRepairableException("[pseudo attribute]not found end flag '>'");
		}

		// fill in return array
		pseudoAttributeValues[0] = version;
		pseudoAttributeValues[1] = encoding;
		pseudoAttributeValues[2] = standalone;

		cwriter.writeXmlDecl("1.0", encoding, "no");
	}

	protected void scanDoctype() throws IOException, RepairableException, TmxEndEntityException {
		// spaces
		if (!entityScanner.skipSpaces()) {
			newRepairableException("[doctype]:Miss space after DOCTYPE.");
		}

		// root element name
		String fDoctypeName = entityScanner.scanName();
		if (fDoctypeName == null) {
			newRepairableException("[doctype]:Miss root element.");
		}

		// external id
		String[] fStrings = new String[3];
		String fDoctypeSystemId = null;
		String fDoctypePublicId = null;
		if (entityScanner.skipSpaces()) {
			scanExternalID(fStrings, false);
			fDoctypeSystemId = fStrings[0];
			fDoctypePublicId = fStrings[1];
			entityScanner.skipSpaces();
		} else {
			newRepairableException("[doctype]:Miss space after root element.");
		}
		if (entityScanner.skipChar('>')) {
			cwriter.writeDoctype(fDoctypeName, fDoctypePublicId, fDoctypeSystemId);
		} else {
			newRepairableException("[doctype]:Miss '>' in the end.");
		}
	}

	protected void scanPI() throws IOException, TmxEndEntityException {
		// 未实现 TODO
	}

	protected void scanStartElement() throws IOException, RepairableException, TmxEndEntityException {
		entityScanner.scanQName(elementQName);
		attributes.removeAllAttributes();
		copyQname(elementQName, fCurrentElement);
		do {
			boolean sawSpace = entityScanner.skipSpaces();
			int c = entityScanner.peekChar();
			if (c == '>') {
				entityScanner.scanChar();
				break;
			} else if (c == '/') {
				entityScanner.scanChar();
				if (!entityScanner.skipChar('>')) {
					newRepairableException("[start element] miss '>' after '/'");
				}
				break;
			} else if (!XMLChar.isNameStart(c) || !sawSpace) {
				setScannerState(SCANNER_STATE_CONTENT);
				newRepairableException("[start element] illegal char '" + Integer.toHexString(c) + '\'');
			}
			scanAttribute(attributes);
		} while (true);

		cwriter.writeStartElement(fCurrentElement, attributes);
	}

	protected void scanEndElement() throws IOException, TmxEndEntityException, RepairableException {
		entityScanner.scanQName(elementQName);
		entityScanner.skipSpaces();
		if (!entityScanner.skipChar('>')) {
			newRepairableException("[end element]miss '>' when scan end element.");
		}
		cwriter.writeEndElement(elementQName);
	}

	protected void scanCDATASection(boolean complete2) {
		// 未实现 TODO
	}

	protected int scanContent() throws IOException, TmxEndEntityException, RepairableException {

		int line = entityScanner.getLineNumber();
		int column = entityScanner.getOffsetNumber();

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

		// String str = new String(content.ch, content.offset, content.length);
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
					// TODO reportFatalError("CDEndInContent", null);
				}
			}
			// TODO fInScanContent = false;
			c = -1;
		}

		try {
			cwriter.write(content);
		} catch (RepairableException e) {
			e.setColumn(column);
			e.setRow(line);
			throw e;
		}

		return c;
	}

	protected void scanComment() {
		// 未实现 TODO
	}

	private void next() throws IOException, RepairableException, TmxEndEntityException {
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
								// TODO reportFatalError("InvalidCharInContent", new Object[] { Integer.toString(c,
								// 16)});
								entityScanner.scanChar();
							}
						}
					} while (complete);
				}
				break;
			}
			case SCANNER_STATE_TEXT_DECL: {
				if (entityScanner.skipString("<?xml")) {
					if (XMLChar.isName(entityScanner.peekChar())) {// 如果是 PI
						fStringBuffer.clear();
						fStringBuffer.append("xml");
						while (XMLChar.isNCName(entityScanner.peekChar())) {
							fStringBuffer.append((char) entityScanner.scanChar());
						}
						String target = pitable.addSymbol(fStringBuffer.ch, fStringBuffer.offset, fStringBuffer.length);
						scanPIData(target, fTempString);
					} else {// 检测 xml 声明
						scanXMLDecl();
					}
				}
				setScannerState(SCANNER_STATE_CONTENT);
				break;
			}
			case SCANNER_STATE_START_OF_MARKUP: {
				// fMarkupDepth++;
				if (entityScanner.skipChar('/')) {
					scanEndElement();
					setScannerState(SCANNER_STATE_CONTENT);
				} else if (XMLChar.isNameStart(entityScanner.peekChar())) {
					scanStartElement();
					setScannerState(SCANNER_STATE_CONTENT);
				} else if (entityScanner.skipChar('!')) {
					if (entityScanner.skipChar('-')) {
						if (!entityScanner.skipChar('-')) {
							newRepairableException("InvalidCommentStart");
						}
						setScannerState(SCANNER_STATE_COMMENT);
						again = true;
					} else if (entityScanner.skipString("[CDATA[")) {
						setScannerState(SCANNER_STATE_CDATA);
						again = true;
					} else if (entityScanner.skipString("DOCTYPE")) {
						setScannerState(SCANNER_STATE_DOCTYPE);
						again = true;
					} else {
						newRepairableException("[illegal]:illegal syntax");
					}
				} else if (entityScanner.skipChar('?')) {
					setScannerState(SCANNER_STATE_PI);
					again = true;
				}
				// else if (isValidNameStartHighSurrogate(entityScanner.peekChar())) {
				// scanStartElement();
				// setScannerState(SCANNER_STATE_CONTENT);
				// }
				else {
					// TODO reportFatalError("MarkupNotRecognizedInContent", null);
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
				setScannerState(SCANNER_STATE_CONTENT);
				if (entityScanner.skipChar('#')) {
					scanCharReference();
				} else {
					scanEntityReference();
				}
				break;
			}
			case SCANNER_STATE_DOCTYPE: {
				scanDoctype();
				setScannerState(SCANNER_STATE_CONTENT);
			}
			}
		} while (complete || again);
	}

	protected void scanExternalID(String[] identifiers, boolean optionalSystemId) throws IOException,
			TmxEndEntityException, RepairableException {

		String systemId = null;
		String publicId = null;
		if (entityScanner.skipString("PUBLIC")) {
			if (!entityScanner.skipSpaces()) {
				newRepairableException("SpaceRequiredAfterPUBLIC");
			}
			scanPubidLiteral(fString);
			publicId = fString.toString();

			if (!entityScanner.skipSpaces() && !optionalSystemId) {
				newRepairableException("SpaceRequiredBetweenPublicAndSystem");
			}
		}

		if (publicId != null || entityScanner.skipString("SYSTEM")) {
			if (publicId == null && !entityScanner.skipSpaces()) {
				newRepairableException("SpaceRequiredAfterSYSTEM");
			}
			int quote = entityScanner.peekChar();
			if (quote != '\'' && quote != '"') {
				if (publicId != null && optionalSystemId) {
					// looks like we don't have any system id
					// simply return the public id
					identifiers[0] = null;
					identifiers[1] = publicId;
					return;
				}
				newRepairableException("QuoteRequiredInSystemID");
			}
			entityScanner.scanChar();
			XMLString ident = fString;
			if (entityScanner.scanLiteral(quote, ident) != quote) {
				fStringBuffer.clear();
				do {
					fStringBuffer.append(ident);
					int c = entityScanner.peekChar();
					if (XMLChar.isMarkup(c) || c == ']') {
						fStringBuffer.append((char) entityScanner.scanChar());
					} else if (XMLChar.isHighSurrogate(c)) {
						scanSurrogates(fStringBuffer);
					} else if (XMLChar.isInvalid(c)) {
						newRepairableException("InvalidCharInSystemID");
						entityScanner.scanChar();
					}
				} while (entityScanner.scanLiteral(quote, ident) != quote);
				fStringBuffer.append(ident);
				ident = fStringBuffer;
			}
			systemId = ident.toString();
			if (!entityScanner.skipChar((char) quote)) {
				newRepairableException("SystemIDUnterminated");
			}
		}

		// store result in array
		identifiers[0] = systemId;
		identifiers[1] = publicId;
	}

	private boolean scanPubidLiteral(XMLString literal) throws TmxEndEntityException, IOException, RepairableException {
		int quote = entityScanner.scanChar();
		if (quote != '\'' && quote != '"') {
			newRepairableException("QuoteRequiredInPublicID");
			return false;
		}

		fStringBuffer.clear();
		// skip leading whitespace
		boolean skipSpace = true;
		boolean dataok = true;
		while (true) {
			int c = entityScanner.scanChar();
			if (c == ' ' || c == '\n' || c == '\r') {
				if (!skipSpace) {
					// take the first whitespace as a space and skip the others
					fStringBuffer.append(' ');
					skipSpace = true;
				}
			} else if (c == quote) {
				if (skipSpace) {
					// if we finished on a space let's trim it
					fStringBuffer.length--;
				}
				literal.setValues(fStringBuffer);
				break;
			} else if (XMLChar.isPubid(c)) {
				fStringBuffer.append((char) c);
				skipSpace = false;
			} else if (c == -1) {
				newRepairableException("PublicIDUnterminated");
				return false;
			} else {
				dataok = false;
				newRepairableException("InvalidCharInPublicID");
			}
		}
		return dataok;
	}

	private long loadFile() throws UnsupportedEncodingException, FileNotFoundException, IOException {
		File file = new File(tmpFilePath);
		String encoding = FileEncodingDetector.detectFileEncoding(file);
		if (encoding == null) {// bad luck! set default.
			encoding = "UTF-8";
		}
		long l = countChar(file, encoding);

		BufferedReader reader = null;
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

		TmxEntity entity = new TmxEntity();
		entity.reader = reader;
		entityScanner = new TmxEntityScanner2(entity);

		//skip BOM
		reader.mark(1);
		char[] bom = new char[1];
		reader.read(bom);
		if (bom[0] != '\ufeff') {
			reader.reset();
		}
		
		if (cwriter != null) {
			cwriter.setEncoding(cwriter.isCorrectEncoding() ? "UTF-8" : encoding);
		}

		return l;
	}

	private long countChar(File file, String chs) throws IOException {
		BufferedReader r = null;
		try {
			long l = 0L;
			int count = 0;
			char[] ch = new char[8192];
			r = new BufferedReader(new InputStreamReader(new FileInputStream(file), chs));
			while ((count = r.read(ch)) != -1) {
				l += count;
			}
			return l;
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private String scanPseudoAttribute(boolean scanningTextDecl, XMLString value) throws IOException,
			RepairableException, TmxEndEntityException {
		String name = entityScanner.scanName();
		if (name.isEmpty()) {
			newRepairableException("not found Pseudo Attribute name", entityScanner.getLineNumber(),
					entityScanner.getOffsetNumber());
		}
		entityScanner.skipDeclSpaces();
		if (!entityScanner.skipChar('=')) {
			newRepairableException("not found '=' when scan Pseudo Attribute", entityScanner.getLineNumber(),
					entityScanner.getOffsetNumber());
		}
		entityScanner.skipDeclSpaces();
		int quote = entityScanner.peekChar();
		if (quote != '\'' && quote != '"') {
			newRepairableException("not found 'quote' when scan Pseudo Attribute", entityScanner.getLineNumber(),
					entityScanner.getOffsetNumber());
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
						entityScanner.scanChar();
						// TODO should we report error, or skip this char silence?
						// error("Invalid Char in xml declaration : '&#" + Integer.toHexString(c) + "'");
					}
				}
				c = entityScanner.scanLiteral(quote, value);
			} while (c != quote);
			fStringBuffer2.append(value);
			value.setValues(fStringBuffer2);
		}
		if (!entityScanner.skipChar((char) quote)) {
			throw new RepairableException("not found close quote");
		}
		return name;
	}

	private void scanPIData(String target, XMLString xs) throws IOException, TmxEndEntityException {
		// check target
		if (target.length() == 3) {
			char c0 = Character.toLowerCase(target.charAt(0));
			char c1 = Character.toLowerCase(target.charAt(1));
			char c2 = Character.toLowerCase(target.charAt(2));
			if (c0 == 'x' && c1 == 'm' && c2 == 'l') {
				// TODO 非法命名
				return;
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
					// TODO reportFatalError("ColonNotLegalWithNS", new Object[] {colonName.toString()});
					entityScanner.skipSpaces();
				} else {
					// TODO reportFatalError("SpaceRequiredInPI", null);
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

	private void scanAttribute(XMLAttributesImpl attributes) throws RepairableException, IOException,
			TmxEndEntityException {

		entityScanner.scanQName(attributeQName);

		entityScanner.skipSpaces();
		if (!entityScanner.skipChar('=')) {
			throw new RepairableException("[attribute]miss '='.");
		}
		entityScanner.skipSpaces();

		// content
		int oldLen = attributes.getLength();
		int attrIndex = attributes.addAttribute(attributeQName, XMLSymbols.fCDATASymbol, null);

		if (oldLen == attributes.getLength()) {
			newRepairableException("[attribute]multiplay attribute-key.");
		}

		// Scan attribute value and return true if the un-normalized and normalized value are the same
		boolean isSameNormalizedAttr = scanAttributeValue(fTempString, fTempString2, attributeQName.rawname,
				fIsEntityDeclaredVC, fCurrentElement.rawname, null);

		attributes.setValue(attrIndex, fTempString.toString());

		// If the non-normalized and normalized value are the same, avoid creating a new string.
		if (!isSameNormalizedAttr) {
			attributes.setNonNormalizedValue(attrIndex, fTempString2.toString());
		}
		attributes.setSpecified(attrIndex, true);
	}

	private boolean scanAttributeValue(XMLString value, XMLString nonNormalizedValue, String atName,
			boolean checkEntities, String eleName, TmxEntityScanner2 scanner) throws RepairableException, IOException,
			TmxEndEntityException {
		// set default
		if (scanner == null) {
			scanner = entityScanner;
		}

		int quote = scanner.peekChar();
		if (quote != '\'' && quote != '"') {
			newRepairableException("[attribute value]miss start quote when scan attribute value.");
		}

		scanner.scanChar();
		int fEntityDepth = 0;
		int entityDepth = fEntityDepth;

		int c = scanner.scanLiteral(quote, value);

		int fromIndex = 0;
		if (c == quote && (fromIndex = isUnchangedByNormalization(value)) == -1) {
			/** Both the non-normalized and normalized attribute values are equal. **/
			nonNormalizedValue.setValues(value);
			int cquote = scanner.scanChar();
			if (cquote != quote) {
				newRepairableException("[attribute value]miss end quote when scan attribute value.");
			}
			return true;
		}

		// 非转义内容
		fStringBuffer2.clear();
		fStringBuffer2.append(value);
		normalizeWhitespace(value, fromIndex);

		if (c != quote) {
			scanningAttribute = true;
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
							newRepairableException("[attribute value]miss reference name.");
						} else if (entityDepth == fEntityDepth) {
							fStringBuffer2.append(entityName);
						}
						if (!scanner.skipChar(';')) {
							newRepairableException("[attribute value]miss end flag ';' when scan character reference.");
						} else if (entityDepth == fEntityDepth) {
							fStringBuffer2.append(';');
						}
						if (entityName.equals("amp")) {
							fStringBuffer.append('&');
						} else if (entityName.equals("apos")) {
							fStringBuffer.append('\'');
						} else if (entityName.equals("lt")) {
							fStringBuffer.append('<');
						} else if (entityName.equals("gt")) {
							fStringBuffer.append('>');
						} else if (entityName.equals("quot")) {
							fStringBuffer.append('"');
						} else {
							newRepairableException("[attribute value]illegal reference name '" + entityName + "'.");
						}
					}
				} else if (c == '<') {
					newRepairableException("[attribute value]illegal character '<'.");
					// scanner.scanChar();
					// if (entityDepth == fEntityDepth) {
					// fStringBuffer2.append((char) c);
					// }
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
			scanningAttribute = false;
		}
		nonNormalizedValue.setValues(fStringBuffer2);

		// quote
		int cquote = scanner.scanChar();
		if (cquote != quote) {
			throw new RepairableException("attr_miss_endquote");
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
				if (c == 0x09 || c == 0x0d || c == 0x0a) {
					continue;
				}
				value.ch[i] = ' ';
				illegalCharacter = true;
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

	private int scanCharReferenceValue(XMLStringBuffer buf, XMLStringBuffer buf2) throws IOException,
			TmxEndEntityException {
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
		} else {
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
			// String literal = "#" + (hex ? "x" : "") + fStringBuffer3.toString();
			if (!scanningAttribute) {
				// fCharRefLiteral = literal;
			}
		}
		return value;
	}

	private void normalizeWhitespace(XMLString value) {
		normalizeWhitespace(value, 0);
	}

	private void scanEntityReference() throws IOException, TmxEndEntityException {
		// name
		String name = entityScanner.scanName();
		if (name == null) {
			return;
		}
		// 符合标准
		if (name.equals("amp") || name.equals("lt") || name.equals("gt") || name.equals("apos") || name.equals("quot")) {
			if (entityScanner.skipChar(';')) {
			}
		} else {
		}
	}

	private void scanCharReference() throws IOException, TmxEndEntityException {
		fStringBuffer2.clear();
		boolean hex = entityScanner.peekChar() == 'x';
		int ch = scanCharReferenceValue(fStringBuffer2, null);
		if (ch != -1) {
			int c = Integer.valueOf(fStringBuffer3.toString(), hex ? 16 : 10);
			if (c < 0x20 && !XMLChar.isSpace(c)) {
				return;
			}
		}
	}

	private boolean scanSurrogates(XMLStringBuffer buf) throws IOException, TmxEndEntityException {
		int high = entityScanner.scanChar();
		int low = entityScanner.peekChar();
		if (!XMLChar.isLowSurrogate(low)) {
			error("invalid char in content");
			return false;
		}

		entityScanner.scanChar();

		int c = XMLChar.supplemental((char) high, (char) low);

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

	private void copyQname(QName src, QName dest) {
		dest.localpart = src.localpart;
		dest.prefix = src.prefix;
		dest.rawname = src.rawname;
		dest.uri = src.uri;
	}

	/**
	 * 只设置调试信息。
	 * @param msg
	 * @throws RepairableException
	 *             ;
	 */
	private void newRepairableException(String msg) throws RepairableException {
		newRepairableException(Messages.getString("tmxeditor.tmxFileValidator.autofix.unknown"), msg);
	}

	private void newRepairableException(String reportMsg, String debugMsg) throws RepairableException {
		newRepairableException(reportMsg, entityScanner.getLineNumber(), entityScanner.getOffsetNumber() + 1);
	}

	private void newRepairableException(String msg, int line, int column) throws RepairableException {
		RepairableException e = new RepairableException(msg);
		e.setColumn(column);
		e.setRow(line);
		throw e;
	}

	static void error(String error) {
		System.err.println(error);
	}

	class FatalErrorException extends TmxValidatorException {
		private static final long serialVersionUID = 1L;

		public FatalErrorException() {
			super();
		}

		public FatalErrorException(String message) {
			super(message);
		}

		public FatalErrorException(Throwable cause) {
			super(cause);
		}
	}

	class TmxSchemaException extends TmxValidatorException {
		private static final long serialVersionUID = 1L;

		public TmxSchemaException() {
			super();
		}

		public TmxSchemaException(String message) {
			super(message);
		}

		public TmxSchemaException(Throwable cause) {
			super(cause);
		}
	}
}
