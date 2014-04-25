package net.heartsome.cat.ts.ui.rtf.exporter;

import net.sourceforge.rtf.format.rtfcode.RTFCodeString;

/**
 * 导出 RTF 文件时所用到的 Bean 类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class Export {

	/** xliff 中的 trans-unit id */
	private String id;
	
	/** 源语言文本 */
	private RTFCodeString source;
	
	/** 目标语言文本 */
	private RTFCodeString target;
	
	/** 文本段状态 */
	private String status;
	
	/** 文本段批注 */
	private RTFCodeString comment;

	/**
	 * 无参构造方法
	 */
	public Export() {
		
	}

	/**
	 * 构造方法
	 * @param id
	 * @param source
	 * @param target
	 */
	public Export(String id, RTFCodeString source, RTFCodeString target) {
		this.id = id;
		this.source = source;
		this.target = target;
	}
	
	/**
	 * 构造方法
	 * @param id
	 * @param source
	 * @param target
	 * @param status
	 * @param comment
	 */
	public Export(String id, RTFCodeString source, RTFCodeString target, String status, RTFCodeString comment) {
		this(id, source, target);
		this.status = status;
		this.comment = comment;
	}

	/**
	 * 获取 id 值（xliff 中的 trans-unit id）
	 * @return ;
	 */
	public String getId() {
		return id;
	}

	/**
	 * 设置 id（xliff 中的 trans-unit id）
	 * @param id ;
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 获取源文本的 RTFCodeString 对象
	 * @return ;
	 */
	public RTFCodeString getSource() {
		return source;
	}

	/**
	 * 设置源文本的 RTFCodeString 对象
	 * @param source ;
	 */
	public void setSource(RTFCodeString source) {
		this.source = source;
	}

	/**
	 * 获取目标文本的 RTFCodeString 对象
	 * @return ;
	 */
	public RTFCodeString getTarget() {
		return target;
	}

	/**
	 * 设置目标文本的 RTFCodeString 对象
	 * @param target ;
	 */
	public void setTarget(RTFCodeString target) {
		this.target = target;
	}

	/**
	 * 获取文本段状态
	 * @return ;
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * 设置文本段状态
	 * @param status ;
	 */
	public void setStatus(String status) {
		this.status = status == null ? "" : status;
	}

	/**
	 * 获取文本段批注
	 * @return ;
	 */
	public RTFCodeString getComment() {
		return comment;
	}

	/**
	 * 设置文本段批注
	 * @param comment ;
	 */
	public void setComment(RTFCodeString comment) {
		this.comment = comment;
	}
	
}
