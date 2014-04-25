package net.heartsome.cat.te;

import java.util.Arrays;
import java.util.List;

import net.heartsome.cat.te.resource.Messages;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.menus.IMenuService;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the actions added to a workbench window.
 * Each window will be populated with new actions.
 */
@SuppressWarnings("restriction")
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	private final IWorkbenchWindow window;

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.

	// tool bar context menu
	private IWorkbenchAction lockToolBarAction;

	// file menu action
	private IWorkbenchAction exitAction;

	// edit menu action
	private IWorkbenchAction undoAction;
	private IWorkbenchAction redoAction;
	private IWorkbenchAction cutAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction pasteAction;
	private IWorkbenchAction deleteAction;
	private IWorkbenchAction findAction;

	// help menu action
	private IWorkbenchAction helpAction;
	// private IWorkbenchAction helpSearchAction;
	// private IWorkbenchAction dynamicHelpAction;
	// private Action aboutAction;

	/**
	 * Indicates if the action builder has been disposed
	 */
	private boolean isDisposed = false;

	/**
	 * The coolbar context menu manager.
	 */
	private MenuManager coolbarPopupMenuManager;

	/**
	 * @param configurer
	 */
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
		window = configurer.getWindowConfigurer().getWindow();
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
//		exitAction.setImageDescriptor(Activator.getImageDescriptor("images/file/logout.png"));
		exitAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.exitAction"));

		undoAction = ActionFactory.UNDO.create(window);
		undoAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.undoAction"));
		undoAction.setDisabledImageDescriptor(null);
		register(undoAction);

		redoAction = ActionFactory.REDO.create(window);
		redoAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.redoAction"));
		redoAction.setDisabledImageDescriptor(null);
		register(redoAction);

		cutAction = ActionFactory.CUT.create(window);
		cutAction.setImageDescriptor(Activator.getImageDescriptor("images/edit/cut.png"));
		cutAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.cutAction"));
		cutAction.setDisabledImageDescriptor(null);
		register(cutAction);

		copyAction = ActionFactory.COPY.create(window);
		copyAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.copyAction"));
		copyAction.setImageDescriptor(Activator.getImageDescriptor("images/edit/copy.png"));
		copyAction.setDisabledImageDescriptor(null);
		register(copyAction);

		pasteAction = ActionFactory.PASTE.create(window);
		pasteAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.pasteAction"));
		pasteAction.setImageDescriptor(Activator.getImageDescriptor("images/edit/paste.png"));
		pasteAction.setDisabledImageDescriptor(null);
		register(pasteAction);

		deleteAction = ActionFactory.DELETE.create(window);
		deleteAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.deleteAction"));
		deleteAction.setImageDescriptor(Activator.getImageDescriptor("images/edit/delete.png"));
		register(deleteAction);

		findAction = ActionFactory.FIND.create(window);
		findAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.findAction"));
		findAction.setImageDescriptor(Activator.getImageDescriptor("images/edit/search_replace.png"));
		register(findAction);

		lockToolBarAction = ActionFactory.LOCK_TOOL_BAR.create(window);
		register(lockToolBarAction);

		helpAction = ActionFactory.HELP_CONTENTS.create(window);
		helpAction.setText(Messages.getString("ts.ApplicationActionBarAdvisor.helpAction"));
		helpAction.setImageDescriptor(Activator.getImageDescriptor("images/help/help.png"));
		register(helpAction);

		removeUnusedAction();

	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		menuBar.add(new GroupMarker("view"));
		menuBar.add(new GroupMarker("translation"));
		menuBar.add(new GroupMarker("project"));
		menuBar.add(new GroupMarker("database"));
		menuBar.add(new GroupMarker("qa"));
		menuBar.add(new GroupMarker("advance"));
		menuBar.add(createHelpMenu());
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		// Set up the context Menu
		coolbarPopupMenuManager = new MenuManager();
		coolbarPopupMenuManager.add(new ActionContributionItem(lockToolBarAction));
		coolBar.setContextMenuManager(coolbarPopupMenuManager);
		IMenuService menuService = (IMenuService) window.getService(IMenuService.class);
		menuService.populateContributionManager(coolbarPopupMenuManager, "popup:windowCoolbarContextMenu");

		coolBar.add(new GroupMarker("group.file"));

		coolBar.add(new GroupMarker("group.search"));
		createToolItem(coolBar);

		coolBar.add(new GroupMarker("group.new.menu"));

		coolBar.add(new GroupMarker("group.undoredo"));
		
		coolBar.add(new GroupMarker("group.tu"));
		
		coolBar.add(new GroupMarker("group.tmxtool"));

		coolBar.add(new GroupMarker("group.tmxclear"));

		coolBar.add(new GroupMarker("group.copySource"));

		coolBar.add(new GroupMarker("group.completeTranslation"));

		coolBar.add(new GroupMarker("group.approve"));

		coolBar.add(new GroupMarker("group.addTerm"));

		coolBar.add(new GroupMarker("group.preview"));

		coolBar.add(new GroupMarker("group.tagoperation"));

		coolBar.add(new GroupMarker("group.sourceoperation"));

		coolBar.add(new GroupMarker("group.deleteTrans"));

		coolBar.add(new GroupMarker("group.changeLayout"));

		coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));

		coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_HELP));
		//未提供 24 位图标，屏蔽工具栏。
//		IToolBarManager toolbar = new ToolBarManager(coolBar.getStyle());
//		coolBar.add(new ToolBarContributionItem(toolbar, "help"));
//		toolbar.add(helpAction);
	}

	private IToolBarManager createToolItem(ICoolBarManager coolBar) {
		IToolBarManager toolBar = new ToolBarManager(coolBar.getStyle());
		coolBar.add(new ToolBarContributionItem(toolBar, "findreplace"));
		//没有设计 24 的图标，所以屏蔽工具栏
//		toolBar.add(cutAction);
//		toolBar.add(copyAction);
//		toolBar.add(pasteAction);
//		toolBar.add(findAction);
		return toolBar;
	}

	/**
	 * 创建文件菜单
	 * @return 返回文件菜单的 menu manager;
	 */
	private MenuManager createFileMenu() {
		MenuManager menu = new MenuManager(Messages.getString("ts.ApplicationActionBarAdvisor.menu.file"),
				IWorkbenchActionConstants.M_FILE); // &File
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		// 添加 new.ext group，这样 IDE 中定义的 Open File... 可以显示在最顶端
		menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		menu.add(new Separator());
		menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
		menu.add(new GroupMarker("xliff.switch"));
		menu.add(new GroupMarker("rtf.switch"));
		menu.add(new GroupMarker("xliff.split"));
		menu.add(new Separator());
		// 设置保存文件记录条数为 5 条
		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.RECENT_FILES, 5);
		menu.add(new GroupMarker(IWorkbenchActionConstants.HISTORY_GROUP));
		menu.add(exitAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}

	/**
	 * 创建编辑菜单
	 * @return 返回编辑菜单的 menu manager;
	 */
	private MenuManager createEditMenu() {
		MenuManager menu = new MenuManager(Messages.getString("ts.ApplicationActionBarAdvisor.menu.edit"),
				IWorkbenchActionConstants.M_EDIT); // &Edit
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
		menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
		menu.add(new Separator());
		menu.add(cutAction);
		menu.add(copyAction);
		menu.add(pasteAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
		menu.add(new Separator());
		menu.add(findAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		menu.add(new Separator());
		menu.add(new GroupMarker("taskEdit"));
		return menu;
	}

	/**
	 * 创建帮助菜单
	 * @return 返回帮助菜单的 menu manager;
	 */
	private MenuManager createHelpMenu() {
		MenuManager menu = new MenuManager(Messages.getString("ts.ApplicationActionBarAdvisor.menu.help"),
				IWorkbenchActionConstants.M_HELP);
		// menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(helpAction);
		// menu.add(helpSearchAction);
		// menu.add(dynamicHelpAction);
		// menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		menu.add(new GroupMarker("help.keyAssist"));
		menu.add(new Separator());
		menu.add(new GroupMarker("help.updatePlugin"));
		menu.add(new Separator());
		menu.add(new GroupMarker("help.license"));
		// 关于菜单需要始终显示在最底端
		menu.add(new GroupMarker("group.about"));
		// ActionContributionItem aboutItem = new ActionContributionItem(aboutAction);
		// aboutItem.setVisible(!Util.isMac());
		// menu.add(aboutItem);
		return menu;
	}

	/**
	 * 创建自定义的插件菜单 2012-03-07
	 * @return ;
	 */
	/*
	 * private MenuManager createAutoPluginMenu() { MenuManager menu = new MenuManager("asdfasd",
	 * "net.heartsome.cat.ts.ui.menu.plugin"); // menu = MenuManag
	 * 
	 * // menu.appendToGroup(groupName, item) menu.add(helpSearchAction); return menu; }
	 */

	@Override
	public void dispose() {
		if (isDisposed) {
			return;
		}
		isDisposed = true;
		IMenuService menuService = (IMenuService) window.getService(IMenuService.class);
		menuService.releaseContributions(coolbarPopupMenuManager);
		coolbarPopupMenuManager.dispose();
		super.dispose();
	}

	/**
	 * 移除无用的菜单项：<br/>
	 * File 菜单下的“open file...”和“Convert Line Delimiters To”
	 */
	private void removeUnusedAction() {
		ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
		IActionSetDescriptor[] actionSets = reg.getActionSets();

		List<String> actionSetIds = Arrays.asList("org.eclipse.ui.actionSet.openFiles",
				"org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo",
				"org.eclipse.ui.actions.showKeyAssistHandler", "org.eclipse.ui.edit.text.actionSet.navigation",
				"org.eclipse.ui.edit.text.actionSet.annotationNavigation");
		for (int i = 0; i < actionSets.length; i++) {
			if (actionSetIds.contains(actionSets[i].getId())) {
				IExtension ext = actionSets[i].getConfigurationElement().getDeclaringExtension();
				reg.removeExtension(ext, new Object[] { actionSets[i] });
			}
		}
	}
}
