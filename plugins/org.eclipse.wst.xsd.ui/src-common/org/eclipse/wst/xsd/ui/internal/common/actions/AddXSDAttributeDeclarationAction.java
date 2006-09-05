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
package org.eclipse.wst.xsd.ui.internal.common.actions;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.xsd.ui.internal.adapters.XSDAdapterFactory;
import org.eclipse.wst.xsd.ui.internal.adapters.XSDBaseAdapter;
import org.eclipse.wst.xsd.ui.internal.common.commands.AddXSDAttributeDeclarationCommand;
import org.eclipse.wst.xsd.ui.internal.common.util.Messages;
import org.eclipse.xsd.XSDAttributeGroupDefinition;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDConcreteComponent;
import org.eclipse.xsd.XSDSchema;

public class AddXSDAttributeDeclarationAction extends XSDBaseAction
{
  public static String ID = "AddXSDAttributeAction"; //$NON-NLS-1$
  public static String REF_ID = "org.eclipse.wst.xsd.ui.internal.common.actions.AddXSDAttributeReferenceAction"; //$NON-NLS-1$
  boolean isReference = false;
  
  public AddXSDAttributeDeclarationAction(IWorkbenchPart part)
  {
    super(part);
    setText(Messages._UI_ACTION_ADD_ATTRIBUTE);
    setId(ID);
    isReference = false;
  }
  
  public AddXSDAttributeDeclarationAction(IWorkbenchPart part, String id, String label, boolean isReference)
  {
    super(part);
    setText(label);
    setId(id);
    this.isReference = isReference;
    doDirectEdit = !isReference;
  }

  public void run()
  {
    Object selection = ((IStructuredSelection) getSelection()).getFirstElement();

    if (selection instanceof XSDBaseAdapter)
    {
      selection = ((XSDBaseAdapter) selection).getTarget();
    }
    AddXSDAttributeDeclarationCommand command = null;
    if (selection instanceof XSDComplexTypeDefinition)
    {
      command = new AddXSDAttributeDeclarationCommand(Messages._UI_ACTION_ADD_ATTRIBUTE, (XSDComplexTypeDefinition) selection);
      command.setReference(isReference);
      getCommandStack().execute(command);
    }
    else if (selection instanceof XSDAttributeUse)
    {
      XSDAttributeUse xsdAttributeUse = (XSDAttributeUse) selection;
      XSDConcreteComponent parent = null;
      XSDComplexTypeDefinition ct = null;
      for (parent = xsdAttributeUse.getContainer(); parent != null;)
      {
        if (parent instanceof XSDComplexTypeDefinition)
        {
          ct = (XSDComplexTypeDefinition) parent;
          break;
        }
        parent = parent.getContainer();
      }
      if (ct != null)
      {
        command = new AddXSDAttributeDeclarationCommand(Messages._UI_ACTION_ADD_ATTRIBUTE, ct);
        command.setReference(isReference);
        getCommandStack().execute(command);
      }
    }
    else if (selection instanceof XSDAttributeGroupDefinition)
    {
      command = new AddXSDAttributeDeclarationCommand(Messages._UI_ACTION_ADD_ATTRIBUTE, (XSDAttributeGroupDefinition)selection);
      command.setReference(isReference);
      getCommandStack().execute(command);
    }
    else if (selection instanceof XSDSchema)
    {
      command = new AddXSDAttributeDeclarationCommand(Messages._UI_ACTION_ADD_ATTRIBUTE, (XSDSchema)selection);
      getCommandStack().execute(command);
    }
    
    if (command != null)
    {
      addedComponent = command.getAddedComponent();
      Adapter adapter = XSDAdapterFactory.getInstance().adapt(addedComponent);
      selectAddedComponent(adapter);
    }

  }
}
