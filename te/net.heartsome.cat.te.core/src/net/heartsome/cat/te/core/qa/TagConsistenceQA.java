package net.heartsome.cat.te.core.qa;

import java.util.List;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TmxInnerTagParser;
import net.heartsome.cat.common.util.TextUtil;

/**
 * 标记一致性检查
 * @author  robert	2013-09-16
 * @version 
 * @since   JDK1.6
 */
public class TagConsistenceQA extends QARealization {

	@Override
	public void beginTmxQA(QATUDataBean tu) {
		String tgtText = model.isIgnoreTag() ? tu.getTgtPureText() : tu.getTgtContent();
		if (TextUtil.checkStringEmpty(tgtText)) {
			return;
		}
		boolean hasError = false;
		
		String srcContent = tu.getSrcContent();
		String tgtContent = tu.getTgtContent();
		
		TmxInnerTagParser parser = TmxInnerTagParser.getInstance();

		List<InnerTagBean> srcTagList = parser.parseInnerTag(new StringBuilder(srcContent));
		List<InnerTagBean> tgtTagList = parser.parseInnerTag(new StringBuilder(tgtContent));
		
		InnerTagBean srcTag = null;
		InnerTagBean tgtTag = null;
		srcTagFor:for (int i = 0; i < srcTagList.size(); i++) {
			srcTag = srcTagList.get(i);
			for (int j = 0; j < tgtTagList.size(); j++) {
				tgtTag = tgtTagList.get(j);
				if (srcTag.getContent().equals(tgtTag.getContent())) {
					srcTagList.remove(i);
					tgtTagList.remove(j);
					i --;
					continue srcTagFor;
				}
			}
		}
		
		String qaType = QAConstant.QA_TagConsistence;
		String qaTypeText = QAConstant.QA_TagConsistenceText;
//		StringBuffer qaTipSB = new StringBuffer();
		if (srcTagList.size() > 0) {
			hasError = true;
//			qaTipSB.append(Messages.getString("qa.TagConsistenceQA.tagLose"));
//			for (InnerTagBean tag : srcTagList) {
//				if (tag.getType() != END) {
//					qaTipSB.append(tag.getName());
//					qaTipSB.append("、");
//				}
//			}
//			qaTipSB.deleteCharAt(qaTipSB.length() - 1);
//			qaTipSB.append("; ");
		}
		if (tgtTagList.size() > 0) {
			hasError = true;
//			qaTipSB.append(Messages.getString("qa.TagConsistenceQA.tagExcess"));
//			for (InnerTagBean tag : tgtTagList) {
//				if (tag.getType() != END) {
//					qaTipSB.append(tag.getName());
//					qaTipSB.append("、");
//				}
//				qaTipSB.deleteCharAt(qaTipSB.length() - 1);
//				qaTipSB.append("; ");
//			}
		}
		if (hasError) {
			super.printResult(new QAResultBean(tu.getTuID(), qaType, tu.getLineNumber(), qaTypeText, null, tu.getSrcContent(), tu.getTgtContent()));
		}
	}

	@Override
	public void beginDBQA() {
		
	}

}
