package net.heartsome.cat.te.ui.splitTmx;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import net.heartsome.cat.common.ui.dialog.FileCoverMsgDialog;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.core.bean.TmxTemplet;
import net.heartsome.cat.te.ui.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * 分割　tmx 文件的　主要功能实现类
 * @author  Mac
 * @version 
 * @since   JDK1.6
 */
public class SplitTmx {
	private String splitFileLC;
	private String tgtFolderLC;
	/** 分割成子文件的数量 */
	private int fileSum;
	/** 每个子文件的　tu 数量 */
	private int tuUnitSum;
	
	private BufferedOutputStream buffer;
	private OutputStream output;
	private String subFileLC;
	private int subFileIndex = 1;
	private String headerFrag;
	/** 这是进度条的前进间隔，也就是当循环多少个trans-unit节点后前进一格，针对匹配 */
	private int workInterval = 1;
	private boolean always = false;
	private int retunCode = FileCoverMsgDialog.OVER;
	public final static Logger LOGG = LoggerFactory.getLogger(SplitTmx.class);
	
	public SplitTmx(String splitFileLC, String tgtFolderLC, int fileSum){
		this.splitFileLC = splitFileLC;
		this.tgtFolderLC = tgtFolderLC;
		this.fileSum = fileSum;
	}
	
	
	/**
	 * 开始分割文件
	 * @param monitor ;
	 */
	public void beginSplit(IProgressMonitor monitor) throws Exception{
		// UNDO 这里未处理　当　tmx 文件编码不为 UTF-8　时的情况
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(Messages.getString("te.ui.splitTmx.taskName"), 10);
		try {
			// 首先解析文件,　并获取出 tu 总数,　解析　花费　1　格.
			VTDGen vg = new VTDGen();
			if (!vg.parseFile(splitFileLC, true)) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						OpenMessageUtils.openMessage(IStatus.WARNING, MessageFormat.format(Messages.getString("te.ui.splitTmx.warn.msg"), splitFileLC));
						return;
					}
				});
			}
			monitor.worked(1);
			
			VTDNav vn = vg.getNav();
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vu = new VTDUtils(vn);
			
			int allTUSize = -1;
			String xpath = "count(/tmx/body/tu)";
			ap.selectXPath(xpath);
			allTUSize = (int)ap.evalXPathToNumber();
			
			if (allTUSize > 500) {
				workInterval = allTUSize / 500;
			}
			tuUnitSum = allTUSize % fileSum == 0 ? (allTUSize / fileSum) : (allTUSize / fileSum) + 1; 
			int matchWorkUnit = allTUSize % workInterval == 0 ? (allTUSize / workInterval) : (allTUSize / workInterval) + 1;
			
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 9);
			subMonitor.beginTask("", matchWorkUnit);
			
			
			// 首先将　header 里面的数据填充到第一个文件中
			ap.selectXPath("/tmx/header");
			if (ap.evalXPath() != -1) {
				headerFrag = vu.getElementFragment();
			}
			
			createNewTempFile(monitor);
			xpath = "/tmx/body/tu";
			ap.selectXPath(xpath);
			int i = 0;
			String tuFrag = null;
			while(ap.evalXPath() != -1){
				i ++;
				tuFrag = vu.getElementFragment();
				writeSegment(tuFrag + "\n");
				if (i % tuUnitSum == 0) {
					endTempFile();
					
					if (i < allTUSize) {
						// 开始定义下一下文件
						createNewTempFile(monitor);
					}
				}
				monitorWork(subMonitor, i, false);
			}
			
			if (buffer != null) {
				endTempFile(); 
			}
			monitorWork(subMonitor, i, true);
			
			subMonitor.done();
		} catch (final FileNotFoundException e) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					OpenMessageUtils.openMessage(IStatus.ERROR, e.getMessage());
				}
			});
			LOGG.error("", e);
		} catch (OperationCanceledException e) {
			// do nothing.
		} catch (final Exception e) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					OpenMessageUtils.openMessage(IStatus.ERROR, e.getMessage());
				}
			});
			LOGG.error("", e);
		} 
	}
	
	
	/**
	 * 创建新的临时文件
	 * @param tmxTempFolderLC	所有临时文件所处的文件夹
	 * @throws Exception
	 */
	private void createNewTempFile(IProgressMonitor monitor) throws OperationCanceledException, Exception{
		String name = new File(splitFileLC).getName();
		int extentionIdx = name.lastIndexOf(".");
		subFileLC = tgtFolderLC + File.separator + name.substring(0, extentionIdx) + "_" + (subFileIndex++)
				+ name.substring(extentionIdx, name.length());
		checkRepeate(subFileLC, monitor);
		if (retunCode == FileCoverMsgDialog.OVER) {
			output = new FileOutputStream(subFileLC);
			buffer = new BufferedOutputStream(output);
			String xmlDcle = TmxTemplet.genertateTmxXmlDeclar();
			writeSegment(xmlDcle);
			writeSegment("<tmx version=\"1.4\">\n");
			writeSegment(headerFrag);
			writeSegment("<body>\n");
		}else if (retunCode == FileCoverMsgDialog.SKIP) {
			output = null;
			buffer = null;
		}
	}
	
	
	/**
	 * 是否覆盖
	 * @param fileLC
	 * @return ;
	 */
	private void checkRepeate(final String fileLC, final IProgressMonitor monitor) {
		if (new File(fileLC).exists()) {
			if (!always) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						FileCoverMsgDialog dialog = new FileCoverMsgDialog(Display.getDefault().getActiveShell(), fileLC);
						retunCode = dialog.open();
						always = dialog.isAlways();
					}
				});
			}
		}else {
			retunCode = FileCoverMsgDialog.OVER;
		}
		if (retunCode == FileCoverMsgDialog.CANCEL) {
			monitor.setCanceled(true);
			throw new OperationCanceledException();
		}
	}
	
	
	private void endTempFile() throws Exception{
		writeSegment("</body>\n");
		writeSegment("</tmx>\n");
		if (buffer != null) {
			buffer.flush();
		}
		if (output != null) {
			output.close();
		}
		if (buffer != null) {
			buffer.close();
		}
		buffer = null;
	}
	
	
	private void writeSegment(String segment) throws Exception{
		if (buffer != null) {
			buffer.write(segment.getBytes("UTF-8"));
		}
	}
	
	public void monitorWork(IProgressMonitor monitor, int traversalTuIndex, boolean last) throws Exception{
		if (last) {
			if (traversalTuIndex % workInterval != 0) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			}
		}else {
			if (traversalTuIndex % workInterval == 0) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			}
		}
	}
	
	
	
	
	
}
