/*******************************************************************************
 * Copyright (c) 2005,2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jst.jsp.core.taglib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.jsp.core.internal.Logger;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TLDCMDocumentManager;
import org.eclipse.wst.sse.core.utils.StringUtils;
import org.osgi.framework.Bundle;

/**
 * A non-extendable index manager for taglibs similar to the previous J2EE
 * ITaglibRegistry but lacking any ties to project natures. Each record
 * returned from the index represents a single tag library descriptor.
 * 
 * Indexing is not persisted between sessions, so new ADD events will be sent
 * to ITaglibIndexListeners during each workbench session. REMOVE events are
 * not fired on workbench shutdown. The record's contents should be examined
 * for any further information.
 * 
 * @since 1.0
 */
public final class TaglibIndex {
	class ClasspathChangeListener implements IElementChangedListener {
		Stack classpathStack = new Stack();
		List projectsIndexed = new ArrayList(1);

		public void elementChanged(ElementChangedEvent event) {
			if (!isIndexAvailable())
				return;
			try {
				LOCK.acquire();
				classpathStack.clear();
				projectsIndexed.clear();
				elementChanged(event.getDelta());
			}
			finally {
				LOCK.release();
			}
		}

		private void elementChanged(IJavaElementDelta delta) {
			if (frameworkIsShuttingDown())
				return;

			if (delta.getElement().getElementType() == IJavaElement.JAVA_MODEL) {
				IJavaElementDelta[] changed = delta.getChangedChildren();
				for (int i = 0; i < changed.length; i++) {
					elementChanged(changed[i]);
				}
			}
			else if (delta.getElement().getElementType() == IJavaElement.JAVA_PROJECT) {
				if ((delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) {
					IJavaElement proj = delta.getElement();
					handleClasspathChange((IJavaProject) proj);
				}
			}
		}

		private void handleClasspathChange(IJavaProject project) {
			if (frameworkIsShuttingDown())
				return;

			/*
			 * Loops in the build paths could cause us to pop more than we
			 * push
			 */
			if (classpathStack.contains(project.getElementName()))
				return;

			classpathStack.push(project.getElementName());
			try {
				/* Handle changes to this project's build path */
				IResource resource = project.getCorrespondingResource();
				if (resource.getType() == IResource.PROJECT && !projectsIndexed.contains(resource)) {
					ProjectDescription description = getDescription((IProject) resource);
					if (description != null && !frameworkIsShuttingDown()) {
						projectsIndexed.add(resource);
						description.indexClasspath();
					}
				}
				/*
				 * Update indeces for projects who include this project in
				 * their build path (e.g. toggling the "exportation" of a
				 * taglib JAR in this project affects the JAR's visibility in
				 * other projects)
				 */
				IJavaProject[] projects = project.getJavaModel().getJavaProjects();
				for (int i = 0; i < projects.length; i++) {
					IJavaProject otherProject = projects[i];
					if (StringUtils.contains(otherProject.getRequiredProjectNames(), project.getElementName(), false) && !classpathStack.contains(otherProject.getElementName()) && !frameworkIsShuttingDown()) {
						handleClasspathChange(otherProject);
					}
				}
			}
			catch (JavaModelException e) {
				Logger.logException(e);
			}
			classpathStack.pop();

		}
	}

	class ResourceChangeListener implements IResourceChangeListener {
		public void resourceChanged(IResourceChangeEvent event) {
			if (!isIndexAvailable())
				return;
			try {
				LOCK.acquire();
				switch (event.getType()) {
					case IResourceChangeEvent.PRE_CLOSE :
					case IResourceChangeEvent.PRE_DELETE : {
						try {
							// pair deltas with projects
							IResourceDelta[] deltas = new IResourceDelta[]{event.getDelta()};
							IProject[] projects = null;

							if (deltas != null && deltas.length > 0) {
								IResource resource = null;
								if (deltas[0] != null) {
									resource = deltas[0].getResource();
								}
								else {
									resource = event.getResource();
								}

								if (resource != null) {
									if (resource.getType() == IResource.ROOT) {
										deltas = deltas[0].getAffectedChildren();
										projects = new IProject[deltas.length];
										for (int i = 0; i < deltas.length; i++) {
											if (deltas[i].getResource().getType() == IResource.PROJECT) {
												projects[i] = (IProject) deltas[i].getResource();
											}
										}
									}
									else {
										projects = new IProject[1];
										if (resource.getType() != IResource.PROJECT) {
											projects[0] = resource.getProject();
										}
										else {
											projects[0] = (IProject) resource;
										}
									}
								}
								for (int i = 0; i < projects.length; i++) {
									if (_debugIndexCreation) {
										Logger.log(Logger.INFO_DEBUG, "TaglibIndex noticed " + projects[i].getName() + " is about to be deleted/closed"); //$NON-NLS-1$ //$NON-NLS-2$
									}
									ProjectDescription description = (ProjectDescription) fProjectDescriptions.remove(projects[i]);
									if (description != null) {
										if (_debugIndexCreation) {
											Logger.log(Logger.INFO_DEBUG, "removing index of " + description.fProject.getName()); //$NON-NLS-1$
										}
										description.clear();
									}
								}
							}
						}
						catch (Exception e) {
							Logger.logException("Exception while processing resource deletion", e); //$NON-NLS-1$
						}
					}
					case IResourceChangeEvent.POST_CHANGE : {
						try {
							// pair deltas with projects
							IResourceDelta[] deltas = new IResourceDelta[]{event.getDelta()};
							IProject[] projects = null;

							if (deltas != null && deltas.length > 0) {
								IResource resource = null;
								if (deltas[0] != null) {
									resource = deltas[0].getResource();
								}
								else {
									resource = event.getResource();
								}

								if (resource != null) {
									if (resource.getType() == IResource.ROOT) {
										deltas = deltas[0].getAffectedChildren();
										projects = new IProject[deltas.length];
										for (int i = 0; i < deltas.length; i++) {
											if (deltas[i].getResource().getType() == IResource.PROJECT) {
												projects[i] = (IProject) deltas[i].getResource();
											}
										}
									}
									else {
										projects = new IProject[1];
										if (resource.getType() != IResource.PROJECT) {
											projects[0] = resource.getProject();
										}
										else {
											projects[0] = (IProject) resource;
										}
									}
								}
								for (int i = 0; i < projects.length; i++) {
									try {
										if (deltas[i] != null && deltas[i].getKind() != IResourceDelta.REMOVED && projects[i].isAccessible()) {
											ProjectDescription description = getDescription(projects[i]);
											if (description != null && !frameworkIsShuttingDown()) {
												deltas[i].accept(description.getVisitor());
											}
										}
										if (!projects[i].isAccessible() || (deltas[i] != null && deltas[i].getKind() == IResourceDelta.REMOVED)) {
											if (_debugIndexCreation) {
												Logger.log(Logger.INFO_DEBUG, "TaglibIndex noticed " + projects[i].getName() + " was removed or is no longer accessible"); //$NON-NLS-1$ //$NON-NLS-2$
											}
											ProjectDescription description = (ProjectDescription) fProjectDescriptions.remove(projects[i]);
											if (description != null) {
												if (_debugIndexCreation) {
													Logger.log(Logger.INFO_DEBUG, "removing index of " + description.fProject.getName()); //$NON-NLS-1$
												}
												description.clear();
											}
										}
									}
									catch (CoreException e) {
										Logger.logException(e);
									}
								}
							}
						}
						catch (Exception e) {
							Logger.logException("Exception while processing resource change", e); //$NON-NLS-1$
						}
					}
				}
			}
			finally {
				LOCK.release();
			}
		}
	}

	static final boolean _debugChangeListener = false;

	static boolean _debugEvents = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jst.jsp.core/taglib/events")); //$NON-NLS-1$ //$NON-NLS-2$

	static boolean _debugIndexCreation = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jst.jsp.core/taglib/indexcreation")); //$NON-NLS-1$ //$NON-NLS-2$

	static final boolean _debugResolution = "true".equals(Platform.getDebugOption("org.eclipse.jst.jsp.core/taglib/resolve")); //$NON-NLS-1$ //$NON-NLS-2$

	static TaglibIndex _instance;

	static boolean ENABLED = false;
	static ILock LOCK = null;

	/**
	 * NOT API.
	 * 
	 * @param listener
	 *            the listener to be added
	 */
	public static void addTaglibIndexListener(ITaglibIndexListener listener) {
		try {
			LOCK.acquire();
			_instance.internalAddTaglibIndexListener(listener);
		}
		finally {
			LOCK.release();
		}
	}

	static void fireTaglibRecordEvent(ITaglibRecordEvent event) {
		if (_debugEvents) {
			Logger.log(Logger.INFO_DEBUG, "TaglibIndex fired event:" + event); //$NON-NLS-1$
		}
		/*
		 * Flush any shared cache entries, the TaglibControllers should handle
		 * updating their documents as needed.
		 */
		TLDCMDocumentManager.getSharedDocumentCache().remove(TLDCMDocumentManager.getUniqueIdentifier(event.getTaglibRecord()));

		ITaglibIndexListener[] listeners = _instance.fTaglibIndexListeners;
		if (listeners != null) {
			for (int i = 0; i < listeners.length; i++) {
				try {
					listeners[i].indexChanged(event);
				}
				catch (Exception e) {
					Logger.log(Logger.WARNING, e.getMessage());
				}
			}
		}
	}

	/**
	 * Finds all of the visible ITaglibRecords for the given path in the
	 * workspace. Taglib mappings from web.xml files are only visible to paths
	 * within the web.xml's corresponding web content folder.
	 * <p>
	 * Values defined within the XML Catalog will not be returned.
	 * </p>
	 * 
	 * @param fullPath -
	 *            a path within the workspace
	 * @return All of the visible ITaglibRecords from the given path.
	 */
	public static ITaglibRecord[] getAvailableTaglibRecords(IPath fullPath) {
		ITaglibRecord[] records = _instance.internalGetAvailableTaglibRecords(fullPath);
		return records;
	}

	/**
	 * Returns the IPath considered to be the web-app root for the given path.
	 * All resolution from the given path beginning with '/' will be relative
	 * to the computed web-app root.
	 * 
	 * @deprecated - is not correct in flexible projects
	 * @param path -
	 *            a path under the web-app root
	 * @return the IPath considered to be the web-app's root for the given
	 *         path
	 */
	public static IPath getContextRoot(IPath path) {
		try {
			LOCK.acquire();
			return _instance.internalGetContextRoot(path);
		}
		finally {
			LOCK.release();
		}
	}

	/**
	 * NOT API.
	 * 
	 * @param listener
	 *            the listener to be removed
	 */
	public static void removeTaglibIndexListener(ITaglibIndexListener listener) {
		try {
			LOCK.acquire();
			_instance.internalRemoveTaglibIndexListener(listener);
		}
		finally {
			LOCK.release();
		}
	}

	/**
	 * Finds a matching ITaglibRecord given the reference. Typically the
	 * result will have to be cast to a subiinterface of ITaglibRecord.
	 * 
	 * @param basePath -
	 *            the workspace-relative path for IResources, full filesystem
	 *            path otherwise
	 * @param reference -
	 *            the URI to lookup, for example the uri value from a taglib
	 *            directive
	 * @param crossProjects -
	 *            whether to search across projects (currently ignored)
	 * 
	 * @return a visible ITaglibRecord or null if the reference points to no
	 *         known tag library descriptor
	 * 
	 * @See ITaglibRecord
	 */
	public static ITaglibRecord resolve(String basePath, String reference, boolean crossProjects) {
		ITaglibRecord result = null;
		try {
			LOCK.acquire();
			result = _instance.internalResolve(basePath, reference, crossProjects);
		}
		finally {
			LOCK.release();
		}
		if (_debugResolution) {
			if (result == null) {
				Logger.log(Logger.INFO_DEBUG, "TaglibIndex could not resolve \"" + reference + "\" from " + basePath); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				switch (result.getRecordType()) {
					case (ITaglibRecord.TLD) : {
						ITLDRecord record = (ITLDRecord) result;
						Logger.log(Logger.INFO_DEBUG, "TaglibIndex resolved " + basePath + ":" + reference + " = " + record.getPath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
						break;
					case (ITaglibRecord.JAR) : {
						IJarRecord record = (IJarRecord) result;
						Logger.log(Logger.INFO_DEBUG, "TaglibIndex resolved " + basePath + ":" + reference + " = " + record.getLocation()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
						break;
					case (ITaglibRecord.TAGDIR) : {
					}
						break;
					case (ITaglibRecord.URL) : {
						IURLRecord record = (IURLRecord) result;
						Logger.log(Logger.INFO_DEBUG, "TaglibIndex resolved " + basePath + ":" + reference + " = " + record.getURL()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
						break;
				}
			}
		}
		return result;
	}

	/**
	 * Instructs the index to stop listening for resource and classpath
	 * changes, and to forget all information about the workspace.
	 */
	public static synchronized void shutdown() {
		if (_instance != null) {
			_instance.stop();
		}
		_instance = null;
	}

	/**
	 * Instructs the index to begin listening for resource and classpath
	 * changes.
	 */
	public static synchronized void startup() {
		ENABLED = !"false".equalsIgnoreCase(System.getProperty(TaglibIndex.class.getName())); //$NON-NLS-1$
		_instance = new TaglibIndex();
	}

	private ClasspathChangeListener fClasspathChangeListener = null;

	Map fProjectDescriptions;

	private ResourceChangeListener fResourceChangeListener;

	private ITaglibIndexListener[] fTaglibIndexListeners = null;
	/** symbolic name for OSGI framework */
	private final String OSGI_FRAMEWORK_ID = "org.eclipse.osgi"; //$NON-NLS-1$

	private TaglibIndex() {
		super();
		fResourceChangeListener = new ResourceChangeListener();
		fClasspathChangeListener = new ClasspathChangeListener();
		if (ENABLED) {
			ResourcesPlugin.getWorkspace().addResourceChangeListener(fResourceChangeListener, IResourceChangeEvent.POST_CHANGE);
			JavaCore.addElementChangedListener(fClasspathChangeListener);
		}
		LOCK = Platform.getJobManager().newLock();
		fProjectDescriptions = new Hashtable();
	}

	/**
	 * @param project
	 * @return
	 */
	ProjectDescription createDescription(IProject project) {
		ProjectDescription description = null;
		description = (ProjectDescription) fProjectDescriptions.get(project);
		if (description == null) {
			description = new ProjectDescription(project);
			if (ENABLED) {
				description.index();
				description.indexClasspath();
			}
			fProjectDescriptions.put(project, description);
		}
		return description;
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
		boolean shuttingDown = !Platform.isRunning() || Platform.getBundle(OSGI_FRAMEWORK_ID).getState() == Bundle.STOPPING;
		return shuttingDown;
	}

	ProjectDescription getDescription(IProject project) {
		ProjectDescription description = null;
		description = (ProjectDescription) fProjectDescriptions.get(project);
		return description;
	}

	private void internalAddTaglibIndexListener(ITaglibIndexListener listener) {
		if (fTaglibIndexListeners == null) {
			fTaglibIndexListeners = new ITaglibIndexListener[]{listener};
		}
		else {
			List listeners = new ArrayList(Arrays.asList(fTaglibIndexListeners));
			listeners.add(listener);
			fTaglibIndexListeners = (ITaglibIndexListener[]) listeners.toArray(new ITaglibIndexListener[0]);
		}
	}

	private ITaglibRecord[] internalGetAvailableTaglibRecords(IPath path) {
		ITaglibRecord[] records = new ITaglibRecord[0];
		if (path.segmentCount() > 0) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
			ProjectDescription description = createDescription(project);
			List availableRecords = description.getAvailableTaglibRecords(path);
			records = (ITaglibRecord[]) availableRecords.toArray(records);
		}
		return records;
	}

	private IPath internalGetContextRoot(IPath path) {
		IFile baseResource = FileBuffers.getWorkspaceFileAtLocation(path);
		if (baseResource != null) {
			IProject project = baseResource.getProject();
			ProjectDescription description = _instance.createDescription(project);
			IPath rootPath = description.getLocalRoot(baseResource.getFullPath());
			return rootPath;
		}
		// try to handle out-of-workspace paths
		IPath root = path;
		while (root != null && !root.isRoot())
			root = root.removeLastSegments(1);
		if (root == null)
			root = path;
		return root;
	}

	private void internalRemoveTaglibIndexListener(ITaglibIndexListener listener) {
		if (fTaglibIndexListeners != null) {
			List listeners = new ArrayList(Arrays.asList(fTaglibIndexListeners));
			listeners.remove(listener);
			fTaglibIndexListeners = (ITaglibIndexListener[]) listeners.toArray(new ITaglibIndexListener[0]);
		}
	}

	private ITaglibRecord internalResolve(String basePath, final String reference, boolean crossProjects) {
		IProject project = null;
		ITaglibRecord resolved = null;

		Path baseIPath = new Path(basePath);
		IResource baseResource = FileBuffers.getWorkspaceFileAtLocation(baseIPath);

		if (baseResource == null) {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			// Try the base path as a folder first
			if (baseResource == null && baseIPath.segmentCount() > 1) {
				baseResource = workspaceRoot.getFolder(baseIPath);
			}
			// If not a folder, then try base path as a file
			if (baseResource != null && !baseResource.exists() && baseIPath.segmentCount() > 1) {
				baseResource = workspaceRoot.getFile(baseIPath);
			}
			if (baseResource == null && baseIPath.segmentCount() == 1) {
				baseResource = workspaceRoot.getProject(baseIPath.segment(0));
			}
		}

		if (baseResource == null) {
			/*
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=116529
			 * 
			 * This method produces a less accurate result, but doesn't
			 * require that the file exist yet.
			 */
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(baseIPath);
			if (files.length > 0)
				baseResource = files[0];
		}
		if (baseResource != null) {
			project = baseResource.getProject();
			ProjectDescription description = createDescription(project);
			resolved = description.resolve(basePath, reference);
		}
		return resolved;
	}

	boolean isIndexAvailable() {
		return _instance != null && ENABLED;
	}

	private void stop() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(fResourceChangeListener);
		JavaCore.removeElementChangedListener(fClasspathChangeListener);
		fProjectDescriptions.clear();
	}
}
