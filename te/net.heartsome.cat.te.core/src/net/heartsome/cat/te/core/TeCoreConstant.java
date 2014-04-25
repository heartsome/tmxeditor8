/**
 * TeCoreConstant.java
 *
 * Version information :
 *
 * Date:2013/5/17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */

package net.heartsome.cat.te.core;

/**
 * TE 代码区所用到的常量类
 */
public final class TeCoreConstant {
	public final static String QAResultViewer_ID = "net.heartsome.cat.te.tmxeditor.QAResultViewer";
	
	/** subFile 文件名　与　tu position 组装成唯一标识符用到的　分隔符 */
	public final static String ID_MARK = "\uFFF1";

	
	/** 系统过虑器id- 所有文本段 */
	public final static String FILTERID_allSeg = "te.system.filter.allSegment";
	/** 系统过虑器id- 源文和译文相同的文本段 */
	public final static String FILTERID_srcSameWIthTgtSeg = "te.system.filter.srcSameWithTgtSeg";
	/** 系统过虑器id- 源文相同，译文不同的文本段 */
	public final static String FILTERID_srcSameButTgtSeg = "te.system.filter.srcSameButTgtSeg";
	/** 系统过虑器id- 译文相同，源文不同的文本段 */
	public final static String FILTERID_tgtSameButSrcSeg = "te.system.filter.tgtSameButSrcSeg";
	/** 系统过滤器id- 重复文本段 */
	public final static String FILTERID_duplicateSeg = "te.system.filter.duplicateSeg";
	/** 系统过虑器id- 带有批注的文本段 */
	public final static String FILTERID_withNoteSeg = "te.system.filter.withNoteSeg";
	/** 系统过虑器id- 存在乱码的文本段 */
	public final static String FILTERID_withGarbleSeg = "te.system.filter.withGarbleSeg";
	/** 系统过滤器id- 译文为空的文本段 */
	public final static String FILTERID_tgtNullSeg = "te.system.filter.tgtNullSeg";
	
	/** tmx editor 是否忽略标记，　qa 与　过滤器共用参数，该数据保存在　te.core 插件下 */
	public final static String FILTER_ignoreTag = "tmxEditorFilter.ignoreTag";
	/** tmx editor 是否忽略大小写，qa 与　过滤器共用参数，该数据保存在　te.core 插件下 */
	public final static String FILTER_ignoreCase = "tmxEditorFilter.ignoreCase";
	
	/** 自定义过滤器的操作类型 包含 */
	public final static String FILTER_TYPE_include = "include";
	/** 自定义过滤器的操作类型 不包含 */
	public final static String FILTER_TYPE_notInclude = "notInclude";
	/** 自定义过滤器的操作类型 等于 */
	public final static String FILTER_TYPE_equal = "equal";
	/** 自定义过滤器的操作类型 不等于 */
	public final static String FILTER_TYPE_notEqual = "notEqual";
	
	/**  更改应用到当前过滤结果 */
	public final static String FILTERID_filteredSegs = "filteredSegs";
	/**  更改应用到指定文本段 */
	public final static String FILTERID_givenSegs = "givenSegs";
	
}
