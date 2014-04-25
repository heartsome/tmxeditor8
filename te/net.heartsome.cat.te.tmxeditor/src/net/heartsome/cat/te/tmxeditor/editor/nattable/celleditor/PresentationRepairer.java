package net.heartsome.cat.te.tmxeditor.editor.nattable.celleditor;

import net.heartsome.cat.common.ui.innertag.InnerTag;
import net.heartsome.cat.te.tmxeditor.TmxEditorConstanst;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;

public class PresentationRepairer implements IPresentationRepairer {

	/** The document this object works on */
	protected IDocument fDocument;
	/** The scanner it uses */
	protected ITokenScanner fScanner;
	/** The default text style if non is returned as data by the current token */
	protected TextStyle fDefaultTextStyle;

	protected CellEditorTextViewer viewer;

	public PresentationRepairer(ITokenScanner scanner, CellEditorTextViewer viewer) {
		Assert.isNotNull(scanner);

		this.viewer = viewer;
		fScanner = scanner;
		fDefaultTextStyle = new TextStyle();
	}

	public void setDocument(IDocument document) {
		this.fDocument = document;
	}

	public void createPresentation(TextPresentation presentation, ITypedRegion region) {
		if (fScanner == null) {
			// will be removed if deprecated constructor will be removed
			addRange(presentation, region.getOffset(), region.getLength(), fDefaultTextStyle);
			return;
		}

		int lastStart = region.getOffset();
		int length = 0;
		boolean firstToken = true;
		IToken lastToken = Token.UNDEFINED;
		TextStyle lastTextStyle = getTokenTextStyle(lastToken);

		fScanner.setRange(fDocument, lastStart, region.getLength());

		while (true) {
			IToken token = fScanner.nextToken();
			if (token.isEOF())
				break;

			TextStyle textStyle = getTokenTextStyle(token);
			if (lastTextStyle != null && lastTextStyle.equals(textStyle)) {
				length += fScanner.getTokenLength();
				firstToken = false;
			} else {
				if (!firstToken)
					addRange(presentation, lastStart, length, lastTextStyle);
				firstToken = false;
				lastToken = token;
				lastTextStyle = textStyle;
				lastStart = fScanner.getTokenOffset();
				length = fScanner.getTokenLength();
			}
		}

		addRange(presentation, lastStart, length, lastTextStyle);
	}

	/**
	 * Returns a text style encoded in the given token. If the token's data is not <code>null</code> and a text style it
	 * is assumed that it is the encoded text style. It returns the default text style if there is no encoded text style
	 * found.
	 * @param token
	 *            the token whose text style is to be determined
	 * @return the token's text style
	 */
	protected TextStyle getTokenTextStyle(IToken token) {
		Object data = token.getData();
		if (data instanceof TextStyle)
			return (TextStyle) data;
		return fDefaultTextStyle;
	}

	/**
	 * Adds style information to the given text presentation.
	 * @param presentation
	 *            the text presentation to be extended
	 * @param offset
	 *            the offset of the range to be styled
	 * @param length
	 *            the length of the range to be styled
	 * @param textStyle
	 *            the style of the range to be styled
	 */
	protected void addRange(TextPresentation presentation, int offset, int length, TextStyle textStyle) {
		if (textStyle != null) {

			if (textStyle.metrics != null && length >= 1) {
				for (int i = offset; i < offset + length; i++) {
					try {
						StyleRange styleRange = new StyleRange(textStyle);
						String placeHolder = fDocument.get(i, 1);
						InnerTag innerTag = TmxInnerTagUtils.getInnerTagControl(viewer.innerTagCacheList, placeHolder);
						if (innerTag != null) {
							Point rect = innerTag.computeSize(SWT.DEFAULT, SWT.DEFAULT);
							// int ascent = 4 * rect.height / 5 + SEGMENT_LINE_SPACING / 2;
							// int descent = rect.height - ascent + SEGMENT_LINE_SPACING;
							styleRange.metrics = new GlyphMetrics(rect.y, 0, rect.x + TmxEditorConstanst.SEGMENT_LINE_SPACING * 2);
						}
						styleRange.start = i;
						styleRange.length = 1;
						presentation.addStyleRange(styleRange);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			} else {
				StyleRange styleRange = new StyleRange(textStyle);
				styleRange.start = offset;
				styleRange.length = length;
				presentation.addStyleRange(styleRange);
			}
		}
	}
}
