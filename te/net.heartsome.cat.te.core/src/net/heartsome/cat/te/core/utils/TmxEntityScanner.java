package net.heartsome.cat.te.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;

public class TmxEntityScanner {

	final private int defaultSize = 1024;
	private TmxEntity entity = null;
	private boolean finish = false;
	private boolean hasReader = true;
	
	public TmxEntityScanner(String path, String encoding) throws FileNotFoundException {
		entity = new TmxEntity();
		entity.reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
		entity.ch = new char[defaultSize];
		hasReader = true;
	}
	
	public TmxEntityScanner(String path) throws FileNotFoundException {
		entity = new TmxEntity();
		entity.reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
		entity.ch = new char[defaultSize];
		hasReader = true;
	}

	
	public int getLineNumber() {
		return entity.lineNumber;
	}
	
	public int getColumnNumber() {
		return entity.columnNumber;
	}
	
	public int getHasLoad() {
		return entity.hasLoad;
	}
	
	public TmxEntityScanner(XMLStringBuffer buf) {
		entity = new TmxEntity();
		entity.ch = buf.ch;
		entity.count = buf.ch.length;
		hasReader = false;
	}
	
	char peekChar() throws IOException {
		if (entity.position == entity.count) {
			load(0);
		}

		char c = entity.ch[entity.position];
		return (c != '\r') ? c : '\n';
	}

	char scanChar() throws IOException {
		char c = peekChar();
		entity.position++;
		
		if (c == '\n') {
			entity.lineNumber++;
			entity.columnNumber = 1;
			if (entity.position == entity.count) {
				entity.ch[0] = c;
				load(1);
			}
			if (c == '\r') {
				int cc = entity.ch[entity.position++];
				if (cc != '\n' && c != 0x85) {
					entity.position--;
				}
			}
			c = '\n';
		}
		return c;
	}

	String scanName() throws IOException {
		if (entity.position == entity.count) {
			load(0);
		}

		int offset = entity.position;
		if (XMLChar.isNameStart(entity.ch[offset])) {
			if (++entity.position == entity.count) {
				entity.ch[0] = entity.ch[offset];
				offset = 0;
				if (load(1)) {
					entity.columnNumber++;
					return new String(entity.ch, 0, 1);
				}
			}
			while (XMLChar.isName(entity.ch[entity.position])) {
				if (++entity.position == entity.count) {
					int length = entity.position - offset;
					if (length == entity.ch.length) {
						// bad luck we have to resize our buffer
						char[] tmp = new char[entity.ch.length << 1];
						System.arraycopy(entity.ch, offset, tmp, 0, length);
						entity.ch = tmp;
					} else {
						System.arraycopy(entity.ch, offset, entity.ch, 0, length);
					}
					offset = 0;
					if (load(length)) {
						break;
					}
				}
			}
		}
		int length = entity.position - offset;
		entity.columnNumber += length;
		return new String(entity.ch, offset, entity.position - offset);
	}

	boolean scanQName(QName qname) throws IOException {

		if (entity.position == entity.count) {
			load(0);
		}

		// scan qualified name
		int offset = entity.position;
		if (XMLChar.isNCNameStart(entity.ch[offset])) {
			if (++entity.position == entity.count) {
				entity.ch[0] = entity.ch[offset];
				offset = 0;
				if (load(1)) {
					entity.columnNumber++;
					// fSymbolTable.addSymbol(entity.ch, 0, 1);
					String name = new String(entity.ch, 0, 1);
					qname.setValues(null, name, name, null);
					return true;
				}
			}
			int index = -1;
			while (XMLChar.isName(entity.ch[entity.position])) {
				char c = entity.ch[entity.position];
				if (c == ':') {
					if (index != -1) {
						break;
					}
					index = entity.position;
				}
				if (++entity.position == entity.count) {
					int length = entity.position - offset;
					if (length == entity.ch.length) {
						// bad luck we have to resize our buffer
						char[] tmp = new char[entity.ch.length << 1];
						System.arraycopy(entity.ch, offset, tmp, 0, length);
						entity.ch = tmp;
					} else {
						System.arraycopy(entity.ch, offset, entity.ch, 0, length);
					}
					if (index != -1) {
						index = index - offset;
					}
					offset = 0;
					if (load(length)) {
						break;
					}
				}
			}
			int length = entity.position - offset;
			entity.columnNumber += length;
			if (length > 0) {
				String prefix = null;
				String localpart = null;
				// fSymbolTable.addSymbol(entity.ch, offset, length);
				String rawname = new String(entity.ch, offset, length);
				if (index != -1) {
					int prefixLength = index - offset;
					// fSymbolTable.addSymbol(entity.ch, offset, prefixLength);
					prefix = new String(entity.ch, offset, prefixLength);
					int len = length - prefixLength - 1;
					int startLocal = index + 1;
					if (!XMLChar.isNCNameStart(entity.ch[startLocal])) {
						// TODO:
					}
					// fSymbolTable.addSymbol(entity.ch, startLocal, len);
					localpart = new String(entity.ch, startLocal, len);

				} else {
					localpart = rawname;
				}
				qname.setValues(prefix, localpart, rawname, null);
				return true;
			}
		}
		return false;
	}

	int scanLiteral(int quote, XMLString xs) throws IOException {

		if (entity.position == entity.count) {
			load(0);
		} else if (entity.position == entity.count - 1) {
			entity.ch[0] = entity.ch[entity.count - 1];
			load(1);
			entity.position = 0;
		}

		int offset = entity.position;
		int c = entity.ch[offset];
		int newlines = 0;
		if (c == '\r' || c == '\n') {
			do {
				c = entity.ch[entity.position++];
				if (c == '\r') {
					newlines++;
					entity.lineNumber++;
					entity.columnNumber = 1;
					if (entity.position == entity.count) {
						offset = 0;
						if (load(newlines)) {
							break;
						}
					}
					if (entity.ch[entity.position] == '\n') {
						entity.position++;
						offset++;
					} else {
						newlines++;
					}
				} else if (c == '\n') {
					newlines++;
					entity.lineNumber++;
					entity.columnNumber = 1;
					if (entity.position == entity.count) {
						offset = 0;
						entity.baseCharOffset += (entity.position - entity.startPosition);
						entity.position = newlines;
						entity.startPosition = newlines;
						if (
						load(newlines))
							break;
					}
				} else {
					entity.position--;
					break;
				}
			} while (entity.position < entity.count - 1);
			for (int i = offset; i < entity.position; i++) {
				entity.ch[i] = '\n';
			}
			int length = entity.position - offset;
			if (entity.position == entity.count - 1) {
				xs.setValues(entity.ch, offset, length);
				return -1;
			}
		}

		while (entity.position < entity.count) {
			c = entity.ch[entity.position++];
			if ((c == quote && !entity.literal) || c == '%' || !XMLChar.isContent(c)) {
				entity.position--;
				break;
			}
		}
		int length = entity.position - offset;
		entity.columnNumber += length - newlines;
		xs.setValues(entity.ch, offset, length);
		if (entity.position != entity.count) {
			c = entity.ch[entity.position];
			if (c == quote && entity.literal) {
				c = -1;
			}
		} else {
			c = -1;
		}
		return c;
	}

	int scanContent(XMLString xs) throws IOException {

		if (entity.position == entity.count) {
			load(0);
		} else if (entity.position == entity.count - 1) {
			entity.ch[0] = entity.ch[entity.count - 1];
			load(1);
		}

		int offset = entity.position;
		int c = entity.ch[offset];
		int newlines = 0;
		if (c == '\n' || (c == '\r')) {// linefeed, carriage return
			do {
				c = entity.ch[entity.position++];
				if (c == '\r') {
					newlines++;
					entity.lineNumber++;
					entity.columnNumber = 1;
					if (entity.position == entity.count) {
						offset = 0;
						entity.baseCharOffset += (entity.position - entity.startPosition);
						entity.position = newlines;
						entity.startPosition = newlines;
						if (load(newlines)) {
							break;
						}
					}
					if (entity.ch[entity.position] == '\n') {
						entity.position++;
						offset++;
					} else {
						newlines++;
					}
				} else if (c == '\n') {
					newlines++;
					entity.lineNumber++;
					entity.columnNumber = 1;
					if (entity.position == entity.count) {
						offset = 0;
						entity.baseCharOffset += (entity.position - entity.startPosition);
						entity.position = newlines;
						entity.startPosition = newlines;
						if (load(newlines)) {
							break;
						}
					}
				} else {
					entity.position--;
					break;
				}
			} while (entity.position < entity.count - 1);
			for (int i = offset; i < entity.position; i++) {
				entity.ch[i] = '\n';
			}
			int length = entity.position - offset;

			if (entity.position == entity.count - 1) {
				xs.setValues(entity.ch, offset, length);
				return -1;
			}
		}

		while (entity.position < entity.count) {
			c = entity.ch[entity.position++];
			if (!XMLChar.isContent(c)) {
				entity.position--;
				break;
			}
		}
		int length = entity.position - offset;
		entity.columnNumber += length - newlines;
		xs.setValues(entity.ch, offset, length);

		if (entity.position != entity.count) {
			c = entity.ch[entity.position];
			if (c == '\r') {
				c = '\n';
			}
		} else {
			c = -1;
		}
		return c;
	}

	boolean scanData(String delimiter, XMLStringBuffer buff) throws IOException {
		boolean found = false;

		return found;
	}

	boolean skipChar(char ch) throws IOException {
		if (entity.position == entity.count) {
			load(0);
		}
		int cc = entity.ch[entity.position];
		if (cc == ch) {
			entity.position++;
			if (ch == '\n') {
				entity.lineNumber++;
				entity.columnNumber = 1;
			} else {
				entity.columnNumber++;
			}
			return true;
		} else if (ch == '\n' && cc == '\r') {
			if (entity.position == entity.count) {
				entity.ch[0] = (char) cc;
				load(1);
			}
			entity.position++;
			if (entity.ch[entity.position] == '\n') {
				entity.position++;
			}
			entity.lineNumber++;
			entity.columnNumber = 1;
			return true;
		}
		return false;
	}

	boolean skipSpaces() throws IOException {
		if (entity.position == entity.count) {
			load(0);
		}
		int c = entity.ch[entity.position];
		if (XMLChar.isSpace(c)) {
			do {
				boolean finish = false;
				if (c == '\n' || c == '\r') {
					entity.lineNumber++;
					entity.columnNumber = 1;
					if (entity.position == entity.count - 1) {
						entity.ch[0] = (char) c;
						finish = load(1);
						if (!finish) {
							entity.position = 0;
							entity.startPosition = 0;
						}
					}
					if (c == '\r') {
						if (entity.ch[++entity.position] != '\n'){
							entity.position--;
						}
					}
				} else {
					entity.columnNumber++;
				}
				if (!finish) entity.position++;
				if (entity.position == entity.count) {
					load(0);
				}
			} while (XMLChar.isSpace(c = entity.ch[entity.position]));
			return true;
		}
		return false;
	}

	boolean skipString(String str) throws IOException {
		
		if (entity.position == entity.count) {
			load(0);
		}
		
		final int length = str.length();

		for (int i = 0; i < length; i++) {
			char c = entity.ch[entity.position++];
			if (c != str.charAt(i)) {
				entity.position -= i + 1;
				return false;
			}
			if (i < length - 1 && entity.position == entity.count) {
				System.arraycopy(entity.ch, entity.count - i - 1, entity.ch, 0, i + 1);
				if (load(i + 1)) {
					entity.startPosition -= i + 1;
					entity.position -= i + 1;
					return false;
				}
			} 
		}
		entity.columnNumber += length;
		return true;
	}
//	boolean skipString(String str) throws IOException {
//		
//		if (entity.position == entity.count) {
//			load(0);
//		}
//		
//		final int length = str.length();
//		
//		if (arrangeCapacity(length, false)) {
//			final int beforeSkip = entity.position;
//			int afterSkip = entity.position + length - 1;
//			
//			int i = length - 1;
//			
//			while (str.charAt(i--) == entity.ch[afterSkip]) {
//				if (afterSkip-- == beforeSkip) {
//					entity.position = entity.position + length;
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	boolean arrangeCapacity(int length, boolean changeEntity) throws IOException {
		if (entity.count - entity.position >= length) {
			return true;
		}
		while (entity.count - entity.position < length) {
			if (entity.ch.length - entity.position < length) {
				System.arraycopy(entity.ch, entity.position, entity.ch, 0, entity.count - entity.position);
				entity.count = entity.count - entity.position;
				entity.position = 0;
			}

			if (entity.count - entity.position < length) {
				int pos = entity.position;
				boolean b = load(entity.count);
				entity.position = pos;
				if (b)
					break;
			}
		}
		return entity.count - entity.position >= length;
	}

	/**
	 * @param offset
	 * @return end file ? true : false;
	 * @throws IOException ;
	 */
	boolean load(int offset) throws IOException {
		if (!hasReader) {
			finish = true;
			return true;
		}
		
		int length = entity.ch.length - offset;
		int count = entity.reader.read(entity.ch, offset, length);
		if (count != -1) {
			entity.hasLoad += count;
			if (count != 0) {
				entity.count = count + offset;
				entity.position = offset;
			}
		} else {
			entity.count = offset;
			entity.position = offset;
			finish = true;
		}
		return false;
	}

	class TmxEntity {
		public int hasLoad = 0;
		public int baseCharOffset;
		public int columnNumber;
		public BufferedReader reader;
		char[] ch = null;
		int position = 0;
		int count = 0;
		boolean literal;
		public int lineNumber;
		public int startPosition;
	}

	public boolean hasFinish() {
		return finish;
	}
	public boolean skipDeclSpaces() throws IOException {
		// load more characters, if needed
		if (entity.position == entity.count) {
			load(0);
		}

		// skip spaces
		int c = entity.ch[entity.position];
		if (XMLChar.isSpace(c)) {
			do {
				boolean entityChanged = false;
				// handle newlines
				if (c == '\n' || c == '\r') {
					entity.lineNumber++;
					entity.columnNumber = 1;
					if (entity.position == entity.count - 1) {
						entity.ch[0] = (char) c;
						entityChanged = load(1);
						if (!entityChanged) {
							// the load change the position to be 1,
							// need to restore it when entity not changed
							entity.position = 0;
							entity.startPosition = 0;
						}
					}
					if (c == '\r') {
						// REVISIT: Does this need to be updated to fix the
						// #x0D ^#x0A newline normalization problem? -Ac
						if (entity.ch[++entity.position] != '\n') {
							entity.position--;
						}
					}
					/***
					 * NEWLINE NORMALIZATION *** else { if (fCurrentEntity.ch[fCurrentEntity.position + 1] == '\r' &&
					 * external) { fCurrentEntity.position++; } } /
					 ***/
				} else {
					entity.columnNumber++;
				}
				// load more characters, if needed
				if (!entityChanged)
					entity.position++;
				if (entity.position == entity.count) {
					load(0);
				}
			} while (XMLChar.isSpace(c = entity.ch[entity.position]));
			return true;
		}
		return false;
	}
}
