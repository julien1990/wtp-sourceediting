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
package org.eclipse.wst.sse.core.internal.format;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.sse.core.IStructuredModel;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.Logger;
import org.eclipse.wst.sse.core.internal.util.Assert;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;


public abstract class AbstractStructuredFormatProcessor implements IStructuredFormatProcessor {
	protected IStructuredFormatContraints fFormatContraints = null;
	protected IProgressMonitor fProgressMonitor = null;
	public boolean refreshFormatPreferences = true; // special flag for JUnit

	protected void ensureClosed(OutputStream outputStream, InputStream inputStream) {

		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
			Logger.logException(e); // hopeless
		}
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (IOException e) {
			Logger.logException(e); // hopeless
		}
	}

	public String formatContent(String input) throws IOException, CoreException {
		if (input == null)
			return input;

		IStructuredModel structuredModel = null;
		InputStream inputStream = null;
		try {
			// setup structuredModel
			// Note: We are getting model for read. Will return formatted
			// string and NOT save model.
			inputStream = new ByteArrayInputStream(input.getBytes("UTF8")); //$NON-NLS-1$
			String id = inputStream.toString() + "." + getFileExtension(); //$NON-NLS-1$
			structuredModel = StructuredModelManager.getModelManager().getModelForRead(id, inputStream, null);

			// format
			formatModel(structuredModel);

			// return output
			return structuredModel.getStructuredDocument().get();
		} finally {
			ensureClosed(null, inputStream);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromRead();
		}
	}

	public String formatContent(String input, int start, int length) throws IOException, CoreException {
		if (input == null)
			return input;

		if (start >= 0 && length >= 0 && start + length <= input.length()) {
			IStructuredModel structuredModel = null;
			InputStream inputStream = null;
			try {
				// setup structuredModel
				// Note: We are getting model for read. Will return formatted
				// string and NOT save model.
				inputStream = new ByteArrayInputStream(input.getBytes("UTF8")); //$NON-NLS-1$
				String id = inputStream.toString() + "." + getFileExtension(); //$NON-NLS-1$
				structuredModel = StructuredModelManager.getModelManager().getModelForRead(id, inputStream, null);

				// format
				formatModel(structuredModel, start, length);

				// return output
				return structuredModel.getStructuredDocument().get();
			} finally {
				ensureClosed(null, inputStream);
				// release from model manager
				if (structuredModel != null)
					structuredModel.releaseFromRead();
			}
		} else
			return input;
	}

	public void formatDocument(IDocument document) throws IOException, CoreException {
		if (document == null)
			return;

		IStructuredModel structuredModel = null;
		//OutputStream outputStream = null;
		try {
			// setup structuredModel
			// Note: We are getting model for edit. Will save model if model
			// changed.
			structuredModel = StructuredModelManager.getModelManager().getExistingModelForEdit(document);

			// format
			formatModel(structuredModel);

			// save model if needed
			if (!structuredModel.isSharedForEdit() && structuredModel.isSaveNeeded())
				structuredModel.save();
		} finally {
			//ensureClosed(outputStream, null);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromEdit();
		}
	}

	public void formatDocument(IDocument document, int start, int length) throws IOException, CoreException {
		if (document == null)
			return;

		if (start >= 0 && length >= 0 && start + length <= document.getLength()) {
			IStructuredModel structuredModel = null;
			//OutputStream outputStream = null;
			try {
				// setup structuredModel
				// Note: We are getting model for edit. Will save model if
				// model changed.
				structuredModel = StructuredModelManager.getModelManager().getExistingModelForEdit(document);

				// format
				formatModel(structuredModel, start, length);

				// save model if needed
				if (!structuredModel.isSharedForEdit() && structuredModel.isSaveNeeded())
					structuredModel.save();
			} finally {
				//ensureClosed(outputStream, null);
				// release from model manager
				if (structuredModel != null)
					structuredModel.releaseFromEdit();
			}
		}
	}

	public void formatFile(IFile file) throws IOException, CoreException {
		if (file == null)
			return;

		IStructuredModel structuredModel = null;
		//OutputStream outputStream = null;
		try {
			// setup structuredModel
			// Note: We are getting model for edit. Will save model if model
			// changed.
			structuredModel = StructuredModelManager.getModelManager().getModelForEdit(file);

			// format
			formatModel(structuredModel);

			// save model if needed
			if (!structuredModel.isSharedForEdit() && structuredModel.isSaveNeeded())
				structuredModel.save();
		} finally {
			//ensureClosed(outputStream, null);
			// release from model manager
			if (structuredModel != null) {
				structuredModel.releaseFromEdit();
			}

		}
	}

	public void formatFile(IFile file, int start, int length) throws IOException, CoreException {
		if (file == null)
			return;

		IStructuredModel structuredModel = null;
		//OutputStream outputStream = null;
		try {
			// setup structuredModel
			// Note: We are getting model for edit. Will save model if model
			// changed.
			structuredModel = StructuredModelManager.getModelManager().getModelForEdit(file);

			// format
			formatModel(structuredModel, start, length);

			// save model if needed
			if (!structuredModel.isSharedForEdit() && structuredModel.isSaveNeeded())
				structuredModel.save();
		} finally {
			//ensureClosed(outputStream, null);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromEdit();
		}
	}

	public void formatFileName(String fileName) throws IOException, CoreException {
		if (fileName == null)
			return;

		IStructuredModel structuredModel = null;
		InputStream inputStream = null;
		//OutputStream outputStream = null;
		try {
			// setup structuredModel
			// Note: We are getting model for edit. Will save model if model
			// changed.
			inputStream = new FileInputStream(fileName);
			structuredModel = StructuredModelManager.getModelManager().getModelForEdit(fileName, inputStream, null);

			// format
			formatModel(structuredModel);

			// save model if needed
			if (!structuredModel.isSharedForEdit() && structuredModel.isSaveNeeded())
				structuredModel.save();
		} finally {
			//ensureClosed(outputStream, inputStream);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromEdit();
		}
	}

	public void formatFileName(String fileName, int start, int length) throws IOException, CoreException {
		if (fileName == null)
			return;

		IStructuredModel structuredModel = null;
		InputStream inputStream = null;
		//OutputStream outputStream = null;
		try {
			// setup structuredModel
			// Note: We are getting model for edit. Will save model if model
			// changed.
			inputStream = new FileInputStream(fileName);
			structuredModel = StructuredModelManager.getModelManager().getModelForEdit(fileName, inputStream, null);

			// format
			formatModel(structuredModel, start, length);

			// save model if needed
			if (!structuredModel.isSharedForEdit() && structuredModel.isSaveNeeded())
				structuredModel.save();
		} finally {
			//ensureClosed(outputStream, inputStream);
			// release from model manager
			if (structuredModel != null)
				structuredModel.releaseFromEdit();
		}
	}

	public void formatModel(IStructuredModel structuredModel) {
		int start = 0;
		int length = structuredModel.getStructuredDocument().getLength();

		formatModel(structuredModel, start, length);
	}

	public void formatModel(IStructuredModel structuredModel, int start, int length) {
		if (structuredModel != null) {
			try {
				// whenever formatting model, fire abouttochange/modelchanged
				structuredModel.aboutToChangeModel();
				if (start == 0 && length == structuredModel.getStructuredDocument().getLength())
					setFormatWithSiblingIndent(structuredModel, false);
				else
					setFormatWithSiblingIndent(structuredModel, true);
	
				if (start >= 0 && length >= 0 && start + length <= structuredModel.getStructuredDocument().getLength()) {
					Vector activeNodes = getActiveNodes(structuredModel, start, length);
					if (activeNodes.size() > 0) {
						Node firstNode = (Node) activeNodes.firstElement();
						Node lastNode = (Node) activeNodes.lastElement();
	
						boolean done = false;
						Node eachNode = firstNode;
						Node nextNode = null;
						// TODO: we should be able to call something like
						// sequentialRewrite, but
						// doesn't work for
						// our case, since we do "gets" during reparsing, so makes
						// sequential rewrite
						// store actually
						// less efficient than gap store. Someday we need our own
						// gap store, that
						// handles
						// structured text more efficiently. I thought I'd leave
						// this commented out
						// code here
						// as a reminder.
						//					try {
						//						structuredModel.getStructuredDocument().startSequentialRewrite(false);
						while (!done) {
							// update "done"
							done = (eachNode == lastNode);
	
							// get next sibling before format because eachNode
							// may
							// be deleted,
							// for example when it's an empty text node
							nextNode = eachNode.getNextSibling();
	
							// format each node
							formatNode(eachNode);
	
							// update each node
							if (nextNode != null && nextNode.getParentNode() == null)
								// nextNode is deleted during format
								eachNode = eachNode.getNextSibling();
							else
								eachNode = nextNode;
	
							// This should not be needed, but just in case
							// something went wrong with with eachNode.
							// We don't want an infinite loop here.
							if (eachNode == null)
								done = true;
						}
						//					}
						//					finally {
						//						structuredModel.getStructuredDocument().stopSequentialRewrite();
						//					}
					}
				}
			} finally {
				// always make sure to fire changedmodel when done
				structuredModel.changedModel();
			}
		}
	}

	public void formatNode(Node node) {
		if (node != null) {
			Node newNode = node;

			// format the owner node if it's an attribute node
			if (node.getNodeType() == Node.ATTRIBUTE_NODE)
				newNode = ((Attr) node).getOwnerElement();

			// refresh format preferences before getting formatter
			if (refreshFormatPreferences)
				refreshFormatPreferences();

			// get formatter and format contraints
			IStructuredFormatter formatter = getFormatter(newNode);
			// TODO_future: added assert to replace "redundant null check".
			// if formatter is ever null, we should provide some
			// default formatter to serve as place holder.
			Assert.isNotNull(formatter, "formatter was null for a node, "); //$NON-NLS-1$
			IStructuredFormatContraints formatContraints = formatter.getFormatContraints();
			formatContraints.setFormatWithSiblingIndent(true);
			// format each node
			formatter.format(newNode, formatContraints);
		}
	}

	protected Vector getActiveNodes(IStructuredModel structuredModel, int startNodeOffset, int length) {
		Vector activeNodes = new Vector();

		if (structuredModel != null) {
			Node startNode = (Node) structuredModel.getIndexedRegion(startNodeOffset);
			// see https://w3.opensource.ibm.com/bugzilla/show_bug.cgi?id=4711
			//
			// We have to watch for selection boundary conditions. Use this as
			// an example: <a>text</a><b>text</b>,
			// If the whole <a> node is selected, like:
			// |<a>text</a>|<b>text</b>, we need to substract the length by 1
			// to find
			// the node at the end of the selection:
			// structuredModel.getIndexedRegion(startNodeOffset + length - 1),
			// or else
			// we'd find the next adjacent node.
			//
			// However, when the selection length is 0 (meaning no text is
			// selected), the cursor is at the beginning
			// of the node we want to format: |<a>text</a><b>text</b>, the
			// node at the end of the selection is:
			// structuredModel.getIndexedRegion(startNodeOffset + length).
			int endNodeOffset = length > 0 ? startNodeOffset + length - 1 : startNodeOffset + length;
			Node endNode = (Node) structuredModel.getIndexedRegion(endNodeOffset);

			// make sure it's an non-empty document
			if (startNode != null) {
				while (isSiblingOf(startNode, endNode) == false) {
					if (endNode != null)
						endNode = endNode.getParentNode();
					if (endNode == null) {
						startNode = startNode.getParentNode();
						// see
						// https://w3.opensource.ibm.com/bugzilla/show_bug.cgi?id=4711
						// and notes above
						endNodeOffset = length > 0 ? startNodeOffset + length - 1 : startNodeOffset + length;
						endNode = (Node) structuredModel.getIndexedRegion(endNodeOffset);
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

	abstract protected String getFileExtension();

	protected IStructuredFormatContraints getFormatContraints(IStructuredModel structuredModel) {
		// 262135 - NPE during format of empty document
		if (fFormatContraints == null && structuredModel != null) {
			Node node = (Node) structuredModel.getIndexedRegion(0);

			if (node != null) {
				IStructuredFormatter formatter = getFormatter(node);
				if (formatter != null) {
					fFormatContraints = formatter.getFormatContraints();
				}
			}
		}

		return fFormatContraints;
	}

	abstract protected IStructuredFormatter getFormatter(Node node);

	protected boolean isSiblingOf(Node node, Node endNode) {
		if (endNode == null) {
			return true;
		} else {
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

	abstract protected void refreshFormatPreferences();

	protected void setFormatWithSiblingIndent(IStructuredModel structuredModel, boolean formatWithSiblingIndent) {
		// 262135 - NPE during format of empty document
		IStructuredFormatContraints formatContraints = getFormatContraints(structuredModel);

		if (formatContraints != null)
			formatContraints.setFormatWithSiblingIndent(formatWithSiblingIndent);
	}

	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor = monitor;
	}
}
