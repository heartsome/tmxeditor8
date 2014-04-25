/**
 * CompositeLayerConfiguration.java
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

import net.heartsome.cat.te.tmxeditor.editor.nattable.action.KeyEditAction;
import net.heartsome.cat.te.tmxeditor.editor.nattable.action.MouseEditAction;
import net.sourceforge.nattable.config.AggregateConfiguration;
import net.sourceforge.nattable.edit.config.DefaultEditBindings;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import net.sourceforge.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import net.sourceforge.nattable.layer.CompositeLayer;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.IKeyEventMatcher;
import net.sourceforge.nattable.ui.matcher.KeyEventMatcher;
import net.sourceforge.nattable.ui.matcher.LetterOrDigitKeyEventMatcher;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class CompositeLayerConfiguration extends AggregateConfiguration {
	public CompositeLayerConfiguration(CompositeLayer compositeLayer) {
		addAlternateRowColoringConfig(compositeLayer);
//		addEditingHandlerConfig();
		addEditingUIConfig();
	}


	protected void addEditingUIConfig() {
		addConfiguration(new DefaultEditBindings() {

			KeyEditAction action = new KeyEditAction();

			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				// 在用户点击 Enter 键时，进入编辑状态
				uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.CR), action);
				// 在用户点击小键盘的 Enter 键时，进入编辑状态
				uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.KEYPAD_CR), action);
	
				// 在用户点击 whitespace 键时，进入编辑状态
				uiBindingRegistry.registerKeyBinding(new IKeyEventMatcher() {
					public boolean matches(KeyEvent event) {
						return event.character == ' ';
					}
				}, action);
				// 在用户输入字母或数字时，进入编辑状态
				uiBindingRegistry.registerKeyBinding(new LetterOrDigitKeyEventMatcher(), action);
				// 在用户单击时，进入编辑状态
				uiBindingRegistry.registerFirstMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new MouseEditAction());
				uiBindingRegistry.unregisterMouseDragMode(MouseEventMatcher.bodyLeftClick(SWT.NONE));
			}
		});
	}

//	protected void addEditingHandlerConfig() {
//		addConfiguration(new AbstractLayerConfiguration<CompositeLayer>() {
//
//			@Override
//			public void configureTypedLayer(CompositeLayer layer) {
//				layer.registerCommandHandler(new EditCellCommandHandler());				
//			}
//			
//			@Override
//			public void configureRegistry(IConfigRegistry configRegistry) {
//				configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.NEVER_EDITABLE);
////				configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, new DefaultDataValidator());
//			}
//		});
//		
//	}

	protected void addAlternateRowColoringConfig(CompositeLayer compositeLayer) {
		DefaultRowStyleConfiguration configuration = new DefaultRowStyleConfiguration();
		configuration.oddRowBgColor = GUIHelper.COLOR_WHITE;
		configuration.evenRowBgColor = GUIHelper.COLOR_WHITE;
		addConfiguration(configuration);
		compositeLayer.setConfigLabelAccumulatorForRegion(GridRegion.BODY, new AlternatingRowConfigLabelAccumulator());
	}
}
