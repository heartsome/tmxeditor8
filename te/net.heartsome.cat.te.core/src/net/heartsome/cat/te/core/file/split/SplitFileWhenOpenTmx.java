package net.heartsome.cat.te.core.file.split;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.te.core.utils.TeCoreUtils;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;

/**
 * 当打开　tmx 文件时，对其进行分割
 * @author robert 2013-07-12
 */
public class SplitFileWhenOpenTmx {
	private OutputStream output = null;
	private BufferedOutputStream buffer = null;
	private String tempFileLC = null;
	private String spliteFilePath = "/Users/Mac/Desktop/testSplitBigFile/medicaldevicesc_big.tmx";
	/** 每个临时文件的序号，从 0 开始 */
	private int tempFileIndex = 0;
	/** 以 tuUnitSum 个　tu为单位进行分割文件 */
	private final int tuUnitSum = 5000;
	// private final int tuUnitSum = 1000;

	public static final Logger LOGGER = LoggerFactory.getLogger(SplitFileWhenOpenTmx.class);

	public SplitFileWhenOpenTmx() {
	}

	/**
	 * 根据传入的大文件，以　tuUnitSum 个 tu 为单位进行切割成无数小文件， 该方法会抛出退出异常。
	 * @param monitor
	 *            NEEDDELETE 这里的　monitor 负责开始任务以及　done
	 * @param spliteFilePath
	 *            要分割文件的绝对路径
	 * @return null 如果返回为　null。证明文件解析异常。
	 */
	public List<String> splitFile(IProgressMonitor monitor, final String spliteFilePath, VTDNav vn, int tuNum)
			throws OperationCanceledException {
		this.spliteFilePath = spliteFilePath;
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		List<String> subFileList = new LinkedList<String>();

		// 如果临时文件夹不存在，生成临时文件夹
		String fileParentLC = new File(spliteFilePath).getParentFile().getAbsolutePath();
		String tmxTempFolderLC = fileParentLC + File.separator + "." + (new File(spliteFilePath).getName()) + "_folder";
		File tmxTempFolder = new File(tmxTempFolderLC);
		if (!tmxTempFolder.exists()) {
			tmxTempFolder.mkdirs();
		}
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("windows")) {
			String sets = "attrib +H \"" + tmxTempFolder.getAbsolutePath() + "\"";
			try {
				Runtime.getRuntime().exec(sets);
			} catch (IOException e) {
				LOGGER.error("", e);
			}
		}

		try {
			AutoPilot ap = new AutoPilot(vn);
			VTDUtils vu = new VTDUtils(vn);

			// 求出　tu 数量总和
			int subFileSum = (tuNum % tuUnitSum == 0) ? tuNum / tuUnitSum : (tuNum / tuUnitSum) + 1;
			monitor.beginTask("", subFileSum);

			// 首先将　header 里面的数据填充到第一个文件中
			ap.selectXPath("/tmx/header");
			if (ap.evalXPath() != -1) {
				createNewTempFile(tmxTempFolderLC, vu.getElementFragment());
			}

			int i = 0;
			ap.selectXPath("/tmx/body/tu");
			String frag = null;
			while (ap.evalXPath() != -1) {
				i++;
				frag = vu.getElementFragment();
				// UNDO 这里添加 hsid 时还要进行判断是否有重复的 hsid
				writeSegment("<tu hsid='" + i + "'" + frag.substring(3, frag.length()) + "\n");

				if (i == tuUnitSum) {
					endTempFile(subFileList, monitor);

					if (tempFileIndex < subFileSum) {
						// 开始定义下一下文件
						createNewTempFile(tmxTempFolderLC, "");
					}
					i = 0;
				}
			}
			if (buffer != null) {
				endTempFile(subFileList, monitor);
			}
			return subFileList;
		} catch (OperationCanceledException e) {
			if (tmxTempFolder != null && tmxTempFolder.exists()) {
				TeCoreUtils.deleteFolder(tmxTempFolder);
			}
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			if (tmxTempFolder != null && tmxTempFolder.exists()) {
				TeCoreUtils.deleteFolder(tmxTempFolder);
			}
			return null;
		} finally {
			try {
				if (output != null) {
					output.close();
				}
				if (buffer != null) {
					buffer.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			monitor.done();
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	private void writeSegment(String segment) throws Exception {
		buffer.write(segment.getBytes("UTF-8"));
	}

	/**
	 * 创建新的临时文件
	 * @param tmxTempFolderLC
	 *            所有临时文件所处的文件夹
	 * @throws Exception
	 */
	private void createNewTempFile(String tmxTempFolderLC, String headerFrag) throws Exception {
		tempFileLC = tmxTempFolderLC + File.separator + new File(spliteFilePath).getName() + "_" + (tempFileIndex++);
		output = new FileOutputStream(tempFileLC);
		buffer = new BufferedOutputStream(output);
		writeSegment("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		writeSegment("<tmx>\n");
		writeSegment(headerFrag);
		writeSegment("<body>\n");
	}

	private void endTempFile(List<String> tempFileList, IProgressMonitor subMonitor) throws Exception {
		writeSegment("</body>\n");
		writeSegment("</tmx>\n");
		buffer.flush();
		tempFileList.add(tempFileLC);
		subMonitor.worked(1);

		output.close();
		buffer.close();
		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		buffer = null;
	}

}
