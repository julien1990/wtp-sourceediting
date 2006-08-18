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
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.model.FocusTypeColumn;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IComplexType;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IType;

public class ComplexTypeEditPart extends StructureEditPart
{   
  protected boolean shouldDrawConnection()
  {
    if (getParent().getModel() instanceof FocusTypeColumn)
    {  
      IComplexType complexType = (IComplexType)getModel();
      return complexType.getSuperType() != null;
    } 
    return false;
  }
  
  
  private EditPart getTargetEditPart(IType type)
  {
    ColumnEditPart columnEditPart = null;
    for (EditPart editPart = this; editPart != null; editPart = editPart.getParent())
    {
      if (editPart instanceof ColumnEditPart)
      {
        columnEditPart = (ColumnEditPart)editPart;
        break;
      }  
    }     
    if (columnEditPart != null)
    {
      for (Iterator i = columnEditPart.getChildren().iterator(); i.hasNext(); )
      {
        EditPart child = (EditPart)i.next();
        if (child.getModel() == type)
        {
          return child;
        }         
      }  
    }
    return null;
  }
  
  public TypeReferenceConnection createConnectionFigure()
  {
    TypeReferenceConnection connectionFigure = null;
    IComplexType complexType = (IComplexType)getModel();
    IType type = complexType.getSuperType();
    if (type != null)
    {      
      AbstractGraphicalEditPart referenceTypePart = (AbstractGraphicalEditPart)getTargetEditPart(type);
      if (referenceTypePart != null)
      {
        connectionFigure = new TypeReferenceConnection();
        // draw a line out from the top         
        connectionFigure.setSourceAnchor(new CenteredConnectionAnchor(getFigure(), CenteredConnectionAnchor.TOP, 1));
        
        // TODO (cs) need to draw the target anchor to look like a UML inheritance relationship
        // adding a label to the connection would help to
        connectionFigure.setTargetAnchor(new CenteredConnectionAnchor(referenceTypePart.getFigure(), CenteredConnectionAnchor.BOTTOM, 0, 0)); 
        connectionFigure.setHighlight(false);
      }
    }    
    return connectionFigure;
  }  
}