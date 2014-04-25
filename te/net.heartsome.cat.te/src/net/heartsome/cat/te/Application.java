package net.heartsome.cat.te;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		OpenDocumentEventProcessor openDocProcessor = new OpenDocumentEventProcessor();

		Display display = PlatformUI.createDisplay();
		display.addListener(SWT.OpenDocument, openDocProcessor);

		try {
			IProduct product = Platform.getProduct();
			String id = product.getId();
			String hsVersion = "";
			if (id.equals("net.heartsome.cat.te.tmx_editor_product")) {
				hsVersion = "F";
			}
			System.getProperties().put("TSVersion", "88");
			System.getProperties().put("TSEdition", hsVersion);

			String versionDate = System.getProperty("date", "");
			String version = System.getProperty("version", "");
			System.getProperties().put("TSVersionDate", version + "." + versionDate);
			checkCleanValue();

			int returnCode = PlatformUI.createAndRunWorkbench(display,
					new ApplicationWorkbenchAdvisor(openDocProcessor));
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			else
				return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}

	/**
	 * 检查 osgi.clean 的值，如果为 true，就改为 false
	 * @param locale
	 *            ;
	 */
	public void checkCleanValue() {
		Location configArea = Platform.getInstallLocation();
		if (configArea == null) {
			return;
		}

		URL location = null;
		try {
			location = new URL(configArea.getURL().toExternalForm() + "configuration" + File.separator + "config.ini");
		} catch (MalformedURLException e) {
			// This should never happen
		}

		try {
			String fileName = location.getFile();
			File file = new File(fileName);
			fileName += ".bak";
			file.renameTo(new File(fileName));
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			BufferedWriter out = null;
			boolean isFind = false;
			try {
				String line = in.readLine();
				StringBuffer sbOut = new StringBuffer();
				while (line != null) {
					if (line.trim().equals("osgi.clean=true")) {
						sbOut.append("osgi.clean=false");
						isFind = true;
					} else {
						sbOut.append(line);
					}
					sbOut.append("\n");
					line = in.readLine();
				}
				if (isFind) {
					out = new BufferedWriter(new FileWriter(location.getFile()));
					out.write(sbOut.toString());
					out.flush();
				}
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				File tmpFile = new File(location.getFile() + ".bak");
				if (isFind) {
					if (tmpFile.exists()) {
						tmpFile.delete();
					}
				} else {
					tmpFile.renameTo(new File(location.getFile()));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
