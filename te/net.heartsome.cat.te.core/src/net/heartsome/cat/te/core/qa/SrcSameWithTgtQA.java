package net.heartsome.cat.te.core.qa;

import net.heartsome.cat.common.util.TextUtil;


/**
 * 源文与译文相同的品质检查
 * @author  
 * @version 
 * @since   JDK1.6
 */
public class SrcSameWithTgtQA extends QARealization {

	@Override
	public void beginTmxQA(QATUDataBean tu) {
		String tgtText = model.isIgnoreTag() ? tu.getTgtPureText() : tu.getTgtContent();
		if (TextUtil.checkStringEmpty(tgtText)) {
			return;
		}
		tgtText = TextUtil.trimString(tgtText);
		String srcText = model.isIgnoreTag() ? tu.getSrcPureText() : tu.getSrcContent();
		srcText = TextUtil.trimString(srcText);
		
		if (model.isIgnoreCase() ? srcText.equalsIgnoreCase(tgtText) : srcText.equals(tgtText)) {
			String qaType = QAConstant.QA_SrcSameWithTgt;
			String qaTypeText = QAConstant.QA_SrcSameWithTgtText;
//			String qaTip = Messages.getString("qa.SrcSameWithTgtQA.srcSameButTgtTip");
//			super.printResult(new QAResultBean(tu.getTuID(), tu.getLineNumber(), qaType, qaTypeText, qaTip));
			super.printResult(new QAResultBean(tu.getTuID(), qaType, tu.getLineNumber(), qaTypeText, null, tu.getSrcContent(), tu.getTgtContent()));
		}
	}

	@Override
	public void beginDBQA() {
		
	}

}
