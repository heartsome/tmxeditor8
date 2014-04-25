package net.heartsome.cat.common.bean;

import static net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder.PATTERN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TmxInnerTagParser;

/**
 * TUV
 * 
 * 更新by yule : 2013-8-19
 * 如果TmxSegment 是由数据库中构建，其固定值是存在#attributes中的
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TmxSegement {

	private int dbPk;
	private String pureText;
	private String fullText;
	private String langCode;
	private String textTagPlaceHolder;
	private List<InnerTagBean> innerTags;

	private String creationUser;
	private String creationDate;
	private String changeDate;
	private String changeUser;
	private String creationTool;
	private String creationToolVersion;

	/** TUV XMLElement Attributes */
	private Map<String, String> attributes;

	public TmxSegement() {
	};

	public TmxSegement(String pureText, String fullText, String langCode) {
		this(fullText,langCode);
		this.pureText = pureText;
		this.fullText = fullText;
		this.langCode = langCode;
	}

	public TmxSegement(String fullText, String langCode) {
		StringBuilder b = new StringBuilder(fullText);
		innerTags = TmxInnerTagParser.getInstance().parseInnerTag(b);
		textTagPlaceHolder = b.toString();
		this.langCode = langCode;
	}

	/**
	 * Set the value of pureText
	 * @param newVar
	 *            the new value of pureText
	 */
	public void setPureText(String newVar) {
		pureText = newVar;
	}

	/**
	 * Get the value of pureText
	 * @return the value of pureText
	 */
	public String getPureText() {
		if (pureText == null) {
			if (textTagPlaceHolder != null) {
				return PATTERN.matcher(textTagPlaceHolder).replaceAll("");
			}
		}
		return pureText;
	}

	public void setFullTextWithParseTag(String fullText) {
		StringBuilder b = new StringBuilder(fullText);
		innerTags = TmxInnerTagParser.getInstance().parseInnerTag(b);
		textTagPlaceHolder = b.toString();
	}

	public String getTextTagPlaceHolder() {
		return this.textTagPlaceHolder;
	}

	public List<InnerTagBean> getInnerTags() {
		return this.innerTags;
	}

	/**
	 * Set the value of fullText
	 * @param newVar
	 *            the new value of fullText
	 */
	public void setFullText(String newVar) {
		fullText = newVar;
	}

	/**
	 * Get the value of fullText
	 * @return the value of fullText
	 */
	public String getFullText() {
		if (fullText == null) {
			if (textTagPlaceHolder != null) {
				Matcher matcher = PATTERN.matcher(textTagPlaceHolder);
				int offset = 0;
				StringBuilder sb = new StringBuilder(textTagPlaceHolder);
				while (matcher.find()) {
					String placeHolder = matcher.group();
					int index = TmxInnerTagParser.getInstance().getPlaceHolderBuilder().getIndex(null, placeHolder);
					if (index != -1) {
						InnerTagBean bean = innerTags.get(index);
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
			}
		}
		return fullText;
	}

	/**
	 * Set the value of langCode
	 * @param newVar
	 *            the new value of langCode
	 */
	public void setLangCode(String newVar) {
		langCode = newVar;
	}

	/**
	 * Get the value of langCode
	 * @return the value of langCode
	 */
	public String getLangCode() {
		return langCode;
	}

	/** @return the dbPk */
	public int getDbPk() {
		return dbPk;
	}

	/**
	 * @param dbPk
	 *            the dbPk to set
	 */
	public void setDbPk(int dbPk) {
		this.dbPk = dbPk;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	/** @return the attributes */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void appendAttribute(String name, String value) {
		if (this.attributes == null) {
			attributes = new HashMap<String, String>();
		}
		attributes.put(name, value);
	}

	/** @return the creationUser */
	public String getCreationUser() {
		if(attributes !=null &&creationUser ==null){
			return attributes.get("creationUser");
		}
		return creationUser;
	}

	/**
	 * @param creationUser
	 *            the creationUser to set
	 */
	public void setCreationUser(String creationUser) {
		this.creationUser = creationUser;
	}

	/** @return the creationDate */
	public String getCreationDate() {
		if(attributes !=null &&creationDate==null){
			return attributes.get("creationDate");
		}
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	/** @return the changeDate */
	public String getChangeDate() {
		if(attributes !=null &&changeDate==null){
			return attributes.get("changeDate");
		}
		return changeDate;
	}

	/**
	 * @param changeDate
	 *            the changeDate to set
	 */
	public void setChangeDate(String changeDate) {
		this.changeDate = changeDate;
	}

	/** @return the changeUser */
	public String getChangeUser() {
		if(attributes !=null &&changeUser==null){
			return attributes.get("changeUser");
		}
		return changeUser;
	}

	/**
	 * @param changeUser
	 *            the changeUser to set
	 */
	public void setChangeUser(String changeUser) {
		this.changeUser = changeUser;
	}

	/** @return the creationTool */
	public String getCreationTool() {
		if(attributes !=null &&creationTool==null){
			return attributes.get("creationTool");
		}
		return creationTool;
	}

	/**
	 * @param creationTool
	 *            the creationTool to set
	 */
	public void setCreationTool(String creationTool) {
		this.creationTool = creationTool;
	}

	/** @return the creationToolVersion */
	public String getCreationToolVersion() {
		if(attributes !=null &&creationToolVersion==null){
			return attributes.get("creationToolVersion");
		}
		return creationToolVersion;
	}

	/**
	 * @param creationToolVersion
	 *            the creationToolVersion to set
	 */
	public void setCreationToolVersion(String creationToolVersion) {
		this.creationToolVersion = creationToolVersion;
	}

}
