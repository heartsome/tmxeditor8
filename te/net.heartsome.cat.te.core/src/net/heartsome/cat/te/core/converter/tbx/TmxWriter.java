/**
 * TmxWriter.java
 *
 * Version information :
 *
 * Date:2013-7-1
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.converter.tbx;

import java.io.FileNotFoundException;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.te.core.bean.TmxTemplet;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class TmxWriter extends AbstractWriter {

	private String attibuteString;
	/**
	 * 
	 */
	public TmxWriter() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param filePath
	 * @throws FileNotFoundException
	 */
	public TmxWriter(String filePath) throws FileNotFoundException {
		super(filePath);
		// TODO Auto-generated constructor stub
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.tbx.AbstractWriter#getHeaderXml()
	 */
	@Override
	protected String getHeaderXml(String srcLang) {
		TmxHeader header = TmxTemplet.generateTmxHeader(srcLang, null, null, null, null, null, null);
		return TmxTemplet.header2Xml(header);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.tbx.AbstractWriter#getEndXml()
	 */
	@Override
	protected String getEndXml() {
		// TODO Auto-generated method stub
		return "</body>\n</tmx>";
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.tbx.AbstractWriter#writeTmxTU(net.heartsome.cat.common.bean.TmxTU)
	 */
	@Override
	protected void writeTmxTU(TmxTU tu) {
		// TODO Auto-generated method stub
		writeXmlString(Model2String.TmxTU2TmxXmlString(tu, true,getAttibuteString()));

	}
	/** (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.tbx.AbstractWriter#getWriterEnconding()
	 */
	@Override
	protected String getWriterEnconding() {
		// TODO Auto-generated method stub
		return "utf-8";
	}
	/** @return the attibuteString */
	public String getAttibuteString() {
		return attibuteString;
	}
	/** @param attibuteString the attibuteString to set */
	public void setAttibuteString(String attibuteString) {
		this.attibuteString = attibuteString;
	}

}
