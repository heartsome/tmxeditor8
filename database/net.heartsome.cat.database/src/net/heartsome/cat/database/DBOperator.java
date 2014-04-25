package net.heartsome.cat.database;

import java.nio.charset.CharsetEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.common.bean.TmxContexts;
import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.bean.TranslationUnitAnalysisResult;
import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.common.tm.MatchQuality;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.common.util.InnerTagClearUtil;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.database.bean.ExportFilterBean;
import net.heartsome.cat.database.bean.ExportFilterComponentBean;
import net.heartsome.cat.database.bean.FuzzySearchResults;
import net.heartsome.cat.database.bean.TranslationUnitAnalysisResults;
import net.heartsome.cat.database.tmx.ConcordanceBean;
import net.heartsome.cat.database.tmx.LanguageTMX;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记忆库和术语库管理类
 * @author Jason
 * @version
 * @since JDK1.6
 */
public abstract class DBOperator {

	public static final Logger LOGGER = LoggerFactory.getLogger(DBOperator.class);

	/** 数据库元数据 */
	protected MetaData metaData;

	/** 数据库配置 */
	protected DBConfig dbConfig;

	/** 数据库连接 */
	protected Connection conn = null;

	/** 用于查询时的缓存 */
	protected Vector<String> langCaches = new Vector<String>();

	/**
	 * 设置数据库元数据
	 * @param metaData
	 *            ;
	 */
	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}

	/**
	 * 获取数据库元数据 ;
	 */
	public MetaData getMetaData() {
		return this.metaData;
	}

	/**
	 * 取得数据库配置
	 * @return ;
	 */
	public DBConfig getDbConfig() {
		return dbConfig;
	}

	/**
	 * 初始化数据库连接
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public abstract void start() throws SQLException, ClassNotFoundException;

	/**
	 * 释放占用的资源
	 * @throws SQLException
	 */
	public void end() throws SQLException {
		if (conn != null) {
			conn.close();
		}
		if (langCaches != null) {
			langCaches.clear();
		}
	}

	public boolean isClosed() throws SQLException {
		return conn == null || conn.isClosed();
	}

	public Connection getConnection() {
		return this.conn;
	}

	/**
	 * 回滚事务
	 * @throws SQLException
	 */
	public void rollBack() throws SQLException {
		if (conn != null) {
			conn.rollback();
		}
	}

	/**
	 * 提交事务
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		if (conn != null) {
			conn.commit();
		}
	}

	public void beginTransaction() throws SQLException {
		if (conn != null && conn.getAutoCommit() != false) {
			conn.setAutoCommit(false);
		}
	}

	// TODO =========================TMX部分(开始)=========================
	/**
	 * 将TMX的header接点的主要属性写入到mheader表中
	 * @throws SQLException
	 */
	public String insertHeader(Hashtable<String, String> params) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-mheader");
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			int i = 1;
			stmt.setString(i++, params.get("CREATIONTOOL"));
			stmt.setString(i++, params.get("CTVERSION"));
			stmt.setString(i++, params.get("TMF"));
			stmt.setString(i++, Utils.convertLangCode(params.get("SRCLANG")));
			stmt.setString(i++, params.get("ADMINLANG"));
			stmt.setString(i++, params.get("DATATYPE"));
			stmt.setString(i++, params.get("SEGTYPE"));
			stmt.setString(i++, params.get("CREATIONID"));
			stmt.setString(i++, params.get("CREATIONDATE"));
			stmt.setString(i++, params.get("CHANGEID"));
			stmt.setString(i++, params.get("CHANGEDATE"));
			stmt.setString(i++, params.get("ENCODING"));
			int row = stmt.executeUpdate();
			String key = "-1";
			if (row == 1) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					key = rs.getString(1);
				}
				if (rs != null)
					rs.close();
			}
			return key;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 将数据插入HEADERNODE表
	 * @param params
	 *            ;
	 * @throws SQLException
	 */
	public void insertHeaderNode(Hashtable<String, String> params) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-mheadernode");
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setInt(i++, Integer.parseInt(params.get("HEADERID")));
			stmt.setString(i++, params.get("NODENAME"));
			stmt.setString(i++, params.get("NODETYPE"));
			stmt.setString(i++, params.get("CONTENT"));
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

	}

	/**
	 * 将数据写入到MTU表中
	 * @param params
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public int insertTU(int headerId, String tuId, String creationId, String creationDate, String changeId,
			String changeDate, String creationTool, String creationToolVersion, String client, String projectRef,
			String jobRef) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-tu");
			if (null == conn) {
				return -1;
			}
			stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
			int i = 1;
			stmt.setInt(i++, headerId);
			stmt.setString(i++, tuId);
			stmt.setString(i++, creationId);
			stmt.setTimestamp(i++, DateUtils.getTimestampFromUTC(creationDate));
			stmt.setString(i++, changeId);
			stmt.setTimestamp(i++, DateUtils.getTimestampFromUTC(changeDate));
			stmt.setString(i++, creationTool);
			stmt.setString(i++, creationToolVersion);
			stmt.setString(i++, client);
			stmt.setString(i++, projectRef);
			stmt.setString(i++, jobRef);
			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return -1;
	}

	/**
	 * 插入数据到TEXTDATA表，更新LANGS表和MATRIX_LANG表
	 * @param params
	 * @return 插入TEXTDATA表记录的ID
	 * @throws SQLException
	 */
	public String insertTextData(String type, int groupId, String hash, String pureText, String content, String lang,
			String preContext, String nextContext) throws SQLException {
		/*
		 * 步骤 1.添加记录到TEXTDATA表 2.查看LANGS表是否有刚才添加的语言，没有则需要增加一条记录，然后需要创建相对应的matrix表 3.添加记录到Matrix表
		 */
		String textDataId = null;
		PreparedStatement stmt = null;
		try {
			String langCode = Utils.langToCode(lang).toUpperCase();
			String sql = dbConfig.getOperateDbSQL("get-lang-bycode");
			if (!langCaches.contains(lang)) {
				Map<Integer, Map<String, String>> langRs = query(sql, new Object[] { lang });
				if (langRs.size() == 0) { // 说明对应的Matrix表还没有创建
					sql = dbConfig.getOperateDbSQL("insert-lang");
					stmt = conn.prepareStatement(sql);
					stmt.setString(1, lang);
					stmt.executeUpdate();
					stmt.close();

					// 创建表
					List<String> createMatrixTables = dbConfig.getCreateMatrixTables();
					for (String i : createMatrixTables) {
						i = i.replaceAll("__LANG__", langCode);
						stmt = conn.prepareStatement(i);
						stmt.execute();
						stmt.close();
					}

					// 创建索引
					List<String> createMatrixIndex = dbConfig.getCreateMatrixIndexes();
					for (String i : createMatrixIndex) {
						i = i.replaceAll("__LANG__", langCode);
						stmt = conn.prepareStatement(i);
						stmt.execute();
						stmt.close();
					}
					langCaches.add(lang);
				}
			}
			if (pureText != null && lang != null && content != null) {
				sql = dbConfig.getOperateDbSQL("insert-textdata");
				stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
				int i = 1;
				stmt.setString(i++, type);
				stmt.setInt(i++, groupId);
				stmt.setInt(i++, Integer.parseInt(hash));
				stmt.setString(i++, pureText);
				stmt.setString(i++, content);
				stmt.setString(i++, lang);
				stmt.setString(i++, preContext);
				stmt.setString(i++, nextContext);
				stmt.execute();
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					textDataId = rs.getString(1);
				}
				rs.close();
				stmt.close();

				int[] ngrams = generateNgrams(lang, pureText);
				if (ngrams.length > 0) {
					String insertMatrix = dbConfig.getMatrixSQL("insert");
					insertMatrix = insertMatrix.replaceAll("__LANG__", langCode);
					stmt = conn.prepareStatement(insertMatrix);
					for (int j = 0; j < ngrams.length; j++) {
						stmt.setInt(1, Integer.parseInt(textDataId));
						stmt.setInt(2, ngrams[j]);
						stmt.setShort(3, (short) ngrams.length);
						stmt.addBatch();
					}
					stmt.executeBatch();
					stmt.close();
				}
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

		return textDataId;
	}

	/**
	 * 将数据插入TMXPROPS表
	 * @param params
	 *            ;
	 * @throws SQLException
	 */
	public void insertTMXProp(int parentPk, String parentName, String type, String lang, String encoding, String content)
			throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-tmxprops");
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, parentName);
			stmt.setInt(i++, parentPk);
			stmt.setString(i++, type);
			stmt.setString(i++, Utils.convertLangCode(lang));
			stmt.setString(i++, encoding);
			stmt.setString(i++, content);
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 将数据插入TMXNOTES表
	 * @param params
	 *            ;
	 * @throws SQLException
	 */
	public void insertTMXNote(int parentPk, String parentName, String content, String creationId, String creationDate,
			String changeId, String changeDate, String encoding, String lang) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-tmxnotes");
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, parentName);
			stmt.setInt(i++, parentPk);
			stmt.setString(i++, content);
			stmt.setString(i++, creationId);
			stmt.setString(i++, creationDate);
			stmt.setString(i++, changeId);
			stmt.setString(i++, changeDate);
			stmt.setString(i++, encoding);
			stmt.setString(i++, Utils.convertLangCode(lang));
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 将数据写入到Extra表中
	 * @param type
	 * @param eleName
	 * @param eleContent
	 * @param pName
	 * @param pId
	 * @throws SQLException
	 *             ;
	 */
	public void insertTMXExtra(String type, String eleName, String eleContent, String pName, String pId)
			throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-tmxextra");
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, pName);
			stmt.setInt(i++, Integer.parseInt(pId));
			stmt.setString(i++, type);
			stmt.setString(i++, eleName);
			stmt.setString(i++, eleContent);
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 语言在数据库中是否存在
	 * @param lang
	 *            语言,如zh-CN
	 * @return true存在,false不存在
	 * @throws SQLException
	 *             ;
	 */
	public boolean hasLangInDB(String lang) throws SQLException {
		String sql = dbConfig.getOperateDbSQL("get-lang-bycode");
		Map<Integer, Map<String, String>> langRs = query(sql, new Object[] { lang });
		if (langRs.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断数据库中是否有相应语言数据库记录
	 * @param lang
	 *            语言代码,如:en-Us
	 * @param type
	 *            数据记录类型,M:记忆库,B:术语库
	 * @return ;
	 * @throws SQLException
	 */
	public boolean checkHasMatchs(String lang, String type) throws SQLException {
		PreparedStatement stm = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("get-textdataid-bylang");
			stm = conn.prepareStatement(sql);
			stm.setString(1, type);
			stm.setString(2, lang);
			rs = stm.executeQuery();
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
		}
	}

	/**
	 * 获取MExtra表中的的值
	 * @param tuid
	 * @return 返回Map<列序号,MAP<列名,值>>
	 * @throws SQLException
	 *             ;
	 */
	public Map<Integer, Map<String, String>> getExtraValue(Integer parentId, String parentName, String type)
			throws SQLException {
		Map<Integer, Map<String, String>> rs = query(dbConfig.getOperateDbSQL("get-tuextra-bytuid"), new Object[] {
				parentId, parentName, type });
		return rs;
	}

	/**
	 * 获取MProp表中的值
	 * @return 返回对应的MProp表中的值 Map<列序号,MAP<列名,值>>;
	 * @throws SQLException
	 */
	public Map<Integer, Map<String, String>> getMPropValue(Integer parentId, String parentName) throws SQLException {
		Map<Integer, Map<String, String>> rs = query(dbConfig.getOperateDbSQL("get-mporp-byparentid"), new Object[] {
				parentId, parentName });
		return rs;
	}

	// SELECT
	// MPPKID, PARENTNAME, PARENTID, PNAME, LANG, ENCODING, CONTENT
	// FROM
	// MPROP WHERE PARENTID=? AND PARENTNAME=?
	public List<TmxProp> getTuMprops(int parentId, String parentName) throws SQLException {
		List<TmxProp> result = new ArrayList<TmxProp>();
		String sql = dbConfig.getOperateDbSQL("get-mporp-byparentid");
		PreparedStatement psmt = null;
		ResultSet rs = null;
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setInt(1, parentId);
			psmt.setString(2, parentName);
			rs = psmt.executeQuery();

			while (rs.next()) {
				TmxProp av = new TmxProp(rs.getString(4), rs.getString(7));
				// fix _addPKID
				av.setDbPk(rs.getInt(1));
				result.add(av);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (psmt != null) {
				psmt.close();
			}
		}
		return result;
	}

	public List<TmxNote> getTuMNote(int parentId, String parentName) throws SQLException {
		List<TmxNote> result = new ArrayList<TmxNote>();
		// SELECT CONTENT FROM MNOTE WHERE PARENTID=? AND PARENTNAME=?
		String sql = dbConfig.getOperateDbSQL("get-mnote-byparentid");
		PreparedStatement psmt = null;
		ResultSet rs = null;
		try {
			psmt = conn.prepareStatement(sql);
			psmt.setInt(1, parentId);
			psmt.setString(2, parentName);
			rs = psmt.executeQuery();

			while (rs.next()) {
				TmxNote note = new TmxNote();
				note.setContent(rs.getString(1));
				note.setDbPk(rs.getInt(2));
				result.add(note);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (psmt != null) {
				psmt.close();
			}
		}
		return result;
	}

	/**
	 * 获取MNote表中的的值
	 * @param parentId
	 * @param parentName
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public Map<Integer, Map<String, String>> getMNoteValue(Integer parentId, String parentName) throws SQLException {
		Map<Integer, Map<String, String>> rs = query(dbConfig.getOperateDbSQL("get-tunote-bytuid"), new Object[] {
				parentName, parentId });
		return rs;
	}

	/**
	 * 根据指定的语言,上下文和纯文本的hash码在TEXTDATA表中查找记录
	 * @param hash
	 *            纯文本的hash码
	 * @param lang
	 *            语言代码
	 * @param preContext
	 *            上文 hash码
	 * @param nextContext
	 *            下文hash码
	 * @param type
	 *            记录类型,M:TMX,B:tbx
	 * @return 一组符合条件的TEXTDATA数据记录的主键
	 * @throws SQLException
	 *             ;
	 */
	public List<String> getTextDataId(int hash, String lang, String preContext, String nextContext, String type)
			throws SQLException {
		List<String> ids = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("get-textdataid-bycontext");
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setInt(i++, hash);
			stmt.setString(i++, preContext);
			stmt.setString(i++, nextContext);
			stmt.setString(i++, lang);
			stmt.setString(i++, type);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ids.add(rs.getInt("TPKID") + "");
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return ids;
	}

	/**
	 * 根据指定的语言,上下文和纯文本的hash码在TEXTDATA表中查找记录
	 * @param hash
	 *            纯文本的hash码
	 * @param lang
	 *            语言代码
	 * @param type
	 *            记录类型,M:TMX,B:tbx
	 * @return 一组符合条件的TEXTDATA数据记录的主键
	 * @throws SQLException
	 *             ;
	 */
	public List<String> getTextDataId(int hash, String lang, String type) throws SQLException {
		List<String> ids = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("get-textdataid");
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setInt(i++, hash);
			stmt.setString(i++, lang);
			stmt.setString(i++, type);
			rs = stmt.executeQuery();
			while (rs.next()) {
				ids.add(rs.getString("TPKID"));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return ids;
	}

	public List<TmxTU> getTUInfoByTuvInfo(int hash, String lang, String tuId) throws SQLException {
		String type = "M";
		List<TmxTU> results = new ArrayList<TmxTU>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("get-tuinfo-bytuvinfo");
			String tempSql = "select count(MTUPKID) from (" + sql + ") A";
			int rowCount = 0;
			PreparedStatement stm = null;
			ResultSet r = null;
			try {
				stm = conn.prepareStatement(tempSql);
				stm.setInt(1, hash);
				stm.setString(2, lang);
				stm.setString(3, type);
				r = stm.executeQuery();
				while (r.next()) {
					rowCount = r.getInt(1);
				}
			} finally {
				if (r != null) {
					r.close();
				}
				if (stm != null) {
					stm.close();
				}
			}
			if (rowCount == 0) {
				return results;
			}
			// SELECT MTUPKID, TUID , CHANGEID, CHANGEDATE, PRECONTEXT, NEXTCONTEXT FROM MTU
			// INNER JOIN TEXTDATA ON MTUPKID = GROUPID AND HASH = ? AND LANG = ? AND TYPE = ?
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setInt(i++, hash);
			stmt.setString(i++, lang);
			stmt.setString(i++, type);
			rs = stmt.executeQuery();
			if (rowCount == 1 && rs.next()) {
				TmxTU tu = new TmxTU();
				tu.setTmId(rs.getInt(1));
				tu.setTuId(rs.getString(2));
				tu.setChangeUser(rs.getString(3));
				Timestamp _v = rs.getTimestamp(4);
				String changeDate = null;
				if (_v != null) {
					changeDate = DateUtils.formatToUTC(_v.getTime());
				}
				tu.setChangeDate(changeDate);
				tu.appendContext(TmxContexts.PRE_CONTEXT_NAME, rs.getString(5));
				tu.appendContext(TmxContexts.NEXT_CONTEXT_NAME, rs.getString(6));
				results.add(tu);
			} else {
				while (rs.next()) {
					String _tuid = rs.getString(2);
					if (_tuid.equals(tuId)) {
						TmxTU tu = new TmxTU();
						tu.setTmId(rs.getInt(1));
						tu.setTuId(_tuid);
						tu.setChangeUser(rs.getString(3));
						Timestamp _v = rs.getTimestamp(4);
						String changeDate = null;
						if (_v != null) {
							changeDate = DateUtils.formatToUTC(_v.getTime());
						}
						tu.setChangeDate(changeDate);
						tu.appendContext(TmxContexts.PRE_CONTEXT_NAME, rs.getString(5));
						tu.appendContext(TmxContexts.NEXT_CONTEXT_NAME, rs.getString(6));
						results.add(tu);
					}
				}
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return results;
	}

	public List<Map<String, String>> getTUInfoByTuvInfo(int hash, String lang, String type, String tuId,
			String preContext, String nextContext) throws SQLException {
		List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("get-tuinfo-bytuvinfo-withcontext");

			String tempSql = "select count(*) from (" + sql + ") A";
			int rowCount = 0;
			PreparedStatement stm = null;
			ResultSet r = null;
			try {
				stm = conn.prepareStatement(tempSql);
				stm.setInt(1, hash);
				stm.setString(2, lang);
				stm.setString(3, type);
				stm.setString(4, preContext);
				stm.setString(5, nextContext);
				r = stm.executeQuery();
				while (r.next()) {
					rowCount = r.getInt(1);
				}
			} finally {
				if (r != null) {
					r.close();
				}
				if (stm != null) {
					stm.close();
				}
			}
			if (rowCount == 0)
				return resultList;

			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setInt(i++, hash);
			stmt.setString(i++, lang);
			stmt.setString(i++, type);
			stmt.setString(i++, preContext);
			stmt.setString(i++, nextContext);
			rs = stmt.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			int cols = meta.getColumnCount();
			if (rowCount == 1 && rs.next()) {
				Map<String, String> rowData = new HashMap<String, String>();
				for (int j = 1; j <= cols; j++) {
					String colName = meta.getColumnName(j).toUpperCase();
					String value = null;
					if (colName.equalsIgnoreCase("CREATIONDATE") || colName.equalsIgnoreCase("CHANGEDATE")) {
						Timestamp _v = rs.getTimestamp(j);
						if (_v != null) {
							value = DateUtils.formatToUTC(rs.getTimestamp(j).getTime());
						}
					} else {
						value = rs.getString(j);
					}
					rowData.put(colName, value);
				}
				resultList.add(rowData);
			} else {
				rs.beforeFirst();
				while (rs.next()) {
					String _tuid = rs.getString(2);
					if (_tuid.equals(tuId)) {
						Map<String, String> rowData = new HashMap<String, String>();
						for (int j = 1; j <= cols; j++) {
							String colName = meta.getColumnName(j).toUpperCase();
							String value = rs.getString(j);
							if (colName.equalsIgnoreCase("CREATIONDATE") || colName.equalsIgnoreCase("CHANGEDATE")) {
								Timestamp _v = rs.getTimestamp(j);
								if (_v != null) {
									value = DateUtils.formatToUTC(rs.getTimestamp(j).getTime());
								}
							}
							rowData.put(colName, value);
						}
						resultList.add(rowData);
					}
				}
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return resultList;
	}

	/**
	 * 获取某一MTU下的对应语言的textData
	 * @param groupId
	 *            MTU表的主键
	 * @param type
	 *            类型,区分TMX和TBX
	 * @param lang
	 *            语言代码
	 * @return TEXTDATA的主键集合
	 * @throws SQLException
	 *             ;
	 */
	public List<TmxSegement> getTextDataIdByGroupIdLang(int groupId, String type, String lang) throws SQLException {
		List<TmxSegement> r = new ArrayList<TmxSegement>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("get-textdataid-bygroupidlang");
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, groupId);
			stmt.setString(2, type);
			stmt.setString(3, lang);
			rs = stmt.executeQuery();
			while (rs.next()) {
				int pk = rs.getInt(1);
				String content = rs.getString(2);
				if (content == null) {
					continue;
				}
				TmxSegement tuv = new TmxSegement();
				tuv.setDbPk(pk);
				tuv.setFullText(content);
				tuv.setLangCode(lang);
				r.add(tuv);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return r;
	}

	/**
	 * 根据所属组获取TextData 表中的数据
	 * @param type
	 *            类型,"M"或者"B"
	 * @param groupId
	 *            TU主键或者TermEntry主键
	 * @param lang
	 *            语言 "EN-US"
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public Map<String, String> getTextDataValue(String type, Integer groupId, String lang) throws SQLException {
		Map<Integer, Map<String, String>> rs = query(dbConfig.getOperateDbSQL("get-textdata-bygroupid"), new Object[] {
				type, groupId, lang });
		return rs.get(0);
	}

	/**
	 * 通过TU在数据库中的主键获取MTU表中的内容,即TU的主要属性
	 * @param tuPkid
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public Map<String, String> getTUByTuPkId(Integer tuPkid) throws SQLException {
		Map<Integer, Map<String, String>> rs = query(dbConfig.getOperateDbSQL("get-tu-bytupkid"),
				new Object[] { tuPkid });
		if (rs == null || rs.size() == 0) {
			return null;
		} else {
			return rs.get(0);
		}
	}

	/**
	 * 根据TUV主键集获取MTU表中的记录，
	 * @param tuvPks
	 *            TUV主键集,同一个TU下的多个TUV主键将只返回一条TU记录
	 * @return 查询到的数据集，空List 未查询到数据
	 * @throws SQLException
	 *             ;
	 */
	public List<Map<String, String>> getTUInfoByTuvPkids(List<String> tuvPks) throws SQLException {
		List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		if (tuvPks.size() == 0) {
			return resultList;
		}
		String set = "" + tuvPks.get(0); //$NON-NLS-1$
		for (int i = 1; i < tuvPks.size(); i++) {
			set = set + "," + tuvPks.get(i); //$NON-NLS-1$
		}
		String querySql = dbConfig.getOperateDbSQL("get-tu-bytupkids");
		querySql = querySql.replace("__SET__", set);
		PreparedStatement psmt = null;
		ResultSet rs = null;
		try {
			psmt = conn.prepareStatement(querySql);
			rs = psmt.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			int cols = meta.getColumnCount();
			while (rs.next()) {
				Map<String, String> rowData = new HashMap<String, String>();
				for (int i = 1; i <= cols; i++) {
					String colName = meta.getColumnName(i).toUpperCase();
					String value = null;
					if (colName.equalsIgnoreCase("CREATIONDATE") || colName.equalsIgnoreCase("CHANGEDATE")) {
						Timestamp _v = rs.getTimestamp(i);
						if (_v != null) {
							value = DateUtils.formatToUTC(rs.getTimestamp(i).getTime());
						}
					} else {
						value = rs.getString(i);
					}
					rowData.put(colName, value);
				}
				resultList.add(rowData);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (psmt != null) {
				psmt.close();
			}
		}
		return resultList;
	}

	/**
	 * 通过主键,更新MTU表
	 * @param params
	 * @throws SQLException
	 *             ;
	 */
	public void updateTU(Map<String, String> params) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("update-tu");
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, params.get("CREATIONID"));
			stmt.setTimestamp(i++, DateUtils.getTimestampFromUTC(params.get("CREATIONDATE")));
			stmt.setString(i++, params.get("CHANGEID"));
			stmt.setTimestamp(i++, DateUtils.getTimestampFromUTC(params.get("CHANGEDATE")));
			stmt.setString(i++, params.get("CREATIONTOOL"));
			stmt.setString(i++, params.get("CREATIONTOOLVERSION"));
			stmt.setString(i++, params.get("CLIENT"));
			stmt.setString(i++, params.get("PROJECTREF"));
			stmt.setString(i++, params.get("JOBREF"));
			stmt.setString(i++, params.get("TUID"));
			stmt.setInt(i++, Integer.parseInt(params.get("MTUPKID")));
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public void updateTuChangeInfo(int tupk, String tuId, String changeId, String changeDate) throws SQLException {
		// <uppdateTuChangeInfo-by-MTUPKID>
		// UPDATE MTU SET TUID=?, CHANGEID=?, CHANGEDATE=? WHERE MTUPKID=?
		// </uppdateTuChangeInfo-by-MTUPKID>
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("uppdateTuChangeInfo-by-MTUPKID");
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, tuId);
			stmt.setString(i++, changeId);
			stmt.setTimestamp(i++, DateUtils.getTimestampFromUTC(changeDate));
			stmt.setInt(i++, tupk);
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 批量更新MExtra表中的Content字段
	 * @param params
	 *            Map<行号，Map<列名，值>>
	 * @throws SQLException
	 *             ;
	 */
	public void updateTUExtra(Map<Integer, Map<String, String>> params) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("update-tuextra");
			stmt = conn.prepareStatement(sql);
			for (Integer key : params.keySet()) {
				int i = 1;
				Map<String, String> param = params.get(key);
				stmt.setString(i++, param.get("CONTENT"));
				stmt.setString(i++, param.get("MEPKID"));
				stmt.addBatch();
			}
			stmt.executeBatch();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 批量更新MProp表中的Content内容
	 * @param params
	 *            Map<行号，Map<列名，值>>
	 * @throws SQLException
	 *             ;
	 */
	public void updateMProp(Map<Integer, Map<String, String>> params) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("update-mprop");
			stmt = conn.prepareStatement(sql);
			for (Integer key : params.keySet()) {
				int i = 1;
				Map<String, String> param = params.get(key);
				stmt.setString(i++, param.get("CONTENT"));
				stmt.setInt(i++, Integer.parseInt(param.get("MPPKID")));
				stmt.addBatch();
			}
			stmt.executeBatch();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public void updateTuvContext(int tupkid, String lang, String preContext, String nextContext) throws SQLException {
		// UPDATE TEXTDATA SET PRECONTEXT = ? , NEXTCONTEXT = ? WHERE GROUP = ? AND LANG = ?
		String sql = dbConfig.getOperateDbSQL("updateContext-by-gruopid-lang");
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, preContext);
			stmt.setString(i++, nextContext);
			stmt.setInt(i++, tupkid);
			stmt.setString(i++, lang);

			stmt.executeUpdate();

		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 删除TUV相关的全部内容,即TEXTDATA表
	 * @param textDataId
	 * @param lang
	 * @throws SQLException
	 *             ;
	 */
	public int[] deleteAllTuvRelations(List<Integer> textDataId, String lang) throws SQLException {
		if (textDataId.size() == 0) {
			return new int[0];
		}
		int[] executeBatch = new int[0];
		StringBuffer sqlWhereBf = new StringBuffer();
		sqlWhereBf.append("__PKID__ = ").append(textDataId.get(0)).append(" or ");
		for (int i = 1; i < textDataId.size(); i++) {
			sqlWhereBf.append("__PKID__ = ").append(textDataId.get(i)).append(" or ");
		}
		String sqlWhereStr = sqlWhereBf.substring(0, sqlWhereBf.length() - 3);

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String sql = dbConfig.getOperateDbSQL("delete-textdata-bytextdataid");
			String _where = sqlWhereStr.replace("__PKID__", "TPKID");
			sql = sql.replace("__WHERE__", _where);
			stmt.addBatch(sql);

			String deleteMatrixSql = dbConfig.getMatrixSQL("delete");
			deleteMatrixSql = deleteMatrixSql.replace("__WHERE__", _where);
			deleteMatrixSql = deleteMatrixSql.replaceAll("__LANG__", Utils.langToCode(lang).toUpperCase());
			stmt.addBatch(deleteMatrixSql);

			_where = sqlWhereStr.replace("__PKID__", "PARENTID");
			_where += " AND PARENTNAME = 'TUV'";
			sql = dbConfig.getOperateDbSQL("delete-tuvprops");
			sql = sql.replace("__WHERE__", _where);
			stmt.addBatch(sql);

			sql = dbConfig.getOperateDbSQL("delete-tuvnotes");
			sql = sql.replace("__WHERE__", _where);
			stmt.addBatch(sql);

			sql = dbConfig.getOperateDbSQL("delete-tuvextra");
			sql = sql.replace("__WHERE__", _where);
			stmt.addBatch(sql);

			executeBatch = stmt.executeBatch();
			return executeBatch;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 删除Mprop表中的记录
	 * @param parentName
	 *            父节点名称
	 * @param set
	 *            SQL set集 用于 xx in (__SET__)
	 * @throws SQLException
	 *             ;
	 */
	public void deleteMprop(String parentName, String set) throws SQLException {
		Statement stmt = null;
		try {
			// DELETE FROM MPROP WHERE __WHERE__
			String sql = dbConfig.getOperateDbSQL("delete-tuvprops");
			String where = "PARENTNAME='" + parentName + "' AND PARENTID IN (" + set + ")";
			sql = sql.replace("__WHERE__", where);
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 删除MNOTE表中的记录
	 * @param parentName
	 * @param set
	 * @throws SQLException
	 *             ;
	 */
	public void deleteMNote(String parentName, String set) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("delete-tuvnotes");
			String where = "PARENTNAME='" + parentName + "' AND PARENTID IN (" + set + ")";
			sql = sql.replace("__WHERE__", where);
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 删除MEXTRA表中的记录
	 * @param parentName
	 * @param set
	 * @throws SQLException
	 *             ;
	 */
	public void deleteExtra(String parentName, String set) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("delete-tuvextra");
			sql = sql.replace("__SET__", set);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, parentName);
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 删除MATRXLANG表中的记录
	 * @param set
	 * @param lang
	 *            如 en-US
	 * @throws SQLException
	 *             ;
	 */
	public void deleteMatrxLang(String set, String lang) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String deleteMatrixSql = dbConfig.getMatrixSQL("delete");
			deleteMatrixSql = deleteMatrixSql.replace("__WHERE__", " TPKID IN (" + set + ") ");
			deleteMatrixSql = deleteMatrixSql.replaceAll("__LANG__", Utils.langToCode(lang).toUpperCase());
			stmt = conn.prepareStatement(deleteMatrixSql);
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

	}

	// TODO =========================TMX部分(结束)=========================

	/**
	 * 查找指定文本相应语言的术语列表。
	 * @param srcText
	 *            指定的文本。
	 * @param srcLang
	 *            源语言代码。
	 * @param tgtLang
	 *            目标语言代码。
	 * @return Vector<Hashtable<String,String> 返回匹配的术语信息。<br/>
	 *         每条术语信息使用一个 Hashable 保存。分别包括如下键值信息:<br/>
	 *         Key TUID 翻译单元ID <br/>
	 *         Key SRCLANG 源语言代码 <br/>
	 *         Key SRCTEXT 源语言术语 <br/>
	 *         Key TGTLANG 目标语言代码 <br/>
	 *         Key TGTTEXT 目标语言术语 <br/>
	 *         若无匹配的术语，返回 null。
	 */
	public Vector<Hashtable<String, String>> checkTerms(String srcText, String srcLang, String tgtLang) {
		// TODO 需添加实现。请参考 R7 业务逻辑代码。若具体数据库实现的代码需重写，请到具体实现类中实现或是重写。

		return null;
	}

	/**
	 * 查找指定文本相应语言高于最低匹配率的匹配。该方法支持上下文。 其中匹配率为 100% 且上下文也匹配的匹配率将设置为 101%。 匹配需要根据罚分策略罚分后，再根据匹配率按降序排序。
	 * 匹配超过匹配上限时，仅保留匹配上限限制的匹配。
	 * @param srcPureText
	 *            指定的源文本。
	 * @param srcContent
	 *            源节点内容文本，用于判断标准是否匹配。
	 * @param srcLang
	 *            源语言代码。
	 * @param tgtLang
	 *            目标语言代码。
	 * @param caseSensitive
	 *            是否区分大小写。
	 * @param minSimilarity
	 *            最小匹配率。为百分比值。如 30%，则取值 30.
	 * @param matchUpperLimit
	 *            匹配个数上限。
	 * @param tagPenalty
	 *            错误标记罚分标准
	 * @param isSinglePenalty
	 *            罚分策略。Yes，则每个错误都要扣一次分。No，则所有错误都只扣一次分。
	 * @param contexts
	 *            上下文。该数组有两个元素，第一个为上文，第二个为下文。传递最近的上下文文本段的hashCode，多个上文或下文中使用逗号分隔。
	 * @param contextSize
	 *            当前上下文的大小。
	 * @param overwritePerfectMatch
	 *            在匹配率为 100% 的完美匹配超过多个时，是否通过覆盖策略仅保留一个。
	 * @return Vector<Hashtable<String,String> 返回匹配信息。<br/>
	 *         每条术语信息使用一个 Hashable 保存。分别包括如下键值信息:<br/>
	 *         Key TUID 翻译单元ID <br/>
	 *         Key SRCLANG 源语言代码 <br/>
	 *         Key SRCTEXT 源语言文本，不带标记。 <br/>
	 *         Key SRCCONTENT 源语言内容，带标记。 <br/>
	 *         Key SRCCREATIONID 源节点创建者ID。 <br/>
	 *         Key SRCCREATIONDATE 源节点创建时间。 <br/>
	 *         Key SRCCHANGEID 源节点修改者ID。 <br/>
	 *         Key SRCCHANGEDATE 源节点修改时间。 <br/>
	 *         Key PRECONTEXT 上文，HashCode 值，多个上文间按顺序用逗号连接。 <br/>
	 *         Key NEXTCONTEXT 下文，HashCode 值，多个下文间按顺序用逗号连接。 <br/>
	 *         Key SRCPROJECTREF 源节点项目参考信息属性。 <br/>
	 *         Key SRCJOBREF 源节点任务参考信息属性。 <br/>
	 *         Key TGTLANG 目标语言代码 <br/>
	 *         Key TGTTEXT 目标语言文本，不带标记。 <br/>
	 *         Key TGTCONTENT 目标语言内容，带标记。 <br/>
	 *         Key TGTCREATIONID 目标节点创建者ID。 <br/>
	 *         Key TGTCREATIONDATE 目标节点创建者时间 <br/>
	 *         Key TGTCHANGEID 目标节点修改者ID。<br/>
	 *         Key TGTCHANGEDATE 目标节点修复时间。 <br/>
	 *         Key TGTPROJECTREF 目标节点项目参考信息属性。 <br/>
	 *         Key TGTJOBREF 目标节点任务参考信息属性。。 <br/>
	 *         Key SIMILARITY 匹配率。<br/>
	 *         若无匹配，返回 null。
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> findMatch(String srcPureText, String srcContent, String srcLang,
			String tgtLang, boolean caseSensitive, int minSimilarity, int matchUpperLimit, int tagPenalty,
			boolean isSinglePenalty, String[] contexts, int contextSize, boolean overwritePerfectMatch)
			throws SQLException {
		// TODO 需添加实现。请参考 R7 业务逻辑代码。若具体数据库实现的代码需重写，请到具体实现类中实现或是重写。

		return null;
	}

	/**
	 * 查找指定文本相应语言高于最低匹配率的匹配。该方法不支持上下文。 匹配需要根据罚分策略罚分后，再根据匹配率按降序排序。 匹配超过匹配上限时，仅保留匹配上限限制的匹配。
	 * @param srcText
	 *            指定的源文本。
	 * @param srcContent
	 *            源节点内容文本，用于判断标准是否匹配。
	 * @param srcLang
	 *            源语言代码。
	 * @param tgtLang
	 *            目标语言代码。
	 * @param caseSensitive
	 *            是否区分大小写。
	 * @param minSimilarity
	 *            最小匹配率。为百分比值。如 30%，则取值 30.
	 * @param matchUpperLimit
	 *            匹配个数上限。
	 * @param tagPenalty
	 *            错误标记罚分标准
	 * @param isSinglePenalty
	 *            罚分策略。Yes，则每个错误都要扣一次分。No，则所有错误都只扣一次分。
	 * @param overwritePerfectMatch
	 *            在匹配率为 100% 的完美匹配超过多个时，是否通过覆盖策略仅保留一个。
	 * @return Vector<Hashtable<String,String> 返回匹配信息。<br/>
	 *         每条术语信息使用一个 Hashable 保存。分别包括如下键值信息:<br/>
	 *         Key TUID 翻译单元ID <br/>
	 *         Key SRCLANG 源语言代码 <br/>
	 *         Key SRCTEXT 源语言文本，不带标记。 <br/>
	 *         Key SRCCONTENT 源语言内容，带标记。 <br/>
	 *         Key SRCCREATIONID 源节点创建者ID。 <br/>
	 *         Key SRCCREATIONDATE 源节点创建时间。 <br/>
	 *         Key SRCCHANGEID 源节点修改者ID。 <br/>
	 *         Key SRCCHANGEDATE 源节点修改时间。 <br/>
	 *         Key SRCPROJECTREF 源节点项目参考信息属性。 <br/>
	 *         Key SRCJOBREF 源节点任务参考信息属性。 <br/>
	 *         Key SRCCLIENT 源节点客户参考信息属性。 <br/>
	 *         Key TGTLANG 目标语言代码 <br/>
	 *         Key TGTTEXT 目标语言文本，不带标记。 <br/>
	 *         Key TGTCONTENT 目标语言内容，带标记。 <br/>
	 *         Key TGTCREATIONID 目标节点创建者ID。 <br/>
	 *         Key TGTCREATIONDATE 目标节点创建者时间 <br/>
	 *         Key TGTCHANGEID 目标节点修改者ID。<br/>
	 *         Key TGTCHANGEDATE 目标节点修复时间。 <br/>
	 *         Key TGTPROJECTREF 目标节点项目参考信息属性。 <br/>
	 *         Key TGTJOBREF 目标节点任务参考信息属性。。 <br/>
	 *         Key TGTCLIENT 目标节点客户参考信息属性。 <br/>
	 *         Key SIMILARITY 匹配率。<br/>
	 *         若无匹配，返回 null。
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> findMatch(String srcText, String srcContent, String srcLang,
			String tgtLang, boolean caseSensitive, int minSimilarity, int matchUpperLimit, int tagPenalty,
			boolean isSinglePenalty, boolean overwritePerfectMatch) throws SQLException {
		// TODO 需添加实现。请参考 R7 业务逻辑代码。若具体数据库实现的代码需重写，请到具体实现类中实现或是重写。

		return null;
	}

	/**
	 * 查找指定正则表达式相应语言的匹配，并且支持分页与指定字段和规则的排序。
	 * @param strLangs
	 *            指定的语言，出于效率和资源占用因素考量，不应指定超过三种语言，其中源语言应为第一个元素。
	 * @param strRegex
	 *            指定的正则表达式。
	 * @param strMatchField
	 *            指定正则表达式需匹配的字段。该字段应为 SQL 语句中查询列表中存在的字段或别名。若使用字段且存在多表连接时应指定表别名。
	 * @param strOrderField
	 *            指定查询排序的字段。取值规则同指定的匹配字段。
	 * @param strOrderRule
	 *            指定排序规则。取值固定为 ASC 或 DESC。默认为 DESC。
	 * @param minSimilarity
	 *            最小匹配率。为百分比值。如 30%，则取值 30.
	 * @param matchUpperLimit
	 *            匹配个数上限。
	 * @param tagPenalty
	 *            错误标记罚分标准
	 * @param isSinglePenalty
	 *            罚分策略。Yes，则每个错误都要扣一次分。No，则所有错误都只扣一次分。
	 * @param overwritePerfectMatch
	 *            在匹配率为 100% 的完美匹配超过多个时，是否通过覆盖策略仅保留一个。
	 * @return Vector<Hashtable<String,String> 返回匹配信息。<br/>
	 *         每条术语信息使用一个 Hashable 保存。分别包括如下键值信息:<br/>
	 *         Key TUID 翻译单元ID <br/>
	 *         Key SRCLANG 源语言代码 <br/>
	 *         Key SRCTEXT 源语言文本，不带标记。 <br/>
	 *         Key SRCCONTENT 源语言内容，带标记。 <br/>
	 *         Key SRCCREATIONID 源节点创建者ID。 <br/>
	 *         Key SRCCREATIONDATE 源节点创建时间。 <br/>
	 *         Key SRCCHANGEID 源节点修改者ID。 <br/>
	 *         Key SRCCHANGEDATE 源节点修改时间。 <br/>
	 *         Key SRCPROJECTREF 源节点项目参考信息属性。 <br/>
	 *         Key SRCJOBREF 源节点任务参考信息属性。 <br/>
	 *         Key TGTLANG 目标语言代码 <br/>
	 *         Key TGTTEXT 目标语言文本，不带标记。 <br/>
	 *         Key TGTCONTENT 目标语言内容，带标记。 <br/>
	 *         Key TGTCREATIONID 目标节点创建者ID。 <br/>
	 *         Key TGTCREATIONDATE 目标节点创建者时间 <br/>
	 *         Key TGTCHANGEID 目标节点修改者ID。<br/>
	 *         Key TGTCHANGEDATE 目标节点修复时间。 <br/>
	 *         Key TGTPROJECTREF 目标节点项目参考信息属性。 <br/>
	 *         Key TGTJOBREF 目标节点任务参考信息属性。。 <br/>
	 *         Key SIMILARITY 匹配率。<br/>
	 *         若无匹配，返回 null。
	 */
	public Vector<Hashtable<String, String>> findMatchByRegex(String[] strLangs, String strRegex, String strMatchField,
			String strOrderField, String strOrderRule, int iMaxRow, int iMinRow, int iDBIndexOffset) {
		// TODO 需添加实现。请参考 R7 业务逻辑代码。若具体数据库实现的代码需重写，请到具体实现类中实现或是重写。

		return null;
	}

	/**
	 * 查找指定文本相应语言高于最低匹配率的匹配信息，用于快速翻译算法，与查找匹配系列方法相比较，其返回值较少。
	 * @param srcText
	 *            指定的源文本。
	 * @param srcLang
	 *            源语言代码。
	 * @param tgtLang
	 *            目标语言代码。
	 * @param caseSensitive
	 *            是否区分大小写。
	 * @param minSimilarity
	 *            最小匹配率。为百分比值。如 30%，则取值 30.
	 * @return Vector<Hashtable<String,String> 返回匹配信息。<br/>
	 *         每条术语信息使用一个 Hashable 保存。分别包括如下键值信息:<br/>
	 *         Key TUID 翻译单元ID <br/>
	 *         Key SRCLANG 源语言代码 <br/>
	 *         Key SRCTEXT 源语言文本，不带标记。 <br/>
	 *         Key TGTLANG 目标语言代码 <br/>
	 *         Key TGTTEXT 目标语言文本，不带标记。 <br/>
	 *         Key SIMILARITY 匹配率。<br/>
	 *         若无匹配，返回 null。
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> findMatch4QT(String srcText, String srcLang, String tgtLang,
			int minSimilarity, boolean caseSensitive) throws SQLException {
		// TODO 需添加实现。请参考 R7 业务逻辑代码。若具体数据库实现的代码需重写，请到具体实现类中实现或是重写。

		return null;
	}

	/**
	 * 根据节点的纯文本获取对应的术语 robert 2011-12-22
	 * @param srcPureText
	 *            源节点的纯文本
	 * @param srcLang
	 *            源语言
	 * @param tarLang
	 *            目标语言
	 * @return
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> findAllTermsByText(String srcPureText, String srcLang, String tarLang)
			throws SQLException {
		return null;
	}

	/**
	 * 通过SQL和参数拼装SQL并执行查询
	 * @param querySql
	 *            SQL语句
	 * @param params
	 *            参数,和SQL中对应的
	 * @return key行号,value map<列名,值>
	 * @throws SQLException
	 *             ;
	 */
	Map<Integer, Map<String, String>> data = new LinkedHashMap<Integer, Map<String, String>>();

	protected Map<Integer, Map<String, String>> query(String querySql, Object[] params) throws SQLException {
		data.clear();
		PreparedStatement psmt = null;
		ResultSet rt = null;
		try {
			if (null == conn) {
				return data;
			}
			psmt = conn.prepareStatement(querySql);

			for (int i = 0; i < params.length; i++) {
				setParameter(psmt, i + 1, params[i]);
			}

			rt = psmt.executeQuery();
			ResultSetMetaData meta = rt.getMetaData();
			int cols = meta.getColumnCount();
			int rowIndex = 0;
			while (rt.next()) {
				Map<String, String> rowData = new HashMap<String, String>();
				for (int i = 1; i <= cols; i++) {
					rowData.put(meta.getColumnLabel(i).toUpperCase(), rt.getString(i));
				}
				data.put(rowIndex++, rowData);
			}
		} finally {
			if (rt != null) {
				rt.close();
			}
			if (psmt != null) {
				psmt.close();
			}
		}
		return data;

	}

	/**
	 * 通过参数构建SQL
	 * @param psmt
	 * @param paramIndex
	 * @param param
	 *            ;
	 */
	protected void setParameter(PreparedStatement psmt, int paramIndex, Object param) {
		try {
			if (param instanceof String) {
				psmt.setString(paramIndex, (String) param);
			} else if (param instanceof Integer) {
				psmt.setInt(paramIndex, (Integer) param);
			} else if (param instanceof Long) {
				psmt.setLong(paramIndex, (Long) param);
			} else if (param instanceof Float) {
				psmt.setFloat(paramIndex, (Float) param);
			} else if (param instanceof Double) {
				psmt.setDouble(paramIndex, (Double) param);
			} else if (param instanceof Timestamp) {
				psmt.setTimestamp(paramIndex, (Timestamp) param);
			} else if (param instanceof Date) {
				psmt.setTimestamp(paramIndex, new Timestamp(((Date) param).getTime()));
			} else if (null == param) {
				psmt.setNull(paramIndex, 0);
			}
		} catch (SQLException e) {
			LOGGER.error("", e);
		}
	}

	// TODO ================tbx(开始)==================
	/**
	 * 写MartifHeader节点内容
	 * @param hContent
	 *            整个节点的内容
	 * @param hIdAttr
	 *            MartifHeader节点的ID属性;
	 * @return
	 * @throws SQLException
	 */
	public int insertBMartifHeader(String hContent, String hIdAttr) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-bmartifheader");
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, hIdAttr);
			stmt.setString(2, hContent);
			stmt.execute();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return 0;
	}

	/**
	 * 写BAttribute的内容
	 * @param attrs
	 * @param parentName
	 * @param parentId
	 * @throws SQLException
	 *             ;
	 */
	public void insertBAttribute(Map<String, String> attrs, String parentName, int parentId) throws SQLException {
		if (attrs != null) {
			PreparedStatement stmt = null;
			String sql = dbConfig.getOperateDbSQL("insert-battribute");
			Iterator<Entry<String, String>> iter = attrs.entrySet().iterator();
			try {
				while (iter.hasNext()) {
					Entry<String, String> entry = iter.next();
					String attrName = entry.getKey();
					String attrValue = entry.getValue();
					stmt = conn.prepareStatement(sql);
					stmt.setInt(1, parentId);
					stmt.setString(2, attrName);
					stmt.setString(3, attrValue);
					stmt.setString(4, parentName);
					stmt.addBatch();
				}
				stmt.executeBatch();
				stmt.clearBatch();
			} finally {
				if (stmt != null) {
					stmt.close();
				}
			}
		}
	}

	/**
	 * 写BRefObjectList内容
	 * @param roblContent
	 * @param roblIdAttr
	 * @param headerId
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public int insertBRefobjectlist(String roblContent, String roblIdAttr, int headerId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-brefobjectlist");
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, headerId);
			stmt.setString(2, roblIdAttr);
			stmt.setString(3, roblContent);
			stmt.execute();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return 0;
	}

	/**
	 * 写TermEntry内容
	 * @param teContent
	 * @param teIdAttr
	 * @param headerId
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public int insertBTermentry(String teContent, String teIdAttr, int headerId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("insert-btermentry");
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, headerId);
			stmt.setString(2, teIdAttr);
			stmt.setString(3, teContent);
			stmt.execute();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return 0;
	}

	public void insertBNode(int pPk, String pName, String nType, String nName, String nId, String content)
			throws SQLException {
		PreparedStatement stmt = null;
		try {
			// INSERT INTO BNODE
			// (PARENTPKID, PARENTNAME, NTYPE, NNAME, NID, CONTENT)
			// VALUES (?, ?, ?, ?, ?, ?)
			String sql = dbConfig.getOperateDbSQL("insert-bnote");
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, pPk);
			stmt.setString(2, pName);
			stmt.setString(3, nType);
			stmt.setString(4, nName);
			stmt.setString(5, nId);
			stmt.setString(6, content);
			stmt.execute();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

	}

	public void deleteBNode(List<String> nPk) throws SQLException {
		if (nPk == null || nPk.size() == 0) {
			return;
		}
		PreparedStatement stmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("delete-bnode-bypk");
			stmt = conn.prepareStatement(sql);
			for (String pk : nPk) {
				stmt.setInt(1, Integer.parseInt(pk));
				stmt.addBatch();
			}
			stmt.executeBatch();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 根据父信息获取扩展节点或者属性
	 * @param pPk
	 *            父主键 必须的
	 * @param pName
	 *            父节点名称 can be null
	 * @param nType
	 *            节点类型 "E","A" or null
	 * @param nName
	 *            节点名称 "E"=节点名称 "A"=属性名
	 * @param nId
	 *            对节点有效，存储节点的id属性值
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public List<Map<String, String>> getBNodeByParent(String pPk, String pName, String nType, String nName, String nId)
			throws SQLException {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("getBnode-byParent");

			StringBuffer bf = new StringBuffer();
			if (pPk == null || pPk.equals("")) {
				return result;
			}
			bf.append(" AND PARENTPKID = ").append(pPk);
			if (pName != null) {
				bf.append(" AND PARENTNAME='").append(pName).append("'");
			}
			if (nType != null) {
				bf.append(" AND NTYPE = '").append(nType).append("'");
			}
			if (nName != null) {
				bf.append(" AND NNAME='").append(nName).append("'");
			}
			if (nId != null) {
				bf.append(" AND NID='").append(nId).append("'");
			}
			sql = sql.replace("__WHERE__", bf.toString());

			stmt = conn.createStatement();

			rs = stmt.executeQuery(sql);
			ResultSetMetaData meta = rs.getMetaData();
			int cols = meta.getColumnCount();
			while (rs.next()) {
				Map<String, String> rowData = new HashMap<String, String>();
				for (int i = 1; i <= cols; i++) {
					rowData.put(meta.getColumnName(i).toUpperCase(), rs.getString(i));
				}
				result.add(rowData);
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return result;
	}

	/**
	 * 通过主键获集取BTERMENTRY表中的对应的全部记录
	 * @param pks
	 *            BTERMENTRY表的主键集
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public List<Map<String, String>> getBTermEntryByPk(List<String> pks) throws SQLException {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("getTermEntrys-bypks");
			String sqlResult = sql;
			for (int i = 1; i < pks.size(); i++) {
				sqlResult += " UNION " + sql;
			}
			stmt = conn.prepareStatement(sqlResult);

			for (int i = 0; i < pks.size(); i++) {
				stmt.setInt(i + 1, Integer.parseInt(pks.get(i)));
			}

			rs = stmt.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			int cols = meta.getColumnCount();
			while (rs.next()) {
				Map<String, String> rowData = new HashMap<String, String>();
				for (int i = 1; i <= cols; i++) {
					rowData.put(meta.getColumnName(i).toUpperCase(), rs.getString(i));
				}
				result.add(rowData);
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return result;
	}

	/**
	 * 通过BTERMENTRY表的主键获取该TermEntry的内容
	 * @param termEntrypk
	 * @return TermEntry节点的完整内容;
	 * @throws SQLException
	 */
	public String getTermEntryContentByPk(Integer termEntrypk) throws SQLException {
		String result = "";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("getTermEntryContent-bypk");
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, termEntrypk);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return result = rs.getString(1);
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return result;
	}

	/**
	 * 通过TextData表中的记录主键,获取它的所在组的主键,即获取TermEntry表中的对应的主键
	 * @param textDataId
	 *            一组TextData表中的主键
	 * @return BTERMENTRY表中的主键
	 * @throws SQLException
	 *             ;
	 */
	public List<String> getTextDataGroupIdByTextId(List<String> textDataId) throws SQLException {
		List<String> groupIds = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = dbConfig.getOperateDbSQL("get-textdatagroupid-bytextid");
			String sqlResult = sql;
			for (int i = 1; i < textDataId.size(); i++) {
				sqlResult += " UNION " + sql;
			}
			stmt = conn.prepareStatement(sqlResult);

			for (int i = 0; i < textDataId.size(); i++) {
				stmt.setInt(i + 1, Integer.parseInt(textDataId.get(i)));
			}
			rs = stmt.executeQuery();
			while (rs.next()) {
				groupIds.add(rs.getString(1));
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return groupIds;
	}

	/**
	 * 更新Term Entry表中的记录
	 * @param content
	 *            更新的内容
	 * @param pk
	 *            主键
	 * @throws SQLException
	 *             ;
	 */
	public void updateTermEntry(String content, String pk) throws SQLException {
		PreparedStatement pstmt = null;
		try {
			String sql = dbConfig.getOperateDbSQL("upateTermEntry-bypk");
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, content);
			pstmt.setInt(2, Integer.parseInt(pk));
			pstmt.executeUpdate();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}

	/**
	 * 删除TermEntry，删除BTERMENTRY表中的记录
	 * @param termEntryId
	 *            Term Entry在表中的主键
	 * @param lang
	 *            语言
	 * @throws SQLException
	 *             ;
	 */
	public void deleteTerm(String termEntryId, String lang) throws SQLException {
		PreparedStatement stmt = null;
		try {
			// 删除martrix_lang表中的记录
			List<TmxSegement> textDataPk = getTextDataIdByGroupIdLang(Integer.parseInt(termEntryId), "B", lang);
			if (textDataPk.size() == 0) {
				return;
			}
			String set = textDataPk.get(0).getDbPk() + "";
			for (int i = 1; i < textDataPk.size(); i++) {
				set = set + "," + textDataPk.get(i).getDbPk();
			}
			deleteMatrxLang(set, lang);

			// 删除TextData表中的记录
			String sql = dbConfig.getOperateDbSQL("delete-textData-bygoupid");
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, Integer.parseInt(termEntryId));
			stmt.setString(2, lang);
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	// TODO =====================================================tbx(结束)============================

	/**
	 * 取得数据库中所有的语言
	 * @return ;
	 * @throws SQLException
	 */
	public List<String> getLanguages() throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> result = new ArrayList<String>();
		try {
			String getLangCount = dbConfig.getOperateDbSQL("get-langs");
			stmt = conn.prepareStatement(getLangCount);
			rs = stmt.executeQuery();
			while (rs.next()) {
				result.add(rs.getString(1));
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return result;
	}

	/**
	 * 通过源文本从数据库MATRIX_Lang表中查找TextData记录
	 * @param srcLang
	 *            源文本语言
	 * @param similarity
	 *            相似率
	 * @param srcPureText
	 *            源文本纯文本内容
	 * @return 如果没有查找到返回null,否则返回一组
	 * @throws SQLException
	 *             ;
	 */
	protected Map<String, Integer> getCandidatesTextDataPks(String srcLang, int minNgramSize, int maxNgramSize,
			int[] ngrams) throws SQLException {
		Map<String, Integer> result = new HashMap<String, Integer>();
		if (!this.langCaches.contains(srcLang)) {
			if (!hasLangInDB(srcLang)) {
				return result;
			}
			langCaches.add(srcLang);
		}

		String set = "" + ngrams[0];
		for (int i = 1; i < ngrams.length; i++) {
			set = set + "," + ngrams[i];
		}
		String select = dbConfig.getMatrixSQL("search");
		select = select.replaceAll("__SET__", set);
		select = select.replaceAll("__LANG__", srcLang.replace("-", "").toUpperCase());
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(select);
			stmt.setInt(1, minNgramSize);
			stmt.setInt(2, maxNgramSize);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String tpkId = rs.getString(1);
				if (result.containsKey(tpkId)) {
					int c = 1 + result.get(tpkId);
					result.put(tpkId, c);
				} else {
					result.put(tpkId, 1);
				}
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return result;
	}

	// TODO ======================匹配相关（开始） ======================//

	/**
	 * 将文本内容生成文本片段
	 * @param srcLang
	 * @param text
	 * @return ;
	 */
	protected int[] generateNgrams(String srcLang, String text) {
		boolean asian = srcLang.startsWith("zh") || srcLang.startsWith("ja") || srcLang.startsWith("ko")
				|| srcLang.startsWith("th") || srcLang.startsWith("he");
		int[] ngrams = NGrams.getNGrams(text, asian);
		return ngrams;
	}

	/**
	 * 通过源文本的纯文本内容查找匹配<br>
	 * 需要处理的业务逻辑：通过从库中的查找结果计算匹配率；同时需要通过最大上限来限制返回最的果集中的记录数<br>
	 * 返回的结果中按匹配率从高到低依次排序
	 * @param puretext
	 *            源文本纯文本
	 * @param srcLang
	 *            源语言
	 * @param tgtLang
	 *            目标语言
	 * @param minSimilarity
	 *            最低匹配率
	 * @param caseSensitive
	 *            是否需要关心大小写
	 * @param matchUpperLimit
	 *            匹配上限
	 * @param contextSize
	 *            上下文个数
	 * @param preHash
	 *            上文
	 * @param nextHash
	 *            下文
	 * @return Vector<Hashtable<String,String> 返回匹配信息。<br/>
	 *         每条术语信息使用一个 Hashable 保存。分别包括如下键值信息:<br/>
	 *         Key tuId 翻译单元ID <br/>
	 *         Key srcLang 源语言代码 <br/>
	 *         Key srcText 源语言文本，不带标记。 <br/>
	 *         Key srcContent 源语言内容，带标记。 <br/>
	 *         Key srcCreationId 源节点创建者ID。 <br/>
	 *         Key srcCreationDate 源节点创建时间。 <br/>
	 *         Key srcChangeId 源节点修改者ID。 <br/>
	 *         Key srcChangeDate 源节点修改时间。 <br/>
	 *         Key srcProjectRef 源节点项目参考信息属性。 <br/>
	 *         Key srcJobRef 源节点任务参考信息属性。 <br/>
	 *         Key srcClient 源节点客户参考信息属性。 <br/>
	 *         Key tgtLang 目标语言代码 <br/>
	 *         Key tgtText 目标语言文本，不带标记。 <br/>
	 *         Key tgtContent 目标语言内容，带标记。 <br/>
	 *         Key tgtCreationId 目标节点创建者ID。 <br/>
	 *         Key tgtCreationDate 目标节点创建者时间 <br/>
	 *         Key tgtChangeId 目标节点修改者ID。<br/>
	 *         Key tgtChangeDate 目标节点修改时间。 <br/>
	 *         Key tgtProjectRef 目标节点项目参考信息属性。 <br/>
	 *         Key tgtJobRef 目标节点任务参考信息属性。。 <br/>
	 *         Key tgtClient 目标节点客户参考信息属性。 <br/>
	 *         Key similarity 本条匹配的匹配率。<br/>
	 *         若无匹配，返回一个size为0的Vector;
	 * @throws SQLException
	 */
	public Vector<Hashtable<String, String>> findMatch(String puretext, String srcLang, String tgtLang,
			int minSimilarity, boolean caseSensitive, int matchUpperLimit, int contextSize, String preHash,
			String nextHash) throws SQLException {
		// TODO在计算匹配率时，可能需要加入罚分策略。
		return null;
	}

	public Vector<Hashtable<String, String>> findMatch_1(String text, String fullText, String srcLang, String tgtLang,
			int minSimilarity, boolean caseSensitive, int matchUpperLimit, int contextSize, String preHash,
			String nextHash, boolean isIngoreTarget) throws SQLException {
		Vector<Hashtable<String, String>> result = new Vector<Hashtable<String, String>>();
		this.commit();
		int[] ngrams = generateNgrams(srcLang, text);
		int size = ngrams.length;
		if (size == 0) {
			return result;
		}
		// long l1 = System.currentTimeMillis();
		int min = size * minSimilarity / 100;
		int max = size * 100 / minSimilarity;
		Map<String, Integer> tpkids = getCandidatesTextDataPks(srcLang, min, max, ngrams);
		// System.out.println("查MATEX_LANG表:"+(System.currentTimeMillis() - l1));
		// 构建SQL
		Iterator<Entry<String, Integer>> it = tpkids.entrySet().iterator();
		StringBuffer bf = new StringBuffer();
		// long l = System.currentTimeMillis();
		while (it.hasNext()) {
			Entry<String, Integer> entry = it.next();
			String tpkid = entry.getKey();
			float c = entry.getValue();
			if (c >= min && c <= max) {
				bf.append(",");
				bf.append(tpkid);
			}
		}
		if (bf.toString().equals("")) {
			return result;
		}
		String textDataSql = dbConfig.getOperateDbSQL("getTMMatch1");
		textDataSql = textDataSql.replace("__SET__", bf.toString().substring(1));
		PreparedStatement stmt = conn.prepareStatement(textDataSql);
		stmt.setString(1, srcLang);
		stmt.setString(2, tgtLang);
		// System.out.println(stmt);
		ResultSet rs = stmt.executeQuery();
		Map<Integer, Map<String, String>> tuSrc = new HashMap<Integer, Map<String, String>>();
		Map<Integer, Map<String, String>> tuTgt = new HashMap<Integer, Map<String, String>>();
		while (rs.next()) {
			Integer groupId = rs.getInt("GROUPID");
			String lang = rs.getString("LANG");
			String pureText = rs.getString("PURE");
			String content = rs.getString("CONTENT");

			String creationId = rs.getString("CREATIONID");
			creationId = creationId == null ? "" : creationId;

			String creationDate = "";
			Timestamp tempCdate = rs.getTimestamp("CREATIONDATE");
			if (tempCdate != null) {
				creationDate = DateUtils.formatToUTC(tempCdate.getTime());
			}

			String changeDate = "";
			Timestamp tempChangeDate = rs.getTimestamp("CHANGEDATE");
			if (tempChangeDate != null) {
				changeDate = DateUtils.formatToUTC(tempChangeDate.getTime());
			}

			String changeid = rs.getString("CHANGEID");
			changeid = changeid == null ? "" : changeid;

			String projectRef = rs.getString("PROJECTREF");
			projectRef = projectRef == null ? "" : projectRef;

			String jobRef = rs.getString("JOBREF");
			jobRef = jobRef == null ? "" : jobRef;

			String client = rs.getString("CLIENT");
			client = client == null ? "" : client;

			if (lang.equalsIgnoreCase(srcLang)) {
				int distance;
				if (caseSensitive) {
					if (isIngoreTarget) {
						distance = similarity(text, pureText);
					} else {
						distance = similarity(fullText, content);
					}
				} else {
					if (isIngoreTarget) {
						distance = similarity(text.toLowerCase(), pureText.toLowerCase());
					} else {
						distance = similarity(fullText.toLowerCase(), content.toLowerCase());
					}
				}

				if (distance == 100 && CommonFunction.checkEdition("U")) {
					String preContext = rs.getString("PRECONTEXT");
					String nextContext = rs.getString("NEXTCONTEXT");
					if (preContext != null && nextContext != null) {
						String[] preContexts = preContext.split(",");
						String[] nextContexts = nextContext.split(",");
						if (preContexts.length > contextSize) {
							preContext = ""; //$NON-NLS-1$
							for (int i = 0; i < contextSize; i++) {
								preContext += "," + preContexts[i]; //$NON-NLS-1$
							}
							if (!"".equals(preContext)) { //$NON-NLS-1$
								preContext = preContext.substring(1);
							}
						}

						if (nextContexts.length > contextSize) {
							nextContext = ""; //$NON-NLS-1$
							for (int i = 0; i < contextSize; i++) {
								nextContext += "," + nextContexts[i]; //$NON-NLS-1$
							}
							if (!"".equals(nextContext)) { //$NON-NLS-1$
								nextContext = nextContext.substring(1);
							}
						}

						if (preHash.equals(preContext) && nextHash.equals(nextContext)) {
							distance = 101;
						}
					}
				}

				if (distance >= minSimilarity) {
					Map<String, String> srcMap = new HashMap<String, String>();
					srcMap.put("srcLang", srcLang);
					srcMap.put("srcText", pureText);
					srcMap.put("srcContent", content);
					srcMap.put("srcCreationId", creationId);
					srcMap.put("srcCreationDate", creationDate);
					srcMap.put("srcChangeId", changeid);
					srcMap.put("srcChangeDate", changeDate);
					srcMap.put("srcProjectRef", projectRef);
					srcMap.put("srcJobRef", jobRef);
					srcMap.put("srcClient", client);
					srcMap.put("similarity", distance + "");
					tuSrc.put(groupId, srcMap);
				}
			}
			if (lang.equalsIgnoreCase(tgtLang)) {
				Map<String, String> tgtMap = new HashMap<String, String>();
				tgtMap.put("tgtLang", tgtLang);
				tgtMap.put("tgtText", pureText);
				tgtMap.put("tgtContent", content);
				tgtMap.put("tgtCreationId", creationId);
				tgtMap.put("tgtCreationDate", creationDate);
				tgtMap.put("tgtChangeId", changeid);
				tgtMap.put("tgtChangeDate", changeDate);
				tgtMap.put("tgtProjectRef", projectRef);
				tgtMap.put("tgtJobRef", jobRef);
				tgtMap.put("tgtClient", client);
				tuTgt.put(groupId, tgtMap);
			}
		}
		if (stmt != null) {
			stmt.close();
		}
		if (rs != null) {
			rs.close();
		}
		String dbName = getMetaData().getDatabaseName();
		if (tuSrc.size() > 0) {
			Iterator<Entry<Integer, Map<String, String>>> itr = tuSrc.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<Integer, Map<String, String>> entry = itr.next();
				Integer key = entry.getKey();
				Map<String, String> srcMap = entry.getValue();
				Map<String, String> tgtMap = tuTgt.get(key);
				if (tgtMap == null) {
					continue;
				}
				Hashtable<String, String> tu = new Hashtable<String, String>();
				tu.putAll(srcMap);
				tu.putAll(tgtMap);
				if (!isDuplicated(result, tu)) {
					tu.put("tupkid", key + "");
					tu.put("dbName", dbName); // 应用于origin属性
					result.add(tu);
				}
			}
		}

		int resultSize = result.size();
		if (resultSize > 1) {
			Collections.sort(result, new FindMatchComparator());
		}

		while (resultSize > matchUpperLimit) {
			resultSize--;
			result.remove(resultSize);
		}
		// System.out.println("查TEXTDATA表:"+(System.currentTimeMillis() - l));
		// System.out.println(bf.toString());
		return result;
	}

	public void fuzzySearch(String pureText, String fullText, String srcLang, String tgtLang, int minSimilarity,
			boolean caseSensitive, int matchUpperLimit, int contextSize, String preHash, String nextHash,
			boolean isIngoreTarget, FuzzySearchResults searchResults, int tagPelanty) throws SQLException {

		int[] ngrams = generateNgrams(srcLang, pureText);
		int size = ngrams.length;
		if (size == 0) {
			return;
		}
		this.commit();
		// long l1 = System.currentTimeMillis();
		int min = size * minSimilarity / 100;
		int max = size * 100 / minSimilarity;
		Map<String, Integer> tpkids = getCandidatesTextDataPks(srcLang, min, max, ngrams);
		// System.out.println("查MATEX_LANG表:"+(System.currentTimeMillis() - l1));
		// 构建SQL
		Iterator<Entry<String, Integer>> it = tpkids.entrySet().iterator();
		StringBuffer bf = new StringBuffer();
		// long l = System.currentTimeMillis();
		while (it.hasNext()) {
			Entry<String, Integer> entry = it.next();
			String tpkid = entry.getKey();
			float c = entry.getValue();
			if (c >= min && c <= max) {
				bf.append(",");
				bf.append(tpkid);
			}
		}
		if (bf.toString().equals("")) {
			return;
		}
		String tag = TranslationMemoryTools.getInnerTagContent(fullText);
		// SELECT TPKID, GROUPID, PURE, CONTENT, PRECONTEXT, NEXTCONTEXT FROM TEXTDATA WHERE TPKID IN (__SET__) ORDER BY
		// GROUPID DESC
		String textDataSql = dbConfig.getOperateDbSQL("fuzzySearch");
		textDataSql = textDataSql.replace("__SET__", bf.toString().substring(1));
		Statement stm = null;
		ResultSet rs = null;
		Statement tmpStm = null;
		try {
			stm = conn.createStatement();
			tmpStm = conn.createStatement();
			rs = stm.executeQuery(textDataSql);
			// SELECT TPKID, PURE, CONTENT, CREATIONID, CREATIONDATE, CHANGEID, CHANGEDATE, PROJECTREF
			// FROM TEXTDATA INNER JOIN MTU ON MTU.MTUPKID = TEXTDATA.GROUPID AND TEXTDATA.GROUPID = __GROUPID__ AND
			// TEXTDATA.LANG = '__LANG__'
			String targetSql = dbConfig.getOperateDbSQL("fuzzySearch-target").replace("__LANG__", tgtLang);
			String dbName = getMetaData().getDatabaseName();
			while (rs.next()) {
				String _pureText = rs.getString(3);
				String _fullText = rs.getString(4);
				int similarity = 0;
				if (caseSensitive) {
					similarity = similarity(pureText, _pureText);
				} else {
					similarity = similarity(pureText.toLowerCase(), _pureText.toLowerCase());
				}

				String _tag = TranslationMemoryTools.getInnerTagContent(_fullText);
				if (!isIngoreTarget && !tag.equals(_tag)) {
					// 标记内容不相等，则执行罚分
					similarity -= tagPelanty;
				}

				if (similarity < minSimilarity) {
					continue;
				}
				int tuId = rs.getInt(2);
				String temptargetSql = targetSql.replace("__GROUPID__", tuId + "");
				// TPKID, PURE, CONTENT, CREATIONID, CREATIONDATE, CHANGEID, CHANGEDATE ,PROJECTREF
				ResultSet rs1 = null;
				try {
					rs1 = tmpStm.executeQuery(temptargetSql);
					if (rs1.next()) {
						TmxSegement source = new TmxSegement(_pureText, _fullText, srcLang);
						source.setDbPk(rs.getInt(1));
						_pureText = rs1.getString(2);
						_fullText = rs1.getString(3);
						if (_pureText == null || _pureText.equals("") || _fullText == null || _fullText.equals("")) {
							continue;
						}
						TmxSegement target = new TmxSegement(_pureText, _fullText, tgtLang);
						target.setDbPk(rs1.getInt(1));
						TmxTU tu = new TmxTU(source, target);
						FuzzySearchResult searchRs = new FuzzySearchResult(tu);
						if (searchResults.contains(searchRs)) {
							continue;
						}

						String creationId = rs1.getString(4);
						creationId = creationId == null ? "" : creationId;
						String creationDate = "";
						Timestamp tempCdate = rs1.getTimestamp(5);
						if (tempCdate != null) {
							creationDate = DateUtils.formatToUTC(tempCdate.getTime());
						}
						String changeid = rs1.getString(6);
						changeid = changeid == null ? "" : changeid;
						String changeDate = "";
						Timestamp tempChangeDate = rs1.getTimestamp(7);
						if (tempChangeDate != null) {
							changeDate = DateUtils.formatToUTC(tempChangeDate.getTime());
						}
						String projectRef = rs1.getString(8);
						projectRef = projectRef == null ? "" : projectRef;
						tu.setCreationDate(creationDate);
						tu.setCreationUser(creationId);
						tu.setChangeDate(changeDate);
						tu.setChangeUser(changeid);
						List<TmxProp> attrs = getTuMprops(tuId, "TU");
						tu.setProps(attrs);

						String preContext = rs.getString(5);
						String nextContext = rs.getString(6);
						tu.appendContext(TmxContexts.PRE_CONTEXT_NAME, preContext);
						tu.appendContext(TmxContexts.NEXT_CONTEXT_NAME, nextContext);
						if (similarity == 100 && CommonFunction.checkEdition("U")) {
							if (preContext != null && nextContext != null) {
								String[] preContexts = preContext.split(",");
								String[] nextContexts = nextContext.split(",");
								if (preContexts.length > contextSize) {
									preContext = ""; //$NON-NLS-1$
									for (int i = 0; i < contextSize; i++) {
										preContext += "," + preContexts[i]; //$NON-NLS-1$
									}
									if (!"".equals(preContext)) { //$NON-NLS-1$
										preContext = preContext.substring(1);
									}
								}

								if (nextContexts.length > contextSize) {
									nextContext = ""; //$NON-NLS-1$
									for (int i = 0; i < contextSize; i++) {
										nextContext += "," + nextContexts[i]; //$NON-NLS-1$
									}
									if (!"".equals(nextContext)) { //$NON-NLS-1$
										nextContext = nextContext.substring(1);
									}
								}

								if (preHash.equals(preContext) && nextHash.equals(nextContext)) {
									similarity = 101;
								}
							}
						}
						searchRs.setDbName(dbName);
						searchRs.setSimilarity(similarity);
						searchRs.setDbOp(this);
						searchRs.getTu().setTmId(tuId);
						searchResults.add(searchRs);
					}
				} finally {
					if (rs1 != null) {
						rs1.close();
					}
				}
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
			if (tmpStm != null) {
				tmpStm.close();
			}
		}
	}

	public void translationUnitAnalysis(String pureText, String fullText, String srcLang, String tgtLang,
			int minSimilarity, boolean caseSensitive, int matchUpperLimit, int contextSize, String preHash,
			String nextHash, boolean isIngoreTarget, TranslationUnitAnalysisResults analysisResults, int tagPelanty)
			throws SQLException {
		int[] ngrams = generateNgrams(srcLang, pureText);
		int size = ngrams.length;
		if (size == 0) {
			return;
		}
		this.commit();
		// long l1 = System.currentTimeMillis();
		int min = size * minSimilarity / 100;
		int max = size * 100 / minSimilarity;
		Map<String, Integer> tpkids = getCandidatesTextDataPks(srcLang, min, max, ngrams);
		// System.out.println("查MATEX_LANG表:"+(System.currentTimeMillis() - l1));
		// 构建SQL
		Iterator<Entry<String, Integer>> it = tpkids.entrySet().iterator();
		StringBuffer bf = new StringBuffer();
		// long l = System.currentTimeMillis();
		while (it.hasNext()) {
			Entry<String, Integer> entry = it.next();
			String tpkid = entry.getKey();
			float c = entry.getValue();
			if (c >= min && c <= max) {
				bf.append(",");
				bf.append(tpkid);
			}
		}
		if (bf.toString().equals("")) {
			return;
		}
		String tag = TranslationMemoryTools.getInnerTagContent(fullText);
		String textDataSql = dbConfig.getOperateDbSQL("fuzzySearch");
		textDataSql = textDataSql.replace("__SET__", bf.toString().substring(1));
		Statement stm = null;
		ResultSet rs = null;
		Statement tmpStm = null;
		try {
			stm = conn.createStatement();
			tmpStm = conn.createStatement();
			rs = stm.executeQuery(textDataSql);
			// SELECT GROUPID, PURE, CONTENT, PRECONTEXT, NEXTCONTEXT FROM TEXTDATA WHERE TPKID IN (__SET__)
			String dbName = getMetaData().getDatabaseName();
			while (rs.next()) {
				String _pureText = rs.getString(3);
				String _fullText = rs.getString(4);
				int similarity = 0;
				if (caseSensitive) {
					similarity = similarity(pureText, _pureText);
				} else {
					similarity = similarity(pureText.toLowerCase(), _pureText.toLowerCase());
				}

				String _tag = TranslationMemoryTools.getInnerTagContent(_fullText);
				if (!isIngoreTarget && !tag.equals(_tag)) {
					// 标记内容不相等，则执行罚分
					similarity -= tagPelanty;
				}

				if (similarity < minSimilarity) {
					continue;
				}
				if (similarity == 100 && CommonFunction.checkEdition("U")) {
					String preContext = rs.getString(5);
					String nextContext = rs.getString(6);
					if (preContext != null && nextContext != null) {
						String[] preContexts = preContext.split(",");
						String[] nextContexts = nextContext.split(",");
						if (preContexts.length > contextSize) {
							preContext = ""; //$NON-NLS-1$
							for (int i = 0; i < contextSize; i++) {
								preContext += "," + preContexts[i]; //$NON-NLS-1$
							}
							if (!"".equals(preContext)) { //$NON-NLS-1$
								preContext = preContext.substring(1);
							}
						}

						if (nextContexts.length > contextSize) {
							nextContext = ""; //$NON-NLS-1$
							for (int i = 0; i < contextSize; i++) {
								nextContext += "," + nextContexts[i]; //$NON-NLS-1$
							}
							if (!"".equals(nextContext)) { //$NON-NLS-1$
								nextContext = nextContext.substring(1);
							}
						}

						if (preHash.equals(preContext) && nextHash.equals(nextContext)) {
							similarity = 101;
						}
					}
				}
				TranslationUnitAnalysisResult r = new TranslationUnitAnalysisResult(similarity, dbName);
				analysisResults.add(r);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stm != null) {
				stm.close();
			}
			if (tmpStm != null) {
				tmpStm.close();
			}
		}
	}

	/**
	 * 匹配率计算
	 * @param x
	 * @param y
	 * @return ;
	 */
	protected int similarity(String x, String y) {
		return MatchQuality.similarity(x, y);
	}

	/**
	 * 判断vector中是否已经存在tu了
	 * @param vector
	 * @param tu
	 * @return true已经存在，false不存在
	 */
	protected boolean isDuplicated(Vector<Hashtable<String, String>> vector, Hashtable<String, String> tu) {
		int size = vector.size();
		String src = tu.get("srcText"); //$NON-NLS-1$
		String tgt = tu.get("tgtText"); //$NON-NLS-1$
		for (int i = 0; i < size; i++) {
			Hashtable<String, String> table = vector.get(i);
			if (src.trim().equals(table.get("srcText").trim()) //$NON-NLS-1$
					&& tgt.trim().equals(table.get("tgtText").trim())) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	/**
	 * 查找匹配结果排序器,按匹配率从高到低依次排序
	 * @author Jason
	 * @version
	 * @since JDK1.6
	 */
	protected final class FindMatchComparator implements Comparator<Hashtable<String, String>> {
		public FindMatchComparator() {
		}

		public int compare(Hashtable<String, String> a, Hashtable<String, String> b) {
			Integer a1 = Integer.parseInt(a.get("similarity"));
			Integer b1 = Integer.parseInt(b.get("similarity"));
			if (a1 < b1) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	// TODO ======================匹配相关（结束） ======================//

	/**
	 * 执行相关搜索
	 * @param strSearch
	 *            搜索文本
	 * @param isCaseSensitive
	 *            是否区分大小写
	 * @param isApplyRegular
	 *            是否使用正则表达式
	 * @param isIgnoreMark
	 *            是否忽略标记
	 * @param strLang
	 *            源语言
	 * @param lstLangs
	 *            语言集合(包括源语言)
	 * @param startIndex
	 *            开始行号
	 * @param intMax
	 *            最大查找结果数
	 * @return ;
	 */
	public List<ConcordanceBean> getConcordanceSearchResult(String strSearch, boolean isCaseSensitive,
			boolean isApplyRegular, boolean isIgnoreMark, String strLang, List<String> lstLangs, List<Integer> subList) {
		String sql = getTMSearchSql(dbConfig.getOperateDbSQL("search-Corcondance"), isIgnoreMark, lstLangs, subList);
		if (sql == null) {
			return null;
		}
		List<ConcordanceBean> lstConcordance = new ArrayList<ConcordanceBean>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			ArrayList<Integer> lstTuId = new ArrayList<Integer>();
			while (rs.next()) {
				Integer tuId = rs.getInt("TUID");
				if (lstTuId.contains(tuId)) {
					ConcordanceBean bean = lstConcordance.get(lstTuId.indexOf(tuId));
					LanguageTMX lang = new LanguageTMX();
					lang.setLanguageCode(rs.getString("LANG"));
					lang.setText(rs.getString("TMTEXT"));
					bean.getLanguageList().add(lang);
				} else {
					lstTuId.add(tuId);
					ConcordanceBean bean = new ConcordanceBean();
					bean.setId(tuId);
					bean.setCreationId(rs.getString("CREATIONID"));
					String creationDate = "";
					Timestamp tempCreationDate = rs.getTimestamp("CREATIONDATE");
					if (tempCreationDate != null) {
						creationDate = DateUtils.formatLongTime(tempCreationDate.getTime(), "yyyy-MM-dd HH:mm:ss");
					}
					bean.setCreationDate(creationDate);
					bean.setChangeId(rs.getString("CHANGEID"));
					String changeDate = "";
					Timestamp tempChangeDate = rs.getTimestamp("CHANGEDATE");
					if (tempChangeDate != null) {
						changeDate = DateUtils.formatLongTime(tempChangeDate.getTime(), "yyyy-MM-dd HH:mm:ss");
					}
					bean.setChangeDate(changeDate);
					bean.setBlnIsFlag(rs.getString("MPPKID") != null);
					List<LanguageTMX> lstLang = new ArrayList<LanguageTMX>();
					LanguageTMX lang = new LanguageTMX();
					lang.setLanguageCode(rs.getString("LANG"));
					lang.setText(rs.getString("TMTEXT"));
					lstLang.add(lang);
					bean.setLanguageList(lstLang);
					bean.setAttributeList(getTuMprops(tuId, "TU"));
					lstConcordance.add(bean);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("", e);
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOGGER.error("", e);
				}
			}
		}
		return lstConcordance;
	}

	/**
	 * 获取相关搜索 SQL 语句
	 * @param tmOrTb
	 * @return ;
	 */
	public String getTMSearchSql(String sql, boolean isIgnoreMark, List<String> lstLangs,
			List<Integer> lstGrouIdWithCurPage) {
		sql = Utils.replaceString(sql, "__IGNORE_MARK__", (isIgnoreMark ? "B.PURE" : "B.CONTENT") + " TMTEXT");
		sql = Utils.replaceString(
				sql,
				"__GROUPID_LIST__",
				"GROUPID IN("
						+ lstGrouIdWithCurPage.toString().substring(1, lstGrouIdWithCurPage.toString().length() - 1)
						+ ") AND ");

		String strLanguage = "(";
		for (String lang : lstLangs) {
			strLanguage += "LANG='" + lang + "' OR ";
		}
		if (strLanguage.length() > 1) {
			strLanguage = strLanguage.substring(0, strLanguage.length() - 3);
		}
		strLanguage += ")";
		if (strLanguage.length() == 2) {
			strLanguage = "";
		} else {
			strLanguage = " AND " + strLanguage;
		}
		sql = Utils.replaceString(sql, "__LANGUAGE_LIST__", strLanguage);
		// System.out.println(sql);
		return sql;
	}

	/**
	 * 替换相关搜索或搜索术语的 SQL 语句中__CONDITION__字段，各种数据库要覆盖此方法（各种数据库只有是否忽略大小写和正则表达式函数不同）
	 * @param sql
	 * @param strSearch
	 * @param isCaseSensitive
	 * @param isApplyRegular
	 * @param isIgnoreMark
	 * @param srcLang
	 * @param arrFilter
	 *            过滤条件，第一个为语言，第二个为 LIKE 或 NOT LIKE，第三个为匹配文本，默认查询 TEXTDATA 的 PURE 列，不区分大小写
	 * @return ;
	 */
	public String replaceTMOrTBConditionSql(String sql, String strSearch, boolean isCaseSensitive,
			boolean isApplyRegular, boolean isIgnoreMark, String srcLang, String[] arrFilter) {
		return null;
	}

	/**
	 * 添加/删除&lt;prop type='x-flag'&gt;HS-Flag&lt;/prop&gt;标记
	 * @param blnAddOrRemove
	 *            true 表示添加标记，false 表示删除
	 * @param strTuId
	 *            MTU 表的 ID 值
	 */
	public void addOrRemoveFlag(boolean blnAddOrRemove, String strTuId) {
		PreparedStatement stmt = null;
		String sql;
		if (blnAddOrRemove) {
			sql = dbConfig.getOperateDbSQL("insert-tmxprops");
		} else {
			sql = dbConfig.getOperateDbSQL("deleteProp-with-HS-Flag");
		}
		try {
			stmt = conn.prepareStatement(sql);
			if (blnAddOrRemove) {
				int i = 1;
				stmt.setString(i++, "TU");
				stmt.setString(i++, strTuId);
				stmt.setString(i++, Constants.X_FLAG);
				stmt.setString(i++, null);
				stmt.setString(i++, null);
				stmt.setString(i++, Constants.HS_FLAG);
				stmt.executeUpdate();
			} else {
				stmt.setString(1, strTuId);
				stmt.setString(2, Constants.X_FLAG);
				stmt.setString(3, Constants.HS_FLAG);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			LOGGER.error("", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("", e);
				}
			}
		}
	}

	/**
	 * 搜索术语库
	 * @param strSearch
	 *            搜索文本
	 * @param isCaseSensitive
	 *            是否区分大小写
	 * @param isApplyRegular
	 *            是否使用正则表达式
	 * @param strLang
	 *            源语言
	 * @param lstLangs
	 *            语言集合(包括源语言)
	 * @param intMatchQuality
	 *            术语相似度
	 * @param intMax
	 *            最大查找结果数
	 * @return ;
	 */
	public HashMap<String, IdentityHashMap<String, String>> getTermBaseResult(String strSearch,
			boolean isCaseSensitive, boolean isApplyRegular, boolean isIgnoreMark, String strLang,
			List<String> lstLangs, int intMatchQuality) {
		String sql = getTermBaseSearchSql(strSearch, isCaseSensitive, isApplyRegular, isIgnoreMark, strLang, lstLangs);
		// System.out.println(sql);
		if (sql == null) {
			return null;
		}
		HashMap<String, IdentityHashMap<String, String>> mapTermBase = new HashMap<String, IdentityHashMap<String, String>>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			ArrayList<String> lstRemoveGroupId = new ArrayList<String>();
			boolean blnIsNext = rs.next();
			while (blnIsNext) {
				String strGroupId = rs.getString("GROUPID");
				String strLanguageString = rs.getString("LANG");
				String strTextString = rs.getString("TMTEXT");
				if (lstRemoveGroupId.contains(strGroupId)) {
					if (mapTermBase.containsKey(strGroupId)) {
						mapTermBase.remove(strGroupId);
					}
					blnIsNext = rs.next();
					continue;
				}
				int distance = -1;
				if (!isApplyRegular) {
					if (strLanguageString.equalsIgnoreCase(strLang)) {
						if (isCaseSensitive) {
							distance = MatchQuality.similarity(strTextString, strSearch);
						} else {
							distance = MatchQuality.similarity(strTextString.toLowerCase(), strSearch.toLowerCase());
						}
						if (distance < intMatchQuality) {
							if (mapTermBase.containsKey(strGroupId)) {
								mapTermBase.remove(strGroupId);
							}
							lstRemoveGroupId.add(strGroupId);
							blnIsNext = rs.next();
							continue;
						}
					}
				}
				if (mapTermBase.containsKey(strGroupId)) {
					mapTermBase.get(strGroupId).put(strLanguageString, strTextString);
				} else {
					IdentityHashMap<String, String> mapTemp = new IdentityHashMap<String, String>();
					mapTemp.put(strLanguageString, strTextString);
					mapTermBase.put(strGroupId, mapTemp);
				}
				if (distance > -1) {
					// 将相似度存起来用于排序
					mapTermBase.get(strGroupId).put("similarity", String.valueOf(distance));
				}
				blnIsNext = rs.next();
			}
		} catch (SQLException e) {
			LOGGER.error("", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("", e);
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOGGER.error("", e);
				}
			}
		}
		Iterator<Entry<String, IdentityHashMap<String, String>>> it = mapTermBase.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, IdentityHashMap<String, String>> e = it.next();
			// 2 表示集合中只有源语言和 similarity 两个值
			if (e.getValue().containsKey("similarity") && e.getValue().size() <= 2) {
				it.remove();
			} else if (!e.getValue().containsKey("similarity") && e.getValue().size() == 1) {
				it.remove();
			}
		}
		return mapTermBase;
	}

	/**
	 * 搜索术语库时根据提供的条件生成 SQL 语句
	 * @param strSearch
	 *            搜索文本
	 * @param isCaseSensitive
	 *            是否区分大小写
	 * @param isApplyRegular
	 *            是否使用正则表达式
	 * @param strLang
	 *            源语言
	 * @param lstLangs
	 *            语言集合(包括源语言)
	 * @param intMatchQuality
	 *            术语相似度
	 * @param intMax
	 *            最大查找结果数
	 * @return ;
	 */
	public String getTermBaseSearchSql(String strSearch, boolean isCaseSensitive, boolean isApplyRegular,
			boolean isIgnoreMark, String strLang, List<String> lstLangs) {
		String sql = replaceTMOrTBConditionSql(dbConfig.getOperateDbSQL("search-TermBase"), strSearch, isCaseSensitive,
				isApplyRegular, isIgnoreMark, strLang, null);
		sql = Utils.replaceString(sql, "__IGNORE_MARK__", (isIgnoreMark ? "B.PURE" : "B.CONTENT") + " TMTEXT");
		StringBuffer strLanguage = new StringBuffer("(");
		for (String lang : lstLangs) {
			strLanguage.append("B.LANG='" + lang + "' OR ");
		}
		if (strLanguage.toString().length() > 1) {
			strLanguage.delete(strLanguage.length() - 3, strLanguage.toString().length());
		}
		strLanguage.append(")");
		if (strLanguage.length() == 2) {
			strLanguage.delete(0, strLanguage.length());
		} else {
			strLanguage.insert(0, " AND ");
		}
		sql = Utils.replaceString(sql, "__LANGUAGE_LIST__", strLanguage.toString());
		return sql;
	}

	/**
	 * 导出时，根据设置的过滤条件获取符合条件的TU的主键
	 * @param mTuFilter
	 *            MTU表的过滤条件
	 * @param mNoteFilter
	 *            MNOTE表的过滤条件
	 * @param textDataFilter
	 *            TEXTDATA表的过滤条件
	 * @return 一组符合结果的TU主键
	 * @throws SQLException
	 *             ;
	 */
	public List<Integer> getAfterFilterTuPk(String mTufilter, String mNoteFilter, String textDataFilter)
			throws SQLException {
		String filterTuSQL = dbConfig.getOperateDbSQL("getTuPkByFilter");
		StringBuffer whereBuff = new StringBuffer();
		if (mTufilter != null && !mTufilter.equals("")) {
			whereBuff.append(" AND ");
			whereBuff.append(mTufilter);
		}
		if (textDataFilter != null && !textDataFilter.equals("")) {
			whereBuff.append(" AND ");
			whereBuff.append(textDataFilter);
		}
		if (mNoteFilter != null && !mNoteFilter.equals("")) {
			whereBuff.append(" AND ");
			whereBuff.append(mNoteFilter);
		}
		filterTuSQL = Utils.replaceString(filterTuSQL, "__where__", whereBuff.toString());

		List<Integer> result = new ArrayList<Integer>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(filterTuSQL);
			rs = stmt.executeQuery();
			while (rs.next()) {
				result.add(rs.getInt("MTUPKID"));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return result;
	}

	/**
	 * 过滤查询：通过界面上的语言条件获取数据库所有 tu 的主键 --robert 2013-08-20
	 * @param srcLang
	 * @param tgtLang
	 * @return ;
	 */
	public List<Integer> getAllTuPkByLang(String srcLang, String tgtLang, String srcSearchText, String tgtSearchText)
			throws Exception {
		List<Integer> result = new ArrayList<Integer>();
		String filterTuSQL = dbConfig.getOperateDbSQL("getAllTuPkByLang");
		StringBuilder sb = new StringBuilder();
		sb.append(" AND LOWER(A.LANG) = LOWER('" + srcLang + "')"); // SRC LANG
		sb.append(" AND LOWER(B.LANG) = LOWER('" + tgtLang + "')"); // TGT LANG
		if (srcSearchText.length() != 0) {
			sb.append(" AND A.PURE LIKE '%").append(srcSearchText).append("%'");
		}
		if (tgtSearchText.length() != 0) {
			sb.append(" AND LOWER(B.LANG) = LOWER('" + tgtLang + "') AND B.PURE LIKE '%").append(tgtSearchText)
					.append("%'");
		}

		filterTuSQL = Utils.replaceString(filterTuSQL, "###WHERE###", sb.toString());

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(filterTuSQL);
			rs = stmt.executeQuery();
			while (rs.next()) {
				result.add(rs.getInt("ID"));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return result;
	}

	/**
	 * 过滤查询：获取源文与译文相同的文本段的主键 robert 2013-08-21
	 * @param srcLang
	 * @param tgtLang
	 * @param ignoreCase
	 * @param ignoreTag
	 * @return ;
	 */
	public List<Integer> getSrcSameWithTgtTuPK(String srcLang, String tgtLang, boolean ignoreCase, boolean ignoreTag)
			throws SQLException {
		srcLang = srcLang.toLowerCase();
		tgtLang = tgtLang.toLowerCase();

		List<Integer> result = new ArrayList<Integer>();
		
		String sql = dbConfig.getOperateDbSQL("getSrcSameWithTgtTuPK");
		
		if (ignoreTag) {
			sql = Utils.replaceString(sql, "###IGNORETAG###", "PURE");
		} else {
			sql = Utils.replaceString(sql, "###IGNORETAG###", "CONTENT");
		}
		
		Map<Integer, Map<String, String>> rs = query(sql, new Object[] { srcLang, tgtLang });
		Collection<Map<String, String>> values = rs.values();
		Iterator<Map<String, String>> iterator = values.iterator();
		String id = null;
		String src = null;
		String tgt = null;
		while (iterator.hasNext()) {
			Map<String, String> next = iterator.next();
			id = next.get("ID");
			src = next.get("SRC");
			tgt = next.get("TGT");
			src = TextUtil.trimString(src);
			tgt = TextUtil.trimString(tgt);
			
			if (ignoreCase ? src.equalsIgnoreCase(tgt) : src.equals(tgt)) {
				result.add(Integer.parseInt(id));
			}
		}

		return result;
	}


	/**
	 * 过滤查询：获取相同源文不同译文的数据 robert 2013-08-22
	 * @param srcLang
	 * @param tgtLang
	 * @param ignorCase
	 * @param ignoreTag
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public List<Integer> getSrcSameButTgtTuPK(String srcLang, String tgtLang, boolean ignorCase, boolean ignoreTag)
			throws SQLException {
		srcLang = srcLang.toLowerCase();
		tgtLang = tgtLang.toLowerCase();

		List<Integer> result = new ArrayList<Integer>();
		String sql = dbConfig.getOperateDbSQL("getSrcSameButTgtTuPK");
		sql = Utils.replaceString(sql, "###IGNORETAG###", ignoreTag ? "PURE" : "CONTENT");

		String[] params = new String[] { srcLang, tgtLang };
		String src = null;

		PreparedStatement psmt = null;
		ResultSet rt = null;
		try {
			if (null == conn) {
				return result;
			}
			psmt = conn.prepareStatement(sql);

			for (int i = 0; i < params.length; i++) {
				setParameter(psmt, i + 1, params[i]);
			}

			List<List<Map<String, String>>> allDataList = new ArrayList<List<Map<String, String>>>();
			List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
			dataList = new ArrayList<Map<String, String>>();

			rt = psmt.executeQuery();
			String id = null;
			String curSrc = null;
			String curTgt = null;
			while (rt.next()) {
				id = rt.getString(1);
				curSrc = rt.getString(2);
				curTgt = rt.getString(3);

				if (src == null) {
					src = curSrc;
					Map<String, String> dataMap = new HashMap<String, String>();
					dataMap.put("id", id);
					dataMap.put("src", curSrc);
					dataMap.put("tgt", curTgt);
					dataList.add(dataMap);
				} else {
					if (ignorCase ? src.equalsIgnoreCase(curSrc) : src.equals(curSrc)) {
						Map<String, String> dataMap = new HashMap<String, String>();
						dataMap.put("id", id);
						dataMap.put("src", curSrc);
						dataMap.put("tgt", curTgt);
						dataList.add(dataMap);
					} else {
						if (dataList.size() > 1) {
							allDataList.add(dataList);
						}
						dataList = new ArrayList<Map<String, String>>();
						src = curSrc;
						Map<String, String> dataMap = new HashMap<String, String>();
						dataMap.put("id", id);
						dataMap.put("src", curSrc);
						dataMap.put("tgt", curTgt);
						dataList.add(dataMap);
					}
				}
			}
			if (dataList.size() > 1) {
				allDataList.add(dataList);
			}

			// 开始判断译文是否不相同
			List<Integer> indexList = new ArrayList<Integer>();
			Map<Integer, List<Integer>> indexMap = new TreeMap<Integer, List<Integer>>();
			allListFor: for (List<Map<String, String>> curDataList : allDataList) {
				String tgtText = "";
				for (Map<String, String> curDataMap : curDataList) {
					if (tgtText.isEmpty()) {
						tgtText = curDataMap.get("tgt");
					} else if (!(ignorCase ? tgtText.equalsIgnoreCase(curDataMap.get("tgt")) : tgtText
							.equals(curDataMap.get("tgt")))) {
						indexList = new ArrayList<Integer>();
						for (Map<String, String> indexDataMap : curDataList) {
							indexList.add(Integer.parseInt(indexDataMap.get("id")));
						}
						Collections.sort(indexList);
						indexMap.put(indexList.get(0), indexList);
						continue allListFor;
					}
				}
			}

			for (Entry<Integer, List<Integer>> entry : indexMap.entrySet()) {
				result.addAll(entry.getValue());
			}

		} finally {
			if (rt != null) {
				rt.close();
			}
			if (psmt != null) {
				psmt.close();
			}
		}

		return result;
	}

	/**
	 * 过滤查询：获取相同译文不同源文的数据 robert 2013-08-22
	 * @param srcLang
	 * @param tgtLang
	 * @param ignoreCase
	 * @param ignoreTag
	 * @return ;
	 */
	public List<Integer> getTgtSameButSrcTuPK(String srcLang, String tgtLang, boolean ignoreCase, boolean ignoreTag)
			throws SQLException {
		srcLang = srcLang.toLowerCase();
		tgtLang = tgtLang.toLowerCase();

		List<Integer> result = new ArrayList<Integer>();
		String sql = dbConfig.getOperateDbSQL("getTgtSameButSrcTuPK");
		sql = Utils.replaceString(sql, "###IGNORETAG###", ignoreTag ? "PURE" : "CONTENT");

		String[] params = new String[] { srcLang, tgtLang };

		PreparedStatement psmt = null;
		ResultSet rt = null;
		try {
			if (null == conn) {
				return result;
			}
			psmt = conn.prepareStatement(sql);

			for (int i = 0; i < params.length; i++) {
				setParameter(psmt, i + 1, params[i]);
			}

			List<List<Map<String, String>>> allDataList = new ArrayList<List<Map<String, String>>>();
			List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
			dataList = new ArrayList<Map<String, String>>();

			rt = psmt.executeQuery();
			String tgt = null;
			String id = null;
			String curSrc = null;
			String curTgt = null;
			while (rt.next()) {
				id = rt.getString(1);
				curSrc = rt.getString(2);
				curTgt = rt.getString(3);

				if (tgt == null) {
					tgt = curTgt;
					Map<String, String> dataMap = new HashMap<String, String>();
					dataMap.put("id", id);
					dataMap.put("src", curSrc);
					dataMap.put("tgt", curTgt);
					dataList.add(dataMap);
				} else {
					if (ignoreCase ? tgt.equalsIgnoreCase(curTgt) : tgt.equals(curTgt)) {
						Map<String, String> dataMap = new HashMap<String, String>();
						dataMap.put("id", id);
						dataMap.put("src", curSrc);
						dataMap.put("tgt", curTgt);
						dataList.add(dataMap);
					} else {
						if (dataList.size() > 1) {
							allDataList.add(dataList);
						}
						dataList = new ArrayList<Map<String, String>>();
						tgt = curTgt;
						Map<String, String> dataMap = new HashMap<String, String>();
						dataMap.put("id", id);
						dataMap.put("src", curSrc);
						dataMap.put("tgt", curTgt);
						dataList.add(dataMap);
					}
				}
			}
			if (dataList.size() > 1) {
				allDataList.add(dataList);
			}

			// 开始判断译文是否不相同
			List<Integer> indexList = new ArrayList<Integer>();
			Map<Integer, List<Integer>> indexMap = new TreeMap<Integer, List<Integer>>();
			allListFor: for (List<Map<String, String>> curDataList : allDataList) {
				String srcText = "";
				for (Map<String, String> curDataMap : curDataList) {
					if (srcText.isEmpty()) {
						srcText = curDataMap.get("src");
					} else if (!(ignoreCase ? srcText.equalsIgnoreCase(curDataMap.get("src")) : srcText
							.equals(curDataMap.get("src")))) {
						indexList = new ArrayList<Integer>();
						for (Map<String, String> indexDataMap : curDataList) {
							indexList.add(Integer.parseInt(indexDataMap.get("id")));
						}
						Collections.sort(indexList);
						indexMap.put(indexList.get(0), indexList);
						continue allListFor;
					}
				}
			}

			for (Entry<Integer, List<Integer>> entry : indexMap.entrySet()) {
				result.addAll(entry.getValue());
			}

		} finally {
			if (rt != null) {
				rt.close();
			}
			if (psmt != null) {
				psmt.close();
			}
		}
		return result;
	}
	
	
	/**
	 * 获取重复文本段	--robert	2013-12-17
	 * @param srcLang
	 * @param tgtLang
	 * @param ignoreCase
	 * @param ignoreTag
	 * @return
	 * @throws SQLException ;
	 */
	public List<Integer> getDuplicateSegTUPK(String srcLang, String tgtLang, final boolean ignoreCase, final boolean ignoreTag) throws SQLException{
		srcLang = srcLang.toLowerCase();
		tgtLang = tgtLang.toLowerCase();
		long time1 = System.currentTimeMillis();
		
		List<Integer> result = new ArrayList<Integer>();
		String sql = dbConfig.getOperateDbSQL("getDuplicateSegTUPK");
		sql = Utils.replaceString(sql, "###IGNORETAG###", ignoreTag ? "PURE" : "CONTENT");

		String[] params = new String[] { srcLang, tgtLang };

		PreparedStatement psmt = null;
		ResultSet rt = null;
		try {
			if (null == conn) {
				return result;
			}
			psmt = conn.prepareStatement(sql);

			for (int i = 0; i < params.length; i++) {
				setParameter(psmt, i + 1, params[i]);
			}

			List<TmxTU> tuDataList = new ArrayList<TmxTU>();
			rt = psmt.executeQuery();
			int mpk = -1;
			String srcText = null;
			String tgtText = null;
			TmxTU tu = new TmxTU();
			TmxSegement srcSegment;
			TmxSegement tgtSegment;
			while (rt.next()) {
				tu = new TmxTU();
				srcSegment = new TmxSegement();
				tgtSegment = new TmxSegement();
				mpk = Integer.parseInt(rt.getString(1));
				srcText = rt.getString(2);
				tgtText = rt.getString(3);
				srcText = TextUtil.trimString(srcText);
				tgtText = TextUtil.trimString(tgtText);
				srcText = ignoreCase ? srcText.toLowerCase() : srcText;
				tgtText = ignoreCase ? tgtText.toLowerCase() : tgtText;
				if (ignoreTag) {
					srcSegment.setPureText(srcText);
					tgtSegment.setPureText(tgtText);
					tu.setSource(srcSegment);
					tu.setTarget(tgtSegment);
				}else {
					srcSegment.setFullText(srcText);
					tgtSegment.setFullText(tgtText);
					tu.setSource(srcSegment);
					tu.setTarget(tgtSegment);
				}
				tu.setTmId(mpk);
				tuDataList.add(tu);
			}
			
			Collections.sort(tuDataList, new Comparator<TmxTU>() {
				public int compare(TmxTU tu1, TmxTU tu2) {
					String srcText1 = ignoreTag ? tu1.getSource().getPureText() : tu1.getSource().getFullText();
					String srcText2 = ignoreTag ? tu2.getSource().getPureText() : tu2.getSource().getFullText();
					return srcText1.compareTo(srcText2);
				}
			});
			
			TmxTU tuBean = null;
			TmxTU curBean = null;
			List<TmxTU> equalsList = null;
			Map<Integer, List<Integer>> indexMap = new TreeMap<Integer, List<Integer>>();
			bigFor:for (int i = 0; i < tuDataList.size(); i++) {
				tuBean = tuDataList.get(i);
				equalsList = new ArrayList<TmxTU>();
				equalsList.add(tuBean);
				srcText = ignoreTag ? tuBean.getSource().getPureText() : tuBean.getSource().getFullText();
				
				for (int j = i + 1; j < tuDataList.size(); j++) {
					curBean = tuDataList.get(j);
					String curSrcText = ignoreTag ? curBean.getSource().getPureText() : curBean.getSource().getFullText();
					if (srcText.equals(curSrcText)) {
						equalsList.add(curBean);
						i ++;
					}else {
						ananysisDuplicateData(indexMap, equalsList, ignoreTag);
						continue bigFor;
					}
					if (j == tuDataList.size() - 1) {
						// 处理　equalslist 中是否有残余的数据。
						ananysisDuplicateData(indexMap, equalsList, ignoreTag);
					}
				}
			}

			for (Entry<Integer, List<Integer>> entry : indexMap.entrySet()) {
				result.addAll(entry.getValue());
			}
			
			tuDataList.clear();
			System.out.println("过滤耗时：" + (System.currentTimeMillis() - time1));

		} finally {
			if (rt != null) {
				rt.close();
			}
			if (psmt != null) {
				psmt.close();
			}
		}
		return result;
	}
	
	/**
	 * 处理相关数据，主要是用于分析　相同译文不同源文的数据，如符合标准，放到缓存中。
	 * @param equalsList
	 * @param ignoreCase
	 * @param ignoreTag ;
	 */
	private void ananysisDuplicateData(Map<Integer, List<Integer>> indexMap, List<TmxTU> equalsList, boolean ignoreTag){
		if (equalsList.size() >= 2) {
			List<Integer> resultIdxIdList = null;
			// 因为　arraylist 的删除不给力，故，将要删除的　index 放到　hashset　里面，为了提高效率。
			HashSet<Integer> removedIdSet = new HashSet<Integer>(); 
			int startIdx = 0;
			TmxTU tuBean = null;
			TmxTU curTuBean = null;
			whileName:while(startIdx < equalsList.size() - 1){
				if (removedIdSet.contains(startIdx)) {
					startIdx ++;
					continue whileName;
				}
				
				resultIdxIdList = new ArrayList<Integer>();
				
				tuBean = equalsList.get(startIdx);
				String tgtText = ignoreTag ? tuBean.getTarget().getPureText() : tuBean.getTarget().getFullText();
				resultIdxIdList.add(tuBean.getTmId());
				removedIdSet.add(startIdx);
				forName:for (int k = startIdx + 1; k < equalsList.size(); k++) {
					if (removedIdSet.contains(k)) {
						continue forName;
					}
					
					curTuBean = equalsList.get(k);
					String curTgtText = ignoreTag ? curTuBean.getTarget().getPureText() : curTuBean.getTarget().getFullText();
					if (tgtText.equals(curTgtText)) {
						resultIdxIdList.add(curTuBean.getTmId());
						removedIdSet.add(k);
					}
				}
				
				if (resultIdxIdList.size() >= 2) {
					Collections.sort(resultIdxIdList, new Comparator<Integer>() {
						public int compare(Integer tmId1, Integer tmId2) {
							return tmId1.compareTo(tmId2);
						}
					});
					indexMap.put(resultIdxIdList.get(0), resultIdxIdList);
				}
				startIdx ++;
			}
		}
	}
	

	/**
	 * 过滤查询：查询带批注的文本段的内容 robert 2013-08-22
	 * @param srcLang
	 * @param tgtLang
	 * @param ignoreCase
	 * @param ignoreTag
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public List<String> getWithNoteTuPK(String srcLang, String tgtLang, boolean ignoreCase, boolean ignoreTag)
			throws SQLException {
		srcLang = srcLang.toLowerCase();
		tgtLang = tgtLang.toLowerCase();

		List<String> result = new ArrayList<String>();
		String sql = dbConfig.getOperateDbSQL("getWithNoteTuPK");
		Map<Integer, Map<String, String>> rs = query(sql, new Object[] { srcLang, tgtLang });
		Collection<Map<String, String>> values = rs.values();
		Iterator<Map<String, String>> iterator = values.iterator();
		while (iterator.hasNext()) {
			Map<String, String> next = iterator.next();
			String string = next.get("ID");
			result.add(string);
		}
		return result;
	}

	/**
	 * 过滤查询：查询有乱码的的文本段的　主键 robert 2013-08-22
	 * @param srcLang
	 * @param tgtLang
	 * @param ignoreCase
	 * @param ignoreTag
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public List<String> getWithGarbleTuPK(String srcLang, String tgtLang, boolean ignoreCase, boolean ignoreTag)
			throws SQLException {
		List<String> result = new ArrayList<String>();
		String sql = dbConfig.getOperateDbSQL("getWithGarbleTuPK");
		Map<Integer, Map<String, String>> rs = query(sql, new Object[] { srcLang, tgtLang });
		Collection<Map<String, String>> values = rs.values();
		Iterator<Map<String, String>> iterator = values.iterator();
		String src = null;
		String tgt = null;
		CharsetEncoder encoder = java.nio.charset.Charset.forName("UTF-8").newEncoder();
		while (iterator.hasNext()) {
			Map<String, String> next = iterator.next();
			String id = next.get("ID");
			src = next.get("SRC");
			if (src != null) {
				if (!encoder.canEncode(src)) {
					result.add(id);
					continue;
				}
			}
			tgt = next.get("TGT");
			if (tgt != null) {
				if (!encoder.canEncode(tgt)) {
					result.add(id);
				}
			}
		}

		return result;
	}

	/**
	 * 获取译文为空的所有文本段主键 --robert 2013-10-12
	 * @param dbOp
	 * @param filterBean
	 * @param srcLang
	 * @param tgtLang
	 * @param ignoreCase
	 * @param ignoreTag
	 * @return
	 * @throws Exception
	 *             ;
	 */
	public List<String> getTgtNullTuPK(String srcLang, String tgtLang, boolean ignoreCase, boolean ignoreTag)
			throws Exception {
		srcLang = srcLang.toLowerCase();
		tgtLang = tgtLang.toLowerCase();

		List<String> result = new ArrayList<String>();
		String sql = dbConfig.getOperateDbSQL("getTgtNullTuPK");
		sql = Utils.replaceString(sql, "###IGNORETAG###", ignoreTag ? "PURE" : "CONTENT");

		Map<Integer, Map<String, String>> rs = query(sql, new Object[] { srcLang, tgtLang });
		Collection<Map<String, String>> values = rs.values();
		Iterator<Map<String, String>> iterator = values.iterator();
		while (iterator.hasNext()) {
			Map<String, String> next = iterator.next();
			String tgtText = next.get("TGT");
			if (TextUtil.checkStringEmpty(tgtText)) {
				String id = next.get("ID");
				result.add(id);
			}
		}
		return result;
	}

	/**
	 * UNDO 所有数据库相关的都未处理 过滤查询：查询自定义过滤器的文件段的　主键 robert 2013-08-22
	 * @param srcLang
	 * @param tgtLang
	 * @param ignoreCase
	 * @param ignoreTag
	 * @return ;
	 */
	public List<String> getCustomFilterTuPK(String sql, String srcLang, String tgtLang, boolean ignoreCase,
			boolean ignoreTag) throws SQLException {
		List<String> result = new ArrayList<String>();

		sql = Utils.replaceString(sql, "###IGNORETAG###", ignoreTag ? "PURE" : "CONTENT");
		Map<Integer, Map<String, String>> rs = query(sql, new Object[] { srcLang, tgtLang });
		Collection<Map<String, String>> values = rs.values();
		Iterator<Map<String, String>> iterator = values.iterator();

		while (iterator.hasNext()) {
			Map<String, String> next = iterator.next();
			String id = next.get("ID");
			result.add(id);
		}

		return result;
	}

	/**
	 * @deprecated 目前些方法暂时未使用,所有导出的TMX文件头都是重新生成的 导出时，根据设置的过滤条件获取本次导出的TMX Header主键
	 * @param mTuFilter
	 *            MTU表的过滤条件
	 * @param mNoteFilter
	 *            MNOTE表的过滤条件
	 * @param textDataFilter
	 *            TEXTDATA表的过滤条件
	 * @return 返回当前导出的TMX内容的Header在库中的主键,如果导的TU具有不同的Header则返回-1
	 * @throws SQLException
	 */
	public int getAfterFilterTuHeaderPk(String mTufilter, String mNoteFilter, String textDataFilter)
			throws SQLException {
		String filterTuSQL = dbConfig.getOperateDbSQL("getTMXHeaderId");
		StringBuffer whereBuff = new StringBuffer();
		if (mTufilter != null && !mTufilter.equals("")) {
			whereBuff.append(" AND ");
			whereBuff.append(mTufilter);
		}
		if (textDataFilter != null && !textDataFilter.equals("")) {
			whereBuff.append(" AND ");
			whereBuff.append(textDataFilter);
		}
		if (mNoteFilter != null && !mNoteFilter.equals("")) {
			whereBuff.append(" AND ");
			whereBuff.append(mNoteFilter);
		}
		filterTuSQL = Utils.replaceString(filterTuSQL, "__where__", whereBuff.toString());

		int result = -1;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(filterTuSQL);
			rs = stmt.executeQuery();
			while (rs.next()) {
				int temp = rs.getInt(1);
				if (result == -1) {
					result = temp;
				} else if (result != temp) {
					return -1;
				}
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return result;
	}

	/**
	 * 根据设置的过滤条件生成相应的SQL语句
	 * @param tableName
	 *            过滤条件的表名
	 * @param filterBean
	 *            设置过滤条件
	 * @return 生成SQL条件部分,即where后面的过滤条件,没有过滤条件则返回一个空串;
	 */
	public String generationExportTMXFilter(String tableName, ExportFilterBean filterBean) {
		String connector = filterBean.getFilterConnector();
		List<ExportFilterComponentBean> filterOption = filterBean.getFilterOption();
		Map<String, Character> tuMatch = Utils.getFilterMatchMTU("MTU");
		Map<String, Character> mNoteDateMatch = Utils.getFilterMatchMTU("MNOTE");
		Map<String, Character> textDateMatch = Utils.getFilterMatchMTU("TEXTDATA");

		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < filterOption.size(); i++) {
			ExportFilterComponentBean bean = filterOption.get(i);

			Character fieldType = null;
			String field = bean.getMatchDbField();
			String op = bean.getExpressionMatchDb();
			String value = bean.getFilterVlaue();
			if ("MTU".equals(tableName)) { // 添加 "A.","B.","C."请参考查询SQL
				fieldType = tuMatch.get(field);
				field = "A." + field;
			} else if ("MNOTE".equals(tableName)) {
				fieldType = mNoteDateMatch.get(field);
				field = "B." + field;
			} else if ("TEXTDATA".equals(tableName)) {
				fieldType = textDateMatch.get(field);
				field = "C." + field;
			}
			if (fieldType == null) {
				continue;
			}
			bf.append(field);
			bf.append(" " + op + " ");

			switch (fieldType) {
			case '1': // 文本内容,包含/不包含内容
				if (op.equals("like") || op.equals("not like")) {
					bf.append(" '%" + value + "%' ");
				} else {
					bf.append(" '" + value + "' ");
				}
				bf.append(connector + " ");
				break;
			case '2': // 日期类型 目前统一采用where dataField > 'yyyy-MM-dd hh:mm:ss' 此方式不适用于Oracle,参见Oracle实现
				bf.append(" '" + value + "' ");
				bf.append(connector + " ");
				break;
			default:
				return "";
			}
		}
		String result = bf.toString();
		if (result.equals("")) {
			return result;
		}
		return result.substring(0, result.lastIndexOf(connector));
	}

	/**
	 * 通过TU在库中的主键获取TU的内容
	 * @param tuPk
	 *            在库中的主键
	 * @param needLangs
	 *            对应的语言
	 * @param isLevel1
	 *            是否获取一级TMX
	 * @param isTagOnly
	 *            是否只获取带标记的TU
	 * @return 从库中查询的TU节点的完整内容
	 * @throws SQLException
	 *             ;
	 */
	public String retrieveTu(int tuPk, List<String> needLangs, boolean isLevel1, boolean isTagOnly) throws SQLException {
		// 选处理TUV,判断TUV是否满足要求
		String tuvContent = retrieveTuv(tuPk, needLangs, isLevel1, isTagOnly);
		if (tuvContent.equals("")) {
			return "";
		}

		Map<String, String> match = Utils.getTUDbMatchTmx();
		Map<String, String> tuAttrsMap = getTUByTuPkId(tuPk);
		String client = tuAttrsMap.get("CLIENT");
		String project = tuAttrsMap.get("PROJECTREF");
		String job = tuAttrsMap.get("JOBREF");

		tuAttrsMap = changeToTMX(tuAttrsMap, match);
		Iterator<Entry<String, String>> it = tuAttrsMap.entrySet().iterator();
		StringBuffer bf = new StringBuffer();
		bf.append("<tu");
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			String attrName = entry.getKey();
			String attrValue = entry.getValue();
			if (attrValue != null && !attrValue.equals("")) {
				if (attrName.equals("creationdate") || attrName.equals("changedate")) {
					Date t = DateUtils.strToDateLong(attrValue);
					if (t != null) {
						attrValue = DateUtils.formatToUTC(t.getTime());
						bf.append(" " + attrName + "=\"" + attrValue + "\"");
					}
				} else {
					bf.append(" " + attrName + "=\"" + attrValue + "\"");
				}
			}
		}
		bf.append(retrieveExtra(tuPk, "TU", "A"));
		bf.append(">\n");

		bf.append(retrieveNote(tuPk, "TU"));

		bf.append(retrieveProp(tuPk, "TU"));

		if (project != null && !project.equals("")) {
			bf.append("<prop type=\"client\">" + project + "</prop>\n");
		}
		if (client != null && !client.equals("")) {
			bf.append("<prop type=\"client\">" + client + "</prop>\n");
		}
		if (job != null && !job.equals("")) {
			bf.append("<prop type=\"client\">" + job + "</prop>\n");
		}

		bf.append(retrieveExtra(tuPk, "TU", "E"));

		bf.append(tuvContent);

		bf.append("</tu>\n");
		return bf.toString();
	}

	/**
	 * 获取扩展表中的数据,包括节点的属性和节点
	 * @param parentId
	 *            父节点的主键
	 * @param parentName
	 *            父节点的名称
	 * @param type
	 *            类型,"E"节点,"A"节点属性
	 * @return 生成的属性或者节点内容;无内容时返回空串
	 * @throws SQLException
	 *             ;
	 */
	private String retrieveExtra(int parentId, String parentName, String type) throws SQLException {
		StringBuffer bf = new StringBuffer();
		Map<Integer, Map<String, String>> tuExtraAttrsMap = getExtraValue(parentId, parentName, type);
		if (type.equals("A")) { // 节点属性
			for (Integer dbRow : tuExtraAttrsMap.keySet()) {
				Map<String, String> dbRowValue = tuExtraAttrsMap.get(dbRow);
				String attrName = dbRowValue.get("NNAME");
				String attrValue = dbRowValue.get("CONTENT");
				if (attrValue != null && !attrValue.equals("")) {
					bf.append(" " + attrName + "=\"" + attrValue + "\"");
				}
			}
		} else if (type.equals("E")) { // 节点
			Map<Integer, Map<String, String>> tuvExtraElement = getExtraValue(parentId, parentName, type);
			for (Integer eRow : tuvExtraElement.keySet()) {
				Map<String, String> tuSubElement = tuvExtraElement.get(eRow);
				String content = tuSubElement.get("CONTENT");
				if (content != null && !content.equals("")) {
					bf.append(content + "\n");
				}
			}
		}
		return bf.toString();
	}

	/**
	 * 通过父节点的主键获取该节点的NOTE子节点内容
	 * @param parentId
	 *            父节点的主键
	 * @param parentName
	 *            父节点的名称
	 * @return 完整的NOTE节点内容;无NOTE节点返回空串
	 * @throws SQLException
	 *             ;
	 */
	private String retrieveNote(int parentId, String parentName) throws SQLException {
		StringBuffer bf = new StringBuffer();
		Map<Integer, Map<String, String>> tuNotesMap = getMNoteValue(parentId, parentName);
		for (Integer noteRow : tuNotesMap.keySet()) {
			Map<String, String> note = tuNotesMap.get(noteRow);
			bf.append(note.get("CONTENT") + "\n");
		}
		return bf.toString();
	}

	/**
	 * 通过父节点的主键获取该节点的prop子节点的完整内容
	 * @param parentId
	 *            父节点的主键
	 * @param parentName
	 *            父节点的名称
	 * @return 完整的PROP节点内容,无PROP节点返回空串
	 * @throws SQLException
	 *             ;
	 */
	private String retrieveProp(int parentId, String parentName) throws SQLException {
		StringBuffer bf = new StringBuffer();
		Map<Integer, Map<String, String>> tuPropMap = getMPropValue(parentId, parentName);
		for (Integer propRow : tuPropMap.keySet()) {
			Map<String, String> propMap = tuPropMap.get(propRow);
			bf.append("<prop");
			bf.append(" type=\"" + propMap.get("PNAME") + "\"");
			String encoding = propMap.get("ENCODING");
			if (encoding != null && !encoding.equals("")) { //$NON-NLS-1$
				bf.append(" o-encoding=\"" + encoding + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			String lang = propMap.get("LANG");
			if (lang != null && !lang.equals("")) { //$NON-NLS-1$
				bf.append(" xml:lang=\"" + lang + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			bf.append(">");
			bf.append(propMap.get("CONTENT"));
			bf.append("</prop>\n");
		}
		return bf.toString();
	}

	/**
	 * 通过父节点的主键获取该节点的TUV子节点的完整内容
	 * @param tuPk
	 *            父节点的主键
	 * @param needLangs
	 *            需要查询的语言,必须成对出现
	 * @param isLevel1
	 *            是否为一级TMX
	 * @param isTagOnly
	 *            是否只导出带标记的TMX
	 * @return 返回完整的TUV节点内容,无内容时返回空串
	 * @throws SQLException
	 *             ;
	 */
	private String retrieveTuv(int tuPk, List<String> needLangs, boolean isLevel1, boolean isTagOnly)
			throws SQLException {
		ArrayList<Map<String, String>> tuvList = new ArrayList<Map<String, String>>();
		for (Iterator<String> iterator = needLangs.iterator(); iterator.hasNext();) {
			String lang = iterator.next();
			Map<String, String> tuvMap = getTextDataValue("M", tuPk, lang);
			if (tuvMap == null) { // TU 不满足语言条件
				// Fixed Bug #2296 不支持多语言对导出TMX文件
				// return "";
				continue;
			}
			String tuvPureText = tuvMap.get("PURE");
			String tuvText = tuvMap.get("CONTENT");
			if (isTagOnly) {
				if (tuvPureText.equals(tuvText)) {
					// Fixed Bug #2296 不支持多语言对导出TMX文件
					// return "";
					continue;
				}
			}
			tuvList.add(tuvMap);
		}
		if (tuvList.size() < 2) {
			return "";
		}
		StringBuffer bf = new StringBuffer();
		for (Iterator<Map<String, String>> iterator = tuvList.iterator(); iterator.hasNext();) {
			Map<String, String> tuvMap = iterator.next();
			int tuvPk = Integer.parseInt(tuvMap.get("TPKID"));
			String preContext = tuvMap.get("PRECONTEXT");
			String nextContext = tuvMap.get("NEXTCONTEXT");

			bf.append("<tuv");
			bf.append(" xml:lang=\"" + tuvMap.get("LANG") + "\"");
			bf.append(retrieveExtra(tuvPk, "TUV", "A"));
			bf.append(">\n");

			bf.append(retrieveNote(tuvPk, "TUV"));

			bf.append(retrieveProp(tuvPk, "TUV"));
			if (preContext != null && !preContext.equals("")) {
				bf.append("<prop type=\"x-precontext\">" + preContext + "</prop>\n");
			}
			if (nextContext != null && !nextContext.equals("")) {
				bf.append("<prop type=\"x-nextcontext\">" + nextContext + "</prop>\n");
			}

			bf.append(retrieveExtra(tuvPk, "TUV", "E"));

			String segContent = "";
			if (isLevel1) {
				segContent = tuvMap.get("PURE");
			} else {
				segContent = tuvMap.get("CONTENT");
			}
			bf.append("<seg>" + InnerTagClearUtil.clearXliffTag4Tmx(segContent) + "</seg>\n");
			bf.append("</tuv>\n");
		}
		return bf.toString();
	}

	/**
	 * 将从数据库中获取的属性对转换成TMX文件中对应的属性键值对.
	 * @param dbMap
	 *            数据库中获取的属性键值对
	 * @param tmxMap
	 *            TMX中的属性键值对
	 * @return 数据库字段与值的对应集合
	 */
	private Map<String, String> changeToTMX(Map<String, String> dbMap, Map<String, String> tmxMap) {
		Map<String, String> params = new HashMap<String, String>();
		if (dbMap != null) {
			Iterator<Entry<String, String>> it = dbMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				String key = entry.getKey();
				String value = entry.getValue();
				String tmpKey = tmxMap.get(key);
				if (tmpKey != null) {
					it.remove();
					params.put(tmpKey, value);
				}
			}
		}
		return params;
	}

	/**
	 * 根据过滤条件和需要语言从TextData表中获取termEntry pk
	 * @param where
	 *            过滤条件
	 * @param needLang
	 *            需要的语言
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public List<Integer> getAfterFilterTermEntryPK(String where, List<String> needLang) throws SQLException {
		String filterSQL = dbConfig.getOperateDbSQL("getTermEntryPkByFilter");

		filterSQL = Utils.replaceString(filterSQL, "__where__", where);
		StringBuffer langWhere = new StringBuffer();
		for (int i = 0; i < needLang.size(); i++) {
			langWhere.append("LANG='" + needLang.get(i) + "' OR ");
		}
		String langs = langWhere.toString();
		langs = langWhere.substring(0, langWhere.lastIndexOf("OR"));
		filterSQL = Utils.replaceString(filterSQL, "__langwhere__", langs);

		List<Integer> result = new ArrayList<Integer>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(filterSQL);
			rs = stmt.executeQuery();
			while (rs.next()) {
				result.add(rs.getInt("GROUPID"));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return result;
	}

	/**
	 * 验证当前termEntry是否是符合语言需要，即langSet节点都必须有我们需要的语言
	 * @param pks
	 *            TermEntry pk
	 * @param needLangs
	 *            需要的语言
	 * @return 返回符合要求的TermEntry pk
	 * @throws SQLException
	 *             ;
	 */
	public List<Integer> validateTermEntryPk(List<Integer> pks, List<String> needLangs, String srcLang)
			throws SQLException {
		List<Integer> result = new ArrayList<Integer>();
		String sqlStr = dbConfig.getOperateDbSQL("getTermEntryLangByPk");
		for (Integer pk : pks) {
			List<String> queryRs = new ArrayList<String>();
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.prepareStatement(sqlStr);
				stmt.setInt(1, pk);
				rs = stmt.executeQuery();
				while (rs.next()) {
					queryRs.add(rs.getString("LANG"));
				}
				// fix Bug #2361 TBX文件导出问题--语言不能正确过滤导出 by Jason
				if (!queryRs.contains(srcLang)) {
					continue;
				}
				for (String needLang : needLangs) {
					if (needLang.equals(srcLang)) {
						continue;
					}
					if (queryRs.contains(needLang)) { // 至少包含一种
						result.add(pk);
						break;
					}
				}

			} finally {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			}
		}
		return result;
	}

	/**
	 * 获取TermEntry节点的完整内容
	 * @param termEntryPK
	 *            TERMETNRY在库中的主键
	 * @return 节点的完整内容
	 * @throws SQLException
	 *             ;
	 */
	public String retrieveTermEntry(int termEntryPK) throws SQLException {
		return getTermEntryContentByPk(termEntryPK) + "\n\n";
	}

	/**
	 * 查询TU的TUV节点
	 * @param tupkid
	 * @param lang
	 * @return ;
	 * @throws SQLException
	 */
	public Map<String, String> getTMXSegmentByIdAddLang(Integer tupkid, String lang) throws SQLException {
		Map<Integer, Map<String, String>> queryRs = query(dbConfig.getOperateDbSQL("get-tmxsegment-byid-lang"),
				new Object[] { tupkid, lang });
		if (queryRs.isEmpty()) {
			return null;
		}
		return queryRs.get(0);
	}

	public Map<String, String> getHeaderinfoById(Integer mnpkid) throws SQLException {
		Map<Integer, Map<String, String>> queryRs = query(dbConfig.getOperateDbSQL("get-header-byid"),
				new Object[] { mnpkid });
		if (queryRs.isEmpty()) {
			return null;
		}
		return queryRs.get(0);
	}

	/**
	 * @param tupkid
	 *            ;
	 * @throws SQLException
	 */
	public void deleteTU(int tupkid) throws SQLException {
		String deleteMtuSQL = dbConfig.getOperateDbSQL("delete-mtu-byid");
		PreparedStatement prepareStatement = conn.prepareStatement(deleteMtuSQL);
		prepareStatement.setInt(1, tupkid);
		prepareStatement.executeUpdate();
		if (null != prepareStatement) {
			prepareStatement.close();
		}

	}

	public void deleteTuProp(int parentId, String parentName) throws SQLException {
		String deleteTuPropSQL = dbConfig.getOperateDbSQL("delete-tumprop-byid");
		PreparedStatement prepareStatement = conn.prepareStatement(deleteTuPropSQL);
		prepareStatement.setInt(1, parentId);
		prepareStatement.setString(2, parentName);
		prepareStatement.executeUpdate();
		if (null != prepareStatement) {
			prepareStatement.close();
		}

	}

	/**
	 * @param tupkid
	 *            ;
	 * @throws SQLException
	 */
	public void deleteTuNote(int parentId, String parentName) throws SQLException {
		String deleteTuNoteSQL = dbConfig.getOperateDbSQL("delete-tunote-byid");
		PreparedStatement prepareStatement = conn.prepareStatement(deleteTuNoteSQL);
		prepareStatement.setInt(1, parentId);
		prepareStatement.setString(2, parentName);
		prepareStatement.executeUpdate();
		if (null != prepareStatement) {
			prepareStatement.close();
		}

	}

	/**
	 * @param tupkid
	 *            ;
	 * @throws SQLException
	 */
	public void deleteTuExtra(int parentId, String parentName) throws SQLException {
		String deleteTuExtra = dbConfig.getOperateDbSQL("delete-mextra-byid");
		PreparedStatement prepareStatement = conn.prepareStatement(deleteTuExtra);
		prepareStatement.setInt(1, parentId);
		prepareStatement.setString(2, parentName);
		prepareStatement.executeUpdate();
		if (null != prepareStatement) {
			prepareStatement.close();
		}

	}

	public void deleteLang(String lang) throws SQLException {
		String deleteTuExtra = dbConfig.getOperateDbSQL("delete-lang");
		PreparedStatement prepareStatement = conn.prepareStatement(deleteTuExtra);
		prepareStatement.setString(1, lang);
		prepareStatement.executeUpdate();
		if (null != prepareStatement) {
			prepareStatement.close();
		}
	}

	public int getLangCountInTextData(String lang) throws SQLException {
		String deleteTuExtra = dbConfig.getOperateDbSQL("get-lang-count");
		PreparedStatement prepareStatement = conn.prepareStatement(deleteTuExtra);
		prepareStatement.setString(1, lang);
		ResultSet executeQuery = prepareStatement.executeQuery();
		executeQuery.next();
		int rs = executeQuery.getInt(1);
		executeQuery.close();
		prepareStatement.close();
		return rs;
	}

	public void updateTUChangeDate(int id, String date) throws SQLException {
		String updateChangeDate = dbConfig.getOperateDbSQL("update-mtu-changedate");
		PreparedStatement prepareStatement = conn.prepareStatement(updateChangeDate);
		prepareStatement.setTimestamp(1, DateUtils.getTimestampFromUTC(date));
		prepareStatement.setInt(2, id);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}

	/**
	 * 更新TU用系统默认时间
	 * @param id
	 * @throws SQLException
	 *             ;
	 */
	public void updateTUChangeDateUseDefault(int id) throws SQLException {
		updateTUChangeDate(id, CommonFunction.retTMXDate());
	}

	public void updateTUChangeUser(Integer tuId, String newUser) throws SQLException {
		// sql :UPDATE MTU SET CHANGEID =? WHERE MTUPKID=?
		Object[] params = new Object[] { newUser, tuId };
		update("update-mtu-changeuser", params);

	}

	public void updateTUVContent(int tuvId, String hashCode, String pureText, String content) throws SQLException {
		String updateTUVContent = dbConfig.getOperateDbSQL("update-tuvcontent");
		PreparedStatement prepareStatement = conn.prepareStatement(updateTUVContent);
		prepareStatement.setString(1, hashCode);
		prepareStatement.setString(2, pureText);
		prepareStatement.setString(3, content);
		prepareStatement.setInt(4, tuvId);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}

	public void updateMProp(int propId, String pName, String content) throws SQLException {
		String updateTUVContent = dbConfig.getOperateDbSQL("update-mprop-byid");
		PreparedStatement prepareStatement = conn.prepareStatement(updateTUVContent);
		prepareStatement.setString(1, pName);
		prepareStatement.setString(2, content);
		prepareStatement.setInt(3, propId);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}

	/**
	 * 根据表的主键更新表，表主键的Id需要放在params的最后 params的参数需要与SQL语句里面的?一致
	 * @param sqlConfig
	 * @param params
	 *            ;
	 * @throws SQLException
	 */
	public boolean update(String sqlConfig, Object[] params) throws SQLException {
		String updateStmt = dbConfig.getOperateDbSQL(sqlConfig);
		PreparedStatement prepareStatement = conn.prepareStatement(updateStmt);
		for (int i = 0; i < params.length; i++) {
			setParameter(prepareStatement, i + 1, params[i]);
		}
		int executeUpdate = prepareStatement.executeUpdate();

		prepareStatement.close();
		if (executeUpdate > 0) {
			return true;
		} else {
			return false;
		}
	}

	public void updateMNote(Integer id, String enconding, String lang, String content) throws SQLException {
		// SQL UPDATE MNOTE SET CONTENT=? ,ENCODING=? ,LANG=? WHERE MNPKID =?
		Object[] params = new Object[] { content, enconding, lang, id };
		update("update-note-byid", params);
	}

	/**
	 * 执行insert返回插入的主键
	 * @param sqlConfig
	 * @param params
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public String insert(String sqlConfig, Object[] params) throws SQLException {
		String updateStmt = dbConfig.getOperateDbSQL(sqlConfig);
		PreparedStatement prepareStatement = conn.prepareStatement(updateStmt, Statement.RETURN_GENERATED_KEYS);
		for (int i = 0; i < params.length; i++) {
			setParameter(prepareStatement, i + 1, params[i]);
		}
		int row = prepareStatement.executeUpdate();
		String key = "-1";
		if (row == 1) {
			ResultSet rs = prepareStatement.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getString(1);
			}
			if (rs != null)
				rs.close();
		}
		return key;
	}

	public List<Integer> getEmptyPure(String lang, IProgressMonitor monitor,boolean ignoreTag) throws SQLException {
		// sql:SELECT GROUPID ,CONTENT FROM TEXTDATA WHERE LANG = ? 
		Map<Integer, Map<String, String>> rs = query(dbConfig.getOperateDbSQL("get-empty-content"),
				new Object[] { lang });
		monitor.beginTask("", rs.size());
		Collection<Map<String, String>> values = rs.values();
		Iterator<Map<String, String>> iterator = values.iterator();
		List<Integer> ids = new ArrayList<Integer>();
		String text = null;
		while (iterator.hasNext()) {
			Map<String, String> next = iterator.next();
			String fullText = next.get("CONTENT");
			text = ignoreTag ? TmxInnerTagParser.getInstance().getTmxPureText(fullText):fullText;
			if(null == text){
				continue;
			}
			if(next.get("GROUPID") == null){
				continue;
			}
			if(text.trim().isEmpty()){				
				ids.add(Integer.parseInt(next.get("GROUPID")));
			}
			monitor.worked(1);
		}
		monitor.done();
		return ids;
	}

	public int addExAttribute(String parentName, Integer parentId, String attName, String attriValue)
			throws SQLException {
		// sql : INSERT INTO MEXTRA (PARENTNAME , PARENTID ,NTYPE ,NNAME,CONTENT) VALUES(?,?,'A',?,?)
		Object[] params = new Object[] { parentName, parentId, attName, attriValue };
		String insertId = insert("add-attribute", params);
		return Integer.parseInt(insertId);
	}

	public void delete(String deleteSql, Object[] params) throws SQLException {
		String updateStmt = dbConfig.getOperateDbSQL(deleteSql);
		PreparedStatement prepareStatement = conn.prepareStatement(updateStmt);
		for (int i = 0; i < params.length; i++) {
			setParameter(prepareStatement, i + 1, params[i]);
		}
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}

	public void deleteExAttribute(Integer parentid, String parentName, String name) throws SQLException {
		// sql : DELETE FROM MEXTRA WHERE PARENTNAME = ? AND PARENTID = ? AND NTYPE='A' AND NNAME =?
		Object[] params = new Object[] { parentName, parentid, name };
		delete("delete-attribute-buinfo", params);
	}

	public boolean updateExAttribute(int parentId, String parentName, String name, String newvalue) throws SQLException {
		// sql : UPDATE MEXTRA SET CONTENT = ? WHERE PARENTID = ? AND NNAME = ? AND PARENTNAME=?
		Object[] params = new Object[] { newvalue, parentId, name, parentName };
		return update("update-ex-attribute-value", params);
	}

	public int addMNote(String parentName, Integer parentId, String content, String enconding, String lang)
			throws SQLException {
		// sql : INSERT INTO MNOTE (PARENTNAME,PARENTID,CONTENT,ENCODING,LANG) VALUES(?,?,?,?,?)
		Object[] params = new Object[] { parentName, parentId, content, enconding, lang };
		String insertId = insert("add-mnote", params);
		return Integer.parseInt(insertId);
	}

	public void deleteMNote(Integer mnpkid) throws SQLException {
		// sql:DELETE FROM MNOTE WHERE MNPKID =?
		Object[] params = new Object[] { mnpkid };
		delete("delete-mnote-byid", params);
	}

	public int addMProp(String parentName, Integer parentId, String pName, String content) throws SQLException {
		// sql :INSERT INTO MPROP (PARENTNAME ,PARENTID,PNAME ,CONTENT) VALUES(?,?,?,?)
		Object[] params = new Object[] { parentName, parentId, pName, content };
		String insertId = insert("add-mprop", params);
		return Integer.parseInt(insertId);
	}

	public void deleteMProp(Integer mppkid) throws SQLException {
		// sql :DELETE FROM MPROP WHERE MPPKID=?
		Object[] params = new Object[] { mppkid };
		delete("delete-mprop-byid", params);

	}

	/**
	 * 删除一个语言片段
	 * @param lang
	 * @param tuvId
	 * @throws SQLException
	 *             ;
	 */
	public void deleteMatrix_lang(String lang, int tuvId) throws SQLException {
		String langCode = Utils.langToCode(lang).toUpperCase();
		String deleteSql = dbConfig.getOperateDbSQL("delete-matrix_lang");
		deleteSql = deleteSql.replaceAll("__LANG__", langCode);
		Object[] params = new Object[] { tuvId };
		PreparedStatement prepareStatement = conn.prepareStatement(deleteSql);
		for (int i = 0; i < params.length; i++) {
			setParameter(prepareStatement, i + 1, params[i]);
		}
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}

	/**
	 * 插入一个语言片段
	 * @param lang
	 * @param tuvId
	 * @param pureText
	 * @throws SQLException
	 *             ;
	 */
	public void insertMatrix_lang(String lang, int tuvId, String pureText) throws SQLException {
		String langCode = Utils.langToCode(lang).toUpperCase();
		int[] ngrams = generateNgrams(lang, pureText);
		PreparedStatement stmt = null;
		if (ngrams.length > 0) {
			String insertMatrix = dbConfig.getMatrixSQL("insert");
			insertMatrix = insertMatrix.replaceAll("__LANG__", langCode);
			stmt = conn.prepareStatement(insertMatrix);
			for (int j = 0; j < ngrams.length; j++) {
				stmt.setInt(1, tuvId);
				stmt.setInt(2, ngrams[j]);
				stmt.setShort(3, (short) ngrams.length);
				stmt.addBatch();
			}
			stmt.executeBatch();
			stmt.close();
		}
	}

	/**
	 * 更新一个语言片段，首先删除以前的片段，然后插入一个新的片段
	 * @param lang
	 * @param tuvId
	 * @param pureText
	 *            ;
	 * @throws SQLException
	 */
	public void updateMatrix_lang(String lang, int tuvId, String pureText) throws SQLException {
		deleteMatrix_lang(lang, tuvId);
		insertMatrix_lang(lang, tuvId, pureText);
	}

	// TODO --------------- 批量处理TMX----------------------------------//

	/**
	 * 1）如果一个TU下面只有一个语言对 <br/>
	 * 一、删除TU .<br/>
	 * 1、删除TU表中TU的数据<br/>
	 * 2、删除TU的扩展属性（A，E）两种类型<br/>
	 * 3、删除TU的note节点<br/>
	 * 4、删除TU的prop节点：只定义属性 <br/>
	 * 二、删除TUV<br/>
	 * 1、删除TUV表的数据 <br/>
	 * 2、删除TUV的扩展属性 <br/>
	 * 3、TUV的note节点 <br/>
	 * 4、TUV的prop节点、<br/>
	 * 5、更新lang表，如果删除TUV后，可能语言表中少一个语言对<br/>
	 * 6、更新Matrix_lang表<br/>
	 * 2）如果一个TU下面有3中或以上语言<br/>
	 * 1、删除TUV表的数据 <br/>
	 * 2、删除TUV的扩展属性<br/>
	 * 3、TUV的note节点<br/>
	 * 4、TUV的prop节点、<br/>
	 * 5、更新lang表，如果删除TUV后，可能语言表中少一个语言对<br/>
	 * 6、更新Matrix_lang表 </br> 7、更新TU的修改者和修改时间<br/>
	 */
	/**
	 * 获取TU下面少于3个TUV的tupkid
	 * @param tupkids
	 * @return ;
	 * @throws SQLException
	 */
	public List<Integer> getTupkids4DeleteTus(List<Integer> tupkids) throws SQLException {
		List<Integer> list = new ArrayList<Integer>();
		for (int id : tupkids) {
			if (getTuvCount(id) < 3) {
				list.add(id);
			}
		}
		return list;
	}

	private int getTuvCount(int id) throws SQLException {
		// SQL:select count(groupid) as TUVCOUNT from textdata where type ='M' and groupid =?
		String sql = dbConfig.getOperateDbSQL("get-tuv-count-by-id");
		Map<Integer, Map<String, String>> rs = query(sql, new Object[] { id });
		String string = rs.get(0).get("TUVCOUNT");
		return Integer.parseInt(string);
	}

	/**
	 * @param tupkids
	 *            :全部TUid
	 * @param tuIds
	 *            ：只有两个TUV的tu
	 * @return ;
	 */
	public List<Integer> getTupkids4DeleteTuv(List<Integer> tupkids, List<Integer> tuIds) {
		List<Integer> ids4Tuv = new ArrayList<Integer>();
		if (tuIds == null || tuIds.isEmpty()) {
			return tupkids;
		}
		for (int id : tuIds) {
			if (!tupkids.contains(id)) {
				ids4Tuv.add(id);
			}
		}
		return ids4Tuv;
	}

	/**
	 * 删除TU表
	 * @param tupkids
	 *            ;
	 * @throws SQLException
	 */
	public void deleteTUs(List<Integer> tupkids) throws SQLException {
		if (null == tupkids || tupkids.isEmpty()) {
			return;
		}
		String deleteMtuSQL = dbConfig.getOperateDbSQL("delete-mtu-byid-use-in");
		deleteMtuSQL = deleteMtuSQL.replaceAll("_SET_", converList2SetString(tupkids));
		PreparedStatement prepareStatement = conn.prepareStatement(deleteMtuSQL);
		prepareStatement.executeUpdate();
		if (null != prepareStatement) {
			prepareStatement.close();
		}
	}

	/**
	 * 删除扩展属性表中的数据
	 * @param tupkids
	 * @param parentName
	 * @throws SQLException
	 *             ;
	 */
	public void deleteExValues(List<Integer> tupkids, String parentName) throws SQLException {
		if (null == tupkids || tupkids.isEmpty()) {
			return;
		}
		String deleteTuExtra = dbConfig.getOperateDbSQL("delete-mextra-byid-use-in");
		deleteTuExtra = deleteTuExtra.replaceAll("_SET_", converList2SetString(tupkids));
		PreparedStatement prepareStatement = conn.prepareStatement(deleteTuExtra);
		prepareStatement.setString(1, parentName);
		prepareStatement.executeUpdate();
		if (null != prepareStatement) {
			prepareStatement.close();
		}
	}

	/**
	 * 刪除NOTE表
	 * @param tupkid
	 *            ;
	 * @throws SQLException
	 */
	public void deleteNotes(List<Integer> tupkids, String parentName) throws SQLException {
		if (null == tupkids || tupkids.isEmpty()) {
			return;
		}
		String deleteTuNoteSQL = dbConfig.getOperateDbSQL("delete-tunote-byid-use-in");
		deleteTuNoteSQL = deleteTuNoteSQL.replaceAll("_SET_", converList2SetString(tupkids));
		PreparedStatement prepareStatement = conn.prepareStatement(deleteTuNoteSQL);
		prepareStatement.setString(1, parentName);
		prepareStatement.executeUpdate();
		if (null != prepareStatement) {
			prepareStatement.close();
		}
	}

	/**
	 * 删除prop表
	 * @param parentId
	 * @param parentName
	 * @throws SQLException
	 *             ;
	 */
	public void deleteProps(List<Integer> tupkids, String parentName) throws SQLException {
		if (null == tupkids || tupkids.isEmpty()) {
			return;
		}
		String deleteTuPropSQL = dbConfig.getOperateDbSQL("delete-tumprop-byid-use-in");
		deleteTuPropSQL = deleteTuPropSQL.replaceAll("_SET_", converList2SetString(tupkids));
		PreparedStatement prepareStatement = conn.prepareStatement(deleteTuPropSQL);
		prepareStatement.setString(1, parentName);
		prepareStatement.executeUpdate();
		if (null != prepareStatement) {
			prepareStatement.close();
		}
	}

	/**
	 * 得到TUV的id值
	 * @param tuids
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public List<Integer> getTuvIdsByTuId(List<Integer> tuids) throws SQLException {
		List<Integer> ids = new ArrayList<Integer>();
		if (null == tuids || tuids.isEmpty()) {
			return ids;
		}
		for (int id : tuids) {
			Map<Integer, Map<String, String>> rs = query(dbConfig.getOperateDbSQL("get-tuv-id-by-tuid"),
					new Object[] { id });
			Collection<Map<String, String>> values = rs.values();
			Iterator<Map<String, String>> iterator = values.iterator();
			while (iterator.hasNext()) {
				Map<String, String> next = iterator.next();
				if(next.get("TPKID")==null){
					continue;
				}
				ids.add(Integer.parseInt(next.get("TPKID")));
			}
		}
		return ids;
	}

	/**
	 * 得到TUV的id值
	 * @param tuids
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public List<Integer> getTuvIdsByTuIdAndTgt(List<Integer> tuids, String tgtCode) throws SQLException {
		List<Integer> ids = new ArrayList<Integer>();
		String operateDbSQL = dbConfig.getOperateDbSQL("get-tuv-id-by-tuid");
		operateDbSQL += "AND LANG =?";
		for (int id : tuids) {
			Map<Integer, Map<String, String>> rs = query(operateDbSQL, new Object[] { id, tgtCode });
			Collection<Map<String, String>> values = rs.values();
			Iterator<Map<String, String>> iterator = values.iterator();
			while (iterator.hasNext()) {
				Map<String, String> next = iterator.next();
				ids.add(Integer.parseInt(next.get("TPKID")));
			}
		}
		return ids;
	}

	/**
	 * 查询src,和tgt按照src排序过后
	 * @param ignoreTag
	 * @param srcCode
	 * @param tgtCode
	 * @throws SQLException
	 *             ;
	 */
	public Map<Integer, Map<String, String>> getSrcTgtGrouped(boolean ignoreTag, String srcCode, String tgtCode)
			throws SQLException {
		String sql = "";
		if (ignoreTag) {
			sql = dbConfig.getOperateDbSQL("get-src-tgt-pure-ordered");
		} else {
			sql = dbConfig.getOperateDbSQL("get-src-tgt-full-ordered");
		}
		// TUPKID ,SRC,TGT,CHANGE_DATE
		return query(sql, new Object[] { srcCode, tgtCode });
	}

	// Collection<Map<String, String>> values = rs.values();
	// Iterator<Map<String, String>> iterator = values.iterator();
	// // 将TU按照原文相同进行分组
	// List<List<Map<String, String>>> groupTus = new ArrayList<List<Map<String, String>>>();
	// String srcPreContent = "-1";
	// String currContent = "";
	// List<Map<String, String>> tempList = null;
	// while (iterator.hasNext()) {
	// Map<String, String> lineRs = iterator.next();
	// currContent = lineRs.get("SRC");
	// if (srcPreContent.equals(currContent)) {
	// tempList.add(lineRs);
	// } else {
	// if (null != tempList && tempList.size() > 1) {
	// groupTus.add(tempList);
	// }
	// tempList = new ArrayList<Map<String, String>>();
	// tempList.add(lineRs);
	// srcPreContent = currContent;
	// }
	//
	// }
	// if (null != tempList && tempList.size() > 1) {
	// groupTus.add(tempList);
	// }
	// return groupTus;
	// }

	public void deleteTUVContentEndsSpace() throws SQLException {
		// SQL :update textdata set content = trim(content) where length(content)<>length(trim(content))
		String updateStmt = dbConfig.getOperateDbSQL("update-tuv-ends-space");
		PreparedStatement prepareStatement = conn.prepareStatement(updateStmt);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}

	/**
	 * 删除TUV和其相关的Matrix_Lang表
	 * @param tuvIds
	 * @param srcCode
	 * @param tgtCode
	 *            ;
	 * @throws SQLException
	 */
	public void deleteTuvMatrixLang(List<Integer> tuvIds, String srcCode, String tgtCode) throws SQLException {
		if (null == tuvIds || tuvIds.isEmpty()) {
			return;
		}
		deleteMatixLang(tuvIds, srcCode);
		deleteMatixLang(tuvIds, tgtCode);
		deleteTuv(tuvIds);
	}

	protected void deleteMatixLang(List<Integer> tuvIds, String lCode) throws SQLException {
		if (tuvIds.isEmpty()) {
			return;
		}
		String langCode = Utils.langToCode(lCode).toUpperCase();
		String deleteSql = dbConfig.getOperateDbSQL("delete-matrix_lang-use-in");
		deleteSql = deleteSql.replaceAll("__LANG__", langCode);
		deleteSql = deleteSql.replaceAll("_SET_", converList2SetString(tuvIds));
		PreparedStatement prepareStatement = conn.prepareStatement(deleteSql);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}

	protected void deleteTuv(List<Integer> tuvIds) throws SQLException {
		if (tuvIds.isEmpty()) {
			return;
		}
		String deleteSql = dbConfig.getOperateDbSQL("delete-tuv-by-id-use-in");
		deleteSql = deleteSql.replaceAll("_SET_", converList2SetString(tuvIds));
		PreparedStatement prepareStatement = conn.prepareStatement(deleteSql);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}

	/**
	 * 将一个id集合转换成set的字符串
	 * @return ;
	 */
	protected String converList2SetString(List<Integer> list) {
		StringBuilder buString = new StringBuilder();
		for (int i : list) {
			buString.append(i);
			buString.append(",");
		}
		buString.deleteCharAt(buString.length() - 1);
		return buString.toString();
	}

	protected String conver2SetString(List<String> list) {
		StringBuilder buString = new StringBuilder();
		for (String i : list) {
			buString.append("'" + i + "'");
			buString.append(",");
		}
		buString.deleteCharAt(buString.length() - 1);
		return buString.toString();
	}

	public boolean updateTuAttrByColumn(String colummName, String value) throws SQLException {
		String updateSql = dbConfig.getOperateDbSQL("update-tu-attr-by-column-name");
		updateSql = updateSql.replaceAll("_COLUMN_NAME_", colummName);
		PreparedStatement prepareStatement = conn.prepareStatement(updateSql);
		if ("CREATIONDATE".equals(colummName) || "CHANGEDATE".equals(colummName)) {
			prepareStatement.setTimestamp(1, DateUtils.getTimestampFromUTC(value));
		} else {
			prepareStatement.setString(1, value);
		}
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
	}

	public boolean updateTuAttrByColumnAndIds(String colummName, String value, List<Integer> tupkids)
			throws SQLException {
		String updateSql = dbConfig.getOperateDbSQL("update-tu-attr-by-column-name-ids");
		updateSql = updateSql.replaceAll("_COLUMN_NAME_", colummName);
		updateSql = updateSql.replaceAll("_SET_", converList2SetString(tupkids));
		PreparedStatement prepareStatement = conn.prepareStatement(updateSql);
		if ("CREATIONDATE".equals(colummName) || "CHANGEDATE".equals(colummName)) {
			prepareStatement.setTimestamp(1, DateUtils.getTimestampFromUTC(value));
		} else {
			prepareStatement.setString(1, value);
		}
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 批量添加TU的Prop节点信息
	 * @param tuids
	 * @return ;
	 * @throws SQLException
	 */
	public boolean addTuRropByTuIds(String parentName, String paname, String content, List<Integer> tuids,
			IProgressMonitor monitor) throws SQLException {
		if (tuids.isEmpty()) {
			return false;
		}
		int total = tuids.size() / 1000;
		monitor.beginTask("", total == 0 ? 1 : total);
		// sql:INSERT INTO MPROP (PARENTNAME ,PARENTID,PNAME ,CONTENT) VALUES(?,?,?,?)
		String insertPropSql = dbConfig.getOperateDbSQL("add-mprop");
		PreparedStatement prepareStatement = conn.prepareStatement(insertPropSql);
		int count = 0;
		boolean flag = false;
		for (int id : tuids) {
			prepareStatement.setString(1, parentName);
			prepareStatement.setInt(2, id);
			prepareStatement.setString(3, paname);
			prepareStatement.setString(4, content);
			count++;
			prepareStatement.addBatch();
			if (count % 1000 == 0) {
				prepareStatement.executeBatch();
				prepareStatement.clearBatch();
				flag = true;
				count = 0;
				monitor.worked(1);
			}
		}
		if (count > 0) {
			prepareStatement.executeBatch();
			prepareStatement.clearBatch();
			flag = true;
			monitor.worked(1);
		}
		prepareStatement.close();
		monitor.done();
		return flag;
	}

	/**
	 * 批量添加TU的Prop节点信息
	 * @param tuids
	 * @return ;
	 * @throws SQLException
	 */
	public boolean addTuNotesByTuIds(String parentName, String content, List<Integer> tuids, IProgressMonitor monitor)
			throws SQLException {
		if (tuids.isEmpty()) {
			return false;
		}
		int total = tuids.size() / 1000;
		monitor.beginTask("", total == 0 ? 1 : total);
		// sql:INSERT INTO MNOTE(PARENTNAME,PARENTID,CONTENT) VALUES(?,?,?)
		String insertPropSql = dbConfig.getOperateDbSQL("add-simple-mnote");
		PreparedStatement prepareStatement = conn.prepareStatement(insertPropSql);
		int count = 0;
		boolean flag = false;
		for (int id : tuids) {
			prepareStatement.setString(1, parentName);
			prepareStatement.setInt(2, id);
			prepareStatement.setString(3, content);
			count++;
			prepareStatement.addBatch();
			if (count % 1000 == 0) {
				prepareStatement.executeBatch();
				prepareStatement.clearBatch();
				flag = true;
				count = 0;
				monitor.worked(1);
			}
		}
		if (count > 0) {
			prepareStatement.executeBatch();
			prepareStatement.clearBatch();
			flag = true;
			monitor.worked(1);
		}
		prepareStatement.close();
		monitor.done();
		return flag;
	}

	/**
	 * 批量更新整个数据库
	 * @param oldPname
	 * @param oldContent
	 * @param newPname
	 * @param newContent
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public boolean updateAllProp(String oldPname, String oldContent, String newPname, String newContent)
			throws SQLException {
		String updateSql = dbConfig.getOperateDbSQL("update-mprop-by-content");
		PreparedStatement prepareStatement = conn.prepareStatement(updateSql);
		prepareStatement.setString(1, newPname);
		prepareStatement.setString(2, newContent);
		prepareStatement.setString(3, oldPname);
		prepareStatement.setString(4, oldContent);
		int executeUpdate = prepareStatement.executeUpdate();
		prepareStatement.close();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
	}
	/**
	 * @param oldPname
	 * @param oldContent
	 * @param newPname
	 * @param newContent
	 * @return
	 * @throws SQLException ;
	 */
	public boolean updateAllPropType(String oldPname, String newPname)
			throws SQLException {
		String updateSql = dbConfig.getOperateDbSQL("update-mprop-type");
		PreparedStatement prepareStatement = conn.prepareStatement(updateSql);
		prepareStatement.setString(1, newPname);
		prepareStatement.setString(2, oldPname);
		int executeUpdate = prepareStatement.executeUpdate();
		prepareStatement.close();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
	}

	public boolean updateAllNotes(String oldContent, String newContent) throws SQLException {
		// sql :UPDATE MNOTE SET CONTENT = ? WHERE CONTENT = ?
		String updateSql = dbConfig.getOperateDbSQL("update-mnote-by-content");
		PreparedStatement prepareStatement = conn.prepareStatement(updateSql);
		prepareStatement.setString(1, newContent);
		prepareStatement.setString(2, oldContent);
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 依据ID值批量更新数据库
	 * @param oldPname
	 * @param oldContent
	 * @param newPname
	 * @param newContent
	 * @param ids
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public boolean updatePropByIds(String oldPname, String oldContent, String newPname, String newContent,
			List<Integer> ids) throws SQLException {
		if (ids.isEmpty()) {
			return false;
		}
		String updateSql = dbConfig.getOperateDbSQL("update-mprop-by-content-ids");
		updateSql = updateSql.replaceAll("_SET_", converList2SetString(ids));
		PreparedStatement prepareStatement = conn.prepareStatement(updateSql);
		prepareStatement.setString(1, newPname);
		prepareStatement.setString(2, newContent);
		prepareStatement.setString(3, oldPname);
		prepareStatement.setString(4, oldContent);
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;

	}
	public boolean updatePropTypeByIds(String oldPname, String newPname, List<Integer> ids) throws SQLException {
		if (ids.isEmpty()) {
			return false;
		}
		String updateSql = dbConfig.getOperateDbSQL("update-mprop-type-by-ids");
		updateSql = updateSql.replaceAll("_SET_", converList2SetString(ids));
		PreparedStatement prepareStatement = conn.prepareStatement(updateSql);
		prepareStatement.setString(1, newPname);
		prepareStatement.setString(2, oldPname);
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
		
	}

	public boolean updateNotesByIds(String oldContent, String newContent, List<Integer> ids) throws SQLException {
		if (ids.isEmpty()) {
			return false;
		}
		// sql :UPDATE MNOTE SET CONTENT = ? WHERE CONTENT = ? AND PARENTID IN (_SET_)
		String updateSql = dbConfig.getOperateDbSQL("update-mnote-by-content-ids");
		updateSql = updateSql.replaceAll("_SET_", converList2SetString(ids));
		PreparedStatement prepareStatement = conn.prepareStatement(updateSql);
		prepareStatement.setString(1, newContent);
		prepareStatement.setString(2, oldContent);
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;

	}

	/**
	 * 批量删除整个数据库内容
	 * @param oldPname
	 * @param oldContent
	 * @param newPname
	 * @param newContent
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public boolean deleteAllProp(String oldPname, String oldContent) throws SQLException {
		String deleteSql = dbConfig.getOperateDbSQL("delete-mprop-content");
		PreparedStatement prepareStatement = conn.prepareStatement(deleteSql);
		prepareStatement.setString(1, oldPname);
		prepareStatement.setString(2, oldContent);
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
	}

	public boolean deleteAllNote(String oldContent) throws SQLException {
		// sql:DELETE FROM MNOTE WHERE CONTENT = ?
		String deleteSql = dbConfig.getOperateDbSQL("delete-mnote-by-content");
		PreparedStatement prepareStatement = conn.prepareStatement(deleteSql);
		prepareStatement.setString(1, oldContent);
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 依据ID值删除数据库内容
	 * @param oldPname
	 * @param oldContent
	 * @param newPname
	 * @param newContent
	 * @param ids
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public boolean deletePropByIds(String oldPname, String oldContent, List<Integer> ids) throws SQLException {
		if (ids.isEmpty()) {
			return false;
		}
		String deleteSql = dbConfig.getOperateDbSQL("delete-mprop-content-ids");
		deleteSql = deleteSql.replaceAll("_SET_", converList2SetString(ids));
		PreparedStatement prepareStatement = conn.prepareStatement(deleteSql);
		prepareStatement.setString(1, oldPname);
		prepareStatement.setString(2, oldContent);
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
	}

	public boolean deleteNotesByIds(String oldContent, List<Integer> ids) throws SQLException {
		if (ids.isEmpty()) {
			return false;
		}
		// sql :DELETE FROM MNOTE WHERE CONTENT = ? AND PARENTID IN(_SET_)
		String deleteSql = dbConfig.getOperateDbSQL("delete-mnote-by-content-ids");
		deleteSql = deleteSql.replaceAll("_SET_", converList2SetString(ids));
		PreparedStatement prepareStatement = conn.prepareStatement(deleteSql);
		prepareStatement.setString(1, oldContent);
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 批量添加扩展属性
	 * @param parentName
	 * @param parentIds
	 * @param attName
	 * @param attriValue
	 * @param monitor
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public boolean addExAttributes(String parentName, List<Integer> parentIds, String attName, String attriValue,
			IProgressMonitor monitor) throws SQLException {
		if (parentIds.isEmpty()) {
			return false;
		}
		int total = parentIds.size() / 1000;
		monitor.beginTask("", total == 0 ? 1 : total);
		// sql : INSERT INTO MEXTRA (PARENTNAME , PARENTID ,NTYPE ,NNAME,CONTENT) VALUES(?,?,'A',?,?)
		String insertPropSql = dbConfig.getOperateDbSQL("add-attribute");
		PreparedStatement prepareStatement = conn.prepareStatement(insertPropSql);
		int count = 0;
		int[] executeBatch = new int[0];
		boolean flag = false;
		for (int id : parentIds) {
			prepareStatement.setString(1, parentName);
			prepareStatement.setInt(2, id);
			prepareStatement.setString(3, attName);
			prepareStatement.setString(4, attriValue);
			count++;
			prepareStatement.addBatch();
			if (count % 1000 == 0) {
				executeBatch = prepareStatement.executeBatch();
				prepareStatement.clearBatch();
				if (executeBatch.length > 0) {
					flag = true;
				}
				count = 0;
				monitor.worked(1);
			}
		}
		if (count > 0) {
			executeBatch = prepareStatement.executeBatch();
			prepareStatement.clearBatch();
			if (executeBatch.length > 0) {
				flag = true;
			}
			monitor.worked(1);
		}
		prepareStatement.close();
		monitor.done();
		return flag;
	}

	public List<Integer> getAttrIdsByName(String name, List<Integer> tuvIds) throws SQLException {
		List<Integer> ids = new ArrayList<Integer>();
		// SQL :SELECT MEPKID FROM MEXTRA WHERE NNAME = ? AND NTYPE =? AND PARENTNAME=?
		String operateDbSQL = dbConfig.getOperateDbSQL("get-mepkids-by-name");
		operateDbSQL = operateDbSQL.replaceAll("_SET_", converList2SetString(tuvIds));
		Map<Integer, Map<String, String>> rs = query(operateDbSQL, new Object[] { name, "A", "TUV" });
		Collection<Map<String, String>> values = rs.values();
		Iterator<Map<String, String>> iterator = values.iterator();
		while (iterator.hasNext()) {
			Map<String, String> next = iterator.next();
			ids.add(Integer.parseInt(next.get("MEPKID")));
		}
		return ids;
	}

	public boolean deleteExAttributes(List<Integer> ids, String name, String content) throws SQLException {
		if (ids.isEmpty()) {
			return false;
		}
		// sql:DELETE FROM MEXTRA WHERE MEPKID IN(_SET_)
		String deleteSql = dbConfig.getOperateDbSQL("update-ex-attibute-by-ids");
		deleteSql = deleteSql.replaceAll("_SET_", converList2SetString(ids));
		PreparedStatement prepareStatement = conn.prepareStatement(deleteSql);
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
	}

	public List<Integer> getTuvIdsByLangs(List<String> langs) throws SQLException {
		List<Integer> results = new ArrayList<Integer>();
		// sql:SELECT TPKID FROM TEXTDATA WHERE LANG IN (_LANG_SET_)
		String sql = dbConfig.getOperateDbSQL("get-tpkid-by-langs");
		sql = sql.replaceAll("_LANG_SET_", conver2SetString(langs));
		PreparedStatement psmt = null;
		ResultSet rs = null;
		try {
			psmt = conn.prepareStatement(sql);
			rs = psmt.executeQuery();
			while (rs.next()) {
				results.add(rs.getInt(1));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (psmt != null) {
				psmt.close();
			}
		}
		return results;
	}

	public List<Integer> getTuvIdsByLangsAndTuids(List<String> langs, List<Integer> tuids) throws SQLException {
		List<Integer> results = new ArrayList<Integer>();
		// sql: SELECT TPKID FROM TEXTDATA WHERE LANG IN (_LANG_SET_) AND GROUPID IN (_SET_)
		String sql = dbConfig.getOperateDbSQL("get-tpkid-by-langs-groupids");
		sql = sql.replaceAll("_LANG_SET_", conver2SetString(langs));
		sql = sql.replaceAll("_SET_", converList2SetString(tuids));
		PreparedStatement psmt = null;
		ResultSet rs = null;
		try {
			psmt = conn.prepareStatement(sql);
			rs = psmt.executeQuery();
			while (rs.next()) {
				results.add(rs.getInt(1));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (psmt != null) {
				psmt.close();
			}
		}
		return results;
	}

	public boolean isReadOnly() {
		String sql1 = "insert into LANG values('test')";
		String sql2 = "update LANG set langcode='test' where langcode='test'";
		Statement stm = null;
		try {
			beginTransaction();
			stm = conn.createStatement();
			stm.executeUpdate(sql1);
			stm.executeUpdate(sql2);
		} catch (SQLException e) {
			return true;
		} finally {
			try {
				rollBack();
				if (stm != null) {
					stm.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
