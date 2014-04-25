package net.heartsome.cat.te.ui.propertytester;

import net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileDataAccess;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditor;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;

import org.eclipse.core.expressions.PropertyTester;

public class DataAccessTester extends PropertyTester {

	public DataAccessTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		try {
			TmxEditorViewer viewer = TmxEditorViewer.getInstance();
			TmxEditor editor = viewer.getTmxEditor();
			AbstractTmxDataAccess acc = editor.getTmxDataAccess();
			return acc instanceof TmxLargeFileDataAccess;
			//for debug
//			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}
}
