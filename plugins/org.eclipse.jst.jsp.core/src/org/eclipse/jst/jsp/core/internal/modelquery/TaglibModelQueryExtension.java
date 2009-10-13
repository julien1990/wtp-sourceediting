/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.jsp.core.internal.modelquery;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jst.jsp.core.internal.contentmodel.TaglibController;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TLDCMDocumentManager;
import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TaglibTracker;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.extension.ModelQueryExtension;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.w3c.dom.Element;

/**
 * An implementation of {@link ModelQueryExtension} for tag libraries in JSP documents
 */
public class TaglibModelQueryExtension extends ModelQueryExtension {
	
	/**
	 * @see org.eclipse.wst.xml.core.internal.contentmodel.modelquery.extension.ModelQueryExtension#getAvailableElementContent(org.w3c.dom.Element, java.lang.String, int)
	 */
	public CMNode[] getAvailableElementContent(Element parentElement,
			String namespace, int includeOptions) {
		
		CMNode[] nodes = EMPTY_CMNODE_ARRAY;
		ArrayList nodeList = new ArrayList();
		
		if(parentElement instanceof IDOMElement) {
			//get the trackers
			IDOMElement elem = (IDOMElement)parentElement;
			IStructuredDocument structDoc = elem.getModel().getStructuredDocument();
			TLDCMDocumentManager manager = TaglibController.getTLDCMDocumentManager(structDoc);
			List trackers = manager.getCMDocumentTrackers(elem.getStartOffset());
			
			//for each tracker add each of its elements to the node list
			for(int trackerIndex = 0; trackerIndex < trackers.size(); ++trackerIndex) {
				CMNamedNodeMap elements = ((TaglibTracker)trackers.get(trackerIndex)).getElements();
				for(int elementIndex = 0; elementIndex < elements.getLength(); ++elementIndex) {
					nodeList.add(elements.item(elementIndex));
				}
			}
			
			nodes = (CMNode[])nodeList.toArray(new CMNode[nodeList.size()]);
		}
		
		return nodes;
	}
}
