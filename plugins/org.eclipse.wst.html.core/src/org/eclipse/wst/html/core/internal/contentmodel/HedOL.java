/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.html.core.internal.contentmodel;



import java.util.Arrays;

import org.eclipse.wst.html.core.HTML40Namespace;
import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;

/**
 * OL.
 */
final class HedOL extends HedListItemContainer {

	/**
	 */
	public HedOL(ElementCollection collection) {
		super(HTML40Namespace.ElementName.OL, collection);
	}

	/**
	 * OL.
	 * (type %OLStyle; #IMPLIED) ... should be defined locally.
	 * (compact (compact) #IMPLIED)
	 * (start NUMBER #IMPLIED)
	 */
	protected void createAttributeDeclarations() {
		if (attributes != null)
			return; // already created.
		if (attributeCollection == null)
			return; // fatal

		attributes = new CMNamedNodeMapImpl();

		// %attrs;
		attributeCollection.getAttrs(attributes);

		// (type %OLStyle; #IMPLIED) ... should be defined locally.
		HTMLCMDataTypeImpl atype = new HTMLCMDataTypeImpl(HTMLCMDataType.OL_STYLE);
		HTMLAttrDeclImpl attr = new HTMLAttrDeclImpl(HTML40Namespace.ATTR_NAME_TYPE, atype, CMAttributeDeclaration.OPTIONAL);
		attributes.putNamedItem(HTML40Namespace.ATTR_NAME_TYPE, attr);

		// the rest.
		String[] names = {HTML40Namespace.ATTR_NAME_COMPACT, HTML40Namespace.ATTR_NAME_START};
		attributeCollection.getDeclarations(attributes, Arrays.asList(names).iterator());
	}

	/**
	 */
	public CMNamedNodeMap getProhibitedAncestors() {
		if (prohibitedAncestors != null)
			return prohibitedAncestors;

		String[] names = {HTML40Namespace.ElementName.DIR, HTML40Namespace.ElementName.MENU};
		prohibitedAncestors = elementCollection.getDeclarations(names);

		return prohibitedAncestors;
	}
}