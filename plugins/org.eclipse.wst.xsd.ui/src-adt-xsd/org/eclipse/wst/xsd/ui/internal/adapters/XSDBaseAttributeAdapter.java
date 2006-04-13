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
package org.eclipse.wst.xsd.ui.internal.adapters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xsd.ui.internal.adt.actions.BaseSelectionAction;
import org.eclipse.wst.xsd.ui.internal.adt.actions.ShowPropertiesViewAction;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IField;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IModel;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IType;
import org.eclipse.wst.xsd.ui.internal.common.actions.AddXSDAttributeDeclarationAction;
import org.eclipse.wst.xsd.ui.internal.common.actions.DeleteXSDConcreteComponentAction;
import org.eclipse.wst.xsd.ui.internal.common.commands.DeleteCommand;
import org.eclipse.wst.xsd.ui.internal.common.commands.UpdateNameCommand;
import org.eclipse.wst.xsd.ui.internal.editor.Messages;
import org.eclipse.wst.xsd.ui.internal.editor.XSDEditorPlugin;
import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;

// a base adapter for reuse by an AttributeUse and AttributeDeclaration
//
public abstract class XSDBaseAttributeAdapter extends XSDBaseAdapter implements IField
{
  protected abstract XSDAttributeDeclaration getXSDAttributeDeclaration();
  protected abstract XSDAttributeDeclaration getResolvedXSDAttributeDeclaration();

  public XSDBaseAttributeAdapter()
  {
    super();
  }

  public String[] getActions(Object object)
  {
    List list = new ArrayList();
    list.add(AddXSDAttributeDeclarationAction.ID);
    list.add(BaseSelectionAction.SEPARATOR_ID);
    list.add(DeleteXSDConcreteComponentAction.DELETE_XSD_COMPONENT_ID);

    list.add(BaseSelectionAction.SEPARATOR_ID);
    list.add(ShowPropertiesViewAction.ID);
    return (String[]) list.toArray(new String[0]);
  }

  public Command getDeleteCommand()
  {
    return new DeleteCommand("", getXSDAttributeDeclaration()); //$NON-NLS-1$
  }

  public String getKind()
  {
    return "attribute"; //$NON-NLS-1$
  }

  public int getMaxOccurs()
  {
    // TODO Auto-generated method stub
    return -3;
  }

  public int getMinOccurs()
  {
    // TODO Auto-generated method stub
    return -3;
  }

  public String getName()
  {
    XSDAttributeDeclaration resolvedAttributeDeclaration = getResolvedXSDAttributeDeclaration();
    String name = resolvedAttributeDeclaration.getName();
    return (name == null) ? "" : name; //$NON-NLS-1$
  }

  public IType getType()
  {
    XSDTypeDefinition td = getResolvedXSDAttributeDeclaration().getTypeDefinition();
    return (td != null) ? (IType) XSDAdapterFactory.getInstance().adapt(td) : null;
  }

  public String getTypeName()
  {
    XSDTypeDefinition td = getResolvedXSDAttributeDeclaration().getTypeDefinition();
    return (td != null) ? td.getName() : Messages._UI_NO_TYPE_DEFINED;
  }

  public String getTypeNameQualifier()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Command getUpdateMaxOccursCommand(int maxOccurs)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Command getUpdateMinOccursCommand(int minOccurs)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Command getUpdateNameCommand(String name)
  {
    return new UpdateNameCommand(Messages._UI_ACTION_UPDATE_NAME, getResolvedXSDAttributeDeclaration(), name);
  }

  public Command getUpdateTypeNameCommand(String typeName, String quailifier)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.wst.xsd.ui.internal.adt.outline.ITreeElement#getImage()
   */
  public Image getImage()
  {
    XSDAttributeDeclaration xsdAttributeDeclaration = getXSDAttributeDeclaration();  // don't want the resolved attribute
    if (xsdAttributeDeclaration.isAttributeDeclarationReference())
    {
      return XSDEditorPlugin.getXSDImage("icons/XSDAttributeRef.gif"); //$NON-NLS-1$
    }
    else
    {
      return XSDEditorPlugin.getXSDImage("icons/XSDAttribute.gif"); //$NON-NLS-1$
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.wst.xsd.ui.internal.adt.outline.ITreeElement#getText()
   */
  public String getText()
  {
    return getTextForAttribute(getResolvedXSDAttributeDeclaration(), true);
  }

  public String getTextForAttribute(XSDAttributeDeclaration ad, boolean showType)
  {
    ad = ad.getResolvedAttributeDeclaration();
    String name = ad.getName();
    StringBuffer result = new StringBuffer();
    if (name == null)
    {
      result.append(" " + Messages._UI_LABEL_ABSENT + " ");  //$NON-NLS-1$ //$NON-NLS-2$
    }
    else
    {
      result.append(name);
    }
    if (ad.getAnonymousTypeDefinition() == null && ad.getTypeDefinition() != null)
    {
      result.append(" : "); //$NON-NLS-1$
      // result.append(resolvedAttributeDeclaration.getTypeDefinition().getQName(xsdAttributeDeclaration));
      result.append(ad.getTypeDefinition().getName());
    }
    return result.toString();
  }

  public boolean isGlobal()
  {
    return false;
  }
  
  public boolean isReference()
  {
    return false;
  }
  
  public IModel getModel()
  {
    Adapter adapter = XSDAdapterFactory.getInstance().adapt(getXSDAttributeDeclaration().getSchema());
    return (IModel)adapter;
  }  

}

