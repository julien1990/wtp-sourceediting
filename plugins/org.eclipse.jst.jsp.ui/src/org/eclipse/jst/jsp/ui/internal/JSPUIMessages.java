/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.jst.jsp.ui.internal;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Strings used by JSP UI
 * 
 * @since 1.0
 */
public class JSPUIMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jst.jsp.ui.internal.JSPUIPluginResources";//$NON-NLS-1$
	private static ResourceBundle fResourceBundle;

	public static String Sample_JSP_doc;
	public static String JSP_Delimiters_UI_;
	public static String Refactor_label;
	public static String RenameElement_label; // resource bundle
	public static String MoveElement_label; // resource bundle
	public static String OK;
	public static String JSP_changes;
	public static String ActionContributorJSP_0;
	public static String JSPRenameElementAction_0;
	public static String JSPMoveElementAction_0;
	public static String BasicRefactorSearchRequestor_0;
	public static String BasicRefactorSearchRequestor_1;
	public static String BasicRefactorSearchRequestor_2;
	public static String BasicRefactorSearchRequestor_3;
	public static String BasicRefactorSearchRequestor_4;
	public static String BasicRefactorSearchRequestor_5;
	public static String BasicRefactorSearchRequestor_6;
	public static String _UI_WIZARD_NEW_TITLE;
	public static String _UI_WIZARD_NEW_HEADING;
	public static String _UI_WIZARD_NEW_DESCRIPTION;
	public static String _ERROR_FILENAME_MUST_END_JSP;
	public static String NewJSPTemplatesWizardPage_0;
	public static String NewJSPTemplatesWizardPage_1;
	public static String NewJSPTemplatesWizardPage_2;
	public static String NewJSPTemplatesWizardPage_3;
	public static String NewJSPTemplatesWizardPage_4;
	public static String NewJSPTemplatesWizardPage_5;
	public static String NewJSPTemplatesWizardPage_6;
	public static String ToggleComment_label; // resource bundle
	public static String ToggleComment_tooltip; // resource bundle
	public static String ToggleComment_description; // resource bundle
	public static String AddBlockComment_label; // resource bundle
	public static String AddBlockComment_tooltip; // resource bundle
	public static String AddBlockComment_description; // resource bundle
	public static String RemoveBlockComment_label; // resource bundle
	public static String RemoveBlockComment_tooltip; // resource bundle
	public static String RemoveBlockComment_description; // resource bundle
	public static String CleanupDocument_label; // resource bundle
	public static String CleanupDocument_tooltip; // resource bundle
	public static String CleanupDocument_description; // resource bundle
	public static String FindOccurrences_label;	// resource bundle
	public static String OccurrencesSearchQuery_0;
	public static String OccurrencesSearchQuery_2;
	public static String Override_method_in;
	public static String Creating_files_encoding;
	public static String Content_Assist_not_availab_UI_;
	public static String Java_Content_Assist_is_not_UI_;
	public static String JSPSourcePreferencePage_0;
	public static String JSPSourcePreferencePage_1;
	public static String JSPSourcePreferencePage_2;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, JSPUIMessages.class);
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
