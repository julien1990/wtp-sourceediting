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
package org.eclipse.wst.html.core.validate;



import java.util.Iterator;

import org.eclipse.wst.sse.core.IndexedRegion;
import org.eclipse.wst.sse.core.text.ITextRegion;
import org.eclipse.wst.sse.core.text.ITextRegionContainer;
import org.eclipse.wst.sse.core.text.ITextRegionList;
import org.eclipse.wst.xml.core.document.IDOMAttr;
import org.eclipse.wst.xml.core.document.IDOMElement;
import org.eclipse.wst.xml.core.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDataType;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.parser.XMLRegionContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class HTMLAttributeValidator extends PrimeValidator {

	private static final int REGION_NAME = 1;
	private static final int REGION_VALUE = 2;
	// <<D210422
	private static final char SINGLE_QUOTE = '\'';
	private static final char DOUBLE_QUOTE = '\"';

	// D210422
	/**
	 * HTMLAttributeValidator constructor comment.
	 */
	public HTMLAttributeValidator() {
		super();
	}

	/**
	 */
	private Segment getErrorSegment(IDOMNode errorNode, int regionType) {
		ITextRegion rgn = null;
		switch (regionType) {
			case REGION_NAME :
				rgn = errorNode.getNameRegion();
				break;
			case REGION_VALUE :
				rgn = errorNode.getValueRegion();
				break;
			default :
				// nothing to do.
				break;
		}
		if (rgn != null) {
			if (errorNode instanceof IDOMAttr) {
				IDOMElement ownerElement = (IDOMElement) ((IDOMAttr) errorNode).getOwnerElement();
				if (ownerElement != null) {
					int regionStartOffset = ownerElement.getFirstStructuredDocumentRegion().getStartOffset(rgn);
					int regionLength = rgn.getLength();
					return new Segment(regionStartOffset, regionLength);
				}
			}
		}
		return new Segment(errorNode.getStartOffset(), 0);
	}

	/**
	 * Allowing the INodeAdapter to compare itself against the type allows it
	 * to return true in more than one case.
	 */
	public boolean isAdapterForType(Object type) {
		return ((type == HTMLAttributeValidator.class) || super.isAdapterForType(type));
	}

	/**
	 */
	public void validate(IndexedRegion node) {
		Element target = (Element) node;
		if (CMUtil.isForeign(target))
			return;
		CMElementDeclaration edec = CMUtil.getDeclaration(target);
		if (edec == null)
			return;
		CMNamedNodeMap declarations = edec.getAttributes();

		NamedNodeMap attrs = target.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			int rgnType = REGION_NAME;
			int state = ErrorState.NONE_ERROR;
			Attr a = (Attr) attrs.item(i);
			// D203637; If the target attr has prefix, the validator should
			// not
			// warn about it. That is, just ignore. It is able to check
			// whether
			// an attr has prefix or not by calling XMLAttr#isGlobalAttr().
			// When a attr has prefix (not global), it returns false.
			boolean isXMLAttr = a instanceof IDOMAttr;
			if (isXMLAttr) {
				IDOMAttr xmlattr = (IDOMAttr) a;
				if (!xmlattr.isGlobalAttr())
					continue; // skip futher validation and begin next loop.
			}

			CMAttributeDeclaration adec = (CMAttributeDeclaration) declarations.getNamedItem(a.getName());
			if (adec == null) {
				// No attr declaration was found. That is, the attr name is
				// undefined.
				// but not regard it as undefined name if it includes JSP
				if (hasJSPRegion(((IDOMNode) a).getNameRegion())) {
					rgnType = REGION_NAME;
					state = ErrorState.UNDEFINED_NAME_ERROR;
				}
			}
			else {
				// The attr declaration was found.
				// At 1st, the name should be checked.
				if (CMUtil.isHTML(edec) && (!CMUtil.isXHTML(edec))) {
					// If the target element is pure HTML (not XHTML), some
					// attributes
					// might be written in boolean format. It should be check
					// specifically.
					if (CMUtil.isBooleanAttr(adec) && ((IDOMAttr) a).hasNameOnly())
						continue; // OK, keep going. No more check is needed
					// against this attr.
				}
				else {
					// If the target is other than pure HTML (JSP or XHTML),
					// the name
					// must be checked exactly (ie in case sensitive way).
					String actual = a.getName();
					String desired = adec.getAttrName();
					if (!actual.equals(desired)) { // case mismatch
						rgnType = REGION_NAME;
						state = ErrorState.MISMATCHED_ERROR;
					}
				}
				// Then, the value must be checked.
				if (state == ErrorState.NONE_ERROR) { // Need more check.
					// Now, the value should be checked, if the type is ENUM.
					CMDataType attrType = adec.getAttrType();
					String actualValue = a.getValue();
					if (attrType.getImpliedValueKind() == CMDataType.IMPLIED_VALUE_FIXED) {
						// Check FIXED value.
						String validValue = attrType.getImpliedValue();
						if (!actualValue.equals(validValue)) {
							rgnType = REGION_VALUE;
							state = ErrorState.UNDEFINED_VALUE_ERROR;
						}
					}
					else {
						String[] candidates = attrType.getEnumeratedValues();
						if (candidates != null && candidates.length > 0) {
							// several candidates are found.
							boolean found = false;
							for (int index = 0; index < candidates.length; index++) {
								String candidate = candidates[index];
								// At 1st, compare ignoring case.
								if (actualValue.equalsIgnoreCase(candidate)) {
									found = true;
									if (CMUtil.isCaseSensitive(edec) && (!actualValue.equals(candidate))) {
										rgnType = REGION_VALUE;
										state = ErrorState.MISMATCHED_VALUE_ERROR;
									}
									break; // exit the loop.
								}
							}
							if (!found) {
								// No candidate was found. That is,
								// actualValue is invalid.
								// but not regard it as undefined value if it
								// includes JSP.
								if (hasJSPRegion(((IDOMNode) a).getValueRegion())) {
									rgnType = REGION_VALUE;
									state = ErrorState.UNDEFINED_VALUE_ERROR;
								}
							}
						}
					}
				}
				// <<D210422
				if (state == ErrorState.NONE_ERROR) { // Need more check.
					if (isXMLAttr) {
						String source = ((IDOMAttr) a).getValueRegionText();
						if (source != null) {
							char firstChar = source.charAt(0);
							char lastChar = source.charAt(source.length() - 1);
							if (isQuote(firstChar) || isQuote(lastChar)) {
								if (lastChar != firstChar) {
									rgnType = REGION_VALUE;
									state = ErrorState.UNCLOSED_ATTR_VALUE;
								}
							}
						}
					}
				}
				// D210422
			}
			if (state != ErrorState.NONE_ERROR) {
				Segment seg = getErrorSegment((IDOMNode) a, rgnType);
				if (seg != null)
					reporter.report(MessageFactory.createMessage(new ErrorInfoImpl(state, seg, a)));
			}
		}
	}

	/**
	 */
	private boolean hasJSPRegion(ITextRegion container) {
		if (!(container instanceof ITextRegionContainer))
			return false;
		ITextRegionList regions = ((ITextRegionContainer) container).getRegions();
		if (regions == null)
			return false;
		Iterator e = regions.iterator();
		while (e.hasNext()) {
			ITextRegion region = (ITextRegion) e.next();
			if (region == null)
				continue;
			String regionType = region.getType();
			if (regionType == XMLRegionContext.XML_TAG_OPEN || (isNextedTagName(regionType)))
				return true;
		}
		return false;
	}

    /**
     * ISSUE: this is a bit of hidden JSP knowledge that was implemented this
     * way for expedency. Should be evolved in future to depend on "nestedContext".
     */
	private boolean isNextedTagName(String regionType) {
		final String JSP_SCRIPTLET_OPEN = "JSP_SCRIPTLET_OPEN"; //$NON-NLS-1$
		final String JSP_EXPRESSION_OPEN = "JSP_EXPRESSION_OPEN"; //$NON-NLS-1$
		final String JSP_DECLARATION_OPEN = "JSP_DECLARATION_OPEN"; //$NON-NLS-1$
		final String JSP_DIRECTIVE_OPEN = "JSP_DIRECTIVE_OPEN"; //$NON-NLS-1$

		boolean result = regionType.equals(JSP_SCRIPTLET_OPEN) || regionType.equals(JSP_EXPRESSION_OPEN) || regionType.equals(JSP_DECLARATION_OPEN) || regionType.equals(JSP_DIRECTIVE_OPEN);
		return result;
	}

	// <<D214022
	private boolean isQuote(char c) {
		return (c == SINGLE_QUOTE) || (c == DOUBLE_QUOTE);
	}
	// D210422
}