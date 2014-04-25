package net.heartsome.cat.te.core.qa;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.resource.Messages;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 品质检查数据控制类
 * @author  robert	2013-09-22
 * @version 
 * @since   JDK1.6
 */
public class QAControl {
	public PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	/** 是否被品质检查视图注册 */
	private boolean isRegist;
	private QAControl curElement = this;
	private IViewPart qaResultViewer = null;
	private List<QAResultBean> qaResultDataList = new ArrayList<QAResultBean>();
	/** 缓存数量单位，当缓存达到这个数量时，开始传送至结果视图 */
	private int dataUnit = 2000;
	/** 原始的源语言，即未更改大小写 */
	private String originalSrcLang;
	/** 原始的目标语言，即未更改大小写 */
	private String originalTgtLang;
	public final static Logger logger = LoggerFactory.getLogger(QAControl.class.getName());
	
	public QAControl (){
		
	}
	
	
	public void storeQAData(QAResultBean data){
		qaResultDataList.add(data);
	}
	
	/**
	 * 将缓存中的数据传送至品质检查结果视图。<div style="color:red">注意，品质检查结束时也应调用一次，将缓存中剩下的数据传送到结果视图</div>
	 * 因为传送过程很浪费时间，故，设置一项缓存，当缓存　达到　dataUnit　大小时，开始传送
	 */
	public void sendDataToViewer(boolean isLast){
		if (!isRegist) {
			// 先调用方法，查看品质检查结果视图是否处于显示状态，如果没有显示，那么先显示它
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window == null) {
						return;
					}
					IWorkbenchPage workbenchPage = window.getActivePage();
					qaResultViewer = workbenchPage.findView(TeCoreConstant.QAResultViewer_ID);

					if (qaResultViewer == null) {
						try {
							workbenchPage.showView(TeCoreConstant.QAResultViewer_ID);
							qaResultViewer = workbenchPage.findView(TeCoreConstant.QAResultViewer_ID);
						} catch (PartInitException e) {
							e.printStackTrace();
							logger.error(Messages.getString("qa.QAControl.LOGG.qaViewerOpenError"), e);
						}
					} else {
						if (!window.getActivePage().isPartVisible(qaResultViewer)) {
							window.getActivePage().activate(qaResultViewer);
						}
						((IQAResultViewer)qaResultViewer).clearTableData();
					}
					// 注册
					((IQAResultViewer) qaResultViewer).registLister(curElement);
					isRegist = true;
				}
			});
		}
		
		if (isLast) {
			if (qaResultDataList.size() > 0) {
				beginSendData();
			}
		}else {
			if (qaResultDataList.size() >= dataUnit) {
				beginSendData();
			}
		}
	}
	
	private void beginSendData(){
		if (qaResultViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window == null) {
						return;
					}
					if (!window.getActivePage().isPartVisible(qaResultViewer)) {
						window.getActivePage().activate(qaResultViewer);
					}
				}
			});
		}
		if (qaResultDataList.size() > 0) {
			listeners.firePropertyChange("printData", null, qaResultDataList);
		}
		qaResultDataList.clear();
	}


	public String getOriginalSrcLang() {
		return originalSrcLang;
	}
	public void setOriginalSrcLang(String originalSrcLang) {
		this.originalSrcLang = originalSrcLang;
	}
	public String getOriginalTgtLang() {
		return originalTgtLang;
	}
	public void setOriginalTgtLang(String originalTgtLang) {
		this.originalTgtLang = originalTgtLang;
	}
	
	
}
