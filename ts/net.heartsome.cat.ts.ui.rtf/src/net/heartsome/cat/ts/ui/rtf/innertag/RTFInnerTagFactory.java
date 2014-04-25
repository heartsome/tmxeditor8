package net.heartsome.cat.ts.ui.rtf.innertag;

import static net.heartsome.cat.common.innertag.TagType.END;
import static net.heartsome.cat.common.innertag.TagType.STANDALONE;
import static net.heartsome.cat.common.innertag.TagType.START;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagType;
import net.heartsome.cat.common.innertag.factory.DefaultPlaceHolderBuilder;
import net.heartsome.cat.common.innertag.factory.IInnerTagFactory;
import net.heartsome.cat.common.innertag.factory.IPlaceHolderBuilder;

/**
 * 标记工厂类实现，该类与 XliffInnerTagFactory 相比，添加了获取标记在源文本中的位置，内容等方式
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class RTFInnerTagFactory implements IInnerTagFactory {

	private ArrayList<InnerTagBean> beans = new ArrayList<InnerTagBean>();

	private String text;

	private IPlaceHolderBuilder placeHolderCreater;

	/**
	 * 内部标记工厂默认实现
	 * @param xml
	 *            XML文本
	 */
	public RTFInnerTagFactory(String xml) {
		this(xml, new DefaultPlaceHolderBuilder());
	}

	/**
	 * 内部标记工厂默认实现
	 * @param placeHolderCreater
	 *            占位符创建器
	 */
	public RTFInnerTagFactory(IPlaceHolderBuilder placeHolderCreater) {
		this(null, placeHolderCreater);
	}

	/**
	 * 内部标记工厂默认实现
	 * @param xml
	 *            XML文本
	 * @param placeHolderCreater
	 *            占位符创建器
	 */
	public RTFInnerTagFactory(String xml, IPlaceHolderBuilder placeHolderCreater) {
		this.placeHolderCreater = placeHolderCreater;
		this.text = parseInnerTag(xml);
	}

	public String getText() {
		return text;
	}

	public List<InnerTagBean> getInnerTagBeans() {
		ArrayList<InnerTagBean> innerTagBeans = new ArrayList<InnerTagBean>();
		innerTagBeans.addAll(beans);
		return innerTagBeans;
	}

	private static final List<String> standaloneTags = Arrays.asList("x", "bx", "ex", "ph");

	private static final List<String> normalTags = Arrays.asList("g", "bpt", "ept", "ph", "it", "mrk", "sub");

	private Stack<Integer> indexStack = new Stack<Integer>(); // 索引集合

	private int start = -1;

	private int maxIndex = 0;

	private boolean HasStartTag = false;
	
	/** 源文本被标记分割之后的标记集合 */
	private ArrayList<SegmentText> lstSegment = new ArrayList<SegmentText>();

	/**
	 * 将带内部标记的文本由XML格式转换为显示格式的文本
	 * @param xml
	 *            原始的带内部标记的XML格式的文本
	 * @return ;
	 */
	public String parseInnerTag(String xml) {
		if (xml == null || xml.length() == 0) {
			return "";
		}
		if (!indexStack.empty()) {
			indexStack.clear();
		}

		StringBuffer sbOriginalValue = new StringBuffer(xml);

		int beanSize;
		if (beans.size() > 0) {
			for (int i = 0; i < beans.size(); i++) {
				String content = beans.get(i).getContent();
				int index = sbOriginalValue.indexOf(content);
				if (index > -1) {
					SegmentText stText = new SegmentText(index, index + content.length(), content);
					lstSegment.add(stText);
					
					String placeHolder = placeHolderCreater.getPlaceHolder(beans, i);
					sbOriginalValue.replace(index, index + content.length(), placeHolder);
				}
			}
			beanSize = beans.size();
		} else {
			beanSize = -1;
		}

		this.start = -1; // 起始索引

		while ((start = sbOriginalValue.indexOf("<", start + 1)) > -1) {
			
			int end = sbOriginalValue.indexOf(">", start + 1);
			if (end > -1) {
				String xmlTag = sbOriginalValue.substring(start, end + 1); // 提取出的内部标记xml形式的文本
				String tagName = getTagName(xmlTag);
				if (xmlTag.indexOf("/>", 1) > -1) { // 独立标签
					if (standaloneTags.contains(tagName) || normalTags.contains(tagName)) {
						if ("bx".equals(tagName)) {
							addInnerTagBean(START, sbOriginalValue, xmlTag, tagName);
						} else if ("ex".equals(tagName)) {
							addInnerTagBean(END, sbOriginalValue, xmlTag, tagName);
						} else {
							addInnerTagBean(STANDALONE, sbOriginalValue, xmlTag, tagName);
						}
					}
				} else if (xmlTag.indexOf("</") > -1) { // 结束标签
					if (normalTags.contains(tagName)) {
						addInnerTagBean(END, sbOriginalValue, xmlTag, tagName);
					}
				} else if (xmlTag.indexOf(">") > -1) { // 开始标签
					if (normalTags.contains(tagName)) {
						if ("bpt".equals(tagName)) {
							xmlTag = sbOriginalValue.substring(start, sbOriginalValue.indexOf("</bpt>")
									+ "</bpt>".length());
							addInnerTagBean(START, sbOriginalValue, xmlTag, tagName);
						} else if ("ept".equals(tagName)) {
							xmlTag = sbOriginalValue.substring(start, sbOriginalValue.indexOf("</ept>")
									+ "</ept>".length());
							addInnerTagBean(END, sbOriginalValue, xmlTag, tagName);
						} else if ("ph".equals(tagName) || "it".equals(tagName)) {
							String tempTagName = "</" + tagName + ">";
							xmlTag = sbOriginalValue.substring(start, sbOriginalValue.indexOf(tempTagName)
									+ tempTagName.length());
							addInnerTagBean(STANDALONE, sbOriginalValue, xmlTag, tagName);
						} else {
							addInnerTagBean(START, sbOriginalValue, xmlTag, tagName);
						}
					}
				}
			}
		}

		if (beanSize > 0) { // 设置为错误标记
			for (int i = beanSize; i < beans.size(); i++) {
				beans.get(i).setWrongTag(true);
			}
		}

		return sbOriginalValue.toString();
	}

	/**
	 * @param tagType
	 * @param text
	 * @param tagContent
	 * @param tagName
	 *            ;
	 */
	private void addInnerTagBean(TagType tagType, StringBuffer text, String tagContent, String tagName) {
		/* 在文本中插入索引 */
		int index = -1;
		if (tagType == START) {
			HasStartTag = true;
			maxIndex++;
			indexStack.push(maxIndex);
			index = maxIndex;
		} else if (tagType == END) {
			if (!HasStartTag) {
				maxIndex++;
				indexStack.push(maxIndex);
			}
			HasStartTag = false;
			if (!indexStack.empty()) {
				index = indexStack.pop();
			}
		} else if (tagType == STANDALONE) {
			maxIndex++;
			index = maxIndex;
		}

		if (index > -1) {
			InnerTagBean bean = new InnerTagBean(index, tagName, tagContent, tagType);
			beans.add(bean);

			SegmentText stText = new SegmentText(start, start + tagContent.length(), tagContent);
			lstSegment.add(stText);
			
			String placeHolder = placeHolderCreater.getPlaceHolder(beans, beans.size() - 1);
			text.replace(start, start + tagContent.length(), placeHolder);
		}
	}

	/**
	 * 得到标记的名称
	 * @param xmlTag
	 *            XML格式的标记
	 * @return 标记名称;
	 */
	private String getTagName(String xmlTag) {
		if (xmlTag.indexOf("</") > -1) { // 结束标记
			return xmlTag.substring(2, xmlTag.length() - 1);
		}
		int end = xmlTag.indexOf("/>", 1); // 独立标记
		if (end == -1) {
			end = xmlTag.length() - 1; // 开始标记
		}
		int tempIndex = xmlTag.indexOf(" ", 1);
		if (tempIndex > -1 && tempIndex < end) {
			end = tempIndex;
		}
		return xmlTag.substring(1, end);
	}

	public ArrayList<SegmentText> getLstSegment() {
		return lstSegment;
	}
}
