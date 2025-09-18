package org.eclipse.mcp.acp.view;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
* Handles command invocations and routes to 
*/
public class CommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			System.err.println(event);
//			Command command = Command.fromCommand(event.getCommand().getId());
//			IActionDelegate action = command.getDelegate();
//			action.selectionChanged(null, HandlerUtil.getCurrentSelection(event));
//			if (action instanceof IEditorActionDelegate) {
//				((IEditorActionDelegate) action).setActiveEditor(null, HandlerUtil.getActiveEditor(event));
//			}
//			if (action instanceof IObjectActionDelegate) {
//				((IObjectActionDelegate) action).setActivePart(null, HandlerUtil.getActivePart(event));
//			}
//			if (action instanceof IViewActionDelegate) {
//				IWorkbenchPart part = HandlerUtil.getActivePart(event);
//				if (part instanceof IViewPart) {
//					((IViewActionDelegate) action).init((IViewPart)part);
//				}
//			}
//			
////			if (command.equals(Command.COMMENT)) {
////				// Comment is a toggle tool bar button
////				Object trigger = event.getTrigger();
////				if (trigger instanceof Event) {
////					Event e = (Event)trigger;
////					if (e.widget instanceof ToolItem) {
////						if (((ToolItem)e.widget).getSelection()) {
////							((CodeExplanationViewAction)action).setToggleSelected(true);
////						}
////					}
////				}
////			}
//			
//			action.run(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
