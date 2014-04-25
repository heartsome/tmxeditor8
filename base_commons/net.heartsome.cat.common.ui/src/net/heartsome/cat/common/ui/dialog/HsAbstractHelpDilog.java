/**
 * HsAbstractHelpDilog.java
 *
 * Version information :
 *
 * Date:2013-11-29
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.ui.dialog;

import java.text.MessageFormat;

import net.heartsome.cat.common.util.CommonFunction;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public abstract class HsAbstractHelpDilog extends TrayDialog {

	/**
	 * @param parentShell
	 */
	protected HsAbstractHelpDilog(IShellProvider parentShell) {
		super(parentShell);
	}

	public HsAbstractHelpDilog(Shell shell) {
		super(shell);
	}

	/**
	 * 
	 */
	@Override
	protected Control createHelpControl(Composite parent) {
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

	public abstract String getDisplayHelpUrl();

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#isHelpAvailable()
	 */
	@Override
	public boolean isHelpAvailable() {
		// TODO Auto-generated method stub
		return true;
	}
}
