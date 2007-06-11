/**
 * 
 */
package org.eclipse.wst.jsdt.web.ui.views.provisional.contentoutline;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.internal.InternalHandlerUtil;
import org.eclipse.wst.jsdt.internal.ui.actions.CompositeActionGroup;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.CompilationUnitEditorActionContributor;
import org.eclipse.wst.jsdt.ui.IContextMenuConstants;
import org.eclipse.wst.jsdt.web.ui.actions.IJavaWebNode;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.internal.IReleasable;
import org.eclipse.wst.xml.ui.internal.contentoutline.XMLNodeActionManager;

/**
 * @author childsb
 *
 */
public class JsMenuListener extends XMLNodeActionManager implements IMenuListener, IReleasable {
		private XMLNodeActionManager fActionManager;
		private TreeViewer fTreeViewer;
		private CompositeActionGroup fActionGroups;
		CompilationUnitEditorActionContributor contrib;
		ISelectionProvider selectionProvider;
		public static final String EDIT_GROUP_ID = "group.edit";
		
		public JsMenuListener(TreeViewer viewer) {
			super((IStructuredModel) viewer.getInput(), viewer);
			contrib = new CompilationUnitEditorActionContributor();
			
			fTreeViewer = viewer;
			
			
			
//			
//			fActionGroups= new CompositeActionGroup(new ActionGroup[] {
//					new OpenViewActionGroup(getWorkbenchSite(), getSelectionProvider()),
//					new CCPActionGroup(getWorkbenchSite()),
//					new GenerateActionGroup(getWorkbenchSite()),
//					new RefactorActionGroup(getWorkbenchSite()),
//					new JavaSearchActionGroup(getWorkbenchSite())});
		}

		private IWorkbenchSite getWorkbenchSite() {
			
			return InternalHandlerUtil.getActiveSite(fTreeViewer);
			

		}
		
		private ISelectionProvider getSelectionProvider() {
			return getWorkbenchSite().getSelectionProvider();
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
		 */
		
		
		public void menuAboutToShow(IMenuManager manager) {
			
			ISelection selection = fTreeViewer.getSelection();
			if(selection instanceof TreeSelection) {
				TreeSelection tselect = (TreeSelection)selection;
				Object[] elements = tselect.toArray();
				int javaCount=0;
				for(int i=0;i<elements.length;i++) {
					if(elements[i] instanceof IJavaWebNode) {
						javaCount++;
					}
				}

//				manager.add(new Separator(IContextMenuConstants.GROUP_NEW));
//				menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
//				menu.add(new Separator(IContextMenuConstants.GROUP_OPEN));
//				manager.add(new GroupMarker(IContextMenuConstants.GROUP_SHOW));
//				menu.add(new Separator(ICommonMenuConstants.GROUP_EDIT));
//				menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
//				menu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
//				menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
//				menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
//				menu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
//				menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
//				menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
				
				if(javaCount==elements.length && javaCount!=0) {
					// see plugin.xml for object contributions that populate these menus
					
					/* Menu for:
					 * 
					 * Open Type Hierarchy
					 * Open Call Hierarchy
					 * Show In-->
					 *     Script Explorer
					 *     Navigator
					 */ 
					manager.add(new Separator(IContextMenuConstants.GROUP_SHOW));
					manager.add(new GroupMarker(IContextMenuConstants.GROUP_SHOW));
					/* Menu for:
					* Cut
					* Copy
					* Paste
					* Delete
					 */ 
					
					
					manager.add(new Separator(EDIT_GROUP_ID));
					manager.add(new GroupMarker(EDIT_GROUP_ID));
					/* Menu for:
					 * 
					 * Refrences-->
					 *     Workspace
					 *     Project
					 *     Hierarchy
					 *     Working Set
					 *     
					 * Declerations-->
					 *     Workspace
					 *     Project
					 *     Hierarchy
					 *     Working Set
					 *     
					 */ 
					
					
					manager.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
					manager.add(new GroupMarker(IContextMenuConstants.GROUP_SEARCH));
					
					
					
					/* all Java Elements */
//					
//					
//					
//					JavaPlugin.createStandardGroups(manager);
//					String[] actionSets = JSDTActionSetUtil.getAllActionSets();
//
//					IAction[] actions = JSDTActionSetUtil.getActionsFromSet(actionSets);
//					for(int i = 0;i<actions.length;i++) {
//						manager.add(actions[i]);
//					}
//					fActionGroups.setContext(new ActionContext(selection));
//					fActionGroups.fillContextMenu(manager);
//					
				}else if(javaCount==0){
					fillContextMenu(manager, selection);
				}
			}
			
		}

		public IAction[] getAllJsActions() {
			
			return null;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.wst.sse.ui.internal.IReleasable#release()
		 */
		public void release() {
			fTreeViewer = null;
			if (fActionManager != null) {
				fActionManager.setModel(null);
			}
		}

		
}
