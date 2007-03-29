/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.html.ui.internal.preferences.ui;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.html.core.internal.HTMLCorePlugin;
import org.eclipse.wst.html.core.internal.preferences.HTMLCorePreferenceNames;
import org.eclipse.wst.html.ui.internal.HTMLUIMessages;
import org.eclipse.wst.html.ui.internal.HTMLUIPlugin;
import org.eclipse.wst.html.ui.internal.editor.IHelpContextIds;
import org.eclipse.wst.html.ui.internal.preferences.HTMLUIPreferenceNames;
import org.eclipse.wst.sse.ui.internal.preferences.ui.AbstractPreferencePage;

public class HTMLSourcePreferencePage extends AbstractPreferencePage {
	private Button fTagNameUpper = null;
	private Button fTagNameLower = null;
	private Button fAttrNameUpper = null;
	private Button fAttrNameLower = null;

	private final int MIN_INDENTATION_SIZE = 0;
	private final int MAX_INDENTATION_SIZE = 16;

	// Content Assist
	private Button fAutoPropose;
	private Label fAutoProposeLabel;
	private Text fAutoProposeText;
	private Button fClearAllBlankLines;

	// Formatting
	private Text fLineWidthText;
	private Button fSplitMultiAttrs;
	private Button fAlignEndBracket;
	private Button fIndentUsingTabs;
	private Button fIndentUsingSpaces;
	private Spinner fIndentationSize;

	private void createContentsForContentAssistGroup(Composite parent) {
		Group contentAssistGroup = createGroup(parent, 2);
		contentAssistGroup.setText(HTMLUIMessages.Content_assist_UI_);

		fAutoPropose = createCheckBox(contentAssistGroup, HTMLUIMessages.Automatically_make_suggest_UI_);
		((GridData) fAutoPropose.getLayoutData()).horizontalSpan = 2;
		fAutoPropose.addSelectionListener(this);

		fAutoProposeLabel = createLabel(contentAssistGroup, HTMLUIMessages.Prompt_when_these_characte_UI_);
		fAutoProposeText = createTextField(contentAssistGroup);

		// add one more group of preferences
		createContentsForPreferredCaseGroup(parent, 2);
	}

	private Composite createContentsForPreferredCaseGroup(Composite parent, int columnSpan) {
		Group caseGroup = createGroup(parent, columnSpan);
		caseGroup.setText(HTMLUIMessages.Preferred_markup_case_UI_);

		// d257064 need to associate group w/ radio buttons so radio buttons
		// header can be read
		Group tagNameGroup = createGroup(caseGroup, 1);
		tagNameGroup.setText(HTMLUIMessages.Tag_names__UI_);
		fTagNameUpper = createRadioButton(tagNameGroup, HTMLUIMessages.Tag_names_Upper_case_UI_);
		fTagNameLower = createRadioButton(tagNameGroup, HTMLUIMessages.Tag_names_Lower_case_UI_);

		// d257064 need to associate group w/ radio buttons so radio buttons
		// header can be read
		Group attrNameGroup = createGroup(caseGroup, 1);
		attrNameGroup.setText(HTMLUIMessages.Attribute_names__UI_);
		fAttrNameUpper = createRadioButton(attrNameGroup, HTMLUIMessages.Attribute_names_Upper_case_UI_);
		fAttrNameLower = createRadioButton(attrNameGroup, HTMLUIMessages.Attribute_names_Lower_case_UI_);

		return parent;

	}

	private void createContentsForFormattingGroup(Composite parent) {
		Group formattingGroup = createGroup(parent, 2);
		formattingGroup.setText(HTMLUIMessages.Formatting_UI_);

		createLabel(formattingGroup, HTMLUIMessages.Line_width__UI_);
		fLineWidthText = new Text(formattingGroup, SWT.SINGLE | SWT.BORDER);
		GridData gData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.BEGINNING);
		gData.widthHint = 25;
		fLineWidthText.setLayoutData(gData);
		fLineWidthText.addModifyListener(this);

		fSplitMultiAttrs = createCheckBox(formattingGroup, HTMLUIMessages.Split_multiple_attributes);
		((GridData) fSplitMultiAttrs.getLayoutData()).horizontalSpan = 2;
		fAlignEndBracket = createCheckBox(formattingGroup, HTMLUIMessages.Align_final_bracket);
		((GridData) fAlignEndBracket.getLayoutData()).horizontalSpan = 2;
		fClearAllBlankLines = createCheckBox(formattingGroup, HTMLUIMessages.Clear_all_blank_lines_UI_);
		((GridData) fClearAllBlankLines.getLayoutData()).horizontalSpan = 2;

		fIndentUsingTabs = createRadioButton(formattingGroup, HTMLUIMessages.Indent_using_tabs);
		((GridData) fIndentUsingTabs.getLayoutData()).horizontalSpan = 2;

		fIndentUsingSpaces = createRadioButton(formattingGroup, HTMLUIMessages.Indent_using_spaces);
		((GridData) fIndentUsingSpaces.getLayoutData()).horizontalSpan = 2;

		createLabel(formattingGroup, HTMLUIMessages.Indentation_size);
		fIndentationSize = new Spinner(formattingGroup, SWT.READ_ONLY | SWT.BORDER);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		fIndentationSize.setLayoutData(gd);
		fIndentationSize.setToolTipText(HTMLUIMessages.Indentation_size_tip);
		fIndentationSize.setMinimum(MIN_INDENTATION_SIZE);
		fIndentationSize.setMaximum(MAX_INDENTATION_SIZE);
		fIndentationSize.setIncrement(1);
		fIndentationSize.setPageIncrement(4);
		fIndentationSize.addModifyListener(this);
	}

	protected void performDefaults() {
		fTagNameUpper.setSelection(getModelPreferences().getDefaultInt(HTMLCorePreferenceNames.TAG_NAME_CASE) == HTMLCorePreferenceNames.UPPER);
		fTagNameLower.setSelection(getModelPreferences().getDefaultInt(HTMLCorePreferenceNames.TAG_NAME_CASE) == HTMLCorePreferenceNames.LOWER);
		fAttrNameUpper.setSelection(getModelPreferences().getDefaultInt(HTMLCorePreferenceNames.ATTR_NAME_CASE) == HTMLCorePreferenceNames.UPPER);
		fAttrNameLower.setSelection(getModelPreferences().getDefaultInt(HTMLCorePreferenceNames.ATTR_NAME_CASE) == HTMLCorePreferenceNames.LOWER);

		performDefaultsForFormattingGroup();
		performDefaultsForContentAssistGroup();

		validateValues();
		enableValues();

		super.performDefaults();
	}

	private void performDefaultsForContentAssistGroup() {
		// Content Assist
		fAutoPropose.setSelection(getPreferenceStore().getDefaultBoolean(HTMLUIPreferenceNames.AUTO_PROPOSE));
		fAutoProposeText.setText(getPreferenceStore().getDefaultString(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE));
	}

	private void performDefaultsForFormattingGroup() {
		// Formatting
		fLineWidthText.setText(getModelPreferences().getDefaultString(HTMLCorePreferenceNames.LINE_WIDTH));
		fSplitMultiAttrs.setSelection(getModelPreferences().getDefaultBoolean(HTMLCorePreferenceNames.SPLIT_MULTI_ATTRS));
		fAlignEndBracket.setSelection(getModelPreferences().getDefaultBoolean(HTMLCorePreferenceNames.ALIGN_END_BRACKET));
		fClearAllBlankLines.setSelection(getModelPreferences().getDefaultBoolean(HTMLCorePreferenceNames.CLEAR_ALL_BLANK_LINES));

		if (HTMLCorePreferenceNames.TAB.equals(getModelPreferences().getDefaultString(HTMLCorePreferenceNames.INDENTATION_CHAR))) {
			fIndentUsingTabs.setSelection(true);
			fIndentUsingSpaces.setSelection(false);
		}
		else {
			fIndentUsingSpaces.setSelection(true);
			fIndentUsingTabs.setSelection(false);
		}
		fIndentationSize.setSelection(getModelPreferences().getDefaultInt(HTMLCorePreferenceNames.INDENTATION_SIZE));
	}

	protected void initializeValues() {
		fTagNameUpper.setSelection(getModelPreferences().getInt(HTMLCorePreferenceNames.TAG_NAME_CASE) == HTMLCorePreferenceNames.UPPER);
		fTagNameLower.setSelection(getModelPreferences().getInt(HTMLCorePreferenceNames.TAG_NAME_CASE) == HTMLCorePreferenceNames.LOWER);
		fAttrNameUpper.setSelection(getModelPreferences().getInt(HTMLCorePreferenceNames.ATTR_NAME_CASE) == HTMLCorePreferenceNames.UPPER);
		fAttrNameLower.setSelection(getModelPreferences().getInt(HTMLCorePreferenceNames.ATTR_NAME_CASE) == HTMLCorePreferenceNames.LOWER);

		initializeValuesForFormattingGroup();
		initializeValuesForContentAssistGroup();
	}

	private void initializeValuesForContentAssistGroup() {
		// Content Assist
		fAutoPropose.setSelection(getPreferenceStore().getBoolean(HTMLUIPreferenceNames.AUTO_PROPOSE));
		fAutoProposeText.setText(getPreferenceStore().getString(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE));
	}

	private void initializeValuesForFormattingGroup() {
		// Formatting
		fLineWidthText.setText(getModelPreferences().getString(HTMLCorePreferenceNames.LINE_WIDTH));
		fSplitMultiAttrs.setSelection(getModelPreferences().getBoolean(HTMLCorePreferenceNames.SPLIT_MULTI_ATTRS));
		fAlignEndBracket.setSelection(getModelPreferences().getBoolean(HTMLCorePreferenceNames.ALIGN_END_BRACKET));
		fClearAllBlankLines.setSelection(getModelPreferences().getBoolean(HTMLCorePreferenceNames.CLEAR_ALL_BLANK_LINES));

		if (HTMLCorePreferenceNames.TAB.equals(getModelPreferences().getString(HTMLCorePreferenceNames.INDENTATION_CHAR))) {
			fIndentUsingTabs.setSelection(true);
			fIndentUsingSpaces.setSelection(false);
		}
		else {
			fIndentUsingSpaces.setSelection(true);
			fIndentUsingTabs.setSelection(false);
		}

		fIndentationSize.setSelection(getModelPreferences().getInt(HTMLCorePreferenceNames.INDENTATION_SIZE));
	}

	protected void storeValues() {
		if (fTagNameUpper.getSelection())
			getModelPreferences().setValue(HTMLCorePreferenceNames.TAG_NAME_CASE, HTMLCorePreferenceNames.UPPER);
		else
			getModelPreferences().setValue(HTMLCorePreferenceNames.TAG_NAME_CASE, HTMLCorePreferenceNames.LOWER);
		if (fAttrNameUpper.getSelection())
			getModelPreferences().setValue(HTMLCorePreferenceNames.ATTR_NAME_CASE, HTMLCorePreferenceNames.UPPER);
		else
			getModelPreferences().setValue(HTMLCorePreferenceNames.ATTR_NAME_CASE, HTMLCorePreferenceNames.LOWER);

		storeValuesForFormattingGroup();
		storeValuesForContentAssistGroup();
	}

	private void storeValuesForContentAssistGroup() {
		// Content Assist
		getPreferenceStore().setValue(HTMLUIPreferenceNames.AUTO_PROPOSE, fAutoPropose.getSelection());
		getPreferenceStore().setValue(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE, fAutoProposeText.getText());
	}

	private void storeValuesForFormattingGroup() {
		// Formatting
		getModelPreferences().setValue(HTMLCorePreferenceNames.LINE_WIDTH, fLineWidthText.getText());
		getModelPreferences().setValue(HTMLCorePreferenceNames.SPLIT_MULTI_ATTRS, fSplitMultiAttrs.getSelection());
		getModelPreferences().setValue(HTMLCorePreferenceNames.ALIGN_END_BRACKET, fAlignEndBracket.getSelection());
		getModelPreferences().setValue(HTMLCorePreferenceNames.CLEAR_ALL_BLANK_LINES, fClearAllBlankLines.getSelection());

		if (fIndentUsingTabs.getSelection()) {
			getModelPreferences().setValue(HTMLCorePreferenceNames.INDENTATION_CHAR, HTMLCorePreferenceNames.TAB);
		}
		else {
			getModelPreferences().setValue(HTMLCorePreferenceNames.INDENTATION_CHAR, HTMLCorePreferenceNames.SPACE);
		}
		getModelPreferences().setValue(HTMLCorePreferenceNames.INDENTATION_SIZE, fIndentationSize.getSelection());
	}

	public boolean performOk() {
		boolean result = super.performOk();

		doSavePreferenceStore();

		return result;
	}

	protected Preferences getModelPreferences() {
		return HTMLCorePlugin.getDefault().getPluginPreferences();
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return HTMLUIPlugin.getDefault().getPreferenceStore();
	}

	private void doSavePreferenceStore() {
		HTMLUIPlugin.getDefault().savePluginPreferences(); // UI
		HTMLCorePlugin.getDefault().savePluginPreferences(); // model
	}

	protected Control createContents(Composite parent) {
		Composite composite = (Composite) super.createContents(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.HTML_PREFWEBX_SOURCE_HELPID);

		createContentsForFormattingGroup(composite);
		createContentsForContentAssistGroup(composite);
		setSize(composite);
		loadPreferences();

		return composite;
	}

	protected void enableValues() {
		if (fAutoPropose != null) {
			if (fAutoPropose.getSelection()) {
				fAutoProposeLabel.setEnabled(true);
				fAutoProposeText.setEnabled(true);
			}
			else {
				fAutoProposeLabel.setEnabled(false);
				fAutoProposeText.setEnabled(false);
			}
		}
	}

	protected void validateValues() {
		boolean isError = false;
		String widthText = null;

		if (fLineWidthText != null) {
			try {
				widthText = fLineWidthText.getText();
				int formattingLineWidth = Integer.parseInt(widthText);
				if ((formattingLineWidth < WIDTH_VALIDATION_LOWER_LIMIT) || (formattingLineWidth > WIDTH_VALIDATION_UPPER_LIMIT)) {
					throw new NumberFormatException();
				}
			}
			catch (NumberFormatException nfexc) {
				setInvalidInputMessage(widthText);
				setValid(false);
				isError = true;
			}
		}

		int indentSize = 0;
		if (fIndentationSize != null) {
			try {
				indentSize = fIndentationSize.getSelection();
				if ((indentSize < MIN_INDENTATION_SIZE) || (indentSize > MAX_INDENTATION_SIZE)) {
					throw new NumberFormatException();
				}
			}
			catch (NumberFormatException nfexc) {
				setInvalidInputMessage(Integer.toString(indentSize));
				setValid(false);
				isError = true;
			}
		}

		if (!isError) {
			setErrorMessage(null);
			setValid(true);
		}
	}
}