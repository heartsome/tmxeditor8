package net.heartsome.license;

import java.text.MessageFormat;

import net.heartsome.cat.common.ui.dialog.HsAbstractHelpDilog;
import net.heartsome.license.constants.Constants;
import net.heartsome.license.generator.LicenseIdGenerator;
import net.heartsome.license.resource.Messages;
import net.heartsome.license.utils.StringUtils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InvalidateDialog extends HsAbstractHelpDilog {

	private int type;
	private String utilDate;
	private String licenseId;
	private Text text;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public InvalidateDialog(Shell parentShell, int type, String utilDate, String licenseId) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.TITLE);
		this.type = type;
		this.utilDate = utilDate;
		this.licenseId = licenseId;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("license.InvalidateDialog.title"));
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(container, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		Label warningIcon = new Label(composite, SWT.NONE);
		warningIcon.setImage(Display.getDefault().getSystemImage(SWT.ICON_WARNING));

		Label msgLabel = new Label(composite, SWT.WRAP);
		GridData gd_lblNewLabel = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_lblNewLabel.heightHint = 75;
		gd_lblNewLabel.widthHint = 460;
		msgLabel.setLayoutData(gd_lblNewLabel);
		msgLabel.setText(Messages.getString("license.InvalidateDialog.msg1"));

		text = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		text.setText(getValidateMessage());

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("license.InvalidateDialog.activeBtn"), true);
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("license.InvalidateDialog.closeBtn"), false);
		Button okBtn = getButton(IDialogConstants.OK_ID);
		if (type == Constants.EXCEPTION_INT16 || type == Constants.EXCEPTION_INT17 || type == Constants.EXCEPTION_INT1
				|| type == Constants.EXCEPTION_INT2 || type == Constants.EXCEPTION_INT3
				|| type == Constants.EXCEPTION_INT4) {
			okBtn.setEnabled(false);
		}
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(514, 261);
	}

	@Override
	protected void okPressed() {
		super.okPressed();
		if (type == Constants.STATE_NOT_ACTIVATED) {
			// 未激活
			LicenseAgreementDialog dialog = new LicenseAgreementDialog(Display.getDefault().getActiveShell());
			dialog.open();
		} else if (type == Constants.STATE_INVALID) {
			// 许可证无效
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(),
					Constants.STATE_INVALID, utilDate, licenseId);
			dialog.open();
		} else if (type == Constants.STATE_EXPIRED) {
			// 试用期已满
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(),
					Constants.STATE_EXPIRED, utilDate, licenseId);
			dialog.open();
		} else if (type == Constants.EXCEPTION_INT14) {
			// 产品版本不一致
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(),
					Constants.EXCEPTION_INT14, utilDate, licenseId);
			dialog.open();
		} else if (type == Constants.EXCEPTION_INT15) {
			// 该许可证并非在当前计算机上激活，或您已更换本计算机的硬件。 请重新在本计算机上激活您的许可证。
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(),
					Constants.EXCEPTION_INT15, utilDate, licenseId);
			dialog.open();
		} else {
			// 请尝试重新运行本软件，或以管理员身份重新运行。 若此问题仍然存在，您也可以尝试重新激活许可证，或联系
			// Heartsome 技术支持人员，并提供如下错误代码：{0}。
			LicenseManageDialog dialog = new LicenseManageDialog(Display.getDefault().getActiveShell(), type, utilDate,
					licenseId);
			dialog.open();
		}
	}

	private String getValidateMessage() {
		String invalidReasonMsg = "";
		if (type == Constants.STATE_NOT_ACTIVATED) {
			invalidReasonMsg = Messages.getString("license.LicenseManageDialog.notActiveLabel");
		} else if (type == Constants.STATE_INVALID) {
			invalidReasonMsg = Messages.getString("license.LicenseManageDialog.invalidLicense");
		} else if (type == Constants.STATE_EXPIRED) {
			invalidReasonMsg = Messages.getString("license.LicenseManageDialog.expired");
		} else if (type == Constants.EXCEPTION_INT16 || type == Constants.EXCEPTION_INT17) {
			// 网络原因
			invalidReasonMsg = MessageFormat.format(Messages.getString("license.LicenseManageDialog.noticeLbl"),
					StringUtils.getErrorCode(type));
		} else if (type == Constants.EXCEPTION_INT14) {
			// 产品版本不一致
			invalidReasonMsg = MessageFormat.format(Messages.getString("license.LicenseManageDialog.noSameVersion"),
					getVersionContent(System.getProperty("TSEdition")), getVersionContent(new LicenseIdGenerator(
							licenseId).getVersion()));

		} else if (type == Constants.EXCEPTION_INT15) {
			invalidReasonMsg = Messages.getString("license.LicenseManageDialog.maccodeError");
			// 该许可证并非在当前计算机上激活，或您已更换本计算机的硬件。 请重新在本计算机上激活您的许可证。
		} else if (type == Constants.EXCEPTION_INT1 || type == Constants.EXCEPTION_INT2
				|| type == Constants.EXCEPTION_INT3 || type == Constants.EXCEPTION_INT4) {
			invalidReasonMsg = MessageFormat.format(
					Messages.getString("license.LicenseManageDialog.licenseExceptionInfo1"),
					StringUtils.getErrorCode(type));
			// 请尝试重新运行本软件，或以管理员身份重新运行。 若此问题仍然存在，您也可以尝试重新激活许可证，或联系
			// Heartsome 技术支持人员，并提供如下错误代码：{0}。
		} else {
			invalidReasonMsg = MessageFormat.format(
					Messages.getString("license.LicenseManageDialog.licenseExceptionInfo"),
					StringUtils.getErrorCode(type));
			// 请尝试重新运行本软件，或以管理员身份重新运行。 若此问题仍然存在，您也可以尝试重新激活许可证，或联系
			// Heartsome 技术支持人员，并提供如下错误代码：{0}。
		}
		return invalidReasonMsg;
	}

	private String getVersionContent(String version) {
		if ("U".equals(version)) {
			return Messages.getString("license.LicenseManageDialog.Ultimate");
		} else if ("F".equals(version)) {
			return Messages.getString("license.LicenseManageDialog.Professional");
		} else if ("P".equals(version)) {
			return Messages.getString("license.LicenseManageDialog.Personal");
		} else {
			return Messages.getString("license.LicenseManageDialog.Lite");
		}
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractHelpDilog#getDisplayHelpUrl()
	 */
	@Override
	public String getDisplayHelpUrl() {
		return "/net.heartsome.cat.te.ui.help/html/{0}/ch08.html#license_id";
	}
}
