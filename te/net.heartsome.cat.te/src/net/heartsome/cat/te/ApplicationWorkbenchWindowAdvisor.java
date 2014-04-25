package net.heartsome.cat.te;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.heartsome.cat.p2update.autoupdate.AutomaticUpdate;
import net.heartsome.cat.te.core.utils.TeCoreUtils;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.tmxeditor.editor.history.OpenRecord;
import net.heartsome.cat.te.ui.preferencepage.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	public static final Logger LOGGER = LoggerFactory.getLogger(ApplicationWorkbenchWindowAdvisor.class);
	private static final String AUTO_UPDATE_FLAG = "AUTO_UPDATE_FLAG";
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShowCoolBar(true);
		configurer.setShowProgressIndicator(true);
		configurer.setShowStatusLine(false);
		Rectangle clientArea = Display.getDefault().getClientArea();
		configurer.setInitialSize(new Point(clientArea.width, clientArea.height));
	}

	@Override
	public void postWindowOpen() {
		TeCoreUtils.closeQAViewer();
		reconverOpenFile();
		// 自动检查更新
		automaticCheckUpdate();
	}
	

	/**
	 * 恢复上次未正常关闭的文件
	 *  ;
	 */
	private void reconverOpenFile() {
		final String mainFile = new OpenRecord().getOpenRecord();
		if (mainFile == null || mainFile.length() == 0) {
			return;
		}
		File f = new File(mainFile);
		if (f.isDirectory() || !f.exists()) {
			return;
		}

		// 临时文件夹
		String fileParentLC = f.getParentFile().getAbsolutePath();
		String tmxTempFolderLC = fileParentLC + File.separator + "." + (f.getName()) + "_folder";
		File tmxTempFolder = new File(tmxTempFolderLC);
		if (!tmxTempFolder.exists()) {
			return;
		}
		final File[] subFiles = tmxTempFolder.listFiles();
		if (subFiles == null || subFiles.length == 0) {
			return;
		}
		final List<String> sfs = new ArrayList<String>();
		for (File sf : subFiles) {
			sfs.add(sf.getAbsolutePath());
		}
		TmxEditorViewer.getInstance().recoverOpen(mainFile, sfs);
	}
	
	private void automaticCheckUpdate() {
		// 自动检查更新
		final IPreferenceStore prefStore = net.heartsome.cat.te.ui.Activator.getDefault().getPreferenceStore();
		int updatePolicy = prefStore.getInt(IPreferenceConstants.SYSTEM_AUTO_UPDATE);
		boolean flg = false;
		if (updatePolicy == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_NEVER) {
			return;
		} else if (updatePolicy == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_STARTUP) {
			// 启动时检查更新
			flg = true;
		} else if (updatePolicy == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_MONTHLY) {
			// 每月 xx 日检查更新
			int day = prefStore.getInt(IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_MONTHLY_DATE);
			Calendar c = Calendar.getInstance();
			int cYear = c.get(Calendar.YEAR);
			int cMoth = c.get(Calendar.MONTH) + 1;
			int cDay = c.get(Calendar.DAY_OF_MONTH);
			String preUpdateDay = prefStore.getString("AUTO_UPDATE_FLAG");
			if (preUpdateDay.equals("")) {
				if (cDay == day) {
					flg = true;
					prefStore.setValue("AUTO_UPDATE_FLAG", cYear + "-" + cMoth + "-" + cDay);
				}
			} else {
				String[] ymdStr = preUpdateDay.split("-");
				Calendar uc = Calendar.getInstance();
				int uYeaer = Integer.parseInt(ymdStr[0]);
				int uMonth = Integer.parseInt(ymdStr[1]);
				int uDay = Integer.parseInt(ymdStr[2]);
				uc.set(uYeaer, uMonth - 1, uDay);
				if(cDay == day && c.getTime().compareTo(uc.getTime()) > 0){
					flg = true;
					prefStore.setValue("AUTO_UPDATE_FLAG", cYear + "-" + cMoth + "-" + cDay);
				}else if( cDay > day && (uYeaer < cYear || uMonth < cMoth )){
					flg = true;
					prefStore.setValue("AUTO_UPDATE_FLAG", cYear + "-" + cMoth + "-" + cDay);
				}
			}
		} else if (updatePolicy == IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_WEEKLY) {
			// 每周 xx 日检查更新
			int weekday = prefStore.getInt(IPreferenceConstants.SYSTEM_CHECK_UPDATE_WITH_WEEKLY_DATE);
			Calendar c = Calendar.getInstance();
			int cWeekDay = c.get(Calendar.DAY_OF_WEEK);
			int cYear = c.get(Calendar.YEAR);
			int cMoth = c.get(Calendar.MONTH) + 1;
			int cDay = c.get(Calendar.DAY_OF_MONTH);
			String preUpdateDay = prefStore.getString(AUTO_UPDATE_FLAG);
			if (preUpdateDay.equals("")) {
				if (cWeekDay == weekday) {
					flg = true;
					prefStore.setValue(AUTO_UPDATE_FLAG, cYear + "-" + cMoth + "-" + cDay);
				}
			} else {
				String[] ymdStr = preUpdateDay.split("-");
				Calendar uc = Calendar.getInstance();
				int uYeaer = Integer.parseInt(ymdStr[0]);
				uc.set(uYeaer, Integer.parseInt(ymdStr[1]) - 1, Integer.parseInt(ymdStr[2]));

				if (cWeekDay == weekday && c.getTime().compareTo(uc.getTime()) > 0) {
					flg = true;
					prefStore.setValue(AUTO_UPDATE_FLAG, cYear + "-" + cMoth + "-" + cDay);
				}else if(cWeekDay > weekday && (uYeaer < cYear || uc.get(Calendar.WEEK_OF_YEAR) < c.get(Calendar.WEEK_OF_YEAR))){
					flg = true;
					prefStore.setValue(AUTO_UPDATE_FLAG, cYear + "-" + cMoth + "-" + cDay);
				}
			}
		}

		if (!flg) {
			return;
		}
		AutomaticUpdate checker = new AutomaticUpdate();
		checker.checkForUpdates();
	}
}
