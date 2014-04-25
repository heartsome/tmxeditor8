/**
 * DatabaseAccessUtils.java
 *
 * Version information :
 *
 * Date:2013-12-13
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.database.TMXDBOperatorFacade;
import net.heartsome.cat.te.core.bean.SimpleTUData;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yule
 * @version
 * @since JDK1.6
 */
public class DatabaseAccessUtils {

	private static Logger log = LoggerFactory.getLogger(DatabaseAccessUtils.class);

	private boolean ignoreTag;

	private boolean ignoreCase;

	private String srcLang;

	private String tgtLang;

	private TMXDBOperatorFacade accessTmxDb;

	public DatabaseAccessUtils() {

	}

	public DatabaseAccessUtils(TMXDBOperatorFacade accessTmxDb, String srcLang, String tgtLang, boolean ignoreTag,
			boolean ignoreCase) {
		this.accessTmxDb = accessTmxDb;
		this.srcLang = srcLang;
		this.tgtLang = tgtLang;
		this.ignoreTag = ignoreTag;
		this.ignoreCase = ignoreCase;

	}

	public List<SimpleTUData> getSimpleTuData(IProgressMonitor monitor) throws SQLException {
		Map<Integer, Map<String, String>> simpleTuDBDatas = accessTmxDb.getSimpleTuDBDatas(ignoreTag, srcLang, tgtLang);
		monitor.beginTask("", simpleTuDBDatas.size());
		Iterator<Map<String, String>> it = simpleTuDBDatas.values().iterator();
		List<SimpleTUData> tus = new ArrayList<SimpleTUData>();
		SimpleTUData tu = null;
		while (it.hasNext()) {
			monitor.worked(1);
			Map<String, String> tuData = it.next();
			tu = new SimpleTUData(tuData.get("SRC"), // 源文</br>
					tuData.get("TGT"),// 译文</br>
					tuData.get("TUPKID"),// id </br>
					tuData.get("CHANGE_DATE"));// 修改时间 </br>
			if (!tu.isEmpty()) {
				tus.add(tu);
			}

		}
		monitor.done();
		return tus;
	}

	public List<Integer> getId4DulicateDelete(IProgressMonitor monitor) throws SQLException {
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100);
		SubProgressMonitor subGetJob = new SubProgressMonitor(monitor, 20);
		List<SimpleTUData> allSimpleTus = getSimpleTuData(subGetJob);
		SubProgressMonitor subQuryJob = new SubProgressMonitor(monitor, 80);
		List<String> ids = DeleteTUHelper.queryDeleteDulicateTuId(allSimpleTus, subQuryJob, ignoreCase);
		return parseStrings2Integers(ids);
	}

	public List<Integer> getId4SrcSameDiffTgtDelete(IProgressMonitor monitor) throws SQLException {
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100);
		SubProgressMonitor subGetJob = new SubProgressMonitor(monitor, 20);
		List<SimpleTUData> allSimpleTus = getSimpleTuData(subGetJob);
		SubProgressMonitor subQuryJob = new SubProgressMonitor(monitor, 80);
		List<String> ids = DeleteTUHelper.queryDeleteSrcSameDiffTgtTuId(allSimpleTus, subQuryJob, ignoreCase);
		return parseStrings2Integers(ids);
	}

	private List<Integer> parseStrings2Integers(List<String> numberStrs) {
		List<Integer> ints = new ArrayList<Integer>();
		for (String num : numberStrs) {
			try {
				int parseInt = Integer.parseInt(num);
				ints.add(parseInt);
			} catch (NumberFormatException e) {
				log.info("", e);
				continue;
			}
		}
		return ints;
	}
}
