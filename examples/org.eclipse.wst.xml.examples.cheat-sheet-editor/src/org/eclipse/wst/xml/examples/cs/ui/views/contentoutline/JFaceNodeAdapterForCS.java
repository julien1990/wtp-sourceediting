/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jens Lukowski/Innoopract - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.wst.xml.examples.cs.ui.views.contentoutline;

import java.util.ArrayList;

import org.eclipse.wst.sse.core.internal.provisional.INodeAdapterFactory;
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeAdapter;
import org.w3c.dom.Node;


/**
 * Adapts a DOM node to a JFace viewer.
 */
public class JFaceNodeAdapterForCS extends JFaceNodeAdapter {
	final static Class ADAPTER_KEY = IJFaceNodeAdapter.class;

	public JFaceNodeAdapterForCS(INodeAdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	public Object[] getChildren(Object object) {
		Node node = (Node) object;
		
		if (node.getNodeType() == Node.DOCUMENT_NODE) {
			ArrayList v = new ArrayList(node.getChildNodes().getLength());
			for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				Node n = child;

				if (n.getNodeType() == Node.ELEMENT_NODE && "cheatsheet".equals(n.getNodeName())) {
					v.add(n);
				}
			}
			return v.toArray();
		} else if ("description".equals(node.getNodeName())) {
			return new Object[0];
		}
		ArrayList v = new ArrayList(node.getChildNodes().getLength());
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			Node n = child;
			if (n.getNodeType() != Node.TEXT_NODE) {
				v.add(n);
			}
		}
		return v.toArray();
	}

	/**
	 * Fetches the label text specific to this object instance.
	 */
	public String getLabelText(Object node) {
		return getNodeName(node);
	}

	private String getNodeName(Object object) {
		Node node = (Node) object;
		String nodeName = node.getNodeName();
		if ("cheatsheet".equals(nodeName)) {
			Node titleNode = node.getAttributes().getNamedItem("title");
			String title = "";
			if (titleNode != null) {
				title = titleNode.getNodeValue();
			}
			return "Title:" + title;
		}
		if ("item".equals(nodeName)) {
			Node titleNode = node.getAttributes().getNamedItem("title");
			String title = "";
			if (titleNode != null) {
				title = titleNode.getNodeValue();
			}
			return "Title:" + title;
		}
		if ("action".equals(nodeName)) {
			Node classNode = node.getAttributes().getNamedItem("class");
			String className = "";
			if (classNode != null) {
				className = classNode.getNodeValue();
				int index = className.lastIndexOf(".");
				
				if (index != -1) {
					className = className.substring(index + 1);
				}
			}
			return "Action:" + className;
		}
		return nodeName;
	}

	public Object getParent(Object object) {

		Node node = (Node) object;
		return node.getParentNode();
	}

	public boolean hasChildren(Object object) {
		Node node = (Node) object;
		if ("description".equals(node.getNodeName())) {
			return false;
		}
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() != Node.TEXT_NODE)
				return true;
		}
		return false;
	}

	/**
	 * Allowing the INodeAdapter to compare itself against the type allows it
	 * to return true in more than one case.
	 */
	public boolean isAdapterForType(Object type) {
		return type.equals(ADAPTER_KEY);
	}

}
