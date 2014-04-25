package net.heartsome.cat.common.ui.dialog;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.common.ui.resource.Messages;
import net.heartsome.cat.common.ui.utils.OpenMessageUtils;
import net.heartsome.cat.common.util.CommonFunction;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 1、 HsAbstractProgressDialog的功能是创建带进度条的对话框模板。以及控制子类任务运行<br>
 * <p>
 * <p>
 * 2、当子类实现了<code>IRunnableWithProgress</code>接口就会调用了子类的{@link IRunnableWithProgress#run(IProgressMonitor)}方法<br>
 * 子类也可以不实现该接口，自定义任务的运行方式 ，此时应该重写{@link #okPressed()}方法
 * </p>
 * <p>
 * 3、客户端需要实现子类需要实现{@link #createButtonsForButtonBar(Composite)}方法，来创建自己的对话框上面的组件 以及实现
 * {@link IRunnableWithProgress#run(IProgressMonitor)}方法
 * </p>
 * <p>
 * 4、 客户端可以重写{@link #isFork()}， {@link #canCancel()}来控制任务运行。是否单独运行，是否可以取消.<br>
 * 以及重写{@link #needsProgressMonitor()} 来控制是否显示进度条
 * </p>
 * @author yule
 * @version
 * @since JDK1.6
 */
public abstract class HsAbstractProgressDialog extends Dialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(HsAbstractProgressDialog.class);
	/**
	 * 锁定界面
	 */
	private boolean lockedUI = false;
	/**
	 * 进度条控件以及进度监视器
	 */
	private ProgressMonitorPart progressMonitorPart;

	/**
	 * 客户区
	 */
	private Composite container;

	/**
	 * 取消按钮
	 */
	private Button cancelButton;
	/**
	 * 确定按钮
	 */
	private Button okButton;
	/**
	 * 取消事件监听器
	 */
	private SelectionAdapter cancelListener;
	/**
	 * 等待光标
	 */
	private Cursor waitCursor;

	private Cursor arrowCursor;

	private static final int RESTORE_ENTER_DELAY = 500;

	private long activeRunningOperations = 0;

	// 是否子类自己创建进度条，需要重写createProgressMonitorPart()方法
	private boolean useCustomProgressMonitorPart = true;

	private static final String FOCUS_CONTROL = "focusControl"; //$NON-NLS-1$

	private long timeWhenLastJobFinished = -1;

	private MessageDialog windowClosingDialog;
	/** 确定按钮初始化值 */
	private boolean okBtnInitState = false;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public HsAbstractProgressDialog(Shell parentShell) {
		super(parentShell);
		cancelListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cancelPressed();
			}
		};
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	public final Control createDialogArea(Composite parent) {
		container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// 创建客户区组件
		createClientArea(composite);

		Composite compositeProgress = new Composite(container, SWT.NONE);
		compositeProgress.setLayout(new GridLayout(1, false));
		compositeProgress.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		// Insert a progress monitor
		setProgressMonitorPart(createProgressMonitorPart(compositeProgress, new GridLayout()));
		getProgressMonitorPart().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		getProgressMonitorPart().setVisible(false);

		// // Build the separator line
		Label separator = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return container;
	}

	/**
	 * 创建客户区域组件
	 * @return ;
	 */
	public abstract Composite createClientArea(Composite clientContainer);

	/**
	 * 创建按钮 (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		setOkBtnEnable(okBtnInitState);
		cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		cancelButton.addSelectionListener(cancelListener);
	}

	@Override
	protected void cancelPressed() {
		if (activeRunningOperations <= 0) {
			setReturnCode(CANCEL);
			close();
		} else {
			cancelButton.setEnabled(false);
		}
	}

	/**
	 * 初始化对话框位置
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	/**
	 * 运行线程
	 * @param fork
	 * @param cancelable
	 * @param runnable
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 *             ;
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		// The operation can only be canceled if it is executed in a separate
		// thread.
		// Otherwise the UI is blocked anyway.
		Object state = null;
		if (activeRunningOperations == 0) {
			state = aboutToStart(fork && cancelable);
		}
		activeRunningOperations++;
		try {
			if (!fork) {
				lockedUI = true;
			}
			ModalContext.run(runnable, fork, getProgressMonitor(), getShell().getDisplay());
			lockedUI = false;
		} finally {
			// explicitly invoke done() on our progress monitor so that its
			// label does not spill over to the next invocation, see bug 271530
			if (getProgressMonitor() != null) {
				getProgressMonitor().done();
			}
			// Stop if this is the last one
			if (state != null) {
				timeWhenLastJobFinished = System.currentTimeMillis();
				stopped(state);
			}
			activeRunningOperations--;
		}
	}

	/**
	 * 创建进度条以及进度监视器
	 * @param composite
	 * @param pmlayout
	 * @return ;
	 */
	protected ProgressMonitorPart createProgressMonitorPart(Composite composite, GridLayout pmlayout) {

		useCustomProgressMonitorPart = false;

		return new ProgressMonitorPart(composite, pmlayout, true) {
			String currentTask = null;

			public void setBlocked(IStatus reason) {
				super.setBlocked(reason);
				if (!lockedUI) {
					getBlockedHandler().showBlocked(getShell(), this, reason, currentTask);
				}
			}

			public void clearBlocked() {
				super.clearBlocked();
				if (!lockedUI) {
					getBlockedHandler().clearBlocked();
				}
			}

			public void beginTask(String name, int totalWork) {
				super.beginTask(name, totalWork);
				currentTask = name;
			}

			public void setTaskName(String name) {
				super.setTaskName(name);
				currentTask = name;
			}

			public void subTask(String name) {
				super.subTask(name);
				if (currentTask == null) {
					currentTask = name;
				}
			}
		};
	}

	protected ProgressMonitorPart getProgressMonitorPart() {
		return progressMonitorPart;
	}

	/**
	 * 运行任务的准备工作
	 * @return UI状态
	 */
	@SuppressWarnings("rawtypes")
	private Object aboutToStart(boolean enableCancelButton) {
		Map savedState = null;
		if (getShell() != null) {
			// Save focus control
			Control focusControl = getShell().getDisplay().getFocusControl();
			if (focusControl != null && focusControl.getShell() != getShell()) {
				focusControl = null;
			}
			boolean needsProgressMonitor = needsProgressMonitor();

			// 设置Shell的鼠标状态
			Display d = getShell().getDisplay();
			waitCursor = new Cursor(d, SWT.CURSOR_WAIT);
			setDisplayCursor(waitCursor);

			if (useCustomProgressMonitorPart) {
				cancelButton.removeSelectionListener(cancelListener);
				// 设置取消按钮的鼠标状态
				arrowCursor = new Cursor(d, SWT.CURSOR_ARROW);
				cancelButton.setCursor(arrowCursor);
			}

			// Deactivate shell
			savedState = saveUIState(useCustomProgressMonitorPart && needsProgressMonitor && enableCancelButton);
			if (focusControl != null) {
				savedState.put(FOCUS_CONTROL, focusControl);
			}
			// Activate cancel behavior.
			if (needsProgressMonitor) {
				if (enableCancelButton || useCustomProgressMonitorPart) {
					progressMonitorPart.attachToCancelComponent(cancelButton);
				}
				progressMonitorPart.setVisible(true);
			}

			if (timeWhenLastJobFinished == -1) {
				timeWhenLastJobFinished = 0;
				// 忽略键盘事件（ enter space）在最后一个任务完成之前
				getShell().addTraverseListener(new TraverseListener() {
					public void keyTraversed(TraverseEvent e) {
						if (e.detail == SWT.TRAVERSE_RETURN || (e.detail == SWT.TRAVERSE_MNEMONIC && e.keyCode == 32)) {
							if (timeWhenLastJobFinished != 0
									&& System.currentTimeMillis() - timeWhenLastJobFinished < RESTORE_ENTER_DELAY) {
								e.doit = false;
								return;
							}
							timeWhenLastJobFinished = 0;
						}
					}
				});
			}
		}
		return savedState;
	}

	public void setProgressMonitorPart(ProgressMonitorPart progressMonitorPart) {
		this.progressMonitorPart = progressMonitorPart;
	}

	protected IProgressMonitor getProgressMonitor() {
		return progressMonitorPart;
	}

	/**
	 * 默认按下任务运行 (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		if (this instanceof IRunnableWithProgress) {
			try {
				run(isFork(), canCancel(), (IRunnableWithProgress) this);
				if (getProgressMonitor().isCanceled()) {
					return;
				}
				OpenMessageUtils.openMessage(IStatus.INFO,
						Messages.getString("dialog.HsabstractProgressDialog.taskFinished"));
				super.okPressed();
			} catch (InvocationTargetException e) {
				LOGGER.error("", e);
				Throwable t = e.getTargetException();
				if (t != null && t instanceof OutOfMemoryError) {
					OpenMessageUtils.openMessage(IStatus.INFO,
							Messages.getString("dialog.HsabstractProgressDialog.outofmemory"));
				}
				return;
			} catch (Exception e) {
				LOGGER.error("", e);
				OpenMessageUtils.openMessage(IStatus.ERROR, e.getMessage());
			}
		}
		return;
	}

	/**
	 * 设置鼠标
	 */
	private void setDisplayCursor(Cursor c) {
		Shell[] shells = getShell().getDisplay().getShells();
		for (int i = 0; i < shells.length; i++) {
			shells[i].setCursor(c);
		}
	}

	@Override
	public boolean close() {
		if (okToClose()) {
			return super.close();
		}
		return false;
	}

	/**
	 * 任务终止
	 * @param savedState
	 *            ;
	 */
	private void stopped(Object savedState) {
		if (getShell() != null && !getShell().isDisposed()) {
			if (needsProgressMonitor()) {
				progressMonitorPart.setVisible(false);
				progressMonitorPart.removeFromCancelComponent(cancelButton);
			}
			Map state = (Map) savedState;
			restoreUIState(state);
			setDisplayCursor(null);
			if (useCustomProgressMonitorPart) {
				cancelButton.addSelectionListener(cancelListener);
				cancelButton.setCursor(null);
				arrowCursor.dispose();
				arrowCursor = null;
			}
			waitCursor.dispose();
			waitCursor = null;
			Control focusControl = (Control) state.get(FOCUS_CONTROL);
			if (focusControl != null && !focusControl.isDisposed()) {
				focusControl.setFocus();
			}
		}
	}

	/**
	 * 恢复按钮状态
	 */
	private void restoreUIState(Map state) {
		restoreEnableState(okButton, state, "ok"); //$NON-NLS-1$
		restoreEnableState(cancelButton, state, "cancel"); //$NON-NLS-1$

	}

	private void restoreEnableState(Control w, Map h, String key) {
		if (w != null) {
			Boolean b = (Boolean) h.get(key);
			if (b != null) {
				w.setEnabled(b.booleanValue());
			}
		}
	}

	protected boolean needsProgressMonitor() {
		return true;
	}

	private Map saveUIState(boolean keepCancelEnabled) {
		Map savedState = new HashMap(2);
		saveEnableStateAndSet(okButton, savedState, "ok", false); //$NON-NLS-1$
		saveEnableStateAndSet(cancelButton, savedState, "cancel", keepCancelEnabled); //$NON-NLS-1$

		return savedState;
	}

	@SuppressWarnings("unchecked")
	private void saveEnableStateAndSet(Control w, Map h, String key, boolean enabled) {
		if (w != null) {
			h.put(key, w.getEnabled() ? Boolean.TRUE : Boolean.FALSE);
			w.setEnabled(enabled);
		}
	}

	/**
	 * 是否可以关闭对话框
	 */
	private boolean okToClose() {
		if (activeRunningOperations > 0) {
			synchronized (this) {
				windowClosingDialog = createWizardClosingDialog();
			}
			windowClosingDialog.open();
			synchronized (this) {
				windowClosingDialog = null;
			}
			return false;
		}
		return true;
	}

	/**
	 * 创建关闭对话框
	 * @return MessageDalog
	 */
	private MessageDialog createWizardClosingDialog() {
		MessageDialog result = new MessageDialog(getShell(),
				Messages.getString("dialog.HsabstractProgressDialog.closeTtile"), //$NON-NLS-1$
				null, Messages.getString("dialog.HsabstractProgressDialog.closeMsg"), //$NON-NLS-1$
				MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL }, 0) {
			protected int getShellStyle() {
				return super.getShellStyle() | SWT.SHEET;
			}
		};
		return result;
	}

	/**
	 * 是否以单独的线程运行,子类可以重写该方法
	 * @return ;
	 */
	public boolean isFork() {
		return true;
	}

	/**
	 * 任务是否可以取消，子类可以重写该方法
	 * @return boolean;
	 */
	public boolean canCancel() {
		return true;
	}

	/**
	 * create help button (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		composite.setFont(parent.getFont());

		// create help control if needed
		Control helpControl = createHelpControl(composite);
		if (helpControl != null) {
			((GridData) helpControl.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		}
		Control buttonSection = super.createButtonBar(composite);
		((GridData) buttonSection.getLayoutData()).grabExcessHorizontalSpace = true;
		return composite;
	}

	protected Control createHelpControl(Composite parent) {
		if (null == getDisplayHelpUrl()) {
			return null;
		}
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(getDisplayHelpUrl(), language);
		Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
		((GridLayout) parent.getLayout()).numColumns++;
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolBar.setCursor(cursor);
		toolBar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cursor.dispose();
			}
		});
		ToolItem helpItem = new ToolItem(toolBar, SWT.NONE);
		helpItem.setImage(helpImage);
		helpItem.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
		helpItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
			}
		});
		return toolBar;

	}

	public String getDisplayHelpUrl() {
		return null;
	}

	protected void setOkBtnEnable(boolean enable) {
		if (okButton == null) {
			okBtnInitState = enable;
		} else {
			okButton.setEnabled(enable);
		}
	}

}
