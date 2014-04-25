/**
 * CellEditorViewer.java
 *
 * Version information :
 *
 * Date:2013-6-6
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor;

import static net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder.PATTERN;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagType;
import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.common.ui.innertag.InnerTag;
import net.heartsome.cat.te.tmxeditor.TmxEditorConstanst;
import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * 在 TmxEditor 中用于编辑 TUV 文本内容的编辑器，它能够将转换的标记以控件的方式进行显示，转换参考<code>TmxInnerTagParser</code><br>
 * 内部已经实现 undo/redo 功能，需要绑定事件来触发事件。在事件响应中通过{@link CellEditorTextViewer#doOperation(int)}执行<br>
 * 内部已经实现 copy/parse 功能，需要绑定事件睐触发事件，在事件响应中通过{@link CellEditorTextViewer#doOperation(int)}执行<br>
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class CellEditorTextViewer extends TextViewer {

	private TmxSegement tuv;
	ArrayList<InnerTag> innerTagCacheList = new ArrayList<InnerTag>();

	public CellEditorTextViewer(Composite parent, int styles) {
		super(parent, styles);
		this.setDocument(new Document()); // 为TextViewer设置一个Document
		initListener();
		// actionHander.addTextViewer(this);
		// 配置标记分析器，根据标记占位符，让标记控件能正确的显示到指定的位置。
		TagStyleConfigurator.configure(this);
	}

	/**
	 * 当前内容为 TMX TUV
	 * @param tuv
	 *            ;
	 */
	public void setTmxSegement(TmxSegement tuv) {
		if (tuv == null) {
			return;
		}
		this.tuv = tuv;
		List<InnerTagBean> innerTagBeans = tuv.getInnerTags();
		for (InnerTagBean innerTagBean : innerTagBeans) {
			InnerTag innerTag = TmxInnerTagUtils.createInnerTagControl(getTextWidget(), innerTagBean, null);
			innerTag.setVisible(false);
			innerTagCacheList.add(innerTag);
		}
		String text = tuv.getTextTagPlaceHolder();
		text = resetRegularString(text);
		setText(text);
	}

	/**
	 * 设置当前内容，内容不做任何处理，直接显示到编辑器中
	 * @param text
	 *            显示的字符串;
	 */
	public void setText(String text) {
		getTextWidget().setText(text);
		// 初始化撤销/重做管理器，设置步长为 50。
		initUndoManager(20);
		getTextWidget().setCaretOffset(text.length());
	}

	/**
	 * 获取文本内容,不包含标识内容 ;
	 * @return
	 */
	public String getPureText() {
		if (getTextWidget() == null) {
			return "";
		}
		String text = getTextWidget().getText();
		if (TmxEditorImpWithNattable.showNonPrinttingChar) {
			text = text.replaceAll(System.getProperty("line.separator"), "\n");
			text = text.replaceAll(TmxEditorConstanst.LINE_SEPARATOR_CHARACTER + "", "");
			text = text.replaceAll(TmxEditorConstanst.TAB_CHARACTER + "\u200B", "\t");
			text = text.replaceAll(TmxEditorConstanst.SPACE_CHARACTER + "\u200B", " ");
		}
		return PATTERN.matcher(text).replaceAll("");
	}

	public String getSelectionText(){
		String text = getTextWidget().getSelectionText();
		if (TmxEditorImpWithNattable.showNonPrinttingChar) {
			text = text.replaceAll(System.getProperty("line.separator"), "\n");
			text = text.replaceAll(TmxEditorConstanst.LINE_SEPARATOR_CHARACTER + "", "");
			text = text.replaceAll(TmxEditorConstanst.TAB_CHARACTER + "\u200B", "\t");
			text = text.replaceAll(TmxEditorConstanst.SPACE_CHARACTER + "\u200B", " ");
		}
		return text;
	}
	
	/**
	 * 获取文本内容，包含标记
	 * @return ;
	 */
	public String getFullText() {
		if (getTextWidget() == null) {
			return "";
		}
		String text = getTextWidget().getText();
		if (text == null) {
			return "";
		}
		if (tuv == null) {
			return cleanRegularString(text);
		}
		return CellEditorViewerUtils.convertDisplayTextToOriginalText(cleanRegularString(text), tuv.getInnerTags());
	}

	/**
	 * Dispose this viewer SWT control ;
	 */
	public void dispose() {
		Control control = getControl();
		if (control != null && !control.isDisposed()) {
			control.dispose();
		}
	}

	private void initListener() {
		StyledText styledText = getTextWidget();

		// 去掉默认的复制、粘贴键绑定，以实现在复制、粘贴前对标记的处理
		styledText.setKeyBinding('V' | SWT.MOD1, SWT.NULL);
		styledText.setKeyBinding(SWT.INSERT | SWT.MOD2, SWT.NULL);
		styledText.setKeyBinding('C' | SWT.MOD1, SWT.NULL);
		styledText.setKeyBinding(SWT.INSERT | SWT.MOD1, SWT.NULL);

		styledText.addPaintObjectListener(new PaintObjectListener() {
			public void paintObject(PaintObjectEvent event) {
				StyleRange styleRange = event.style;
				if (styleRange != null) {
					String text = ((StyledText) event.widget).getText();
					int end = styleRange.start + styleRange.length;
					if (text.length() < end) {
						return;
					}
					String styledString = text.substring(styleRange.start, end);
					Matcher matcher = PATTERN.matcher(styledString);
					if (matcher.matches()) {
						InnerTag tag = TmxInnerTagUtils.getInnerTagControl(innerTagCacheList, styledString);
						if (tag != null) {
							if (!tag.isVisible()) {
								tag.setVisible(true);
							}
							int lineHeight = getTextWidget().getLineHeight();
							int y = event.y + lineHeight / 2 - tag.getBounds().height / 2;
							tag.setLocation(event.x + TmxEditorConstanst.SEGMENT_LINE_SPACING, y);
						}
					}
				}
			}
		});

		styledText.addVerifyListener(new VerifyListener() {

			public void verifyText(final VerifyEvent e) {
				if ((e.start == e.end)|| (e.start != e.end && !e.text.equals(""))) { // 添加内容时，忽略
					if (TmxEditorImpWithNattable.showNonPrinttingChar) {
						String t = e.text;
						t = t.replace("\t", TmxEditorConstanst.TAB_CHARACTER + "\u200B").replace(" ",
								TmxEditorConstanst.SPACE_CHARACTER + "\u200B");
						t = t.replace(System.getProperty("line.separator"), "\n");
						StringBuffer bf = new StringBuffer(t);
						int i = bf.indexOf("\n");
						if (i != -1) {
							if (i == 0) {
								bf.insert(i, TmxEditorConstanst.LINE_SEPARATOR_CHARACTER);
							} else if (i != 0 && bf.charAt(i - 1) != TmxEditorConstanst.LINE_SEPARATOR_CHARACTER) {
								bf.insert(i, TmxEditorConstanst.LINE_SEPARATOR_CHARACTER);
							}
							i = bf.indexOf("\n", i + 1);
						}
						e.text = bf.toString();
					}
					return;
				}
				final StyledText styledText = (StyledText) e.widget;
				final String text = styledText.getText(e.start, e.end - 1);
				final Matcher matcher = PATTERN.matcher(text);
				if (matcher.find()) { // 被删除的部分中存在标记的的情况，进行特殊处理。
					matcher.reset();
					styledText.getDisplay().syncExec(new Runnable() {
						public void run() {
							deleteInnerTagInPairs(e, matcher);
						}
					});
				}
				if (TmxEditorImpWithNattable.showNonPrinttingChar) {
					if (text.length() == 1 && (text.equals("\n") || text.indexOf('\u200B') != -1)) {
						char c = styledText.getText().charAt(e.start - 1);
						if (c == TmxEditorConstanst.LINE_SEPARATOR_CHARACTER || c == TmxEditorConstanst.SPACE_CHARACTER
								|| c == TmxEditorConstanst.TAB_CHARACTER) {
							styledText.replaceTextRange(e.start - 1, 2, "");
							e.doit = false;
						}
					} else if (text.length() == 1
							&& (text.indexOf(TmxEditorConstanst.LINE_SEPARATOR_CHARACTER) != -1
									|| text.indexOf(TmxEditorConstanst.TAB_CHARACTER) != -1 || text
									.indexOf(TmxEditorConstanst.TAB_CHARACTER) != -1)) {
						char c = styledText.getText().charAt(e.start + 1);
						if (c == '\n' || c == '\u200B') {
							styledText.replaceTextRange(e.start, 2, "");
							e.doit = false;
						}
					}
				}
			}

			/**
			 * 成对删除内部标记
			 */
			private void deleteInnerTagInPairs(final VerifyEvent e, Matcher matcher) {
				StyledText styledText = (StyledText) e.widget;
				ArrayList<Integer> tagIndexes = new ArrayList<Integer>(); // 记录被删除的标记的索引。
				while (matcher.find()) {
					String placeHolder = matcher.group();
					InnerTag innerTag = TmxInnerTagUtils.getInnerTagControl(innerTagCacheList, placeHolder);
					if (innerTag != null && innerTag.isVisible()) {
						innerTag.setVisible(false);

						// 保存成对标记中未完全删除的标记索引
						TagType tagType = innerTag.getInnerTagBean().getType();
						if (tagType == TagType.START || tagType == TagType.END) { // 处理成对标记的成对删除
							Integer tagIndex = Integer.valueOf(innerTag.getInnerTagBean().getIndex()); // 标记索引
							if (tagIndexes.contains(tagIndex)) { // 如果已经包含此索引，说明成对标记的2个部分都已经删除。
								tagIndexes.remove(tagIndex);
							} else { // 如果未包含此索引，则说明只删除了一个部分（开始或结束）的标记。
								tagIndexes.add(tagIndex);
							}
						}
					}
				}

				if (!tagIndexes.isEmpty()) { // 存在未删除的情况。
					getUndoManager().beginCompoundChange();

					e.doit = false; // 上一步已经修改，取消修改操作。
					styledText.getContent().replaceTextRange(e.start, e.end - e.start, e.text); // 替换改动内容

					for (int i = 0; i < innerTagCacheList.size(); i++) { // 删除成对标记中未被删除的部分。
						InnerTag innerTag = innerTagCacheList.get(i);
						if (innerTag != null && innerTag.isVisible()) {
							if (tagIndexes.contains(innerTag.getInnerTagBean().getIndex())) {
								innerTag.setVisible(false);
								String placeHolder = TmxInnerTagParser.getInstance().getPlaceHolderBuilder()
										.getPlaceHolder(null, i);
								int start = -1;
								if ((start = styledText.getText().indexOf(placeHolder)) != -1) {
									styledText.getContent().replaceTextRange(start, placeHolder.length(), "");
								}

								tagIndexes.remove(Integer.valueOf(innerTag.getInnerTagBean().getIndex()));
								if (tagIndexes.isEmpty()) {
									break;
								}
							}
						}
					}
					getUndoManager().endCompoundChange();

					/**
					 * 通知更新主菜单（actionBar）中“撤销重做”等菜单项的状态，参见
					 * net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorActionHandler
					 */
					styledText.notifyListeners(SWT.Selection, null);
				}
			}
		});
		styledText.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				for (InnerTag tag : innerTagCacheList) {
					if (tag.isSelected()) {
						tag.setSelected(false);
						tag.redraw();
					}
				}
				String styledString = getTextWidget().getSelectionText();
				Matcher matcher = PATTERN.matcher(styledString);
				while (matcher.find()) {
					String s = matcher.group();
					InnerTag tag = TmxInnerTagUtils.getInnerTagControl(innerTagCacheList, s);
					if (tag != null) {
						tag.setSelected(true);
						tag.redraw();
					}
				}
			}
		});

		/**
		 * 处理在显示非打印隐藏字符的情况光标移动问题。兼容非打印字符替换符号
		 */
		styledText.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
				if (!TmxEditorImpWithNattable.showNonPrinttingChar) {
					return;
				}
				if (e.stateMask == SWT.NONE && (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN)) {
					StyledText styledText = (StyledText) e.widget;
					int offset = styledText.getCaretOffset();
					if (offset < 1 || offset >= styledText.getCharCount()) {
						return;
					}
					char c = styledText.getText().charAt(offset);
					char _c = styledText.getText().charAt(offset - 1);
					if (c == '\n' && (_c == TmxEditorConstanst.LINE_SEPARATOR_CHARACTER)) {
						styledText.setCaretOffset(offset - 1);
					}
				}
			}

			public void keyPressed(KeyEvent e) {
				if (!TmxEditorImpWithNattable.showNonPrinttingChar) {
					return;
				}
				if (e.stateMask == SWT.NONE && (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT)) {
					StyledText styledText = (StyledText) e.widget;
					int offset = styledText.getCaretOffset();
					if (offset < 1 || offset >= styledText.getCharCount()) {
						return;
					}
					char c = styledText.getText().charAt(offset);
					char _c = styledText.getText().charAt(offset - 1);
					if ((c == '\u200B' && (_c == TmxEditorConstanst.TAB_CHARACTER || _c == TmxEditorConstanst.SPACE_CHARACTER))
							|| (c == '\n' && (_c == TmxEditorConstanst.LINE_SEPARATOR_CHARACTER))) {
						if (e.keyCode == SWT.ARROW_LEFT) {
							styledText.setCaretOffset(offset - 1);
						} else if (e.keyCode == SWT.ARROW_RIGHT) {
							styledText.setCaretOffset(offset + 1);
						}
					}
				} else if (e.stateMask == SWT.CTRL && (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT)) {
					// 单独对 ctrl + right ,ctrl + left 换行的处理
					StyledText styledText = (StyledText) e.widget;
					int offset = styledText.getCaretOffset();
					char c = styledText.getText().charAt(offset);
					if (offset < 1 || offset >= styledText.getCharCount()) {
						return;
					}
					char _c = styledText.getText().charAt(offset - 1);
					if (c == '\n' && (_c == TmxEditorConstanst.LINE_SEPARATOR_CHARACTER)) {
						if (e.keyCode == SWT.ARROW_LEFT) {
							styledText.setCaretOffset(offset - 1);
						} else if (e.keyCode == SWT.ARROW_RIGHT) {
							styledText.setCaretOffset(offset + 1);
						}
					}
				} else if ((e.stateMask == SWT.SHIFT || e.stateMask == (SWT.SHIFT | SWT.CTRL))
						&& (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT)) {
					StyledText styledText = (StyledText) e.widget;
					int offset = styledText.getCaretOffset();
					char c = styledText.getText().charAt(offset);
					if (offset < 1 || offset >= styledText.getCharCount()) {
						return;
					}
					char _c = styledText.getText().charAt(offset - 1);
					if ((c == '\u200B' && (_c == TmxEditorConstanst.TAB_CHARACTER || _c == TmxEditorConstanst.SPACE_CHARACTER))
							|| (c == '\n' && (_c == TmxEditorConstanst.LINE_SEPARATOR_CHARACTER))) {
						if (e.keyCode == SWT.ARROW_LEFT) {
							styledText.invokeAction(ST.SELECT_COLUMN_PREVIOUS);
						} else if (e.keyCode == SWT.ARROW_RIGHT) {
							styledText.invokeAction(ST.SELECT_COLUMN_NEXT);
						}
					}
				} else if ((e.stateMask == SWT.SHIFT) && (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN)) {
					StyledText styledText = (StyledText) e.widget;
					int offset = styledText.getCaretOffset();
					char c = styledText.getText().charAt(offset);
					if (offset < 1 || offset >= styledText.getCharCount()) {
						return;
					}
					char _c = styledText.getText().charAt(offset - 1);
					if (c == '\n' && (_c == TmxEditorConstanst.LINE_SEPARATOR_CHARACTER)) {
						if (e.keyCode == SWT.ARROW_UP) {
							styledText.invokeAction(ST.SELECT_COLUMN_PREVIOUS);
						} else if (e.keyCode == SWT.ARROW_DOWN) {
							styledText.invokeAction(ST.SELECT_COLUMN_NEXT);
						}
					}
				}
			}
		});

		// 处理修改内容时，需要非打印字符添加样式。
		styledText.addListener(SWT.Modify, new Listener() {

			public void handleEvent(Event event) {
				if (!TmxEditorImpWithNattable.showNonPrinttingChar) {
					return;
				}
				String s = event.text;
				Matcher matcher = TmxEditorConstanst.NONPRINTING_PATTERN.matcher(s);
				TextStyle style = new TextStyle(null, GUIHelper.getColor(new RGB(100, 100, 100)), null);
				List<StyleRange> ranges = new ArrayList<StyleRange>();
				while (matcher.find()) {
					int start = event.start + matcher.start();
					StyleRange range = new StyleRange(style);
					range.start = start;
					range.length = 1;
					ranges.add(range);
				}
				for (StyleRange range : ranges) {
					getTextWidget().setStyleRange(range);
				}
			}
		});

		/**
		 * 处理在显示非打印隐藏字符的情况光标移动问题。兼容非打印字符替换符号
		 */
		styledText.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				if (!TmxEditorImpWithNattable.showNonPrinttingChar) {
					return;
				}
				StyledText styledText = (StyledText) e.widget;
				int offset = styledText.getCaretOffset();
				if (offset < 1 || offset >= styledText.getCharCount()) {
					return;
				}
				char c = styledText.getText().charAt(offset); // hidden character
				char _c = styledText.getText().charAt(offset - 1); // display character
				if ((_c == TmxEditorConstanst.LINE_SEPARATOR_CHARACTER || _c == TmxEditorConstanst.TAB_CHARACTER || _c == TmxEditorConstanst.SPACE_CHARACTER)
						&& (c == '\n' || c == '\u200B')) {
					styledText.setCaretOffset(offset + 1);
				}
			}

			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		/**
		 * 选择内容时对非打印字符的处理
		 */
		styledText.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				if (!TmxEditorImpWithNattable.showNonPrinttingChar) {
					return;
				}
				StyledText styledText = (StyledText) e.widget;
				int offset = styledText.getCaretOffset();
				if (offset < 1 || offset >= styledText.getCharCount()) {
					return;
				}
				char c = styledText.getText().charAt(offset);
				char _c = styledText.getText().charAt(offset - 1);
				if ((c == '\u200B' && (_c == TmxEditorConstanst.TAB_CHARACTER || _c == TmxEditorConstanst.SPACE_CHARACTER))
						|| (c == '\n' && (_c == TmxEditorConstanst.LINE_SEPARATOR_CHARACTER))) {
					int caretOffset = styledText.getCaretOffset();
					Point p = styledText.getSelection();
					if (caretOffset == p.x) {
						styledText.invokeAction(ST.SELECT_COLUMN_PREVIOUS);
					} else if (caretOffset == p.y) {
						styledText.invokeAction(ST.SELECT_COLUMN_NEXT);
					}
				}
			}
		});
	}

	/** 将所有转义字符全部转换成原始状态，只适用于文本内容的显示 */
	private String cleanRegularString(String input) {
		input = input.replaceAll("&", "&amp;");
		input = input.replaceAll("<", "&lt;");
		input = input.replaceAll(">", "&gt;");
		// input = input.replaceAll("\"", "&quot;");
		input = input.replaceAll(System.getProperty("line.separator"), "\n");
		if (TmxEditorImpWithNattable.showNonPrinttingChar) {
			input = input.replaceAll(TmxEditorConstanst.LINE_SEPARATOR_CHARACTER + "", "");
			input = input.replaceAll(TmxEditorConstanst.TAB_CHARACTER + "\u200B", "\t");
			input = input.replaceAll(TmxEditorConstanst.SPACE_CHARACTER + "\u200B", " ");
		}
		return input;
	}

	/** 将所有转义字符全部转换成原始状态，只适用于文本内容的显示 */
	private String resetRegularString(String input) {
		input = input.replaceAll("&lt;", "<");
		input = input.replaceAll("&gt;", ">");
		input = input.replaceAll("&quot;", "\"");
		input = input.replaceAll("&amp;", "&");
		input = input.replaceAll(System.getProperty("line.separator"), "\n");
		if (TmxEditorImpWithNattable.showNonPrinttingChar) {
			input = input.replaceAll("\\n", TmxEditorConstanst.LINE_SEPARATOR_CHARACTER + "\n");
			input = input.replaceAll("\\t", TmxEditorConstanst.TAB_CHARACTER + "\u200B");
			input = input.replaceAll(" ", TmxEditorConstanst.SPACE_CHARACTER + "\u200B");
		}
		return input;
	}

	private void initUndoManager(int undoLevel) {
		// remembers edit commands
		final TextViewerUndoManager undoManager = new TextViewerUndoManager(undoLevel);
		// add listeners
		undoManager.connect(this);
		this.setUndoManager(undoManager);
	}

	/**
	 * 重载此方法，实现粘贴、复制前对标记的处理。<br>
	 * 已经移出了 默认的复制、粘贴事件键绑定, 参考{@link CellEditorTextViewer#initListener()}
	 * @see org.eclipse.jface.text.TextViewer#doOperation(int)
	 */
	@Override
	public void doOperation(int operation) {
		if (operation == ITextOperationTarget.PASTE) {
			parse();
			return;
		}
		if (operation == ITextOperationTarget.COPY) {
			copy();
			return;
		}
		super.doOperation(operation);
	}

	/**
	 * 执行复制时对标记的处理，复制后在OS系统中不能包含标记占位符 ;
	 */
	private void copy() {
		super.doOperation(ITextOperationTarget.COPY);
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		HSTextTransfer hsTextTransfer = HSTextTransfer.getInstance();
		Clipboard clipboard = new Clipboard(getTextWidget().getDisplay());
		String plainText = (String) clipboard.getContents(plainTextTransfer);
		if (plainText == null || plainText.length() == 0) {
			return;
		}
		plainText = plainText.replaceAll(System.getProperty("line.separator"), "\n");
		plainText = plainText.replaceAll(TmxEditorConstanst.LINE_SEPARATOR_CHARACTER + "", "");
		plainText = plainText.replaceAll(TmxEditorConstanst.TAB_CHARACTER + "", "\t");
		plainText = plainText.replaceAll(TmxEditorConstanst.SPACE_CHARACTER + "", " ");
		plainText = plainText.replaceAll("\u200B", "");
		clipboard.clearContents();
		Object[] data = new Object[] { PATTERN.matcher(plainText).replaceAll(""), plainText };
		Transfer[] types = new Transfer[] { plainTextTransfer, hsTextTransfer };

		clipboard.setContents(data, types);
		clipboard.dispose();
	}

	/**
	 * 执行粘贴前对标记的处理 ;
	 */
	private void parse() {
		Clipboard clipboard = null;
		try {
			if (getTextWidget().isDisposed()) {
				return;
			}
			clipboard = new Clipboard(getTextWidget().getDisplay());
			HSTextTransfer hsTextTransfer = HSTextTransfer.getInstance();
			String hsText = (String) clipboard.getContents(hsTextTransfer);
			String osText = (String) clipboard.getContents(TextTransfer.getInstance());
			if (hsText == null || hsText.length() == 0) {
				if (osText == null || osText.length() == 0) {
					return;
				}
				super.doOperation(ITextOperationTarget.PASTE);
				return;
			}
			String clearedTagText = hsText;
			String selText = getTextWidget().getSelectionText();
			if (selText.equals(hsText)) {
				return;
			}
			if (getTextWidget().getSelectionCount() != getTextWidget().getText().length()) {
				clearedTagText = CellEditorViewerUtils.filterInnerTag(this, hsText); // 过滤掉系统剪切板中的标记。
			} else {
				StringBuffer bf = new StringBuffer(hsText);
				Matcher matcher = PATTERN.matcher(hsText);
				List<String> needRemove = new ArrayList<String>();
				while (matcher.find()) {
					String placeHolder = matcher.group();
					InnerTag tag = TmxInnerTagUtils.getInnerTagControl(innerTagCacheList, placeHolder);
					if (tag == null) {
						needRemove.add(placeHolder);
					}
				}
				clearedTagText = bf.toString();
				for (String r : needRemove) {
					clearedTagText = clearedTagText.replaceAll(r, "");
				}
			}

			if (clearedTagText == null || clearedTagText.length() == 0) {
				return;
			}

			if (clearedTagText.equals(osText)) {
				super.doOperation(ITextOperationTarget.PASTE);
				return;
			}

			Object[] data = new Object[] { clearedTagText, hsText };
			Transfer[] types = new Transfer[] { TextTransfer.getInstance(), hsTextTransfer };
			try {
				clipboard.setContents(data, types);
			} catch (Exception e) {
				e.printStackTrace();
			}

			super.doOperation(ITextOperationTarget.PASTE);

			data = new Object[] { osText, hsText };
			try {
				clipboard.setContents(data, types);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			if (clipboard != null && !clipboard.isDisposed()) {
				clipboard.dispose();
			}
		}
	}
}
