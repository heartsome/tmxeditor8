/**
 * Tbx2Tmx.java
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

package net.heartsome.cat.te.core.converter.tbx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.te.core.bean.File2TmxConvertBean;
import net.heartsome.cat.te.core.bean.TmxTemplet;
import net.heartsome.cat.te.core.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tbx转换成TMX
 * @author yule
 * @version
 * @since JDK1.6
 */
public class Tbx2Tmx extends net.heartsome.cat.te.core.converter.AbstractFile2Tmx {
	/**
	 * 记录日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Tmx2Tbx.class);
	/**
	 * TBX Reader
	 */
	private TbxReader tbxReader = null;
	/**
	 * TMXTU的缓存大小
	 */
	private static final int cacheSzie = 500;
	/**
	 * 缓存TMXtu
	 */
	private List<TmxTU> cache = new ArrayList<TmxTU>(cacheSzie);

	private String attributeString;

	public void doCovnerter(File2TmxConvertBean file2TmxBean, IProgressMonitor monitor) throws Exception {
		tbxReader = new TbxReader(file2TmxBean.sourceFilePath);

		attributeString = Model2String.getCustomArributeXML(file2TmxBean.customeAttr);

		try {
			tbxReader.start();
		} catch (Exception e) {
			LOGGER.error("", e);
			throw new Exception(Messages.getString("convert.tbx2tmx.parseTbxError"));
		}
		int total = tbxReader.getTotal() / cacheSzie;
		monitor.beginTask("", total == 0 ? 1 : total);
		AbstractWriter writer = getWriter(file2TmxBean);
		if (writer instanceof TmxWriter) {
			TmxWriter tmxWriter = (TmxWriter) writer;
			tmxWriter.setAttibuteString(attributeString);

		}
		writeNewTmx(writer, monitor);
	}

	/**
	 * 将缓存内容写入到 文件中，并且清空缓存内容
	 * @param cache
	 * @param tmxWriter
	 *            ;
	 */
	private void writeTmxTU(List<TmxTU> cache, AbstractWriter tmxWriter) {
		if (cache.isEmpty()) {
			return;
		}
		tmxWriter.writeBody(cache);
		cache.clear();
	}

	/**
	 * 将所有内容写在文件中
	 * @param path
	 *            ：文件路径
	 * @param tmxWriter
	 *            ：写入文件的Writer
	 * @return ;
	 * @throws Exception
	 */
	public boolean writeNewTmx(AbstractWriter tmxWriter, IProgressMonitor monitor) throws Exception {
		if (null == tmxWriter) {
			return false;
		}
		try {
			String xmlDecl = TmxTemplet.genertateTmxXmlDeclar();
			tmxWriter.writeXmlString(xmlDecl);
			TmxHeader header = TmxTemplet.generateTmxHeader(tbxReader.getSrcLang(), "unknown", "sentence",
					"Termbase Exchange", null, null, null);
			tmxWriter.writeXmlString("<tmx version=\"1.4\">\n");
			tmxWriter.writeHeader(TmxTemplet.header2Xml(header));
			tmxWriter.writeXmlString("<body>\n");
			while (tbxReader.hasNext()) {
				TmxTU[] tus = tbxReader.readNextTermEntry();
				if (cache.size() != cacheSzie) {
					cache.addAll((Arrays.asList(tus)));
					continue;
				}
				if (monitor.isCanceled()) {
					return false;
				}
				writeTmxTU(cache, tmxWriter);
				monitor.worked(1);
			}
			if (monitor.isCanceled()) {
				return false;
			}
			writeTmxTU(cache, tmxWriter);
			monitor.done();
			return true;
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
		} catch (Exception e) {
			LOGGER.error("读取TermEntry错误", e);
			throw new Exception(Messages.getString("convert.tbx2tmx.writeTmx.error"));
		} finally {
			tmxWriter.writeEnd();
			tmxWriter.closeOutStream();
			monitor.done();
		}
		return false;
	}

	/**
	 * 获取正确的Writer
	 * @param file2TmxBean
	 * @return ;
	 * @throws Exception
	 */
	public AbstractWriter getWriter(File2TmxConvertBean file2TmxBean) throws Exception {
		AbstractWriter tmxWriter = null;
		if (file2TmxBean.newTmxFilePath != null && !file2TmxBean.newTmxFilePath.isEmpty()) {
			File file = new File(file2TmxBean.newTmxFilePath);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			}
			try {
				tmxWriter = new TmxWriter(file2TmxBean.newTmxFilePath);
			} catch (FileNotFoundException e) {
				LOGGER.error("", e);

			}
		} else if (file2TmxBean.appendExistTmxFilePath != null && !file2TmxBean.appendExistTmxFilePath.isEmpty()) {
			try {
				AppendTmxWriter tmxAppendWriter = new AppendTmxWriter(file2TmxBean.appendExistTmxFilePath);
				if (tmxAppendWriter.canAppend(tbxReader.getSrcLang())) {
					tmxAppendWriter.startAppend();
					tmxWriter = tmxAppendWriter;
				} else {
					throw new Exception("DIFF-SRC-LANG");
				}
			} catch (FileNotFoundException e) {
				LOGGER.error("", e);
			} catch (Exception e) {
				LOGGER.error("", e);
				if ("DIFF-SRC-LANG".equals(e.getMessage())) {
					throw new Exception(Messages.getString("converter.common.appendtmx.diffsrcLang.error"));
				}
				throw new Exception(Messages.getString("converter.common.appendtmx.wrongTmx"));
			}

		}
		return tmxWriter;
	}

}
