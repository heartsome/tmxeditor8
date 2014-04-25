package net.heartsome.cat.te.core.qa;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.heartsome.cat.common.util.TextUtil;


/**
 * 数字一致性检查
 * @author  robert	2013-09-16
 * @version 
 * @since   JDK1.6
 */
public class NumberConsistenceQA extends QARealization {

	@Override
	public void beginTmxQA(QATUDataBean tu) {
		String tgtText = model.isIgnoreTag() ? tu.getTgtPureText() : tu.getTgtContent();
		if (TextUtil.checkStringEmpty(tgtText)) {
			return;
		}
		
		boolean hasError = false;
		String pattern = "-?\\d{1,}(\\.?\\d{1,})?";
		Pattern patt = Pattern.compile(pattern);
		//获取源文本中的所有数字
		Matcher matcher = patt.matcher(tu.getSrcPureText());
		List<String> sourceNumbers = new LinkedList<String>();
		while (matcher.find()) {
			sourceNumbers.add(matcher.group()) ;
		}
		
		//获取目标文本中的数字
		matcher = patt.matcher(tu.getTgtPureText());
		List<String> targetNumbers = new LinkedList<String>();
		while (matcher.find()) {
			targetNumbers.add(matcher.group()) ;
		}
		
		String lineNumber = tu.getLineNumber();
		
		String qaType = QAConstant.QA_NumberConsistence;
		String qaTypeText = QAConstant.QA_NumberConsistenceText;
		Map<String, List<String>> resultMap = compareNumber(sourceNumbers, targetNumbers);
		if (resultMap.get("source") != null && resultMap.get("source").size() > 0) {
			//输出数字一致性中目标文件所遗失的数字
			List<String> resultList = resultMap.get("source");
//			String resultStr = "";
//			for (int index = 0; index < resultList.size(); index++) {
//				resultStr += "'" + resultList.get(index) + "', ";
//			}
//			
//			if (resultStr.length() > 0) {
//				resultStr = resultStr.substring(QAConstant.QA_ZERO, resultStr.length() - QAConstant.QA_TWO);
//				String errorTip = MessageFormat.format(Messages.getString("qa.NumberConsistenceQA.catnotFindNumber"),
//						resultStr);
////				super.printResult(new QAResultBean(tu.getTuID(), lineNumber, qaType, qaTypeText, errorTip));
//			}
			if (resultList.size() > 0) {
				hasError = true;
			}
		}
		//输出数字一致性检查中目标文件中多出的数字
		if (resultMap.get("target") != null && resultMap.get("target").size() > 0) {
			//输出数字一致性中目标文件所遗失的数字
			List<String> resultList = resultMap.get("target");
//			String resultStr = "";
//			for (int index = 0; index < resultList.size(); index++) {
//				resultStr += "'" + resultList.get(index) + "', ";
//			}
//			
//			if (resultStr.length() > 0) {
//				resultStr = resultStr.substring(QAConstant.QA_ZERO, resultStr.length() - QAConstant.QA_TWO);
//				String errorTip = MessageFormat.format(Messages.getString("qa.NumberConsistenceQA.findExcessNumber"),
//						resultStr);
////				super.printResult(new QAResultBean(tu.getTuID(), lineNumber, qaType, qaTypeText, errorTip));
//			}
			if (resultList.size() > 0) {
				hasError = true;
			}
		}
		if (hasError) {
			super.printResult(new QAResultBean(tu.getTuID(), qaType, lineNumber, qaTypeText, null, tu.getSrcContent(), tu.getTgtContent()));
		}
		
	}

	@Override
	public void beginDBQA() {
		
	}

	
	
	/**
	 * 将查出的数字与目标文本进行相关的比较
	 * @param findNumber
	 * @param targetText
	 * @return
	 */
	public Map<String, List<String>> compareNumber(List<String> sourceNumbers, List<String> targetNumbers){
		Map<String, List<String>> resultMap  = new HashMap<String, List<String>>();
		List<String> resultList = new LinkedList<String>();
		//下面查看其目标数字集合中是否有源文本中的数字，如果有，则将这个数字从目标数字集合中删除
		int tarIndex;
		if (targetNumbers.size() == 0) {
			resultMap.put("source", sourceNumbers);
			return resultMap;
		}
		for (int index = 0; index < sourceNumbers.size(); index++) {
			String sourceNumber = sourceNumbers.get(index);
			
			if ((tarIndex = targetNumbers.indexOf(sourceNumber)) >= 0) {
				targetNumbers.remove(tarIndex);
			}else {
				resultList.add(sourceNumber);
			}
		}
		resultMap.put("source", resultList);
		if (targetNumbers.size() > 0) {
			resultMap.put("target", targetNumbers);
		}
		return resultMap;
	}
}
