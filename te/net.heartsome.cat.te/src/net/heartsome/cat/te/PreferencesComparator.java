package net.heartsome.cat.te;

import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceNode;
import org.eclipse.ui.model.ContributionComparator;
import org.eclipse.ui.model.IComparableContribution;

/**
 * 用户对首选项菜单进行排序的类
 * Ts中对首先项进行排序  [copy by yule]
 * @author peason
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("restriction")
public class PreferencesComparator extends ContributionComparator {
	public int category(IComparableContribution c) {
		if (c instanceof WorkbenchPreferenceNode) {
			String id = ((WorkbenchPreferenceNode) c).getId();
			if ("net.heartsome.cat.te.ui.preferencepage.system.SystemPreferencePage".equals(id)) {
				// 系统菜单
				return 1;
			}  else if ("org.eclipse.ui.preferencePages.Keys".equals(id)) {
				// 系统 > 快捷键菜单
				return 2;
			} else if ("org.eclipse.ui.net.proxy_preference_page_context".equals(id)) {
				// 系统 > 网络连接
				return 3;
			}else {
				return super.category(c);
			}
		} else {
			return super.category(c);
		}
	}

}
