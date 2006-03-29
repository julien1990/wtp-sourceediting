/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xsd.adt.design;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.wst.xsd.adt.actions.BaseSelectionAction;
import org.eclipse.wst.xsd.adt.design.editparts.model.IActionProvider;


public class DesignViewContextMenuProvider extends ContextMenuProvider
{
  IEditorPart editor;  
  ISelectionProvider selectionProvider;

  /**
   * Constructor for GraphContextMenuProvider.
   * 
   * @param selectionProvider
   * @param editor
   */
  public DesignViewContextMenuProvider(IEditorPart editor, EditPartViewer viewer, ISelectionProvider selectionProvider)
  {
    super(viewer);
    this.editor = editor;
    this.selectionProvider = selectionProvider;
  }

  /**
   * @see org.eclipse.gef.ui.parts.ContextMenuProvider#buildContextMenu(org.eclipse.jface.action.IMenuManager,
   *      org.eclipse.gef.EditPartViewer)
   */
  public void buildContextMenu(IMenuManager menu)
  {
    IMenuManager currentMenu = menu;
    menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    ActionRegistry registry = getEditorActionRegistry();
    ISelection selection = selectionProvider.getSelection();
    
    if (selection != null)
    {
      Object selectedObject = ((StructuredSelection) selection).getFirstElement();
      
      // Convert editparts to model objects as selections 
      if (selectedObject instanceof EditPart)
      {
        selectedObject = ((EditPart)selectedObject).getModel();
      }

      if (selectedObject instanceof IActionProvider)
      {
        IActionProvider actionProvider = (IActionProvider) selectedObject;

        String[] actions = actionProvider.getActions(null);
        for (int i = 0; i < actions.length; i++)
        {
          String id = actions[i];
          if (id.startsWith(BaseSelectionAction.SUBMENU_START_ID))
          {
            String text = id.substring(BaseSelectionAction.SUBMENU_START_ID.length());
            IMenuManager subMenu = new MenuManager(text);
            currentMenu.add(subMenu);
            currentMenu = subMenu;
          }
          else if (id.startsWith(BaseSelectionAction.SUBMENU_END_ID))
          {
            currentMenu = getParentMenu(menu, currentMenu);
          }
          else if (id.equals(BaseSelectionAction.SEPARATOR_ID))
          {
            currentMenu.add(new Separator());
          }
          else
          {
            IAction action = registry.getAction(id);
            if (action != null)
            { 
              action.isEnabled();
              currentMenu.add(action);
            }
          }
        }
        menu.add(new Separator());       
        menu.add(new Separator("refactoring-slot")); 
        menu.add(new Separator());       
        menu.add(new Separator("search-slot"));       
        menu.add(new Separator());
      }
    }    
    menu.add(new Separator());
    //menu.add(registry.getAction("org.eclipse.wst.xsd.DeleteAction"));
    //menu.add(new Separator());
    //ShowPropertiesViewAction showPropertiesAction = (ShowPropertiesViewAction) registry.getAction(ShowPropertiesViewAction.ACTION_ID);
    //showPropertiesAction.setPage(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
    //menu.add(showPropertiesAction);
  }

  protected IMenuManager getParentMenu(IMenuManager root, IMenuManager child) {
    IMenuManager parent = null;
    
    IContributionItem[] kids = root.getItems();
    int index = 0;
    while (index < kids.length && parent == null) {
      IContributionItem item = kids[index];
      if (item.equals(child)) {
        parent = root;
      }
      else {
        if (item instanceof IMenuManager) {
          parent = getParentMenu((IMenuManager) item, child);
        }
      }
      index++;
    }
    
    return parent;
  }

  protected ActionRegistry getEditorActionRegistry()
  {
    return (ActionRegistry) editor.getAdapter(ActionRegistry.class);
  }
  protected CommandStack commandStack;

  protected CommandStack getCommandStack()
  {
    if (commandStack == null)
      commandStack = getViewer().getEditDomain().getCommandStack();
    return commandStack;
  }
}
