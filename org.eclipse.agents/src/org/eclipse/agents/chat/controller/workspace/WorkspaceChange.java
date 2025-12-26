package org.eclipse.agents.chat.controller.workspace;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.HistoryItem;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class WorkspaceChange {

	//Differencer
	int type;
	
	Path path;
	IFileState state;;
	String originalContent;
	WorkspaceController controller;
	
	public WorkspaceChange(WorkspaceController controller, int type, Path path, IFileState state) {
		this.controller = controller;
		this.type = type;
		this.path = path;
		this.state = state;
		this.originalContent = null;
	}
	
	public WorkspaceChange(WorkspaceController controller, int type, Path path, String originalContent) {
		this.controller = controller;
		this.type = type;
		this.path = path;
		this.state = null;
		this.originalContent = originalContent;
	}
	
	public int getKind() {
		return type;
	}
	
	public ImageDescriptor getPathImageDescriptor() {
        IFile file = WorkspaceController.findFile(path);
        if (file != null) {
        	IEditorDescriptor editorDescriptor = IDE.getDefaultEditor(file);
        	if (editorDescriptor != null) {
        		return editorDescriptor.getImageDescriptor();
        	}
        }
        return null;
	}
	
	public String getName() {
		return path.toFile().getName();
	}
	
	public void synchronize() {
		
//		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		ICommandService commandService = window.getService(ICommandService.class);
//		IHandlerService handlerService = window.getService(IHandlerService.class);
//		String commandId = "org.eclipse.team.ui.synchronizeAll"; 
//
//		   
//        try {
//			ParameterizedCommand pc = new ParameterizedCommand(commandService.getCommand(commandId), null);
//			Object result = handlerService.executeCommand(pc, null);
//			System.out.println(result);
//			
//			IServiceLocator locator = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		    IMenuService menuService = locator.getService(IMenuService.class);
//		    
//		    // 1. Create a context and explicitly set your projects as the selection
//		    // Expressions like <iterate> or <adapt> check this 'default variable'
//		    IEvaluationContext context = new EvaluationContext(null, Arrays.asList(selectedProjects));
//		    
//		    // 2. Also set the specific variable names expected by many 'visibleWhen' clauses
//		    context.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, Arrays.asList(selectedProjects));
//		    context.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, Arrays.asList(selectedProjects));
//
//		    // 3. Create the menu manager for the "Compare With" location
//		    MenuManager tempMenu = new MenuManager();
//		    String location = "popup:compareWithMenu";
//
//		    // 4. Use the version of the method that accepts your specific context
//		    // This ensures visibility is calculated based on selectedProjects, not the global UI state
//		    menuService.populateContributionManager(tempMenu, location, context, true);
//
//		    IContributionItem[] items = tempMenu.getItems();
//		    
//		    // Clean up
//		    menuService.releaseContributions(tempMenu);
//		    
//		    return Arrays.asList(items);
//
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		} catch (NotDefinedException e) {
//			e.printStackTrace();
//		} catch (NotEnabledException e) {
//			e.printStackTrace();
//		} catch (NotHandledException e) {
//			e.printStackTrace();
//		}

//		IWizard wizard = new GlobalSynchronizeWizard();
//		WizardDialog dialog = new WizardDialog(Activator.getDisplay().getActiveShell(), wizard);
//		dialog.open();

	}

	
	public void review() {
		IFile file = WorkspaceController.findFile(path);
		ITextEditor editor = WorkspaceController.findFileEditor(path);
		ITypedElement left = null, right = null;

		if (editor != null) {
			left = new LocalResourceTypedElement(file);
		} else if (file != null){
			left = new ResourceNode(file);
		} else {
			//TODO
		}

		if (state != null) {
			right = new HistoryItem(left, state);
		} else if (originalContent != null) {
			right = new StringNode(file, originalContent);
		} else {
			//TODO
		}
		
		if (left !=null && right != null) {
			DiffNode node = new DiffNode(left, right);
			CompareConfiguration configuration = new CompareConfiguration();
			configuration.setLeftLabel("Agent Changes");
			configuration.setRightLabel("Original");
			configuration.setLeftEditable(true);
			configuration.setRightEditable(false);
	
			WorkspaceCompareInput input = new WorkspaceCompareInput(configuration, node);
			CompareUI.openCompareEditor(input);
		} else {
			
		}
	}

	public void remove() {
		controller.removeChange(this);
	}
	
	/**
	 * Use the controller to modify the editor or file based on current Workbench state.
	 * This will update the existing WorkspaceChange for the path
	 * After we will remove the WorkspaceChange from the controller
	 */
	public void revert() {
		if (originalContent == null) {
			//TODO
		} else {
			ITextEditor editor = WorkspaceController.findFileEditor(path);
			if (editor != null) {
				controller.writeToEditor(path, editor, originalContent);
			} else {
				controller.writeToFile(path, originalContent);
			}
		}
		controller.removeChange(this);
		
	}
}
