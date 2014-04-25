/**
 * TmxEditorHistoryItem.java
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxEditorHistoryItem {
	/** 历史类型，Tmx 文件 */
	public static final int TYPE_TMX = 1;
	/** 历史类型，HS TM */
	public static final int TYPE_HSTM = 2;

	private int type = -1;
	private String path;
	private IMemento memento;

	public TmxEditorHistoryItem(int type, String path) {
		this.type = type;
		this.path = path;
	}

	/**
	 * Constructs a new item from a memento.
	 */
	public TmxEditorHistoryItem(IMemento memento) {
		this.memento = memento;
	}

	public int getType() {
		if (isRestored() && type != -1) {
			return type;
		} else if (memento != null) {
			Integer t = memento.getInteger("type");
			if (t != null) {
				type = t;
			}
		}
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getPath() {
		if (isRestored() && path != null && path.length() != 0) {
			return path;
		} else if (memento != null) {
			String path = memento.getString("path");
			if (path != null) {
				return path;
			}
		}
		return ""; //$NON-NLS-1$
	}

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Returns whether this item has been restored from the memento.
	 */
	public boolean isRestored() {
		return memento == null;
	}

	/**
	 * Returns whether this history item can be saved.
	 */
	public boolean canSave() {
		return !isRestored() || (path != null && path.length() != 0);
	}

	/**
	 * Saves the object state in the given memento.
	 * @param memento
	 *            the memento to save the object state in
	 */
	public IStatus saveState(IMemento memento) {
		if (!isRestored()) {
			memento.putMemento(this.memento);
		} else if ((type == TYPE_TMX || type == TYPE_HSTM) && path != null && path.length() != 0) {
			memento.putInteger("type", type);
			memento.putString("path", path);
		}
		return Status.OK_STATUS;
	}
}
