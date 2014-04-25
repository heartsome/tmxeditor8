/**
 * AbstractTmxDataAccess.java
 *
 * Version information :
 *
 * Date:2013/5/17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */

package net.heartsome.cat.te.core.tmxdata;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.te.core.TeCoreConstant;
import net.heartsome.cat.te.core.bean.ExportBean;
import net.heartsome.cat.te.core.bean.TmxEditorFilterBean;
import net.heartsome.cat.te.core.bean.TmxPropertiesBean;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractTmxDataAccess {
	protected List<String> tuIdentifiers;
	protected TmxPropertiesBean tmxPropertiesBean;
	protected String currSrcLang;
	protected String currTgtLang;
	
	protected boolean isDirty = false;

	/** TMX 中存在的语言类型，不包括源语言 */
	protected List<String> langList;

	private List<ITmxDataChangeListener> dataChangeListener;
	
	private TmxContainer tmxContainer;
	public AbstractTmxDataAccess(TmxContainer container) {
		tuIdentifiers = new ArrayList<String>();
		langList = new ArrayList<String>();
		dataChangeListener = new ArrayList<ITmxDataChangeListener>();
		this.tmxContainer = container;
	}
	
	public TmxContainer getTmxContainer() {
		return tmxContainer;
	}
	
	public String getCurrSrcLang() {
		return currSrcLang;
	}

	public String getCurrTgtLang() {
		return currTgtLang;
	}

	/**
	 * @return TMX 中存在的语言类型，不包括源语言;
	 */
	public List<String> getLangList() {
		return langList;
	}

	public int getDisplayTuCount() {
		if (tuIdentifiers == null) {
			return 0;
		}
		return tuIdentifiers.size();
	}

	public List<String> getDisplayTuIdentifiers() {
		return tuIdentifiers;
	}

	/**
	 * Note: 如果是 TMDB 则返属只会包含目标语言属性 Get TMX 文件相关属性，
	 * @return null or <code>TmxPropertiesBean</code>;
	 */
	public TmxPropertiesBean getTmxProperties() {
		if (tmxPropertiesBean == null) {
			tmxPropertiesBean = loadTmxProperties();
		}
		return tmxPropertiesBean;
	}

	public String getTuIndentifierByRowIndex(int rowIndex) {
		if (tuIdentifiers == null) {
			return "";
		}
		return tuIdentifiers.get(rowIndex);
	}

	public boolean isDirty(){
		return isDirty;
	}
	
	public void setDirty(boolean isDirty){
		if (this.isDirty != isDirty) {
			this.isDirty = isDirty;
			for(ITmxDataChangeListener l : dataChangeListener){
				l.tmxDataChanged();
			}
		}
	}
	
	public void addTmxDataChangeListener(ITmxDataChangeListener l){
		if(!this.dataChangeListener.contains(l)){
			this.dataChangeListener.add(l);
		}
	}
	
	public void removeTmxDataChangeListener(ITmxDataChangeListener l){
		this.dataChangeListener.remove(l);
	}
	
	public abstract boolean isSourceExist();
	
	public abstract String retrieveTuXml(int tuIdentifier);

	public abstract void closeTmxDataAccess(IProgressMonitor monitor) throws Exception;
	
	public abstract void save(IProgressMonitor monitor) throws Exception;
	
	public abstract void saveAs(IProgressMonitor monitor, ExportBean exportBean);
	
	public abstract TmxPropertiesBean loadTmxProperties();

	/**
	 * 根据当前过滤条件、源语言、目标语言，以及当前对象是否是多目标加载需要显示的 TU 的唯一标识
	 * @param filterBean
	 *            <code>TmxEditorFilterBean</code>
	 * @param srcLang
	 *            source Language
	 * @param tgtLang
	 *            target Language
	 * @param isMultiTarget
	 *            is or not has multi target language ;
	 */
	public abstract void loadDisplayTuIdentifierByFilter(IProgressMonitor monitor,TmxEditorFilterBean filterBean, String srcLang,
			String tgtLang, String srcSearchText, String tgtSearchText);

	/**
	 * 将TU生成TMX文件节点插入到TMX文件末尾，并将这个记录添加到当前过滤条件中，或者将TU内容插入到数据库中。
	 * @param tu
	 * @param current Selected TU identifier 
	 * @return return new TU  identifier
	 **/
	public abstract String addTu(TmxTU tu, String selTuIdentifer);

	/**
	 *  删除一个TU，如果TU只有三个TUV，直接删除整个TU节点，如果TU下超出了三个TUV（多语言对的情况下），则只删除当前源和目标对应的TUV节点。需要更新修改时间。
	 * @see deleteTu
	 * @param tuIdentifiers
	 **/
	public abstract void deleteTus(String[] tuIdentifiers, IProgressMonitor monitor);

	/**
	 * 1.将newText写入到Tmx文件或者TmDb <br>
	 * 2.将newText更新到TmxSegement对象中。
	 * @param newText
	 * @param tu
	 * @param tuv
	 **/
	public abstract void updateTuvContent(String identifier, String newText, TmxTU tu, TmxSegement tuv);

	public abstract int addTuNote(String tuIdentifier, TmxTU tu, String content);
	public int addTuNote(Map<String, TmxTU> tus, String content) {return 0;};

	public abstract void updateTuNote(String tuIdnetifier, TmxTU tu, TmxNote note, String newContent);
	public void updateTuNote(Map<String, TmxTU> tus, TmxNote note, String newContent) {}
	
	public abstract void deleteTuNote(String tuIdentifier, TmxTU tu, TmxNote deleteNote);
	public void deleteTuNote(Map<String, TmxTU> tus, TmxNote deleteNote) {}

	public abstract int addTuProp(String tuIdentifier, TmxTU tu, String propType, String newContent);
	public int addTuProp(Map<String, TmxTU> tus, String propType, String newContent) {return 0;}

	public abstract void updateTuProp(String tuIdentifier, TmxTU tu, TmxProp prop, String propType, String newContent);
	public void updateTuProp(Map<String, TmxTU> tus, TmxProp prop, String propType, String newContent) {}

	public abstract void deleteTuProp(String tuIdentifier, TmxTU tu, TmxProp deleteProp);
	public void deleteTuProp(Map<String, TmxTU> tus, TmxProp deleteProp){};
	public void deleteTuPropByType(Map<String, TmxTU> tus, String name){};

	public abstract void addTuAttribute(String tuIdentifier, TmxTU tu, String name, String value);

	public abstract void updateTuAttribute(String tuIdentifier, TmxTU tu, String name, String newValue);
	public void updateTuAttribute(Map<String, TmxTU> tus, String name, String newValue){};

	public abstract void deleteTuAttribute(String tuIdentifier, TmxTU tu, String name);
	public void deleteTuAttribute(Map<String, TmxTU> tus, String name){};

	public abstract void addTuvAttribute(String tuIdentifier, TmxSegement tuv, String name, String value);

	public abstract void updateTuvAttribute(String tuIdentifier, TmxSegement tuv, String name, String newValue);

	public abstract void deleteTuvAttribute(String tuIdentifier, TmxSegement tuv, String name);

	/**
	 * @return 包含在当显示的记录中
	 * @param tgtLangCode
	 **/
	public abstract List<Integer> getTgtEmptyTU(String tgtLangCode);

	/**
	 * @return 包含在当显示的记录中
	 * @param srcLang
	 * @param tgtLang
	 * @param boolean isIgnoreTag
	 **/
	public abstract List<Integer> getDuplicatedTU(String srcLang, String tgtLang, boolean isIgnoreTag);

	/**
	 * @return 包含在当显示的记录中
	 * @param srcLang
	 * @param tgtLang
	 * @param boolean isIgnoreTag
	 **/
	public abstract List<Integer> getDuplicatedSrcDiffTgtTU(String srcLang, String tgtLang, boolean isIgnoreTag);

	/**
	 * 从物理存储系统中读取 TmxTU
	 * @param tuIdentifier
	 **/
	public abstract TmxTU getTuByIdentifier(String tuIdentifier);
	
	/**
	 * 删除目标语言为空的文本段
	 * 
	 * @param monitor
	 *   :为一个子任务，在函数内执行begin和done
	 * @return ;
	 */
	public  abstract boolean deleteTgtEmpty(IProgressMonitor monitor,boolean ignoreTag);
	
	/**
	 * 删除重复的文本段
	 * @param monitor
	 * :为一个子任务，在函数内执行begin和done
	 * @param ignoreTag
	 * @return ;
	 */
	public abstract boolean deleteDupaicate(IProgressMonitor monitor, boolean ignoreTag,boolean ignoreCase);
	
	
	/**
	 * 删除相同原文不同译文的TU,删除时如果TU中只有2个TUV，则直接删除TU；如果TU中有超过2个TUV则只删除当前TUV
     * @param monitor
	 * :为一个子任务，在函数内执行begin和done
	 * @param ignoreTag
	 *        ：是否忽略标记
	 * @return ;
	 */

	public abstract boolean deleteSameSrcDiffTgt(IProgressMonitor monitor, boolean ignoreTag,boolean ignoreCase);
	
	/**
	 * 删除段末段未空格
	 * @param monitor
	 * :为一个子任务，在函数内执行begin和done
	 * @param ignoreTag
	 * @return ;
	 */
	public abstract boolean deleteEndsSpaces(IProgressMonitor monitor);

	/**
	 * 批量更新 tu 节点属性，如果不存在此属性，则添加。
	 * @param monitor 进度条
	 * @param name 属性名
	 * @param value 新的属性值
	 * @param filter 如果为 {@link TeCoreConstant#FILTERID_allSeg} 则更新整个文件/记忆库，否则只更新当前结果，可以为 null;
	 */
	public abstract void batchUpdateTuAttr(IProgressMonitor monitor, String name, String value, String filter);

	/**
	 * 批量删除 tu 节点属性。
	 * @param monitor 进度条
	 * @param name 属性名
	 * @param filter 如果为 {@link TeCoreConstant#FILTERID_allSeg} 则更新整个文件/记忆库，否则只更新当前结果，可以为 null;
	 */
	public abstract void batchDeleteTuAttr(IProgressMonitor monitor, String name, String filter);

	/**
	 * 批量添加 prop 节点，为所有 tu 添加此 prop。
	 * @param monitor 进度条
	 * @param type prop 节点的 type 属性值
	 * @param content prop 节点的内容
	 * @param filter 如果为 {@link TeCoreConstant#FILTERID_allSeg} 则更新整个文件/记忆库，否则只更新当前结果，可以为 null;
	 */
	public abstract void batchAddTmxProp(IProgressMonitor monitor, String type, String content, String filter);

	/**
	 * 批量更新 prop 节点
	 * @param monitor 进度条
	 * @param prop 原 {@link TmxProp} 节点
	 * @param propType 更新后的 prop type 属性值
	 * @param content 更新后的 prop 节点内容
	 * @param filter 如果为 {@link TeCoreConstant#FILTERID_allSeg} 则更新整个文件/记忆库，否则只更新当前结果，可以为 null;
	 */
	public abstract void batchUpdateTmxProp(IProgressMonitor monitor, TmxProp prop, String propType, String content,
			String filter);

	/**
	 * 批量删除 prop 节点。
	 * @param monitor 进度条
	 * @param prop 需删除的 {@link TmxProp} 节点
	 * @param filter 如果为 {@link TeCoreConstant#FILTERID_allSeg} 则更新整个文件/记忆库，否则只更新当前结果，可以为 null;
	 */
	public abstract void batchDeleteTmxProp(IProgressMonitor monitor, TmxProp prop, String filter);
	public void batchDeleteTmxPropByType(IProgressMonitor monitor, String name, String filter) {}

	/**
	 * 批量更新 tuv 属性，如果不存在此属性，则添加。
	 * @param monitor 进度条
	 * @param name 属性名
	 * @param value 新的属性值
	 * @param langs tuv 的语言代码集合
	 * @param filter 如果为 {@link TeCoreConstant#FILTERID_allSeg} 则更新整个文件/记忆库，否则只更新当前结果，可以为 null;
	 */
	public abstract void batchUpdateTuvAttr(IProgressMonitor monitor, String name, String value, List<String> langs,
			String filter);

	/**
	 * 批量删除 tuv 属性。
	 * @param monitor 进度条
	 * @param name 属性名
	 * @param langs 属性值
	 * @param filter 如果为 {@link TeCoreConstant#FILTERID_allSeg} 则更新整个文件/记忆库，否则只更新当前结果，可以为 null;
	 */
	public abstract void batchDeleteTuvAttr(IProgressMonitor monitor, String name, List<String> langs, String filter);

	/**
	 * 批量添加 note 节点。
	 * @param monitor 进度条
	 * @param content note节点内容
	 * @param filter 如果为 {@link TeCoreConstant#FILTERID_allSeg} 则更新整个文件/记忆库，否则只更新当前结果，可以为 null;
	 */
	public abstract void batchAddTmxNote(IProgressMonitor monitor, String content, String filter);

	/**
	 * 批量更新 note 节点。
	 * @param monitor 进度条
	 * @param oldContent 原 note 内容
	 * @param newContent 新 note 内容
	 * @param filter 如果为 {@link TeCoreConstant#FILTERID_allSeg} 则更新整个文件/记忆库，否则只更新当前结果，可以为 null;
	 */
	public abstract void batchUpdateTmxNote(IProgressMonitor monitor, String oldContent, String newContent, String filter);

	/**
	 * 批量删除 note 节点。
	 * @param monitor 进度条
	 * @param content note 节点内容
	 * @param filter 如果为 {@link TeCoreConstant#FILTERID_allSeg} 则更新整个文件/记忆库，否则只更新当前结果，可以为 null;
	 */
	public abstract void batchDeleteTmxNote(IProgressMonitor monitor, String content, String filter);
	
	/**
	 * 开始进行品质检查	--robert	2013-09-18
	 */
	public abstract void beginQA(String srcLangCode, String tgtLangCode, boolean ignoreTag, boolean ignoreCase);
	
	public abstract boolean isReadOnly();
	/**
	 * 更新缓存中 {@link TmxTU} 的内容。
	 * @param tu tu 引用
	 * @param name 属性名
	 * @param newValue 新的属性值;
	 */
	public void updateCacheTuAttr(TmxTU tu, String name, String newValue) {
		if ("creationid".equals(name)) {
			tu.setCreationUser(newValue);
		} else if ("creationdate".equals(name)) {
			tu.setCreationDate(newValue);
		} else if ("changedate".equals(name)) {
			tu.setChangeDate(newValue);
		} else if ("changeid".equals(name)) {
			tu.setChangeUser(newValue);
		} else if ("creationtool".equals(name)) {
			tu.setCreationTool(newValue);
		} else if ("creationtoolversion".equals(name)) {
			tu.setCreationToolVersion(newValue);
		} else if ("tuid".equals(name)) {
			tu.setTuId(newValue);
		}
	}

	/**
	 * 更新缓存中的 {@link TmxSegement} 内容。
	 * @param tuv tuv 引用
	 * @param name 属性名
	 * @param newValue 新的属性值;
	 */
	public void updateCacheTuvAttr(TmxSegement tuv, String name, String newValue) {
		if ("creationid".equals(name)) {
			tuv.setCreationUser(newValue);
		} else if ("creationdate".equals(name)) {
			tuv.setCreationDate(newValue);
		} else if ("changedate".equals(name)) {
			tuv.setChangeDate(newValue);
		} else if ("changeid".equals(name)) {
			tuv.setChangeUser(newValue);
		} else if ("creationtool".equals(name)) {
			tuv.setCreationTool(newValue);
		} else if ("creationtoolversion".equals(name)) {
			tuv.setCreationToolVersion(newValue);
		}
	}
	
	/**
	 * 将内容写入文件
	 * @param string
	 * @throws UnsupportedEncodingException
	 * @throws IOException ;
	 */
	public void writeString(FileOutputStream os, String string, String encoding) throws UnsupportedEncodingException, IOException {
		os.write(string.getBytes(encoding));
	}

	public void updateTuPropType(Map<String, TmxTU> tus, TmxProp prop, String name) {
	}

	public void batchUpdateTmxPropType(IProgressMonitor monitor, TmxProp prop, String name, String filter) {
		
	}
}
