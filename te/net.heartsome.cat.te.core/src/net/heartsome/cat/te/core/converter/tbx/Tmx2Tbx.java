/**
 * Tmx2Tbx.java
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

package net.heartsome.cat.te.core.converter.tbx;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.document.TmxReadException;
import net.heartsome.cat.document.TmxReader;
import net.heartsome.cat.te.core.converter.AbstractTmx2File;
import net.heartsome.cat.te.core.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tmx转换成Tbx
 * @author yule
 * @version
 * @since JDK1.6
 */
public class Tmx2Tbx extends AbstractTmx2File {

	private static final Logger LOGGER = LoggerFactory.getLogger(Tmx2Tbx.class);

	public static final String NAME = "tbx";

	private List<TmxTU> cache = new ArrayList<TmxTU>(SIZE);

	private AbstractWriter tbxWriter;

	private static final int SIZE = 500;

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.te.core.converter.AbstractTmx2File#doCovnerter(java.lang.String, java.io.File)
	 */
	public void doCovnerter(String tmxFile, File targetFile, IProgressMonitor monitor) throws Exception {
		try {
			tmxReader = new TmxReader(new File(tmxFile));
			tbxWriter = new TbxWriter(targetFile.getAbsolutePath());
		} catch (TmxReadException e) {
			LOGGER.error("", e);
			throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));

		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
			throw new Exception(Messages.getString("convert.tmx2tbx.tbxFileNotExit"));
		}

		try {
			TmxTU temp = null;
			int total = tmxReader.getTotalTu() / SIZE;
			monitor.beginTask("", total == 0 ? 1 : total);
			String xmlc = tbxWriter.getHeaderXml(tmxReader.getTmxHeader().getSrclang());
			tbxWriter.writeHeader(xmlc);
			while (true) {
				temp = tmxReader.read().getTu();
				if (temp == null) {
					break;
				}
				if (cache.size() != SIZE) {
					cache.add(temp);
					continue;
				}
				if (monitor.isCanceled()) {
					return;
				}
				writeTmxTU(cache);
				monitor.worked(1);
			}
			if (monitor.isCanceled()) {
				return;
			}
			writeTmxTU(cache);
			monitor.done();
		} catch (Exception e) {
			LOGGER.error("", e);
			throw new Exception(Messages.getString("convert.tmx2tbx.writeTbxError"));
		} finally {
			monitor.done();
			tbxWriter.writeEnd();
			tbxWriter.closeOutStream();
		}
	}

	/**
	 * 将缓存内容写入到文件中
	 * @param cache
	 *            ;
	 */
	private void writeTmxTU(List<TmxTU> cache) {
		if (cache.isEmpty()) {
			return;
		}
		tbxWriter.writeBody(cache);
		cache.clear();
	}

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		@SuppressWarnings("unused")
		Tmx2Tbx conver = new Tmx2Tbx();
		File file = new File("E:\\bigTmxTest\\a4.tmx.tbx");
		if (!file.exists()) {
			file.createNewFile();
		}
		// conver.doCovnerter("E:\\bigTmxTest\\a4.tmx", file);
		System.out.println("ALL转换用时:" + (System.currentTimeMillis() - start));
	}
}
