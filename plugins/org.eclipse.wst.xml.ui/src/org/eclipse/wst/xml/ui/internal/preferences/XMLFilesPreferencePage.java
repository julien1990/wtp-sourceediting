/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
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
package org.eclipse.wst.xml.ui.internal.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.internal.encoding.CommonEncodingPreferenceNames;
import org.eclipse.wst.sse.ui.internal.preferences.ui.AbstractPreferencePage;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.preferences.XMLCorePreferenceNames;
import org.eclipse.wst.xml.core.internal.provisional.contenttype.ContentTypeIdForXML;
import org.eclipse.wst.xml.ui.internal.XMLUIMessages;
import org.eclipse.wst.xml.ui.internal.editor.IHelpContextIds;

public class XMLFilesPreferencePage extends AbstractPreferencePage {
	protected EncodingSettings fEncodingSettings = null;

	protected Combo fEndOfLineCode = null;
	private Vector fEOLCodes = null;
	private Text fDefaultSuffix = null;
	private List fValidExtensions = null;
	private Button fWarnNoGrammar = null;

	protected Control createContents(Composite parent) {
		Composite composite = (Composite) super.createContents(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.XML_PREFWEBX_FILES_HELPID);
		createContentsForCreatingOrSavingGroup(composite);
		createContentsForCreatingGroup(composite);
		createContentsForValidatingGroup(composite);

		setSize(composite);
		loadPreferences();

		return composite;
	}

	protected void createContentsForCreatingGroup(Composite parent) {
		Group creatingGroup = createGroup(parent, 2);
		creatingGroup.setText(XMLUIMessages.Creating_files);

		// Default extension for New file Wizard
		createLabel(creatingGroup, XMLUIMessages.XMLFilesPreferencePage_ExtensionLabel);
		fDefaultSuffix = createTextField(creatingGroup);
		fDefaultSuffix.addModifyListener(this);

		Label label = createLabel(creatingGroup, XMLUIMessages.Encoding_desc);
		((GridData) label.getLayoutData()).horizontalSpan = 2;
		fEncodingSettings = new EncodingSettings(creatingGroup, XMLUIMessages.Encoding);
		((GridData) fEncodingSettings.getLayoutData()).horizontalSpan = 2;
	}

	protected void createContentsForCreatingOrSavingGroup(Composite parent) {
		Group creatingOrSavingGroup = createGroup(parent, 2);
		creatingOrSavingGroup.setText(XMLUIMessages.Creating_or_saving_files);

		Label label = createLabel(creatingOrSavingGroup, XMLUIMessages.End_of_line_code_desc);
		((GridData) label.getLayoutData()).horizontalSpan = 2;
		((GridData) label.getLayoutData()).grabExcessHorizontalSpace = true;

		createLabel(creatingOrSavingGroup, XMLUIMessages.End_of_line_code);
		fEndOfLineCode = createDropDownBox(creatingOrSavingGroup);
		populateLineDelimiters();
	}

	protected void createContentsForValidatingGroup(Composite parent) {
		Group validatingGroup = createGroup(parent, 2);
		validatingGroup.setText(XMLUIMessages.Validating_files);

		if (fWarnNoGrammar == null) {
			fWarnNoGrammar = createCheckBox(validatingGroup, XMLUIMessages.Warn_no_grammar_specified);
		}
	}

	public void dispose() {
		fDefaultSuffix.removeModifyListener(this);
		super.dispose();
	}

	protected void doSavePreferenceStore() {
		XMLCorePlugin.getDefault().savePluginPreferences(); // model
	}

	/**
	 * Get content type associated with this new file wizard
	 * 
	 * @return IContentType
	 */
	protected IContentType getContentType() {
		return Platform.getContentTypeManager().getContentType(ContentTypeIdForXML.ContentTypeID_XML);
	}

	/**
	 * Get list of valid extensions
	 * 
	 * @return List
	 */
	private List getValidExtensions() {
		if (fValidExtensions == null) {
			IContentType type = getContentType();
			fValidExtensions = new ArrayList(Arrays.asList(type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));
		}
		return fValidExtensions;
	}

	/**
	 * Return the currently selected line delimiter preference
	 * 
	 * @return a line delimiter constant from CommonEncodingPreferenceNames
	 */
	private String getCurrentEOLCode() {
		int i = fEndOfLineCode.getSelectionIndex();
		if (i >= 0) {
			return (String) (fEOLCodes.elementAt(i));
		}
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.sse.ui.preferences.ui.AbstractPreferencePage#getModelPreferences()
	 */
	protected Preferences getModelPreferences() {
		return XMLCorePlugin.getDefault().getPluginPreferences();
	}

	protected void initializeValues() {
		initializeValuesForCreatingOrSavingGroup();
		initializeValuesForCreatingGroup();
		initializeValuesForValidatingGroup();
	}

	protected void initializeValuesForCreatingGroup() {
		String suffix = getModelPreferences().getString(XMLCorePreferenceNames.DEFAULT_EXTENSION);
		fDefaultSuffix.setText(suffix);

		String encoding = getModelPreferences().getString(CommonEncodingPreferenceNames.OUTPUT_CODESET);

		fEncodingSettings.setIANATag(encoding);
	}

	protected void initializeValuesForCreatingOrSavingGroup() {
		String endOfLineCode = getModelPreferences().getString(CommonEncodingPreferenceNames.END_OF_LINE_CODE);

		if (endOfLineCode.length() > 0) {
			setCurrentEOLCode(endOfLineCode);
		}
		else {
			setCurrentEOLCode(CommonEncodingPreferenceNames.NO_TRANSLATION);
		}
	}

	protected void initializeValuesForValidatingGroup() {
		boolean warnNoGrammarButtonSelected = getModelPreferences().getBoolean(XMLCorePreferenceNames.WARN_NO_GRAMMAR);


		if (fWarnNoGrammar != null) {
			fWarnNoGrammar.setSelection(warnNoGrammarButtonSelected);
		}
	}

	protected void performDefaults() {
		performDefaultsForCreatingOrSavingGroup();
		performDefaultsForCreatingGroup();
		performDefaultsForValidatingGroup();

		super.performDefaults();
	}

	protected void performDefaultsForCreatingGroup() {
		String suffix = getModelPreferences().getDefaultString(XMLCorePreferenceNames.DEFAULT_EXTENSION);
		fDefaultSuffix.setText(suffix);

		String encoding = getModelPreferences().getDefaultString(CommonEncodingPreferenceNames.OUTPUT_CODESET);

		fEncodingSettings.setIANATag(encoding);
		// fEncodingSettings.resetToDefaultEncoding();
	}

	protected void performDefaultsForCreatingOrSavingGroup() {
		String endOfLineCode = getModelPreferences().getDefaultString(CommonEncodingPreferenceNames.END_OF_LINE_CODE);

		if (endOfLineCode.length() > 0) {
			setCurrentEOLCode(endOfLineCode);
		}
		else {
			setCurrentEOLCode(CommonEncodingPreferenceNames.NO_TRANSLATION);
		}
	}

	protected void performDefaultsForValidatingGroup() {
		boolean warnNoGrammarButtonSelected = getModelPreferences().getDefaultBoolean(XMLCorePreferenceNames.WARN_NO_GRAMMAR);

		if (fWarnNoGrammar != null) {
			fWarnNoGrammar.setSelection(warnNoGrammarButtonSelected);
		}
	}

	public boolean performOk() {
		boolean result = super.performOk();

		doSavePreferenceStore();

		return result;
	}

	/**
	 * Populates the vector containing the line delimiter to display string
	 * mapping and the combobox displaying line delimiters
	 */
	private void populateLineDelimiters() {
		fEOLCodes = new Vector();
		fEndOfLineCode.add(XMLUIMessages.EOL_Unix);
		fEOLCodes.add(CommonEncodingPreferenceNames.LF);

		fEndOfLineCode.add(XMLUIMessages.EOL_Mac);
		fEOLCodes.add(CommonEncodingPreferenceNames.CR);

		fEndOfLineCode.add(XMLUIMessages.EOL_Windows);
		fEOLCodes.add(CommonEncodingPreferenceNames.CRLF);

		fEndOfLineCode.add(XMLUIMessages.EOL_NoTranslation);
		fEOLCodes.add(CommonEncodingPreferenceNames.NO_TRANSLATION);
	}

	/**
	 * Select the line delimiter in the eol combobox
	 * 
	 */
	private void setCurrentEOLCode(String eolCode) {
		// Clear the current selection.
		fEndOfLineCode.clearSelection();
		fEndOfLineCode.deselectAll();

		int i = fEOLCodes.indexOf(eolCode);
		if (i >= 0) {
			fEndOfLineCode.select(i);
		}
	}

	protected void storeValues() {
		storeValuesForCreatingOrSavingGroup();
		storeValuesForCreatingGroup();
		storeValuesForValidatingGroup();
	}

	protected void storeValuesForCreatingGroup() {
		String suffix = fDefaultSuffix.getText();
		getModelPreferences().setValue(XMLCorePreferenceNames.DEFAULT_EXTENSION, suffix);

		getModelPreferences().setValue(CommonEncodingPreferenceNames.OUTPUT_CODESET, fEncodingSettings.getIANATag());
	}

	protected void storeValuesForCreatingOrSavingGroup() {
		String eolCode = getCurrentEOLCode();
		getModelPreferences().setValue(CommonEncodingPreferenceNames.END_OF_LINE_CODE, eolCode);
	}

	protected void storeValuesForValidatingGroup() {
		if (fWarnNoGrammar != null) {
			boolean warnNoGrammarButtonSelected = fWarnNoGrammar.getSelection();
			getModelPreferences().setValue(XMLCorePreferenceNames.WARN_NO_GRAMMAR, warnNoGrammarButtonSelected);
		}
	}

	protected void validateValues() {
		boolean isValid = false;
		Iterator i = getValidExtensions().iterator();
		while (i.hasNext() && !isValid) {
			String extension = (String) i.next();
			isValid = extension.equalsIgnoreCase(fDefaultSuffix.getText());
		}

		if (!isValid) {
			setErrorMessage(NLS.bind(XMLUIMessages.XMLFilesPreferencePage_ExtensionError, getValidExtensions().toString()));
			setValid(false);
		}
		else {
			setErrorMessage(null);
			setValid(true);
		}
	}
}
