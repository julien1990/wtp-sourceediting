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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jst.jsp.core.internal.JSPCoreMessages;
import org.eclipse.jst.jsp.core.internal.JSPCorePlugin;
import org.eclipse.jst.jsp.core.internal.Logger;
import org.eclipse.jst.jsp.core.internal.provisional.contenttype.ContentTypeIdForJSP;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

/**
 * Responsible for keeping the JSP index up to date.
 * 
 * @author pavery
 */
public class JSPIndexManager implements IResourceChangeListener {

	// for debugging
	static final boolean DEBUG;
	static {
		String value = Platform.getDebugOption("org.eclipse.jst.jsp.core/debug/jspindexmanager"); //$NON-NLS-1$
		DEBUG = value != null && value.equalsIgnoreCase("true"); //$NON-NLS-1$
	}

	private static final String PKEY_INDEX_STATE = "jspIndexState"; //$NON-NLS-1$

	final IndexWorkspaceJob indexingJob = new IndexWorkspaceJob();



	// set to S_UPDATING once a resource change comes in
	// set to S_STABLE if:
	// - we know we aren't interested in the resource change
	// - or the ProcessFilesJob completes
	// set to S_CANCELED if an indexing job is canceled
	// set to S_REBUILDING if re-indexing the entire workspace

	// the int '0' is reserved for the default value if a preference is not
	// there
	/** index is reliable to use */
	public static final int S_STABLE = 1;
	/** index is being updated (from a resource delta) */
	public static final int S_UPDATING = 2;
	/** entire index is being rebuilt */
	public static final int S_REBUILDING = 3;
	/**
	 * indexing job was canceled in the middle of it, index needs to be
	 * rebuilt
	 */
	public static final int S_CANCELED = 4;

	/** symbolic name for OSGI framework */
	private final String OSGI_FRAMEWORK_ID = "org.eclipse.osgi"; //$NON-NLS-1$

	/**
	 * Collects JSP files from a resource delta.
	 */
	private class JSPResourceVisitor implements IResourceDeltaVisitor {
		// using hash map ensures only one of each file
		// must be reset before every use
		private HashMap jspFiles = null;

		public JSPResourceVisitor() {
			this.jspFiles = new HashMap();
		}

		public boolean visit(IResourceDelta delta) throws CoreException {

			// in case JSP search was canceled (eg. when closing the editor)
			if (JSPSearchSupport.getInstance().isCanceled() || frameworkIsShuttingDown()) {
				setCanceledState();
				return false;
			}

			try {
				if (!isHiddenResource(delta.getFullPath())) {

					int kind = delta.getKind();
					boolean added = (kind & IResourceDelta.ADDED) == IResourceDelta.ADDED;
					boolean isInterestingChange = false;
					if ((kind & IResourceDelta.CHANGED) == IResourceDelta.CHANGED) {
						int flags = delta.getFlags();
						// ignore things like marker changes
						isInterestingChange = (flags & IResourceDelta.CONTENT) == IResourceDelta.CONTENT || (flags & IResourceDelta.REPLACED) == IResourceDelta.REPLACED;
					}
					boolean removed = (kind & IResourceDelta.REMOVED) == IResourceDelta.REMOVED;
					if (added || isInterestingChange) {

						visitAdded(delta);
					}
					else if (removed) {
						visitRemoved(delta);
					}
				}
			}
			catch (Exception e) {
				// need to set state here somehow, and reindex
				// otherwise index will be unreliable
				if (DEBUG)
					Logger.logException("Delta analysis may not be complete", e); //$NON-NLS-1$
			}
			// if the delta has children, continue to add/remove files
			return true;
		}

		private void visitRemoved(IResourceDelta delta) {
			// handle cleanup
			if (delta.getResource() != null) {
				IResource r = delta.getResource();
				if (r.getType() == IResource.FOLDER && r.exists()) {
					deleteIndex((IFile) r);
				}
			}
		}

		private void visitAdded(IResourceDelta delta) {
			// https://w3.opensource.ibm.com/bugzilla/show_bug.cgi?id=3553
			// quick check if it's even JSP related to improve
			// performance
			// checking name from the delta before getting
			// resource because it's lighter
			int numSegments = delta.getFullPath().segmentCount();
			String filename = delta.getFullPath().segment(numSegments - 1);
			if (getJspContentType().isAssociatedWith(filename)) {
				IResource r = delta.getResource();
				if (r != null && r.exists() && r.getType() == IResource.FILE) {
					this.jspFiles.put(r.getFullPath(), r);
				}
			}
		}

		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=93463
		private boolean isHiddenResource(IPath p) {
			String[] segments = p.segments();
			for (int i = 0; i < segments.length; i++) {
				if (segments[i].startsWith(".")) //$NON-NLS-1$
					return true;
			}
			return false;
		}

		private void deleteIndex(IFile folder) {
			// cleanup index
			IndexManager im = JavaModelManager.getJavaModelManager().getIndexManager();
			IPath folderPath = folder.getFullPath();
			IPath indexLocation = JSPSearchSupport.getInstance().computeIndexLocation(folderPath);
			im.removeIndex(indexLocation);
			// im.indexLocations.removeKey(folderPath);
			// im.indexLocations.removeValue(indexLocation);
			File f = indexLocation.toFile();
			f.delete();
		}

		public IFile[] getFiles() {
			return (IFile[]) this.jspFiles.values().toArray(new IFile[this.jspFiles.size()]);
		}

		public void reset() {
			this.jspFiles.clear();
		}
	}

	// end class JSPResourceVisitor

	/**
	 * schedules JSP files for indexing by Java core
	 */
	private class ProcessFilesJob extends Job {
		List fileList = null;
		private final int maximumToRemember = 50;

		ProcessFilesJob(String taskName) {
			super(taskName);
			fileList = new ArrayList();
		}

		synchronized void process(IFile[] files) {
			for (int i = 0; i < files.length; i++) {
				fileList.add(files[i]);
			}
			if (DEBUG) {
				System.out.println("JSPIndexManager queuing " + files.length + " files"); //$NON-NLS-2$ //$NON-NLS-1$
			}
			schedule(20);
		}

		synchronized IFile[] getFiles() {
			IFile[] files = (IFile[]) fileList.toArray(new IFile[fileList.size()]);
			if (fileList.size() > maximumToRemember) {
				fileList = new ArrayList();
			}
			else {
				fileList.clear();
			}
			return files;
		}

		protected IStatus run(IProgressMonitor monitor) {
			// System.out.println("indexer monitor" + monitor);
			if (isCanceled(monitor) || frameworkIsShuttingDown()) {
				setCanceledState();
				return Status.CANCEL_STATUS;
			}

			long start = System.currentTimeMillis();

			try {
				IFile[] filesToBeProcessed = getFiles();
				if (DEBUG) {
					System.out.println("JSPIndexManager indexing " + filesToBeProcessed.length + " files"); //$NON-NLS-2$ //$NON-NLS-1$
				}
				// API indicates that monitor is never null
				monitor.beginTask("", filesToBeProcessed.length); //$NON-NLS-1$
				JSPSearchSupport ss = JSPSearchSupport.getInstance();
				String processingNFiles = ""; //$NON-NLS-1$


				for (int i = 0; i < filesToBeProcessed.length; i++) {

					if (isCanceled(monitor) || frameworkIsShuttingDown()) {
						setCanceledState();
						return Status.CANCEL_STATUS;
					}
					try {
						ss.addJspFile(filesToBeProcessed[i]);
						// JSP Indexer processing n files
						processingNFiles = NLS.bind(JSPCoreMessages.JSPIndexManager_2, new String[]{Integer.toString((filesToBeProcessed.length - i))});
						monitor.subTask(processingNFiles + " - " + filesToBeProcessed[i].getName()); //$NON-NLS-1$
						monitor.worked(1);

						if (DEBUG) {
							System.out.println("JSPIndexManager Job added file: " + filesToBeProcessed[i].getName()); //$NON-NLS-1$
						}
					}
					catch (Exception e) {
						// RATLC00284776
						// ISSUE: we probably shouldn't be catching EVERY
						// exception, but
						// the framework only allows to return IStatus in
						// order to communicate
						// that something went wrong, which means the loop
						// won't complete, and we would hit the same problem
						// the next time.
						// 
						// a possible solution is to keep track of the
						// exceptions logged
						// and only log a certain amt of the same one,
						// otherwise skip it.
						if (!frameworkIsShuttingDown()) {
							String filename = filesToBeProcessed[i] != null ? filesToBeProcessed[i].getFullPath().toString() : ""; //$NON-NLS-1$
							Logger.logException("JSPIndexer problem indexing:" + filename, e); //$NON-NLS-1$
						}
					}
				} // end for
			}
			finally {
				// just in case something didn't follow API (monitor is null)
				if (monitor != null)
					monitor.done();
			}

			long finish = System.currentTimeMillis();
			long diff = finish - start;
			if (DEBUG) {
				fTotalTime += diff;
				System.out.println("============================================================================"); //$NON-NLS-1$
				System.out.println("this time: " + diff + " cumulative time for resource changed: " + fTotalTime); //$NON-NLS-1$ //$NON-NLS-2$
				System.out.println("============================================================================"); //$NON-NLS-1$
			}
			return Status.OK_STATUS;
		}

		private boolean isCanceled(IProgressMonitor runMonitor) {

			boolean canceled = false;
			// check specific monitor passed into run method (the progress
			// group in this case)
			// check main search support canceled
			if (runMonitor != null && runMonitor.isCanceled())
				canceled = true;
			else if (JSPSearchSupport.getInstance().isCanceled()) {
				canceled = true;
				if (runMonitor != null) {
					runMonitor.setCanceled(true);
				}
			}
			return canceled;
		}
	}

	// end class ProcessFilesJob

	private static JSPIndexManager fSingleton = null;
	private JSPResourceVisitor fVisitor = null;
	private IContentType fContentTypeJSP = null;

	static long fTotalTime = 0;

	// Job for processing resource delta
	ProcessFilesJob processFilesJob = null;

	private JSPIndexManager() {
		processFilesJob = new ProcessFilesJob(JSPCoreMessages.JSPIndexManager_0);
		// only show in verbose mode
		processFilesJob.setSystem(true);
		processFilesJob.setPriority(Job.LONG);
		processFilesJob.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				super.done(event);
				setStableState();
			}
		});
	}

	public synchronized static JSPIndexManager getInstance() {

		if (fSingleton == null)
			fSingleton = new JSPIndexManager();
		return fSingleton;
	}

	/**
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {

		// ignore resource changes if already rebuilding
		if (getIndexState() == S_REBUILDING)
			return;
		// previously canceled, needs entire index rebuild
		if (getIndexState() == S_CANCELED) {
			rebuildIndex();
			return;
		}

		// set flag, so we know if a job is going to be started
		// and the state will eventually be set back to S_STABLE
		boolean beganProcess = false;
		setUpdatingState();

		IResourceDelta delta = event.getDelta();
		if (delta != null) {
			// only care about adds or changes right now...
			int kind = delta.getKind();
			boolean added = (kind & IResourceDelta.ADDED) == IResourceDelta.ADDED;
			boolean changed = (kind & IResourceDelta.CHANGED) == IResourceDelta.CHANGED;
			if (added || changed) {

				// only analyze the full (starting at root) delta hierarchy
				if (delta.getFullPath().toString().equals("/")) { //$NON-NLS-1$
					try {
						JSPResourceVisitor v = getVisitor();
						// clear from last run
						v.reset();
						// count files, possibly do this in a job too...
						// don't include PHANTOM resources
						delta.accept(v, false);

						// process files from this delta
						IFile[] files = v.getFiles();
						if (files.length > 0) {
							// processFiles(files);
							indexFiles(files);
							beganProcess = true;
						}
					}
					catch (CoreException e) {
						// need to set state here somehow, and reindex
						// otherwise index will be unreliable
						if (DEBUG)
							Logger.logException(e);
					}
					catch (Exception e) {
						// need to set state here somehow, and reindex
						// otherwise index will be unreliable
						if (DEBUG)
							Logger.logException(e);
					}
				}
			}

		}
		// if we never kicked off process, job won't set back to stable
		// so we set it here
		if (!beganProcess) {
			setStableState();
		}
	}

	public synchronized void setIndexState(int state) {
		if (DEBUG) {
			System.out.println("JSPIndexManager setting index state to: " + state2String(state)); //$NON-NLS-1$
		}
		Plugin jspModelPlugin = JSPCorePlugin.getDefault();
		jspModelPlugin.getPluginPreferences().setValue(PKEY_INDEX_STATE, state);
		jspModelPlugin.savePluginPreferences();

	}

	private String state2String(int state) {
		String s = "UNKNOWN"; //$NON-NLS-1$
		switch (state) {
			case (S_STABLE) :
				s = "S_STABLE"; //$NON-NLS-1$
				break;
			case (S_UPDATING) :
				s = "S_UPDATING"; //$NON-NLS-1$
				break;
			case (S_CANCELED) :
				s = "S_CANCELED"; //$NON-NLS-1$
				break;
			case (S_REBUILDING) :
				s = "S_REBUILDING"; //$NON-NLS-1$
				break;
		}
		return s;
	}

	public int getIndexState() {
		return JSPCorePlugin.getDefault().getPluginPreferences().getInt(PKEY_INDEX_STATE);
	}

	public void setUpdatingState() {
		if (getIndexState() != S_CANCELED)
			setIndexState(S_UPDATING);
	}

	public void setCanceledState() {
		setIndexState(JSPIndexManager.S_CANCELED);
	}

	// ca
	public void setStableState() {
		if (getIndexState() != S_CANCELED)
			setIndexState(S_STABLE);
	}

	public void setRebuildingState() {
		setIndexState(S_REBUILDING);
	}

	public synchronized void rebuildIndexIfNeeded() {
		if (getIndexState() != S_STABLE) {
			rebuildIndex();
		}
	}

	private void rebuildIndex() {

		if (DEBUG)
			System.out.println("*** JSP Index unstable, requesting re-indexing"); //$NON-NLS-1$

		indexingJob.addJobChangeListener(new JobChangeAdapter() {
			public void aboutToRun(IJobChangeEvent event) {
				super.aboutToRun(event);
				setRebuildingState();
			}

			public void done(IJobChangeEvent event) {
				super.done(event);
				setStableState();
				indexingJob.removeJobChangeListener(this);
			}
		});
		indexingJob.schedule();

	}

	/**
	 * Creates and schedules a Job to process collected files. All JSP
	 * indexing should be done through this method or processFiles(IFile file)
	 * 
	 * @param files
	 */
	public final void indexFiles(IFile[] files) {
		// don't use this rule
		// https://w3.opensource.ibm.com/bugzilla/show_bug.cgi?id=4931
		// processFiles.setRule(new IndexFileRule());
		processFilesJob.process(files);
	}


	/**
	 * Package protected for access by inner Job class in resourceChanged(...)
	 * 
	 * @return
	 */
	JSPResourceVisitor getVisitor() {

		if (this.fVisitor == null) {
			this.fVisitor = new JSPResourceVisitor();
		}
		return this.fVisitor;
	}

	// https://w3.opensource.ibm.com/bugzilla/show_bug.cgi?id=5091
	// makes sure IndexManager is aware of our indexes
	public void saveIndexes() {
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		IPath jspModelWorkingLocation = JSPSearchSupport.getInstance().getModelJspPluginWorkingLocation();

		File folder = new File(jspModelWorkingLocation.toOSString());
		String[] files = folder.list();
		String locay = ""; //$NON-NLS-1$
		Index index = null;
		try {
			for (int i = 0; i < files.length; i++) {
				if (files[i].toLowerCase().endsWith(".index")) { //$NON-NLS-1$
					locay = jspModelWorkingLocation.toString() + "/" + files[i]; //$NON-NLS-1$
					index = new Index(locay, "Index for " + locay, true /*
																		 * reuse
																		 * index
																		 * file
																		 */); //$NON-NLS-1$
					indexManager.saveIndex(index);
				}
			}
		}
		catch (Exception e) {
			// we should be shutting down, want to shut down quietly
			if (DEBUG)
				e.printStackTrace();
		}
	}

	IContentType getJspContentType() {
		if (this.fContentTypeJSP == null)
			this.fContentTypeJSP = Platform.getContentTypeManager().getContentType(ContentTypeIdForJSP.ContentTypeID_JSP);
		return this.fContentTypeJSP;
	}

	/**
	 * A check to see if the OSGI framework is shutting down.
	 * 
	 * @return true if the System Bundle is stopped (ie. the framework is
	 *         shutting down)
	 */
	boolean frameworkIsShuttingDown() {
		// in the Framework class there's a note:
		// set the state of the System Bundle to STOPPING.
		// this must be done first according to section 4.19.2 from the OSGi
		// R3 spec.
		boolean shuttingDown = Platform.getBundle(OSGI_FRAMEWORK_ID).getState() == Bundle.STOPPING;
		if (DEBUG && shuttingDown) {
			System.out.println("JSPIndexManager: system is shutting down!"); //$NON-NLS-1$
		}
		return shuttingDown;
	}


	public void shutdown() {
		int maxwait = 5000;
		if (processFilesJob != null) {
			processFilesJob.cancel();
		}
		// attempt to make sure this indexing job is litterally
		// done before continuing, since we are shutting down
		waitTillNotRunning(maxwait, processFilesJob);
		
		if (indexingJob != null) {
			indexingJob.cancel();
		}
		waitTillNotRunning(maxwait, processFilesJob);
	}

	private void waitTillNotRunning(int maxSeconds, Job job) {
		int pauseTime = 10;
		int maxtries = maxSeconds / pauseTime;
		int count = 0;
		while (count++ < maxtries && job.getState() == Job.RUNNING) {
			try {
				Thread.sleep(pauseTime);
				//System.out.println("count: " + count + " max: " + maxtries);
			}
			catch (InterruptedException e) {
				Logger.logException(e);
			}
		}
	}
}