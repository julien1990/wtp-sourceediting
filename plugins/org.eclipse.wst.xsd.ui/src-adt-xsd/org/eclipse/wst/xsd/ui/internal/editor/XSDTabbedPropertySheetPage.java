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
package org.eclipse.wst.xsd.ui.internal.editor;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.xsd.ui.internal.adapters.XSDAdapterFactory;
import org.eclipse.wst.xsd.ui.internal.adapters.XSDBaseAdapter;
import org.eclipse.wst.xsd.ui.internal.adapters.XSDElementDeclarationAdapter;
import org.eclipse.wst.xsd.ui.internal.adapters.XSDParticleAdapter;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IADTObjectListener;
import org.eclipse.wst.xsd.ui.internal.text.XSDModelAdapter;
import org.eclipse.xsd.XSDConcreteComponent;
import org.eclipse.xsd.XSDElementDeclaration;

public class XSDTabbedPropertySheetPage extends TabbedPropertySheetPage implements IADTObjectListener
{
  XSDBaseAdapter oldSelection;
  XSDModelAdapter xsdModelAdapter;
  public XSDTabbedPropertySheetPage(ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor)
  {
    super(tabbedPropertySheetPageContributor);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IWorkbenchPart part, ISelection selection) {

      Object selected = ((StructuredSelection)selection).getFirstElement();
      if (selected instanceof XSDBaseAdapter)
      {
        XSDBaseAdapter adapter = (XSDBaseAdapter)selected;
        if (oldSelection != null)
        {
          oldSelection.unregisterListener(this);
          if (oldSelection instanceof XSDElementDeclarationAdapter)
          {
            XSDElementDeclaration elem = (XSDElementDeclaration)((XSDElementDeclarationAdapter)oldSelection).getTarget();
            if (elem.getContainer() != null)
            {
              Adapter adap = XSDAdapterFactory.getInstance().adapt(elem.getContainer());
              if (adap instanceof XSDParticleAdapter)
              {
                XSDParticleAdapter particleAdapter = (XSDParticleAdapter)adap;
                particleAdapter.unregisterListener(this);
              }
            }
            if (elem.isElementDeclarationReference())
            {
              XSDElementDeclarationAdapter resolvedElementAdapter = (XSDElementDeclarationAdapter)XSDAdapterFactory.getInstance().adapt(elem.getResolvedElementDeclaration());
              resolvedElementAdapter.unregisterListener(this);
            }
          }
        }
        if (adapter instanceof XSDElementDeclarationAdapter)
        {
          XSDElementDeclaration elem = (XSDElementDeclaration)((XSDElementDeclarationAdapter)adapter).getTarget();
          Adapter adap = XSDAdapterFactory.getInstance().adapt(elem.getContainer());
          if (adap instanceof XSDParticleAdapter)
          {
            XSDParticleAdapter particleAdapter = (XSDParticleAdapter)adap;
            particleAdapter.registerListener(this);
          }
          if (elem.isElementDeclarationReference())
          {
            XSDElementDeclarationAdapter resolvedElementAdapter = (XSDElementDeclarationAdapter)XSDAdapterFactory.getInstance().adapt(elem.getResolvedElementDeclaration());
            resolvedElementAdapter.registerListener(this);
          }
        }
        adapter.registerListener(this);
        oldSelection = adapter;
        Object model = adapter.getTarget();

        if (xsdModelAdapter != null && xsdModelAdapter.getModelReconcileAdapter() != null)
        {
          xsdModelAdapter.getModelReconcileAdapter().removeListener(internalNodeAdapter);
        }
        
        xsdModelAdapter = XSDModelAdapter.lookupOrCreateModelAdapter(((XSDConcreteComponent)adapter.getTarget()).getElement().getOwnerDocument());
        if (xsdModelAdapter != null && xsdModelAdapter.getModelReconcileAdapter() != null)
        {
          xsdModelAdapter.getModelReconcileAdapter().addListener(internalNodeAdapter);
        }
        
        if (model instanceof XSDConcreteComponent)
        {
          selection = new StructuredSelection(model);
        }
        super.selectionChanged(part, selection);
        return;
      }
      super.selectionChanged(part, selection);
  }
  
  public void propertyChanged(Object object, String property)
  {
    if (getCurrentTab() != null)
    {
      refresh();
    }
  }
  
  public void dispose()
  {
    if (xsdModelAdapter != null && xsdModelAdapter.getModelReconcileAdapter() != null)
    {
      xsdModelAdapter.getModelReconcileAdapter().removeListener(internalNodeAdapter);
      xsdModelAdapter = null;
    }
    super.dispose();
  }

  protected INodeAdapter internalNodeAdapter = new InternalNodeAdapter();
  class InternalNodeAdapter implements INodeAdapter
  {
    public boolean isAdapterForType(Object type)
    {
      return false;
    }

    public void notifyChanged(INodeNotifier notifier, int eventType, Object changedFeature, Object oldValue, Object newValue, int pos)
    {
      refresh();
    }
  }
}
