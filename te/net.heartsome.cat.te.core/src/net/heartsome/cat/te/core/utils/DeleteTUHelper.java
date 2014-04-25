/**
 * DeleteTUHelper.java
 *
 * Version information :
 *
 * Date:2013-12-12
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.te.core.bean.SimpleTUData;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * @author Yule
 * @version
 * @since JDK1.6
 */
public class DeleteTUHelper {

	public static void main(String[] args) {
		List<SimpleTUData> datas = new ArrayList<SimpleTUData>();
		SimpleTUData data1 = new SimpleTUData("2", "5", "1id", "");
		SimpleTUData data2 = new SimpleTUData("2", "2", "2id", "");
		SimpleTUData data3 = new SimpleTUData("2", "2", "3id", "");
		SimpleTUData data4 = new SimpleTUData("2", "3", "4id", "");
		SimpleTUData data5 = new SimpleTUData("2", "4", "5id", "");
		SimpleTUData data6 = new SimpleTUData("2", "5k", "6id", "");
		SimpleTUData data7 = new SimpleTUData("2", "5", "7id", "");
		datas.add(data1);
		datas.add(data2);
		datas.add(data3);
		datas.add(data4);
		datas.add(data5);
		datas.add(data6);
		datas.add(data7);
		sortDulicateTU(datas, true);
		List<List<SimpleTUData>> groupOrderedDiffTu = groupOrderedDiffTu(datas, null, true);
		for (List<SimpleTUData> tus : groupOrderedDiffTu){
			System.out.println();
			for (SimpleTUData data : tus)
				System.out.println(data.toString());
		}

	}

	/**
	 * @param orderdTus
	 * @param monitor
	 * @param ignoreCase
	 * @return ;
	 */
	public static List<String> queryDeleteDulicateTuId(List<SimpleTUData> orderdTus, IProgressMonitor monitor,
			boolean ignoreCase) {
		if (null == orderdTus || orderdTus.isEmpty()) {
			return new ArrayList<String>();
		}
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100);
		sortDulicateTU(orderdTus, ignoreCase);
		monitor.worked(60);
		SubProgressMonitor subOrderjob = new SubProgressMonitor(monitor, 20);
		List<List<SimpleTUData>> groupOrderedTu = groupOrderedTu(orderdTus, subOrderjob, ignoreCase);
		SubProgressMonitor subGetjob = new SubProgressMonitor(monitor, 20);
		subGetjob.beginTask("", groupOrderedTu.size());
		Iterator<List<SimpleTUData>> iterator = groupOrderedTu.iterator();
		List<String> tuDeleteIds = new ArrayList<String>();
		while (iterator.hasNext()) {
			subGetjob.worked(1);
			List<SimpleTUData> nextData = iterator.next();
			if (nextData.size() > 1) {
				tuDeleteIds.addAll(removeNotLastDateTu(nextData));
			}
		}
		subGetjob.done();
		return tuDeleteIds;
	}

	/**
	 * @param orderdTus
	 * @param monitor
	 * @param ignoreCase
	 * @return ;
	 */
	public static List<String> queryDeleteSrcSameDiffTgtTuId(List<SimpleTUData> orderdTus, IProgressMonitor monitor,
			boolean ignoreCase) {
		if (null == orderdTus || orderdTus.isEmpty()) {
			return new ArrayList<String>();
		}
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100);
		sortDulicateTU(orderdTus, ignoreCase);
		monitor.worked(60);
		SubProgressMonitor subOrderjob = new SubProgressMonitor(monitor, 20);
		List<List<SimpleTUData>> groupOrderedTu = groupOrderedDiffTu(orderdTus, subOrderjob, ignoreCase);
		SubProgressMonitor subGetjob = new SubProgressMonitor(monitor, 20);
		subGetjob.beginTask("", groupOrderedTu.size());
		Iterator<List<SimpleTUData>> iterator = groupOrderedTu.iterator();
		List<String> tuDeleteIds = new ArrayList<String>();
		while (iterator.hasNext()) {
			subGetjob.worked(1);
			List<SimpleTUData> nextData = iterator.next();
			if (nextData.size() > 1) {
				tuDeleteIds.addAll(removeNotLastDateTu(nextData));
			}
		}
		subGetjob.done();
		return tuDeleteIds;
	}

	/**
	 * 保留最新时间的一个TU ;
	 */
	public static List<String> removeNotLastDateTu(List<SimpleTUData> tuGroup) {
		if (null == tuGroup || tuGroup.isEmpty()) {
			return new ArrayList<String>();
		}
		int tuGroupSize = tuGroup.size();

		List<String> ids = new ArrayList<String>();
		if (1 == tuGroupSize) {
			return ids;
		}

		Iterator<SimpleTUData> iterator = tuGroup.iterator();
		while (iterator.hasNext()) {
			SimpleTUData next = iterator.next();
			String formatedTime = DateUtils.formatStringTime(next.getModifyTime());
			if (null == formatedTime) {
				ids.add(next.getTuId());
				iterator.remove();
			}
		}
		if (tuGroupSize == ids.size()) {
			ids.remove(0);
			return ids;
		}

		Collections.sort(tuGroup, new Comparator<SimpleTUData>() {
			@Override
			public int compare(SimpleTUData tuOne, SimpleTUData tuOther) {
				String modifyTimeOne = tuOne.getModifyTime();
				String modifyTimeOther = tuOther.getModifyTime();
				String firTime = DateUtils.formatStringTime(modifyTimeOne);
				String secTime = DateUtils.formatStringTime(modifyTimeOther);
				Date firdate = DateUtils.getDateFromUTC(firTime);
				Date secdate = DateUtils.getDateFromUTC(secTime);
				return firdate.compareTo(secdate);
			}

		});

		for (int i = 0; i < tuGroup.size() - 1; i++) {
			String tuId = tuGroup.get(i).getTuId();
			ids.add(tuId);
		}
		return ids;
	}

	private static void sortDulicateTU(List<SimpleTUData> datas, final boolean ignoreCase) {

		Collections.sort(datas, new Comparator<SimpleTUData>() {
			@Override
			public int compare(SimpleTUData tuOne, SimpleTUData tuOther) {
				String srcText1 = tuOne.getSrcText();
				String srcText2 = tuOther.getSrcText();
				String tgtText1 = tuOne.getTgtText();
				String tgtText2 = tuOther.getTgtText();
				if (ignoreCase) {
					int srcCmp = srcText1.compareToIgnoreCase(srcText2);
					if (0 == srcCmp) {
						return tgtText1.compareToIgnoreCase(tgtText2);
					} else {
						return srcCmp;
					}
				} else {
					int srcCmp = srcText1.compareTo(srcText2);
					if (0 == srcCmp) {
						return tgtText1.compareTo(tgtText2);
					} else {
						return srcCmp;
					}
				}
			}
		});
	}

	private static List<List<SimpleTUData>> groupOrderedDiffTu(List<SimpleTUData> orderdTus, IProgressMonitor monitor,
			boolean ignoreCase) {
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		List<List<SimpleTUData>> duplicateGroupTu = new ArrayList<List<SimpleTUData>>();
		List<SimpleTUData> oneTuGroup = new ArrayList<SimpleTUData>();
		int size = orderdTus.size();
		monitor.beginTask("", size);
		SimpleTUData currentTu = null;
		SimpleTUData preTu = null;
		for (int i = 0; i < size; i++) {
			if (monitor.isCanceled()) {
				return new ArrayList<List<SimpleTUData>>();
			}
			monitor.worked(1);
			if (i == 0) {
				oneTuGroup = new ArrayList<SimpleTUData>();
				currentTu = orderdTus.get(i);
				oneTuGroup.add(currentTu);
				duplicateGroupTu.add(oneTuGroup);
				continue;
			}
			preTu = currentTu;
			currentTu = orderdTus.get(i);
			if (currentTu.isSrcSameDiffTgtEquals(preTu, ignoreCase)) {
				oneTuGroup.add(currentTu);
			} else {
				oneTuGroup = new ArrayList<SimpleTUData>();
				oneTuGroup.add(currentTu);
				duplicateGroupTu.add(oneTuGroup);
			}

		}
		// 去掉重复TU
		Iterator<List<SimpleTUData>> iterator = duplicateGroupTu.iterator();
		while (iterator.hasNext()) {
			List<SimpleTUData> next = iterator.next();
			if (next.size() <= 1) {
				iterator.remove();
				continue;
			}
			List<List<SimpleTUData>> temp = groupOrderedTu(next, null, ignoreCase);
			for (List<SimpleTUData> tus : temp) {
				if (tus.size() <= 1) {
					continue;
				}
				for (SimpleTUData tu : tus) {
					next.remove(tu);
				}
			}

		}

		monitor.done();
		return duplicateGroupTu;
	}

	private static List<List<SimpleTUData>> groupOrderedTu(List<SimpleTUData> orderdTus, IProgressMonitor monitor,
			boolean ignoreCase) {
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		List<List<SimpleTUData>> duplicateGroupTu = new ArrayList<List<SimpleTUData>>();
		List<SimpleTUData> oneTuGroup = new ArrayList<SimpleTUData>();
		int size = orderdTus.size();
		monitor.beginTask("", size);
		SimpleTUData currentTu = null;
		SimpleTUData preTu = null;
		for (int i = 0; i < size; i++) {
			if (monitor.isCanceled()) {
				return new ArrayList<List<SimpleTUData>>();
			}
			monitor.worked(1);
			if (i == 0) {
				oneTuGroup = new ArrayList<SimpleTUData>();
				currentTu = orderdTus.get(i);
				oneTuGroup.add(currentTu);
				duplicateGroupTu.add(oneTuGroup);
				continue;
			}
			preTu = currentTu;
			currentTu = orderdTus.get(i);
			if (currentTu.isDulicateEquals(preTu, ignoreCase)) {
				oneTuGroup.add(currentTu);
			} else {
				oneTuGroup = new ArrayList<SimpleTUData>();
				oneTuGroup.add(currentTu);
				duplicateGroupTu.add(oneTuGroup);
			}

		}
		monitor.done();
		return duplicateGroupTu;
	}
}
