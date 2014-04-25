package net.heartsome.cat.te.tmxeditor.preference;

import net.heartsome.cat.te.core.TeCoreConstant;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 参数初始化类
 * @author  robert	2013-10-15
 * @version 
 * @since   JDK1.6
 */
public class TMXEditorPreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = net.heartsome.cat.te.core.Activator.getDefault().getPreferenceStore();
		
		store.setDefault(TeCoreConstant.FILTER_ignoreCase, true);
		store.setDefault(TeCoreConstant.FILTER_ignoreTag, true);
		
		
	}
}
