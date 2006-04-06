/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *******************************************************************************/
package org.eclipse.wst.sse.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverPlugin;
import org.eclipse.wst.common.uriresolver.internal.util.URIHelper;
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.IModelHandler;
import org.eclipse.wst.sse.core.internal.model.AbstractStructuredModel;
import org.eclipse.wst.sse.core.internal.modelhandler.ModelHandlerRegistry;
import org.eclipse.wst.sse.core.internal.provisional.IModelLoader;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.exceptions.ResourceInUse;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.util.URIResolver;


public class FileBufferModelManager {

	static class DocumentInfo {
		/**
		 * The ITextFileBuffer
		 */
		ITextFileBuffer buffer = null;

		/**
		 * The platform content-type ID of this document
		 */
		String contentTypeID = null;

		/**
		 * The IStructureModel containing this document; might be null at
		 * points in the ITextFileBuffer's lifecycle
		 */
		IStructuredModel model = null;

		/**
		 * Whether FileBufferModelManager called connect() for this
		 * DocumentInfo's text filebuffer
		 */
		boolean selfConnected = false;

		int bufferReferenceCount = 0;
		int modelReferenceCount = 0;
	}

	/**
	 * A URIResolver instance of models built on java.io.Files
	 */
	class ExternalURIResolver implements URIResolver {
		IPath fLocation;

		ExternalURIResolver(IPath location) {
			fLocation = location;
		}

		public String getFileBaseLocation() {
			return fLocation.toString();
		}

		public String getLocationByURI(String uri) {
			return getLocationByURI(uri, getFileBaseLocation(), false);
		}

		public String getLocationByURI(String uri, boolean resolveCrossProjectLinks) {
			return getLocationByURI(uri, getFileBaseLocation(), resolveCrossProjectLinks);
		}

		public String getLocationByURI(String uri, String baseReference) {
			return getLocationByURI(uri, baseReference, false);
		}

		public String getLocationByURI(String uri, String baseReference, boolean resolveCrossProjectLinks) {
			// ignore resolveCrossProjectLinks value
			if (uri == null)
				return null;
			if (uri.startsWith("file:")) { //$NON-NLS-1$
				try {
					URL url = new URL(uri);
					return url.getFile();
				}
				catch (MalformedURLException e) {
				}
			}
			return URIHelper.normalize(uri, baseReference, Path.ROOT.toString());
		}

		public IProject getProject() {
			return null;
		}

		public IContainer getRootLocation() {
			return ResourcesPlugin.getWorkspace().getRoot();
		}

		public InputStream getURIStream(String uri) {
			return null;
		}

		public void setFileBaseLocation(String newLocation) {
		}

		public void setProject(IProject newProject) {
		}
	}

	/**
	 * A URIResolver instance of models built on the extensible WST URI
	 * resolver
	 */
	class CommonURIResolver implements URIResolver {
		IPath fLocation;
		IPath fPath;
		final static String SEPARATOR = "/"; //$NON-NLS-1$ 
		final static String FILE_PREFIX = "file://"; //$NON-NLS-1$

		CommonURIResolver(IPath path, IPath location) {
			fLocation = location;
			fPath = path;
		}

		public String getFileBaseLocation() {
			return fLocation.toString();
		}

		public String getLocationByURI(String uri) {
			return getLocationByURI(uri, getFileBaseLocation(), false);
		}

		public String getLocationByURI(String uri, boolean resolveCrossProjectLinks) {
			return getLocationByURI(uri, getFileBaseLocation(), resolveCrossProjectLinks);
		}

		public String getLocationByURI(String uri, String baseReference) {
			return getLocationByURI(uri, baseReference, false);
		}

		public String getLocationByURI(String uri, String baseReference, boolean resolveCrossProjectLinks) {
			boolean baseHasPrefix = baseReference != null && baseReference.startsWith(FILE_PREFIX);
			String reference = null;
			if (baseHasPrefix) {
				reference = baseReference;
			}
			else {
				reference = FILE_PREFIX + baseReference;
			}
			String result = URIResolverPlugin.createResolver().resolve(reference, null, uri);
			// Logger.log(Logger.INFO_DEBUG,
			// "URIResolverPlugin.createResolver().resolve("
			// + reference + ", null, " +uri+") = " + result);
			if (!baseHasPrefix && result.startsWith(FILE_PREFIX) && result.length() > FILE_PREFIX.length()) {
				result = result.substring(FILE_PREFIX.length());
			}
			return result;
		}

		public IProject getProject() {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(fPath.segment(0));
		}

		public IContainer getRootLocation() {
			String root = URIResolverPlugin.createResolver().resolve(FILE_PREFIX + getFileBaseLocation(), null, SEPARATOR);
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(root));
			for (int i = 0; i < files.length; i++) {
				if ((files[i].getType() & IResource.FOLDER) == IResource.FOLDER) {
					if (((IFolder) files[i]).getFullPath().isPrefixOf(fPath)) {
						return (IFolder) files[i];
					}
				}
			}
			return getProject();
		}

		public InputStream getURIStream(String uri) {
			return null;
		}

		public void setFileBaseLocation(String newLocation) {
		}

		public void setProject(IProject newProject) {
		}
	}

	/**
	 * Maps interesting documents in file buffers to those file buffers.
	 * Required to allows us to go from documents to complete models.
	 */
	class FileBufferMapper implements IFileBufferListener {
		public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
		}

		public void bufferContentReplaced(IFileBuffer buffer) {
		}

		public void bufferCreated(IFileBuffer buffer) {
			if (buffer instanceof ITextFileBuffer) {
				ITextFileBuffer textBuffer = (ITextFileBuffer) buffer;
				if (!(textBuffer.getDocument() instanceof IStructuredDocument))
					return;
				if (Logger.DEBUG_TEXTBUFFERLIFECYCLE) {
					Logger.log(Logger.INFO, "Learned new buffer: " + buffer.getLocation().toString() + " " + buffer + " " + ((ITextFileBuffer) buffer).getDocument()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				DocumentInfo info = new DocumentInfo();
				info.buffer = textBuffer;
				info.contentTypeID = detectContentType(buffer.getLocation()).getId();
				info.bufferReferenceCount++;
				fDocumentMap.put(textBuffer.getDocument(), info);
			}
		}

		public void bufferDisposed(IFileBuffer buffer) {
			if (buffer instanceof ITextFileBuffer) {
				ITextFileBuffer textBuffer = (ITextFileBuffer) buffer;
				if (!(textBuffer.getDocument() instanceof IStructuredDocument))
					return;
				if (Logger.DEBUG_TEXTBUFFERLIFECYCLE) {
					Logger.log(Logger.INFO, "Discarded buffer: " + buffer.getLocation().toString() + " " + buffer + " " + ((ITextFileBuffer) buffer).getDocument()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				DocumentInfo info = (DocumentInfo) fDocumentMap.get(textBuffer.getDocument());
				if (info != null) {
					info.bufferReferenceCount--;
					if (info.bufferReferenceCount == 0 && info.modelReferenceCount == 0)
						fDocumentMap.remove(textBuffer.getDocument());
				}
			}
		}

		public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
			if (buffer instanceof ITextFileBuffer) {
				if (Logger.DEBUG_TEXTBUFFERLIFECYCLE) {
					Logger.log(Logger.INFO, "Buffer dirty state changed: (" + isDirty + ") " + buffer.getLocation().toString() + " " + buffer + " " + ((ITextFileBuffer) buffer).getDocument()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				ITextFileBuffer textBuffer = (ITextFileBuffer) buffer;
				if (!(textBuffer.getDocument() instanceof IStructuredDocument))
					return;
				DocumentInfo info = (DocumentInfo) fDocumentMap.get(textBuffer.getDocument());
				if (info != null && info.model != null) {
					String msg = "Updating model dirty state for" + info.buffer.getLocation(); //$NON-NLS-1$
					if (Logger.DEBUG_FILEBUFFERMODELMANAGEMENT || Logger.DEBUG_TEXTBUFFERLIFECYCLE) {
						Logger.log(Logger.INFO, msg);
					}
					info.model.setDirtyState(isDirty);

					IFile workspaceFile = FileBuffers.getWorkspaceFileAtLocation(info.buffer.getLocation());
					if (!isDirty && workspaceFile != null) {
						info.model.resetSynchronizationStamp(workspaceFile);
					}
				}
			}
		}

		public void stateChangeFailed(IFileBuffer buffer) {
		}

		public void stateChanging(IFileBuffer buffer) {
		}

		public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
		}

		public void underlyingFileDeleted(IFileBuffer buffer) {
			if (buffer instanceof ITextFileBuffer) {
				if (Logger.DEBUG_TEXTBUFFERLIFECYCLE) {
					Logger.log(Logger.INFO, "Deleted buffer: " + buffer.getLocation().toOSString() + " " + buffer); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

		public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
			if (buffer instanceof ITextFileBuffer) {
				if (Logger.DEBUG_TEXTBUFFERLIFECYCLE) {
					Logger.log(Logger.INFO, "Moved buffer from: " + buffer.getLocation().toOSString() + " " + buffer); //$NON-NLS-1$ //$NON-NLS-2$
					Logger.log(Logger.INFO, "Moved buffer to: " + path.toOSString() + " " + buffer); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}

	private static FileBufferModelManager instance;

	public static FileBufferModelManager getInstance() {
		if (instance == null) {
			instance = new FileBufferModelManager();
		}
		return instance;
	}

	static final void shutdown() {
		if (instance != null) {
			if (Logger.DEBUG_FILEBUFFERMODELMANAGEMENT) {
				IDocument[] danglingDocuments = (IDocument[]) instance.fDocumentMap.keySet().toArray(new IDocument[0]);
				for (int i = 0; i < danglingDocuments.length; i++) {
					DocumentInfo info = (DocumentInfo) instance.fDocumentMap.get(danglingDocuments[i]);
					if (info.modelReferenceCount > 0)
						System.err.println("LEAKED MODEL: " + info.buffer.getLocation() + " " + (info.model != null ? info.model.getId() : null)); //$NON-NLS-1$ //$NON-NLS-2$
					if (info.bufferReferenceCount > 0)
						System.err.println("LEAKED BUFFER: " + info.buffer.getLocation() + " " + info.buffer.getDocument()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			FileBuffers.getTextFileBufferManager().removeFileBufferListener(instance.fFileBufferListener);
			instance = null;
		}
	}

	static final void startup() {
		getInstance();
	}

	// a map of IStructuredDocuments to DocumentInfo objects
	Map fDocumentMap = null;

	IFileBufferListener fFileBufferListener = null;

	FileBufferModelManager() {
		super();
		fDocumentMap = new Hashtable(4);
		FileBuffers.getTextFileBufferManager().addFileBufferListener(fFileBufferListener = new FileBufferMapper());
	}

	public String calculateId(IFile file) {
		if (file == null) {
			Exception iae = new IllegalArgumentException("can not calculate a model ID without an IFile"); //$NON-NLS-1$ 
			Logger.logException(iae);
			return null;
		}

		String id = null;
		IPath path = file.getFullPath();
		if (path != null) {
			/*
			 * The ID of models must be the same as the normalized paths
			 * stored in the underlying FileBuffers to retrieve them by common
			 * ID later on. We chose the FileBuffer normalized path over the
			 * previously used absolute IFile path because the buffers should
			 * already exist before we build a model and we can't retrieve a
			 * FileBuffer using the ID of a model that doesn't yet exist.
			 */
			id = FileBuffers.normalizeLocation(path).toString();
		}
		return id;

	}


	public String calculateId(IDocument document) {
		if (document == null) {
			Exception iae = new IllegalArgumentException("can not calculate a model ID without a document reference"); //$NON-NLS-1$ 
			Logger.logException(iae);
			return null;
		}

		String id = null;
		ITextFileBuffer buffer = getBuffer(document);
		if (buffer != null) {
			id = buffer.getLocation().toString();
		}
		return id;
	}

	URIResolver createURIResolver(ITextFileBuffer buffer) {
		IPath location = buffer.getLocation();
		IFile workspaceFile = FileBuffers.getWorkspaceFileAtLocation(location);
		URIResolver resolver = null;
		if (workspaceFile != null) {
			IProject project = workspaceFile.getProject();
			resolver = (URIResolver) project.getAdapter(URIResolver.class);
			if (resolver == null) {
				resolver = new CommonURIResolver(workspaceFile.getFullPath(), workspaceFile.getLocation());
			}
			resolver.setFileBaseLocation(workspaceFile.getLocation().toString());
		}
		else {
			resolver = new ExternalURIResolver(location);
		}
		return resolver;
	}


	IContentType detectContentType(IPath location) {
		IContentType type = null;

		IResource resource = FileBuffers.getWorkspaceFileAtLocation(location);
		if (resource != null) {
			if (resource.getType() == IResource.FILE && resource.isAccessible()) {
				IContentDescription d = null;
				try {
					// Optimized description lookup, might not succeed
					d = ((IFile) resource).getContentDescription();
					if (d != null) {
						type = d.getContentType();
					}
				}
				catch (CoreException e) {
					// Should not be possible given the accessible and file
					// type check above
				}
				if (type == null) {
					type = Platform.getContentTypeManager().findContentTypeFor(resource.getName());
				}
			}
		}
		else {
			File file = FileBuffers.getSystemFileAtLocation(location);
			if (file != null) {
				InputStream input = null;
				try {
					input = new FileInputStream(file);
					type = Platform.getContentTypeManager().findContentTypeFor(input, location.toOSString());
				}
				catch (FileNotFoundException e) {
				}
				catch (IOException e) {
				}
				finally {
					if (input != null) {
						try {
							input.close();
						}
						catch (IOException e1) {
						}
					}
				}
				if (type == null) {
					type = Platform.getContentTypeManager().findContentTypeFor(file.getName());
				}
			}
		}
		if (type == null) {
			type = Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);
		}
		return type;
	}

	public ITextFileBuffer getBuffer(IDocument document) {
		if (document == null) {
			Exception iae = new IllegalArgumentException("can not get a buffer without a document reference"); //$NON-NLS-1$ 
			Logger.logException(iae);
			return null;
		}

		DocumentInfo info = (DocumentInfo) fDocumentMap.get(document);
		if (info != null)
			return info.buffer;
		return null;
	}

	String getContentTypeID(IDocument document) {
		DocumentInfo info = (DocumentInfo) fDocumentMap.get(document);
		if (info != null)
			return info.contentTypeID;
		return null;
	}

	IStructuredModel getModel(File file) {
		if (file == null) {
			Exception iae = new IllegalArgumentException("can not get/create a model without a java.io.File"); //$NON-NLS-1$ 
			Logger.logException(iae);
			return null;
		}

		IStructuredModel model = null;
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		try {
			IPath location = new Path(file.getAbsolutePath());
			if (Logger.DEBUG_FILEBUFFERMODELMANAGEMENT) {
				Logger.log(Logger.INFO, "FileBufferModelManager connecting to File " + location); //$NON-NLS-1$
			}
			bufferManager.connect(location, getProgressMonitor());
			ITextFileBuffer buffer = bufferManager.getTextFileBuffer(location);
			if (buffer != null) {
				DocumentInfo info = (DocumentInfo) fDocumentMap.get(buffer.getDocument());
				info.selfConnected = true;
				model = getModel((IStructuredDocument) buffer.getDocument());
			}
		}
		catch (CoreException e) {
			Logger.logException("Error getting model for " + file.getPath(), e); //$NON-NLS-1$
		}
		return model;
	}

	public IStructuredModel getModel(IFile file) {
		if (file == null) {
			Exception iae = new IllegalArgumentException("can not get/create a model without an IFile"); //$NON-NLS-1$ 
			Logger.logException(iae);
			return null;
		}

		IStructuredModel model = null;
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		try {
			if (Logger.DEBUG_FILEBUFFERMODELMANAGEMENT) {
				Logger.log(Logger.INFO, "FileBufferModelManager connecting to IFile " + file.getLocation()); //$NON-NLS-1$
			}
			// see TextFileDocumentProvider#createFileInfo about why we use
			// IFile#getFullPath
			// here, not IFile#getLocation.
			IPath location = file.getFullPath();
			if (location != null) {
				bufferManager.connect(location, getProgressMonitor());
				ITextFileBuffer buffer = bufferManager.getTextFileBuffer(location);
				if (buffer != null) {
					DocumentInfo info = (DocumentInfo) fDocumentMap.get(buffer.getDocument());
					if (info != null) {
						/*
						 * Note: "info" being null at this point is a slight
						 * error.
						 * 
						 * The connect call from above (or at some time
						 * earlier in the session) would have notified the
						 * FileBufferMapper of the creation of the
						 * corresponding text buffer and created the
						 * DocumentInfo object for IStructuredDocuments.
						 */
						info.selfConnected = true;
					}
					/*
					 * Check the document type. Although returning null for
					 * unknown documents would be fair, try to get a model if
					 * the document is at least a valid type.
					 */
					IDocument bufferDocument = buffer.getDocument();
					if (bufferDocument instanceof IStructuredDocument) {
						model = getModel((IStructuredDocument) bufferDocument);
					}
				}
			}
		}
		catch (CoreException e) {
			Logger.logException("Error getting model for " + file.getLocation(), e); //$NON-NLS-1$
		}
		return model;
	}

	public IStructuredModel getModel(IStructuredDocument document) {
		if (document == null) {
			Exception iae = new IllegalArgumentException("can not get/create a model without a document reference"); //$NON-NLS-1$ 
			Logger.logException(iae);
			return null;
		}

		DocumentInfo info = (DocumentInfo) fDocumentMap.get(document);
		if (info != null && info.model == null) {
			if (Logger.DEBUG_FILEBUFFERMODELMANAGEMENT) {
				Logger.log(Logger.INFO, "FileBufferModelManager creating model for " + info.buffer.getLocation() + " " + info.buffer.getDocument()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			info.modelReferenceCount++;

			IStructuredModel model = null;
			IModelHandler handler = ModelHandlerRegistry.getInstance().getHandlerForContentTypeId(info.contentTypeID);
			IModelLoader loader = handler.getModelLoader();
			model = loader.createModel(document, info.buffer.getLocation().toString(), handler);
			try {
				info.model = model;
				model.setId(info.buffer.getLocation().toString());
				// handler now set by loader, for now
				// model.setModelHandler(handler);
				if (model instanceof AbstractStructuredModel) {
					((AbstractStructuredModel) model).setContentTypeIdentifier(info.contentTypeID);
				}
				model.setResolver(createURIResolver(getBuffer(document)));
				if (info.buffer.isDirty()) {
					model.setDirtyState(true);
				}
			}
			catch (ResourceInUse e) {
				Logger.logException("attempted to create new model with existing ID", e); //$NON-NLS-1$
				model = null;
			}
		}
		if (info != null) {
			return info.model;
		}
		return null;
	}

	/**
	 * @return
	 */
	private IProgressMonitor getProgressMonitor() {
		return new NullProgressMonitor();
	}

	public boolean isExistingBuffer(IDocument document) {
		if (document == null) {
			Exception iae = new IllegalArgumentException("can not check for an existing buffer without a document reference"); //$NON-NLS-1$ 
			Logger.logException(iae);
			return false;
		}

		DocumentInfo info = (DocumentInfo) fDocumentMap.get(document);
		return info != null;
	}

	public void releaseModel(IDocument document) {
		if (document == null) {
			Exception iae = new IllegalArgumentException("can not release a model without a document reference"); //$NON-NLS-1$ 
			Logger.logException(iae);
			return;
		}
		DocumentInfo info = (DocumentInfo) fDocumentMap.get(document);
		if (info != null) {
			if (Logger.DEBUG_FILEBUFFERMODELMANAGEMENT) {
				Logger.log(Logger.INFO, "FileBufferModelManager noticed full release of model for " + info.buffer.getLocation() + " " + info.buffer.getDocument()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			info.model = null;
			info.modelReferenceCount--;
			if (info.selfConnected) {
				if (Logger.DEBUG_FILEBUFFERMODELMANAGEMENT) {
					Logger.log(Logger.INFO, "FileBufferModelManager disconnecting from " + info.buffer.getLocation() + " " + info.buffer.getDocument()); //$NON-NLS-1$ //$NON-NLS-2$
				}
				IPath location = info.buffer.getLocation();
				try {
					FileBuffers.getTextFileBufferManager().disconnect(location, getProgressMonitor());
				}
				catch (CoreException e) {
					Logger.logException("Error releasing model for " + location, e); //$NON-NLS-1$
				}
			}
		}
	}

	public void revert(IDocument document) {
		if (document == null) {
			Exception iae = new IllegalArgumentException("can not release a model without a document reference"); //$NON-NLS-1$ 
			Logger.logException(iae);
			return;
		}
		DocumentInfo info = (DocumentInfo) fDocumentMap.get(document);
		if (info == null) {
			Logger.log(Logger.ERROR, "FileBufferModelManager was asked to revert a document but was not being managed"); //$NON-NLS-1$
		}
		else {
			// get path just for potential error message
			IPath location = info.buffer.getLocation();
			try {
				// ISSUE: in future, clients should provide progress monitor
				info.buffer.revert(getProgressMonitor());
			}
			catch (CoreException e) {
				// ISSUE: shoudl we not be re-throwing CoreExceptions? Or
				// not catch them at all?
				Logger.logException("Error reverting model for " + location, e); //$NON-NLS-1$
			}
		}
	}
}
