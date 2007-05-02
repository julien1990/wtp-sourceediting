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
package org.eclipse.wst.xsd.ui.internal.common.properties.sections.appinfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.eclipse.wst.xsd.ui.internal.common.properties.sections.appinfo.custom.DialogNodeEditorConfiguration;
import org.eclipse.wst.xsd.ui.internal.common.properties.sections.appinfo.custom.ListNodeEditorConfiguration;
import org.eclipse.wst.xsd.ui.internal.common.properties.sections.appinfo.custom.NodeEditorConfiguration;

public class ExtensionDetailsViewer extends Viewer
{
  private final static String ITEM_DATA = "ITEM_DATA"; //$NON-NLS-1$
  private final static String EDITOR_CONFIGURATION_DATA = "EDITOR_CONFIGURATION_DATA"; //$NON-NLS-1$
  
  Composite control;  
  Composite composite;
  ExtensionDetailsContentProvider contentProvider;
  TabbedPropertySheetWidgetFactory widgetFactory;  
  InternalControlListener internalControlListener;
  
  public ExtensionDetailsViewer(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory)
  {
    this.widgetFactory = widgetFactory;    
    control =  widgetFactory.createComposite(parent);
    internalControlListener = new InternalControlListener();
    control.setLayout(new GridLayout());    
  }
  public Control getControl()
  {
    return control;
  }
  

  public Object getInput()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public ISelection getSelection()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void refresh()
  {
    Control[] children = composite.getChildren(); 
    for (int i = 0; i < children.length; i++)
    {
      Control control = children[i];
      if (control instanceof Text)
      {
         ExtensionItem item = (ExtensionItem)control.getData(ITEM_DATA);
         String value = contentProvider.getValue(item);        
        ((Text)control).setText(value); 
      }  
    }
  }

  private void createTextOrComboControl(ExtensionItem item, Composite composite)
  {
    Control control = null;
    String value = contentProvider.getValue(item);
    NodeEditorConfiguration editorConfiguration = item.getPropertyEditorConfiguration();

    if (editorConfiguration != null && hasStyle(editorConfiguration, NodeEditorConfiguration.STYLE_COMBO))
    {          
      ListNodeEditorConfiguration configuration = (ListNodeEditorConfiguration)editorConfiguration;
      CCombo combo = widgetFactory.createCCombo(composite);
      combo.setText(value);
      Object[] values = configuration.getValues(item);
      LabelProvider labelProvider = configuration.getLabelProvider();
      for (int j = 0; j < values.length; j++)
      {            
        Object o = values[j];
        String displayName = labelProvider != null ?
            labelProvider.getText(o) :
              o.toString();
            combo.add(displayName);
      }   
      control = combo;
    }
    if (control == null)
    {
      Text text = widgetFactory.createText(composite,value);
      control = text; 
    } 
    control.setData(ITEM_DATA, item);
    control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    control.addFocusListener(internalControlListener);      
  }
  
  private void createButtonControl(ExtensionItem item, Composite composite)
  {
    NodeEditorConfiguration editorConfiguration = item.getPropertyEditorConfiguration();
    if (editorConfiguration != null && hasStyle(editorConfiguration, NodeEditorConfiguration.STYLE_DIALOG))
    {    
      DialogNodeEditorConfiguration configuration = (DialogNodeEditorConfiguration)editorConfiguration;            
      Button button = new Button(composite, SWT.NONE);
      GridData gridData = new GridData();
      gridData.heightHint = 17;
      button.setLayoutData(gridData);
      button.addSelectionListener(internalControlListener);
      button.setData(ITEM_DATA, item);
      button.setData(EDITOR_CONFIGURATION_DATA, configuration);
      String text = configuration.getButonText();
      if (text != null)
      {  
        button.setText(text); //$NON-NLS-1$
      }  
      button.setImage(configuration.getButtonImage());
    }
    else
    {
      Control placeHolder = new Label(composite, SWT.NONE);
      placeHolder.setVisible(false);
      placeHolder.setEnabled(false);
      placeHolder.setLayoutData(new GridData()); 
    }      
  } 
  
  public void setInput(Object input)
  { 
    // TODO (cs) add assertions
    //
    if (contentProvider == null)
      return;
    
    if (composite != null)
    {/*
      for (Iterator i = controlsThatWeAreListeningTo.iterator(); i.hasNext(); )
      {
        Control control = (Control)i.next();       
        if (control != null)
        {  
          control.removeFocusListener(internalFocusListener);
        }  
      } */ 
      composite.dispose();       
    }   

    composite = widgetFactory.createComposite(control);
    composite.setBackground(ColorConstants.white);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    composite.setLayout(gridLayout);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));

    Object[] items = contentProvider.getItems(input);      

    for (int i = 0; i < items.length; i++)
    {
      ExtensionItem item = (ExtensionItem)items[i];
      String name = contentProvider.getName(item);
      Label label = widgetFactory.createLabel(composite, name + ":"); //$NON-NLS-1$
      label.setLayoutData(new GridData());
      createTextOrComboControl(item, composite);
      createButtonControl(item, composite);
    }  
    control.layout(true);    
  }
    
  private boolean hasStyle(NodeEditorConfiguration configuration, int style)
  {
    return (configuration.getStyle() & style) != 0;
  }
  
  
  public void setSelection(ISelection selection, boolean reveal)
  {
    // TODO Auto-generated method stub
    
  }
  public ExtensionDetailsContentProvider getContentProvider()
  {
    return contentProvider;
  }
  public void setContentProvider(ExtensionDetailsContentProvider contentProvider)
  {
    this.contentProvider = contentProvider;
  }
  
  private void applyEdit(ExtensionItem item, Widget widget)
  {
    if (item != null)
    {    
      String value = null;
      if (widget instanceof Text)
      {
        Text text = (Text)widget;
        value = text.getText();    
      }
      else if (widget instanceof CCombo)
      {
        CCombo combo = (CCombo)widget;
        int index = combo.getSelectionIndex();
        if (index != -1)
        {  
          value = combo.getItem(index);
        }  
      }       
      if (value != null)
      {  
        Command command = item.getUpdateValueCommand(value);
        if (command != null)
        {
          // TODO (cs) add command stack handling stuff
          command.execute();
        }
      }                    
    }              
  }
  
  class InternalControlListener implements FocusListener, SelectionListener
  {
    public void widgetSelected(SelectionEvent e)
    {
      // for button controls we handle selection events
      //        
      Object item = e.widget.getData(EDITOR_CONFIGURATION_DATA);
      if (item instanceof DialogNodeEditorConfiguration)
      {
        DialogNodeEditorConfiguration dialogNodeEditorConfiguration = (DialogNodeEditorConfiguration)item;        
        dialogNodeEditorConfiguration.invokeDialog();               
        //applyEdit((ExtensionItem)item, e.widget);
      }             
    }
    
    public void widgetDefaultSelected(SelectionEvent e)
    {
      // do nothing      
    } 
    
    public void focusGained(FocusEvent e)
    {
    }    
    
    public void focusLost(FocusEvent e)
    {
      // apply edits for text and combo box controls
      // via the focusLost event
      // TODO (cs) handle explict ENTER key
      //
      Object item = e.widget.getData(ITEM_DATA);
      if (item instanceof ExtensionItem)
      {
        applyEdit((ExtensionItem)item, e.widget);
      }      
    }
  }
}
