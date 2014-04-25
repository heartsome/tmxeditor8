package net.heartsome.cat.te.core.qa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.heartsome.cat.common.util.TextUtil;

/**
 * 译文相同，但源文不同
 * @author  robert	2013-09-16
 * @version 
 * @since   JDK1.6
 */
public class TgtSameButSrcQA extends QARealization {
	boolean isFilter = false;
	private Map<Set<String>, List<QATUDataBean>> dataMap = null;
	
	@Override
	public void beginTmxQA(QATUDataBean tu) {
		if (dataMap == null) {
			initData();
		}
		
		String tuId = tu.getTuID();
		
		String qaType = QAConstant.QA_TgtSameButSrc;
		String qaTypeText = QAConstant.QA_TgtSameButSrcText;
		Set<String> deleteSetKey = null;
		filterFor:for (Set<String> tuSet : dataMap.keySet()) {
			if (tuSet.contains(tuId)) {
				deleteSetKey = tuSet;
				String mergeID = TextUtil.createUUID();
				
				for(QATUDataBean curTUBean : dataMap.get(tuSet)){
					super.printResult(new QAResultBean(curTUBean.getTuID(), qaType, curTUBean.getLineNumber(), qaTypeText, mergeID, curTUBean.getSrcContent(), curTUBean.getTgtContent()));
				}
				dataMap.remove(deleteSetKey);
				break filterFor;
			}
		}
	}

	@Override
	public void beginDBQA() {
		
	}
	
	
	
	private void initData(){
		dataMap = new HashMap<Set<String>, List<QATUDataBean>>();
		
//		long time1 = System.currentTimeMillis();
		final boolean ignoreTag = model.isIgnoreTag();
		final boolean ignoreCase = model.isIgnoreCase();
		
		try {
			List<QATUDataBean> dataTempList = QACommonFuction.getAllTuDataOfTmxList(model);
			List<QATUDataBean> dataList = new ArrayList<QATUDataBean>();
			// 清除掉译文为空的文本
			// 这里的译文不可能为　null，因为 getAllTuDataOfTmxList 方法已经做了限制,这里主要是为了排除译文全为空，此时会报很多译文相同源文不同的情况。
			QATUDataBean tempBean = null;
			for (int i = 0; i < dataTempList.size(); i++) {
				tempBean = dataTempList.get(i);
				String tgtText = ignoreTag ? tempBean.getTgtPureText() : tempBean.getTgtContent();
				tgtText = TextUtil.trimString(tgtText);
				if (!tgtText.isEmpty()) {
					dataList.add(tempBean);
				}
			}
			
//			System.out.println("获取所有数据　tu 耗时：" + (System.currentTimeMillis() - time1));
//			long time2 = System.currentTimeMillis();
			
			Collections.sort(dataList, new Comparator<QATUDataBean>() {
				@Override
				public int compare(QATUDataBean tu1, QATUDataBean tu2) {
					// UNDO 这里 ts 的品质检查也需要修改。
					String tgtText1 = ignoreTag ? tu1.getTgtPureText() : tu1.getTgtContent();
					String tgtText2 = ignoreTag ? tu2.getTgtPureText() : tu2.getTgtContent();
					tgtText1 = model.isIgnoreCase() ? tgtText1.toLowerCase() : tgtText1;
					tgtText1 = TextUtil.trimString(tgtText1);
					tgtText2 = model.isIgnoreCase() ? tgtText2.toLowerCase() : tgtText2;
					tgtText2 = TextUtil.trimString(tgtText2);
					return tgtText1.compareTo(tgtText2);
				}
			});
//			System.out.println("排序　tu 耗时：" + (System.currentTimeMillis() - time2));
//			time2 = System.currentTimeMillis();
			
			QATUDataBean bean = null;
			QATUDataBean curBean = null;
			List<QATUDataBean> equalsList;
			bigFor:for (int i = 0; i < dataList.size(); i++) {
				bean = dataList.get(i);
				equalsList = new ArrayList<QATUDataBean>();
				equalsList.add(bean);
				String tgtText = ignoreTag ? bean.getTgtPureText() : bean.getTgtContent();
				tgtText = TextUtil.trimString(tgtText);
				for (int j = i + 1; j < dataList.size(); j++) {
					curBean = dataList.get(j);
					String curTgtText = ignoreTag ? curBean.getTgtPureText() : curBean.getTgtContent();
					curTgtText = TextUtil.trimString(curTgtText);
					if (ignoreCase ? tgtText.equalsIgnoreCase(curTgtText) : tgtText.equals(curTgtText)) {
						equalsList.add(curBean);
//						dataList.remove(j);
//						j --;
						i ++;
					}else {
						ananysisData(equalsList, ignoreCase, ignoreTag);
						continue bigFor;
					}
					if (j == dataList.size() - 1) {
						// 处理　equalslist 中是否有残余的数据。
						ananysisData(equalsList, ignoreCase, ignoreTag);
					}
				}
			}
//			System.out.println("处理排序耗时：" + (System.currentTimeMillis() - time2));
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
//		System.out.println("排序所需时间为:" + (System.currentTimeMillis() - time1));
		
	}
	
	
	/**
	 * 处理相关数据，主要是用于分析　相同译文不同源文的数据，如符合标准，放到缓存中。
	 * @param equalsList
	 * @param ignoreCase
	 * @param ignoreTag ;
	 */
	private void ananysisData(List<QATUDataBean> equalsList, boolean ignoreCase, boolean ignoreTag){
		if (equalsList.size() > 1) {
			// 先检查是否源文不同
			boolean isSrcDiff = false;
			String srcText = ignoreTag ? equalsList.get(0).getSrcPureText() : equalsList.get(0).getSrcContent();
			srcText = TextUtil.trimString(srcText);
			tgtFor:for (int k = 1; k < equalsList.size(); k++) {
				String curSrcText = ignoreTag ? equalsList.get(k).getSrcPureText() : equalsList.get(k).getSrcContent();
				curSrcText = TextUtil.trimString(curSrcText);
				if (!(ignoreCase ? srcText.equalsIgnoreCase(curSrcText) : srcText.equals(curSrcText))) {
					isSrcDiff = true;
					break tgtFor;
				}
			}
			
			if (isSrcDiff) {
				Set<String> tuIdSet = new HashSet<String>();
				for(QATUDataBean thisBean : equalsList){
					tuIdSet.add(thisBean.getTuID());
				}
				// 对　equalsList 按　lineNumber 排序
				Collections.sort(equalsList, new Comparator<QATUDataBean>() {
					@Override
					public int compare(QATUDataBean tu1, QATUDataBean tu2) {
						int lineNumber1 = Integer.parseInt(tu1.getLineNumber());
						int lineNumber2 = Integer.parseInt(tu2.getLineNumber());
						return lineNumber1 > lineNumber2 ? 1 : -1;
					}
				});
				dataMap.put(tuIdSet, equalsList);
			}
		}
		equalsList = new ArrayList<QATUDataBean>();
	}
}
