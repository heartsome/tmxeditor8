package net.heartsome.cat.te.ui.propertytester;

import net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileDataAccess;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditor;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;

import org.eclipse.core.expressions.PropertyTester;

public class SelectFileDataTester extends PropertyTester {

	public SelectFileDataTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		try {
			TmxEditorViewer viewer = TmxEditorViewer.getInstance();
			TmxEditor editor = viewer.getTmxEditor();
			AbstractTmxDataAccess acc = editor.getTmxDataAccess();
			if(acc instanceof TmxLargeFileDataAccess){
				 if(editor.getSelectIdentifiers().length==0){
					 return false;
				 }else{
					 return true;
				 }
			}
			return false;
		} catch (NullPointerException e) {
			return false;
		}
	}
}
