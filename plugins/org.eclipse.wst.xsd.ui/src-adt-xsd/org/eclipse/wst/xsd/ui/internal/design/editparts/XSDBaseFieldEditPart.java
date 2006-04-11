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
package org.eclipse.wst.xsd.ui.internal.design.editparts;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.wst.xsd.ui.internal.adt.design.IAnnotationProvider;
import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.BaseFieldEditPart;
import org.eclipse.wst.xsd.ui.internal.adt.design.figures.IFieldFigure;
import org.eclipse.wst.xsd.ui.internal.adt.facade.IField;
import org.eclipse.wst.xsd.ui.internal.adt.outline.ITreeElement;
import org.eclipse.wst.xsd.ui.internal.design.editpolicies.DragAndDropEditPolicy;
import org.eclipse.wst.xsd.ui.internal.design.editpolicies.SelectionHandlesEditPolicyImpl;

public class XSDBaseFieldEditPart extends BaseFieldEditPart
{

  public XSDBaseFieldEditPart()
  {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
   */
  protected void refreshVisuals()
  {
    IFieldFigure figure = getFieldFigure();
    IField field = (IField) getModel();
    
    figure.getNameLabel().setText(field.getName());
    figure.getTypeLabel().setText(field.getTypeName());
    figure.refreshVisuals(getModel());

    String occurrenceDescription = "";
    if (field instanceof IAnnotationProvider)
    {
      occurrenceDescription = ((IAnnotationProvider)field).getNameAnnotationString();
    }
    
    figure.getNameAnnotationLabel().setText(occurrenceDescription);
    
    figure.recomputeLayout();

    // our model implements ITreeElement
    if (getModel() instanceof ITreeElement)
    {
      figure.getNameLabel().setIcon(((ITreeElement)getModel()).getImage());
    }

    if (getRoot() != null)
      ((GraphicalEditPart)getRoot()).getFigure().invalidateTree();
  }

  public void addNotify()
  {
    super.addNotify();
    getFieldFigure().editPartAttached(this);
  }

  protected SelectionHandlesEditPolicyImpl selectionHandlesEditPolicy = new SelectionHandlesEditPolicyImpl();
  protected void createEditPolicies()
  {
    installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, adtDirectEditPolicy);
    installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, selectionHandlesEditPolicy);
    installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new DragAndDropEditPolicy(getViewer(), selectionHandlesEditPolicy));
  }
}
