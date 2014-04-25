package net.heartsome.cat.database.ui.tb.handler;

import java.util.List;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog;
import net.heartsome.cat.database.ui.tb.resource.Messages;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.LoggerFactory;

/**
 * 搜索术语库的 Handler
 * @author peason
 * @version
 * @since JDK1.6
 */
public class TermBaseSearchHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!isEnabled()) {
			return null;
		}
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof IXliffEditor) {
			String tshelp = System.getProperties().getProperty("TSHelp");
			String tsstate = System.getProperties().getProperty("TSState");
			if (tshelp == null || !"true".equals(tshelp) || tsstate == null || !"true".equals(tsstate)) {
				LoggerFactory.getLogger(TermBaseSearchHandler.class).error("Exception:key hs008 is lost.(Can't find the key)");
				System.exit(0);
			}
			IXliffEditor xliffEditor = (IXliffEditor) editor;
			IProject project = ((FileEditorInput) editor.getEditorInput()).getFile().getProject();
			ProjectConfiger projectConfig = ProjectConfigerFactory.getProjectConfiger(project);
			List<DatabaseModelBean> lstDatabase = projectConfig.getTermBaseDbs(false);
			if (lstDatabase == null || lstDatabase.size() == 0) {
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
						Messages.getString("handler.TermBaseSearchHandler.msgTitle"),
						Messages.getString("handler.TermBaseSearchHandler.msg"));
				return null;
			}
			String selectText = xliffEditor.getSelectPureText();
			if ((selectText == null || selectText.equals("")) && xliffEditor.getSelectedRowIds().size() == 1) {
				selectText = xliffEditor.getXLFHandler().getSrcPureText(xliffEditor.getSelectedRowIds().get(0));
				selectText = resetCeanString(selectText);
			}
			TermBaseSearchDialog dialog = new TermBaseSearchDialog(editor.getSite().getShell(), project,
					xliffEditor.getSrcColumnName(), xliffEditor.getTgtColumnName(), selectText.trim());
			dialog.open();
			if (selectText != null && !selectText.trim().equals("")) {
				dialog.search();
			}
		}
		return null;
	}
	
	public static String resetCeanString(String string) {
		string = string.replaceAll("&lt;", "<" ); 
		string = string.replaceAll("&gt;", ">"); 
//		string = string.replaceAll("&quot;", "\""); 
		string = string.replaceAll("&amp;", "&"); 
		return string;
	}

}
