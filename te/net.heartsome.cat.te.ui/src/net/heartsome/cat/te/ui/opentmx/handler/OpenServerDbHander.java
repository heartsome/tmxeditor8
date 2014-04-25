/**
 * OpenServerDbHander.java
 *
 * Version information :
 *
 * Date:2013-6-9
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.ui.opentmx.handler;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.ui.opentmx.ui.TmDbManagerDialog;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class OpenServerDbHander extends AbstractHandler {

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final TmxEditorViewer tmxEditorViewer = TmxEditorViewer.getInstance();
		if (tmxEditorViewer == null) {
			OpenMessageUtils.openMessageWithReason(IStatus.ERROR,
					Messages.getString("handler.OpenTmxFileHandler.openFileErrorMsg"),
					Messages.getString("handler.OpenTmxFileHandler.cantFindEditorViewerMsg"));
			return null;
		}

		TmDbManagerDialog tmDbManagerDialog = new TmDbManagerDialog(Display.getDefault().getActiveShell());
		tmDbManagerDialog.setDialogUseFor(TmDbManagerDialog.TYPE_DBSELECTED);
		tmDbManagerDialog.open();
		final DatabaseModelBean db = tmDbManagerDialog.getDB();
		if (null == db) {
			return null;
		}

		// 修改当文件打开后关闭以前打开的文件
		if (tmxEditorViewer.getTmxEditor() != null) {
			if(!tmxEditorViewer.closeTmx()){
				return null;
			}
		}
		tmxEditorViewer.open(db);
		return null;
	}

}
