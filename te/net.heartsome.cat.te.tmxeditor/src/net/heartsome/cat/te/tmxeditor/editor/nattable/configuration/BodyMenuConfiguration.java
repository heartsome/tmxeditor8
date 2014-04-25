/**
 * BodyMenuConfiguration.java
 *
 * Version information :
 *
 * Date:2013-9-6
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.editor.nattable.configuration;

import java.util.Collections;

import net.heartsome.cat.te.tmxeditor.resource.Messages;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;
import net.sourceforge.nattable.ui.menu.PopupMenuAction;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class BodyMenuConfiguration extends AbstractUiBindingConfiguration {

	private Menu bodyMenu;
	private NatTable table;

	public BodyMenuConfiguration(NatTable table) {
		this.table = table;
		createBodyMenu();
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, GridRegion.BODY,
				MouseEventMatcher.RIGHT_BUTTON), new PopupMenuAction(bodyMenu));
		uiBindingRegistry.unregisterMouseDragMode(new MouseEventMatcher(SWT.NONE, GridRegion.BODY,
				MouseEventMatcher.RIGHT_BUTTON));
	}

	private void createBodyMenu() {
		MenuManager menuMgr = new MenuManager();
		bodyMenu = menuMgr.createContextMenu(table.getShell());
		menuMgr.add(new CommandContributionItem(new CommandContributionItemParameter(PlatformUI.getWorkbench(), null,
				ActionFactory.CUT.getCommandId(), Collections.EMPTY_MAP, null, null, null, Messages
						.getString("tmxedtior.bodyMenuConfiguration.cut"), null, null,
				CommandContributionItem.STYLE_PUSH, null, false)));
		menuMgr.add(new CommandContributionItem(new CommandContributionItemParameter(PlatformUI.getWorkbench(), null,
				ActionFactory.COPY.getCommandId(), Collections.EMPTY_MAP, null, null, null, Messages
						.getString("tmxedtior.bodyMenuConfiguration.copy"), null, null,
				CommandContributionItem.STYLE_PUSH, null, false)));
		menuMgr.add(new CommandContributionItem(new CommandContributionItemParameter(PlatformUI.getWorkbench(), null,
				ActionFactory.PASTE.getCommandId(), Collections.EMPTY_MAP, null, null, null, Messages
						.getString("tmxedtior.bodyMenuConfiguration.paste"), null, null,
				CommandContributionItem.STYLE_PUSH, null, false)));
		menuMgr.add(new Separator());

		menuMgr.add(new CommandContributionItem(new CommandContributionItemParameter(PlatformUI.getWorkbench(), null,
				"net.heartsome.cat.te.tmxeditor.command.addtu", Collections.EMPTY_MAP, null, null, null, null, null,
				null, CommandContributionItem.STYLE_PUSH, null, false)));
		menuMgr.add(new CommandContributionItem(new CommandContributionItemParameter(PlatformUI.getWorkbench(), null,
				"net.heartsome.cat.te.tmxeditor.command.deletetu", Collections.EMPTY_MAP, null, null, null, null, null,
				null, CommandContributionItem.STYLE_PUSH, null, false)));
		
		menuMgr.add(new Separator());
		menuMgr.add(new CommandContributionItem(new CommandContributionItemParameter(PlatformUI.getWorkbench(), null,
				"net.heartsome.cat.te.ui.command.cleanSelectInnerTag", Collections.EMPTY_MAP, null, null, null, null, null,
				null, CommandContributionItem.STYLE_PUSH, null, false)));
	}

}
