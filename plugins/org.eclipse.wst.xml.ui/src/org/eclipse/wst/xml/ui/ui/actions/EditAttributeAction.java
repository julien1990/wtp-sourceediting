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
package org.eclipse.wst.xml.ui.ui.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
import org.eclipse.wst.xml.ui.ui.XMLCommonResources;
import org.eclipse.wst.xml.ui.ui.dialogs.EditAttributeDialog;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



public class EditAttributeAction extends NodeAction {
	protected AbstractNodeActionManager manager;
	protected String title;
	protected Element ownerElement;
	protected Attr attr;
	protected static ImageDescriptor imageDescriptor;

	public static ImageDescriptor createImageDescriptor() {
		if (imageDescriptor == null) {
			imageDescriptor = XMLEditorPluginImageHelper.getInstance().getImageDescriptor(XMLEditorPluginImages.IMG_OBJ_ATTRIBUTE);
		}
		return imageDescriptor;
	}

	public EditAttributeAction(AbstractNodeActionManager manager, Element ownerElement, Attr attr, String actionLabel, String title) {
		this.manager = manager;
		this.ownerElement = ownerElement;
		this.attr = attr;
		this.title = title;
		setText(actionLabel);
		// assume if attr is null then this is an 'Add' that requires action an icons... otherwise this is an edit
		if (attr == null) {
			setImageDescriptor(createImageDescriptor());
		}
	}

	public String getUndoDescription() {
		return title;
	}

	public void run() {
		manager.beginNodeAction(this);
		Shell shell = XMLCommonResources.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell();
		EditAttributeDialog dialog = new EditAttributeDialog(shell, ownerElement, attr);
		dialog.create();
		dialog.getShell().setText(title);
		dialog.setBlockOnOpen(true);
		dialog.open();

		if (dialog.getReturnCode() == Window.OK) {
			if (attr != null) {
				ownerElement.removeAttributeNode(attr);
			}
			Document document = ownerElement.getOwnerDocument();
			Attr newAttribute = document.createAttribute(dialog.getAttributeName());
			newAttribute.setValue(dialog.getAttributeValue());
			ownerElement.setAttributeNode(newAttribute);
			manager.setViewerSelection(newAttribute);
		}
		manager.endNodeAction(this);
	}
}

