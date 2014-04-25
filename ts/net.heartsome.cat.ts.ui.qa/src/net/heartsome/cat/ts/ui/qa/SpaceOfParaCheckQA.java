package net.heartsome.cat.ts.ui.qa;

import java.text.MessageFormat;
import java.util.Map;

import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.model.QAResultBean;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 段首、段末空格检查
 * 是指源文本与目标文本的段首和段末的空格必须一致。
 * @author  robert	2012-01-17
 * @version 
 * @since   JDK1.6
 */
public class SpaceOfParaCheckQA extends QARealization {
	private int tipLevel;
	private IPreferenceStore preferenceStore;
	private boolean hasError;
	
	public SpaceOfParaCheckQA(){
		preferenceStore = Activator.getDefault().getPreferenceStore();
		tipLevel = preferenceStore.getInt(QAConstant.QA_PREF_spaceOfPara_TIPLEVEL);
	}
	
	@Override
	void setParentQaResult(QAResult qaResult) {
		super.setQaResult(qaResult);
	}
	
	@Override
	public String startQA(QAModel model, IProgressMonitor monitor, IFile iFile, QAXmlHandler xmlHandler,
			Map<String, String> tuMap) {
		
		hasError = false;
		String langPair = tuMap.get("langPair");
		
		String lineNumber = tuMap.get("lineNumber");
		String iFileFullPath = tuMap.get("iFileFullPath");
		String qaType = Messages.getString("qa.all.qaItem.SpaceOfParaCheck");
		
		String srcContent = tuMap.get("srcContent");
		String tarContent = tuMap.get("tarContent");
		String rowId = tuMap.get("rowId");
		
		int srcFirstSpaceNum = getSpaceNumber(srcContent, true);
		int srcLastSpaceNum = getSpaceNumber(srcContent, false);
		
		int tarFirstSpaceNum = getSpaceNumber(tarContent, true);
		int tarLastSpaceNum = getSpaceNumber(tarContent, false);
		
		//先处理段首空格的检查
		String errorTip = "";
		if (srcFirstSpaceNum > tarFirstSpaceNum) {
			errorTip = MessageFormat.format(Messages.getString("qa.SpaceOfParaCheckQA.tip1"), new Object[] {
					srcFirstSpaceNum, tarFirstSpaceNum, (srcFirstSpaceNum - tarFirstSpaceNum) });
			hasError = true;
			super.printQAResult(new QAResultBean(lineNumber, qaType, errorTip, iFileFullPath, langPair, rowId, tipLevel, QAConstant.QA_SPACEOFPARACHECK));
		}else if (srcFirstSpaceNum < tarFirstSpaceNum) {
			errorTip = MessageFormat.format(Messages.getString("qa.SpaceOfParaCheckQA.tip2"), new Object[] {
					srcFirstSpaceNum, tarFirstSpaceNum, (tarFirstSpaceNum - srcFirstSpaceNum) });
			hasError = true;
			super.printQAResult(new QAResultBean(lineNumber, qaType, errorTip, iFileFullPath, langPair, rowId, tipLevel, QAConstant.QA_SPACEOFPARACHECK));
		}
		
		//如果源文本全是空格，只提示段首空格，不提示段尾
		if (srcFirstSpaceNum == srcContent.length()) {
			return "";
		}
		
		//处理段末空格的检查
		if (srcLastSpaceNum > tarLastSpaceNum) {
			errorTip = MessageFormat.format(Messages.getString("qa.SpaceOfParaCheckQA.tip3"), new Object[] {
					srcLastSpaceNum, tarLastSpaceNum, (srcLastSpaceNum - tarLastSpaceNum) });
			hasError = true;
			super.printQAResult(new QAResultBean(lineNumber, qaType, errorTip, iFileFullPath, langPair, rowId, tipLevel, QAConstant.QA_SPACEOFPARACHECK));
		}else if (srcLastSpaceNum < tarLastSpaceNum) {
			errorTip = MessageFormat.format(Messages.getString("qa.SpaceOfParaCheckQA.tip4"), new Object[] {
					srcLastSpaceNum, tarLastSpaceNum, (tarLastSpaceNum - srcLastSpaceNum) });
			hasError = true;
			super.printQAResult(new QAResultBean(lineNumber, qaType, errorTip, iFileFullPath, langPair, rowId, tipLevel, QAConstant.QA_SPACEOFPARACHECK));
		}
		
		String result = "";
		if (hasError && tipLevel == 0) {
			result = QAConstant.QA_SPACEOFPARACHECK;
		}
		return result;
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
