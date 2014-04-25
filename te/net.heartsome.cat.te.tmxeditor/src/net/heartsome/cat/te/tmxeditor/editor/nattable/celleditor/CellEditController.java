/**
 * CellEditController.java
 *
 * Version information :
 *
 * Date:2013-6-8
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor;

import java.util.Arrays;
import java.util.List;

import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.te.tmxeditor.TmxEditorConstanst;
import net.heartsome.cat.te.tmxeditor.TmxEditorUtils;
import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable;
import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable.BodyLayer;
import net.heartsome.cat.te.tmxeditor.editor.nattable.layer.LayerUtil;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.edit.ICellEditHandler;
import net.sourceforge.nattable.edit.command.EditCellCommand;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;
import net.sourceforge.nattable.selection.command.ScrollSelectionCommand;
import net.sourceforge.nattable.style.CellStyleProxy;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.util.GUIHelper;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public final class CellEditController {
	public static boolean editCellInline(final TmxEditorImpWithNattable editor) {
		int[] selectedRowIndexs = editor.getSelectedRows();
		if (selectedRowIndexs.length == 0) {
			return false;
		}
		Arrays.sort(selectedRowIndexs);
		final int rowIndex = selectedRowIndexs[selectedRowIndexs.length - 1];
		final NatTable natTable = editor.getTable();
		IConfigRegistry configRegistry = natTable.getConfigRegistry();
		ViewportLayer vLayer = LayerUtil.getLayer(natTable, ViewportLayer.class);
		int rowPosition = vLayer.getRowPositionByIndex(rowIndex);
		rowPosition += 1;
		if (rowPosition < 1) {
			return false;
		}

		final SelectionLayer selectionLayer = LayerUtil.getLayer(natTable, SelectionLayer.class);
		int _columnIndex = selectionLayer.getLastSelectedCellPosition().columnPosition;
		if (_columnIndex != editor.getSrcColumnIndex() && _columnIndex != editor.getTgtColumnIndex()) {
			_columnIndex = editor.getTgtColumnIndex();
		}
		final int columnIndex = _columnIndex;
		int columnPosition = vLayer.getColumnPositionByIndex(columnIndex);

		LayerCell cell = natTable.getCellByPosition(columnPosition, rowPosition);

		try {
			TeActiveCellEditor.commit();
			final List<String> configLabels = cell.getConfigLabels().getLabels();
			Rectangle cellBounds = cell.getBounds();

			ILayer layer = cell.getLayer();

			boolean editable = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE,
					DisplayMode.EDIT, configLabels).isEditable(columnIndex, rowIndex);
			if (!editable) {
				return false;
			}

			ICellEditor iCellEditor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR,
					DisplayMode.EDIT, configLabels);
			IDisplayConverter displayConverter = configRegistry.getConfigAttribute(
					CellConfigAttributes.DISPLAY_CONVERTER, DisplayMode.EDIT, configLabels);
			IStyle cellStyle = new CellStyleProxy(configRegistry, DisplayMode.EDIT, configLabels);
			IDataValidator dataValidator = configRegistry.getConfigAttribute(EditConfigAttributes.DATA_VALIDATOR,
					DisplayMode.EDIT, configLabels);

			DataLayer dataLayer = LayerUtil.getLayer(natTable, DataLayer.class);
			ICellEditHandler editHandler = new CellEditHandler(iCellEditor, dataLayer, columnIndex, rowIndex);

			final Rectangle editorBounds = layer.getLayerPainter().adjustCellBounds(
					new Rectangle(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height));

			TmxTU tu = (TmxTU) cell.getDataValue();
			if (tu == null) {
				return false;
			}
			CellEditorCanonicalValue originalCanonicalValue = null;
			String searchString = null;
			if (cell.getColumnPosition() == editor.getSrcColumnIndex()) {
				originalCanonicalValue = new CellEditorCanonicalValue(tu, tu.getSource());
				searchString = editor.getSrcSearchStr();
			} else if (cell.getColumnPosition() == editor.getTgtColumnIndex()) {
				originalCanonicalValue = new CellEditorCanonicalValue(tu, tu.getTarget());
				searchString = editor.getTgtSearchStr();
			}

			TeActiveCellEditor.activate(iCellEditor, editor.getTable(), originalCanonicalValue, null, displayConverter,
					cellStyle, dataValidator, editHandler, columnPosition, rowPosition, columnIndex, rowIndex);
			final Control editorControl = TeActiveCellEditor.getControl();

			if (editorControl != null) {
				editorControl.setBounds(editorBounds);
			}

			if (iCellEditor != null && (iCellEditor instanceof CellEditor) && editorControl != null) {
				final CellEditor cellEditor = (CellEditor) iCellEditor;
				if (searchString != null && searchString.length() != 0) {
					StyledText styledText = cellEditor.getTextViewer().getTextWidget();
					String text = styledText.getText();
					char[] source = text.toCharArray();
					if (TmxEditorImpWithNattable.showNonPrinttingChar) {
						searchString = searchString.replaceAll("\\n", TmxEditorConstanst.LINE_SEPARATOR_CHARACTER
								+ "\n");
						searchString = searchString.replaceAll("\\t", TmxEditorConstanst.TAB_CHARACTER + "\u200B");
						searchString = searchString.replaceAll(" ", TmxEditorConstanst.SPACE_CHARACTER + "\u200B");
					}
					TextStyle style = new TextStyle(null, null, GUIHelper.COLOR_GREEN);
					List<StyleRange> ranges = TmxEditorUtils.calculateSearchStringStyleRange(source,
							searchString.toCharArray(), style);
					for (StyleRange range : ranges) {
						styledText.setStyleRange(range);
					}
				}
				IDocument document = cellEditor.getTextViewer().getDocument();
				if (document != null) {
					document.addDocumentListener(new IDocumentListener() {

						@Override
						public void documentChanged(DocumentEvent event) {
							editor.setEditorDirty();
							// CompositeLayer comlayer = LayerUtil.getLayer(natTable, CompositeLayer.class);
							DataLayer dataLayer = LayerUtil.getLayer(natTable, DataLayer.class);
							BodyLayer bodyLayer = LayerUtil.getLayer(natTable, BodyLayer.class);

							Rectangle controlBounds = editorControl.getBounds();
							Point p = editorControl.computeSize(controlBounds.width, SWT.DEFAULT, true);
							int newHeight = p.y;

							int maxHeight = 0;
							IConfigRegistry configRegistry = natTable.getConfigRegistry();
							ICellPainter painter;
							LayerCell cell;
							SelectionLayer layer = bodyLayer.getSelectionLayer();
							for (int columnPosition = 0; columnPosition < layer.getColumnCount(); columnPosition++) {
								if (columnPosition == columnIndex) {
									continue;
								}
								cell = layer.getCellByPosition(columnPosition, rowIndex);
								if (cell != null) {
									painter = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_PAINTER,
											cell.getDisplayMode(),
											bodyLayer.getConfigLabelsByPosition(columnPosition, rowIndex).getLabels());
									if (painter != null) {
										int preferedHeight = painter.getPreferredHeight(cell, null, configRegistry);
										maxHeight = (preferedHeight > maxHeight) ? preferedHeight : maxHeight;
									}
								}
							}

							int rowHeight = dataLayer.getRowHeightByPosition(rowIndex);
							// 加上编辑模式下，StyledTextCellEditor的边框 可以参考 TmxEditorTextPainter
							newHeight += 4;
							newHeight = Math.max(newHeight, maxHeight);
							if (rowHeight == newHeight) {
								return;
							}

							editorBounds.height = newHeight;
							int cellStartY = editorBounds.y;
							int cellEndY = cellStartY + editorBounds.height;
							Rectangle clientArea = natTable.getClientAreaProvider().getClientArea();
							int clientAreaEndY = clientArea.y + clientArea.height;
							if (cellEndY > clientAreaEndY) {
								editorBounds.height = clientAreaEndY - cellStartY;
							}
							// comlayer.doCommand(new TurnViewportOffCommand());
							dataLayer.setRowHeightByPosition(dataLayer.getRowPositionByIndex(rowIndex),
									editorBounds.height);
							// comlayer.doCommand(new TurnViewportOnCommand());
							// if(!editorControl.isDisposed()){
							// editorControl.setSize(editorBounds.width, editorBounds.height);
							TeActiveCellEditor.recalculateCellsBounds();
							// natTable.notifyListeners(SWT.Resize, null);
							// }
						}

						@Override
						public void documentAboutToBeChanged(DocumentEvent event) {
						}
					});
				}
				CellEditorTextViewer viewer = cellEditor.getTextViewer();
				final StyledText styledText = viewer.getTextWidget();
				// 移除向上和向下键默认事件处理，将此部分实现放到upAndDownKeyListener监听中
				styledText.setKeyBinding(SWT.ARROW_DOWN, SWT.NULL);
				styledText.setKeyBinding(SWT.ARROW_UP, SWT.NULL);
				styledText.addKeyListener(new KeyListener() {
					public void keyReleased(KeyEvent e) {
					}

					public void keyPressed(KeyEvent e) {
						if (e.keyCode == SWT.ESC && e.stateMask == SWT.NONE) {
							TeActiveCellEditor.commit();
							return;
						}
						if (e.keyCode == SWT.ARROW_DOWN && e.stateMask == SWT.NONE) {
							int oldOffset = styledText.getCaretOffset();
							styledText.invokeAction(ST.LINE_DOWN);
							int newOffset = styledText.getCaretOffset();
							if (oldOffset == newOffset) {
								int rowPosition = selectionLayer.getLastSelectedCellPosition().rowPosition;
								if (rowPosition != selectionLayer.getRowCount() - 1) { // 减去列头行
									TeActiveCellEditor.commit();
									natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, 1, false, false));
									natTable.doCommand(new EditCellCommand(null, null, null));
								}
							}
						} else if (e.keyCode == SWT.ARROW_UP && e.stateMask == SWT.NONE) {
							int oldOffset = styledText.getCaretOffset();
							styledText.invokeAction(ST.LINE_UP);
							int newOffset = styledText.getCaretOffset();
							if (oldOffset == newOffset) {
								int rowPosition = selectionLayer.getLastSelectedCellPosition().rowPosition;
								if (rowPosition != 0) {
									TeActiveCellEditor.commit();
									natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, 1, false, false));
									natTable.doCommand(new EditCellCommand(null, null, null));
								}
							}
						} else if (e.keyCode == SWT.PAGE_UP && e.stateMask == SWT.NONE) {
							int rowPosition = selectionLayer.getLastSelectedCellPosition().rowPosition;
							if (rowPosition != 0) {
								TeActiveCellEditor.commit();
								natTable.doCommand(new ScrollSelectionCommand(MoveDirectionEnum.UP, false, false));
								natTable.doCommand(new EditCellCommand(null, null, null));
							}
						} else if (e.keyCode == SWT.PAGE_DOWN && e.stateMask == SWT.NONE) {
							int rowPosition = selectionLayer.getLastSelectedCellPosition().rowPosition;
							if (rowPosition != selectionLayer.getRowCount() - 1) {
								TeActiveCellEditor.commit();
								natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, 1, false, false));
								natTable.doCommand(new ScrollSelectionCommand(MoveDirectionEnum.DOWN, false, false));
								natTable.doCommand(new EditCellCommand(null, null, null));
							}
						} else if (e.keyCode == SWT.HOME && e.stateMask == SWT.CTRL) {
							TeActiveCellEditor.commit();
							natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.UP, SelectionLayer.MOVE_ALL,
									false, false));
							natTable.doCommand(new EditCellCommand(null, null, null));
						} else if (e.keyCode == SWT.END && e.stateMask == SWT.CTRL) {
							TeActiveCellEditor.commit();
							natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN,
									SelectionLayer.MOVE_ALL, false, false));
							natTable.doCommand(new EditCellCommand(null, null, null));
						} else if (e.keyCode == SWT.ARROW_LEFT && e.stateMask == SWT.NONE) {
							int offset = styledText.getCaretOffset();
							if (offset == 0 && TeActiveCellEditor.getColumnIndex() == editor.getTgtColumnIndex()) {
								TeActiveCellEditor.commit();
								natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.LEFT, 1, false, false));
								natTable.doCommand(new EditCellCommand(null, null, null));
							}
						} else if (e.keyCode == SWT.ARROW_RIGHT && e.stateMask == SWT.NONE) {
							int offset = styledText.getCaretOffset();
							if (offset == styledText.getText().length()
									&& TeActiveCellEditor.getColumnIndex() == editor.getSrcColumnIndex()) {
								TeActiveCellEditor.commit();
								natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, 1, false, false));
								natTable.doCommand(new EditCellCommand(null, null, null));
								ICellEditor ice = TeActiveCellEditor.getCellEditor();
								if (ice != null && ice instanceof CellEditor) {
									CellEditor ce = (CellEditor) ice;
									ce.getTextViewer().getTextWidget().setCaretOffset(0);
								}
							}
						} else if (e.keyCode == SWT.CR && e.stateMask == SWT.CTRL) {
							TeActiveCellEditor.commit();
						}
					}
				});
				styledText.addVerifyKeyListener(new VerifyKeyListener() {

					@Override
					public void verifyKey(VerifyEvent e) {
						if (e.start == e.end && e.character == SWT.CR) {
							e.doit = false;
						}
					}
				});
				styledText.addMouseListener(new MouseListener() {

					@Override
					public void mouseUp(MouseEvent e) {
						if (e.button == 3) {
							int x = e.x;
							int y = e.y;
							Point p = Display.getDefault().map(styledText, natTable, x, y);
							e.x = p.x;
							e.y = p.y;
							e.widget = natTable;
							e.stateMask = 0;
							natTable.getUiBindingRegistry().getMouseDownAction(e).run(natTable, e);
						}
					}

					@Override
					public void mouseDown(MouseEvent e) {
					}

					@Override
					public void mouseDoubleClick(MouseEvent e) {
					}
				});
			}
		} catch (Exception e) {
			if (cell == null) {
				System.err.println("Cell being edited is no longer available. ");
			} else {
				System.err.println("Error while editing cell (inline): " + "Cell: " + cell);
				e.printStackTrace(System.err);
			}
			e.printStackTrace();
			TeActiveCellEditor.close();
		}

		return true;
	}
}
