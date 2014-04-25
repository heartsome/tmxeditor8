/**
 * TmxEditorImpWithNattable.java
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
package net.heartsome.cat.te.tmxeditor.editor.nattable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess;
import net.heartsome.cat.te.tmxeditor.Activator;
import net.heartsome.cat.te.tmxeditor.TmxEditorConstanst;
import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.CellEditor;
import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.TeActiveCellEditor;
import net.heartsome.cat.te.tmxeditor.editor.nattable.commond.AutoResizeCurrentRowsCommand;
import net.heartsome.cat.te.tmxeditor.editor.nattable.commond.AutoResizeCurrentRowsCommandHandler;
import net.heartsome.cat.te.tmxeditor.editor.nattable.commond.EditCellCommandHandler;
import net.heartsome.cat.te.tmxeditor.editor.nattable.commond.UpdateDataCommandHandler;
import net.heartsome.cat.te.tmxeditor.editor.nattable.configuration.BodyMenuConfiguration;
import net.heartsome.cat.te.tmxeditor.editor.nattable.configuration.CompositeLayerConfiguration;
import net.heartsome.cat.te.tmxeditor.editor.nattable.configuration.TmxEditorSelectionLayerConfiguration;
import net.heartsome.cat.te.tmxeditor.editor.nattable.layer.HorizontalViewportLayer;
import net.heartsome.cat.te.tmxeditor.editor.nattable.layer.LayerUtil;
import net.heartsome.cat.te.tmxeditor.editor.nattable.layer.RowHeightCalculator;
import net.heartsome.cat.te.tmxeditor.editor.nattable.painter.AttributePainter;
import net.heartsome.cat.te.tmxeditor.editor.nattable.painter.FlagPainter;
import net.heartsome.cat.te.tmxeditor.editor.nattable.painter.LineNumberPainter;
import net.heartsome.cat.te.tmxeditor.editor.nattable.painter.TmxEditorTextPainter;
import net.heartsome.cat.te.tmxeditor.editor.nattable.selection.RowSelectionProvider;
import net.heartsome.cat.te.tmxeditor.editor.nattable.selection.TmxEditorRowSelectionModel;
import net.heartsome.cat.te.tmxeditor.resource.Messages;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.ConfigRegistry;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.config.IEditableRule;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.data.ReflectiveColumnPropertyAccessor;
import net.sourceforge.nattable.data.convert.DefaultDisplayConverter;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.edit.command.EditCellCommand;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.command.ClientAreaResizeCommand;
import net.sourceforge.nattable.grid.data.DefaultColumnHeaderDataProvider;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.CompositeLayer;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import net.sourceforge.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.resize.command.MultiColumnResizeCommand;
import net.sourceforge.nattable.selection.ISelectionModel;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.selection.command.SelectColumnCommand;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.util.GUIHelper;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxEditorImpWithNattable {

	public static boolean showNonPrinttingChar = Activator.getDefault().getPreferenceStore()
			.getBoolean(TmxEditorConstanst.TMX_EDITOR_SHOWHIDEN_NONPRINTCHARACTER);

	private AbstractTmxDataAccess tmxDataAccess;

	private NatTable natTable;

	private BodyLayer bodyLayer;
	private String[] headerLabelNames;
	private Map<String, String> headerLabels;
	private String srcSearchStr;
	private String tgtSearchStr;
	private IPropertyChangeListener fontChangeListenner = new FontPropertyChangeListener();

	public TmxEditorImpWithNattable(AbstractTmxDataAccess tmxDataAccess) {
		this.tmxDataAccess = tmxDataAccess;
	}

	public void createContents(Composite container) {
		natTable = new NatTable(container, false);
		Listener[] ls = natTable.getListeners(SWT.Resize);
		for (Listener l : ls) {
			natTable.removeListener(SWT.Resize, l);
		}

		JFaceResources.getFontRegistry().addListener(fontChangeListenner);

		natTable.removePaintListener(natTable);
		natTable.addPaintListener(paintListener);
		natTable.addListener(SWT.Resize, resizeListener);
		natTable.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				TeActiveCellEditor.commit(); // before dispose commit the last change
				natTable.removeListener(SWT.Resize, resizeListener);
				natTable.removePaintListener(paintListener);
				bodyLayer.dispose();
			}
		});
		// create NatTable configuration
		IConfigRegistry configRegistry = createConfigRegistry();
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new BodyMenuConfiguration(natTable));
		// body configuration
		bodyLayer = new BodyLayer();
		// header configuration
		ColumnHeaderLayerStack colHLayer = new ColumnHeaderLayerStack();
		CompositeLayer compositeLayer = new CompositeLayer(1, 2);
		compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, colHLayer, 0, 0);
		compositeLayer.setChildLayer(GridRegion.BODY, bodyLayer, 0, 1);
		compositeLayer.addConfiguration(new CompositeLayerConfiguration(compositeLayer));
		compositeLayer.registerCommandHandler(new AutoResizeCurrentRowsCommandHandler(compositeLayer));
		compositeLayer.registerCommandHandler(new EditCellCommandHandler(this));
		LayerUtil.setBodyLayerPosition(0, 1); // 设置 LayerUtil位置 add by Yule
		natTable.setLayer(compositeLayer);

		// manual configure NatTable
		natTable.configure();

		// default select the first row
		selectCell(getTgtColumnIndex(), 0);
		RowHeightCalculator rowHeightCalculator = new RowHeightCalculator(bodyLayer, natTable, 25);
		((HorizontalViewportLayer) bodyLayer.getViewportLayer()).setRowHeightCalculator(rowHeightCalculator);
	}

	public ISelectionProvider getSelectionProvider() {
		if (bodyLayer != null) {
			return bodyLayer.getSelectionProvider();
		}
		return null;
	}

	private IConfigRegistry createConfigRegistry() {
		IConfigRegistry configRegistry = new ConfigRegistry();
		// Line number column configuration
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new LineNumberPainter(),
				DisplayMode.NORMAL, TeNattableConstant.LINENUMBER_CELL_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter(),
				DisplayMode.NORMAL, TeNattableConstant.LINENUMBER_CELL_LABEL);
		Style lineNumberStyle = new Style();
		lineNumberStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
		lineNumberStyle.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.DEFAULT_FONT);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, lineNumberStyle, DisplayMode.NORMAL,
				TeNattableConstant.LINENUMBER_CELL_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, lineNumberStyle, DisplayMode.SELECT,
				TeNattableConstant.LINENUMBER_CELL_LABEL);

		TmxEditorTextPainter painter = new TmxEditorTextPainter(TmxEditorImpWithNattable.this);

		// source column configuration
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, painter, DisplayMode.NORMAL,
				TeNattableConstant.SOURCE_EDIT_CELL_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE,
				DisplayMode.EDIT, TeNattableConstant.SOURCE_EDIT_CELL_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new CellEditor(), DisplayMode.EDIT,
				TeNattableConstant.SOURCE_EDIT_CELL_LABEL);

		// target column configuration
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, painter, DisplayMode.NORMAL,
				TeNattableConstant.TARGET_EDIT_CELL_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE,
				DisplayMode.EDIT, TeNattableConstant.TARGET_EDIT_CELL_LABEL);
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new CellEditor(), DisplayMode.EDIT,
				TeNattableConstant.TARGET_EDIT_CELL_LABEL);

		// attribute column configuration
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new AttributePainter(natTable),
				DisplayMode.NORMAL, TeNattableConstant.ATTRIBUTE_CELL_LABEL);

		// flag column configuration
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new FlagPainter(),
				DisplayMode.NORMAL, TeNattableConstant.FLAG_CELL_LABEL);
		return configRegistry;
	}

	public class BodyLayer extends AbstractLayerTransform {
		private TmxEditorDataProvider<TmxTU> bodyDataProvider;
		private SelectionLayer selectionLayer;
		private ViewportLayer viewportLayer;
		private ISelectionProvider selectionProvider;
		private DataLayer bodyDataLayer;

		public BodyLayer() {
			bodyDataProvider = createBodyDataProvider();
			bodyDataLayer = new DataLayer(bodyDataProvider, 300, 25);
			// ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
			// ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
			selectionLayer = new SelectionLayer(bodyDataLayer, false);
			configSelectionLayer();

			viewportLayer = new HorizontalViewportLayer(selectionLayer);
			setUnderlyingLayer(viewportLayer);

			configLabelAccumulator();
			bodyDataLayer.registerCommandHandler(new UpdateDataCommandHandler(natTable, this));
		}

		public void dispose() {
			selectionLayer.clear();
			selectionLayer = null;
			viewportLayer = null;
			selectionProvider = null;
			clearDataCache();
			bodyDataLayer = null;
		}

		public void clearDataCache() {
			if (bodyDataLayer != null)
				bodyDataProvider.clearCache();
		}

		private void configSelectionLayer() {
			// 移除点击列头触发全选所有行的 Handler
			selectionLayer.unregisterCommandHandler(SelectColumnCommand.class);
			ISelectionModel rowSelectionModel = new TmxEditorRowSelectionModel(selectionLayer);

			// Preserve selection on updates and sort
			selectionLayer.setSelectionModel(rowSelectionModel);
			selectionLayer.addConfiguration(new TmxEditorSelectionLayerConfiguration());

			selectionProvider = new RowSelectionProvider(selectionLayer, true, tmxDataAccess);
		}

		public DataLayer getDataLayer() {
			return this.bodyDataLayer;
		}

		public SelectionLayer getSelectionLayer() {
			return selectionLayer;
		}

		public ViewportLayer getViewportLayer() {
			return this.viewportLayer;
		}

		public ISelectionProvider getSelectionProvider() {
			return this.selectionProvider;
		}

		private TmxEditorDataProvider<TmxTU> createBodyDataProvider() {
			headerLabels = new HashMap<String, String>();
			headerLabels.put("id", "No.");
			headerLabels.put("srcContent", tmxDataAccess.getCurrSrcLang());
			headerLabels.put("tgtContent", tmxDataAccess.getCurrTgtLang());
			headerLabels.put("attribute", Messages.getString("tmxeditor.tmxeditorimpwithNattable.headerLable1"));
			headerLabels.put("flag", "");

			headerLabelNames = new String[] { "id", "srcContent", "tgtContent", "attribute", "flag", };

			return new TmxEditorDataProvider<TmxTU>(tmxDataAccess, new ReflectiveColumnPropertyAccessor<TmxTU>(
					headerLabelNames));
		}

		private void configLabelAccumulator() {
			ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(this);
			columnLabelAccumulator.registerColumnOverrides(0, TeNattableConstant.LINENUMBER_CELL_LABEL);
			columnLabelAccumulator.registerColumnOverrides(1, TeNattableConstant.SOURCE_EDIT_CELL_LABEL);
			columnLabelAccumulator.registerColumnOverrides(2, TeNattableConstant.TARGET_EDIT_CELL_LABEL);
			columnLabelAccumulator.registerColumnOverrides(3, TeNattableConstant.ATTRIBUTE_CELL_LABEL);
			columnLabelAccumulator.registerColumnOverrides(4, TeNattableConstant.FLAG_CELL_LABEL);
			setConfigLabelAccumulator(columnLabelAccumulator);
		}
	}

	public class ColumnHeaderLayerStack extends AbstractLayerTransform {

		public ColumnHeaderLayerStack() {
			IDataProvider dataProvider = new DefaultColumnHeaderDataProvider(headerLabelNames, headerLabels);
			DataLayer dataLayer = new DataLayer(dataProvider);
			ColumnHeaderLayer colHeaderLayer = new ColumnHeaderLayer(dataLayer, bodyLayer,
					bodyLayer.getSelectionLayer());
			colHeaderLayer.addConfiguration(new DefaultColumnHeaderStyleConfiguration() {
				public void configureRegistry(IConfigRegistry configRegistry) {
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter,
							DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
					configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER,
							new DefaultDisplayConverter(), DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
					// Normal
					Style cellStyle = new Style();
					cellStyle
							.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_WIDGET_BACKGROUND);
					cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_BLACK);
					cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign);
					cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, vAlign);
					cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
					cellStyle.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.DEFAULT_FONT);

					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.NORMAL, GridRegion.CORNER);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.SELECT, GridRegion.CORNER);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.SELECT, GridRegion.ROW_HEADER);
				}
			});

			setUnderlyingLayer(colHeaderLayer);
		}
	}

	private Listener resizeListener = new Listener() {
		public void handleEvent(Event event) {
			NatTable table = (NatTable) event.widget;
			if (table == null || table.isDisposed()) {
				return;
			}
			TeActiveCellEditor.commitWithoutClose();
			int clientAreaWidth = table.getClientArea().width;
			if (clientAreaWidth <= 0) {
				return;
			}
			int count = headerLabelNames.length; // 编辑器中的列数
			if (count <= 0) {
				return;
			}

			Map<String, Double> propertyToColWidths = new HashMap<String, Double>();

			int tuNumber = tmxDataAccess.getDisplayTuCount();
			String tuNumberStr = tuNumber + "";
			GC gc = new GC(table.getDisplay());
			double _width = (gc.textExtent(tuNumberStr).x + 4) * 1.0;
			gc.dispose();
			if (_width < 32.0) {
				_width = 32.0;
			}
			gc.dispose();
			propertyToColWidths.put("id", _width); // 大于1的值为像素值，小于等于1的值为除了像素值剩下部分的百分比（例如0.5，表示50％）
			propertyToColWidths.put("srcContent", 0.4);
			propertyToColWidths.put("tgtContent", 0.4);
			propertyToColWidths.put("attribute", 0.2);
			propertyToColWidths.put("flag", 32.0);

			int[] columnPositions = new int[count]; // 需要修改的列的列号数组
			int[] columnWidths = new int[count]; // 需要修改的列对应的宽度
			double shownPercentage = 1; // 显示的百分比，原始为1（即 100%，表示所有列显示，后面要减去被隐藏的列所占的百分比）

			for (int i = 0, j = 0; i < count; i++) {
				double width = propertyToColWidths.get(headerLabelNames[i]);
				columnPositions[j] = i;
				if (width > 1) { // 如果指定的是像素值
					columnWidths[j] = (int) width;
					clientAreaWidth -= (int) width; // 从总宽度中除去明确指定像素的列宽
				}
				j++;
			}
			for (int i = 0, j = 0; i < count; i++) {
				double width = propertyToColWidths.get(headerLabelNames[i]);
				if (width <= 1) { // 如果指定的是百分比
					columnWidths[j] = (int) (clientAreaWidth * (width / shownPercentage)); // 按指定百分比计算像素
				}
				j++;
			}
			table.doCommand(new ClientAreaResizeCommand(table));
			cleanRowHeightCache();
			table.doCommand(new MultiColumnResizeCommand(bodyLayer, columnPositions, columnWidths));
			TeActiveCellEditor.recalculateCellsBounds();
		}
	};

	/** 清除行高计算缓存，清空后在下次 Paint 事件时触发时，会重新计算当前显示行在自动换行模式下的行高。 */
	private void cleanRowHeightCache() {
		rowHeightCache.clear();
	}

	/** 为了避免重复计算行高，添加了此缓存，用存储已经计算过行高的行。 */
	private List<Integer> rowHeightCache = new ArrayList<Integer>();

	/** 重写 Nattable 的 Paint 事件，实现在绘制前计算各行在自动换行模式下需要的最低行高。 */
	private PaintListener paintListener = new PaintListener() {

		@Override
		public void paintControl(PaintEvent e) {
			resizeRowsHeight();
			natTable.getLayerPainter().paintLayer(natTable, e.gc, 0, 0, new Rectangle(e.x, e.y, e.width, e.height),
					natTable.getConfigRegistry());
		}

	};

	public void resizeRowsHeight() {
		ViewportLayer viewportLayer = bodyLayer.getViewportLayer();
		int rowPosition = viewportLayer.getOriginRowPosition() + 1; // 起始行
		int rowCount = viewportLayer.getRowCount(); // 总行数
		List<Integer> rowPositions = new ArrayList<Integer>();
		for (int i = 0; i < rowCount; i++) {
			int rowp = i + rowPosition;
			if (!rowHeightCache.contains(rowp)) {
				rowPositions.add(rowp);
				rowHeightCache.add(rowp);
			}
		}
		if (rowPositions.size() != 0) {
			int[] temp = new int[rowPositions.size()];
			for (int i = 0; i < temp.length; i++) {
				temp[i] = rowPositions.get(i);
			}
			natTable.doCommand(new AutoResizeCurrentRowsCommand(natTable, temp, natTable.getConfigRegistry()));
			TeActiveCellEditor.recalculateCellsBounds();
		}
	}

	/**
	 * Internal property change listener for handling workbench font changes.
	 */
	class FontPropertyChangeListener implements IPropertyChangeListener {
		/*
		 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (natTable == null || natTable.isDisposed()) {
				return;
			}
			String property = event.getProperty();

			if ("net.heartsome.cat.te.ui.tmxeditor.font".equals(property)) {
				Font font = JFaceResources.getFont("net.heartsome.cat.te.ui.tmxeditor.font");
				ICellPainter cellPainter = natTable.getConfigRegistry().getConfigAttribute(
						CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL,
						TeNattableConstant.SOURCE_EDIT_CELL_LABEL);

				if (cellPainter instanceof TmxEditorTextPainter) {
					TmxEditorTextPainter textPainter = (TmxEditorTextPainter) cellPainter;
					if (textPainter.getFont() == null || !textPainter.getFont().equals(font)) {
						TeActiveCellEditor.commit();
						textPainter.loadFont(font);
						refrush();
					}
				}
			}
		}
	}

	/**
	 * 得到当前选中的行
	 * @return ;
	 */
	public int[] getSelectedRows() {
		SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();
		return selectionLayer.getFullySelectedRowPositions();
	}

	public NatTable getTable() {
		return natTable;
	}

	public int getSrcColumnIndex() {
		return 1;
	}

	public int getTgtColumnIndex() {
		return 2;
	}

	/**
	 * Dispose nattable ，在调用此方法前必须关闭编辑模式 ;
	 */
	public void dispose() {
		if (natTable != null && !natTable.isDisposed()) {
			natTable.dispose();
		}
		this.tmxDataAccess = null;
		JFaceResources.getFontRegistry().removeListener(fontChangeListenner);
	}

	public boolean isDispose() {
		return natTable == null || natTable.isDisposed();
	}

	public void refrush() {
		headerLabels.put("srcContent", tmxDataAccess.getCurrSrcLang());
		headerLabels.put("tgtContent", tmxDataAccess.getCurrTgtLang());
		bodyLayer.clearDataCache();
		cleanRowHeightCache();
		redraw();
	}

	public void redraw() {
		if (natTable != null && !natTable.isDisposed()) {
			natTable.redraw();
		}
	}

	public void selectCell(int colIndex, int rowIndex) {
		if (colIndex < 0 || colIndex > headerLabelNames.length - 1) {
			return;
		}
		if (rowIndex < 0) {
			return;
		}
		bodyLayer.selectionLayer.doCommand(new SelectCellCommand(bodyLayer.getSelectionLayer(), colIndex, rowIndex,
				false, false));
	}

	public void editSelectedCell() {
		if (isDispose()) {
			return;
		}
		natTable.doCommand(new EditCellCommand(null, null, null));
	}

	public void setEditorDirty() {
		tmxDataAccess.setDirty(true);
	}

	public void refreshSelectionedRow() {
		PositionCoordinate sel = bodyLayer.selectionLayer.getLastSelectedCellPosition();
		selectCell(sel.columnPosition, sel.rowPosition);
	}

	public void setSrcSearchStr(String srcSearchStr) {
		this.srcSearchStr = srcSearchStr;
	}

	public String getSrcSearchStr() {
		return srcSearchStr;
	}

	public void setTgtSearchStr(String tgtSearchStr) {
		this.tgtSearchStr = tgtSearchStr;
	}

	public String getTgtSearchStr() {
		return tgtSearchStr;
	}
}
