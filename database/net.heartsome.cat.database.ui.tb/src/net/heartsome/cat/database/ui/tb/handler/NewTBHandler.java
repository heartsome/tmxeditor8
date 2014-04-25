package net.heartsome.cat.database.ui.tb.handler;

import net.heartsome.cat.database.ui.tb.wizard.NewTermDbWizard;
import net.heartsome.cat.database.ui.tb.wizard.TermDbManagerImportWizardDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.LoggerFactory;

/**
 * 新建术语库的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class NewTBHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String tshelp = System.getProperties().getProperty("TSHelp");
		String tsstate = System.getProperties().getProperty("TSState");
		if (tshelp == null || !"true".equals(tshelp) || tsstate == null || !"true".equals(tsstate)) {
			LoggerFactory.getLogger(NewTBHandler.class).error("Exception:key hs008 is lost.(Can't find the key)");
			System.exit(0);
		}
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		NewTermDbWizard wizard = new NewTermDbWizard();
		TermDbManagerImportWizardDialog dialog = new TermDbManagerImportWizardDialog(window.getShell(), wizard);
		dialog.open();
		return null;
	}

}
