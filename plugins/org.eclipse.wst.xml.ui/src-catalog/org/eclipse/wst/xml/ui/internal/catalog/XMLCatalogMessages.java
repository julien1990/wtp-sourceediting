/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.wst.xml.ui.internal.catalog;

import org.eclipse.osgi.util.NLS;

/**
 * Strings used by XML Editor
 * 
 * @since 1.0
 */
public class XMLCatalogMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.wst.xml.ui.internal.catalog.XMLCatalogResources";//$NON-NLS-1$
	
	public static String UI_WIZARD_SCHEMA_AND_NAME_SPACE_INFO;
	public static String UI_LABEL_NAME_SPACE_PREFIX;
    public static String UI_LABEL_NAME_SPACE_URI;
	public static String UI_LABEL_XSD_LOCATION;
	public static String UI_LABEL_DOCTYPE_INFORMATION;
	public static String UI_LABEL_SYSTEM_ID;
	public static String UI_LABEL_PUBLIC_ID;
	public static String UI_LABEL_SELECT_FILE;
	public static String UI_LABEL_KEY;
	public static String UI_LABEL_KEY_COLON;
	public static String UI_LABEL_DETAILS_KEY_COLON;
	public static String UI_LABEL_URI;
	public static String UI_LABEL_URI_COLON;
	public static String UI_LABEL_CATALOG_URI_COLON;
	public static String UI_LABEL_DETAILS_URI_COLON;
	public static String UI_KEY_TYPE_COLON;
	public static String UI_KEY_TYPE_DETAILS_COLON;
	public static String UI_KEY_TYPE_DESCRIPTION_XSD_PUBLIC;
	public static String UI_KEY_TYPE_DESCRIPTION_XSD_SYSTEM;
	public static String UI_KEY_TYPE_DESCRIPTION_DTD_PUBLIC;
	public static String UI_KEY_TYPE_DESCRIPTION_DTD_SYSTEM;
	public static String UI_KEY_TYPE_DESCRIPTION_URI;
	public static String UI_LABEL_SPECIFY_ALTERNATIVE_WEB_URL;
	public static String UI_WARNING_URI_MUST_NOT_HAVE_DOTS;

	public static String UI_WARNING_URI_NOT_FOUND_COLON;
	public static String UI_WARNING_URI_NOT_FOUND_LONG;
	public static String UI_WARNING_INVALID_FILE;
	public static String UI_WARNING_INVALID_FILE_LONG;
	public static String UI_WARNING_NO_ELEMENT;
	public static String UI_WARNING_NO_ELEMENT_DTD_LONG;
	public static String UI_WARNING_NO_ELEMENT_XSD_LONG;


	//NewModelWizard
	public static String UI_INVALID_GRAMMAR_ERROR;
	public static String UI_BUTTON_BROWSE;

	//XMLCatalogIdMappingPage
	public static String UI_LABEL_MAP_TO;
	public static String UI_LABEL_MAP_FROM ;
	public static String UI_BUTTON_NEW;
	public static String UI_BUTTON_EDIT;
	public static String UI_BUTTON_DELETE;
	public static String UI_LABEL_NEW_DIALOG_TITLE;
	public static String UI_LABEL_EDIT_DIALOG_TITLE;


	// XMLCatalogPreferencePage
	public static String UI_LABEL_USER_ENTRIES;
	public static String UI_LABEL_USER_ENTRIES_TOOL_TIP;
	public static String UI_LABEL_SYSTEM_ENTRIES;
	public static String UI_LABEL_SYSTEM_ENTRIES_TOOL_TIP;
	public static String UI_BUTTON_CHANGE;
	public static String UI_LABEL_SELECT_PROJECT;
	public static String UI_LABEL_SPECIFY_PROJECT_DESCRIPTION;
	public static String UI_WARNING_NO_PROJECTS_CREATED;
	public static String UI_WARNING_PROJECT_NOT_SPECIFIED ;
	public static String UI_WARNING_PROJECT_DOES_NOT_EXIST ;
	public static String UI_LABEL_PROJECT_TO_USE ;
	public static String UI_LABEL_CATALOG_SAVE_ERROR;
	public static String UI_LABEL_CATALOG_COULD_NOT_BE_SAVED;
	public static String UI_LABEL_ADVANCED;
	public static String UI_LABEL_ADVANCED_XML_CATALOG_PREFS;   
	public static String UI_LABEL_SPECIFY_PERSISTENCE_FILE;
	public static String UI_LABEL_SAVE_CATALOG_DIALOG_TITLE;
	public static String UI_LABEL_SAVE_CATALOG_DIALOG_DESC;   
	public static String UI_LABEL_BROWSE_CATALOG_FILE_TITLE;
	public static String UI_LABEL_BROWSE_CATALOG_FILE_DESC;   
	public static String UI_LABEL_DETAILS;
	public static String UI_LABEL_USER_SPECIFIED_ENTRIES;
	public static String UI_LABEL_PLUGIN_SPECIFIED_ENTRIES;

	// AdvancedOptionsDialog
	public static String UI_BUTTON_IMPORT;
	public static String UI_BUTTON_EXPORT;
	                                   
	public static String UI_LABEL_IMPORT_DIALOG_TITLE;
	public static String UI_LABEL_IMPORT_DIALOG_HEADING;
	public static String UI_LABEL_IMPORT_DIALOG_MESSAGE;

	public static String UI_LABEL_EXPORT_DIALOG_TITLE;
	public static String UI_LABEL_EXPORT_DIALOG_HEADING;
	public static String UI_LABEL_EXPORT_DIALOG_MESSAGE;
	public static String ERROR_SAVING_FILE;
	public static String UI_LABEL_FILE_IS_READ_ONLY;
	public static String UI_LABEL_DIALOG_DESCRIPTION;

	public static String EditCatalogEntryDialog_catalogEntryLabel;
	public static String EditCatalogEntryDialog_nextCatalogLabel;
	public static String UI_BUTTON_MENU_BROWSE_WORKSPACE;
	public static String UI_BUTTON_MENU_BROWSE_FILE_SYSTEM;
	public static String UI_LABEL_SELECT_FILE_FILTER_CONTROL;
	public static String UI_TEXT_SELECT_FILE_FILTER_CONTROL;
	
	


	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, XMLCatalogMessages.class);
	}
	
	private XMLCatalogMessages() {
		// cannot create new instance
	}
}
