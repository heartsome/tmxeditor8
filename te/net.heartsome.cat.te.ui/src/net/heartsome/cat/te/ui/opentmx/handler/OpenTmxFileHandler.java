/**
 * OpenTmxFileHandler.java
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

package net.heartsome.cat.te.ui.opentmx.handler;

import java.io.File;

import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.cat.te.core.utils.TeCoreUtils;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenTmxFileHandler extends AbstractHandler {
	public static final Logger LOGGER = LoggerFactory.getLogger(OpenTmxFileHandler.class);
	private IWorkbenchWindow window;

	private File tmxfileBrowser() {
		FileDialog dlg = new FileDialog(window.getShell(), SWT.SINGLE);
		String[] supExtentions = new String[] { "*.tmx" };
		dlg.setFilterExtensions(supExtentions);
		String absolutePath = dlg.open();
		if (absolutePath == null)
			return null;
		return new File(absolutePath);
	}

	/**
	 * 验证 Tmx 文件编码
	 * @param file
	 *            目标文件
	 * @return true 是 UTF-8 编码，false 不是 UTF-8
	 **/
	private boolean validateTmxFileEncoding(File file) {
		return FileEncodingDetector.detectFileEncoding(file).equalsIgnoreCase("utf-8");
	}

	/**
	 * 1. 创建 Container 2. 创建 DataAccess 3. 通过 TmxEditorViewer 打开 DataAccess
	 * @param file
	 **/
	public static void open(final File file) {
		// TODO 修改当文件打开后关闭以前打开的文件
		final TmxEditorViewer tmxEditorViewer = TmxEditorViewer.getInstance();
		if (tmxEditorViewer == null) {
			OpenMessageUtils.openMessageWithReason(IStatus.ERROR,
					Messages.getString("handler.OpenTmxFileHandler.openFileErrorMsg"),
					Messages.getString("handler.OpenTmxFileHandler.cantFindEditorViewerMsg"));
			return;
		}
		if (tmxEditorViewer.getTmxEditor() != null) {
			if(!tmxEditorViewer.closeTmx()){
				return;
			}
		}
		tmxEditorViewer.open(file);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindow(event);
		File f = tmxfileBrowser();
		if (f == null)
			return null;

//		if (!validateTmxFileEncoding(f)) {
//			if(!OpenMessageUtils.openConfirmMessage(Messages.getString("handler.OpenTmxFileHandler.notUtf8"))){
//				return null;
//			}
//			EncodingConverterDialog dlg = new EncodingConverterDialog(window.getShell(), f.getAbsolutePath());
//			dlg.open();
//			String path = dlg.getNewFilePath();
//			if(path != null && path.length() != 0){
//				f = new File(path);
//				if(!f.exists()){
//					return null;
//				}
//			} else {
//				return null;
//			}
//		}
		open(f);
		
		return null;
	}
}
