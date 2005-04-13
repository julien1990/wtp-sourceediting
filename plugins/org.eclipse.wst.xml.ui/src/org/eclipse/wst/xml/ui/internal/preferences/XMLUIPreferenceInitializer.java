package org.eclipse.wst.xml.ui.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wst.sse.ui.internal.preferences.ui.ColorHelper;
import org.eclipse.wst.xml.ui.internal.XMLUIPlugin;
import org.eclipse.wst.xml.ui.internal.style.IStyleConstantsXML;

/**
 * Sets default values for XML UI preferences
 */
public class XMLUIPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = XMLUIPlugin.getDefault().getPreferenceStore();
		
		store.setDefault(XMLUIPreferenceNames.AUTO_PROPOSE, true);
		store.setDefault(XMLUIPreferenceNames.AUTO_PROPOSE_CODE, "<"); //$NON-NLS-1$

		store.setDefault(
					XMLUIPreferenceNames.USE_INFERRED_GRAMMAR, true);

		// XML Style Preferences
		String NOBACKGROUNDBOLD = " | null | false"; //$NON-NLS-1$
		String styleValue = ColorHelper.getColorString(127, 0, 127)
				+ NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.TAG_ATTRIBUTE_NAME, styleValue);

		styleValue = ColorHelper.getColorString(42, 0, 255) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.TAG_ATTRIBUTE_VALUE, styleValue);

		styleValue = "null" + NOBACKGROUNDBOLD; //$NON-NLS-1$
		store.setDefault(IStyleConstantsXML.TAG_ATTRIBUTE_EQUALS, styleValue); // specified
		// value
		// is
		// black;
		// leaving
		// as
		// widget
		// default

		styleValue = ColorHelper.getColorString(63, 95, 191) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.COMMENT_BORDER, styleValue);
		store.setDefault(IStyleConstantsXML.COMMENT_TEXT, styleValue);

		styleValue = ColorHelper.getColorString(0, 128, 128) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.DECL_BORDER, styleValue);

		styleValue = ColorHelper.getColorString(0, 0, 128) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.DOCTYPE_NAME, styleValue);
		store.setDefault(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_PUBREF,
				styleValue);

		styleValue = ColorHelper.getColorString(128, 128, 128)
				+ NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID, styleValue);

		styleValue = ColorHelper.getColorString(63, 127, 95) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.DOCTYPE_EXTERNAL_ID_SYSREF,
				styleValue);

		styleValue = "null" + NOBACKGROUNDBOLD; //$NON-NLS-1$
		store.setDefault(IStyleConstantsXML.XML_CONTENT, styleValue); // specified
		// value
		// is
		// black;
		// leaving
		// as
		// widget
		// default

		styleValue = ColorHelper.getColorString(0, 128, 128) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.TAG_BORDER, styleValue);

		styleValue = ColorHelper.getColorString(63, 127, 127)
				+ NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.TAG_NAME, styleValue);

		styleValue = ColorHelper.getColorString(0, 128, 128) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.PI_BORDER, styleValue);

		styleValue = "null" + NOBACKGROUNDBOLD; //$NON-NLS-1$
		store.setDefault(IStyleConstantsXML.PI_CONTENT, styleValue); // specified
		// value
		// is
		// black;
		// leaving
		// as
		// widget
		// default

		styleValue = ColorHelper.getColorString(0, 128, 128) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.CDATA_BORDER, styleValue);

		styleValue = ColorHelper.getColorString(0, 0, 0) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.CDATA_TEXT, styleValue);
	}

}
