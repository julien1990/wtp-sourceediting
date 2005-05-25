/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.wst.html.ui.internal;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Strings used by HTML UI
 * 
 * @since 1.0
 */
public class HTMLUIMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.wst.html.ui.internal.HTMLUIPluginResources";//$NON-NLS-1$
	private static ResourceBundle fResourceBundle;

	public static String Sample_HTML_doc;
	public static String HTMLFilesPreferencePage_0;
	public static String _UI_WIZARD_NEW_TITLE;
	public static String _UI_WIZARD_NEW_HEADING;
	public static String _UI_WIZARD_NEW_DESCRIPTION;
	public static String _ERROR_FILENAME_MUST_END_HTML;
	public static String NewHTMLTemplatesWizardPage_0;
	public static String NewHTMLTemplatesWizardPage_1;
	public static String NewHTMLTemplatesWizardPage_2;
	public static String NewHTMLTemplatesWizardPage_3;
	public static String NewHTMLTemplatesWizardPage_4;
	public static String NewHTMLTemplatesWizardPage_5;
	public static String NewHTMLTemplatesWizardPage_6;
	public static String Creating_files_encoding;
	public static String MESSAGE_HTML_VALIDATION_MESSAGE_UI_;
	public static String CleanupDocument_label; // resource bundle
	public static String CleanupDocument_tooltip; // resource bundle
	public static String CleanupDocument_description; // resource bundle
	public static String ToggleComment_label; // resource bundle
	public static String ToggleComment_tooltip; // resource bundle
	public static String ToggleComment_description; // resource bundle
	public static String AddBlockComment_label; // resource bundle
	public static String AddBlockComment_tooltip; // resource bundle
	public static String AddBlockComment_description; // resource bundle
	public static String RemoveBlockComment_label; // resource bundle
	public static String RemoveBlockComment_tooltip; // resource bundle
	public static String RemoveBlockComment_description; // resource bundle
	public static String FindOccurrences_label;	// resource bundle
	public static String Creating_files;
	public static String Encoding_desc;
	public static String UI_Description_of_role_of_following_DOCTYPE;
	public static String UI_Public_ID;
	public static String UI_System_ID;
	public static String UI_none;
	public static String Preferred_markup_case_UI_;
	public static String Tag_names__UI_;
	public static String Tag_names_Upper_case_UI_;
	public static String Tag_names_Lower_case_UI_;
	public static String Attribute_names__UI_;
	public static String Attribute_names_Upper_case_UI_;
	public static String Attribute_names_Lower_case_UI_;
	public static String Cleanup_UI_;
	public static String Tag_name_case_for_HTML_UI_;
	public static String Tag_name_case_As_is_UI_;
	public static String Tag_name_case_Lower_UI_;
	public static String Tag_name_case_Upper_UI_;
	public static String Attribute_name_case_for_HTML_UI_;
	public static String Attribute_name_case_As_is_UI_;
	public static String Attribute_name_case_Lower_UI_;
	public static String Attribute_name_case_Upper_UI_;
	public static String Insert_required_attributes_UI_;
	public static String Insert_missing_tags_UI_;
	public static String Quote_attribute_values_UI_;
	public static String Format_source_UI_;
	public static String Convert_EOL_codes_UI_;
	public static String EOL_Windows_UI;
	public static String EOL_Unix_UI;
	public static String EOL_Mac_UI;

	// below are possibly unused strings that may be deleted
	public static String HTMLFilesPreferencePage_1;
	public static String HTMLFilesPreferencePage_2;
	public static String HTMLFilesPreferencePage_3;
	// above are possibly unused strings that may be deleted
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, HTMLUIMessages.class);
	}
	
	private HTMLUIMessages() {
		// cannot create new instance of this class
	}
	
	public static ResourceBundle getResourceBundle() {
		try {
			if (fResourceBundle == null)
				fResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
		}
		catch (MissingResourceException x) {
			fResourceBundle = null;
		}
		return fResourceBundle;
	}
}
