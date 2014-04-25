/**
 * TmxValidator.java
 *
 * Version information :
 *
 * Date:2013-11-28
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.utils.tmxvalidator;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.custom.StyledText;

/**
 * @author Austen
 * @version
 * @since JDK1.6
 */
public class TmxValidator {

	private boolean debug = false;
	private boolean report = false;
	private boolean normalize = false;
	private boolean output = false;
	private boolean correctEncoding = false;

	private StyledText styledText = null;
	private IProgressMonitor monitor = null;

	public TmxValidator(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	
	public void validate(String tmxFile) {
		debug = false;
		correctEncoding = true;
		if (debug) {
			System.out.println("debug:" + debug);
			System.out.println("report:" + report);
			System.out.println("normalize:" + normalize);
			System.out.println("output:" + output);
		}

		final ReportBoard board = new ReportBoard(styledText);
		CorrectWriter cwriter = null;
		try {
			File f = new File(tmxFile);
			String newpath = f.getName();
			newpath = f.getParent() + "\\" +newpath.substring(0, newpath.length() - 4) + "_fixed.tmx";
			cwriter = new CorrectWriter(newpath);
			cwriter.setCorrectEncoding(correctEncoding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		TmxScanner2 scanner = new TmxScanner2(tmxFile);
		scanner.setReportBoard(board);
		scanner.setCorrectWriter(cwriter);
		scanner.run(monitor);
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setReport(boolean report) {
		this.report = report;
	}

	public void setStyledText(StyledText styledText) {
		this.styledText = styledText;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public void setOutput(boolean output) {
		this.output = output;
	}

	public void setCorrectEncoding(boolean correctEncoding) {
		this.correctEncoding = correctEncoding;
	}
}
