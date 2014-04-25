/**
 * NewTmxFileHandler.java
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

package net.heartsome.cat.te.ui.newtmx.handler;

import java.io.File;

import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.ui.newtmx.ui.NewTmxFileDialog;
import net.heartsome.cat.te.ui.opentmx.handler.OpenTmxFileHandler;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 创建一个空的TMX文件，空的TMX文件是指body节点中初始化一个TU，源语言和目标语言的TUV内容为空串，其他内容填充默认值，如创建者、创建时间等信息。具体默认取值参考TMX标准。 2013-06-07
 * @author robert
 * @version
 * @since JDK1.6
 */
public class NewTmxFileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShell(event);
		final TmxEditorViewer tmxEditorViewer = TmxEditorViewer.getInstance();
		if (tmxEditorViewer == null) {
			OpenMessageUtils.openMessage(IStatus.ERROR,
					Messages.getString("handler.OpenTmxFileHandler.cantFindEditorViewerMsg"));
			return null;
		}
		if (tmxEditorViewer.getTmxEditor() != null) {
			if (!tmxEditorViewer.closeTmx()) {
				return null;
			}
		}
		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {

			@Override
			public void run() {
				NewTmxFileDialog dialog = new NewTmxFileDialog(shell);
				if (dialog.open() == Dialog.OK) {
					String path = dialog.getNewFilePath();
					if (path != null) {
						File f = new File(path);
						if (f.exists()) {
							OpenTmxFileHandler.open(f);
						}
					}
				}
			}
		});
		return null;
	}
}
