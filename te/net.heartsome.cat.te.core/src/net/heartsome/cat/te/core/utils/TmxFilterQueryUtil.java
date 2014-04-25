package net.heartsome.cat.te.core.utils;

import java.io.File;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQSequence;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.te.core.Activator;
import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.bean.Property;
import net.heartsome.cat.te.core.bean.SimpleTUData;
import net.heartsome.cat.te.core.bean.TmxEditorFilterBean;
import net.heartsome.cat.te.core.qa.QACommonFuction;
import net.heartsome.cat.te.core.resource.Messages;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileContainer;
import net.heartsome.xml.vtdimpl.VTDUtils;
import net.sf.saxon.xqj.SaxonXQDataSource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;

/**
 * 过滤器查询工具
 * @author robert 2013-07-19
 * @version
 * @since JDK1.6
 */
public class TmxFilterQueryUtil {
	private boolean ignoreTag;
	private boolean ignoreCase;
	private String srcLang;
	private String tgtLang;
	/** 生成的临时索引文件的路径 */
	private String indexFileLC;
	private static final String SEGName = "seg";

	private TmxLargeFileContainer container;
	public static final Logger LOGGER = LoggerFactory.getLogger(TmxFilterQueryUtil.class);

	public TmxFilterQueryUtil(TmxLargeFileContainer container, String srcLang, String tgtLang) {
		this.container = container;
		this.srcLang = srcLang.toLowerCase();
		this.tgtLang = tgtLang.toLowerCase();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		ignoreTag = store.getBoolean(TeCoreConstant.FILTER_ignoreTag);
		ignoreCase = store.getBoolean(TeCoreConstant.FILTER_ignoreCase);

	}

	/**
	 * 2、译文与源文相同的过滤器
	 */
	public List<String> getSrcSameWithTgtTuIdentifiers(IProgressMonitor monitor) throws OperationCanceledException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		long time1 = System.currentTimeMillis();
		monitor.beginTask("", container.getSubFiles().size());

		List<String> tuidentifieList = new ArrayList<String>();
		TmxInnerTagParser parser = TmxInnerTagParser.getInstance();
		try {
			VTDUtils vu = null;
			VTDNav vn = null;
			AutoPilot ap = new AutoPilot();

			for (String subFile : container.getSubFiles()) {
				vu = container.getVTDUtils(subFile);
				vn = vu.getVTDNav();
				vn.push();
				ap.bind(vn);
				ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);

				String tuXpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang
						+ "'] and tuv[lower-case(@xml:lang)='" + tgtLang + "']]/tuv[lower-case(@xml:lang)='" + srcLang
						+ "']/seg";
				ap.selectXPath(tuXpath);

				String text = "";
				String hsid = "";
				String identifie = "";
				int index = -1;
				Map<String, String> srcMap = new LinkedHashMap<String, String>();
				while (ap.evalXPath() != -1) {
					vn.push();
					vn.toElement(VTDNav.PARENT);
					vn.toElement(VTDNav.PARENT);
					if ((index = vn.getAttrVal("hsid")) != -1) {
						hsid = vn.toString(index);
					}
					vn.pop();
					if (index == -1) {
						continue;
					}

					identifie = subFile + TeCoreConstant.ID_MARK + hsid;
					text = vu.getElementContent();
					text = TextUtil.trimString(text);
					if (text == null) {
						text = "";
					}
					if (ignoreTag) {
						text = parser.getTmxPureText(text);
					}
					srcMap.put(identifie, text);
				}

				tuXpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "'] and tuv[lower-case(@xml:lang)='"
						+ tgtLang + "']]/tuv[lower-case(@xml:lang)='" + tgtLang + "']/seg";
				ap.selectXPath(tuXpath);
				while (ap.evalXPath() != -1) {
					vn.push();
					vn.toElement(VTDNav.PARENT);
					vn.toElement(VTDNav.PARENT);
					if ((index = vn.getAttrVal("hsid")) != -1) {
						hsid = vn.toString(index);
					}
					vn.pop();
					if (index == -1) {
						continue;
					}

					identifie = subFile + TeCoreConstant.ID_MARK + hsid;
					text = vu.getElementContent();
					text = TextUtil.trimString(text);
					if (text == null) {
						text = "";
					}
					if (ignoreTag) {
						text = parser.getTmxPureText(text);
					}

					if (ignoreCase ? text.equalsIgnoreCase(srcMap.get(identifie)) : text.equals(srcMap.get(identifie))) {
						tuidentifieList.add(identifie);
					}
					srcMap.remove(identifie);
				}
				vn.pop();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			}

			monitor.done();
			System.out.println("过滤所需耗时为: " + (System.currentTimeMillis() - time1));
		} catch (Exception e) {
			LOGGER.error(Messages.getString("utils.TmxFilterQueryUtil.LOG.getSrcSameWithTgtTu"), e);
		}

		return tuidentifieList;
	}
	
	
	/**
	 * 3、获取源文相同译文不同的　文本段的　tuIdentifie
	 * @param monitor
	 * @return ;
	 */
	public List<String> getSrcSameButTgtTUIdentifies(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		long time1 = System.currentTimeMillis();

		List<String> tuidentifieList = new ArrayList<String>();

		monitor.beginTask("", container.getSubFiles().size() + 2);
		
		try {
			List<TmxTU> allDataList = getAllTuDataOfTmxForFilter(monitor);
			
			
			Collections.sort(allDataList, new Comparator<TmxTU>() {
				@Override
				public int compare(TmxTU tu1, TmxTU tu2) {
					String srcText1 = ignoreTag ? tu1.getSource().getPureText() : tu1.getSource().getFullText();
					String srcText2 = ignoreTag ? tu2.getSource().getPureText() : tu2.getSource().getFullText();
					return srcText1.compareTo(srcText2);
				}
			});
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
			
			Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
			
			TmxTU bean = null;
			TmxTU curBean = null;
			List<TmxTU> equalsList;
			bigFor:for (int i = 0; i < allDataList.size(); i++) {
				bean = allDataList.get(i);
				equalsList = new ArrayList<TmxTU>();
				equalsList.add(bean);
				String srcText = ignoreTag ? bean.getSource().getPureText() : bean.getSource().getFullText();
				
				for (int j = i + 1; j < allDataList.size(); j++) {
					curBean = allDataList.get(j);
					String curSrcText = ignoreTag ? curBean.getSource().getPureText() : curBean.getSource().getFullText();
					if (srcText.equals(curSrcText)) {
						equalsList.add(curBean);
						i ++;
					}else {
						ananysisSrcSameData(indexMap, equalsList);
						continue bigFor;
					}
					if (j == allDataList.size() - 1) {
						// 处理　equalslist 中是否有残余的数据。
						ananysisSrcSameData(indexMap, equalsList);
					}
				}
			}
			
			// 最后处理排序，将现在的排序恢复到之前的情况。
			List<String> keyList = new ArrayList<String>();
			keyList.addAll(indexMap.keySet());
			indexFileIdListSort(keyList);
			List<String> resultIndexList = new ArrayList<String>();
			for (int i = 0; i < keyList.size(); i++) {
				resultIndexList.addAll(indexMap.get(keyList.get(i)));
			}
			// 将　indexId　转成　tuId
			int index = -1;
			int subFileIndex = -1;
			String tuID = "";
			for (String indexID : resultIndexList) {
				index = indexID.indexOf(TeCoreConstant.ID_MARK);
				subFileIndex = Integer.parseInt(indexID.substring(0, index));
				tuID = container.getSubFiles().get(subFileIndex) + indexID.substring(index, indexID.length());
				tuidentifieList.add(tuID);
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
		} catch (Exception e) {
			LOGGER.error(Messages.getString("utils.TmxFilterQueryUtil.LOG.getSrcSameButTgtTU"), e);
			return null;
		} 
		System.out.println("过滤所需耗时为: " + (System.currentTimeMillis() - time1));
		return tuidentifieList;
	} 
	
	
	/**
	 * 处理相关数据，主要是用于分析　相同源文不同译文的数据，如符合标准，放到缓存中。
	 * @param equalsList
	 * @param ignoreCase
	 * @param ignoreTag ;
	 */
	private void ananysisSrcSameData(Map<String, List<String>> indexMap, List<TmxTU> equalsList){
		if (equalsList.size() > 1) {
			// 先检查是否译文不同
			boolean isTgtDiff = false;
			String tgtText = ignoreTag ? equalsList.get(0).getTarget().getPureText() 
					: equalsList.get(0).getTarget().getFullText();
			tgtFor:for (int k = 1; k < equalsList.size(); k++) {
				String curTgtText = ignoreTag ? equalsList.get(k).getTarget().getPureText() 
						: equalsList.get(k).getTarget().getFullText();
				if (!tgtText.equals(curTgtText)) {
					isTgtDiff = true;
					break tgtFor;
				}
			}
			
			if (isTgtDiff) {
				List<String> resultIndexIdList = new ArrayList<String>();
				for(TmxTU thisBean : equalsList){
					resultIndexIdList.add(thisBean.getFidx() + TeCoreConstant.ID_MARK + thisBean.getTmId());
				}
				// 对　equalsList 按　lineNumber 排序
				indexFileIdListSort(resultIndexIdList);
				indexMap.put(resultIndexIdList.get(0), resultIndexIdList);
			}
		}
	}

	/**
	 * 4、获取译文相同源文不同的　文本段的　tuIdentifie
	 * @param monitor
	 * @return ;
	 */
	public List<String> getTgtSameButSrcTUIdentifies(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		long time1 = System.currentTimeMillis();

		List<String> tuidentifieList = new ArrayList<String>();

		monitor.beginTask("", container.getSubFiles().size() + 2);
		
		try {
			List<TmxTU> allDataTempList = getAllTuDataOfTmxForFilter(monitor);
			List<TmxTU> allDataList = new ArrayList<TmxTU>();
			TmxTU tempTU = null;
			// 这里的译文不可能为　null，因为 getAllTuDataOfTmxList 方法已经做了限制，这里主要是为了排除译文全为空，此时会报很多译文相同源文不同的情况。
			for (int i = 0; i < allDataTempList.size(); i++) {
				tempTU = allDataTempList.get(i);
				if (!(ignoreTag ? tempTU.getTarget().getPureText().isEmpty() : tempTU.getTarget().getFullText().isEmpty())) {
					allDataList.add(tempTU);
				}
			}
			
			Collections.sort(allDataList, new Comparator<TmxTU>() {
				@Override
				public int compare(TmxTU tu1, TmxTU tu2) {
					String tgtText1 = ignoreTag ? tu1.getTarget().getPureText() : tu1.getTarget().getFullText();
					String tgtText2 = ignoreTag ? tu2.getTarget().getPureText() : tu2.getTarget().getFullText();
					return tgtText1.compareTo(tgtText2);
				}
			});
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
			
			Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
			
			TmxTU bean = null;
			TmxTU curBean = null;
			List<TmxTU> equalsList;
			bigFor:for (int i = 0; i < allDataList.size(); i++) {
				bean = allDataList.get(i);
				equalsList = new ArrayList<TmxTU>();
				equalsList.add(bean);
				String tgtText = ignoreTag ? bean.getTarget().getPureText() : bean.getTarget().getFullText();
				
				for (int j = i + 1; j < allDataList.size(); j++) {
					curBean = allDataList.get(j);
					String curTgtText = ignoreTag ? curBean.getTarget().getPureText() : curBean.getTarget().getFullText();
					if (tgtText.equals(curTgtText)) {
						equalsList.add(curBean);
						i ++;
					}else {
						ananysisTgtSameData(indexMap, equalsList);
						continue bigFor;
					}
					if (j == allDataList.size() - 1) {
						// 处理　equalslist 中是否有残余的数据。
						ananysisTgtSameData(indexMap, equalsList);
					}
				}
			}
			
			// 最后处理排序，将现在的排序恢复到之前的情况。
			List<String> keyList = new ArrayList<String>();
			keyList.addAll(indexMap.keySet());
			indexFileIdListSort(keyList);
			List<String> resultIndexList = new ArrayList<String>();
			for (int i = 0; i < keyList.size(); i++) {
				resultIndexList.addAll(indexMap.get(keyList.get(i)));
			}
			// 将　indexId　转成　tuId
			int index = -1;
			int subFileIndex = -1;
			String tuID = "";
			for (String indexID : resultIndexList) {
				index = indexID.indexOf(TeCoreConstant.ID_MARK);
				subFileIndex = Integer.parseInt(indexID.substring(0, index));
				tuID = container.getSubFiles().get(subFileIndex) + indexID.substring(index, indexID.length());
				tuidentifieList.add(tuID);
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
		} catch (Exception e) {
			LOGGER.error(Messages.getString("utils.TmxFilterQueryUtil.LOG.getTgtSameButSrcTU"), e);
			return null;
		}

		System.out.println("过滤所需耗时为: " + (System.currentTimeMillis() - time1));
		return tuidentifieList;
	}
	
	/**
	 * 处理相关数据，主要是用于分析　相同译文不同源文的数据，如符合标准，放到缓存中。
	 * @param equalsList
	 * @param ignoreCase
	 * @param ignoreTag ;
	 */
	private void ananysisTgtSameData(Map<String, List<String>> indexMap, List<TmxTU> equalsList){
		if (equalsList.size() > 1) {
			// 先检查是否源文不同
			boolean isSrcDiff = false;
			String srcText = ignoreTag ? equalsList.get(0).getSource().getPureText() 
					: equalsList.get(0).getSource().getFullText();
			tgtFor:for (int k = 1; k < equalsList.size(); k++) {
				String curSrcText = ignoreTag ? equalsList.get(k).getSource().getPureText() 
						: equalsList.get(k).getSource().getFullText();
				if (!srcText.equals(curSrcText)) {
					isSrcDiff = true;
					break tgtFor;
				}
			}
			
			if (isSrcDiff) {
				List<String> resultIndexIdList = new ArrayList<String>();
				for(TmxTU thisBean : equalsList){
					resultIndexIdList.add(thisBean.getFidx() + TeCoreConstant.ID_MARK + thisBean.getTmId());
				}
				// 对　equalsList 按　lineNumber 排序
				indexFileIdListSort(resultIndexIdList);
				indexMap.put(resultIndexIdList.get(0), resultIndexIdList);
			}
		}
	}
	
	
	/**
	 * 5、获取重复文本段
	 * @param monitor
	 * @return ;
	 */
	public List<String> getDuplicateSegTUIdentifies(IProgressMonitor monitor){
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		long time1 = System.currentTimeMillis();
		monitor.beginTask("", container.getSubFiles().size() + 2);
		List<String> tuidentifieList = new ArrayList<String>();
		
		try {
			List<TmxTU> tuSrcList = getAllTuDataOfTmxForFilter(monitor);
			Collections.sort(tuSrcList, new Comparator<TmxTU>() {
				@Override
				public int compare(TmxTU tu1, TmxTU tu2) {
					String srcText1 = ignoreTag ? tu1.getSource().getPureText() : tu1.getSource().getFullText();
					String srcText2 = ignoreTag ? tu2.getSource().getPureText() : tu2.getSource().getFullText();
					return srcText1.compareTo(srcText2);
				}
			});
			
			TmxTU tuBean = null;
			TmxTU curBean = null;
			List<TmxTU> equalsList = new ArrayList<TmxTU>();
			Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
			bigFor:for (int i = 0; i < tuSrcList.size(); i++) {
				tuBean = tuSrcList.get(i);
				equalsList = new ArrayList<TmxTU>();
				equalsList.add(tuBean);
				String srcText = ignoreTag ? tuBean.getSource().getPureText() : tuBean.getSource().getFullText();
				
				for (int j = i + 1; j < tuSrcList.size(); j++) {
					curBean = tuSrcList.get(j);
					String curSrcText = ignoreTag ? curBean.getSource().getPureText() : curBean.getSource().getFullText();
					if (srcText.equals(curSrcText)) {
						equalsList.add(curBean);
						i ++;
					}else {
						if (equalsList.size() > 1) {
							ananysisDuplicateData(indexMap, equalsList);
						}
						continue bigFor;
					}
					if (j == tuSrcList.size() - 1) {
						// 处理　equalslist 中是否有残余的数据。
						if (equalsList.size() > 1) {
							ananysisDuplicateData(indexMap, equalsList);
						}
					}
				}
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
			
			// 最后处理排序，将现在的排序恢复到之前的情况。
			List<String> keyList = new ArrayList<String>();
			keyList.addAll(indexMap.keySet());
			indexFileIdListSort(keyList);
			List<String> resultIndexList = new ArrayList<String>();
			for (int i = 0; i < keyList.size(); i++) {
				resultIndexList.addAll(indexMap.get(keyList.get(i)));
			}
			// 将　indexId　转成　tuId
			int index = -1;
			int subFileIndex = -1;
			String tuID = "";
			for (String indexID : resultIndexList) {
				index = indexID.indexOf(TeCoreConstant.ID_MARK);
				subFileIndex = Integer.parseInt(indexID.substring(0, index));
				tuID = container.getSubFiles().get(subFileIndex) + indexID.substring(index, indexID.length());
				tuidentifieList.add(tuID);
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
			
		} catch (Exception e) {
			LOGGER.error(Messages.getString("utils.TmxFilterQueryUtil.LOG.getDuplicateSrcTU"), e);
			return null;
		}
		monitor.done();
		System.out.println("过滤所需耗时为: " + (System.currentTimeMillis() - time1));
		return tuidentifieList;
	}

	/**
	 * 处理相关数据，主要是用于分析　相同译文不同源文的数据，如符合标准，放到缓存中。
	 * @param equalsList
	 * @param ignoreCase
	 * @param ignoreTag ;
	 */
	private void ananysisDuplicateData(Map<String, List<String>> indexMap, List<TmxTU> equalsList){
		if (equalsList.size() > 1) {
			List<String> resultIdxIdList = null;
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
				
				resultIdxIdList = new ArrayList<String>();
				
				tuBean = equalsList.get(startIdx);
				String tgtText = ignoreTag ? tuBean.getTarget().getPureText() : tuBean.getTarget().getFullText();
				resultIdxIdList.add(tuBean.getFidx() + TeCoreConstant.ID_MARK + tuBean.getTmId());
				removedIdSet.add(startIdx);
				forName:for (int k = startIdx + 1; k < equalsList.size(); k++) {
					if (removedIdSet.contains(k)) {
						continue forName;
					}
					
					curTuBean = equalsList.get(k);
					String curTgtText = ignoreTag ? curTuBean.getTarget().getPureText() : curTuBean.getTarget().getFullText();
					if (tgtText.equals(curTgtText)) {
						resultIdxIdList.add(curTuBean.getFidx() + TeCoreConstant.ID_MARK + curTuBean.getTmId());
						removedIdSet.add(k);
					}
				}
				
				if (resultIdxIdList.size() >= 2) {
					indexFileIdListSort(resultIdxIdList);
					indexMap.put(resultIdxIdList.get(0), resultIdxIdList);
				}
				startIdx ++;
			}
		}
	}
	
	
	/**
	 * 6、获取带批注的文本段的 tuIdentifie
	 * @param monitor
	 * @return ;
	 */
	public List<String> getWithNoteSegTUIdentifies(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		long time1 = System.currentTimeMillis();
		monitor.beginTask("", container.getSubFiles().size());

		List<String> tuidentifieList = new ArrayList<String>();
		VTDUtils vu = null;
		VTDNav vn = null;
		AutoPilot ap = new AutoPilot();

		try {
			for (String subFile : container.getSubFiles()) {
				vu = container.getVTDUtils(subFile);
				vn = vu.getVTDNav();
				ap.bind(vn);
				ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
				vn.push();
				String xpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "'] "
						+ "and tuv[lower-case(@xml:lang)='" + tgtLang + "'] and (note/text()!='' or note/*)]";
				ap.selectXPath(xpath);
				String hsid = null;
				int index = -1;
				while (ap.evalXPath() != -1) {
					if ((index = vn.getAttrVal("hsid")) != -1) {
						hsid = vn.toString(index);
					}
					if (index == -1) {
						continue;
					}

					String identifie = subFile + TeCoreConstant.ID_MARK + hsid;
					tuidentifieList.add(identifie);
				}
				vn.pop();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			}

			monitor.done();
			System.out.println("过滤所需耗时为: " + (System.currentTimeMillis() - time1));
		} catch (Exception e) {
			LOGGER.error(Messages.getString("utils.TmxFilterQueryUtil.LOG.getWithNoteSegTU"), e);
		} finally {
			monitor.done();
		}
		return tuidentifieList;
	}

	/**
	 * 7、获取带乱码的文本段的 tuIdentifie
	 * @param monitor
	 * @return ;
	 */
	public List<String> getWithGarbleSegTUIdentifies(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		List<String> tuidentifieList = new ArrayList<String>();
		long time1 = System.currentTimeMillis();
		monitor.beginTask("", container.getSubFiles().size());

		VTDUtils vu = null;
		VTDNav vn = null;
		AutoPilot ap = new AutoPilot();
		TmxInnerTagParser parser = TmxInnerTagParser.getInstance();

		try {
			for (String subFile : container.getSubFiles()) {
				vu = container.getVTDUtils(subFile);
				vn = vu.getVTDNav();
				ap.bind(vn);
				ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
				vn.push();
				String xpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang
						+ "'] and tuv[lower-case(@xml:lang)='" + tgtLang + "']]/tuv[lower-case(@xml:lang)='" + srcLang
						+ "']/seg";
				ap.selectXPath(xpath);
				String text = null;
				String hsid = null;
				int index = -1;
				String identifie = null;

				while (ap.evalXPath() != -1) {
					vn.push();
					vn.toElement(VTDNav.PARENT);
					vn.toElement(VTDNav.PARENT);
					if ((index = vn.getAttrVal("hsid")) != -1) {
						hsid = vn.toString(index);
					}
					vn.pop();
					if (index == -1) {
						continue;
					}

					identifie = subFile + TeCoreConstant.ID_MARK + hsid;
					text = parser.getTmxPureText(vu.getElementContent());

					if (!java.nio.charset.Charset.forName("UTF-8").newEncoder().canEncode(text)) {
						tuidentifieList.add(identifie);
					}
				}

				xpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "'] and tuv[lower-case(@xml:lang)='"
						+ tgtLang + "']]/tuv[lower-case(@xml:lang)='" + tgtLang + "']/seg";
				ap.selectXPath(xpath);
				text = null;
				identifie = null;
				while (ap.evalXPath() != -1) {
					vn.push();
					vn.toElement(VTDNav.PARENT);
					vn.toElement(VTDNav.PARENT);
					if ((index = vn.getAttrVal("hsid")) != -1) {
						hsid = vn.toString(index);
					}
					vn.pop();
					if (index == -1) {
						continue;
					}

					identifie = subFile + TeCoreConstant.ID_MARK + hsid;

					if (tuidentifieList.contains(identifie)) {
						continue;
					}

					text = parser.getTmxPureText(vu.getElementContent());

					if (!java.nio.charset.Charset.forName("UTF-8").newEncoder().canEncode(text)) {
						tuidentifieList.add(identifie);
					}
				}
				vn.pop();

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			}

			monitor.done();
			System.out.println("过滤所需耗时为: " + (System.currentTimeMillis() - time1));
		} catch (Exception e) {
			LOGGER.error(Messages.getString("utils.TmxFilterQueryUtil.LOG.getWithGarbleSegTU"), e);
		} finally {
			monitor.done();
		}

		return tuidentifieList;
	}

	/**
	 * 8、获取译文为空的文本段
	 * @return ;
	 */
	public List<String> getTgtNullSegTUIdentifies(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		List<String> tuidentifieList = new ArrayList<String>();
		long time1 = System.currentTimeMillis();
		monitor.beginTask("", container.getSubFiles().size());

		VTDUtils vu = null;
		VTDNav vn = null;
		AutoPilot ap = new AutoPilot();

		try {
			for (String subFile : container.getSubFiles()) {
				vu = container.getVTDUtils(subFile);
				vn = vu.getVTDNav();
				ap.bind(vn);
				ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
				vn.push();

				// 译文为空的判断标准为：
				String xpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang
						+ "'] and tuv[lower-case(@xml:lang)='" + tgtLang + "']]" + "/tuv[lower-case(@xml:lang)='"
						+ tgtLang + "']";
				ap.selectXPath(xpath);
				int index = -1;
				String hsid = null;
				String tuID = null;
				String nodeName = null;
				String tgtText = null;
				final TmxInnerTagParser parser = TmxInnerTagParser.getInstance();
				while (ap.evalXPath() != -1) {
					tgtText = null;
					vn.push();
					vn.toElement(VTDNav.PARENT);
					if ((index = vn.getAttrVal("hsid")) != -1) {
						hsid = vn.toString(index);
					} else {
						continue;
					}
					tuID = subFile + TeCoreConstant.ID_MARK + hsid;
					vn.pop();

					// 开始获取译文
					vn.push();
					if (vn.toElement(VTDNav.FIRST_CHILD)) {
						nodeName = vn.toRawString(vn.getCurrentIndex());
						if (nodeName.equalsIgnoreCase(SEGName)) {
							tgtText = vu.getElementContent();
							if (ignoreTag) {
								tgtText = parser.getTmxPureText(tgtText);
							}
						} else {
							vn.push();
							while (vn.toElement(VTDNav.NEXT_SIBLING)) {
								nodeName = vn.toRawString(vn.getCurrentIndex());
								if (nodeName.equalsIgnoreCase(SEGName)) {
									tgtText = vu.getElementContent();
									if (ignoreTag) {
										tgtText = parser.getTmxPureText(tgtText);
									}
									break;
								}
							}
							vn.pop();
						}
					}
					vn.pop();

					if (TextUtil.checkStringEmpty(tgtText)) {
						tuidentifieList.add(tuID);
					}
				}

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.worked(1);
				vn.pop();
			}
			monitor.done();
			System.out.println("过滤所需耗时为: " + (System.currentTimeMillis() - time1));
		} catch (Exception e) {
			LOGGER.error(Messages.getString("utils.TmxFilterQueryUtil.LOG.getTgtNullSegTU"), e);
		} finally {
			monitor.done();
		}

		return tuidentifieList;
	}

	/**
	 * 根据传入的临时文件的　id 的集合进行排序，值必须为类似　0MARK50
	 * @param indexFileIdList
	 *            ;
	 */
	private static void indexFileIdListSort(List<String> indexFileIdList) {
		Collections.sort(indexFileIdList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String[] indexArray1 = o1.split(TeCoreConstant.ID_MARK);
				String[] indexArray2 = o2.split(TeCoreConstant.ID_MARK);

				int fileIndex1 = Integer.parseInt(indexArray1[0]);
				int fileIndex2 = Integer.parseInt(indexArray2[0]);

				int tuIndex1 = Integer.parseInt(indexArray1[1]);
				int tuIndex2 = Integer.parseInt(indexArray2[1]);

				if (fileIndex1 == fileIndex2) {
					return tuIndex1 > tuIndex2 ? 1 : -1;
				} else {
					return fileIndex1 > fileIndex2 ? 1 : -1;
				}
			}
		});
	}

	/**
	 * UNDO 所有的过滤器，因为译文为空的判断修改后，这些过滤器也要修改，但是目前未完成修改。 --robert 2013-10-12 获取自定义过滤器结果 <div
	 * style="color:red">注意。本方法逻辑有点复杂，请勿修改，修改时请与 robert 联系</div>
	 * @return ;
	 */
	public List<String> getCustomFilterTuIdentifies(IProgressMonitor monitor, TmxEditorFilterBean filterBean) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 5);
		List<String> tuidentifieList = new ArrayList<String>();
		long time1 = System.currentTimeMillis();

		VTDUtils vu = null;
		VTDNav vn = null;
		TmxInnerTagParser parser = TmxInnerTagParser.getInstance();
		Set<String> useableIdxIdSet = new HashSet<String>();
		// 只针对　fitall 时。若fitAll 为false ，该集合不使用
		Set<String> notUseableIdxIdSet = new HashSet<String>();

		try {
			// 根据五个处理对象，分批次进行处理
			srcOrTgtOrNoteFilterQuery(new SubProgressMonitor(monitor, 1), vu, vn, parser, filterBean, useableIdxIdSet,
					notUseableIdxIdSet, "src");
			srcOrTgtOrNoteFilterQuery(new SubProgressMonitor(monitor, 1), vu, vn, parser, filterBean, useableIdxIdSet,
					notUseableIdxIdSet, "tgt");
			srcOrTgtOrNoteFilterQuery(new SubProgressMonitor(monitor, 1), vu, vn, parser, filterBean, useableIdxIdSet,
					notUseableIdxIdSet, "note");
			fixedOrCustomPropFilterQuery(new SubProgressMonitor(monitor, 1), vu, vn, parser, filterBean,
					useableIdxIdSet, notUseableIdxIdSet, true);
			fixedOrCustomPropFilterQuery(new SubProgressMonitor(monitor, 1), vu, vn, parser, filterBean,
					useableIdxIdSet, notUseableIdxIdSet, false);

			AutoPilot ap = new AutoPilot();

			// 因为自定义过滤器是根据每种类别来进行处理的。因此会打乱　tuID 的顺序
			final Map<String, Integer> indexIDSortMap = new HashMap<String, Integer>();
			String xpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "']]";
			int subFileIndex = -1;
			String hsid = null;
			int index = -1;
			// 下标标识符，即由 subfileIndex + mark + hsid 组成
			String indexId = null;
			int travleIdex = -1;
			for (String subFile : container.getSubFiles()) {
				subFileIndex++;
				vu = container.getVTDUtils(subFile);
				vn = vu.getVTDNav();
				ap.bind(vn);
				ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
				vn.push();

				ap.selectXPath(xpath);
				while (ap.evalXPath() != -1) {
					travleIdex++;
					if ((index = vn.getAttrVal("hsid")) != -1) {
						hsid = vn.toString(index);
						indexId = subFileIndex + TeCoreConstant.ID_MARK + hsid;
						indexIDSortMap.put(indexId, travleIdex);
					} else {
						continue;
					}
				}
			}

			List<String> indexIDList = new ArrayList<String>();
			indexIDList.addAll(useableIdxIdSet);
			// 随即对　tuidentifieList 结果集合进行排序，以 indexIDSortMap 中的顺序为准
			Collections.sort(indexIDList, new Comparator<String>() {
				@Override
				public int compare(String indexID1, String indexID2) {
					return indexIDSortMap.get(indexID1).compareTo(indexIDSortMap.get(indexID2));
				}
			});

			for (String indexTUID : indexIDList) {
				tuidentifieList.add(parseIdxId2Indetiferid(indexTUID));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			monitor.done();
		}

		System.out.println("自定义过滤所需耗时为: " + (System.currentTimeMillis() - time1));
		return tuidentifieList;
	}

	/**
	 * 自定义过滤器处理对象为源文或译文的查询
	 * @param vu
	 * @param vn
	 * @param parser
	 * @param filterBean
	 * @param useableIdxIdSet
	 * @param notUseableIdxIdSet
	 * @param objetFlag
	 *            src: 针对源文， tgt: 针对译文, note: 针对批注
	 * @throws Exception
	 *             ;
	 */
	private void srcOrTgtOrNoteFilterQuery(IProgressMonitor monitor, VTDUtils vu, VTDNav vn, TmxInnerTagParser parser,
			TmxEditorFilterBean filterBean, Set<String> useableIdxIdSet, Set<String> notUseableIdxIdSet,
			String objectFlag) throws Exception {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		int totalWork = container.getSubFiles().size();
		monitor.beginTask("", totalWork);
		// 是否满足当前过滤器的所有条件，true：满足所有条件，　false：满足以下任意一条件
		boolean isFitAll = filterBean.isFitAll();
		List<Property> objectFilter = null;
		String xpath = null;
		if ("src".equals(objectFlag)) {
			objectFilter = filterBean.getSrcFilter();
			xpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "'] and tuv[lower-case(@xml:lang)='"
					+ tgtLang + "']]/tuv[lower-case(@xml:lang)='" + srcLang + "']/seg";
		} else if ("tgt".equals(objectFlag)) {
			objectFilter = filterBean.getTgtFilter();
			xpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "'] and tuv[lower-case(@xml:lang)='"
					+ tgtLang + "']]/tuv[lower-case(@xml:lang)='" + tgtLang + "']/seg";
		} else if ("note".equals(objectFlag)) {
			objectFilter = filterBean.getNoteFilter();
			xpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "'] and tuv[lower-case(@xml:lang)='"
					+ tgtLang + "']]/note";
		} else {
			monitor.worked(totalWork);
			monitor.done();
			return;
		}
		if (objectFilter.size() <= 0) {
			monitor.worked(totalWork);
			monitor.done();
			return;
		}

		AutoPilot ap = new AutoPilot();

		int subFileIndex = -1;
		for (String subFile : container.getSubFiles()) {
			subFileIndex++;
			vu = container.getVTDUtils(subFile);
			vn = vu.getVTDNav();
			ap.bind(vn);
			ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			vn.push();
			String text = null;
			int index = -1;
			String hsid = null;
			// 下标标识符，即由 subfileIndex + mark + hsid 组成
			String indexId = null;

			// 开始处理操作对象为
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				vn.push();
				if (!"note".equals(objectFlag)) {
					vn.toElement(VTDNav.PARENT);
				}
				vn.toElement(VTDNav.PARENT);
				if ((index = vn.getAttrVal("hsid")) != -1) {
					hsid = vn.toString(index);
				}
				vn.pop();
				if (index == -1) {
					continue;
				}
				indexId = subFileIndex + TeCoreConstant.ID_MARK + hsid;

				// 首先判断是否适用于所有
				if (isFitAll) {
					if (notUseableIdxIdSet.contains(indexId)) {
						continue;
					}
				} else {
					if (useableIdxIdSet.contains(indexId)) {
						continue;
					}
				}

				text = vu.getElementContent();
				if (ignoreTag) {
					text = parser.getTmxPureText(text);
				}

				for (Property srcPro : objectFilter) {
					if (srcPro.getKey().equals(TeCoreConstant.FILTER_TYPE_equal)) {
						if (ignoreCase ? text.equalsIgnoreCase(srcPro.getValue()) : text.equals(srcPro.getValue())) {
							if (isFitAll) {
								useableIdxIdSet.add(indexId);
							} else {
								useableIdxIdSet.add(indexId);
							}
						} else {
							if (isFitAll) {
								useableIdxIdSet.remove(indexId);
								notUseableIdxIdSet.add(indexId);
							}
						}
					} else if (srcPro.getKey().equals(TeCoreConstant.FILTER_TYPE_notEqual)) {
						if (ignoreCase ? !text.equalsIgnoreCase(srcPro.getValue()) : !text.equals(srcPro.getValue())) {
							if (isFitAll) {
								useableIdxIdSet.add(indexId);
							} else {
								useableIdxIdSet.add(indexId);
							}
						} else {
							if (isFitAll) {
								useableIdxIdSet.remove(indexId);
								notUseableIdxIdSet.add(indexId);
							}
						}
					} else if (srcPro.getKey().equals(TeCoreConstant.FILTER_TYPE_include)) {
						if (ignoreCase ? text.toLowerCase().indexOf(srcPro.getValue().toLowerCase()) != -1 : text
								.indexOf(srcPro.getValue()) != -1) {
							if (isFitAll) {
								useableIdxIdSet.add(indexId);
							} else {
								useableIdxIdSet.add(indexId);
							}
						} else {
							if (isFitAll) {
								useableIdxIdSet.remove(indexId);
								notUseableIdxIdSet.add(indexId);
							}
						}
					} else if (srcPro.getKey().equals(TeCoreConstant.FILTER_TYPE_notInclude)) {
						if (ignoreCase ? text.toLowerCase().indexOf(srcPro.getValue().toLowerCase()) == -1 : text
								.indexOf(srcPro.getValue()) == -1) {
							if (isFitAll) {
								useableIdxIdSet.add(indexId);
							} else {
								useableIdxIdSet.add(indexId);
							}
						} else {
							if (isFitAll) {
								useableIdxIdSet.remove(indexId);
								notUseableIdxIdSet.add(indexId);
							}
						}
					}
				}
			}
			vn.pop();
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * 自定义过滤条件 的 固定属性的查询
	 * @param vu
	 * @param vn
	 * @param parser
	 * @param filterBean
	 * @param useableIdxIdSet
	 * @param notUseableIdxIdSet
	 *            ;
	 * @param isFixProperty
	 *            是否是检查固定属性的过滤
	 */
	private void fixedOrCustomPropFilterQuery(IProgressMonitor monitor, VTDUtils vu, VTDNav vn,
			TmxInnerTagParser parser, TmxEditorFilterBean filterBean, Set<String> useableIdxIdSet,
			Set<String> notUseableIdxIdSet, boolean isFixProperty) throws Exception {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		int totalWork = container.getSubFiles().size() + 1;
		monitor.beginTask("", totalWork);
		// 是否满足当前过滤器的所有条件，true：满足所有条件，　false：满足以下任意一条件
		boolean isFitAll = filterBean.isFitAll();
		List<Property> objectFilter = null;
		if (isFixProperty) {
			objectFilter = filterBean.getFixedPropFilter();
		} else {
			objectFilter = filterBean.getCustomPropFilter();
		}
		if (objectFilter.size() <= 0) {
			monitor.worked(totalWork);
			monitor.done();
			return;
		}

		String xpath = null;
		AutoPilot ap = new AutoPilot();
		// 保存当前所有节点的 indexId
		Set<String> curNoUseIdxIdSet = new HashSet<String>();
		// 分两步走，第一步，先循环所有的 tu ,将所有的 tu indexId 加入到 curNoUseIdxIdSet 中。第二步，找出符合标准的文本段，添加至 useableIdxIdSet
		int subFileIndex = -1;
		if (isFitAll) {
			for (String subFile : container.getSubFiles()) {
				subFileIndex++;
				vu = container.getVTDUtils(subFile);
				vn = vu.getVTDNav();
				ap.bind(vn);
				ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
				vn.push();
				int index = -1;
				String hsid = null;
				// 下标标识符，即由 subfileIndex + mark + hsid 组成
				String identifie = null;
				xpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "'] and tuv[lower-case(@xml:lang)='"
						+ tgtLang + "']]";
				// 开始处理操作对象为
				ap.selectXPath(xpath);
				while (ap.evalXPath() != -1) {
					if ((index = vn.getAttrVal("hsid")) != -1) {
						hsid = vn.toString(index);
					}
					if (index == -1) {
						continue;
					}
					identifie = subFileIndex + TeCoreConstant.ID_MARK + hsid;
					curNoUseIdxIdSet.add(identifie);
				}
				vn.pop();
			}
		}
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.worked(1);

		// 第二步，遍历符合要求的文本段
		StringBuffer sb = new StringBuffer();
		sb.append("/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "'] and tuv[lower-case(@xml:lang)='" + tgtLang
				+ "'] and (");
		Property pro = null;
		for (int i = 0; i < objectFilter.size(); i++) {
			pro = objectFilter.get(i);
			if (isFixProperty) {
				if (ignoreCase) {
					sb.append("lower-case(@").append(pro.getKey()).append(")='").append(pro.getValue().toLowerCase())
							.append("'");
				} else {
					sb.append("@").append(pro.getKey()).append("='").append(pro.getValue()).append("'");
				}
			} else {
				if (ignoreCase) {
					sb.append("prop[lower-case(@type)='").append(pro.getKey().toLowerCase())
							.append("' and lower-case(text())='").append(pro.getValue().toLowerCase()).append("']");
				} else {
					sb.append("prop[@type='").append(pro.getKey()).append("' and text()='").append(pro.getValue())
							.append("']");
				}
			}

			if (i < objectFilter.size() - 1) {
				if (isFitAll) {
					sb.append(" and ");
				} else {
					sb.append(" or ");
				}
			}
		}
		sb.append(")]");

		xpath = sb.toString();
		subFileIndex = -1;
		for (String subFile : container.getSubFiles()) {
			subFileIndex++;
			vu = container.getVTDUtils(subFile);
			vn = vu.getVTDNav();
			ap.bind(vn);
			ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			vn.push();
			int index = -1;
			String hsid = null;
			// 下标标识符，即由 subfileIndex + mark + hsid 组成
			String identifie = null;
			// 开始处理操作对象为
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				if ((index = vn.getAttrVal("hsid")) != -1) {
					hsid = vn.toString(index);
				}
				if (index == -1) {
					continue;
				}
				identifie = subFileIndex + TeCoreConstant.ID_MARK + hsid;
				if (isFitAll) {
					curNoUseIdxIdSet.remove(identifie);
					if (!notUseableIdxIdSet.contains(identifie)) {
						useableIdxIdSet.add(identifie);
					}
				} else {
					useableIdxIdSet.add(identifie);
				}
			}
			vn.pop();

			if (isFitAll) {
				for (String curIdentifie : curNoUseIdxIdSet) {
					if (useableIdxIdSet.contains(curIdentifie)) {
						useableIdxIdSet.remove(curIdentifie);
					}
				}
				notUseableIdxIdSet.addAll(curNoUseIdxIdSet);
				curNoUseIdxIdSet.clear();
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * add by yule
	 * @param ignoreTag2
	 *            ;
	 */
	public void setIngoreTag(boolean ignoreTag) {
		this.ignoreTag = ignoreTag;
	}

	/**
	 * 查询需要删除重复的翻译单元的Id值，保留最新修改时间
	 * @return ;
	 */
	public List<String> getDuplicate4DeleteIds(IProgressMonitor monitor) {
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100);
		SubProgressMonitor subGetJob = new SubProgressMonitor(monitor, 20);
		List<SimpleTUData> allSimpleTus = getAllSimpleTus(subGetJob, srcLang, tgtLang);
		SubProgressMonitor subQuryJob = new SubProgressMonitor(monitor, 80);
		return DeleteTUHelper.queryDeleteDulicateTuId(allSimpleTus, subQuryJob, ignoreCase);
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	/**
	 * 查询需要删除相同原文不同译文的单元的ID值，保留最新修改时间
	 * @return ;
	 */
	public List<String> getSrcSameButTgtDiff4DeleteIds(IProgressMonitor monitor) {
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 100);
		SubProgressMonitor subGetJob = new SubProgressMonitor(monitor, 20);
		List<SimpleTUData> allSimpleTus = getAllSimpleTus(subGetJob, srcLang, tgtLang);
		SubProgressMonitor subQuryJob = new SubProgressMonitor(monitor, 80);
		return DeleteTUHelper.queryDeleteSrcSameDiffTgtTuId(allSimpleTus, subQuryJob, ignoreCase);

	}

	/**
	 * 将hsId转换成Indifier
	 * @param hsids
	 * @return ;
	 */
	private String parseIdxId2Indetiferid(String hsid) {
		int id_mark = hsid.indexOf(TeCoreConstant.ID_MARK);
		int fileIndex = Integer.parseInt(hsid.substring(0, id_mark));
		return container.getSubFiles().get(fileIndex) + hsid.substring(id_mark);

	}

	/**
	 * 获取源文相同译文不同的　文本段的　tuId,　返回结果的　tuID
	 * @param monitor
	 * @return ;
	 */
	public List<List<String>> getSrcSameButTgtGroupTUID(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		long time1 = System.currentTimeMillis();

		List<List<String>> tuidentifieList = new ArrayList<List<String>>();

		// 此时 monitor 共有 10份，分8份用于创建索引文件
		monitor.beginTask("", 10);
		indexFileLC = TeCoreUtils.createIndexFile(new SubProgressMonitor(monitor, 8), container, srcLang, tgtLang,
				ignoreTag);
		URI uri = new File(indexFileLC).toURI();
		String uriPath = uri.getPath();

		try {
			XQDataSource dataSource = new SaxonXQDataSource();

			XQConnection conn = dataSource.getConnection();
			String queryString = "for $tu in doc('" + uriPath + "')/tus/tu order by $tu/@src return $tu";

			XQExpression expression = conn.createExpression();
			XQSequence results = expression.executeQuery(queryString);
			List<List<Node>> allNodeList = new ArrayList<List<Node>>();
			List<Node> nodeList = null;
			String src = null;
			while (results.next()) {
				Node node = results.getNode();
				if (src == null) {
					src = node.getAttributes().getNamedItem("src").getNodeValue();
					nodeList = new ArrayList<Node>();
					nodeList.add(node);
				} else {
					if (ignoreCase ? node.getAttributes().getNamedItem("src").getNodeValue().equalsIgnoreCase(src)
							: node.getAttributes().getNamedItem("src").getNodeValue().equals(src)) {
						nodeList.add(node);
					} else {
						if (nodeList.size() > 1) {
							allNodeList.add(nodeList);
						}
						nodeList = new ArrayList<Node>();
						src = node.getAttributes().getNamedItem("src").getNodeValue();
						nodeList.add(node);
					}
				}
			}
			if (nodeList.size() > 1) {
				allNodeList.add(nodeList);
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);

			// 开始处理　allNodeList
			List<List<String>> allIndexList = new ArrayList<List<String>>();
			// 保存
			List<String> indexList = null;
			for (List<Node> curNodeList : allNodeList) {
				String tgtStr = "";
				for (Node curNode : curNodeList) {
					if (tgtStr.isEmpty()) {
						tgtStr = curNode.getAttributes().getNamedItem("tgt").getNodeValue();
					} else {
						if (ignoreCase ? !tgtStr.equalsIgnoreCase(curNode.getAttributes().getNamedItem("tgt")
								.getNodeValue()) : !tgtStr.equals(curNode.getAttributes().getNamedItem("tgt")
								.getNodeValue())) {
							indexList = new ArrayList<String>();
							for (Node indexNode : curNodeList) {
								indexList.add(indexNode.getAttributes().getNamedItem("id").getNodeValue());
							}
							indexFileIdListSort(indexList);
							allIndexList.add(indexList);
							break;
						}
					}
				}
			}

			// 将　hsid　转换成　tuId
			for (List<String> curList : allIndexList) {
				List<String> resultList = new ArrayList<String>();
				for (String id : curList) {
					resultList.add(parseIdxId2Indetiferid(id));
				}
				tuidentifieList.add(resultList);
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (new File(indexFileLC).exists()) {
				new File(indexFileLC).delete();
			}
		}

		System.out.println("过滤所需耗时为: " + (System.currentTimeMillis() - time1));
		return tuidentifieList;
	}

	/**
	 * 获取译文相同源文不同的　文本段的　tuId，　返回的结果是进行分组了的。
	 * @param monitor
	 * @return ;
	 */
	public List<List<String>> getTgtSameButSrcGroupTUID(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		long time1 = System.currentTimeMillis();

		List<List<String>> tuidentifieList = new ArrayList<List<String>>();

		monitor.beginTask("", 10);
		// 分7份用于创建索引文件
		indexFileLC = TeCoreUtils.createIndexFile(new SubProgressMonitor(monitor, 8), container, srcLang, tgtLang,
				ignoreTag);
		URI uri = new File(indexFileLC).toURI();
		String uriPath = uri.getPath();
		try {
			XQDataSource dataSource = new SaxonXQDataSource();

			XQConnection conn = dataSource.getConnection();
			String queryString = "for $tu in doc('" + uriPath + "')/tus/tu order by $tu/@tgt return $tu";

			XQExpression expression = conn.createExpression();
			XQSequence results = expression.executeQuery(queryString);
			List<List<Node>> allNodeList = new ArrayList<List<Node>>();
			List<Node> nodeList = null;
			String src = null;
			while (results.next()) {
				Node node = results.getNode();
				if (src == null) {
					src = node.getAttributes().getNamedItem("tgt").getNodeValue();
					nodeList = new ArrayList<Node>();
					nodeList.add(node);
				} else {
					if (ignoreCase ? node.getAttributes().getNamedItem("tgt").getNodeValue().equalsIgnoreCase(src)
							: node.getAttributes().getNamedItem("tgt").getNodeValue().equals(src)) {
						nodeList.add(node);
					} else {
						if (nodeList.size() > 1) {
							allNodeList.add(nodeList);
						}
						nodeList = new ArrayList<Node>();
						src = node.getAttributes().getNamedItem("tgt").getNodeValue();
						nodeList.add(node);
					}
				}
			}

			if (nodeList.size() > 1) {
				allNodeList.add(nodeList);
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);

			// 开始处理　allNodeList
			List<List<String>> allIndexList = new ArrayList<List<String>>();
			// 保存
			List<String> indexList = null;
			for (List<Node> curNodeList : allNodeList) {
				String tgtStr = "";
				for (Node curNode : curNodeList) {
					if (tgtStr.isEmpty()) {
						tgtStr = curNode.getAttributes().getNamedItem("src").getNodeValue();
					} else {
						if (ignoreCase ? !curNode.getAttributes().getNamedItem("src").getNodeValue()
								.equalsIgnoreCase(tgtStr) : !curNode.getAttributes().getNamedItem("src").getNodeValue()
								.equals(tgtStr)) {
							indexList = new ArrayList<String>();
							for (Node indexNode : curNodeList) {
								indexList.add(indexNode.getAttributes().getNamedItem("id").getNodeValue());
							}
							indexFileIdListSort(indexList);
							allIndexList.add(indexList);
							break;
						}
					}
				}
			}

			// 将　hsid　转换成　tuId
			for (List<String> curList : allIndexList) {
				List<String> resultList = new ArrayList<String>();
				for (String id : curList) {
					resultList.add(parseIdxId2Indetiferid(id));
				}
				tuidentifieList.add(resultList);
			}

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (new File(indexFileLC).exists()) {
				new File(indexFileLC).delete();
			}
		}

		System.out.println("过滤所需耗时为: " + (System.currentTimeMillis() - time1));
		return tuidentifieList;
	}

	public List<SimpleTUData> getAllSimpleTus(IProgressMonitor monitor, String srclang, String tgtLang) {
		if (null == monitor) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", container.getSubFiles().size());
		List<SimpleTUData> simpleTus = new ArrayList<SimpleTUData>();
		for (String subFile : container.getSubFiles()) {
			monitor.worked(1);
			VTDUtils vu = container.getVTDUtils(subFile);
			VTDNav vn = vu.getVTDNav();
			AutoPilot ap = new AutoPilot(vn);
			try {
				ap.selectXPath("//tu");
				while (ap.evalXPath() != -1) {
					TmxTU tu = new TmxTU();
					readTUAttr4VTDNav(vu, tu);
					TmxFileDataAccessUtils.readTUTuv4VTDNav(vu, tu, srclang, tgtLang);
					SimpleTUData sTu = ignoreTag ? new SimpleTUData(tu.getSource().getPureText(), tu.getTarget()
							.getPureText(), tu.getTuId(), tu.getChangeDate()) : new SimpleTUData(tu.getSource()
							.getFullText(), tu.getTarget().getFullText(), tu.getTuId(), tu.getChangeDate());
					sTu.setTuId(subFile + TeCoreConstant.ID_MARK + tu.getTuId());// reset the Indetifer
					if(!sTu.isEmpty()){						
						simpleTus.add(sTu);
					}
				}
			} catch (VTDException e1) {
				e1.printStackTrace();
			}
		}
		monitor.done();

		return simpleTus;
	}

	public void readTUAttr4VTDNav(VTDUtils vu, TmxTU tu) throws VTDException {
		VTDNav vn = vu.getVTDNav();
		vn.push();
		AutoPilot apAttributes = new AutoPilot(vu.getVTDNav());
		apAttributes.selectXPath("@*");
		int inx = -1;
		while ((inx = apAttributes.evalXPath()) != -1) {
			String name = vn.toString(inx);
			inx = vn.getAttrVal(name);
			String value = inx != -1 ? vn.toString(inx) : "";
			if (name.equals("hsid")) {
				tu.setTuId(value);
			} else if (name.equals("creationtool")) {
				tu.setCreationTool(value);
			} else if (name.equals("creationtoolversion")) {
				tu.setCreationToolVersion(value);
			} else if (name.equals("creationdate")) {
				tu.setCreationDate(value);
			} else if (name.equals("creationid")) {
				tu.setCreationUser(value);
			} else if (name.equals("changedate")) {
				tu.setChangeDate(value);
			} else if (name.equals("changeid")) {
				tu.setChangeUser(value);
			} else {
				tu.appendAttribute(name, value);
			}
		}
		vn.pop();
	}

	
	/**
	 * 获取所有 tu 的关键数据，如　src(tgt)FullText, src(tgt)Puretext, indexID 
	 * <div style="color:red">此方法仅适用于过滤器，若其他地方需要调用，请认真查看代码。</div>
	 * <div style="color:red">此方法与　{@link QACommonFuction#getAllTuDataOfTmxList(net.heartsome.cat.te.core.qa.QAModel)} 相似，修改时，两个方法皆需修改。</div>
	 * @return ;
	 */
	private List<TmxTU> getAllTuDataOfTmxForFilter(IProgressMonitor monitor) throws Exception {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		final String TUVName = "tuv";
		final String SEGName = "seg";
		
		List<TmxTU> allTuDataBean = new ArrayList<TmxTU>();
		final TmxInnerTagParser parser = TmxInnerTagParser.getInstance();
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		
		int index = -1;
		int subFileIndex = -1;
		
		for (String subFile : container.getSubFiles()) {
			subFileIndex = container.getSubFiles().indexOf(subFile);
			VTDUtils vu = container.getVTDUtils(subFile);
			VTDNav vn = vu.getVTDNav();
			ap.bind(vn);
			vn.push();
			String xpath = "/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "'] "
					+ "and tuv[lower-case(@xml:lang)='" + tgtLang + "']]";
			ap.selectXPath(xpath);
			
			String srcText = null;
			String tgtText = null;
			TmxSegement srcSeg = null;
			TmxSegement tgtSeg = null;
			String curLang = "";
			String nodeName = "";
			TmxTU tuDataBean = new TmxTU();
			while (ap.evalXPath() != -1) {
				srcText = null;
				tgtText = null;
				
				curLang = "";
				tuDataBean = new TmxTU();

				if ((index = vn.getAttrVal("hsid")) != -1) {
					tuDataBean.setTmId(Integer.parseInt(vn.toRawString(index)));
					tuDataBean.setFidx(subFileIndex);
				}else {
					continue;
				}
				
				// 先切换到子节点
				vn.push();
				if (vn.toElement(VTDNav.FIRST_CHILD)) {
					nodeName = vn.toRawString(vn.getCurrentIndex());
					if (nodeName.equals(TUVName)) {
						if ((index = vn.getAttrVal("xml:lang")) != -1) {
							curLang = vn.toRawString(index);
							if (curLang.equalsIgnoreCase(srcLang)) {
								vn.push();
								if (vn.toElement(VTDNav.FIRST_CHILD)) {
									nodeName = vn.toRawString(vn.getCurrentIndex());
									if (nodeName.equalsIgnoreCase(SEGName)) {
										srcText = vu.getElementContent();
									}else {
										vn.push();
										while(vn.toElement(VTDNav.NEXT_SIBLING)){
											nodeName = vn.toRawString(vn.getCurrentIndex());
											if (nodeName.equalsIgnoreCase(SEGName)) {
												srcText = vu.getElementContent();
												break;
											}
										}
										vn.pop();
									}
								}
								vn.pop();
							}else if (curLang.equalsIgnoreCase(tgtLang)) {
								vn.push();
								if (vn.toElement(VTDNav.FIRST_CHILD)) {
									nodeName = vn.toRawString(vn.getCurrentIndex());
									if (nodeName.equalsIgnoreCase(SEGName)) {
										tgtText = vu.getElementContent();
									}else {
										vn.push();
										while(vn.toElement(VTDNav.NEXT_SIBLING)){
											nodeName = vn.toRawString(vn.getCurrentIndex());
											if (nodeName.equalsIgnoreCase(SEGName)) {
												tgtText = vu.getElementContent();
												break;
											}
										}
										vn.pop();
									}
								}
								vn.pop();
							}
						}
					}
					
					// 开始遍历 tu 第一个子节点，找出　tuv
					while(vn.toElement(VTDNav.NEXT_SIBLING)){
						nodeName = vn.toRawString(vn.getCurrentIndex());
						if (nodeName.equals(TUVName)) {
							if ((index = vn.getAttrVal("xml:lang")) != -1) {
								curLang = vn.toRawString(index);
								if (curLang.equalsIgnoreCase(srcLang)) {
									vn.push();
									if (vn.toElement(VTDNav.FIRST_CHILD)) {
										nodeName = vn.toRawString(vn.getCurrentIndex());
										if (nodeName.equalsIgnoreCase(SEGName)) {
											srcText = vu.getElementContent();
										}else {
											vn.push();
											while(vn.toElement(VTDNav.NEXT_SIBLING)){
												nodeName = vn.toRawString(vn.getCurrentIndex());
												if (nodeName.equalsIgnoreCase(SEGName)) {
													srcText = vu.getElementContent();
													break;
												}
											}
											vn.pop();
										}
									}
									vn.pop();
								}else if (curLang.equalsIgnoreCase(tgtLang)) {
									vn.push();
									if (vn.toElement(VTDNav.FIRST_CHILD)) {
										nodeName = vn.toRawString(vn.getCurrentIndex());
										if (nodeName.equalsIgnoreCase(SEGName)) {
											tgtText = vu.getElementContent();
										}else {
											vn.push();
											while(vn.toElement(VTDNav.NEXT_SIBLING)){
												nodeName = vn.toRawString(vn.getCurrentIndex());
												if (nodeName.equalsIgnoreCase(SEGName)) {
													tgtText = vu.getElementContent();
													break;
												}
											}
											vn.pop();
										}
									}
									vn.pop();
								}
							}
						}
					}
				}
				vn.pop();
				
				// 判断源文译文是否为空 ,　如果为空，设为空值，以防面后调用该方法的代码出现　空指针异常。
				// 处理　　
				if (srcText == null) {
					srcText = "";
				}
				srcText = TextUtil.trimString(srcText);
				if (tgtText == null) {
					tgtText = "";
				}
				tgtText = TextUtil.trimString(tgtText);
				if (ignoreCase) {
					srcText = srcText.toLowerCase();
					tgtText = tgtText.toLowerCase();
				}
				
				
				srcSeg = new TmxSegement();
				tgtSeg = new TmxSegement();
				if (ignoreTag) {
					srcText = parser.getTmxPureText(srcText);
					srcSeg.setPureText(srcText);
					tgtText = parser.getTmxPureText(tgtText);
					tgtSeg.setPureText(tgtText);
				}else {
					srcSeg.setFullText(srcText);
					tgtSeg.setFullText(tgtText);
				}
				tuDataBean.setSource(srcSeg);
				tuDataBean.setTarget(tgtSeg);
				
				allTuDataBean.add(tuDataBean);
			}
			vn.pop();
			
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(1);
		}
		return allTuDataBean;
	}

	public static void main(String[] args) throws ParseException {
		Set<String> haseSet = new HashSet<String>();
		haseSet.add("1");
		haseSet.add("3");
		haseSet.add("2");
		haseSet.add("5");


		
		String test1 = "this is a test.as asdf asdf wf dasfewf asdf ..asd rwt asv drst  era qa  .";
//		String test2 = "this is a test...e fughjfs qrew juzb dfhj gh fewfqw ds dsfg dsfv a.as  ear fg cv asdf gasd";
		List<String> testList = new ArrayList<String>();
		for (int i = 0; i < 1000000; i++) {
			testList.add(test1);
		}
		long time1 = System.currentTimeMillis();
		List<String> testLinkedList = new ArrayList<String>();
		
		for (int i = 0; i < testLinkedList.size(); i++) {
			testLinkedList.add(testList.get(i));
		}
		System.out.println(System.currentTimeMillis() - time1);
		
	}
	
	

}
