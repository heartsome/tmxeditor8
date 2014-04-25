package net.heartsome.cat.database.ui.tm.wizard;

import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.database.ui.tm.preference.TMDatabasePage;
import net.heartsome.cat.database.ui.tm.resource.Messages;
import net.heartsome.cat.ts.ui.util.PreferenceUtil;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * 导入 TMX 向导框
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ImportTmxWizardDialog extends TSWizardDialog {

	private Button btnSetting;
	
	public ImportTmxWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnSetting = createButton(parent, -1, Messages.getString("wizard.ImportTmxPage.settingBtn"), true);
		super.createButtonsForButtonBar(parent);
		btnSetting.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferenceUtil.openPreferenceDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), TMDatabasePage.ID);
			}
		});
	}
	
	@Override
	public void updateButtons() {
		super.updateButtons();
		btnSetting.setVisible(getCurrentPage() instanceof ImportTmxPage
				|| getCurrentPage() instanceof NewTmDbImportPage
				|| getCurrentPage() instanceof TmDbManagerImportWizardTmxPage);
	}
}
