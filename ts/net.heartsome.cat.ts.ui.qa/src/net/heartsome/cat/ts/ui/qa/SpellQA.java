package net.heartsome.cat.ts.ui.qa;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.bean.SingleWord;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.model.QAResultBean;
import net.heartsome.cat.ts.ui.qa.nonTransElement.NonTransElementOperate;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.qa.spell.AspellChecker;
import net.heartsome.cat.ts.ui.qa.spell.Hunspell;
import net.heartsome.cat.ts.ui.qa.spell.inter.HSSpellChecker;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 拼写检查，
 * @author  robert	2012-02-03
 * @version 
 * @since   JDK1.6
 */
public class SpellQA extends QARealization {
	private HSSpellChecker spelling = null;
	/** 不需用进行拼写检查的目标语言 */
	private List<String> nonSpellTarLangList = new ArrayList<String>();
	
	private int tipLevel;
	private IPreferenceStore qaPreferenceStore;
	private boolean hasError;
	private boolean isContinue;
	private StringTokenizer stringToken;
	
	/** 拼写检查器类型，是否是 hunspell 拼写词典 */
	private boolean isHunspell = false;
	private NonTransElementOperate nontransOper;
	/** 是否忽略非译元素 */
	private boolean ignoreNontrans;
	/** 是否忽略单词首字母为数字 */
	private boolean ignoreDigitalFirst;
	/** 是否忽略单词首字母为大写 */
	private boolean ignoreUpperCaseFirst;
	public final static Logger logger = LoggerFactory.getLogger(SpellQA.class.getName());
	
	@Override
	void setParentQaResult(QAResult qaResult) {
		super.setQaResult(qaResult);
	}
	
	public SpellQA(){
		//确定检查类别
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		isHunspell = preferenceStore.getBoolean(QAConstant.QA_PREF_isHunspell);
		ignoreNontrans = preferenceStore.getBoolean(QAConstant.QA_PREF_ignoreNontrans);
		ignoreDigitalFirst = preferenceStore.getBoolean(QAConstant.QA_PREF_ignoreDigitalFirst);
		ignoreUpperCaseFirst = preferenceStore.getBoolean(QAConstant.QA_PREF_ignoreUpperCaseFirst);
		
		qaPreferenceStore = Activator.getDefault().getPreferenceStore();
		tipLevel = qaPreferenceStore.getInt(QAConstant.QA_PREF_spell_TIPLEVEL);
		//UNDO 拼写检查的信息提示太过专业化，详细化提示。比如：qa.spellCheck.HashDictionary.tip1=词典文件缺少表格大小。
	}
	
	@Override
	public String startQA(final QAModel model, IProgressMonitor monitor, IFile iFile, QAXmlHandler xmlHandler,
			Map<String, String> tuMap) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		hasError = false;
		isContinue = false;
		//目标语言
		final String target_lan = tuMap.get("target_lan");
		
		//若未配置该目标语言的词典，退出程序的执行
		if (nonSpellTarLangList.indexOf(target_lan) != -1) {
			return "";
		}
		
		String targetPureText = TextUtil.resetSpecialString(tuMap.get("tarPureText"));
		
		String lineNumber = tuMap.get("lineNumber");
		String langPair = tuMap.get("langPair");
		String iFileFullPath = tuMap.get("iFileFullPath");
		String rowId = tuMap.get("rowId");
		
		if (spelling == null) {
			if (isHunspell) {
				spelling = new Hunspell(model.getShell());
				if (ignoreNontrans) {
					nontransOper = new NonTransElementOperate();
					nontransOper.openNonTransDB();
				}
			}else {
				spelling = new AspellChecker();
			}
		}
		
		// 若拼写检查器错误，或者出错，返回 null
		if (spelling == null || spelling.isError()) {
			return null;
		}
		
		//如果该拼写检查实例为空，退出执行，并且下次遇到相同目标语言不再检查
		if (!spelling.checkLangAvailable(target_lan)) {
			nonSpellTarLangList.add(target_lan);
			if (!isHunspell) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						String message = Messages.getString("qa.SpellQA.addTip1");
						message = MessageFormat.format(message, new Object[]{target_lan});
						isContinue = MessageDialog.openConfirm(model.getShell(), Messages.getString("qa.all.dialog.ok"), message);
					}
				});
				
				if (!isContinue) {
					monitor.setCanceled(true);
				}
			}
			return "";
		}
		
		List<SingleWord> errorWords;
		if (isHunspell) {
			LinkedList<SingleWord> wordList = new LinkedList<SingleWord>();
			getSingleWords(targetPureText, wordList);
			errorWords = spelling.getErrorWords(null, wordList, target_lan);
		}else {
			LinkedList<SingleWord> wordList = new LinkedList<SingleWord>();
			getSingleWords(targetPureText, wordList);
			errorWords = spelling.getErrorWords(targetPureText, wordList, target_lan);
		}
		
		if (spelling.isError()) {
			return null;
		}
		
		//开始输入结果
		if (errorWords == null || errorWords.size() == 0) {
			return "";
		}
		
		String errorTip = Messages.getString("qa.SpellQA.tip1");
		String qaType = Messages.getString("qa.all.qaItem.SpellQA");
			
		for (int i = 0; i < errorWords.size(); i++) {
			errorTip += "'" + errorWords.get(i).getPureWord() + "' 、";
		}
		errorTip = errorTip.substring(0, errorTip.length() - 1);
		errorTip += Messages.getString("qa.SpellQA.tip2");
		
		hasError = true;
		super.printQAResult(new QAResultBean(lineNumber, qaType, errorTip, iFileFullPath, langPair, rowId, tipLevel, QAConstant.QA_SPELL));
		String result = "";
		if (hasError && tipLevel == 0) {
			result = QAConstant.QA_SPELL;
		}
		return result;
	}
	
	/**
	 * 获取单个单词，这里面主要是根据不同的选项。得到要进行拼写检查的单词
	 */
	private void getSingleWords(String pureText, List<SingleWord> tgtWordList) {
		stringToken = new StringTokenizer(pureText, Constant.SEPARATORS, false);
		List<Integer[]> ignoreParaList = null;
		// 如果要处理忽略非译元素，那么执行如下操作 (备注：所有的忽略项。只是针对 hunspell)
		if (isHunspell && ignoreNontrans) {
			ignoreParaList = nontransOper.getIgnorePara(pureText, null);
		}
		
		int start = 0;
		int length = 0;
		int end = 0;
		while(stringToken.hasMoreTokens()){
			String pureWord = stringToken.nextToken();
			start = pureText.indexOf(pureWord, start);
			length = pureWord.length();
			end = start + length;
			
			// 经过一系列的判断，从而删除一些不符合标准的单词。将剩下的单词传入拼写检查器中进行检查
			if (isHunspell) {
				// 是否忽略非译元素
				if (ignoreNontrans) {
					boolean needIgnore = false;
					for(Integer[] ignoreIndexs : ignoreParaList){
						if (start >= ignoreIndexs[0] && end <= ignoreIndexs[1] ) {
							needIgnore = true;
							break;
						}
					}
					if (needIgnore) {
						start = start + pureWord.length();
						continue;
					}
				}
				// 是否忽略首字母为数字
				if (ignoreDigitalFirst && checkDigitalFirst(pureWord)) {
					start = start + pureWord.length();
					continue;
				}
				
				// 是否忽略首字母为大写
				if (ignoreUpperCaseFirst && checkUpperCaseFirst(pureWord)) {
					start = start + pureWord.length();
					continue;
				}
			}
			
			tgtWordList.add(new SingleWord(null, pureWord, start, length));
			start = start + pureWord.length();
		}
	}
	
	/**
	 * 检查单词是否首字母大写
	 * @return
	 */
	private boolean checkDigitalFirst(String pureWord){
		return Character.isDigit(pureWord.charAt(0));
	}
	
	/**
	 * 检查单词是否首字母为数字
	 * @return
	 */
	private boolean checkUpperCaseFirst(String pureWord){
		String otherStr = pureWord.substring(1, pureWord.length());
		return Character.isUpperCase(pureWord.charAt(0)) && otherStr.equals(otherStr.toLowerCase());
	}
	


}
