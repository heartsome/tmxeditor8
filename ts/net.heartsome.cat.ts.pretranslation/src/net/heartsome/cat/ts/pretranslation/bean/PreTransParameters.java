/**
 * PreTransParameters.java
 *
 * Version information :
 *
 * Date:2012-5-8
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.pretranslation.bean;

import net.heartsome.cat.ts.pretranslation.Activator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class PreTransParameters implements IPropertyChangeListener {

	private int updateStrategy = 0;
	private boolean lockFullMatch = false;
	private boolean lockContextMatch = false;

	private IPreferenceStore ps;

	private static PreTransParameters instance;

	public static PreTransParameters getInstance() {
		if (instance == null) {
			instance = new PreTransParameters();
		}
		return instance;
	}

	private PreTransParameters() {
		this.ps = Activator.getDefault().getPreferenceStore();
		if (ps != null) {
			this.ps.addPropertyChangeListener(this);
			this.loadParameters();
		}
	}

	private void loadParameters() {
		updateStrategy = ps.getInt(IPreTransConstants.UPDATE_STRATEGY);
		lockFullMatch = ps.getBoolean(IPreTransConstants.LOCK_FULL_MATCH);
		lockContextMatch = ps.getBoolean(IPreTransConstants.LOCK_CONTEXT_MATCH);
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		loadParameters();
	}

	/**
	 * 预翻译时，当前已经存在匹配时的更新策略
	 * @return the updateStrategy
	 */
	public int getUpdateStrategy() {
		return updateStrategy;
	}

	/**
	 * 是否锁定完全匹配
	 * @return the lockFullMatch
	 */
	public boolean isLockFullMatch() {
		return lockFullMatch;
	}

	/**
	 * 是否锁定上下文匹配
	 * @return the lockContextMatch
	 */
	public boolean isLockContextMatch() {
		return lockContextMatch;
	}
}
