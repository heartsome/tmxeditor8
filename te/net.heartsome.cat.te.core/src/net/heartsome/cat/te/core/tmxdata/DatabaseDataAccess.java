/**
 * DatabaseDataAccess.java
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.database.DBConfig;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.TMXDBOperatorFacade;
import net.heartsome.cat.database.Utils;
import net.heartsome.cat.te.core.Activator;
import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.bean.ExportBean;
import net.heartsome.cat.te.core.bean.Property;
import net.heartsome.cat.te.core.bean.TmxEditorFilterBean;
import net.heartsome.cat.te.core.bean.TmxPropertiesBean;
import net.heartsome.cat.te.core.bean.TmxTemplet;
import net.heartsome.cat.te.core.converter.tbx.AppendTmxWriter;
import net.heartsome.cat.te.core.resource.Messages;
import net.heartsome.cat.te.core.utils.DatabaseAccessUtils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseDataAccess extends AbstractTmxDataAccess {

	public static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDataAccess.class);

	private TmxDbContainer container;

	private TMXDBOperatorFacade facade;

	public DatabaseDataAccess(TmxDbContainer container) {
		super(container);
		this.container = container;
		facade = new TMXDBOperatorFacade(container.getDbOp());
	}

	public String retrieveTuXml(int tuIdentifier) {
		return null;
	}

	public void closeTmxDataAccess(IProgressMonitor monitor) {
		if (container != null) {
			container.closeDatabaseConnection();
		}
		container = null;
	}

	@Override
	public void save(IProgressMonitor monitor) {
		// Nothing to do
	}

	@Override
	public void saveAs(IProgressMonitor monitor, ExportBean exportBean) {
		// ExportDatabaseBean eBean = new ExportDatabaseBean(container.getDbOp().getMetaData(), "");
		// List<String> ls = new ArrayList<String>();
		// ls.add(currSrcLang);
		// ls.add(currTgtLang);
		// eBean.setHasSelectedLangs(ls);
		// eBean.setExportFilePath(newFilePath);
		// eBean.setSrcLang(currSrcLang);
		// List<ExportDatabaseBean> lb = new ArrayList<ExportDatabaseBean>();
		// lb.add(eBean);
		// ExportAbstract exp = new ExportTmxImpl(lb, null, "UTF-8", false, false);
		// final String result = exp.executeExport(monitor);
		// Display.getDefault().syncExec(new Runnable() {
		// public void run() {
		// MessageDialog.openInformation(Display.getDefault().getActiveShell(),
		// Messages.getString("dialog.ExportTmxDialog.msgTitle"), result);
		// }
		// });
		int scope = exportBean.getExportScope();
		List<String> ids = null;
		switch (scope) {
		case 1: // all filter TU
			ids = tuIdentifiers;
			break;
		case 2: // all selected TU
			ids = exportBean.getSelectIds();
			break;
		case 3: // all TU
			ids = null;
			break;
		}
		DBOperator dbOp = container.getDbOp();
		List<String> langs = new ArrayList<String>();
		langs.add(currSrcLang);
		langs.add(currTgtLang);
		if (!exportBean.isAppend()) {
			FileOutputStream os = null;
			try {
				os = new FileOutputStream(exportBean.getTargetFile());
				String encoding = "UTF-8";
				writeString(os, TmxTemplet.genertateTmxXmlDeclar(), encoding);
				writeString(os, "<tmx version=\"1.4\">\n", encoding);
				TmxHeader header = TmxTemplet.generateTmxHeader(currSrcLang, "unknown", "sentence",
						"Heartsome TM Server", null, null, null);
				writeString(os, TmxTemplet.header2Xml(header), encoding);
				writeString(os, "<body>\n", encoding);
				if (ids == null) {
					List<Integer> s = dbOp.getAllTuPkByLang(currSrcLang, currTgtLang, "", "");
					ids = new ArrayList<String>();
					for (int i : s) {
						ids.add(i + "");
					}
				}
				for (int i = 0; i < ids.size(); i++) {
					int tuPk = Integer.parseInt(tuIdentifiers.get(i));
					String tuNodeContent = dbOp.retrieveTu(tuPk, langs, false, false);
					if (tuNodeContent != null && !tuNodeContent.equals("")) {
						writeString(os, tuNodeContent, encoding);
					}
				}
				writeString(os, "</body>\n", encoding);
				writeString(os, "</tmx>\n", encoding);
			} catch (Exception e) {
				LOGGER.error("", e);
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						LOGGER.error("", e);
					}
				}
			}
		} else {
			AppendTmxWriter tmxAppendWriter;
			try {
				if (ids == null) {
					List<Integer> s = dbOp.getAllTuPkByLang(currSrcLang, currTgtLang, "", "");
					ids = new ArrayList<String>();
					for (int i : s) {
						ids.add(i + "");
					}
				}
				tmxAppendWriter = new AppendTmxWriter(exportBean.getTargetFile());
				String aSrcLang = tmxAppendWriter.getSrcLang();
				if (currSrcLang.equals(aSrcLang)) {
					tmxAppendWriter.startAppend();
					for (int i = 0; i < ids.size(); i++) {
						int tuPk = Integer.parseInt(tuIdentifiers.get(i));
						String tuNodeContent = dbOp.retrieveTu(tuPk, langs, false, false);
						if (tuNodeContent != null && !tuNodeContent.equals("")) {
							tmxAppendWriter.writeXmlString(tuNodeContent);
						}
					}
					tmxAppendWriter.writeEnd();
					tmxAppendWriter.closeOutStream();
				} else {
					OpenMessageUtils.openMessage(IStatus.ERROR, "");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Return null; (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#loadTmxProperties()
	 */
	public TmxPropertiesBean loadTmxProperties() {
		TmxPropertiesBean bean = new TmxPropertiesBean();
		DBOperator dbOp = container.getDbOp();
		try {
			List<String> lang = dbOp.getLanguages();
			if (lang.size() == 0) {
				return null;
			}
			String srcLang = lang.get(0);
			bean.setSrcLang(srcLang);
			lang.remove(0);
			bean.setTargetLang(lang);
			// facade.setLangs(srcLang, lang.get(0));
			// init super langs
			super.currSrcLang = srcLang;
			super.currTgtLang = lang.get(0);
			super.langList.addAll(lang);

		} catch (SQLException e) {
			LOGGER.error("", e);
		}
		return bean;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#loadDisplayTuIdentifierByFilter(net.heartsome.cat.te.core.bean.TmxEditorFilterBean,
	 *      java.lang.String, java.lang.String, boolean)
	 */
	public void loadDisplayTuIdentifierByFilter(IProgressMonitor monitor, TmxEditorFilterBean filterBean,
			String srcLang, String tgtLang, String srcSearchText, String tgtSearchText) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		Assert.isLegal(srcLang != null && srcLang.length() > 0 && tgtLang != null && tgtLang.length() > 0);
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean ignoreTag = store.getBoolean(TeCoreConstant.FILTER_ignoreTag);
		boolean ignoreCase = store.getBoolean(TeCoreConstant.FILTER_ignoreCase);
		super.currSrcLang = srcLang;
		super.currTgtLang = tgtLang;

		// 设置目标语言和源语言
		facade.setLangs(srcLang, tgtLang);
		try {
			monitor.beginTask("", IProgressMonitor.UNKNOWN);
			monitor.setTaskName(Messages.getString("core.dataAccess.filter.data.taskname"));
			DBOperator dbOp = container.getDbOp();
			dbOp.beginTransaction();
			if (filterBean == null || (filterBean != null && TeCoreConstant.FILTERID_allSeg.equals(filterBean.getId()))) { // 查询所有
				super.tuIdentifiers.clear();
				if (langList.size() == 1 && srcSearchText.length() == 0 && tgtSearchText.length() == 0) {
					dbOp.commit();
					// 不需要语言过滤,加载全部TU
					List<Integer> ids = dbOp.getAfterFilterTuPk(null, null, null);
					for (int i : ids) {
						super.tuIdentifiers.add(i + "");
					}
				} else {
					dbOp.commit();
					List<Integer> ids = dbOp.getAllTuPkByLang(currSrcLang, currTgtLang, srcSearchText, tgtSearchText);
					for (int i : ids) {
						super.tuIdentifiers.add(i + "");
					}
				}
				return;

			} else if (TeCoreConstant.FILTERID_srcSameWIthTgtSeg.equals(filterBean.getId())) {
				dbOp.commit();
				List<Integer> ids = dbOp.getSrcSameWithTgtTuPK(srcLang, tgtLang, ignoreCase, ignoreTag);
				super.tuIdentifiers.clear();
				for (int i : ids) {
					super.tuIdentifiers.add(i + "");
				}
			} else if (TeCoreConstant.FILTERID_srcSameButTgtSeg.equals(filterBean.getId())) {
				dbOp.commit();
				List<Integer> ids = dbOp.getSrcSameButTgtTuPK(srcLang, tgtLang, ignoreCase, ignoreTag);
				super.tuIdentifiers.clear();
				for (int i : ids) {
					super.tuIdentifiers.add(i + "");
				}
			} else if (TeCoreConstant.FILTERID_tgtSameButSrcSeg.equals(filterBean.getId())) {
				dbOp.commit();
				List<Integer> ids = dbOp.getTgtSameButSrcTuPK(srcLang, tgtLang, ignoreCase, ignoreTag);
				super.tuIdentifiers.clear();
				for (int i : ids) {
					super.tuIdentifiers.add(i + "");
				}
			} else if (TeCoreConstant.FILTERID_duplicateSeg.equals(filterBean.getId())) {
				dbOp.commit();
				List<Integer> ids = dbOp.getDuplicateSegTUPK(srcLang, tgtLang, ignoreCase, ignoreTag);
				super.tuIdentifiers.clear();
				for (int i : ids) {
					super.tuIdentifiers.add(i + "");
				}
			} else if (TeCoreConstant.FILTERID_withNoteSeg.equals(filterBean.getId())) {
				dbOp.commit();
				List<String> ids = dbOp.getWithNoteTuPK(srcLang, tgtLang, ignoreCase, ignoreTag);
				super.tuIdentifiers.clear();
				super.tuIdentifiers = ids;
			} else if (TeCoreConstant.FILTERID_withGarbleSeg.equals(filterBean.getId())) {
				dbOp.commit();
				List<String> ids = dbOp.getWithGarbleTuPK(srcLang, tgtLang, ignoreCase, ignoreTag);
				super.tuIdentifiers.clear();
				super.tuIdentifiers = ids;
			} else if (TeCoreConstant.FILTERID_tgtNullSeg.equals(filterBean.getId())) {
				dbOp.commit();
				List<String> ids = dbOp.getTgtNullTuPK(srcLang, tgtLang, ignoreCase, ignoreTag);
				super.tuIdentifiers.clear();
				for (String i : ids) {
					super.tuIdentifiers.add(i);
				}
			} else {
				dbOp.commit();
				List<String> ids = getCustomFilterTuPK(dbOp, filterBean, srcLang, tgtLang, ignoreCase, ignoreTag);
				super.tuIdentifiers.clear();
				super.tuIdentifiers = ids;
			}

			monitor.done();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 自定义过滤查询 robert 2013-08-26 UNDO 这里还有一个问题，就是
	 * @throws Exception
	 *             ;
	 */
	public List<String> getCustomFilterTuPK(DBOperator dbOp, TmxEditorFilterBean filterBean, String srcLang,
			String tgtLang, boolean ignoreCase, boolean ignoreTag) throws Exception {
		srcLang = srcLang.toLowerCase();
		tgtLang = tgtLang.toLowerCase();
		StringBuffer sqlSB = new StringBuffer();
		// 是否满足当前过滤器的所有条件，true：满足所有条件，　false：满足以下任意一条件
		boolean isFitAll = filterBean.isFitAll();
		String link = isFitAll ? " AND " : " OR ";

		// 根据五个处理对象，分批次进行处理，先处理源文的情况
		List<Property> srcFilter = filterBean.getSrcFilter();
		List<Property> tgtFilter = filterBean.getTgtFilter();
		List<Property> fixedPropFilter = filterBean.getFixedPropFilter();
		List<Property> customPropFilter = filterBean.getCustomPropFilter();
		List<Property> noteFilter = filterBean.getNoteFilter();

		// 开始组装，其中　A 为　MTU 表， B 为源文所在的表，　C 为译文所在的表，　D 为批注的表, E 为自定义属性表
		sqlSB.append("SELECT MTUPKID AS ID FROM __DATABASE_NAME___MTU A\n");
		sqlSB.append("LEFT JOIN __DATABASE_NAME___TEXTDATA B ON B.GROUPID = A.MTUPKID\n");
		sqlSB.append("LEFT JOIN __DATABASE_NAME___TEXTDATA C ON C.GROUPID = A.MTUPKID\n");
		if (noteFilter != null && noteFilter.size() > 0) {
			sqlSB.append("LEFT JOIN __DATABASE_NAME___MNOTE D ON D.PARENTID = A.MTUPKID\n");
		}
		if (noteFilter != null && customPropFilter.size() > 0) {
			sqlSB.append("LEFT JOIN __DATABASE_NAME___MPROP E ON E.PARENTID = A.MTUPKID\n");
		}
		sqlSB.append("WHERE LOWER(B.LANG) = ? AND LOWER(C.LANG) = ? AND (");

		if (srcFilter != null && srcFilter.size() > 0) {
			sqlSB.append("\n");
			for (Property prop : srcFilter) {
				if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_equal)) {
					sqlSB.append(ignoreCase ? "###BINARY### LOWER(B.###IGNORETAG###) = '"
							+ prop.getValue().toLowerCase() + "'" : "###BINARY### B.###IGNORETAG### = '"
							+ prop.getValue() + "'");
					sqlSB.append(link);
				} else if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_notEqual)) {
					sqlSB.append((ignoreCase ? "###BINARY### LOWER(B.###IGNORETAG###) <> '"
							: "###BINARY### B.###IGNORETAG### <> '")
							+ (ignoreCase ? prop.getValue().toLowerCase() : prop.getValue()) + "'");
					sqlSB.append(link);
				} else if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_include)) {
					sqlSB.append("INSTR("
							+ (ignoreCase ? "###BINARY### LOWER(B.###IGNORETAG###)" : "###BINARY### B.###IGNORETAG###")
							+ ",'" + (ignoreCase ? prop.getValue().toLowerCase() : prop.getValue()) + "') > 0");
					sqlSB.append(link);
				} else if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_notInclude)) {
					sqlSB.append("INSTR("
							+ (ignoreCase ? "###BINARY### LOWER(B.###IGNORETAG###)" : "###BINARY### B.###IGNORETAG###")
							+ ",'" + (ignoreCase ? prop.getValue().toLowerCase() : prop.getValue()) + "') = 0");
					sqlSB.append(link);
				}
			}
		}

		if (tgtFilter != null && tgtFilter.size() > 0) {
			sqlSB.append("\n");
			for (Property prop : tgtFilter) {
				if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_equal)) {
					sqlSB.append(ignoreCase ? "###BINARY### LOWER(C.###IGNORETAG###) = '"
							+ prop.getValue().toLowerCase() + "'" : "###BINARY### C.###IGNORETAG### = '"
							+ prop.getValue() + "'");
					sqlSB.append(link);
				} else if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_notEqual)) {
					sqlSB.append((ignoreCase ? "###BINARY### LOWER(C.###IGNORETAG###) <> '"
							: "###BINARY### C.###IGNORETAG### <> '")
							+ (ignoreCase ? prop.getValue().toLowerCase() : prop.getValue()) + "'");
					sqlSB.append(link);
				} else if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_include)) {
					sqlSB.append("INSTR("
							+ (ignoreCase ? "###BINARY### LOWER(C.###IGNORETAG###)" : "###BINARY### C.###IGNORETAG###")
							+ ",'" + (ignoreCase ? prop.getValue().toLowerCase() : prop.getValue()) + "') > 0");
					sqlSB.append(link);
				} else if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_notInclude)) {
					sqlSB.append("INSTR("
							+ (ignoreCase ? "###BINARY### LOWER(C.###IGNORETAG###)" : "###BINARY### C.###IGNORETAG###")
							+ ",'" + (ignoreCase ? prop.getValue().toLowerCase() : prop.getValue()) + "') = 0");
					sqlSB.append(link);
				}
			}
		}

		if (noteFilter != null && noteFilter.size() > 0) {
			sqlSB.append("\n");
			for (Property prop : noteFilter) {
				if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_equal)) {
					sqlSB.append(ignoreCase ? "###BINARY### LOWER(D.CONTENT) = '" + prop.getValue().toLowerCase() + "'"
							: "###BINARY### D.###IGNORETAG### = '" + prop.getValue() + "'");
					sqlSB.append(link);
				} else if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_notEqual)) {
					sqlSB.append((ignoreCase ? "###BINARY### LOWER(D.CONTENT) <> '" : "###BINARY### D.CONTENT <> '")
							+ (ignoreCase ? prop.getValue().toLowerCase() : prop.getValue()) + "'");
					sqlSB.append(link);
				} else if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_include)) {
					sqlSB.append("INSTR(" + (ignoreCase ? "###BINARY### LOWER(D.CONTENT)" : "###BINARY### D.CONTENT")
							+ ",'" + (ignoreCase ? prop.getValue().toLowerCase() : prop.getValue()) + "') > 0");
					sqlSB.append(link);
				} else if (prop.getKey().equals(TeCoreConstant.FILTER_TYPE_notInclude)) {
					sqlSB.append("INSTR(" + (ignoreCase ? "###BINARY### LOWER(D.CONTENT)" : "###BINARY### D.CONTENT")
							+ ",'" + (ignoreCase ? prop.getValue().toLowerCase() : prop.getValue()) + "') = 0");
					sqlSB.append(link);
				}
			}
		}

		if (fixedPropFilter != null && fixedPropFilter.size() > 0) {
			sqlSB.append("\n");
			for (Property prop : fixedPropFilter) {
				if (ignoreCase) {
					sqlSB.append("###BINARY### LOWER(A." + prop.getKey() + ") = " + prop.getValue().toLowerCase() + "");
				} else {
					sqlSB.append("###BINARY### A." + prop.getKey() + " = " + prop.getValue() + "");
				}
				sqlSB.append("###BINARY### A." + prop.getKey() + " = " + prop.getValue() + "");
				sqlSB.append(link);
			}
		}

		if (customPropFilter != null && customPropFilter.size() > 0) {
			sqlSB.append("\n");
			for (Property prop : customPropFilter) {
				if (ignoreCase) {
					sqlSB.append("###BINARY### LOWER(E." + prop.getKey() + ") = " + prop.getValue().toLowerCase() + "");
				} else {
					sqlSB.append("###BINARY### E." + prop.getKey() + " = " + prop.getValue() + "");
				}
				sqlSB.append(link);
			}
		}

		String sql = sqlSB.toString();
		if (sql.lastIndexOf(link) == sql.length() - link.length()) {
			sql = sql.substring(0, sql.lastIndexOf(link));
		}
		sql += ")";

		if ("Oracle".equalsIgnoreCase(dbOp.getMetaData().getDbType())) {
			sql = Utils.replaceString(sql, "__DATABASE_NAME__", dbOp.getMetaData().getDatabaseName());
			sql = Utils.replaceString(sql, "###BINARY###", "");
		} else {
			sql = Utils.replaceString(sql, "__DATABASE_NAME___", "");
			sql = Utils.replaceString(sql, "###BINARY###", "BINARY");
		}

		List<String> result = dbOp.getCustomFilterTuPK(sql, srcLang, tgtLang, ignoreCase, ignoreTag);
		return result;
	}

	@Override
	public String addTu(TmxTU tu, String selTuIdentifer) {
		try {
			facade.beginTransaction();
			int tuId = facade.insertTu(tu);
			facade.commit();
			tu.setTmId(tuId);
			return tuId + "";
		} catch (SQLException e) {
			LOGGER.error("", e);
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
		}
		return null;
	}

	@Override
	public void deleteTus(String[] tuIdentifiers, IProgressMonitor monitor) {
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		List<Integer> list = new ArrayList<Integer>();
		for (String id : tuIdentifiers) {
			list.add(Integer.parseInt(id));
		}
		try {
			facade.deleteTuByIds(list, currSrcLang, currTgtLang, System.getProperty("user.name"), monitor);
		} catch (SQLException e) {
			LOGGER.error("", e);
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * 1.将newText写入到Tmx文件或者TmDb 2.将newText更新到TmxSegement对象中。
	 * @param newText
	 * @param tu
	 * @param tuv
	 **/
	public void updateTuvContent(String identifier, String newText, TmxTU tu, TmxSegement tuv) {
		if (tuv.getFullText().equals(newText)) { // 新内容和原来的内容一样
			return;
		}
		TmxSegement newTuv = new TmxSegement(newText, null);
		newTuv.setDbPk(tuv.getDbPk());
		newTuv.setLangCode(tuv.getLangCode());
		try {
			facade.beginTransaction();
			facade.updateTUVContent(tu.getTmId(), newTuv);
			String changedate = DateUtils.getStringDate();
			String changeid = System.getProperty("user.name");
			tu.setChangeDate(changedate);
			tu.setChangeUser(changeid);
			facade.updateTU(tu);
			facade.commit();
			if (tuv.getDbPk() == -1) {
				tuv.setDbPk(newTuv.getDbPk());
			}
			tuv.setFullTextWithParseTag(newText);
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	public void updateTuNote(String tuIdnetifier, TmxTU tu, TmxNote note, String newContent) {
		String oldContent = note.getContent();
		try {
			facade.beginTransaction();
			note.setContent(newContent);
			facade.updateNote(tu.getTmId(), note);
			facade.commit();
		} catch (SQLException e) {
			try {
				note.setContent(oldContent);
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	@Override
	public void updateTuNote(Map<String, TmxTU> tus, TmxNote note, String newContent) {
		String oldContent = note.getContent();
		try {
			facade.beginTransaction();
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				List<TmxNote> notes = entry.getValue().getNotes();
				if (notes == null) {
					continue;
				}
				for (TmxNote n : notes) {
					if (oldContent.equals(n.getContent())) {
						n.setContent(newContent);
						facade.updateNote(entry.getValue().getTmId(), n);
					}
				}
			}
			facade.commit();
		} catch (SQLException e) {
			try {
				note.setContent(oldContent);
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	public void deleteTuNote(String tuIdentifier, TmxTU tu, TmxNote deleteNote) {
		try {
			facade.beginTransaction();
			facade.deleteTuNote(deleteNote.getDbPk());
			facade.commit();
			List<TmxNote> notes = tu.getNotes();
			if (null != notes) {
				notes.remove(deleteNote);
			}
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	@Override
	public void deleteTuNote(Map<String, TmxTU> tus, TmxNote deleteNote) {
		String deleteContent = deleteNote.getContent();
		try {
			facade.beginTransaction();
			List<TmxNote> rmNotes = new LinkedList<TmxNote>();
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				List<TmxNote> notes = entry.getValue().getNotes();
				if (notes == null) {
					continue;
				}
				for (TmxNote note : notes) {
					if (deleteContent.equals(note.getContent())) {
						rmNotes.add(note);
						facade.deleteTuNote(note.getDbPk());
					}
				}
				notes.removeAll(rmNotes);
			}
			facade.commit();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	public int addTuNote(String tuIdentifier, TmxTU tu, String content) {
		int addId = -1;
		try {
			TmxNote newNote = new TmxNote();
			newNote.setContent(content);
			facade.beginTransaction();
			addId = facade.addTuNote(tu.getTmId(), newNote);
			facade.commit();
			tu.appendNote(newNote);
			newNote.setDbPk(addId);
			return addId;
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
		return addId;
	}

	@Override
	public int addTuNote(Map<String, TmxTU> tus, String content) {
		int addId = -1;
		try {
			facade.beginTransaction();
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				TmxNote newNote = new TmxNote();
				newNote.setContent(content);
				addId = facade.addTuNote(entry.getValue().getTmId(), newNote);
				newNote.setDbPk(addId);
				entry.getValue().appendNote(newNote);
			}
			facade.commit();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
		return tus.size();
	}

	public int addTuProp(String tuIdentifier, TmxTU tu, String propType, String newContent) {
		int addId = -1;
		try {
			facade.beginTransaction();
			TmxProp newProp = new TmxProp();
			newProp.setName(propType);
			newProp.setValue(newContent);
			addId = facade.addTuProp(tu.getTmId(), newProp);
			facade.commit();
			newProp.setDbPk(addId);
			tu.appendProp(newProp);
			return addId;
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
		return addId;
	}

	@Override
	public int addTuProp(Map<String, TmxTU> tus, String propType, String newContent) {
		int addId = -1;
		try {
			facade.beginTransaction();
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				TmxProp newProp = new TmxProp();
				newProp.setName(propType);
				newProp.setValue(newContent);
				addId = facade.addTuProp(entry.getValue().getTmId(), newProp);
				newProp.setDbPk(addId);
				entry.getValue().appendProp(newProp);
			}
			facade.commit();
			return addId;
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
		return addId;
	}

	/**
	 * 添加翻译单元自定义属性 (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#addTuAttribute(java.lang.String,
	 *      net.heartsome.cat.common.bean.TmxTU, java.lang.String, java.lang.String)
	 */
	public void addTuAttribute(String tuIdentifier, TmxTU tu, String name, String value) {
		try {
			facade.beginTransaction();
			facade.addTuAttribute(tu.getTmId(), name, value);
			facade.commit();
			tu.appendAttribute(name, value);
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	public void addTuvAttribute(String tuIdentifier, TmxSegement tuv, String name, String value) {
		try {
			facade.beginTransaction();
			facade.addTuvAttribute(tuv.getDbPk(), name, value);
			facade.commit();
			tuv.appendAttribute(name, value);
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	/**
	 * 删除TU固定属性值 (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#deleteTuAttribute(java.lang.String,
	 *      net.heartsome.cat.common.bean.TmxTU, java.lang.String)
	 */
	public void deleteTuAttribute(String tuIdentifier, TmxTU tu, String name) {
		updateTuAttribute(tuIdentifier, tu, name, "");
	}

	@Override
	public void deleteTuAttribute(Map<String, TmxTU> tus, String name) {
		updateTuAttribute(tus, name, "");
	}

	public void updateTuvAttribute(String tuIdentifier, TmxSegement tuv, String name, String newValue) {
		name = getTuvAttrName(name);
		try {
			facade.beginTransaction();
			facade.updateTUVAttribute(tuv.getDbPk(), name, newValue);
			facade.commit();
			updateCacheTuvAttr(tuv, name, newValue);
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	/**
	 * 将界面的值转换成数据库中的字段值
	 * @param name
	 * @return ;
	 */
	private String getTuvAttrName(String name) {
		if ("creationid".equals(name)) {
			return "creationUser";
		} else if ("creationdate".equals(name)) {
			return "creationDate";
		} else if ("changedate".equals(name)) {
			return "changeDate";
		} else if ("changeid".equals(name)) {
			return "changeUser";
		} else if ("creationtool".equals(name)) {
			return "creationTool";
		} else if ("creationtoolversion".equals(name)) {
			return "creationToolVersion";
		}
		return name;
	}

	public void updateTuProp(String tuIdentifier, TmxTU tu, TmxProp prop, String propType, String newContent) {
		String Oldname = prop.getName();
		String oldValue = prop.getValue();
		try {
			prop.setName(propType);
			prop.setValue(newContent);
			facade.beginTransaction();
			facade.updateProp(tu.getTmId(), prop);
			facade.commit();
		} catch (SQLException e) {
			try {
				prop.setName(Oldname);
				prop.setValue(oldValue);
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);

		}
	}

	@Override
	public void updateTuProp(Map<String, TmxTU> tus, TmxProp prop, String propType, String newContent) {
		String Oldname = prop.getName();
		String oldValue = prop.getValue();
		try {
			facade.beginTransaction();
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				List<TmxProp> props = entry.getValue().getProps();
				if (props == null) {
					continue;
				}
				for (TmxProp p : props) {
					if (Oldname.equals(p.getName()) && oldValue.equals(p.getValue())) {
						p.setName(propType);
						p.setValue(newContent);
						facade.updateProp(entry.getValue().getTmId(), p);
					}
				}
			}
			facade.commit();
		} catch (SQLException e) {
			try {
				prop.setName(Oldname);
				prop.setValue(oldValue);
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);

		}
	}

	public void deleteTuProp(String tuIdentifier, TmxTU tu, TmxProp deleteProp) {
		try {
			facade.beginTransaction();
			facade.deleteTuProp(deleteProp.getDbPk());
			facade.commit();
			tu.getProps().remove(deleteProp);
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	@Override
	public void deleteTuPropByType(Map<String, TmxTU> tus, String type) {
		final String cleanType = TextUtil.cleanSpecialString(type);
		try {
			facade.beginTransaction();
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				TmxTU tu = entry.getValue();
				List<TmxProp> list = tu.getProps();
				if (list == null) {
					continue;
				}
				List<TmxProp> rmProps = new ArrayList<TmxProp>();
				for (TmxProp prop : list) {
					if (cleanType.equals(prop.getName())) {
						facade.deleteTuProp(prop.getDbPk());
						rmProps.add(prop);
					}
				}
				list.removeAll(rmProps);
			}
			facade.commit();
		} catch (SQLException e) {
			LOGGER.error("", e);
		}
	}
	
	@Override
	public void deleteTuProp(Map<String, TmxTU> tus, TmxProp deleteProp) {
		String Oldname = deleteProp.getName();
		String oldValue = deleteProp.getValue();
		try {
			facade.beginTransaction();
			List<TmxProp> rmProps = new LinkedList<TmxProp>();
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				List<TmxProp> props = entry.getValue().getProps();
				if (props == null) {
					continue;
				}
				for (TmxProp p : props) {
					if (Oldname.equals(p.getName()) && oldValue.equals(p.getValue())) {
						facade.deleteTuProp(p.getDbPk());
						rmProps.add(p);
					}
				}
				props.removeAll(rmProps);
				rmProps.clear();
			}
			facade.commit();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	public void deleteTuvAttribute(String tuIdentifier, TmxSegement tuv, String name) {
		updateTuvAttribute(tuIdentifier, tuv, name, "");
	}

	/**
	 * 更新tu固定属性 (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#updateTuAttribute(net.heartsome.cat.common.bean.TmxTU,
	 *      java.lang.String, java.lang.String)
	 */
	public void updateTuAttribute(String tuIdentifier, TmxTU tu, String name, String newValue) {
		try {
			facade.beginTransaction();
			TmxTU newTu = copyTu(tu);
			updateCacheTuAttr(newTu, name, newValue);
			facade.updateTuAttribute(newTu);
			facade.commit();
			updateCacheTuAttr(tu, name, newValue);
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	@Override
	public void updateTuAttribute(Map<String, TmxTU> tus, String name, String newValue) {
		try {
			facade.beginTransaction();
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				TmxTU newTu = copyTu(entry.getValue());
				updateCacheTuAttr(newTu, name, newValue);
				facade.updateTuAttribute(newTu);
				updateCacheTuAttr(entry.getValue(), name, newValue);
			}
			facade.commit();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		}
	}

	private TmxTU copyTu(TmxTU tu) {
		TmxTU oldTU = new TmxTU();
		oldTU.setAttributes(tu.getAttributes());
		oldTU.setChangeDate(tu.getChangeDate());
		oldTU.setChangeUser(tu.getChangeUser());
		oldTU.setContexts(tu.getContexts());
		oldTU.setCreationDate(tu.getCreationDate());
		oldTU.setCreationTool(tu.getCreationTool());
		oldTU.setCreationUser(tu.getCreationUser());
		oldTU.setNotes(tu.getNotes());
		oldTU.setProps(tu.getProps());
		oldTU.setSegments(tu.getSegments());
		oldTU.setSource(tu.getSource());
		oldTU.setTarget(tu.getTarget());
		oldTU.setTmId(tu.getTmId());
		oldTU.setTuId(tu.getTuId());
		return oldTU;
	}

	/**
	 * @return 包含在当显示的记录中
	 * @param tgtLangCode
	 **/
	public List<Integer> getTgtEmptyTU(String tgtLangCode) {
		try {
			return facade.getTgtEmptyTU(tgtLangCode, null,false);
		} catch (SQLException e) {
			LOGGER.error("", e);
			return new ArrayList<Integer>(0);
		}
	}

	/**
	 * 首选在缓存中取值，如果缓存中没有，则从物理存储中读取值，最后添加缓存中。
	 * @param tuIdentifier
	 **/
	public TmxTU getTuByIdentifier(String tuIdentifier) {
		try {
			TmxTU tmxTU = facade.getTuByIdentifier(Integer.parseInt(tuIdentifier), false);
			return tmxTU;
		} catch (SQLException e) {
			LOGGER.error("", e);
		}
		return new TmxTU();
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#deleteTgtEmpty(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean deleteTgtEmpty(IProgressMonitor monitor,boolean ignoreTag) {

		try {
			monitor.beginTask("", 100);
			monitor.setTaskName(Messages.getString("core.databaseAccess.filter.empty.taskname"));
			SubProgressMonitor subFilterTask = new SubProgressMonitor(monitor, 30);
			List<Integer> tgtEmptyTU = facade.getTgtEmptyTU(super.currTgtLang, subFilterTask, ignoreTag);
			SubProgressMonitor subDeleteTask = new SubProgressMonitor(monitor, 70);
			if (tgtEmptyTU.isEmpty()) {
				subDeleteTask.done();
				return false;
			}
			subDeleteTask.setTaskName(Messages.getString("core.databaseAccess.delete.empty.taskname"));
			facade.deleteTuByIds(tgtEmptyTU, currSrcLang, currTgtLang, null, subDeleteTask);
			monitor.done();
		} catch (SQLException e) {
			LOGGER.error("", e);

		} finally {
			monitor.done();
		}
		return false;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#deleteDupaicate(org.eclipse.core.runtime.IProgressMonitor,
	 *      boolean)
	 */
	@Override
	public boolean deleteDupaicate(IProgressMonitor monitor, boolean ignoreTag, boolean ignoreCase) {
		DatabaseAccessUtils deleteUtil = new DatabaseAccessUtils(facade, currSrcLang, currTgtLang, ignoreTag,
				ignoreCase);
		try {
			monitor.beginTask("", 100);
			SubProgressMonitor subFilerJob = new SubProgressMonitor(monitor, 40);
			subFilerJob.setTaskName(Messages.getString("core.fileAccess.filterDupliacteSegment"));
			List<Integer> tuids = deleteUtil.getId4DulicateDelete(subFilerJob);
			subFilerJob.done();
			SubProgressMonitor subdeleteJob = new SubProgressMonitor(monitor, 60);
			subdeleteJob.setTaskName(Messages.getString("core.fileAccess.deleteDuplicateSegment"));
			facade.deleteTuByIds(tuids, currSrcLang, currTgtLang, System.getProperty("user.name"), subdeleteJob);
			subdeleteJob.done();
		} catch (SQLException e) {
			LOGGER.error("", e);
		} finally {
			monitor.done();
		}
		return false;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#deleteSameSrcDiffTgt(org.eclipse.core.runtime.IProgressMonitor,
	 *      boolean)
	 */
	@Override
	public boolean deleteSameSrcDiffTgt(IProgressMonitor monitor, boolean ignoreTag, boolean ignoreCase) {
		DatabaseAccessUtils deleteUtil = new DatabaseAccessUtils(facade, currSrcLang, currTgtLang, ignoreTag,
				ignoreCase);
		try {
			SubProgressMonitor subFilerJob = new SubProgressMonitor(monitor, 40);
			subFilerJob.setTaskName(Messages.getString("core.fileAccess.filterSameSrcDiffTgtSegment"));
			List<Integer> tuids = deleteUtil.getId4SrcSameDiffTgtDelete(subFilerJob);
			SubProgressMonitor subdeleteJob = new SubProgressMonitor(monitor, 60);
			subdeleteJob.setTaskName(Messages.getString("core.fileAccess.deleteSameSrcDiffTgtSegment"));
			facade.deleteTuByIds(tuids, currSrcLang, currTgtLang, System.getProperty("user.name"), subdeleteJob);
		} catch (SQLException e) {
			LOGGER.error("", e);
		} finally {
			monitor.done();
		}
		return false;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#deleteEndsSpaces(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean deleteEndsSpaces(IProgressMonitor monitor) {
		try {
			facade.beginTransaction();
			facade.deleteEndsSpaces(monitor);
			facade.commit();
		} catch (SQLException e) {
			LOGGER.error("", e);
		} finally {
			monitor.done();
		}
		return false;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchUpdateTuAttr(org.eclipse.core.runtime.IProgressMonitor,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void batchUpdateTuAttr(IProgressMonitor monitor, String name, String value, String filter) {
		// TODO 批量修改TU的固定属性值
		monitor.beginTask("", 1);
		try {
			facade.beginTransaction();
			if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {// 应用到整个数据库
				if (facade.updateAllTuAttr(name, value)) {
					facade.commit();
				}
			} else {// 更新应用到当前过滤结果中
				List<Integer> list = new ArrayList<Integer>();
				for (String id : super.tuIdentifiers) {
					list.add(Integer.parseInt(id));
				}
				if (facade.updateFilterTuAttr(name, value, list)) {
					facade.commit();
				}

			}
			monitor.done();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);
		} finally {
			monitor.done();
		}

	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchDeleteTuAttr(org.eclipse.core.runtime.IProgressMonitor,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public void batchDeleteTuAttr(IProgressMonitor monitor, String name, String filter) {
		batchUpdateTuAttr(monitor, name, "", filter);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchAddTmxProp(org.eclipse.core.runtime.IProgressMonitor,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void batchAddTmxProp(IProgressMonitor monitor, String type, String content, String filter) {
		monitor.beginTask("", 100);
		SubProgressMonitor filterJob = new SubProgressMonitor(monitor, 30);
		filterJob.beginTask("", 100);
		List<Integer> ids = new ArrayList<Integer>();
		try {
			if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {// 应用到整个数据库
				ids = container.getDbOp().getAfterFilterTuPk(null, null, null);

			} else {// 应用到当前过滤结果
				for (String str : super.tuIdentifiers) {
					ids.add(Integer.parseInt(str));
				}
			}
			filterJob.done();
			SubProgressMonitor addJob = new SubProgressMonitor(monitor, 70);
			facade.beginTransaction();
			if (facade.batchAddTuProps(type, content, ids, addJob)) {
				facade.commit();
			}
			addJob.done();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e1);
			}
			LOGGER.error("", e);

		} finally {
			monitor.done();
		}

	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchUpdateTmxProp(org.eclipse.core.runtime.IProgressMonitor,
	 *      net.heartsome.cat.common.bean.TmxProp, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void batchUpdateTmxProp(IProgressMonitor monitor, TmxProp prop, String propType, String content,
			String filter) {
		monitor.beginTask("", 100);
		try {
			facade.beginTransaction();
			monitor.worked(20);
			if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {// 应用到整个数据库
				if (facade.batchUpdateTuProp(prop.getName(), prop.getValue(), propType, content, null)) {
					facade.commit();
				}

			} else {// 应用到当前过滤结果
				List<Integer> ids = new ArrayList<Integer>();
				for (String str : super.tuIdentifiers) {
					ids.add(Integer.parseInt(str));
				}
				if (facade.batchUpdateTuProp(prop.getName(), prop.getValue(), propType, content, ids)) {
					facade.commit();
				}
			}
			prop.setName(propType);
			prop.setValue(content);
			monitor.done();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e1);
			}
			LOGGER.error("", e);

		} finally {
			monitor.done();
		}

	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchDeleteTmxProp(org.eclipse.core.runtime.IProgressMonitor,
	 *      net.heartsome.cat.common.bean.TmxProp, java.lang.String)
	 */
	@Override
	public void batchDeleteTmxProp(IProgressMonitor monitor, TmxProp prop, String filter) {
		monitor.beginTask("", 100);
		try {
			facade.beginTransaction();
			monitor.worked(20);
			if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {// 应用到整个数据库
				if (facade.batchDeleteTuProp(prop.getName(), prop.getValue(), null)) {
					facade.commit();
				}

			} else {// 应用到当前过滤结果
				List<Integer> ids = new ArrayList<Integer>();
				for (String str : super.tuIdentifiers) {
					ids.add(Integer.parseInt(str));
				}
				if (facade.batchDeleteTuProp(prop.getName(), prop.getValue(), ids)) {
					facade.commit();
				}
			}
			monitor.done();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e1);
			}
			LOGGER.error("", e);

		} finally {
			monitor.done();
		}
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchUpdateTuvAttr(org.eclipse.core.runtime.IProgressMonitor,
	 *      java.lang.String, java.lang.String, java.util.List, java.lang.String)
	 */
	@Override
	public void batchUpdateTuvAttr(IProgressMonitor monitor, String name, String value, List<String> langs,
			String filter) {
		name = getTuvAttrName(name);
		try {
			facade.beginTransaction();
			if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {// 应用到整个数据库
				if (facade.batchUpdateTuvExAttribute(name, value, null, langs, monitor)) {
					facade.commit();
				}

			} else {// 应用到当前过滤结果
				List<Integer> ids = new ArrayList<Integer>();
				for (String str : super.tuIdentifiers) {
					ids.add(Integer.parseInt(str));
				}
				if (facade.batchUpdateTuvExAttribute(name, value, ids, langs, monitor)) {
					facade.commit();
				}
			}
			monitor.done();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e1);
			}
			LOGGER.error("", e);

		} finally {
			monitor.done();
		}

	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchDeleteTuvAttr(org.eclipse.core.runtime.IProgressMonitor,
	 *      java.lang.String, java.util.List, java.lang.String)
	 */
	@Override
	public void batchDeleteTuvAttr(IProgressMonitor monitor, String name, List<String> langs, String filter) {
		batchUpdateTuvAttr(monitor, name, "", langs, filter);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchAddTmxNote(org.eclipse.core.runtime.IProgressMonitor,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public void batchAddTmxNote(IProgressMonitor monitor, String content, String filter) {
		monitor.beginTask("", 100);
		SubProgressMonitor filterJob = new SubProgressMonitor(monitor, 30);
		filterJob.beginTask("", 100);
		List<Integer> ids = new ArrayList<Integer>();
		try {
			if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {// 应用到整个数据库
				ids = container.getDbOp().getAfterFilterTuPk(null, null, null);

			} else {// 应用到当前过滤结果
				for (String str : super.tuIdentifiers) {
					ids.add(Integer.parseInt(str));
				}
			}
			filterJob.done();
			SubProgressMonitor addJob = new SubProgressMonitor(monitor, 70);
			facade.beginTransaction();
			if (facade.batchAddTuNotes(content, ids, addJob)) {
				facade.commit();
			}
			addJob.done();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e1);
			}
			LOGGER.error("", e);

		} finally {
			monitor.done();
		}
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchUpdateTmxNote(org.eclipse.core.runtime.IProgressMonitor,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void batchUpdateTmxNote(IProgressMonitor monitor, String oldContent, String newContent, String filter) {
		monitor.beginTask("", 100);
		try {
			facade.beginTransaction();
			monitor.worked(20);
			if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {// 应用到整个数据库
				if (facade.batchUpdateTuNote(oldContent, newContent, null)) {
					facade.commit();
				}

			} else {// 应用到当前过滤结果
				List<Integer> ids = new ArrayList<Integer>();
				for (String str : super.tuIdentifiers) {
					ids.add(Integer.parseInt(str));
				}
				if (facade.batchUpdateTuNote(oldContent, newContent, ids)) {
					facade.commit();
				}
			}
			monitor.done();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e1);
			}
			LOGGER.error("", e);

		} finally {
			monitor.done();
		}
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#batchDeleteTmxNote(org.eclipse.core.runtime.IProgressMonitor,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public void batchDeleteTmxNote(IProgressMonitor monitor, String content, String filter) {

		monitor.beginTask("", 100);
		try {
			facade.beginTransaction();
			monitor.worked(20);
			if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {// 应用到整个数据库
				if (facade.batchDeleteTuNote(content, null)) {
					facade.commit();
				}

			} else {// 应用到当前过滤结果
				List<Integer> ids = new ArrayList<Integer>();
				for (String str : super.tuIdentifiers) {
					ids.add(Integer.parseInt(str));
				}
				if (facade.batchDeleteTuNote(content, ids)) {
					facade.commit();
				}
			}
			monitor.done();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e1);
			}
			LOGGER.error("", e);

		} finally {
			monitor.done();
		}
	}

	@Override
	public List<Integer> getDuplicatedTU(String srcLang, String tgtLang, boolean isIgnoreTag) {
		return null;

	}

	public List<Integer> searchText(String findStr, String lang, int startPk, boolean isForward,
			boolean isCaseSensitive, boolean isRegx) {
		DBOperator dbOp = container.getDbOp();
		if (dbOp == null) {
			return null;
		}
		DBConfig config = dbOp.getDbConfig();
		String sql = config.getOperateDbSQL("search-corcondance-groupid");

		if (isForward) {
			sql = sql.replace("__GROUP__", " GROUPID >= " + startPk);
		} else {
			sql = sql.replace("__GROUP__", " GROUPID <= " + startPk);
		}
		sql = dbOp.replaceTMOrTBConditionSql(sql, findStr, isCaseSensitive, isRegx, true, lang, null);

		try {
			Connection conn = dbOp.getConnection();
			Statement stm = conn.createStatement();
			ResultSet rs = stm.executeQuery(sql);
			List<Integer> tuPks = new ArrayList<Integer>();
			while (rs.next()) {
				tuPks.add(rs.getInt(1));
			}
			if (tuPks.size() == 0) {
				tuPks = null;
			}
			return tuPks;
		} catch (SQLException e) {
			LOGGER.error("", e);
		}
		return null;
	}

	@Override
	public List<Integer> getDuplicatedSrcDiffTgtTU(String srcLang, String tgtLang, boolean isIgnoreTag) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 数据库端，不需要保存 (non-Javadoc)
	 * @see net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSourceExist() {
		return false;
	}

	@Override
	public void beginQA(String srcLangCode, String tgtLangCode, boolean ignoreTag, boolean ignoreCase) {
		// do nothing
	}

	@Override
	public boolean isReadOnly() {
		return container.getDbOp().isReadOnly();
	}
	
	@Override
	public void updateTuPropType(Map<String, TmxTU> tus, TmxProp prop, String propType) {
		if (propType == null) {
			return;
		}
		propType = TextUtil.cleanSpecialString(propType);
		String Oldname = prop.getName();
		try {
			facade.beginTransaction();
			for (Entry<String, TmxTU> entry : tus.entrySet()) {
				List<TmxProp> props = entry.getValue().getProps();
				if (props == null) {
					continue;
				}
				for (TmxProp p : props) {
					if (Oldname.equals(p.getName())) {
						p.setName(propType);
						facade.updateProp(entry.getValue().getTmId(), p);
					}
				}
			}
			facade.commit();
		} catch (SQLException e) {
			try {
				prop.setName(Oldname);
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e);
			}
			LOGGER.error("", e);

		}
	}
	
	@Override
	public void batchUpdateTmxPropType(IProgressMonitor monitor, TmxProp prop, String propType, String filter) {
		if (propType == null) {
			return;
		}
		final String cleanPropType = TextUtil.cleanSpecialString(propType);
		monitor.beginTask("", 100);
		try {
			facade.beginTransaction();
			monitor.worked(20);
			if (TeCoreConstant.FILTERID_allSeg.equals(filter)) {// 应用到整个数据库
				if (facade.batchUpdateTuPropType(prop.getName(), cleanPropType, null)) {
					facade.commit();
				}

			} else {// 应用到当前过滤结果
				List<Integer> ids = new ArrayList<Integer>();
				for (String str : super.tuIdentifiers) {
					ids.add(Integer.parseInt(str));
				}
				if (facade.batchUpdateTuPropType(prop.getName(), cleanPropType, ids)) {
					facade.commit();
				}
			}
			monitor.done();
		} catch (SQLException e) {
			try {
				facade.rollback();
			} catch (SQLException e1) {
				LOGGER.error("", e1);
			}
			LOGGER.error("", e);

		} finally {
			monitor.done();
		}

	}
}
