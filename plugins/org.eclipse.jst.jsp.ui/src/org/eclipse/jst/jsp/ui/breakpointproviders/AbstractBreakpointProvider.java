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
package org.eclipse.jst.jsp.ui.breakpointproviders;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jst.jsp.core.JSP12Namespace;
import org.eclipse.jst.jsp.core.contentmodel.tld.TLDElementDeclaration;
import org.eclipse.jst.jsp.core.model.parser.XMLJSPRegionContexts;
import org.eclipse.ui.IEditorInput;
import org.eclipse.wst.common.contentmodel.CMElementDeclaration;
import org.eclipse.wst.common.contentmodel.CMNode;
import org.eclipse.wst.common.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.sse.core.IStructuredModel;
import org.eclipse.wst.sse.core.IndexedRegion;
import org.eclipse.wst.sse.core.contentmodel.CMNodeWrapper;
import org.eclipse.wst.sse.core.text.IStructuredDocument;
import org.eclipse.wst.sse.core.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.text.ITextRegion;
import org.eclipse.wst.sse.core.text.ITextRegionCollection;
import org.eclipse.wst.sse.core.text.ITextRegionList;
import org.eclipse.wst.sse.ui.extensions.breakpoint.IBreakpointProvider;
import org.eclipse.wst.xml.core.document.XMLDocument;
import org.eclipse.wst.xml.core.modelquery.ModelQueryUtil;
import org.eclipse.wst.xml.core.parser.XMLRegionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract breakpoint provider class which implements breakpoint provider
 * interface.
 * 
 * This is a temporary class for JavaBreakpointProvider and
 * JavaScriptBreakpointProvider, and should be refactored to separate Java and
 * JavaScript parts.
 */
public abstract class AbstractBreakpointProvider implements IBreakpointProvider {

	private static final String JSP_DIRECTIVE_PAGE = "jsp:directive.page"; //$NON-NLS-1$
	private static final String[] JAVASCRIPT_LANGUAGE_KEYS = new String[]{"javascript", "javascript1.0", "javascript1.1_3", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"javascript1.2", "javascript1.3", "javascript1.4", "javascript1.5", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"javascript1.6", "jscript", "sashscript"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	protected static final int UNSUPPORTED = 0;
	protected static final int JAVA = 1;
	protected static final int JAVASCRIPT = 2;

	protected static final int END_OF_LINE = -1;
	protected static final int NO_VALID_CONTENT = -2;

	protected int getValidPosition(Document doc, IDocument idoc, int lineNumber) {
		if (doc == null)
			return NO_VALID_CONTENT;
		if (idoc == null)
			return NO_VALID_CONTENT;

		int startOffset, endOffset;
		try {
			startOffset = idoc.getLineOffset(lineNumber - 1);
			endOffset = idoc.getLineOffset(lineNumber) - 1;

			if (idoc == null)
				return NO_VALID_CONTENT;
			String lineText = idoc.get(startOffset, endOffset - startOffset).trim();

			// blank lines or lines with only an open or close brace or
			// scriptlet tag cannot have a breakpoint
			if (lineText.equals("") || lineText.equals("{") || //$NON-NLS-2$//$NON-NLS-1$
						lineText.equals("}") || lineText.equals("<%"))//$NON-NLS-2$//$NON-NLS-1$
				return NO_VALID_CONTENT;
		}
		catch (BadLocationException e) {
			return NO_VALID_CONTENT;
		}

		IStructuredDocumentRegion flatNode = ((IStructuredDocument) idoc).getRegionAtCharacterOffset(startOffset);
		// go through the node's regions looking for JSP content
		// until reaching the end of the line
		while (flatNode != null) {
			int validPosition = getValidRegionPosition(((XMLDocument) doc).getModel(), flatNode, startOffset, endOffset);

			if (validPosition == END_OF_LINE)
				return NO_VALID_CONTENT;

			if (validPosition >= 0)
				return validPosition;

			flatNode = flatNode.getNext();
		}
		return NO_VALID_CONTENT;
	}

	/*
	 * Search the RegionContainer's regions looking for JSP content.
	 * If valid content is found, return the position >= 0
	 * If no valid content is found, return NO_VALID_CONTENT.
	 * If a region starts after the line's endOffset, return END_OF_LINE.
	 */
	private static int getValidRegionPosition(IStructuredModel model, ITextRegionCollection regionContainer, int startOffset, int endOffset) {

		ITextRegionList regions = regionContainer.getRegions();
		for (int i = 0; i < regions.size(); i++) {
			ITextRegion region = regions.get(i);
			if (region instanceof ITextRegionCollection) {
				int validPosition = getValidRegionPosition(model, (ITextRegionCollection) region, startOffset, endOffset);
				if (validPosition == END_OF_LINE || validPosition >= 0)
					return validPosition;
			}
			else {
				// region must be at least partially on selected line
				if (regionContainer.getEndOffset(region) > startOffset) {

					int regionStartOffset = regionContainer.getStartOffset(region);
					// if region starts after line's endOffset, we're done searching
					if (regionStartOffset > endOffset)
						return END_OF_LINE;

					// If region is JSP content, make sure the language is Java not Javascript by
					// checking the content assist adapter's type.
					if (region.getType().equals(XMLJSPRegionContexts.JSP_CONTENT)) {
						// DWM: this logic is not incorrect ... given changes to adapters, etc.
						// but probably don't need anything here, since both Java and JavaScript
						// are supported in V5.

						// nsd_TODO: verify this!!!

						//						INodeNotifier notifier = (INodeNotifier)model.getNode(region.getStartOffset());
						//						AdapterFactory factory = model.getFactoryRegistry().getFactoryFor(ContentAssistAdapter.class);
						//						if(factory instanceof HTMLContentAssistAdapterFactory) {
						//							INodeAdapter adapter = ((HTMLContentAssistAdapterFactory)factory).createAdapter(notifier, region);
						//							if(adapter != null && adapter instanceof JSPJavaContentAssistAdapter)

						if (regionStartOffset > startOffset)
							return regionStartOffset;
						else
							return startOffset;
						//						}
					}
					// a custom tag, jsp:useBean, getproperty or setproperty statement is also a valid breakpoint location
					else if (region.getType().equals(XMLRegionContext.XML_TAG_NAME) && (isCustomTagRegion(model.getIndexedRegion(regionStartOffset)) || regionContainer.getText(region).equals(JSP12Namespace.ElementName.USEBEAN) || regionContainer.getText(region).equals(JSP12Namespace.ElementName.GETPROPERTY) || regionContainer.getText(region).equals(JSP12Namespace.ElementName.SETPROPERTY))) {

						if (regionStartOffset > startOffset)
							return regionStartOffset;
						else
							return startOffset;
					}
					else {
						// Defect #241090, the Text Nodes inside of JSP scriptlets, expressions, and declarations are valid
						// breakpoint-able locations
						boolean isCodeNode = false;
						IndexedRegion node = model.getIndexedRegion(regionStartOffset);
						if (node != null && node instanceof Node) {
							Node domNode = (Node) node;
							Node root = domNode.getOwnerDocument().getDocumentElement();
							if (root != null && root.getNodeName().equals(JSP12Namespace.ElementName.ROOT) && domNode.getNodeType() == Node.TEXT_NODE && domNode.getParentNode() != null) {
								String parentName = domNode.getParentNode().getNodeName();
								isCodeNode = parentName.equals(JSP12Namespace.ElementName.SCRIPTLET) || parentName.equals(JSP12Namespace.ElementName.EXPRESSION) || parentName.equals(JSP12Namespace.ElementName.DECLARATION);
							}
						}
						if (isCodeNode) {
							if (regionStartOffset > startOffset)
								return regionStartOffset;
							else
								return startOffset;
						}
					}
				}
			}
		}
		return NO_VALID_CONTENT;
	}

	private static boolean isCustomTagRegion(IndexedRegion node) {

		if (node instanceof Element) {
			Element xmlElement = (Element) node;
			ModelQuery mq = ModelQueryUtil.getModelQuery(xmlElement.getOwnerDocument());
			CMElementDeclaration decl = mq.getCMElementDeclaration(xmlElement);
			if (decl instanceof CMNodeWrapper) {
				CMNode cmNode = ((CMNodeWrapper) decl).getOriginNode();
				return cmNode instanceof TLDElementDeclaration;
			}
		}
		return false;
	}

	protected IResource getEditorInputResource(IEditorInput input) {
		IResource resource = (IResource) input.getAdapter(IFile.class);
		if (resource == null) {
			resource = (IResource) input.getAdapter(IResource.class);
		}
		return resource;
	}


	/*
	 * Return the page language
	 */
	protected static int getPageLanguage(Document doc) {
		if (doc == null)
			return UNSUPPORTED;

		NodeList pageDirectives = doc.getElementsByTagName(JSP_DIRECTIVE_PAGE);
		// Search for first language directive
		for (int i = 0; i < pageDirectives.getLength(); i++) {
			Node child = pageDirectives.item(i);
			Node languageAttr = child.getAttributes().getNamedItem("language"); //$NON-NLS-1$
			if (languageAttr != null) {
				String pageLanguage = languageAttr.getNodeValue();
				if (pageLanguage == null || pageLanguage.length() == 0)
					return UNSUPPORTED;
				pageLanguage = pageLanguage.toLowerCase();
				if (contains(JAVASCRIPT_LANGUAGE_KEYS, pageLanguage))
					return JAVASCRIPT;
				else if (pageLanguage.equals("java"))//$NON-NLS-1$
					return JAVA;
				else
					return UNSUPPORTED;
			}
		}
		return JAVA; // Java is default if no language directive
	}

	protected static boolean contains(String[] haystack, String needle) {
		for (int i = 0; i < haystack.length; i++) {
			if (haystack[i].equals(needle)) {
				return true;
			}
		}
		return false;
	}
}