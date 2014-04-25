package net.heartsome.cat.te;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.model.ContributionComparator;
import org.eclipse.ui.model.IContributionService;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "net.heartsome.cat.te.perspective"; //$NON-NLS-1$

	private OpenDocumentEventProcessor openDocProcessor;

	public ApplicationWorkbenchAdvisor(OpenDocumentEventProcessor openDocProcessor) {
		this.openDocProcessor = openDocProcessor;
	}

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		configurer.setSaveAndRestore(true);
	}

	/**
	 * 对首选项菜单排序时，需要覆盖该方法
	 */
	public ContributionComparator getComparatorFor(String contributionType) {
		if (contributionType.equals(IContributionService.TYPE_PREFERENCE)) {
			return new PreferencesComparator();
		} else {
			return super.getComparatorFor(contributionType);
		}
	}

	@Override
	public void eventLoopIdle(Display display) {
		openDocProcessor.openFiles();
		super.eventLoopIdle(display);
	}

}
