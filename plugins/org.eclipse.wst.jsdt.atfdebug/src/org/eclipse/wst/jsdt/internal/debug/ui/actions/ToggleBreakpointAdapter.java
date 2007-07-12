/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.debug.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.wst.jsdt.core.Flags;
import org.eclipse.wst.jsdt.core.IClassFile;
import org.eclipse.wst.jsdt.core.ICompilationUnit;
import org.eclipse.wst.jsdt.core.IField;
import org.eclipse.wst.jsdt.core.IJavaElement;
import org.eclipse.wst.jsdt.core.IMember;
import org.eclipse.wst.jsdt.core.IMethod;
import org.eclipse.wst.jsdt.core.IPackageDeclaration;
import org.eclipse.wst.jsdt.core.ISourceRange;
import org.eclipse.wst.jsdt.core.IType;
import org.eclipse.wst.jsdt.core.ITypeParameter;
import org.eclipse.wst.jsdt.core.ITypeRoot;
import org.eclipse.wst.jsdt.core.JavaModelException;
import org.eclipse.wst.jsdt.core.Signature;
import org.eclipse.wst.jsdt.core.dom.AST;
import org.eclipse.wst.jsdt.core.dom.ASTParser;
import org.eclipse.wst.jsdt.core.dom.CompilationUnit;
import org.eclipse.wst.jsdt.internal.debug.ui.BreakpointUtils;
import org.eclipse.wst.jsdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.WorkingCopyManager;
import org.eclipse.wst.jsdt.ui.IWorkingCopyManager;
import org.eclipse.wst.jsdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.atf.mozilla.ide.debug.model.JSLineBreakpoint;

/**
 * Toggles a line breakpoint in a Java editor.
 * 
 * @since 3.0
 */
public class ToggleBreakpointAdapter implements IToggleBreakpointsTargetExtension {
	
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * Constructor
	 */
	public ToggleBreakpointAdapter() {
	}

    /**
     * Convenience method for printing messages to the status line
     * @param message the message to be displayed
     * @param part the currently active workbench part
     */
    protected void report(final String message, final IWorkbenchPart part) {
        JDIDebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
            public void run() {
                IEditorStatusLine statusLine = (IEditorStatusLine) part.getAdapter(IEditorStatusLine.class);
                if (statusLine != null) {
                    if (message != null) {
                        statusLine.setMessage(true, message, null);
                    } else {
                        statusLine.setMessage(true, null, null);
                    }
                }
                if (message != null && JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
                    JDIDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
                }
            }
        });
    }

    /**
     * Returns the <code>IType</code> for the given selection
     * @param selection the current text selection
     * @return the <code>IType</code> for the text selection or <code>null</code>
     */
/*    protected IType getType(ITextSelection selection) {
        IMember member = ActionDelegateHelper.getDefault().getCurrentMember(selection);
        IType type = null;
        if (member instanceof IType) {
            type = (IType) member;
        } else if (member != null) {
            type = member.getDeclaringType();
        }
        // bug 52385: we don't want local and anonymous types from compilation
        // unit,
        // we are getting 'not-always-correct' names for them.
        try {
            while (type != null && !type.isBinary() && type.isLocal()) {
                type = type.getDeclaringType();
            }
        } catch (JavaModelException e) {
            JDIDebugUIPlugin.log(e);
        }
        return type;
    }*/

    /**
     * Returns the IType associated with the <code>IJavaElement</code> passed in
     * @param element the <code>IJavaElement</code> to get the type from
     * @return the corresponding <code>IType</code> for the <code>IJavaElement</code>, or <code>null</code> if there is not one.
     * @since 3.3
     */
 /*   protected IType getType(IJavaElement element) {
    	switch(element.getElementType()) {
	    	case IJavaElement.FIELD: {
	    		return ((IField)element).getDeclaringType();
	    	}	
	    	case IJavaElement.METHOD: {
	    		return ((IMethod)element).getDeclaringType();
	    	}
	    	case IJavaElement.TYPE: {
	    		return (IType)element;
	    	}
	    	default: {
	    		return null;
	    	}
    	}
    }*/
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
    	toggleLineBreakpoints(part, selection, false);
    }
    
    /**
     * Toggles a line breakpoint.
     * @param part the currently active workbench part 
     * @param selection the current selection
     * @param bestMatch if we should make a best match or not
     */
    public void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection, final boolean bestMatch) {
        Job job = new Job("Toggle Line Breakpoint") { //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
            	ITextEditor editor = getTextEditor(part);
                if (editor != null && selection instanceof ITextSelection) {
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    try {
	                    report(null, part);
	                    ISelection sel = selection;
	                	if(!(selection instanceof IStructuredSelection)) {
	                		sel = translateToMembers(part, selection);
	                	}
	                    if(sel instanceof IStructuredSelection) {
	                    	IJavaElement member = (IJavaElement) ((IStructuredSelection)sel).getFirstElement();
	                    	String tname = createQualifiedTypeName(member);
	                    	IResource resource = BreakpointUtils.getBreakpointResource(member);
							int lnumber = ((ITextSelection) selection).getStartLine() + 1;
							JSLineBreakpoint existingBreakpoint = BreakpointUtils.lineBreakpointExists(resource, tname, lnumber);
							if (existingBreakpoint != null) {
								DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(existingBreakpoint, true);
								return Status.OK_STATUS;
							}
							Map attributes = new HashMap(10);
							IDocumentProvider documentProvider = editor.getDocumentProvider();
							if (documentProvider == null) {
							    return Status.CANCEL_STATUS;
							}
							IDocument document = documentProvider.getDocument(editor.getEditorInput());
							try {
								IRegion line = document.getLineInformation(lnumber - 1);
								int start = line.getOffset();
								int end = start + line.getLength() - 1;
								BreakpointUtils.addJavaBreakpointAttributesWithMemberDetails(attributes, member, start, end);
							} 	
							catch (BadLocationException ble) {JDIDebugUIPlugin.log(ble);}
							JSLineBreakpoint breakpoint = BreakpointUtils.createLineBreakpoint(document, editor.getEditorInput(), lnumber, -1, -1, 0, true, attributes);
							new BreakpointLocationVerifierJob(document, breakpoint, lnumber, bestMatch, tname, member, resource, editor).schedule();
	                    }
	                    else {
	                    	report(ActionMessages.ToggleBreakpointAdapter_3, part);
	                    	return Status.OK_STATUS;
	                    }
                    } 
                    catch (CoreException ce) {return ce.getStatus();}
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(IWorkbenchPart,
     *      ISelection)
     */
    public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}
        return selection instanceof ITextSelection;
    }

    
    /**
     * Returns the package qualified name, while accounting for the fact that a source file might
     * not have a project
     * @param type the type to ensure the package qualified name is created for
     * @return the package qualified name
     * @since 3.3
     */
    private String createQualifiedTypeName(IJavaElement type) {
    	ITypeRoot typeRoot;
    	if (type instanceof IMember)
    		typeRoot=((IMember)type).getTypeRoot();
    	else
    		typeRoot=(ITypeRoot)type;
    	String tname =typeRoot.getElementName();
    	
//    	try {
	    	if(!type.getJavaProject().exists()) {
//				IPackageDeclaration[] pd = type.getCompilationUnit().getPackageDeclarations();
//				if(pd.length > 0) {
//					String pack = pd[0].getElementName();
//					if(!pack.equals(EMPTY_STRING)) {
//						tname =  pack+"."+tname; //$NON-NLS-1$
//					}
//				}
			}
//    	} 
//    	catch (JavaModelException e) {}
    	return tname;
    }
    
    /**
     * gets the <code>IJavaElement</code> from the editor input
     * @param input the current editor input
     * @return the corresponding <code>IJavaElement</code>
     * @since 3.3
     */
    private IJavaElement getJavaElement(IEditorInput input) {
    	IJavaElement je = JavaUI.getEditorInputJavaElement(input);
    	if(je != null) {
    		return je;
    	}
    	//try to get from the working copy manager
    	//TODO this one depends on bug 151260
    	return ((WorkingCopyManager)JavaUI.getWorkingCopyManager()).getWorkingCopy(input, false);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            return getMethods(ss).length > 0;
        }
        return (selection instanceof ITextSelection) && isMethod((ITextSelection) selection, part);
    }
    
    /**
     * Returns whether the given part/selection is remote (viewing a repository)
     * 
     * @param part
     * @param selection
     * @return
     */
    protected boolean isRemote(IWorkbenchPart part, ISelection selection) {
    	if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if(element instanceof IMember) {
				IMember member = (IMember) element;
				return !member.getJavaProject().getProject().exists();
			}
		}
    	ITextEditor editor = getTextEditor(part);
    	if (editor != null) {
    		IEditorInput input = editor.getEditorInput();
    		Object adapter = Platform.getAdapterManager().getAdapter(input, "org.eclipse.team.core.history.IFileRevision"); //$NON-NLS-1$
    		return adapter != null;
    	} 
    	return false;
    }
    
    /**
     * Returns the text editor associated with the given part or <code>null</code>
     * if none. In case of a multi-page editor, this method should be used to retrieve
     * the correct editor to perform the breakpoint operation on.
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

    /**
     * Returns the methods from the selection, or an empty array
     * @param selection the selection to get the methods from
     * @return an array of the methods from the selection or an empty array
     */
    protected IMethod[] getMethods(IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return new IMethod[0];
        }
        List methods = new ArrayList(selection.size());
        Iterator iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object thing = iterator.next();
            try {
                if (thing instanceof IMethod) {
                	IMethod method = (IMethod) thing;
                	if (!Flags.isAbstract(method.getFlags())) {
                		methods.add(method);
                	}
                }
            } 
            catch (JavaModelException e) {}
        }
        return (IMethod[]) methods.toArray(new IMethod[methods.size()]);
    }

    /**
     * Returns if the text selection is a valid method or not
     * @param selection the text selection
     * @param part the associated workbench part
     * @return true if the selection is a valid method, false otherwise
     */
    private boolean isMethod(ITextSelection selection, IWorkbenchPart part) {
		ITextEditor editor = getTextEditor(part);
		if(editor != null) {
			IJavaElement element = getJavaElement(editor.getEditorInput());
			if(element != null) {
				try {
					if(element instanceof ICompilationUnit) {
						element = ((ICompilationUnit) element).getElementAt(selection.getOffset());
					}
					else if(element instanceof IClassFile) {
						element = ((IClassFile) element).getElementAt(selection.getOffset());
					}
					return element != null && element.getElementType() == IJavaElement.METHOD;
				} 
    			catch (JavaModelException e) {return false;}
			}
		}
    	return false;
    }
    
    /**
     * Returns a list of <code>IField</code> and <code>IJavaFieldVariable</code> in the given selection.
     * When an <code>IField</code> can be resolved for an <code>IJavaFieldVariable</code>, it is
     * returned in favour of the variable.
     *
     * @param selection
     * @return list of <code>IField</code> and <code>IJavaFieldVariable</code>, possibly empty
     * @throws CoreException
     */
    protected List getFields(IStructuredSelection selection) throws CoreException {
        if (selection.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List fields = new ArrayList(selection.size());
        Iterator iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object thing = iterator.next();
            if (thing instanceof IField) {
                fields.add(thing);
            } 
        }
        return fields;
    }

    /**
     * Returns if the structured selection is itself or is part of an interface
     * @param selection the current selection
     * @return true if the selection is part of an interface, false otherwise
     * @since 3.2
     */
    private boolean isInterface(ISelection selection, IWorkbenchPart part) {
    	return false;
    }
    
    /**
     * Returns if the text selection is a field selection or not
     * @param selection the text selection
     * @param part the associated workbench part
     * @return true if the text selection is a valid field for a watchpoint, false otherwise
     * @since 3.3
     */
    private boolean isField(ITextSelection selection, IWorkbenchPart part) {
    	ITextEditor editor = getTextEditor(part);
    	if(editor != null) {
    		IJavaElement element = getJavaElement(editor.getEditorInput());
    		if(element != null) {
    			try {
	    			if(element instanceof ICompilationUnit) {
						element = ((ICompilationUnit) element).getElementAt(selection.getOffset());
	    			}
	    			else if(element instanceof IClassFile) {
	    				element = ((IClassFile) element).getElementAt(selection.getOffset());
	    			}
	    			return element != null && element.getElementType() == IJavaElement.FIELD;
				} 
    			catch (JavaModelException e) {return false;}		
    		}
    	}
    	return false;
    }
    
    /**
     * Determines if the selection is a field or not
     * @param selection the current selection
     * @return true if the selection is a field false otherwise
     */
    private boolean isFields(IStructuredSelection selection) {
        if (!selection.isEmpty()) {
        	try {
	            Iterator iterator = selection.iterator();
	            while (iterator.hasNext()) {
	                Object thing = iterator.next();
	                if (thing instanceof IField) {
	                	int flags = ((IField)thing).getFlags();
	                	return !Flags.isFinal(flags) & !(Flags.isFinal(flags) & Flags.isStatic(flags));
	                }
	            }
        	}
        	catch(JavaModelException e) {return false;}
        }
        return false;
    }
    

    /**
     * Returns the resolved method signature for the specified type
     * @param type the declaring type the method is contained in
     * @param methodSignature the method signature to resolve
     * @return the resolved method signature
     * @throws JavaModelException
     */
    public static String resolveMethodSignature(IType type, String methodSignature) throws JavaModelException {
        String[] parameterTypes = Signature.getParameterTypes(methodSignature);
        int length = parameterTypes.length;
        String[] resolvedParameterTypes = new String[length];
        for (int i = 0; i < length; i++) {
            resolvedParameterTypes[i] = resolveType(type, parameterTypes[i]);
            if (resolvedParameterTypes[i] == null) {
                return null;
            }
        }
        String resolvedReturnType = resolveType(type, Signature.getReturnType(methodSignature));
        if (resolvedReturnType == null) {
            return null;
        }
        return Signature.createMethodSignature(resolvedParameterTypes, resolvedReturnType);
    }

    /**
     * Resolves the the type for its given signature
     * @param type the type
     * @param typeSignature the types signature
     * @return the resolved type name
     * @throws JavaModelException
     */
    private static String resolveType(IType type, String typeSignature) throws JavaModelException {
        int count = Signature.getArrayCount(typeSignature);
        String elementTypeSignature = Signature.getElementType(typeSignature);
        if (elementTypeSignature.length() == 1) {
            // no need to resolve primitive types
            return typeSignature;
        }
        String elementTypeName = Signature.toString(elementTypeSignature);
        String[][] resolvedElementTypeNames = type.resolveType(elementTypeName);
        if (resolvedElementTypeNames == null || resolvedElementTypeNames.length != 1) {
        	// check if type parameter
            ITypeParameter[] typeParameters = type.getTypeParameters();
            for (int i = 0; i < typeParameters.length; i++) {
    			ITypeParameter parameter = typeParameters[i];
    			if (parameter.getElementName().equals(elementTypeName)) {
    				String[] bounds = parameter.getBounds();
    				if (bounds.length == 0) {
    					return "Ljava/lang/Object;"; //$NON-NLS-1$
    				} else {
						String bound = Signature.createTypeSignature(bounds[0], false);
						return resolveType(type, bound);
    				}
    			}
    		}
            // the type name cannot be resolved
            return null;
        }

        String[] types = resolvedElementTypeNames[0];
        types[1] = types[1].replace('.', '$');
        
        String resolvedElementTypeName = Signature.toQualifiedName(types);
        String resolvedElementTypeSignature = EMPTY_STRING;
        if(types[0].equals(EMPTY_STRING)) {
        	resolvedElementTypeName = resolvedElementTypeName.substring(1);
        	resolvedElementTypeSignature = Signature.createTypeSignature(resolvedElementTypeName, true);
        }
        else {
        	resolvedElementTypeSignature = Signature.createTypeSignature(resolvedElementTypeName, true).replace('.', '/');
        }

        return Signature.createArraySignature(resolvedElementTypeSignature, count);
    }

    /**
     * Returns the resource associated with the specified editor part
     * @param editor the currently active editor part
     * @return the corresponding <code>IResource</code> from the editor part
     */
    protected static IResource getResource(IEditorPart editor) {
        IEditorInput editorInput = editor.getEditorInput();
        IResource resource = (IResource) editorInput.getAdapter(IFile.class);
        if (resource == null) {
            resource = ResourcesPlugin.getWorkspace().getRoot();
        }
        return resource;
    }

    /**
     * Returns a handle to the specified method or <code>null</code> if none.
     * 
     * @param editorPart
     *            the editor containing the method
     * @param typeName
     * @param methodName
     * @param signature
     * @return handle or <code>null</code>
     */
    protected IMethod getMethodHandle(IEditorPart editorPart, String typeName, String methodName, String signature) throws CoreException {
        IJavaElement element = (IJavaElement) editorPart.getEditorInput().getAdapter(IJavaElement.class);
        IType type = null;
        if (element instanceof ICompilationUnit) {
            IType[] types = ((ICompilationUnit) element).getAllTypes();
            for (int i = 0; i < types.length; i++) {
                if (types[i].getFullyQualifiedName().equals(typeName)) {
                    type = types[i];
                    break;
                }
            }
        } else if (element instanceof IClassFile) {
            type = ((IClassFile) element).getType();
        }
        if (type != null) {
            String[] sigs = Signature.getParameterTypes(signature);
            return type.getMethod(methodName, sigs);
        }
        return null;
    }

    /**
     * Returns the compilation unit from the editor
     * @param editor the editor to get the compilation unit from
     * @return the compilation unit or <code>null</code>
     * @throws CoreException
     */
    protected CompilationUnit parseCompilationUnit(ITextEditor editor) throws CoreException {
        IEditorInput editorInput = editor.getEditorInput();
        IDocumentProvider documentProvider = editor.getDocumentProvider();
        if (documentProvider == null) {
            throw new CoreException(Status.CANCEL_STATUS);
        }
        IDocument document = documentProvider.getDocument(editorInput);
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(document.get().toCharArray());
        return (CompilationUnit) parser.createAST(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            return isFields(ss);
        }
        return (selection instanceof ITextSelection) && isField((ITextSelection) selection, part);
    }
    
    /**
     * Returns a selection of the member in the given text selection, or the
     * original selection if none.
     * 
     * @param part
     * @param selection
     * @return a structured selection of the member in the given text selection,
     *         or the original selection if none
     * @exception CoreException
     *                if an exception occurs
     */
    protected ISelection translateToMembers(IWorkbenchPart part, ISelection selection) throws CoreException {
    	ITextEditor textEditor = getTextEditor(part);
        if (textEditor != null && selection instanceof ITextSelection) {
            ITextSelection textSelection = (ITextSelection) selection;
            IEditorInput editorInput = textEditor.getEditorInput();
            IDocumentProvider documentProvider = textEditor.getDocumentProvider();
            if (documentProvider == null) {
                throw new CoreException(Status.CANCEL_STATUS);
            }
            IDocument document = documentProvider.getDocument(editorInput);
            int offset = textSelection.getOffset();
            if (document != null) {
                try {
                    IRegion region = document.getLineInformationOfOffset(offset);
                    int end = region.getOffset() + region.getLength();
                    while (Character.isWhitespace(document.getChar(offset)) && offset < end) {
                        offset++;
                    }
                } catch (BadLocationException e) {}
            }
            IJavaElement m = null;
            IClassFile classFile = (IClassFile) editorInput.getAdapter(IClassFile.class);
            if (classFile != null) {
                IJavaElement e = classFile.getElementAt(offset);
                if (e instanceof IMember) {
                    m = (IMember) e;
                }
                else m=classFile;
            } else {
                IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
                ICompilationUnit unit = manager.getWorkingCopy(editorInput);
                if (unit != null) {
                    synchronized (unit) {
                        unit.reconcile(ICompilationUnit.NO_AST , false, null, null);
                    }
                }
                else {
                	unit = ((WorkingCopyManager)JavaUI.getWorkingCopyManager()).getWorkingCopy(editorInput, false);
                	if(unit != null) {
	                	synchronized (unit) {
	                		unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
	                	}
                	}
                }
                IJavaElement e = unit.getElementAt(offset);
                if (e instanceof IMember) {
                    m = (IMember) e;
                }
                else 
                	m=unit;
            }
            if (m != null) {
                return new StructuredSelection(m);
            }
        }
        return selection;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#toggleBreakpoints(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
    			toggleLineBreakpoints(part, selection, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#canToggleBreakpoints(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
    	if (isRemote(part, selection)) {
    		return false;
    	}    	
        return canToggleLineBreakpoints(part, selection);
    }

	public void toggleMethodBreakpoints(IWorkbenchPart part,
			ISelection selection) throws CoreException {
	}

	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection)
			throws CoreException {

	}
}
