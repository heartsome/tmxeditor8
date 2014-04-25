/**
 * TmxEditorSelectionLayerConfiguration.java
 *
 * Version information :
 *
 * Date:2013-6-4
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.editor.nattable.configuration;

import net.sourceforge.nattable.config.AggregateConfiguration;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.action.MoveSelectionAction;
import net.sourceforge.nattable.selection.action.MoveToFirstRowAction;
import net.sourceforge.nattable.selection.action.MoveToLastRowAction;
import net.sourceforge.nattable.selection.action.PageDownAction;
import net.sourceforge.nattable.selection.action.PageUpAction;
import net.sourceforge.nattable.selection.action.SelectAllAction;
import net.sourceforge.nattable.selection.action.SelectCellAction;
import net.sourceforge.nattable.selection.config.DefaultMoveSelectionConfiguration;
import net.sourceforge.nattable.selection.config.DefaultSelectionBindings;
import net.sourceforge.nattable.selection.config.DefaultSelectionStyleConfiguration;
import net.sourceforge.nattable.tickupdate.config.DefaultTickUpdateConfiguration;
import net.sourceforge.nattable.ui.action.IKeyAction;
import net.sourceforge.nattable.ui.action.IMouseAction;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.KeyEventMatcher;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Nattable 选中相关配置
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxEditorSelectionLayerConfiguration extends AggregateConfiguration {
	public TmxEditorSelectionLayerConfiguration() {
		addSelectionStyleConfig();
		addSelectionUIBindings();
		addTickUpdateConfig();
		addMoveSelectionConfig();
	}

	protected void addSelectionStyleConfig() {
		// 去掉表头选中样式和选中单元格样式
		DefaultSelectionStyleConfiguration configure = new DefaultSelectionStyleConfiguration() {
			@Override
			protected void configureHeaderHasSelectionStyle(IConfigRegistry configRegistry) {
			}

			@Override
			protected void configureHeaderFullySelectedStyle(IConfigRegistry configRegistry) {
			}

			@Override
			protected void configureSelectionAnchorStyle(IConfigRegistry configRegistry) {
			}
		};
		configure.selectionBgColor = GUIHelper.getColor(210, 210, 240);// Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION);
		configure.selectionFgColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		addConfiguration(configure);
	}

	/**
	 * @see XLIFFEditorCompositeLayerConfiguration#addEditingUIConfig() ;
	 */
	protected void addSelectionUIBindings() {
		addConfiguration(new DefaultSelectionBindings() {

			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				// Move up
				configureMoveUpBindings(uiBindingRegistry, new MoveSelectionAction(MoveDirectionEnum.UP));

				// Move down
				configureMoveDownBindings(uiBindingRegistry, new MoveSelectionAction(MoveDirectionEnum.DOWN));

				// // Move left
				// configureMoveLeftBindings(uiBindingRegistry, new MoveSelectionAction(MoveDirectionEnum.LEFT));
				//
				// // Move right
				// configureMoveRightBindings(uiBindingRegistry, new MoveSelectionAction(MoveDirectionEnum.RIGHT));

				// Page Up
				uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.PAGE_UP), new PageUpAction());

				// Page down
				uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.PAGE_DOWN), new PageDownAction());

				// Home - Move to first column
				// configureHomeButtonBindings(uiBindingRegistry, new MoveToFirstColumnAction());

				// End - Move to last column
				// configureEndButtonBindings(uiBindingRegistry, new MoveToLastColumnAction());

				 // Select all
				 uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, 'a'), new SelectAllAction());

				// Copy
				// uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, 'c'), new CopyDataAction());

				// Mouse bindings - select Cell
				configureBodyMouseClickBindings(uiBindingRegistry);

				// Mouse bindings - select columns
				configureColumnHeaderMouseClickBindings(uiBindingRegistry);

				// Mouse bindings - select rows
				configureRowHeaderMouseClickBindings(uiBindingRegistry);

				// Mouse bindings - Drag
				configureBodyMouseDragMode(uiBindingRegistry);
			}

			@Override
			protected void configureMoveUpBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
				uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_UP), action);
				uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.HOME),
						new MoveToFirstRowAction());
			}

			@Override
			protected void configureMoveDownBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
				uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_DOWN), action);
				uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CONTROL, SWT.END),
						new MoveToLastRowAction());
			}

			@Override
			protected void configureBodyMouseClickBindings(UiBindingRegistry uiBindingRegistry) {
				// （1）按下 Ctrl 键、Shift 键或者同时按下两者，则执行选中该行。
				// （2）只鼠标单击则进入编辑模式：见 XLIFFEditorCompositeLayerConfiguration#addEditingUIConfig()
				IMouseAction action = new SelectCellAction();
				uiBindingRegistry.registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.SHIFT), action);
				uiBindingRegistry.registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.CTRL), action);
				// uiBindingRegistry.registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.SHIFT |
				// SWT.CONTROL), action);
			}

			@Override
			protected void configureBodyMouseDragMode(UiBindingRegistry uiBindingRegistry) {
				// CellSelectionDragMode dragMode = new CellSelectionDragMode();
				// uiBindingRegistry.registerMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.NONE), dragMode);
				// uiBindingRegistry.registerMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.SHIFT), dragMode);
				// uiBindingRegistry.registerMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.CONTROL), dragMode);
				// uiBindingRegistry.registerMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.SHIFT | SWT.CONTROL),
				// dragMode);
			}
		});
	}

	protected void addTickUpdateConfig() {
		addConfiguration(new DefaultTickUpdateConfiguration());
	}

	protected void addMoveSelectionConfig() {
		addConfiguration(new DefaultMoveSelectionConfiguration());
	}
}
