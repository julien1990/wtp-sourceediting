/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM - Initial API and implementation
 *   Jens Lukowski/Innoopract - initial renaming/restructuring
 *******************************************************************************/
package org.eclipse.wst.xml.ui.tests;

import junit.framework.TestCase;

import org.eclipse.wst.sse.ui.internal.ExtendedConfigurationBuilder;
import org.eclipse.wst.xml.core.internal.provisional.contenttype.ContentTypeIdForXML;
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML;
import org.eclipse.wst.xml.ui.views.contentoutline.XMLContentOutlineConfiguration;
import org.eclipse.wst.xml.ui.views.properties.XMLPropertySheetConfiguration;

/**
 * Tests retrieving editor contributions for xml content type
 */
public class TestEditorConfigurationXML extends TestCase {
	public void testGetSourceViewerConfiguration() {
		Object o = ExtendedConfigurationBuilder.getInstance().getConfiguration(ExtendedConfigurationBuilder.SOURCEVIEWERCONFIGURATION, ContentTypeIdForXML.ContentTypeID_XML);
		assertNotNull("no source viewer configuration for " + ContentTypeIdForXML.ContentTypeID_XML, o);
		// check for over-qualified subclasses
		assertEquals("unexpected source viewer configuration for " + ContentTypeIdForXML.ContentTypeID_XML, o.getClass(), StructuredTextViewerConfigurationXML.class);
	}

	public void testGetContentOutlineViewerConfiguration() {
		Object o = ExtendedConfigurationBuilder.getInstance().getConfiguration(ExtendedConfigurationBuilder.CONTENTOUTLINECONFIGURATION, ContentTypeIdForXML.ContentTypeID_XML);
		assertNotNull("no content outline viewer configuration for " + ContentTypeIdForXML.ContentTypeID_XML, o);
		// check for over-qualified subclasses
		assertEquals("unexpected content outline viewer configuration for " + ContentTypeIdForXML.ContentTypeID_XML, o.getClass(), XMLContentOutlineConfiguration.class);
	}

	public void testGetPropertySheetConfiguration() {
		Object o = ExtendedConfigurationBuilder.getInstance().getConfiguration(ExtendedConfigurationBuilder.PROPERTYSHEETCONFIGURATION, ContentTypeIdForXML.ContentTypeID_XML);
		assertNotNull("no property sheet configuration for " + ContentTypeIdForXML.ContentTypeID_XML, o);
		// check for over-qualified subclasses
		assertEquals("unexpected property sheet configuration for " + ContentTypeIdForXML.ContentTypeID_XML, o.getClass(), XMLPropertySheetConfiguration.class);
	}
}
