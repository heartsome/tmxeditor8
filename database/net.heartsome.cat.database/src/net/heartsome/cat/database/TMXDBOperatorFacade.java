/**
 * TMXDBOperatorFacade.java
 *
 * Version information :
 *
 * Date:2013-6-3
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.database.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * 注意该类操作数据的提交事务
 * @author yule
 * @version
 * @since JDK1.6
 */
public class TMXDBOperatorFacade {
	// ----------数据库里面对应列名字常量
	public static final String MTUPKID = "MTUPKID";

	public static final String TUID = "TUID";

	public static final String CREATIONID = "CREATIONID";

	public static final String CREATIONDATE = "CREATIONDATE";

	public static final String CHANGEID = "CHANGEID";

	public static final String CHANGEDATE = "CHANGEDATE";

	public static final String CREATIONTOOL = "CREATIONTOOL";

	public static final String CTVERSION = "CTVERSION";

	public static final String NNAME = "NNAME";

	public static final String CONTENT = "CONTENT";

	public static final String ELEMENT_TYPE = "E";

	public static final String ARRIBUTE_TYPE = "A";

	public static final String TEXTDATA_TM_TYPE = "M";

	public static final String TU_NAME = "TU";

	public static final String TUV_NAME = "TUV";

	public static final String HASH = "HASH";

	public static final String PURE = "PURE";

	private DBOperator dbOperator;
	/**
	 * TU的源语言与目标语言 langs[0]:源语言 langs[1]:目标语言
	 */
	private String[] langs;

	/**
	 * 如果没有设置语言,默认取lang表前两个语言对设置为默认语言
	 */
	private String[] defaultLangs;

	/**
	 * 
	 */
	public TMXDBOperatorFacade(DBOperator dbOperator) {

		this.dbOperator = dbOperator;
	}

	/**
	 * 查询数据库中的TU
	 * @param tuIdentifier
	 *            TU主键
	 * @return ; 一个完整的TU
	 * @throws SQLException
	 */
	public TmxTU getTuByIdentifier(int tuIdentifier, boolean isSetPure) throws SQLException {
		TmxTU tu = new TmxTU();
		// 1、查询TU的主要属性
		int mHeaderId = 0;
		Map<String, String> tuArributes = dbOperator.getTUByTuPkId(tuIdentifier);
		if (null == tuArributes) {
			return tu;
		}
		addTUNormalArributes(tu, tuArributes);
		mHeaderId = Integer.parseInt(tuArributes.get("MHPKID"));
		// 2、查询TU的附加属性
		Map<Integer, Map<String, String>> extraValue = dbOperator.getExtraValue(tuIdentifier, TU_NAME, ARRIBUTE_TYPE);
		addTUExArributes(tu, getTUExArributes(extraValue));

		// 3、查询TU的prop节点
		List<TmxProp> tuMprops = dbOperator.getTuMprops(tuIdentifier, TU_NAME);
		addTUProp(tu, tuMprops);

		// 4、查询TU的note节点
		List<TmxNote> tuMNote = dbOperator.getTuMNote(tuIdentifier, TU_NAME);
		addTUNotes(tu, tuMNote);

		// 5、查询TU的源语言文本和目标语言文本
		String[] lang = getLang(mHeaderId);
		Map<String, String> source = dbOperator.getTMXSegmentByIdAddLang(tuIdentifier, lang[0]);
		Map<String, String> tgt = dbOperator.getTMXSegmentByIdAddLang(tuIdentifier, lang[1]);
		TmxSegement sourceSeg = createTmxSegment(source);
		TmxSegement tgtSeg = null;
		if (tgt == null) {
			tgtSeg = new TmxSegement("", Utils.convertLangCode(lang[1]));
			tgtSeg.setDbPk(-1);
		} else {
			tgtSeg = createTmxSegment(tgt);
		}
		addTUContent(tu, sourceSeg, tgtSeg);
		return tu;
	}

	public TmxTU getTuByIdentifierAllLang(int tuIdentifier, boolean isSetPure) throws SQLException {
		TmxTU tu = new TmxTU();
		// 1、查询TU的主要属性
		Map<String, String> tuArributes = dbOperator.getTUByTuPkId(tuIdentifier);
		if (null == tuArributes) {
			return tu;
		}
		addTUNormalArributes(tu, tuArributes);
		// 2、查询TU的附加属性
		Map<Integer, Map<String, String>> extraValue = dbOperator.getExtraValue(tuIdentifier, TU_NAME, ARRIBUTE_TYPE);
		addTUExArributes(tu, getTUExArributes(extraValue));

		// 3、查询TU的prop节点
		List<TmxProp> tuMprops = dbOperator.getTuMprops(tuIdentifier, TU_NAME);
		addTUProp(tu, tuMprops);

		// 4、查询TU的note节点
		List<TmxNote> tuMNote = dbOperator.getTuMNote(tuIdentifier, TU_NAME);
		addTUNotes(tu, tuMNote);

		// 5、查询TU的源语言文本和目标语言文本
		List<String> allLangs = getAllLangs();
		if (allLangs.isEmpty()) {
			return tu;
		}
		for (String lang : allLangs) {
			Map<String, String> seg = dbOperator.getTMXSegmentByIdAddLang(tuIdentifier, lang);
			TmxSegement sourceSeg = createTmxSegment(seg);
			if (null == sourceSeg) {
				continue;
			}
			tu.appendSegement(sourceSeg);
		}

		return tu;
	}

	private List<String> langCache = null;

	private List<String> getAllLangs() throws SQLException {
		if (langCache != null) {
			return langCache;
		} else {
			langCache = dbOperator.getLanguages();
		}
		return langCache;
	}

	/**
	 * 得到TU的扩展属性值
	 * @param extraValue
	 * @return ;
	 */
	private Map<String, String> getTUExArributes(Map<Integer, Map<String, String>> extraValue) {
		Collection<Map<String, String>> values = extraValue.values();
		if (null == values || values.isEmpty()) {
			return null;
		}
		Iterator<Map<String, String>> iterator = values.iterator();
		Map<String, String> rs = new HashMap<String, String>(5);
		while (iterator.hasNext()) {
			Map<String, String> next = iterator.next();
			rs.put(next.get(NNAME), next.get(CONTENT));
		}
		return rs;
	}

	/**
	 * 添加TU的一般属性
	 * @param tu
	 *            需要修改的TU
	 * @param tuArributes
	 *            tu的属性值
	 * @return ; 参数的tu（修改过后的值）
	 */
	private TmxTU addTUNormalArributes(TmxTU tu, Map<String, String> tuArributes) {

		if (null == tu || null == tuArributes || tuArributes.isEmpty()) {
			return tu;
		}

		tu.setTmId(Integer.parseInt(tuArributes.get(MTUPKID)));
		tu.setTuId(tuArributes.get(TUID));

		tu.setCreationUser(tuArributes.get(CREATIONID));
		tu.setCreationDate(DateUtils.formatStringTime(tuArributes.get(CREATIONDATE)));

		tu.setChangeUser(tuArributes.get(CHANGEID));
		tu.setChangeDate(DateUtils.formatStringTime(tuArributes.get(CHANGEDATE)));

		tu.setCreationTool(tuArributes.get(CREATIONTOOL));
		tu.setCreationToolVersion(tuArributes.get(CTVERSION));

		return tu;
	}

	/**
	 * 添加TU的扩展属性
	 * @param tu
	 * @param tuArributes
	 * @return ;
	 */
	private TmxTU addTUExArributes(TmxTU tu, Map<String, String> tuArributes) {
		if (null == tu || null == tuArributes || tuArributes.isEmpty()) {
			return tu;
		}
		tu.setAttributes(tuArributes);
		return tu;
	}

	/**
	 * 添加TU下面PROP 节点
	 * @param tu
	 * @param props
	 * @return ;
	 */
	private TmxTU addTUProp(TmxTU tu, List<TmxProp> props) {
		if (null == tu || null == props || props.isEmpty()) {
			return tu;
		}
		tu.setProps(props);
		return tu;
	}

	/**
	 * 添加TU下面note节点
	 * @param tu
	 * @param notes
	 * @return ;
	 */
	private TmxTU addTUNotes(TmxTU tu, List<TmxNote> notes) {
		if (null == tu || null == notes || notes.isEmpty()) {
			return tu;
		}
		tu.setNotes(notes);
		return tu;
	}

	/**
	 * 添加TU的源语言和目标语言对
	 * @param tu
	 * @param source
	 * @param target
	 * @return ;
	 */
	private TmxTU addTUContent(TmxTU tu, TmxSegement source, TmxSegement target) {
		if (null == tu) {
			return tu;
		}
		if (null != source) {
			tu.setSource(source);
		}
		if (null != target) {
			tu.setTarget(target);
		}
		return tu;
	}

	/**
	 * 创建一个TUV ,只设置了纯文本和全部文本以及hashcode的值。
	 * @param segment
	 * @return ;
	 * @throws SQLException
	 */
	private TmxSegement createTmxSegment(Map<String, String> segment) throws SQLException {
		if (null == segment || segment.isEmpty()) {
			return null;
		}
		TmxSegement tmxSegment = new TmxSegement(segment.get(CONTENT) == null ? "" : segment.get(CONTENT),
				segment.get("LANG"));
		tmxSegment.setDbPk(Integer.parseInt(segment.get("TPKID")));
		Map<Integer, Map<String, String>> extraValue = dbOperator.getExtraValue(tmxSegment.getDbPk(), TUV_NAME,
				ARRIBUTE_TYPE);
		tmxSegment.setAttributes(getTUExArributes(extraValue));
		return tmxSegment;
	}

	/**
	 * @param mnpkid
	 *            TU的headerID
	 * @return ;
	 * @throws SQLException
	 */
	private String[] getLang(int mnpkid) throws SQLException {
		// 首先选择用户设置的语言
		if (langs != null) {
			return langs;
		}

		if (defaultLangs != null) {
			return defaultLangs;
		}
		// 从HEAD 中获取源语言和目标语言
		String temp[] = new String[2];
		defaultLangs = temp;
		Map<String, String> headerinfoById = dbOperator.getHeaderinfoById(mnpkid);
		if (null != headerinfoById && !headerinfoById.isEmpty()) {
			temp[0] = headerinfoById.get("SRCLANG");
			temp[1] = headerinfoById.get("ADMINLANG");
			return temp;

		}
		// 当从header中获取不到语言对时，默认取Lang表中前两个语言。
		List<String> languages = dbOperator.getLanguages();
		temp[0] = languages.get(0);
		temp[1] = languages.get(1);
		return temp;
	}

	/**
	 * 当从数据库中获取不到源语言和目标语言时，可以设置源语言和目标语言就行获取
	 * @param srcLang
	 * @param tgtLang
	 *            ;
	 */
	public void setLangs(String srcLang, String tgtLang) {
		langs = new String[2];
		langs[0] = srcLang;
		langs[1] = tgtLang;
	}

	/**
	 * 添加一个TU到数据库
	 * @param tu
	 * @throws SQLException
	 *             ;
	 */
	public int insertTu(TmxTU tu) throws SQLException {
		// 1、 添加数据到MTU表中
		// TODO :tu属性时间
		int insertTU = dbOperator.insertTU(0, tu.getTuId(), tu.getCreationUser(), tu.getCreationDate(),
				tu.getChangeUser(), tu.getChangeDate(), tu.getCreationTool(), tu.getCreationToolVersion(), null, null,
				null);
		// need to query from database
		String tuNewID = String.valueOf(insertTU);
		// 2、添加数据到mextra中
		Map<String, String> attributes = tu.getAttributes();
		if (attributes != null && !attributes.isEmpty()) {
			Set<Entry<String, String>> entrySet = attributes.entrySet();
			Iterator<Entry<String, String>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Entry<String, String> next = iterator.next();
				dbOperator.insertTMXExtra(ARRIBUTE_TYPE, next.getKey(), next.getValue(), TU_NAME, tuNewID);
			}
		}

		// 3、添加数据到TMXprop中
		List<TmxProp> props = tu.getProps();
		if (null != props && !props.isEmpty()) {
			for (TmxProp prop : props) {
				dbOperator.insertTMXProp(insertTU, "TU", prop.getName(), null, null, prop.getValue());
			}
		}
		// 4 、添加数据到note中
		List<TmxNote> notes = tu.getNotes();
		if (null != notes && !notes.isEmpty()) {
			for (TmxNote note : notes) {
				dbOperator.insertTMXNote(insertTU, "TU", note.getContent(), null, null, null, null, note.getEncoding(),
						note.getXmlLang());
			}
		}

		// 5、添加数据到1、textdata中,2、更新lang表，3、更新语言片段表
		TmxSegement source = tu.getSource();
		int groupId = insertTU;
		if (source != null) {
			dbOperator
					.insertTextData("M", groupId, String.valueOf(source.getPureText().hashCode()),
							source.getPureText(), source.getFullText(), Utils.convertLangCode(source.getLangCode()),
							null, null);
		}
		TmxSegement target = tu.getTarget();
		if (target != null) {
			dbOperator
					.insertTextData("M", groupId, String.valueOf(target.getPureText().hashCode()),
							target.getPureText(), target.getFullText(), Utils.convertLangCode(target.getLangCode()),
							null, null);
		}
		List<TmxSegement> segments = tu.getSegments();
		if (segments != null && !segments.isEmpty()) {
			for (TmxSegement seg : segments) {
				dbOperator.insertTextData("M", groupId, String.valueOf(seg.getPureText().hashCode()),
						seg.getPureText(), seg.getFullText(), Utils.convertLangCode(seg.getLangCode()), null, null);
			}

		}

		return insertTU;
	}

	/**
	 * 删除TU
	 * @param tu
	 * @throws SQLException
	 *             ;
	 */
	public void deleteTu(TmxTU tu, String changeUser) throws SQLException {
		int tmId = tu.getTmId();
		String srcLang = tu.getSource().getLangCode();
		String targetLang = tu.getTarget().getLangCode();
		// 0、可能删除lang表中的数据
		if (dbOperator.getLangCountInTextData(srcLang) == 1) {
			dbOperator.deleteLang(srcLang);
		}
		if (dbOperator.getLangCountInTextData(targetLang) == 1) {
			dbOperator.deleteLang(targetLang);
		}
		List<Integer> list = new ArrayList<Integer>(3);

		// 1、删除textdata表中的 数据删除TUV相关的数据
		Map<String, String> textDataValue = dbOperator.getTextDataValue("M", tmId, srcLang);
		list.add(Integer.parseInt(textDataValue.get("TPKID")));
		dbOperator.deleteAllTuvRelations(list, srcLang);
		list.clear();

		textDataValue = dbOperator.getTextDataValue("M", tmId, targetLang);
		list.add(Integer.parseInt(textDataValue.get("TPKID")));
		dbOperator.deleteAllTuvRelations(list, targetLang);

		// 如果只包含两个语言对，删除一个TU节点
		// 如果TU包含两个以上的语言对，只删除目标语言和源语言（tuv）
		if (tu.getSegments() == null || tu.getSegments().isEmpty()) {
			// 4、删除TU
			dbOperator.deleteTU(tmId);
			// 5、删除notes
			dbOperator.deleteTuNote(tmId, "TU");
			// 6、删除prop
			dbOperator.deleteTuProp(tmId, "TU");
			// 7、删除扩展属性
			dbOperator.deleteTuExtra(tmId, "TU");

		} else {
			// 更新TU的修改时间和修改用户
			updateTuInfo(tmId, changeUser, null);
		}
	}

	/**
	 * 更新TUV的内容
	 * @param groupId
	 *            TUV的组ID
	 * @param newSegment
	 * @throws SQLException
	 *             ;
	 */
	public void updateTUVContent(int groupId, TmxSegement newSegment) throws SQLException {
		int tuvId = newSegment.getDbPk();
		if (tuvId == -1) {
			String id = dbOperator.insertTextData("M", groupId, String.valueOf(newSegment.getPureText().hashCode()),
					newSegment.getPureText(), newSegment.getFullText(),
					Utils.convertLangCode(newSegment.getLangCode()), null, null);
			newSegment.setDbPk(Integer.parseInt(id));
			return;
		}
		dbOperator.updateTUVContent(tuvId, String.valueOf(newSegment.getPureText().hashCode()),
				newSegment.getPureText(), newSegment.getFullText());
		// 更新matrix_lang表,目前暂时没有更新matrix_lang的数据
		dbOperator.updateMatrix_lang(newSegment.getLangCode(), tuvId, newSegment.getPureText());
	}

	/**
	 * 只更新MTU表
	 * @param newTu
	 *            ;
	 * @throws SQLException
	 */
	public void updateTU(TmxTU newTu) throws SQLException {
		Map<String, String> params = new HashMap<String, String>();
		params.put(MTUPKID, String.valueOf(newTu.getTmId()));
		params.put(TUID, newTu.getTuId());
		params.put(CREATIONID, newTu.getCreationUser());
		params.put(CREATIONDATE, newTu.getCreationDate());
		params.put(CHANGEID, newTu.getChangeUser());
		params.put(CHANGEDATE, newTu.getChangeDate());
		params.put(CREATIONTOOL, newTu.getCreationTool());
		params.put(CTVERSION, newTu.getCreationToolVersion());
		dbOperator.updateTU(params);
	}

	/**
	 * 添加TU的note
	 * @param tuId
	 * @param content
	 * @return ;
	 * @throws SQLException
	 */
	public int addTuNote(int tuId, TmxNote newNote) throws SQLException {
		int addMNoteId = dbOperator.addMNote(TU_NAME, tuId, newNote.getContent(), newNote.getEncoding(),
				newNote.getXmlLang());
		return addMNoteId;
	}

	/**
	 * 删除note
	 * @param noteid
	 * @throws SQLException
	 *             ;
	 */
	public void deleteTuNote(int noteid) throws SQLException {
		dbOperator.deleteMNote(noteid);
	}

	/**
	 * 更新note里面的所有字段
	 * @param tuId
	 * @param newNote
	 *            ;
	 * @throws SQLException
	 */
	public void updateNote(int tuId, TmxNote newNote) throws SQLException {
		int dbPk = newNote.getDbPk();
		String content = newNote.getContent();
		String encoding = newNote.getEncoding();
		String xmlLang = newNote.getXmlLang();
		dbOperator.updateMNote(dbPk, encoding, xmlLang, content);
		dbOperator.updateTUChangeDate(tuId, CommonFunction.retTMXDate());

	}

	/**
	 * 添加TU的prop
	 * @param tu
	 * @param propType
	 * @param newContent
	 * @return ;
	 * @throws SQLException
	 */
	public int addTuProp(int tuId, TmxProp newProp) throws SQLException {
		int rs = dbOperator.addMProp(TU_NAME, tuId, newProp.getName(), newProp.getValue());
		return rs;

	}

	/**
	 * 删除TU的prop节点
	 * @param propId
	 * @throws SQLException
	 *             ;
	 */
	public void deleteTuProp(int propId) throws SQLException {
		dbOperator.deleteMProp(propId);
	}

	/**
	 * 更新TmxProp里面包含的所有的字段
	 * @param tuId
	 * @param newTmx
	 *            ;
	 * @throws SQLException
	 */
	public void updateProp(int tuId, TmxProp newTmx) throws SQLException {
		int id = newTmx.getDbPk();
		String pName = newTmx.getName();
		String content = newTmx.getValue();
		dbOperator.updateMProp(id, pName, content);
		dbOperator.updateTUChangeDate(tuId, CommonFunction.retTMXDate());

	}

	/**
	 * 添加TU的扩展属性
	 * @param tu
	 * @param name
	 * @param value
	 *            ;
	 * @throws SQLException
	 */
	public void addTuAttribute(int tuId, String name, String value) throws SQLException {
		dbOperator.addExAttribute(TU_NAME, tuId, name, value);
	}

	/**
	 * 删除TU的属性
	 * @param tuId
	 * @param name
	 * @throws SQLException
	 *             ;
	 */
	public void deleteTuAttribute(int tuId, String name) throws SQLException {
		dbOperator.deleteExAttribute(tuId, TU_NAME, name);
	}

	/**
	 * 修改属性值
	 * @param tuId
	 * @param name
	 * @param newvalue
	 *            ;
	 * @throws SQLException
	 */
	public void updateTuAttribute(TmxTU newTU) throws SQLException {
		updateTU(newTU);
	}

	/**
	 * 添加TUV的扩展属性
	 * @param tu
	 * @param tuv
	 * @param name
	 * @param value
	 *            ;
	 * @throws SQLException
	 */
	public void addTuvAttribute(int tuvId, String name, String value) throws SQLException {
		dbOperator.addExAttribute(TUV_NAME, tuvId, name, value);
	}

	/**
	 * 删除TUV的属性
	 * @param tuvId
	 * @param name
	 * @throws SQLException
	 *             ;
	 */
	public void deleteTuvAttibute(int tuvId, String name) throws SQLException {
		dbOperator.deleteExAttribute(tuvId, TUV_NAME, name);
	}

	/**
	 * 更新TUV的固定属性
	 * @param tuvId
	 * @param name
	 * @param newValue
	 * @throws SQLException
	 *             ;
	 */
	public void updateTUVAttribute(int tuvId, String name, String newValue) throws SQLException {
		boolean updateExAttribute = dbOperator.updateExAttribute(tuvId, TUV_NAME, name, newValue);
		if (!updateExAttribute) {
			dbOperator.addExAttribute(TUV_NAME, tuvId, name, newValue);
		}
	}

	/**
	 * 得到TUV中纯文本为空的TUID
	 * @param tgtLangCode
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public List<Integer> getTgtEmptyTU(String tgtLangCode, IProgressMonitor monitor,boolean ignoreTag) throws SQLException {
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		return dbOperator.getEmptyPure(tgtLangCode, monitor,ignoreTag);
	}

	/**
	 * 提交事务
	 * @throws SQLException
	 *             ;
	 */
	public void commit() throws SQLException {
		dbOperator.commit();
	}

	/**
	 * 回滚事务
	 * @throws SQLException
	 *             ;
	 */
	public void rollback() throws SQLException {
		dbOperator.rollBack();
	}

	/**
	 * 设置事务开始
	 * @throws SQLException
	 *             ;
	 */
	public void beginTransaction() throws SQLException {
		dbOperator.beginTransaction();
	}

	/**
	 * 连接数据库
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 *             ;
	 */
	public void start() throws SQLException, ClassNotFoundException {
		if (dbOperator.getConnection() != null) {
			return;
		}
		dbOperator.start();
	}

	/**
	 * 释放数据库资源
	 * @throws SQLException
	 *             ;
	 */
	public void end() throws SQLException {
		if (dbOperator.isClosed()) {
			return;
		}
		dbOperator.end();
	}

	/**
	 * 更新TU的信息
	 * @param id
	 *            TU在数据库中的ID
	 * @param changeUser
	 *            如果changeUser==null,不更新。如果changeUser!=null,更新changeUser
	 * @param dateTime
	 *            如果dateTime==null,使用系统当前默认值更新。如果changeUser!=null,使用参数值更新 dateTime格式为：xxxxxxxTxxxxZ的格式
	 * @throws SQLException
	 *             ;
	 */
	public void updateTuInfo(int id, String changeUser, String dateTime) throws SQLException {
		if (null != dateTime) {
			dbOperator.updateTUChangeDate(id, dateTime);
		} else {
			dbOperator.updateTUChangeDateUseDefault(id);
		}
		if (null != changeUser) {
			dbOperator.updateTUChangeUser(id, changeUser);
		}

	}

	/**
	 * 批量删除TU，该方法内部进行了事务处理
	 * @param tuids
	 *            ：需要处理的TU的所有ID值
	 * @param srcCode
	 *            ：源语言代码
	 * @param tgtCode
	 *            ：目标语言代码
	 * @param user
	 *            ：修改者
	 * @param monitor
	 *            :monitor==null ,不要进度条
	 * @throws SQLException
	 *             ;
	 */
	public void deleteTuByIds(List<Integer> tuids, String srcCode, String tgtCode, String user, IProgressMonitor monitor)
			throws SQLException {

		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 45);
		List<Integer> tupkids4DeleteTus = dbOperator.getTupkids4DeleteTus(tuids);
		monitor.worked(3);
		List<Integer> tupkids4DeleteTuv = dbOperator.getTupkids4DeleteTuv(tuids, tupkids4DeleteTus);
		monitor.worked(1);
		List<Integer> tuvpkids = dbOperator.getTuvIdsByTuId(tupkids4DeleteTus);
		List<Integer> tuvpkids_signle = dbOperator.getTuvIdsByTuIdAndTgt(tupkids4DeleteTuv, tgtCode);
		monitor.worked(2);
		dbOperator.beginTransaction();
		// ---------------------A、删除只有两个语言对的情况
		// 1、删除TU表中的数据<br/>
		dbOperator.deleteTUs(tupkids4DeleteTus);
		monitor.worked(5);
		// 2、删除TU的扩展属性（A，E）两种类型<br/>
		dbOperator.deleteExValues(tupkids4DeleteTus, TU_NAME);
		monitor.worked(5);
		// 3、删除TU的note节点<br/>
		dbOperator.deleteNotes(tupkids4DeleteTus, TU_NAME);
		// 4、删除TU的prop节点：只定义属性 <br/>
		monitor.worked(5);
		dbOperator.deleteProps(tupkids4DeleteTus, TU_NAME);
		monitor.worked(5);

		// 5、删除TUV表,包含了删除语言片段表
		dbOperator.deleteTuvMatrixLang(tuvpkids, srcCode, tgtCode);
		monitor.worked(5);
		dbOperator.deleteExValues(tuvpkids, TUV_NAME);
		// 3、删除TU的note节点<br/>
		monitor.worked(5);
		dbOperator.deleteNotes(tuvpkids, TUV_NAME);
		monitor.worked(5);
		// 4、删除TU的prop节点：只定义属性 <br/>
		dbOperator.deleteProps(tuvpkids, TUV_NAME);
		monitor.worked(5);
		// ---------------------B、删除有3个以上语言对的情况
		if (null != tupkids4DeleteTuv && !tupkids4DeleteTuv.isEmpty()) {
			dbOperator.deleteTuvMatrixLang(tuvpkids_signle, srcCode, tgtCode);
			dbOperator.deleteExValues(tuvpkids_signle, TUV_NAME);
			dbOperator.deleteNotes(tuvpkids_signle, TUV_NAME);
			dbOperator.deleteProps(tuvpkids_signle, TUV_NAME);
			for (int id : tupkids4DeleteTuv) {
				updateTuInfo(id, user, null);
			}
		}

		// 删除语言
		if (dbOperator.getLangCountInTextData(srcCode) == 0) {
			dbOperator.deleteLang(srcCode);
		}
		if (dbOperator.getLangCountInTextData(tgtCode) == 0) {
			dbOperator.deleteLang(tgtCode);
		}
		dbOperator.commit();
		monitor.done();
	}

	/**
	 * @param ignoreTag
	 * @param srcCode
	 * @param tgtCode
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public Map<Integer, Map<String, String>> getSimpleTuDBDatas(boolean ignoreTag, String srcCode, String tgtCode)
			throws SQLException {
		return dbOperator.getSrcTgtGrouped(ignoreTag, srcCode, tgtCode);
	}

	public void deleteEndsSpaces(IProgressMonitor monitor) throws SQLException {
		monitor.beginTask("", 1);
		monitor.setTaskName(Messages.getString("net.heartsome.cat.database.TMXDBOperatorFacade.deleteEndsSpace"));
		dbOperator.deleteTUVContentEndsSpace();
		monitor.done();
	}

	public boolean updateAllTuAttr(String name, String newValue) throws SQLException {
		String tuColumn = getTuColumn(name);
		if (null == tuColumn) {
			return false;
		}
		return dbOperator.updateTuAttrByColumn(tuColumn, newValue);
	}

	public boolean updateFilterTuAttr(String name, String newValue, List<Integer> tupkids) throws SQLException {
		String tuColumn = getTuColumn(name);
		if (null == tuColumn) {
			return false;
		}
		return dbOperator.updateTuAttrByColumnAndIds(tuColumn, newValue, tupkids);
	}

	public boolean batchAddTuProps(String paname, String content, List<Integer> ids, IProgressMonitor monitor)
			throws SQLException {
		return dbOperator.addTuRropByTuIds(TU_NAME, paname, content, ids, monitor);
	}

	public boolean batchAddTuNotes(String content, List<Integer> tuids, IProgressMonitor monitor) throws SQLException {
		return dbOperator.addTuNotesByTuIds(TU_NAME, content, tuids, monitor);
	}

	public String getTuColumn(String name) {
		if ("creationid".equals(name)) {
			return CREATIONID;
		} else if ("creationdate".equals(name)) {
			return CREATIONDATE;
		} else if ("changedate".equals(name)) {
			return CHANGEDATE;
		} else if ("changeid".equals(name)) {
			return CHANGEID;
		} else if ("creationtool".equals(name)) {
			return CREATIONTOOL;
		} else if ("creationtoolversion".equals(name)) {
			return CTVERSION;
		} else if ("tuid".equals(name)) {
			return TUID;
		}
		return null;
	}

	public boolean batchUpdateTuProp(String oldPname, String oldContent, String newPname, String newContent,
			List<Integer> ids) throws SQLException {
		if (null == ids) {
			return dbOperator.updateAllProp(oldPname, oldContent, newPname, newContent);
		} else {
			return dbOperator.updatePropByIds(oldPname, oldContent, newPname, newContent, ids);
		}
	}
	
	public boolean batchUpdateTuPropType(String oldPname, String newPname, List<Integer> ids) throws SQLException {
		if (null == ids) {
			return dbOperator.updateAllPropType(oldPname, newPname);
		} else {
			return dbOperator.updatePropTypeByIds(oldPname, newPname, ids);
		}
	}

	public boolean batchUpdateTuNote(String oldContent, String newContent, List<Integer> ids) throws SQLException {
		if (null == ids) {
			return dbOperator.updateAllNotes(oldContent, newContent);
		} else {
			return dbOperator.updateNotesByIds(oldContent, newContent, ids);
		}
	}

	public boolean batchDeleteTuProp(String oldPname, String oldContent, List<Integer> ids) throws SQLException {
		if (null == ids) {
			return dbOperator.deleteAllProp(oldPname, oldContent);
		} else {
			return dbOperator.deletePropByIds(oldPname, oldContent, ids);
		}
	}

	public boolean batchDeleteTuNote(String oldContent, List<Integer> ids) throws SQLException {
		if (null == ids) {
			return dbOperator.deleteAllNote(oldContent);
		} else {
			return dbOperator.deleteNotesByIds(oldContent, ids);
		}
	}

	/**
	 * 批量更新TUv的固定属性
	 * @param name
	 * @param content
	 * @param filterTuIds
	 * @param langs
	 * @param monitor
	 * @return
	 * @throws SQLException
	 *             ;
	 */
	public boolean batchUpdateTuvExAttribute(String name, String content, List<Integer> filterTuIds,
			List<String> langs, IProgressMonitor monitor) throws SQLException {
		boolean dataBaseModifyed = false;
		List<Integer> tuvIdsByLangs = null;
		monitor.beginTask("", 100);
		if (null == filterTuIds) { // 更新整个数据库
			tuvIdsByLangs = dbOperator.getTuvIdsByLangs(langs);
		} else {
			tuvIdsByLangs = dbOperator.getTuvIdsByLangsAndTuids(langs, filterTuIds);
		}
		monitor.worked(20);
		// 含有固定属性的TEXTRA_ID
		List<Integer> attrIdsByName = dbOperator.getAttrIdsByName(name, tuvIdsByLangs);
		monitor.worked(5);
		if (dbOperator.deleteExAttributes(attrIdsByName, name, content)) {
			dataBaseModifyed = true;
		}
		monitor.worked(30);
		// 新增固定的属性
		SubProgressMonitor addJob = new SubProgressMonitor(monitor, 45);
		if (dbOperator.addExAttributes(TUV_NAME, tuvIdsByLangs, name, content, addJob)) {
			dataBaseModifyed = true;
		}
		monitor.done();
		return dataBaseModifyed;
	}
}
