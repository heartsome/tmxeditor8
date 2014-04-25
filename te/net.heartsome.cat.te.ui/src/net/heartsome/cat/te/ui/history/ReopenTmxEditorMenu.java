/**
 * ReopenMenu.java
 *
 * Version information :
 *
 * Date:2013-8-2
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.ui.history;

import java.io.File;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.tmxeditor.editor.history.TmxEditorHistory;
import net.heartsome.cat.te.tmxeditor.editor.history.TmxEditorHistoryItem;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("restriction")
public class ReopenTmxEditorMenu extends ContributionItem {

	public static final Logger LOGGER = LoggerFactory.getLogger(ReopenTmxEditorMenu.class);

	private TmxEditorHistory history = TmxEditorHistory.getInstance();

	private boolean showSeparator;

	private boolean dirty = true;

	private IMenuListener menuListener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
			dirty = true;
		}
	};

	// the maximum length for a file name; must be >= 4
	private static final int MAX_TEXT_LENGTH = 40;

	// only assign mnemonic to the first nine items
	private static final int MAX_MNEMONIC_SIZE = 9;

	public ReopenTmxEditorMenu() {

	}

	public ReopenTmxEditorMenu(String id) {
		super(id);
	}

	/**
	 * Returns the text for a history item. This may be truncated to fit within the MAX_TEXT_LENGTH.
	 */
	private String calcText(int index, TmxEditorHistoryItem item) {
		File f = new File(item.getPath());
		String name = f.getName();
		return calcText(index, name, f.getAbsolutePath(), Window.getDefaultOrientation() == SWT.RIGHT_TO_LEFT);
	}

	/**
	 * Return a string suitable for a file MRU list. This should not be called outside the framework.
	 * @param index
	 *            the index in the MRU list
	 * @param name
	 *            the file name
	 * @param toolTip
	 *            potentially the path
	 * @param rtl
	 *            should it be right-to-left
	 * @return a string suitable for an MRU file menu
	 */
	public static String calcText(int index, String name, String toolTip, boolean rtl) {
		StringBuffer sb = new StringBuffer();

		int mnemonic = index + 1;
		StringBuffer nm = new StringBuffer();
		nm.append(mnemonic);
		if (mnemonic <= MAX_MNEMONIC_SIZE) {
			nm.insert(nm.length() - (mnemonic + "").length(), '&'); //$NON-NLS-1$
		}
		//        sb.append(" "); //$NON-NLS-1$

		String fileName = name;
		String pathName = toolTip;
		if (pathName.equals(fileName)) {
			// tool tip text isn't necessarily a path;
			// sometimes it's the same as name, so it shouldn't be treated as a path then
			pathName = ""; //$NON-NLS-1$
		}
		IPath path = new Path(pathName);
		// if last segment in path is the fileName, remove it
		if (path.segmentCount() > 1 && path.segment(path.segmentCount() - 1).equals(fileName)) {
			path = path.removeLastSegments(1);
			pathName = path.toString();
		}

		if ((fileName.length() + pathName.length()) <= (MAX_TEXT_LENGTH - 4)) {
			// entire item name fits within maximum length
			sb.append(fileName);
			if (pathName.length() > 0) {
				sb.append("  ["); //$NON-NLS-1$
				sb.append(pathName);
				sb.append("]"); //$NON-NLS-1$
			}
		} else {
			// need to shorten the item name
			int length = fileName.length();
			if (length > MAX_TEXT_LENGTH) {
				// file name does not fit within length, truncate it
				sb.append(fileName.substring(0, MAX_TEXT_LENGTH - 3));
				sb.append("..."); //$NON-NLS-1$
			} else if (length > MAX_TEXT_LENGTH - 7) {
				sb.append(fileName);
			} else {
				sb.append(fileName);
				int segmentCount = path.segmentCount();
				if (segmentCount > 0) {
					length += 7; // 7 chars are taken for "  [...]"

					sb.append("  ["); //$NON-NLS-1$

					// Add first n segments that fit
					int i = 0;
					while (i < segmentCount && length < MAX_TEXT_LENGTH) {
						String segment = path.segment(i);
						if (length + segment.length() < MAX_TEXT_LENGTH) {
							sb.append(segment);
							sb.append(IPath.SEPARATOR);
							length += segment.length() + 1;
							i++;
						} else if (i == 0) {
							// append at least part of the first segment
							sb.append(segment.substring(0, MAX_TEXT_LENGTH - length));
							length = MAX_TEXT_LENGTH;
							break;
						} else {
							break;
						}
					}

					sb.append("..."); //$NON-NLS-1$

					i = segmentCount - 1;
					// Add last n segments that fit
					while (i > 0 && length < MAX_TEXT_LENGTH) {
						String segment = path.segment(i);
						if (length + segment.length() < MAX_TEXT_LENGTH) {
							sb.append(IPath.SEPARATOR);
							sb.append(segment);
							length += segment.length() + 1;
							i--;
						} else {
							break;
						}
					}

					sb.append("]"); //$NON-NLS-1$
				}
			}
		}
		final String process;
		if (rtl) {
			process = sb + " " + nm; //$NON-NLS-1$
		} else {
			process = nm + " " + sb; //$NON-NLS-1$
		}
		return TextProcessor.process(process, TextProcessor.getDefaultDelimiters() + "[]");//$NON-NLS-1$
	}

	/**
	 * Fills the given menu with menu items for all windows.
	 */
	public void fill(final Menu menu, int index) {

		if (getParent() instanceof MenuManager) {
			((MenuManager) getParent()).addMenuListener(menuListener);
		}

		int itemsToShow = WorkbenchPlugin.getDefault().getPreferenceStore().getInt(IPreferenceConstants.RECENT_FILES);
		if (itemsToShow == 0 || history == null) {
			return;
		}

		// Get items.
		TmxEditorHistoryItem[] historyItems = history.getItems();

		int n = Math.min(itemsToShow, historyItems.length);
		if (n <= 0) {
			return;
		}

		if (showSeparator) {
			new MenuItem(menu, SWT.SEPARATOR, index);
			++index;
		}

		final int menuIndex[] = new int[] { index };

		for (int i = 0; i < n; i++) {
			final TmxEditorHistoryItem item = historyItems[i];
			final int historyIndex = i;
			SafeRunner.run(new SafeRunnable() {
				public void run() throws Exception {
					String text =/*  (historyIndex + 1) + " " + item.getPath();*/calcText(historyIndex, item); 
					MenuItem mi = new MenuItem(menu, SWT.PUSH, menuIndex[0]);
					++menuIndex[0];
					mi.setText(text);
					mi.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							open(item);
						}
					});
				}

				public void handleException(Throwable e) {
					// just skip the item if there's an error,
					// e.g. in the calculation of the shortened name
					// WorkbenchPlugin.log(getClass(), "fill", e); //$NON-NLS-1$
					LOGGER.error("fill", e);
				}
			});
		}
		new MenuItem(menu, SWT.SEPARATOR, menuIndex[0]);
		dirty = false;
	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDynamic() {
		return true;
	}

	/**
	 * Reopens the editor for the given history item.
	 */
	private void open(TmxEditorHistoryItem item) {
		String filePath = item.getPath();
		File f = new File(filePath);
		final TmxEditorViewer tmxEditorViewer = TmxEditorViewer.getInstance();
		if (tmxEditorViewer == null || !f.exists() || f.isDirectory()) {
			String title = WorkbenchMessages.OpenRecent_errorTitle;
			String msg = NLS.bind(WorkbenchMessages.OpenRecent_unableToOpen, item.getPath());
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), title, msg);
			history.remove(item);
			return;
		}
		if (tmxEditorViewer.getTmxEditor() != null) {
			if(!tmxEditorViewer.closeTmx()){
				return;
			}
		}
		int type = item.getType();
		if (type == TmxEditorHistoryItem.TYPE_TMX) {
			tmxEditorViewer.open(f);
		} else if (type == TmxEditorHistoryItem.TYPE_HSTM) {
			DatabaseModelBean selectedVal = new DatabaseModelBean();
			selectedVal.setDbName(f.getName());
			selectedVal.setDbType(Constants.DBTYPE_SQLITE);
			selectedVal.setItlDBLocation(f.getParent());
			tmxEditorViewer.open(selectedVal);
		} else {
			String title = WorkbenchMessages.OpenRecent_errorTitle;
			String msg = NLS.bind(WorkbenchMessages.OpenRecent_unableToOpen, item.getPath());
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), title, msg);
			history.remove(item);
		}
	}

}
