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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.common.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.common.contentmodel.CMDataType;
import org.eclipse.wst.common.contentmodel.CMElementDeclaration;
import org.eclipse.wst.common.contentmodel.CMNode;
import org.eclipse.wst.common.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.common.contentmodel.util.CMDescriptionBuilder;
import org.eclipse.wst.common.contentmodel.util.DOMContentBuilder;
import org.eclipse.wst.common.contentmodel.util.DOMContentBuilderImpl;
import org.eclipse.wst.common.contentmodel.util.DOMNamespaceHelper;
import org.eclipse.wst.sse.core.IStructuredModel;
import org.eclipse.wst.xml.ui.internal.XMLUIPlugin;
import org.eclipse.wst.xml.ui.internal.editor.CMImageUtil;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

public abstract class AbstractNodeActionManager extends BaseNodeActionManager {


	/**
	 * AddNodeAction
	 */
	public class AddNodeAction extends NodeAction {
		protected CMNode cmnode;
		protected String description;
		protected int index;
		protected int nodeType;
		protected Node parent;
		protected String undoDescription;


		public AddNodeAction(CMNode cmnode, Node parent, int index) {
			this.cmnode = cmnode;
			this.parent = parent;
			this.index = index;

			String text = getLabel(parent, cmnode);
			setText(text);
			description = text;
			undoDescription = XMLUIPlugin.getResourceString("%_UI_MENU_ADD") + " " + text; //$NON-NLS-1$ //$NON-NLS-2$
			ImageDescriptor descriptor = CMImageUtil.getImageDescriptor(cmnode);
			if (descriptor == null) {
				descriptor = imageDescriptorCache.getImageDescriptor(cmnode);
			}
			setImageDescriptor(descriptor);
		}


		public AddNodeAction(int nodeType, Node parent, int index) {
			this.nodeType = nodeType;
			this.index = index;
			this.parent = parent;

			switch (nodeType) {
				case Node.COMMENT_NODE : {
					description = XMLUIPlugin.getResourceString("%_UI_MENU_COMMENT"); //$NON-NLS-1$
					undoDescription = XMLUIPlugin.getResourceString("%_UI_MENU_ADD_COMMENT"); //$NON-NLS-1$
					break;
				}
				case Node.PROCESSING_INSTRUCTION_NODE : {
					description = XMLUIPlugin.getResourceString("%_UI_MENU_PROCESSING_INSTRUCTION"); //$NON-NLS-1$
					undoDescription = XMLUIPlugin.getResourceString("%_UI_MENU_ADD_PROCESSING_INSTRUCTION"); //$NON-NLS-1$
					break;
				}
				case Node.CDATA_SECTION_NODE : {
					description = XMLUIPlugin.getResourceString("%_UI_MENU_CDATA_SECTION"); //$NON-NLS-1$
					undoDescription = XMLUIPlugin.getResourceString("%_UI_MENU_ADD_CDATA_SECTION"); //$NON-NLS-1$
					break;
				}
				case Node.TEXT_NODE : {
					description = XMLUIPlugin.getResourceString("%_UI_MENU_PCDATA"); //$NON-NLS-1$
					undoDescription = XMLUIPlugin.getResourceString("%_UI_MENU_ADD_PCDATA"); //$NON-NLS-1$
					break;
				}
			}
			setText(description);
			setImageDescriptor(imageDescriptorCache.getImageDescriptor(new Integer(nodeType)));
		}


		protected void addNodeForCMNode() {
			if (parent != null) {
				insert(parent, cmnode, index);
			}
		}


		protected void addNodeForNodeType() {
			Document document = parent.getNodeType() == Node.DOCUMENT_NODE ? (Document) parent : parent.getOwnerDocument();
			Node newChildNode = null;
			boolean format = true;
			switch (nodeType) {
				case Node.COMMENT_NODE : {
					newChildNode = document.createComment(XMLUIPlugin.getResourceString("%_UI_COMMENT_VALUE")); //$NON-NLS-1$
					break;
				}
				case Node.PROCESSING_INSTRUCTION_NODE : {
					newChildNode = document.createProcessingInstruction(XMLUIPlugin.getResourceString("%_UI_PI_TARGET_VALUE"), XMLUIPlugin.getResourceString("%_UI_PI_DATA_VALUE")); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				}
				case Node.CDATA_SECTION_NODE : {
					newChildNode = document.createCDATASection(""); //$NON-NLS-1$
					break;
				}
				case Node.TEXT_NODE : {
					format = false;
					newChildNode = document.createTextNode(parent.getNodeName());
					break;
				}
			}

			if (newChildNode != null) {
				List list = new Vector(1);
				list.add(newChildNode);
				insertNodesAtIndex(parent, list, index, format);
			}
		}


		public String getUndoDescription() {
			return undoDescription;
		}


		public void run() {
			beginNodeAction(this);
			if (cmnode != null) {
				addNodeForCMNode();
			} else {
				addNodeForNodeType();
			}
			endNodeAction(this);
		}
	}


	/**
	 * DeleteAction
	 */
	public class DeleteAction extends NodeAction {
		protected List list;

		public DeleteAction(List list) {
			setText(XMLUIPlugin.getResourceString("%_UI_MENU_REMOVE")); //$NON-NLS-1$
			this.list = list;
		}

		public DeleteAction(Node node) {
			setText(XMLUIPlugin.getResourceString("%_UI_MENU_REMOVE")); //$NON-NLS-1$
			list = new Vector();
			list.add(node);
		}

		public String getUndoDescription() {
			return XMLUIPlugin.getResourceString("%DELETE"); //$NON-NLS-1$
		}

		public void run() {
			beginNodeAction(this);

			for (Iterator i = list.iterator(); i.hasNext();) {
				Node node = (Node) i.next();
				if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
					Attr attr = (Attr) node;
					attr.getOwnerElement().removeAttributeNode(attr);
				} else {
					Node parent = node.getParentNode();
					if (parent != null) {
						Node previousSibling = node.getPreviousSibling();
						if (previousSibling != null && isWhitespaceTextNode(previousSibling)) {
							parent.removeChild(previousSibling);
						}
						parent.removeChild(node);
					}
				}
			}

			endNodeAction(this);
		}
	}


	class ImageDescriptorCache {
		protected ImageDescriptor attributeImage = XMLEditorPluginImageHelper.getInstance().getImageDescriptor(XMLEditorPluginImages.IMG_OBJ_ATTRIBUTE);
		protected ImageDescriptor attributeReqImage = XMLEditorPluginImageHelper.getInstance().getImageDescriptor(XMLEditorPluginImages.IMG_OBJ_ATT_REQ_OBJ);
		protected ImageDescriptor cdataSectionImage = XMLEditorPluginImageHelper.getInstance().getImageDescriptor(XMLEditorPluginImages.IMG_OBJ_CDATASECTION);
		protected ImageDescriptor commentImage = XMLEditorPluginImageHelper.getInstance().getImageDescriptor(XMLEditorPluginImages.IMG_OBJ_COMMENT);
		protected ImageDescriptor elementImage = XMLEditorPluginImageHelper.getInstance().getImageDescriptor(XMLEditorPluginImages.IMG_OBJ_ELEMENT);
		protected ImageDescriptor piImage = XMLEditorPluginImageHelper.getInstance().getImageDescriptor(XMLEditorPluginImages.IMG_OBJ_PROCESSINGINSTRUCTION);
		protected ImageDescriptor textImage = XMLEditorPluginImageHelper.getInstance().getImageDescriptor(XMLEditorPluginImages.IMG_OBJ_TXTEXT);

		public ImageDescriptor getImageDescriptor(Object object) {
			ImageDescriptor result = null;
			if (object instanceof CMNode) {
				CMNode cmnode = (CMNode) object;
				switch (cmnode.getNodeType()) {
					case CMNode.ATTRIBUTE_DECLARATION : {
						result = CMImageUtil.getImageDescriptor(cmnode);
						if (result == null)
							if (((CMAttributeDeclaration) cmnode).getUsage() == CMAttributeDeclaration.REQUIRED)
								result = attributeReqImage;
							else
								result = attributeImage;
						break;
					}
					case CMNode.DATA_TYPE : {
						result = textImage;
						break;
					}
					case CMNode.ELEMENT_DECLARATION : {
						result = CMImageUtil.getImageDescriptor(cmnode);
						if (result == null)
							result = elementImage;
						break;
					}
					case CMNode.GROUP : {
						result = elementImage;
						break;
					}
				}
			} else if (object instanceof Integer) {
				Integer integer = (Integer) object;
				switch (integer.intValue()) {
					case Node.COMMENT_NODE : {
						result = commentImage;
						break;
					}
					case Node.PROCESSING_INSTRUCTION_NODE : {
						result = piImage;
						break;
					}
					case Node.CDATA_SECTION_NODE : {
						result = cdataSectionImage;
						break;
					}
					case Node.TEXT_NODE : {
						result = textImage;
						break;
					}
				}
			}
			return result;
		}
	}

	// TODO... remove this class. I'm pretty sure it is no longer used by
	// anyone.
	/**
	 * @depracated
	 */
	public class InsertAction extends NodeAction {
		protected String description;
		protected int index;
		protected int nodeType;
		protected Node parent;

		public InsertAction(int nodeType, Node parent, int index) {
			this.nodeType = nodeType;
			this.index = index;
			this.parent = parent;
			switch (nodeType) {
				case Node.COMMENT_NODE : {
					description = XMLUIPlugin.getResourceString("%_UI_MENU_COMMENT"); //$NON-NLS-1$
					break;
				}
				case Node.PROCESSING_INSTRUCTION_NODE : {
					description = XMLUIPlugin.getResourceString("%_UI_MENU_PROCESSING_INSTRUCTION"); //$NON-NLS-1$
					break;
				}
				case Node.CDATA_SECTION_NODE : {
					description = XMLUIPlugin.getResourceString("%_UI_MENU_CDATA_SECTION"); //$NON-NLS-1$
					break;
				}
				case Node.TEXT_NODE : {
					description = XMLUIPlugin.getResourceString("%_UI_MENU_PCDATA"); //$NON-NLS-1$
					break;
				}
			}
			setText(description);
			setImageDescriptor(imageDescriptorCache.getImageDescriptor(new Integer(nodeType)));
		}

		public InsertAction(int nodeType, Node parent, int index, String title) {
			this.nodeType = nodeType;
			this.index = index;
			this.parent = parent;
			description = title;
			setText(description);
			setImageDescriptor(imageDescriptorCache.getImageDescriptor(new Integer(nodeType)));
		}

		public String getUndoDescription() {
			return XMLUIPlugin.getResourceString("%_UI_MENU_ADD") + " " + description; //$NON-NLS-1$ //$NON-NLS-2$
		}

		public void run() {
			beginNodeAction(this);

			Document document = parent.getNodeType() == Node.DOCUMENT_NODE ? (Document) parent : parent.getOwnerDocument();
			Node newChildNode = null;
			boolean format = true;
			switch (nodeType) {
				case Node.COMMENT_NODE : {
					newChildNode = document.createComment(XMLUIPlugin.getResourceString("%_UI_COMMENT_VALUE")); //$NON-NLS-1$
					break;
				}
				case Node.PROCESSING_INSTRUCTION_NODE : {
					newChildNode = document.createProcessingInstruction(XMLUIPlugin.getResourceString("%_UI_PI_TARGET_VALUE"), XMLUIPlugin.getResourceString("%_UI_PI_DATA_VALUE")); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				}
				case Node.CDATA_SECTION_NODE : {
					newChildNode = document.createCDATASection(""); //$NON-NLS-1$
					break;
				}
				case Node.TEXT_NODE : {
					format = false;
					newChildNode = document.createTextNode(parent.getNodeName());
					break;
				}
			}

			if (newChildNode != null) {
				List list = new Vector(1);
				list.add(newChildNode);
				insertNodesAtIndex(parent, list, index, format);
			}

			endNodeAction(this);
		}
	}


	/**
	 * ReplaceNodeAction
	 */
	public class ReplaceNodeAction extends NodeAction {
		protected CMNode cmnode;
		protected String description;
		protected int endIndex;
		protected Node parent;
		protected int startIndex;


		public ReplaceNodeAction(Node parent, CMNode cmnode, int startIndex, int endIndex) {
			this.parent = parent;
			this.cmnode = cmnode;
			this.startIndex = startIndex;
			this.endIndex = endIndex;

			setText(getLabel(parent, cmnode));
			setImageDescriptor(imageDescriptorCache.getImageDescriptor(cmnode));
		}

		public String getUndoDescription() {
			String result = XMLUIPlugin.getResourceString("%_UI_LABEL_UNDO_REPLACE_DESCRIPTION"); //$NON-NLS-1$
			result += " " + getLabel(parent, cmnode); //$NON-NLS-1$
			return result;
		}

		public void run() {
			beginNodeAction(this);

			if (parent != null && cmnode != null) {
				remove(parent, startIndex, endIndex);
				insert(parent, cmnode, startIndex);
			}
			endNodeAction(this);
		}
	}

	protected ImageDescriptorCache imageDescriptorCache = new ImageDescriptorCache();
	protected Viewer viewer;

	public AbstractNodeActionManager(IStructuredModel model, ModelQuery modelQuery, Viewer viewer) {
		super(model, modelQuery);
		this.viewer = viewer;
	}


	public void beginNodeAction(NodeAction action) {
		model.beginRecording(action, action.getUndoDescription());
	}


	protected Action createAddAttributeAction(Element parent, CMAttributeDeclaration ad) {
		Action action = null;
		if (ad == null) {
			action = new EditAttributeAction(this, parent, null, XMLUIPlugin.getResourceString("%_UI_MENU_NEW_ATTRIBUTE"), XMLUIPlugin.getResourceString("%_UI_MENU_NEW_ATTRIBUTE_TITLE")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			action = new AddNodeAction(ad, parent, -1);
		}
		return action;
	}


	protected Action createAddCDataSectionAction(Node parent, int index) {
		return new AddNodeAction(Node.CDATA_SECTION_NODE, parent, index);
	}


	protected Action createAddCommentAction(Node parent, int index) {
		return new AddNodeAction(Node.COMMENT_NODE, parent, index);
	}


	protected Action createAddDoctypeAction(Document document, int index) {
		return new EditDoctypeAction(model, document, model.getBaseLocation(), XMLUIPlugin.getResourceString("%_UI_MENU_ADD_DTD_INFORMATION")); //$NON-NLS-1$
	}


	protected Action createAddElementAction(Node parent, CMElementDeclaration ed, int index) {
		Action action = null;
		if (ed == null) {
			action = new EditElementAction(this, parent, index, XMLUIPlugin.getResourceString("%_UI_MENU_NEW_ELEMENT"), XMLUIPlugin.getResourceString("%_UI_MENU_NEW_ELEMENT_TITLE")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			action = new AddNodeAction(ed, parent, index);
		}
		return action;
	}


	protected Action createAddPCDataAction(Node parent, CMDataType dataType, int index) {
		Action action = null;
		if (dataType == null) {
			action = new AddNodeAction(Node.TEXT_NODE, parent, index);
		} else {
			action = new AddNodeAction(dataType, parent, index);
		}
		return action;
	}


	protected Action createAddProcessingInstructionAction(Node parent, int index) {
		Node refChild = getRefChildNodeAtIndex(parent, index);
		Action action = new EditProcessingInstructionAction(this, parent, refChild, XMLUIPlugin.getResourceString("%_UI_MENU_ADD_PROCESSING_INSTRUCTION"), XMLUIPlugin.getResourceString("%ADD_PROCESSING_INSTRUCTION")); //$NON-NLS-1$ //$NON-NLS-2$
		action.setImageDescriptor(imageDescriptorCache.getImageDescriptor(new Integer(Node.PROCESSING_INSTRUCTION_NODE)));
		return action;
	}


	protected Action createAddSchemaInfoAction(Element element) {
		return new EditSchemaInfoAction(this, element.getOwnerDocument(), model.getBaseLocation(), XMLUIPlugin.getResourceString("%_UI_MENU_ADD_SCHEMA_INFORMATION")); //$NON-NLS-1$
	}


	protected Action createDeleteAction(List selection) {
		DeleteAction deleteAction = new DeleteAction(selection);
		deleteAction.setEnabled(selection.size() > 0);
		return deleteAction;
	}


	public DOMContentBuilder createDOMContentBuilder(Document document) {
		DOMContentBuilderImpl builder = new DOMContentBuilderImpl(document);
		return builder;
	}


	protected Action createEditAttributeAction(Attr attr, CMAttributeDeclaration ad) {
		return new EditAttributeAction(this, attr.getOwnerElement(), attr, XMLUIPlugin.getResourceString("%_UI_MENU_EDIT_ATTRIBUTE"), XMLUIPlugin.getResourceString("%_UI_MENU_EDIT_ATTRIBUTE_TITLE")); //$NON-NLS-1$ //$NON-NLS-2$
	}


	protected Action createEditDoctypeAction(DocumentType doctype) {
		return new EditDoctypeAction(model, doctype, model.getBaseLocation(), XMLUIPlugin.getResourceString("%_UI_MENU_EDIT_DOCTYPE")); //$NON-NLS-1$
	}


	protected Action createEditProcessingInstructionAction(ProcessingInstruction pi) {
		return new EditProcessingInstructionAction(this, pi, XMLUIPlugin.getResourceString("%_UI_MENU_EDIT_PROCESSING_INSTRUCTION"), XMLUIPlugin.getResourceString("%_UI_MENU_EDIT_PROCESSING_INSTRUCTION_TITLE")); //$NON-NLS-1$ //$NON-NLS-2$
	}


	protected Action createEditSchemaInfoAction(Element element) {
		return new EditSchemaInfoAction(this, element.getOwnerDocument(), model.getBaseLocation(), XMLUIPlugin.getResourceString("%_UI_MENU_EDIT_NAMESPACES")); //$NON-NLS-1$
	}


	protected Action createRenameAction(Node node) {
		Action result = null;
		if (node instanceof Element) {
			result = new EditElementAction(this, (Element) node, XMLUIPlugin.getResourceString("%_UI_MENU_RENAME"), XMLUIPlugin.getResourceString("%_UI_MENU_RENAME_TITLE")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return result;
	}


	protected Action createReplaceAction(Node parent, CMNode cmnode, int startIndex, int endIndex) {
		return new ReplaceNodeAction(parent, cmnode, startIndex, endIndex);
	}

	public void endNodeAction(NodeAction action) {
		model.endRecording(action);
	}



	public void fillContextMenu(IMenuManager menuManager, ISelection selection) {
		try {
			List selectionList = new ArrayList();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection es = (IStructuredSelection) selection;
				for (Iterator i = es.iterator(); i.hasNext();) {
					selectionList.add(i.next());
				}
			}

			contributeActions(menuManager, selectionList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  
	 */
	public String getLabel(Node parent, CMNode cmnode) {
		String result = "?" + cmnode + "?"; //$NON-NLS-1$ //$NON-NLS-2$
		if (cmnode != null) {
			result = (String) cmnode.getProperty("description"); //$NON-NLS-1$
			if (result == null) {
				if (cmnode.getNodeType() == CMNode.GROUP) {
					CMDescriptionBuilder descriptionBuilder = new CMDescriptionBuilder();
					result = descriptionBuilder.buildDescription(cmnode);
				} else {
					result = DOMNamespaceHelper.computeName(cmnode, parent, null);
				}
			}
		}
		return result;
	}


	public IStructuredModel getModel() {
		return model;
	}


	public Shell getWorkbenchWindowShell() {
		return XMLUIPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell();
	}


	public void insert(Node parent, CMNode cmnode, int index) {
		Document document = parent.getNodeType() == Node.DOCUMENT_NODE ? (Document) parent : parent.getOwnerDocument();
		DOMContentBuilder builder = createDOMContentBuilder(document);
		builder.setBuildPolicy(DOMContentBuilder.BUILD_ONLY_REQUIRED_CONTENT);
		builder.build(parent, cmnode);
		insertNodesAtIndex(parent, builder.getResult(), index);
	}


	public void insertNodesAtIndex(Node parent, List list, int index) {
		insertNodesAtIndex(parent, list, index, true);
	}


	public void insertNodesAtIndex(Node parent, List list, int index, boolean format) {
		NodeList nodeList = parent.getChildNodes();
		if (index == -1) {
			index = nodeList.getLength();
		}
		Node refChild = (index < nodeList.getLength()) ? nodeList.item(index) : null;

		// here we consider the case where the previous node is a 'white
		// space' Text node
		// we should really do the insert before this node
		//
		int prevIndex = index - 1;
		Node prevChild = (prevIndex < nodeList.getLength()) ? nodeList.item(prevIndex) : null;
		if (isWhitespaceTextNode(prevChild)) {
			refChild = prevChild;
		}

		for (Iterator i = list.iterator(); i.hasNext();) {
			Node newNode = (Node) i.next();

			if (newNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Element parentElement = (Element) parent;
				parentElement.setAttributeNode((Attr) newNode);
			} else {
				parent.insertBefore(newNode, refChild);
			}
		}

		boolean formatDeep = false;
		for (Iterator i = list.iterator(); i.hasNext();) {
			Node newNode = (Node) i.next();
			if (newNode.getNodeType() == Node.ELEMENT_NODE) {
				formatDeep = true;
			}

			if (format) {
				reformat(newNode, formatDeep);
			}
		}

		setViewerSelection(list);
	}


	/**
	 * This method is abstract since currently, the sed editor is required to
	 * perform formating and we don't want to create a dependency on the sed
	 * editor.
	 */
	public abstract void reformat(Node parent, boolean deep);


	public void remove(Node parent, int startIndex, int endIndex) {
		NodeList nodeList = parent.getChildNodes();
		for (int i = endIndex; i >= startIndex; i--) {
			Node node = nodeList.item(i);
			if (node != null) {
				parent.removeChild(node);
			}
		}
	}


	public void setViewerSelection(List list) {
		if (viewer != null) {
			viewer.setSelection(new StructuredSelection(list), true);
		}
	}


	public void setViewerSelection(Node node) {
		if (viewer != null) {
			viewer.setSelection(new StructuredSelection(node), true);
		}
	}
}
