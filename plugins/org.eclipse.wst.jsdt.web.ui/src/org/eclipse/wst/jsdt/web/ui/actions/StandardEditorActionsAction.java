/**
 * 
 */
package org.eclipse.wst.jsdt.web.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.jsdt.core.IJavaElement;
import org.eclipse.wst.jsdt.web.ui.views.contentoutline.JsJfaceNode;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

/**
 * @author childsb
 * 
 */
public class StandardEditorActionsAction implements IObjectActionDelegate {
	private static final boolean APPEND_NEW_LINES_TO_COPY=true;
	private static final char NEW_LINE='\n';
	
	protected  static final String COPY = "org.eclipse.wst.jsdt.web.ui.copy";
	protected  static final String CUT = "org.eclipse.wst.jsdt.web.ui.cut";
	protected  static final String DELETE = "org.eclipse.wst.jsdt.web.ui.delete";
	protected  static final String PASTE_BEFORE = "org.eclipse.wst.jsdt.web.ui.paste.before";
	protected  static final String PASTE_AFTER = "org.eclipse.wst.jsdt.web.ui.paste.after";
	protected  ISelection selection;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	
	private void copy(IAction action) {
		JsJfaceNode[] nodes = parseSelection();
		
		if (nodes == null || nodes.length == 0) {
			return;
		}
		Clipboard clipboard=null;
		StringBuffer text = new StringBuffer();
		if(APPEND_NEW_LINES_TO_COPY) text.append(NEW_LINE);
		
		try {
			
			for (int i = 0; i < nodes.length; i++) {
				JsJfaceNode currentNode = nodes[i];
				int start = currentNode.getStartOffset();
				int length = currentNode.getLength();
				IStructuredDocument doc = currentNode.getStructuredDocument();
				try {
					String elementText = doc.get(start, length);
					text.append(elementText);
				} catch (BadLocationException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				
				if(APPEND_NEW_LINES_TO_COPY) text.append(NEW_LINE);
				
				clipboard = new Clipboard(Display.getCurrent());
				clipboard.setContents(new Object[]{text.toString()}, new Transfer[]{TextTransfer.getInstance()});
			}
		}finally {
			if(clipboard!=null) clipboard.dispose();
		}
	}
	private void delete(IAction action) {
		JsJfaceNode[] nodes = parseSelection();
		if (nodes == null || nodes.length == 0) {
			return;
		}
		IStructuredDocument lastDoc = null;
		IModelManager modelManager = StructuredModelManager.getModelManager();
		IStructuredModel model = null;
		try {
			int start;
			int length;
			for (int i = 0; i < nodes.length; i++) {
				JsJfaceNode currentNode = nodes[i];
				start = currentNode.getStartOffset();
				length = currentNode.getLength();
				IStructuredDocument doc = currentNode.getStructuredDocument();
				if (doc != lastDoc) {
					lastDoc = doc;
					if (model != null) {
						model.endRecording(action);
						model.changedModel();
						model.releaseFromEdit();
					}
					if (modelManager != null) {
						model = modelManager.getExistingModelForEdit(doc);
						model.aboutToChangeModel();
						model.beginRecording(action, "Delete JavaScript Element", "Delete JavaScript Element");
					}
				}
				doc.replaceText(action, start, length, "");
			}
			model.endRecording(action);
		} catch (Exception e) {
			System.out.println("Error in Standard Editor Action : " + e);
		} finally {
			if (model != null) {
				model.changedModel();
				model.releaseFromEdit();
			}
		}
	}
	
	private JsJfaceNode[] parseSelection() {
		if (selection == null) {
			return new JsJfaceNode[0];
		}
		ArrayList elements = new ArrayList();
		if (selection instanceof IStructuredSelection) {
			Iterator itt = ((IStructuredSelection) selection).iterator();
			while (itt.hasNext()) {
				Object element = itt.next();
				if (element instanceof IJavaElement) {
					elements.add(element);
				}
				if (element instanceof IJavaWebNode) {
					elements.add(element);
				}
			}
			return (JsJfaceNode[]) elements.toArray(new JsJfaceNode[elements.size()]);
		}
		return new JsJfaceNode[0];
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (action.getId().equals(StandardEditorActionsAction.CUT)) {
			copy(action);
			delete(action);
		} else if (action.getId().equals(StandardEditorActionsAction.COPY)) {
			copy(action);
		} else if (action.getId().equals(StandardEditorActionsAction.PASTE_BEFORE)) {
			paste(action, false);
		} else if (action.getId().equals(StandardEditorActionsAction.PASTE_AFTER)) {
			paste(action, true);
		} else if (action.getId().equals(StandardEditorActionsAction.DELETE)) {
			delete(action);
		}
	}
	
	private void paste(IAction action, boolean atEnd) {
		JsJfaceNode[] nodes = parseSelection();
		
		if (nodes == null || nodes.length == 0) {
			return;
		}
		
		StringBuffer text = new StringBuffer();
		
		int startOfPaste = -1;
		IStructuredDocument doc = null;
		/* Figure out where to paste the content */
		if(atEnd) {
			for(int i = 0;i<nodes.length;i++) {
				if(     (nodes[i].getStartOffset() + nodes[i].getLength() ) > startOfPaste) {
					startOfPaste = (nodes[i].getStartOffset() + nodes[i].getLength() );
					doc = nodes[i].getStructuredDocument();
				}
			}
		}else {
			for(int i = 0;i<nodes.length;i++) {
				if(     (nodes[i].getStartOffset() <  startOfPaste || startOfPaste<0)) {
					startOfPaste = nodes[i].getStartOffset();
					doc = nodes[i].getStructuredDocument();
				}
			}
		}
		Clipboard clipboard = null;
		IModelManager modelManager = StructuredModelManager.getModelManager();
		IStructuredModel model = null;
		
		try {
			clipboard = new Clipboard(Display.getCurrent());
			String pasteString = (String)clipboard.getContents(TextTransfer.getInstance());
			model = modelManager.getExistingModelForEdit(doc);
			model.aboutToChangeModel();
			model.beginRecording(action, "Insert Text "+ (atEnd?"before": "after") + " a JavaScript Element.", "Insert Text " + (atEnd?"before": "after")+ " a JavaScript Element.");	
			doc.replaceText(action, startOfPaste, 0, pasteString);
		}finally {
			if(clipboard!=null) clipboard.dispose();
			if (model != null) {
				model.endRecording(action);
				model.changedModel();
				model.releaseFromEdit();
			}
		}
	}
	
	
	
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
}
