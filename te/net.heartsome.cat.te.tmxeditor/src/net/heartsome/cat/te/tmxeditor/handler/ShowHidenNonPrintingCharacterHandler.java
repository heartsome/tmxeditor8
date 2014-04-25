/**
 * ShowHidenNonPrintingCharacter.java
 *
 * Version information :
 *
 * Date:2013-4-19
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.handler;

import java.util.Map;

import net.heartsome.cat.te.tmxeditor.Activator;
import net.heartsome.cat.te.tmxeditor.TmxEditorConstanst;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditor;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ShowHidenNonPrintingCharacterHandler extends AbstractHandler implements IElementUpdater {

	boolean isSelected = Activator.getDefault().getPreferenceStore()
			.getBoolean(TmxEditorConstanst.TMX_EDITOR_SHOWHIDEN_NONPRINTCHARACTER);

	public Object execute(ExecutionEvent event) throws ExecutionException {
		isSelected = !isSelected;
		TmxEditorViewer viewer = TmxEditorViewer.getInstance();
		if (viewer != null) {
			TmxEditor editor = viewer.getTmxEditor();
			if (editor != null) {
				editor.commit();
				TmxEditorImpWithNattable.showNonPrinttingChar = isSelected;
				editor.refreshUI();
				Activator.getDefault().getPreferenceStore()
						.setValue(TmxEditorConstanst.TMX_EDITOR_SHOWHIDEN_NONPRINTCHARACTER, isSelected);
			}
		}
		return null;
	}

	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		element.setChecked(isSelected);
	}

}
