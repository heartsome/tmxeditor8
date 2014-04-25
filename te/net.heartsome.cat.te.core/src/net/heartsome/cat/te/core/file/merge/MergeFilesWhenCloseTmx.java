package net.heartsome.cat.te.core.file.merge;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;

/**
 * 当关闭文件时，将传入的临时文件进行合并
 * @author robert 2013-07-12
 */
public class MergeFilesWhenCloseTmx {
	private FileOutputStream output = null;
	private BufferedOutputStream buffer = null;
	public static final Logger LOGGER = LoggerFactory.getLogger(MergeFilesWhenCloseTmx.class);
	private String mergedTgtFileEncoding;

	public MergeFilesWhenCloseTmx() {

	}

	/**
	 * 合并文件
	 * @param vnMap
	 *            保存　临时子文件路径与　VTDNav 对象的一对一数据
	 * @param subFileList
	 *            分割的临时子文件(路径)集合。
	 * @param mergedTgtFilePath
	 *            将　tempFileList 内文件合并后生成的目标文件的路径
	 * @param monitor
	 *            传入的　子　monitor ,本方法自动结束
	 * @return true:　合并成功，　false: 合并失败
	 * @throws Exception
	 */
	public boolean mergeTempFile(Map<String, VTDUtils> vnMap, List<String> subFileList, final String mergedTgtFilePath,
			final String mergedTgtFileEncoding, String tmxVersion, String docTypeContent, IProgressMonitor monitor)
			throws Exception {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", subFileList.size());
		this.mergedTgtFileEncoding = mergedTgtFileEncoding;
		if (this.mergedTgtFileEncoding == null || this.mergedTgtFileEncoding.length() == 0) {
			this.mergedTgtFileEncoding = "utf-8";
		}
		try {
			output = new FileOutputStream(mergedTgtFilePath);
			buffer = new BufferedOutputStream(output);
			// 开始生成　目标　文件的头等　信息
			if (mergedTgtFileEncoding.equalsIgnoreCase("UTF-16LE")) {
				output.write(0xFF);
				output.write(0xFE);
			} else if (mergedTgtFileEncoding.equalsIgnoreCase("UTF-16BE")) {
				output.write(0xFE);
				output.write(0xFF);
			}
			writeSegment("<?xml version=\"1.0\" encoding=\"" + mergedTgtFileEncoding + "\"?>\n");
			if(docTypeContent != null && docTypeContent.length() != 0){
				writeSegment("<!DOCTYPE"+docTypeContent+">\n");
			}
			if (tmxVersion != null && tmxVersion.length() != 0) {
				writeSegment("<tmx version=\""+tmxVersion+"\">\n");
			} else {
				writeSegment("<tmx>\n");
			}

			VTDNav vn = null;
			AutoPilot ap = new AutoPilot();
			VTDUtils vu = new VTDUtils();

			for (int i = 0; i < subFileList.size(); i++) {
				vn = vnMap.get(subFileList.get(i)).getVTDNav();
				ap.bind(vn);
				vu.bind(vn);

				// 如果是第一个文件，那么要将这个文件中的　header 节点拷到目标文件中
				if (i == 0) {
					ap.selectXPath("/tmx/header");
					if (ap.evalXPath() != -1) {
						writeSegment(vu.getElementFragment() + "\n");
					}
					writeSegment("<body>\n");
				}

				ap.selectXPath("/tmx/body/tu");
				while (ap.evalXPath() != -1) {
					writeSegment(vu.getElementFragment().replaceFirst("\\s{1}hsid='\\d+'", "") + "\n");
				}

				monitor.worked(1);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}

			writeSegment("</body>\n");
			writeSegment("</tmx>");
			buffer.flush();
			return true;
		} catch (OperationCanceledException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("", e);
			throw e;
		} finally {
			try {
				if (output != null) {
					output.close();
				}
				if (buffer != null) {
					buffer.close();
				}
				monitor.done();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	/**
	 * @param segment
	 * @throws Exception
	 */
	private void writeSegment(String segment) throws Exception {
		buffer.write(segment.getBytes(mergedTgtFileEncoding));
	}

}
