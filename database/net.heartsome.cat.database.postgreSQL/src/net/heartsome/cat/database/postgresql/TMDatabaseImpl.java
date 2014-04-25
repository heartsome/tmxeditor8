package net.heartsome.cat.database.postgresql;

import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBConfig;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.Utils;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * @author terry
 * @version
 * @since JDK1.6
 */
public class TMDatabaseImpl extends DBOperator {

	public void start() throws SQLException, ClassNotFoundException {
		String driver = dbConfig.getDriver();
		Class.forName(driver);
		String url = Utils.replaceParams(dbConfig.getDbURL(), metaData);
		Properties prop = Utils.replaceParams(dbConfig.getConfigProperty(), metaData);
		conn = DriverManager.getConnection(url, prop);
		// 在 PostgreSQL 中如果使用事务，那么在事务中创建表格会抛出异常。
		conn.setAutoCommit(false);
	}

	/**
	 * 构造函数
	 */
	public TMDatabaseImpl() {
		Bundle buddle = Platform.getBundle(Activator.PLUGIN_ID);
		URL fileUrl = buddle.getEntry(Constants.DBCONFIG_PATH);
		dbConfig = new DBConfig(fileUrl);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.database.DBOperator#replaceTMOrTBConditionSql(java.lang.String, java.lang.String, boolean,
	 *      boolean, boolean, java.lang.String, java.lang.String[])
	 */
	public String replaceTMOrTBConditionSql(String sql, String strSearch, boolean isCaseSensitive,
			boolean isApplyRegular, boolean isIgnoreMark, String srcLang, String[] arrFilter) {
		strSearch = strSearch == null ? "" : strSearch;
		StringBuffer strCondition = new StringBuffer();
		if (srcLang != null) {
			strCondition.append(" AND A.LANG='" + srcLang + "'");
		} else {
			return null;
		}
		if (isApplyRegular) {
			// ~ 匹配正则表达式，大小写相关。例: 'thomas' ~ '.*thomas.*'
			// ~* 匹配正则表达式，大小写无关。例: 'thomas' ~* '.*Thomas.*'
			// !~ 不匹配正则表达式，大小写相关。例: 'thomas' !~ '.*Thomas.*'
			// !~* 不匹配正则表达式，大小写无关。例: 'thomas' !~* '.*vadim.*'
			strCondition.append(" AND " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " "
					+ (isCaseSensitive ? "~" : "~*") + " '" + TextUtil.replaceRegextSqlWithMOP(strSearch) + "'");
		} else if (isCaseSensitive) {
			// postgreSql 中区分大小写用 LIKE，不区分用 ILIKE
			strCondition.append(" AND " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " LIKE '%"
					+ TextUtil.cleanStringByLikeWithPostgreSql(strSearch) + "%'");
		} else {
			strCondition.append(" AND " + (isIgnoreMark ? "A.PURE" : "A.CONTENT") + " ILIKE '%"
					+ TextUtil.cleanStringByLikeWithPostgreSql(strSearch) + "%'");
		}
		if (arrFilter != null) {
			StringBuffer strFilter = new StringBuffer(arrFilter[1].replaceAll("LIKE", "ILIKE").replaceAll("like",
					"ILIKE")
					+ " '%" + TextUtil.cleanStringByLikeWithPostgreSql(arrFilter[2]) + "%'");
			// 过滤条件要加在源语言中
			if (arrFilter[0].equalsIgnoreCase(srcLang)) {
				sql = Utils.replaceString(sql, "__TABLE_TEXTDATA__", "");
				strFilter.insert(0, " AND A.PURE ");
				strCondition.append(strFilter.toString());
			} else {
				sql = Utils.replaceString(sql, "__TABLE_TEXTDATA__", " ,TEXTDATA B ");
				strCondition.append(" AND A.GROUPID=B.GROUPID AND B.TYPE='M' AND B.LANG='" + arrFilter[0] + "'");
				strFilter.insert(0, " AND B.PURE ");
				strCondition.append(strFilter.toString());
			}
		} else {
			sql = Utils.replaceString(sql, "__TABLE_TEXTDATA__", "");
		}
		sql = Utils.replaceString(sql, "__CONDITION__", strCondition.toString());
		return sql;
	}

	@Override
	public Vector<Hashtable<String, String>> findAllTermsByText(String srcPureText, String srcLang, String tarLang)
			throws SQLException {
		Vector<Hashtable<String, String>> terms = new Vector<Hashtable<String, String>>();
		// 构建SQL
		String getTermSql = dbConfig.getOperateDbSQL("getTerm");
		PreparedStatement stmt = conn.prepareStatement(getTermSql);

		stmt.setString(1, tarLang);
		stmt.setString(2, srcLang + "," + tarLang);
		stmt.setString(3, tarLang + "," + srcLang);
		stmt.setString(4, srcLang);
		stmt.setString(5, srcPureText.toLowerCase());

		/*
		 * SELECT A.TPKID, A.PURE, B.PURE FROM TEXTDATA A LEFT JOIN TEXTDATA B ON A.GROUPID=B.GROUPID AND B.LANG=? AND
		 * B.TYPE='B' WHERE A.TYPE='B' AND A.LANG=? AND PATINDEX(STUFF('%%',2,0,CAST(A.PURE AS NVARCHAR(4000))),?) &gt;
		 * 0 AND B.PURE IS NOT NULL
		 */

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String tuid = rs.getString(1);
			String srcWord = rs.getString(2);
			String tgtWord = rs.getString(3);
			String property = rs.getString(4);
			Hashtable<String, String> tu = new Hashtable<String, String>();
			tu.put("tuid", tuid);
			tu.put("srcLang", srcLang);
			tu.put("srcWord", srcWord);
			tu.put("tgtLang", tarLang);
			tu.put("tgtWord", tgtWord);
			tu.put("property", property == null ? "" : property);
			terms.add(tu);
		}
		rs.close();
		stmt.close();
		return terms;
	}
	
	public void updateTUVContent(int tuvId, String hashCode, String pureText, String content) throws SQLException {
		String updateTUVContent = dbConfig.getOperateDbSQL("update-tuvcontent");
		PreparedStatement prepareStatement = conn.prepareStatement(updateTUVContent);
		prepareStatement.setInt(1, Integer.parseInt(hashCode));
		prepareStatement.setString(2, pureText);
		prepareStatement.setString(3, content);
		prepareStatement.setInt(4, tuvId);
		prepareStatement.executeUpdate();
		prepareStatement.close();
	}
	

	public boolean updateTuAttrByColumn(String colummName, String value) throws SQLException {
		String updateSql = dbConfig.getOperateDbSQL("update-tu-attr-by-column-name");
		updateSql = updateSql.replaceAll("_COLUMN_NAME_", colummName);
		PreparedStatement prepareStatement = conn.prepareStatement(updateSql);
		if("CREATIONDATE".equals(colummName) || "CHANGEDATE".equals(colummName)){
			value = value == null || value.equals("") ? DateUtils.getStringDate() : value;
			prepareStatement.setTimestamp(1, Timestamp.valueOf(value));
		}else{			
			prepareStatement.setString(1, value);
		}
		int executeUpdate = prepareStatement.executeUpdate();
		if (executeUpdate > 0) {
			return true;
		}
		return false;
	}
}
