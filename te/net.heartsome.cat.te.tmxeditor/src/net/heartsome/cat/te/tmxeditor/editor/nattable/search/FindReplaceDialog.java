package net.heartsome.cat.te.tmxeditor.editor.nattable.search;

import java.text.MessageFormat;
import java.util.regex.Matcher;

import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.te.core.tmxdata.AbstractTmxDataAccess;
import net.heartsome.cat.te.core.tmxdata.DatabaseDataAccess;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileDataAccess;
import net.heartsome.cat.te.tmxeditor.Activator;
import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable;
import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.CellEditor;
import net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor.TeActiveCellEditor;
import net.heartsome.cat.te.tmxeditor.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Find/replace dialog
 * @author yule
 * @version
 * @since JDK1.5
 */
public class FindReplaceDialog extends Dialog {

	private Combo findTextCombo;
	private Combo replaceTextCombo;

	private Button forwardRadioBtn;
	private Button backRadioBtn;

	private Button sourceRadioBtn;
	private Button targetRadioBtn;

	private Button ignoreCaseCheckBtn;
	private Button wholeWordCheckBtn;
	private Button regExCheckBtn;

	private Button findBtn;
	private Button replaceFindBtn;
	private Button replaceBtn;
	private Button replaceAllBtn;

	private Label statusLabel;

	private AbstractTmxDataAccess tmxDataAccess;
	private TmxEditorImpWithNattable tmxEditorImpWithNattable;
	private AbstractFindReplace findImpl;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public FindReplaceDialog(Shell parentShell) {
		super(parentShell);
		setBlockOnOpen(false);
		setShellStyle(getShellStyle() ^ SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
	}

	public void setTmxDataAccess(AbstractTmxDataAccess tmxDataAccess) {
		this.tmxDataAccess = tmxDataAccess;
	}

	public void setTmxEditorImpWithNattable(TmxEditorImpWithNattable tmxEditorImpWithNattable) {
		this.tmxEditorImpWithNattable = tmxEditorImpWithNattable;
	}

	public void setFindText(String text) {
		if (text == null || text.length() == 0) {
			return;
		}
		if (this.findTextCombo != null && !this.findTextCombo.isDisposed()) {
			this.findTextCombo.setText(text);
		}
	}

	public void clearResource() {
		this.findImpl = null;
		this.tmxDataAccess = null;
		this.tmxEditorImpWithNattable = null;
	}

	@Override
	public boolean close() {
		clearResource();
		if (getShell() != null && !getShell().isDisposed()) {
			writeDialogSettings();
		}
		return super.close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.FindReplaceDialog.Title"));
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, 0);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(composite);
		initializeDialogUnits(composite);
		dialogArea = createDialogArea(composite);
		loadDialogSettings();
		return composite;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Composite inputComp = new Composite(container, SWT.NONE);
		inputComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout glInputComp = new GridLayout(2, false);
		glInputComp.marginHeight = 0;
		glInputComp.marginWidth = 0;
		inputComp.setLayout(glInputComp);

		Label findLabel = new Label(inputComp, SWT.NONE);
		findLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		findLabel.setText(Messages.getString("dialog.FindReplaceDialog.findLabel"));

		findTextCombo = new Combo(inputComp, SWT.NONE);
		findTextCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label replaceWithLabel = new Label(inputComp, SWT.NONE);
		replaceWithLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		replaceWithLabel.setText(Messages.getString("dialog.FindReplaceDialog.replaceWithLabel"));

		replaceTextCombo = new Combo(inputComp, SWT.NONE);
		replaceTextCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite settingComp = new Composite(container, SWT.NONE);
		settingComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		GridLayout glSettingComp = new GridLayout(2, false);
		glSettingComp.marginWidth = 0;
		glSettingComp.marginHeight = 0;
		settingComp.setLayout(glSettingComp);

		Group directionGroup = new Group(settingComp, SWT.NONE);
		directionGroup.setLayout(new GridLayout(1, false));
		directionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		directionGroup.setText(Messages.getString("dialog.FindReplaceDialog.directionGroup"));

		forwardRadioBtn = new Button(directionGroup, SWT.RADIO);
		forwardRadioBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		forwardRadioBtn.setText(Messages.getString("dialog.FindReplaceDialog.forwardButton"));

		backRadioBtn = new Button(directionGroup, SWT.RADIO);
		backRadioBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		backRadioBtn.setText(Messages.getString("dialog.FindReplaceDialog.backwardButton"));

		Group rangeGroup = new Group(settingComp, SWT.NONE);
		rangeGroup.setLayout(new GridLayout(1, false));
		rangeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		rangeGroup.setText(Messages.getString("dialog.FindReplaceDialog.rangeGroup"));

		sourceRadioBtn = new Button(rangeGroup, SWT.RADIO);
		sourceRadioBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sourceRadioBtn.setText(Messages.getString("dialog.FindReplaceDialog.sourceButton"));

		targetRadioBtn = new Button(rangeGroup, SWT.RADIO);
		targetRadioBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		targetRadioBtn.setText(Messages.getString("dialog.FindReplaceDialog.targetButton"));

		Group optionGroup = new Group(container, SWT.NONE);
		optionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		optionGroup.setLayout(new GridLayout(2, false));
		optionGroup.setText(Messages.getString("dialog.FindReplaceDialog.optionsGroup"));

		ignoreCaseCheckBtn = new Button(optionGroup, SWT.CHECK);
		ignoreCaseCheckBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		ignoreCaseCheckBtn.setText(Messages.getString("dialog.FindReplaceDialog.caseSensitiveButton"));

		wholeWordCheckBtn = new Button(optionGroup, SWT.CHECK);
		wholeWordCheckBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		wholeWordCheckBtn.setText(Messages.getString("dialog.FindReplaceDialog.wholeWordButton"));

		regExCheckBtn = new Button(optionGroup, SWT.CHECK);
		regExCheckBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		regExCheckBtn.setText(Messages.getString("dialog.FindReplaceDialog.regExButton"));

		Composite btnComp = new Composite(container, SWT.NONE);
		GridLayout glBtnComp = new GridLayout(2, false);
		glBtnComp.marginHeight = 0;
		glBtnComp.marginWidth = 0;
		btnComp.setLayout(glBtnComp);
		btnComp.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));

		findBtn = new Button(btnComp, SWT.NONE);
		GridData gdFindBtn = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gdFindBtn.widthHint = 105;
		findBtn.setLayoutData(gdFindBtn);
		findBtn.setText(Messages.getString("dialog.FindReplaceDialog.findButton"));
		findBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				find();
			}
		});

		replaceFindBtn = new Button(btnComp, SWT.NONE);
		GridData gdFindNextBtn = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gdFindNextBtn.widthHint = 105;
		replaceFindBtn.setLayoutData(gdFindNextBtn);
		replaceFindBtn.setText(Messages.getString("dialog.FindReplaceDialog.findNextButton"));
		replaceFindBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				replaceFind();
			}
		});

		replaceBtn = new Button(btnComp, SWT.NONE);
		GridData gdReplaceBtn = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gdReplaceBtn.widthHint = 105;
		replaceBtn.setLayoutData(gdReplaceBtn);
		replaceBtn.setText(Messages.getString("dialog.FindReplaceDialog.replaceButton"));
		replaceBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				replace();
			};
		});
		replaceAllBtn = new Button(btnComp, SWT.NONE);
		GridData gdReplaceAllBtn = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gdReplaceAllBtn.widthHint = 105;
		replaceAllBtn.setLayoutData(gdReplaceAllBtn);
		replaceAllBtn.setText(Messages.getString("dialog.FindReplaceDialog.replaceAllButton"));
		replaceAllBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				replaceAll();
			}
		});
		Composite closeComposite = new Composite(container, SWT.NONE);
		GridLayout glCloseCmp = new GridLayout(2, false);
		glCloseCmp.marginWidth = 0;
		glCloseCmp.marginHeight = 0;
		closeComposite.setLayout(glCloseCmp);
		closeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		statusLabel = new Label(closeComposite, SWT.NONE);
		statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button closeBtn = new Button(closeComposite, SWT.NONE);
		GridData gdClosebtn = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gdClosebtn.widthHint = 95;
		closeBtn.setLayoutData(gdClosebtn);
		closeBtn.setText(IDialogConstants.CLOSE_LABEL);
		closeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				close();
			}
		});
		return container;
	}

	private boolean find() {
		if (tmxEditorImpWithNattable == null || tmxEditorImpWithNattable.isDispose()) {
			return false;
		}
		final String findStr = findTextCombo.getText();
		if (findStr == null || findStr.length() == 0) {
			return false;
		}
		updateCombHistory(findTextCombo);
		final boolean[] result = new boolean[] { false };
		BusyIndicator.showWhile(super.getShell().getDisplay(), new Runnable() {

			public void run() {
				boolean isForward = forwardRadioBtn.getSelection();
				boolean isFindTarget = targetRadioBtn.getSelection();
				int startRow = 0;
				String lang = tmxDataAccess.getCurrSrcLang();
				int findCol = tmxEditorImpWithNattable.getSrcColumnIndex();
				int findOffset = 0;

				int[] rows = tmxEditorImpWithNattable.getSelectedRows();
				if (rows.length != 0) {
					startRow = rows[0];
				}
				if (isFindTarget) {
					lang = tmxDataAccess.getCurrTgtLang();
					findCol = tmxEditorImpWithNattable.getTgtColumnIndex();
				}

				int activeCellCol = TeActiveCellEditor.getColumnIndex();
				CellEditor cellEditor = (CellEditor) TeActiveCellEditor.getCellEditor();
				if (activeCellCol == findCol && cellEditor != null) {
					findOffset = cellEditor.getTextViewer().getTextWidget().getCaretOffset();
					findOffset = isForward ? findOffset : findOffset - 1;// back find
					findOffset = findOffset < 0 ? 0 : findOffset;
				}
				FindReasult r = doFind(startRow, findOffset, lang, findStr, isForward,
						ignoreCaseCheckBtn.getSelection(), wholeWordCheckBtn.getSelection(),
						regExCheckBtn.getSelection());
				if (r == null) {
					statusLabel.setText(Messages.getString("dialog.FindReplaceDialog.status1"));
					result[0] = false;
					return;
				}
				int rowIndex = tmxDataAccess.getDisplayTuIdentifiers().indexOf(r.getTuIdentifier());
				processFindResult(r, rowIndex, findCol, isForward);
				result[0] = true;
			}
		});
		return result[0];
	}

	private void replace() {
		if (tmxEditorImpWithNattable == null || tmxEditorImpWithNattable.isDispose()) {
			return;
		}
		String findStr = findTextCombo.getText();
		if (findStr == null || findStr.length() == 0) {
			return;
		}
		String replaceStr = replaceTextCombo.getText();
		if (replaceStr == null || replaceStr.length() == 0) {
			return;
		}
		CellEditor cellEditor = (CellEditor) TeActiveCellEditor.getCellEditor();
		if (cellEditor != null) {
			StyledText text = cellEditor.getTextViewer().getTextWidget();
			String sleText = cellEditor.getTextViewer().getSelectionText();
			if (sleText != null && sleText.toLowerCase().equals(findStr.toLowerCase()) && !sleText.equals(replaceStr)) {
				Matcher matcher = PlaceHolderEditModeBuilder.PATTERN.matcher(sleText);
				StringBuilder sb = new StringBuilder();
				while (matcher.find()) {
					sb.append(matcher.group());
				}
				sb.insert(0, replaceStr);
				Point p = text.getSelection();
				text.replaceTextRange(p.x, p.y - p.x, sb.toString());
				text.setSelection(p.x, p.x + replaceStr.length());
				TeActiveCellEditor.commitWithoutClose();
				updateCombHistory(replaceTextCombo);
			}
		}
	}

	private void replaceFind() {
		if (tmxEditorImpWithNattable == null || tmxEditorImpWithNattable.isDispose()) {
			return;
		}
		final String findStr = findTextCombo.getText();
		if (findStr == null || findStr.length() == 0) {
			return;
		}
		CellEditor cellEditor = (CellEditor) TeActiveCellEditor.getCellEditor();
		if (cellEditor != null) {
			String sleText = cellEditor.getTextViewer().getSelectionText();
			if (sleText.isEmpty() || !sleText.toLowerCase().equals(findStr.toLowerCase())) {
				find();
				return;
			} else {
				replace();
				find();
			}
		}
	}

	private void replaceAll() {
		if (tmxEditorImpWithNattable == null || tmxEditorImpWithNattable.isDispose()) {
			return;
		}
		final String findStr = findTextCombo.getText();
		if (findStr == null || findStr.length() == 0) {
			return;
		}
		final String replaceStr = replaceTextCombo.getText();
		if (replaceStr == null || replaceStr.length() == 0) {
			return;
		}
		if (replaceStr.equals(findStr)) {
			return;
		}
		BusyIndicator.showWhile(super.getShell().getDisplay(), new Runnable() {

			public void run() {
				boolean isForward = true;
				int startRow = 0;
				int findOffset = 0;
				String lang = tmxDataAccess.getCurrSrcLang();
				int findCol = tmxEditorImpWithNattable.getSrcColumnIndex();

				boolean isFindTarget = targetRadioBtn.getSelection();
				if (isFindTarget) {
					lang = tmxDataAccess.getCurrTgtLang();
					findCol = tmxEditorImpWithNattable.getTgtColumnIndex();
				}

				boolean isIgnoreCase = ignoreCaseCheckBtn.getSelection();
				boolean isWholeWord = wholeWordCheckBtn.getSelection();
				boolean isRegEx = regExCheckBtn.getSelection();

				FindReasult r = doFind(startRow, findOffset, lang, findStr, true, isIgnoreCase, isWholeWord, isRegEx);
				if (r == null) {
					statusLabel.setText(Messages.getString("dialog.FindReplaceDialog.status1"));
					return;
				}
				int rowIndex = tmxDataAccess.getDisplayTuIdentifiers().indexOf(r.getTuIdentifier());
				processFindResult(r, rowIndex, findCol, isForward);
				replace();

				int replaceCount = 1;
				do {
					startRow = rowIndex;
					findOffset = r.getRegin().getOffset() + r.getRegin().getLength();
					r = doFind(startRow, findOffset, lang, findStr, true, isIgnoreCase, isWholeWord, isRegEx);
					if (r == null) {
						break;
					}
					rowIndex = tmxDataAccess.getDisplayTuIdentifiers().indexOf(r.getTuIdentifier());
					processFindResult(r, rowIndex, findCol, isForward);
					replace();
					replaceCount++;
				} while (r != null);
				String msg = Messages.getString("dialog.FindReplaceDialog.status3");
				statusLabel.setText(MessageFormat.format(msg, replaceCount));
			}
		});
	}

	private FindReasult doFind(int startRow, int offset, String lang, String findStr, boolean isForward,
			boolean isCaseSensitive, boolean isWholeWord, boolean isRegx) {
		if (findImpl == null) {
			if (tmxDataAccess instanceof TmxLargeFileDataAccess) {
				findImpl = new TmxFileFindReplaceImpl((TmxLargeFileDataAccess) tmxDataAccess);
			} else {
				findImpl = new TmDBFindReplaceImpl((DatabaseDataAccess) tmxDataAccess);
			}
		}
		findImpl.setSearchStategy(isForward, isCaseSensitive, isWholeWord, isRegx);
		FindReasult r = findImpl.find(startRow, offset, lang, findStr);

		return r;
	}

	private void processFindResult(FindReasult reasult, int rowIndex, int findCol, boolean isForward) {
		if (tmxEditorImpWithNattable == null || tmxEditorImpWithNattable.isDispose()) {
			return;
		}
		statusLabel.setText("");

		TeActiveCellEditor.commit();
		tmxEditorImpWithNattable.selectCell(findCol, rowIndex);
		tmxEditorImpWithNattable.editSelectedCell();

		CellEditor cellEditor = (CellEditor) TeActiveCellEditor.getCellEditor();
		if (cellEditor != null) {
			StyledText text = cellEditor.getTextViewer().getTextWidget();
			IRegion region = reasult.getRegin();
			if (isForward) {
				text.setSelection(region.getOffset(), region.getOffset() + region.getLength());
			} else {
				text.setSelection(region.getOffset() + region.getLength(), region.getOffset());
			}
		}
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(353, 350);
	}

	private void updateCombHistory(Combo combo) {
		if (combo == null || combo.isDisposed()) {
			return;
		}
		String value = combo.getText();
		if (value == null || value.length() == 0) {
			return;
		}

		int index = combo.indexOf(value);
		if (index == 0) {
			return;
		}
		if (index != -1) {
			combo.remove(index);
		}

		int itemCount = combo.getItemCount();
		if (itemCount == 0) {
			combo.add(value, 0);
			combo.setText(value);
			return;
		}
		combo.add(value, 0);
		combo.setText(value);
		if (itemCount > 10) {
			combo.remove(11);
		}
	}

	private void writeDialogSettings() {
		IDialogSettings ids = getDialogSettings();
		ids.put("nattable.FindReplaceDialog.direction", backRadioBtn.getSelection());
		ids.put("nattable.FindReplaceDialog.range", targetRadioBtn.getSelection());
		ids.put("nattable.FindReplaceDialog.caseSensitive", ignoreCaseCheckBtn.getSelection());
		ids.put("nattable.FindReplaceDialog.wholeWord", wholeWordCheckBtn.getSelection());
		ids.put("nattable.FindReplaceDialog.regEx", regExCheckBtn.getSelection());
		if (findTextCombo != null && !findTextCombo.isDisposed()) {
			String[] names = findTextCombo.getItems();
			ids.put("nattable.FindReplaceDialog.findHistory", names);
		}
		if (replaceTextCombo != null && !replaceTextCombo.isDisposed()) {
			String[] names = replaceTextCombo.getItems();
			ids.put("nattable.FindReplaceDialog.replaceHistory", names);
		}
	}

	private void loadDialogSettings() {
		IDialogSettings ids = getDialogSettings();
		boolean blnDirection = ids.getBoolean("nattable.FindReplaceDialog.direction");
		forwardRadioBtn.setSelection(!blnDirection);
		backRadioBtn.setSelection(blnDirection);
		boolean blnRange = ids.getBoolean("nattable.FindReplaceDialog.range");
		sourceRadioBtn.setSelection(!blnRange);
		targetRadioBtn.setSelection(blnRange);

		ignoreCaseCheckBtn.setSelection(ids.getBoolean("nattable.FindReplaceDialog.caseSensitive"));
		wholeWordCheckBtn.setSelection(ids.getBoolean("nattable.FindReplaceDialog.wholeWord"));
		regExCheckBtn.setSelection(ids.getBoolean("nattable.FindReplaceDialog.regEx"));
		if (wholeWordCheckBtn.getSelection() && regExCheckBtn.getSelection()) {
			regExCheckBtn.setSelection(false);
		}

		String[] arrFindHistory = ids.getArray("nattable.FindReplaceDialog.findHistory");
		if (arrFindHistory != null) {
			findTextCombo.setItems(arrFindHistory);
			findTextCombo.select(0);
		}

		String[] arrReplaceHistory = ids.getArray("nattable.FindReplaceDialog.replaceHistory");
		if (arrReplaceHistory != null) {
			replaceTextCombo.setItems(arrReplaceHistory);
			replaceTextCombo.select(0);
		}
	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings fDialogSettings = settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings = settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}
}