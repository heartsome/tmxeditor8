/**
 * InnerTagParser.java
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
package net.heartsome.cat.common.innertag;

import static net.heartsome.cat.common.innertag.TagType.END;
import static net.heartsome.cat.common.innertag.TagType.STANDALONE;
import static net.heartsome.cat.common.innertag.TagType.START;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import net.heartsome.cat.common.innertag.factory.IPlaceHolderBuilder;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxInnerTagParser {

	private final List<String> standaloneTags = Arrays.asList("x", "bx", "ex", "ph", "ut", "hi");
	private final List<String> normalTags = Arrays.asList("g", "bpt", "ept", "ph", "it", "mrk", "sub", "ut", "hi");
	private IPlaceHolderBuilder placeHolderCreater = new PlaceHolderEditModeBuilder();

	private static TmxInnerTagParser instance;

	public static TmxInnerTagParser getInstance() {
		if (instance == null) {
			instance = new TmxInnerTagParser();
		}
		return instance;
	}

	private TmxInnerTagParser() {

	}

	public IPlaceHolderBuilder getPlaceHolderBuilder() {
		return placeHolderCreater;
	}

	Stack<Integer> indexStack = new Stack<Integer>(); // 索引缓存，用于处理成对标记
	List<InnerTagBean> innertTags = new ArrayList<InnerTagBean>();;
	int maxIndex = 0;
	boolean hasStartTag = false;
	int start = -1;

	private void resetParser() {
		if (innertTags != null) {
			innertTags.clear();
		}
		indexStack.clear(); // 索引集合
		start = -1;
		maxIndex = 0;
		hasStartTag = false;
	}

	public List<InnerTagBean> parseInnerTag(StringBuilder xmlSb) {
		if (xmlSb == null || xmlSb.length() == 0) {
			return new ArrayList<InnerTagBean>();
		}
		while ((start = xmlSb.indexOf("<", start + 1)) > -1) {
			int end = xmlSb.indexOf(">", start + 1);
			if (end > -1) {
				String xmlTag = xmlSb.substring(start, end + 1); // 提取出的内部标记xml形式的文本
				String tagName = getTagName(xmlTag);
				if (xmlTag.indexOf("/>", 1) > -1) { // 独立标签
					if (standaloneTags.contains(tagName) || normalTags.contains(tagName)) {
						// if ("bx".equals(tagName)) {
						// addInnerTagBean(START, xmlSb, xmlTag, tagName);
						// } else if ("ex".equals(tagName)) {
						// addInnerTagBean(END, xmlSb, xmlTag, tagName);
						// } else {
						addInnerTagBean(STANDALONE, xmlSb, xmlTag, tagName);
						// }
					}
				} else if (xmlTag.indexOf("</") > -1) { // 结束标签
					if (normalTags.contains(tagName)) {
						addInnerTagBean(END, xmlSb, xmlTag, tagName);
					}
				} else if (xmlTag.indexOf(">") > -1) { // 开始标签
					if (normalTags.contains(tagName)) {
						if ("bpt".equals(tagName)) {
							int endIndex = xmlSb.indexOf("</bpt>", start) + "</bpt>".length();
							xmlTag = xmlSb.substring(start, endIndex);
							xmlSb.replace(start, endIndex, xmlTag);
							addInnerTagBean(START, xmlSb, xmlTag, tagName);
						} else if ("ept".equals(tagName)) {
							int endIndex = xmlSb.indexOf("</ept>", start) + "</ept>".length();
							xmlTag = xmlSb.substring(start, endIndex);
							xmlSb.replace(start, endIndex, xmlTag);
							addInnerTagBean(END, xmlSb, xmlTag, tagName);
						} else if ("ph".equals(tagName) || "it".equals(tagName)|| "ut".equals(tagName)) {
							String tempTagName = "</" + tagName + ">";
							int endIndex = xmlSb.indexOf(tempTagName, start) + tempTagName.length();
							xmlTag = xmlSb.substring(start, endIndex);
							xmlSb.replace(start, endIndex, xmlTag);
							addInnerTagBean(STANDALONE, xmlSb, xmlTag, tagName);
						} else {
							addInnerTagBean(START, xmlSb, xmlTag, tagName);
						}
					}
				}
			}
		}
		ArrayList<InnerTagBean> result = new ArrayList<InnerTagBean>();
		result.addAll(innertTags);
		resetParser();
		return result;
	}

	private void addInnerTagBean(TagType tagType, StringBuilder text, String tagContent, String tagName) {
		int index = -1;
		if (tagType == START) {
			hasStartTag = true;
			maxIndex++;
			indexStack.push(maxIndex);
			index = maxIndex;
		} else if (tagType == END) {
			if (!hasStartTag) {
				maxIndex++;
				indexStack.push(maxIndex);
			}
			hasStartTag = false;
			if (!indexStack.empty()) {
				index = indexStack.pop();
			}
		} else if (tagType == STANDALONE) {
			maxIndex++;
			index = maxIndex;
		}

		if (index > -1) {
			InnerTagBean bean = new InnerTagBean(index, tagName, tagContent, tagType);
			innertTags.add(bean);

			String placeHolder = placeHolderCreater.getPlaceHolder(innertTags, innertTags.size() - 1);
			text.replace(start, start + tagContent.length(), placeHolder);
			// 显示完整标记时，start 计算错误，因此添加下行语句
			start += placeHolder.length() - 1;
		}
	}

	/**
	 * 得到标记的名称
	 * @param xmlTag
	 *            XML格式的标记
	 * @return 标记名称;
	 */
	private static String getTagName(String xmlTag) {
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

	/**
	 * 获取　tmx 的纯文本 robert 2013-07-08
	 * @param content
	 * @return
	 */
	public String getTmxPureText(String fullText) {
		if (fullText == null || fullText.isEmpty()) {
			return fullText;
		}
		StringBuilder b = new StringBuilder(fullText);
		parseInnerTag(b);
		return PlaceHolderEditModeBuilder.PATTERN.matcher(b.toString()).replaceAll("");
	}

	public static void main(String[] args) {
		String text = "<seg><bpt i='1'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='1'>&lt;/mrk&gt;</ept><bpt i='2'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='2'>&lt;/mrk&gt;</ept><bpt i='3'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='3'>&lt;/mrk&gt;</ept>                    Quickly and easily make backup copies of all my files<bpt i='4'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='4'>&lt;/mrk&gt;</ept><bpt i='5'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='5'>&lt;/mrk&gt;</ept>                <bpt i='6'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='6'>&lt;/mrk&gt;</ept><bpt i='7'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='7'>&lt;/mrk&gt;</ept><bpt i='8'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='8'>&lt;/mrk&gt;</ept>                <bpt i='9'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='9'>&lt;/mrk&gt;</ept><bpt i='10'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='10'>&lt;/mrk&gt;</ept><bpt i='11'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='11'>&lt;/mrk&gt;</ept>                    Automatically backs up all your files whenever your PC is idle or at pre-determined<bpt i='12'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='12'>&lt;/mrk&gt;</ept><bpt i='13'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='13'>&lt;/mrk&gt;</ept>                    intervals<bpt i='14'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='14'>&lt;/mrk&gt;</ept><bpt i='15'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='15'>&lt;/mrk&gt;</ept>                <bpt i='16'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='16'>&lt;/mrk&gt;</ept><bpt i='17'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='17'>&lt;/mrk&gt;</ept><bpt i='18'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='18'>&lt;/mrk&gt;</ept>            <bpt i='19'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='19'>&lt;/mrk&gt;</ept><bpt i='20'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='20'>&lt;/mrk&gt;</ept><bpt i='21'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='21'>&lt;/mrk&gt;</ept>            <bpt i='22'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='22'>&lt;/mrk&gt;</ept><bpt i='23'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='23'>&lt;/mrk&gt;</ept><bpt i='24'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='24'>&lt;/mrk&gt;</ept>                <bpt i='25'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='25'>&lt;/mrk&gt;</ept><bpt i='26'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='26'>&lt;/mrk&gt;</ept><bpt i='27'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='27'>&lt;/mrk&gt;</ept>                    Have backup copies of all my files<bpt i='28'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='28'>&lt;/mrk&gt;</ept><bpt i='29'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='29'>&lt;/mrk&gt;</ept>                <bpt i='30'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='30'>&lt;/mrk&gt;</ept><bpt i='31'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='31'>&lt;/mrk&gt;</ept><bpt i='32'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='32'>&lt;/mrk&gt;</ept>                <bpt i='33'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='33'>&lt;/mrk&gt;</ept><bpt i='34'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='34'>&lt;/mrk&gt;</ept><bpt i='35'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='35'>&lt;/mrk&gt;</ept>                    With unlimited storage capacity, you don&apos;t have to decide which file is important<bpt i='36'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='36'>&lt;/mrk&gt;</ept><bpt i='37'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='37'>&lt;/mrk&gt;</ept>                    enough to backup and which isn&apos;t<bpt i='38'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='38'>&lt;/mrk&gt;</ept><bpt i='39'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='39'>&lt;/mrk&gt;</ept>                <bpt i='40'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='40'>&lt;/mrk&gt;</ept><bpt i='41'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='41'>&lt;/mrk&gt;</ept><bpt i='42'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='42'>&lt;/mrk&gt;</ept>            <bpt i='43'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='43'>&lt;/mrk&gt;</ept><bpt i='44'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='44'>&lt;/mrk&gt;</ept><bpt i='45'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='45'>&lt;/mrk&gt;</ept>            <bpt i='46'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='46'>&lt;/mrk&gt;</ept><bpt i='47'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='47'>&lt;/mrk&gt;</ept><bpt i='48'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='48'>&lt;/mrk&gt;</ept>                <bpt i='49'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='49'>&lt;/mrk&gt;</ept><bpt i='50'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='50'>&lt;/mrk&gt;</ept><bpt i='51'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='51'>&lt;/mrk&gt;</ept>                    Have all my backup copies stored in a safe place in the event of PC crash, loss,<bpt i='52'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='52'>&lt;/mrk&gt;</ept><bpt i='53'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='53'>&lt;/mrk&gt;</ept>                    theft, or a natural disaster<bpt i='54'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='54'>&lt;/mrk&gt;</ept><bpt i='55'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='55'>&lt;/mrk&gt;</ept>                <bpt i='56'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='56'>&lt;/mrk&gt;</ept><bpt i='57'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='57'>&lt;/mrk&gt;</ept><bpt i='58'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='58'>&lt;/mrk&gt;</ept>                <bpt i='59'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='59'>&lt;/mrk&gt;</ept><bpt i='60'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='60'>&lt;/mrk&gt;</ept><bpt i='61'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='61'>&lt;/mrk&gt;</ept>                    Backs up all your files to a secure offsite server that is accessible online<bpt i='62'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='62'>&lt;/mrk&gt;</ept><bpt i='63'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='63'>&lt;/mrk&gt;</ept>                <bpt i='64'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='64'>&lt;/mrk&gt;</ept><bpt i='65'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='65'>&lt;/mrk&gt;</ept><bpt i='66'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='66'>&lt;/mrk&gt;</ept>            <bpt i='67'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='67'>&lt;/mrk&gt;</ept><bpt i='68'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='68'>&lt;/mrk&gt;</ept><bpt i='69'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='69'>&lt;/mrk&gt;</ept>            <bpt i='70'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='70'>&lt;/mrk&gt;</ept><bpt i='71'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='71'>&lt;/mrk&gt;</ept><bpt i='72'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='72'>&lt;/mrk&gt;</ept>                <bpt i='73'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='73'>&lt;/mrk&gt;</ept><bpt i='74'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='74'>&lt;/mrk&gt;</ept><bpt i='75'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='75'>&lt;/mrk&gt;</ept>                    Secure my files from hackers and identity thieves<bpt i='76'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='76'>&lt;/mrk&gt;</ept><bpt i='77'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='77'>&lt;/mrk&gt;</ept>                <bpt i='78'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='78'>&lt;/mrk&gt;</ept><bpt i='79'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='79'>&lt;/mrk&gt;</ept><bpt i='80'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='80'>&lt;/mrk&gt;</ept>                <bpt i='81'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='81'>&lt;/mrk&gt;</ept><bpt i='82'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='82'>&lt;/mrk&gt;</ept><bpt i='83'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='83'>&lt;/mrk&gt;</ept>                    Uses 128-bit SSL encryption during the backup process of your files and 448-bit<bpt i='84'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='84'>&lt;/mrk&gt;</ept><bpt i='85'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='85'>&lt;/mrk&gt;</ept>                    Blowfish encryption while they are in storage, keeping your private data safe from<bpt i='86'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='86'>&lt;/mrk&gt;</ept><bpt i='87'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='87'>&lt;/mrk&gt;</ept>                    cybercriminals<bpt i='88'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='88'>&lt;/mrk&gt;</ept><bpt i='89'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='89'>&lt;/mrk&gt;</ept>                <bpt i='90'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='90'>&lt;/mrk&gt;</ept><bpt i='91'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='91'>&lt;/mrk&gt;</ept><bpt i='92'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='92'>&lt;/mrk&gt;</ept>            <bpt i='93'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='93'>&lt;/mrk&gt;</ept><bpt i='94'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='94'>&lt;/mrk&gt;</ept><bpt i='95'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='95'>&lt;/mrk&gt;</ept>            <bpt i='96'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='96'>&lt;/mrk&gt;</ept><bpt i='97'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='97'>&lt;/mrk&gt;</ept><bpt i='98'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='98'>&lt;/mrk&gt;</ept>                <bpt i='99'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='99'>&lt;/mrk&gt;</ept><bpt i='100'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='100'>&lt;/mrk&gt;</ept><bpt i='101'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='101'>&lt;/mrk&gt;</ept>                    Conveniently access the digital files via the Internet<bpt i='102'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='102'>&lt;/mrk&gt;</ept><bpt i='103'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='103'>&lt;/mrk&gt;</ept>                <bpt i='104'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='104'>&lt;/mrk&gt;</ept><bpt i='105'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='105'>&lt;/mrk&gt;</ept><bpt i='106'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='106'>&lt;/mrk&gt;</ept>                <bpt i='107'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='107'>&lt;/mrk&gt;</ept><bpt i='108'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='108'>&lt;/mrk&gt;</ept><bpt i='109'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='109'>&lt;/mrk&gt;</ept>                    With web access from anywhere, you can restore your files from any PC that has high-speed<bpt i='110'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='110'>&lt;/mrk&gt;</ept><bpt i='111'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='111'>&lt;/mrk&gt;</ept>                    Internet service<bpt i='112'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='112'>&lt;/mrk&gt;</ept><bpt i='113'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='113'>&lt;/mrk&gt;</ept>                <bpt i='114'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='114'>&lt;/mrk&gt;</ept><bpt i='115'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='115'>&lt;/mrk&gt;</ept><bpt i='116'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='116'>&lt;/mrk&gt;</ept>            <bpt i='117'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='117'>&lt;/mrk&gt;</ept><bpt i='118'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='118'>&lt;/mrk&gt;</ept><bpt i='119'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='119'>&lt;/mrk&gt;</ept>            <bpt i='120'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='120'>&lt;/mrk&gt;</ept><bpt i='121'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='121'>&lt;/mrk&gt;</ept><bpt i='122'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='122'>&lt;/mrk&gt;</ept>                <bpt i='123'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='123'>&lt;/mrk&gt;</ept><bpt i='124'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='124'>&lt;/mrk&gt;</ept><bpt i='125'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='125'>&lt;/mrk&gt;</ept>                    Have changes to original files automatically found and updated in the backed up<bpt i='126'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='126'>&lt;/mrk&gt;</ept><bpt i='127'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='127'>&lt;/mrk&gt;</ept>                    set<bpt i='128'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='128'>&lt;/mrk&gt;</ept><bpt i='129'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='129'>&lt;/mrk&gt;</ept>                <bpt i='130'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='130'>&lt;/mrk&gt;</ept><bpt i='131'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='131'>&lt;/mrk&gt;</ept><bpt i='132'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='132'>&lt;/mrk&gt;</ept>                <bpt i='133'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='133'>&lt;/mrk&gt;</ept><bpt i='134'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='134'>&lt;/mrk&gt;</ept><bpt i='135'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='135'>&lt;/mrk&gt;</ept>                    Detects and saves every change and updates archived copy, giving you an always updated<bpt i='136'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='136'>&lt;/mrk&gt;</ept><bpt i='137'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='137'>&lt;/mrk&gt;</ept>                    backup<bpt i='138'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='138'>&lt;/mrk&gt;</ept><bpt i='139'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='139'>&lt;/mrk&gt;</ept>                <bpt i='140'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='140'>&lt;/mrk&gt;</ept><bpt i='141'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='141'>&lt;/mrk&gt;</ept><bpt i='142'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='142'>&lt;/mrk&gt;</ept>            <bpt i='143'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='143'>&lt;/mrk&gt;</ept><bpt i='144'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='144'>&lt;/mrk&gt;</ept><bpt i='145'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='145'>&lt;/mrk&gt;</ept>            <bpt i='146'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='146'>&lt;/mrk&gt;</ept><bpt i='147'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='147'>&lt;/mrk&gt;</ept><bpt i='148'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='148'>&lt;/mrk&gt;</ept>                <bpt i='149'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='149'>&lt;/mrk&gt;</ept><bpt i='150'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='150'>&lt;/mrk&gt;</ept><bpt i='151'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='151'>&lt;/mrk&gt;</ept>                    Back up my Outlook files<bpt i='152'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='152'>&lt;/mrk&gt;</ept><bpt i='153'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='153'>&lt;/mrk&gt;</ept>                <bpt i='154'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='154'>&lt;/mrk&gt;</ept><bpt i='155'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='155'>&lt;/mrk&gt;</ept><bpt i='156'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='156'>&lt;/mrk&gt;</ept>                <bpt i='157'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='157'>&lt;/mrk&gt;</ept><bpt i='158'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='158'>&lt;/mrk&gt;</ept><bpt i='159'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='159'>&lt;/mrk&gt;</ept>                    Saves copies of all your Outlook email and contacts automatically<bpt i='160'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='160'>&lt;/mrk&gt;</ept><bpt i='161'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='161'>&lt;/mrk&gt;</ept>                <bpt i='162'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='162'>&lt;/mrk&gt;</ept><bpt i='163'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='163'>&lt;/mrk&gt;</ept><bpt i='164'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='164'>&lt;/mrk&gt;</ept>            <bpt i='165'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='165'>&lt;/mrk&gt;</ept><bpt i='166'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='166'>&lt;/mrk&gt;</ept><bpt i='167'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='167'>&lt;/mrk&gt;</ept>            <bpt i='168'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='168'>&lt;/mrk&gt;</ept><bpt i='169'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='169'>&lt;/mrk&gt;</ept><bpt i='170'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='170'>&lt;/mrk&gt;</ept>                <bpt i='171'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='171'>&lt;/mrk&gt;</ept><bpt i='172'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='172'>&lt;/mrk&gt;</ept><bpt i='173'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='173'>&lt;/mrk&gt;</ept>                    Have back up copies made whether the files are open or locked<bpt i='174'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='174'>&lt;/mrk&gt;</ept><bpt i='175'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='175'>&lt;/mrk&gt;</ept>                <bpt i='176'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='176'>&lt;/mrk&gt;</ept><bpt i='177'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='177'>&lt;/mrk&gt;</ept><bpt i='178'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='178'>&lt;/mrk&gt;</ept>                <bpt i='179'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='179'>&lt;/mrk&gt;</ept><bpt i='180'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='180'>&lt;/mrk&gt;</ept><bpt i='181'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='181'>&lt;/mrk&gt;</ept>                    Makes backup copies of your files whether they&apos;re open and being worked on or locked<bpt i='182'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='182'>&lt;/mrk&gt;</ept><bpt i='183'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='183'>&lt;/mrk&gt;</ept>                <bpt i='184'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='184'>&lt;/mrk&gt;</ept><bpt i='185'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='185'>&lt;/mrk&gt;</ept><bpt i='186'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='186'>&lt;/mrk&gt;</ept>            <bpt i='187'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='187'>&lt;/mrk&gt;</ept><bpt i='188'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='188'>&lt;/mrk&gt;</ept><bpt i='189'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='189'>&lt;/mrk&gt;</ept>        <bpt i='190'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='190'>&lt;/mrk&gt;</ept><bpt i='191'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='191'>&lt;/mrk&gt;</ept><bpt i='192'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='192'>&lt;/mrk&gt;</ept>    <bpt i='193'>&lt;mrk mtype=&quot;protected&quot;&gt;</bpt><ept i='193'>&lt;/mrk&gt;</ept>]]<ph>&amp;gt;&lt;mrk mtype=&quot;protected&quot;&gt;&amp;lt;/Description&amp;gt;&lt;/mrk&gt;</ph></seg>";
		TmxInnerTagParser parser = new TmxInnerTagParser();
		System.out.println(parser.getTmxPureText(text));

	}
}
