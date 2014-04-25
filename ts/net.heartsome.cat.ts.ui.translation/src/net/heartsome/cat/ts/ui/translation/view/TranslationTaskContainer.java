/**
 * Task.java
 *
 * Version information :
 *
 * Date:2013-1-7
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.translation.view;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.ts.tm.complexMatch.IComplexMatch;
import net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TranslationTaskContainer {

	private List<ISimpleMatcher> simpleMatchers;

	private List<IComplexMatch> complexMatchers;

	public TranslationTaskContainer() {
		simpleMatchers = new ArrayList<ISimpleMatcher>();
		complexMatchers = new ArrayList<IComplexMatch>();
	}

	public synchronized void clearContainer(){
		complexMatchers.clear();
		simpleMatchers.clear();
	}
	
	public boolean isEmpty() {
		if (simpleMatchers.size() == 0 && complexMatchers.size() == 0) {
			return true;
		}
		return false;
	}

	public synchronized Object popTranslationTask() {
		if (complexMatchers.size() != 0) {
			return complexMatchers.remove(complexMatchers.size() - 1);
		}
		if (simpleMatchers.size() != 0) {
			return simpleMatchers.remove(simpleMatchers.size() - 1);
		}
		return null;
	}

	public synchronized void pushTranslationTask(Object matcher) {
		if (matcher instanceof ISimpleMatcher) {
			ISimpleMatcher _matcher = (ISimpleMatcher) matcher;
			for (ISimpleMatcher simpleMatcher : simpleMatchers) {
				if (simpleMatcher.getMathcerToolId().equals(_matcher.getMathcerToolId())) {
					return;
				}
			}
			simpleMatchers.add(0, _matcher);
		} else if (matcher instanceof IComplexMatch) {
			IComplexMatch _matcher = (IComplexMatch) matcher;
			for (IComplexMatch complexMatcher : complexMatchers) {
				if (complexMatcher.getToolId().equals(_matcher.getToolId())) {
					return;
				}
			}
			complexMatchers.add(0, _matcher);
		}
	}
}
