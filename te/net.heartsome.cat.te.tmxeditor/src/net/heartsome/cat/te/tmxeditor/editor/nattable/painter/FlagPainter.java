package net.heartsome.cat.te.tmxeditor.editor.nattable.painter;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.BackgroundPainter;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * 状态列Painter
 * @author Leakey
 * @version
 * @since JDK1.5
 */
public class FlagPainter extends BackgroundPainter {

	@Override
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return 0;
	}
	
	@Override
	public void paintCell(LayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		super.paintCell(cell, gc, bounds, configRegistry);
	}

}
