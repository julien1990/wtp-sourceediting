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
package org.eclipse.wst.xsd.ui.internal.adt.editor;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.wst.xsd.ui.internal.adt.actions.BaseSelectionAction;
import org.eclipse.wst.xsd.ui.internal.adt.actions.SetInputToGraphView;
import org.eclipse.wst.xsd.ui.internal.adt.design.DesignViewGraphicalViewer;
import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.ADTEditPartFactory;
import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.BackToSchemaEditPart;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IModel;
import org.eclipse.wst.xsd.ui.internal.adt.outline.ADTContentOutlinePage;
import org.eclipse.wst.xsd.ui.internal.adt.outline.ADTContentOutlineProvider;
import org.eclipse.wst.xsd.ui.internal.adt.outline.ADTLabelProvider;
import org.eclipse.wst.xsd.ui.internal.editor.XSDEditorPlugin;

public abstract class ADTMultiPageEditor extends CommonMultiPageEditor
{
  protected IModel model;
  private int currentPage = -1;
  protected Button tableOfContentsButton;
  
  /**
   * Creates a multi-page editor example.
   */
  public ADTMultiPageEditor()
  {
    super();
  }

  
  private class InternalLayout extends StackLayout
  {
    public InternalLayout()
    {
      super();  
    }
    
    protected void layout(Composite composite, boolean flushCache)
    {
      Control children[] = composite.getChildren();
      Rectangle rect = composite.getClientArea();
      rect.x += marginWidth;
      rect.y += marginHeight;
      rect.width -= 2 * marginWidth;
      rect.height -= 2 * marginHeight;
      
      for (int i = 0; i < children.length; i++) 
      {
        if (i == 0)  // For the back to schema button 
        {  
          children[i].setBounds(rect.x + 10, rect.y + 10, 26, 26);
        }
        else if (i == 1 && modeCombo != null) // For the drop down toolbar
        {
          children[i].setBounds(rect.x + rect.width - 90 - maxLength, rect.y + 10, maxLength + 60, 26);
        }
        else // For the main graph viewer
        {
          children[i].setBounds(rect);          
        }
      }       
    }               
  }
  
  GraphicalViewerImpl toolbarViewer;
  BackToSchemaEditPart backToSchemaEditPart;
  protected Composite createGraphPageComposite()
  {    
    Composite parent = new Composite(getContainer(), SWT.FLAT);
    parent.setLayout(new InternalLayout());
    
    // the palletViewer extends from this...maybe use it instead?
    toolbarViewer = new GraphicalViewerImpl();
    toolbarViewer.createControl(parent);
    toolbarViewer.getControl().setVisible(true);
    backToSchemaEditPart = new BackToSchemaEditPart(this);
    backToSchemaEditPart.setModel(getModel());
    toolbarViewer.setContents(backToSchemaEditPart);
    
    createViewModeToolbar(parent);
    
    return parent;
  }
  
  protected void createGraphPage()
  {
    super.createGraphPage(); 
//    toolbarViewer.getControl().moveAbove(graphicalViewer.getControl());
//    graphicalViewer.getControl().moveBelow(toolbarViewer.getControl());
  }
  
  public String getContributorId()
  {
    return "org.eclipse.wst.xsd.ui.internal.editor"; //$NON-NLS-1$
  }
  
  public IContentOutlinePage getContentOutlinePage()
  {
    if (fOutlinePage == null || fOutlinePage.getControl() == null || fOutlinePage.getControl().isDisposed())
    {
      ADTContentOutlinePage outlinePage = new ADTContentOutlinePage(this);
      //outlinePage.getTreeViewer().removeF(filter);
      ADTContentOutlineProvider adtContentProvider = new ADTContentOutlineProvider();
      outlinePage.setContentProvider(adtContentProvider);
      ADTLabelProvider adtLabelProvider = new ADTLabelProvider();
      outlinePage.setLabelProvider(adtLabelProvider);
      outlinePage.setModel(getModel());
      
      fOutlinePage = outlinePage;
    }
    return fOutlinePage;
  }

  /**
   * Creates the pages of the multi-page editor.
   */
  protected void createPages()
  {
    selectionProvider = getSelectionManager();
    
    createGraphPage();
    createSourcePage();
    
    getEditorSite().setSelectionProvider(selectionProvider);

    model = buildModel();  // (IFileEditorInput)getEditorInput());
    
    initializeGraphicalViewer();
    
    int pageIndexToShow = getDefaultPageTypeIndex();
    setActivePage(pageIndexToShow);
  }

  protected int getDefaultPageTypeIndex() {
    int pageIndex = SOURCE_PAGE_INDEX;
    if (XSDEditorPlugin.getPlugin().getDefaultPage().equals(XSDEditorPlugin.DESIGN_PAGE)) {
        pageIndex = DESIGN_PAGE_INDEX;
    }

    return pageIndex;
  }

  protected void pageChange(int newPageIndex)
  {
    currentPage = newPageIndex;
    super.pageChange(newPageIndex);
  }
  
  private boolean isTableOfContentsApplicable(Object graphViewInput)
  {
    return !(graphViewInput instanceof IModel);
  }

  protected ScrollingGraphicalViewer getGraphicalViewer()
  {
    DesignViewGraphicalViewer viewer = new DesignViewGraphicalViewer(this, getSelectionManager());
    viewer.addInputChangdListener(new ISelectionChangedListener()
    {
      public void selectionChanged(SelectionChangedEvent event)
      {        
        IStructuredSelection input = (IStructuredSelection)event.getSelection();
        backToSchemaEditPart.setEnabled(isTableOfContentsApplicable(input.getFirstElement()));
        backToSchemaEditPart.setModel(getModel());
      }      
    });
    return viewer;
  }
  
  abstract public IModel buildModel();  // (IFileEditorInput editorInput);
  
  protected void createActions()
  {
    ActionRegistry registry = getActionRegistry();
    
    BaseSelectionAction action = new SetInputToGraphView(this);
    action.setSelectionProvider(getSelectionManager());
    registry.registerAction(action);
  }


  public IModel getModel()
  {
    return model;
  }

  public Object getAdapter(Class type)
  {
    if (type == ZoomManager.class)
      return graphicalViewer.getProperty(ZoomManager.class.toString());
    
    if (type == GraphicalViewer.class)
      return graphicalViewer;
    if (type == EditPart.class && graphicalViewer != null)
      return graphicalViewer.getRootEditPart();
    if (type == IFigure.class && graphicalViewer != null)
      return ((GraphicalEditPart) graphicalViewer.getRootEditPart()).getFigure();

    if (type == IContentOutlinePage.class)
    {
      return getContentOutlinePage();
    }

    return super.getAdapter(type);
  }

  protected EditPartFactory getEditPartFactory() {
    return new ADTEditPartFactory();
  }

  protected void initializeGraphicalViewer()
  {
    graphicalViewer.setContents(model);
  }
  
  public void dispose()
  {
    if (currentPage == SOURCE_PAGE_INDEX)
    {
      XSDEditorPlugin.getPlugin().setSourcePageAsDefault();
    }
    else
    {
      XSDEditorPlugin.getPlugin().setDesignPageAsDefault();
    }
    toolbarViewer = null;
    backToSchemaEditPart = null;
    super.dispose();
  }
}
