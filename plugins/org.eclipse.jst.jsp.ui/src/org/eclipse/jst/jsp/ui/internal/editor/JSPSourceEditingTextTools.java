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
package org.eclipse.jst.jsp.ui.internal.editor;



import org.eclipse.jst.jsp.core.JSP11Namespace;
import org.eclipse.jst.jsp.core.JSP12Namespace;
import org.eclipse.jst.jsp.core.internal.document.PageDirectiveAdapter;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.ui.internal.provisional.XMLSourceEditingTextTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implements SourceEditingTextTools interface
 */
public class JSPSourceEditingTextTools extends XMLSourceEditingTextTools {

	public String getPageLanguage(Node node) {
		String language = null;
		Document doc = null;
		if (node.getNodeType() == Node.DOCUMENT_NODE) {
			doc = (Document) node;
		}
		else {
			doc = node.getOwnerDocument();
		}
		if (doc != null) {
			if (doc instanceof IDOMDocument) {
				PageDirectiveAdapter adapter = (PageDirectiveAdapter) ((IDOMDocument) doc).getAdapterFor(PageDirectiveAdapter.class);
				if (adapter != null)
					language = adapter.getLanguage();
			}
			else {
				// iterate through all of the page directives
				NodeList pageDirectives = doc.getElementsByTagName(JSP12Namespace.ElementName.DIRECTIVE_PAGE);
				for (int i = 0; i < pageDirectives.getLength(); i++) {
					Element pageDirective = (Element) pageDirectives.item(i);
					String langValue = pageDirective.getAttribute(JSP11Namespace.ATTR_NAME_LANGUAGE);
					// last one to declare a language wins
					if (langValue != null)
						language = langValue;
				}
			}
		}
		// if no language was specified anywhere, assume Java
		if (language == null)
			language = JSP11Namespace.ATTR_VALUE_JAVA;
		return language;
	}
}
