/**
 * TmxContainer.java
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

package net.heartsome.cat.te.core.tmxdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.heartsome.cat.document.TmxReadException;
import net.heartsome.cat.te.core.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.EncodingException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

public abstract class TmxContainer {
	public static final Logger LOGGER = LoggerFactory.getLogger(TmxContainer.class);
	/**
	 * Parse current TMX file
	 * @param file
	 * @return
	 * @throws Exception
	 *             VTD parse file exception;
	 */
	public static void parseFile(VTDGen vg, File file, IProgressMonitor monitor) throws Exception {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		FileInputStream fis = null;
		String message = "";
		try {
			fis = new FileInputStream(file);
			byte[] bArr = new byte[(int) file.length()];

			int offset = 0;
			int numRead = 0;
			int numOfBytes = 1048576;// I choose this value randomally,
			monitor.beginTask("", bArr.length / numOfBytes + 2);
			// any other (not too big) value also can be here.
			if (bArr.length - offset < numOfBytes) {
				numOfBytes = bArr.length - offset;
			}
			while (offset < bArr.length && (numRead = fis.read(bArr, offset, numOfBytes)) >= 0) {
				offset += numRead;
				if (bArr.length - offset < numOfBytes) {
					numOfBytes = bArr.length - offset;
				}
				monitor.worked(1);
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			vg.setDoc(bArr);
			monitor.worked(1);
			vg.parse(true);
			monitor.worked(1);
		} catch (IOException e) {
			LOGGER.error("", e);
			message = Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError");
			throw new Exception(message, e);
		} catch (EncodingException e) {
			LOGGER.error("", e);
			message = Messages.getString("tmxdata.TmxFileContainer.parseTmxFileEncodingError");
			throw new TmxReadException(message, e);
		} catch (ParseException e) {
			LOGGER.error("", e);
			String errMsg = e.getMessage();
			if (errMsg.indexOf("invalid encoding") != -1) { // 编码异常
				message = Messages.getString("tmxdata.TmxFileContainer.parseTmxFileEncodingError");
			} else {
				message = Messages.getString("tmxdata.TmxFileContainer.parseTmxFileContentError");
			}
			throw new TmxReadException(message + e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				}
			}
			monitor.done();
		}
	}
	
	public static int countFileTuNumber(VTDUtils vu) {
		if (vu == null) {
			return 0;
		}		
		VTDNav vn = vu.getVTDNav();
		vn.push();
		try {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("count(/tmx/body/tu)");
			int tuCount = (int)ap.evalXPathToNumber();
			return tuCount;
		} catch (VTDException e) {
			LOGGER.error("", e);
		} finally {
			vn.pop();
		}
		return 0;
	}
}
