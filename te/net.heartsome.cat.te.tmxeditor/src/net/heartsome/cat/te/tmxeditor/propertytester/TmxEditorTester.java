package net.heartsome.cat.te.tmxeditor.propertytester;

import net.heartsome.cat.te.tmxeditor.editor.TmxEditor;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;

import org.eclipse.core.expressions.PropertyTester;

public class TmxEditorTester extends PropertyTester {

	public TmxEditorTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		try {
			TmxEditorViewer viewer = TmxEditorViewer.getInstance();
			TmxEditor editor = viewer.getTmxEditor();
			return editor != null;
		} catch (NullPointerException e) {
			return false;
		}
	}
}
