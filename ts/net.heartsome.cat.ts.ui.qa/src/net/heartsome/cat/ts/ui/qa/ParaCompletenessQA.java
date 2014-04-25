package net.heartsome.cat.ts.ui.qa;

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
 * 文本段完整性检查
 * 分三种情况：
 *	a、目标文本不能为空。
 *	b、若目标文本有一个空格，那么就检查源文本是否是一个空格，若源文本中并不是空格，那么也不得行。
 *	c、检查源文本与目标文本是否一致。
 * @author  robert
 * @version 
 * @since   JDK1.6
 */
public class ParaCompletenessQA extends QARealization{
	private int tipLevel;
	private IPreferenceStore preferenceStore;
	private boolean hasError;
	
	public ParaCompletenessQA(){
		preferenceStore = Activator.getDefault().getPreferenceStore();
		tipLevel = preferenceStore.getInt(QAConstant.QA_PREF_paraComplete_TIPLEVEL);
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
		String qaType = Messages.getString("qa.all.qaItem.ParaCompletenessQA");
		
		String srcContent = tuMap.get("srcContent");
		String tarContent = tuMap.get("tarContent");
		String rowId = tuMap.get("rowId");
		
		String errorTip = "";
		//检查未翻译的情况
		if (tarContent == null || "".equals(tarContent)) {
			errorTip = Messages.getString("qa.ParaCompletenessQA.tip1");
			hasError = true;
			super.printQAResult(new QAResultBean(lineNumber, qaType, errorTip, iFileFullPath, langPair, rowId, tipLevel, QAConstant.QA_PARACOMPLETENESS));
		}else if (tarContent.length() > 0 && "".equals(tarContent.trim())) {	//检查目标文本中有一空格的情况
			//如果源文也是一个空格，那么通过检查，如果源文不是空格，则提示未翻译
			if (!(srcContent.length() > 0 && "".equals(srcContent.trim()))) {
				errorTip = Messages.getString("qa.ParaCompletenessQA.tip2");
				hasError = true;
				super.printQAResult(new QAResultBean(lineNumber, qaType, errorTip, iFileFullPath, langPair, rowId, tipLevel, QAConstant.QA_PARACOMPLETENESS));
			}
			
		}else if ( tarContent.trim().equals(srcContent.trim())) {//检查源文与译文一致的情况
			errorTip = Messages.getString("qa.ParaCompletenessQA.tip3");
			hasError = true;
			super.printQAResult(new QAResultBean(lineNumber, qaType, errorTip, iFileFullPath, langPair, rowId, tipLevel, QAConstant.QA_PARACOMPLETENESS));
		}
		
		String result = "";
		if (hasError && tipLevel == 0) {
			result = QAConstant.QA_PARACOMPLETENESS;
		}
		return result;
	}
}
