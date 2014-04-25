package net.heartsome.cat.database.ui.tm.handler;

import net.heartsome.cat.database.ui.tm.wizard.ImportTmxWizardDialog;
import net.heartsome.cat.database.ui.tm.wizard.NewTmDbWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.LoggerFactory;

/**
 * 新建记忆库的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class NewTMHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String tshelp = System.getProperties().getProperty("TSHelp");
		String tsstate = System.getProperties().getProperty("TSState");
		if (tshelp == null || !"true".equals(tshelp) || tsstate == null || !"true".equals(tsstate)) {
			LoggerFactory.getLogger(NewTMHandler.class).error("Exception:key hs008 is lost.(Can't find the key)");
			System.exit(0);
		}
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		NewTmDbWizard wizard = new NewTmDbWizard();
		ImportTmxWizardDialog dialog = new ImportTmxWizardDialog(window.getShell(), wizard);
		dialog.open();
		return null;
	}

}
