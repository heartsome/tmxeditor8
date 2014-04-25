/**
 * TmxEditorTextPainter.java
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
package net.heartsome.cat.te.tmxeditor.editor.nattable.painter;

import java.util.List;
import java.util.regex.Matcher;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.common.ui.innertag.InnerTagRender;
import net.heartsome.cat.te.tmxeditor.TmxEditorConstanst;
import net.heartsome.cat.te.tmxeditor.TmxEditorUtils;
import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable;
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
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

/**
 * 源和目标列的绘制器
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxEditorTextPainter extends BackgroundPainter {
	private TmxEditorImpWithNattable editor;
	private Font font;
	private int ascent, descent;

	private final int tabSize = 4;
	private int tabWidth;

	private final int topPadding = TmxEditorConstanst.SEGMENT_TOP_MARGIN;
	private final int rightPadding = TmxEditorConstanst.SEGMENT_RIGHT_MARGIN;
	private final int bottomPadding = TmxEditorConstanst.SEGMENT_BOTTOM_MARGIN;
	private final int leftPadding = TmxEditorConstanst.SEGMENT_LEFT_MARGIN;
	private final int lineSpace = TmxEditorConstanst.SEGMENT_LINE_SPACING;

	private InnerTagRender tagRender;
	private TmxSegement tuv;

	public TmxEditorTextPainter(TmxEditorImpWithNattable editor) {
		this.editor = editor;
		Font font = JFaceResources.getFont("net.heartsome.cat.te.ui.tmxeditor.font");
		if (font == null) {
			font = JFaceResources.getDefaultFont();
		}
		loadFont(font);
		tagRender = new InnerTagRender();
	}

	@Override
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		TextLayout layout = getCellTextLayout(cell);
		int contentHeight = layout.getBounds().height;
		layout.dispose();
		tuv = null;
		contentHeight += topPadding;
		contentHeight += bottomPadding;
		contentHeight += 4;// 加上编辑模式下，StyledTextCellEditor的边框
		return contentHeight;
	}

	@Override
	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return super.getPreferredWidth(cell, gc, configRegistry);
	}

	@Override
	public void paintCell(LayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		super.paintCell(cell, gc, bounds, configRegistry);
		IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		setupGCFromConfig(gc, cellStyle);

		TextLayout layout = getCellTextLayout(cell);
		if (TmxEditorImpWithNattable.showNonPrinttingChar) {
			appendNonprintingStyle(layout);
		}
		Rectangle rectangle = cell.getBounds();
		layout.draw(gc, rectangle.x + leftPadding, rectangle.y + topPadding);
		String displayText = layout.getText();
		if (tuv == null) {
			layout.dispose();
			return;
		}
		List<InnerTagBean> innerTagBeans = tuv.getInnerTags();
		for (InnerTagBean innerTagBean : innerTagBeans) {
			String placeHolder = TmxInnerTagParser.getInstance().getPlaceHolderBuilder()
					.getPlaceHolder(innerTagBeans, innerTagBeans.indexOf(innerTagBean));
			int start = displayText.indexOf(placeHolder);
			if (start == -1) {
				continue;
			}
			Point p = layout.getLocation(start, false);
			int x = rectangle.x + p.x + leftPadding;
			x += TmxEditorConstanst.SEGMENT_LINE_SPACING;

			Point tagSize = tagRender.calculateTagSize(innerTagBean);
			int lineIdx = layout.getLineIndex(start);
			Rectangle r = layout.getLineBounds(lineIdx);
			int y = rectangle.y + p.y + topPadding + r.height / 2 - tagSize.y / 2;
			tagRender.draw(gc, innerTagBean, x, y);
		}

		layout.dispose();
		tuv = null;
	}

	private TextLayout getCellTextLayout(LayerCell cell) {
		int orientation = editor.getTable().getStyle() & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
		TextLayout layout = new TextLayout(editor.getTable().getDisplay());
		layout.setOrientation(orientation);
		layout.setSpacing(lineSpace);
		layout.setFont(font);
		layout.setAscent(ascent);
		layout.setDescent(descent); // 和 StyledTextEditor 同步
		layout.setTabs(new int[] { tabWidth });

		Rectangle rectangle = cell.getBounds();
		int width = rectangle.width - leftPadding - rightPadding - 1; // 减去编辑模式下左右边框
		layout.setWidth(width);
		TmxTU tu = (TmxTU) cell.getDataValue();
		String searchStr = null;
		if (cell.getColumnPosition() == editor.getSrcColumnIndex()) {
			// source
			tuv = tu.getSource();
			searchStr = editor.getSrcSearchStr();
		} else if (cell.getColumnPosition() == editor.getTgtColumnIndex()) {
			// target
			tuv = tu.getTarget();
			searchStr = editor.getTgtSearchStr();
		}
		if (tuv != null) {
			String displayText = resetRegularString(tuv.getTextTagPlaceHolder());
			if (TmxEditorImpWithNattable.showNonPrinttingChar) {
				displayText = displayText.replaceAll("\\n", TmxEditorConstanst.LINE_SEPARATOR_CHARACTER + "\n");
				displayText = displayText.replaceAll("\\t", TmxEditorConstanst.TAB_CHARACTER + "\u200B");
				displayText = displayText.replaceAll(" ", TmxEditorConstanst.SPACE_CHARACTER + "\u200B");
			}
			layout.setText(displayText);
			if(searchStr != null && searchStr.length() != 0){
				if(TmxEditorImpWithNattable.showNonPrinttingChar){
					searchStr = searchStr.replaceAll("\\n", TmxEditorConstanst.LINE_SEPARATOR_CHARACTER + "\n");
					searchStr = searchStr.replaceAll("\\t", TmxEditorConstanst.TAB_CHARACTER + "\u200B");
					searchStr = searchStr.replaceAll(" ", TmxEditorConstanst.SPACE_CHARACTER + "\u200B");
				}
				TextStyle style = new TextStyle(null, null, GUIHelper.COLOR_GREEN);
				List<StyleRange> ranges = TmxEditorUtils.calculateSearchStringStyleRange(displayText.toCharArray(), searchStr.toCharArray(), style);
				for (StyleRange range : ranges) {
					layout.setStyle(range, range.start, range.start + range.length - 1);
				}
			}
			List<InnerTagBean> innerTagBeans = tuv.getInnerTags();
			for (InnerTagBean innerTagBean : innerTagBeans) {
				String placeHolder = TmxInnerTagParser.getInstance().getPlaceHolderBuilder()
						.getPlaceHolder(innerTagBeans, innerTagBeans.indexOf(innerTagBean));
				int start = displayText.indexOf(placeHolder);
				if (start == -1) {
					continue;
				}
				TextStyle style = new TextStyle();
				Point rect = tagRender.calculateTagSize(innerTagBean);
				style.metrics = new GlyphMetrics(rect.y, 0, rect.x + TmxEditorConstanst.SEGMENT_LINE_SPACING * 2);
				layout.setStyle(style, start, start + placeHolder.length() - 1);
			}
		}
		return layout;
	}

	public void loadFont(Font font) {
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

	public Font getFont() {
		return this.font;
	}

	/** 将所有转义字符全部转换成原始状态，只适用于文本内容的显示 */
	private String resetRegularString(String input) {
		input = input.replaceAll("&lt;", "<");
		input = input.replaceAll("&gt;", ">");
		input = input.replaceAll("&quot;", "\"");
		input = input.replaceAll("&amp;", "&");
		input = input.replaceAll(System.getProperty("line.separator"), "\n");
		return input;
	}
	
	private void appendNonprintingStyle(TextLayout layout) {
		TextStyle style = null;
		String s = layout.getText();
		Matcher matcher = TmxEditorConstanst.NONPRINTING_PATTERN.matcher(s);
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			TextStyle st = layout.getStyle(start);
			if(st != null && st.background != null){
				style = new TextStyle(font, GUIHelper.getColor(new RGB(100, 100, 100)), st.background);
			} else {
				style = new TextStyle(font, GUIHelper.getColor(new RGB(100, 100, 100)), null);
			}
			layout.setStyle(style, start, end - 1);
		}
	}
}
