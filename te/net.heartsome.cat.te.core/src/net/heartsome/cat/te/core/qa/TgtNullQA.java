package net.heartsome.cat.te.core.qa;

import net.heartsome.cat.common.util.TextUtil;



/**
 * 译文为空的检查
 * @author  robert	2013-09-16
 * @version 
 * @since   JDK1.6
 */
public class TgtNullQA extends QARealization {

	@Override
	public void beginTmxQA(QATUDataBean tu) {
		// UNDO 这里还要控制，译文为空的标准。
		String tgtText = model.isIgnoreTag() ? tu.getTgtPureText() : tu.getTgtContent();
		if (TextUtil.checkStringEmpty(tgtText)) {
			String qaType = QAConstant.QA_TgtNull;
			String qaTypeText = QAConstant.QA_TgtNullText;
//			String qaTip = Messages.getString("qa.TgtNullQA.tgtNullTip");
//			super.printResult(new QAResultBean(tu.getTuID(), tu.getLineNumber(), qaType, qaTypeText, qaTip));
			
			super.printResult(new QAResultBean(tu.getTuID(), qaType, tu.getLineNumber(), qaTypeText, null, tu.getSrcContent(), tu.getTgtContent()));
		}
		
	}

	@Override
	public void beginDBQA() {
		
	}
	
	public static void main(String[] args) {
		String text = "	　	this "+
"is a\n" +
				" test	";
		System.out.println(TextUtil.trimEnter(text));
		long time1 = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			TextUtil.trimString(text);
		}
		System.out.println("耗时为：" + (System.currentTimeMillis() - time1));
	}

}
