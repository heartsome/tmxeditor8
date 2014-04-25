package net.heartsome.cat.te.core.qa;

/**
 * 品质检查结果视图的类的接口，添加此接口的主要原因，是通过　qaControl 类来控制　QAResultViewer　类的。因为他们不在同一个包。
 * @author  robert	2013-09-29
 * @version 
 * @since   JDK1.6
 */
public interface IQAResultViewer {
	/** 与　{@link #TeCoreConstant.QAResultViewer_ID} 保持一致 */
	public static final String ID = "net.heartsome.cat.te.tmxeditor.QAResultViewer";
	
	
	public void registLister(QAControl control);
	
	/**
	 * 清除结果显示视图的列表中的数据
	 */
	public void clearTableData();
	
	
}
