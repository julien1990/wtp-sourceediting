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
package org.eclipse.wst.xsd.ui.internal.common.commands;

import org.w3c.dom.Element;

/*
 * This command is used from the extension view to edit extension elements
 * and attributes which are implemented as DOM objects (not part of the EMF model)
 * 
 * (trung) also used in XSDComplexTypeAdvancedSection to change attribute of
 * specical attributes like block, restriction which is not part of EMF Model
 */
public class UpdateAttributeValueCommand  extends BaseCommand
{
  Element element;
  String attributeName;
  String attributeValue;
  
  /** Whether the attribute should be deleted if value to 
   *   be set is an empty String or null  */
  protected boolean deleteIfValueEmpty = false;
  
  public UpdateAttributeValueCommand(Element element, String attributeName, String attributeValue)
  {
    this.element = element;
    this.attributeName = attributeName;
    this.attributeValue = attributeValue;
  }

  public void setDeleteIfEmpty(boolean v)
  {
	deleteIfValueEmpty = v;
  }
  
  public void execute()
  {
    try
    {
      beginRecording(element);
      if (deleteIfValueEmpty && 
    		  (attributeValue == null || attributeValue.equals("") ) )
      {
    	element.removeAttribute(attributeName);
      }
      else
      {
        element.setAttribute(attributeName, attributeValue);
      }
    }
    finally
    {
      endRecording();
    }
  } 
}
