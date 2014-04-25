package net.heartsome.cat.te.core.qa;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.te.core.Activator;
import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.resource.Messages;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileContainer;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;


/**
 * te 品质检查触发类
 * @author  robert	2013-09-17
 * @version 
 * @since   JDK1.6
 */
public class QATrigger {
	private QAModel model = new QAModel();
	private QAControl qaControl = new QAControl();
	/** 这是进度条的前进间隔，也就是当循环多少个trans-unit节点后前进一格 */
	private static int workInterval = 1;
	private Map<String, QARealization> qaItemClassMap = new HashMap<String, QARealization>();
	private static final String TUVName = "tuv";
	private static final String SEGName = "seg";
	public static final Logger logger = LoggerFactory.getLogger(QATrigger.class);
	
	
	/**
	 * 开始进行　tmx 的品质检查
	 */
	public void beginTMXQA(final TmxLargeFileContainer container, String srcLangCode, String tgtLangCode, List<String> tuIdentifiers){
		final String srcLang = srcLangCode.toLowerCase();
		final String tgtLang = tgtLangCode.toLowerCase();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		final boolean ignoreCase = store.getBoolean(TeCoreConstant.FILTER_ignoreCase);
		final boolean ignoreTag = store.getBoolean(TeCoreConstant.FILTER_ignoreTag);
		model.setIgnoreTag(ignoreTag);
		model.setIgnoreCase(ignoreCase);
		model.setSrcLang(srcLang);
		model.setTgtLang(tgtLang);
		model.setContainer(container);
		qaControl.setOriginalSrcLang(srcLangCode);
		qaControl.setOriginalTgtLang(tgtLangCode);
		
		
		final TmxInnerTagParser parser = TmxInnerTagParser.getInstance();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {	
				long time1 = System.currentTimeMillis();
				
				try {
					// UNDO 这里还应给相同源文不同译文分配进度条数据。
					QARealization realization = null;
					AutoPilot ap = new AutoPilot();
					ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
					List<String> itemList = QACommonFuction.getUseableQAItems();
					
					// 先统计总数
					int allTuSum = 0;
					for (String subFile : container.getSubFiles()) {
						VTDUtils vu = container.getVTDUtils(subFile);
						VTDNav vn = vu.getVTDNav();
						ap.bind(vn);
						// 之前过滤都是带源语言以及目标语言的，后来因为考虑到译文为空的判断，故搞成这样。
						String xpath = "count(/tmx/body/tu[tuv[lower-case(@xml:lang)='" + srcLang + "'] " + "and tuv[lower-case(@xml:lang)='" + tgtLang + "']])";
						ap.selectXPath(xpath);
						allTuSum += (int) ap.evalXPathToNumber();
					}
					if (allTuSum > 500) {
						workInterval = allTuSum / 500;
					}
					monitor.beginTask(Messages.getString("qa.QATrigger.job.monitor.name"),
							allTuSum % workInterval == 0 ? (allTuSum / workInterval) : (allTuSum / workInterval) + 1);
					
					String tuID = null;
					int index = -1;
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
						
						String srcText = "";
						String tgtText = "";
						String curLang = "";
						String nodeName = "";
						QATUDataBean tuDataBean = new QATUDataBean();
						while (ap.evalXPath() != -1) {
							curLang = "";
							tuDataBean = new QATUDataBean();
							traversalTuIndex ++;

							if ((index = vn.getAttrVal("hsid")) != -1) {
								// 这里的　tuid 采用　subFileIndex + mark + hsId 的情况，是为了节省内存。
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
													tuDataBean.setSrcContent(srcText);
													srcText = parser.getTmxPureText(srcText);
													tuDataBean.setSrcPureText(srcText);
												}else {
													vn.push();
													while(vn.toElement(VTDNav.NEXT_SIBLING)){
														nodeName = vn.toRawString(vn.getCurrentIndex());
														if (nodeName.equalsIgnoreCase(SEGName)) {
															srcText = vu.getElementContent();
															tuDataBean.setSrcContent(srcText);
															srcText = parser.getTmxPureText(srcText);
															tuDataBean.setSrcPureText(srcText);
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
													tuDataBean.setTgtContent(tgtText);
													tgtText = parser.getTmxPureText(tgtText);
													tuDataBean.setTgtPureText(tgtText);
												}else {
													vn.push();
													while(vn.toElement(VTDNav.NEXT_SIBLING)){
														nodeName = vn.toRawString(vn.getCurrentIndex());
														if (nodeName.equalsIgnoreCase(SEGName)) {
															tgtText = vu.getElementContent();
															tuDataBean.setTgtContent(tgtText);
															tgtText = parser.getTmxPureText(tgtText);
															tuDataBean.setTgtPureText(tgtText);
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
														tuDataBean.setSrcContent(srcText);
														srcText = parser.getTmxPureText(srcText);
														tuDataBean.setSrcPureText(srcText);
													}else {
														vn.push();
														while(vn.toElement(VTDNav.NEXT_SIBLING)){
															nodeName = vn.toRawString(vn.getCurrentIndex());
															if (nodeName.equalsIgnoreCase(SEGName)) {
																srcText = vu.getElementContent();
																tuDataBean.setSrcContent(srcText);
																srcText = parser.getTmxPureText(srcText);
																tuDataBean.setSrcPureText(srcText);
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
														tuDataBean.setTgtContent(tgtText);
														tgtText = parser.getTmxPureText(tgtText);
														tuDataBean.setTgtPureText(tgtText);
													}else {
														vn.push();
														while(vn.toElement(VTDNav.NEXT_SIBLING)){
															nodeName = vn.toRawString(vn.getCurrentIndex());
															if (nodeName.equalsIgnoreCase(SEGName)) {
																tgtText = vu.getElementContent();
																tuDataBean.setTgtContent(tgtText);
																tgtText = parser.getTmxPureText(tgtText);
																tuDataBean.setTgtPureText(tgtText);
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
							
							tuDataBean.setLineNumber((traversalTuIndex) + "");
							
							for (String item : itemList) {
								realization = getClassInstance(item);
								realization.beginTmxQA(tuDataBean);
							}
							qaControl.sendDataToViewer(false);
							
							monitorWork(monitor, traversalTuIndex, false);

						}
						monitorWork(monitor, traversalTuIndex, true);
						vn.pop();
					}
					qaControl.sendDataToViewer(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				monitor.done();
				System.out.println("当前时间为 = " + (System.currentTimeMillis() - time1));
			}
		};
		try {
			new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, runnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	
	/**
	 * 开始进行　db　的品质检查
	 */
	public void beginDBQA(){
		
		
		
	}
	
	
	/**
	 * 获取实例
	 * @param qaItemId
	 * @return ;
	 */
	public QARealization getClassInstance(String qaItemId) {
		if (qaItemClassMap.get(qaItemId) != null) {
			return (QARealization) qaItemClassMap.get(qaItemId);
		}

		try {
			HashMap<String, String> valueMap = model.getQaItemId_Name_Class().get(qaItemId);
			Object obj = null;
			try {
				obj = Class.forName(valueMap.get(QAConstant.ITEM_CLASSNAME)).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(
						MessageFormat.format(Messages.getString("qa.QATrigger.LOGG.getInstanceError"),
								valueMap.get(QAConstant.ITEM_NAME)), e);
			}
			if (QARealization.class.isInstance(obj)) {
				qaItemClassMap.put(qaItemId, (QARealization) obj);
				((QARealization) obj).setQaControl(qaControl);
				QARealization realication = (QARealization) obj;
				realication.setModel(model);
				return realication;
			}
		} catch (Exception e) {
			logger.error(Messages.getString("qa.QATrigger.qaError"), e);
		}
		return null;
	}
	
	
	/**
	 * 控制进度条的进度
	 * @param monitor
	 * @param traversalTuIndex
	 * @param last
	 * @return ;
	 */
	private void monitorWork(IProgressMonitor monitor, int traversalTuIndex, boolean last){
		if (last) {
			if (traversalTuIndex % workInterval != 0) {
				/*try {
					Thread.sleep(500);
				} catch (Exception e) {
				}*/
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			}
		}else {
			if (traversalTuIndex % workInterval == 0) {
				/*try {
					Thread.sleep(500);
				} catch (Exception e) {
				}*/
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			}
		}
	}
	
	
	public static void main(String[] args) {
		String test = "/mac/descktop/robert/document/te document/tmx file/test/file1" + TeCoreConstant.ID_MARK + "10";
		long time1 = System.currentTimeMillis();
		for (int i = 0; i < 10000000; i++) {
//			System.out.println(test.split(TeCoreConstant.ID_MARK)[1]);
			test.split(TeCoreConstant.ID_MARK);
		}
		System.out.println(System.currentTimeMillis() - time1);
		
		
		
	}
	
	
}
