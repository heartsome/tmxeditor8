/**
 * AppendTmxWriter.java
 *
 * Version information :
 *
 * Date:2013-7-4
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.converter.tbx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDNav;

/**
 * 追加到以有TMX文件的后面的Writer
 * @author yule
 * @version
 * @since JDK1.6
 */
public class AppendTmxWriter extends TmxWriter {

	private static Logger LOGGER = LoggerFactory.getLogger(AppendTmxWriter.class);

	private VTDUtils vtdUtil = null;
	/**
	 * 原始TMX文件的前部分
	 */
	private String tmxBegainString;
	/**
	 * 原始TMX文件的后部分
	 */
	private String tmxEndString;

	private long offset = 0l;

	private String srcLang;

	private boolean istruncateFile;
	
	private String appendTmxEnconding;

	/**
	 * @param filePath
	 *            :tmxfilePath
	 * @throws IOException
	 * @throws ParseException
	 */
	public AppendTmxWriter(String filePath) throws Exception {
		vtdUtil = new VTDUtils();
		File f = new File(filePath);		
		vtdUtil.parseFile(f, false);
		appendTmxEnconding =FileEncodingDetector.detectFileEncoding(f);
		setSrcLang();
		setTMXString();
		try {
			out = new FileOutputStream(new File(filePath), true);
		} catch (FileNotFoundException e) {
			LOGGER.error(MessageFormat.format("R:{0}", filePath + "not exist"));
			return;
		}
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.tbx.TmxWriter#getHeaderXml(java.lang.String)
	 */
	@Override
	protected String getHeaderXml(String srcLang) {
		return tmxBegainString;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.tbx.TmxWriter#getEndXml()
	 */
	@Override
	protected String getEndXml() {
		return istruncateFile ? tmxEndString : "";
	}

	/**
	 * 得到Tmx的前部分内容
	 * @return ;
	 */
	private void setTMXString() {
		if (-1 != vtdUtil.pilot("/tmx/body/tu[last()]")) {
			try {
				VTDNav vn = vtdUtil.getVTDNav();
				long l;
				l = vn.getElementFragment();
				int os = (int) l + (int) (l >> 32);
				offset = os;
				int len = vn.getXML().length() - os;
				tmxBegainString = "";// vn.toRawString(0, os);
				tmxEndString = vn.toRawString(os, len);

			} catch (NavException e) {
				LOGGER.error(" R:Wrong XML format", e);
				e.printStackTrace();
			}

		}

	}

	private void truncateFile() {
		FileChannel channel = out.getChannel();
		try {
			channel.truncate(offset);
		} catch (IOException e) {
			LOGGER.error("R:truncate file failed", e);
		}
	}

	public static void main(String[] args) throws Exception {
		AppendTmxWriter writer = new AppendTmxWriter("e:/bigTmxTest/3.xml.tmx");
		System.out.println(writer);
		System.out.println(writer.getEndXml());
	}

	/** @return the srcLang */
	public String getSrcLang() {
		return srcLang;
	}

	/**
	 * @param srcLang
	 *            the srcLang to set
	 */
	private void setSrcLang() {
		if (-1 != vtdUtil.pilot("/tmx/header")) {
			try {
				VTDNav vn = vtdUtil.getVTDNav();

				int srcLangIndex = vn.getAttrVal("srclang");
				if (srcLangIndex != -1) {
					this.srcLang = vn.toString(srcLangIndex).trim();
				}
			} catch (NavException e) {
				LOGGER.error("R: Wrong XML format", e);
				e.printStackTrace();
			}

		}

	}

	public boolean canAppend(String tgtSrcLang) {
		if (tgtSrcLang == null || null == getSrcLang()) {
			return false;
		}
		if("*all*".equalsIgnoreCase(getSrcLang())){// 添加源语言为 all的时候
			return true;
		}
		if (tgtSrcLang.equalsIgnoreCase(getSrcLang())) {
			return true;
		} else {
			return false;
		}
	}

	public void startAppend() {
		truncateFile();
		istruncateFile = true;
		vtdUtil.clear();
	}
	/**添加追加内容与源文件一致的编码 
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.tbx.TmxWriter#getWriterEnconding()
	 */
	@Override
	protected String getWriterEnconding() {
	   if(null == appendTmxEnconding ||appendTmxEnconding.isEmpty() ){		   
		   return super.getWriterEnconding();
	   }
	   return appendTmxEnconding;
	}
}
