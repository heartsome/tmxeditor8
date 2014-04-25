/**
 * LineNumberPainter.java
 *
 * Version information :
 *
 * Date:Mar 1, 2012
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.editor.nattable.painter;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.te.tmxeditor.TmxEditorConstanst;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.BackgroundPainter;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.CellStyleUtil;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

/**
 * 属性列绘制器
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class AttributePainter extends BackgroundPainter {
	NatTable table;
	private Font font;
	private int ascent, descent;

	private final int tabSize = 4;
	private int tabWidth;

	private final int topPadding = TmxEditorConstanst.SEGMENT_TOP_MARGIN;
	private final int rightPadding = TmxEditorConstanst.SEGMENT_RIGHT_MARGIN;
	private final int bottomPadding = TmxEditorConstanst.SEGMENT_BOTTOM_MARGIN;
	private final int leftPadding = TmxEditorConstanst.SEGMENT_LEFT_MARGIN;
	private final int lineSpace = TmxEditorConstanst.SEGMENT_LINE_SPACING;

	public AttributePainter(NatTable table) {
		this.table = table;
		Font font = JFaceResources.getDefaultFont();
		loadFont(font);
	}

	@Override
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		TextLayout layout = getCellTextLayout(cell);
		int contentHeight = layout.getBounds().height;
		layout.dispose();
		contentHeight += topPadding;
		contentHeight += bottomPadding;
		contentHeight += 4;// 加上编辑模式下，StyledTextCellEditor的边框
		return contentHeight;
	}

	@Override
	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		// TODO Auto-generated method stub
		return super.getPreferredWidth(cell, gc, configRegistry);
	}

	@Override
	public void paintCell(LayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		super.paintCell(cell, gc, bounds, configRegistry);
		IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		setupGCFromConfig(gc, cellStyle);

		TextLayout layout = getCellTextLayout(cell);
		Rectangle rectangle = cell.getBounds();
		layout.draw(gc, rectangle.x + leftPadding, rectangle.y + topPadding);
		layout.dispose();
	}

	private TextLayout getCellTextLayout(LayerCell cell) {
		int orientation = table.getStyle() & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
		TextLayout layout = new TextLayout(table.getDisplay());
		layout.setOrientation(orientation);
		layout.setSpacing(lineSpace);
		layout.setFont(font);
		layout.setAscent(ascent);
		layout.setDescent(descent); // 和 StyledTextEditor 同步
		layout.setTabs(new int[] { tabWidth });

		Rectangle rectangle = cell.getBounds();
		int width = rectangle.width - leftPadding - rightPadding;
		layout.setWidth(width);
		TmxTU tu = (TmxTU) cell.getDataValue();
		// String dispalyStr = TmxEditorUtils.tuAttr2Str4UI(tu);
		// if(dispalyStr.length() == 0){
		// dispalyStr = "N/A";
		// layout.setText(dispalyStr);
		// return layout;
		// }
		// createid: jason changeid: jason
		StringBuilder sb = new StringBuilder();
		// String creationid = tu.getCreationUser();
		// if(creationid != null && creationid.length() > 0){
		// sb.append("creationid: ").append(creationid);
		// }
		// String creationdate = tu.getCreationDate();
		// if(creationdate != null && creationdate.length()>0){
		// sb.append(" creationdate: ").append(DateUtils.formatDateFromUTC(creationdate));
		// }
		String changedate = tu.getChangeDate();
		List<StyleRange> styleRanges = new ArrayList<StyleRange>();
		int nextStart = 0;
		if (changedate != null && changedate.length() > 0) {
			String name = "changedate: ";
			sb.append(name).append(DateUtils.formatDateFromUTC(DateUtils.formatStringTime(changedate)));
			StyleRange s = new StyleRange();
			s.start = nextStart;
			s.length = name.length();
			nextStart = sb.length();
			styleRanges.add(s);
		}

		String changeid = tu.getChangeUser();
		if (changeid != null && changeid.length() > 0) {
			String name = " changeid: ";
			sb.append(name).append(changeid);
			StyleRange s = new StyleRange();
			s.start = nextStart;
			s.length = name.length();
			nextStart = sb.length();
			styleRanges.add(s);
		}
		String dispalyStr = sb.toString();
		if (dispalyStr.length() == 0) {
			dispalyStr = "N/A";
			layout.setText(dispalyStr);
			return layout;
		}
		layout.setText(dispalyStr);
		for (StyleRange styleRange : styleRanges) {
			TextStyle style = new TextStyle();
			style.foreground = GUIHelper.COLOR_GRAY;
			layout.setStyle(style, styleRange.start, styleRange.start + styleRange.length - 1);
		}
		return layout;
	}

	private void loadFont(Font font) {
		TextLayout layout = new TextLayout(Display.getDefault());
		try {
			if (font != null) {
				this.font = font;
				Font boldFont = getFont(SWT.BOLD), italicFont = getFont(SWT.ITALIC), boldItalicFont = getFont(SWT.BOLD
						| SWT.ITALIC);
				layout.setText("    ");
				layout.setFont(font);
				layout.setStyle(new TextStyle(font, null, null), 0, 0);
				layout.setStyle(new TextStyle(boldFont, null, null), 1, 1);
				layout.setStyle(new TextStyle(italicFont, null, null), 2, 2);
				layout.setStyle(new TextStyle(boldItalicFont, null, null), 3, 3);
				FontMetrics metrics = layout.getLineMetrics(0);
				ascent = metrics.getAscent() + metrics.getLeading();
				descent = metrics.getDescent();
				boldFont.dispose();
				italicFont.dispose();
				boldItalicFont.dispose();
				boldFont = italicFont = boldItalicFont = null;
			}
			layout.dispose();
			layout = new TextLayout(Display.getDefault());
			layout.setFont(this.font);
			StringBuffer tabBuffer = new StringBuffer(tabSize);
			for (int i = 0; i < tabSize; i++) {
				tabBuffer.append(' ');
			}
			layout.setText(tabBuffer.toString());
			tabWidth = layout.getBounds().width;
			layout.dispose();
		} finally {
			if (layout != null && !layout.isDisposed()) {
				layout.dispose();
			}
		}
	}

	public void setupGCFromConfig(GC gc, IStyle cellStyle) {
		Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
		Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);

		gc.setAntialias(GUIHelper.DEFAULT_ANTIALIAS);
		gc.setTextAntialias(GUIHelper.DEFAULT_TEXT_ANTIALIAS);
		gc.setFont(font);
		gc.setForeground(fg != null ? fg : GUIHelper.COLOR_LIST_FOREGROUND);
		gc.setBackground(bg != null ? bg : GUIHelper.COLOR_LIST_BACKGROUND);
	}

	private Font getFont(int style) {
		Device device = Display.getDefault();
		switch (style) {
		case SWT.BOLD:
			return new Font(device, getFontData(style));
		case SWT.ITALIC:
			return new Font(device, getFontData(style));
		case SWT.BOLD | SWT.ITALIC:
			return new Font(device, getFontData(style));
		default:
			return font;
		}
	}

	private FontData[] getFontData(int style) {
		FontData[] fontDatas = font.getFontData();
		for (int i = 0; i < fontDatas.length; i++) {
			fontDatas[i].setStyle(style);
		}
		return fontDatas;
	}
}
