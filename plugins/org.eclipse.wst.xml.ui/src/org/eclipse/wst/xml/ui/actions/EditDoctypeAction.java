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


package org.eclipse.wst.xml.ui.actions;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.IStructuredModel;
import org.eclipse.wst.xml.core.document.XMLDocument;
import org.eclipse.wst.xml.core.document.XMLDocumentType;
import org.eclipse.wst.xml.core.internal.document.DocumentImpl;
import org.eclipse.wst.xml.ui.dialogs.EditDoctypeDialog;
import org.eclipse.wst.xml.ui.util.XMLCommonResources;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * EditDoctypeAction
 */
public class EditDoctypeAction extends Action {
	protected DocumentType doctype;
	protected Document document;
	protected IStructuredModel model;
	protected String resourceLocation;
	protected String title;

	/**
	 * This constructor is used to create a new doctype.
	 */
	public EditDoctypeAction(IStructuredModel model, Document document, String resourceLocation, String title) {
		setText(title);
		this.model = model;
		this.document = document;
		this.resourceLocation = resourceLocation;
		this.title = title;
	}

	/**
	 * This constructor is used to edit an exisitng doctype.
	 */
	public EditDoctypeAction(IStructuredModel model, DocumentType doctype, String resourceLocation, String title) {
		setText(title);
		this.model = model;
		this.doctype = doctype;
		this.resourceLocation = resourceLocation;
		this.title = title;
	}


	protected DocumentType createDoctype(EditDoctypeDialog dialog, Document document) {
		DocumentType result = null;
		if (document instanceof DocumentImpl) {
			XMLDocument documentImpl = (XMLDocument) document;
			XMLDocumentType doctypeImpl = (XMLDocumentType) documentImpl.createDoctype(dialog.getName());
			doctypeImpl.setPublicId(dialog.getPublicId());
			doctypeImpl.setSystemId(dialog.getSystemId());
			result = doctypeImpl;
		}
		return result;
	}

	private Display getDisplay() {

		return PlatformUI.getWorkbench().getDisplay();
	}


	protected String getRootElementName(Document document) {
		Element rootElement = null;
		NodeList nodeList = document.getChildNodes();
		int nodeListLength = nodeList.getLength();
		for (int i = 0; i < nodeListLength; i++) {
			Node childNode = nodeList.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				rootElement = (Element) childNode;
				break;
			}
		}
		return rootElement != null ? rootElement.getNodeName() : XMLCommonResources.getInstance().getString("_UI_LABEL_ROOT_ELEMENT_VALUE"); //$NON-NLS-1$
	}

	public String getUndoDescription() {
		return title;
	}


	protected void insertDoctype(DocumentType doctype, Document document) {
		Node refChild = null;
		NodeList nodeList = document.getChildNodes();
		int nodeListLength = nodeList.getLength();
		for (int i = 0; i < nodeListLength; i++) {
			Node childNode = nodeList.item(i);
			if (childNode.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE || childNode.getNodeType() == Node.COMMENT_NODE) {
				// continue on to the nextNode
			} else {
				refChild = childNode;
				break;
			}
		}

		document.insertBefore(doctype, refChild);
		//manager.reformat(doctype, false);
	}

	public void run() {
		model.beginRecording(this, getUndoDescription());
		//Shell shell =
		// XMLCommonUIPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell();
		Shell shell = getDisplay().getActiveShell();
		EditDoctypeDialog dialog = showEditDoctypeDialog(shell);

		if (dialog.getReturnCode() == Window.OK) {
			if (doctype != null) {
				updateDoctype(dialog, doctype);
			} else if (document != null) {
				DocumentType doctype = createDoctype(dialog, document);
				if (doctype != null) {
					insertDoctype(doctype, document);
				}
			}
		}
		model.endRecording(this);
	}

	protected EditDoctypeDialog showEditDoctypeDialog(Shell shell) {
		EditDoctypeDialog dialog = null;

		if (doctype != null) {
			dialog = new EditDoctypeDialog(shell, doctype);
			if (title == null) {
				title = XMLCommonResources.getInstance().getString("_UI_LABEL_EDIT_DOCTYPE"); //$NON-NLS-1$
			}
		} else if (document != null) {
			String rootElementName = getRootElementName(document);
			dialog = new EditDoctypeDialog(shell, rootElementName, "", rootElementName + ".dtd"); //$NON-NLS-1$ //$NON-NLS-2$
			if (title == null) {
				title = XMLCommonResources.getInstance().getString("_UI_MENU_ADD_DTD_INFORMATION_TITLE"); //$NON-NLS-1$
			}
		}

		dialog.setComputeSystemId(doctype == null || doctype.getSystemId() == null || doctype.getSystemId().trim().length() == 0);

		dialog.setErrorChecking(false);//!model.getType().equals(IStructuredModel.HTML));
		dialog.create();
		dialog.getShell().setText(title);
		dialog.setBlockOnOpen(true);
		dialog.setResourceLocation(new Path(resourceLocation));
		dialog.open();

		return dialog;
	}


	protected void updateDoctype(EditDoctypeDialog dialog, DocumentType doctype) {
		if (doctype instanceof XMLDocumentType) {
			XMLDocumentType doctypeImpl = (XMLDocumentType) doctype;
			if (doctypeImpl.getName().equals(dialog.getName())) {
				doctypeImpl.setPublicId(dialog.getPublicId());
				doctypeImpl.setSystemId(dialog.getSystemId());
			} else {
				// we need to create a new one and remove the old
				//                  
				Document document = doctype.getOwnerDocument();
				DocumentType newDoctype = createDoctype(dialog, document);
				document.insertBefore(newDoctype, doctype);
				document.removeChild(doctype);
				//manager.reformat(newDoctype, false);
			}
		}
	}
}
