package net.heartsome.cat.te.core.qa;

import net.heartsome.cat.common.util.TextUtil;

/**
 * 段首/段末空格
 * @author  robert	2013-09-16
 * @version 
 * @since   JDK1.6
 */
public class SpaceOfParaCheckQA extends QARealization {

	@Override
	public void beginTmxQA(QATUDataBean tu) {
		String tgtText = model.isIgnoreTag() ? tu.getTgtPureText() : tu.getTgtContent();
		if (TextUtil.checkStringEmpty(tgtText)) {
			return;
		}
		boolean hasError = false;
		
		String srcText = model.isIgnoreTag() ? tu.getSrcPureText() : tu.getSrcContent();
		
		int srcFirstSpaceNum = getSpaceNumber(srcText, true);
		int srcLastSpaceNum = getSpaceNumber(srcText, false);
		
		int tarFirstSpaceNum = getSpaceNumber(tgtText, true);
		int tarLastSpaceNum = getSpaceNumber(tgtText, false);
		
		//先处理段首空格的检查
		String qaType = QAConstant.QA_SpaceOfParaCheck;
		String qaTypeText = QAConstant.QA_SpaceOfParaCheckText;
//		String qaTip = "";
		if (srcFirstSpaceNum > tarFirstSpaceNum) {
//			qaTip = MessageFormat.format(Messages.getString("qa.SpaceOfParaCheckQA.paraFirstLost"), new Object[] {
//					srcFirstSpaceNum, tarFirstSpaceNum, (srcFirstSpaceNum - tarFirstSpaceNum) });
//			super.printResult(new QAResultBean(tu.getTuID(), tu.getLineNumber(), qaType, qaTypeText, qaTip));
			hasError = true;
		} else if (srcFirstSpaceNum < tarFirstSpaceNum) {
//			qaTip = MessageFormat.format(Messages.getString("qa.SpaceOfParaCheckQA.paraFirstExcess"), new Object[] {
//					srcFirstSpaceNum, tarFirstSpaceNum, (tarFirstSpaceNum - srcFirstSpaceNum) });
//			super.printResult(new QAResultBean(tu.getTuID(), tu.getLineNumber(), qaType, qaTypeText, qaTip));
			hasError = true;
		}
		
		//处理段末空格的检查
		if (srcLastSpaceNum > tarLastSpaceNum) {
//			qaTip = MessageFormat.format(Messages.getString("qa.SpaceOfParaCheckQA.paraLastLost"), new Object[] {
//					srcLastSpaceNum, tarLastSpaceNum, (srcLastSpaceNum - tarLastSpaceNum) });
//			super.printResult(new QAResultBean(tu.getTuID(), tu.getLineNumber(), qaType, qaTypeText, qaTip));
			hasError = true;
		}else if (srcLastSpaceNum < tarLastSpaceNum) {
//			qaTip = MessageFormat.format(Messages.getString("qa.SpaceOfParaCheckQA.paraLastExcess"), new Object[] {
//					srcLastSpaceNum, tarLastSpaceNum, (tarLastSpaceNum - srcLastSpaceNum) });
//			super.printResult(new QAResultBean(tu.getTuID(), tu.getLineNumber(), qaType, qaTypeText, qaTip));
			hasError = true;
		}
		
		if (hasError) {
			super.printResult(new QAResultBean(tu.getTuID(), qaType, tu.getLineNumber(), qaTypeText, null, tu.getSrcContent(), tu.getTgtContent()));
		}
		
	}

	@Override
	public void beginDBQA() {
		
	}
	
	
	
	/**
	 * 获取文本段段首或段尾的空格数
	 * @param content	要获取空格数的文本
	 * @param isFirst	是否获取段首的空格数
	 * @return 空格数;
	 */
	public int getSpaceNumber(String content, boolean isFirst){
		int spaceNum = 0;
		
		if (isFirst) {
			for (int i = 0; i < content.length(); i++) {
				if (content.charAt(i) == QAConstant.QA_ONE_SPACE_CHAR) {
					spaceNum ++;
				}else {
					break;
				}
			}
		}else {
			for (int i = content.length() - 1; i >= 0; i--) {
				if (content.charAt(i) == QAConstant.QA_ONE_SPACE_CHAR) {
					spaceNum ++;
				}else {
					break;
				}
			}
		}
		return spaceNum;
	}

}
