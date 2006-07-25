package org.eclipse.wst.xsd.ui.internal.adt.editor;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xsd.ui.internal.adt.design.FlatCCombo;
import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.RootEditPart;
import org.eclipse.wst.xsd.ui.internal.editor.XSDEditorPlugin;

public abstract class CommonMultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener, CommandStackListener, ITabbedPropertySheetPageContributor, IPropertyListener, IEditorModeListener
{
  public static int SOURCE_PAGE_INDEX = 1, DESIGN_PAGE_INDEX = 0;
  
  protected IContentOutlinePage fOutlinePage;
  protected DefaultEditDomain editDomain;
  protected SelectionSynchronizer synchronizer;
  protected ActionRegistry actionRegistry;
  protected StructuredTextEditor structuredTextEditor;
  protected CommonSelectionManager selectionProvider;
  protected ScrollingGraphicalViewer graphicalViewer;
  protected EditorModeManager editorModeManager;
  protected FlatCCombo modeCombo;
  protected Composite toolbar;
  protected ModeComboListener modeComboListener;
  protected int maxLength = 0;
  
  public CommonMultiPageEditor()
  {
    super();
    editDomain = new DefaultEditDomain(this);

  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor#getContributorId()
   */
  public abstract String getContributorId();
  
  
  /**
   * 
   */
  protected abstract void createActions();
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
   */
  protected void createPages()
  {

  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
   */
  public void doSave(IProgressMonitor monitor)
  {
//    getEditor(1).doSave(monitor); 
    structuredTextEditor.doSave(monitor);
    getCommandStack().markSaveLocation();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.EditorPart#doSaveAs()
   */
  public void doSaveAs()
  {
    IEditorPart editor = getEditor(1);
//    editor.doSaveAs();
    structuredTextEditor.doSaveAs();
    setInput(structuredTextEditor.getEditorInput());
    setPartName(editor.getTitle());
    getCommandStack().markSaveLocation();
    
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
   */
  public boolean isSaveAsAllowed()
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
   * 
   * Closes all project files on project close.
   */
  public void resourceChanged(final IResourceChangeEvent event)
  {
    if (event.getType() == IResourceChangeEvent.PRE_CLOSE)
    {
      Display.getDefault().asyncExec(new Runnable()
      {
        public void run()
        {
          IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
          for (int i = 0; i < pages.length; i++)
          {
            if (((FileEditorInput) structuredTextEditor.getEditorInput()).getFile().getProject().equals(event.getResource()))
            {
              IEditorPart editorPart = pages[i].findEditor(structuredTextEditor.getEditorInput());
              pages[i].closeEditor(editorPart, true);
            }
          }
        }
      });
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.gef.commands.CommandStackListener#commandStackChanged(java.util.EventObject)
   */
  public void commandStackChanged(EventObject event)
  {
    firePropertyChange(PROP_DIRTY);
  }
  
  /**
   * Indicates that a property has changed.
   * 
   * @param source
   *          the object whose property has changed
   * @param propId
   *          the id of the property which has changed; property ids are
   *          generally defined as constants on the source class
   */
  public void propertyChanged(Object source, int propId)
  {
    switch (propId)
    {
      // had to implement input changed "listener" so that
      // strucutedText could tell it containing editor that
      // the input has change, when a 'resource moved' event is
      // found.
      case IEditorPart.PROP_INPUT :
      case IEditorPart.PROP_DIRTY : {
        if (source == structuredTextEditor)
        {
          if (structuredTextEditor.getEditorInput() != getEditorInput())
          {
            setInput(structuredTextEditor.getEditorInput());
            // title should always change when input changes.
            // create runnable for following post call
            Runnable runnable = new Runnable()
            {
              public void run()
              {
                _firePropertyChange(IWorkbenchPart.PROP_TITLE);
              }
            };
            // Update is just to post things on the display queue
            // (thread). We have to do this to get the dirty
            // property to get updated after other things on the
            // queue are executed.
            postOnDisplayQue(runnable);
          }
        }
        break;
      }
      case IWorkbenchPart.PROP_TITLE : {
        // update the input if the title is changed
        if (source == structuredTextEditor)
        {
          if (structuredTextEditor.getEditorInput() != getEditorInput())
          {
            setInput(structuredTextEditor.getEditorInput());
          }
        }
        break;
      }
      default : {
        // propagate changes. Is this needed? Answer: Yes.
        if (source == structuredTextEditor)
        {
          firePropertyChange(propId);
        }
        break;
      }
    }
  }

  /**
   * @return
   */
  protected SelectionSynchronizer getSelectionSynchronizer()
  {
    if (synchronizer == null)
      synchronizer = new SelectionSynchronizer();
    return synchronizer;
  }

  public CommonSelectionManager getSelectionManager()
  {
    if (selectionProvider == null)
    {
      selectionProvider = new CommonSelectionManager(this);
    }
    return selectionProvider;
  }
  
  /*
   * This method is just to make firePropertyChanged accessbible from some
   * (anonomous) inner classes.
   */
  protected void _firePropertyChange(int property)
  {
    super.firePropertyChange(property);
  }
  
  /**
   * Posts the update code "behind" the running operation.
   */
  protected void postOnDisplayQue(Runnable runnable)
  {
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
    if (windows != null && windows.length > 0)
    {
      Display display = windows[0].getShell().getDisplay();
      display.asyncExec(runnable);
    }
    else
      runnable.run();
  }

  /**
   * The <code>MultiPageEditorPart</code> implementation of this
   * <code>IWorkbenchPart</code> method disposes all nested editors.
   * Subclasses may extend.
   */
  public void dispose()
  {
    getCommandStack().removeCommandStackListener(this);
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    actionRegistry.dispose();
    
    if (structuredTextEditor != null) {
      structuredTextEditor.removePropertyListener(this);
    }
    structuredTextEditor = null;
    editDomain = null;
    fOutlinePage = null;
    synchronizer = null;
    actionRegistry = null;
    selectionProvider = null;
    graphicalViewer = null;
    if (modeCombo != null && !modeCombo.isDisposed())
    {
      modeCombo.removeSelectionListener(modeComboListener);
      modeComboListener = null;
    }
    
    super.dispose();
  }

  protected CommandStack getCommandStack()
  {
    return editDomain.getCommandStack();
  }

  /*
   * (non-Javadoc) Method declared on IEditorPart
   */
  public void gotoMarker(IMarker marker)
  {
    setActivePage(0);
    IDE.gotoMarker(getEditor(0), marker);
  }

  /**
   * The <code>MultiPageEditorExample</code> implementation of this method
   * checks that the input is an instance of <code>IFileEditorInput</code>.
   */
  public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException
  {
//    if (!(editorInput instanceof IFileEditorInput))
//      throw new PartInitException("Invalid Input: Must be IFileEditorInput"); //$NON-NLS-1$
    super.init(site, editorInput);
    
    getCommandStack().addCommandStackListener(this);
    
    initializeActionRegistry();
    
    String title = null;
    if (getEditorInput() != null) {
      title = getEditorInput().getName();
    }
    setPartName(title);
  }

  protected void initializeActionRegistry()
  {
    createActions();
  }
  
  protected ActionRegistry getActionRegistry()
  {
    if (actionRegistry == null)
      actionRegistry = new ActionRegistry();
    return actionRegistry;
  }

  public Object getAdapter(Class type)
  {
    if (type == CommandStack.class)
      return getCommandStack();
    if (type == ActionRegistry.class)
      return getActionRegistry();
    if (type == EditorModeManager.class)
      return getEditorModeManager();
    
    return super.getAdapter(type);
  }
  
  protected DefaultEditDomain getEditDomain()
  {
    return editDomain;
  }

  /**
   * From GEF GraphicalEditor A convenience method for updating a set of actions
   * defined by the given List of action IDs. The actions are found by looking
   * up the ID in the {@link #getActionRegistry() action registry}. If the
   * corresponding action is an {@link UpdateAction}, it will have its
   * <code>update()</code> method called.
   * 
   * @param actionIds
   *          the list of IDs to update
   */
  protected void updateActions(List actionIds)
  {
    ActionRegistry registry = getActionRegistry();
    Iterator iter = actionIds.iterator();
    while (iter.hasNext())
    {
      IAction action = registry.getAction(iter.next());
      if (action instanceof UpdateAction)
        ((UpdateAction) action).update();
    }
  }

  /**
   * Returns <code>true</code> if the command stack is dirty
   * 
   * @see org.eclipse.ui.ISaveablePart#isDirty()
   */
  public boolean isDirty()
  {
    if (getCommandStack().isDirty())
      return true;
    else
      return super.isDirty();
  }

  public StructuredTextEditor getTextEditor()
  {
    return structuredTextEditor;
  }
 
  
  protected Composite createGraphPageComposite()
  {
    Composite parent = new Composite(getContainer(), SWT.NONE);    
    parent.setLayout(new FillLayout());
    return parent;
  }
  
  protected void createGraphPage()
  {
    Composite parent = createGraphPageComposite();
    
    graphicalViewer = getGraphicalViewer();
    graphicalViewer.createControl(parent);
        
    getEditDomain().addViewer(graphicalViewer);
    
    configureGraphicalViewer();
    hookGraphicalViewer();    
    int index = addPage(parent);
    setPageText(index, Messages._UI_LABEL_DESIGN);
  }

  protected void createSourcePage()
  {
    structuredTextEditor = new StructuredTextEditor();
    try
    {
      int index = addPage(structuredTextEditor, getEditorInput());
      setPageText(index, Messages._UI_LABEL_SOURCE);
      structuredTextEditor.update();
      structuredTextEditor.setEditorPart(this);
      structuredTextEditor.addPropertyListener(this);
      firePropertyChange(PROP_TITLE);
    }
    catch (PartInitException e)
    {
      ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus()); //$NON-NLS-1$
    }
  }
  
  protected void configureGraphicalViewer()
  {
    graphicalViewer.getControl().setBackground(ColorConstants.listBackground);

    // Set the root edit part
    // ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
    RootEditPart root = new RootEditPart();
    ZoomManager zoomManager = root.getZoomManager();
    
    List zoomLevels = new ArrayList(3);
    zoomLevels.add(ZoomManager.FIT_ALL);
    zoomLevels.add(ZoomManager.FIT_WIDTH);
    zoomLevels.add(ZoomManager.FIT_HEIGHT);
    zoomManager.setZoomLevelContributions(zoomLevels);

    IAction zoomIn = new ZoomInAction(zoomManager);
    IAction zoomOut = new ZoomOutAction(zoomManager);
    getActionRegistry().registerAction(zoomIn);
    getActionRegistry().registerAction(zoomOut);

    getSite().getKeyBindingService().registerAction(zoomIn);
    getSite().getKeyBindingService().registerAction(zoomOut);

    //ConnectionLayer connectionLayer = (ConnectionLayer) root.getLayer(LayerConstants.CONNECTION_LAYER);
    //connectionLayer.setConnectionRouter(new BendpointConnectionRouter());

    //connectionLayer.setConnectionRouter(new ShortestPathConnectionRouter(connectionLayer));
    // connectionLayer.setVisible(false);

    // Zoom
    zoomManager.setZoom(1.0);
    // Scroll-wheel Zoom
    graphicalViewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.CTRL), MouseWheelZoomHandler.SINGLETON);
    graphicalViewer.setRootEditPart(root);
    graphicalViewer.setEditPartFactory(getEditPartFactory());
  }
  
  protected void hookGraphicalViewer()
  {
    getSelectionSynchronizer().addViewer(graphicalViewer);
  }
  
  protected abstract ScrollingGraphicalViewer getGraphicalViewer();
  protected abstract EditPartFactory getEditPartFactory();
  protected abstract void initializeGraphicalViewer();

  private EditorModeManager getEditorModeManager()
  {
    if (editorModeManager == null)
    {
      editorModeManager = createEditorModeManager();
      editorModeManager.addListener(this);
      editorModeManager.init();
    }  
    return editorModeManager;
  }
  
  protected abstract EditorModeManager createEditorModeManager();
  
  
  protected void createViewModeToolbar(Composite parent)
  {
    EditorModeManager manager = (EditorModeManager)getAdapter(EditorModeManager.class);
    EditorMode [] modeList = manager.getModes();
    
    int modeListLength = modeList.length;
    boolean showToolBar = modeListLength > 1;
   
    if (showToolBar)
    {
      toolbar = new Composite(parent, SWT.FLAT | SWT.DRAW_TRANSPARENT);
      toolbar.setBackground(ColorConstants.white);
      toolbar.addPaintListener(new PaintListener() {

        public void paintControl(PaintEvent e)
        {
          Rectangle clientArea = toolbar.getClientArea(); 
          e.gc.setForeground(ColorConstants.lightGray);
          e.gc.drawRectangle(clientArea.x, clientArea.y, clientArea.width - 1, clientArea.height - 1);
        }
      });
      
      GridLayout gridLayout = new GridLayout(3, false);
      toolbar.setLayout(gridLayout);

      Label label = new Label(toolbar, SWT.FLAT | SWT.HORIZONTAL);
      label.setBackground(ColorConstants.white);
      label.setText(Messages._UI_LABEL_VIEW);
      label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

      modeCombo = new FlatCCombo(toolbar, SWT.FLAT);
      modeCombo.setText(modeList[0].getDisplayName());
      // populate combo with modes
      for (int i = 0; i < modeListLength; i++ )
      {
        String modeName = modeList[i].getDisplayName(); 
        modeCombo.add(modeName);

        int approxWidthOfLetter = parent.getFont().getFontData()[0].getHeight();
        int approxWidthOfStrings = approxWidthOfLetter * modeName.length() + approxWidthOfLetter * Messages._UI_LABEL_VIEW.length();
        if (approxWidthOfStrings > maxLength)
          maxLength = approxWidthOfStrings;
      }
      
      modeComboListener = new ModeComboListener();
      modeCombo.addSelectionListener(modeComboListener);
      modeCombo.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));

      Control [] children = modeCombo.getChildren();
      int length = children.length;
      for (int i = 0; i < length; i++)
      {
        if (children[i] instanceof Button)
        {
          Button arrow = (Button)children[i];
          final Rectangle bounds = arrow.getBounds();
          arrow.setBackground(toolbar.getBackground());
          arrow.addPaintListener(new PaintListener()
          {
            public void paintControl(PaintEvent e)
            {
              Image image = XSDEditorPlugin.getXSDImage("icons/TriangleToolBar.gif"); //$NON-NLS-1$  
              Rectangle b = image.getBounds();
              e.gc.fillRectangle(b.x, b.y, b.width + 1, b.height + 1);
              e.gc.drawImage(image, bounds.x, bounds.y - 1);
            }
          });
          break;
        }
      }

      ImageHyperlink hyperlink = new ImageHyperlink(toolbar, SWT.FLAT);
      hyperlink.setBackground(ColorConstants.white);
      hyperlink.setImage(WorkbenchImages.getImageRegistry().get(IWorkbenchGraphicConstants.IMG_ETOOL_HELP_CONTENTS));
      hyperlink.setToolTipText(Messages._UI_HOVER_VIEW_MODE_DESCRIPTION);
      hyperlink.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
    }
  }
  
  
  protected class ModeComboListener implements SelectionListener
  {
    public ModeComboListener()
    {
    }
    
    public void widgetDefaultSelected(SelectionEvent e)
    {
    }

    public void widgetSelected(SelectionEvent e)
    {
      if (e.widget == modeCombo)
      {
        EditorModeManager manager = (EditorModeManager)getAdapter(EditorModeManager.class);
        EditorMode [] modeList = manager.getModes();
        if (modeList.length >= 1)
          manager.setCurrentMode(modeList[modeCombo.getSelectionIndex()]);
      }
    }
  }
}
