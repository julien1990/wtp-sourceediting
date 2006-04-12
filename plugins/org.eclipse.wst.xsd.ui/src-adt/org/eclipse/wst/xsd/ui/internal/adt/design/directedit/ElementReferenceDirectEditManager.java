package org.eclipse.wst.xsd.ui.internal.adt.design.directedit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Label;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.common.ui.internal.search.dialogs.ComponentSpecification;
import org.eclipse.wst.xsd.ui.internal.adt.edit.ComponentReferenceEditManager;
import org.eclipse.wst.xsd.ui.internal.adt.editor.Messages;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IField;
import org.eclipse.wst.xsd.ui.internal.editor.XSDElementReferenceEditManager;

public class ElementReferenceDirectEditManager extends ComboBoxCellEditorManager
{
  protected AbstractGraphicalEditPart editPart;
  protected IField setObject;

  public ElementReferenceDirectEditManager(IField parameter, AbstractGraphicalEditPart source, Label label)
  {
    super(source, label);
    editPart = source;
    setObject = parameter;
  }

  protected CellEditor createCellEditorOn(Composite composite)
  {
    return super.createCellEditorOn(composite);
  }

  protected List computeComboContent()
  {
    List list = new ArrayList();
    ComponentReferenceEditManager editManager = getComponentReferenceEditManager();
    if (editManager != null)
    {
       list.add(Messages._UI_ACTION_BROWSE);
       list.add(Messages._UI_ACTION_NEW);
       ComponentSpecification[] quickPicks = editManager.getQuickPicks();
       if (quickPicks != null)
       {
         for (int i=0; i < quickPicks.length; i++)
         {
           ComponentSpecification componentSpecification = quickPicks[i];
           list.add(componentSpecification.getName());
         }  
       }
       ComponentSpecification[] history = editManager.getHistory();
       if (history != null)
       {
         for (int i=0; i < history.length; i++)
         {
           ComponentSpecification componentSpecification = history[i];
           list.add(componentSpecification.getName());
         }  
       }
    } 
    return list; 
  }

  protected ComponentSpecification getComponentSpecificationForValue(String value)
  {
    ComponentReferenceEditManager editManager = getComponentReferenceEditManager();
    if (editManager != null)
    {  
      ComponentSpecification[] quickPicks = editManager.getQuickPicks();
      if (quickPicks != null)
      {
        for (int i=0; i < quickPicks.length; i++)
        {
          ComponentSpecification componentSpecification = quickPicks[i];
          if (value.equals(componentSpecification.getName()))
          {
            return componentSpecification;
          }                
        }  
      }
      ComponentSpecification[] history = editManager.getHistory();
      if (history != null)
      {
        for (int i=0; i < history.length; i++)
        {
          ComponentSpecification componentSpecification = history[i];
          if (value.equals(componentSpecification.getName()))
          {  
            return componentSpecification;
          }
        }  
      }
    }
    return null;
  }
  
  public void performModify(Object value)
  {
    ComponentReferenceEditManager editManager = getComponentReferenceEditManager();
    if (editManager == null)
    {
      return;
    }
    
    // our crude combo box can only work with 'String' objects
    // if we get a String back we need to do some clever mapping to get the ComponentSpecification 
    //    
    if (value instanceof String)
    {
      value = getComponentSpecificationForValue((String)value);     
    }  
    // we assume the selected value is always of the form of a ComponentSpecification
    // 
    if (value instanceof ComponentSpecification)      
    {
      // we need to perform an asyncExec here since the 'host' editpart may be
      // removed as a side effect of performing the action           
      DelayedSetElementReferenceRunnable runnable = new DelayedSetElementReferenceRunnable(editManager, setObject, (ComponentSpecification)value);
      //runnable.run();
      Display.getCurrent().asyncExec(runnable);
    }
  }

  protected List computeSortedList(List list)
  {
    // return TypesHelper.sortList(list);
    return list;
  }
  
  protected CellEditor createCellEditor(Composite composite, String[] stringArray)
  {
    ADTComboBoxCellEditor cellEditor = new ADTComboBoxCellEditor(composite, stringArray, getComponentReferenceEditManager());
    //((ADTComboBoxCellEditor) cellEditor).setObjectToModify(setObject);
    return cellEditor;
  }

  protected ComponentReferenceEditManager getComponentReferenceEditManager()
  {
    ComponentReferenceEditManager result = null;
    IEditorPart editor = getActiveEditor();
    if (editor != null)
    {
      result = (ComponentReferenceEditManager)editor.getAdapter(XSDElementReferenceEditManager.class);
    }  
    return result;
  }
  
  private IEditorPart getActiveEditor()
  {
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
    IEditorPart editorPart = workbenchWindow.getActivePage().getActiveEditor();
    return editorPart;
  }    
  
  protected static class DelayedSetElementReferenceRunnable implements Runnable
  {
    protected ComponentReferenceEditManager componentReferenceEditManager;
    protected ComponentSpecification newValue;
    protected IField field;

    public DelayedSetElementReferenceRunnable(ComponentReferenceEditManager componentReferenceEditManager,
    		IField setObject, ComponentSpecification selectedValue)
    {
      this.componentReferenceEditManager = componentReferenceEditManager;
      newValue = selectedValue;
      field = setObject;
    }

    public void run()
    {
      componentReferenceEditManager.modifyComponentReference(field, newValue);
    }
  }
}
