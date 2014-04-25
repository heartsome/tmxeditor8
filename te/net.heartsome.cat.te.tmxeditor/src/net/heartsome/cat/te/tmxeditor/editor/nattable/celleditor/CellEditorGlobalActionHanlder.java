package net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.WorkbenchMessages;

@SuppressWarnings("restriction")
public class CellEditorGlobalActionHanlder {

	private CutActionHandler textCutAction = new CutActionHandler();

	private CopyActionHandler textCopyAction = new CopyActionHandler();

	private PasteActionHandler textPasteAction = new PasteActionHandler();

	private UndoActionHandler textUndoAction = new UndoActionHandler();

	private RedoActionHandler textRedoAction = new RedoActionHandler();

	// private FindReplaceActionHandler textFindReplaceAction = new FindReplaceActionHandler();

	private IActionBars actionBar;

	private CellEditorTextViewer viewer;

	private static CellEditorGlobalActionHanlder instance = new CellEditorGlobalActionHanlder();

	public static CellEditorGlobalActionHanlder getInstance() {
		return instance;
	}

	private CellEditorGlobalActionHanlder() {

	}

	public void setIActionBars(IActionBars actionBars) {
		this.actionBar = actionBars;
		// 添加查找/替换
		// actionBar.setGlobalActionHandler(ActionFactory.FIND.getId(), textFindReplaceAction);
	}

	private class CutActionHandler extends Action {

		protected CutActionHandler() {
			super(WorkbenchMessages.Workbench_cut);
			setId("XLIFFEditorCutActionHandler");//$NON-NLS-1$
			setEnabled(false);
		}

		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				viewer.doOperation(ITextOperationTarget.CUT);
				updateActionsEnableState();
				return;
			}
		}

		/**
		 * Update state.
		 */
		public void updateEnabledState() {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.CUT));
				return;
			}
			setEnabled(false);
		}
	}

	/**
	 * 撤销处理
	 * @author Leakey
	 * @version
	 * @since JDK1.6
	 */
	private class UndoActionHandler extends Action {
		protected UndoActionHandler() {
			super("UNDO");//$NON-NLS-1$
			setId("XLIFFEditorUndoActionHandler");//$NON-NLS-1$
			setEnabled(true);
		}

		public void runWithEvent(Event event) {
			TeActiveCellEditor.commit();
			IOperationHistory history = OperationHistoryFactory.getOperationHistory();
			IUndoContext context = PlatformUI.getWorkbench().getOperationSupport().getUndoContext();
			if (history.canUndo(context)) {
				try {
					history.undo(context, null, null);
					updateActionsEnableState();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Update the state.
		 */
		public void updateEnabledState() {
			IOperationHistory opHisotry = OperationHistoryFactory.getOperationHistory();
			IUndoContext context = PlatformUI.getWorkbench().getOperationSupport().getUndoContext();
			if (opHisotry.canUndo(context)) {
				setEnabled(true);
				return;
			}
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.UNDO));
				return;
			}
			setEnabled(false);
		}
	}

	/**
	 * 重做处理
	 * @author Leakey
	 * @version
	 * @since JDK1.6
	 */
	private class RedoActionHandler extends Action {
		protected RedoActionHandler() {
			super("REDO");//$NON-NLS-1$
			setId("XLIFFEditorRedoActionHandler");//$NON-NLS-1$
			setEnabled(true);
		}

		public void runWithEvent(Event event) {
			TeActiveCellEditor.commit();
			IOperationHistory history = OperationHistoryFactory.getOperationHistory();
			IUndoContext context = PlatformUI.getWorkbench().getOperationSupport().getUndoContext();
			if (history.canRedo(context)) {
				try {
					history.redo(context, null, null);
					updateActionsEnableState();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Update the state.
		 */
		public void updateEnabledState() {
			IOperationHistory opHisotry = OperationHistoryFactory.getOperationHistory();
			IUndoContext context = PlatformUI.getWorkbench().getOperationSupport().getUndoContext();
			if (opHisotry.canRedo(context)) {
				setEnabled(true);
				return;
			}
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.REDO));
				return;
			}
			setEnabled(false);
		}
	}

	private class CopyActionHandler extends Action {
		protected CopyActionHandler() {
			super(WorkbenchMessages.Workbench_copy);
			setId("XLIFFEditorCopyActionHandler");//$NON-NLS-1$
			setEnabled(false);
		}

		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				viewer.doOperation(ITextOperationTarget.COPY);
				updateActionsEnableState();
				return;
			}
		}

		/**
		 * Update the state.
		 */
		public void updateEnabledState() {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.COPY));
				return;
			}
			setEnabled(false);
		}
	}

	private class PasteActionHandler extends Action {
		protected PasteActionHandler() {
			super(WorkbenchMessages.Workbench_paste);
			setId("XLIFFEditorPasteActionHandler");//$NON-NLS-1$
			setEnabled(false);
		}

		public void runWithEvent(Event event) {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				viewer.doOperation(ITextOperationTarget.PASTE);
				updateActionsEnableState();
				return;
			}
		}

		/**
		 * Update the state
		 */
		public void updateEnabledState() {
			if (viewer != null && !viewer.getTextWidget().isDisposed()) {
				setEnabled(viewer.canDoOperation(ITextOperationTarget.PASTE));
				return;
			}
			setEnabled(false);
		}
	}

	/**
	 * Add a <code>Text</code> control to the handler so that the Cut, Copy, Paste, Delete, Undo, Redo and Select All
	 * actions are redirected to it when active.
	 * @param viewer
	 *            the inline <code>Text</code> control
	 */
	public void addTextViewer(CellEditorTextViewer viewer) {
		if (viewer == null) {
			return;
		}
		this.viewer = viewer;
		StyledText textControl = viewer.getTextWidget();

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActionsEnableState();
			}
		});

		textControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateActionsEnableState();
			}
		});

		updateActionsEnableState();
	}

	public void updateGlobalActionHandler() {
		if (actionBar == null) {
			return;
		}
		preCutAction = actionBar.getGlobalActionHandler(ActionFactory.CUT.getId());
		actionBar.setGlobalActionHandler(ActionFactory.CUT.getId(), textCutAction);
		preCopyAction = actionBar.getGlobalActionHandler(ActionFactory.COPY.getId());
		actionBar.setGlobalActionHandler(ActionFactory.COPY.getId(), textCopyAction);
		prePasteAction= actionBar.getGlobalActionHandler(ActionFactory.PASTE.getId());
		actionBar.setGlobalActionHandler(ActionFactory.PASTE.getId(), textPasteAction);
		preUndoAction = actionBar.getGlobalActionHandler(ActionFactory.UNDO.getId());
		actionBar.setGlobalActionHandler(ActionFactory.UNDO.getId(), textUndoAction);
		preRedoAction = actionBar.getGlobalActionHandler(ActionFactory.REDO.getId());
		actionBar.setGlobalActionHandler(ActionFactory.REDO.getId(), textRedoAction);
		// actionBar.setGlobalActionHandler(ActionFactory.FIND.getId(), textFindReplaceAction);
		if (viewer != null && viewer.getTextWidget().isFocusControl()) {
			updateActionsEnableState();
		} else {
			actionBar.updateActionBars();
		}
	}
	
	private IAction preCutAction;
	private IAction preCopyAction;
	private IAction prePasteAction;
	private IAction preUndoAction;
	private IAction preRedoAction;
	public void resetGlobalActionHandler() {
		actionBar.setGlobalActionHandler(ActionFactory.CUT.getId(), preCutAction);
		actionBar.setGlobalActionHandler(ActionFactory.COPY.getId(), preCopyAction);
		actionBar.setGlobalActionHandler(ActionFactory.PASTE.getId(), prePasteAction);
		actionBar.setGlobalActionHandler(ActionFactory.UNDO.getId(), preUndoAction);
		actionBar.setGlobalActionHandler(ActionFactory.REDO.getId(), preRedoAction);
		actionBar.updateActionBars();
	}
	/**
	 * Dispose of this action handler
	 */
	public void dispose() {
	}

	/**
	 * Removes a <code>Text</code> control from the handler so that the Cut, Copy, Paste, Delete, and Select All actions
	 * are no longer redirected to it when active.
	 * @param textControl
	 *            the inline <code>Text</code> control
	 */
	public void removeTextViewer() {
		if (viewer == null) {
			return;
		}
		viewer = null;
		updateActionsEnableState();
	}

	/**
	 * Update the enable state of the Cut, Copy, Paste, Delete, Undo, Redo and Select All action handlers
	 */
	public void updateActionsEnableState() {
		textCutAction.updateEnabledState();
		textCopyAction.updateEnabledState();
		textPasteAction.updateEnabledState();
		textUndoAction.updateEnabledState();
		textRedoAction.updateEnabledState();
		// textFindReplaceAction.updateEnabledState();
		if (actionBar == null) {
			return;
		}
		actionBar.updateActionBars();
	}
}
