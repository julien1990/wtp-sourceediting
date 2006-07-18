/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xsd.ui.internal.text;

import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xsd.ui.internal.util.ModelReconcileAdapter;
import org.eclipse.xsd.XSDConcreteComponent;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XSDModelReconcileAdapter extends ModelReconcileAdapter
{
  protected XSDSchema schema;
  
  public XSDModelReconcileAdapter(Document document, XSDSchema schema)
  {
    super(document);
    this.schema = schema;
  }
  
  protected void handleNodeChanged(Node node)
  {
    if (node instanceof Element)
    {  
      XSDConcreteComponent concreteComponent = schema.getCorrespondingComponent(node);    
      concreteComponent.elementChanged((Element)node);
    }
    else if (node instanceof Document)
    {
      // The document changed so we may need to fix up the 
      // definition's root element
      Document document = (Document)node;    
      Element schemaElement = document.getDocumentElement();
      if (schemaElement != null && schemaElement != schema.getElement())
      {   
        // here we handle the case where a new 'schema' element was added
        //(e.g. the file was totally blank and then we type in the root element)        
        //
        if (schemaElement.getLocalName().equals(XSDConstants.SCHEMA_ELEMENT_TAG))         
        {  
          //System.out.println("****** Setting new schema");
          schema.setElement(schemaElement);
        }
      }      
      else if (schemaElement != null)
      {       
        // handle the case where the definition element's content has changed
        // TODO (cs) do we really need to handle this case?
        schema.elementChanged(schemaElement);
      }      
      else if (schemaElement == null)
      {
        // if there's no root element clear out the schema
        //
        schema.getContents().clear();
        // TODO (cs) I'm not sure why the above isn't enough
        // to clear out all of the component lists
        // for now I've just added a few more lines to do additional clearing
        //
        schema.getElementDeclarations().clear();
        schema.getTypeDefinitions().clear();
        schema.getAttributeDeclarations().clear();
        schema.getModelGroupDefinitions().clear();
        schema.getAttributeGroupDefinitions().clear();     
        
        schema.setElement(null);
      }      
    } 
  }
  
  public void modelDirtyStateChanged(IStructuredModel model, boolean isDirty)
  {
    if (!isDirty)
    {
      // TODO need a way to tell the views to stop refreshing (temporarily)
      //
      /*
      schema.reset();
      
      // For some reason type references don't get fixed up when an import is removed
      // even if we call schema.reset() explictly.  To work around this  we iterate thru
      // the type references and recompute them incase a schema did infact change
      //
      for (Iterator i = schema.getElementDeclarations().iterator(); i.hasNext(); )
      {  
        XSDElementDeclarationImpl ed = (XSDElementDeclarationImpl)i.next();
        //ed.elementAttributesChanged(ed.getElement());
        XSDTypeDefinition td = ed.getTypeDefinition();
        td = ed.resolveTypeDefinition(td.getSchema().getTargetNamespace(), td.getName());        
        ed.setTypeDefinition(td);
      }*/  
    }
  }  
}