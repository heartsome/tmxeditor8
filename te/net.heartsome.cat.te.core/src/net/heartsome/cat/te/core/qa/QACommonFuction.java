package net.heartsome.cat.te.core.qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.te.core.Activator;
import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileContainer;
import net.heartsome.cat.te.core.utils.TmxFilterQueryUtil;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.jface.preference.IPreferenceStore;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;

/**
 * 品质检查所用到的公共方法类
 * @author  robert	2013-09-17
 * @version 
 * @since   JDK1.6
 */
public class QACommonFuction {
	
	
	public QACommonFuction(){
		
	}
	
	
	
	
	/**
	 * 获取当前可用的品质检查项。
	 * @return List<String> 存放每个检查项的　id ,如　QAConstant.QA_TagConsistence;
	 */
	public static List<String> getUseableQAItems(){
		List<String> usebaleQAItemList = new ArrayList<String>();
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String useableQAItemStr = store.getString(QAConstant.PREF_useableQAItemStr);
		if (!useableQAItemStr.isEmpty()) {
			usebaleQAItemList = Arrays.asList(useableQAItemStr.split(";"));
		}
		return usebaleQAItemList;
	}
	
	
	/**
	 * 获取所有数据，包括行号。　此方法仅适用于品质检查
	 * <div style="color:red">此方法与　{@link TmxFilterQueryUtil#getAllTuDataOfTmxList()} 相似，修改时，两个方法皆需修改。</div>
	 * @return ;
	 */
	public static List<QATUDataBean> getAllTuDataOfTmxList(QAModel model) throws Exception{
		final String TUVName = "tuv";
		final String SEGName = "seg";
		
		String srcLang = model.getSrcLang();
		String tgtLang = model.getTgtLang();
		boolean ignoreTag = model.isIgnoreTag();
		TmxLargeFileContainer container = model.getContainer();
		
		List<QATUDataBean> allTuDataBean = new ArrayList<QATUDataBean>();
		final TmxInnerTagParser parser = TmxInnerTagParser.getInstance();
		AutoPilot ap = new AutoPilot();
		ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		
		int index = -1;
		String tuID = "";
		int traversalTuIndex = 0;
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
			String curLang = "";
			String nodeName = "";
			QATUDataBean tuDataBean = new QATUDataBean();
			while (ap.evalXPath() != -1) {
				srcText = null;
				tgtText = null;
				
				curLang = "";
				tuDataBean = new QATUDataBean();
				traversalTuIndex ++;

				if ((index = vn.getAttrVal("hsid")) != -1) {
					tuID = vn.toString(index);
					tuID = subFileIndex + TeCoreConstant.ID_MARK + tuID;
					tuDataBean.setTuID(tuID);
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
				if (srcText == null) {
					srcText = "";
				}
				tuDataBean.setSrcContent(srcText);
				
				if (tgtText == null) {
					tgtText = "";
				}
				tuDataBean.setTgtContent(tgtText);
				
				if (ignoreTag) {
					srcText = parser.getTmxPureText(srcText);
					tuDataBean.setSrcPureText(srcText);
					tgtText = parser.getTmxPureText(tgtText);
					tuDataBean.setTgtPureText(tgtText);
				}
				
				tuDataBean.setLineNumber(traversalTuIndex + "");
				allTuDataBean.add(tuDataBean);
			}
			vn.pop();
		}
		
		return allTuDataBean;
	}
	

}
