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
package org.eclipse.wst.xml.core.internal.document;



import org.eclipse.wst.sse.core.text.IStructuredDocumentRegion;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;


/**
 * CharacterDataImpl class
 */
public abstract class CharacterDataImpl extends NodeImpl implements CharacterData {

	private String data = null;

	/**
	 * CharacterDataImpl constructor
	 */
	protected CharacterDataImpl() {
		super();
	}

	/**
	 * CharacterDataImpl constructor
	 * 
	 * @param that
	 *            CharacterDataImpl
	 */
	protected CharacterDataImpl(CharacterDataImpl that) {
		super(that);

		if (that != null) {
			this.data = that.getData();
		}
	}

	/**
	 * appendData method
	 * 
	 * @param arg
	 *            java.lang.String
	 */
	public void appendData(String arg) throws DOMException {
		if (arg == null)
			return;

		String data = getData();
		if (data == null)
			data = arg;
		else
			data += arg;
		setData(data);
	}

	/**
	 * deleteData method
	 * 
	 * @param offset
	 *            int
	 * @param count
	 *            int
	 */
	public void deleteData(int offset, int count) throws DOMException {
		if (count == 0)
			return;
		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		if (count < 0 || offset < 0) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
		}

		String data = getData();
		if (data == null) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
		}
		int length = data.length();
		if (offset > length) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
		}
		if (offset == 0) {
			if (count > length) {
				throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
			}
			if (count == length)
				data = new String();
			else
				data = data.substring(count);
		} else {
			int end = offset + count;
			if (end > length) {
				throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
			}
			if (end == length)
				data = data.substring(0, offset);
			else
				data = data.substring(0, offset) + data.substring(end);
		}
		setData(data);
	}

	/**
	 */
	protected final String getCharacterData() {
		return this.data;
	}

	/**
	 * getData method
	 * 
	 * @return java.lang.String
	 */
	public String getData() throws DOMException {
		return getCharacterData();
	}

	/**
	 * getLength method
	 * 
	 * @return int
	 */
	public int getLength() {
		String data = getData();
		if (data == null)
			return 0;
		return data.length();
	}

	/**
	 * getNodeValue method
	 * 
	 * @return java.lang.String
	 */
	public String getNodeValue() {
		return getData();
	}

	/**
	 * insertData method
	 * 
	 * @param offset
	 *            int
	 * @param arg
	 *            java.lang.String
	 */
	public void insertData(int offset, String arg) throws DOMException {
		if (arg == null)
			return;

		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		if (offset < 0) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
		}

		String data = getData();
		if (data == null) {
			if (offset > 0) {
				throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
			}
			data = arg;
		} else if (offset == 0) {
			data = arg + data;
		} else {
			int length = data.length();
			if (offset > length) {
				throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
			}
			if (offset == length)
				data += arg;
			else
				data = data.substring(0, offset) + arg + data.substring(offset);
		}
		setData(data);
	}

	/**
	 * isJSPContent method
	 * 
	 * @return boolean
	 */
	public boolean isJSPContent() {
		Node parent = getParentNode();
		if (parent == null || parent.getNodeType() != Node.ELEMENT_NODE)
			return false;
		ElementImpl element = (ElementImpl) parent;
		return element.isJSPContainer();
	}

	/**
	 * replaceData method
	 * 
	 * @param offset
	 *            int
	 * @param count
	 *            int
	 * @param arg
	 *            java.lang.String
	 */
	public void replaceData(int offset, int count, String arg) throws DOMException {
		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		if (arg == null) {
			deleteData(offset, count);
			return;
		}
		if (count == 0) {
			insertData(offset, arg);
			return;
		}
		if (offset < 0 || count < 0) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
		}

		String data = getData();
		if (data == null) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
		} else if (offset == 0) {
			int length = data.length();
			if (count > length) {
				throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
			}
			if (count == length)
				data = arg;
			else
				data = arg + data.substring(count);
		} else {
			int length = data.length();
			int end = offset + count;
			if (end > length) {
				throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
			}
			if (end == length)
				data = data.substring(0, offset) + arg;
			else
				data = data.substring(0, offset) + arg + data.substring(end);
		}
		setData(data);
	}

	/**
	 */
	void resetStructuredDocumentRegions() {
		this.data = getData();
		setStructuredDocumentRegion(null);
	}

	/**
	 * setData method
	 * 
	 * @param data
	 *            java.lang.String
	 */
	public void setData(String data) throws DOMException {
		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		this.data = data;

		notifyValueChanged();
	}

	/**
	 * setNodeValue method
	 * 
	 * @param nodeValue
	 *            java.lang.String
	 */
	public void setNodeValue(String nodeValue) throws DOMException {
		setData(nodeValue);
	}

	/**
	 */
	void setStructuredDocumentRegion(IStructuredDocumentRegion flatNode) {
		super.setStructuredDocumentRegion(flatNode);
		if (flatNode != null)
			this.data = null;
	}

	/**
	 * substringData method
	 * 
	 * @return java.lang.String
	 * @param offset
	 *            int
	 * @param count
	 *            int
	 */
	public String substringData(int offset, int count) throws DOMException {
		if (count == 0)
			return new String();
		if (offset < 0 || count < 0) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
		}

		String data = getData();
		if (data == null) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
		}
		int length = data.length();
		if (offset == 0 && count == length)
			return data;
		if (offset > length) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
		}
		int end = offset + count;
		if (end > length) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR, new String());
		}
		return data.substring(offset, end);
	}

	/**
	 * toString method
	 * 
	 * @return java.lang.String
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getNodeName());
		buffer.append('(');
		buffer.append(getData());
		buffer.append(')');
		IStructuredDocumentRegion flatNode = getStructuredDocumentRegion();
		if (flatNode != null) {
			buffer.append('@');
			buffer.append(flatNode.toString());
		}
		return buffer.toString();
	}
}
