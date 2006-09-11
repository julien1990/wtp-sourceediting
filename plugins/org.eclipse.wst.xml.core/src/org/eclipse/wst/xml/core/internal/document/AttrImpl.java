/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *     Balazs Banfai: Bug 154737 getUserData/setUserData support for Node
 *     https://bugs.eclipse.org/bugs/show_bug.cgi?id=154737
 *******************************************************************************/
package org.eclipse.wst.xml.core.internal.document;



import java.util.Iterator;

import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionContainer;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.provisional.IXMLCharEntity;
import org.eclipse.wst.xml.core.internal.provisional.IXMLNamespace;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;


/**
 * AttrImpl class
 */
public class AttrImpl extends NodeImpl implements IDOMAttr {
	private ITextRegion equalRegion = null;

	private String name = null;
	private ITextRegion nameRegion = null;
	private String namespaceURI = null;
	private ElementImpl ownerElement = null;
	private ITextRegion valueRegion = null;
	private String valueSource = null;

	/**
	 * AttrImpl constructor
	 */
	protected AttrImpl() {
		super();
	}

	/**
	 * AttrImpl constructor
	 * 
	 * @param that
	 *            AttrImpl
	 */
	protected AttrImpl(AttrImpl that) {
		super(that);

		if (that != null) {
			this.name = that.name;
			this.valueSource = that.getValueSource();
		}
	}

	/**
	 * cloneNode method
	 * 
	 * @return org.w3c.dom.Node
	 */
	public Node cloneNode(boolean deep) {
		AttrImpl cloned = new AttrImpl(this);
		notifyUserDataHandlers(UserDataHandler.NODE_CLONED, cloned);
		return cloned;
	}

	/**
	 */
	protected CMAttributeDeclaration getDeclaration() {
		ElementImpl element = (ElementImpl) getOwnerElement();
		if (element == null)
			return null;
		CMElementDeclaration elementDecl = element.getDeclaration();
		if (elementDecl == null)
			return null;
		CMNamedNodeMap attributes = elementDecl.getAttributes();
		if (attributes == null)
			return null;
		return (CMAttributeDeclaration) attributes.getNamedItem(getName());
	}

	/**
	 * getEndOffset method
	 * 
	 * @return int
	 */
	public int getEndOffset() {
		if (this.ownerElement == null)
			return 0;
		int offset = this.ownerElement.getStartOffset();
		if (this.valueRegion != null) {
			return (offset + this.valueRegion.getEnd());
		}
		if (this.equalRegion != null) {
			return (offset + this.equalRegion.getEnd());
		}
		if (this.nameRegion != null) {
			return (offset + this.nameRegion.getEnd());
		}
		return 0;
	}


	public ITextRegion getEqualRegion() {
		return this.equalRegion;
	}

	/**
	 */
	public String getLocalName() {
		if (this.name == null)
			return null;
		int index = this.name.indexOf(':');
		if (index < 0)
			return this.name;
		return this.name.substring(index + 1);
	}

	/**
	 * getName method
	 * 
	 * @return java.lang.String
	 */
	public String getName() {
		if (this.name == null)
			return new String();
		return this.name;
	}


	public ITextRegion getNameRegion() {
		return this.nameRegion;
	}

	public int getNameRegionEndOffset() {
		if (this.ownerElement == null)
			return 0;
		// assuming the firstStructuredDocumentRegion is the one that contains
		// attributes
		IStructuredDocumentRegion flatNode = this.ownerElement.getFirstStructuredDocumentRegion();
		if (flatNode == null)
			return 0;
		return flatNode.getEndOffset(this.nameRegion);
	}

	public int getNameRegionStartOffset() {
		if (this.ownerElement == null)
			return 0;
		// assuming the firstStructuredDocumentRegion is the one that contains
		// attributes
		IStructuredDocumentRegion flatNode = this.ownerElement.getFirstStructuredDocumentRegion();
		if (flatNode == null)
			return 0;
		return flatNode.getStartOffset(this.nameRegion);
	}

	public String getNameRegionText() {
		if (this.ownerElement == null)
			return null;
		// assuming the firstStructuredDocumentRegion is the one that contains
		// attributes
		IStructuredDocumentRegion flatNode = this.ownerElement.getFirstStructuredDocumentRegion();
		if (flatNode == null)
			return null;
		return flatNode.getText(this.nameRegion);
	}

	public int getNameRegionTextEndOffset() {
		if (this.ownerElement == null)
			return 0;
		// assuming the firstStructuredDocumentRegion is the one that contains
		// attributes
		IStructuredDocumentRegion flatNode = this.ownerElement.getFirstStructuredDocumentRegion();
		if (flatNode == null)
			return 0;
		return flatNode.getTextEndOffset(this.nameRegion);
	}

	/**
	 */
	public String getNamespaceURI() {
		String nsAttrName = null;
		String prefix = getPrefix();
		if (prefix != null && prefix.length() > 0) {
			if (prefix.equals(IXMLNamespace.XMLNS)) {
				// fixed URI
				return IXMLNamespace.XMLNS_URI;
			}
			nsAttrName = IXMLNamespace.XMLNS_PREFIX + prefix;
		}
		else {
			String name = getName();
			if (name != null && name.equals(IXMLNamespace.XMLNS)) {
				// fixed URI
				return IXMLNamespace.XMLNS_URI;
			}
			// does not inherit namespace from owner element
			// if (this.ownerElement != null) return
			// this.ownerElement.getNamespaceURI();
			return this.namespaceURI;
		}

		for (Node node = this.ownerElement; node != null; node = node.getParentNode()) {
			if (node.getNodeType() != ELEMENT_NODE)
				break;
			Element element = (Element) node;
			Attr attr = element.getAttributeNode(nsAttrName);
			if (attr != null)
				return attr.getValue();
		}

		return this.namespaceURI;
	}

	/**
	 * getNodeName method
	 * 
	 * @return java.lang.String
	 */
	public String getNodeName() {
		return getName();
	}

	/**
	 * getNodeType method
	 * 
	 * @return short
	 */
	public short getNodeType() {
		return ATTRIBUTE_NODE;
	}

	/**
	 * getNodeValue method
	 * 
	 * @return java.lang.String
	 */
	public String getNodeValue() {
		return getValue();
	}

	/**
	 * getOwnerElement method
	 * 
	 * @return org.w3c.dom.Element
	 */
	public Element getOwnerElement() {
		return this.ownerElement;
	}

	/**
	 */
	public String getPrefix() {
		if (this.name == null)
			return null;
		int index = this.name.indexOf(':');
		if (index <= 0)
			return null;
		// exclude JSP tag in name
		if (this.name.charAt(0) == '<')
			return null;
		return this.name.substring(0, index);
	}

	/**
	 * getSpecified method
	 * 
	 * @return boolean
	 */
	public boolean getSpecified() {
		return true;
	}

	/**
	 * getStartOffset method
	 * 
	 * @return int
	 */
	public int getStartOffset() {
		if (this.ownerElement == null)
			return 0;
		int offset = this.ownerElement.getStartOffset();
		if (this.nameRegion != null) {
			return (offset + this.nameRegion.getStart());
		}
		if (this.equalRegion != null) {
			return (offset + this.equalRegion.getStart());
		}
		if (this.valueRegion != null) {
			return (offset + this.valueRegion.getStart());
		}
		return 0;
	}

	/**
	 * getValue method
	 * 
	 * @return java.lang.String
	 */
	public String getValue() {
		return getValue(getValueSource());
	}

	/**
	 * Returns value for the source
	 */
	private String getValue(String source) {
		if (source == null)
			return new String();
		if (source.length() == 0)
			return source;
		StringBuffer buffer = null;
		int offset = 0;
		int length = source.length();
		int ref = source.indexOf('&');
		while (ref >= 0) {
			int end = source.indexOf(';', ref + 1);
			if (end > ref + 1) {
				String name = source.substring(ref + 1, end);
				String value = getCharValue(name);
				if (value != null) {
					if (buffer == null)
						buffer = new StringBuffer(length);
					if (ref > offset)
						buffer.append(source.substring(offset, ref));
					buffer.append(value);
					offset = end + 1;
					ref = end;
				}
			}
			ref = source.indexOf('&', ref + 1);
		}
		if (buffer == null)
			return source;
		if (length > offset)
			buffer.append(source.substring(offset));
		return buffer.toString();
	}

	public ITextRegion getValueRegion() {
		return this.valueRegion;
	}

	/**
	 * ISSUE: what should behavior be if this.value == null? It's an "error"
	 * to be in that state, but seems to occur relatively easily ... probably
	 * due to threading bugs ... but this just shows its needs to be spec'd.
	 * 
	 */
	public int getValueRegionStartOffset() {
		if (this.ownerElement == null)
			return 0;
		// assuming the firstStructuredDocumentRegion is the one that contains
		// the valueRegion -- should make smarter?
		IStructuredDocumentRegion structuredDocumentRegion = this.ownerElement.getFirstStructuredDocumentRegion();
		if (structuredDocumentRegion == null)
			return 0;
		// ensure we never pass null to getStartOffset.
		if (this.valueRegion == null) {
			return 0;
		}
		return structuredDocumentRegion.getStartOffset(this.valueRegion);
	}

	public String getValueRegionText() {
		if (this.ownerElement == null)
			return null;
		// assuming the firstStructuredDocumentRegion is the one that contains
		// attributes
		IStructuredDocumentRegion flatNode = this.ownerElement.getFirstStructuredDocumentRegion();
		if (flatNode == null)
			return null;
		if (this.valueRegion == null)
			return null;
		return flatNode.getText(this.valueRegion);
	}

	/**
	 */
	public String getValueSource() {
		if (this.valueSource != null)
			return this.valueSource;
		// DW: 4/16/2003 due to change in structuredDocument ... we need a
		// flatnode to
		// get at region values. For now I'll assume this is always the first
		// flatnode .. may need to make smarter later (e.g. to search for
		// the flatnode that this.valueRegion belongs to.
		// DW: 4/30/2003 For some reason, this method is getting called a lot
		// Not sure if its a threading problem, or a fundamental error
		// elsewhere.
		// It needs more investigation, but in the use cases I've seen,
		// doesn't
		// seem to hurt to simply return null in those cases. I saw this null
		// case,
		// when tryint go format an XML file.
		if (this.ownerElement == null)
			return null;
		IStructuredDocumentRegion ownerRegion = this.ownerElement.getFirstStructuredDocumentRegion();
		if (ownerRegion == null)
			return null;
		if (this.valueRegion != null)
			return StructuredDocumentRegionUtil.getAttrValue(ownerRegion, this.valueRegion);
		return new String();
	}

	private String getValueSource(ElementImpl ownerElement) {
		if (this.valueSource != null)
			return this.valueSource;
		// DW: 4/16/2003 due to change in structuredDocument ... we need a
		// flatnode to
		// get at region values. For now I'll assume this is always the first
		// flatnode .. may need to make smarter later (e.g. to search for
		// the flatnode that this.valueRegion belongs to.
		if (this.valueRegion != null)
			return StructuredDocumentRegionUtil.getAttrValue(ownerElement.getStructuredDocumentRegion(), this.valueRegion);
		return new String();
	}

	/**
	 */
	private String getValueSource(String value) {
		if (value == null)
			return null;
		if (value.length() == 0)
			return value;
		StringBuffer buffer = null;
		int offset = 0;
		int length = value.length();
		int amp = value.indexOf('&');
		while (amp >= 0) {
			if (buffer == null)
				buffer = new StringBuffer(length + 4);
			if (amp > offset)
				buffer.append(value.substring(offset, amp));
			buffer.append(IXMLCharEntity.AMP_REF);
			offset = amp + 1;
			amp = value.indexOf('&', offset);
		}
		if (buffer == null)
			return value;
		if (length > offset)
			buffer.append(value.substring(offset));
		return buffer.toString();
	}

	/**
	 * Check if Attr has JSP in value
	 */
	public boolean hasNestedValue() {
		if (this.valueRegion == null)
			return false;
		if (!(this.valueRegion instanceof ITextRegionContainer))
			return false;
		ITextRegionList regions = ((ITextRegionContainer) this.valueRegion).getRegions();
		if (regions == null)
			return false;
		Iterator e = regions.iterator();
		while (e.hasNext()) {
			ITextRegion region = (ITextRegion) e.next();
			if (region == null)
				continue;
			String regionType = region.getType();
			if (regionType == DOMRegionContext.XML_TAG_OPEN || isNestedLanguageOpening(regionType))
				return true;
		}
		return false;
	}

	/**
	 * Check if Attr has only name but not equal sign nor value
	 */
	public boolean hasNameOnly() {
		return (this.nameRegion != null && this.equalRegion == null && this.valueRegion == null);
	}

	/**
	 */
	protected final boolean hasPrefix() {
		if (this.name == null)
			return false;
		if (this.name.indexOf(':') <= 0)
			return false;
		// exclude JSP tag in name
		if (this.name.charAt(0) == '<')
			return false;
		return true;
	}

	/**
	 */
	protected final boolean ignoreCase() {
		if (this.ownerElement != null) {
			if (this.ownerElement.ignoreCase()) {
				return !hasPrefix();
			}
		}
		else {
			DocumentImpl document = (DocumentImpl) getOwnerDocument();
			if (document != null && document.ignoreCase()) {
				// even in case insensitive document, if having prefix, it's
				// case sensitive
				return !hasPrefix();
			}
		}
		return false;
	}

	/**
	 */
	public boolean isGlobalAttr() {
		if (hasPrefix())
			return false;
		if (this.ownerElement == null)
			return false;
		return this.ownerElement.isGlobalTag();
	}

	/**
	 */
	public final boolean isXMLAttr() {
		if (this.ownerElement != null) {
			if (!this.ownerElement.isXMLTag()) {
				return hasPrefix();
			}
		}
		else {
			DocumentImpl document = (DocumentImpl) getOwnerDocument();
			if (document != null && !document.isXMLType()) {
				// even in non-XML document, if having prefix, it's XML tag
				return hasPrefix();
			}
		}
		return true;
	}

	/**
	 * matchName method
	 * 
	 * @return boolean
	 * @param name
	 *            java.lang.String
	 */
	protected boolean matchName(String name) {
		if (name == null)
			return (this.name == null);
		if (this.name == null)
			return false;
		if (this.name.length() != name.length())
			return false;
		if (this.name.equals(name))
			return true;
		return this.name.equalsIgnoreCase(name) && ignoreCase();
	}


	/**
	 * notifyValueChanged method
	 */
	protected void notifyNameChanged() {
		if (this.ownerElement == null)
			return;
		DocumentImpl document = (DocumentImpl) this.ownerElement.getContainerDocument();
		if (document == null)
			return;
		DOMModelImpl model = (DOMModelImpl) document.getModel();
		if (model == null)
			return;
		model.nameChanged(this);
	}

	/**
	 * notifyValueChanged method
	 */
	protected void notifyValueChanged() {
		if (this.ownerElement == null)
			return;
		DocumentImpl document = (DocumentImpl) this.ownerElement.getContainerDocument();
		if (document == null)
			return;
		DOMModelImpl model = (DOMModelImpl) document.getModel();
		if (model == null)
			return;
		model.valueChanged(this);
	}

	/**
	 * removeRegions method
	 */
	void removeRegions() {
		this.nameRegion = null;
		this.valueRegion = null;
		this.equalRegion = null;
	}

	/**
	 */
	void resetRegions() {
		this.valueSource = getValueSource();
		removeRegions();
	}

	/**
	 */
	void resetRegions(ElementImpl ownerElement) {
		this.valueSource = getValueSource(ownerElement);
		removeRegions();
	}

	void setEqualRegion(ITextRegion equalRegion) {
		this.equalRegion = equalRegion;
	}

	/**
	 * setName method
	 * 
	 * @param name
	 *            java.lang.String
	 */
	protected void setName(String name) {
		String value = null;
		int startOffset = 0;
		if (this.ownerElement != null) {
			value = getValue();
			startOffset = this.ownerElement.getStartOffset();
			this.ownerElement.notify(CHANGE, this, value, null, startOffset);
		}
		this.name = name;
		if (this.ownerElement != null) {
			this.ownerElement.notify(CHANGE, this, null, value, startOffset);
		}
	}

	void setNameRegion(ITextRegion nameRegion) {
		this.nameRegion = nameRegion;
	}

	protected void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	/**
	 * setNodeValue method
	 * 
	 * @param nodeValue
	 *            java.lang.String
	 */
	public void setNodeValue(String nodeValue) throws DOMException {
		setValue(nodeValue);
	}

	/**
	 * setOwnerElement method
	 * 
	 * @param ownerElement
	 *            org.w3c.dom.Element
	 */
	protected void setOwnerElement(Element ownerElement) {
		this.ownerElement = (ElementImpl) ownerElement;
	}

	/**
	 */
	public void setPrefix(String prefix) throws DOMException {
		if (this.ownerElement != null && !this.ownerElement.isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}
		int prefixLength = (prefix != null ? prefix.length() : 0);
		String localName = getLocalName();
		if (prefixLength == 0) {
			setName(localName);
			return;
		}
		if (localName == null)
			localName = new String();
		int localLength = localName.length();
		StringBuffer buffer = new StringBuffer(prefixLength + 1 + localLength);
		buffer.append(prefix);
		buffer.append(':');
		buffer.append(localName);
		setName(buffer.toString());

		notifyNameChanged();
	}

	/**
	 * setValue method
	 * 
	 * @param value
	 *            java.lang.String
	 */
	public void setValue(String value) {
		// Remember: as we account for "floaters" in
		// future, remember that some are created
		// in the natural process of implementing
		// DOM apis.
		// this "self notification" of about/changed,
		// is added for this case, because it known to
		// be called from properties pages. Should be a
		// added to all DOM Modifiying APIs eventually.
		try {
			getModel().aboutToChangeModel();
			setValueSource(getValueSource(value));
		}
		finally {
			getModel().changedModel();
		}
	}

	void setValueRegion(ITextRegion valueRegion) {
		this.valueRegion = valueRegion;
		if (valueRegion != null)
			this.valueSource = null;
	}

	public void setValueSource(String source) {
		if (this.ownerElement != null && !this.ownerElement.isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}
		this.valueSource = source;

		notifyValueChanged();
	}

	/**
	 * Subclasses must override
	 * 
	 * @param regionType
	 * @return
	 */
	protected boolean isNestedLanguageOpening(String regionType) {
		boolean result = false;
		return result;
	}
}
