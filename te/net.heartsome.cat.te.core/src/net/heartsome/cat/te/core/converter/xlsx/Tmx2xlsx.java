/**
 * Tmx2xlsx.java
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

package net.heartsome.cat.te.core.converter.xlsx;

import java.io.File;
import java.io.IOException;
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

public class Tmx2xlsx extends AbstractTmx2File {

	private static final Logger LOGGER = LoggerFactory.getLogger(Tmx2xlsx.class);

	public static final String NAME = "xlsx";

	private Excel2007Writer writer;

	private static final int size = 1024;

	public void doCovnerter(String tmxFile, File targetFile, IProgressMonitor monitor) throws Exception {
		writer = new Excel2007Writer(targetFile, size);
		try {
			tmxReader = new TmxReader(new File(tmxFile));
		} catch (TmxReadException e) {
			LOGGER.error("", e);
			writer.outZip();// close the outputStream
			throw new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));
		}
		TmxTU tuTemp = null;
		List<TmxTU> tus = new ArrayList<TmxTU>(100);

		int total = tmxReader.getTotalTu() / size;
		monitor.beginTask("", total == 0 ? 1 : total);
		try {
			while (true) {
				tuTemp = tmxReader.read().getTu();
				if (null == tuTemp) {
					break;
				}
				if (tus.size() < size) {
					tus.add(tuTemp);
					continue;
				}

				try {
					if (monitor.isCanceled()) {
						return;
					}
					writer.witerTmxTU(tus, false);
					monitor.worked(1);
					tus.clear();
				} catch (IOException e) {
					LOGGER.error("写入文件出错", e);
					throw new Exception(Messages.getString("converter.tmx2xlsx.writeTmx.error"));
				}

			}

			if (!tus.isEmpty()) {
				try {
					if (monitor.isCanceled()) {
						return;
					}
					writer.witerTmxTU(tus, false);
					monitor.done();
					tus.clear();
				} catch (IOException e) {
					LOGGER.error("写入文件出错", e);
					throw new Exception(Messages.getString("converter.tmx2xlsx.writeTmx.error"));
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			writer.outZip();
			monitor.done();
		}

	}

	public static void main(String[] args) throws IOException {
		Tmx2xlsx conver = new Tmx2xlsx();
		File file = new File("E:/bigTmxTest/a1_temp_test.xlsx");
		if (!file.exists()) {
			file.createNewFile();
		}
		long start = System.currentTimeMillis();
		try {
			// conver.doCovnerter("e:/bigTmxTest/a1.tmx", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("ALL转换用时:" + (System.currentTimeMillis() - start));
		System.out.println("source Size:=>+" + new File("e:/bigTmxTest/a1.tmx").length() / 1024 + "kb");
		System.out.println("Taget  Size:=>" + file.length() / 1024 + "KB");
	}
}
