/*
* Copyright (c) 2002 IBM Corporation and others.
* All rights reserved.   This program and the accompanying materials
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*   IBM - Initial API and implementation
*   Jens Lukowski/Innoopract - initial renaming/restructuring
* 
*/
package org.eclipse.wst.sse.core.cleanup;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.wst.sse.core.IModelManager;
import org.eclipse.wst.sse.core.IModelManagerPlugin;
import org.eclipse.wst.sse.core.IStructuredModel;
import org.eclipse.wst.sse.core.format.IStructuredFormatProcessor;
import org.eclipse.wst.sse.core.internal.Logger;
import org.eclipse.wst.sse.core.preferences.CommonModelPreferenceNames;
import org.eclipse.wst.sse.core.text.IStructuredDocument;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;


public abstract class AbstractStructuredCleanupProcessor implements IStructuredCleanupProcessor {
	public boolean refreshCleanupPreferences = true; // special flag for JUnit tests to skip refresh of cleanup preferences when it's set to false

	public String cleanupContent(String input) throws IOException, CoreException {
		IStructuredModel structuredModel = null;
		InputStream inputStream = null;
		try {
			// setup structuredModel
			inputStream = new ByteArrayInputStream(input.getBytes("UTF8")); //$NON-NLS-1$
			String id = inputStream.toString() + getContentType();
			structuredModel = getModelManager().getModelForRead(id, inputStream, null);

			// cleanup
			cleanupModel(structuredModel, 0, structuredModel.getStructuredDocument().getLength());

			// return output
			return structuredModel.getStructuredDocument().get();
		}
		finally {
			ensureClosed(null, inputStream);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromRead();
		}
	}

	public String cleanupContent(String input, int start, int length) throws IOException, CoreException {
		IStructuredModel structuredModel = null;
		InputStream inputStream = null;
		try {
			// setup structuredModel
			inputStream = new ByteArrayInputStream(input.getBytes("UTF8")); //$NON-NLS-1$
			String id = inputStream.toString() + getContentType();
			structuredModel = getModelManager().getModelForRead(id, inputStream, null);

			// cleanup
			cleanupModel(structuredModel, start, length);

			// return output
			return structuredModel.getStructuredDocument().get();
		}
		finally {
			ensureClosed(null, inputStream);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromRead();
		}
	}

	public void cleanupDocument(IDocument document) throws IOException, CoreException {
		if (document == null)
			return;

		IStructuredModel structuredModel = null;
		//OutputStream outputStream = null;
		try {
			// setup structuredModel
			// Note: We are getting model for edit. Will save model if model
			// changed.
			structuredModel = getModelManager().getExistingModelForEdit(document);

			// cleanup
			cleanupModel(structuredModel);

			// save model if needed
			if (!structuredModel.isSharedForEdit() && structuredModel.isSaveNeeded())
				structuredModel.save();
		}
		finally {
			//ensureClosed(outputStream, null);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromEdit();
		}
	}

	public void cleanupDocument(IDocument document, int start, int length) throws IOException, CoreException {
		if (document == null)
			return;

		if (start >= 0 && length >= 0 && start + length <= document.getLength()) {
			IStructuredModel structuredModel = null;
			//OutputStream outputStream = null;
			try {
				// setup structuredModel
				// Note: We are getting model for edit. Will save model if
				// model changed.
				structuredModel = getModelManager().getExistingModelForEdit(document);

				// cleanup
				cleanupModel(structuredModel, start, length);

				// save model if needed
				if (!structuredModel.isSharedForEdit() && structuredModel.isSaveNeeded())
					structuredModel.save();
			}
			finally {
				//ensureClosed(outputStream, null);
				// release from model manager
				if (structuredModel != null)
					structuredModel.releaseFromEdit();
			}
		}
	}

	public void cleanupFile(IFile file) throws IOException, CoreException {
		IStructuredModel structuredModel = null;
		//OutputStream outputStream = null;
		try {
			// setup structuredModel
			structuredModel = getModelManager().getModelForRead(file);

			// cleanup
			cleanupModel(structuredModel, 0, structuredModel.getStructuredDocument().getLength());

			// save output to file
			//outputStream = new
			// FileOutputStream(file.getLocation().toString());
			structuredModel.save(file);
		}
		finally {
			//ensureClosed(outputStream, null);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromRead();
		}
	}

	public void cleanupFile(IFile file, int start, int length) throws IOException, CoreException {
		IStructuredModel structuredModel = null;
		//OutputStream outputStream = null;
		try {
			// setup structuredModel
			structuredModel = getModelManager().getModelForRead(file);

			// cleanup
			cleanupModel(structuredModel, start, length);

			// save output to file
			//outputStream = new
			// FileOutputStream(file.getLocation().toString());
			structuredModel.save(file);
		}
		finally {
			//ensureClosed(outputStream, null);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromRead();
		}
	}

	public void cleanupFileName(String fileName) throws IOException, CoreException {
		IStructuredModel structuredModel = null;
		InputStream inputStream = null;
		//OutputStream outputStream = null;
		try {
			// setup structuredModel
			inputStream = new FileInputStream(fileName);
			structuredModel = getModelManager().getModelForRead(fileName, inputStream, null);

			// cleanup
			cleanupModel(structuredModel, 0, structuredModel.getStructuredDocument().getLength());

			// save output to file
			//outputStream = new FileOutputStream(fileName);
			structuredModel.save();
		}
		finally {
			//ensureClosed(outputStream, inputStream);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromRead();
		}
	}

	public void cleanupFileName(String fileName, int start, int length) throws IOException, CoreException {
		IStructuredModel structuredModel = null;
		InputStream inputStream = null;
		//OutputStream outputStream = null;
		try {
			// setup structuredModel
			inputStream = new FileInputStream(fileName);
			structuredModel = getModelManager().getModelForRead(fileName, inputStream, null);

			// cleanup
			cleanupModel(structuredModel, start, length);

			// save output to file
			//outputStream = new FileOutputStream(fileName);
			structuredModel.save();
		}
		finally {
			//ensureClosed(outputStream, inputStream);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromRead();
		}
	}

	public void cleanupModel(IStructuredModel structuredModel) {

		int start = 0;
		int length = structuredModel.getStructuredDocument().getLength();
		cleanupModel(structuredModel, start, length);
	}

	public void cleanupModel(IStructuredModel structuredModel, int start, int length) {

		if (structuredModel != null) {
			if ((start >= 0) && (length <= structuredModel.getStructuredDocument().getLength())) {
				Vector activeNodes = getActiveNodes(structuredModel, start, length);
				if (activeNodes.size() > 0) {
					Node firstNode = (Node) activeNodes.firstElement();
					Node lastNode = (Node) activeNodes.lastElement();
					boolean done = false;
					Node eachNode = firstNode;
					Node nextNode = null;
					while (!done) {
						// update "done"
						done = (eachNode == lastNode);

						// get next sibling before cleanup because eachNode
						// may
						// be deleted,
						// for example when it's an empty text node
						nextNode = eachNode.getNextSibling();

						// cleanup selected node(s)
						cleanupNode(eachNode);

						// update each node
						if (nextNode != null && nextNode.getParentNode() == null)
							// nextNode is deleted during cleanup
							eachNode = eachNode.getNextSibling();
						else
							eachNode = nextNode;

						// This should not be needed, but just in case
						// something went wrong with with eachNode.
						// We don't want an infinite loop here.
						if (eachNode == null)
							done = true;
					}

					// format source
					if (getFormatSourcePreference(structuredModel)) {
						// format the document
						IStructuredFormatProcessor formatProcessor = getFormatProcessor();
						formatProcessor.formatModel(structuredModel);
					}

					// convert EOL codes
					if (getConvertEOLCodesPreference(structuredModel)) {
						IDocument document = structuredModel.getStructuredDocument();
						String endOfLineCode = getEOLCodePreference(structuredModel);
						String endOfLineCodeString = null;
						if (endOfLineCode.compareTo(CommonModelPreferenceNames.LF) == 0)
							endOfLineCodeString = CommonModelPreferenceNames.STRING_LF;
						else if (endOfLineCode.compareTo(CommonModelPreferenceNames.CR) == 0)
							endOfLineCodeString = CommonModelPreferenceNames.STRING_CR;
						else if (endOfLineCode.compareTo(CommonModelPreferenceNames.CRLF) == 0)
							endOfLineCodeString = CommonModelPreferenceNames.STRING_CRLF;
						if (endOfLineCodeString != null) {
							convertLineDelimiters(document, endOfLineCodeString);
							// DMW: 8/24/2002 setting line delimiter in
							// document allows
							// subsequent editing to insert the same line
							// delimiter.
							if (document instanceof IStructuredDocument) {
								((IStructuredDocument) document).setLineDelimiter(endOfLineCodeString);
							}
							structuredModel.setDirtyState(true);
						}
					}
				}
			}
		}
	}

	public void cleanupNode(Node node) {
		if (node != null) {
			Node cleanupNode = node;

			// cleanup the owner node if it's an attribute node
			if (cleanupNode.getNodeType() == Node.ATTRIBUTE_NODE)
				cleanupNode = ((Attr) cleanupNode).getOwnerElement();

			// refresh cleanup preferences before getting cleanup handler
			if (refreshCleanupPreferences)
				refreshCleanupPreferences();

			// get cleanup handler
			IStructuredCleanupHandler cleanupHandler = getCleanupHandler(cleanupNode);
			if (cleanupHandler != null) {
				// cleanup each node
				cleanupHandler.cleanup(cleanupNode);
			}
		}
	}

	protected void ensureClosed(OutputStream outputStream, InputStream inputStream) {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		catch (IOException e) {
			Logger.logException(e); // hopeless
		}
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		}
		catch (IOException e) {
			Logger.logException(e); // hopeless
		}
	}

	private IModelManager getModelManager() {

		IModelManagerPlugin plugin = (IModelManagerPlugin) Platform.getPlugin(IModelManagerPlugin.ID);
		return plugin.getModelManager();
	}

	protected Vector getActiveNodes(IStructuredModel structuredModel, int startNodeOffset, int length) {
		Vector activeNodes = new Vector();

		if (structuredModel != null) {
			Node startNode = (Node) structuredModel.getIndexedRegion(startNodeOffset);
			Node endNode = (Node) structuredModel.getIndexedRegion(startNodeOffset + length);

			// make sure it's an non-empty document
			if (startNode != null) {
				while (isSiblingOf(startNode, endNode) == false) {
					if (endNode != null)
						endNode = endNode.getParentNode();
					if (endNode == null) {
						startNode = startNode.getParentNode();
						endNode = (Node) structuredModel.getIndexedRegion(startNodeOffset + length);
					}
				}

				while (startNode != endNode) {
					activeNodes.addElement(startNode);
					startNode = startNode.getNextSibling();
				}
				if (startNode != null)
					activeNodes.addElement(startNode);
			}
		}

		return activeNodes;
	}

	protected boolean isSiblingOf(Node node, Node endNode) {
		if (endNode == null) {
			return true;
		}
		else {
			Node siblingNode = node;
			while (siblingNode != null) {
				if (siblingNode == endNode)
					return true;
				else
					siblingNode = siblingNode.getNextSibling();
			}
			return false;
		}
	}

	protected boolean getFormatSourcePreference(IStructuredModel structuredModel) {

		boolean formatSource = true;
		IStructuredCleanupHandler cleanupHandler = getCleanupHandler((Node) structuredModel.getIndexedRegion(0));
		if (cleanupHandler != null) {
			IStructuredCleanupPreferences cleanupPreferences = cleanupHandler.getCleanupPreferences();
			formatSource = cleanupPreferences.getFormatSource();
		}
		return formatSource;
	}

	protected boolean getConvertEOLCodesPreference(IStructuredModel structuredModel) {

		boolean convertEOLCodes = true;
		IStructuredCleanupHandler cleanupHandler = getCleanupHandler((Node) structuredModel.getIndexedRegion(0));
		if (cleanupHandler != null) {
			IStructuredCleanupPreferences cleanupPreferences = cleanupHandler.getCleanupPreferences();
			convertEOLCodes = cleanupPreferences.getConvertEOLCodes();
		}
		return convertEOLCodes;
	}

	protected String getEOLCodePreference(IStructuredModel structuredModel) {

		String eolCode = System.getProperty("line.separator"); //$NON-NLS-1$

		IStructuredCleanupHandler cleanupHandler = getCleanupHandler((Node) structuredModel.getIndexedRegion(0));
		if (cleanupHandler != null) {
			IStructuredCleanupPreferences cleanupPreferences = cleanupHandler.getCleanupPreferences();
			eolCode = cleanupPreferences.getEOLCode();
		}
		return eolCode;
	}

	protected void convertLineDelimiters(IDocument document, String newDelimiter) {

		final int lineCount = document.getNumberOfLines();
		IDocumentPartitioner partitioner = document.getDocumentPartitioner();
		if (partitioner != null) {
			partitioner.disconnect();
			document.setDocumentPartitioner(null);
		}
		try {
			for (int i = 0; i < lineCount; i++) {
				final String delimiter = document.getLineDelimiter(i);
				if (delimiter != null && delimiter.length() > 0 && !delimiter.equals(newDelimiter)) {
					IRegion region = document.getLineInformation(i);
					document.replace(region.getOffset() + region.getLength(), delimiter.length(), newDelimiter);
				}
			}
		}
		catch (BadLocationException e) {
			Logger.logException(e);
		}
		finally {
			if (partitioner != null) {
				partitioner.connect(document);
				document.setDocumentPartitioner(partitioner);
			}
		}
	}

	abstract protected String getContentType();

	abstract protected IStructuredCleanupHandler getCleanupHandler(Node node);

	abstract protected IStructuredFormatProcessor getFormatProcessor();

	abstract protected void refreshCleanupPreferences();
}
