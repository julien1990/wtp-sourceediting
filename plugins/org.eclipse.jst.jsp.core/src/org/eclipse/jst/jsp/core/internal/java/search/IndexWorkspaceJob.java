/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.jsp.core.internal.java.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jst.jsp.core.internal.JSPCorePlugin;
import org.eclipse.wst.common.encoding.content.IContentTypeIdentifier;

/**
 * Re-indexes the entire workspace.
 * Ensures the JSP Index is in a stable state before performing a search.
 * (like after a crash or if previous indexing was canceled)
 * 
 * @author pavery
 */
public class IndexWorkspaceJob extends Job {

	// for debugging
	static final boolean DEBUG;
	static {
		String value= Platform.getDebugOption("org.eclipse.jst.jsp.core/debug/jspindexmanager"); //$NON-NLS-1$
		DEBUG= value != null && value.equalsIgnoreCase("true"); //$NON-NLS-1$
	}
	
	/**
	 * Visitor that retrieves jsp project paths for all jsp files in the workspace,
	 * and adds the files to be indexed as they are encountered
	 */
	private class JSPFileVisitor implements IResourceProxyVisitor {
		// monitor from the Job
		IProgressMonitor fInnerMonitor = null;
		public JSPFileVisitor(IProgressMonitor monitor) {
			this.fInnerMonitor = monitor;
		}
		
		public boolean visit(IResourceProxy proxy) throws CoreException {
			
			// check job canceled
			if (this.fInnerMonitor != null && this.fInnerMonitor.isCanceled()) {
				setCanceledState();
				return false;
			}
			
			// check search support canceled
			if(JSPSearchSupport.getInstance().isCanceled()) {
				setCanceledState();
				return false;
			}
			
			if (proxy.getType() == IResource.FILE) {
				
				// https://w3.opensource.ibm.com/bugzilla/show_bug.cgi?id=3553
				// check this before description
				// check name before actually getting the file (less work)
				if(getJspContentType().isAssociatedWith(proxy.getName())) {
					IFile file = (IFile) proxy.requestResource();
					if(file.exists()) {
						
						if(DEBUG)
							System.out.println("(+) IndexWorkspaceJob adding file: " + file.getName());
						// this call will check the ContentTypeDescription, so don't need to do it here.
						JSPSearchSupport.getInstance().addJspFile(file);
						this.fInnerMonitor.subTask(proxy.getName());
						
						// don't search deeper for files
						return false;
					}
				}
			}
			return true;
		}
	}
	
	private IContentType fContentTypeJSP = null;
	
	public IndexWorkspaceJob() {
		// pa_TODO may want to say something like "Rebuilding JSP Index" to be more
		// descriptive instead of "Updating JSP Index" since they are 2 different things
		super(JSPCorePlugin.getResourceString("%JSPIndexManager.0"));
		setPriority(Job.LONG);
		setSystem(true);
	}

	IContentType getJspContentType() {
		if(this.fContentTypeJSP == null)
			this.fContentTypeJSP = Platform.getContentTypeManager().getContentType(IContentTypeIdentifier.ContentTypeID_JSP);
		return this.fContentTypeJSP;
	}
	
	/**
	 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		
		IStatus status = Status.OK_STATUS;
		
		if(monitor.isCanceled()) {
			setCanceledState();
			return Status.CANCEL_STATUS;
		}
		
		if(DEBUG)
			System.out.println(" ^ IndexWorkspaceJob started: "); //$NON-NLS-1$
		
		long start = System.currentTimeMillis();
		
		try {
			ResourcesPlugin.getWorkspace().getRoot().accept(new JSPFileVisitor(monitor), IResource.DEPTH_INFINITE);
		}
		catch (CoreException e) {
			if(DEBUG)
				e.printStackTrace();
		}
		finally {
			if(monitor != null)
				monitor.done();
		}
		long finish = System.currentTimeMillis();
		if(DEBUG)
			System.out.println(" ^ IndexWorkspaceJob finished\n   total time running: " + (finish - start)); //$NON-NLS-1$
		
		return status;
	}
	
	void setCanceledState() {
		JSPIndexManager.getInstance().setIndexState(JSPIndexManager.S_CANCELED);
	}
}
