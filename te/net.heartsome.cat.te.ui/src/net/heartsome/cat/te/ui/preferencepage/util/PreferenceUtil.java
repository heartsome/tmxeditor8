package net.heartsome.cat.te.ui.preferencepage.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import net.heartsome.cat.common.ui.dialog.HsPreferenceDialog;
import net.heartsome.cat.common.ui.languagesetting.LanguageCodesPreferencePage;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.te.ui.Activator;
import net.heartsome.cat.te.ui.preferencepage.system.SystemPreferencePage;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取首选项值的工具类
 * @author peason
 * @version
 * @since JDK1.6
 */
public final class PreferenceUtil {

	/**
	 * 获取项目属性的文本字段
	 * @return ;
	 */
	public static ArrayList<String> getProjectFieldList() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		ArrayList<String> lstField = new ArrayList<String>();
		int fieldCount = store
				.getInt("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.fieldCount");
		if (fieldCount > 0) {
			for (int i = 0; i < fieldCount; i++) {
				lstField.add(store
						.getString("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.field" + i));
			}
		}
		// 对中文按拼音排序
		Collator collatorChinese = Collator.getInstance(java.util.Locale.CHINA);
		Collections.sort(lstField, collatorChinese);
		return lstField;
	}

	/**
	 * 获取项目属性的属性字段
	 * @return key 为属性名称，value 为属性值集合
	 */
	public static LinkedHashMap<String, ArrayList<String>> getProjectAttributeMap() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		HashMap<String, ArrayList<String>> mapAttr = new HashMap<String, ArrayList<String>>();
		int attrNameCount = store
				.getInt("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrNameCount");
		// 对中文按拼音排序
		final Collator collatorChinese = Collator.getInstance(java.util.Locale.CHINA);
		LinkedHashMap<String, ArrayList<String>> linkedMapAttr = new LinkedHashMap<String, ArrayList<String>>();
		if (attrNameCount > 0) {
			for (int i = 0; i < attrNameCount; i++) {
				String strAttrName = store
						.getString("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName"
								+ i);
				int attrValCount = store
						.getInt("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName" + i
								+ ".count");
				ArrayList<String> lstAttrVal = new ArrayList<String>();
				if (attrValCount > 0) {
					for (int j = 0; j < attrValCount; j++) {
						lstAttrVal
								.add(store
										.getString("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName"
												+ i + ".attrVal" + j));
					}
				}
				Collections.sort(lstAttrVal, collatorChinese);
				mapAttr.put(strAttrName, lstAttrVal);
			}
			List<Entry<String, ArrayList<String>>> lstAttr = new ArrayList<Entry<String, ArrayList<String>>>(
					mapAttr.entrySet());
			Collections.sort(lstAttr, new Comparator<Entry<String, ArrayList<String>>>() {

				public int compare(Entry<String, ArrayList<String>> arg0, Entry<String, ArrayList<String>> arg1) {
					return collatorChinese.compare(arg0.getKey(), arg1.getKey());
				}
			});

			for (Entry<String, ArrayList<String>> entry : lstAttr) {
				linkedMapAttr.put(entry.getKey(), entry.getValue());
			}
		}

		return linkedMapAttr;
	}

	public static void openPreferenceDialog(IWorkbenchWindow window, final String defaultId) {
		PreferenceManager mgr = window.getWorkbench().getPreferenceManager();
		mgr.remove("net.heartsome.cat.ui.preferencePages.Perspectives");
		mgr.remove("org.eclipse.ui.preferencePages.Workbench");
		mgr.remove("org.eclipse.update.internal.ui.preferences.MainPreferencePage");
		mgr.remove("org.eclipse.help.ui.browsersPreferencePage");
		final Object[] defaultNode = new Object[1];
		HsPreferenceDialog dlg = new HsPreferenceDialog(window.getShell(), mgr);
		dlg.create();

		final List<Image> imageList = new ArrayList<Image>();
		dlg.getTreeViewer().setLabelProvider(new PreferenceLabelProvider() {
			public Image getImage(Object element) {
				String id = ((IPreferenceNode) element).getId();
				if (defaultId != null && id.equals(defaultId)) {
					defaultNode[0] = element;
				}
				Image image = null;
				if (SystemPreferencePage.ID.equals(id)) {
					// 系统菜单
					image = Activator.getImageDescriptor("images/preference/system/system.png").createImage();
					imageList.add(image);
					return image;
				} else if ("org.eclipse.ui.preferencePages.Keys".equals(id)) {
					// 系统 > 快捷键菜单
					image = Activator.getImageDescriptor("images/preference/system/keys.png").createImage();
					imageList.add(image);
					return image;
				} else if ("org.eclipse.ui.net.proxy_preference_page_context".equals(id)) {
					// 网络连接
					image = Activator.getImageDescriptor("images/preference/system/network.png").createImage();
					imageList.add(image);
					return image;
				}  else {
					return null;
				}
			}
		});

		if (defaultNode[0] != null) {
			dlg.getTreeViewer().setSelection(new StructuredSelection(defaultNode), true);
			dlg.getTreeViewer().getControl().setFocus();
		}

		dlg.open();

		// 清理资源
		for (Image img : imageList) {
			if (img != null && !img.isDisposed()) {
				img.dispose();
			}
		}
		imageList.clear();
	}

	

	/**
	 * 检查 osgi.clean 的值，如果为 true，就改为 false
	 * @param locale
	 *            ;
	 */
	public static void checkCleanValue() {
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
	
	public static boolean checkEdition() {
		String temp = System.getProperty("TSEdition");
		if (!"U".equals(temp) && !"F".equals(temp) && !"P".equals(temp) && !"L".equals(temp)) {
			return false;
		} else {
			return true;
		}
	}
}
