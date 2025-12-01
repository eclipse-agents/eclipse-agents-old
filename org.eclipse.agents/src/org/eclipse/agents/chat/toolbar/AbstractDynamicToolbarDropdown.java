/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.agents.chat.toolbar;


import org.eclipse.agents.chat.ChatView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;

public abstract class AbstractDynamicToolbarDropdown extends Action implements IMenuCreator {
    
	private Menu menu;
    private String text;
    private ChatView view;

    public AbstractDynamicToolbarDropdown(String name, String tooltip, ChatView view) {
        // Set the style to pulldown to get the drop-down arrow
        super(name, IAction.AS_DROP_DOWN_MENU);
        
        this.text = name;
        this.view = view;
        setToolTipText(tooltip);
        setMenuCreator(this);
        
    }
    
    @Override
    public void runWithEvent(Event event) {
    	// handles when the button itself is clicked, rather than drop-down triangle
    	if (event.widget instanceof ToolItem) {
            ToolItem toolItem = (ToolItem) event.widget;
            Menu menu = getMenu(toolItem.getParent());
            
            // Get the bounds of the tool item to position the menu.
            Rectangle bounds = toolItem.getBounds();
            Point topLeft = new Point(bounds.x, bounds.y + bounds.height);
            topLeft = toolItem.getParent().toDisplay(topLeft);
            menu.setLocation(topLeft.x, topLeft.y);
            menu.setVisible(true);
        }
    }

    
    // --- IMenuCreator methods for the dynamic drop-down ---

    @Override
    public Menu getMenu(Control parent) {
        if (menu != null) {
            menu.dispose();
        }
        MenuManager menuManager = new MenuManager();
        fillMenu(menuManager);
        menu = menuManager.createContextMenu(parent);
        return menu;
    }
    
    @Override
    public Menu getMenu(Menu parent) {
        // Not used, but must be implemented
        return null;
    }
    
    @Override
    public void dispose() {
        if (menu != null) {
            menu.dispose();
            menu = null;
        }
    }

    // --- Dynamic menu item logic ---
    
    protected abstract void fillMenu(MenuManager menuManager);// {
//        // Add dynamic menu items here.
//        // For example, based on the current time or other application state.
//        
//        String currentTime = DateFormat.getTimeInstance().format(new Date());
//        
//        // Add a push action with a dynamic label
//        IAction dynamicAction1 = new Action("Action created at " + currentTime) {
//            @Override
//            public void run() {
//                MessageDialog.openInformation(null, "Dynamic Menu", "You clicked the first dynamic item.");
//                DynamicToolbarDropdown.this.text = "1";
//                toolbarManager.update(true);
//            }
//        };
//        
//        // Add another action
//        IAction dynamicAction2 = new Action("Another action") {
//            @Override
//            public void run() {
//                MessageDialog.openInformation(null, "Dynamic Menu", "You clicked the second dynamic item.");
//                DynamicToolbarDropdown.this.text = "22222";
//                toolbarManager.update(true);
//            }
//        };
//
//        menuManager.add(dynamicAction1);
//        menuManager.add(dynamicAction2);
//    }
    
    // --- Dynamic button label logic ---

    public void updateText(String text) {
    	setText(text);
    	view.getViewSite().getActionBars().getToolBarManager().update(true);
    }
   
    public void setText(String text) {
    	this.text = text;
    }

    // Override the getText method to provide a dynamic label for the button.
    @Override
    public String getText() {
        return text;
    }
    
    protected ChatView getView() {
    	return view;
    }
}
