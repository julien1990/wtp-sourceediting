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
package org.eclipse.wst.xml.core.internal.document;



import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Notation;

/**
 * NotationImpl class
 */
public class NotationImpl extends NodeImpl implements Notation {

	private String name = null;
	private String publicId = null;
	private String systemId = null;

	/**
	 * NotationImpl constructor
	 */
	protected NotationImpl() {
		super();
	}

	/**
	 * NotationImpl constructor
	 * @param that NotationImpl
	 */
	protected NotationImpl(NotationImpl that) {
		super(that);

		if (that != null) {
			this.name = that.name;
			this.publicId = that.publicId;
			this.systemId = that.systemId;
		}
	}

	/**
	 * cloneNode method
	 * @return org.w3c.dom.Node
	 * @param deep boolean
	 */
	public Node cloneNode(boolean deep) {
		NotationImpl cloned = new NotationImpl(this);
		return cloned;
	}

	/**
	 * getNodeName method
	 * @return java.lang.String
	 */
	public String getNodeName() {
		if (this.name == null)
			return new String();
		return this.name;
	}

	/**
	 * getNodeType method
	 * @return short
	 */
	public short getNodeType() {
		return NOTATION_NODE;
	}

	/**
	 * getPublicId method
	 * @return java.lang.String
	 */
	public String getPublicId() {
		return this.publicId;
	}

	/**
	 * getSystemId method
	 * @return java.lang.String
	 */
	public String getSystemId() {
		return this.systemId;
	}

	/**
	 * setName method
	 * @param name java.lang.String
	 */
	protected void setName(String name) {
		this.name = name;
	}

	/**
	 * setPublicId method
	 * @param publicId java.lang.String
	 */
	public void setPublicId(String publicId) {
		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}
		this.publicId = publicId;
	}

	/**
	 * setSystemId method
	 * @param systemId java.lang.String
	 */
	public void setSystemId(String systemId) {
		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}
		this.systemId = systemId;
	}
}
