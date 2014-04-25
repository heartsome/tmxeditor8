package net.heartsome.cat.te.core.qa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.heartsome.cat.common.util.TextUtil;

/**
 * 源文相同但译文不同
 * @author  robert	2013-09-16
 * @version 
 * @since   JDK1.6
 */
public class SrcSameButTgtQA extends QARealization {
	boolean isFilter = false;
	private Map<Set<String>, List<QATUDataBean>> dataMap = null;
	
	@Override
	public void beginTmxQA(QATUDataBean tu) {
		// 备注：这里不判断译文为空的情况
		if (dataMap == null) {
			initData();
		}
		
		if (dataMap.size() < 0) {
			return;
		}
		
		String tuId = tu.getTuID();
		
		String qaType = QAConstant.QA_SrcSameButTgt;
		String qaTypeText = QAConstant.QA_SrcSameButTgtText;
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
	
	
	/**
	 * 初始化相同源文不同译文所要用到的数据
	 * @return ;
	 */
	private Map<Set<String>, List<QATUDataBean>> initData(){
		dataMap = new HashMap<Set<String>, List<QATUDataBean>>();
		
		long time1 = System.currentTimeMillis();
		final boolean ignoreTag = model.isIgnoreTag();
		final boolean ignoreCase = model.isIgnoreCase();
		
		try {
			List<QATUDataBean> dataList = QACommonFuction.getAllTuDataOfTmxList(model);

			Collections.sort(dataList, new Comparator<QATUDataBean>() {
				@Override
				public int compare(QATUDataBean tu1, QATUDataBean tu2) {
					String srcText1 = ignoreTag ? tu1.getSrcPureText() : tu1.getSrcContent();
					String srcText2 = ignoreTag ? tu2.getSrcPureText() : tu2.getSrcContent();
					srcText1 = model.isIgnoreCase() ? srcText1.toLowerCase() : srcText1;
					srcText1 = TextUtil.trimString(srcText1);
					srcText2 = model.isIgnoreCase() ? srcText2.toLowerCase() : srcText2;
					srcText2 = TextUtil.trimString(srcText2);
					return srcText1.compareTo(srcText2);
				}
			});
			
			QATUDataBean bean = null;
			QATUDataBean curBean = null;
			List<QATUDataBean> equalsList;
			bigFor:for (int i = 0; i < dataList.size(); i++) {
				bean = dataList.get(i);
				equalsList = new ArrayList<QATUDataBean>();
				equalsList.add(bean);
				String srcText = ignoreTag ? bean.getSrcPureText() : bean.getSrcContent();
				srcText = TextUtil.trimString(srcText);
				
				for (int j = i + 1; j < dataList.size(); j++) {
					curBean = dataList.get(j);
					String curSrcText = ignoreTag ? curBean.getSrcPureText() : curBean.getSrcContent();
					curSrcText = TextUtil.trimString(curSrcText);
					if (ignoreCase ? srcText.equalsIgnoreCase(curSrcText) : srcText.equals(curSrcText)) {
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
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("排序所需时间为:" + (System.currentTimeMillis() - time1));
		return dataMap;
	}
	
	/**
	 * 处理相关数据，主要是用于分析　相同源文不同译文的数据，如符合标准，放到缓存中。
	 * @param equalsList
	 * @param ignoreCase
	 * @param ignoreTag ;
	 */
	private void ananysisData(List<QATUDataBean> equalsList, boolean ignoreCase, boolean ignoreTag){
		if (equalsList.size() > 1) {
			// 先检查是否译文不同
			boolean isTgtDiff = false;
			String tgtText = ignoreTag ? equalsList.get(0).getTgtPureText() : equalsList.get(0).getTgtContent();
			tgtText = TextUtil.trimString(tgtText);
			tgtFor:for (int k = 1; k < equalsList.size(); k++) {
				String curTgtText = ignoreTag ? equalsList.get(k).getTgtPureText() : equalsList.get(k).getTgtContent();
				curTgtText = TextUtil.trimString(curTgtText);
				if (!(ignoreCase ? tgtText.equalsIgnoreCase(curTgtText) : tgtText.equals(curTgtText))) {
					isTgtDiff = true;
					break tgtFor;
				}
			}
			
			if (isTgtDiff) {
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
	
	
	public static void main(String[] args) {
		Map<String, String> testMap = new HashMap<String, String>();
		testMap.put("1", "1");
		testMap.put("2", "2");
		testMap.put("3", "3");
		testMap.put("4", "4");
		testMap.put("5", "5");
		testMap.put("6", "6");
		
		Iterator<String> it = testMap.keySet().iterator();
		while(it.hasNext()){
			System.out.println("--------");
			String key = it.next();
			if (key.equals("2")) {
				testMap.remove(key);
				it = testMap.keySet().iterator();
			}
		}
		
		System.out.println(testMap.size());
		
		
	}
	
	
	

}
