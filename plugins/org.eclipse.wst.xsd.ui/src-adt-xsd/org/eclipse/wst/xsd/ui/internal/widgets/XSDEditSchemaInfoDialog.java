/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *	   David Schneider, david.schneider@unisys.com - [142500] WTP properties pages fonts don't follow Eclipse preferences
 *******************************************************************************/
package org.eclipse.wst.xsd.ui.internal.widgets;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.xml.ui.internal.dialogs.EditSchemaInfoDialog;
import org.eclipse.wst.xml.ui.internal.nsedit.CommonEditNamespacesTargetFieldDialog;
import org.eclipse.wst.xsd.ui.internal.adt.editor.ProductCustomizationProvider;
import org.eclipse.wst.xsd.ui.internal.editor.Messages;
import org.eclipse.xsd.XSDForm;

public class XSDEditSchemaInfoDialog extends EditSchemaInfoDialog implements SelectionListener {
	String targetNamespace;
	CommonEditNamespacesTargetFieldDialog editNamespacesControl;
  Combo elementFormCombo, attributeFormCombo;
  String elementFormQualified = "", attributeFormQualified = ""; //$NON-NLS-1$ //$NON-NLS-2$
  
  private String [] formQualification = { "", XSDForm.QUALIFIED_LITERAL.getLiteral(), XSDForm.UNQUALIFIED_LITERAL.getLiteral() };  //$NON-NLS-1$
	
	public XSDEditSchemaInfoDialog(Shell parentShell, IPath resourceLocation, String targetNamespace) {
		super(parentShell, resourceLocation);
		this.targetNamespace = targetNamespace;
	}
/*
	// in super
	protected CommonEditNamespacesDialog createCommonEditNamespacesDialog(Composite dialogArea)
	{
	  return new CommonEditNamespacesDialog(dialogArea, resourceLocation, XMLUIPlugin.getResourceString("%_UI_NAMESPACE_DECLARATIONS"), false, true); //$NON-NLS-1$				
	}
	
	// in super
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		CommonEditNamespacesDialog editNamespacesControl = createCommonEditNamespacesDialog(dialogArea); 
		editNamespacesControl.setNamespaceInfoList(namespaceInfoList);
		editNamespacesControl.updateErrorMessage(namespaceInfoList);
		return dialogArea;
	}
	
	// in this
	protected CommonEditNamespacesDialog createCommonEditNamespacesDialog(Composite dialogArea)
	{
	  return new CommonEditNamespacesTargetFieldDialog(dialogArea, resourceLocation); //$NON-NLS-1$				
	}	*/
	
	// this is copy of ....
    protected Control __internalCreateDialogArea(Composite parent) {
        // create a composite with standard margins and spacing
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return composite;
    }	
	
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) __internalCreateDialogArea(parent);
		editNamespacesControl = new CommonEditNamespacesTargetFieldDialog(dialogArea, resourceLocation); //$NON-NLS-1$
		if (targetNamespace != null)
		{	
			editNamespacesControl.setTargetNamespace(targetNamespace);
		}
		editNamespacesControl.setNamespaceInfoList(namespaceInfoList);
		editNamespacesControl.updateErrorMessage(namespaceInfoList);
    
    Label separator = new Label(dialogArea, SWT.SEPARATOR | SWT.HORIZONTAL);
    GridData gd = new GridData(GridData.FILL_BOTH);
    separator.setLayoutData(gd);
    
    Composite otherAttributesComposite = new Composite(dialogArea, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    otherAttributesComposite.setLayout(layout);
    GridData data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    otherAttributesComposite.setLayoutData(data);
    
    Label elementFormLabel = new Label(otherAttributesComposite, SWT.LEFT);
    elementFormLabel.setText(Messages._UI_LABEL_ELEMENTFORMDEFAULT);
    
    Object object = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getAdapter(ProductCustomizationProvider.class);
    if (object instanceof ProductCustomizationProvider)
    {
      ProductCustomizationProvider productCustomizationProvider = (ProductCustomizationProvider)object;
      String newString = productCustomizationProvider.getProductString("LABEL_ELEMENT_FORM_DEFAULT");
      if (newString != null)
      {
        elementFormLabel.setText(newString);
      }
    }

    
    elementFormCombo = new Combo(otherAttributesComposite, SWT.NONE);
    elementFormCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    elementFormCombo.setItems(formQualification);
    elementFormCombo.addSelectionListener(this);
    
    Label attributeFormLabel = new Label(otherAttributesComposite, SWT.LEFT);
    attributeFormLabel.setText(Messages._UI_LABEL_ATTRIBUTEFORMDEFAULT);
    
    attributeFormCombo = new Combo(otherAttributesComposite, SWT.NONE);
    attributeFormCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    attributeFormCombo.setItems(formQualification);
    attributeFormCombo.addSelectionListener(this);
    applyDialogFont(parent);
		return dialogArea;
	}	
	
	public String getTargetNamespace() {
		return editNamespacesControl.getTargetNamespace();
	}
  
  public void setIsElementQualified(String state)
  {
    elementFormCombo.setText(state);
    elementFormQualified = state;
  }
  
  public void setIsAttributeQualified(String state)
  {
    attributeFormCombo.setText(state);
    attributeFormQualified = state;
  }
  
  public String getElementFormQualified()
  {
    return elementFormQualified;
  }
  
  public String getAttributeFormQualified()
  {
    return attributeFormQualified;
  }
  
  public void widgetDefaultSelected(SelectionEvent e)
  {
   
  }
  
  public void widgetSelected(SelectionEvent e)
  {
    if (e.widget == attributeFormCombo)
    {
      attributeFormQualified = attributeFormCombo.getText();
    }
    else if (e.widget == elementFormCombo)
    {
      elementFormQualified = elementFormCombo.getText();
    }

  }

}
