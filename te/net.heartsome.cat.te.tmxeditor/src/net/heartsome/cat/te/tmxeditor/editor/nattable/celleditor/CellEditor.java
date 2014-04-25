/**
 * TmxEditorCellEditor.java
 *
 * Version information :
 *
 * Date:2013-6-6
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor;

import net.heartsome.cat.te.tmxeditor.TmxEditorConstanst;
import net.sourceforge.nattable.edit.editor.AbstractCellEditor;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.IME;

/**
 * 源和目标单元编辑器
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class CellEditor extends AbstractCellEditor {

	private CellEditorTextViewer textViewer;
	private CellEditorCanonicalValue canonicalValue;


	@Override
	public void setCanonicalValue(Object canonicalValue) {
		this.canonicalValue = (CellEditorCanonicalValue) canonicalValue;
		textViewer.setTmxSegement(this.canonicalValue.getTuv());
	}

	@Override
	public Object getCanonicalValue() {
		this.canonicalValue.setNewFullValue(textViewer.getFullText());
		this.canonicalValue.setNewPureText(textViewer.getPureText());
		return this.canonicalValue;
	}

	@Override
	protected Control activateCell(Composite parent, Object originalCanonicalValue, Character initialEditValue) {
		if (originalCanonicalValue == null || !(originalCanonicalValue instanceof CellEditorCanonicalValue)) {
			return null;
		}

		StyledText textControl = createTextControl(parent);
		// init style
		IStyle cellStyle = getCellStyle();
	
		textControl.setBackground(GUIHelper.getColor(210, 210, 240));
		textControl.setForeground(cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
		//textControl.setFont(cellStyle.getAttributeValue(CellStyleAttributes.FONT));
		textControl.setLineSpacing(TmxEditorConstanst.SEGMENT_LINE_SPACING);
		textControl.setLeftMargin(TmxEditorConstanst.SEGMENT_LEFT_MARGIN);
		textControl.setRightMargin(TmxEditorConstanst.SEGMENT_RIGHT_MARGIN);
		textControl.setTopMargin(TmxEditorConstanst.SEGMENT_TOP_MARGIN);
		textControl.setBottomMargin(TmxEditorConstanst.SEGMENT_TOP_MARGIN);
		textControl.setIME(new IME(textControl, SWT.NONE));

		setCanonicalValue(originalCanonicalValue);
		textControl.forceFocus();
		return textControl;
	}

	// TODO
	@Override
	public void close() {
		super.close();
		CellEditorGlobalActionHanlder.getInstance().removeTextViewer();
		textViewer.dispose();
	}
	private StyledText createTextControl(Composite parent) {
		IStyle cellStyle = getCellStyle();
		int styled = HorizontalAlignmentEnum.getSWTStyle(cellStyle);
		styled |= SWT.MULTI | SWT.WRAP;
		textViewer = new CellEditorTextViewer(parent, styled);
		StyledText textControl = textViewer.getTextWidget();
		Font font = JFaceResources.getFont("net.heartsome.cat.te.ui.tmxeditor.font");
		if (font == null) {
			font = JFaceResources.getDefaultFont();
		}
		textControl.setFont(font);
		CellEditorGlobalActionHanlder.getInstance().addTextViewer(textViewer);
		textControl.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				CellEditorGlobalActionHanlder.getInstance().resetGlobalActionHandler();
			}

			@Override
			public void focusGained(FocusEvent e) {
				CellEditorGlobalActionHanlder.getInstance().updateGlobalActionHandler();
			}
		});
		return textControl;
	}

	public CellEditorTextViewer getTextViewer() {
		return textViewer;
	}
	
	public void insertCanonicalValue(Object canonicalValue) {
		StyledText text = textViewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}

		int offset = text.getCaretOffset();
		text.insert(canonicalValue.toString());
		text.setCaretOffset(offset + canonicalValue.toString().length());
	}
	
}
