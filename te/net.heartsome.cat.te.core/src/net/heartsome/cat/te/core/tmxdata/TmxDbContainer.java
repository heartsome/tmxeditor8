/**
 * TmxDbContainer.java
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

package net.heartsome.cat.te.core.tmxdata;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.te.core.resource.Messages;

public class TmxDbContainer extends TmxContainer {
	public static final Logger LOGGER = LoggerFactory.getLogger(TmxDbContainer.class);
	private DatabaseModelBean dbModelBean;
	private DBOperator dbOp;

	/**
	 * Constructor
	 * @param dbModelBean
	 */
	public TmxDbContainer(DatabaseModelBean dbModelBean) {
		this.dbModelBean = dbModelBean;
	}

	/** @return the dbModelBean */
	public DatabaseModelBean getDbModelBean() {
		return dbModelBean;
	}

	/** @return the dbOp */
	public DBOperator getDbOp() {
		return dbOp;
	}

	/**
	 * Load current database OSGI service by database type
	 * @throws Exception
	 *             did not find the DB service throw null exception;
	 */
	public void loadDatabaseOperator() throws Exception {
		MetaData metaData = dbModelBean.toDbMetaData();
		dbOp = DatabaseService.getDBOperator(metaData);
		if (dbOp == null) {
			throw new Exception(Messages.getString("tmxdata.TmxDatabaseContainer.loadDbOpError"),
					new NullPointerException());
		}
	}

	/**
	 * Connect to current database;
	 * @throws Exception
	 *             connection exception ;
	 */
	public void connectDatabase() throws Exception {
		try {
			this.dbOp.start();
		} catch (SQLException e) {
			String msg = e.getMessage();
			if(msg.contains("SQLITE_NOTADB")){
				msg = Messages.getString("tmxdata.TmxDatabaseContainer.connDbrOpError")+"\n"+Messages.getString("Tmxdata.TmxDatabaseContainer.nonHstmMsg");
				throw new Exception(msg);
			} else {
				LOGGER.error("", e);
				throw new Exception(Messages.getString("tmxdata.TmxDatabaseContainer.connDbrOpError")+"\n"+e.getMessage());
			}
		} catch (ClassNotFoundException e) {
			LOGGER.error("", e);
			throw new Exception(Messages.getString("tmxdata.TmxDatabaseContainer.connDbDriverOpError"), e);
		}
	}

	/**
	 * close current database connection. ;
	 */
	public void closeDatabaseConnection() {
		try {
			this.dbOp.end();
		} catch (SQLException e) {
			LOGGER.error("", e);
		}
	}
}
