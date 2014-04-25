package net.heartsome.cat.te.tmxeditor.view;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.common.innertag.factory.XliffInnerTagFactory;
import net.heartsome.cat.common.ui.innertag.InnerTagRender;
import net.heartsome.cat.common.ui.utils.InnerTagUtil;
import net.heartsome.cat.te.core.qa.IQAResultViewer;
import net.heartsome.cat.te.core.qa.QAConstant;
import net.heartsome.cat.te.core.qa.QAControl;
import net.heartsome.cat.te.core.qa.QAResultBean;
import net.heartsome.cat.te.tmxeditor.TmxEditorConstanst;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditor;
import net.heartsome.cat.te.tmxeditor.editor.TmxEditorViewer;
import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable;
import net.heartsome.cat.te.tmxeditor.resource.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableCellResizeListener;
import de.kupzog.ktable.KTableDefaultModel;
import de.kupzog.ktable.KTableModel;
import de.kupzog.ktable.SWTX;
import de.kupzog.ktable.renderers.FixedCellRenderer;
import de.kupzog.ktable.renderers.TextCellRenderer;

/**
 * 品质检查结果视图
 * @author  robert	2013-09-23
 * @version 
 * @since   JDK1.6
 */
public class QAResultViewer extends ViewPart implements PropertyChangeListener, IQAResultViewer{
	// UNDO 这个　id 是怎么回事？
	public static final String ID = "";
	private Composite parent;
	private QAControl qaControl;
	/** 列表中所显示的数据，这个是用来排序的，其值随列表数据删除时删除 */
	private List<QAResultBean> dataList = new ArrayList<QAResultBean>();
	private TmxEditorImpWithNattable nattable = null;
	private TmxEditor editor = null;
	
	private KTable table;
	private KtableModel tableModel;
	
	public final static Logger logger = LoggerFactory.getLogger(QAResultViewer.class.getName());

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		createTable();
	}
	
	
	public void createTable() {
		table = new KTable(parent, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWTX.FILL_WITH_LASTCOL | SWT.WRAP);
		tableModel = new KtableModel();
		table.setModel(tableModel);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setColorRightBorder(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		table.setColorLeftBorder(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		table.setColorTopBorder(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		table.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				locationRow();
			}
		});

		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == QAConstant.QA_CENTERKEY_1 || e.keyCode == QAConstant.QA_CENTERKEY_2) {
					locationRow();
				}
			}
		});
		
		table.addCellResizeListener(new KTableCellResizeListener() {
			
			public void rowResized(int arg0, int arg1) {
				tableModel.textRenderer.clearRowHeiMap();
				table.redraw();
			}
			
			public void columnResized(int arg0, int arg1) {
				tableModel.textRenderer.clearRowHeiMap();
				table.redraw();
			}
		});
		
		table.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				tableModel.textRenderer.clearRowHeiMap();
				table.redraw();
			}
		});
	}
	

	@Override
	public void setFocus() {
		
	}
	
	@Override
	public void registLister(QAControl control) {
		this.qaControl = control;
		this.qaControl.listeners.addPropertyChangeListener(this);
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		/*
		 * 备注，传过来的数据是一个 ArrayList<QAResultBean>, 每组数据都是相同的 tuID 或　tuPK
		 */
		if ("printData".equals(evt.getPropertyName())) {
			try {
				Display.getDefault().syncExec(new Runnable() {
					@SuppressWarnings("unchecked")
					public void run() {
						Object obj = evt.getNewValue();
						if (obj instanceof List) {
							List<QAResultBean> objList = (List<QAResultBean>) obj;
							if (objList.size() <= 0) {
								return;
							}
							
							dataList.addAll(objList);
							table.redraw();
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(Messages.getString("view.QAResultViewer.LOGG.dataInpuError"), e);
			}
		}
	}
	
	
	@Override
	public void clearTableData() {
		dataList.clear();
		tableModel.textRenderer.clearRowHeiMap();
		table.redraw();
	}
	
	/**
	 * 双击定位
	 */
	private void locationRow(){
		int[] selectRow = table.getRowSelection();
		
		if (selectRow.length <= 0) {
			return;
		}
		// 获取第一行选择的值
		QAResultBean bean = dataList.get(selectRow[0] - 1);
		
		if (nattable == null) {
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = workbenchPage.findView(TmxEditorViewer.ID);
			if (view == null) {
				return;
			}
			TmxEditorViewer tmxEditorViewer = (TmxEditorViewer) view;
			editor = tmxEditorViewer.getTmxEditor();
			if (editor == null) {
				return;
			}
			nattable = editor.getTmxEditorImpWithNattable();
		}
		
		if (nattable != null && !nattable.isDispose()) {
			// 获取第一行选择的值
			// UNDO 这里应该通过　tuid 进行定位
			editor.resetFilterBeforLocation(qaControl.getOriginalSrcLang(), qaControl.getOriginalTgtLang());
			nattable.selectCell(nattable.getTgtColumnIndex(), Integer.parseInt(bean.getLineNumber()) - 1);
		}
	}
	
	
	
	/**
	 * 控制 ktable 的数据显示
	 * @author robert
	 *
	 */
	private class KtableModel extends KTableDefaultModel{
		private Map<String, Integer> belongMap = new HashMap<String, Integer>();
		
		private final FixedCellRenderer fixedRenderer = new FixedCellRenderer(
				FixedCellRenderer.STYLE_FLAT | TextCellRenderer.INDICATION_FOCUS_ROW);
		
		public final TextRenderer textRenderer = new TextRenderer();
		
		public KtableModel() {
			initialize();
		}
		
		public int getFixedHeaderColumnCount() {
			return 0;
		}

		public int getFixedHeaderRowCount() {
			return 1;
		}

		public int getFixedSelectableColumnCount() {
			return 0;
		}

		public int getFixedSelectableRowCount() {
			return 0;
		}

		public int getRowHeightMinimum() {
			return 20;
		}

		public boolean isColumnResizable(int col) {
			// 第一列不允许更改列宽
			return !(col == 0);
		}

		public boolean isRowResizable(int arg0) {
			return false;
		}

		public KTableCellEditor doGetCellEditor(int arg0, int arg1) {
			return null;
		}
		
		public int getInitialRowHeight(int row) {
			if (row==0) {
				return 22;
			}else {
				return 50;
			}
		}

		public KTableCellRenderer doGetCellRenderer(int col, int row) {
			if (isFixedCell(col, row)) {
				return fixedRenderer;
			} else {
				return textRenderer;
			}
		}
		
		public void doSetContentAt(int arg0, int arg1, Object arg2) {
			// do nothing
		}

		public int doGetColumnCount() {
			return 4;
		}

		public Object doGetContentAt(int col, int row) {
			if (row == 0) {
				switch (col) {
				case 0:
					return Messages.getString("view.QAResultViewer.columnNameType");
				case 1:
					return Messages.getString("view.QAResultViewer.columnNameLine");
				case 2:
					return Messages.getString("view.QAResultViewer.columnNameSrcText");
				case 3:
					return Messages.getString("view.QAResultViewer.columnNameTgtText");
				default:
					return "";
				}
			}else {
				if (dataList.size() > 0) {
					QAResultBean bean = dataList.get(row - 1);
					switch (col) {
					case 0:
						return bean.getQaTypeText();
					case 1:
						return bean.getLineNumber();
					case 2:
						return bean.getSrcContent() == null ? "" : bean.getSrcContent();
					case 3:
						return bean.getTgtContent() == null ? "" : bean.getTgtContent();
					default:
						return "";
					}
				}else {
					return "";
				}
			}
		}

		@Override
		public int doGetRowCount() {
			return dataList.size() + 1;
		}

		@Override
		public int getInitialColumnWidth(int col) {
			table.getVerticalBar().getSize();
			int lastWidth = table.getBounds().width - table.getVerticalBar().getSize().x;
			switch (col) {
			case 0:
				return (int)(lastWidth * 0.09);
			case 1:
				return (int)(lastWidth * 0.05);
			case 2:
				return (int)(lastWidth * 0.43);
			case 3:
				return (int)(lastWidth * 0.43);
			case 4:
			default:
				return 0;
			}
		}


		@Override
		public Point doBelongsToCell(int col, int row) {
			if (isFixedCell(col, row)){
	        	return new Point(col, row);
	        }
	        QAResultBean bean = dataList.get(row - 1);
	        
	        if (bean.getMergeID() != null && !bean.getMergeID().isEmpty()) {
	        	if (belongMap.get(bean.getMergeID()) != null) {
	        		if (col < 1) {
	        			if (row <= belongMap.get(bean.getMergeID())) {
	        				belongMap.put(bean.getMergeID(), row);
						}else {
							return new Point(col, belongMap.get(bean.getMergeID()));
						}
					}
				}else {
					belongMap.put(bean.getMergeID(), row);
				}
			}
	        return new Point(col, row);
		}
	}
	
	
	/**
	 * 级别列的　renderer,主要用于绘画图片。	
	 * @author robert	2013-10-24
	 */
	private class TextRenderer implements KTableCellRenderer {
		protected Display display;
		protected PlaceHolderEditModeBuilder placeHolderBuilder = new PlaceHolderEditModeBuilder();
		protected XliffInnerTagFactory innerTagFactory = new XliffInnerTagFactory(placeHolderBuilder);
		protected InnerTagRender tagRender = new InnerTagRender();

		private final int topPadding = TmxEditorConstanst.SEGMENT_TOP_MARGIN;
		private final int rightPadding = TmxEditorConstanst.SEGMENT_RIGHT_MARGIN;
		private final int bottomPadding = TmxEditorConstanst.SEGMENT_BOTTOM_MARGIN;
		private final int leftPadding = TmxEditorConstanst.SEGMENT_LEFT_MARGIN;
		private final int lineSpace = TmxEditorConstanst.SEGMENT_LINE_SPACING;
		private Color borderColor = null;

		
		private Map<Integer, Integer> rowHeightMap = new HashMap<Integer, Integer>();
		
		public TextRenderer() {
			display = Display.getCurrent();
			borderColor = display.getSystemColor(SWT.COLOR_GRAY);
		}

		public int getOptimalWidth(GC gc, int col, int row, Object content,
				boolean fixed, KTableModel model) {
			// UNDO 这个方法有什么用？到现在为止还不晓得，我哪个去。
//			if (col == 2) {
//				System.out.println(Math.max(gc.stringExtent(content.toString()).x + 8, 20));
//				
//				String text = SWTX.wrapText(gc, content.toString(), model.getRowHeight(row)-6);
//		        int w =  SWTX.getCachedStringExtent(gc, text).y;
//		        w+=6;
//		        System.out.println(w);
//		        return w;
//		        
//			}
//			return Math.max(gc.stringExtent(content.toString()).x + 8, 20);
			return 100;
		}

		public void drawCell(GC gc, Rectangle rect, int col, int row,
				Object content, boolean focus, boolean fixed, boolean clicked,
				KTableModel model) {
			Color backColor;
			
			if (focus) {
				backColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
			} else {
				backColor = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
			}
			Color oldForgeColor = gc.getForeground();
			
			if (col == 2 || col == 3) {
				innerTagFactory.reset();
				TextLayout layout = new TextLayout(display);
				layout.setWidth(model.getColumnWidth(col) - leftPadding - rightPadding);
				
				String displayText = InnerTagUtil.resolveTag(innerTagFactory.parseInnerTag((String) content));
				
				gc.setBackground(backColor);
				gc.fillRectangle(rect);
				gc.setForeground(borderColor);
				// 最后一列不画右边
				if (col != 3) {
					gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.height + rect.y);
				}
				gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
				gc.setForeground(oldForgeColor);
				layout.setTabs(new int[]{17});
				layout.setText(displayText);
				
				List<InnerTagBean> innerTagBeans = innerTagFactory.getInnerTagBeans();
				for (InnerTagBean innerTagBean : innerTagBeans) {
					String placeHolder = placeHolderBuilder.getPlaceHolder(innerTagBeans, innerTagBeans.indexOf(innerTagBean));
					int start = displayText.indexOf(placeHolder);
					if (start == -1) {
						continue;
					}
					TextStyle style = new TextStyle();
					Point point = tagRender.calculateTagSize(innerTagBean);
					style.metrics = new GlyphMetrics(point.y, 0, point.x + lineSpace * 2);
					layout.setStyle(style, start, start + placeHolder.length() - 1);
				}
				layout.draw(gc, rect.x + leftPadding, rect.y + topPadding);

				int curHeight = layout.getBounds().height + topPadding + bottomPadding;
				if (rowHeightMap.get(row) == null || (rowHeightMap.get(row) != null && curHeight > rowHeightMap.get(row))) {
					rowHeightMap.put(row, curHeight);
				}
				
				// UNDO 这里控制自动换行的，还需要更好的设计模式。
				if (col == 3) {
					if (rowHeightMap.get(row) != model.getRowHeight(row)) {
						model.setRowHeight(row, rowHeightMap.get(row));
						table.redraw();
					}
				}
				
				for (InnerTagBean innerTagBean : innerTagBeans) {
					String placeHolder = placeHolderBuilder.getPlaceHolder(innerTagBeans,
							innerTagBeans.indexOf(innerTagBean));
					int start = displayText.indexOf(placeHolder);
					if (start == -1) {
						continue;
					}
					Point p = layout.getLocation(start, false);
					int x = rect.x + p.x + leftPadding;
					x += lineSpace;

					Point tagSize = tagRender.calculateTagSize(innerTagBean);
					int lineIdx = layout.getLineIndex(start);
					Rectangle r = layout.getLineBounds(lineIdx);
					int y = rect.y + p.y + topPadding + r.height / 2 - tagSize.y / 2;
					tagRender.draw(gc, innerTagBean, x, y);
				}
				
				layout.dispose();
			}else {
				gc.setBackground(backColor);
				gc.fillRectangle(rect);
				gc.setForeground(borderColor);
				gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.height + rect.y);
				gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
				gc.setForeground(oldForgeColor);
				String text = SWTX.wrapText(gc, content.toString(), model.getColumnWidth(col) - 2);
				gc.drawText(text, rect.x + leftPadding, rect.y + topPadding, true);
				int curHeight = gc.textExtent(text).y + topPadding + bottomPadding;
				
				if (rowHeightMap.get(row) == null || (rowHeightMap.get(row) != null && curHeight > rowHeightMap.get(row))) {
					rowHeightMap.put(row, curHeight);
				}
			}
		}
		
		public void clearRowHeiMap(){
			rowHeightMap.clear();
		}
	}

}
