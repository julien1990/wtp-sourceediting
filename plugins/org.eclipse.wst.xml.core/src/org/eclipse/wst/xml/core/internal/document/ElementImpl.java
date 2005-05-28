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



import java.util.Iterator;

import org.eclipse.wst.sse.core.internal.ltk.parser.RegionParser;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.commentelement.CommentElementAdapter;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
import org.eclipse.wst.xml.core.internal.parser.XMLSourceParser;
import org.eclipse.wst.xml.core.internal.provisional.IXMLNamespace;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;


/**
 * ElementImpl class
 */
public class ElementImpl extends NodeContainer implements IDOMElement {

	private class Attributes implements NamedNodeMap {
		Attributes() {
			super();
		}

		public int getLength() {
			if (attrNodes == null)
				return 0;
			return attrNodes.getLength();
		}

		public Node getNamedItem(String name) {
			return getAttributeNode(name);
		}

		public Node getNamedItemNS(String uri, String name) {
			return getAttributeNodeNS(uri, name);
		}

		public Node item(int index) {
			if (attrNodes == null)
				return null;
			return attrNodes.item(index);
		}

		public Node removeNamedItem(String name) throws DOMException {
			return removeAttributeNode(name);
		}

		public Node removeNamedItemNS(String uri, String name) throws DOMException {
			return removeAttributeNodeNS(uri, name);
		}

		public Node setNamedItem(Node arg) throws DOMException {
			return setAttributeNode((AttrImpl) arg);
		}

		public Node setNamedItemNS(Node arg) throws DOMException {
			return setAttributeNodeNS((AttrImpl) arg);
		}
	}

	NodeListImpl attrNodes = null;
	private IStructuredDocumentRegion endStructuredDocumentRegion = null;
	private boolean isCommentTag = false;
	private boolean isEmptyTag = false;
	private boolean isJSPTag = false;
	private String namespaceURI = null;

	private String tagName = null;

	/**
	 * ElementImpl constructor
	 */
	protected ElementImpl() {
		super();
	}

	/**
	 * ElementImpl constructor
	 * 
	 * @param that
	 *            ElementImpl
	 */
	protected ElementImpl(ElementImpl that) {
		super(that);

		if (that != null) {
			this.tagName = that.tagName;
			this.isEmptyTag = that.isEmptyTag;
			this.isJSPTag = that.isJSPTag;
			this.isCommentTag = that.isCommentTag;

			// clone attributes
			that.cloneAttributes(this);
		}
	}

	/**
	 * addEndTag method
	 * 
	 * @param end
	 *            org.w3c.dom.Element
	 */
	protected void addEndTag(Element endTag) {
		if (endTag == null)
			return;
		if (hasEndTag())
			return;
		ElementImpl end = (ElementImpl) endTag;

		// move the end flat node from the end tag
		IStructuredDocumentRegion flatNode = end.getEndStructuredDocumentRegion();
		if (flatNode == null)
			return;
		end.setEndStructuredDocumentRegion(null);
		setEndStructuredDocumentRegion(flatNode);
	}

	/**
	 * appendAttibuteNode method
	 * 
	 * @return org.w3c.dom.Attr
	 * @param newAttr
	 *            org.w3c.dom.Attr
	 */
	public Attr appendAttributeNode(Attr newAttr) {
		if (newAttr == null)
			return null;
		AttrImpl attr = (AttrImpl) newAttr;
		if (attr.getOwnerElement() != null)
			return null;

		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		if (this.attrNodes == null)
			this.attrNodes = new NodeListImpl();
		this.attrNodes.appendNode(attr);
		attr.setOwnerElement(this);

		notifyAttrReplaced(attr, null);
		return attr;
	}

	/**
	 * cloneAttributes method
	 * 
	 * @param newOwner
	 *            org.w3c.dom.Element
	 */
	protected void cloneAttributes(Element newOwner) {
		if (newOwner == null || newOwner == this)
			return;

		ElementImpl element = (ElementImpl) newOwner;
		element.removeAttributes();

		if (this.attrNodes == null)
			return;

		int length = this.attrNodes.getLength();
		for (int i = 0; i < length; i++) {
			Node node = this.attrNodes.item(i);
			if (node == null)
				continue;
			Attr cloned = (Attr) node.cloneNode(false);
			if (cloned != null)
				element.appendAttributeNode(cloned);
		}
	}

	/**
	 * cloneNode method
	 * 
	 * @return org.w3c.dom.Node
	 * @param deep
	 *            boolean
	 */
	public Node cloneNode(boolean deep) {
		ElementImpl cloned = new ElementImpl(this);
		if (deep)
			cloneChildNodes(cloned, deep);
		return cloned;
	}

	/**
	 * getAttribute method
	 * 
	 * @return java.lang.String
	 * @param name
	 *            java.lang.String
	 */
	public String getAttribute(String name) {
		Attr attr = getAttributeNode(name);
		if (attr == null)
			return null;
		return attr.getValue();
	}

	/**
	 * getAttributeNode method
	 * 
	 * @return org.w3c.dom.Attr
	 * @param name
	 *            java.lang.String
	 */
	public Attr getAttributeNode(String name) {
		if (name == null)
			return null; // invalid parameter
		if (this.attrNodes == null)
			return null; // no attribute

		int length = this.attrNodes.getLength();
		for (int i = 0; i < length; i++) {
			AttrImpl attr = (AttrImpl) this.attrNodes.item(i);
			if (attr == null)
				continue;
			if (attr.matchName(name))
				return attr; // found
		}

		return null; // not found
	}

	/**
	 */
	public Attr getAttributeNodeNS(String uri, String name) {
		if (name == null)
			return null; // invalid parameter
		if (this.attrNodes == null)
			return null; // no attribute

		int length = this.attrNodes.getLength();
		for (int i = 0; i < length; i++) {
			AttrImpl attr = (AttrImpl) this.attrNodes.item(i);
			if (attr == null)
				continue;
			String localName = attr.getLocalName();
			if (localName == null || !localName.equals(name))
				continue;
			String nsURI = attr.getNamespaceURI();
			if (uri == null) {
				if (nsURI != null)
					continue;
			}
			else {
				if (nsURI == null || !nsURI.equals(uri))
					continue;
			}

			// found
			return attr;
		}

		return null; // not found
	}

	/**
	 */
	public String getAttributeNS(String uri, String name) {
		Attr attr = getAttributeNodeNS(uri, name);
		if (attr == null)
			return null;
		return attr.getValue();
	}

	/**
	 * getAttributes method
	 * 
	 * @return org.w3c.dom.NamedNodeMap
	 */
	public NamedNodeMap getAttributes() {
		return new Attributes();
	}

	/**
	 */
	protected CMElementDeclaration getDeclaration() {
		Document document = getOwnerDocument();
		if (document == null)
			return null;
		ModelQuery modelQuery = ModelQueryUtil.getModelQuery(document);
		if (modelQuery == null)
			return null;
		return modelQuery.getCMElementDeclaration(this);
	}

	/**
	 * getElementsByTagName method
	 * 
	 * @return org.w3c.dom.NodeList
	 * @param tagName
	 *            java.lang.String
	 */
	public NodeList getElementsByTagName(String tagName) {
		if (tagName == null)
			return new NodeListImpl();

		DocumentImpl document = (DocumentImpl) getOwnerDocument();
		if (document == null)
			return new NodeListImpl();
		NodeIterator it = document.createNodeIterator(this, NodeFilter.SHOW_ALL, null, false);
		if (it == null)
			return new NodeListImpl();
		NodeListImpl elements = new NodeListImpl();

		if (tagName.length() == 1 && tagName.charAt(0) == '*') {
			tagName = null; // do not care
		}

		for (Node node = it.nextNode(); node != null; node = it.nextNode()) {
			if (node.getNodeType() != ELEMENT_NODE)
				continue;
			if (tagName != null) {
				ElementImpl element = (ElementImpl) node;
				if (!element.matchTagName(tagName))
					continue;
			}
			elements.appendNode(node);
		}

		return elements;
	}

	/**
	 */
	public NodeList getElementsByTagNameNS(String uri, String tagName) {
		if (tagName == null)
			return new NodeListImpl();

		DocumentImpl document = (DocumentImpl) getOwnerDocument();
		if (document == null)
			return new NodeListImpl();
		NodeIterator it = document.createNodeIterator(this, NodeFilter.SHOW_ALL, null, false);
		if (it == null)
			return new NodeListImpl();
		NodeListImpl elements = new NodeListImpl();

		if (uri != null && uri.length() == 1 && uri.charAt(0) == '*') {
			uri = null; // do not care
		}
		if (tagName.length() == 1 && tagName.charAt(0) == '*') {
			tagName = null; // do not care
		}

		for (Node node = it.nextNode(); node != null; node = it.nextNode()) {
			if (node.getNodeType() != ELEMENT_NODE)
				continue;
			ElementImpl element = (ElementImpl) node;
			if (tagName != null) {
				String localName = element.getLocalName();
				if (localName == null || !localName.equals(tagName))
					continue;
			}
			if (uri != null) {
				String nsURI = element.getNamespaceURI();
				if (nsURI == null || !nsURI.equals(uri))
					continue;
			}
			elements.appendNode(element);
		}

		return elements;
	}

	/**
	 * getEndOffset method
	 * 
	 * @return int
	 */
	public int getEndOffset() {
		if (this.endStructuredDocumentRegion != null)
			return this.endStructuredDocumentRegion.getEnd();
		return super.getEndOffset();
	}

	/**
	 * getEndStartOffset method
	 * 
	 * @return int
	 */
	public int getEndStartOffset() {
		if (this.endStructuredDocumentRegion != null)
			return this.endStructuredDocumentRegion.getStart();
		return super.getEndOffset();
	}

	/**
	 * getEndStructuredDocumentRegion method
	 * 
	 */
	public IStructuredDocumentRegion getEndStructuredDocumentRegion() {
		return this.endStructuredDocumentRegion;
	}

	public String getEndTagName() {
		if (this.endStructuredDocumentRegion == null)
			return null;

		ITextRegionList regions = this.endStructuredDocumentRegion.getRegions();
		if (regions == null)
			return null;
		Iterator e = regions.iterator();
		while (e.hasNext()) {
			ITextRegion region = (ITextRegion) e.next();
			String regionType = region.getType();
			if (regionType == DOMRegionContext.XML_TAG_NAME || isNestedEndTag(regionType)) {
				return this.endStructuredDocumentRegion.getText(region);
			}
		}

		return null;
	}

	protected boolean isNestedEndTag(String regionType) {
		boolean result = false;
		return result;
	}

	/**
	 * getFirstStructuredDocumentRegion method
	 * 
	 */
	public IStructuredDocumentRegion getFirstStructuredDocumentRegion() {
		IStructuredDocumentRegion flatNode = getStructuredDocumentRegion();
		if (flatNode != null)
			return StructuredDocumentRegionUtil.getStructuredDocumentRegion(flatNode);
		return StructuredDocumentRegionUtil.getStructuredDocumentRegion(this.endStructuredDocumentRegion);
	}

	/**
	 * getLastStructuredDocumentRegion method
	 * 
	 */
	public IStructuredDocumentRegion getLastStructuredDocumentRegion() {
		if (this.endStructuredDocumentRegion != null)
			return StructuredDocumentRegionUtil.getStructuredDocumentRegion(this.endStructuredDocumentRegion);
		return StructuredDocumentRegionUtil.getStructuredDocumentRegion(getStructuredDocumentRegion());
	}

	/**
	 */
	public String getLocalName() {
		if (this.tagName == null)
			return null;
		int index = this.tagName.indexOf(':');
		if (index < 0)
			return this.tagName;
		return this.tagName.substring(index + 1);
	}

	/**
	 */
	public String getNamespaceURI() {
		String nsAttrName = null;
		String prefix = getPrefix();
		if (prefix != null && prefix.length() > 0) {
			nsAttrName = IXMLNamespace.XMLNS_PREFIX + prefix;
		}
		else {
			nsAttrName = IXMLNamespace.XMLNS;
		}

		for (Node node = this; node != null; node = node.getParentNode()) {
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
		return getTagName();
	}

	/**
	 * getNodeType method
	 * 
	 * @return short
	 */
	public short getNodeType() {
		return ELEMENT_NODE;
	}

	/**
	 */
	public String getPrefix() {
		if (this.tagName == null)
			return null;
		int index = this.tagName.indexOf(':');
		if (index <= 0)
			return null;
		// exclude JSP tag in tag name
		if (this.tagName.charAt(0) == '<')
			return null;
		return this.tagName.substring(0, index);
	}

	/**
	 * getStartEndOffset method
	 * 
	 * @return int
	 */
	public int getStartEndOffset() {
		IStructuredDocumentRegion flatNode = getStructuredDocumentRegion();
		if (flatNode != null)
			return flatNode.getEnd();
		return super.getStartOffset();
	}

	/**
	 * getStartOffset method
	 * 
	 * @return int
	 */
	public int getStartOffset() {
		if (getStartStructuredDocumentRegion() == null && this.endStructuredDocumentRegion != null && !hasChildNodes()) {
			return this.endStructuredDocumentRegion.getStart();
		}
		return super.getStartOffset();
	}

	/**
	 * getStartStructuredDocumentRegion method
	 * 
	 */
	public IStructuredDocumentRegion getStartStructuredDocumentRegion() {
		return getStructuredDocumentRegion();
	}

	/**
	 * getTagName method
	 * 
	 * @return java.lang.String
	 */
	public String getTagName() {
		if (this.tagName == null)
			return new String();
		return this.tagName;
	}

	/**
	 */
	public boolean hasAttribute(String name) {
		return (getAttributeNode(name) != null);
	}

	/**
	 */
	public boolean hasAttributeNS(String uri, String name) {
		return (getAttributeNodeNS(uri, name) != null);
	}

	/**
	 */
	public boolean hasAttributes() {
		return (this.attrNodes != null && this.attrNodes.getLength() > 0);
	}

	/**
	 * hasEndTag method
	 * 
	 * @return boolean
	 */
	public boolean hasEndTag() {
		return (this.endStructuredDocumentRegion != null);
	}

	/**
	 */
	protected final boolean hasPrefix() {
		if (this.tagName == null)
			return false;
		if (this.tagName.indexOf(':') <= 0)
			return false;
		// exclude JSP tag in tag name
		if (this.tagName.charAt(0) == '<')
			return false;
		return true;
	}

	/**
	 * hasStartTag method
	 * 
	 * @return boolean
	 */
	public boolean hasStartTag() {
		return (getStructuredDocumentRegion() != null);
	}

	/**
	 */
	protected final boolean ignoreCase() {
		DocumentImpl document = (DocumentImpl) getOwnerDocument();
		if (document != null && document.ignoreCase()) {
			// even in case insensitive document, if having prefix, it's case
			// sensitive tag
			return !hasPrefix();
		}
		return false;
	}

	/**
	 */
	protected Attr insertAttributeNode(Attr newAttr, int index) {
		if (newAttr == null)
			return null;
		AttrImpl attr = (AttrImpl) newAttr;
		if (attr.getOwnerElement() != null)
			return null;

		if (this.attrNodes == null)
			this.attrNodes = new NodeListImpl();
		this.attrNodes.insertNode(attr, index);
		attr.setOwnerElement(this);

		notifyAttrReplaced(attr, null);
		return attr;
	}

	/**
	 * insertBefore method
	 * 
	 * @return org.w3c.dom.Node
	 * @param newChild
	 *            org.w3c.dom.Node
	 * @param refChild
	 *            org.w3c.dom.Node
	 */
	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
		// should throw DOMException instead of return null?
		if (newChild == null)
			return null;
		if (!isContainer()) { // never be container
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, new String());
		}
		if (newChild.getNodeType() != TEXT_NODE) {
			if (isJSPContainer() || isCDATAContainer()) { // accepts only
				// Text
				// child
				throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, new String());
			}
		}
		return super.insertBefore(newChild, refChild);
	}

	/**
	 */
	protected boolean isCDATAContainer() {
		// use BlockMaker instead of CMElementDeclaration
		// because <style> and <script> in XHTML is not CDATA content type
		IDOMModel model = getModel();
		if (model == null)
			return false; // error
		IStructuredDocument structuredDocument = model.getStructuredDocument();
		if (structuredDocument == null)
			return false; // eror
		RegionParser parser = structuredDocument.getParser();
		if (parser == null || !(parser instanceof XMLSourceParser))
			return false;
		return (((XMLSourceParser) parser).getBlockMarker(this.tagName) != null);
		/*
		 * CMElementDeclaration decl = getDeclaration(); if (decl == null)
		 * return false; if (decl instanceof CMNodeWrapper) { decl =
		 * (CMElementDeclaration)((CMNodeWrapper)decl).getOriginNode(); if
		 * (decl == null) return false; } if (decl instanceof
		 * TLDElementDeclaration) { String content =
		 * ((TLDElementDeclaration)decl).getBodycontent(); if (content ==
		 * null) return false; return
		 * content.equals(JSP11TLDNames.CONTENT_TAGDEPENDENT); } if
		 * (!isGlobalTag()) return false; return (decl.getContentType() ==
		 * CMElementDeclaration.CDATA);
		 */
	}

	/**
	 */
	public boolean isClosed() {
		IStructuredDocumentRegion flatNode = null;
		if (isEmptyTag() || !isContainer()) {
			flatNode = getStructuredDocumentRegion();
			if (flatNode == null)
				return true; // will be generated
		}
		else {
			flatNode = getEndStructuredDocumentRegion();
			if (flatNode == null)
				return false; // must be generated
		}
		String regionType = StructuredDocumentRegionUtil.getLastRegionType(flatNode);
		if (isCommentTag()) {
			return (isNestedClosedComment(regionType) || regionType == DOMRegionContext.XML_COMMENT_CLOSE);
		}
		if (isJSPTag()) {
			return isNestedClosed(regionType);
		}
		return (regionType == DOMRegionContext.XML_TAG_CLOSE || regionType == DOMRegionContext.XML_EMPTY_TAG_CLOSE || regionType == DOMRegionContext.XML_DECLARATION_CLOSE);
	}

	protected boolean isNestedClosed(String regionType) {
		boolean result = false;
		return result;
	}

	protected boolean isNestedClosedComment(String regionType) {
		boolean result = false;
		return result;
	}

	/**
	 */
	public final boolean isCommentTag() {
		return this.isCommentTag;
	}

	/**
	 * isContainer method
	 * 
	 * @return boolean
	 */
	public boolean isContainer() {
		if (isCommentTag()) {
			CommentElementAdapter adapter = (CommentElementAdapter) getAdapterFor(CommentElementAdapter.class);
			if (adapter != null) {
				return (adapter.isContainer());
			}
			return (getDeclaration() == null);
		}
		if (isJSPTag()) {
			// exclude JSP directive
			return (matchTagName(JSPTag.JSP_SCRIPTLET) || matchTagName(JSPTag.JSP_DECLARATION) || matchTagName(JSPTag.JSP_EXPRESSION));
		}
		if (!isXMLTag()) { // non-XML tag
			CMElementDeclaration decl = getDeclaration();
			if (decl == null)
				return false; // undefined tag
			return (decl.getContentType() != CMElementDeclaration.EMPTY);
		}
		return true;
	}

	/**
	 * isEmptyTag method
	 * 
	 * @return boolean
	 */
	public boolean isEmptyTag() {
		if (isJSPTag())
			return false;
		if (isCommentTag())
			return false;
		if (!isXMLTag())
			return false;
		return this.isEmptyTag;
	}

	/**
	 */
	public boolean isEndTag() {
		return (hasEndTag() && !hasStartTag() && !hasChildNodes());
	}

	/**
	 */
	public boolean isGlobalTag() {
		return !hasPrefix();
	}

	/**
	 */
	public boolean isImplicitTag() {
		if (hasStartTag() || hasEndTag())
			return false;
		// make sure this is in the document tree
		// because if not in the document tree, no tags are generated yet
		return (getContainerDocument() != null);
	}

	/**
	 */
	public boolean isJSPContainer() {
		return (isJSPTag() && !isCommentTag() && isContainer());
	}

	/**
	 * isJSPTag method
	 * 
	 * @return boolean
	 */
	public final boolean isJSPTag() {
		return this.isJSPTag;
	}

	/**
	 */
	public boolean isStartTagClosed() {
		IStructuredDocumentRegion flatNode = getStructuredDocumentRegion();
		if (flatNode == null)
			return true; // will be generated
		String regionType = StructuredDocumentRegionUtil.getLastRegionType(flatNode);
		if (isCommentTag()) {
			return (isNestedClosedComment(regionType) || regionType == DOMRegionContext.XML_COMMENT_CLOSE);
		}
		if (isJSPTag()) {
			if (isContainer())
				return true; // start tag always has a single region
			return isClosedNestedDirective(regionType);
		}
		return (regionType == DOMRegionContext.XML_TAG_CLOSE || regionType == DOMRegionContext.XML_EMPTY_TAG_CLOSE || regionType == DOMRegionContext.XML_DECLARATION_CLOSE);
	}

	protected boolean isClosedNestedDirective(String regionType) {
		boolean result = false;
		return result;
	}

	/**
	 */
	public final boolean isXMLTag() {
		if (isJSPTag())
			return false;
		if (isCommentTag())
			return false;
		DocumentImpl document = (DocumentImpl) getOwnerDocument();
		if (document != null && !document.isXMLType()) {
			// even in non-XML document, if having prefix, it's XML tag
			return hasPrefix();
		}
		return true;
	}

	/**
	 */
	protected boolean matchEndTag(Element element) {
		if (element == null)
			return false;
		ElementImpl impl = (ElementImpl) element;
		if (isJSPTag() && !isCommentTag()) {
			return (impl.isJSPTag() && !impl.isCommentTag());
		}
		return matchTagName(element.getTagName());
	}

	/**
	 * matchTagName method
	 * 
	 * @return boolean
	 * @param tagName
	 *            java.lang.String
	 */
	public boolean matchTagName(String tagName) {
		if (tagName == null)
			return (this.tagName == null);
		if (this.tagName == null)
			return false;
		if (!ignoreCase())
			return this.tagName.equals(tagName);
		return this.tagName.equalsIgnoreCase(tagName);
	}

	/**
	 * notifyAttrReplaced method
	 * 
	 * @param newAttr
	 *            org.w3c.dom.Attr
	 * @param oldAttr
	 *            org.w3c.dom.Attr
	 */
	protected void notifyAttrReplaced(Attr newAttr, Attr oldAttr) {
		DocumentImpl document = (DocumentImpl) getContainerDocument();
		if (document == null)
			return;
		DOMModelImpl model = (DOMModelImpl) document.getModel();
		if (model == null)
			return;
		model.attrReplaced(this, newAttr, oldAttr);
	}

	/**
	 * notifyValueChanged method
	 */
	public void notifyEndTagChanged() {
		DocumentImpl document = (DocumentImpl) getContainerDocument();
		if (document == null)
			return;
		DOMModelImpl model = (DOMModelImpl) document.getModel();
		if (model == null)
			return;
		model.endTagChanged(this);
	}

	/**
	 */
	public void notifyStartTagChanged() {
		DocumentImpl document = (DocumentImpl) getContainerDocument();
		if (document == null)
			return;
		DOMModelImpl model = (DOMModelImpl) document.getModel();
		if (model == null)
			return;
		model.startTagChanged(this);
	}

	/**
	 */
	public boolean preferEmptyTag() {
		if (hasChildNodes())
			return false;
		if (isJSPTag())
			return false;
		if (isCommentTag())
			return false;
		if (!isXMLTag())
			return false;
		CMElementDeclaration decl = getDeclaration();
		if (decl == null)
			return false;
		return (decl.getContentType() == CMElementDeclaration.EMPTY);
	}

	/**
	 * removeAttribute method
	 * 
	 * @param name
	 *            java.lang.String
	 */
	public void removeAttribute(String name) throws DOMException {
		removeAttributeNode(name);
	}

	/**
	 * removeAttributeNode method
	 * 
	 * @return org.w3c.dom.Attr
	 * @param oldAttr
	 *            org.w3c.dom.Attr
	 */
	public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
		if (oldAttr == null)
			return null; // invalid parameter

		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		if (this.attrNodes == null) { // no attribute
			throw new DOMException(DOMException.NOT_FOUND_ERR, new String());
		}

		int length = this.attrNodes.getLength();
		for (int i = 0; i < length; i++) {
			AttrImpl attr = (AttrImpl) this.attrNodes.item(i);
			if (attr != oldAttr)
				continue;

			// found
			this.attrNodes.removeNode(i);
			attr.setOwnerElement(null);

			notifyAttrReplaced(null, attr);
			return attr;
		}

		// not found
		throw new DOMException(DOMException.NOT_FOUND_ERR, new String());
	}

	/**
	 * removeAttributeNode method
	 * 
	 * @return org.w3c.dom.Attr
	 * @param name
	 *            java.lang.String
	 */
	public Attr removeAttributeNode(String name) {
		if (name == null)
			return null; // invalid parameter
		if (this.attrNodes == null)
			return null; // no attribute

		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		int length = this.attrNodes.getLength();
		for (int i = 0; i < length; i++) {
			AttrImpl attr = (AttrImpl) this.attrNodes.item(i);
			if (attr == null)
				continue;
			if (!attr.matchName(name))
				continue;

			// found
			this.attrNodes.removeNode(i);
			attr.setOwnerElement(null);

			notifyAttrReplaced(null, attr);
			return attr;
		}

		return null; // not found
	}

	/**
	 */
	public Attr removeAttributeNodeNS(String uri, String name) {
		if (name == null)
			return null; // invalid parameter
		if (this.attrNodes == null)
			return null; // no attribute

		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		int length = this.attrNodes.getLength();
		for (int i = 0; i < length; i++) {
			AttrImpl attr = (AttrImpl) this.attrNodes.item(i);
			if (attr == null)
				continue;
			String localName = attr.getLocalName();
			if (localName == null || !localName.equals(name))
				continue;
			String nsURI = attr.getNamespaceURI();
			if (uri == null) {
				if (nsURI != null)
					continue;
			}
			else {
				if (nsURI == null || !nsURI.equals(uri))
					continue;
			}

			// found
			this.attrNodes.removeNode(i);
			attr.setOwnerElement(null);

			notifyAttrReplaced(null, attr);
			return attr;
		}

		return null; // not found
	}

	/**
	 */
	public void removeAttributeNS(String uri, String name) throws DOMException {
		removeAttributeNodeNS(uri, name);
	}

	/**
	 * removeAttributes method
	 */
	public void removeAttributes() {
		if (this.attrNodes == null)
			return;

		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		int length = this.attrNodes.getLength();
		for (int i = 0; i < length; i++) {
			AttrImpl attr = (AttrImpl) this.attrNodes.item(i);
			if (attr != null) {
				attr.setOwnerElement(null);
				notifyAttrReplaced(null, attr);
			}
		}

		this.attrNodes = null;
	}

	/**
	 * removeEndTag method
	 * 
	 * @return org.w3c.dom.Element
	 */
	protected Element removeEndTag() {
		if (!hasEndTag())
			return null;
		NodeListImpl attrNodes = this.attrNodes;
		this.attrNodes = null; // not to copy attributes
		ElementImpl end = (ElementImpl) cloneNode(false);
		this.attrNodes = attrNodes;
		if (end == null)
			return null;

		// move the end flat node to the end tag
		IStructuredDocumentRegion flatNode = getEndStructuredDocumentRegion();
		if (flatNode == null)
			return null;
		setEndStructuredDocumentRegion(null);
		end.setEndStructuredDocumentRegion(flatNode);
		return end;
	}

	/**
	 */
	protected void removeStartTag() {
		removeAttributes();
	}

	/**
	 * Resets attribute values from IStructuredDocumentRegion.
	 */
	void resetStructuredDocumentRegions() {
		if (this.attrNodes != null) {
			int length = this.attrNodes.getLength();
			for (int i = 0; i < length; i++) {
				AttrImpl attr = (AttrImpl) this.attrNodes.item(i);
				if (attr == null)
					continue;
				attr.resetRegions();
			}
		}

		super.resetStructuredDocumentRegions(); // for children

		this.endStructuredDocumentRegion = null;
	}

	/**
	 * setAttribute method
	 * 
	 * @param name
	 *            java.lang.String
	 * @param value
	 *            java.lang.String
	 */
	public void setAttribute(String name, String value) throws DOMException {
		if (name == null)
			return;

		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		Attr attr = getAttributeNode(name);
		if (attr != null) {
			attr.setValue(value); // change value
			return;
		}

		// new attribute
		Document doc = getOwnerDocument();
		if (doc == null)
			return;
		attr = doc.createAttribute(name);
		if (attr == null)
			return;
		attr.setValue(value);
		appendAttributeNode(attr);
	}

	/**
	 * setAttributeNode method
	 * 
	 * @return org.w3c.dom.Attr
	 * @param newAttr
	 *            org.w3c.dom.Attr
	 */
	public Attr setAttributeNode(Attr newAttr) throws DOMException {
		if (newAttr == null)
			return null; // nothing to do

		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		AttrImpl attr = (AttrImpl) newAttr;
		Element owner = attr.getOwnerElement();
		if (owner != null) {
			if (owner == this)
				return null; // nothing to do
			throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, new String());
		}

		Attr oldAttr = removeAttributeNode(newAttr.getName());
		appendAttributeNode(attr);
		return oldAttr;
	}

	/**
	 */
	public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
		if (newAttr == null)
			return null; // nothing to do

		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		AttrImpl attr = (AttrImpl) newAttr;
		Element owner = attr.getOwnerElement();
		if (owner != null) {
			if (owner == this)
				return null; // nothing to do
			throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, new String());
		}

		String name = newAttr.getLocalName();
		String uri = newAttr.getNamespaceURI();
		Attr oldAttr = removeAttributeNodeNS(uri, name);
		appendAttributeNode(attr);
		return oldAttr;
	}

	/**
	 */
	public void setAttributeNS(String uri, String name, String value) throws DOMException {
		if (name == null)
			return;

		if (!isDataEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		Attr attr = getAttributeNodeNS(uri, name);
		if (attr != null) {
			attr.setValue(value); // change value
			return;
		}

		// new attribute
		Document doc = getOwnerDocument();
		if (doc == null)
			return;
		attr = doc.createAttributeNS(uri, name);
		if (attr == null)
			return;
		attr.setValue(value);
		appendAttributeNode(attr);
	}

	/**
	 */
	public void setCommentTag(boolean isCommentTag) {
		IDOMNode parent = (IDOMNode) getParentNode();
		if (parent != null && !parent.isChildEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		this.isCommentTag = isCommentTag;
	}

	/**
	 * setEmptyTag method
	 * 
	 * @param isEmptyTag
	 *            boolean
	 */
	public void setEmptyTag(boolean isEmptyTag) {
		IDOMNode parent = (IDOMNode) getParentNode();
		if (parent != null && !parent.isChildEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		this.isEmptyTag = isEmptyTag;
	}

	/**
	 * setEndStructuredDocumentRegion method
	 * 
	 * @param flatNode
	 */
	void setEndStructuredDocumentRegion(IStructuredDocumentRegion flatNode) {
		this.endStructuredDocumentRegion = flatNode;

		NodeContainer parent = (NodeContainer) getParentNode();
		if (parent != null) {
			parent.syncChildEditableState(this);
		}
	}

	/**
	 * setJSPTag method
	 * 
	 * @param isJSPTag
	 *            boolean
	 */
	public void setJSPTag(boolean isJSPTag) {
		IDOMNode parent = (IDOMNode) getParentNode();
		if (parent != null && !parent.isChildEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		this.isJSPTag = isJSPTag;
	}

	/**
	 */
	protected void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	/**
	 */
	protected void setOwnerDocument(Document ownerDocument, boolean deep) {
		super.setOwnerDocument(ownerDocument, deep);

		if (this.attrNodes == null)
			return;

		int length = this.attrNodes.getLength();
		for (int i = 0; i < length; i++) {
			AttrImpl attr = (AttrImpl) this.attrNodes.item(i);
			if (attr == null)
				continue;
			attr.setOwnerDocument(ownerDocument);
		}
	}

	/**
	 */
	public void setPrefix(String prefix) throws DOMException {
		IDOMNode parent = (IDOMNode) getParentNode();
		if (parent != null && !parent.isChildEditable()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, new String());
		}

		int prefixLength = (prefix != null ? prefix.length() : 0);
		String localName = getLocalName();
		if (prefixLength == 0) {
			if (localName == null || localName.length() == 0) {
				// invalid local name
				return;
			}
			setTagName(localName);
		}
		else {
			int localLength = (localName != null ? localName.length() : 0);
			StringBuffer buffer = new StringBuffer(prefixLength + 1 + localLength);
			buffer.append(prefix);
			buffer.append(':');
			if (localName != null)
				buffer.append(localName);
			setTagName(buffer.toString());
		}

		boolean changeEndTag = hasEndTag();
		notifyStartTagChanged();
		if (changeEndTag)
			notifyEndTagChanged();
	}

	/**
	 * setStartStructuredDocumentRegion method
	 * 
	 * @param flatNode
	 */
	void setStartStructuredDocumentRegion(IStructuredDocumentRegion flatNode) {
		setStructuredDocumentRegion(flatNode);
	}

	/**
	 * setTagName method
	 * 
	 * @param tagName
	 *            java.lang.String
	 */
	protected void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * toString method
	 * 
	 * @return java.lang.String
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		String tagName = getTagName();
		if (hasStartTag())
			buffer.append(tagName);
		if (isEmptyTag())
			buffer.append('/');
		if (hasEndTag()) {
			buffer.append('/');
			buffer.append(tagName);
		}
		if (buffer.length() == 0)
			buffer.append(tagName);

		IStructuredDocumentRegion startStructuredDocumentRegion = getStartStructuredDocumentRegion();
		if (startStructuredDocumentRegion != null) {
			buffer.append('@');
			buffer.append(startStructuredDocumentRegion.toString());
		}
		IStructuredDocumentRegion endStructuredDocumentRegion = getEndStructuredDocumentRegion();
		if (endStructuredDocumentRegion != null) {
			buffer.append('@');
			buffer.append(endStructuredDocumentRegion.toString());
		}
		return buffer.toString();
	}

	/**
	 * NOT IMPLEMENTED. Is defined here in preparation for DOM 3.
	 */
	public void setIdAttribute(String name, boolean isId) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not supported in this version"); //$NON-NLS-1$
	}

	/**
	 * NOT IMPLEMENTED. Is defined here in preparation for DOM 3.
	 */
	public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not supported in this version"); //$NON-NLS-1$
	}

	/**
	 * NOT IMPLEMENTED. Is defined here in preparation for DOM 3.
	 */
	public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not supported in this version"); //$NON-NLS-1$
	}
}
