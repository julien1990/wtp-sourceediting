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
package org.eclipse.wst.xml.ui.internal.style;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.wst.sse.core.text.ITextRegion;
import org.eclipse.wst.sse.ui.style.AbstractLineStyleProvider;
import org.eclipse.wst.sse.ui.style.LineStyleProvider;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.XMLUIPlugin;

public class LineStyleProviderForXML extends AbstractLineStyleProvider implements LineStyleProvider {
	public LineStyleProviderForXML() {
		super();
		loadColors();
	}

	protected void clearColors() {
		getTextAttributes().clear();
	}

	protected TextAttribute getAttributeFor(ITextRegion region) {
		/**
		 * a method to centralize all the "format rules" for regions
		 * specifically associated for how to "open" the region.
		 */
		// not sure why this is coming through null, but just to catch it
		if (region == null) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.CDATA_TEXT);
		}
		String type = region.getType();
		if ((type == DOMRegionContext.XML_CONTENT) || (type == DOMRegionContext.XML_DOCTYPE_INTERNAL_SUBSET)) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.XML_CONTENT);
		} else if ((type == DOMRegionContext.XML_TAG_OPEN) || (type == DOMRegionContext.XML_END_TAG_OPEN) || (type == DOMRegionContext.XML_TAG_CLOSE) || (type == DOMRegionContext.XML_EMPTY_TAG_CLOSE)) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.TAG_BORDER);
		} else if ((type == DOMRegionContext.XML_CDATA_OPEN) || (type == DOMRegionContext.XML_CDATA_CLOSE)) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.CDATA_BORDER);
		} else if (type == DOMRegionContext.XML_CDATA_TEXT) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.CDATA_TEXT);
		} else if (type == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.TAG_ATTRIBUTE_NAME);
		} else if (type == DOMRegionContext.XML_DOCTYPE_DECLARATION) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.TAG_NAME);
		} else if (type == DOMRegionContext.XML_TAG_NAME) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.TAG_NAME);
		} else if ((type == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE)) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.TAG_ATTRIBUTE_VALUE);
		} else if (type == DOMRegionContext.XML_TAG_ATTRIBUTE_EQUALS) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.TAG_ATTRIBUTE_EQUALS);
		} else if ((type == DOMRegionContext.XML_COMMENT_OPEN) || (type == DOMRegionContext.XML_COMMENT_CLOSE)) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.COMMENT_BORDER);
		} else if (type == DOMRegionContext.XML_COMMENT_TEXT) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.COMMENT_TEXT);
		} else if (type == DOMRegionContext.XML_DOCTYPE_NAME) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.DOCTYPE_NAME);
		} else if (type == DOMRegionContext.XML_PI_CONTENT) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.PI_CONTENT);
		} else if ((type == DOMRegionContext.XML_PI_OPEN) || (type == DOMRegionContext.XML_PI_CLOSE)) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.PI_BORDER);
		} else if ((type == DOMRegionContext.XML_DECLARATION_OPEN) || (type == DOMRegionContext.XML_DECLARATION_CLOSE)) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.DECL_BORDER);
		} else if (type == DOMRegionContext.XML_DOCTYPE_EXTERNAL_ID_SYSREF) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_SYSREF);
		} else if (type == DOMRegionContext.XML_DOCTYPE_EXTERNAL_ID_PUBREF) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_PUBREF);
		} else if (type == DOMRegionContext.XML_DOCTYPE_EXTERNAL_ID_PUBLIC || type == DOMRegionContext.XML_DOCTYPE_EXTERNAL_ID_SYSTEM) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID);
		} else if (type == DOMRegionContext.UNDEFINED) {
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.CDATA_TEXT);
		} else if (type == DOMRegionContext.WHITE_SPACE) {
			// white space is normall not on its own ... but when it is, we'll
			// treat as content
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.XML_CONTENT);
		} else if ((type == DOMRegionContext.XML_CHAR_REFERENCE) || (type == DOMRegionContext.XML_ENTITY_REFERENCE) || (type == DOMRegionContext.XML_PE_REFERENCE)) {
			// we may want to character and entity references to have it own
			// color in future,
			// but for now, we'll make attribute value
			return (TextAttribute) getTextAttributes().get(IStyleConstantsXML.TAG_ATTRIBUTE_VALUE);
		} else {
			// default, return null to signal "not handled"
			// in which case, other factories should be tried
			return null;
		}
	}

	protected IPreferenceStore getColorPreferences() {
		return XMLUIPlugin.getDefault().getPreferenceStore();
	}

	protected void handlePropertyChange(PropertyChangeEvent event) {
		String styleKey = null;

		if (event != null) {
			String prefKey = event.getProperty();
			// check if preference changed is a style preference
			if (IStyleConstantsXML.TAG_NAME.equals(prefKey)) {
				styleKey = IStyleConstantsXML.TAG_NAME;
			} else if (IStyleConstantsXML.TAG_BORDER.equals(prefKey)) {
				styleKey = IStyleConstantsXML.TAG_BORDER;
			} else if (IStyleConstantsXML.TAG_ATTRIBUTE_NAME.equals(prefKey)) {
				styleKey = IStyleConstantsXML.TAG_ATTRIBUTE_NAME;
			} else if (IStyleConstantsXML.TAG_ATTRIBUTE_VALUE.equals(prefKey)) {
				styleKey = IStyleConstantsXML.TAG_ATTRIBUTE_VALUE;
			} else if (IStyleConstantsXML.TAG_ATTRIBUTE_EQUALS.equals(prefKey)) {
				styleKey = IStyleConstantsXML.TAG_ATTRIBUTE_EQUALS;
			} else if (IStyleConstantsXML.COMMENT_BORDER.equals(prefKey)) {
				styleKey = IStyleConstantsXML.COMMENT_BORDER;
			} else if (IStyleConstantsXML.COMMENT_TEXT.equals(prefKey)) {
				styleKey = IStyleConstantsXML.COMMENT_TEXT;
			} else if (IStyleConstantsXML.CDATA_BORDER.equals(prefKey)) {
				styleKey = IStyleConstantsXML.CDATA_BORDER;
			} else if (IStyleConstantsXML.CDATA_TEXT.equals(prefKey)) {
				styleKey = IStyleConstantsXML.CDATA_TEXT;
			} else if (IStyleConstantsXML.DECL_BORDER.equals(prefKey)) {
				styleKey = IStyleConstantsXML.DECL_BORDER;
			} else if (IStyleConstantsXML.DOCTYPE_EXTERNAL_ID.equals(prefKey)) {
				styleKey = IStyleConstantsXML.DOCTYPE_EXTERNAL_ID;
			} else if (IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_PUBREF.equals(prefKey)) {
				styleKey = IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_PUBREF;
			} else if (IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_SYSREF.equals(prefKey)) {
				styleKey = IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_SYSREF;
			} else if (IStyleConstantsXML.DOCTYPE_NAME.equals(prefKey)) {
				styleKey = IStyleConstantsXML.DOCTYPE_NAME;
			} else if (IStyleConstantsXML.PI_CONTENT.equals(prefKey)) {
				styleKey = IStyleConstantsXML.PI_CONTENT;
			} else if (IStyleConstantsXML.PI_BORDER.equals(prefKey)) {
				styleKey = IStyleConstantsXML.PI_BORDER;
			} else if (IStyleConstantsXML.XML_CONTENT.equals(prefKey)) {
				styleKey = IStyleConstantsXML.XML_CONTENT;
			}
		}

		if (styleKey != null) {
			// overwrite style preference with new value
			addTextAttribute(styleKey);
			super.handlePropertyChange(event);
		}
	}

	protected void loadColors() {
		clearColors();

		addTextAttribute(IStyleConstantsXML.TAG_NAME);
		addTextAttribute(IStyleConstantsXML.TAG_BORDER);
		addTextAttribute(IStyleConstantsXML.TAG_ATTRIBUTE_NAME);
		addTextAttribute(IStyleConstantsXML.TAG_ATTRIBUTE_VALUE);
		addTextAttribute(IStyleConstantsXML.TAG_ATTRIBUTE_EQUALS);
		addTextAttribute(IStyleConstantsXML.COMMENT_BORDER);
		addTextAttribute(IStyleConstantsXML.COMMENT_TEXT);
		addTextAttribute(IStyleConstantsXML.CDATA_BORDER);
		addTextAttribute(IStyleConstantsXML.CDATA_TEXT);
		addTextAttribute(IStyleConstantsXML.DECL_BORDER);
		addTextAttribute(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID);
		addTextAttribute(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_PUBREF);
		addTextAttribute(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_SYSREF);
		addTextAttribute(IStyleConstantsXML.DOCTYPE_NAME);
		addTextAttribute(IStyleConstantsXML.PI_CONTENT);
		addTextAttribute(IStyleConstantsXML.PI_BORDER);
		addTextAttribute(IStyleConstantsXML.XML_CONTENT);
	}
}
