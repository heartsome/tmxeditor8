package net.heartsome.cat.te.core.qa;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.tmxdata.TmxLargeFileContainer;

/**
 * te 品质检查的　model 类，以及常量相关
 * @author  robert	2012-09-16
 * @version 
 * @since   JDK1.6
 */
public class QAModel {
	
	private Map<String, HashMap<String, String>> qaItemId_Name_Class;
	private boolean ignoreTag;
	private boolean ignoreCase;
	private String srcLang;
	private String tgtLang;
	private TmxLargeFileContainer container;
	
	
	public QAModel(){
		qaItemId_Name_Class = new LinkedHashMap<String, HashMap<String,String>>();
		HashMap<String, String> valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.ITEM_NAME, QAConstant.QA_TagConsistenceText);
		valueMap.put(QAConstant.ITEM_CLASSNAME, "net.heartsome.cat.te.core.qa.TagConsistenceQA");
		qaItemId_Name_Class.put(QAConstant.QA_TagConsistence, valueMap);
		
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.ITEM_NAME, QAConstant.QA_NumberConsistenceText);
		valueMap.put(QAConstant.ITEM_CLASSNAME, "net.heartsome.cat.te.core.qa.NumberConsistenceQA");
		qaItemId_Name_Class.put(QAConstant.QA_NumberConsistence, valueMap);

		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.ITEM_NAME, QAConstant.QA_SpaceOfParaCheckText);
		valueMap.put(QAConstant.ITEM_CLASSNAME, "net.heartsome.cat.te.core.qa.SpaceOfParaCheckQA");
		qaItemId_Name_Class.put(QAConstant.QA_SpaceOfParaCheck, valueMap);
		
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.ITEM_NAME, QAConstant.QA_TgtNullText);
		valueMap.put(QAConstant.ITEM_CLASSNAME, "net.heartsome.cat.te.core.qa.TgtNullQA");
		qaItemId_Name_Class.put(QAConstant.QA_TgtNull, valueMap);
		
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.ITEM_NAME, QAConstant.QA_SrcSameWithTgtText);
		valueMap.put(QAConstant.ITEM_CLASSNAME, "net.heartsome.cat.te.core.qa.SrcSameWithTgtQA");
		qaItemId_Name_Class.put(QAConstant.QA_SrcSameWithTgt, valueMap);
		
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.ITEM_NAME, QAConstant.QA_SrcSameButTgtText);
		valueMap.put(QAConstant.ITEM_CLASSNAME, "net.heartsome.cat.te.core.qa.SrcSameButTgtQA");
		qaItemId_Name_Class.put(QAConstant.QA_SrcSameButTgt, valueMap);
		
		valueMap = new HashMap<String, String>();
		valueMap.put(QAConstant.ITEM_NAME, QAConstant.QA_TgtSameButSrcText);
		valueMap.put(QAConstant.ITEM_CLASSNAME, "net.heartsome.cat.te.core.qa.TgtSameButSrcQA");
		qaItemId_Name_Class.put(QAConstant.QA_TgtSameButSrc, valueMap);
		
	}


	public Map<String, HashMap<String, String>> getQaItemId_Name_Class() {
		return qaItemId_Name_Class;
	}
	public boolean isIgnoreTag() {
		return ignoreTag;
	}
	public void setIgnoreTag(boolean ignoreTag) {
		this.ignoreTag = ignoreTag;
	}
	public boolean isIgnoreCase() {
		return ignoreCase;
	}
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	public String getSrcLang() {
		return srcLang;
	}
	public void setSrcLang(String srcLang) {
		this.srcLang = srcLang;
	}
	public String getTgtLang() {
		return tgtLang;
	}
	public void setTgtLang(String tgtLang) {
		this.tgtLang = tgtLang;
	}
	public TmxLargeFileContainer getContainer() {
		return container;
	}
	public void setContainer(TmxLargeFileContainer container) {
		this.container = container;
	}

	
	
	public static void main(String[] args) {
//		List<String> tuIdList = new ArrayList<String>();
//		for (int i = 0; i < 300000; i++) {
//			tuIdList.add("/desctop/robert/new folder1" + TeCoreConstant.ID_MARK + i);
//		}
//		
//		System.out.println(tuIdList.size());
//		String a = "asdf";
//		
//		long time1 = System.currentTimeMillis();
//		System.out.println(tuIdList.indexOf(a));
//		System.out.println(System.currentTimeMillis() - time1);
		
		long time1 = System.currentTimeMillis();
		Map<String, Integer> tuIdMap = new HashMap<String, Integer>();
		for (int i = 1; i <= 300000; i++) {
			tuIdMap.put("/desctop/robert/new folder1" + TeCoreConstant.ID_MARK + i, i);
		}
		System.out.println(System.currentTimeMillis() - time1);
		
		System.out.println(tuIdMap.size());
		
		time1 = System.currentTimeMillis();
		for (int i = 1; i <= 300000; i++) {
			tuIdMap.get("/desctop/robert/new folder1" + TeCoreConstant.ID_MARK + i);
		}
		System.out.println(System.currentTimeMillis() - time1);
		
		
	}
	
}
