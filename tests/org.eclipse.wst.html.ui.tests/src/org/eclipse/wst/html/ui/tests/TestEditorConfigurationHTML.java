/*
 * Copyright (c) 2005 IBM Corporation and others.
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
package org.eclipse.wst.html.ui.tests;

import junit.framework.TestCase;

import org.eclipse.wst.html.core.internal.provisional.contenttype.ContentTypeIdForHTML;
import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML;
import org.eclipse.wst.html.ui.views.contentoutline.HTMLContentOutlineConfiguration;
import org.eclipse.wst.sse.ui.internal.ExtendedConfigurationBuilder;
import org.eclipse.wst.xml.ui.views.properties.XMLPropertySheetConfiguration;

/**
 * Tests retrieving editor contributions for html content type
 */
public class TestEditorConfigurationHTML extends TestCase {
	public void testGetSourceViewerConfiguration() {
		Object o = ExtendedConfigurationBuilder.getInstance().getConfiguration(ExtendedConfigurationBuilder.SOURCEVIEWERCONFIGURATION, ContentTypeIdForHTML.ContentTypeID_HTML);
		assertNotNull("no source viewer configuration for " + ContentTypeIdForHTML.ContentTypeID_HTML, o);
		// check for over-qualified subclasses
		assertEquals("unexpected source viewer configuration for " + ContentTypeIdForHTML.ContentTypeID_HTML, o.getClass(), StructuredTextViewerConfigurationHTML.class);
	}

	public void testGetContentOutlineViewerConfiguration() {
		Object o = ExtendedConfigurationBuilder.getInstance().getConfiguration(ExtendedConfigurationBuilder.CONTENTOUTLINECONFIGURATION, ContentTypeIdForHTML.ContentTypeID_HTML);
		assertNotNull("no content outline viewer configuration for " + ContentTypeIdForHTML.ContentTypeID_HTML, o);
		// check for over-qualified subclasses
		assertEquals("unexpected content outline viewer configuration for " + ContentTypeIdForHTML.ContentTypeID_HTML, o.getClass(), HTMLContentOutlineConfiguration.class);
	}

	public void testGetPropertySheetConfiguration() {
		Object o = ExtendedConfigurationBuilder.getInstance().getConfiguration(ExtendedConfigurationBuilder.PROPERTYSHEETCONFIGURATION, ContentTypeIdForHTML.ContentTypeID_HTML);
		assertNotNull("no property sheet configuration for " + ContentTypeIdForHTML.ContentTypeID_HTML, o);
		// check for over-qualified subclasses
		assertEquals("unexpected property sheet configuration for " + ContentTypeIdForHTML.ContentTypeID_HTML, o.getClass(), XMLPropertySheetConfiguration.class);
	}
}
