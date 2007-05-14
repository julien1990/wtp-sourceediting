/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xsd.ui.internal.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.ide.IDE;
import org.eclipse.wst.common.uriresolver.internal.util.URIHelper;
import org.eclipse.wst.xsd.ui.internal.adapters.XSDAdapterFactory;
import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.RootContentEditPart;
import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.RootEditPart;
import org.eclipse.wst.xsd.ui.internal.adt.typeviz.design.figures.TypeVizFigureFactory;
import org.eclipse.wst.xsd.ui.internal.common.util.Messages;
import org.eclipse.wst.xsd.ui.internal.design.editparts.XSDEditPartFactory;
import org.eclipse.wst.xsd.ui.internal.editor.InternalXSDMultiPageEditor;
import org.eclipse.wst.xsd.ui.internal.editor.XSDEditorPlugin;
import org.eclipse.wst.xsd.ui.internal.editor.XSDFileEditorInput;
import org.eclipse.xsd.XSDConcreteComponent;
import org.eclipse.xsd.XSDSchema;

public class XSDGraphViewerDialog extends PopupDialog
{
  protected Object model;
  protected ScrollingGraphicalViewer viewer;
  protected IOpenInNewEditor openInNewEditorHelper;
  private OpenEditorLinkListener linkListener;
  private Label nsInfoLabel;
  private Hyperlink link;
  private String infoText;
  private Font infoFont;
  private String uniqueID;
  private PreviewControlListener moveListener;
  
  private static String X_ORIGIN = "DIALOG_X_ORIGIN"; //$NON-NLS-1$
  private static String Y_ORIGIN = "DIALOG_Y_ORIGIN"; //$NON-NLS-1$

  public XSDGraphViewerDialog(Shell parentShell, String titleText, String infoText, Object model, String ID)
  {
    super(parentShell, HOVER_SHELLSTYLE, true, true, true, false, titleText, infoText);
    setModel(model);
    linkListener = new OpenEditorLinkListener();
    this.infoText = infoText;
    this.uniqueID = ID;
    Assert.isTrue(ID != null && ID.length() > 0);    
    moveListener = new PreviewControlListener();
    create();
  }

  public void setOpenExternalEditor(IOpenInNewEditor helper)
  {
    this.openInNewEditorHelper = helper;
  }
  
  protected void fillDialogMenu(IMenuManager dialogMenu)
  {
    super.fillDialogMenu(dialogMenu);
    dialogMenu.add(new Separator());
    dialogMenu.add(new SetOpenInEditor());
  }

  protected Control createDialogArea(Composite parent)
  {
    viewer = new ScrollingGraphicalViewer();
    Composite c = new Composite(parent, SWT.NONE);
    c.setBackground(ColorConstants.white);
    c.setLayout(new FillLayout());

    RootEditPart root = new RootEditPart();
    viewer.setRootEditPart(root);

    viewer.createControl(c);
    viewer.getControl().setBackground(ColorConstants.white);
    EditPartFactory editPartFactory = new XSDEditPartFactory(new TypeVizFigureFactory());
    viewer.setEditPartFactory(editPartFactory);

    RootContentEditPart rootContentEditPart = new RootContentEditPart();
    rootContentEditPart.setModel(model);
    viewer.setContents(rootContentEditPart);
    
    getShell().addControlListener(moveListener);
    return c;
  }
  
  protected Control createInfoTextArea(Composite parent)
  {
    Composite infoComposite = new Composite(parent, SWT.NONE);
    GridLayout gridLayout = new GridLayout(2, false);
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    infoComposite.setLayout(gridLayout);
    GridData gd = new GridData(GridData.FILL_BOTH);
    infoComposite.setLayoutData(gd);

    nsInfoLabel = new Label(infoComposite, SWT.LEFT);
    nsInfoLabel.setText(infoText);

    Font font = nsInfoLabel.getFont();
    FontData[] fontDatas = font.getFontData();
    for (int i = 0; i < fontDatas.length; i++)
    {
      fontDatas[i].setHeight(fontDatas[i].getHeight() * 9 / 10);
    }
    infoFont = new Font(nsInfoLabel.getDisplay(), fontDatas);
    nsInfoLabel.setFont(infoFont);
    gd = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
    nsInfoLabel.setLayoutData(gd);
    nsInfoLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));

    link = new Hyperlink(infoComposite, SWT.RIGHT);
    link.setText(Messages._UI_ACTION_OPEN_IN_NEW_EDITOR);
    link.setFont(infoFont);
    link.addHyperlinkListener(linkListener);
    return infoComposite;
  }
  
  private void setModel(Object model)
  {
    Assert.isTrue(model instanceof XSDConcreteComponent);
    this.model = XSDAdapterFactory.getInstance().adapt((XSDConcreteComponent) model);
  }
    
  protected class SetOpenInEditor extends Action
  {
    public SetOpenInEditor()
    {
      super(Messages._UI_ACTION_OPEN_IN_NEW_EDITOR);
    }
    
    public void run()
    {
      if (openInNewEditorHelper != null)
      {
        try
        {
          openInNewEditorHelper.openXSDEditor();
        }
        catch (Exception e)
        {
          
        }
      }
    }
  }
  
  protected IDialogSettings getDialogSettings()
  {
    IDialogSettings settings= XSDEditorPlugin.getDefault().getDialogSettings().getSection(uniqueID);
    if (settings == null)
      settings= XSDEditorPlugin.getDefault().getDialogSettings().addNewSection(uniqueID);

    return settings;
  }
  
  protected Point getInitialLocation(Point initialSize)
  {
    Point result = super.getInitialLocation(initialSize);

    IDialogSettings settings = getDialogSettings();
    if (settings != null)
    {
      try
      {

        String prefix = uniqueID == null ? getClass().getName() : uniqueID;
        int x = settings.getInt(prefix + X_ORIGIN);
        int y = settings.getInt(prefix + Y_ORIGIN);
        result = new Point(x, y);
        Shell parent = getParentShell();
        if (parent != null)
        {
          Point parentLocation = parent.getLocation();
          result.x += parentLocation.x;
          result.y += parentLocation.y;
        }
      }
      catch (NumberFormatException e)
      {
      }
    }
    return result;
  }

  protected void saveDialogBounds(Shell shell)
  {
    IDialogSettings settings = getDialogSettings();
    if (settings != null)
    {
      Point shellLocation = shell.getLocation();
      Shell parent = getParentShell();
      if (parent != null)
      {
        Point parentLocation = parent.getLocation();
        shellLocation.x -= parentLocation.x;
        shellLocation.y -= parentLocation.y;
      }
      String prefix = uniqueID == null ? getClass().getName() : uniqueID;
      settings.put(prefix + X_ORIGIN, shellLocation.x);
      settings.put(prefix + Y_ORIGIN, shellLocation.y);
    }
  }

  
  public boolean close()
  {
    getShell().removeControlListener(moveListener);
    link.removeHyperlinkListener(linkListener);
    infoFont.dispose();
    infoFont = null;
    return super.close();
  }
  
  private final class OpenEditorLinkListener implements IHyperlinkListener
  {

    public void linkActivated(HyperlinkEvent e)
    {
      new SetOpenInEditor().run();
      close();
    }

    public void linkEntered(HyperlinkEvent e)
    {
      link.setForeground(ColorConstants.lightBlue);
    }

    public void linkExited(HyperlinkEvent e)
    {
      link.setForeground(link.getParent().getForeground());
    }
    
  }
  
  protected class PreviewControlListener implements ControlListener
  {

    public void controlMoved(ControlEvent e)
    {
      saveDialogBounds(getShell());
    }

    public void controlResized(ControlEvent e)
    {
    }
  }
 
  public static void openNonXSDResourceSchema(XSDConcreteComponent xsdComponent, XSDSchema schema, String editorName)
  {
    if (schema != null)
    {
      String schemaLocation = URIHelper.removePlatformResourceProtocol(schema.getSchemaLocation());
      IPath schemaPath = new Path(schemaLocation);
      IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(schemaPath);
      if (file != null && file.exists())
      {
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workbenchWindow != null)
        {
          IWorkbenchPage page = workbenchWindow.getActivePage();
          try
          {
            IEditorPart editorPart = null;

            XSDFileEditorInput editorInput = new XSDFileEditorInput(file, schema);

            editorInput.setEditorName(editorName);
            IEditorReference[] refs = page.getEditorReferences();
            int length = refs.length;
            for (int i = 0; i < length; i++)
            {
              IEditorInput input = refs[i].getEditorInput();
              if (input instanceof XSDFileEditorInput)
              {
                IFile aFile = ((XSDFileEditorInput) input).getFile();
                if (aFile.getFullPath().equals(file.getFullPath()))
                {
                  if (((XSDFileEditorInput) input).getSchema() == schema)
                  {
                    editorPart = refs[i].getEditor(true);
                    page.activate(refs[i].getPart(true));
                    break;
                  }
                }
              }
            }

            if (editorPart == null)
            {
              editorPart = page.openEditor(editorInput, "org.eclipse.wst.xsd.ui.internal.editor.InternalXSDMultiPageEditor", true, 0); //$NON-NLS-1$
            }

            if (editorPart instanceof InternalXSDMultiPageEditor)
            {
              InternalXSDMultiPageEditor xsdEditor = (InternalXSDMultiPageEditor) editorPart;
              xsdEditor.openOnGlobalReference(xsdComponent);
            }
          }
          catch (PartInitException pie)
          {

          }
        }
      }
    }         
  }
  
  public static void openXSDEditor(XSDConcreteComponent xsdComponent)
  {
    XSDSchema schema = xsdComponent.getSchema();
    if (schema != null)
    {
      String schemaLocation = URIHelper.removePlatformResourceProtocol(schema.getSchemaLocation());
      IPath schemaPath = new Path(schemaLocation);
      IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(schemaPath);
      if (file != null && file.exists())
      {
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workbenchWindow != null)
        {
          IWorkbenchPage page = workbenchWindow.getActivePage();
          try
          {
            IEditorPart editorPart = IDE.openEditor(page, file, true);
            if (editorPart instanceof InternalXSDMultiPageEditor)
            {
              InternalXSDMultiPageEditor xsdEditor = (InternalXSDMultiPageEditor) editorPart;
              xsdEditor.openOnGlobalReference(xsdComponent);
            }
          }
          catch (PartInitException pie)
          {

          }
        }
      }
    }         
  }

}
