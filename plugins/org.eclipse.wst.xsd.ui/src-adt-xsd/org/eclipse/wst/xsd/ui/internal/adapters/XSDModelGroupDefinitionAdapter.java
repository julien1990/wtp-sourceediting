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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xsd.ui.internal.adt.actions.BaseSelectionAction;
import org.eclipse.wst.xsd.ui.internal.adt.actions.ShowPropertiesViewAction;
import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.model.IActionProvider;
import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.model.IGraphElement;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IADTObject;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IADTObjectListener;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IModel;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IStructure;
import org.eclipse.wst.xsd.ui.internal.adt.outline.ITreeElement;
import org.eclipse.wst.xsd.ui.internal.common.actions.AddXSDElementAction;
import org.eclipse.wst.xsd.ui.internal.common.actions.AddXSDModelGroupAction;
import org.eclipse.wst.xsd.ui.internal.common.actions.DeleteXSDConcreteComponentAction;
import org.eclipse.wst.xsd.ui.internal.common.actions.SetMultiplicityAction;
import org.eclipse.wst.xsd.ui.internal.common.commands.DeleteCommand;
import org.eclipse.wst.xsd.ui.internal.editor.Messages;
import org.eclipse.wst.xsd.ui.internal.editor.XSDEditorPlugin;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDModelGroupDefinition;

public class XSDModelGroupDefinitionAdapter extends XSDParticleAdapter implements IStructure, IActionProvider, IGraphElement, IADTObjectListener
{
  public static final Image MODEL_GROUP_ICON = XSDEditorPlugin.getPlugin().getIcon("obj16/XSDGroup.gif"); //$NON-NLS-1$
  public static final Image MODEL_GROUP_DISABLED_ICON = XSDEditorPlugin.getPlugin().getIcon("obj16/XSDGroupdis.gif"); //$NON-NLS-1$
  public static final Image MODEL_GROUP_REF_ICON = XSDEditorPlugin.getPlugin().getIcon("obj16/XSDGroupRef.gif"); //$NON-NLS-1$
  public static final Image MODEL_GROUP_REF_DISABLED_ICON = XSDEditorPlugin.getPlugin().getIcon("obj16/XSDGroupRefdis.gif"); //$NON-NLS-1$

  protected List fields = null;
  protected List otherThingsToListenTo = null;
  
  public XSDModelGroupDefinitionAdapter()
  {
    super();
  }

  public XSDModelGroupDefinition getXSDModelGroupDefinition()
  {
    return (XSDModelGroupDefinition) target;
  }

  public Image getImage()
  {
    XSDModelGroupDefinition xsdModelGroupDefinition = (XSDModelGroupDefinition) target;

    if (xsdModelGroupDefinition.isModelGroupDefinitionReference())
    {
      if (isReadOnly())
      {
        return MODEL_GROUP_REF_DISABLED_ICON;
      }
      return MODEL_GROUP_REF_ICON;
    }
    else
    {
      if (isReadOnly())
      {
        return MODEL_GROUP_DISABLED_ICON;
      }
      return MODEL_GROUP_ICON;
    }
  }

  public String getText()
  {
    XSDModelGroupDefinition xsdModelGroupDefinition = (XSDModelGroupDefinition) target;
    String result = xsdModelGroupDefinition.isModelGroupDefinitionReference() ? xsdModelGroupDefinition.getQName() : xsdModelGroupDefinition.getName();
    return result == null ? Messages._UI_LABEL_ABSENT : result;
  }

  public ITreeElement[] getChildren()
  {
    List list = new ArrayList();
    XSDModelGroup xsdModelGroup = ((XSDModelGroupDefinition) target).getResolvedModelGroupDefinition().getModelGroup();
    if (xsdModelGroup != null)
      list.add(xsdModelGroup);

    List adapterList = new ArrayList();
    populateAdapterList(list, adapterList);
    return (ITreeElement[]) adapterList.toArray(new ITreeElement[0]);

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.wst.xsd.ui.internal.adt.design.editparts.model.IActionProvider#getActions(java.lang.Object)
   */
  public String[] getActions(Object object)
  {
    Collection list = new ArrayList();

    if (!getXSDModelGroupDefinition().isModelGroupDefinitionReference())
    {
      list.add(AddXSDElementAction.ID);
      list.add(AddXSDElementAction.REF_ID);
      list.add(BaseSelectionAction.SEPARATOR_ID);
      list.add(AddXSDModelGroupAction.SEQUENCE_ID);
      list.add(AddXSDModelGroupAction.CHOICE_ID);
    }
    
    list.add(DeleteXSDConcreteComponentAction.DELETE_XSD_COMPONENT_ID);
    
    if (getXSDModelGroupDefinition().isModelGroupDefinitionReference())
    {
      list.add(BaseSelectionAction.SEPARATOR_ID);
      list.add(BaseSelectionAction.SUBMENU_START_ID + Messages._UI_ACTION_SET_MULTIPLICITY);
      list.add(SetMultiplicityAction.REQUIRED_ID);
      list.add(SetMultiplicityAction.ZERO_OR_ONE_ID);
      list.add(SetMultiplicityAction.ZERO_OR_MORE_ID);
      list.add(SetMultiplicityAction.ONE_OR_MORE_ID);    
      list.add(BaseSelectionAction.SUBMENU_END_ID); 
    }
    
    list.add(BaseSelectionAction.SEPARATOR_ID);
    list.add(ShowPropertiesViewAction.ID);

    return (String [])list.toArray(new String[0]);
  }

  public Command getAddNewFieldCommand(String fieldKind)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Command getDeleteCommand()
  {
    return new DeleteCommand("", getXSDModelGroupDefinition()); //$NON-NLS-1$
  }

  // TODO Common this up with XSDComplexType's.  See also getFields 
  protected void clearFields()
  {
    if (otherThingsToListenTo != null)
    {
      for (Iterator i = otherThingsToListenTo.iterator(); i.hasNext();)
      {
        Adapter adapter = (Adapter) i.next();
        if (adapter instanceof IADTObject)
        {
          IADTObject adtObject = (IADTObject) adapter;
          adtObject.unregisterListener(this);
        }
      }
    }
    fields = null;
    otherThingsToListenTo = null;
  }

  public List getFields()
  {
    List fields = new ArrayList();
    otherThingsToListenTo = new ArrayList();
    XSDVisitorForFields visitor = new XSDVisitorForFields();
    visitor.visitModelGroupDefinition(getXSDModelGroupDefinition());
    populateAdapterList(visitor.concreteComponentList, fields);
    
    // TODO (cs) common a base class for a structure thingee
    //
    populateAdapterList(visitor.thingsWeNeedToListenTo, otherThingsToListenTo);
    for (Iterator i = otherThingsToListenTo.iterator(); i.hasNext();)
    {
      Adapter adapter = (Adapter) i.next();
      if (adapter instanceof IADTObject)
      {
        IADTObject adtObject = (IADTObject) adapter;
        adtObject.registerListener(this);
      }
    }
    return fields;
  }

  public IModel getModel()
  {
    Adapter adapter = XSDAdapterFactory.getInstance().adapt(getXSDModelGroupDefinition().getSchema());
    return (IModel)adapter;
  }
  public String getName()
  {
    return getText();
  }

  public boolean isFocusAllowed()
  {
    XSDModelGroupDefinition xsdModelGroupDefinition = (XSDModelGroupDefinition) target;
    if (xsdModelGroupDefinition.isModelGroupDefinitionReference())
    { 
      return false;
    }
    return true;
  }

  public void propertyChanged(Object object, String property)
  {
    clearFields();
    notifyListeners(this, null);
  }
  
  public int getMaxOccurs()
  {
    return getMaxOccurs(getXSDModelGroupDefinition());
  }

  public int getMinOccurs()
  {
    return getMinOccurs(getXSDModelGroupDefinition());
  }

}
