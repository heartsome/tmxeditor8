/**
 * BatchJobsDialog.java
 *
 * Version information :
 *
 * Date:2013-8-6
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.ui.task.ui;

import net.heartsome.cat.common.ui.dialog.HsAbstractHelpDilog;
import net.heartsome.cat.te.tmxeditor.Activator;
import net.heartsome.cat.te.ui.resource.Messages;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class BatchJobsDialog extends HsAbstractHelpDilog {

	private Button deleteEmptyBtn;
	private Button deleteDupliteBtn;
	private Button deleteSameSrcDiffTgtBtn;
	private Button trimBtn;
	private Button ignoreTagBtn;
	private Button ignoreCaseBtn;

	private BatchSelectionsBean jobs;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public BatchJobsDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		// TODO Auto-generated method stub
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.BatchJobsDialog.title"));
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gl_container = new GridLayout(1, false);
		gl_container.verticalSpacing = 6;
		container.setLayout(gl_container);

		Group jobsGroup = new Group(container, SWT.NONE);
		jobsGroup.setLayoutData(GridDataFactory.createFrom(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1))
				.hint(450, SWT.DEFAULT).create());
		GridLayout gl_jobsGroup = new GridLayout(1, false);
		gl_jobsGroup.verticalSpacing = 7;
		jobsGroup.setLayout(gl_jobsGroup);
		jobsGroup.setText(Messages.getString("dialog.BatchJobsDialog.bacthJobSelection"));

		deleteEmptyBtn = new Button(jobsGroup, SWT.CHECK);
		deleteEmptyBtn.setText(Messages.getString("dialog.BatchJobsDialog.deleteEmpty"));
		deleteEmptyBtn.addSelectionListener(new SelectionAdapter() {
			/**
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {

				setIgoreTagState();

			}
		});
		
		
		deleteDupliteBtn = new Button(jobsGroup, SWT.CHECK);
		deleteDupliteBtn.setText(Messages.getString("dialog.BatchJobsDialog.deleteDuplicate"));

		deleteDupliteBtn.addSelectionListener(new SelectionAdapter() {
			/**
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {

				setIgoreTagState();

			}
		});

		deleteSameSrcDiffTgtBtn = new Button(jobsGroup, SWT.CHECK);
		deleteSameSrcDiffTgtBtn.setText(Messages.getString("dialog.BatchJobsDialog.deleteSameSrcDiffTgt"));
		deleteSameSrcDiffTgtBtn.addSelectionListener(new SelectionAdapter() {
			/**
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				setIgoreTagState();
			}
		});

		trimBtn = new Button(jobsGroup, SWT.CHECK);
		trimBtn.setText(Messages.getString("dialog.BatchJobsDialog.trimSegment"));

		Group ignoreGroup = new Group(container, SWT.NONE);
		ignoreGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		ignoreGroup.setText(Messages.getString("dialog.BatchJobsDialog.ingoreSelection"));
		ignoreGroup.setLayout(new GridLayout(2, false));
		ignoreTagBtn = new Button(ignoreGroup, SWT.CHECK);
		ignoreTagBtn.setText(Messages.getString("dialog.BatchJobsDialog.ingoreTag"));

		ignoreCaseBtn = new Button(ignoreGroup, SWT.CHECK);
		ignoreCaseBtn.setText(Messages.getString("dialog.BatchJobsDialog.ignoreCase"));

		Label separator = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setIgoreTagState();
		loadDialogSettings();
		return container;
	}

	private void setIgoreTagState() {

		if (deleteDupliteBtn.getSelection() || deleteSameSrcDiffTgtBtn.getSelection()) {
			ignoreCaseBtn.setEnabled(true);
		} else {
			ignoreCaseBtn.setEnabled(false);
		}

		if (deleteDupliteBtn.getSelection() || deleteSameSrcDiffTgtBtn.getSelection()// <br/>
				|| deleteEmptyBtn.getSelection()) {
			ignoreTagBtn.setEnabled(true);
		} else {
			ignoreTagBtn.setEnabled(false);
		}
	}

	private BatchSelectionsBean setJobs() {
		BatchSelectionsBean bean = new BatchSelectionsBean();
		bean.setDeleteDupliacate(deleteDupliteBtn.getSelection());
		bean.setDeleteEmpty(deleteEmptyBtn.getSelection());
		bean.setDeleteSameSrcDiffTgt(deleteSameSrcDiffTgtBtn.getSelection());
		bean.setIgnoreTag(ignoreTagBtn.getSelection() && ignoreTagBtn.getEnabled());
		bean.setIgnoreCase(ignoreCaseBtn.getSelection() && ignoreCaseBtn.getEnabled());
		bean.setTrimSegment(trimBtn.getSelection());
		return bean;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		jobs = setJobs();
		super.okPressed();
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#close()
	 */
	@Override
	public boolean close() {
		// TODO Auto-generated method stub
		if (getShell() != null && !getShell().isDisposed()) {
			writeDialogSettings();
		}
		return super.close();
	}

	public BatchSelectionsBean getJobs() {
		return jobs;
	}

	private void writeDialogSettings() {
		IDialogSettings dialogSettings = getDialogSettings();
		dialogSettings.put("EMPTY", deleteEmptyBtn.getSelection());
		dialogSettings.put("TRIMENDS", trimBtn.getSelection());
		dialogSettings.put("DUPLICATE", deleteDupliteBtn.getSelection());
		dialogSettings.put("DIFFRENT", deleteSameSrcDiffTgtBtn.getSelection());
		dialogSettings.put("IGNORE_TAG_ENALBE", ignoreTagBtn.getEnabled());
		dialogSettings.put("IGNORE_TAG_SELECT", ignoreTagBtn.getSelection());
		dialogSettings.put("IGNORE_CASE_ENALBE", ignoreCaseBtn.getEnabled());
		dialogSettings.put("IGNORE_CASE_SELECT", ignoreCaseBtn.getSelection());
	}

	private void loadDialogSettings() {
		IDialogSettings dialogSettings = getDialogSettings();
		deleteEmptyBtn.setSelection(dialogSettings.getBoolean("EMPTY"));
		trimBtn.setSelection(dialogSettings.getBoolean("TRIMENDS"));
		deleteDupliteBtn.setSelection(dialogSettings.getBoolean("DUPLICATE"));
		deleteSameSrcDiffTgtBtn.setSelection(dialogSettings.getBoolean("DIFFRENT"));
		ignoreTagBtn.setEnabled(dialogSettings.getBoolean("IGNORE_TAG_ENALBE"));
		ignoreTagBtn.setSelection(dialogSettings.getBoolean("IGNORE_TAG_SELECT"));
		ignoreCaseBtn.setEnabled(dialogSettings.getBoolean("IGNORE_CASE_ENALBE"));
		ignoreCaseBtn.setSelection(dialogSettings.getBoolean("IGNORE_CASE_SELECT"));
	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings fDialogSettings = settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings = settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.common.ui.dialog.HsAbstractHelpDilog#getDisplayHelpUrl()
	 */
	@Override
	public String getDisplayHelpUrl() {
		// TODO Auto-generated method stub
		return "/net.heartsome.cat.te.ui.help/html/{0}/ch03.html#clean_tmx_id";
	}
}
