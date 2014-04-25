/**
 * TmxEditorHisitory.java
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
package net.heartsome.cat.te.tmxeditor.editor.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ximpleware.VTDGen;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxEditorHistory {
	public static final Logger LOGGER = LoggerFactory.getLogger(TmxEditorHistory.class);
	public static TmxEditorHistory instance = new TmxEditorHistory();
	private final String historyStoreFile = ResourcesPlugin.getWorkspace().getRoot().getLocation()
			.append(".metadata/.preference/.history.xml").toOSString();
	private IMemento memento;
	private final String rootTag = "file";

	public static TmxEditorHistory getInstance() {
		return instance;
	}

	/**
	 * The maximum of entries in the history.
	 */
	public static final int MAX_SIZE = 15;

	/**
	 * The list of editor entries, in FIFO order.
	 */
	private List<TmxEditorHistoryItem> fifoList = new ArrayList<TmxEditorHistoryItem>(MAX_SIZE);

	/**
	 * Constructs a new history.
	 */
	private TmxEditorHistory() {
		final File f = new File(historyStoreFile);
		if (!f.exists() || f.length() <= 0 || !new VTDGen().parseFile(f.getAbsolutePath(), true)) {
			creatStoreFile();
		}
		try {
			FileInputStream input = new FileInputStream(f);
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
			memento = XMLMemento.createReadRoot(reader);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		restoreState(memento);
		PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {

			@Override
			public boolean preShutdown(IWorkbench workbench, boolean forced) {
				try {
					Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
					Element element = document.createElement("hisitory");
					document.appendChild(element);
					XMLMemento xm = new XMLMemento(document, element);
					saveState(xm);

					FileOutputStream stream = new FileOutputStream(f);
					OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
					xm.save(writer);
					writer.close();
				} catch (Exception e) {
					LOGGER.error("", e);
				}
				return true;
			}

			@Override
			public void postShutdown(IWorkbench workbench) {

			}
		});
	}

	/**
	 * Adds an item to the history. Added in fifo fashion.
	 */
	public void add(int type, String file) {
		add(new TmxEditorHistoryItem(type, file), 0);
	}

	private void add(TmxEditorHistoryItem item, int index) {
		remove(item);

		// Remove the oldest one
		if (fifoList.size() == MAX_SIZE) {
			fifoList.remove(MAX_SIZE - 1);
		}

		// Add the new item.
		fifoList.add(index < MAX_SIZE ? index : MAX_SIZE - 1, item);
	}

	/**
	 * Returns an array of editor history items. The items are returned in order of most recent first.
	 */
	public TmxEditorHistoryItem[] getItems() {
		refresh();
		TmxEditorHistoryItem[] array = new TmxEditorHistoryItem[fifoList.size()];
		fifoList.toArray(array);
		return array;
	}

	/**
	 * Refresh the editor list. Any stale items are removed. Only restored items are considered.
	 */
	public void refresh() {
		Iterator<TmxEditorHistoryItem> iter = fifoList.iterator();
		while (iter.hasNext()) {
			TmxEditorHistoryItem item = iter.next();
			int type = item.getType();
			if (type != TmxEditorHistoryItem.TYPE_HSTM && type != TmxEditorHistoryItem.TYPE_TMX) {
				iter.remove();
				continue;
			}
			String filePath = item.getPath();
			File f = new File(filePath);
			if (!f.exists()) {
				iter.remove();
			}
		}
	}

	/**
	 * Removes the given history item.
	 */
	public void remove(TmxEditorHistoryItem item) {
		if (item == null) {
			return;
		}
		if (!fifoList.remove(item)) {
			Iterator<TmxEditorHistoryItem> iter = fifoList.iterator();
			while (iter.hasNext()) {
				TmxEditorHistoryItem t = iter.next();
				if (t.getPath().equals(item.getPath()) && t.getType() == item.getType()) {
					iter.remove();
				}
			}
		}
	}

	/**
	 * 创建文件
	 */
	private void creatStoreFile() {
		File parentFile = new File(historyStoreFile).getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(historyStoreFile);
			output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8"));
			output.write("<history>\n</history>".getBytes("UTF-8"));
		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (Exception e2) {
				LOGGER.error("", e2);
			}
		}
	}

	/**
	 * Restore the most-recently-used history from the given memento.
	 * @param memento
	 *            the memento to restore the mru history from
	 */
	public IStatus restoreState(IMemento memento) {
		IMemento[] mementos = memento.getChildren(rootTag);
		for (int i = 0; i < mementos.length; i++) {
			TmxEditorHistoryItem item = new TmxEditorHistoryItem(mementos[i]);
			if ((item.getType() == TmxEditorHistoryItem.TYPE_HSTM || item.getType() == TmxEditorHistoryItem.TYPE_TMX)
					&& item.getPath() != null && item.getPath().length() != 0)
				add(item, fifoList.size());
		}
		return Status.OK_STATUS;
	}

	/**
	 * Save the most-recently-used history in the history file
	 */
	public IStatus saveState(IMemento memento) {
		Iterator<TmxEditorHistoryItem> iterator = fifoList.iterator();
		while (iterator.hasNext()) {
			TmxEditorHistoryItem item = iterator.next();
			if (item.canSave()) {
				IMemento itemMemento = memento.createChild(rootTag);
				item.saveState(itemMemento);
			}
		}
		return Status.OK_STATUS;
	}

}
