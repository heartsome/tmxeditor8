package net.heartsome.cat.te.ui;


import net.heartsome.cat.te.ui.preferencepage.IPreferenceConstants;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.heartsome.cat.te.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);		
		plugin = this;	
		String fontName = getPreferenceStore().getString(IPreferenceConstants.TMX_EDITOR_FONT_NAME);
		int size = getPreferenceStore().getInt(IPreferenceConstants.TMX_EDITOR_FONT_SIZE);
		FontData fontData = new FontData();
		fontData.setHeight(size);
		fontData.setName(fontName);
		JFaceResources.getFontRegistry().put(Constants.TMX_EDITOR_TEXT_FONT, new FontData[]{fontData});
		System.setProperty("user.name", getPreferenceStore().getString(IPreferenceConstants.SYSTEM_USER));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
