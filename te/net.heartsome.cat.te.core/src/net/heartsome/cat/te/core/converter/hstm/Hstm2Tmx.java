/**
 * Hstm2Tmx.java
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

package net.heartsome.cat.te.core.converter.hstm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.TMXDBOperatorFacade;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.te.core.bean.File2TmxConvertBean;
import net.heartsome.cat.te.core.bean.TmxTemplet;
import net.heartsome.cat.te.core.converter.tbx.AbstractWriter;
import net.heartsome.cat.te.core.converter.tbx.AppendTmxWriter;
import net.heartsome.cat.te.core.converter.tbx.Model2String;
import net.heartsome.cat.te.core.converter.tbx.TmxWriter;
import net.heartsome.cat.te.core.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hstm2Tmx extends net.heartsome.cat.te.core.converter.AbstractFile2Tmx {
	/**
	 * 记录日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Hstm2Tmx.class);

	private final int cache_size = 500;

	private List<TmxTU> cache = new ArrayList<TmxTU>(30);

	private String attributeString;

	private List<Integer> tuIds;

	private TMXDBOperatorFacade facade;

	private List<String> languages;

	public void doCovnerter(File2TmxConvertBean file2TmxBean, IProgressMonitor monitor) throws Exception {

		DBOperator operater = getHstmOperator(file2TmxBean);

		facade = new TMXDBOperatorFacade(operater);

		attributeString = Model2String.getCustomArributeXML(file2TmxBean.customeAttr);

		try {
			facade.start();
		} catch (Exception e) {
			throw new Exception(Messages.getString("converter.hstm2tmx.error1"));
		}
		tuIds = operater.getAfterFilterTuPk(null, null, null);
		int totalTask = tuIds.size() / cache_size;
		monitor.beginTask("", totalTask == 0 ? 1 : totalTask);

		languages = operater.getLanguages();

		if (tuIds == null || tuIds.isEmpty()) {
			throw new Exception(Messages.getString("converter.hstm2tmx.error2"));
		}

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
			TmxHeader header = TmxTemplet.generateTmxHeader(languages.get(0), "unknown", "sentence",
					"Heartsome File-based TM", null, null, null);
			tmxWriter.writeXmlString("<tmx version=\"1.4\">\n");
			tmxWriter.writeHeader(TmxTemplet.header2Xml(header));
			tmxWriter.writeXmlString("<body>\n");
			for (int tuId : tuIds) {
				TmxTU dbTu = facade.getTuByIdentifierAllLang(tuId, true);
				if (cache.size() != cache_size) {
					cache.add(dbTu);
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
			return true;
		} catch (Exception e) {
			LOGGER.error(Messages.getString("converter.hstm2tmx.error3"), e);
			throw new Exception(Messages.getString("converter.hstm2tmx.error3"));
		} finally {
			monitor.done();
			tmxWriter.writeEnd();
			tmxWriter.closeOutStream();
			facade.end();
		}
	}

	public DBOperator getHstmOperator(File2TmxConvertBean file2TmxBean) {
		MetaData metaData = new MetaData();
		metaData.setDbType(Constants.DBTYPE_SQLITE);
		File hstmFile = new File(file2TmxBean.sourceFilePath);
		metaData.setDataPath(hstmFile.getParent());
		metaData.setDatabaseName(hstmFile.getName());
		return DatabaseService.getDBOperator(metaData);
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
				if (hasLang(tmxAppendWriter)) {
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
					throw new Exception(Messages.getString("converter.hstm2tmx.error4"));
				}
				throw new Exception(Messages.getString("converter.common.appendtmx.wrongTmx"));
			}

		}
		return tmxWriter;
	}

	/**
	 * 数据库中是否有tmx文件中的源语言
	 * @param tmxAppendWriter
	 * @return ;
	 */
	private boolean hasLang(AppendTmxWriter tmxAppendWriter) {
		String tmxLang = tmxAppendWriter.getSrcLang();
		for (String lang : languages) {
			if (lang.equalsIgnoreCase(tmxLang)) {
				return true;
			}
		}
		return false;
	}
}
