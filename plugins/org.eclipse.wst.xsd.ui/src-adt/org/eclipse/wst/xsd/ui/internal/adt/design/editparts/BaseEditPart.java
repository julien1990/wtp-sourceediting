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
package org.eclipse.wst.xsd.ui.internal.adt.design.editparts;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.model.IActionProvider;
import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.model.IFeedbackHandler;
import org.eclipse.wst.xsd.ui.internal.adt.design.figures.IFigureFactory;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IADTObject;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IADTObjectListener;

public abstract class BaseEditPart extends AbstractGraphicalEditPart implements IActionProvider, IADTObjectListener, IFeedbackHandler
{
  protected static final String[] EMPTY_ACTION_ARRAY = {};
  protected boolean isSelected = false;
  
  public IFigureFactory getFigureFactory()
  {
    EditPartFactory factory = getViewer().getEditPartFactory();
    Assert.isTrue(factory instanceof IFigureFactory, "EditPartFactory must be an instanceof of IFigureFactory");     //$NON-NLS-1$
    return (IFigureFactory)factory; 
  }
  
  public String[] getActions(Object object)
  {
    Object model = getModel();
    if (model instanceof IActionProvider)
    {
      return ((IActionProvider)model).getActions(object);
    }  
    return EMPTY_ACTION_ARRAY;
  }
  
  protected void addActionsToList(List list, IAction[] actions)
  {
    for (int i = 0; i < actions.length; i++)
    {
      list.add(actions[i]);
    }  
  }
  
  public void activate()
  {
    super.activate();
    Object model = getModel();
    if (model instanceof IADTObject)
    {
      IADTObject object = (IADTObject)model;
      object.registerListener(this);
    }
    
    if (getZoomManager() != null)
      getZoomManager().addZoomListener(zoomListener);

  }
  
  public void deactivate()
  {
    try
    {
    Object model = getModel();
    if (model instanceof IADTObject)
    {
      IADTObject object = (IADTObject)model;
      object.unregisterListener(this);
    }   
    
    if (getZoomManager() != null)
      getZoomManager().removeZoomListener(zoomListener);    
    }
    finally
    {
      super.deactivate();
    }  
  }  
  
  public void propertyChanged(Object object, String property)
  {
    refresh();
  }
  
  public void refresh() {
    super.refresh();

    // Tell our children to refresh (note, this is NOT the function of 
    // refreshChildren(), strangely enough)
    for(Iterator i = getChildren().iterator(); i.hasNext(); )
    {
      Object obj = i.next();
      if (obj instanceof BaseEditPart)
      {
        ((BaseEditPart)obj).refresh();
      }
      else if (obj instanceof AbstractGraphicalEditPart)
      {
        ((AbstractGraphicalEditPart)obj).refresh();
      }
      
    }
  }

  public void addFeedback()
  {
    isSelected = true;
    refreshVisuals();
  }

  public void removeFeedback()
  {
    isSelected = false;
    refreshVisuals();
  }
  
  public ZoomManager getZoomManager()
  {
    return ((ScalableRootEditPart)getRoot()).getZoomManager();
  }
  
  public Rectangle getZoomedBounds(Rectangle r)
  {
    double factor = getZoomManager().getZoom();
    int x = (int)Math.round(r.x * factor);
    int y = (int)Math.round(r.y * factor);
    int width = (int)Math.round(r.width * factor);
    int height = (int)Math.round(r.height * factor);

    return new Rectangle(x, y, width, height);
  }
  
  private ZoomListener zoomListener = new ZoomListener()
  {
    public void zoomChanged(double zoom)
    {
      handleZoomChanged();
    }
  };

  protected void handleZoomChanged()
  {
    refreshVisuals();
  }

}
