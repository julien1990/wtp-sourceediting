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
package org.eclipse.wst.xml.ui.taginfo;



import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.wst.sse.core.IndexedRegion;
import org.eclipse.wst.sse.core.text.IStructuredDocument;
import org.eclipse.wst.sse.core.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.text.ITextRegion;
import org.eclipse.wst.sse.core.text.ITextRegionList;
import org.eclipse.wst.sse.core.util.Debug;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.xml.core.document.DOMNode;
import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.contentmodel.util.DOMNamespaceHelper;
import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
import org.eclipse.wst.xml.core.parser.XMLRegionContext;
import org.eclipse.wst.xml.ui.internal.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Provides hover help documentation for xml tags
 * 
 * @author amywu
 * @see org.eclipse.jface.text.ITextHover
 */
public class XMLTagInfoHoverProcessor implements ITextHover {
	protected MarkupTagInfoProvider fInfoProvider = null;

	/**
	 * Constructor for XMLTextHoverProcessor.
	 */
	public XMLTagInfoHoverProcessor() {
	}

	/**
	 * Retreives documentation to display in the hover help popup.
	 * 
	 * @return String any documentation information to display
	 *         <code>null</code> if there is nothing to display.
	 *  
	 */
	protected String computeHoverHelp(ITextViewer textViewer, int documentPosition) {
		String result = null;

		IndexedRegion treeNode = ContentAssistUtils.getNodeAt((StructuredTextViewer) textViewer, documentPosition);
		if (treeNode == null)
			return null;
		Node node = (Node) treeNode;

		while (node != null && node.getNodeType() == Node.TEXT_NODE && node.getParentNode() != null)
			node = node.getParentNode();
		DOMNode parentNode = (DOMNode) node;

		IStructuredDocumentRegion flatNode = ((IStructuredDocument) textViewer.getDocument()).getRegionAtCharacterOffset(documentPosition);
		if (flatNode != null) {
			ITextRegion region = flatNode.getRegionAtCharacterOffset(documentPosition);
			if (region != null) {
				result = computeRegionHelp(treeNode, parentNode, flatNode, region);
			}
		}

		return result;
	}

	/**
	 * Computes the hoverhelp based on region
	 * 
	 * @return String hoverhelp
	 */
	protected String computeRegionHelp(IndexedRegion treeNode, DOMNode parentNode, IStructuredDocumentRegion flatNode, ITextRegion region) {
		String result = null;
		if (region == null)
			return null;
		String regionType = region.getType();
		if (regionType == XMLRegionContext.XML_TAG_NAME)
			result = computeTagNameHelp((DOMNode) treeNode, parentNode, flatNode, region);
		else if (regionType == XMLRegionContext.XML_TAG_ATTRIBUTE_NAME)
			result = computeTagAttNameHelp((DOMNode) treeNode, parentNode, flatNode, region);
		else if (regionType == XMLRegionContext.XML_TAG_ATTRIBUTE_VALUE)
			result = computeTagAttValueHelp((DOMNode) treeNode, parentNode, flatNode, region);
		return result;
	}

	/**
	 * Computes the hover help for the attribute name
	 */
	protected String computeTagAttNameHelp(DOMNode xmlnode, DOMNode parentNode, IStructuredDocumentRegion flatNode, ITextRegion region) {
		CMElementDeclaration elementDecl = getCMElementDeclaration(xmlnode);
		String attName = flatNode.getText(region);
		CMAttributeDeclaration attDecl = getCMAttributeDeclaration(elementDecl, attName);
		return getAdditionalInfo(elementDecl, attDecl);
	}

	/**
	 * Computes the hover help for the attribute value (this is the same as
	 * the attribute name's help)
	 */
	protected String computeTagAttValueHelp(DOMNode xmlnode, DOMNode parentNode, IStructuredDocumentRegion flatNode, ITextRegion region) {
		CMElementDeclaration elementDecl = getCMElementDeclaration(xmlnode);
		ITextRegion attrNameRegion = getAttrNameRegion(xmlnode, region);

		String attName = flatNode.getText(attrNameRegion);
		CMAttributeDeclaration attDecl = getCMAttributeDeclaration(elementDecl, attName);
		return getAdditionalInfo(elementDecl, attDecl);
	}

	/**
	 * Computes the hover help for the tag name
	 */
	protected String computeTagNameHelp(DOMNode xmlnode, DOMNode parentNode, IStructuredDocumentRegion flatNode, ITextRegion region) {
		CMElementDeclaration elementDecl = getCMElementDeclaration(xmlnode);
		CMElementDeclaration pelementDecl = getCMElementDeclaration(parentNode);
		return getAdditionalInfo(pelementDecl, elementDecl);
	}

	/**
	 * Retreives cmnode's documentation to display in the hover help popup. If
	 * no documentation exists for cmnode, try displaying parentOrOwner's
	 * documentation
	 * 
	 * @return String any documentation information to display for cmnode.
	 *         <code>null</code> if there is nothing to display.
	 */
	protected String getAdditionalInfo(CMNode parentOrOwner, CMNode cmnode) {
		String addlInfo = null;

		if (cmnode == null) {
			if (Debug.displayWarnings) {
				new IllegalArgumentException("Null declaration!").printStackTrace(); //$NON-NLS-1$
			}
			return null;
		}

		addlInfo = getInfoProvider().getInfo(cmnode);
		if (addlInfo == null && parentOrOwner != null)
			addlInfo = getInfoProvider().getInfo(parentOrOwner);
		return addlInfo;
	}

	/**
	 * Find the region of the attribute name for the given attribute value
	 * region
	 * 
	 */
	protected ITextRegion getAttrNameRegion(DOMNode node, ITextRegion region) {
		// Find the attribute name for which this position should have a value
		IStructuredDocumentRegion open = node.getFirstStructuredDocumentRegion();
		ITextRegionList openRegions = open.getRegions();
		int i = openRegions.indexOf(region);
		if (i < 0)
			return null;
		ITextRegion nameRegion = null;
		while (i >= 0) {
			nameRegion = openRegions.get(i--);
			if (nameRegion.getType() == XMLRegionContext.XML_TAG_ATTRIBUTE_NAME)
				break;
		}
		return nameRegion;
	}

	/**
	 * Retreives CMAttributeDeclaration indicated by attribute name within
	 * elementDecl
	 */
	protected CMAttributeDeclaration getCMAttributeDeclaration(CMElementDeclaration elementDecl, String attName) {
		CMAttributeDeclaration attrDecl = null;

		if (elementDecl != null) {
			CMNamedNodeMap attributes = elementDecl.getAttributes();
			String noprefixName = DOMNamespaceHelper.getUnprefixedName(attName);
			if (attributes != null) {
				attrDecl = (CMAttributeDeclaration) attributes.getNamedItem(noprefixName);
				if (attrDecl == null) {
					attrDecl = (CMAttributeDeclaration) attributes.getNamedItem(attName);
				}
			}
		}
		return attrDecl;
	}

	/**
	 * Retreives CMElementDeclaration for given node
	 * 
	 * @return CMElementDeclaration - CMElementDeclaration of node or
	 *         <code>null</code> if not possible
	 */
	protected CMElementDeclaration getCMElementDeclaration(Node node) {
		CMElementDeclaration result = null;
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			ModelQuery modelQuery = ModelQueryUtil.getModelQuery(node.getOwnerDocument());
			if (modelQuery != null)
				result = modelQuery.getCMElementDeclaration((Element) node);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer,
	 *      org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer viewer, IRegion hoverRegion) {
		if ((hoverRegion == null) || (viewer == null) || (viewer.getDocument() == null))
			return null;

		String displayText = null;
		int documentOffset = hoverRegion.getOffset();
		displayText = computeHoverHelp(viewer, documentOffset);

		return displayText;
	}

	/**
	 * Returns the region to hover the text over based on the offset.
	 * 
	 * @param textViewer
	 * @param offset
	 * 
	 * @return IRegion region to hover over if offset is within tag name,
	 *         attribute name, or attribute value and if offset is not over
	 *         invalid whitespace. otherwise, returns <code>null</code>
	 * 
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		if ((textViewer == null) || (textViewer.getDocument() == null))
			return null;

		IStructuredDocumentRegion flatNode = ((IStructuredDocument) textViewer.getDocument()).getRegionAtCharacterOffset(offset);
		ITextRegion region = null;

		if (flatNode != null) {
			region = flatNode.getRegionAtCharacterOffset(offset);
		}

		if (region != null) {
			// only supply hoverhelp for tag name, attribute name, or
			// attribute value
			String regionType = region.getType();
			if ((regionType == XMLRegionContext.XML_TAG_NAME) || (regionType == XMLRegionContext.XML_TAG_ATTRIBUTE_NAME) || (regionType == XMLRegionContext.XML_TAG_ATTRIBUTE_VALUE)) {
				try {
					// check if we are at whitespace before or after line
					IRegion line = textViewer.getDocument().getLineInformationOfOffset(offset);
					if ((offset > (line.getOffset())) && (offset < (line.getOffset() + line.getLength()))) {
						// check if we are in region's trailing whitespace
						// (whitespace after relevant info)
						if (offset < flatNode.getTextEndOffset(region)) {
							return new Region(flatNode.getStartOffset(region), region.getTextLength());
						}
					}
				} catch (BadLocationException e) {
					Logger.logException(e);
				}
			}
		}
		return null;
	}

	/**
	 * @deprecated if enabled flag is false, dont call getHoverRegion in the
	 *             first place if true, use getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset, boolean enabled) {
		if ((!enabled) || (textViewer == null) || (textViewer.getDocument() == null))
			return null;

		IStructuredDocumentRegion flatNode = ((IStructuredDocument) textViewer.getDocument()).getRegionAtCharacterOffset(offset);
		ITextRegion region = null;

		if (flatNode != null) {
			region = flatNode.getRegionAtCharacterOffset(offset);
		}

		if (region != null) {
			// only supply hoverhelp for tag name, attribute name, or
			// attribute value
			String regionType = region.getType();
			if ((regionType == XMLRegionContext.XML_TAG_NAME) || (regionType == XMLRegionContext.XML_TAG_ATTRIBUTE_NAME) || (regionType == XMLRegionContext.XML_TAG_ATTRIBUTE_VALUE)) {
				try {
					// check if we are at whitespace before or after line
					IRegion line = textViewer.getDocument().getLineInformationOfOffset(offset);
					if ((offset > (line.getOffset())) && (offset < (line.getOffset() + line.getLength()))) {
						// check if we are in region's trailing whitespace
						// (whitespace after relevant info)
						if (offset < flatNode.getTextEndOffset(region)) {
							return new Region(flatNode.getStartOffset(region), region.getTextLength());
						}
					}
				} catch (BadLocationException e) {
					Logger.logException(e);
				}
			}
		}
		return null;
	}

	/**
	 * Gets the infoProvider.
	 * 
	 * @return Returns fInfoProvider and if fInfoProvider was
	 *         <code>null</code> set fInfoProvider to DefaultInfoProvider
	 */
	public MarkupTagInfoProvider getInfoProvider() {
		if (fInfoProvider == null) {
			fInfoProvider = new MarkupTagInfoProvider();
		}
		return fInfoProvider;
	}
}
