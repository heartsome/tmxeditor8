package net.heartsome.cat.te.core.qa;

import net.heartsome.cat.te.core.resource.Messages;



/**
 * te 品质检查的常量类
 * @author  robert	2012-09-16
 * @version 
 * @since   JDK1.6
 */
public class QAConstant {
	/** 标记一致性检查 */
	public static final String QA_TagConsistence = "te.qa.tagConsistence";
	/** 数字一致性检查 */
	public static final String QA_NumberConsistence = "te.qa.numberConsistence";
	/** 文本段前后空格检查 */
	public static final String QA_SpaceOfParaCheck = "te.qa.spaceOfParaCheck";
	/** 译文为空 */
	public static final String QA_TgtNull = "te.tgtNull";
	/** 源文与译文相同 */
	public static final String QA_SrcSameWithTgt = "te.qa.srcSameWithTgt";
	/** 源文相同但译文不同 */
	public static final String QA_SrcSameButTgt = "te.qa.srcSameButTgt";
	/** 译文相同但源文不同 */
	public static final String QA_TgtSameButSrc = "te.qa.tgtSameButSrc";
	
	//----------------- 每个检查项的名称 -----------------
	public static final String QA_TagConsistenceText = Messages.getString("qa.QAConstant.QA_TagConsistenceText");
	public static final String QA_NumberConsistenceText = Messages.getString("qa.QAConstant.QA_NumberConsistenceText");
	public static final String QA_SpaceOfParaCheckText = Messages.getString("qa.QAConstant.QA_SpaceOfParaCheckText");
	public static final String QA_TgtNullText = Messages.getString("qa.QAConstant.QA_TgtNullText");
	public static final String QA_SrcSameWithTgtText = Messages.getString("qa.QAConstant.QA_SrcSameWithTgtText");
	public static final String QA_SrcSameButTgtText = Messages.getString("qa.QAConstant.QA_SrcSameButTgtText");
	public static final String QA_TgtSameButSrcText = Messages.getString("qa.QAConstant.QA_TgtSameButSrcText");
	
	//----------------------------------------------------------------------//
	//---------------------程序中数字或字符串常量-------------------------------//
	//----------------------------------------------------------------------//
	
	public static final int QA_ZERO = 0;
	public static final int QA_FIRST = 1;
	public static final int QA_TWO = 2;
	public static final int QA_THREE = 3;
	/** 键盘上回车键的键码 */
	public static final int QA_CENTERKEY_1 = 13;
	/** 小键盘上回车键的键码 */
	public static final int QA_CENTERKEY_2 = 16777296;
	
	/** 品质检查项中存放品质检查名的key值常量 */
	public static final String ITEM_NAME = "qaItemName";
	/** 品质检查项中存放品质检查类名的key值常量 */
	public static final String ITEM_CLASSNAME = "qaItemClassName";
	
	/** 保存当前要进行检查的品质检查项，每项之前用　; 隔开，该值保存在　te.core　的插件里面 */
	public static final String PREF_useableQAItemStr = "te.tmxeditor.qa.pref.useableQAItemStr";
	
	/** 一个空格字符 */
	public static final char QA_ONE_SPACE_CHAR = ' ';
	


}
