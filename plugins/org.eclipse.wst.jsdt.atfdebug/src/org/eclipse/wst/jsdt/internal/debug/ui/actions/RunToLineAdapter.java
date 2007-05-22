/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.debug.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.debug.ui.actions.RunToLineHandler;
import org.eclipse.wst.jsdt.core.dom.AST;
import org.eclipse.wst.jsdt.core.dom.ASTParser;
import org.eclipse.wst.jsdt.core.dom.CompilationUnit;
import org.eclipse.wst.jsdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.wst.jsdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.wst.jsdt.internal.debug.ui.JDIDebugUIPlugin;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Run to line target for the Java debugger
 */
public class RunToLineAdapter implements IRunToLineTarget {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#runToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public void runToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) throws CoreException {
		ITextEditor textEditor = getTextEditor(part);
		String errorMessage = null;
		if (textEditor == null) {
			errorMessage = ActionMessages.RunToLineAdapter_1;
		} else {
			IEditorInput input = textEditor.getEditorInput();
			if (input == null) {
				errorMessage = ActionMessages.RunToLineAdapter_0; 
			} else {
				final IDocument document= textEditor.getDocumentProvider().getDocument(input);
				if (document == null) {
					errorMessage = ActionMessages.RunToLineAdapter_1; 
				} else {
					final int[] validLine = new int[1];
					final String[] typeName = new String[1];
					final int[] lineNumber = new int[1];
					final ITextSelection textSelection = (ITextSelection) selection;
					Runnable r = new Runnable() {
						public void run() {
							lineNumber[0] = textSelection.getStartLine() + 1;
							ASTParser parser = ASTParser.newParser(AST.JLS3);
							parser.setSource(document.get().toCharArray());
							CompilationUnit compilationUnit= (CompilationUnit)parser.createAST(null);
							ValidBreakpointLocationLocator locator= new ValidBreakpointLocationLocator(compilationUnit, lineNumber[0], false, false);
							compilationUnit.accept(locator);
							validLine[0]= locator.getLineLocation();		
							typeName[0]= locator.getFullyQualifiedTypeName();
						}
					};
					BusyIndicator.showWhile(JDIDebugUIPlugin.getStandardDisplay(), r);
					if (validLine[0] == lineNumber[0]) {
						IBreakpoint breakpoint= null;
						Map attributes = new HashMap(4);
						BreakpointUtils.addRunToLineAttributes(attributes);
						breakpoint= BreakpointUtils.createLineBreakpoint(document,input, lineNumber[0], -1, -1, 1, false, attributes);
						errorMessage = ActionMessages.RunToLineAdapter_2; 
						if (target instanceof IAdaptable) {
							IDebugTarget debugTarget = (IDebugTarget) ((IAdaptable)target).getAdapter(IDebugTarget.class);
							if (debugTarget != null) {
	                            RunToLineHandler handler = new RunToLineHandler(debugTarget, target, breakpoint);
	                            handler.run(new NullProgressMonitor());
								return;
							}
						}
					} else {
						// invalid line
						if (textSelection.getLength() > 0) {
							errorMessage = ActionMessages.RunToLineAdapter_3; 
						} else {
							errorMessage = ActionMessages.RunToLineAdapter_4; 
						}
	
					}
				}
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR,
				errorMessage, null));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#canRunToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public boolean canRunToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) {
	    if (target instanceof IDebugElement) {
            IDebugElement element = (IDebugElement) target;
            IJavaDebugTarget adapter = (IJavaDebugTarget) element.getDebugTarget().getAdapter(IJavaDebugTarget.class);
            return adapter != null;
        }
		return false;
	}
	
    /**
     * Returns the text editor associated with the given part or <code>null</code>
     * if none. In case of a multi-page editor, this method should be used to retrieve
     * the correct editor to perform the operation on.
     * 
     * @param part workbench part
     * @return text editor part or <code>null</code>
     */
    protected ITextEditor getTextEditor(IWorkbenchPart part) {
    	if (part instanceof ITextEditor) {
    		return (ITextEditor) part;
    	}
    	return (ITextEditor) part.getAdapter(ITextEditor.class);
    }	
}
