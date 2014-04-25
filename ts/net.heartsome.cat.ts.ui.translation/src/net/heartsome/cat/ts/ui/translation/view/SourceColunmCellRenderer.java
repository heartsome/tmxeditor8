/**
 * MatchViewCellRenderer.java
 *
 * Version information :
 *
 * Date:Dec 26, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.translation.view;

import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.common.bean.ColorConfigBean;
import net.heartsome.cat.common.ui.utils.InnerTagUtil;
import net.heartsome.cat.ts.ui.grid.XGridCellRenderer;
import net.heartsome.cat.ts.ui.translation.comparator.Comparator;

import org.eclipse.jface.text.Position;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class SourceColunmCellRenderer extends XGridCellRenderer {

	// private TextLayout textLayout;

	private String tuSrcText;

	private ColorConfigBean colorConfigBean = ColorConfigBean.getInstance();

	/**
	 * @param tuSrcText
	 *            the tuSrcText to set
	 */
	public void setTuSrcText(String tuSrcText) {
		innerTagFactory.reset();
		this.tuSrcText = InnerTagUtil.resolveTag(innerTagFactory.parseInnerTag(tuSrcText.trim()));
	}

	/**
	 * {@inheritDoc}
	 */
	public void paint(GC gc, Object value) {
		GridItem item = (GridItem) value;
		gc.setFont(item.getFont(getColumn()));
		boolean drawBackground = true;

		boolean drawAsSelected = isSelected();
		if (isCellSelected()) {
			drawAsSelected = true;
		}
		gc.setForeground(item.getForeground(getColumn()));
		if (drawAsSelected) {
			gc.setBackground((Color) item.getParent().getData("selectedBgColor"));
		} else {
			if (item.getParent().isEnabled()) {
				Color back = item.getBackground(getColumn());
				if (back != null) {
					gc.setBackground(back);
				} else {
					drawBackground = false;
				}
			} else {
				gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			}
		}

		if (drawBackground) {
			gc.fillRectangle(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
		}

		// 创建 TextLayout
		TextLayout layout = getTextLayout(gc, item, getColumn(), false, false);
		String displayStr = layout.getText();
		// 附加内部标记样式前，先和源文比较，不一样的地方着色实现
		if (!tuSrcText.equals(displayStr)) {
			TextStyle style = new TextStyle(layout.getFont(), colorConfigBean.getSrcDiffFgColor(),
					colorConfigBean.getSrcDiffBgColor());
			List<Position> diff = Comparator.Compare(tuSrcText, displayStr);
			for (Iterator<Position> iterator = diff.iterator(); iterator.hasNext();) {
				Position position = iterator.next();
				layout.setStyle(style, position.offset, position.length);
			}
		}
		// 添加标记样式，并创建标记
		attachInnertTagStyle(gc, layout, true);
		try {
			int y = getBounds().y + textTopMargin + topMargin;
			y += getVerticalAlignmentAdjustment(layout.getBounds().height, getBounds().height);

			if (item.getParent().isAutoHeight()) {
				int textHeight = topMargin + textTopMargin;
				for (int cnt = 0; cnt < layout.getLineCount(); cnt++)
					textHeight += layout.getLineBounds(cnt).height;
				textHeight += textBottomMargin + bottomMargin;
				item.setData("itemHeight", textHeight);
			}
			
			Point selection = copyEnable.getSelectionRange(getColumn(),item);
			if (selection == null || selection.x == selection.y) {
				layout.draw(gc, getBounds().x + leftMargin, y);
			} else {
				int x = getBounds().x + leftMargin;
				int start = Math.max(0, selection.x);
				int end = Math.min(displayStr.length(), selection.y);
				layout.draw(gc, x, y, start, end - 1, getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT),
						getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
				attachInnertTagStyle(gc, layout, true);
			}

			if (item.getParent().getLinesVisible()) {
				if (isCellSelected()) {
					gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
				} else {
					gc.setForeground(item.getParent().getLineColor());
				}
				gc.drawLine(getBounds().x, getBounds().y + getBounds().height, getBounds().x + getBounds().width - 1,
						getBounds().y + getBounds().height);
				gc.drawLine(getBounds().x + getBounds().width - 1, getBounds().y,
						getBounds().x + getBounds().width - 1, getBounds().y + getBounds().height);
			}
		} finally {
			if (layout != null) {
				layout.dispose();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Point computeSize(GC gc, int wHint, int hHint, Object value) {
		GridItem item = (GridItem) value;

		gc.setFont(item.getFont(getColumn()));

		int x = 0;

		x += leftMargin;

		int y = 0;

		Image image = item.getImage(getColumn());
		if (image != null) {
			y = topMargin + image.getBounds().height + bottomMargin;
		}

		// MOPR-DND
		// MOPR: replaced this code (to get correct preferred height for cells in word-wrap columns)
		//
		// x += gc.stringExtent(item.getText(column)).x + rightMargin;
		//
		// y = Math.max(y,topMargin + gc.getFontMetrics().getHeight() + bottomMargin);
		//
		// with this code:

		int textHeight = 0;
		if (!isWordWrap()) {
			x += gc.textExtent(item.getText(getColumn())).x + rightMargin;

			textHeight = topMargin + textTopMargin + gc.getFontMetrics().getHeight() + textBottomMargin + bottomMargin;
		} else {
			int plainTextWidth;
			if (wHint == SWT.DEFAULT)
				plainTextWidth = getBounds().width - x - rightMargin;
			else
				plainTextWidth = wHint - x - rightMargin;

			TextLayout currTextLayout = new TextLayout(gc.getDevice());
			currTextLayout.setFont(gc.getFont());
			currTextLayout.setText(item.getText(getColumn()));
			currTextLayout.setAlignment(getAlignment());
			currTextLayout.setWidth(plainTextWidth < 1 ? 1 : plainTextWidth);

			x += plainTextWidth + rightMargin;

			textHeight += topMargin + textTopMargin;
			for (int cnt = 0; cnt < currTextLayout.getLineCount(); cnt++)
				textHeight += currTextLayout.getLineBounds(cnt).height;
			textHeight += textBottomMargin + bottomMargin;

			currTextLayout.dispose();
		}

		y = Math.max(y, textHeight);

		return new Point(x, y);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean notify(int event, Point point, Object value) {
		return false;
	}
}
