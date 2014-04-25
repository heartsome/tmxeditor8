package net.heartsome.cat.te.ui.preferencepage;

/**
 * TE 应用中定义的首选项常量.
 * @author yule
 */
public interface IPreferenceConstants {

	/**
	 * 自动更新策略
	 */
	String SYSTEM_AUTO_UPDATE = "net.heartsome.cat.ts.ui.preferencepage.autoupdate";

	/**
	 * 启动时检查更新
	 */
	int SYSTEM_CHECK_UPDATE_WITH_STARTUP = 0;

	/**
	 * 每月检查更新
	 */
	int SYSTEM_CHECK_UPDATE_WITH_MONTHLY = 1;

	/**
	 * 每月检查更新时所选的日期
	 */
	String SYSTEM_CHECK_UPDATE_WITH_MONTHLY_DATE = "net.heartsome.cat.ts.ui.preferencepage.systemCheckUpdateWithMonthlyDate";

	/**
	 * 每周检查更新
	 */
	int SYSTEM_CHECK_UPDATE_WITH_WEEKLY = 2;

	/**
	 * 每周检查更新时所选的日期
	 */
	String SYSTEM_CHECK_UPDATE_WITH_WEEKLY_DATE = "net.heartsome.cat.ts.ui.preferencepage.systemCheckUpdateWithWeeklyDate";


	/**
	 * 从不检查更新
	 */
	int SYSTEM_CHECK_UPDATE_WITH_NEVER = 4;

	/**
	 * 用户界面语言
	 */
	String SYSTEM_LANGUAGE = "net.heartsome.cat.ts.ui.preferencepage.systemLanguage";

	/**
	 * 用户界面语言为英文
	 */
	int SYSTEM_LANGUAGE_WITH_EN = 0;

	/**
	 * 用户界面语言为简体中文
	 */
	int SYSTEM_LANGUAGE_WITH_ZH_CN = 1;

	/**
	 * 系统用户
	 */
	String SYSTEM_USER = "net.heartsome.cat.ts.ui.preferencepage.systemUser";

	/**
	 * XLIFF编辑器字体名称
	 */
	String TMX_EDITOR_FONT_NAME = "net.heartsome.cat.ts.ui.preferencepage.systemDefaultFontName";
	/**
	 * XLIFF编辑器字体大小
	 */
	String TMX_EDITOR_FONT_SIZE = "net.heartsome.cat.ts.ui.preferencepage.systemDefaultFontSize";

	

}
