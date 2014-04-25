/**
 * MergeTmxFileHanlder.java
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

package net.heartsome.cat.te.ui.mergetmx.handler;

import net.heartsome.cat.te.ui.mergetmx.ui.MergeTmxDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 分割 tmx 文件，注意，该类与　类 net.heartsome.cat.te.core.file.merge.MergeFilesWhenCloseTmx 的区别在于， 后者是打开关闭　tmx 文件时的一个附属操作，而当前类是一个独立的操作
 * @author  robert	2013-08-12
 * @version 
 * @since   JDK1.6
 */
public class MergeTmxHanlder extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MergeTmxDialog dialog = new MergeTmxDialog(HandlerUtil.getActiveShell(event));
		dialog.open();
		
		return null;
	}
}
