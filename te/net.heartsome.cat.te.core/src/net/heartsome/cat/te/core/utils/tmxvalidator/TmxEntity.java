package net.heartsome.cat.te.core.utils.tmxvalidator;

import java.io.Reader;

/**
 * xni 内部类，纯属 copy
 * @author  Austen
 * @version 
 * @since   JDK1.6
 */
class TmxEntity {
	
	private final int defaultCapability = 8192;
	
	public TmxEntity() {
		ch = new char[defaultCapability];
	}
	
	public TmxEntity(int capability) {
		ch = new char[capability];
	}
	
	public boolean literal;
	
	public int hasLoad = 0;
	
	public int baseCharOffset = 0;
	
	public int lineNumber = 1;

	public int columnNumber = 1;
	
	public int startPosition = 0;

	public int position = 0;

	public int count = 0;
	
	public char[] ch = null;
	
	public Reader reader;
}
