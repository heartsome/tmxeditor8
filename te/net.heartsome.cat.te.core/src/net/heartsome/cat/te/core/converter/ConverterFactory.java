/**
 * ConverterFactory.java
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

package net.heartsome.cat.te.core.converter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.heartsome.cat.te.core.converter.csv.Csv2Tmx;
import net.heartsome.cat.te.core.converter.csv.Tmx2Csv;
import net.heartsome.cat.te.core.converter.docx.Docx2Tmx;
import net.heartsome.cat.te.core.converter.docx.Tmx2Docx;
import net.heartsome.cat.te.core.converter.hstm.Hstm2Tmx;
import net.heartsome.cat.te.core.converter.hstm.Tmx2Hstm;
import net.heartsome.cat.te.core.converter.tbx.Tbx2Tmx;
import net.heartsome.cat.te.core.converter.tbx.Tmx2Tbx;
import net.heartsome.cat.te.core.converter.txt.Tmx2Txt;
import net.heartsome.cat.te.core.converter.txt.Txt2Tmx;
import net.heartsome.cat.te.core.converter.xlsx.Tmx2xlsx;
import net.heartsome.cat.te.core.converter.xlsx.Xlsx2Tmx;

public final class ConverterFactory {

	public final static int FILE_TYPE_DOCX = 1;

	public final static int FILE_TYPE_CSV = 2;

	public final static int FILE_TYPE_XLSX = 3;

	public final static int FILE_TYPE_TXT = 4;

	public final static int FILE_TYPE_TBX = 5;

	public final static int FILE_TYPE_HSTM = 6;

	/**
	 * Tmx转换成其他文件
	 * @param fileType
	 * @return ;
	 */
	public static AbstractTmx2File getTmx2FileConverter(int fileType) {
		switch (fileType) {
		case FILE_TYPE_DOCX:
			return new Tmx2Docx();

		case FILE_TYPE_CSV:
			return new Tmx2Csv();

		case FILE_TYPE_XLSX:
			return new Tmx2xlsx();

		case FILE_TYPE_TXT:
			return new Tmx2Txt();

		case FILE_TYPE_TBX:
			return new Tmx2Tbx();

		case FILE_TYPE_HSTM:
			return new Tmx2Hstm();

		default:
			return null;
		}

	}

	/**
	 * 其他文件转化成Tmx
	 * @param sourceFile
	 * @return ;
	 */
	public static AbstractFile2Tmx getFile2TmxConverter(String sourceFile) {
		return getFile2TmxConverter(getFileType(sourceFile));
	}

	/**
	 * 其他文件转化成Tmx
	 * @param sourceFile
	 * @return ;
	 */
	public static AbstractFile2Tmx getFile2TmxConverter(int fileType) {
		switch (fileType) {

		case FILE_TYPE_DOCX:
			return new Docx2Tmx();

		case FILE_TYPE_CSV:
			return new Csv2Tmx();

		case FILE_TYPE_XLSX:
			return new Xlsx2Tmx();

		case FILE_TYPE_TXT:
			return new Txt2Tmx();

		case FILE_TYPE_TBX:
			return new Tbx2Tmx();

		case FILE_TYPE_HSTM:
			return new Hstm2Tmx();

		default:
			return null;
		}
	}

	/**
	 * 文件名转换成文件类型
	 * @param sourceFile
	 * @return ;
	 */
	public static int getFileType(String sourceFile) {
		if (null == sourceFile || sourceFile.trim().isEmpty()) {
			return -1;
		}
		String tempName = sourceFile.toLowerCase(Locale.ENGLISH);
		if (tempName.endsWith(".docx")) {
			return FILE_TYPE_DOCX;
		} else if (tempName.endsWith(".csv")) {
			return FILE_TYPE_CSV;
		} else if (tempName.endsWith(".xlsx")) {
			return FILE_TYPE_XLSX;
		} else if (tempName.endsWith(".txt")) {
			return FILE_TYPE_TXT;
		} else if (tempName.endsWith(".tbx")) {
			return FILE_TYPE_TBX;
		} else if (tempName.endsWith(".hstm")) {
			return FILE_TYPE_HSTM;
		} else {
			return -1;
		}
	}

	public static Map<Integer, String> getAllTmx2FileConverterName() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(FILE_TYPE_DOCX, Tmx2Docx.NAME);
		map.put(FILE_TYPE_CSV, Tmx2Csv.NAME);
		map.put(FILE_TYPE_XLSX, Tmx2xlsx.NAME);
		map.put(FILE_TYPE_TXT, Tmx2Txt.NAME);
		map.put(FILE_TYPE_TBX, Tmx2Tbx.NAME);
		map.put(FILE_TYPE_HSTM, Tmx2Hstm.NAME);
		return map;
	}
}
