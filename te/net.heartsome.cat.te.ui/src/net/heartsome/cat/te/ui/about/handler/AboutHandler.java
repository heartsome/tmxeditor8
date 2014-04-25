package net.heartsome.cat.te.ui.about.handler;

import net.heartsome.cat.te.ui.about.ui.AboutDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 关于...
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class AboutHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
//		String tshelp = System.getProperties().getProperty("TSHelp");
//		String tsstate = System.getProperties().getProperty("TSState");
//		if (tshelp == null || !"true".equals(tshelp) || tsstate == null || !"true".equals(tsstate)) {
//			LoggerFactory.getLogger(AboutHandler.class).error("Exception:key hs008 is lost.(Can't find the key)");
//			System.exit(0);
//		}
		Shell shell = HandlerUtil.getActiveShell(event);
//		String version = System.getProperty("TSEdition");
//		String version2 = System.getProperty("TSVersionDate");
//		if (version == null || version2 == null || version.equals("") || version2.equals("")) {
//			MessageDialog.openInformation(shell, Messages.getString("dialog.AboutDialog.msgTitle"),
//					Messages.getString("dialog.AboutDialog.msg"));
//			PlatformUI.getWorkbench().close();
//		} else {
//			SystemResourceUtil.load();
			AboutDialog dialog = new AboutDialog(shell);
			dialog.open();
//		}
		return null;
	}

}
