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
package org.eclipse.wst.html.core.internal.cleanup;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.wst.css.core.internal.format.CSSSourceFormatter;
import org.eclipse.wst.css.core.internal.provisional.adapters.IStyleDeclarationAdapter;
import org.eclipse.wst.css.core.internal.provisional.document.ICSSModel;
import org.eclipse.wst.css.core.internal.provisional.document.ICSSNode;
import org.eclipse.wst.sse.core.INodeAdapter;
import org.eclipse.wst.sse.core.INodeNotifier;
import org.eclipse.wst.sse.core.exceptions.SourceEditingRuntimeException;
import org.eclipse.wst.sse.core.internal.cleanup.IStructuredCleanupHandler;
import org.eclipse.wst.sse.core.internal.util.StringUtils;
import org.eclipse.wst.sse.core.preferences.CommonModelPreferenceNames;
import org.eclipse.wst.sse.core.text.IStructuredDocument;
import org.eclipse.wst.sse.core.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.text.ITextRegion;
import org.eclipse.wst.sse.core.text.ITextRegionList;
import org.eclipse.wst.xml.core.document.IDOMAttr;
import org.eclipse.wst.xml.core.document.IDOMDocument;
import org.eclipse.wst.xml.core.document.IDOMElement;
import org.eclipse.wst.xml.core.document.IDOMModel;
import org.eclipse.wst.xml.core.document.IDOMNode;
import org.eclipse.wst.xml.core.document.ISourceGenerator;
import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

// nakamori_TODO: check and remove CSS formatting

public class ElementNodeCleanupHandler extends AbstractNodeCleanupHandler {

	/** Non-NLS strings */
	protected static final String START_TAG_OPEN = "<"; //$NON-NLS-1$
	protected static final String END_TAG_OPEN = "</"; //$NON-NLS-1$
	protected static final String TAG_CLOSE = ">"; //$NON-NLS-1$
	protected static final String EMPTY_TAG_CLOSE = "/>"; //$NON-NLS-1$
	protected static final String SINGLE_QUOTES = "''"; //$NON-NLS-1$
	protected static final String DOUBLE_QUOTES = "\"\""; //$NON-NLS-1$
	protected static final char SINGLE_QUOTE = '\''; //$NON-NLS-1$
	protected static final char DOUBLE_QUOTE = '\"'; //$NON-NLS-1$

	public Node cleanup(Node node) {
		IDOMNode renamedNode = (IDOMNode) cleanupChildren(node);

		// call quoteAttrValue() first so it will close any unclosed attr
		// quoteAttrValue() will return the new start tag if there is a structure change
		renamedNode = quoteAttrValue(renamedNode);

		// insert tag close if missing
		// if node is not comment tag
		// and not implicit tag
		if (!((IDOMElement) renamedNode).isCommentTag() && (renamedNode.getStartStructuredDocumentRegion() != null)) {
			IDOMModel structuredModel = renamedNode.getModel();

			// save start offset before insertTagClose()
			// or else renamedNode.getStartOffset() will be zero if renamedNode replaced by insertTagClose()
			int startTagStartOffset = renamedNode.getStartOffset();

			// for start tag
			IStructuredDocumentRegion startTagStructuredDocumentRegion = renamedNode.getStartStructuredDocumentRegion();
			insertTagClose(structuredModel, startTagStructuredDocumentRegion);

			// update renamedNode and startTagStructuredDocumentRegion after insertTagClose()
			renamedNode = (IDOMNode) structuredModel.getIndexedRegion(startTagStartOffset);
			startTagStructuredDocumentRegion = renamedNode.getStartStructuredDocumentRegion();

			// for end tag
			IStructuredDocumentRegion endTagStructuredDocumentRegion = renamedNode.getEndStructuredDocumentRegion();
			if (endTagStructuredDocumentRegion != startTagStructuredDocumentRegion)
				insertTagClose(structuredModel, endTagStructuredDocumentRegion);
		}

		// call insertMissingTags() next, it will generate implicit tags if there are any
		// insertMissingTags() will return the new missing start tag if one is missing
		// applyTagNameCase() will return the renamed node.
		// The renamed/new node will be saved and returned to caller when all cleanup is done.
		renamedNode = insertMissingTags(renamedNode);
		renamedNode = insertRequiredAttrs(renamedNode);
		renamedNode = applyTagNameCase(renamedNode);
		applyAttrNameCase(renamedNode);
		cleanupCSSAttrValue(renamedNode);

		return renamedNode;
	}

	private boolean shouldIgnoreCase(IDOMElement element) {
		// case option can be applied to no namespace tags
		return element.isGlobalTag();
		/*
		 ModelQueryAdapter mqadapter = (ModelQueryAdapter) element.getAdapterFor(ModelQueryAdapter.class);
		 ModelQuery mq = null;
		 CMNode nodedecl = null;
		 if (mqadapter != null)
		 mq = mqadapter.getModelQuery();
		 if (mq != null)
		 nodedecl = mq.getCMNode(node);
		 // if a Node isn't recognized as HTML or is and cares about case, do not alter it
		 //	if (nodedecl == null || (nodedecl instanceof HTMLCMNode && ((HTMLCMNode) nodedecl).shouldIgnoreCase()))
		 if (! nodedecl.supports(HTMLCMProperties.SHOULD_IGNORE_CASE)) return false;
		 return ((Boolean)cmnode.getProperty(HTMLCMProperties.SHOULD_IGNORE_CASE)).booleanValue();
		 */
	}

	protected void applyAttrNameCase(IDOMNode node) {
		IDOMElement element = (IDOMElement) node;
		if (element.isCommentTag())
			return; // do nothing

		int attrNameCase = CommonModelPreferenceNames.ASIS;

		if (shouldIgnoreCase(element))
			attrNameCase = getCleanupPreferences().getAttrNameCase();

		NamedNodeMap attributes = node.getAttributes();
		int attributesLength = attributes.getLength();

		for (int i = 0; i < attributesLength; i++) {
			IDOMNode eachAttr = (IDOMNode) attributes.item(i);
			String oldAttrName = eachAttr.getNodeName();
			String newAttrName = oldAttrName;
			// 254961 - all HTML tag names and attribute names should be in English
			//          even for HTML files in other languages like Japanese or Turkish.
			//          English locale should be used to convert between uppercase and lowercase
			//          (otherwise "link" would be converted to "L�NK" in Turkish, where '?' in "L�NK"
			//          is the "I Overdot Capital" in Turkish).
			if (attrNameCase == CommonModelPreferenceNames.LOWER)
				newAttrName = oldAttrName.toLowerCase(Locale.US);
			else if (attrNameCase == CommonModelPreferenceNames.UPPER)
				newAttrName = oldAttrName.toUpperCase(Locale.US);

			if (newAttrName.compareTo(oldAttrName) != 0) {
				int attrNameStartOffset = eachAttr.getStartOffset();
				int attrNameLength = oldAttrName.length();

				IDOMModel structuredModel = node.getModel();
				IStructuredDocument structuredDocument = structuredModel.getStructuredDocument();
				replaceSource(structuredModel, structuredDocument, attrNameStartOffset, attrNameLength, newAttrName);
			}
		}
	}

	protected IDOMNode applyTagNameCase(IDOMNode node) {
		IDOMElement element = (IDOMElement) node;
		if (element.isCommentTag())
			return node; // do nothing

		int tagNameCase = CommonModelPreferenceNames.ASIS;

		if (shouldIgnoreCase(element))
			tagNameCase = getCleanupPreferences().getTagNameCase();

		String oldTagName = node.getNodeName();
		String newTagName = oldTagName;
		IDOMNode newNode = node;

		// 254961 - all HTML tag names and attribute names should be in English
		//          even for HTML files in other languages like Japanese or Turkish.
		//          English locale should be used to convert between uppercase and lowercase
		//          (otherwise "link" would be converted to "L�NK" in Turkish, where '?' in "L�NK"
		//          is the "I Overdot Capital" in Turkish).
		if (tagNameCase == CommonModelPreferenceNames.LOWER)
			newTagName = oldTagName.toLowerCase(Locale.US);
		else if (tagNameCase == CommonModelPreferenceNames.UPPER)
			newTagName = oldTagName.toUpperCase(Locale.US);

		IDOMModel structuredModel = node.getModel();
		IStructuredDocument structuredDocument = structuredModel.getStructuredDocument();

		IStructuredDocumentRegion startTagStructuredDocumentRegion = node.getStartStructuredDocumentRegion();
		if (startTagStructuredDocumentRegion != null) {
			ITextRegionList regions = startTagStructuredDocumentRegion.getRegions();
			if (regions != null && regions.size() > 0) {
				ITextRegion startTagNameRegion = regions.get(1);
				int startTagNameStartOffset = startTagStructuredDocumentRegion.getStartOffset(startTagNameRegion);
				int startTagNameLength = startTagStructuredDocumentRegion.getTextEndOffset(startTagNameRegion) - startTagNameStartOffset;

				replaceSource(structuredModel, structuredDocument, startTagNameStartOffset, startTagNameLength, newTagName);
				newNode = (IDOMNode) structuredModel.getIndexedRegion(startTagNameStartOffset); // save new node
			}
		}

		IStructuredDocumentRegion endTagStructuredDocumentRegion = node.getEndStructuredDocumentRegion();
		if (endTagStructuredDocumentRegion != null) {
			ITextRegionList regions = endTagStructuredDocumentRegion.getRegions();
			if (regions != null && regions.size() > 0) {
				ITextRegion endTagNameRegion = regions.get(1);
				int endTagNameStartOffset = endTagStructuredDocumentRegion.getStartOffset(endTagNameRegion);
				int endTagNameLength = endTagStructuredDocumentRegion.getTextEndOffset(endTagNameRegion) - endTagNameStartOffset;

				if (startTagStructuredDocumentRegion != endTagStructuredDocumentRegion)
					replaceSource(structuredModel, structuredDocument, endTagNameStartOffset, endTagNameLength, newTagName);
			}
		}

		return newNode;
	}

	protected Node cleanupChildren(Node node) {
		Node parentNode = node;

		if (node != null) {
			Node childNode = node.getFirstChild();
			HTMLCleanupHandlerFactory factory = HTMLCleanupHandlerFactory.getInstance();
			while (childNode != null) {
				// cleanup this child node
				IStructuredCleanupHandler cleanupHandler = factory.createHandler(childNode, getCleanupPreferences());
				childNode = cleanupHandler.cleanup(childNode);

				// get new parent node
				parentNode = (IDOMNode) childNode.getParentNode();

				// get next child node
				childNode = (IDOMNode) childNode.getNextSibling();
			}
		}

		return parentNode;
	}

	/**
	 */
	protected void cleanupCSSAttrValue(IDOMNode node) {
		if (node == null || node.getNodeType() != Node.ELEMENT_NODE)
			return;
		IDOMElement element = (IDOMElement) node;
		if (!element.isGlobalTag())
			return;

		Attr attr = element.getAttributeNode("style"); //$NON-NLS-1$
		if (attr == null)
			return;
		String value = getCSSValue(attr);
		if (value == null)
			return;
		String oldValue = ((IDOMNode) attr).getValueSource();
		if (oldValue != null && value.equals(oldValue))
			return;
		attr.setValue(value);
	}

	/**
	 */
	private ICSSModel getCSSModel(Attr attr) {
		if (attr == null)
			return null;
		INodeNotifier notifier = (INodeNotifier) attr.getOwnerElement();
		if (notifier == null)
			return null;
		INodeAdapter adapter = notifier.getAdapterFor(IStyleDeclarationAdapter.class);
		if (adapter == null)
			return null;
		if (!(adapter instanceof IStyleDeclarationAdapter))
			return null;
		IStyleDeclarationAdapter styleAdapter = (IStyleDeclarationAdapter) adapter;
		return styleAdapter.getModel();
	}

	/**
	 */
	private String getCSSValue(Attr attr) {
		ICSSModel model = getCSSModel(attr);
		if (model == null)
			return null;
		ICSSNode document = model.getDocument();
		if (document == null)
			return null;
		INodeNotifier notifier = (INodeNotifier) document;
		INodeAdapter adapter = notifier.getAdapterFor(CSSSourceFormatter.class);
		if (adapter == null)
			return null;
		CSSSourceFormatter formatter = (CSSSourceFormatter) adapter;
		StringBuffer buffer = formatter.cleanup(document);
		if (buffer == null)
			return null;
		return buffer.toString();
	}

	private boolean isEmptyElement(IDOMElement element) {
		Document document = element.getOwnerDocument();
		if (document == null)
			// undefined tag, return default
			return false;

		ModelQuery modelQuery = ModelQueryUtil.getModelQuery(document);
		if (modelQuery == null)
			// undefined tag, return default
			return false;

		CMElementDeclaration decl = modelQuery.getCMElementDeclaration(element);
		if (decl == null)
			// undefined tag, return default
			return false;

		return (decl.getContentType() == CMElementDeclaration.EMPTY);
	}

	protected IDOMNode insertEndTag(IDOMNode node) {
		IDOMElement element = (IDOMElement) node;

		int startTagStartOffset = node.getStartOffset();
		IDOMModel structuredModel = node.getModel();
		IDOMNode newNode = null;

		if (element.isCommentTag()) {
			// do nothing
		}
		else if (isEmptyElement(element)) {
			IStructuredDocument structuredDocument = structuredModel.getStructuredDocument();
			IStructuredDocumentRegion startStructuredDocumentRegion = node.getStartStructuredDocumentRegion();
			ITextRegionList regions = startStructuredDocumentRegion.getRegions();
			ITextRegion lastRegion = regions.get(regions.size() - 1);
			replaceSource(structuredModel, structuredDocument, startStructuredDocumentRegion.getStartOffset(lastRegion), lastRegion.getLength(), EMPTY_TAG_CLOSE);

			if (regions.size() > 1) {
				ITextRegion regionBeforeTagClose = regions.get(regions.size() - 1 - 1);

				// insert a space separator before tag close if the previous region does not have extra spaces
				if (regionBeforeTagClose.getTextLength() == regionBeforeTagClose.getLength())
					replaceSource(structuredModel, structuredDocument, startStructuredDocumentRegion.getStartOffset(lastRegion), 0, " "); //$NON-NLS-1$
			}
		}
		else {
			String tagName = node.getNodeName();
			String endTag = END_TAG_OPEN.concat(tagName).concat(TAG_CLOSE);

			IDOMNode lastChild = (IDOMNode) node.getLastChild();
			int endTagStartOffset = 0;
			if (lastChild != null)
				// if this node has children, insert the end tag after the last child
				endTagStartOffset = lastChild.getEndOffset();
			else
				// if this node does not has children, insert the end tag after the start tag
				endTagStartOffset = node.getEndOffset();

			IStructuredDocument structuredDocument = structuredModel.getStructuredDocument();
			replaceSource(structuredModel, structuredDocument, endTagStartOffset, 0, endTag);
		}

		newNode = (IDOMNode) structuredModel.getIndexedRegion(startTagStartOffset); // save new node

		return newNode;
	}

	protected IDOMNode insertMissingTags(IDOMNode node) {
		boolean insertMissingTags = getCleanupPreferences().getInsertMissingTags();
		IDOMNode newNode = node;

		if (insertMissingTags) {
			IStructuredDocumentRegion startTagStructuredDocumentRegion = node.getStartStructuredDocumentRegion();
			if (startTagStructuredDocumentRegion == null) {
				// implicit start tag; generate tag for it
				newNode = insertStartTag(node);
				startTagStructuredDocumentRegion = newNode.getStartStructuredDocumentRegion();
			}

			IStructuredDocumentRegion endTagStructuredDocumentRegion = newNode.getEndStructuredDocumentRegion();

			ITextRegionList regionList = startTagStructuredDocumentRegion.getRegions();
			if (startTagStructuredDocumentRegion != null && regionList != null && regionList.get(regionList.size() - 1).getType() == DOMRegionContext.XML_EMPTY_TAG_CLOSE) {

			}
			else {
				if (startTagStructuredDocumentRegion == null) {
					// start tag missing
					if (isStartTagRequired(newNode))
						newNode = insertStartTag(newNode);
				}
				else if (endTagStructuredDocumentRegion == null) {
					// end tag missing
					if (isEndTagRequired(newNode))
						newNode = insertEndTag(newNode);
				}
			}
		}

		return newNode;
	}

	protected IDOMNode insertStartTag(IDOMNode node) {
		IDOMElement element = (IDOMElement) node;
		if (element.isCommentTag())
			return node; // do nothing

		IDOMNode newNode = null;

		String tagName = node.getNodeName();
		String startTag = START_TAG_OPEN.concat(tagName).concat(TAG_CLOSE);
		int startTagStartOffset = node.getStartOffset();

		IDOMModel structuredModel = node.getModel();
		IStructuredDocument structuredDocument = structuredModel.getStructuredDocument();
		replaceSource(structuredModel, structuredDocument, startTagStartOffset, 0, startTag);
		newNode = (IDOMNode) structuredModel.getIndexedRegion(startTagStartOffset); // save new node

		return newNode;
	}

	protected void insertTagClose(IDOMModel structuredModel, IStructuredDocumentRegion flatNode) {
		if ((flatNode != null) && (flatNode.getRegions() != null)) {
			ITextRegionList regionList = flatNode.getRegions();
			ITextRegion lastRegion = regionList.get(regionList.size() - 1);
			if (lastRegion != null) {
				String regionType = lastRegion.getType();
				if ((regionType != DOMRegionContext.XML_EMPTY_TAG_CLOSE) && (regionType != DOMRegionContext.XML_TAG_CLOSE)) {
					IStructuredDocument structuredDocument = structuredModel.getStructuredDocument();

					// insert ">" after lastRegion of flatNode
					// as in "<a</a>" if flatNode is for start tag, or in "<a></a" if flatNode is for end tag
					replaceSource(structuredModel, structuredDocument, flatNode.getTextEndOffset(lastRegion), 0, ">"); //$NON-NLS-1$
				}
			}
		}
	}

	protected boolean isEndTagRequired(IDOMNode node) {
		if (node == null)
			return false;
		return node.isContainer();
	}

	/**
	 * The end tags of HTML EMPTY content type, such as IMG,
	 * and HTML undefined tags are parsed separately from the start tags.
	 * So inserting the missing start tag is useless and even harmful.
	 */
	protected boolean isStartTagRequired(IDOMNode node) {
		if (node == null)
			return false;
		return node.isContainer();
	}

	protected boolean isXMLType(IDOMModel structuredModel) {
		boolean result = false;

		if (structuredModel != null && structuredModel != null) {
			IDOMDocument document = structuredModel.getDocument();

			if (document != null)
				result = document.isXMLType();
		}

		return result;
	}

	protected IDOMNode quoteAttrValue(IDOMNode node) {
		IDOMElement element = (IDOMElement) node;
		if (element.isCommentTag())
			return node; // do nothing

		boolean quoteAttrValues = getCleanupPreferences().getQuoteAttrValues();
		IDOMNode newNode = node;

		if (quoteAttrValues) {
			NamedNodeMap attributes = newNode.getAttributes();
			int attributesLength = attributes.getLength();
			ISourceGenerator generator = node.getModel().getGenerator();

			for (int i = 0; i < attributesLength; i++) {
				attributes = newNode.getAttributes();
				attributesLength = attributes.getLength();
				IDOMAttr eachAttr = (IDOMAttr) attributes.item(i);
				//ITextRegion oldAttrValueRegion = eachAttr.getValueRegion();
				String oldAttrValue = eachAttr.getValueRegionText();
				if (oldAttrValue == null) {
					IDOMModel structuredModel = node.getModel();
					if (isXMLType(structuredModel)) {
						// TODO: Kit, please check. Is there any way to not rely on getting regions from attributes?
						String newAttrValue = "=\"" + eachAttr.getNameRegionText() + "\""; //$NON-NLS-1$ //$NON-NLS-2$

						IStructuredDocument structuredDocument = structuredModel.getStructuredDocument();
						replaceSource(structuredModel, structuredDocument, eachAttr.getNameRegionEndOffset(), 0, newAttrValue);
						newNode = (IDOMNode) structuredModel.getIndexedRegion(node.getStartOffset()); // save new node
					}
				}
				else {

					char quote = StringUtils.isQuoted(oldAttrValue) ? oldAttrValue.charAt(0) : DOUBLE_QUOTE;
					String newAttrValue = generator.generateAttrValue(eachAttr, quote);

					// There is a problem in StructuredDocumentRegionUtil.getAttrValue(ITextRegion) when the region is instanceof ContextRegion.
					// Workaround for now...
					if (oldAttrValue.length() == 1) {
						char firstChar = oldAttrValue.charAt(0);
						if (firstChar == SINGLE_QUOTE)
							newAttrValue = SINGLE_QUOTES;
						else if (firstChar == DOUBLE_QUOTE)
							newAttrValue = DOUBLE_QUOTES;
					}

					if (newAttrValue != null) {
						if (newAttrValue.compareTo(oldAttrValue) != 0) {
							int attrValueStartOffset = eachAttr.getValueRegionStartOffset();
							int attrValueLength = oldAttrValue.length();
							int startTagStartOffset = node.getStartOffset();

							IDOMModel structuredModel = node.getModel();
							IStructuredDocument structuredDocument = structuredModel.getStructuredDocument();
							replaceSource(structuredModel, structuredDocument, attrValueStartOffset, attrValueLength, newAttrValue);
							newNode = (IDOMNode) structuredModel.getIndexedRegion(startTagStartOffset); // save new node
						}
					}
				}
			}
		}

		return newNode;
	}

	private IDOMNode insertRequiredAttrs(IDOMNode node) {
		boolean insertRequiredAttrs = getCleanupPreferences().getInsertRequiredAttrs();
		IDOMNode newNode = node;

		if (insertRequiredAttrs) {
			List requiredAttrs = getRequiredAttrs(newNode);
			if (requiredAttrs.size() > 0) {
				NamedNodeMap currentAttrs = node.getAttributes();
				List insertAttrs = new ArrayList();
				if (currentAttrs.getLength() == 0)
					insertAttrs.addAll(requiredAttrs);
				else {
					for (int i = 0; i < requiredAttrs.size(); i++) {
						String requiredAttrName = ((CMAttributeDeclaration) requiredAttrs.get(i)).getAttrName();
						boolean found = false;
						for (int j = 0; j < currentAttrs.getLength(); j++) {
							String currentAttrName = currentAttrs.item(j).getNodeName();
							if (requiredAttrName.compareToIgnoreCase(currentAttrName) == 0) {
								found = true;
								break;
							}
						}
						if (!found)
							insertAttrs.add(requiredAttrs.get(i));
					}
				}
				if (insertAttrs.size() > 0) {
					IStructuredDocumentRegion startStructuredDocumentRegion = newNode.getStartStructuredDocumentRegion();
					int index = startStructuredDocumentRegion.getEndOffset();
					ITextRegion lastRegion = startStructuredDocumentRegion.getLastRegion(); 
					if (lastRegion.getType() == DOMRegionContext.XML_TAG_CLOSE) {
						index--;
						lastRegion = startStructuredDocumentRegion.getRegionAtCharacterOffset(index - 1);
					}
					else if (lastRegion.getType() == DOMRegionContext.XML_EMPTY_TAG_CLOSE) {
						index = index - 2;
						lastRegion = startStructuredDocumentRegion.getRegionAtCharacterOffset(index - 1);
					}
					MultiTextEdit multiTextEdit = new MultiTextEdit();
					try {
						for (int i = insertAttrs.size() - 1; i >= 0; i--) {
							CMAttributeDeclaration attrDecl = (CMAttributeDeclaration) insertAttrs.get(i);
							String requiredAttributeName = attrDecl.getAttrName();
							String defaultValue = attrDecl.getDefaultValue();
							if (defaultValue == null)
								defaultValue = ""; //$NON-NLS-1$
							String nameAndDefaultValue = " "; //$NON-NLS-1$
							if (i == 0 && lastRegion.getLength() > lastRegion.getTextLength())
								nameAndDefaultValue = ""; //$NON-NLS-1$
							nameAndDefaultValue += requiredAttributeName + "=\"" + defaultValue + "\""; //$NON-NLS-1$ //$NON-NLS-2$
							multiTextEdit.addChild(new InsertEdit(index, nameAndDefaultValue));
							// BUG3381: MultiTextEdit applies all child TextEdit's basing on offsets
							//          in the document before the first TextEdit, not after each
							//          child TextEdit. Therefore, do not need to advance the index.
							//index += nameAndDefaultValue.length();
						}
						multiTextEdit.apply(newNode.getStructuredDocument());
					}
					catch (BadLocationException e) {
						throw new SourceEditingRuntimeException(e);
					}
				}
			}
		}

		return newNode;
	}


	protected ModelQuery getModelQuery(Node node) {
		if (node.getNodeType() == Node.DOCUMENT_NODE) {
			return ModelQueryUtil.getModelQuery((Document) node);
		}
		else {
			return ModelQueryUtil.getModelQuery(node.getOwnerDocument());
		}
	}

	protected List getRequiredAttrs(Node node) {
		List result = new ArrayList();

		ModelQuery modelQuery = getModelQuery(node);
		if (modelQuery != null) {
			CMElementDeclaration elementDecl = modelQuery.getCMElementDeclaration((Element) node);
			if (elementDecl != null) {
				CMNamedNodeMap attrMap = elementDecl.getAttributes();
				Iterator it = attrMap.iterator();
				CMAttributeDeclaration attr = null;
				while (it.hasNext()) {
					attr = (CMAttributeDeclaration) it.next();
					if (attr.getUsage() == CMAttributeDeclaration.REQUIRED) {
						result.add(attr);
					}
				}
			}
		}

		return result;
	}
}