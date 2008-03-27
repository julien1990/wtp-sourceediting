/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla, b.muskalla@gmx.net - [158660] character entities should have their own syntax highlighting preference      
 *     
 *******************************************************************************/
package org.eclipse.wst.html.ui.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.wst.html.ui.internal.HTMLUIPlugin;
import org.eclipse.wst.html.ui.internal.style.IStyleConstantsHTML;
import org.eclipse.wst.sse.ui.internal.preferences.ui.ColorHelper;
import org.eclipse.wst.xml.ui.internal.style.IStyleConstantsXML;

/**
 * Sets default values for HTML UI preferences
 */
public class HTMLUIPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = HTMLUIPlugin.getDefault().getPreferenceStore();

		store.setDefault(HTMLUIPreferenceNames.AUTO_PROPOSE, true);
		store.setDefault(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE, "<=");//$NON-NLS-1$

		// HTML Style Preferences
		String NOBACKGROUNDBOLD = " | null | false"; //$NON-NLS-1$
		String JUSTITALIC = " | null | false | true"; //$NON-NLS-1$
		String styleValue = ColorHelper.getColorString(127, 0, 127) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.TAG_ATTRIBUTE_NAME, styleValue);

		styleValue = ColorHelper.getColorString(42, 0, 255) + JUSTITALIC;
		store.setDefault(IStyleConstantsXML.TAG_ATTRIBUTE_VALUE, styleValue);

		styleValue = "null" + NOBACKGROUNDBOLD; //$NON-NLS-1$
		store.setDefault(IStyleConstantsXML.TAG_ATTRIBUTE_EQUALS, styleValue); // specified value is black; leaving as widget default

		styleValue = ColorHelper.getColorString(63, 95, 191) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.COMMENT_BORDER, styleValue);
		store.setDefault(IStyleConstantsXML.COMMENT_TEXT, styleValue);

		styleValue = ColorHelper.getColorString(0, 128, 128) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.DECL_BORDER, styleValue);

		styleValue = ColorHelper.getColorString(0, 0, 128) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.DOCTYPE_NAME, styleValue);
		store.setDefault(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_PUBREF, styleValue);

		styleValue = ColorHelper.getColorString(128, 128, 128) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID, styleValue);

		styleValue = ColorHelper.getColorString(63, 127, 95) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_SYSREF, styleValue);

		styleValue = "null" + NOBACKGROUNDBOLD; //$NON-NLS-1$
		store.setDefault(IStyleConstantsXML.XML_CONTENT, styleValue);	// specified value is black; leaving as widget default

		styleValue = ColorHelper.getColorString(0, 128, 128) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.TAG_BORDER, styleValue);

		styleValue = ColorHelper.getColorString(63, 127, 127) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.TAG_NAME, styleValue);

		styleValue = ColorHelper.getColorString(0, 128, 128) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.PI_BORDER, styleValue);

		styleValue = "null" + NOBACKGROUNDBOLD; //$NON-NLS-1$
		store.setDefault(IStyleConstantsXML.PI_CONTENT, styleValue);	// specified value is black; leaving as widget default

		styleValue = ColorHelper.getColorString(0, 128, 128) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.CDATA_BORDER, styleValue);

		styleValue = ColorHelper.getColorString(0, 0, 0) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.CDATA_TEXT, styleValue);

		styleValue = ColorHelper.getColorString(191, 95, 63) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsHTML.SCRIPT_AREA_BORDER, styleValue);

		styleValue = ColorHelper.getColorString(42, 0, 255) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.ENTITY_REFERENCE, styleValue);

		// set default new html file template to use in new file wizard
		/*
		 * Need to find template name that goes with default template id (name
		 * may change for differnt language)
		 */
		String templateName = ""; //$NON-NLS-1$
		Template template = HTMLUIPlugin.getDefault().getTemplateStore().findTemplateById("org.eclipse.wst.html.ui.templates.html"); //$NON-NLS-1$
		if (template != null)
			templateName = template.getName();
		store.setDefault(HTMLUIPreferenceNames.NEW_FILE_TEMPLATE_NAME, templateName);
		
		// Defaults for the Typing preference page
		store.setDefault(HTMLUIPreferenceNames.TYPING_COMPLETE_COMMENTS, true);
		store.setDefault(HTMLUIPreferenceNames.TYPING_COMPLETE_END_TAGS, true);
		store.setDefault(HTMLUIPreferenceNames.TYPING_REMOVE_END_TAGS, true);
	}

}
