/**
 * Report.java
 *
 * Version information :
 *
 * Date:2013-12-19
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.core.utils.tmxvalidator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * @author  Austen
 * @version 
 * @since   JDK1.6
 */
public class ReportBoard {

	private StyledText styledText = null;
	Color red = null;
	
	public ReportBoard(StyledText styledText) {
		this.styledText = styledText;
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				red = Display.getDefault().getSystemColor(SWT.COLOR_RED);
			}
		});
	}

	public void info(final String str) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				styledText.append(str + "\n");
			}
		});
	}
	
	public void info(RepairableException e) {
		final StringBuilder builder = new StringBuilder();
		builder.append("[Ln:").append(e.getRow()).append(", Col:").append(e.getColumn()).append("] ");
//		final int l = builder.length();
		builder.append(e.getMessage());
		builder.append("\n");
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				styledText.append(builder.toString());
//				StyleRange range = new StyleRange(0, l, red, null);
//				styledText.setStyleRange(range);
			}
		});
	}

	public void error(final String message) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				styledText.append(message);
//				StyleRange range = new StyleRange(0, message.length(), red, null);
//				styledText.setStyleRange(range);
			}
		});
	}
	
	public void warn() {}

	public void dispose() {
		if (red != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					red.dispose();
				}
			});
		}
	}
}
