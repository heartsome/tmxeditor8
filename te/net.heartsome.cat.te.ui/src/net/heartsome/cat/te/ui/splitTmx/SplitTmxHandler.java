package net.heartsome.cat.te.ui.splitTmx;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 分割　tmx 文件
 * @author  robert	2013-08-13
 * @version 
 * @since   JDK1.6
 */
public class SplitTmxHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		SplitTmxDialog dialog = new SplitTmxDialog(HandlerUtil.getActiveShell(event));
		dialog.open();
		
		return null;
	}
	

}
