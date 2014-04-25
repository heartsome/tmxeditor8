package net.heartsome.cat.te.core.qa;

/**
 * te 品质检查的父类
 * @author  robert	2013-09-17
 * @version 
 * @since   JDK1.6
 */
public abstract class QARealization {
	protected QAModel model;
	private QAControl qaControl;
	
	/** 开始　tmx 的品质检查 */
	public abstract void beginTmxQA(QATUDataBean tu);
	/** 开始　数据库　的品质检查 */
	public abstract void beginDBQA();
	
	public void printResult(QAResultBean data){
		qaControl.storeQAData(data);
	}
	
	
	public QAModel getModel() {
		return model;
	}
	public void setModel(QAModel model) {
		this.model = model;
	}
	public QAControl getQaControl() {
		return qaControl;
	}
	public void setQaControl(QAControl qaControl) {
		this.qaControl = qaControl;
	}
	
	

}
