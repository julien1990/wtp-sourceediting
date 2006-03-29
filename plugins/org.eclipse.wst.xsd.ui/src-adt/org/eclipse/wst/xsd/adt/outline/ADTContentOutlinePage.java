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
package org.eclipse.wst.xsd.adt.outline;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.MultiPageSelectionProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.wst.xsd.adt.design.DesignViewContextMenuProvider;
import org.eclipse.wst.xsd.adt.editor.ADTMultiPageEditor;
import org.eclipse.xsd.XSDConcreteComponent;
import org.eclipse.xsd.XSDSchema;

public class ADTContentOutlinePage extends ContentOutlinePage
{
  protected ADTMultiPageEditor editor;
  protected int level = 0;
  protected Object model;
  protected ITreeContentProvider contentProvider;
  protected ILabelProvider labelProvider;
  protected MultiPageSelectionProvider selectionManager;
  protected SelectionManagerSelectionChangeListener selectionManagerSelectionChangeListener = new SelectionManagerSelectionChangeListener();
  protected TreeSelectionChangeListener treeSelectionChangeListener = new TreeSelectionChangeListener();

  /**
   * 
   */
  public ADTContentOutlinePage(ADTMultiPageEditor editor)
  {
    super();
    this.editor = editor;
  }

  public void setModel(Object newModel)
  {
    model = newModel;
  }

  public void setContentProvider(ITreeContentProvider contentProvider)
  {
    this.contentProvider = contentProvider;
  }

  public void setLabelProvider(ILabelProvider labelProvider)
  {
    this.labelProvider = labelProvider;
  }

  // expose
  public TreeViewer getTreeViewer()
  {
    return super.getTreeViewer();
  }

  public void createControl(Composite parent)
  {
    super.createControl(parent);
    getTreeViewer().setContentProvider(contentProvider);
    getTreeViewer().setLabelProvider(labelProvider);
    getTreeViewer().setInput(model);
    getTreeViewer().addSelectionChangedListener(this);
    MenuManager menuManager = new MenuManager("#popup");//$NON-NLS-1$
    menuManager.setRemoveAllWhenShown(true);
    Menu menu = menuManager.createContextMenu(getTreeViewer().getControl());
    getTreeViewer().getControl().setMenu(menu);
    setSelectionManager(editor.getSelectionManager());
    
    // Create menu...for now reuse graph's.  Note edit part viewer = null
    DesignViewContextMenuProvider menuProvider = new DesignViewContextMenuProvider(editor, null, (ISelectionProvider)editor.getSelectionManager());
    menuManager.addMenuListener(menuProvider);
    getSite().registerContextMenu("org.eclipse.wst.xsd.ui.popup.outline", menuManager, editor.getSelectionManager());

    // enable popupMenus extension
    // getSite().registerContextMenu("org.eclipse.wst.xsdeditor.ui.popup.outline",
    // menuManager, xsdEditor.getSelectionManager());

    // cs... why are we doing this from the outline view?
    //
    // xsdTextEditor.getXSDEditor().getSelectionManager().setSelection(new
    // StructuredSelection(xsdTextEditor.getXSDSchema()));
    // drill down from outline view
    getTreeViewer().getControl().addMouseListener(new MouseAdapter()
    {
      public void mouseDoubleClick(MouseEvent e)
      {
        ISelection iSelection = getTreeViewer().getSelection();
        if (iSelection instanceof StructuredSelection)
        {
          StructuredSelection selection = (StructuredSelection) iSelection;
          Object obj = selection.getFirstElement();
          if (obj instanceof XSDConcreteComponent)
          {
            XSDConcreteComponent comp = (XSDConcreteComponent) obj;
            if (comp.getContainer() instanceof XSDSchema)
            {
              // getXSDEditor().getGraphViewer().setInput(obj);
            }
          }
        }

      }
    });
  }

  class XSDKeyListener extends KeyAdapter
  {
  }

  public void dispose()
  {
    contentProvider.dispose();
    super.dispose();
  }

  public void setExpandToLevel(int i)
  {
    level = i;
  }

  public void setInput(Object value)
  {
    getTreeViewer().setInput(value);
    getTreeViewer().expandToLevel(level);
  }

  // public ISelection getSelection()
  // {
  // if (getTreeViewer() == null)
  // return StructuredSelection.EMPTY;
  // return getTreeViewer().getSelection();
  // }
  public void setSelectionManager(MultiPageSelectionProvider newSelectionManager)
  {
    TreeViewer treeViewer = getTreeViewer();
    // disconnect from old one
    if (selectionManager != null)
    {
      selectionManager.removeSelectionChangedListener(selectionManagerSelectionChangeListener);
      treeViewer.removeSelectionChangedListener(treeSelectionChangeListener);
    }
    selectionManager = newSelectionManager;
    // connect to new one
    if (selectionManager != null)
    {
      selectionManager.addSelectionChangedListener(selectionManagerSelectionChangeListener);
      treeViewer.addSelectionChangedListener(treeSelectionChangeListener);
    }
  }

  class SelectionManagerSelectionChangeListener implements ISelectionChangedListener
  {
    public void selectionChanged(SelectionChangedEvent event)
    {
      if (event.getSelectionProvider() != getTreeViewer())
      {
        getTreeViewer().setSelection(event.getSelection(), true);
      }
    }
  }

  class TreeSelectionChangeListener implements ISelectionChangedListener
  {
    public void selectionChanged(SelectionChangedEvent event)
    {
      if (selectionManager != null)
      {
        ISelection selection = event.getSelection();
        if (selection instanceof IStructuredSelection)
        {
          IStructuredSelection structuredSelection = (IStructuredSelection) selection;
          Object o = structuredSelection.getFirstElement();
          if (o != null)
          {
            selectionManager.setSelection(structuredSelection);
          }
        }
      }
    }
  }
}