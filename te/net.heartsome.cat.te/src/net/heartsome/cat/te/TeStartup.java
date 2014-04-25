package net.heartsome.cat.te;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;

public class TeStartup implements IStartup {

	public void earlyStartup() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				IContextService contextService = (IContextService) workbench.getService(IContextService.class);
				if(contextService != null){
					contextService.activateContext("net.heartsome.cat.te.context");
				}
			}
		});
	}

}
