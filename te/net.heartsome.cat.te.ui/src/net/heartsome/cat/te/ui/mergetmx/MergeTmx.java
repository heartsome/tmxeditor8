/**
 * MergeTmxFile.java
 *
 * Version information :
 *
 * Date:2013/5/17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */

package net.heartsome.cat.te.ui.mergetmx;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.List;

import net.heartsome.cat.common.bean.TmxHeader;
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
 * 合并　tmx 文件
 * @author  Mac
 * @version 
 * @since   JDK1.6
 */
public class MergeTmx {
	private List<String> fileList;
	private String tgtFileLC;
	private FileOutputStream output = null;
	private BufferedOutputStream buffer = null;
	/** 这是进度条的前进间隔，也就是当循环多少个trans-unit节点后前进一格，针对匹配 */
	private static int workInterval = 1;
	public final static Logger LOGG = LoggerFactory.getLogger(MergeTmx.class);
	
	public MergeTmx(List<String> fileList, String tgtFileLC){
		this.fileList = fileList;
		this.tgtFileLC = tgtFileLC;
	}
	
	public void beginMerge(IProgressMonitor monitor){
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		// UNDO 这里还需添加验证　tmx 的代码。因此暂不考虑解析所需要的进度条
		// 每个文件分五格，其中解析　一格，合并一格
		
		try {
			// 先解析文件
			output = new FileOutputStream(tgtFileLC);
			buffer = new BufferedOutputStream(output);
			String srcLang = "";
			boolean isCreateHeaderOrBody = false;
			
			VTDGen vg = null;
			VTDNav vn = null;
			AutoPilot ap = new AutoPilot();
			VTDUtils vu = new VTDUtils();
			monitor.beginTask("", fileList.size());
			for(final String fileLC : fileList){
				String xpath = "count(/tmx/body/tu)";
				vg = new VTDGen();
				if (!vg.parseFile(fileLC, true)) {
					final String message = MessageFormat.format(Messages.getString("dialog.MergeTmxDilog.merge.parseError"), fileLC);
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							OpenMessageUtils.openMessage(IStatus.INFO, message);
						}
					});
					LOGG.error("", new Exception(message));
					throw new OperationCanceledException();
				}
				vn = vg.getNav();
				ap.bind(vn);
				vu.bind(vn);
				ap.selectXPath(xpath);
				final int allTUSize = (int)ap.evalXPathToNumber();
				
				if (allTUSize <= 0) {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							OpenMessageUtils.openMessage(IStatus.INFO,
									MessageFormat.format(Messages.getString("dialog.MergeTmxDilog.merge.fileNull.msg"), fileLC));
						}
					});
					throw new OperationCanceledException();
				}
				
				if (allTUSize > 500) {
					workInterval = allTUSize / 500;
				}
				int totalWork = allTUSize % workInterval == 0 ? (allTUSize / workInterval) : (allTUSize / workInterval) + 1;
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
				subMonitor.beginTask("", totalWork);
				
				// 先获取出源语言。
				if (!isCreateHeaderOrBody) {
					isCreateHeaderOrBody = true;
					xpath = "/tmx/header";
					ap.selectXPath(xpath);
					if (ap.evalXPath() != -1) {
						if (vn.getAttrVal("srclang") != -1) {
							srcLang = vn.toString(vn.getAttrVal("srclang"));
						}
					}
					String xmlDcle = TmxTemplet.genertateTmxXmlDeclar();
					TmxHeader header =TmxTemplet.generateTmxHeader(srcLang, null, null, null, null, null, null); 
					writeSegment(xmlDcle);
					writeSegment("<tmx version=\"1.4\">\n");
					writeSegment(TmxTemplet.header2Xml(header));
					writeSegment("<body>\n");
				}
				xpath = "/tmx/body/tu";
				int i = 0;
				ap.selectXPath(xpath);
				while(ap.evalXPath() != -1){
					i ++;
					writeSegment(vu.getElementFragment());
					monitorWork(subMonitor, i, false);
				}
				monitorWork(subMonitor, i, true);

				subMonitor.done();
			}
			writeSegment("\n</body>\n");
			writeSegment("</tmx>");
			
			buffer.flush();
			buffer.close();
			monitor.done();
			
		} catch (final Exception e) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					OpenMessageUtils.openMessage(IStatus.ERROR, e.getMessage());
				}
			});
			
			LOGG.error("", e);
			if (new File(tgtFileLC).exists()) {
				new File(tgtFileLC).delete();
			}
			throw new OperationCanceledException();
		}finally{
			try {
				if (output != null) {
					output.close();
				}
				if (buffer != null) {
					buffer.close();
				}
			} catch (Exception e2) {
				LOGG.error("", e2);
			}
		}
		
	}
	
	public void MergeTmxFile(List<File> txmFiles, File targetFile) {

	}

	public boolean validateFilesHeader() {
		return false;
	}

	public boolean validateFilesLanguages() {
		return false;
	}

	
	/**
	 * @param segment
	 * @throws Exception
	 */
	private void writeSegment(String segment) throws Exception{
		buffer.write(segment.getBytes("UTF-8"));
	}
	
	private void monitorWork(IProgressMonitor monitor, int traversalTuIndex, boolean last) throws Exception{
		if (last) {
			if (traversalTuIndex % workInterval != 0) {
				if (monitor.isCanceled()) {
					if (new File(tgtFileLC).exists()) {
						new File(tgtFileLC).delete();
					}
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			}
		}else {
			if (traversalTuIndex % workInterval == 0) {
				if (monitor.isCanceled()) {
					if (new File(tgtFileLC).exists()) {
						new File(tgtFileLC).delete();
					}
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			}
		}
	}
}
