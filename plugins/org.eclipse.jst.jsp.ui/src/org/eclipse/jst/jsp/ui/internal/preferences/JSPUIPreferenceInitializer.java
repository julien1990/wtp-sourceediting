package org.eclipse.jst.jsp.ui.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.jsp.ui.internal.JSPUIPlugin;
import org.eclipse.wst.html.ui.internal.style.IStyleConstantsHTML;
import org.eclipse.wst.sse.ui.preferences.ui.ColorHelper;
import org.eclipse.wst.xml.ui.internal.style.IStyleConstantsXML;

/**
 * Sets default values for JSP UI preferences
 */
public class JSPUIPreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = JSPUIPlugin.getDefault().getPreferenceStore();
		
		// setting the same as HTML
		store.setDefault(JSPUIPreferenceNames.AUTO_PROPOSE, true);
		store.setDefault(JSPUIPreferenceNames.AUTO_PROPOSE_CODE, "<");//$NON-NLS-1$
		
		// JSP Style Preferences
		String NOBACKGROUNDBOLD = " | null | false";   //$NON-NLS-1$
		String styleValue = ColorHelper.getColorString(127, 0, 127) + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.TAG_ATTRIBUTE_NAME, styleValue);
		
		styleValue = ColorHelper.getColorString(42, 0, 255)  + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.TAG_ATTRIBUTE_VALUE, styleValue);
		
		styleValue = "null" + NOBACKGROUNDBOLD;  //$NON-NLS-1$
		store.setDefault(IStyleConstantsXML.TAG_ATTRIBUTE_EQUALS, styleValue); // specified value is black; leaving as widget default
		
		styleValue = ColorHelper.getColorString(63, 95, 191)  + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.COMMENT_BORDER, styleValue);
		store.setDefault(IStyleConstantsXML.COMMENT_TEXT, styleValue);
		
		styleValue = ColorHelper.getColorString(0, 128, 128)  + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.TAG_BORDER, styleValue);
		
		styleValue = ColorHelper.getColorString(63, 127, 127)  + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsXML.TAG_NAME, styleValue);
		
		styleValue = ColorHelper.getColorString(191, 95, 63)  + NOBACKGROUNDBOLD;
		store.setDefault(IStyleConstantsHTML.SCRIPT_AREA_BORDER, styleValue);
	}

}
