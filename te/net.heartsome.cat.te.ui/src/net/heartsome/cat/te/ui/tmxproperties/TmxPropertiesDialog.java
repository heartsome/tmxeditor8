package net.heartsome.cat.te.ui.tmxproperties;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.util.LanguageUtils;
import net.heartsome.cat.te.core.bean.TmxPropertiesBean;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TmxPropertiesDialog extends Dialog {
	private Text txtVersion;
	private Text txtLocation;
	private Text txtSize;
	private Text txtCreateUser;
	private Text txtCreateTime;
	private Text txtCreateTool;
	private Text txtCountTu;
	private Text txtSourceLang;
	private Text txtTargetLang;

	private TmxPropertiesBean tmxPropBean;
	private Text txtCreateToolVersion;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public TmxPropertiesDialog(Shell parentShell, TmxPropertiesBean tmxPropBean) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		this.tmxPropBean = tmxPropBean;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("tmxproperties.dialog.dialogTitile"));
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setEnabled(false);
		GridLayout gl_container = new GridLayout(2, false);
		gl_container.marginTop = 5;
		gl_container.marginRight = 5;
		gl_container.marginLeft = 5;
		container.setLayout(gl_container);
		GridDataFactory.swtDefaults().hint(650, SWT.DEFAULT).applyTo(container);

		GridData gdLeftAlign = new GridData(SWT.RIGHT, SWT.CENTER, false, false);

		Label titleLabel = new Label(container, SWT.NONE);
		titleLabel.setFont(JFaceResources.getBannerFont());
		titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		titleLabel.setText(Messages.getString("tmxproperties.dialog.resourceTitle"));

		Label sp1 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		sp1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		Label lblVersion = new Label(container, SWT.NONE);
		lblVersion.setText(Messages.getString("tmxproperties.dialog.version"));

		txtVersion = new Text(container, SWT.NONE);
		txtVersion.setEnabled(false);
		txtVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblLocation = new Label(container, SWT.NONE);
		lblLocation.setText(Messages.getString("tmxproperties.dialog.location"));

		txtLocation = new Text(container, SWT.NONE);
		txtLocation.setEnabled(false);
		txtLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblSize = new Label(container, SWT.NONE);
		lblSize.setText(Messages.getString("tmxproperties.dialog.size"));

		txtSize = new Text(container, SWT.NONE);
		txtSize.setEnabled(false);
		txtSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblCreateUser = new Label(container, SWT.NONE);
		lblCreateUser.setText(Messages.getString("tmxproperties.dialog.createuser"));

		txtCreateUser = new Text(container, SWT.NONE);
		txtCreateUser.setEnabled(false);
		txtCreateUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblCreateTime = new Label(container, SWT.NONE);
		lblCreateTime.setText(Messages.getString("tmxproperties.dialog.createtime"));

		txtCreateTime = new Text(container, SWT.NONE);
		txtCreateTime.setEnabled(false);
		txtCreateTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblCreateTool = new Label(container, SWT.NONE);
		lblCreateTool.setText(Messages.getString("tmxproperties.dialog.createtool"));

		txtCreateTool = new Text(container, SWT.NONE);
		txtCreateTool.setEnabled(false);
		txtCreateTool.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblCreateToolVersion = new Label(container, SWT.NONE);
		lblCreateToolVersion.setText(Messages.getString("tmxproperties.dialog.createversion"));

		txtCreateToolVersion = new Text(container, SWT.NONE);
		txtCreateToolVersion.setEnabled(false);
		txtCreateToolVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label sp2 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		sp2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		Label lblCountTu = new Label(container, SWT.NONE);
		lblCountTu.setText(Messages.getString("tmxproperties.dialog.conuttu"));

		txtCountTu = new Text(container, SWT.NONE);
		txtCountTu.setEnabled(false);
		txtCountTu.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblSourceLang = new Label(container, SWT.NONE);
		lblSourceLang.setText(Messages.getString("tmxproperties.dialog.sourcelang"));
		
		txtSourceLang = new Text(container, SWT.NONE);
		txtSourceLang.setEnabled(false);
		txtSourceLang.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblTargetLang = new Label(container, SWT.NONE);
		lblTargetLang.setText(Messages.getString("tmxproperties.dialog.targetlang"));
		int size = tmxPropBean.getTargetLang().size();
		txtTargetLang = new Text(container, SWT.MULTI);
		txtTargetLang.setEnabled(false);
		txtTargetLang.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, --size));
		for (int i = 0; i < size; i++) {
			new Label(container, SWT.NONE);
		}

		// label align left #3816
		GridDataFactory.createFrom(gdLeftAlign).applyTo(lblVersion);
		GridDataFactory.createFrom(gdLeftAlign).applyTo(lblLocation);
		GridDataFactory.createFrom(gdLeftAlign).applyTo(lblSize);
		GridDataFactory.createFrom(gdLeftAlign).applyTo(lblCreateUser);
		GridDataFactory.createFrom(gdLeftAlign).applyTo(lblCreateTime);
		GridDataFactory.createFrom(gdLeftAlign).applyTo(lblCreateTool);
		GridDataFactory.createFrom(gdLeftAlign).applyTo(lblCreateToolVersion);
		GridDataFactory.createFrom(gdLeftAlign).applyTo(lblCountTu);
		GridDataFactory.createFrom(gdLeftAlign).applyTo(lblSourceLang);
		GridDataFactory.createFrom(gdLeftAlign).align(SWT.RIGHT, SWT.TOP).applyTo(lblTargetLang);

		this.fillInfo();
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}

	private void fillInfo() {
		if (tmxPropBean != null) {
			if (tmxPropBean.getVersion() != null) {
				txtVersion.setText(tmxPropBean.getVersion());
			}
			if (tmxPropBean.getLocation() != null) {
				txtLocation.setText(tmxPropBean.getLocation());
			}
			if (tmxPropBean.getFileSize() != null) {
				DecimalFormat df = new DecimalFormat("#.##");
				NumberFormat nf = NumberFormat.getInstance();
				Long lSize = Long.parseLong(tmxPropBean.getFileSize());
				String size = null;
				if (lSize < 1024) {
					size = new StringBuffer().append(lSize).append(" bytes").toString();
				} else if (lSize < 1024 * 1024) {
					double d = Double.parseDouble(df.format(lSize / 1024.0));
					size = new StringBuffer().append(nf.format(d)).append(" KB").toString();
				} else if (lSize < 1024 * 1024 * 1024) {
					double d = Double.parseDouble(df.format(lSize / (1024.0 * 1024.0)));
					size = new StringBuffer().append(nf.format(d)).append(" MB").toString();
				} else {
					double d = Double.parseDouble(df.format(lSize / (1024.0 * 1024.0 * 1024.0)));
					size = new StringBuffer().append(nf.format(d)).append(" GB").toString();
				}
				txtSize.setText(size);
			}
			if (tmxPropBean.getCreator() != null) {
				txtCreateUser.setText(tmxPropBean.getCreator());
			}
			if (tmxPropBean.getCreationDate() != null) {
				txtCreateTime.setText(tmxPropBean.getCreationDate());
			}
			if (tmxPropBean.getCreationTooles() != null) {
				txtCreateTool.setText(tmxPropBean.getCreationTooles());
			}
			if (tmxPropBean.getCreationTooleVersion() != null) {
				txtCreateToolVersion.setText(tmxPropBean.getCreationTooleVersion());
			}
			if (tmxPropBean.getTuNumber() != 0) {
				txtCountTu.setText(String.valueOf(tmxPropBean.getTuNumber()));
			}
			Language src = LocaleService.getDefaultLanguage().get(tmxPropBean.getSrcLang());
			String srcDisplay = src == null ? tmxPropBean.getSrcLang() : src.toString();
			txtSourceLang.setText(srcDisplay);

			if (tmxPropBean.getTargetLang() != null) {
				StringBuffer targetLang = new StringBuffer();
				for (String str : tmxPropBean.getTargetLang()) {
					Language language = LocaleService.getDefaultLanguage().get(LanguageUtils.convertLangCode(str));
					targetLang.append(language == null ? str : language.toString()).append("\n");
				}
				txtTargetLang.setText(targetLang.subSequence(0, targetLang.length() - 1).toString());
			}
		}
	}

//	/**
//	 * Return the initial size of the dialog.
//	 */
//	@Override
//	protected Point getInitialSize() {
//		return new Point(650, 369);
//	}

}
