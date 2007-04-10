/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.wst.xml.ui.internal.editor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
import org.eclipse.wst.xml.ui.internal.XMLUIPlugin;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author nsd
 */
public class CMImageUtil {

	public static CMNode getDeclaration(Node node) {
		CMNode decl = null;
		ModelQuery mq = null;
		switch (node.getNodeType()) {
			case Node.ATTRIBUTE_NODE : {
				mq = ModelQueryUtil.getModelQuery(((Attr) node).getOwnerDocument());
				decl = mq.getCMAttributeDeclaration((Attr) node);
			}
				break;
			case Node.ELEMENT_NODE : {
				mq = ModelQueryUtil.getModelQuery(node.getOwnerDocument());
				decl = mq.getCMElementDeclaration((Element) node);
			}
				break;
		}
		return decl;
	}

	public static Image getImage(CMNode cmnode) {
		if (cmnode == null) {
			return null;
		}
		Image image = null;
		ImageDescriptor descriptor = getImageDescriptor(cmnode);
		if (descriptor != null) {
			image = descriptor.createImage(false);
		}
		return image;
	}

	public static ImageDescriptor getImageDescriptor(CMNode cmnode) {
		if (cmnode == null) {
			return null;
		}
		// cache CM-specified images with the XML UI plugin
		String imageURLString = (String) cmnode.getProperty("small-icon"); //$NON-NLS-1$
		ImageDescriptor descriptor = null;
		if ((imageURLString != null) && (imageURLString.length() > 0)) {
			descriptor = XMLUIPlugin.getInstance().getImageRegistry().getDescriptor(imageURLString);
			if (descriptor == null) {
				try {
					URL imageURL = new URL(imageURLString);
					URLConnection connection = imageURL.openConnection();
					connection.setUseCaches(false);
					ImageData data = new ImageData(connection.getInputStream());
					descriptor = ImageDescriptor.createFromImageData(data);
					XMLUIPlugin.getInstance().getImageRegistry().put(imageURLString, descriptor);
				}
				catch (MalformedURLException e) {
					descriptor = null;
				}
				catch (IOException e) {
					descriptor = null;
				}
			}
		}
		return descriptor;
	}

	/**
	 * 
	 */
	private CMImageUtil() {
		super();
	}
}
