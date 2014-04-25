package net.heartsome.cat.database.ui.tm.handler;

import java.util.Collections;
import java.util.List;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog;
import net.heartsome.cat.database.ui.tm.resource.Messages;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.services.IEvaluationService;
import org.slf4j.LoggerFactory;

/**
 * 相关搜索的 Handler
 * @author peason
 * @version
 * @since JDK1.6
 */
public class ConcordanceSearchHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!isEnabled()) {
			return null;
		}
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof IXliffEditor) {
			String tshelp = System.getProperties().getProperty("TSHelp");
			String tsstate = System.getProperties().getProperty("TSState");
			if (tshelp == null || !"true".equals(tshelp) || tsstate == null || !"true".equals(tsstate)) {
				LoggerFactory.getLogger(ConcordanceSearchHandler.class).error("Exception:key hs008 is lost.(Can't find the key)");
				System.exit(0);
			}
			IXliffEditor xliffEditor = (IXliffEditor) editor;
			String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";

			IEditorPart editorRefer = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
			if (editorRefer.getSite().getId().equals(XLIFF_EDITOR_ID)) {
				// IProject project = ((FileEditorInput) editorRefer.getEditorInput()).getFile().getProject();
				IFile file = ((FileEditorInput) editorRefer.getEditorInput()).getFile();
				ProjectConfiger projectConfig = ProjectConfigerFactory.getProjectConfiger(file.getProject());
				List<DatabaseModelBean> lstDatabase = projectConfig.getAllTmDbs();
				if (lstDatabase.size() == 0) {
					MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
							Messages.getString("handler.ConcordanceSearchHandler.msgTitle"),
							Messages.getString("handler.ConcordanceSearchHandler.msg"));
					return null;
				}

				String selectText = xliffEditor.getSelectPureText();
				if ((selectText == null || selectText.equals("")) && xliffEditor.getSelectedRowIds().size() == 1) {
					selectText = xliffEditor.getXLFHandler().getSrcPureText(xliffEditor.getSelectedRowIds().get(0));
				} else if (selectText == null) {
					selectText = "";
				}
				ConcordanceSearchDialog dialog = new ConcordanceSearchDialog(editorRefer.getSite().getShell(), file,
						xliffEditor.getSrcColumnName(), xliffEditor.getTgtColumnName(), selectText.trim());
				dialog.open();
				if (selectText != null && !selectText.trim().equals("")) {
					dialog.initGroupIdAndSearch();
					IWorkbenchPartSite site = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getSite();
					ICommandService commandService = (ICommandService) site.getService(
							ICommandService.class);
					Command command = commandService
							.getCommand(ActionFactory.COPY.getCommandId());
					IEvaluationService evalService = (IEvaluationService) site.getService(
							IEvaluationService.class);
					IEvaluationContext currentState = evalService.getCurrentState();
					ExecutionEvent executionEvent = new ExecutionEvent(command, Collections.EMPTY_MAP, this, currentState);
					try {
						command.executeWithChecks(executionEvent);
					} catch (Exception e1) {}
				}
			}
		}
		return null;
	}

}
