package net.heartsome.cat.te.tmxeditor.view.provider;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.te.tmxeditor.view.PropertiesView;
import net.heartsome.cat.te.tmxeditor.view.PropertiesView.TableViewerInput;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TuPropContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getElements(Object inputElement) {

		List<String[]> list = new ArrayList<String[]>();
		if (inputElement instanceof TableViewerInput) {
			TableViewerInput input = (TableViewerInput) inputElement;

			switch (input.category) {
			case PropertiesView.TU_ATTRS:
				TmxTU tu = input.selection == null || input.selection.getDisplayTu() == null ? new TmxTU() : input.selection.getDisplayTu();
				// list.add(new String[] { "tuid", tu.getTuId() == null ? "" : tu.getTuId() });
				list.add(new String[] { "creationid", tu.getCreationUser() == null ? "" : tu.getCreationUser() });
				list.add(new String[] { "creationdate", tu.getCreationDate() == null ? "" : DateUtils.formatDateFromUTC(
						DateUtils.formatStringTime(tu.getCreationDate())) });
				list.add(new String[] { "changeid", tu.getChangeUser() == null ? "" : tu.getChangeUser() });
				list.add(new String[] { "changedate", tu.getChangeDate() == null ? "" : DateUtils.formatDateFromUTC(
						DateUtils.formatStringTime(tu.getChangeDate())) });
				list.add(new String[] { "creationtool", tu.getCreationTool() == null ? "" : tu.getCreationTool() });
				list.add(new String[] { "creationtoolversion",
						tu.getCreationToolVersion() == null ? "" : tu.getCreationToolVersion() });
				break;
			case PropertiesView.TUV_ATTRS:
				TmxSegement seg = input.selection == null || input.selection.getDisplayTu() == null ? new TmxSegement() : 
					input.selection.getSelectedColumn() > 1 ? input.selection.getDisplayTu().getTarget() : input.selection.getDisplayTu().getSource();
				list.add(new String[] { "creationid", seg.getCreationUser() == null ? "" : seg.getCreationUser() });
				list.add(new String[] { "creationdate", seg.getCreationDate() == null ? "" : DateUtils.formatDateFromUTC(
						DateUtils.formatStringTime(seg.getCreationDate())) });
				list.add(new String[] { "changedate", seg.getChangeDate() == null ? "" : DateUtils.formatDateFromUTC(
						DateUtils.formatStringTime(seg.getChangeDate())) });
				list.add(new String[] { "changeid", seg.getChangeUser() == null ? "" : seg.getChangeUser() });
				list.add(new String[] { "creationtool", seg.getCreationTool() == null ? "" : seg.getCreationTool() });
				list.add(new String[] { "creationtoolversion",
						seg.getCreationToolVersion() == null ? "" : seg.getCreationToolVersion() });
				break;
			case PropertiesView.TU_NODE_NOTE:
				Object[] obj = new Object[5];
				List<TmxNote> notes = null;
				if (input.selection  != null && input.selection.getDisplayTu() != null) {
					notes = input.selection.getDisplayTu().getNotes();
				}
				if (notes == null) {
					for (int i = 0; i < obj.length; i++) {
						obj[i] = new String[]{"", ""};
					}
				} else if (notes.size() < 5) {
					int i = 0;
					while (i < notes.size()) {
						obj[i] = notes.get(i);
						i++;
					}
					while (i < 5) {
						obj[i] = new String[]{"", ""};
						i++;
					}
				} else {
					obj = notes.toArray();
				}
				return obj;
			case PropertiesView.TU_NODE_PROPS:
				Object[] obj2 = new Object[5];
				List<TmxProp> props = null;
				if (input.selection != null && input.selection.getDisplayTu() != null) {
					props = input.selection.getDisplayTu().getProps();
				}
				if (props == null) {
					for (int i = 0; i < obj2.length; i++) {
						obj2[i] = new String[]{"", ""};
					}
				} else if (props.size() < 5) {
					int i = 0;
					while (i < props.size()) {
						obj2[i] = props.get(i);
						i++;
					}
					while (i < 5) {
						obj2[i] = new String[]{"", ""};
						i++;
					}
				} else {
					obj2 = props.toArray();
				}
				return obj2;
			}
		}
		return list.toArray();
	}
}
