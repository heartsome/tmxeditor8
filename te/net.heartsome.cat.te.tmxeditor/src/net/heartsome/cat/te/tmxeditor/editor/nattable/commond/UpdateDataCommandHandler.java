/**
 * UpdateDataCommandHandler.java
 *
 * Version information :
 *
 * Date:2013-6-4
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.te.tmxeditor.editor.nattable.commond;

import net.heartsome.cat.te.tmxeditor.editor.nattable.TmxEditorImpWithNattable.BodyLayer;
import net.heartsome.cat.te.tmxeditor.editor.nattable.undo.UpdateDataOperation;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.command.AbstractLayerCommandHandler;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.ui.PlatformUI;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class UpdateDataCommandHandler extends AbstractLayerCommandHandler<UpdateDataCommand> {

	private final NatTable table;
	private final BodyLayer bodyLayerStack;

	public UpdateDataCommandHandler(NatTable table, BodyLayer bodyLayerStack) {
		this.table = table;
		this.bodyLayerStack = bodyLayerStack;
	}

	public Class<UpdateDataCommand> getCommandClass() {
		return UpdateDataCommand.class;
	}

	@Override
	protected boolean doCommand(UpdateDataCommand command) {
		IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
		try {
			UpdateDataOperation op = new UpdateDataOperation(table, bodyLayerStack, command);
			op.addContext(PlatformUI.getWorkbench().getOperationSupport().getUndoContext());
			operationHistory.execute(op, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return true;
	}
}
