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
package org.eclipse.wst.html.core.document;



/**
 */
public class HTMLDocumentTypeEntry {

	private String name;
	private String publicId;
	private String systemId;
	private String namespaceURI;
	private boolean isXMLType;
	private boolean isXHTMLType;
	private boolean isWMLType;
	private boolean hasFrameset;
	private String displayName;
	private boolean isDefaultXHTML;
	private boolean isDefaultWML;
	private static String XHTML_NAME = "html"; //$NON-NLS-1$
	private static String HTML_NAME = "HTML"; //$NON-NLS-1$

	/**
	 */
	private HTMLDocumentTypeEntry() {
		super();
	}

	/**
	 */
	public HTMLDocumentTypeEntry(String name, String publicId, String systemId, String namespaceURI, boolean isXHTMLType, boolean hasFrameset, String displayName, boolean isDefaultXHTML, boolean isDefaultWML, boolean isWMLType) {
		super();

		if (name != null && name.length() > 0) {
			this.name = name;
		}
		else {
			if (isXMLType)
				this.name = XHTML_NAME;// need code for wml?
			else
				this.name = HTML_NAME;
		}
		this.publicId = publicId;
		this.systemId = systemId;
		this.namespaceURI = namespaceURI;
		this.isXMLType = isXHTMLType | isWMLType;
		this.hasFrameset = hasFrameset;
		this.displayName = displayName;
		this.isDefaultXHTML = isDefaultXHTML;
		this.isDefaultWML = isDefaultWML;
		this.isXHTMLType = isXHTMLType;
		this.isWMLType = isWMLType;
	}

	/**
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 */
	public final String getNamespaceURI() {
		return this.namespaceURI;
	}

	/**
	 */
	public final String getPublicId() {
		return this.publicId;
	}

	/**
	 */
	public final String getSystemId() {
		return this.systemId;
	}

	/**
	 */
	public final boolean isXMLType() {
		return this.isXMLType;
	}

	/**
	 */
	public final boolean hasFrameset() {
		return this.hasFrameset;
	}

	/**
	 */
	public final String getDisplayName() {
		return this.displayName;
	}

	/**
	 */
	public final boolean isDefaultXHTML() {
		return this.isDefaultXHTML;
	}

	public final boolean isDefaultWML() {
		return this.isDefaultWML;
	}


	public final boolean isXHTMLType() {
		return isXHTMLType;
	}

	public final boolean isWMLType() {
		return isWMLType;
	}

}