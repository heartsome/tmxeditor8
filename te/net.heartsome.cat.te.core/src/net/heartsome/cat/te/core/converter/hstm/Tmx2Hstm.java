/**
 * Tmx2Hstm.java
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.SystemDBOperator;
import net.heartsome.cat.database.TMXDBOperatorFacade;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.document.TmxReadException;
import net.heartsome.cat.document.TmxReader;
import net.heartsome.cat.te.core.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tmx2Hstm extends net.heartsome.cat.te.core.converter.AbstractTmx2File {

	private static final Logger LOGGER = LoggerFactory.getLogger(Tmx2Hstm.class);

	public final static String NAME = "hstm";

	public String hsTmFilePath;

	private TMXDBOperatorFacade facade;

	private List<TmxTU> cache = new ArrayList<TmxTU>(SIZE);

	private static final int SIZE = 500;

	private boolean flushAttr = false;

	@SuppressWarnings("unused")
	private void initHstm() {

	}

	public void intDbOperator() {

	}

	public void doImportTmx(DBOperator dbOp, String txmFile) {

	}

	public void doCovnerter(String tmxFile, File targetFile,IProgressMonitor monitor) throws Exception {
		MetaData metaData = new MetaData();
		metaData.setDbType(Constants.DBTYPE_SQLITE);
		metaData.setDataPath(targetFile.getParent());
		metaData.setDatabaseName(targetFile.getName());
		DBOperator dbOperator = getHstmDbOperator(metaData);
		facade = new TMXDBOperatorFacade(dbOperator);
		try {
			try {
				facade.start();
			} catch (SQLException e) {
				throw new Exception(Messages.getString("converter.tmx2hstm.error1"));
			} catch (ClassNotFoundException e) {
				throw new Exception(Messages.getString("converter.tmx2hstm.error2"));
			}
			SystemDBOperator sysDbOperateByMetaData = DatabaseService.getSysDbOperateByMetaData(metaData);

			try {
				sysDbOperateByMetaData.createDB();
			} catch (SQLException e) {
				throw new Exception(Messages.getString("converter.tmx2hstm.error3"));
			}

			if (tmxFile.startsWith("TRUE")) {// 包含属性
				flushAttr = true;
				tmxFile = tmxFile.substring("TRUE".length());
			}

			try {
				tmxReader = new TmxReader(new File(tmxFile));
			} catch (TmxReadException e) {
				throw new Exception(Messages.getString("converter.tmx2hstm.error4"));
			}

			int total = tmxReader.getTotalTu()/SIZE;
			monitor.beginTask("", total==0?1:total);
			TmxTU temp;
			while (true) {
				temp = tmxReader.read().getTu();
				if (temp == null) {
					break;
				}
				if (cache.size() != SIZE) {
					cache.add(temp);
					continue;
				}
				try {
					if(monitor.isCanceled()){
						return;
					}
					flushTu2DB(cache);
					monitor.worked(1);
				} catch (SQLException e) {
					throw new Exception(Messages.getString("converter.tmx2hstm.error5"));
				}
			}
			try {
				if(monitor.isCanceled()){
					return;
				}
				flushTu2DB(cache);
				monitor.done();
			} catch (SQLException e) {
				throw new Exception(Messages.getString("converter.tmx2hstm.error5"));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			releaseResource();
			monitor.done();
		}

	}

	/**
	 * 翻译单元值存入数据库
	 * @param cache2
	 *            ;
	 * @throws SQLException
	 */
	private void flushTu2DB(List<TmxTU> cache) throws Exception {
		if (null == cache || cache.isEmpty()) {
			return;
		}
		facade.beginTransaction();
		for (TmxTU tu : cache) {
			// TODO :处理TU的某些值
			resetTU(tu);
			try {
				facade.insertTu(tu);
			} catch (SQLException e) {
				facade.rollback();
			}
		}
		facade.commit();
		cache.clear();

	}

	public void releaseResource() {
		try {
			facade.end();
		} catch (SQLException e) {
			LOGGER.error("", e);
		}
	}

	public DBOperator getHstmDbOperator(MetaData metaData) {
		return DatabaseService.getDBOperator(metaData);
	}

	/**
	 * 去掉TU属性值
	 * @param tu
	 *            ;
	 */
	public void resetTU(TmxTU tu) {
		if (null == tu) {
			return;
		}
		if (!flushAttr) {
			tu.setAttributes(null);
			tu.setChangeDate(null);
			tu.setChangeUser(null);
			tu.setCreationDate(null);
			tu.setCreationTool(null);
			tu.setCreationToolVersion(null);
			tu.setCreationUser(null);
			tu.setNotes(null);
			tu.setProps(null);
		}
	}
}
