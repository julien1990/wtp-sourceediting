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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.wst.xsd.ui.internal.adapters.XSDVisitor;
import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeGroupDefinition;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDConcreteComponent;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDEnumerationFacet;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDModelGroupDefinition;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDWildcard;
import org.eclipse.xsd.util.XSDConstants;

public class DeleteCommand extends BaseCommand
{
  XSDConcreteComponent target;

  public DeleteCommand(String label, XSDConcreteComponent target)
  {
    super(label);
    this.target = target;
  }

  public void execute()
  {
    XSDVisitor visitor = new XSDVisitor()
    {
      public void visitElementDeclaration(org.eclipse.xsd.XSDElementDeclaration element)
      {
        if (element.getTypeDefinition() == target)
        {
          XSDSimpleTypeDefinition type = target.getSchema().getSchemaForSchema().resolveSimpleTypeDefinition("string"); //$NON-NLS-1$
          element.setTypeDefinition(type);
        }
        super.visitElementDeclaration(element);
      }
    };

    XSDConcreteComponent parent = target.getContainer();

    if (target instanceof XSDModelGroup || target instanceof XSDElementDeclaration || target instanceof XSDModelGroupDefinition)
    {
      if (parent instanceof XSDParticle)
      {
        if (parent.getContainer() instanceof XSDModelGroup)
        {
          XSDModelGroup modelGroup = (XSDModelGroup) ((XSDParticle) parent).getContainer();

          modelGroup.getContents().remove(parent);
        }
        else if (parent.getContainer() instanceof XSDComplexTypeDefinition)
        {
          XSDComplexTypeDefinition complexType = (XSDComplexTypeDefinition) parent.getContainer();
          complexType.setContent(null);
        }
      }
      else if (parent instanceof XSDSchema)
      {
        visitor.visitSchema(target.getSchema());
        ((XSDSchema) parent).getContents().remove(target);
      }

    }
    else if (target instanceof XSDAttributeDeclaration)
    {
      if (parent instanceof XSDAttributeUse)
      {
        EObject obj = parent.eContainer();
        XSDComplexTypeDefinition complexType = null;
        while (obj != null)
        {
          if (obj instanceof XSDComplexTypeDefinition)
          {
            complexType = (XSDComplexTypeDefinition) obj;
            break;
          }
          obj = obj.eContainer();
        }
        if (complexType != null)
        {
          complexType.getAttributeContents().remove(parent);
        }

        if (parent.getContainer() instanceof XSDAttributeGroupDefinition)
        {
          XSDAttributeGroupDefinition attrGroup = (XSDAttributeGroupDefinition) parent.getContainer();

          attrGroup.getContents().remove(parent);
        }
      }
      else if (parent instanceof XSDSchema)
      {
        visitor.visitSchema(target.getSchema());
        ((XSDSchema) parent).getContents().remove(target);
      }
    }
    else if (target instanceof XSDAttributeGroupDefinition &&
             parent instanceof XSDComplexTypeDefinition)
    {
      ((XSDComplexTypeDefinition)parent).getAttributeContents().remove(target);
    }
    else if (target instanceof XSDEnumerationFacet)
    {
      XSDEnumerationFacet enumerationFacet = (XSDEnumerationFacet)target;
      enumerationFacet.getSimpleTypeDefinition().getFacetContents().remove(enumerationFacet);
    }
    else if (target instanceof XSDWildcard)
    {
      if (parent instanceof XSDParticle)
      {
        if (parent.getContainer() instanceof XSDModelGroup)
        {
          XSDModelGroup modelGroup = (XSDModelGroup) ((XSDParticle) parent).getContainer();
          modelGroup.getContents().remove(parent);
        }
      }
      else if (parent instanceof XSDComplexTypeDefinition)
      {
        ((XSDComplexTypeDefinition)parent).setAttributeWildcardContent(null);
      }
      else if (parent instanceof XSDAttributeGroupDefinition)
      {
        ((XSDAttributeGroupDefinition)parent).setAttributeWildcardContent(null);
      }
    }
    else if (target instanceof XSDComplexTypeDefinition && parent instanceof XSDElementDeclaration)
    {
      ((XSDElementDeclaration)parent).setTypeDefinition(target.resolveSimpleTypeDefinition(XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001, "string"));
    }
    else
    {
      if (parent instanceof XSDSchema)
      {
        visitor.visitSchema(target.getSchema());
        ((XSDSchema) parent).getContents().remove(target);
      }
    }
  }
}
