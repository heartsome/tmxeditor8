package net.heartsome.cat.ts;

import java.io.File;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.help.SystemResourceUtil;
import net.heartsome.cat.ts.resource.Messages;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;
import net.heartsome.cat.ts.ui.util.PreferenceUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		try {
			PreferenceUtil.initProductEdition();
			if (!PreferenceUtil.checkEdition()) {
				MessageDialog.openInformation(display.getActiveShell(), Messages.getString("dialog.AboutDialog.msgTitle"),
						Messages.getString("dialog.AboutDialog.msg"));
				return IApplication.EXIT_OK;
			}
			initSystemLan();
			SystemResourceUtil.beforeload();
			PreferenceUtil.checkCleanValue();
			deleteErrorMemoryFile();
			int returnCode = PlatformUI.createAndRunWorkbench(display,
					new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}
	
	private void initSystemLan(){
		int lanId = Activator.getDefault().getPreferenceStore().getInt(IPreferenceConstants.SYSTEM_LANGUAGE);
		CommonFunction.setSystemLanguage(lanId == 0 ? "en" : "zh");
	}

	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) {
			return;
		}
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed()) {
					workbench.close();
				}
			}
		});
	}
	
	/**
	 * 删除错误记录文件，以修改产品第一次运行后，存储错误信息导致第二次打不开的情况	robert	2013-05-06
	 */
	private static void deleteErrorMemoryFile(){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath projectsIPath = root.getLocation().append(".metadata/.plugins/org.eclipse.core.resources/.projects");
		String errorFileLC = "";
		File file = null;
		for(IProject project : root.getProjects()){
			errorFileLC = projectsIPath.append(project.getName()).append(".markers").toOSString();
			file = new File(errorFileLC);
			if (file.exists()) {
				file.delete();
			}
			
			errorFileLC = projectsIPath.append(project.getName()).append(".markers.snap").toOSString();
			file = new File(errorFileLC);
			if (file.exists()) {
				file.delete();
			}
		}
		
		IPath rootFolderIpath = root.getLocation().append(".metadata/.plugins/org.eclipse.core.resources/.root");
		errorFileLC = rootFolderIpath.append(".markers").toOSString();
		file = new File(errorFileLC);
		if (file.exists()) {
			file.delete();
		}
		
		errorFileLC = rootFolderIpath.append(".markers.snap").toOSString();
		file = new File(errorFileLC);
		if (file.exists()) {
			file.delete();
		}
	}
	
}
