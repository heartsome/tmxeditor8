/**
 * CellEditorViewerUtils.java
 *
 * Version information :
 *
 * Date:2013-6-7
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
import java.util.Stack;
import java.util.regex.Matcher;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagType;
import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.common.ui.innertag.InnerTag;

/**
 * {@link CellEditorTextViewer} 通用工具方法封装
 * @author Jason
 * @version
 * @since JDK1.6
 */
public final class CellEditorViewerUtils {

	/**
	 * 转换显示文本为原始文本。
	 * @param displayText
	 *            显示文本
	 * @return XML 中的原始文本;
	 */
	public static String convertDisplayTextToOriginalText(String displayText, List<InnerTagBean> tagBeans) {
		if (displayText == null || displayText.length() == 0) {
			return "";
		}
		Matcher matcher = PATTERN.matcher(displayText);
		int offset = 0;
		StringBuffer sb = new StringBuffer(displayText);
		while (matcher.find()) {
			String placeHolder = matcher.group();
			int index = TmxInnerTagParser.getInstance().getPlaceHolderBuilder().getIndex(null, placeHolder);
			if (index != -1) {
				InnerTagBean bean = tagBeans.get(index);
				if (bean != null) {
					String tagContent = bean.getContent();
					int start = matcher.start() + offset;
					int end = matcher.end() + offset;
					sb.replace(start, end, tagContent);
					offset += tagContent.length() - 1;
				}
			}
		}
		return sb.toString();
	};

	/**
	 * 此方用于粘贴时处理粘贴板中的标记 根据当前 {@link CellEditorTextViewer} 中的标记，过滤 contents 中的标记。
	 */
	public static String filterInnerTag(CellEditorTextViewer viewer, String contents) {
		if (contents == null) {
			return contents;
		}
		List<InnerTag> cacheTags = viewer.innerTagCacheList;
		String fullText = viewer.getTextWidget().getText();

		Matcher matcher = PATTERN.matcher(contents);
		Stack<InnerTag> stack = new Stack<InnerTag>();
		Stack<String> phStack = new Stack<String>();
		List<String> needRemove = new ArrayList<String>();
		while (matcher.find()) {
			String placeHolder = matcher.group();
			InnerTag tag = TmxInnerTagUtils.getInnerTagControl(cacheTags, placeHolder);
			if (tag == null) {
				needRemove.add(placeHolder);
				continue;
			}
			if (tag.getInnerTagBean().getType() == TagType.START) {
				stack.push(tag);
				phStack.push(placeHolder);
				continue;
			} else if (tag.getInnerTagBean().getType() == TagType.END) {
				if (stack.isEmpty()) {
					// 只有结束 没有开始
					needRemove.add(placeHolder);
					continue;
				}
				InnerTag _tag = stack.pop();
				String _placeHolder = phStack.pop();
				if (tag.getInnerTagBean().getIndex() != _tag.getInnerTagBean().getIndex()) {
					needRemove.add(placeHolder);
					needRemove.add(_placeHolder);
					continue;
				}
				int start = -1;
				if ((start = fullText.indexOf(_placeHolder)) != -1) {
					viewer.getTextWidget().replaceTextRange(start, _placeHolder.length(), "");
					fullText = viewer.getTextWidget().getText();
				}
				if ((start = fullText.indexOf(placeHolder)) != -1) {
					viewer.getTextWidget().replaceTextRange(start, placeHolder.length(), "");
					fullText = viewer.getTextWidget().getText();
				}
			} else {
				int start = -1;
				if ((start = fullText.indexOf(placeHolder)) != -1) {
					viewer.getTextWidget().replaceTextRange(start, placeHolder.length(), "");
					fullText = viewer.getTextWidget().getText();
				}
			}
		}

		while (!stack.isEmpty()) {
			needRemove.add(TmxInnerTagUtils.getPlaceHolder(viewer.innerTagCacheList, stack.pop().getInnerTagBean()));
		}
		for (String r : needRemove) {
			contents = contents.replaceAll(r, "");
		}
		return contents;
	}

	/**
	 * Private Constructor to prevent instanced
	 */
	private CellEditorViewerUtils() {
	}
}
