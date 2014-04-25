package net.heartsome.cat.common.ui.dialog;

import java.text.MessageFormat;

import net.heartsome.cat.common.ui.Activator;
import net.heartsome.cat.common.ui.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * 文件重复时，需要进行提示是否覆盖所用到的对话框。
 * @author  robert	2013-12-12
 * @version 
 * @since   JDK1.6
 */
public class FileCoverMsgDialog extends Dialog implements Listener {
	private Image warningImg = null;
	private final String fileName;
	
	private Button alwaysBtn;
	private Button skipBtn;
	private Button cancelBtn;
	private Button overBtn;
	
	public static boolean ALWAYS = false;
	public final static int CANCEL = IDialogConstants.CANCEL_ID;
	public final static int SKIP = 1025;	// 在　clien id 之后
	public final static int OVER = 1026;
	
	private final String alwaysStoreKey = "common.ui.fileCover.alwaysStoreKey";
	
	public FileCoverMsgDialog(Shell parentShell, String fileName) {
		super(parentShell);
		this.fileName = fileName;
		warningImg = Activator.getImageDescriptor("icons/dialog_warning.ico").createImage();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.FileCoverMsgDialog.windowTitle"));
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2; // this is incremented by createButton
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_CENTER);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		
		createAlwaysBtnArea(composite);
		createBtnArea(composite);
		
		return composite;
	}
	
	private void createAlwaysBtnArea(Composite parent){
		Composite btnCmp = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().grab(true, true).applyTo(btnCmp);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(1).applyTo(btnCmp);
		
		alwaysBtn = new Button(btnCmp, SWT.CHECK);
		alwaysBtn.setText(Messages.getString("dialog.FileCoverMsgDialog.alwaysBtn"));
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		alwaysBtn.setSelection(store.getBoolean(alwaysStoreKey));
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(alwaysBtn);
	}
	
	private void createBtnArea(Composite parent){
		Composite btnCmp = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(btnCmp);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(3).applyTo(btnCmp);
		
		// 跳过
		skipBtn = new Button(btnCmp, SWT.PUSH);
		skipBtn.setText(Messages.getString("dialog.FileCoverMsgDialog.skipBtn"));
		setButtonLayoutData(skipBtn);
		skipBtn.addListener(SWT.Selection, this);
		
		// 停止或退出
		cancelBtn = new Button(btnCmp, SWT.PUSH);
		cancelBtn.setText(Messages.getString("dialog.FileCoverMsgDialog.cancelBtn"));
		setButtonLayoutData(cancelBtn);
		cancelBtn.addListener(SWT.Selection, this);
		getShell().setDefaultButton(cancelBtn);
		
		// 覆盖
		overBtn = new Button(btnCmp, SWT.PUSH);
		overBtn.setText(Messages.getString("dialog.FileCoverMsgDialog.overBtn"));
		setButtonLayoutData(overBtn);
		overBtn.addListener(SWT.Selection, this);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		Point defaultSpacing = LayoutConstants.getSpacing();
		GridLayoutFactory.fillDefaults().margins(LayoutConstants.getMargins())
				.spacing(defaultSpacing.x * 2, defaultSpacing.y).numColumns(2).applyTo(tparent);
		
		Label imgLbl = new Label(tparent, SWT.TOP);
		imgLbl.setImage(warningImg);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(imgLbl);
		
		String message = MessageFormat.format(Messages.getString("dialog.FileCoverMsgDialog.message"), fileName);
		
		if (message != null) {
			Label messageLbl = new Label(tparent, SWT.WRAP);
			messageLbl.setText(message);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).hint(
					convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
					SWT.DEFAULT).applyTo(messageLbl);
		}
		
		return tparent;
	}

	@Override
	public boolean close() {
		if (warningImg != null) {
			warningImg.dispose();
		}
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(alwaysStoreKey, alwaysBtn.getSelection());
		return super.close();
	}

	public void handleEvent(Event event) {
		Widget resource = event.widget;
		if (resource == cancelBtn) {
			cancelPressed();
		}else if (resource == skipBtn) {
			setReturnCode(SKIP);
			ALWAYS = alwaysBtn.getSelection();
			close();
		}else if (resource == overBtn) {
			setReturnCode(OVER);
			ALWAYS = alwaysBtn.getSelection();
			close();
		}
	}
	

	/**
	 * 是否应用到所有
	 * @return ;
	 */
	public boolean isAlways() {
		return ALWAYS;
	}
	
	
}
